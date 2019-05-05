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
package tmu.fs.sim4stamp.model.iop;

import org.json.JSONArray;
import org.json.JSONObject;
import tmu.fs.sim4stamp.util.JSONConvert;

/**
 *
 * @author Keiichi Tsumuta
 */
public class IOParam implements JSONConvert {

    public enum ValueType { //
        REAL, // 浮動小数点
        INT, // 整数
        BOOL, // true/false
        LOGI_VAL  // Logical Value
    }

    private final AppendParams.ParamType pType; // Element or Connector
    private final String[] parentId; // pTypeがConnectorのみ有意（Elementは「null」）
    private final String id;
    private final ValueType vType;
    private String unit;
    private boolean initData;

    public IOParam(AppendParams.ParamType ptype, String[] parentId, String id, ValueType type, String unit) {
        this.pType = ptype;
        this.parentId = parentId;
        this.id = id;
        this.vType = type;
        this.initData = false;
        this.unit = unit;
    }

    public AppendParams.ParamType getParamType() {
        return pType;
    }

    public String[] getParentId() {
        return parentId;
    }

    public String getId() {
        return id;
    }

    public ValueType getType() {
        return vType;
    }

    public String getTypeToString() {
        String s = "";
        switch (vType) {
            case REAL:
                s = "real";
                break;
            case INT:
                s = "int";
                break;
            case BOOL:
                s = "bool";
                break;
            case LOGI_VAL:
                s = "logic";
                break;
        }
        return s;
    }

    /**
     * @return the initData
     */
    public boolean isInitData() {
        return initData;
    }

    /**
     * @param initData the initData to set
     */
    public void setInitData(boolean initData) {
        this.initData = initData;
    }

    /**
     * @return the unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * @param unit the unit to set
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public void parseJson(JSONObject sj) {

    }

    @Override
    public void addJSON(JSONObject jj) {
        JSONObject jobj = new JSONObject();
        jobj.accumulate("type", getTypeToString());
        if (unit != null && unit.length() > 0) {
            jobj.accumulate("unit", unit);
        }
        jobj.accumulate("id", id);
        JSONArray arr = new JSONArray();
        for (String pi : parentId) {
            arr.put(pi);
        }
        jobj.accumulate("parent", arr);
        jj.accumulate("ioparam", jobj);
    }

    public String toString() {
        return parentId + "," + id + "," + getTypeToString();
    }

}
