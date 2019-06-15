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

import tmu.fs.sim4stamp.util.Deviation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import tmu.fs.sim4stamp.SimService;
import tmu.fs.sim4stamp.gui.util.GraphData;
import tmu.fs.sim4stamp.model.ConnectorManager;
import tmu.fs.sim4stamp.model.IOParamManager;
import tmu.fs.sim4stamp.model.LogicalValueManager;
import tmu.fs.sim4stamp.model.co.Connector;
import tmu.fs.sim4stamp.model.lv.LogicalValue;
import tmu.fs.sim4stamp.util.DeviationMap;
import tmu.fs.sim4stamp.util.JSONConvert;

/**
 * シミュレーション・シナリオに基づく一連の時系列データ
 *
 * @author Keiichi Tsumuta
 */
public class IOScene implements JSONConvert {

    private static double providingMoreParam = 2.0;
    private static double providingLessParam = 0.5;
    private static int deviationTooEarly = 2;
    private static int deviationTooLate = 2;

    private String scene = "XXXX";
    private int size = 0;
    private Map<String, List<IOParam>> nodeParamMap;
    private Map<String, List<IOValue>> nodeValues;
    private Map<String, IOValue> nodeValueMap;
    private List<String[]> nfntList;
    private String deviationConector = null;
    private Deviation deviation = Deviation.NORMAL;
    private String deviationConnParamId = null;
    private String deviationConnParamFromId = null;
    private String deviationConnParamToId = null;

    private double devWorkValue = 0.0;
    private double devWorkInitValue = 0.0;
    private int devWorkIntValue = 0;
    private int devWorkInitIntValue = 0;
    private boolean devWorkBoolValue = false;
    private boolean devWorkInitBoolValue = false;

    private int deviationStartIndex = 0;

    public IOScene() {
        // System.out.println("IOScen new :" + this);
    }

    public void init(Map<String, List<IOParam>> mp, List<String[]> nfnt, int size) {
        nodeParamMap = mp;
        this.size = size;
        this.nfntList = nfnt;
        nodeValues = new HashMap<>();
        nodeValueMap = new HashMap<>();
        for (String nodeId : mp.keySet()) {
            List<IOParam> list = nodeParamMap.get(nodeId);
            List<IOValue> iovs = new ArrayList<>();
            for (IOParam p : list) {
                IOValue iov = new IOValue(p.getParamType(), p, size);
                iovs.add(iov);
                nodeValueMap.put(nodeId + "_" + p.getId(), iov);
            }
            nodeValues.put(nodeId, iovs);
        }
        // System.out.println("IOScene init **");
    }

    public void dataInitSelection() {
        for (String nodeId : nodeParamMap.keySet()) {
            List<IOValue> valList = nodeValues.get(nodeId);
            for (IOValue iov : valList) {
                iov.setInitFlag(false);
            }
        }
    }

    public void remake(Map<String, List<IOParam>> mp, List<String[]> nfnt) {
        nodeParamMap = mp;
        this.nfntList = nfnt;
        for (String nodeId : mp.keySet()) {
            List<IOParam> list = nodeParamMap.get(nodeId);
            List<IOValue> valList = nodeValues.get(nodeId);
            if (valList == null) {
                List<IOValue> iovs = new ArrayList<>();
                for (IOParam p : list) {
                    IOValue iov = new IOValue(p.getParamType(), p, size);
                    iovs.add(iov);
                    nodeValueMap.put(nodeId + "_" + p.getId(), iov);
                }
                nodeValues.put(nodeId, iovs);
            } else {
                for (IOParam p : list) {
                    boolean f = false;
                    for (IOValue iov : valList) {
                        AppendParams.ParamType type = iov.getParamType();
                        String iopId = iov.getId();
                        if (p.getParamType() == type && p.getId().equals(iopId)) {
                            f = true;
                            break;
                        }
                    }
                    if (!f) {
                        IOValue iov = new IOValue(p.getParamType(), p, size);
                        valList.add(iov);
                        nodeValueMap.put(nodeId + "_" + p.getId(), iov);
                    }
                }
            }
        }
    }

