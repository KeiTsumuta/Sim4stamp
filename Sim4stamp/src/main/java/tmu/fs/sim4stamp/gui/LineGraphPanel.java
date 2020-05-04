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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.text.DecimalFormat;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import tmu.fs.sim4stamp.gui.util.GraphAxis;
import tmu.fs.sim4stamp.gui.util.GraphData;
import tmu.fs.sim4stamp.gui.util.GuiUtil;
import tmu.fs.sim4stamp.model.iop.SafetyConstraintValue;
import static tmu.fs.sim4stamp.gui.util.GraphData.GhType.FIVE_VALUE;

/**
 * 時系列グラフの表示
 *
 * @author Keiichi Tsumuta
 */
public class LineGraphPanel implements Initializable {

	private static final DecimalFormat D_FORMAT = new DecimalFormat("#0.00");

	private static final Color FILL_BACK_COLOR = Color.FLORALWHITE;
	private static final Color GRPAH_EDGE_COLOR = Color.DARKGREY;
	private static final Color GRPAH_MESH_COLOR = Color.GREY;
	private static final Color GRPAH_METRIC_COLOR = Color.BLACK;
	private static final Color GRPAH_TITLE_COLOR = Color.BLACK;
	private static final Color UPPER_CONSTRAINT_COLOR = Color.MAGENTA;
	private static final Color UNDER_CONSTRAINT_COLOR = Color.PURPLE;
	private static final double W_AX_LEFT = 60.0;
	private static final double H_AX_BOTTOM = 30.0;
	private static final double INSET_TOP = 25.0;
	private static final double INSET_RIGHT = 10.0;

	private Canvas modelCanvas = null;
	private double modelCanvasWidth;
	private double modelCanvasHeight;

	private List<String> seriesNames;
	private List<GraphData> graphDataList;
	private SafetyConstraintValue upperValue;
	private SafetyConstraintValue underValue;
	private double heightMaxValue;
	private double heightMinValue;

	private GraphAxis graphAxis;
	private double[] yAxis = new double[]{0.0, 1.0};
	private int pow = 0;
	private GraphData.GhType ghType;

	private String[] graphLineColors;

	private String title = null;

	private int fillIndex = 0;

	public LineGraphPanel() {
		modelCanvas = new Canvas();
		seriesNames = new ArrayList<>();
		graphDataList = new ArrayList<>();
		heightMaxValue = 0.0;
		heightMinValue = 0.0;
		pow = 0;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}

	public void reset() {
		seriesNames = new ArrayList<>();
		graphDataList = new ArrayList<>();
		upperValue = null;
		underValue = null;
		heightMaxValue = 0.0;
		heightMinValue = 0.0;
		pow = 0;
		graphAxis = new GraphAxis();
		drawCanvasPanel();
	}

