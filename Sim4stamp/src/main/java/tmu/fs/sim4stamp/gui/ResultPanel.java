/*
 *   sim4stamp - The simulation tool for STAMP/STPA
 *   Copyright (C) 2017  Keiichi Tsumuta
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tmu.fs.sim4stamp.gui;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import tmu.fs.sim4stamp.PanelManager;
import tmu.fs.sim4stamp.SimService;
import tmu.fs.sim4stamp.gui.util.GraphData;
import tmu.fs.sim4stamp.model.IOParamManager;
import tmu.fs.sim4stamp.model.em.Element;
import tmu.fs.sim4stamp.model.iop.AppendParams;
import tmu.fs.sim4stamp.model.iop.IOParam;
import tmu.fs.sim4stamp.model.iop.IOScene;

/**
 * 結果の表示（グラフと表）に用いるデータを編集する。
 *
 * @author Keiichi Tsumuta
 */
public class ResultPanel implements Initializable {

	private static final Logger log = Logger.getLogger(ResultPanel.class.getPackage().getName());

	private static final String[] GRAPH_LINE_COLORS = {"#32cd32", "#ffa500", "#ff0000", "#4d66cc",
		"#b22222", "#0000ff", "#daa520", "#40e0d0"};

	private static final double CHART_INIT_WIDTH = 400.0;
	private static final double CHART_INIT_HEIGHT = 200.0;
	private static final DecimalFormat D_FORMAT = new DecimalFormat("#0.00");

	private final ChoiceBox<String> resultChoice;

	private int graphSize = 0;
	private VBox graphInfoPane;
	private AnchorPane lineChartPane;
	private GridPane resultGraphGrid;
	private LineChart[] lineCharts = new LineChart[graphSize];
	private LineGraphPanel[] linePanels = new LineGraphPanel[graphSize];
	private String initSelectParentIds[] = new String[graphSize];
	private String initSelectIds[] = new String[graphSize];
	private String currentSelectParentIds[] = new String[graphSize];
	private String currentSelectIds[] = new String[graphSize];

	private int selectResultIndex = -1;
	private double chartWidth = CHART_INIT_WIDTH;

	private final ResultTablePanel resultTable;

	public ResultPanel(Control[] controls, VBox graphInfoPane, AnchorPane lineChartPanel, GridPane resultGraphGrid) {
		this.resultChoice = (ChoiceBox) controls[0];
		this.graphInfoPane = graphInfoPane;
		this.lineChartPane = lineChartPanel;
		this.resultGraphGrid = resultGraphGrid;
		this.resultTable = new ResultTablePanel((TableView) controls[1]);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initGraphSize();
		getResultTable().initialize(location, resources);
		PanelManager.get().setResultTablePanel(getResultTable());
	}

	private void initGraphSize() {
		graphSize = getGraphSize();
		System.out.println("graph size=" + graphSize);
		resultGraphGrid.getChildren().clear();

		//graphChoiseBoxs = new ChoiceBox[graphSize];
		lineCharts = new LineChart[graphSize];
		linePanels = new LineGraphPanel[graphSize];
		initSelectParentIds = new String[graphSize];
		initSelectIds = new String[graphSize];
		currentSelectParentIds = new String[graphSize];
		currentSelectIds = new String[graphSize];
		for (int i = 0; i < graphSize; i++) {
			BorderPane bp = new BorderPane();
			//graphChoiseBoxs[i] = new ChoiceBox();
			//bp.setTop(graphChoiseBoxs[i]);
			NumberAxis xa = new NumberAxis();
			NumberAxis ya = new NumberAxis();
			LineChart chart = new LineChart(xa, ya);
			chart.setAnimated(false);
			bp.setCenter(chart);
			lineCharts[i] = chart;
			linePanels[i] = new LineGraphPanel(chart);
			linePanels[i].setChartSize(chartWidth, CHART_INIT_HEIGHT);
			resultGraphGrid.add(bp, i % 2, i / 2);
		}
	}