    public IOScene copyClone() {
        IOScene ic = new IOScene();
        ic.init(nodeParamMap, nfntList, size);
        for (String nodeId : nodeValues.keySet()) {
            List<IOParam> list = nodeParamMap.get(nodeId);
            for (IOParam iop : list) {
                boolean initSel = isInitDataSelected(nodeId, iop.getId());
                if (initSel) {
                    ic.initDataSelect(nodeId, iop.getId());
                }
                IOValue icIoValue = ic.nodeValueMap.get(nodeId + "_" + iop.getId());
                double[] dVals = getData(nodeId, iop.getId());
                if (dVals != null) {
                    for (int i = 0; i < dVals.length; i++) {
                        icIoValue.set(i, dVals[i]);
                    }
                }
                int[] iVals = getIntData(nodeId, iop.getId());
                if (iVals != null) {
                    for (int i = 0; i < iVals.length; i++) {
                        icIoValue.set(i, iVals[i]);
                    }
                }
                boolean[] bVals = getBoolData(nodeId, iop.getId());
                if (bVals != null) {
                    for (int i = 0; i < bVals.length; i++) {
                        icIoValue.set(i, bVals[i]);
                    }
                }
                IOValue iov = getIOData(nodeId, iop.getId());
                icIoValue.setUpperValue(iov.getUpperValue());
                icIoValue.setUnderValue(iov.getUnderValue());
            }
        }
        // System.out.println("copy clone:" + deviation);
        ic.setDeviation(deviation);
        ic.scene = new String(scene);
        ic.devWorkInitValue = devWorkInitValue;
        ic.devWorkValue = devWorkValue;
        ic.devWorkInitIntValue = devWorkInitIntValue;
        ic.devWorkIntValue = devWorkIntValue;
        ic.devWorkInitBoolValue = devWorkInitBoolValue;
        ic.devWorkBoolValue = devWorkBoolValue;
        ic.deviationConnParamId = deviationConnParamId;
        ic.deviationConnParamFromId = deviationConnParamFromId;
        ic.deviationConnParamToId = deviationConnParamToId;
        ic.deviationStartIndex = deviationStartIndex;
        return ic;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int newSize) {
        // System.out.println("set size:" + newSize + ", old:" + size);
        size = newSize;
        updateSize();
    }

    public void updateSize() {
        for (String nodeId : nodeValues.keySet()) {
            List<IOValue> iovs = nodeValues.get(nodeId);
            for (IOValue iov : iovs) {
                iov.setSize(size);
            }
        }
    }

    /**
     * @return the scene
     */
    public String getScene() {
        return scene;
    }

    /**
     * @param scene the scene to set
     */
    public void setScene(String scene) {
        this.scene = scene;
    }

    public void setData(String elementId, String paramId, int index, double value) {
        // System.out.println("set:" + elementId + "," + paramId + "," + index + "," +
        // value);
        IOValue val = nodeValueMap.get(elementId + "_" + paramId);
        if (val != null) {
            val.set(index, value);
            if (val.getParamType() == AppendParams.ParamType.Connector) {
                copyNfNt(elementId, val.getParentId()[1], paramId, index);
            }
        }
    }

    public void setIntData(String elementId, String paramId, int index, int value) {
        // System.out.println("set:" + elementId + "," + paramId + "," + index + "," +
        // value);
        IOValue val = nodeValueMap.get(elementId + "_" + paramId);
        if (val != null) {
            val.set(index, value);
            if (val.getParamType() == AppendParams.ParamType.Connector) {
                copyNfNt(elementId, val.getParentId()[1], paramId, index);
            }
        }
    }

    public void setBoolData(String elementId, String paramId, int index, boolean value) {
        // System.out.println("set:" + elementId + "," + paramId + "," + index + "," +
        // value);
        IOValue val = nodeValueMap.get(elementId + "_" + paramId);
        if (val != null) {
            val.set(index, value);
            if (val.getParamType() == AppendParams.ParamType.Connector) {
                copyNfNt(elementId, val.getParentId()[1], paramId, index);
            }
        }
    }