	public void setWidth(double w) {
		modelCanvasWidth = w;
		drawCanvasPanel();
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * 表示データの追加
	 */
	public void addData(String seriesName, GraphData data) {
		seriesNames.add(seriesName);
		getGraphDataList().add(data);

		// populating the aSeries with data
		ghType = data.getGhType();
		if (null != ghType) {
			switch (ghType) {
				case DOUBLE: {
					double[] dVals = data.getDoubleData();
					for (int i = 0; i < dVals.length; i++) {
						heightMaxValue = Math.max(heightMaxValue, dVals[i]);
						heightMinValue = Math.min(heightMinValue, dVals[i]);
					}
					break;
				}
				case INT: {
					int[] iVals = data.getIntData();
					for (int i = 0; i < iVals.length; i++) {
						heightMaxValue = Math.max(heightMaxValue, iVals[i]);
						heightMinValue = Math.min(heightMinValue, iVals[i]);
					}
					break;
				}
				case BOOL: {
					heightMaxValue = 1.0;
					heightMinValue = 0.0;
					break;
				}
				case FIVE_VALUE: {
					heightMaxValue = 5.0;
					heightMinValue = 0.0;
					break;
				}
				default:
					break;
			}
		}
		upperValue = data.getUpperValue();
		if (upperValue != null && upperValue.isSetting()) {
			heightMaxValue = Math.max(heightMaxValue, upperValue.getConstraintValue());
		}
		underValue = data.getUnderValue();
		if (underValue != null && underValue.isSetting()) {
			heightMinValue = Math.min(heightMinValue, underValue.getConstraintValue());
		}
		yAxis = graphAxis.getScale(heightMaxValue, heightMinValue, ghType);
		pow = graphAxis.getPow();

		drawCanvasPanel();
	}

	public void setChartSize(double width, double height) {
		modelCanvasWidth = width;
		modelCanvasHeight = height;
		drawCanvasPanel();
	}

	public Canvas getCanvas() {
		return modelCanvas;
	}

	public void drawCanvasPanel() {
		Platform.runLater(() -> {
			try {
				drawCanvas(modelCanvas);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
	}

	private void drawCanvas(Canvas canvas) {

		GraphicsContext gc = canvas.getGraphicsContext2D();
		canvas.setWidth(modelCanvasWidth);
		canvas.setHeight(modelCanvasHeight);
		double wMax = canvas.getWidth();
		double hMax = canvas.getHeight();

		gc.setFill(FILL_BACK_COLOR);
		gc.fillRect(0, 0, wMax, hMax);
		gc.setStroke(GRPAH_EDGE_COLOR);
		gc.strokeRect(0, 0, wMax, hMax);
		//System.out.println("w=" + wMax + ", h=" + hMax);
		double graphWidth = wMax - W_AX_LEFT - INSET_RIGHT;
		double graphHeight = hMax - INSET_TOP - H_AX_BOTTOM;

		gc.strokeRect(W_AX_LEFT, INSET_TOP, graphWidth, graphHeight);

		if (title != null) {
			Font font = gc.getFont();
			Font tfont = new Font(font.getSize() * 1.3);
			gc.setFont(tfont);
			gc.setFill(GRPAH_TITLE_COLOR);
			double fontTitleHeight = GuiUtil.getFontHight(gc);
			gc.fillText(title, W_AX_LEFT + 10.0, fontTitleHeight + 1.0);
			gc.setFont(font);
		}

		if (getGraphDataList().size() == 0) {
			return;
		}
		gc.setStroke(GRPAH_MESH_COLOR);
		gc.setLineWidth(0.5);
		gc.setLineDashes(2);
		int x_count = getGraphDataList().get(0).getDataCount() + 1;
		for (int i = 1; i < x_count; i++) {
			double x = W_AX_LEFT + graphWidth / x_count * i;
			gc.strokeLine(x, INSET_TOP, x, hMax - H_AX_BOTTOM);
		}

		int yCount = yAxis.length - 1;
		for (int i = 1; i < yCount; i++) {
			double y = INSET_TOP + graphHeight / yCount * i;
			gc.strokeLine(W_AX_LEFT, y, wMax - INSET_RIGHT, y);
		}
		gc.setFill(GRPAH_METRIC_COLOR);
		gc.setLineDashes(0);
		double fontHeight = GuiUtil.getFontHight(gc);
		for (int i = 1; i < x_count; i++) {
			double x = W_AX_LEFT + graphWidth / x_count * i;
			String num = Integer.toString(i);
			double fontWidth = GuiUtil.getFontWidth(gc, num);
			gc.fillText(num, x - fontWidth / 2.0, hMax - H_AX_BOTTOM + fontHeight);
		}
		String[] yAxNums = getGraphYAxis();
		for (int i = 0; i <= yCount; i++) {
			double y = INSET_TOP + graphHeight / yCount * (yCount - i);
			double fontWidth = GuiUtil.getFontWidth(gc, yAxNums[i]);
			gc.fillText(yAxNums[i], W_AX_LEFT - fontWidth - 2.0, y);
		}

		double yConv = (Math.abs(yAxis[yAxis.length - 1]) + Math.abs(yAxis[0])) * Math.pow(10, pow);
		double xm = Math.abs(yAxis[0]) * Math.pow(10, pow);

		gc.setLineWidth(2.0);
		gc.setLineDashes(5);

		boolean upFlag = false;
		if (upperValue != null && upperValue.isSetting()) {
			upFlag = true;
			gc.setStroke(UPPER_CONSTRAINT_COLOR);
			double constraitMax = upperValue.getConstraintValue();
			if (null != ghType) {
				switch (ghType) {
					case BOOL:
						constraitMax = 1.0;
						break;
					case FIVE_VALUE:
						constraitMax = constraitMax - 0.45;
						break;
				}
			}
			double y = INSET_TOP + graphHeight * (1 - (constraitMax + xm) / yConv);
			gc.strokeLine(W_AX_LEFT, y, wMax - INSET_RIGHT, y);
			// 凡例表示
			y = INSET_TOP / 2.0;
			gc.strokeLine(wMax - INSET_RIGHT - 100, y, wMax - INSET_RIGHT - 70, y);
			gc.fillText("上限制約値", wMax - INSET_RIGHT - 68, y + fontHeight / 3.0);
		}

		if (underValue != null && underValue.isSetting()) {
			gc.setStroke(UNDER_CONSTRAINT_COLOR);
			double constraitMin = underValue.getConstraintValue();
			if (null != ghType) {
				switch (ghType) {
					case BOOL:
						constraitMin = 0.2;
						break;
					case FIVE_VALUE:
						constraitMin = constraitMin + 0.45;
						break;
				}
			}
			double y = INSET_TOP + graphHeight * (1 - (constraitMin + xm) / yConv);
			gc.strokeLine(W_AX_LEFT, y, wMax - INSET_RIGHT, y);
			// 凡例表示
			y = INSET_TOP / 2.0;
			int wc = 0;
			if (upFlag) {
				wc = 105;
			}
			gc.strokeLine(wMax - INSET_RIGHT - 100 - wc, y, wMax - INSET_RIGHT - 70 - wc, y);
			gc.fillText("下限制約値", wMax - INSET_RIGHT - 68 - wc, y + fontHeight / 3.0);
		}
		gc.setLineDashes(0);

		for (int k = 0; k < getGraphDataList().size(); k++) {
			Color gcolor = Color.web(graphLineColors[(k) % graphLineColors.length]);
			gc.setStroke(gcolor);
			List<Double> xarr = new ArrayList<>();
			List<Double> yarr = new ArrayList<>();
			GraphData dg = getGraphDataList().get(k);
			if (dg.isDisabled()) {
				continue;
			}

			switch (ghType) {
				case DOUBLE:
					double[] d = dg.getDoubleData();
					for (int i = 0; i < x_count - 1; i++) {
						xarr.add(W_AX_LEFT + graphWidth / x_count * (i + 1));
						yarr.add(INSET_TOP + graphHeight * (1 - (d[i] + xm) / yConv));
					}
					break;
				case INT:
					int[] iv = dg.getIntData();
					int ivOld = 0;
					if (iv.length > 0) {
						ivOld = iv[0];
					}
					for (int i = 0; i < x_count - 1; i++) {
						double x = W_AX_LEFT + graphWidth / x_count * (i + 1);
						if (iv[i] != ivOld) {
							xarr.add(x);
							yarr.add(INSET_TOP + graphHeight * (1 - (ivOld + xm) / yConv));
						}
						xarr.add(x);
						yarr.add(INSET_TOP + graphHeight * (1 - (iv[i] + xm) / yConv));
						ivOld = iv[i];
					}
					break;
				case BOOL:
					boolean[] b = dg.getBoolData();
					boolean bOld = false;
					if (b.length > 0) {
						bOld = b[0];
					}
					for (int i = 0; i < x_count - 1; i++) {
						double x = W_AX_LEFT + graphWidth / x_count * (i + 1);
						if (b[i] != bOld) {
							xarr.add(x);
							if (bOld) {
								yarr.add(INSET_TOP + graphHeight * (1 - (0.9) / 1.0));
							} else {
								yarr.add(INSET_TOP + graphHeight * (1 - (0.1) / 1.0));
							}
						}
						xarr.add(x);
						if (b[i]) {
							yarr.add(INSET_TOP + graphHeight * (1 - (0.9) / 1.0));
						} else {
							yarr.add(INSET_TOP + graphHeight * (1 - (0.1) / 1.0));
						}
						bOld = b[i];
					}
					break;
				case FIVE_VALUE:
					d = dg.getDoubleData();
					for (int i = 0; i < x_count - 1; i++) {
						xarr.add(W_AX_LEFT + graphWidth / x_count * (i + 1));
						double val = d[i];
						if (val != 0.0) {
							if (val > 5.0) {
								val = 5.0;
							} else if (val < 1.0) {
								val = 1.0;
							}
						}
						yarr.add(INSET_TOP + graphHeight * (1 - (val + xm) / yConv));
					}
					break;
			}
			int xsize = xarr.size();
			if (xsize == 0) {
				continue;
			}
			if (k == fillIndex) {
				double[] xas0 = new double[xsize + 2];
				double[] yas0 = new double[xsize + 2];
				xas0[0] = xarr.get(0);
				yas0[0] = hMax - H_AX_BOTTOM;
				for (int i = 0; i < xsize; i++) {
					xas0[i + 1] = xarr.get(i);
					yas0[i + 1] = yarr.get(i);
				}
				xas0[xsize + 1] = xarr.get(xsize - 1);
				yas0[xsize + 1] = hMax - H_AX_BOTTOM;
				gc.setFill(gcolor);
				gc.setGlobalAlpha(0.1);
				gc.fillPolygon(xas0, yas0, xsize + 2);
			}

			gc.setGlobalAlpha(1.0);
			double[] xas = new double[xsize];
			double[] yas = new double[xsize];
			for (int i = 0; i < xsize; i++) {
				xas[i] = xarr.get(i);
				yas[i] = yarr.get(i);
			}
			StrokeLineJoin sj = gc.getLineJoin();
			gc.setLineJoin(StrokeLineJoin.ROUND);
			gc.strokePolyline(xas, yas, xsize);
			gc.setLineJoin(sj);
		}

	}

	private String[] getGraphYAxis() {
		String[] yScVals = new String[yAxis.length];
		if (null != ghType) {
			switch (ghType) {
				case DOUBLE: {
					for (int i = 0; i < yAxis.length; i++) {
						yScVals[i] = D_FORMAT.format(yAxis[i] * Math.pow(10, pow));
					}
					break;
				}
				case INT: {
					for (int i = 0; i < yAxis.length; i++) {
						yScVals[i] = Integer.toString((int) (yAxis[i] * Math.pow(10, pow)));
					}
					break;
				}
				case BOOL: {
					yScVals[0] = "FALSE";
					yScVals[yScVals.length - 1] = "TRUE";
					break;
				}
				case FIVE_VALUE: {
					String[] vals = getGraphDataList().get(0).getUnitValues();
					for (int i = 0; i < vals.length; i++) {
						yScVals[i] = vals[i];
					}
					break;
				}
				default:
					break;
			}
		}
		return yScVals;
	}

	/**
	 * @return the graphLineColors
	 */
	public String[] getGraphLineColors() {
		return graphLineColors;
	}

	/**
	 * @param graphLineColors the graphLineColors to set
	 */
	public void setGraphLineColors(String[] graphLineColors) {
		this.graphLineColors = graphLineColors;
	}

	/**
	 * @return the graphDataList
	 */
	public List<GraphData> getGraphDataList() {
		return graphDataList;
	}

	public void selectDisplayData(int index, boolean select) {
		if (graphDataList.size() > index) {
			GraphData gData = graphDataList.get(index);
			gData.setDisabled(!select);
		}
	}

	/**
	 * @return the fillIndex
	 */
	public int getFillIndex() {
		return fillIndex;
	}

	/**
	 * @param fillIndex the fillIndex to set
	 */
	public void setFillIndex(int fillIndex) {
		this.fillIndex = fillIndex;
	}

}
