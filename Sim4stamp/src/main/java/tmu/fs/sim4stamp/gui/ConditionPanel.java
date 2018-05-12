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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import tmu.fs.sim4stamp.PanelManager;
import tmu.fs.sim4stamp.SimService;
import tmu.fs.sim4stamp.gui.util.GuiUtil;
import tmu.fs.sim4stamp.model.IOParamManager;
import tmu.fs.sim4stamp.model.em.Element;
import tmu.fs.sim4stamp.model.iop.AppendParams;
import tmu.fs.sim4stamp.util.Deviation;
import tmu.fs.sim4stamp.model.iop.IOParam;
import tmu.fs.sim4stamp.model.iop.IOScene;
import tmu.fs.sim4stamp.model.iop.IOValue;

/**
 * シミュレーションに関する計算パラメータの条件設定を行うパネル
 *
 * @author Keiichi Tsumuta
 */
public class ConditionPanel implements Initializable {

	private static final DecimalFormat D_FORMAT = new DecimalFormat("#0.00");
	private static final Deviation[] CONNECTOR_DEVIATIONS = { //
		Deviation.NORMAL, //
		Deviation.NOT_PROVIDING, //
		Deviation.PROVIDING_MORE, //
		Deviation.PROVIDING_LESS, //
		Deviation.TOO_LATE, //
		Deviation.STOPPING_TOO_SOON, //
		Deviation.APPLYING_TOO_LONG}; //

	private final TextField sceneTitle;
	private final TableView simItemParamView; // 構成要素パラメータテーブル

	// private final TableView simConnectorView; // コネクタパラメータテーブル
	private Map<String, RadioButton> radioMap;

	private final TextField simInitSeqSize; // 時系列数
	private final TableView initDataTable; // 初期値設定テーブル
	private final Button conditionSetButton; // 設定ボタン（条件データ初期値テーブル反映）
	// private final ComboBox deviationSelectBox; // 偏差投入種別
	private final TextField deviationStartIndex; // 偏差投入開始インデックス

	private Deviation selectedConnectorDeviation = Deviation.NORMAL;

	public ConditionPanel(Control[] controls) {
		this.sceneTitle = (TextField) controls[0];
		this.simItemParamView = (TableView) controls[1];
		// this.simConnectorView = (TableView) controls[2];
		this.simInitSeqSize = (TextField) controls[3];
		// this.deviationSelectBox = (ComboBox) controls[4];
		this.deviationStartIndex = (TextField) controls[5];
		this.conditionSetButton = (Button) controls[6];
		this.initDataTable = (TableView) controls[7];
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		radioMap = new HashMap<>();
		IOScene ioScene = SimService.getInstance().getIoParamManager().getCurrentScene();
		sceneTitle.setText(ioScene.getScene());
		simInitSeqSize.textProperty().set(Integer.toString(ioScene.getSize()));
		simInitSeqSize.setTextFormatter(GuiUtil.getIntTextFormater());
		setItemTableView();
		// setConnectorTableView();
		this.conditionSetButton.setOnAction((ActionEvent event) -> {
			setInitTable();
			PanelManager.get().updateCondition();
		});
		ObservableList<Deviation> list = FXCollections.observableArrayList(CONNECTOR_DEVIATIONS);

		deviationStartIndex.textProperty().set(Integer.toString(ioScene.getDeviationStartIndex()));
		deviationStartIndex.setTextFormatter(GuiUtil.getIntTextFormater());
	}

