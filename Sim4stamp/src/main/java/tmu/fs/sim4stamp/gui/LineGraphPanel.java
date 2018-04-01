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

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

/**
 *
 * @author Keiichi Tsumuta
 */
public class LineGraphPanel implements Initializable {

    private final LineChart lineChart;

    public LineGraphPanel(LineChart chart) {
        this.lineChart = chart;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setTitle(String title) {
        lineChart.setTitle(title);
    }

    public void setData(String seriesName, double[] data) {
        //defining a series
        XYChart.Series series = new XYChart.Series();
        series.setName(seriesName);
        //populating the series with data
        for (int i = 0; i < data.length; i++) {
            series.getData().add(new XYChart.Data(i + 1, data[i]));
        }
        lineChart.getData().setAll(series);
    }

    public void addData(String seriesName, double[] data) {
        XYChart.Series series = new XYChart.Series();
        series.setName(seriesName);
        //populating the series with data
        for (int i = 0; i < data.length; i++) {
            series.getData().add(new XYChart.Data(i + 1, data[i]));
        }
        lineChart.getData().add(series);
    }

    public void reset() {
        lineChart.getData().removeAll(lineChart.getData().toArray());
    }

}
