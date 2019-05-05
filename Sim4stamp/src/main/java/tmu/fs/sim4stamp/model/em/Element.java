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
package tmu.fs.sim4stamp.model.em;

import javafx.scene.canvas.GraphicsContext;
import java.awt.geom.Rectangle2D;
import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.json.JSONObject;
import tmu.fs.sim4stamp.gui.util.GuiUtil;
import tmu.fs.sim4stamp.model.iop.AppendParams;
import tmu.fs.sim4stamp.model.iop.IOParam;
import tmu.fs.sim4stamp.util.DisplayLevel;
import tmu.fs.sim4stamp.util.DisplayValues;
import tmu.fs.sim4stamp.util.JSONConvert;

/**
 *
 * @author Keiichi Tsumuta
 */
public abstract class Element implements JSONConvert, DisplayLevel {

    public enum EType { //
        CONTROLLER, //
        ACTUATOR, //
        SENSOR, //
        CONTROLLED_EQUIPMENT, //
        INJECTOR //
    }

    protected static final Color SELECTED_COLOR = Color.RED;
    private static final Color STROKE_COLOR = Color.BROWN;
    private static final Color STROKE_INIT_COLOR = Color.DARKGREEN;
    private static final Color STROKE2_COLOR = Color.SEASHELL;

    protected EType etype;
    protected final String nodeId;
    protected final String title;
    private boolean rootElement;
    private Rectangle2D.Double baseRect;
    protected Rectangle2D.Double baseRectOutside;
    protected Rectangle2D.Double baseRectInside;
    protected double eleWidth;
    protected double eleHeight;
    protected volatile boolean isSelected = false;
    private boolean tempFlag = false;
    private int order = 0;
    private AppendParams appendParams;
    private volatile double pointXDelta = 0L;
    private volatile double pointYDelta = 0L;

    private volatile Level displayLevel = Level.Base;

    public Element(EType et, String id, String title, double width, double height) {
        etype = et;
        this.nodeId = id;
        this.title = title;
        this.eleWidth = width;
        this.eleHeight = height;
    }

    public void setPoint(double x, double y) {
        baseRect = new Rectangle2D.Double(x, y, eleWidth, eleHeight);
        baseRectOutside = new Rectangle2D.Double(x - 8, y - 8, eleWidth + 16, eleHeight + 16);
        baseRectInside = new Rectangle2D.Double(x + 4, y + 4, eleWidth - 8, eleHeight - 8);
    }

    /**
     * @return the baseRect
     */
    public Rectangle2D.Double getBaseRect() {
        return baseRect;
    }

    public String getNodeId() {
        return nodeId;
    }

    public EType getType() {
        return etype;
    }

    public abstract void draw(GraphicsContext gc);

    public boolean contains(double pointX, double pointY) {
        if (getBaseRect() != null) {
            if (getBaseRect().contains(pointX, pointY)) {
                pointXDelta = pointX - getBaseRect().getX();
                pointYDelta = pointY - getBaseRect().getY();
                return true;
            }
        }
        return false;
    }

    public boolean containsInside(double pointX, double pointY) {
        if (baseRectInside != null) {
            if (baseRectInside.contains(pointX, pointY)) {
                pointXDelta = pointX - getBaseRect().getX();
                pointYDelta = pointY - getBaseRect().getY();
                return true;
            }
        }
        return false;
    }

    public boolean containsOutside(double pointX, double pointY) {
        if (baseRectOutside != null) {
            if (baseRectOutside.contains(pointX, pointY)) {
                pointXDelta = pointX - getBaseRect().getX();
                pointYDelta = pointY - getBaseRect().getY();
                return true;
            }
        }
        return false;
    }

    public void setSelect(boolean state) {
        isSelected = state;
        if (!state) {
            pointXDelta = 0L;
            pointYDelta = 0L;
        }
    }

    public boolean isSelected() {
        return isSelected;
    }

    protected void setSelectState(GraphicsContext gc, Color normalColor, Color seletctedColor) {
        if (isSelected) {
            gc.setStroke(seletctedColor);
            gc.setLineWidth(4.0);
        } else {
            gc.setStroke(normalColor);
            if (rootElement) {
                gc.setLineWidth(2.5);
            } else {
                gc.setLineWidth(2.0);
            }
        }
    }

