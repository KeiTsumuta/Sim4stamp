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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Keiichi Tsumuta
 */
public class AppendParams {

	public enum ParamType {
		Element, Connector
	};

	private static final Logger log = Logger.getLogger(AppendParams.class.getPackage().getName());

	private final ParamType type;
	private List<IOParam> ioParams;

	public AppendParams(ParamType type) {
		this.type = type;
		init();
	}

	public void init() {
		ioParams = new ArrayList<>();
	}

	public ParamType getPramType() {
		return type;
	}

	public List<IOParam> getParams() {
		return ioParams;
	}

	public String addIOParam(IOParam ioParam) {
		if (ioParam == null) {
			return "エラー：未定義です。";
		}
		String id = ioParam.getId();
		for (IOParam iop : ioParams) {
			if (iop.getId().equals(id)) {
				return "エラー：すでに登録済みです。";
			}
		}

		ioParams.add(ioParam);
		return null;
	}

	public void deleteIOParam(IOParam del) {
		String id = del.getId();
		for (int i = 0; i < ioParams.size(); i++) {
			String rid = ioParams.get(i).getId();
			if (rid.equals(id)) {
				ioParams.remove(i);
				return;
			}
		}
	}

	public void parseJson(String[] parentId, JSONArray arr) {
		try {
			int len = arr.length();
			for (int i = 0; i < len; i++) {
				JSONObject ob = arr.getJSONObject(i).getJSONObject("ioparam");
				String id = ob.optString("id");
				String type = ob.optString("type");
				IOParam p = null;
				switch (type) {
				case "real":
					p = new IOParam(this.type, parentId, id, IOParam.ValueType.REAL);
					break;
				case "int":
					p = new IOParam(this.type, parentId, id, IOParam.ValueType.INT);
					break;
				case "bool":
					p = new IOParam(this.type, parentId, id, IOParam.ValueType.BOOL);
					break;
				}
				ioParams.add(p);
			}
		} catch (Exception ex) {
			log.severe(ex.toString());
		}
	}

	public void addJson(ParamType ptype, JSONObject mobj) {
		List<JSONObject> list = new ArrayList<>();
		Iterator<IOParam> it = ioParams.iterator();
		while (it.hasNext()) {
			IOParam pm = it.next();
			if (pm.getParamType() == ptype) {
				JSONObject jj = new JSONObject();
				JSONObject jobj = new JSONObject();
				jobj.accumulate("id", pm.getId());
				jobj.accumulate("type", pm.getTypeToString());
				jj.accumulate("ioparam", jobj);
				list.add(jj);
			}
		}
		mobj.accumulate("ioparams", list);
	}

}
