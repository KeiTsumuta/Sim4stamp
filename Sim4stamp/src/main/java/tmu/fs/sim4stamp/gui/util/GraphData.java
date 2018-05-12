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
package tmu.fs.sim4stamp.gui.util;

/**
 *
 * @author keiichi
 */
public class GraphData {

	public enum GhType {
		DOUBLE, INT, BOOL
	}

	private GhType ghType = GhType.DOUBLE;

	private double[] dData = new double[0];
	private int[] iData = new int[0];
	private boolean[] bData = new boolean[0];

	public GraphData() {
	}

	/**
	 * @return the ghType
	 */
	public GhType getGhType() {
		return ghType;
	}

	/**
	 * @return the dData
	 */
	public double[] getDoubleData() {
		return dData;
	}

	/**
	 * @param dData the dData to set
	 */
	public void setDoubleData(double[] dData) {
		this.dData = dData;
		ghType = GhType.DOUBLE;
	}

	/**
	 * @return the iData
	 */
	public int[] getIntData() {
		return iData;
	}

	/**
	 * @param iData the iData to set
	 */
	public void setIntData(int[] iData) {
		this.iData = iData;
		ghType = GhType.INT;
	}

	/**
	 * @return the bData
	 */
	public boolean[] getBoolData() {
		return bData;
	}

	/**
	 * @param bData the bData to set
	 */
	public void setBoolData(boolean[] bData) {
		this.bData = bData;
		ghType = GhType.BOOL;
	}

}
