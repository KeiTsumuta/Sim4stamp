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
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.json.JSONObject;
import tmu.fs.sim4stamp.MainApp;
import tmu.fs.sim4stamp.PanelManager;
import tmu.fs.sim4stamp.Sim4stampVersion;
import tmu.fs.sim4stamp.SimService;
import tmu.fs.sim4stamp.export.ExportResults;
import tmu.fs.sim4stamp.export.UCAHintsExport;
import tmu.fs.sim4stamp.state.CommandLineExecute;
import tmu.fs.sim4stamp.vdm.VdmCodeMaker;

/**
 *
 * @author Keiichi Tsumuta
 */
public class FXMLController implements Initializable {

	private static final String SP = System.getProperty("file.separator");

	@FXML
	private Label label;

	@FXML
	private Canvas modelCanvas;

	@FXML
	private Canvas deviationCanvas;

	@FXML
	private VBox graphInfoPane;

	@FXML
	private AnchorPane lineChartPanel;

	@FXML
	private GridPane resultGraphGrid;

	@FXML
	private TableView itemParamTable;
	// @FXML
	// private TableView connectorParamTable;
	@FXML
	private TextField sceneTitle;
	@FXML
	private TextField simInitSeqSize;
	@FXML
	private Button conditionSetButton;
	@FXML
	private TableView initDataTable;

	// @FXML
	// private ComboBox deviationSelectBox;
	@FXML
	private Button executeAllDeviationButton;

	@FXML
	private TextField deviationStartIndex;

	@FXML
	private ComboBox deviationSelectionByType;

	@FXML
	private ComboBox executeTypeSelection;

	@FXML
	private ChoiceBox resultChoice;

	@FXML
	private VBox displaySelectParams;

	@FXML
	private TableView resultTable;

	@FXML
	private CheckBox modelDitailDisplayCheckbox;

	@FXML
	private CheckBox connectorJointDisplayCheckbox;

	@FXML
	private Label selectedProjectName;

	@FXML
	private Button clearButton;
	@FXML
	private TextArea executeLog;

	@FXML
	private Button executeSimButton;
	@FXML
	private Button stepExecuteSimButton;
	@FXML
	private Button executeStopButton;
	@FXML
	private Button loopDisplayButton;
	@FXML
	private Button loopDisplayDownButton;
	@FXML
	private Button loopDisplayUpButton;
	@FXML
	private TextField loopDisplayCount;

	@FXML
	private void handleButtonAction(ActionEvent event) {

	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		PanelManager pm = PanelManager.get();

		Control[] modelPanelControls = new Control[]{modelDitailDisplayCheckbox, connectorJointDisplayCheckbox};
		ModelPanel modelPanel = new ModelPanel(modelCanvas, modelPanelControls);
		modelPanel.initialize(url, rb);
		pm.setModelPanel(modelPanel);

		Control[] conditonPanelControls = new Control[]{sceneTitle, itemParamTable, null, simInitSeqSize, null,
			deviationStartIndex, conditionSetButton, initDataTable};
		ConditionPanel conditionPanel = new ConditionPanel(conditonPanelControls);
		conditionPanel.initialize(url, rb);
		pm.setConditionPanel(conditionPanel);

		Control[] devMapPanelControls = new Control[]{deviationSelectionByType,
			executeTypeSelection, executeSimButton, stepExecuteSimButton, executeStopButton,
			loopDisplayButton, loopDisplayDownButton, loopDisplayUpButton, loopDisplayCount, executeAllDeviationButton};
		DeviationMapPanel devMapPanel = new DeviationMapPanel(deviationCanvas, devMapPanelControls);
		devMapPanel.initialize(url, rb);
		pm.setDeviationMapPanel(devMapPanel);

		ExecuteLogPanel executeLogPanel = new ExecuteLogPanel(clearButton, executeLog);
		executeLogPanel.initialize(url, rb);
		pm.setExecuteLogPanel(executeLogPanel);

		Control[] resultPanelControls = new Control[]{resultChoice, resultTable};
		//LineChart[] lineCharts = new LineChart[]{lineChart1, lineChart2, lineChart3, lineChart4};
		ResultPanel resultPanel = new ResultPanel(resultPanelControls, graphInfoPane, lineChartPanel, resultGraphGrid, displaySelectParams);
		resultPanel.initialize(url, rb);
		pm.setResutPanel(resultPanel);

		pm.setSelectLabel(selectedProjectName);
		pm.initDisplay();
	}