    private void copyNfNt(String nfId, String ntId, String paramId, int index) {
        int dsi = deviationStartIndex;
        if (dsi == 0) {
            dsi = 1;
        }
        List<IOValue> listNf = nodeValues.get(nfId);
        for (IOValue ioNf : listNf) {
            if (ioNf.getId().equals(paramId)) {
                List<IOValue> listNt = nodeValues.get(ntId);
                for (IOValue ioNt : listNt) {
                    if (ioNt.getId().equals(paramId)) {
                        IOParam.ValueType type = ioNt.getType();
                        if (type == IOParam.ValueType.REAL) {
                            double sValue = 0.0;
                            if (deviation == Deviation.NORMAL || deviationConnParamId == null
                                || index < (dsi - 1)) {
                                sValue = ioNf.getDoubleValues()[index];
                            } else {
                                if (deviationConnParamFromId.equals(nfId) && deviationConnParamToId.equals(ntId)
                                    && deviationConnParamId.equals(paramId)) {
                                    sValue = getDeviationValue(ioNf, index);
                                } else {
                                    sValue = ioNf.getDoubleValues()[index];
                                }
                            }
                            ioNt.getDoubleValues()[index] = sValue;
                        } else if (type == IOParam.ValueType.INT) {
                            int iValue = 0;
                            if (deviation == Deviation.NORMAL || deviationConnParamId == null
                                || index < (dsi - 1)) {
                                iValue = ioNf.getIntValues()[index];
                            } else {
                                if (deviationConnParamFromId.equals(nfId) && deviationConnParamToId.equals(ntId)
                                    && deviationConnParamId.equals(paramId)) {
                                    iValue = getDeviationIntValue(ioNf, index);
                                } else {
                                    iValue = ioNf.getIntValues()[index];
                                }
                            }
                            ioNt.getIntValues()[index] = iValue;
                        } else if (type == IOParam.ValueType.BOOL) {
                            boolean bValue = false;
                            if (deviation == Deviation.NORMAL || deviationConnParamId == null
                                || index < (dsi - 1)) {
                                bValue = ioNf.getBoolValues()[index];
                            } else {
                                if (deviationConnParamFromId.equals(nfId) && deviationConnParamToId.equals(ntId)
                                    && deviationConnParamId.equals(paramId)) {
                                    bValue = getDeviationBoolValue(ioNf, index);
                                } else {
                                    bValue = ioNf.getBoolValues()[index];
                                }
                            }
                            ioNt.getBoolValues()[index] = bValue;
                        }
                        if (type == IOParam.ValueType.LOGI_VAL) {
                            double sValue = 0.0;
                            if (deviation == Deviation.NORMAL || deviationConnParamId == null
                                || index < (dsi - 1)) {
                                sValue = ioNf.getDoubleValues()[index];
                            } else {
                                if (deviationConnParamFromId.equals(nfId) && deviationConnParamToId.equals(ntId)
                                    && deviationConnParamId.equals(paramId)) {
                                    sValue = getDeviationLogicalValue(ioNf, index);
                                } else {
                                    sValue = ioNf.getDoubleValues()[index];
                                }
                            }
                            ioNt.getDoubleValues()[index] = sValue;
                        }
                        return;
                    }
                }
            }
        }
    }

    public void initDataSelect(String elementId, String paramId) {
        IOValue val = nodeValueMap.get(elementId + "_" + paramId);
        if (val != null) {
            val.setInitFlag(true);
        }
    }

    public boolean isInitDataSelected(String elementId, String paramId) {
        IOValue val = nodeValueMap.get(elementId + "_" + paramId);
        if (val != null) {
            return val.isInitFlag();
        }
        return false;
    }

    private double getDeviationValue(IOValue ioNf, int index) {
        double val = ioNf.getDoubleValues()[index];
        switch (deviation) {
            case NOT_PROVIDING:
                if (index == 0) {
                    devWorkValue = val;
                } else {
                    val = devWorkValue;
                }
                break;
            case PROVIDING_MORE:
                val = val * getProvidingMoreParam();
                break;
            case PROVIDING_LESS:
                val = val * getProvidingLessParam();
                break;
            case TOO_EARLY:
                break;
            case TOO_LATE:
                if (index < getDeviationTooLate()) {
                    val = 0.0;
                } else {
                    val = ioNf.getDoubleValues()[index - getDeviationTooLate()];
                }
                break;
            case WRONG_ORDER:
                break;
            case STOPPING_TOO_SOON:
                if (index == 0) {
                    devWorkInitValue = val;
                    devWorkValue = val;
                } else {
                    if (val > devWorkValue) {
                        devWorkValue = val;
                    } else {
                        val = devWorkInitValue;
                    }
                }
                break;
            case APPLYING_TOO_LONG:
                if (index == 0) {
                    devWorkValue = val;
                } else {
                    val = Math.max(devWorkValue, val);
                    devWorkValue = val;
                }
                break;
        }
        return val;
    }

