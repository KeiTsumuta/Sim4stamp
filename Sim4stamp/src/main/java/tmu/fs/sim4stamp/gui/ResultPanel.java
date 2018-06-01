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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.TableView;
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
	private static final int GRAPH_SIZE = 4;
	private static final DecimalFormat D_FORMAT = new DecimalFormat("#0.00");

	private final ChoiceBox<String> resultChoice;

	private final ChoiceBox<String>[] graphChoiseBoxs = new ChoiceBox[GRAPH_SIZE];
	private final LineChart[] lineCharts = new LineChart[GRAPH_SIZE];
	private final LineGraphPanel[] linePanels = new LineGraphPanel[GRAPH_SIZE];
	private final String initSelectParentIds[] = new String[GRAPH_SIZE];
	private final String initSelectIds[] = new String[GRAPH_SIZE];
	private final String currentSelectParentIds[] = new String[GRAPH_SIZE];
	private final String currentSelectIds[] = new String[GRAPH_SIZE];

	private int selectResultIndex = -1;

	private final ResultTablePanel resultTable;

	public ResultPanel(Control[] controls, LineChart[] lineCharts) {
		this.resultChoice = (ChoiceBox) controls[0];
		graphChoiseBoxs[0] = (ChoiceBox) controls[1];
		graphChoiseBoxs[1] = (ChoiceBox) controls[2];
		graphChoiseBoxs[2] = (ChoiceBox) controls[3];
		graphChoiseBoxs[3] = (ChoiceBox) controls[4];
		this.lineCharts[0] = lineCharts[0];
		this.lineCharts[1] = lineCharts[1];
		this.lineCharts[2] = lineCharts[2];
		this.lineCharts[3] = lineCharts[3];
		this.resultTable = new ResultTablePanel((TableView) controls[5]);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		for (int i = 0; i < GRAPH_SIZE; i++) {
			linePanels[i] = new LineGraphPanel(lineCharts[i]);
		}

		getResultTable().initialize(location, resources);
		PanelManager.get().setResultTablePanel(getResultTable());
	}

	public void initDisplay() {
		resetData();
		resultTable.initData();
		setGraphSelections();
	}

	private void setGraphSelections() {
		for (int i = 0; i < GRAPH_SIZE; i++) {
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
		List<Element> elments = SimService.getInstance().getElementManger().getElements();
		elments.forEach((e) -> {
			Element.EType eType = e.getType();
			AppendParams ap = e.getAppendParams();
			if (!(ap == null)) {
				String nodeId = e.getNodeId();
				List<String> colChildren = new ArrayList<>();
				List<IOParam> ios = ap.getParams();
				for (IOParam ip : ios) {
					if (ip.getParamType() == AppendParams.ParamType.Element) {
						eis.add(ip.getId());
						for (int i = 0; i < GRAPH_SIZE; i++) {
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

		for (int i = 0; i < GRAPH_SIZE; i++) {
			graphChoiseBoxs[i].setItems(eis);
			if (initSelectIds[i] != null) {
				graphChoiseBoxs[i].getSelectionModel().select(initSelectIds[i]);
			}
			final int idx = i;
			graphChoiseBoxs[i].getSelectionModel().selectedItemProperty()
					.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
						// System.out.println(newValue);
						IOParamManager iom = SimService.getInstance().getIoParamManager();
						List<String> elemIds = iom.getNodeIds();
						for (String elemId : elemIds) {
							String parentId = elemId;
							List<IOParam> iops = iom.getParamMap().get(elemId);
							for (IOParam iop : iops) {
								String id = iop.getId();
								if (id.equals(newValue)) {
									linePanels[idx].reset();
									addGraphDisplay(linePanels[idx], id, parentId, id);
									currentSelectParentIds[idx] = parentId;
									currentSelectIds[idx] = id;
									return;
								}
							}
						}
					});
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
		for (int i = 0; i < GRAPH_SIZE; i++) {
			if (currentSelectParentIds[i] != null) {
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
		graph.reset();
		int i = 1;
		for (IOScene ios : resultScenes) {
			if (i > GRAPH_SIZE) {
				break;
			}
			if (currentSelectParentIds[i - 1] != null) {
				GraphData data = ios.getGraphData(parentId, id);
				graph.addData(i + ":" + ios.getDeviation().toString(), data);
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
		for (int i = 0; i < GRAPH_SIZE; i++) {
			linePanels[i].reset();
		}
	}

	public void displayFirstData() {
		List<IOScene> resultScenes = SimService.getInstance().getIoParamManager().getResultScenes();
		if (resultScenes.size() > 0) {
			displayResultTable(resultScenes.get(0));
			// グラフ
			for (int i = 0; i < GRAPH_SIZE; i++) {
				if (currentSelectParentIds[i] != null) {
					addGraphDisplay(linePanels[i], currentSelectIds[i], currentSelectParentIds[i], currentSelectIds[i]);
				}
			}
		}
		makeResultSelect();
	}

	/**
	 * @return the linePanel1
	 */
	public LineGraphPanel getLinePanel1() {
		return linePanels[0];
	}

	/**
	 * @return the linePanel2
	 */
	public LineGraphPanel getLinePanel2() {
		return linePanels[1];
	}

	public LineGraphPanel getLinePanel3() {
		return linePanels[2];
	}

	public LineGraphPanel getLinePanel4() {
		return linePanels[3];
	}

	/**
	 * @return the resultTable
	 */
	public ResultTablePanel getResultTable() {
		return resultTable;
	}

}