	@FXML
	public void sim4StampSettingsAction(ActionEvent event) {
		//log.info("file settings!!");
		SystemParamPanel pp = new SystemParamPanel();
		try {
			pp.show(event);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@FXML
	public void projectSettingsAction(ActionEvent event) {
		//log.info("project settings!! " + selectedProjectName);
		ProjectSelectionPanel ps = new ProjectSelectionPanel();
		try {
			ps.show(event);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@FXML
	public void deviationPanelAction(ActionEvent event) {
		//log.info("deviation setting panel!!");
		DeviationSettingPanel dsp = new DeviationSettingPanel();
		try {
			dsp.show(event);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@FXML
	public void reportExportAction(ActionEvent event) {
		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setTitle("レポート出力データフォルダ選択");
		SimService simService = SimService.getInstance();
		String currentPj = simService.getCurrentProjectId();
		if (currentPj != null) {
			String dir = simService.getProjectHome(currentPj);
			dirChooser.setInitialDirectory(new File(dir));
		} else {
			// 初期ディレクトリをホームにする。
			dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		}
		// フォルダ選択
		File dir = dirChooser.showDialog(MainApp.getStage());
		if (dir != null) {
			ExportResults er = new ExportResults();
			er.exportFiles(dir);
		}
	}

	@FXML
	public void ucaHintsExportAction(ActionEvent event) {
		DirectoryChooser dirChooser = new DirectoryChooser();
		dirChooser.setTitle("UCAヒント出力データフォルダ選択");
		SimService simService = SimService.getInstance();
		String currentPj = simService.getCurrentProjectId();
		if (currentPj != null) {
			String dir = simService.getProjectHome(currentPj);
			dirChooser.setInitialDirectory(new File(dir));
		} else {
			// 初期ディレクトリをホームにする。
			dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		}
		// フォルダ選択
		File dir = dirChooser.showDialog(MainApp.getStage());
		if (dir != null) {
			UCAHintsExport uca = new UCAHintsExport();
			uca.exportFile(dir);
		}
	}

	@FXML
	public void conditionSaveActin(ActionEvent evnt) {
		//log.info("condition save!!");
		SimService simService = SimService.getInstance();
		String currentPj = simService.getCurrentProjectId();
		if (currentPj != null) {
			String dir = simService.getProjectHome(currentPj);
			String file = simService.getProjectParams(currentPj);
			File f = new File(dir + SP + file);
			simService.saveProjectParams(f, false);
			//log.log(Level.INFO, "save file:{0}", f.getAbsolutePath());
		}
	}

	@FXML
	public void appExitAction(ActionEvent event) {
		systemExit();
	}

	public static boolean systemExit() {

		if (SimService.isChanged()) {
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.OK, ButtonType.CANCEL);
			alert.setTitle("終了警告");
			alert.getDialogPane().setHeaderText("プロジェクトデータの変更あり");
			alert.getDialogPane().setContentText("保存しないと設定データが消失します。\nそれでも終了しますか？");
			ButtonType bt = alert.showAndWait().orElse(ButtonType.CANCEL);
			if (bt == ButtonType.CANCEL) {
				return false;
			}
		}
		SimService.getInstance().close();
		//log.info("Exit sim4stamp");
		Platform.exit();
		System.exit(0);
		return true;
	}

	@FXML
	public void initResultAction(ActionEvent event) {
		//log.info("result init");
		PanelManager.get().resetResult();
	}

	@FXML
	public void initSecondResultAction(ActionEvent event) {
		//log.info("result init 2th");
		PanelManager.get().resetSecondResult();
	}

	@FXML
	public void vdmppCreateAction(ActionEvent event) {
		//log.info("VDM++ source create action");
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.OK, ButtonType.CANCEL);
		alert.setTitle("コード生成");
		alert.getDialogPane().setHeaderText("VDM++コード生成");
		alert.getDialogPane().setContentText("カレントのプロジェクト下でVDM++コードを新しく生成します。");
		ButtonType bt = alert.showAndWait().orElse(ButtonType.CANCEL);
		if (bt == ButtonType.OK) {
			VdmCodeMaker vcm = new VdmCodeMaker();
			vcm.make();
		}
	}

	@FXML
	public void modelInfoUpdateAction(ActionEvent event) {
		//log.info("modelInfoUpdateAction:--");
		SimService ss = SimService.getInstance();
		String pjJson = ss.toJson();
		// System.out.println("JSON:\n"+pjJson);
		JSONObject job = new JSONObject(pjJson);
		ss.parseProjectParams(job);
		PanelManager.get().initDisplay();
		Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
		alert.setTitle("データ更新");
		alert.getDialogPane().setHeaderText("データ更新");
		alert.getDialogPane().setContentText("モデル図の変更を条件設定データ等に反映しました。");
		alert.showAndWait();
	}

	@FXML
	public void overtureExecuteAction(ActionEvent event) {
		try {
			DeviationMapPanel dm = PanelManager.get().getDeviationMapPanel();
			CommandLineExecute ce = new CommandLineExecute(dm);
			ce.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@FXML
	public void overtureAllExecuteAction(ActionEvent event) {
		try {
			DeviationMapPanel dm = PanelManager.get().getDeviationMapPanel();
			CommandLineExecute ce = new CommandLineExecute(dm);
			ce.allStart();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@FXML
	public void aboutAction(ActionEvent event) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("sim4stamp");
		alert.setHeaderText("sim4stamp : The simulation tool for STAMP/STPA");
		alert.setContentText(
			"Version : " + Sim4stampVersion.version + "\n\n" + "License : GNU General Public License version 3");
		alert.showAndWait();
	}
}
