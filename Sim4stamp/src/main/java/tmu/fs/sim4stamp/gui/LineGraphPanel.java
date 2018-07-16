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
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import tmu.fs.sim4stamp.gui.util.GraphData;

/**
 * 時系列グラフの表示
 *
 * @author Keiichi Tsumuta
 */
public class LineGraphPanel implements Initializable {

	private final LineChart lineChart;
	private int chartCount = 0;
	private GraphData firstData;

	public LineGraphPanel() {
		CategoryAxis xa = new CategoryAxis();
		NumberAxis ya = new NumberAxis();
		LineChart<String, Number> chart = new LineChart<String, Number>(xa, ya);
		chart.setAnimated(false);
		lineChart = chart;
	}

	public LineGraphPanel(LineChart chart) {
		this.lineChart = chart;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		chartCount = 0;
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
		XYChart.Series series = null;
		if (chartCount == 0) {
			firstData = data;
		}

		// populating the series with data
		GraphData.GhType ghType = data.getGhType();
		if (ghType == GraphData.GhType.DOUBLE) {
			series = new XYChart.Series<String, Double>();
			lineChart.setCreateSymbols(true);
			double[] dVals = data.getDoubleData();
			double[] frDVals = firstData.getDoubleData();
			boolean f = false;
			for (int i = 0; i < dVals.length; i++) {
				String num = Integer.toString(i + 1);
				if (chartCount == 0) {
					series.getData().add(new XYChart.Data(num, dVals[i]));
				} else if (dVals[i] != frDVals[i]) {
					if (i >= 1 && !f) {
						series.getData().add(new XYChart.Data(Integer.toString(i), dVals[i - 1]));
						f = true;
					}
					series.getData().add(new XYChart.Data(num, dVals[i]));
				} else {
					f = false;
				}
			}
		} else if (ghType == GraphData.GhType.INT) {
			series = new XYChart.Series<String, Integer>();
			lineChart.setCreateSymbols(false);
			int[] iVals = data.getIntData();
			int[] frIVals = firstData.getIntData();
			boolean f = false;
			for (int i = 0; i < iVals.length; i++) {
				String num = Integer.toString(i + 1);
				if (chartCount == 0) {
					if (i > 0 && iVals[i - 1] != iVals[i]) {
						series.getData().add(new XYChart.Data(num, iVals[i - 1]));
					}
					series.getData().add(new XYChart.Data(num, iVals[i]));
				} else if (iVals[i] != frIVals[i]) {
					if (i > 0 && iVals[i - 1] != iVals[i]) {
						series.getData().add(new XYChart.Data(num, iVals[i - 1]));
					}
					series.getData().add(new XYChart.Data(num, iVals[i]));
					f = true;
				} else {
					if (f) {
						if (i > 0 && iVals[i - 1] != iVals[i]) {
							series.getData().add(new XYChart.Data(num, iVals[i - 1]));
						}
						series.getData().add(new XYChart.Data(num, iVals[i]));
					}
					f = false;
				}
			}
		} else if (ghType == GraphData.GhType.BOOL) {
			series = new XYChart.Series<String, Integer>();
			lineChart.setCreateSymbols(false);
			boolean[] bVals = data.getBoolData();
			boolean[] frBVals = firstData.getBoolData();
			boolean f = false;
			for (int i = 0; i < bVals.length; i++) {
				String num = Integer.toString(i + 1);
				if (chartCount == 0) {
					if (i > 0 && bVals[i - 1] != bVals[i]) {
						series.getData().add(new XYChart.Data(num, convBoolToInt(bVals[i - 1])));
					}
					series.getData().add(new XYChart.Data(num, convBoolToInt(bVals[i])));
				} else if (bVals[i] != frBVals[i]) {
					if (i > 0 && bVals[i - 1] != bVals[i]) {
						series.getData().add(new XYChart.Data(num, convBoolToInt(bVals[i - 1])));
					}
					series.getData().add(new XYChart.Data(num, convBoolToInt(bVals[i])));
					f = true;
				} else {
					if (f) {
						if (i > 0 && bVals[i - 1] != bVals[i]) {
							series.getData().add(new XYChart.Data(num, convBoolToInt(bVals[i - 1])));
						}
						series.getData().add(new XYChart.Data(num, convBoolToInt(bVals[i])));
					}
					f = false;
				}
			}
		}
		if (seriesName != null) {
			series.setName(seriesName);
		} else {
			lineChart.legendVisibleProperty().set(false);
		}
		lineChart.getData().add(series);
		chartCount++;
	}

	private int convBoolToInt(boolean b) {
		if (b) {
			return 1;
		}
		return 0;
	}

	public void reset() {
		lineChart.getData().removeAll(lineChart.getData().toArray());
		chartCount = 0;
	}

	public void setChartSize(double width, double height) {
		lineChart.setMaxSize(width, height);
		lineChart.setMinSize(width, height);
	}

	public LineChart getChart() {
		return lineChart;
	}

}
