/*
 *	 sim4stamp - The simulation tool for STAMP/STPA
 *	 Copyright (C) 2017  Keiichi Tsumuta
 *
 *	 This program is free software: you can redistribute it and/or modify
 *	 it under the terms of the GNU General Public License as published by
 *	 the Free Software Foundation, either version 3 of the License, or
 *	 (at your option) any later version.
 *
 *	 This program is distributed in the hope that it will be useful,
 *	 but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	 GNU General Public License for more details.
 *
 *	 You should have received a copy of the GNU General Public License
 *	 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tmu.fs.sim4stamp.gui.util;

/**
 * 結果表の値（値と逸脱有無）を保持するオブジェクト。
 *
 * @author Keiichi Tsumuta
 */
public class ResultValue {

	private String value;
	private boolean upper;
	private boolean under;

	public ResultValue() {

	}

	public ResultValue(String value, boolean upper, boolean under) {
		this.value = value;
		this.upper = upper;
		this.under = under;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	public String getResult() {
		if (upper || under) {
			return "* " + value;
		}
		return value;
	}

	/**
	 * @return the upper
	 */
	public boolean isUpper() {
		return upper;
	}

	/**
	 * @param upper the upper to set
	 */
	public void setUpper(boolean upper) {
		this.upper = upper;
	}

	/**
	 * @return the under
	 */
	public boolean isUnder() {
		return under;
	}

	/**
	 * @param under the under to set
	 */
	public void setUnder(boolean under) {
		this.under = under;
	}

}
