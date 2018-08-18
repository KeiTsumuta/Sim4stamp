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
import javafx.geometry.Pos;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import tmu.fs.sim4stamp.gui.util.GraphData;

/**
 * 時系列グラフの表示
 *
 * @author Keiichi Tsumuta
 */
public class LineGraphPanel implements Initializable {

	private NumberAxis ya;
	private final LineChart lineChart;
	private int chartCount = 0;
	private GraphData firstData;
	private AreaChart<String, Number> areaChart;
	private StackPane stpane = null;

	public LineGraphPanel() {
		CategoryAxis xa = new CategoryAxis();
		ya = new NumberAxis();

		//areaChart = new AreaChart<String, Number>(xa, ya);
		//areaChart.setAnimated(false);
		lineChart = new LineChart<String, Number>(xa, ya);
		lineChart.setAnimated(false);

		stpane = new StackPane();
		//stpane.getChildren().addAll(areaChart, lineChart);
		stpane.getChildren().addAll(lineChart);
		stpane.setAlignment(Pos.BOTTOM_LEFT);
	}

	public LineGraphPanel(LineChart chart) {
		this.lineChart = chart;
		ya = (NumberAxis) chart.getYAxis();
		stpane = null;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		chartCount = 0;
	}

	public void setTitle(String title) {
		lineChart.setTitle(title);
		if (areaChart != null) {
			areaChart.setTitle(" ");
		}
	}

	public void setWidth(double w) {
		lineChart.setMaxWidth(w);
		if (areaChart != null) {
			areaChart.setMaxWidth(w);
		}
	}

	/**
	 * 表示データの追加
	 */
	public void addData(String seriesName, GraphData data) {
		XYChart.Series series = null;
		if (chartCount == 0) {
			firstData = data;
			if (areaChart != null) {
				//addAreaChart();
			}
		}

		// populating the aSeries with data
		GraphData.GhType ghType = data.getGhType();
		if (null != ghType) {
			switch (ghType) {
			case DOUBLE: {
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
				break;
			}
			case INT: {
				ya.setMinorTickCount(0);
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
				break;
			}
			case BOOL: {
				ya.setMinorTickCount(0);
				ya.setUpperBound(1.0);
				ya.setLowerBound(0.0);
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
				break;
			}
			default:
				break;
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

	private void addAreaChart() {
		XYChart.Series aSeries = null;
		GraphData.GhType ghType = firstData.getGhType();
		if (null != ghType) {
			switch (ghType) {
			case DOUBLE:
				aSeries = new XYChart.Series<String, Double>();
				areaChart.setCreateSymbols(false);
				double[] dVals = firstData.getDoubleData();
				for (int i = 0; i < dVals.length; i++) {
					String num = Integer.toString(i + 1);
					aSeries.getData().add(new XYChart.Data(num, dVals[i]));
				}
				break;
			case INT:
				ya.setMinorTickCount(0);
				aSeries = new XYChart.Series<String, Integer>();
				areaChart.setCreateSymbols(false);
				int[] iVals = firstData.getIntData();
				for (int i = 0; i < iVals.length; i++) {
					String num = Integer.toString(i + 1);
					aSeries.getData().add(new XYChart.Data(num, iVals[i - 1]));
					aSeries.getData().add(new XYChart.Data(num, iVals[i]));
				}
				break;
			case BOOL:
				ya.setMinorTickCount(0);
				ya.setUpperBound(1.0);
				ya.setLowerBound(0.0);
				aSeries = new XYChart.Series<String, Integer>();
				areaChart.setCreateSymbols(false);
				boolean[] bVals = firstData.getBoolData();
				for (int i = 0; i < bVals.length; i++) {
					String num = Integer.toString(i + 1);
					aSeries.getData().add(new XYChart.Data(num, convBoolToInt(bVals[i - 1])));
					aSeries.getData().add(new XYChart.Data(num, convBoolToInt(bVals[i])));
				}
				break;
			default:
				break;
			}
		}
		//areaChart.legendVisibleProperty().set(true);
		areaChart.getData().add(aSeries);
	}

	public void reset() {
		lineChart.getData().removeAll(lineChart.getData().toArray());
		if (areaChart != null) {
			areaChart.getData().removeAll(areaChart.getData());
		}
		chartCount = 0;
	}

	public void setChartSize(double width, double height) {
		lineChart.setMaxSize(width, height);
		lineChart.setMinSize(width, height);
		if (areaChart != null) {
			areaChart.setMaxSize(width, height);
			areaChart.setMinSize(width, height);
		}
	}

	public LineChart getChart() {
		return lineChart;
	}

	public StackPane getStackPane() {
		return stpane;
	}

}
