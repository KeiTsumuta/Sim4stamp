/*
 *   sim4stamp - The simulation tool for STAMP/STPA
 *   Copyright (C) 2020  Keiichi Tsumuta
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

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tmu.fs.sim4stamp.SimService;
import tmu.fs.sim4stamp.model.LogicalValueManager;
import tmu.fs.sim4stamp.model.lv.LogicalValue;

/**
 * 5値論理値リスト表示ダイアログ
 *
 * @author Keiichi Tsumuta
 */
public class LogicalValueListDialog implements Initializable {

	@FXML
	private TableView<LvValue> lvList;

	@FXML
	private TableColumn numberColumn;

	@FXML
	private TableColumn unitColumn;

	@FXML
	private TableColumn v0Column;

	@FXML
	private TableColumn v1Column;

	@FXML
	private TableColumn v2Column;

	@FXML
	private TableColumn v3Column;

	@FXML
	private TableColumn v4Column;

	@FXML
	private TableColumn v5Column;

	private Stage stage;

	public LogicalValueListDialog() {

	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		numberColumn.setCellValueFactory(new PropertyValueFactory<LvValue, Integer>("number"));
		numberColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
		unitColumn.setCellValueFactory(new PropertyValueFactory<LvValue, String>("unit"));
		v0Column.setCellValueFactory(new PropertyValueFactory<LvValue, String>("v0"));
		v1Column.setCellValueFactory(new PropertyValueFactory<LvValue, String>("v1"));
		v2Column.setCellValueFactory(new PropertyValueFactory<LvValue, String>("v2"));
		v3Column.setCellValueFactory(new PropertyValueFactory<LvValue, String>("v3"));
		v4Column.setCellValueFactory(new PropertyValueFactory<LvValue, String>("v4"));
		v5Column.setCellValueFactory(new PropertyValueFactory<LvValue, String>("v5"));

		lvList.setRowFactory(tv -> new TableRow<LvValue>() {
			@Override
			protected void updateItem(LvValue item, boolean empty) {
				super.updateItem(item, empty);
				if (item == null || item.type == null) {
					setStyle("");
				} else if (!item.type.equals("0")) {
					setStyle("-fx-background-color: #baffba;");
				} else {
					setStyle("");
				}
			}
		});

		makeList();
	}

	private void makeList() {
		lvList.getItems().clear();
		LogicalValueManager lm = SimService.getInstance().getLogicalValueManager();
		List<String> units = lm.getUnitList();
		for (int i = 0; i < units.size(); i++) {
			String unitId = units.get(i);
			LogicalValue lov = lm.getLogicalValue(unitId);
			String[] nv = lov.getValues();
			String type = lov.getType();
			LvValue lv = new LvValue(i + 1, unitId, type, nv[0], nv[1], nv[2], nv[3], nv[4], nv[5]);
			lvList.getItems().add(lv);
		}
	}

	public void show(ActionEvent event) throws IOException {
		stage = new Stage();
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/logicalValueListDialog.fxml"));
		Scene scene = new Scene(root);
		scene.getStylesheets().add("/styles/Styles.css");
		stage.setScene(scene);
		stage.setTitle("sim4stamp");
		stage.initModality(Modality.WINDOW_MODAL);
		SimService s = SimService.getInstance();
		stage.initOwner(s.getStage());
		stage.show();
	}

	@FXML
	public void hideAction(ActionEvent event) {
		((Node) event.getSource()).getScene().getWindow().hide();
	}

	@FXML
	public void addUnitAction(ActionEvent event) {
		LogicalValueSettingDialog lvSet = new LogicalValueSettingDialog();
		lvSet.setUpdatePanel(this);
		lvSet.setEdit(null);
		try {
			lvSet.show(event);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@FXML
	public void editUnitAction(ActionEvent event) {
		TableViewSelectionModel<LvValue> sm = lvList.getSelectionModel();
		int selectIndex = sm.getSelectedIndex();
		if (selectIndex < 0) {
			return;
		}
		try {
			LogicalValueManager lm = SimService.getInstance().getLogicalValueManager();
			List<String> units = lm.getUnitList();
			String selUnit = units.get(selectIndex);
			LogicalValue lov = lm.getLogicalValue(selUnit);
			String type = lov.getType();
			if (type.equals("0")) {
				showAlert(Alert.AlertType.ERROR, "システムより供給されている単位は変更できません。");
				return;
			}
			LogicalValueSettingDialog lvSet = new LogicalValueSettingDialog();
			lvSet.setUpdatePanel(this);
			lvSet.setEdit(lov);
			lvSet.show(event);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	@FXML
	public void deleteUnitAction(ActionEvent event) {
		TableViewSelectionModel<LvValue> sm = lvList.getSelectionModel();
		int selectIndex = sm.getSelectedIndex();
		if (selectIndex < 0) {
			return;
		}
		try {
			LogicalValueManager lm = SimService.getInstance().getLogicalValueManager();
			List<String> units = lm.getUnitList();
			String selUnit = units.get(selectIndex);
			LogicalValue lov = lm.getLogicalValue(selUnit);
			String type = lov.getType();
			if (type.equals("0")) {
				showAlert(Alert.AlertType.ERROR, "システムより供給されている単位は削除できません。");
				return;
			}
			Optional<ButtonType> result = showAlert(Alert.AlertType.CONFIRMATION, "「" + selUnit + "」を削除しますか？");
			if (result.get() == ButtonType.OK) {
				lm.deleteLogicalValue(selUnit);
				updateData();
				SimService.getInstance().writeInfoFile();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void updateData() {
		Platform.runLater(() -> {
			try {
				makeList();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
	}

	private Optional<ButtonType> showAlert(Alert.AlertType atype, String msg) {
		Alert alert = new Alert(atype);
		alert.setTitle("sim4stamp");
		alert.setHeaderText("sim4stamp : 入力エラー");
		alert.setContentText(msg);
		return alert.showAndWait();
	}

	public class LvValue {

		private final IntegerProperty number;
		private final StringProperty unit;
		private final StringProperty v0;
		private final StringProperty v1;
		private final StringProperty v2;
		private final StringProperty v3;
		private final StringProperty v4;
		private final StringProperty v5;
		private final String type;

		public LvValue(int numId, String unitName, String type, String nv0, String nv1, String nv2, String nv3, String nv4, String nv5) {
			number = new SimpleIntegerProperty(numId);
			unit = new SimpleStringProperty(unitName);
			this.type = type;
			v0 = new SimpleStringProperty(nv0);
			v1 = new SimpleStringProperty(nv1);
			v2 = new SimpleStringProperty(nv2);
			v3 = new SimpleStringProperty(nv3);
			v4 = new SimpleStringProperty(nv4);
			v5 = new SimpleStringProperty(nv5);
		}

		/**
		 * @return the number
		 */
		public IntegerProperty numberProperty() {
			return number;
		}

		/**
		 * @return the unit
		 */
		public StringProperty unitProperty() {
			return unit;
		}

		/**
		 * @return the v0
		 */
		public StringProperty v0Property() {
			return v0;
		}

		/**
		 * @return the v1
		 */
		public StringProperty v1Property() {
			return v1;
		}

		/**
		 * @return the v2
		 */
		public StringProperty v2Property() {
			return v2;
		}

		/**
		 * @return the v3
		 */
		public StringProperty v3Property() {
			return v3;
		}

		/**
		 * @return the v4
		 */
		public StringProperty v4Property() {
			return v4;
		}

		/**
		 * @return the v5
		 */
		public StringProperty v5Property() {
			return v5;
		}

	}

}
