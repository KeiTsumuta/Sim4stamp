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
 * グラフの縦軸を管理するクラス
 *
 * @author Keiichi Tsumuta
 */
public class GraphAxis {

	private static final double[][] axisList = {
		{0.0, 2.0, 4.0, 6.0, 8.0, 10.0},
		{0.0, 2.0, 4.0, 6.0, 8.0},
		{0.0, 2.0, 4.0, 6.0},
		{0.0, 1.0, 2.0, 3.0, 4.0, 5.0},
		{0.0, 1.0, 2.0, 3.0, 4.0},
		{0.0, 1.0, 2.0, 3.0},
		{0.0, 0.5, 1.0, 1.5, 2.0},
		{0.0, 0.5, 1.0, 1.5},
		{0.0, 0.2, 0.4, 0.6, 0.8, 1.0, 1.2}
	};

	private int pow = 0;

	public GraphAxis() {

	}

	public double[] getScale(double max, double min) {
		double[] retList = new double[]{0.0, 1.0};
		int axizIndex = 0;
		pow = 0;
		if (max > 0) {
			if (min >= 0.0) {
				pow = getPow(max);
				axizIndex = getAxisIndex(pow, max);
				retList = axisList[axizIndex];
			} else {
				if ((-1) * min > max) { // 最低値がマイナスで絶対値がmaxより大きい
					pow = getPow((-1) * min);
					axizIndex = getAxisIndex(pow, (-1) * min);
					double[] axiwk = axisList[axizIndex];
					int i = 0;
					double maxNor = max / Math.pow(10, pow);
					for (i = 1; i < axiwk.length; i++) {
						if (axiwk[i] > maxNor) {
							break;
						}
					}
					double[] wk = new double[axiwk.length + i];
					for (int k = 0; k < axiwk.length - 1; k++) {
						wk[k] = axiwk[axiwk.length - k - 1] * (-1.0);
					}
					for (int k = 0; k <= i; k++) {
						wk[k + axiwk.length - 1] = axiwk[k];
					}
					retList = wk;
				} else {
					pow = getPow(max);
					axizIndex = getAxisIndex(pow, max);
					double[] axiwk = axisList[axizIndex];
					int i = 0;
					double minNor = (-1) * min / Math.pow(10, pow);
					for (i = 1; i < axiwk.length; i++) {
						if (axiwk[i] > minNor) {
							break;
						}
					}
					double[] wk = new double[axiwk.length + i];
					for (int k = 0; k < i; k++) {
						wk[k] = axiwk[i - k] * (-1.0);
					}
					for (int k = 0; k < axiwk.length; k++) {
						wk[k + i] = axiwk[k];
					}
					retList = wk;
				}
			}
		} else if (max <= 0 && min < 0) {
			// max,minがともにマイナス値
			pow = getPow(min);
			axizIndex = getAxisIndex(pow, (-1) * min);
			double[] axs = new double[axisList[axizIndex].length];
			for (int i = 0; i < axs.length; i++) {
				axs[i] = (-1) * axisList[axizIndex][axisList[axizIndex].length - i - 1];
			}
			retList = axs;
		}
		return retList;
	}

	public int getPow() {
		return pow;
	}

	private static int getPow(double d) {
		int p = 0;
		if (d != 0) {
			double ad = Math.abs(d);
			if (ad >= 1.0) {
				for (int i = 0; i < 10; i++) {
					if (ad < 10.0) {
						p = i;
						break;
					}
					ad = ad / 10.0;
				}
			} else {
				for (int i = 0; i < 10; i++) {
					if (ad > 1.0 && ad < 10.0) {
						p = -i;
						break;
					}
					ad = ad * 10;
				}
			}
		}
		return p;
	}

	private static int getAxisIndex(int pow, double value) {
		double norm = value / Math.pow(10, pow);
		for (int i = axisList.length - 1; i >= 0; i--) {
			double vm = axisList[i][axisList[i].length - 1];
			if (norm < vm) {
				return i;
			}
		}
		return 0;
	}

}
