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
package tmu.fs.sim4stamp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import tmu.fs.sim4stamp.model.IOParamManager;
import tmu.fs.sim4stamp.model.co.Connector;
import tmu.fs.sim4stamp.model.ConnectorManager;
import tmu.fs.sim4stamp.model.em.Element;
import tmu.fs.sim4stamp.model.ElementManager;
import tmu.fs.sim4stamp.model.LogicalValueManager;
import tmu.fs.sim4stamp.model.iop.IOScene;
import tmu.fs.sim4stamp.state.OvertureExecManager;
import tmu.fs.sim4stamp.tcp.SimServer;
import tmu.fs.sim4stamp.util.ResourceFileIO;

/**
 *
 * @author Keiichi Tsumuta
 */
public class SimService extends ResourceFileIO implements java.io.Serializable {

	private static final int SERVER_PORT = 8001;

	private static final String SIMINFO_INI = "/info/siminfo.json";
	public static final String SP = System.getProperty("file.separator");
	public static final String PAS = System.getProperty("path.separator");
	public static final String INFO_DIR_NAME = SP + ".stamp";
	public static final String INFO_FILE_NAME = SP + ".stamp" + SP + "siminfo.json";
	private static final int JSON_SIZE_MAX = 1024 * 100;

	private volatile static boolean changed = false;

	private volatile static SimService simService = new SimService();

	private Stage stage;
	private List<String> projectList;
	private String currentProjectId = null;
	private Map<String, String> paramMap;
	private ElementManager elementManager;
	private ConnectorManager connectorManager;
	private IOParamManager ioParamManager;
	private LogicalValueManager logicalValueManager;

	private SimServer simServer;

