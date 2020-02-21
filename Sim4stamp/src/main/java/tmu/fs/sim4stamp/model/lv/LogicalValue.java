/*
 *   sim4stamp - The simulation tool for STAMP/STPA
 *   Copyright (C) 2019  Keiichi Tsumuta
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
package tmu.fs.sim4stamp.model.lv;

/**
 * 5値論理に基づく値クラス
 *
 * @author Keiichi Tsumuta
 */
public class LogicalValue {

    private String unitId;
    private String[] values;
    private int logicValue;
	private String type;

    public LogicalValue(String vid) {
        this.unitId = vid;
    }

    /**
     * @return the unitId
     */
    public String getUnitId() {
        return unitId;
    }

    /**
     * @return the values
     */
    public String[] getValues() {
        return values;
    }

    /**
     * @param values the values to set
     */
    public void setValues(String[] values) {
        if (values.length == 6) {
            this.values = values;
        }
    }

    /**
     * @return the logicValue
     */
    public int getLogicValue() {
        return logicValue;
    }

    /**
     * @param logicValue the logicValue to set
     */
    public void setLogicValue(int logicValue) {
        if (logicValue >= 0) {
            if (logicValue <= 5) {
                this.logicValue = logicValue;
            } else {
                this.logicValue = 5;
            }
        } else {
            this.logicValue = 0;
        }
    }

    public String getLogicalName() {
        if (values != null && values.length == 6) {
            return values[logicValue];
        }
        return Integer.toString(logicValue);
    }

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("unitId :").append(unitId).append(" (");
        sb.append(getLogicalName()).append(") : ");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(values[i]);
        }
        return sb.toString();
    }
}
