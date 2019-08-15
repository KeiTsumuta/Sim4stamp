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
package tmu.fs.sim4stamp.gui.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import tmu.fs.sim4stamp.SimService;
import tmu.fs.sim4stamp.model.IOParamManager;
import tmu.fs.sim4stamp.model.LogicalValueManager;
import tmu.fs.sim4stamp.model.iop.IOParam;
import tmu.fs.sim4stamp.model.iop.IOScene;
import tmu.fs.sim4stamp.model.lv.LogicalValue;

/**
 *
 * @author Keiichi Tsumuta
 */
public class MakeResultTable {

	public static final boolean LOGIACAL_VALUE_MODE = true;
	public static final boolean NUM_VALUE_MODE = false;

	private static final DecimalFormat D_FORMAT = new DecimalFormat("#0.00");
	private static final DecimalFormat L_FORMAT = new DecimalFormat("#0.0");

	private boolean lvMode = false;
	private List<String> parentElementIds;
	private List<List<String>> dataIds;
	private List<String[]> dataList;

	public MakeResultTable(boolean mode) {
		lvMode = mode;
		parentElementIds = new ArrayList<>();
		dataIds = new ArrayList<>();
		dataList = new ArrayList<>();
	}

	public void makeResultTable(IOScene ioScene) {
		SimService ss = SimService.getInstance();
		IOParamManager iom = ss.getIoParamManager();

		List<String> elemIds = iom.getNodeIds();
		for (String elemId : elemIds) {
			getParentElementIds().add(elemId);
			List<String> colChildren = new ArrayList<>();
			List<IOParam> iops = iom.getParamMap().get(elemId);
			for (IOParam iop : iops) {
				colChildren.add(iop.getId());
				IOParam.ValueType type = iop.getType();
				String[] arr = new String[ioScene.getSize()];
				if (null != type) {
					switch (type) {
						case REAL:
							double[] dData = ioScene.getData(elemId, iop.getId());
							for (int i = 0; i < dData.length; i++) {
								arr[i] = D_FORMAT.format(dData[i]);
							}
							break;
						case INT:
							int[] iData = ioScene.getIntData(elemId, iop.getId());
							for (int i = 0; i < iData.length; i++) {
								arr[i] = Integer.toString(iData[i]);
							}
							break;
						case BOOL:
							boolean[] bData = ioScene.getBoolData(elemId, iop.getId());
							for (int i = 0; i < bData.length; i++) {
								if (bData[i]) {
									arr[i] = "true";
								} else {
									arr[i] = "false";
								}
							}
							break;
						case LOGI_VAL:
							String unitId = iop.getUnit();
							LogicalValue lv = LogicalValueManager.getLogicalValue(unitId);
							String[] licalVals = lv.getValues();
							double[] dlData = ioScene.getData(elemId, iop.getId());
							for (int i = 0; i < dlData.length; i++) {
								double val = dlData[i];
								if (lvMode) {
									String name = licalVals[0];
									if (0.0 < val && val < 1.5) {
										name = licalVals[1];
									} else if (1.5 <= val && val < 2.5) {
										name = licalVals[2];
									} else if (2.5 <= val && val < 3.5) {
										name = licalVals[3];
									} else if (3.5 <= val && val < 4.5) {
										name = licalVals[4];
									} else if (4.5 <= val) {
										name = licalVals[5];
									}
									arr[i] = name + "(" + L_FORMAT.format(val) + ")";
								} else {
									arr[i] = L_FORMAT.format(val);
								}
							}
							break;
						default:
							break;
					}
				}
				getDataList().add(arr);
			}
			getDataIds().add(colChildren);
		}
	}

	/**
	 * @return the parentElementIds
	 */
	public List<String> getParentElementIds() {
		return parentElementIds;
	}

	/**
	 * @return the dataIds
	 */
	public List<List<String>> getDataIds() {
		return dataIds;
	}

	/**
	 * @return the dataList
	 */
	public List<String[]> getDataList() {
		return dataList;
	}

}
