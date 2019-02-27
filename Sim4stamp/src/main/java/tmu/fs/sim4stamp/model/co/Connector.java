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
package tmu.fs.sim4stamp.model.co;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.json.JSONArray;
import org.json.JSONObject;
import tmu.fs.sim4stamp.SimService;
import tmu.fs.sim4stamp.gui.util.GuiUtil;
import tmu.fs.sim4stamp.model.em.Element;
import tmu.fs.sim4stamp.model.iop.AppendParams;
import tmu.fs.sim4stamp.model.iop.IOParam;
import tmu.fs.sim4stamp.util.DisplayLevel;
import tmu.fs.sim4stamp.util.DisplayValues;
import tmu.fs.sim4stamp.util.JSONConvert;

/**
 *
 * @author Keiichi Tsumuta
 */
public class Connector implements JSONConvert, DisplayLevel {

	private static final double ARROW_LENGTH = 14.0;
	private static final double DISTANCE_CLOSED = 10.0;
	private static final Color STROKE_COLOR = Color.BLUE;
	private static final Color STROKE2_COLOR = Color.SEASHELL;
	private static final Color MARKED_COLOR = Color.RED;
	private static final double DELTA = 8.0;

	private String nodeFromId;
	private String nodeToId;
	private List<Point2D.Double> points;
	private AppendParams appendParams;
	private volatile double pointXDelta = 0L;
	private volatile double pointYDelta = 0L;
	private volatile int selectedIndex = -1;
	private volatile int jointSelected = -1;
	private volatile boolean jointDisplay = false;
	private volatile boolean pointed = false;
	private volatile boolean marked = false;
	private volatile String markedComment = null;

	private volatile Level displayLevel = Level.Base;

	public Connector() {

	}

	public Connector(String fromId, String toId) {
		this.nodeFromId = fromId;
		this.nodeToId = toId;
	}

	public String getNodeFromId() {
		return nodeFromId;
	}

	public String getNodeToId() {
		return nodeToId;
	}

	public void setPoints(List<Point2D.Double> points) {
		this.points = points;
	}

	public List<Point2D.Double> getPoints() {
		return this.points;
	}

	/**
	 * @return the jointDisplay
	 */
	public boolean isJointDisplay() {
		return jointDisplay;
	}

	public boolean isJointSelected() {
		if (jointSelected == -1) {
			return false;
		}
		return true;
	}

	public boolean isSelected() {
		if (selectedIndex != -1 || jointSelected != -1) {
			return true;
		}
		return false;
	}

	public void resetSelect() {
		selectedIndex = -1;
	}

	/**
	 * @param jointDisplay the jointDisplay to inject
	 */
	public void setJointDisplay(boolean jointDisplay) {
		this.jointDisplay = jointDisplay;
	}

	/**
	 * @return the pointed
	 */
	public boolean isPointed() {
		return pointed;
	}

	/**
	 * @param pointed the pointed to inject
	 */
	public void setPointed(boolean pointed) {
		this.pointed = pointed;
	}

	public void draw(GraphicsContext gc) {
		if (points == null) {
			return;
		}
		if (points.size() <= 1) {
			return;
		}
		gc.setLineWidth(2.0);
		if (pointed) {
			gc.setStroke(Color.RED);
		} else if (nodeFromId != null && nodeFromId.length() > 0 && nodeToId != null && nodeToId.length() > 0) {
			gc.setStroke(Color.BLACK);
		} else {
			gc.setStroke(Color.GOLD);
		}
		double[] xp = new double[points.size()];
		double[] yp = new double[points.size()];
		for (int i = 0; i < points.size(); i++) {
			Point2D.Double p = points.get(i);
			xp[i] = p.getX();
			yp[i] = p.getY();
		}
		gc.strokePolyline(xp, yp, points.size());
		Point2D.Double p1 = points.get(points.size() - 1);
		Point2D.Double p2 = points.get(points.size() - 2);
		drawArrow(gc, p2.getX(), p2.getY(), p1.getX(), p1.getY());
		if (displayLevel != Level.Base) {
			drawDetailParams(gc);
		}
		if (jointDisplay) {
			for (int i = 0; i < points.size(); i++) {
				Point2D.Double point = points.get(i);
				double xo = point.getX();
				double yo = point.getY();
				gc.setGlobalAlpha(0.5);
				if (selectedIndex == i) {
					gc.setFill(Color.AQUA);
				} else {
					gc.setFill(Color.LIGHTBLUE);
				}
				gc.fillOval(xo - DISTANCE_CLOSED, yo - DISTANCE_CLOSED, DISTANCE_CLOSED * 2, DISTANCE_CLOSED * 2);
				gc.setGlobalAlpha(1.0);
			}
		} else if (selectedIndex != -1) {
			double xo = points.get(selectedIndex).getX();
			double yo = points.get(selectedIndex).getY();
			gc.setGlobalAlpha(0.5);
			gc.setFill(Color.AQUA);
			gc.fillOval(xo - DISTANCE_CLOSED, yo - DISTANCE_CLOSED, DISTANCE_CLOSED * 2, DISTANCE_CLOSED * 2);
			gc.setGlobalAlpha(1.0);
		}
	}

