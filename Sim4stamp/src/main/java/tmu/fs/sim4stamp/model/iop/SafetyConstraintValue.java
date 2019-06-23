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
package tmu.fs.sim4stamp.model.iop;

import java.text.DecimalFormat;

/**
 * 安全制約設定値
 *
 * @author Keiichi Tsumuta
 */
public class SafetyConstraintValue {

	private static final DecimalFormat D_FORMAT = new DecimalFormat("#0.00");

	private IOParam.ValueType valueType = IOParam.ValueType.REAL;

	private boolean setting = false;

	private double doubleValue;
	private int integerValue;
	private boolean booleanValue;

	public SafetyConstraintValue(IOParam.ValueType type) {
		valueType = type;
	}

	/**
	 * @return the setting
	 */
	public boolean isSetting() {
		return setting;
	}

	/**
	 * @param Setting the setting to set
	 */
	public void setSetting(boolean isSetting) {
		this.setting = isSetting;
	}

	/**
	 * @return the valueType
	 */
	public IOParam.ValueType getValueType() {
		return valueType;
	}

	/**
	 * @return the doubleValue
	 */
	public double getDoubleValue() {
		return doubleValue;
	}

	/**
	 * @param doubleValue the doubleValue to set
	 */
	public void setDoubleValue(double doubleValue) {
		this.doubleValue = doubleValue;
	}

	/**
	 * @return the integerValue
	 */
	public int getIntegerValue() {
		return integerValue;
	}

	/**
	 * @param integerValue the integerValue to set
	 */
	public void setIntegerValue(int integerValue) {
		this.integerValue = integerValue;
	}

	/**
	 * @return the booleanValue
	 */
	public boolean isBooleanValue() {
		return booleanValue;
	}

	/**
	 * @param booleanValue the booleanValue to set
	 */
	public void setBooleanValue(boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	public String getValue() {
		if (setting) {
			if (null != valueType) {
				switch (valueType) {
					case REAL:
						return D_FORMAT.format(doubleValue);
					case INT:
						return Integer.toString(integerValue);
					case BOOL:
						if (booleanValue) {
							return "true";
						} else {
							return "false";
						}
					case LOGI_VAL:
						return D_FORMAT.format(doubleValue);
					default:
						break;
				}
			}
		}
		return "*";
	}

	public void setVaue(String value) {
		setting = false;
		if (value != null && !value.trim().equals("*")) {
			try {
				if (null != valueType) {
					switch (valueType) {
						case REAL:
							doubleValue = Double.parseDouble(value);
							setting = true;
							break;
						case INT:
							integerValue = Integer.parseInt(value);
							setting = true;
							break;
						case BOOL:
							if (value.charAt(0) == 't') {
								booleanValue = true;
							} else {
								booleanValue = false;
							}
							setting = true;
							break;
						case LOGI_VAL:
							doubleValue = Double.parseDouble(value);
							setting = true;
							break;
						default:
							break;
					}
				}
			} catch (Exception ex) {
			}
		}
	}

	public double getConstraintValue() {
		if (setting) {
			if (null != valueType) {
				switch (valueType) {
					case REAL:
						return doubleValue;
					case INT:
						return integerValue;
					case BOOL:
						if (booleanValue) {
							return 1.0;
						} else {
							return 0.0;
						}
					case LOGI_VAL:
						return doubleValue;
					default:
						break;
				}
			}
		}
		return 0.0;
	}
}
