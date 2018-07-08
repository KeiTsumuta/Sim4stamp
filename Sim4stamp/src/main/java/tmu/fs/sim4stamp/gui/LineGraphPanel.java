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
import tmu.fs.sim4stamp.gui.util.GraphData;

/**
 * 時系列グラフの表示
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

	public void setWidth(double w) {
		lineChart.setMaxWidth(w);
	}

	/**
	 * 表示データの追加
	 */
	public void addData(String seriesName, GraphData data) {
		XYChart.Series series = new XYChart.Series();
		if (seriesName != null) {
			series.setName(seriesName);
		} else {
			lineChart.legendVisibleProperty().set(false);
		}
		// populating the series with data
		GraphData.GhType ghType = data.getGhType();
		if (ghType == GraphData.GhType.DOUBLE) {
			lineChart.setCreateSymbols(true);
			double[] dVals = data.getDoubleData();
			for (int i = 0; i < dVals.length; i++) {
				series.getData().add(new XYChart.Data(i + 1, dVals[i]));
			}
		} else if (ghType == GraphData.GhType.INT) {
			lineChart.setCreateSymbols(false);
			int[] iVals = data.getIntData();
			if (iVals.length > 0) {
				int iOld = iVals[0];
				series.getData().add(new XYChart.Data(1, iOld));
				for (int i = 1; i < iVals.length; i++) {
					if (iVals[i] == iOld) {
						series.getData().add(new XYChart.Data(i + 1, iVals[i]));
					} else {
						series.getData().add(new XYChart.Data(i + 1, iOld));
						series.getData().add(new XYChart.Data(i + 1, iVals[i]));
					}
					iOld = iVals[i];
				}
			}
		} else if (ghType == GraphData.GhType.BOOL) {
			lineChart.setCreateSymbols(false);
			boolean[] bVals = data.getBoolData();
			if (bVals.length > 0) {
				boolean old = bVals[0];
				series.getData().add(new XYChart.Data(1, convBoolToInt(old)));
				for (int i = 1; i < bVals.length; i++) {
					if (bVals[i] == old) {
						series.getData().add(new XYChart.Data(i + 1, convBoolToInt(bVals[i])));
					} else {
						series.getData().add(new XYChart.Data(i + 1, convBoolToInt(old)));
						series.getData().add(new XYChart.Data(i + 1, convBoolToInt(bVals[i])));
					}
					old = bVals[i];
				}
			}
		}
		lineChart.getData().add(series);
	}

	private int convBoolToInt(boolean b) {
		if (b) {
			return 1;
		}
		return 0;
	}

	public void reset() {
		lineChart.getData().removeAll(lineChart.getData().toArray());
	}

	public void setChartSize(double width, double height) {
		lineChart.setMaxSize(width, height);
		lineChart.setMinSize(width, height);
	}

}
