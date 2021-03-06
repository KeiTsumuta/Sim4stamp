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
import javafx.scene.canvas.GraphicsContext;

/**
 *
 * @author Keiichi Tsumuta
 */
public class ControllledEquipment extends Element {

	private static final int RECT_WIDTH = 150;
	private static final int RECT_HIGHT = 100;
	private static final Color FILL_COLOR = Color.KHAKI;
	private static final Color STROKE_COLOR = Color.BLACK;

	public ControllledEquipment(String id) {
		super(EType.CONTROLLED_EQUIPMENT, id, id, RECT_WIDTH, RECT_HIGHT);
	}

	public ControllledEquipment(String id, String title) {
		super(EType.CONTROLLED_EQUIPMENT, id, title, RECT_WIDTH, RECT_HIGHT);
	}

	@Override
	public void draw(GraphicsContext gc) {
		double x0 = getBaseRect().getX();
		double y0 = getBaseRect().getY();
		gc.setFill(FILL_COLOR);
		gc.fillRect(x0, y0, RECT_WIDTH, RECT_HIGHT);

		setSelectState(gc, STROKE_COLOR, SELECTED_COLOR);
		gc.strokeRect(x0, y0, RECT_WIDTH, RECT_HIGHT);

		double width = getFontWidth(gc, title);
		double height = getFontHight(gc);
		gc.setFill(STROKE_COLOR);
		if (getLevel() == Level.Base) {
			gc.fillText("Controllled Equipment", x0 + 8, y0 + height);
			gc.fillText(title, x0 + (RECT_WIDTH - width) / 2, y0 + (RECT_HIGHT + height) / 2);
		} else {
			gc.fillText(title, x0 + 8, y0 + height);
			drawParams(gc, x0 + 30, y0 + 4 + height * 2);
		}
	}

	@Override
	public ControllledEquipment clone() {
		ControllledEquipment c = new ControllledEquipment(nodeId, title);
		c.setOrder(getOrder());
		c.setAppendParams(getAppendParams());
		return c;
	}

	public String toString() {
		return super.toString();
	}
}
