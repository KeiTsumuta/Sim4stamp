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
import tmu.fs.sim4stamp.model.co.Connector;
import tmu.fs.sim4stamp.model.em.Element;
import tmu.fs.sim4stamp.model.iop.AppendParams;
import tmu.fs.sim4stamp.model.iop.IOParam;
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

	public static final String CTLIB = "CtlLib-2.1.jar";

	private final SimService simService;
	private final ElementManager elemMgr;
	private final ConnectorManager connMgr;

	public VdmCodeMaker() {
		simService = SimService.getInstance();
		elemMgr = simService.getElementManger();
		connMgr = simService.getConnectorManager();
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
			writeFile(dir + SP + LIB_DIR + SP + ELEM_SLIB, elem.getBytes("UTF-8"));
			String ctlib = getResource(R_VDM_LIB_DIR + STAMP_CTL_LIB);
			writeFile(dir + SP + "lib" + SP + STAMP_CTL_LIB, ctlib.getBytes("UTF-8"));
			byte[] jarlib = getBinResource(R_VDM_LIB_DIR + CTLIB);
			writeFile(dir + SP + "lib" + SP + CTLIB, jarlib);
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
						if (element.getType() == Element.EType.INJECTOR) {
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
						sbi.append(", ");
					}
					f = true;
					String id = iop.getId();
					IOParam.ValueType type = iop.getType();
					if (type == IOParam.ValueType.REAL) {
						sbi.append(id).append(" : real = getData(\"").append(id).append("\")");
					} else if (type == IOParam.ValueType.INT) {
						sbi.append(id).append(" : int = getIntData(\"").append(id).append("\")");
					} else if (type == IOParam.ValueType.BOOL) {
						sbi.append(id).append(" : bool = getBoolData(\"").append(id).append("\")");
					}
				}
				String ec3 = ec2.replace("$3", sbi.toString());
				StringBuilder sbe = new StringBuilder();
				f = false;
				for (IOParam iop : exportList) {
					if (f) {
						sbe.append("\n");
					}
					f = true;
					IOParam.ValueType type = iop.getType();
					if (type == IOParam.ValueType.REAL) {
						sbe.append("setData(\"").append(iop.getId()).append("\",0.0);");
					} else if (type == IOParam.ValueType.INT) {
						sbe.append("setIntData(\"").append(iop.getId()).append("\",0);");
					} else if (type == IOParam.ValueType.BOOL) {
						sbe.append("setBoolData(\"").append(iop.getId()).append("\",false);");
					}
				}
				String ec4 = ec3.replace("$4", sbe.toString());
				String file = dir + SP + "『" + elemId + "』.vdmpp_ini";
				writeFile(file, ec4.getBytes("UTF-8"));
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

}
