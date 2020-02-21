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
package tmu.fs.sim4stamp.vdm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import tmu.fs.sim4stamp.SimService;
import tmu.fs.sim4stamp.model.ConnectorManager;
import tmu.fs.sim4stamp.model.ElementManager;
import tmu.fs.sim4stamp.model.IOParamManager;
import tmu.fs.sim4stamp.model.LogicalValueManager;
import tmu.fs.sim4stamp.model.co.Connector;
import tmu.fs.sim4stamp.model.em.Element;
import tmu.fs.sim4stamp.model.iop.AppendParams;
import tmu.fs.sim4stamp.model.iop.IOParam;
import tmu.fs.sim4stamp.model.iop.IOScene;
import tmu.fs.sim4stamp.model.iop.IOValue;
import tmu.fs.sim4stamp.model.lv.LogicalValue;
import tmu.fs.sim4stamp.util.ResourceFileIO;

/**
 *
 * @author Keiichi Tsumuta
 */
public class VdmCodeMaker extends ResourceFileIO {

	private static final String SP = System.getProperty("file.separator");

	private static final String R_VDM_DIR = "/vdm/";
	private static final String R_VDM_LIB_DIR = "/vdm/lib/";
	private static final String EXECUTE_MAIN = "ExecuteMain.vdmpp";
	private static final String ELEM_CLASS = "『$1』.vdmpp";
	private static final String LIB_DIR = "stamplib";
	private static final String R_STAMP_LIB_DIR = "/vdm/stamplib/";
	private static final String IOPARAM_SLIB = "『IOパラメータ』.vdmpp";
	private static final String ELEM_SLIB = "『作用素』.vdmpp";
	private static final String STAMP_CTL_LIB = "ovt_ctl_lib_CtlTool.vdmpp";

	private static final String CTLIB_NAME = "CtlLib-";
	private static final String CTLIB_REV = "2.1";
	public static final String CTLIB = CTLIB_NAME + CTLIB_REV + ".jar";

	private final SimService simService;
	private final ElementManager elemMgr;
	private final ConnectorManager connMgr;
	private final IOScene ioScene;

	public VdmCodeMaker() {
		simService = SimService.getInstance();
		elemMgr = simService.getElementManger();
		connMgr = simService.getConnectorManager();
		IOParamManager iom = simService.getIoParamManager();
		ioScene = iom.getCurrentScene();
	}

	public void make() {
		String projectHome = simService.getCurrentProjectHome();
		//log.info("project home:" + projectHome);
		copyFiles(projectHome);
		makeExecuteMain(projectHome);
		makeElementClasses(projectHome);
	}

