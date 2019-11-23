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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import tmu.fs.sim4stamp.PanelManager;
import tmu.fs.sim4stamp.SimService;
import tmu.fs.sim4stamp.gui.util.GraphData;
import tmu.fs.sim4stamp.gui.util.MakeResultTable;
import tmu.fs.sim4stamp.model.em.Element;
import tmu.fs.sim4stamp.model.iop.AppendParams;
import tmu.fs.sim4stamp.model.iop.IOParam;
import tmu.fs.sim4stamp.model.iop.IOScene;
import tmu.fs.sim4stamp.model.iop.IOValue;

/**
 * 結果の表示（グラフと表）に用いるデータを編集する。
 *
 * @author Keiichi Tsumuta
 */
public class ResultPanel implements Initializable {

	public static final String[] GRAPH_LINE_COLORS = {"#32cd32", "#ffa500", "#ff0000", "#4d66cc",
		"#b22222", "#0000ff", "#daa520"};

	private static final double CHART_INIT_WIDTH = 400.0;
	private static final double CHART_INIT_HEIGHT = 200.0;

	private final ChoiceBox<String> resultChoice;
	private List<CheckBox> resultGraphs;

	private int graphSize = 0;
	private VBox graphInfoPane;
	private AnchorPane lineChartPane;
	private GridPane resultGraphGrid;
	private LineGraphPanel[] linePanels = new LineGraphPanel[graphSize];
	private String[] initSelectParentIds = new String[graphSize];
	private String[] initSelectIds = new String[graphSize];
	private String[] currentSelectParentIds = new String[graphSize];
	private String[] currentSelectIds = new String[graphSize];
	private boolean[] displaySelects = new boolean[graphSize];

	private int selectResultIndex = -1;

	private final ResultTablePanel resultTable;
	private int gridColumnSizeOld;

	private VBox displaySelectParams;

	public ResultPanel(Control[] controls, VBox graphInfoPane, AnchorPane lineChartPanel, GridPane resultGraphGrid, VBox displaySelectParams) {
		this.resultChoice = (ChoiceBox) controls[0];
		this.graphInfoPane = graphInfoPane;
		this.lineChartPane = lineChartPanel;
		this.resultGraphGrid = resultGraphGrid;
		this.resultTable = new ResultTablePanel((TableView) controls[1]);
		this.displaySelectParams = displaySelectParams;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initGraphSize();
		getResultTable().initialize(location, resources);
		PanelManager.get().setResultTablePanel(getResultTable());
	}