    private int getDeviationIntValue(IOValue ioNf, int index) {
        int val = ioNf.getIntValues()[index];
        switch (deviation) {
            case NOT_PROVIDING:
                if (index == 0) {
                    devWorkIntValue = val;
                } else {
                    val = devWorkIntValue;
                }
                break;
            case PROVIDING_MORE:
                val = (int) (val * getProvidingMoreParam());
                break;
            case PROVIDING_LESS:
                val = (int) (val * getProvidingLessParam());
                break;
            case TOO_EARLY:
                break;
            case TOO_LATE:
                if (index < getDeviationTooLate()) {
                    val = 0;
                } else {
                    val = ioNf.getIntValues()[index - getDeviationTooLate()];
                }
                break;
            case WRONG_ORDER:
                break;
            case STOPPING_TOO_SOON:
                if (index == 0) {
                    devWorkInitIntValue = val;
                    devWorkIntValue = val;
                } else {
                    if (val > devWorkIntValue) {
                        devWorkIntValue = val;
                    } else {
                        val = devWorkInitIntValue;
                    }
                }
                break;
            case APPLYING_TOO_LONG:
                if (index == 0) {
                    devWorkValue = val;
                } else {
                    val = Math.max(devWorkIntValue, val);
                    devWorkIntValue = val;
                }
                break;
        }
        return val;
    }

    private boolean getDeviationBoolValue(IOValue ioNf, int index) {
        boolean val = ioNf.getBoolValues()[index];
        switch (deviation) {
            case NOT_PROVIDING:
                if (index == 0) {
                    devWorkBoolValue = val;
                } else {
                    val = devWorkBoolValue;
                }
                break;
            case PROVIDING_MORE:
                if (index == 0) {
                    devWorkInitBoolValue = val;
                } else {
                    val = !devWorkInitBoolValue;
                }
                break;
            case PROVIDING_LESS:
                if (index == 0) {
                    devWorkInitBoolValue = val;
                } else {
                    val = !devWorkInitBoolValue;
                }
                break;
            case TOO_EARLY:
                break;
            case TOO_LATE:
                if (index < getDeviationTooLate()) {
                    val = false;
                } else {
                    val = ioNf.getBoolValues()[index - getDeviationTooLate()];
                }
                break;
            case WRONG_ORDER:
                break;
            case STOPPING_TOO_SOON:
                if (index == 0) {
                    devWorkInitBoolValue = val;
                    devWorkBoolValue = val;
                } else {
                    if (val != devWorkBoolValue) {
                        devWorkBoolValue = val;
                    } else {
                        val = devWorkInitBoolValue;
                    }
                }
                break;
            case APPLYING_TOO_LONG:
                if (index == 0) {
                    devWorkBoolValue = val;
                } else {
                    val = devWorkBoolValue || val;
                    devWorkBoolValue = val;
                }
                break;
        }
        return val;
    }

    private double getDeviationLogicalValue(IOValue ioNf, int index) {
        double val = ioNf.getDoubleValues()[index];
        if (val == 0.0) {
            return 0.0; // 不明値の場合は不明値を返す！
        }
        switch (deviation) {
            case NOT_PROVIDING:
                if (index == 0) {
                    devWorkValue = val;
                } else {
                    val = devWorkValue;
                }
                break;
            case PROVIDING_MORE:
                val = val * 1.35;
                break;
            case PROVIDING_LESS:
                val = val * 0.7;
                break;
            case TOO_EARLY:
                break;
            case TOO_LATE:
                if (index < getDeviationTooLate()) {
                    val = 1.0;
                } else {
                    val = ioNf.getDoubleValues()[index - getDeviationTooLate()];
                }
                break;
            case WRONG_ORDER:
                break;
            case STOPPING_TOO_SOON:
                if (index == 0) {
                    devWorkInitValue = val;
                    devWorkValue = val;
                } else {
                    if (val > devWorkValue) {
                        devWorkValue = val;
                    } else {
                        val = devWorkInitValue;
                    }
                }
                break;
            case APPLYING_TOO_LONG:
                if (index == 0) {
                    devWorkValue = val;
                } else {
                    val = Math.max(devWorkValue, val);
                    devWorkValue = val;
                }
                break;
        }
        if (val > 5.0) {
            val = 5.0;
        } else if (val < 1.0) {
            val = 1.0;
        }
        return val;
    }

