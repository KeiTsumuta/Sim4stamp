/*
 *   sim4stamp - The simulation tool for STAMP/STPA
 *   Copyright (C) 2017  Keiichi Tsumuta
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

import org.json.JSONObject;
import tmu.fs.sim4stamp.util.JSONConvert;

/**
 * 時系列の設定・計算結果データの集合
 *
 * @author Keiichi Tsumuta
 */
public class IOValue implements JSONConvert {

	private AppendParams.ParamType paramType = AppendParams.ParamType.Element;
	private IOParam ioParam;
	private int size;
	private boolean initFlag;
	private SafetyConstraintValue upperValue;
	private SafetyConstraintValue underValue;

	private double[] doubleValues;
	private int[] intValues;
	private boolean[] boolValues;

	private boolean[] attentionsUpper;
	private boolean upperWarning;
	private boolean[] attentionsUnder;
	private boolean underWarning;

	public IOValue(AppendParams.ParamType ptype, IOParam ioParam, int size) {
		this.paramType = ptype;
		this.ioParam = ioParam;
		this.size = size;
		this.initFlag = false;
		switch (ioParam.getType()) {
			case REAL:
				doubleValues = new double[size];
				break;
			case INT:
				intValues = new int[size];
				break;
			case BOOL:
				boolValues = new boolean[size];
				break;
			case LOGI_VAL:
				doubleValues = new double[size];
				break;
		}
		upperValue = new SafetyConstraintValue(ioParam.getType());
		underValue = new SafetyConstraintValue(ioParam.getType());
		attentionsUpper = new boolean[size];
		attentionsUnder = new boolean[size];
		upperWarning = false;
		underWarning = false;
	}

	/**
	 * @return the parentId
	 */
	public String[] getParentId() {
		return ioParam.getParentId();
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return ioParam.getId();
	}

	public AppendParams.ParamType getParamType() {
		return paramType;
	}

	/**
	 * @return the type
	 */
	public IOParam.ValueType getType() {
		return ioParam.getType();
	}

	public int getSize() {
		return size;
	}

	public void setSize(int newSize) {
		if (size == newSize) {
			return;
		}
		if (size < newSize) {
			switch (ioParam.getType()) {
				case REAL:
					double[] newDoubleValues = new double[newSize];
					for (int i = 0; i < size; i++) {
						newDoubleValues[i] = doubleValues[i];
					}
					for (int i = size - 1; i < newSize; i++) {
						if (i >= 0) {
							newDoubleValues[i] = doubleValues[size - 1];
						}
					}
					doubleValues = newDoubleValues;
					break;
				case INT:
					int[] newIntValues = new int[newSize];
					for (int i = 0; i < size; i++) {
						newIntValues[i] = intValues[i];
					}
					for (int i = size - 1; i < newSize; i++) {
						if (i >= 0) {
							newIntValues[i] = intValues[size - 1];
						}
					}
					intValues = newIntValues;
					break;
				case BOOL:
					boolean[] newBoolValues = new boolean[newSize];
					for (int i = 0; i < size; i++) {
						newBoolValues[i] = boolValues[i];
					}
					for (int i = size - 1; i < newSize; i++) {
						if (i >= 0) {
							newBoolValues[i] = boolValues[size - 1];
						}
					}
					boolValues = newBoolValues;
					break;
				case LOGI_VAL:
					newDoubleValues = new double[newSize];
					for (int i = 0; i < size; i++) {
						newDoubleValues[i] = doubleValues[i];
					}
					for (int i = size - 1; i < newSize; i++) {
						if (i >= 0) {
							newDoubleValues[i] = doubleValues[size - 1];
						}
					}
					doubleValues = newDoubleValues;
					break;
			}
		}
		size = newSize;
	}

	public void set(int index, double value) {
		if (index < 0 || index >= size || doubleValues == null) {
			return;
		}
		doubleValues[index] = value;
	}

	/**
	 * @return the doubleValues
	 */
	public double[] getDoubleValues() {
		return doubleValues;
	}

	public void set(int index, int value) {
		if (index < 0 || index >= size || intValues == null) {
			return;
		}
		intValues[index] = value;
	}

	/**
	 * @return the intValues
	 */
	public int[] getIntValues() {
		return intValues;
	}

	public void set(int index, boolean value) {
		if (index < 0 || index >= size || boolValues == null) {
			return;
		}
		boolValues[index] = value;
	}

	/**
	 * @return the boolValues
	 */
	public boolean[] getBoolValues() {
		return boolValues;
	}

	@Override
	public void parseJson(JSONObject sj) {
		String list = sj.optString("list");
		String[] tokens = list.split(",");
		// String type = tokens[0];
		size = tokens.length - 1;
		switch (ioParam.getType()) {
			case REAL:
				doubleValues = new double[size];
				for (int i = 0; i < size; i++) {
					try {
						doubleValues[i] = Double.parseDouble(tokens[i + 1]);
					} catch (Exception ex) {
					}
				}
				break;
			case INT:
				intValues = new int[size];
				for (int i = 0; i < size; i++) {
					try {
						intValues[i] = Integer.parseInt(tokens[i + 1]);
					} catch (Exception ex) {
					}
				}
				break;
			case BOOL:
				boolValues = new boolean[size];
				for (int i = 0; i < size; i++) {
					if (tokens[i + 1].startsWith("t")) {
						boolValues[i] = true;
					} else {
						boolValues[i] = false;
					}
				}
				break;
			case LOGI_VAL:
				doubleValues = new double[size];
				for (int i = 0; i < size; i++) {
					try {
						doubleValues[i] = Double.parseDouble(tokens[i + 1]);
					} catch (Exception ex) {
					}
				}
				break;
		}
		initFlag = sj.optBoolean("init");
		upperValue.setVaue(sj.optString("upper", "*"));
		underValue.setVaue(sj.optString("under", "*"));
		attentionsUpper = new boolean[size];
		attentionsUnder = new boolean[size];
		//System.out.println("IOV parse:"+getId()+":"+sj.optString("id")+", "+list+","+initFlag);
	}