	private void setItemTableView() {
		TableColumn<ElementItem, String> elemColumn = new TableColumn("構成要素");
		elemColumn.setCellValueFactory(new PropertyValueFactory<>("elementId"));

		TableColumn<ElementItem, String> paramColumn = new TableColumn("パラメータ");
		paramColumn.setCellValueFactory(new PropertyValueFactory<>("paramId"));

		TableColumn<ElementItem, CheckBox> selColumn = new TableColumn("初期設定");
		selColumn.setStyle("-fx-alignment: CENTER;");
		selColumn.setCellValueFactory((TableColumn.CellDataFeatures<ElementItem, CheckBox> arg0) -> {
			ElementItem elem = arg0.getValue();
			CheckBox checkBox = new CheckBox();
			checkBox.selectedProperty().setValue(elem.isSelected());
			checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
					elem.setSelected(new_val);
				}
			});
			return new SimpleObjectProperty<CheckBox>(checkBox);
		});
		simItemParamView.getColumns().setAll(elemColumn, paramColumn, selColumn);
	}

	public void setInitDisplay() {
		IOScene ioScene = SimService.getInstance().getIoParamManager().getCurrentScene();
		sceneTitle.textProperty().set(ioScene.getScene());
		simInitSeqSize.textProperty().set(Integer.toString(ioScene.getSize()));
		setItemTableView();
		// setConnectorTableView();

		setItemDisplay();
		setInitTable();
	}

	public void setItemDisplay() {
		Platform.runLater(() -> {
			try {
				List<Element.EType> etypes = new ArrayList<>();
				List<String> colParents = new ArrayList<>();
				List<List<IOParam>> colTitles = new ArrayList<>();

				List<Element> elements = SimService.getInstance().getElementManger().getElements();
				elements.sort((a, b) -> b.getOrder() - a.getOrder());
				elements.forEach((elem) -> {
					AppendParams ap = elem.getAppendParams();
					if (!(ap == null)) {
						String nodeId = elem.getNodeId();
						List<IOParam> colChildren = new ArrayList<>();
						etypes.add(elem.getType());
						colParents.add(nodeId);
						List<IOParam> ios = ap.getParams();
						for (IOParam ip : ios) {
							if (ip.getParamType() == AppendParams.ParamType.Element) {
								colChildren.add(ip);
							}
						}
						colTitles.add(colChildren);
					}
				});
				setItems(etypes, colParents, colTitles);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
	}

	private void setItems(List<Element.EType> etypes, List<String> elements, List<List<IOParam>> elementParams) {
		ObservableList<ElementItem> eis = FXCollections.observableArrayList();
		int n = 0;
		for (int i = 0; i < elements.size(); i++) {
			String ename = elements.get(i);
			for (int k = 0; k < elementParams.get(i).size(); k++) {
				boolean sel = false;
				if (etypes.get(i) == Element.EType.INJECTOR) {
					sel = true;
				}
				ElementItem ei = new ElementItem(ename, elementParams.get(i).get(k), sel);
				eis.add(ei);
			}
			n++;
		}
		simItemParamView.setItems(eis);
	}

	public void setInitTable() {
		Platform.runLater(() -> {
			try {
				List<ElementItem> initList = new ArrayList<>();
				ObservableList<ElementItem> eis = simItemParamView.getItems();
				eis.stream().filter((ei) -> (ei.isSelected())).forEachOrdered((ei) -> {
					initList.add(ei);
				});
				eis.stream().filter((ei) -> (!ei.isSelected())).forEachOrdered((ei) -> {
					clearInitValues(ei);
				});

				List<String> hs = new ArrayList<>();
				hs.add("No."); // No列
				for (ElementItem e : initList) { // 初期値設置列
					hs.add(e.getElementId() + "_" + e.getParamId());
				}
				ObservableList<String> headers = FXCollections.observableArrayList();
				headers.setAll(hs);

				int colIndex = 0;
				List<TableColumn<ObservableList, String>> columnList = new ArrayList<>();
				for (String header : headers) {
					final int idx = colIndex;
					if (idx == 0) { // No列
						TableColumn<ObservableList, String> column = new TableColumn<>(header);
						column.setStyle("-fx-alignment: CENTER-RIGHT;");
						column.setCellValueFactory((CellDataFeatures<ObservableList, String> param) -> {
							return new SimpleStringProperty(param.getValue().get(idx).toString());
						});
						columnList.add(column);
					} else { // 初期値設置列
						TableColumn<ObservableList, String> column = new TableColumn<>(header);
						column.setStyle("-fx-alignment: CENTER-RIGHT;");
						column.setCellValueFactory((CellDataFeatures<ObservableList, String> param) -> {
							return new SimpleStringProperty(param.getValue().get(idx).toString());
						});
						column.setCellFactory(TextFieldTableCell.<ObservableList>forTableColumn());
						column.setOnEditCommit(new EventHandler<CellEditEvent<ObservableList, String>>() {
							@Override
							public void handle(CellEditEvent<ObservableList, String> event) {
								int row = event.getTablePosition().getRow();
								String tableId = event.getTableColumn().getText();
								String[] idel = tableId.split("_");
								IOScene ioScene = SimService.getInstance().getIoParamManager().getCurrentScene();
								IOValue ioValue = ioScene.getIOData(idel[0], idel[1]);
								ObservableList<String> list = event.getTableView().getItems().get(row);
								IOParam.ValueType type = ioValue.getType();
								if (type == IOParam.ValueType.REAL) {
									double[] d = ioScene.getData(idel[0], idel[1]);
									d[row] = getDouble(event.getNewValue());
									list.set(idx, D_FORMAT.format(d[row]));
								} else if (type == IOParam.ValueType.INT) {
									int[] n = ioScene.getIntData(idel[0], idel[1]);
									n[row] = getInt(event.getNewValue());
									list.set(idx, Integer.toString(n[row]));
								} else if (type == IOParam.ValueType.BOOL) {
									boolean[] b = ioScene.getBoolData(idel[0], idel[1]);
									b[row] = getBoolean(event.getNewValue());
									if (b[row]) {
										list.set(idx, "true");
									} else {
										list.set(idx, "false");
									}
								}
								if ((row + 1) < ioValue.getSize()) {
									initDataTable.getSelectionModel().clearAndSelect(row + 1, event.getTableColumn());
								}
								initDataTable.requestFocus();
							}
						});
						column.setEditable(true);
						columnList.add(column);
					}
					colIndex++;
				}
				IOParamManager iom = SimService.getInstance().getIoParamManager();
				IOScene ioScene = iom.getCurrentScene();
				ioScene.dataInitSelection();
				ioScene.setScene(sceneTitle.getText());
				// sceneTitle.setText(ioScene.getScene());
				initDataTable.getColumns().setAll(columnList);
				int startIndex = getInt(deviationStartIndex.getText());
				ioScene.setDeviationStartIndex(startIndex);
				int devIdx = Math.max(startIndex - 1, 0);
				ObservableList<ObservableList> initDataVals = FXCollections.<ObservableList>observableArrayList();
				int size = getInt(simInitSeqSize.getText());
				ioScene.setSize(size);
				for (int i = 0; i < size; i++) {
					ObservableList<String> rows = FXCollections.<String>observableArrayList();
					// No列の行データ設定
					StringBuilder sb = new StringBuilder();
					if (i == devIdx) {
						sb.append("# ").append(i + 1);
					} else {
						sb.append(i + 1);
					}
					rows.add(sb.toString());
					// 初期値設置列の行データ設定
					for (int k = 0; k < headers.size() - 1; k++) {
						ElementItem ei = initList.get(k);
						if (i == 0) {
							ioScene.initDataSelect(ei.getElementId(), ei.getParamId());
						}
						IOValue ioValue = ioScene.getIOData(ei.getElementId(), ei.getParamId());
						IOParam.ValueType type = ioValue.getType();
						if (type == IOParam.ValueType.REAL) {
							double[] d = ioScene.getData(ei.getElementId(), ei.getParamId());
							rows.add(D_FORMAT.format(d[i]));
						} else if (type == IOParam.ValueType.INT) {
							int[] n = ioScene.getIntData(ei.getElementId(), ei.getParamId());
							rows.add(Integer.toString(n[i]));
						} else if (type == IOParam.ValueType.BOOL) {
							boolean[] b = ioScene.getBoolData(ei.getElementId(), ei.getParamId());
							if (b[i]) {
								rows.add("true");
							} else {
								rows.add("false");
							}
						}
					}
					initDataVals.add(rows);
				}
				initDataTable.getSelectionModel().setCellSelectionEnabled(true);
				initDataTable.setItems(initDataVals);
				initDataTable.setEditable(true);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
	}

	private int getInt(String s) {
		int i = 0;
		try {
			if (s != null) {
				i = Integer.parseInt(s);
			}
		} catch (Exception ex) {
		}
		return i;
	}

	private double getDouble(String val) {
		double d = 0.0;
		try {
			d = Double.parseDouble(val);
		} catch (Exception ex) {
		}
		return d;
	}

	private boolean getBoolean(String value) {
		boolean b = false;
		try {
			if (value != null) {
				if (value.startsWith("t")) {
					b = true;
				} else if (value.equals("1")) {
					b = true;
				}
			}
		} catch (Exception ex) {
		}
		return b;
	}

	private void clearInitValues(ElementItem ei) {
		IOParamManager iom = SimService.getInstance().getIoParamManager();
		IOScene ioScene = iom.getCurrentScene();
		IOValue ioValue = ioScene.getIOData(ei.getElementId(), ei.getParamId());
		IOParam.ValueType type = ioValue.getType();
		if (type == IOParam.ValueType.REAL) {
			double[] d = ioScene.getData(ei.getElementId(), ei.getParamId());
			for (int i = 0; i < d.length; i++) {
				d[i] = 0.0;
			}
		} else if (type == IOParam.ValueType.INT) {
			int[] n = ioScene.getIntData(ei.getElementId(), ei.getParamId());
			for (int i = 0; i < n.length; i++) {
				n[i] = 0;
			}
		} else if (type == IOParam.ValueType.BOOL) {
			boolean[] b = ioScene.getBoolData(ei.getElementId(), ei.getParamId());
			for (int i = 0; i < b.length; i++) {
				b[i] = false;
			}
		}
	}

	public class ElementItem {

		private SimpleStringProperty elementId;
		private SimpleStringProperty paramId;
		private SimpleBooleanProperty selected;
		private IOParam ioparam;

		public ElementItem(String elemId, IOParam param, boolean select) {
			this.elementId = new SimpleStringProperty(elemId);
			this.ioparam = param;
			this.paramId = new SimpleStringProperty(param.getId());
			this.selected = new SimpleBooleanProperty(select);
			this.ioparam.setInitData(select);
		}

		public IOParam getIOParam() {
			return ioparam;
		}

		/**
		 * @return the elementId
		 */
		public String getElementId() {
			return elementId.get();
		}

		public SimpleStringProperty getElementIdProperty() {
			return elementId;
		}

		/**
		 * @return the paramId
		 */
		public String getParamId() {
			return paramId.get();
		}

		public SimpleStringProperty getParamIdProperty() {
			return paramId;
		}

		/**
		 * @return the select
		 */
		public boolean isSelected() {
			return selected.get();
		}

		public SimpleBooleanProperty getSelectedProperty() {
			return selected;
		}

		/**
		 * @param select the select to set
		 */
		public void setSelected(boolean select) {
			this.selected.set(select);
			ioparam.setInitData(select);
		}
	}

	public class ConnectorItem {

		private SimpleStringProperty fromId;
		private SimpleStringProperty toId;
		private SimpleStringProperty paramId;
		private SimpleBooleanProperty selected;

		public ConnectorItem(String fromId, String toId, String paramId, boolean select) {
			this.fromId = new SimpleStringProperty(fromId);
			this.toId = new SimpleStringProperty(toId);
			this.paramId = new SimpleStringProperty(paramId);
			this.selected = new SimpleBooleanProperty(select);
		}

		/**
		 * @return the fromId
		 */
		public SimpleStringProperty getFromIdProperty() {
			return fromId;
		}

		public String getFromId() {
			return fromId.get();
		}

		/**
		 * @param fromId the fromId to set
		 */
		public void setFromId(SimpleStringProperty fromId) {
			this.fromId = fromId;
		}

		/**
		 * @return the toId
		 */
		public SimpleStringProperty getToIdProperty() {
			return toId;
		}

		public String getToId() {
			return toId.get();
		}

		/**
		 * @param toId the toId to set
		 */
		public void setToId(SimpleStringProperty toId) {
			this.toId = toId;
		}

		public boolean isSelected() {
			return selected.get();
		}

		/**
		 * @return the paramId
		 */
		public String getParamId() {
			return paramId.get();
		}

		public SimpleStringProperty getParamIdProperty() {
			return paramId;
		}

		/**
		 * @return the selected
		 */
		public SimpleBooleanProperty getSelectedProperty() {
			return selected;
		}

		public void setSelected(boolean selected) {
			this.selected.set(selected);
		}

		/**
		 * @param selected the selected to set
		 */
		public void setSelected(SimpleBooleanProperty selected) {
			this.selected = selected;
		}

		public String toString() {
			return fromId.get() + "," + toId.get() + "," + paramId.get();
		}

	}

}