    public IOValue getIOData(String elementId, String paramId) {
        if (nodeValues != null) {
            IOValue iv = nodeValueMap.get(elementId + "_" + paramId);
            if (iv != null) {
                return iv;
            }
        }
        return null;
    }

    public double[] getData(String elementId, String paramId) {
        IOValue iv = getIOData(elementId, paramId);
        if (iv != null) {
            return iv.getDoubleValues();
        }
        return new double[size];
    }

    public int[] getIntData(String elementId, String paramId) {
        IOValue iv = getIOData(elementId, paramId);
        if (iv != null) {
            return iv.getIntValues();
        }
        return new int[size];
    }

    public boolean[] getBoolData(String elementId, String paramId) {
        IOValue iv = getIOData(elementId, paramId);
        if (iv != null) {
            return iv.getBoolValues();
        }
        return new boolean[size];
    }

    public GraphData getGraphData(String elementId, String paramId) {
        GraphData gd = new GraphData();
        IOValue iv = getIOData(elementId, paramId);
        if (iv != null) {
            IOParam.ValueType type = iv.getType();
            if (type == IOParam.ValueType.REAL) {
                gd.setDoubleData(iv.getDoubleValues());
            } else if (type == IOParam.ValueType.INT) {
                gd.setIntData(iv.getIntValues());
            } else if (type == IOParam.ValueType.BOOL) {
                gd.setBoolData(iv.getBoolValues());
            } else if (type == IOParam.ValueType.LOGI_VAL) {
                gd.setLogicalValueData(iv.getDoubleValues());
                String unit = iv.getUnit();
                gd.setUnit(unit);
                LogicalValue lv = LogicalValueManager.getLogicalValue(unit);
                gd.setUnitValues(lv.getValues());
            }
            gd.setUpperValue(iv.getSafetyConstraintUpper());
            gd.setUnderValue(iv.getSafetyConstraintUnder());
        }
        return gd;
    }

    @Override
    public void parseJson(JSONObject sj) {
        JSONObject obj = sj.getJSONObject("scene");
        scene = obj.optString("sceneid");
        size = obj.optInt("size");
        if (size <= 0) {
            size = IOParamManager.INIT_DATA_SIZE;
        }
        JSONArray ioValObj = obj.optJSONArray("values");
        int len = ioValObj.length();
        for (int i = 0; i < len; i++) {
            JSONObject ob = ioValObj.getJSONObject(i);
            String eleId = ob.optString("eleid");
            // System.out.println("***** eleId:" + eleId);
            List<IOValue> iovals = nodeValues.get(eleId);
            if (iovals == null) {
                continue;
            }
            Map<String, IOValue> ioMap = new HashMap<>();
            for (IOValue ioVal : iovals) {
                ioVal.setSize(size);
                ioMap.put(ioVal.getId(), ioVal);
                // System.out.println("IO map put:" + ioVal.getId() + "," + ioVal);
            }
            JSONArray arr = ob.getJSONArray("ioValues");
            for (int k = 0; k < arr.length(); k++) {
                JSONObject ob2 = arr.optJSONObject(k);
                if (ob2 != null) {
                    // System.out.println("******** id:" + ob2.toString());
                    JSONObject ob3 = ob2.getJSONObject("iovalue");
                    String id = ob3.optString("id");
                    if (id != null) {
                        IOValue iov = ioMap.get(id);
                        if (iov != null) { // typeがElementの場合設定あり
                            iov.parseJson(ob3);
                        }
                    }
                }
            }
        }
        JSONObject devObj = obj.optJSONObject("deviation");
        if (devObj != null) {
            String kind = devObj.optString("kind"); // 今のところ固定値
            String deviationId = devObj.optString("deviationid");
            String connectorParamId = devObj.optString("paramid");
            this.deviation = DeviationMap.getDeviation(deviationId);
            setDeviationConnection(connectorParamId);
        } else {
            deviation = Deviation.NORMAL;
        }
        setDeviationStartIndex(obj.optInt("dStartIdx"));

        // System.out.println("parse deviaiton:" + deviation + ", " + this);
    }

