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

import javafx.scene.paint.Color;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

/**
 *
 * @author Keiichi Tsumuta
 */
public class Actuator extends Element {

    private static final int RECT_WIDTH = 150;
    private static final int RECT_HIGHT = 80;
    private static final Color FILL_COLOR = Color.HONEYDEW;
    private static final Color STROKE_COLOR = Color.BLACK;

    public Actuator(String id) {
        super(EType.ACTUATOR, id, id, RECT_WIDTH, RECT_HIGHT);
    }

    public Actuator(String id, String title) {
        super(EType.ACTUATOR, id, title, RECT_WIDTH, RECT_HIGHT);
    }

    @Override
    public void draw(GraphicsContext gc) {
        double x0 = baseRect.getX();
        double y0 = baseRect.getY();
        gc.setFill(FILL_COLOR);
        gc.fillRect(x0, y0, RECT_WIDTH, RECT_HIGHT);

        setSelectState(gc, STROKE_COLOR, SELECTED_COLOR);
        gc.strokeRect(x0, y0, RECT_WIDTH, RECT_HIGHT);

        double width = getFontWidth(gc, title);
        double height = getFontHight(gc);
        gc.setFill(STROKE_COLOR);
        if (getLevel() == Level.Base) {
            gc.fillText(title, x0 + (RECT_WIDTH - width) / 2, y0 + (RECT_HIGHT + height) / 2);
        } else {
            gc.fillText(title, x0 + 8, y0 + height);
            drawParams(gc, x0 + 30, y0 + 4 + height * 2);
        }
    }

    @Override
    public Actuator clone() {
        Actuator a = new Actuator(nodeId, title);
        a.setOrder(getOrder());
        a.setAppendParams(getAppendParams());
        return a;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
