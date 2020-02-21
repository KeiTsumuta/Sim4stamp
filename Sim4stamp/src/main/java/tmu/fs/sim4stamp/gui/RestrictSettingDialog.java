/*
 *	 sim4stamp - The simulation tool for STAMP/STPA
 *	 Copyright (C) 2019  Keiichi Tsumuta
 *
 *	 This program is free software: you can redistribute it and/or modify
 *	 it under the terms of the GNU General Public License as published by
 *	 the Free Software Foundation, either version 3 of the License, or
 *	 (at your option) any later version.
 *
 *	 This program is distributed in the hope that it will be useful,
 *	 but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	 GNU General Public License for more details.
 *
 *	 You should have received a copy of the GNU General Public License
 *	 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tmu.fs.sim4stamp.gui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tmu.fs.sim4stamp.PanelManager;
import tmu.fs.sim4stamp.SimService;
import tmu.fs.sim4stamp.model.LogicalValueManager;
import tmu.fs.sim4stamp.model.iop.IOParam;
import tmu.fs.sim4stamp.model.iop.IOValue;
import tmu.fs.sim4stamp.model.iop.SafetyConstraintValue;
import tmu.fs.sim4stamp.model.lv.LogicalValue;

/**
 *
 * @author Keiichi Tsumuta
 */
public class RestrictSettingDialog implements Initializable {

	@FXML
	private CheckBox underCheck;

	@FXML
	private CheckBox upperCheck;

	@FXML
	private TextField unrealvalue;
	@FXML
	private TextField uprealvalue;

	@FXML
	private TextField unintvalue;
	@FXML
	private TextField upintvalue;

	@FXML
	private RadioButton unboolvaltrue;
	@FXML
	private RadioButton unboolvalfalse;

	@FXML
	private RadioButton upboolvaltrue;
	@FXML
	private RadioButton upboolvalfalse;

	@FXML
	private RadioButton under5;
	@FXML
	private RadioButton under4;
	@FXML
	private RadioButton under3;
	@FXML
	private RadioButton under2;
	@FXML
	private RadioButton under1;

	@FXML
	private RadioButton upper5;
	@FXML
	private RadioButton upper4;
	@FXML
	private RadioButton upper3;
	@FXML
	private RadioButton upper2;
	@FXML
	private RadioButton upper1;

	private RadioButton[] underValues;
	private RadioButton[] upperValues;

	private static ConditionPanel.ElementItem elementItem;