	private SimService() {
		this.paramMap = new HashMap<>();
		this.projectList = new ArrayList<>();
		elementManager = new ElementManager();
		connectorManager = new ConnectorManager();
		ioParamManager = new IOParamManager(elementManager, connectorManager);
		logicalValueManager = new LogicalValueManager();
		try {
			simServer = new SimServer(SERVER_PORT);
			new Thread(simServer).start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// readInfoFile();
	}

	public static SimService getInstance() {
		return simService;
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	/**
	 * システム設定ファイル読込み
	 */
	public void readInfoFile() {
		try {
			File f = new File(System.getProperty("user.home") + INFO_FILE_NAME);
			if (!f.exists()) {
				String iop = getResource(SIMINFO_INI);
				writeFile(System.getProperty("user.home") + INFO_FILE_NAME, iop.getBytes("UTF-8"));
			}
			parseSimInfo(readJsonFile(f));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void parseSimInfo(JSONObject jObj) {
		if (jObj == null) {
			return;
		}
		// System.out.println("--- parseSimInfo");
		JSONObject sj = jObj.getJSONObject("siminfos");
		JSONObject overture = sj.getJSONObject("overture");
		paramMap.put("overture.home", overture.getString("home"));
		paramMap.put("overture.commandline", overture.getString("commandline"));

		int ghColum = 1;
		JSONObject sp = sj.optJSONObject("systemparams");
		if (sp != null) {
			ghColum = sp.optInt("graphcolums", 1);
		}
		paramMap.put("systemparams.graphcolums", Integer.toString(ghColum));

		double ghWidth = 1.0;
		if (sp != null) {
			ghWidth = sp.optDouble("graphwidth", 1);
		}
		paramMap.put("systemparams.graphwidth", Double.toString(ghWidth));

		JSONObject devparams = sj.optJSONObject("deviationParams");
		if (devparams != null) {
			IOScene.setProvidingMoreParam(devparams.optDouble("providingMore"));
			IOScene.setProvidingLessParam(devparams.optDouble("providingLess"));
			IOScene.setDeviationTooEarly(devparams.optInt("deviationTooEarly"));
			IOScene.setDeviationTooLate(devparams.optInt("deviationTooLate"));
		}

		String currentPjObj = sj.optString("currentProject");
		setCurrentProjectId(null);
		setCurrentProjectId(currentPjObj);

		//log.info("o-home -> " + paramMap.get("overture.home"));
		// log.info("commandlinetool -> " + paramMap.get("overture.commandline"));
		JSONArray pjs = sj.getJSONArray("projects");
		int len = pjs.length();
		projectList = new ArrayList<>();
		for (int i = 0; i < len; i++) {
			JSONObject pj = pjs.getJSONObject(i);
			if (pj.has("project")) {
				JSONObject base = pj.getJSONObject("project");
				String id = base.optString("id");
				projectList.add(id);
				paramMap.put("project.home." + id, base.optString("home"));
				paramMap.put("project.pjparams." + id, base.optString("pjparams"));
			}
		}
	}

	/**
	 * システム設定ファイルの書込み
	 */
	public void writeInfoFile() {
		try {
			File f = new File(System.getProperty("user.home") + INFO_FILE_NAME);
			writeInfos(f, getSimInfoJson().toString(2));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private JSONObject getSimInfoJson() {
		JSONObject jobj = new JSONObject();
		JSONObject ov = new JSONObject();
		ov.accumulate("home", paramMap.get("overture.home"));
		ov.accumulate("commandline", paramMap.get("overture.commandline"));
		jobj.accumulate("overture", ov);

		JSONObject sp = new JSONObject();
		sp.accumulate("graphcolums", paramMap.get("systemparams.graphcolums"));
		sp.accumulate("graphwidth", paramMap.get("systemparams.graphwidth"));
		jobj.accumulate("systemparams", sp);

		ov = new JSONObject();
		ov.accumulate("providingMore", IOScene.getProvidingMoreParam());
		ov.accumulate("providingLess", IOScene.getProvidingLessParam());
		ov.accumulate("deviationTooEarly", IOScene.getDeviationTooEarly());
		ov.accumulate("deviationTooLate", IOScene.getDeviationTooLate());
		jobj.accumulate("deviationParams", ov);

		jobj.accumulate("currentProject", getCurrentProjectId());

		List<JSONObject> list = new ArrayList<>();
		for (String pjId : projectList) {
			JSONObject pjt = new JSONObject();
			JSONObject jp = new JSONObject();
			jp.accumulate("id", pjId);
			jp.accumulate("home", paramMap.get("project.home." + pjId));
			jp.accumulate("pjparams", paramMap.get("project.pjparams." + pjId));
			pjt.accumulate("project", jp);
			list.add(pjt);
		}
		jobj.accumulate("projects", list);

		JSONObject mobj = new JSONObject();
		mobj.accumulate("siminfos", jobj);
		return mobj;
	}

	/**
	 * プロジェクト毎ファイル （project param.json）読込み
	 */
	public void readProjectFile(String id) {
		if (projectList.isEmpty()) {
			return;
		}
		String pf = paramMap.get("project.home." + id) + "/" + paramMap.get("project.pjparams." + id);
		//log.info("param.json -> " + pf);
		elementManager.init();
		connectorManager.init();
		ioParamManager.init();
		File paramFile = new File(pf);
		if (paramFile.exists()) {
			JSONObject ob = readJsonFile(paramFile);
			parseProjectParams(ob);
		} else {
			System.out.println("project params file no exists:" + pf);
		}
		OvertureExecManager.getInstance().init();
		/* Debug */
		// Map<String, List<IOParam>> map = ioParamManager.getParamMap();
		// for (String key : map.keySet()) {
		// System.out.println("-- node=" + key);
		// for (IOParam iop : map.get(key)) {
		// System.out.println(iop.getId());
		// }
		// }
	}

	private JSONObject readJsonFile(File jsonFile) {
		JSONObject jobj = null;
		try ( FileInputStream in = new FileInputStream(jsonFile);) {
			int size = (int) jsonFile.length();
			if (size < JSON_SIZE_MAX) {
				byte[] buf = new byte[size];
				int readSize = in.read(buf);
				if (readSize > 0) {
					String json = new String(buf, "UTF-8");
					jobj = new JSONObject(json);
				}
			} else {
				System.out.println("ERROR:JSONデータサイズオーバー");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return jobj;
	}

	public void parseProjectParams(JSONObject jObj) {
		elementManager.init();
		connectorManager.init();
		ioParamManager.init();
		JSONObject sj = jObj.getJSONObject("pjparams");
		elementManager.parseJson(sj);
		connectorManager.parseJson(sj);
		ioParamManager.setItems();
		ioParamManager.parseJson(sj);
	}

	private double getDouble(String t) {
		try {
			return Double.parseDouble(t);
		} catch (Exception ex) {

		}
		return 0.0;
	}

	private void writeInfos(File f, String contents) {
		File dir = new File(System.getProperty("user.home") + INFO_DIR_NAME);
		dir.mkdir();
		try ( FileOutputStream fo = new FileOutputStream(f);  BufferedOutputStream out = new BufferedOutputStream(fo);) {
			out.write(contents.getBytes("UTF-8"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private Map<String, String> getParamMap() {
		return paramMap;
	}

	public String getOvertureHome() {
		return paramMap.get("overture.home");
	}

	public void setOvertureHome(String dir) {
		paramMap.put("overture.home", dir);
	}

	public String getOvertureCommandLine() {
		return paramMap.get("overture.commandline");
	}

	public void setOvertureCommandLine(String oc) {
		paramMap.put("overture.commandline", oc);
	}

	public String getOvertureCommandLineJar() {
		return paramMap.get("overture.home") + SP + "commandline" + SP + paramMap.get("overture.commandline");
	}

	public void setResultGraphColumSize(int colSize) {
		if (colSize > 0) {
			paramMap.put("systemparams.graphcolums", Integer.toString(colSize));
		}
	}

	public int getResultGraphColumSize() {
		String size = paramMap.get("systemparams.graphcolums");
		try {
			return Integer.parseInt(size);
		} catch (Exception ex) {
		}
		return 1;
	}

	public void setResultGraphWidth(double width) {
		if (width > 0.0) {
			paramMap.put("systemparams.graphwidth", Double.toString(width));
		}
	}

	public double getResultGraphWidth() {
		String width = paramMap.get("systemparams.graphwidth");
		try {
			return Double.parseDouble(width);
		} catch (Exception ex) {
		}
		return 1.0;
	}

	public List<String> getProjects() {
		return projectList;
	}

	public void addProject(String id, String home, String params) {
		if (id == null || home == null || params == null) {
			return;
		}
		for (int i = 0; i < projectList.size(); i++) {
			if (projectList.get(i).equals(id)) {
				return;
			}
		}
		projectList.add(id);
		paramMap.put("project.home." + id, home);
		paramMap.put("project.pjparams." + id, params);
	}

	public String getProjectHome(String id) {
		return paramMap.get("project.home." + id);
	}

	public String getProjectParams(String id) {
		return paramMap.get("project.pjparams." + id);
	}

	public void deleteProject(String id) {
		for (int i = 0; i < projectList.size(); i++) {
			if (projectList.get(i).equals(id)) {
				projectList.remove(i);
				paramMap.remove("project.home." + id);
				paramMap.remove("project.pjparams." + id);
				if (id.equals(currentProjectId)) {
					initCurrentProject();
				}
				return;
			}
		}
	}

	public void initCurrentProject() {
		currentProjectId = null;
		elementManager.init();
		connectorManager.init();
		ioParamManager.init();
	}

	public List<Element> getElements() {
		return elementManager.getElements();
	}

	public List<Connector> getConnectors() {
		return connectorManager.getConnectors();
	}

	public ConnectorManager getConnectorManager() {
		return connectorManager;
	}

	public LogicalValueManager getLogicalValueManager() {
		return logicalValueManager;
	}

	public String toJson() {
		JSONObject jobj = new JSONObject();
		// elementManager section
		elementManager.addJSON(jobj);

		// connections section
		connectorManager.addJSON(jobj);

		// ioparams section
		ioParamManager.addJSON(jobj);

		JSONObject mobj = new JSONObject();
		mobj.accumulate("pjparams", jobj);
		return mobj.toString(2);
	}

	public void saveProjectParams(File jfile) {
		try ( FileOutputStream fo = new FileOutputStream(jfile);  BufferedOutputStream out = new BufferedOutputStream(fo);) {
			out.write(toJson().getBytes("UTF-8"));
			setChanged(false);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * @return the stage
	 */
	public Stage getStage() {
		return stage;
	}

	/**
	 * @return the currentProjectId
	 */
	public String getCurrentProjectId() {
		if (currentProjectId == null) {
			return "";
		}
		return currentProjectId;
	}

	/**
	 * @param currentProjectId the currentProjectId to set
	 */
	public void setCurrentProjectId(String currentProjectId) {
		this.currentProjectId = currentProjectId;
	}

	public ElementManager getElementManger() {
		return elementManager;
	}

	/**
	 * @return the ioParamManager
	 */
	public IOParamManager getIoParamManager() {
		return ioParamManager;
	}

	public String getCurrentProjectHome() {
		// System.out.println("@@@@ " + "project.home." + getCurrentProjectId() + " : "
		// + paramMap.get("project.home." + getCurrentProjectId()));
		return paramMap.get("project.home." + getCurrentProjectId());
	}

	public void close() {
		simServer.close();
	}

	/**
	 * @return the changed
	 */
	public static boolean isChanged() {
		return changed;
	}

	/**
	 * @param flag the changed to set
	 */
	public static void setChanged(boolean flag) {
		changed = flag;
	}
}