	private void drawArrow(GraphicsContext gc, double x1, double y1, double x2, double y2) {
		double th = 0;
		double[] axs, ays;
		if ((x2 - x1) != 0) {
			th = Math.atan((y2 - y1) / (x2 - x1));
			if (x2 < x1) {
				th = th + Math.PI;
			}
			double ax = Math.cos(th - Math.PI * 5 / 6.0) * ARROW_LENGTH;
			double ay = Math.sin(th - Math.PI * 5 / 6.0) * ARROW_LENGTH;
			double ax2 = Math.cos(th - Math.PI * 7 / 6.0) * ARROW_LENGTH;
			double ay2 = Math.sin(th - Math.PI * 1 / 6.0) * ARROW_LENGTH;
			axs = new double[]{ax + x2, x2, ax2 + x2};
			ays = new double[]{ay + y2, y2, -ay2 + y2};
		} else {
			th = Math.PI / 2.0;
			if (y1 > y2) {
				th = -th;
			}
			double ax = Math.cos(th - Math.PI * 5 / 6.0) * ARROW_LENGTH;
			double ay = Math.sin(th - Math.PI * 5 / 6.0) * ARROW_LENGTH;
			axs = new double[]{ax + x2, x2, -ax + x2};
			ays = new double[]{ay + y2, y2, ay + y2};
		}
		gc.strokePolyline(axs, ays, 3);
	}

