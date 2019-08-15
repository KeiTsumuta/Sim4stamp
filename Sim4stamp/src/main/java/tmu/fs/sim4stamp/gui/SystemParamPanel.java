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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tmu.fs.sim4stamp.PanelManager;
import tmu.fs.sim4stamp.SimService;

/**
 * システムパラメータ設定ダイアログ
 *
 * @author Keiichi Tsumuta
 */
public class SystemParamPanel implements Initializable {

	@FXML
	private TextField overtureHome;

	@FXML
	private TextField commandTool;

	@FXML
	private TextField resultGraphColumSize;

	@FXML
	private Slider resultGraphWidth;

	private Stage stage;

	public SystemParamPanel() {
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		SimService s = SimService.getInstance();
		overtureHome.setText(s.getOvertureHome());
		commandTool.setText(s.getOvertureCommandLine());
		int gsize = s.getResultGraphColumSize();
		resultGraphColumSize.setText(Integer.toString(gsize));
		resultGraphWidth.setValue(s.getResultGraphWidth());
		resultGraphWidth.valueProperty().addListener((
				ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
			SimService.getInstance().setResultGraphWidth(new_val.doubleValue());
		});
	}

	public void show(ActionEvent event) throws IOException {
		stage = new Stage();
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/systemparampanel.fxml"));
		stage.setScene(new Scene(root));
		stage.setTitle("sim4stamp");
		stage.initModality(Modality.WINDOW_MODAL);
		SimService s = SimService.getInstance();
		stage.initOwner(s.getStage());
		stage.show();
	}

	@FXML
	public void saveAction(ActionEvent event) {
		// System.out.println("SystemParamPanel saveAction !!");
		SimService s = SimService.getInstance();
		String size = resultGraphColumSize.getText();
		try {
			int isize = Integer.parseInt(size);
			if (isize <= 0) {
				isize = 1;
			}
			s.setResultGraphColumSize(isize);
		} catch (Exception ex) {
		}
		s.writeInfoFile();
		PanelManager.get().getResutPanel().updateGraphColumnSize();
		closeDialog(event);
	}

	@FXML
	public void cancelAction(ActionEvent event) {
		// System.out.println("cancelAction !!");
		SimService s = SimService.getInstance();
		s.readInfoFile();
		// s.readProjectFile();
		closeDialog(event);
	}

	public void closeDialog(ActionEvent event) {
		((Node) event.getSource()).getScene().getWindow().hide();
	}

	@FXML
	public void overtureRefAction(ActionEvent event) {
		final DirectoryChooser fc = new DirectoryChooser();
		fc.setTitle("Overture Home選択");
		File dir = fc.showDialog(stage);
		if (dir != null) {
			SimService s = SimService.getInstance();
			s.setOvertureHome(dir.getAbsolutePath());
			overtureHome.textProperty().set(s.getOvertureHome());
		}

	}

	@FXML
	public void commandToolRefAction(ActionEvent event) {
		// System.out.println("commandToolRefAction !!");
		final FileChooser fc = new FileChooser();
		fc.setTitle("Overture commandlinetool選択");
		fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("jar", "*.jar"));
		File jarFile = fc.showOpenDialog(stage);
		if (jarFile != null) {
			SimService s = SimService.getInstance();
			s.setOvertureCommandLine(jarFile.getName());
			commandTool.textProperty().set(s.getOvertureCommandLine());
		}
	}

}
