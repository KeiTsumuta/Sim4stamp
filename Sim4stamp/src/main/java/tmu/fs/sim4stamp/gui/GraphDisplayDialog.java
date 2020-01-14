/*
 *   sim4stamp - The simulation tool for STAMP/STPA
 *   Copyright (C) 2018  Keiichi Tsumuta
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tmu.fs.sim4stamp.SimService;
import tmu.fs.sim4stamp.gui.util.GraphData;
import tmu.fs.sim4stamp.model.iop.IOScene;

/**
 * グラフ表示ダイアログ
 *
 * @author Keiichi Tsumuta
 */
public class GraphDisplayDialog implements Initializable {

	private static final String[] GRAPH_LINE_COLORS = ResultPanel.GRAPH_LINE_COLORS;

	@FXML
	private Label graphTitle;

	@FXML
	private AnchorPane displayedGraph;
	private LineGraphPanel linePanel;

	@FXML
	private VBox graphInfoPane2;

	private static String mainTitle;
	private static List<String> subTitles;
	private static List<GraphData> gdata;

	private Stage stage;

	public GraphDisplayDialog() {
		linePanel = new LineGraphPanel();
		linePanel.setGraphLineColors(GRAPH_LINE_COLORS);
		linePanel.setChartSize(600, 300);
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		linePanel.reset();
		final ToggleGroup group = new ToggleGroup();
		graphInfoPane2.getChildren().clear();
		if (subTitles != null) {
			List<IOScene> resultScenes = SimService.getInstance().getIoParamManager().getResultScenes();
			if (resultScenes.size() > 0) {
				String oldDeviationConnParamId = "";
				for (int i = 0; i < subTitles.size(); i++) {
					IOScene ios = resultScenes.get(i);
					String deviationConnParamId = ios.getDeviationConnParamId();
					if (!oldDeviationConnParamId.equals(deviationConnParamId)) {
						FlowPane dfp = new FlowPane();
						RadioButton rbt = new RadioButton(deviationConnParamId);
						rbt.setToggleGroup(group);
						rbt.setUserData(i);
						if (oldDeviationConnParamId.equals("")) {
							rbt.setSelected(true);
						}
						dfp.getChildren().addAll(rbt);
						graphInfoPane2.getChildren().add(dfp);
						oldDeviationConnParamId = deviationConnParamId;
					}
					Label li = new Label();
					li.setText("●");
					li.setTextFill(Color.web(GRAPH_LINE_COLORS[(i) % GRAPH_LINE_COLORS.length]));
					li.setFont(new Font("Arial", 12));
					Label la = new Label();
					String deviation = (i + 1) + ":" + ios.getDeviation().toString();
					la.setText(deviation);
					//la.setTextFill(Color.web(GRAPH_LINE_COLORS[(i - 1) % GRAPH_LINE_COLORS.length]));
					la.setFont(new Font("Arial", 12));
					FlowPane fp = new FlowPane();
					fp.setStyle("-fx-padding: 4.0 2.0 0 8.0;");
					fp.getChildren().addAll(li, la);
					graphInfoPane2.getChildren().add(fp);

					linePanel.addData(i + "：" + subTitles.get(i), gdata.get(i));
				}
				setSelectDevSetting(0);
				group.selectedToggleProperty().addListener(
					(ObservableValue<? extends Toggle> ov, Toggle old_toggle,
						Toggle new_toggle) -> {
						if (group.getSelectedToggle() != null) {
							Integer index = (Integer) (group.getSelectedToggle().getUserData());
							setSelectDevSetting(index);
						}
					});

			}
			graphTitle.textProperty().set(mainTitle);
		} else {
			graphTitle.textProperty().set("");
		}
		displayedGraph.getChildren().add(linePanel.getCanvas());
	}

	private void setSelectDevSetting(int index) {
		List<IOScene> resultScenes = SimService.getInstance().getIoParamManager().getResultScenes();
		if (resultScenes.size() <= 0) {
			return;
		}
		for (int i = 0; i < linePanel.getGraphDataList().size(); i++) {
			linePanel.selectDisplayData(i, false);
		}
		String startDcp = resultScenes.get(index).getDeviationConnParamId();
		linePanel.selectDisplayData(index, true);
		linePanel.setFillIndex(index);
		for (int i = index + 1; i < resultScenes.size(); i++) {
			String dcp = resultScenes.get(i).getDeviationConnParamId();
			if (!dcp.equals(startDcp)) {
				break;
			}
			linePanel.selectDisplayData(i, true);
		}
		linePanel.drawCanvasPanel();
	}

	public void reset(String title) {
		mainTitle = title;
		subTitles = new ArrayList<>();
		gdata = new ArrayList<>();
	}

	public void addData(String title, GraphData data) {
		subTitles.add(title);
		gdata.add(data);
	}

	public void show(ActionEvent event) throws IOException {
		stage = new Stage();
		Parent root = FXMLLoader.load(getClass().getResource("/fxml/graphDisplayDialog.fxml"));
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
	public void okAction(ActionEvent event) {
		((Node) event.getSource()).getScene().getWindow().hide();
	}
}