	private void drawDetailParams(GraphicsContext gc) {
		int size = points.size();
		double leng = 0.0;
		double[] lengs = new double[size];
		for (int i = 1; i < size; i++) {
			Point2D.Double p0 = points.get(i - 1);
			double x0 = p0.getX();
			double y0 = p0.getY();
			Point2D.Double p1 = points.get(i);
			double x1 = p1.getX();
			double y1 = p1.getY();
			lengs[i - 1] = Math.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0));
			leng += lengs[i - 1];
		}
		double leng2 = leng / 2.0;
		double wk = 0.0;
		double xp = 0.0;
		double yp = 0.0;
		for (int i = 0; i < (lengs.length - 1); i++) {
			wk += lengs[i];
			if (wk > leng2) {
				Point2D.Double p0 = points.get(i);
				double x0 = p0.getX();
				double y0 = p0.getY();
				Point2D.Double p1 = points.get(i + 1);
				double x1 = p1.getX();
				double y1 = p1.getY();
				if (Math.abs(x1 - x0) > 1) {
					double th = Math.atan((y1 - y0) / (x1 - x0));
					double r = wk - leng2;
					if (x1 > x0) {
						xp = x1 - r * Math.cos(th);
						yp = y1 - r * Math.sin(th);
					} else {
						xp = x1 + r * Math.cos(th);
						yp = y1 + r * Math.sin(th);
					}
				} else {
					xp = x0;
					if (y1 >= y0) {
						yp = y1 - (wk - leng2);
					} else {
						yp = y1 + (wk - leng2);
					}
				}
				break;
			}
		}
		if (appendParams != null) {
			List<IOParam> ioParams = appendParams.getParams();
			double height = getFontHight(gc) + (float) 0.5;
			int i = 0;
			for (IOParam ioParam : ioParams) {
				String id = ioParam.getId();
				if (displayLevel != Level.Progress) {
					IOParam.ValueType type = ioParam.getType();
					if (type == IOParam.ValueType.REAL) {
						id += "<R>";
					} else if (type == IOParam.ValueType.INT) {
						id += "<I>";
					}
					if (type == IOParam.ValueType.BOOL) {
						id += "<B>";
					}
				}
				if (displayLevel == Level.Progress) {
					id += DisplayValues.getInstance().getDisplayData(nodeToId, id);
				}
				Color color = STROKE_COLOR;
				if (marked) {
					id = "≪ " + id + " ≫";
					color = MARKED_COLOR;
				}

				double width = getFontWidth(gc, id);
				gc.setFill(STROKE2_COLOR);
				gc.fillRect(xp - 2 - width / 2, yp - height, width + 2, height);
				gc.setFill(color);
				gc.fillText(id, xp - width / 2, yp + height * i - 2);
				if (marked) {
					gc.fillText(markedComment, xp - width / 2, yp - 2.0 + height * (i + 1));
				}
				i++;
			}
		}
	}

	protected double getFontWidth(GraphicsContext gc, String t) {
		return GuiUtil.getFontWidth(gc, t);
	}

	protected double getFontWidth(String t, Font font) {
		return GuiUtil.getFontWidth(t, font);
	}

	protected double getFontHight(GraphicsContext gc) {
		return GuiUtil.getFontHight(gc);
	}

	protected double getFontHight(Font font) {
		return GuiUtil.getFontHight(font);
	}

	/**
	 * @return the appendParams
	 */
	public AppendParams getAppendParams() {
		if (appendParams == null) {
			appendParams = new AppendParams(AppendParams.ParamType.Connector);
		}
		return appendParams;
	}

	/**
	 * @param appendParams the appendParams to inject
	 */
	public void setAppendParams(AppendParams appendParams) {
		this.appendParams = appendParams;
	}

	public void elementFocus(String elementId, double pointX, double pointY) {
		if (elementId.equals(nodeFromId)) {
			pointXDelta = pointX - points.get(0).getX();
			pointYDelta = pointY - points.get(0).getY();
			selectedIndex = -1;
		} else if (elementId.equals(nodeToId)) {
			int toIndex = points.size() - 1;
			pointXDelta = pointX - points.get(toIndex).getX();
			pointYDelta = pointY - points.get(toIndex).getY();
			selectedIndex = -1;
		}
	}

	public void elementMove(String elementId, double x, double y) {
		if (elementId.equals(nodeFromId)) {
			Point2D.Double point = new Point2D.Double(x - pointXDelta, y - pointYDelta);
			points.set(0, point);
			selectedIndex = -1;
		} else if (elementId.equals(nodeToId)) {
			Point2D.Double point = new Point2D.Double(x - pointXDelta, y - pointYDelta);
			points.set(points.size() - 1, point);
			selectedIndex = -1;
		}
	}

	public void jointMove(double pointX, double pointY) {
		if (selectedIndex != -1) {
			if (selectedIndex == 0 || selectedIndex == (points.size()) - 1
					|| (nodeFromId != null && nodeFromId.length() > 0) || (nodeToId != null && nodeToId.length() > 0)) {
				// 部分移動
				Point2D.Double point = new Point2D.Double(pointX, pointY);
				points.set(selectedIndex, point);
			} else { // 全移動
				Point2D.Double selPoint = points.get(selectedIndex);
				double sx = selPoint.getX();
				double sy = selPoint.getY();
				for (int i = 0; i < points.size(); i++) {
					if (selectedIndex == i) {
						Point2D.Double point = new Point2D.Double(pointX, pointY);
						points.set(selectedIndex, point);
					} else {
						Point2D.Double oldPoint = points.get(i);
						double nx = pointX - (sx - oldPoint.getX());
						double ny = pointY - (sy - oldPoint.getY());
						if (nx < 0.0) {
							nx = 0.0;
						}
						if (ny < 0.0) {
							ny = 0.0;
						}
						Point2D.Double point = new Point2D.Double(nx, ny);
						points.set(i, point);
					}
				}
			}
		}
	}

	public boolean selectDistance(double x, double y) {
		jointSelected = -1;
		selectedIndex = -1;
		if (points.size() >= 2) {
			for (int i = 1; i < points.size(); i++) {
				Point2D.Double p1 = points.get(i - 1);
				double x1 = p1.getX();
				double y1 = p1.getY();
				Point2D.Double p2 = points.get(i);
				double x2 = p2.getX();
				double y2 = p2.getY();

				if (p1.distanceSq(x, y) <= DISTANCE_CLOSED) {
					selectedIndex = i - 1;
					return true;
				} else if (p2.distanceSq(x, y) <= DISTANCE_CLOSED) {
					selectedIndex = i;
					return true;
				}
				if (Math.abs(x - x1) < DISTANCE_CLOSED) {
					if (y2 > y1 && y1 <= y && y <= y2) {
						jointSelected = i;
						return true;
					} else if (y2 <= y && y <= y1) {
						jointSelected = i;
						return true;
					}
				}
				if (Math.abs(y - y1) < DISTANCE_CLOSED) {
					if (x2 > x1 && x1 <= x && x <= x2) {
						jointSelected = i;
						return true;
					} else if (x2 <= x && x <= x1) {
						jointSelected = i;
						return true;
					}
				}
				if ((x1 > x && x2 > x) || (x1 < x && x2 < x)) {
					continue;
				}
				if ((y1 > y && y2 > y) || (y1 < y && y2 < y)) {
					continue;
				}
				if (Math.abs(x2 - x1) > 1.0) {
					double a = (y2 - y1) / (x2 - x1);
					double b = y1 - a * x1;
					double z2 = (x - (y - b) / a) * (x - (y - b) / a) + (a * x + b - y) * (a * x + b - y);
					double d = (a * x + b - y) * (a * x + b - y) / Math.sqrt(z2);
					if (d < 2.0) {
						jointSelected = i;
						return true;
					}
				}
			}
		}
		return false;
	}

	public void setPos() {
		if (selectedIndex == 0) { // From node side
			nodeFromId = "";
			List<Element> elements = SimService.getInstance().getElements();
			double x = points.get(0).getX();
			double y = points.get(0).getY();
			for (Element el : elements) {
				if (el.containsOutside(x, y) && !el.containsInside(x, y)) {
					nodeFromId = el.getNodeId();
					setFit(el, points.get(0));
					break;
				}
			}
		} else if (selectedIndex == (points.size() - 1)) { // To node side
			nodeToId = "";
			List<Element> elements = SimService.getInstance().getElements();
			int index = points.size() - 1;
			double x = points.get(index).getX();
			double y = points.get(index).getY();
			for (Element el : elements) {
				if (el.containsOutside(x, y) && !el.containsInside(x, y)) {
					nodeToId = el.getNodeId();
					setFit(el, points.get(index));
					break;
				}
			}
		}
		// System.out.println("cons:" + nodeFromId + "," + nodeToId);
	}

	private void setFit(Element el, Point2D.Double point) {
		Rectangle2D.Double base = el.getBaseRect();
		if (Math.abs(base.x - point.x) < DELTA) {
			point.x = base.x;
		} else if (Math.abs(base.x + base.width - point.x) < DELTA) {
			point.x = base.x + base.width;
		} else if (Math.abs(base.y - point.y) < DELTA) {
			point.y = base.y;
		} else if (Math.abs(base.y + base.height - point.y) < DELTA) {
			point.y = base.y + base.height;
		}
	}

	public void checkNode(String nodeId) {
		if (nodeId != null) {
			if (nodeFromId != null && nodeFromId.equals(nodeId)) {
				nodeFromId = null;
			} else if (nodeToId != null && nodeToId.equals(nodeId)) {
				nodeToId = null;
			}
		}
	}

	public void addJoint(double x, double y) {
		if (jointSelected == -1) {
			return;
		}
		Point2D.Double point = new Point2D.Double(x, y);
		points.add(jointSelected, point);
		selectedIndex = jointSelected;
		jointSelected = -1;
	}

	public void deleteJoint() {
		if (selectedIndex == -1) {
			return;
		}
		if (selectedIndex == 0 || selectedIndex == (points.size() - 1)) {
			return;
		}
		points.remove(selectedIndex);
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public void parseJson(JSONObject ob) {
		nodeFromId = ob.optString("from");
		nodeToId = ob.optString("to");
		JSONArray coordinates = ob.getJSONArray("coordinates");
		int cLen = coordinates.length();
		List<Point2D.Double> ps = new ArrayList<>();
		for (int k = 0; k < cLen; k++) {
			String xy = coordinates.getJSONObject(k).getString("xy");
			String[] tk = xy.split(",");
			double x = getDouble(tk[0].trim());
			double y = getDouble(tk[1].trim());
			ps.add(new Point2D.Double(x, y));
		}
		setPoints(ps);
	}

	private double getDouble(String t) {
		try {
			return Double.parseDouble(t);
		} catch (Exception ex) {

		}
		return 0.0;
	}

	@Override
	public void addJSON(JSONObject jj) {
		JSONObject jobj = new JSONObject();
		try {
			jobj.accumulate("from", nodeFromId);
			jobj.accumulate("to", nodeToId);
			List<JSONObject> coordinates = new ArrayList<>();
			for (int i = 0; i < points.size(); i++) {
				Point2D.Double p = points.get(i);
				JSONObject xy = new JSONObject();
				xy.accumulate("xy", p.getX() + "," + p.getY());
				coordinates.add(xy);
			}
			jobj.accumulate("coordinates", coordinates);
			if (appendParams != null) {
				appendParams.addJson(AppendParams.ParamType.Connector, jobj);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		jj.accumulate("connection", jobj);
	}

	@Override
	public void setLevel(Level level) {
		displayLevel = level;
	}

	@Override
	public Level getLevel() {
		return displayLevel;
	}

	@Override
	public Connector clone() {
		Connector co = new Connector(nodeFromId, nodeToId);
		co.setAppendParams(appendParams);
		return co;
	}

	/**
	 * @return the marked
	 */
	public boolean isMarked() {
		return marked;
	}

	/**
	 * @param marked the marked to inject
	 */
	public void setMarked(boolean marked, String comment) {
		this.marked = marked;
		this.markedComment = comment;
	}

}