	private Stage stage;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		underValues = new RadioButton[]{under1, under2, under3, under4, under5};
		upperValues = new RadioButton[]{upper1, upper2, upper3, upper4, upper5};
		initSetting();
	}

	public void setElementItem(ConditionPanel.ElementItem elem) {
		elementItem = elem;
	}

	private void initSetting() {
		String under = elementItem.getUnderRestrict();
		String upper = elementItem.getUpperRestrict();
		if (under == null || under.equals("*")) {
			underCheck.setSelected(false);
			under = "";
		} else {
			underCheck.setSelected(true);
		}
		if (upper == null || upper.equals("*")) {
			upperCheck.setSelected(false);
			upper = "";
		} else {
			upperCheck.setSelected(true);
		}

		IOParam.ValueType type = elementItem.getType();
		//System.out.println("type:" + type);
		if (type == IOParam.ValueType.REAL) {
			unrealvalue.setDisable(false);
			unrealvalue.setText(under);
			uprealvalue.setDisable(false);
			uprealvalue.setText(upper);
		} else {
			unrealvalue.setDisable(true);
			unrealvalue.setText("");
			uprealvalue.setDisable(true);
			uprealvalue.setText("");
		}
		if (type == IOParam.ValueType.INT) {
			unintvalue.setDisable(false);
			unintvalue.setText(under);
			upintvalue.setDisable(false);
			upintvalue.setText(upper);
		} else {
			unintvalue.setDisable(true);
			unintvalue.setText("");
			upintvalue.setDisable(true);
			upintvalue.setText("");
		}

		if (type == IOParam.ValueType.BOOL) {
			unboolvaltrue.setDisable(false);
			unboolvalfalse.setDisable(false);
			if (under.startsWith("t")) {
				unboolvaltrue.setSelected(true);
				unboolvalfalse.setSelected(false);
			} else {
				unboolvaltrue.setSelected(false);
				unboolvalfalse.setSelected(true);
			}

			upboolvaltrue.setDisable(false);
			upboolvalfalse.setDisable(false);
			if (upper.startsWith("t")) {
				upboolvaltrue.setSelected(true);
				upboolvalfalse.setSelected(false);
			} else {
				upboolvaltrue.setSelected(false);
				upboolvalfalse.setSelected(true);
			}
		} else {
			unboolvaltrue.setDisable(true);
			unboolvaltrue.setSelected(false);
			unboolvalfalse.setDisable(true);
			unboolvalfalse.setSelected(false);

			upboolvaltrue.setDisable(true);
			upboolvaltrue.setSelected(false);
			upboolvalfalse.setDisable(true);
			upboolvalfalse.setSelected(false);
		}

		if (type == IOParam.ValueType.F_VAL_LOGIC) {
			String unitId = elementItem.getIOParam().getUnit();
			LogicalValue lv = SimService.getInstance().getLogicalValueManager().getLogicalValue(unitId);
			IOValue ioValue = elementItem.getIOValue();
			SafetyConstraintValue scUnder = ioValue.getSafetyConstraintUnder();
			double dUnder = scUnder.getDoubleValue();
			int resUnIndex = getRestrictNum(dUnder);
			SafetyConstraintValue scUpper = ioValue.getSafetyConstraintUpper();
			double dUpper = scUpper.getDoubleValue();
			int resUpIndex = getRestrictNum(dUpper);
			String[] units = lv.getValues();
			for (int i = 0; i < underValues.length; i++) {
				underValues[i].setDisable(false);
				underValues[i].setText(Integer.toString(i + 1) + ":" + units[i + 1]);
				if (resUnIndex == (i + 1) && under.length() > 0) {
					underValues[i].setSelected(true);
				} else {
					underValues[i].setSelected(false);
				}
				upperValues[i].setDisable(false);
				upperValues[i].setText(Integer.toString(i + 1) + ":" + units[i + 1]);
				if (resUpIndex == (i + 1) && under.length() > 0) {
					upperValues[i].setSelected(true);
				} else {
					upperValues[i].setSelected(false);
				}
			}
		} else {
			for (int i = 0; i < underValues.length; i++) {
				underValues[i].setDisable(true);
				underValues[i].setText(Integer.toString(i + 1));
				underValues[i].setSelected(false);
				upperValues[i].setDisable(true);
				upperValues[i].setText(Integer.toString(i + 1));
				upperValues[i].setSelected(false);
			}
		}
	}

	private int getRestrictNum(double val) {
		if (val < 1.5) {
			return 1;
		} else if (val < 2.5) {
			return 2;
		} else if (val < 3.5) {
			return 3;
		} else if (val < 4.5) {
			return 4;
		}
		return 5;
	}

	public void show(ActionEvent event) throws IOException {
		stage = new Stage();
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/restrictSettingDialog.fxml"));
		Scene scene = new Scene(root);
		scene.getStylesheets().add("/styles/Styles.css");
		stage.setScene(scene);
		stage.setTitle("sim4stamp");
		stage.initModality(Modality.WINDOW_MODAL);
		SimService s = SimService.getInstance();
		stage.initOwner(s.getStage());
		stage.show();
	}

	public void saveRestrictAction(ActionEvent event) {
		((Node) event.getSource()).getScene().getWindow().hide();
		//System.out.println("save button action!!");
		String underVal = "*";
		String upperVal = "*";
		IOParam.ValueType type = elementItem.getType();
		if (underCheck.isSelected()) {
			if (null != type) {
				switch (type) {
					case REAL:
						underVal = unrealvalue.getText();
						break;
					case INT:
						underVal = unrealvalue.getText();
						break;
					case BOOL:
						underVal = getBoolRadioSelection(unboolvaltrue, unboolvalfalse);
						break;
					case F_VAL_LOGIC:
						underVal = getRadioSelection(underValues);
						break;
					default:
						break;
				}
			}
		}
		if (upperCheck.isSelected()) {
			if (null != type) {
				switch (type) {
					case REAL:
						upperVal = uprealvalue.getText();
						break;
					case INT:
						upperVal = uprealvalue.getText();
						break;
					case BOOL:
						upperVal = getBoolRadioSelection(upboolvaltrue, upboolvalfalse);
						break;
					case F_VAL_LOGIC:
						upperVal = getRadioSelection(upperValues);
						break;
					default:
						break;
				}
			}
		}
		elementItem.setUnderRestrict(underVal);
		elementItem.setUpperRestrict(upperVal);
		SimService.setChanged(true);
		Platform.runLater(() -> {
			try {
				ConditionPanel sp = PanelManager.get().getConditionPanel();
				sp.setItemTableView();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
	}

	private String getBoolRadioSelection(RadioButton trueButton, RadioButton falseButton) {
		String sel = "*";
		if (trueButton.isSelected()) {
			sel = "true";
		} else if (falseButton.isSelected()) {
			sel = "false";
		}
		return sel;
	}

	private String getRadioSelection(RadioButton[] rbs) {
		String sel = "*";
		for (int i = 0; i < rbs.length; i++) {
			if (rbs[i].isSelected()) {
				sel = Integer.toString(i + 1);
				break;
			}
		}
		return sel;
	}

	public void cancelRestrictAction(ActionEvent event) {
		((Node) event.getSource()).getScene().getWindow().hide();
		//System.out.println("cancel button action!!");
	}

}