	private void copyFiles(String dir) {
		try {
			File stlibDir = new File(dir + SP + LIB_DIR);
			stlibDir.mkdir();
			String iop = getResource(R_STAMP_LIB_DIR + IOPARAM_SLIB);
			writeFile(dir + SP + LIB_DIR + SP + IOPARAM_SLIB, iop.getBytes("UTF-8"));
			String elem = getResource(R_STAMP_LIB_DIR + ELEM_SLIB);
			File vdmlibDir = new File(dir + SP + "lib");
			vdmlibDir.mkdir();
			writeFile(dir + SP + LIB_DIR + SP + ELEM_SLIB, elem.getBytes("UTF-8"));
			String ctlib = getResource(R_VDM_LIB_DIR + STAMP_CTL_LIB);
			writeFile(dir + SP + "lib" + SP + STAMP_CTL_LIB, ctlib.getBytes("UTF-8"));
			byte[] jarlib = getBinResource(R_VDM_LIB_DIR + CTLIB_NAME + CTLIB_REV + ".jar");
			deleteOldFiles(dir + SP + "lib" + SP, CTLIB_NAME, ".jar");
			writeFile(dir + SP + "lib" + SP + CTLIB_NAME + CTLIB_REV + ".jar", jarlib);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void makeExecuteMain(String dir) {
		String source = getResource(R_VDM_DIR + EXECUTE_MAIN);
		StringBuilder sb = new StringBuilder();
		try {
			boolean f = false;
			for (Element element : elemMgr.getElements()) {
				if (f) {
					sb.append(",");
				}
				f = true;
				sb.append("new 『").append(element.getNodeId()).append("』()");
			}
			// System.out.println(sb.toString());
			String result = source.replace("$1", sb.toString());
			// System.out.println(result);
			String file = dir + SP + "ExecuteMain.vdmpp";
			writeFile(file, result.getBytes("UTF-8"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void makeElementClasses(String dir) {
		String source = getResource(R_VDM_DIR + ELEM_CLASS);
		List<Connector> connectors = connMgr.getConnectors();
		try {
			for (Element element : elemMgr.getElements()) {
				StringBuilder vals = new StringBuilder();
				String elemId = element.getNodeId();
				// System.out.println("elemId :" + elemId);
				String ec = source.replace("$1", elemId);
				String et = getElemType(element.getType());
				String ec2 = ec.replace("$2", et);
				List<IOParam> importList = new ArrayList<>();
				List<IOParam> exportList = new ArrayList<>();
				AppendParams aps = element.getAppendParams();
				List<IOParam> iops = aps.getParams();
				for (IOParam iop : iops) {
					if (iop.getParamType() == AppendParams.ParamType.Element) {
						boolean initFlag = false;
						if (ioScene != null) {
							IOValue ioVal = ioScene.getIOData(elemId, iop.getId());
							if (ioVal != null && ioVal.isInitFlag()) {
								initFlag = true;
							}
						}
						if (element.getType() == Element.EType.INJECTOR || initFlag) {
							importList.add(iop);
						} else {
							exportList.add(iop);
						}
						// System.out.println("Node IOParam :" + iop.getId());
					}
				}
				for (Connector conn : connectors) {
					String fromId = conn.getNodeFromId();
					String toId = conn.getNodeToId();
					if (fromId == null || toId == null) {
						continue;
					}
					if (toId.equals(elemId)) {
						aps = conn.getAppendParams();
						iops = aps.getParams();
						for (IOParam iop : iops) {
							importList.add(iop);
							// System.out.println("NT IOParam :" + iop.getId());
						}
					}
					if (fromId.equals(elemId)) {
						aps = conn.getAppendParams();
						iops = aps.getParams();
						for (IOParam iop : iops) {
							exportList.add(iop);
							// System.out.println("NF IOParam :" + iop.getId());
						}
					}
				}
				StringBuilder sbi = new StringBuilder();
				boolean f = false;
				for (IOParam iop : importList) {
					if (f) {
						sbi.append(",\n\t\t\t");
					}
					f = true;
					String id = iop.getId();
					IOParam.ValueType type = iop.getType();
					if (null != type) {
						switch (type) {
							case REAL:
								sbi.append(id).append(" : real = getData(\"").append(id).append("\")");
								break;
							case INT:
								sbi.append(id).append(" : int = getIntData(\"").append(id).append("\")");
								break;
							case BOOL:
								sbi.append(id).append(" : bool = getBoolData(\"").append(id).append("\")");
								break;
							case F_VAL_LOGIC:
								sbi.append(id).append(" : real = get5ValData(\"").append(id).append("\")");
								vals.append(get5ValUnits(id, iop));
								break;
							default:
								break;
						}
					}
				}
				String ec3 = ec2.replace("$3", sbi.toString());
				StringBuilder sbe = new StringBuilder();
				f = false;
				for (IOParam iop : exportList) {
					if (f) {
						sbe.append("\n\t\t\t");
					}
					f = true;
					IOParam.ValueType type = iop.getType();
					if (null != type) {
						switch (type) {
							case REAL:
								sbe.append("setData(\"").append(iop.getId()).append("\",0.0);");
								break;
							case INT:
								sbe.append("setIntData(\"").append(iop.getId()).append("\",0);");
								break;
							case BOOL:
								sbe.append("setBoolData(\"").append(iop.getId()).append("\",false);");
								break;
							case F_VAL_LOGIC:
								sbe.append("set5ValData(\"").append(iop.getId()).append("\",0.0);");
								vals.append(get5ValUnits(iop.getId(), iop));
								break;
							default:
								break;
						}
					}
				}
				String ec4 = ec3.replace("$4", sbe.toString());
				String ec5 = ec4.replace("$5", sbe.toString());
				String ec6 = ec5.replace("$6", vals.toString());
				String file = dir + SP + "『" + elemId + "』.vdmpp";
				writeElementClassFile(file, ec6.getBytes("UTF-8"));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private String getElemType(Element.EType t) {
		String r = "";
		switch (t) {
			case CONTROLLER:
				r = "コントローラ";
				break;
			case ACTUATOR:
				r = "アクチュエータ";
				break;
			case SENSOR:
				r = "センサ";
				break;
			case CONTROLLED_EQUIPMENT:
				r = "制御対象";
				break;
			case INJECTOR:
				r = "外部パラメータ";
				break;
		}
		return r;
	}

	private String get5ValUnits(String id, IOParam iop) {
		StringBuilder sb = new StringBuilder();
		sb.append("\t");
		String unitId = iop.getUnit();
		LogicalValue lv = SimService.getInstance().getLogicalValueManager().getLogicalValue(unitId);
		String[] units = lv.getValues();
		for (int i = 0; i < units.length; i++) {
			sb.append(id).append("_").append(units[i]).append("=");
			sb.append(i).append(".0; ");
		}
		sb.append("\n");
		return sb.toString();
	}

}
