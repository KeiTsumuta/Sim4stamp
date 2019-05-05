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

import tmu.fs.sim4stamp.model.iop.SafetyConstraintValue;

/**
 *
 * @author keiichi
 */
public class GraphData {

    public enum GhType {
        DOUBLE, INT, BOOL, LOGICAL_VALUE
    }

    private GhType ghType = GhType.DOUBLE;

    private double[] dData = new double[0];
    private int[] iData = new int[0];
    private boolean[] bData = new boolean[0];
    
    private String unit = null;
    private String[] unitValues = new String[]{"","","","","",""};

    private boolean disabled = false;

    private SafetyConstraintValue upperValue;
    private SafetyConstraintValue underValue;

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

    /**
     * @return the dData
     */
    public double[] getLogicalValueData() {
        return dData;
    }

    /**
     * @param dData the dData to set
     */
    public void setLogicalValueData(double[] dData) {
        this.dData = dData;
        ghType = GhType.LOGICAL_VALUE;
    }

    public int getDataCount() {
        try {
            if (ghType == GhType.DOUBLE) {
                return dData.length;
            } else if (ghType == GhType.INT) {
                return iData.length;
            } else if (ghType == GhType.BOOL) {
                return bData.length;
            } else if (ghType == GhType.LOGICAL_VALUE) {
                return dData.length;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * @return the disable
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * @param disable the disable to set
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    /**
     * @return the upperValue
     */
    public SafetyConstraintValue getUpperValue() {
        return upperValue;
    }

    /**
     * @param upperValue the upperValue to set
     */
    public void setUpperValue(SafetyConstraintValue upperValue) {
        this.upperValue = upperValue;
    }

    /**
     * @return the underValue
     */
    public SafetyConstraintValue getUnderValue() {
        return underValue;
    }

    /**
     * @param underValue the underValue to set
     */
    public void setUnderValue(SafetyConstraintValue underValue) {
        this.underValue = underValue;
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

    /**
     * @return the unitValues
     */
    public String[] getUnitValues() {
        return unitValues;
    }

    /**
     * @param unitValues the unitValues to set
     */
    public void setUnitValues(String[] unitValues) {
        this.unitValues = unitValues;
    }
}