	private int getGraphSize() {
		int count = 0;
		List<Element> elements = SimService.getInstance().getElementManger().getElements();
		for (Element e : elements) {
			AppendParams ap = e.getAppendParams();
			if (!(ap == null)) {
				List<IOParam> ios = ap.getParams();
				for (IOParam ip : ios) {
					if (ip.getParamType() == AppendParams.ParamType.Element) {
						count++;
					}
				}
			}
		}
		return count;
	}

	public void initDisplay() {
		if (getGraphSize() != graphSize) {
			initGraphSize();
		}
		graphInfoPane.getChildren().clear();
		resultGraphGrid.setStyle("-fx-background-color: #eaf0f0;");
		resetData();
		resultTable.initData();
		setGraphSelections();
	}

	private void setGraphSelections() {
		for (int i = 0; i < graphSize; i++) {
			initSelectParentIds[i] = null;
			initSelectIds[i] = null;
		}

		selectResultIndex = -1;
		ObservableList<String> rss = FXCollections.observableArrayList();
		resultChoice.setItems(rss);
		resultChoice.getSelectionModel().selectedItemProperty()
				.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
					if (newValue == null) {
						return;
					}
					String[] tk = newValue.split(" ");
					if (tk.length > 1) {
						selectResultIndex = getInt(tk[0]);
						// System.out.println("sel index:" + selectResultIndex);
						if (selectResultIndex > 0) {
							List<IOScene> resultScenes = SimService.getInstance().getIoParamManager().getResultScenes();
							displayResultTable(resultScenes.get(selectResultIndex - 1));
						}
					}
				});
		//
		ObservableList<String> eis = FXCollections.observableArrayList();
		List<Element> elements = SimService.getInstance().getElementManger().getElements();
		elements.forEach((e) -> {
			Element.EType eType = e.getType();
			AppendParams ap = e.getAppendParams();
			if (!(ap == null)) {
				String nodeId = e.getNodeId();
				List<String> colChildren = new ArrayList<>();
				List<IOParam> ios = ap.getParams();
				for (IOParam ip : ios) {
					if (ip.getParamType() == AppendParams.ParamType.Element) {
						eis.add(ip.getId());
						for (int i = 0; i < graphSize; i++) {
							if (initSelectIds[i] == null) {
								if (i == 0 && eType == Element.EType.INJECTOR) {
									initSelectParentIds[i] = nodeId;
									initSelectIds[i] = ip.getId();
								} else if (i > 0) {
									initSelectParentIds[i] = nodeId;
									initSelectIds[i] = ip.getId();
								}
								break;
							}
						}
					}
				}
			}
		});

		for (int i = 0; i < graphSize; i++) {
			currentSelectParentIds[i] = initSelectParentIds[i];
			currentSelectIds[i] = initSelectIds[i];
		}
	}

	private int getInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (Exception ex) {

		}
		return 0;
	}

	public void resultDisplay() {
		// 結果データのプルダウンリスト
		makeResultSelect();
		// グラフ
		for (int i = 0; i < graphSize; i++) {
			if (currentSelectParentIds[i] != null) {
				//lineCharts[i].setPrefSize(gw, gh);
				addGraphDisplay(linePanels[i], currentSelectIds[i], currentSelectParentIds[i], currentSelectIds[i]);
			}
		}
		// テーブル
		IOScene executeScene = SimService.getInstance().getIoParamManager().getExceuteScene();
		displayResultTable(executeScene);
	}

	private void makeResultSelect() {
		ObservableList<String> rss = FXCollections.observableArrayList();
		List<IOScene> resultScenes = SimService.getInstance().getIoParamManager().getResultScenes();
		String sel = null;
		int i = 1;
		for (IOScene ioScene : resultScenes) {
			sel = i + " : " + ioScene.getDeviation().toString();
			rss.add(sel);
			i++;
		}
		resultChoice.setItems(rss);
		if (sel != null) {
			resultChoice.getSelectionModel().select(sel);
		}
	}

	private void addGraphDisplay(LineGraphPanel graph, String title, String parentId, String id) {
		List<IOScene> resultScenes = SimService.getInstance().getIoParamManager().getResultScenes();
		if (resultScenes.size() == 0) {
			return;
		}
		graphInfoPane.getChildren().clear();
		graph.reset();
		int i = 1;
		for (IOScene ios : resultScenes) {
			if (i > graphSize) {
				break;
			}
			if (currentSelectParentIds[i - 1] != null) {
				FlowPane fp = new FlowPane();
				String deviation = i + ":" + ios.getDeviation().toString();
				Label li = new Label();
				li.setText("●");
				li.setTextFill(Color.web(GRAPH_LINE_COLORS[(i - 1) % GRAPH_LINE_COLORS.length]));
				li.setFont(new Font("Arial", 15));
				Label la = new Label();
				la.setText(deviation);
				//la.setTextFill(Color.web(GRAPH_LINE_COLORS[(i - 1) % GRAPH_LINE_COLORS.length]));
				la.setFont(new Font("Arial", 15));
				fp.getChildren().addAll(li, la);
				graphInfoPane.getChildren().add(fp);
				GraphData data = ios.getGraphData(parentId, id);
				graph.setTitle(id);
				graph.addData(null, data);
			}
			i++;
		}
	}

	private void displayResultTable(IOScene ioScene) {
		IOParamManager iom = SimService.getInstance().getIoParamManager();
		List<String> colParents = new ArrayList<>();
		List<List<String>> colTitles = new ArrayList<>();
		List<String[]> colData = new ArrayList<>();
		List<String> elemIds = iom.getNodeIds();
		for (String elemId : elemIds) {
			colParents.add(elemId);
			List<String> colChildren = new ArrayList<>();
			List<IOParam> iops = iom.getParamMap().get(elemId);
			for (IOParam iop : iops) {
				colChildren.add(iop.getId());
				IOParam.ValueType type = iop.getType();
				String[] arr = new String[ioScene.getSize()];
				if (type == IOParam.ValueType.REAL) {
					double[] dData = ioScene.getData(elemId, iop.getId());
					for (int i = 0; i < dData.length; i++) {
						arr[i] = D_FORMAT.format(dData[i]);
					}
				} else if (type == IOParam.ValueType.INT) {
					int[] iData = ioScene.getIntData(elemId, iop.getId());
					for (int i = 0; i < iData.length; i++) {
						arr[i] = Integer.toString(iData[i]);
					}
				} else if (type == IOParam.ValueType.BOOL) {
					boolean[] bData = ioScene.getBoolData(elemId, iop.getId());
					for (int i = 0; i < bData.length; i++) {
						if (bData[i]) {
							arr[i] = "true";
						} else {
							arr[i] = "false";
						}
					}
				}
				colData.add(arr);
			}
			colTitles.add(colChildren);
		}
		resultTable.setData(colParents, colTitles, colData);
	}

	public void resetData() {
		for (int i = 0; i < graphSize; i++) {
			linePanels[i].reset();
		}
	}

	public void displayFirstData() {
		List<IOScene> resultScenes = SimService.getInstance().getIoParamManager().getResultScenes();
		if (resultScenes.size() > 0) {
			displayResultTable(resultScenes.get(0));
			// グラフ
			for (int i = 0; i < graphSize; i++) {
				if (currentSelectParentIds[i] != null) {
					addGraphDisplay(linePanels[i], currentSelectIds[i], currentSelectParentIds[i], currentSelectIds[i]);
				}
			}
		}
		makeResultSelect();
	}

	/**
	 * @return the resultTable
	 */
	public ResultTablePanel getResultTable() {
		return resultTable;
	}

}
