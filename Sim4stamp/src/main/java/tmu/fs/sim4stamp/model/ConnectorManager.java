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

import tmu.fs.sim4stamp.model.co.Connector;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import tmu.fs.sim4stamp.model.iop.AppendParams;
import tmu.fs.sim4stamp.util.JSONConvert;

/**
 *
 * @author Keiichi Tsumuta
 */
public class ConnectorManager implements JSONConvert {

	private static final Logger log = Logger.getLogger(ConnectorManager.class.getPackage().getName());

	private List<Connector> connectors;

	public ConnectorManager() {
		init();
	}

	public void init() {
		connectors = new ArrayList<>();
	}

	public void add(Connector connector) {
		connectors.add(0, connector);
	}

	public List<Connector> getConnectors() {
		return connectors;
	}

	public void deleteConnector() {
		for (int i = 0; i < connectors.size(); i++) {
			Connector c = connectors.get(i);
			if (c.isSelected()) {
				connectors.remove(c);
			}
		}
	}

	public Connector getSelected() {
		for (int i = 0; i < connectors.size(); i++) {
			Connector c = connectors.get(i);
			if (c.isSelected()) {
				return c;
			}
		}
		return null;
	}

	public Connector getJointSelected() {
		for (int i = 0; i < connectors.size(); i++) {
			Connector c = connectors.get(i);
			if (c.isJointSelected()) {
				return c;
			}
		}
		return null;
	}

	public void resetSelect() {
		for (int i = 0; i < connectors.size(); i++) {
			Connector c = connectors.get(i);
			c.resetSelect();
		}
	}

	@Override
	public void parseJson(JSONObject sj) {
		try {
			JSONArray arr = sj.getJSONArray("connections");
			int len = arr.length();
			for (int i = 0; i < len; i++) {
				JSONObject ob = arr.getJSONObject(i).getJSONObject("connection");
				Connector connector = new Connector();
				connector.parseJson(ob);
				String fromId = connector.getNodeFromId();
				String toId = connector.getNodeToId();
				AppendParams ap = new AppendParams(AppendParams.ParamType.Connector);
				JSONArray apObj = ob.optJSONArray("ioparams");
				if (apObj != null) {
					String[] parentId = new String[]{fromId, toId};
					ap.parseJson(parentId, apObj);
					connector.setAppendParams(ap);
				}
				connectors.add(connector);
			}

		} catch (Exception ex) {
			log.severe(ex.toString());
		}
	}

	@Override
	public void addJSON(JSONObject ob) {
		List<JSONObject> objs = new ArrayList<>();
		for (Connector co : getConnectors()) {
			JSONObject obj = new JSONObject();
			co.addJSON(obj);
			objs.add(obj);
		}
		ob.accumulate("connections", objs);
	}
}
