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
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tmu.fs.sim4stamp.PanelManager;
import tmu.fs.sim4stamp.SimService;

/**
 *
 * @author Keiichi Tsumuta
 */
public class ProjectSelectionPanel implements Initializable {

	private static final String SP = System.getProperty("file.separator");
	private static final String NO_PROJECT = "未選択";
	private static final String DEFAULT_PARAM_NAME = "param.json";

	@FXML
	private ComboBox projectSelectBox;

	@FXML
	private TextField selectProject;

	@FXML
	private TextField selectPjHome;

	@FXML
	private TextField selectParam;

	@FXML
	private TextField createProjectId;

	@FXML
	private TextField createPjHome;

	@FXML
	private TextField createPjParam;

	private Stage stage;
	private String selectedProjectId = null;

	public ProjectSelectionPanel() {
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		setProjectBoxInfos();

		createProjectId.setText("");
		createPjHome.setText("");
		createPjParam.setText(DEFAULT_PARAM_NAME);
		setCurrentEditale(false);
	}

	private void setProjectBoxInfos() {
		SimService s = SimService.getInstance();
		List<String> projects = s.getProjects();
		String cpj = s.getCurrentProjectId();
		projectSelectBox.getItems().setAll(NO_PROJECT);
		for (String pj : projects) {
			projectSelectBox.getItems().add(pj);
			if (cpj != null && cpj.equals(pj)) {
				selectedProjectId = pj;
				selectProject.textProperty().set(pj);
				selectPjHome.textProperty().set(s.getProjectHome(pj));
				selectParam.textProperty().set(s.getProjectParams(pj));
			}
		}
		if (selectedProjectId != null) {
			projectSelectBox.setValue(selectedProjectId);
		} else {
			projectSelectBox.setValue(NO_PROJECT);
		}
	}

	public void show(ActionEvent event) throws IOException {
		stage = new Stage();
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/projectparampanel.fxml"));
		stage.setScene(new Scene(root));
		stage.setTitle("sim4stamp");
		stage.initModality(Modality.WINDOW_MODAL);
		SimService s = SimService.getInstance();
		stage.initOwner(s.getStage());
		stage.show();
	}

	public void setCurrentEditale(boolean b) {
		selectProject.setEditable(b);
		selectPjHome.setEditable(b);
		selectParam.setEditable(b);
		if (b) {
			selectProject.setStyle("-fx-text-fill: black;");
			selectPjHome.setStyle("-fx-text-fill: black;");
			selectParam.setStyle("-fx-text-fill: black;");
		} else {
			selectProject.setStyle("-fx-text-fill: gray;");
			selectPjHome.setStyle("-fx-text-fill: gray;");
			selectParam.setStyle("-fx-text-fill: gray;");
		}
	}

