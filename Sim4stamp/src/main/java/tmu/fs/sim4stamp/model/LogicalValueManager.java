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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import tmu.fs.sim4stamp.SimService;
import tmu.fs.sim4stamp.model.lv.LogicalValue;
import tmu.fs.sim4stamp.util.ResourceFileIO;

/**
 * 5値論理管理クラス
 *
 * @author Keiichi Tsumuta
 */
public class LogicalValueManager extends ResourceFileIO implements java.io.Serializable {

	private static final String LV_INI = SimService.LV_INI;

	private String rev = "";
	private Map<String, LogicalValue> lvMap;
	private List<String> units;

	public LogicalValueManager() {
		init();
	}

	public void init() {
		rev = "";
		units = new ArrayList<>();
		lvMap = new HashMap<>();
	}

	public void readInitFile() {
		String json = getResource(LV_INI);
		JSONObject job = new JSONObject(json);
		readJson(job);
	}

	public void readJson(JSONObject job) {
		rev = job.optString("lvRev");
		JSONArray jarr = job.getJSONArray("logiacalValues");
		for (int i = 0; i < jarr.length(); i++) {
			JSONObject jo = jarr.getJSONObject(i);
			String vid = jo.optString("unit");
			String type = jo.optString("type");
			JSONArray ar = jo.getJSONArray("value");
			LogicalValue lv = new LogicalValue(vid);
			lv.setType(type);
			String[] vals = new String[6];
			for (int k = 0; k < ar.length(); k++) {
				vals[k] = ar.getString(k);
			}
			lv.setValues(vals);
			units.add(vid);
			lvMap.put(vid, lv);
			//System.out.println(lv.toString());
		}
	}

	public JSONObject getJSON() {
		JSONObject job = new JSONObject();
		job.accumulate("lvRev", rev);
		for (String unit : units) {
			JSONObject ju = new JSONObject();
			ju.accumulate("unit", unit);
			LogicalValue lv = lvMap.get(unit);
			String[] values = lv.getValues();
			for (int i = 0; i < values.length; i++) {
				ju.accumulate("value", values[i]);
			}
			String type = lv.getType();
			ju.accumulate("type", type);
			job.accumulate("logiacalValues", ju);
		}
		return job;
	}

	public List<String> getUnitList() {
		return units;
	}

	public LogicalValue getLogicalValue(String unitId) {
		LogicalValue lv = lvMap.get(unitId);
		if (lv == null && units.size() > 0) {
			// 該当単位が未登録の場合、デフォルト単位を返す
			lv = lvMap.get(units.get(0));
		}
		return lv;
	}

	public boolean isExsist(String unitId) {
		LogicalValue lv = lvMap.get(unitId);
		if (lv != null) {
			return true;
		}
		return false;
	}

	public void addLogicalValue(String unitId, LogicalValue lv) {
		LogicalValue mlv = lvMap.get(unitId);
		if (mlv == null) { // Updateはしない！
			units.add(unitId);
			lvMap.put(unitId, lv);
		}
	}

	public void setRev(String rev) {
		this.rev = rev;
	}
}
