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
package tmu.fs.sim4stamp.model;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import tmu.fs.sim4stamp.model.lv.LogicalValue;
import tmu.fs.sim4stamp.util.ResourceFileIO;

/**
 * 5値論理管理クラス
 *
 * @author Keiichi Tsumuta
 */
public class LogicalValueManager extends ResourceFileIO implements java.io.Serializable {

    private static final String LV_INI = "/lv/logicalValues.json";

    private static LogicalValueManager logicalValueManager = new LogicalValueManager();

    private Map<String, LogicalValue> lvMap = new HashMap<>();

    private LogicalValueManager() {
    }

    public static LogicalValueManager getInstance() {
        return logicalValueManager;
    }

    public void readInitFile() {
        String json = getResource(LV_INI);
        JSONObject job = new JSONObject(json);
        JSONArray jarr = job.getJSONArray("logiacalValues");
        for (int i = 0; i < jarr.length(); i++) {
            JSONObject jo = jarr.getJSONObject(i);
            String vid = jo.optString("kind");
            JSONArray ar = jo.getJSONArray("value");
            LogicalValue lv = new LogicalValue(vid);
            String[] vals = new String[6];
            for (int k = 0; k < ar.length(); k++) {
                vals[k] = ar.getString(k);
            }
            lv.setValues(vals);
            lvMap.put(vid, lv);
            //System.out.println(lv.toString());
        }
    }

}
