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
package tmu.fs.sim4stamp.model;

import tmu.fs.sim4stamp.model.em.Injector;
import tmu.fs.sim4stamp.model.em.ControllledEquipment;
import tmu.fs.sim4stamp.model.em.Sensor;
import tmu.fs.sim4stamp.model.em.Element;
import tmu.fs.sim4stamp.model.em.Controller;
import tmu.fs.sim4stamp.model.em.Actuator;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import tmu.fs.sim4stamp.model.iop.AppendParams;
import tmu.fs.sim4stamp.util.JSONConvert;

/**
 *
 * @author Keiichi Tsumuta
 */
public class ElementManager implements JSONConvert {

	private List<Element> elements;

	public ElementManager() {
		init();
	}

	public void init() {
		elements = new ArrayList<>();
	}

	public void addElement(Element element) {
		elements.add(element);
	}

	public void deleteElement(String id) {
		for (int i = 0; i < elements.size(); i++) {
			Element e = elements.get(i);
			if (e.getNodeId().equals(id)) {
				elements.remove(i);
				break;
			}
		}
	}

	public List<Element> getElements() {
		return elements;
	}

	@Override
	public void parseJson(JSONObject sj) {
		try {
			JSONArray arr = sj.getJSONArray("elements");
			int len = arr.length();
			for (int i = 0; i < len; i++) {
				JSONObject ob = arr.getJSONObject(i).getJSONObject("element");
				// System.out.println("element:" + ob);
				String type = ob.optString("type");
				String id = ob.optString("id");
				Element element = null;
				switch (type) {
				case "equipment":
					element = new ControllledEquipment(id);
					break;
				case "controller":
					element = new Controller(id);
					break;
				case "actuator":
					element = new Actuator(id);
					break;
				case "sensor":
					element = new Sensor(id);
					break;
				case "injector":
					element = new Injector(id);
					break;
				}
				element.parseJson(ob);
				AppendParams ap = new AppendParams(AppendParams.ParamType.Element);
				JSONArray apObj = ob.optJSONArray("ioparams");
				if (apObj != null) {
					String[] parentId = new String[]{id};
					ap.parseJson(parentId, apObj);
					element.setAppendParams(ap);
				}
				addElement(element);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void addJSON(JSONObject ob) {
		List<JSONObject> objs = new ArrayList<>();
		for (Element el : getElements()) {
			JSONObject obj = new JSONObject();
			el.addJSON(obj);
			objs.add(obj);
		}
		ob.accumulate("elements", objs);
	}
}