	@Override
	public void addJSON(JSONObject mobj) {
		JSONObject jobj = new JSONObject();
		jobj.accumulate("id", getId());
		jobj.accumulate("type", paramType);
		jobj.accumulate("init", initFlag);
		jobj.accumulate("upper", upperValue.getValue());
		jobj.accumulate("under", underValue.getValue());
		StringBuilder sb = new StringBuilder();
		switch (ioParam.getType()) {
			case REAL:
				if (doubleValues != null) {
					sb.append("REAL");
					for (double d : doubleValues) {
						sb.append(",").append(d);
					}
				}
				break;
			case INT:
				if (intValues != null) {
					sb.append("INT");
					for (int i : intValues) {
						sb.append(",").append(i);
					}
				}
				break;
			case BOOL:
				if (boolValues != null) {
					sb.append("BOOL");
					for (boolean b : boolValues) {
						sb.append(",").append(b);
					}
				}
				break;
			case LOGI_VAL:
				if (doubleValues != null) {
					sb.append(ioParam.getUnit());
					for (double d : doubleValues) {
						sb.append(",").append(d);
					}
				}
				break;
		}
		jobj.accumulate("list", sb.toString());
		mobj.accumulate("iovalue", jobj);
	}

	/**
	 * @return the initFlag
	 */
	public boolean isInitFlag() {
		return initFlag;
	}

	/**
	 * @param initFlag the initFlag to set
	 */
	public void setInitFlag(boolean initFlag) {
		this.initFlag = initFlag;
	}

	public void setUpperValue(String value) {
		upperValue.setVaue(value);
	}

	public String getUpperValue() {
		return upperValue.getValue();
	}

	public void setUnderValue(String value) {
		underValue.setVaue(value);
	}

	public String getUnderValue() {
		return underValue.getValue();
	}

	public void makeAttentions(int startIndex) {
		switch (ioParam.getType()) {
			case REAL:
				if (doubleValues != null) {
					if (upperValue.isSetting()) {
						double upper = upperValue.getDoubleValue();
						for (int i = startIndex; i < size; i++) {
							if (doubleValues[i] > upper) {
								attentionsUpper[i] = true;
							}
						}
					}
					if (underValue.isSetting()) {
						double under = underValue.getDoubleValue();
						for (int i = startIndex; i < size; i++) {
							if (doubleValues[i] < under) {
								attentionsUnder[i] = true;
							}
						}
					}
				}
				break;
			case INT:
				if (intValues != null) {
					if (upperValue.isSetting()) {
						int upper2 = upperValue.getIntegerValue();
						for (int i = startIndex; i < size; i++) {
							if (intValues[i] > upper2) {
								attentionsUpper[i] = true;
							}
						}
					}
					if (underValue.isSetting()) {
						int under2 = underValue.getIntegerValue();
						for (int i = startIndex; i < size; i++) {
							if (intValues[i] < under2) {
								attentionsUnder[i] = true;
							}
						}
					}
				}
				break;
			case BOOL:
				if (boolValues != null) {
					if (upperValue.isSetting()) {
						boolean upper3 = upperValue.isBooleanValue();
						for (int i = startIndex; i < size; i++) {
							if (boolValues[i] == upper3) {
								attentionsUpper[i] = true;
							}
						}
					}
					if (underValue.isSetting()) {
						boolean under3 = underValue.isBooleanValue();
						for (int i = startIndex; i < size; i++) {
							if (boolValues[i] == under3) {
								attentionsUnder[i] = true;
							}
						}
					}
				}
				break;
			case LOGI_VAL:
				if (doubleValues != null) {
					if (upperValue.isSetting()) {
						double upper = upperValue.getDoubleValue() - 0.5;
						for (int i = startIndex; i < size; i++) {
							if (doubleValues[i] > upper) {
								attentionsUpper[i] = true;
							}
						}
					}
					if (underValue.isSetting()) {
						double under = underValue.getDoubleValue() + 0.5;
						for (int i = startIndex; i < size; i++) {
							if (doubleValues[i] < under) {
								attentionsUnder[i] = true;
							}
						}
					}
				}
				break;
		}
		for (int i = startIndex; i < size; i++) {
			if (attentionsUpper[i]) {
				upperWarning = true;
				break;
			}
		}
		for (int i = startIndex; i < size; i++) {
			if (attentionsUnder[i]) {
				underWarning = true;
				break;
			}
		}
	}

	/**
	 * @return the attentions
	 */
	public boolean[] getAttentionsUpper() {
		return attentionsUpper;
	}

	public boolean[] getAttentionsUnder() {
		return attentionsUnder;
	}

	public SafetyConstraintValue getSafetyConstraintUpper() {
		return upperValue;
	}

	public SafetyConstraintValue getSafetyConstraintUnder() {
		return underValue;
	}

	public String getUnit() {
		return ioParam.getUnit();
	}

	/**
	 * @return the upperWarning
	 */
	public boolean isUpperWarning() {
		return upperWarning;
	}

	/**
	 * @return the underWarning
	 */
	public boolean isUnderWarning() {
		return underWarning;
	}

}
