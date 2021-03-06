/*
 *   sim4stamp - The simulation tool for STAMP/STPA
 *   Copyright (C) 2016  Keiichi Tsumuta
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
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tmu.fs.sim4stamp.SimService;
import tmu.fs.sim4stamp.gui.util.GuiUtil;
import tmu.fs.sim4stamp.model.iop.IOScene;

/**
 *
 * @author Keiichi Tsumuta
 */
public class DeviationSettingPanel implements Initializable {

	@FXML
	private TextField providingMoreSetting;

	@FXML
	private TextField pentaProvidingMoreSetting;

	@FXML
	private TextField providingLessConst;

	@FXML
	private TextField pentaProvidingLessConst;

	@FXML
	private TextField tooEarlyConst;

	@FXML
	private TextField tooLateConst;

	private Stage stage;

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		providingMoreSetting.setText(Double.toString(IOScene.getProvidingMoreParam()));
		providingMoreSetting.setTextFormatter(GuiUtil.getDecimalTextFormater());

		pentaProvidingMoreSetting.setText(Double.toString(IOScene.getPentaProvidingMoreParam()));
		pentaProvidingMoreSetting.setTextFormatter(GuiUtil.getDecimalTextFormater());

		providingLessConst.setText(Double.toString(IOScene.getProvidingLessParam()));
		providingLessConst.setTextFormatter(GuiUtil.getDecimalTextFormater());

		pentaProvidingLessConst.setText(Double.toString(IOScene.getPentaProvidingLessParam()));
		pentaProvidingLessConst.setTextFormatter(GuiUtil.getDecimalTextFormater());

		tooEarlyConst.setText(Integer.toString(IOScene.getDeviationTooEarly()));
		tooEarlyConst.setTextFormatter(GuiUtil.getIntTextFormater());

		tooLateConst.setText(Integer.toString(IOScene.getDeviationTooLate()));
		tooLateConst.setTextFormatter(GuiUtil.getIntTextFormater());
	}

	public void show(ActionEvent event) throws IOException {
		stage = new Stage();
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/deviationSetting.fxml"));
		stage.setScene(new Scene(root));
		stage.setTitle("sim4stamp");
		stage.initModality(Modality.WINDOW_MODAL);
		SimService s = SimService.getInstance();
		stage.initOwner(s.getStage());
		stage.show();
	}

	@FXML
	public void saveDeviationSettingAction(ActionEvent event) {
		IOScene.setProvidingMoreParam(getDouble(providingMoreSetting.getText()));
		IOScene.setPentaProvidingMoreParam(getDouble(pentaProvidingMoreSetting.getText()));
		IOScene.setProvidingLessParam(getDouble(providingLessConst.getText()));
		IOScene.setPentaProvidingLessParam(getDouble(pentaProvidingLessConst.getText()));
		IOScene.setDeviationTooEarly(getInt(tooEarlyConst.getText()));
		IOScene.setDeviationTooLate(getInt(tooLateConst.getText()));

		SimService.getInstance().writeInfoFile();
	}

	private double getDouble(String s) {
		try {
			return Double.parseDouble(s);
		} catch (Exception ex) {

		}
		return 0.0;
	}

	private int getInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (Exception ex) {

		}
		return 0;
	}

	@FXML
	public void cancelAction(ActionEvent event) {
		((Node) event.getSource()).getScene().getWindow().hide();
	}

}