	private void initGraphSize() {
		graphSize = getGraphSize();
		//System.out.println("graph size=" + graphSize);
		int gridColumnSize = SimService.getInstance().getResultGraphColumSize();
		gridColumnSizeOld = gridColumnSize;
		//graphChoiseBoxs = new ChoiceBox[graphSize];
		linePanels = new LineGraphPanel[graphSize];
		initSelectParentIds = new String[graphSize];
		initSelectIds = new String[graphSize];
		currentSelectParentIds = new String[graphSize];
		currentSelectIds = new String[graphSize];
		displaySelects = new boolean[graphSize];
		double chartWidth = CHART_INIT_WIDTH * SimService.getInstance().getResultGraphWidth();
		for (int i = 0; i < graphSize; i++) {
			LineGraphPanel gpanel = new LineGraphPanel();
			gpanel.setGraphLineColors(GRAPH_LINE_COLORS);
			gpanel.setChartSize(chartWidth, CHART_INIT_HEIGHT);
			linePanels[i] = gpanel;
			displaySelects[i] = true;
		}

		// グラフの割り付け
		resultGraphGrid.getChildren().clear();
		for (int i = 0; i < graphSize; i++) {
			BorderPane bp = new BorderPane();
			bp.setCenter(linePanels[i].getCanvas());
			resultGraphGrid.add(bp, i % gridColumnSize, i / gridColumnSize);
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
		if (getGraphSize() != graphSize || gridColumnSizeOld != SimService.getInstance().getResultGraphColumSize()) {
			initGraphSize();
		}
		graphInfoPane.getChildren().clear();
		resultGraphGrid.setStyle("-fx-background-color: #eaf0f0;");
		resetData();
		resultTable.initData();
		setGraphSelections();
		setDisplaySelectionItems();
	}

	private void setDisplaySelectionItems() {
		displaySelectParams.getChildren().clear();

		for (int i = 0; i < graphSize; i++) {
			FlowPane fp = new FlowPane();
			CheckBox cb = new CheckBox(currentSelectParentIds[i] + " : " + currentSelectIds[i]);
			cb.setSelected(displaySelects[i]);
			cb.setStyle("-fx-padding: 8.0;");
			final int index = i;
			cb.setOnAction((ActionEvent) -> selectGraphDisplay(index, cb.isSelected()));
			fp.getChildren().addAll(cb);
			displaySelectParams.getChildren().add(fp);
		}
	}

	private void selectGraphDisplay(int index, boolean select) {
		if (select) {
			displaySelects[index] = true;
		} else {
			displaySelects[index] = false;
		}
		// グラフの割り付け
		int gridColumnSize = SimService.getInstance().getResultGraphColumSize();
		resultGraphGrid.getChildren().clear();
		int count = 0;
		for (int i = 0; i < graphSize; i++) {
			if (!displaySelects[i]) {
				continue;
			}
			BorderPane bp = new BorderPane();
			bp.setCenter(linePanels[i].getCanvas());
			resultGraphGrid.add(bp, count % gridColumnSize, count / gridColumnSize);
			count++;
		}
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
		elements.sort((a, b) -> b.getOrder() - a.getOrder());
		elements.forEach((e) -> {
			Element.EType eType = e.getType();
			AppendParams ap = e.getAppendParams();
			if (!(ap == null)) {
				String nodeId = e.getNodeId();
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

	public void updateGraphColumnSize() {
		Platform.runLater(() -> {
			try {
				int gridColumnSize = SimService.getInstance().getResultGraphColumSize();
				double chartWidth = CHART_INIT_WIDTH * SimService.getInstance().getResultGraphWidth();
				int count = 0;
				for (int i = 0; i < graphSize; i++) {
					if (displaySelects[i]) {
						BorderPane bp = new BorderPane();
						LineGraphPanel gpanel = linePanels[i];
						gpanel.setChartSize(chartWidth, CHART_INIT_HEIGHT);
						bp.setCenter(gpanel.getCanvas());
						resultGraphGrid.add(bp, count % gridColumnSize, count / gridColumnSize);
						count++;
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
	}

	private boolean[] graphWarns = null;

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
		graphWarns = new boolean[resultScenes.size()];
	}

	private void addGraphDisplay(LineGraphPanel graph, String title, String parentId, String id) {
		graphInfoPane.getChildren().clear();
		graph.reset();
		List<IOScene> resultScenes = SimService.getInstance().getIoParamManager().getResultScenes();
		if (resultScenes.size() == 0) {
			return;
		}

		resultGraphs = new ArrayList<>();
		boolean f = false;
		final ToggleGroup group = new ToggleGroup();
		String oldDeviationConnParamId = "";
		for (int i = 0; i < resultScenes.size(); i++) {
			IOScene ios = resultScenes.get(i);
			String deviationConnParamId = ios.getDeviationConnParamId();
			if (!oldDeviationConnParamId.equals(deviationConnParamId)) {
				FlowPane dfp = new FlowPane();
				RadioButton rbt = new RadioButton(deviationConnParamId);
				rbt.setToggleGroup(group);
				rbt.setUserData(i);
				rbt.setStyle("-fx-padding: 4.0 2.0 0 1.0;"); // 上 右 下 左
				if (oldDeviationConnParamId.equals("")) {
					rbt.setSelected(true);
					f = true;
				} else {
					f = false;
				}
				dfp.getChildren().addAll(rbt);
				graphInfoPane.getChildren().add(dfp);
				if (f) {
					graph.setFillIndex(i);
				}
				oldDeviationConnParamId = deviationConnParamId;
			}
			// グラフ描画
			GraphData data = ios.getGraphData(parentId, id);
			data.setDisabled(!f);
			graph.setTitle(id);
			graph.addData(null, data);
			// グラフタイトル描画
			IOValue iov = ios.getIOData(parentId, id);
			int deviationStart = ios.getDeviationStartIndex() + 1;
			iov.makeAttentions(deviationStart);
			boolean isWarn = iov.isUpperWarning() | iov.isUnderWarning();
			graphWarns[i] |= isWarn;
			FlowPane fp = new FlowPane();
			fp.setStyle("-fx-padding: 4.0 2.0 0 8.0;"); // 上 右 下 左
			String deviation = (i + 1) + " : " + ios.getDeviation().toString();
			CheckBox cb = new CheckBox();
			cb.setSelected(f);
			final int index = i;
			cb.setOnAction((ActionEvent) -> selectGraphData(index, cb.isSelected(), false));
			resultGraphs.add(cb);
			Label li = new Label();
			if (graphWarns[i]) {
				li.setText("✖");
			} else {
				li.setText("●");
			}
			li.setTextFill(Color.web(GRAPH_LINE_COLORS[(i) % GRAPH_LINE_COLORS.length]));
			li.setFont(new Font("Arial", 14));
			Label la = new Label();
			la.setText(deviation);
			//la.setTextFill(Color.web(GRAPH_LINE_COLORS[(i - 1) % GRAPH_LINE_COLORS.length]));
			la.setFont(new Font("Arial", 12));
			fp.getChildren().addAll(cb, li, la);
			graphInfoPane.getChildren().add(fp);
		}
		group.selectedToggleProperty().addListener(
			(ObservableValue<? extends Toggle> ov, Toggle old_toggle,
				Toggle new_toggle) -> {
				if (group.getSelectedToggle() != null) {
					Integer index = (Integer) (group.getSelectedToggle().getUserData());
					setSelectDevSetting(index);
				}
			});
	}

	private void setSelectDevSetting(int index) {
		List<IOScene> resultScenes = SimService.getInstance().getIoParamManager().getResultScenes();
		if (resultScenes.size() <= 0) {
			return;
		}
		for (int i = 0; i < resultScenes.size(); i++) {
			selectGraphData(i, false, false);
			resultGraphs.get(i).setSelected(false);
		}
		selectGraphData(index, true, true);
		resultGraphs.get(index).setSelected(true);
		String selId = resultScenes.get(index).getDeviationConnParamId();
		for (int i = index + 1; i < resultScenes.size(); i++) {
			String deviationConnParamId = resultScenes.get(i).getDeviationConnParamId();
			if (selId.equals(deviationConnParamId)) {
				selectGraphData(i, true, false);
				resultGraphs.get(i).setSelected(true);
			}
		}
	}

	private void selectGraphData(int index, boolean select, boolean fill) {
		for (LineGraphPanel lp : linePanels) {
			lp.selectDisplayData(index, select);
			if (fill) {
				lp.setFillIndex(index);
			}
			lp.drawCanvasPanel();
		}
	}

	private void displayResultTable(IOScene ioScene) {
		MakeResultTable mrt = new MakeResultTable(MakeResultTable.LOGIACAL_VALUE_MODE);
		mrt.makeResultTable(ioScene);
		resultTable.setData(mrt.getParentElementIds(), mrt.getDataIds(), mrt.getDataList());
	}

	public void resetData() {
		for (int i = 0; i < graphSize; i++) {
			linePanels[i].reset();
		}
		resultTable.initData();
		makeResultSelect();
	}

	public void displayFirstData() {
		List<IOScene> resultScenes = SimService.getInstance().getIoParamManager().getResultScenes();
		if (resultScenes.size() > 0) {
			displayResultTable(resultScenes.get(0));
			// グラフ
			for (int i = 0; i < graphSize; i++) {
				if (currentSelectParentIds[i] != null && displaySelects[i]) {
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