    public void setDeviationConnection(String connectorParamId) {
        ConnectorManager cm = SimService.getInstance().getConnectorManager();
        List<Connector> connectors = cm.getConnectors();
        for (Connector connector : connectors) {
            AppendParams ap = connector.getAppendParams();
            if (ap != null && ap.getPramType() == AppendParams.ParamType.Connector) {
                List<IOParam> plist = ap.getParams();
                if (plist != null) {
                    for (IOParam ip : plist) {
                        if (ip.getId().equals(connectorParamId)) {
                            setDeviationSetting(connectorParamId, connector.getNodeFromId(), connector.getNodeToId());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void addJSON(JSONObject ob) {
        JSONObject obj = new JSONObject();
        obj.accumulate("sceneid", scene);
        obj.accumulate("size", size);
        List<JSONObject> arr0 = new ArrayList<>();
        Iterator<String> it = nodeValues.keySet().iterator();
        while (it.hasNext()) {
            String eleid = it.next();
            JSONObject obj2 = new JSONObject();
            obj2.accumulate("eleid", eleid);
            List<JSONObject> arr = new ArrayList<>();
            List<IOValue> ioValues = nodeValues.get(eleid);
            for (IOValue ioValue : ioValues) {
                JSONObject jobj3 = new JSONObject();
                ioValue.addJSON(jobj3);

                arr.add(jobj3);
            }
            obj2.accumulate("ioValues", arr);
            arr0.add(obj2);
        }
        obj.accumulate("values", arr0);
        obj.accumulate("dStartIdx", getDeviationStartIndex());
        JSONObject devObj = new JSONObject();
        devObj.accumulate("kind", "connector");
        if (deviation == null) {
            deviation = Deviation.NORMAL;
        }
        devObj.accumulate("deviationid", deviation.getId());
        if (getDeviationConnParamId() != null) {
            devObj.accumulate("paramid", getDeviationConnParamId());
        }
        obj.accumulate("deviation", devObj);
        ob.accumulate("scene", obj);
    }

    /**
     * @return the deviation
     */
    public Deviation getDeviation() {
        return deviation;
    }

    /**
     * @param deviation the deviation to set
     */
    public void setDeviation(Deviation deviation) {
        this.deviation = deviation;
        // System.out.println("setDeviation :" + deviation + ", " + this);
    }

    /**
     *
     */
    private void setDeviationSetting(String paramId, String from, String to) {
        this.deviationConnParamId = paramId;
        this.deviationConnParamFromId = from;
        this.deviationConnParamToId = to;
        // System.out.println("setDeviationSetting id:" + paramId);
    }

    /**
     * @return the providingMore
     */
    public static double getProvidingMoreParam() {
        return providingMoreParam;
    }

    /**
     * @param providingMore the providingMoreParam to set
     */
    public static void setProvidingMoreParam(double providingMore) {
        providingMoreParam = providingMore;
    }

    /**
     * @return the providingLess
     */
    public static double getProvidingLessParam() {
        return providingLessParam;
    }

    /**
     * @param providingLess the providingLessParam to set
     */
    public static void setProvidingLessParam(double providingLess) {
        providingLessParam = providingLess;
    }

    /**
     * @return the deviationDelayTimes
     */
    public static int getDeviationTooEarly() {
        return deviationTooEarly;
    }

    /**
     * @param deviationDelay the deviationDelayTimes to set
     */
    public static void setDeviationTooEarly(int delayTimes) {
        deviationTooEarly = delayTimes;
    }

    /**
     * @return the deviationTooLate
     */
    public static int getDeviationTooLate() {
        return deviationTooLate;
    }

    /**
     * @param aDeviationTooLate the deviationTooLate to set
     */
    public static void setDeviationTooLate(int aDeviationTooLate) {
        deviationTooLate = aDeviationTooLate;
    }

    /**
     * @return the deviationConnParamId
     */
    public String getDeviationConnParamId() {
        return deviationConnParamId;
    }

    /**
     * @return the deviationStartIndex
     */
    public int getDeviationStartIndex() {
        return deviationStartIndex;
    }

    /**
     * @param deviationStartIndex the deviationStartIndex to set
     */
    public void setDeviationStartIndex(int deviationStartIndex) {
        this.deviationStartIndex = deviationStartIndex;
    }

    public boolean isDeviationConnector(Connector c) {
        try {
            if (deviationConnParamFromId.equals(c.getNodeFromId()) && deviationConnParamToId.equals(c.getNodeToId())) {
                AppendParams ap = c.getAppendParams();
                List<IOParam> aps = ap.getParams();
                if (aps.size() > 0) {
                    if (deviationConnParamId.equals(aps.get(0).getId())) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {

        }
        return false;
    }
}