    public void move(double x, double y) {
        setPoint(x - pointXDelta, y - pointYDelta);
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
            appendParams = new AppendParams(AppendParams.ParamType.Element);
        }
        return appendParams;
    }

    /**
     * @param appendParams the appendParams to inject
     */
    public void setAppendParams(AppendParams appendParams) {
        this.appendParams = appendParams;
    }

    protected void drawParams(GraphicsContext gc, double ax, double ay) {
        if (appendParams != null) {
            List<IOParam> ioParams = appendParams.getParams();
            double height = getFontHight(gc) + (float) 4.0;
            int i = 0;
            for (IOParam ioParam : ioParams) {
                if (ioParam.getParamType() != AppendParams.ParamType.Element) {
                    continue;
                }
                String id = ioParam.getId();
                if (displayLevel != Level.Progress) {
                    IOParam.ValueType type = ioParam.getType();
                    if (type == IOParam.ValueType.REAL) {
                        id += "<R>";
                    } else if (type == IOParam.ValueType.INT) {
                        id += "<I>";
                    } else if (type == IOParam.ValueType.BOOL) {
                        id += "<B>";
                    } else if (type == IOParam.ValueType.LOGI_VAL) {
                        id += "<" + ioParam.getUnit() + ">";
                    }
                }
                if (displayLevel == Level.Progress) {
                    id += DisplayValues.getInstance().getDisplayData(nodeId, id);
                }
                double width = getFontWidth(gc, id);
                gc.setFill(STROKE2_COLOR);
                gc.fillRect(ax - 2, (ay - height + 4) + height * i, width + 2, height + 2);
                if (ioParam.isInitData()) {
                    gc.setFill(STROKE_INIT_COLOR);
                } else {
                    gc.setFill(STROKE_COLOR);
                }
                gc.fillText(id, ax, ay + height * i);
                i++;
            }
        }

    }

    @Override
    public String toString() {
        return etype + "," + nodeId + ", (" + getBaseRect().getX() + ", " + getBaseRect().getY() + ")";
    }

    @Override
    public void parseJson(JSONObject ob) {
        String xy = ob.optString("xy");
        String[] tk = xy.split(",");
        double x = getDouble(tk[0].trim());
        double y = getDouble(tk[1].trim());
        setPoint(x, y);
        rootElement = ob.optBoolean("root", false);
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
            switch (etype) {
                case CONTROLLER:
                    jobj.accumulate("type", "controller");
                    break;
                case ACTUATOR:
                    jobj.accumulate("type", "actuator");
                    break;
                case SENSOR:
                    jobj.accumulate("type", "sensor");
                    break;
                case CONTROLLED_EQUIPMENT:
                    jobj.accumulate("type", "equipment");
                    break;
                case INJECTOR:
                    jobj.accumulate("type", "injector");
                    break;
            }
            jobj.accumulate("id", nodeId);
            jobj.accumulate("xy", getBaseRect().getX() + "," + getBaseRect().getY());
            if (appendParams != null) {
                appendParams.addJson(AppendParams.ParamType.Element, jobj);
            }
            jobj.accumulate("root", rootElement);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        jj.accumulate("element", jobj);
    }

    /**
     * @return the tempFlag
     */
    public boolean isTempFlag() {
        return tempFlag;
    }

    /**
     * @param tempFlag the tempFlag to inject
     */
    public void setTempFlag(boolean tempFlag) {
        this.tempFlag = tempFlag;
    }

    /**
     * @return the order
     */
    public int getOrder() {
        return order;
    }

    /**
     * @param order the order to inject
     */
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public void setLevel(Level level) {
        displayLevel = level;
    }

    @Override
    public Level getLevel() {
        return displayLevel;
    }

    /**
     * @return the rootElement
     */
    public boolean isRootElement() {
        return rootElement;
    }

    /**
     * @param rootElement the rootElement to set
     */
    public void setRootElement(boolean rootElement) {
        this.rootElement = rootElement;
    }

}
