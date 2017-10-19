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

import com.sun.javafx.tk.Toolkit;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.json.JSONArray;
import org.json.JSONObject;
import tmu.fs.sim4stamp.SimService;
import tmu.fs.sim4stamp.model.em.Element;
import tmu.fs.sim4stamp.model.iop.AppendParams;
import tmu.fs.sim4stamp.model.iop.IOParam;
import tmu.fs.sim4stamp.util.DisplayLevel;
import tmu.fs.sim4stamp.util.JSONConvert;

/**
 *
 * @author Keiichi Tsumuta
 */
public class Connector implements JSONConvert, DisplayLevel {

    private static Logger log = Logger.getLogger(Connector.class.getPackage().getName());

    private static final double ARROW_LENGTH = 20.0;
    private static final double DISTANCE_CLOSED = 10.0;
    private static final Color STROKE_COLOR = Color.BROWN;
    private static final Color STROKE2_COLOR = Color.SEASHELL;

    private String nodeFromId;
    private String nodeToId;
    private List<Point2D.Double> points;
    private AppendParams appendParams;
    private volatile double pointXDelta = 0L;
    private volatile double pointYDelta = 0L;
    private volatile int selectedIndex = -1;
    private volatile boolean jointDisplay = false;

    private Level displayLevel = Level.Base;

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

    /**
     * @return the jointDisplay
     */
    public boolean isJointDisplay() {
        return jointDisplay;
    }

    public boolean isSelected() {
        if (selectedIndex != -1) {
            return true;
        }
        return false;
    }

    /**
     * @param jointDisplay the jointDisplay to set
     */
    public void setJointDisplay(boolean jointDisplay) {
        this.jointDisplay = jointDisplay;
    }

    public void draw(GraphicsContext gc) {
        if (points == null) {
            return;
        }
        if (points.size() <= 1) {
            return;
        }
        gc.setLineWidth(2.0);
        if (nodeFromId != null && nodeFromId.length() > 0 && nodeToId != null && nodeToId.length() > 0) {
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
        if (displayLevel == Level.Detail) {
            drawDetailParams(gc);
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
            double height = getFontHight(gc) + (float) 2.0;
            int i = 0;
            for (IOParam ioParam : ioParams) {
                String id = ioParam.getId();
                double width = getFontWidth(gc, id);
                gc.setFill(STROKE2_COLOR);
                gc.fillRect(xp - 2 - width / 2, yp - height, width + 2, height);
                gc.setFill(STROKE_COLOR);
                gc.fillText(id, xp - width / 2, yp + height * i - 2);
                i++;
            }
        }
    }

    protected double getFontWidth(GraphicsContext gc, String t) {
        return getFontWidth(t, gc.getFont());
    }

    protected double getFontWidth(String t, Font font) {
        Text text = new Text(t);
        text.setFont(font);
        return text.getLayoutBounds().getWidth();
    }

    protected double getFontHight(GraphicsContext gc) {
        return getFontHight(gc.getFont());
    }

    protected double getFontHight(Font font) {
        Text text = new Text("X");
        text.setFont(font);
        return text.getLayoutBounds().getHeight();
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
     * @param appendParams the appendParams to set
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
                    || (nodeFromId != null && nodeFromId.length() > 0)
                    || (nodeToId != null && nodeToId.length() > 0)) {
                // 部分移動
                Point2D.Double point = new Point2D.Double(pointX, pointY);
                points.set(selectedIndex, point);
            } else {  // 全移動
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
        selectedIndex = -1;
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).distanceSq(x, y) <= DISTANCE_CLOSED) {
                selectedIndex = i;
                return true;
            }
        }
        return false;
    }

    public void setPos() {
        if (selectedIndex == 0) {  // From node side
            nodeFromId = "";
            List<Element> elements = SimService.getInstance().getElements();
            double x = points.get(0).getX();
            double y = points.get(0).getY();
            for (Element el : elements) {
                if (el.containsOutside(x, y)) {
                    nodeFromId = el.getNodeId();
                    break;
                }
            }
        } else if (selectedIndex == (points.size() - 1)) { // To node side
            nodeToId = "";
            List<Element> elements = SimService.getInstance().getElements();
            double x = points.get(points.size() - 1).getX();
            double y = points.get(points.size() - 1).getY();
            for (Element el : elements) {
                if (el.containsOutside(x, y)) {
                    nodeToId = el.getNodeId();
                    break;
                }
            }
        }
        //System.out.println("cons:" + nodeFromId + "," + nodeToId);
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
            log.severe(ex.toString());
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

}
