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
package tmu.fs.sim4stamp.tcp;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author  Keiichi Tsumuta
 */
public class TransObject implements java.io.Serializable {

    static final long serialVersionUID = 1L;

    private String id;
    private List<String> stValues = null;
    private List<Double> dValues = null;
    private List<Integer> intValues = null;

    public TransObject(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /**
     * @return the stValues
     */
    public List<String> getStValues() {
        return stValues;
    }

    /**
     * @param stValues the stValues to set
     */
    public void addStValue(String value) {
        if (stValues == null) {
            stValues = new ArrayList<>();
        }
        stValues.add(value);
    }

    /**
     * @return the dValues
     */
    public List<Double> getDValues() {
        return dValues;
    }

    /**
     * @param dValues the dValues to set
     */
    public void addDValue(Double value) {
        if (dValues == null) {
            dValues = new ArrayList<>();
        }
        dValues.add(value);
    }

    /**
     * @return the intValues
     */
    public List<Integer> getIntValues() {
        return intValues;
    }

    /**
     * @param iValues the intValues to set
     */
    public void addIntValue(Integer value) {
        if (intValues == null) {
            intValues = new ArrayList<>();
        }
        intValues.add(value);
    }

}