	@FXML
	public void currentProjectSelectAction(ActionEvent event) {
		// System.out.println("currentProjectSelectAction !!");
		SimService ss = SimService.getInstance();
		ss.writeInfoFile(); // システム設定ファイルの書込み
		// プロジェクト毎のパラメータ定義ファイルの読み込み
		ss.readProjectFile(ss.getCurrentProjectId());
		// 表示の初期化
		PanelManager.get().initDisplay();
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
	public void projectSelectBoxChangeAction(ActionEvent event) {
		selectedProjectId = (String) projectSelectBox.getValue();
		// System.out.println("projectSelectAction !! :" + selectedProjectId);
		SimService ss = SimService.getInstance();
		selectProject.textProperty().set(selectedProjectId);
		selectPjHome.textProperty().set(ss.getProjectHome(selectedProjectId));
		selectParam.textProperty().set(ss.getProjectParams(selectedProjectId));
		ss.setCurrentProjectId(selectedProjectId);
	}

	@FXML
	public void createProjectAction(ActionEvent event) {
		createProject(true);
		// 表示の初期化
		PanelManager.get().initDisplay();
		closeDialog(event);
	}

	private void createProject(boolean init) {
		SimService ss = SimService.getInstance();
		String createPjId = createProjectId.getText();
		String crPjHome = createPjHome.getText();
		String crPjParam = createPjParam.getText();
		if (!isPjIdExists(createPjId)) {
			if (createPjId.length() > 0 && crPjHome.length() > 0 && crPjParam.length() > 0) {
				ss.setCurrentProjectId(createPjId);
				ss.addProject(createPjId, crPjHome, crPjParam);
				selectedProjectId = createPjId;
				setProjectBoxInfos();
				ss.writeInfoFile();
				String dir = ss.getProjectHome(createPjId);
				String file = ss.getProjectParams(createPjId);
				File f = new File(dir + SP + file);
				ss.saveProjectParams(f, init);
				Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
				alert.setTitle("新規設定");
				alert.getDialogPane().setHeaderText("新規設定");
				alert.getDialogPane().setContentText("新規設定しました。");
				alert.showAndWait();
			} else {
				Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.OK);
				alert.setTitle("パラメータ追加");
				alert.getDialogPane().setHeaderText("エラー");
				alert.getDialogPane().setContentText("未入力データがあります。");
				alert.showAndWait();
			}
		} else {
			Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.OK);
			alert.setTitle("パラメータ追加");
			alert.getDialogPane().setHeaderText("エラー");
			alert.getDialogPane().setContentText("プロジェクト名が重複しています。");
			alert.showAndWait();
		}

	}

	@FXML
	public void copyCreateAction(ActionEvent event) {
		createProject(false);
		// 表示の初期化
		PanelManager.get().initDisplay();
		closeDialog(event);
	}

	@FXML
	public void projectHomeAction(ActionEvent event) {
		final DirectoryChooser fc = new DirectoryChooser();
		fc.setTitle("Project Home選択");
		File dir = fc.showDialog(stage);
		if (dir != null) {
			String dirPath = dir.getAbsolutePath();
			selectedProjectId = dir.getName();

			createProjectId.textProperty().set(dir.getName());
			createPjHome.textProperty().set(dirPath);
		}
	}

	@FXML
	public void setSelectedProjectAction(ActionEvent event) {
		// System.out.println("setSelectedProjectAction !!");
		SimService ss = SimService.getInstance();
		String selPjId = selectProject.getText();

		String selHome = selectPjHome.getText();
		String selParam = selectParam.getText();
		if (!isPjIdExists(selPjId)) {
			if (selPjId.length() > 0 && selHome.length() > 0 && selParam.length() > 0) {
				ss.setCurrentProjectId(selPjId);
				ss.addProject(selPjId, selHome, selParam);
				selectedProjectId = selPjId;
			} else {
				Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.OK);
				alert.setTitle("パラメータ追加");
				alert.getDialogPane().setHeaderText("エラー");
				alert.getDialogPane().setContentText("未入力データがあります。");
				alert.showAndWait();
			}
		} else {
			Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.OK);
			alert.setTitle("パラメータ追加");
			alert.getDialogPane().setHeaderText("エラー");
			alert.getDialogPane().setContentText("プロジェクト名が重複しています。");
			alert.showAndWait();
		}
	}

	private boolean isPjIdExists(String pjId) {
		for (String ipjId : SimService.getInstance().getProjects()) {
			if (ipjId.equals(pjId)) {
				return true;
			}
		}
		return false;
	}

	@FXML
	public void deleteSelectedProjectAction(ActionEvent event) {
		// System.out.println("deleteSelectedProjectAction !!");
		if (selectedProjectId != null && selectedProjectId.length() > 0) {
			SimService ss = SimService.getInstance();
			ss.deleteProject(selectedProjectId);
			ss.writeInfoFile();
			ss.setCurrentProjectId(null);
			ss.initCurrentProject();
			projectSelectBox.getItems().removeAll(selectedProjectId);
			selectedProjectId = null;
			selectProject.textProperty().set("");
			selectPjHome.textProperty().set("");
			selectParam.textProperty().set("");
			// 画面更新
			PanelManager.get().initDisplay();
		}
	}
}
