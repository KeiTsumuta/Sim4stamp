/*
 *   sim4stamp - The simulation tool for STAMP/STPA
 *   Copyright (C) 2018  Keiichi Tsumuta
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
package tmu.fs.sim4stamp.util;

/**
 * 部品にシミュレーション表示値を与えるクラス。
 *
 * @author keiichi
 */
public class DisplayValues {

	private static final DisplayValues displayValues = new DisplayValues();
	private DisplayItem displayItem = null;

	private DisplayValues() {

	}

	public void inject(DisplayItem displayItem) {
		this.displayItem = displayItem;
	}

	public static DisplayValues getInstance() {
		return displayValues;
	}

	public String getDisplayData(String nodeId, String id) {
		StringBuilder sb = new StringBuilder();
		if (displayItem != null) {
			sb.append("(");
			sb.append(displayItem.getValue(nodeId, id));
			sb.append(")");
		}
		return sb.toString();
	}

}
