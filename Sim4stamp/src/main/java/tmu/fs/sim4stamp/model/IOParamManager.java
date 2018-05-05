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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import tmu.fs.sim4stamp.model.co.Connector;
import tmu.fs.sim4stamp.model.em.Element;
import tmu.fs.sim4stamp.model.iop.AppendParams;
import tmu.fs.sim4stamp.model.iop.IOParam;
import tmu.fs.sim4stamp.model.iop.IOScene;
import tmu.fs.sim4stamp.util.ElementTree;
import tmu.fs.sim4stamp.util.JSONConvert;

/**
 *
 * @author Keiichi Tsumuta
 */
public class IOParamManager implements JSONConvert {

	private static final Logger log = Logger.getLogger(IOParamManager.class.getPackage().getName());

	public static final int INIT_DATA_SIZE = 10;

	private final ElementManager em;
	private final ConnectorManager cm;

	private Map<String, List<IOParam>> nodeParamMap;

	// Element param
	private List<String> elementIds;
	private List<Element> series = new ArrayList<>();

	// Connector param
	private List<List<IOParam>> connectorParams;
	private List<String[]> nfntList;

	// シミュレーション 初期シーン
	private IOScene currentScene;
	// シミュレーション 計算シーン
	private IOScene executeScene;
	// シミュレーション 結果保存
	private List<IOScene> resultScenes;

	public IOParamManager(ElementManager em, ConnectorManager cm) {
		this.em = em;
		this.cm = cm;
		init();
	}

	public void init() {
		// System.out.println("IOParamManager init ***");
		currentScene = null;
		executeScene = null;
		nodeParamMap = new HashMap<>();

		// Element param
		elementIds = new ArrayList<>();

		// Connector param
		connectorParams = new ArrayList<>();
		nfntList = new ArrayList<>();

		resultScenes = new ArrayList<>();
	}

	public void setItems() {
		System.out.println("IOParamManager setItems:---");
		nodeParamMap = new HashMap<>();
		setElements();
		setConnectors();
		ElementTree elementTree = new ElementTree();
		elementTree.setTree(em.getElements(), cm.getConnectors());
		series = elementTree.getSeries();
		if (currentScene != null) {
			currentScene.remake(nodeParamMap, nfntList);
		}
	}

	private void setElements() {
		elementIds = new ArrayList<>();
		List<Element> list = em.getElements();
		for (Element e : list) {
			String nodeId = e.getNodeId();
			elementIds.add(nodeId);
			AppendParams ap = e.getAppendParams();
			if (ap == null) {
				// Element側に属するパラメータが存在しない場合は入れ物のみを作っておく
				nodeParamMap.put(nodeId, new ArrayList<IOParam>());
				continue;
			}
			List<IOParam> ios = ap.getParams();
			// Element側に属するパラメータが存在する場合に設定する
			nodeParamMap.put(nodeId, ios);
		}
	}

	private void setConnectors() {
		connectorParams = new ArrayList<>();
		nfntList = new ArrayList<>();
		List<Connector> list = cm.getConnectors();
		for (Connector c : list) {
			String connectorFromId = c.getNodeFromId();
			String connectorToId = c.getNodeToId();
			if (connectorFromId == null || connectorToId == null) {
				continue;
			}
			List<IOParam> ios = c.getAppendParams().getParams();
			connectorParams.add(ios);
			nfntList.add(new String[] { connectorFromId, connectorToId });

			// System.out.println("conn:" + connectorFromId + "," + connectorToId + "," +
			// ios);
			// Connector側の両端のElementにConnectorに属する変数パラメータを追加する。
			for (IOParam ip : ios) {
				String[] nfnt = ip.getParentId();
				nfnt[0] = connectorFromId;
				nfnt[1] = connectorToId;
				List<IOParam> nfs = nodeParamMap.get(connectorFromId);
				if (nfs != null) {
					nfs.add(ip);
				} else {
					nodeParamMap.put(connectorFromId, new ArrayList<IOParam>());
				}
				List<IOParam> nts = nodeParamMap.get(connectorToId);
				if (nts != null) {
					nts.add(ip);
				} else {
					nodeParamMap.put(connectorToId, new ArrayList<IOParam>());
				}
			}
		}
	}

	public List<String> getNodeIds() {
		return elementIds;
	}

	public Map<String, List<IOParam>> getParamMap() {
		return nodeParamMap;
	}

	public List<IOParam> getParams(String nodeId) {
		return nodeParamMap.get(nodeId);
	}

	public List<String[]> getNfNtList() {
		return nfntList;
	}

	public List<Element> getSeries() {
		if (series != null) {
			return series;
		}
		return new ArrayList<>();
	}

	/**
	 * @return the currentScene
	 */
	public IOScene getCurrentScene() {
		if (currentScene == null) {
			currentScene = getNewScene();
		}
		return currentScene;
	}

	public void setCurrentScene(IOScene ioScene) {
		currentScene = ioScene;
	}

	public IOScene getNewScene() {
		IOScene sc = new IOScene();
		sc.init(nodeParamMap, nfntList, INIT_DATA_SIZE);
		return sc;
	}

	/**
	 * @return the connectorParams
	 */
	public List<List<IOParam>> getConnectorParams() {
		return connectorParams;
	}

	@Override
	public void parseJson(JSONObject sj) {
		try {
			JSONArray arr = sj.getJSONArray("scenes");
			// シーン毎のデータ管理はまだ未実装、現状、シーンは1個（カレント）のみ扱う
			int len = arr.length();
			for (int i = 0; i < len; i++) {
				JSONObject ob = arr.getJSONObject(i);
				currentScene = getNewScene();
				currentScene.parseJson(ob);
			}
		} catch (Exception ex) {
			log.severe(ex.toString());
			ex.printStackTrace();
		}
	}

	@Override
	public void addJSON(JSONObject pob) {
		List<JSONObject> objs = new ArrayList<>();
		JSONObject obj = new JSONObject();
		getCurrentScene().addJSON(obj);
		objs.add(obj);
		pob.accumulate("scenes", objs);
	}

	public void sceneCompensation() {
		getCurrentScene().updateSize();
		setItems();
	}

	public IOScene getNewExceuteScene() {
		executeScene = currentScene.copyClone();
		return executeScene;
	}

	public IOScene getExceuteScene() {
		return executeScene;
	}

	/**
	 * @return the resultScenes
	 */
	public List<IOScene> getResultScenes() {
		return resultScenes;
	}

	public void addResultScene(IOScene ioScene) {
		resultScenes.add(ioScene);
	}

	public void initResultScenes() {
		resultScenes = new ArrayList<>();
	}

	public void initSecondScenes() {
		if (resultScenes != null && resultScenes.size() >= 2) {
			List<IOScene> wkScenes = new ArrayList<>();
			wkScenes.add(resultScenes.get(0));
			resultScenes = wkScenes;
		}
	}
}
