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
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tmu.fs.sim4stamp.SimService;
import tmu.fs.sim4stamp.model.LogicalValueManager;
import tmu.fs.sim4stamp.model.lv.LogicalValue;

/**
 * 5値論理値新規追加/編集ダイアログ
 *
 * @author Keiichi Tsumuta
 */
public class LogicalValueSettingDialog implements Initializable {

	@FXML
	private TextField unitname;

	@FXML
	private TextField value0;

	@FXML
	private TextField value1;

	@FXML
	private TextField value2;

	@FXML
	private TextField value3;

	@FXML
	private TextField value4;

	@FXML
	private TextField value5;

	private Stage stage;

	private static LogicalValue logicalValue = null;

	private static LogicalValueListDialog logicalValueListDialog;

	public LogicalValueSettingDialog() {

	}

	public void setEdit(LogicalValue lov) {
		logicalValue = lov;
	}

	public void setUpdatePanel(LogicalValueListDialog logicalValueListDialog) {
		this.logicalValueListDialog = logicalValueListDialog;
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initFields();
	}

	private void initFields() {
		if (logicalValue == null) {
			unitname.setText("");
			unitname.setEditable(true);
			value0.setText("不明");
			value1.setText("");
			value2.setText("");
			value3.setText("");
			value4.setText("");
			value5.setText("");
		} else {
			unitname.setText(logicalValue.getUnitId());
			unitname.setEditable(false);
			String[] vals = logicalValue.getValues();
			value0.setText(vals[0]);
			value1.setText(vals[1]);
			value2.setText(vals[2]);
			value3.setText(vals[3]);
			value4.setText(vals[4]);
			value5.setText(vals[5]);
		}
	}

	public void show(ActionEvent event) throws IOException {
		stage = new Stage();
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/logicalValueSettingDialog.fxml"));
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
	public void saveAction(ActionEvent event) {
		String unit = unitname.getText().trim();
		String[] vals = new String[]{
			value0.getText(), value1.getText(), value2.getText(), value3.getText(), value4.getText(), value5.getText()
		};
		if (unit.length() == 0) {
			showAlert("単位の入力は必須です。");
			return;
		}
		LogicalValueManager lm = SimService.getInstance().getLogicalValueManager();
		LogicalValue clv = lm.getLogicalValue(unit);
		if (logicalValue == null && lm.isExsist(unit)) { // 追加モード & すでに登録済み
			showAlert("「" + unit + "」はすでに登録済みの単位です。");
			return;
		}
		boolean f = true;
		for (String s : vals) {
			if (s == null || s.length() == 0) {
				f = false;
			}
		}
		if (!f) {
			showAlert("値の入力はすべて必須です。");
			return;
		}
		if (logicalValue == null) { // Add
			LogicalValue lv = new LogicalValue(unit);
			lv.setValues(vals);
			lv.setType("2");
			lm.addLogicalValue(unit, lv);
		} else { // Edit
			LogicalValue elv = lm.getLogicalValue(unit);
			elv.setValues(vals);
		}
		logicalValueListDialog.updateData();
		SimService.getInstance().writeInfoFile();
		((Node) event.getSource()).getScene().getWindow().hide();
	}

	private void showAlert(String msg) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("sim4stamp");
		alert.setHeaderText("sim4stamp : 入力エラー");
		alert.setContentText(msg);
		alert.showAndWait();
	}

	@FXML
	public void cancelAction(ActionEvent event) {
		((Node) event.getSource()).getScene().getWindow().hide();
	}
}
