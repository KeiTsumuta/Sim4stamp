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
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tmu.fs.sim4stamp.SimService;

/**
 * グラフ表示ダイアログ
 *
 * @author keiichi
 */
public class GraphDisplayDialog implements Initializable {

    @FXML
    private Label graphTitle;

    @FXML
    private LineChart displayedGraph;
    private LineGraphPanel linePanel;

    private static String title;
    private static double[] data;

    private Stage stage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        linePanel = new LineGraphPanel(displayedGraph);
        linePanel.reset();
        if (data != null) {
            linePanel.addData(title, data);
        }
        graphTitle.textProperty().set(title);
    }

    public void setData(String title, double[] data) {
        this.title = title;
        this.data = data;
    }

    public void show(ActionEvent event) throws IOException {
        stage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/graphDisplayDialog.fxml"));
        stage.setScene(new Scene(root));
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
