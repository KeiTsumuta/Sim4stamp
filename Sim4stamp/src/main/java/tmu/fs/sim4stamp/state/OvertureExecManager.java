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
package tmu.fs.sim4stamp.state;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tmu.fs.sim4stamp.PanelManager;
import tmu.fs.sim4stamp.SimService;
import tmu.fs.sim4stamp.gui.DeviationMapPanel;
import tmu.fs.sim4stamp.model.IOParamManager;
import tmu.fs.sim4stamp.model.em.Element;
import tmu.fs.sim4stamp.model.iop.IOParam;
import tmu.fs.sim4stamp.model.iop.IOScene;
import tmu.fs.sim4stamp.model.iop.IOValue;
import tmu.fs.sim4stamp.util.DisplayItem;
import tmu.fs.sim4stamp.util.DisplayValues;

/**
 * Overture実行データ管理クラス
 *
 * @author Keiichi Tsumuta
 */
public class OvertureExecManager implements DisplayItem {

	private static volatile OvertureExecManager oeManager = new OvertureExecManager();
	private static final DecimalFormat FORMAT = new DecimalFormat("#0.00");
	private int loopCounter = 0;
	private int loopMax = 0;
	private Map<String, Element> elmMap = null;
	private List<String> idOrders = null;
	private List<String> nodeIds;

	private IOScene executeScene = null;

	private int displayCount;

	private OvertureExecManager() {
	}

	public static OvertureExecManager getInstance() {
		return oeManager;
	}

	public void init() {
		idOrders = new ArrayList<>();
		elmMap = new HashMap<>();
		executeScene = null;
		displayCount = -1;
	}

	public void calcInit() {
		loopCounter = -1;
		displayCount = loopCounter + 1;
		SimService ss = SimService.getInstance();
		idOrders = new ArrayList<>();
		elmMap = new HashMap<>();
		IOParamManager ioParamManager = ss.getIoParamManager();
		nodeIds = ioParamManager.getNodeIds();
		// 構成要素の評価順を求める
		List<Element> elements = ss.getElements();
		int max = 0;
		for (Element ele : elements) {
			if (ele.getOrder() > max) {
				max = ele.getOrder();
			}
		}
		for (int i = max; i > 0; i--) {
			for (Element ele : elements) {
				if (i == ele.getOrder()) {
					idOrders.add(ele.getNodeId());
					elmMap.put(ele.getNodeId(), ele);
				}
			}
		}
		// 計算用のパラメータ
		executeScene = ioParamManager.getNewExceuteScene();
		// ioParamManager.setCurrentScene(executeScene);
		loopMax = executeScene.getSize();
		DisplayValues.getInstance().inject(this);
	}

	public boolean hasNext() {
		loopCounter++;
		DeviationMapPanel dpanel = PanelManager.get().getDeviationMapPanel();
		IOParamManager ioParamManager = SimService.getInstance().getIoParamManager();
		if (loopCounter < loopMax) {
			displayCount = loopCounter + 1;
			if (loopCounter > 0) {
				try {
					for (String nodeId : nodeIds) {
						List<IOParam> iops = ioParamManager.getParams(nodeId);
						for (IOParam iop : iops) {
							if (!executeScene.isInitDataSelected(nodeId, iop.getId())) {
								IOValue ioValue = executeScene.getIOData(nodeId, iop.getId());
								IOParam.ValueType type = ioValue.getType();
								if (type == IOParam.ValueType.REAL) {
									double[] dVals = ioValue.getDoubleValues();
									if (dVals != null) {
										dVals[loopCounter] = dVals[loopCounter - 1];
									}
								} else if (type == IOParam.ValueType.INT) {
									int[] iVals = ioValue.getIntValues();
									if (iVals != null) {
										iVals[loopCounter] = iVals[loopCounter - 1];
									}
								} else if (type == IOParam.ValueType.BOOL) {
									boolean[] bVals = ioValue.getBoolValues();
									if (bVals != null) {
										bVals[loopCounter] = bVals[loopCounter - 1];
									}
								}
							}
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				dpanel.drawMapPanel();
			}
			return true;
		}
		ioParamManager.addResultScene(executeScene);
		PanelManager.get().resultDisplay();
		return false;
	}

	public void setData(String elemId, String dataId, double value) {
		if (executeScene == null) {
			return;
		}
		executeScene.setData(elemId, dataId, loopCounter, value);
		// System.out.println("inject Data:(" + loopCounter + ") " + elemId + "," +
		// dataId + " = " + value);
	}

	public double getData(String elemId, String dataId) {
		if (executeScene == null) {
			return 0.0;
		}
		double[] vals = executeScene.getData(elemId, dataId);
		// System.out.println("getInstance Data:(" + loopCounter + ") " + elemId + "," +
		// dataId + " = " + dVals[loopCounter]);
		return vals[loopCounter];
	}

	public void setIntData(String elemId, String dataId, int value) {
		if (executeScene == null) {
			return;
		}
		executeScene.setIntData(elemId, dataId, loopCounter, value);
		// System.out.println("inject Data:(" + loopCounter + ") " + elemId + "," +
		// dataId + " = " + value);
	}

	public int getIntData(String elemId, String dataId) {
		if (executeScene == null) {
			return 0;
		}
		int[] vals = executeScene.getIntData(elemId, dataId);
		// System.out.println("getInstance Data:(" + loopCounter + ") " + elemId + "," +
		// dataId + " = " + dVals[loopCounter]);
		return vals[loopCounter];
	}

	public void setBoolData(String elemId, String dataId, boolean value) {
		if (executeScene == null) {
			return;
		}
		executeScene.setBoolData(elemId, dataId, loopCounter, value);
		// System.out.println("inject Data:(" + loopCounter + ") " + elemId + "," +
		// dataId + " = " + value);
	}

	public boolean getBoolData(String elemId, String dataId) {
		if (executeScene == null) {
			return false;
		}
		boolean[] vals = executeScene.getBoolData(elemId, dataId);
		// System.out.println("getInstance Data:(" + loopCounter + ") " + elemId + "," +
		// dataId + " = " + dVals[loopCounter]);
		return vals[loopCounter];
	}

	public int getDisplayCount() {
		return displayCount;
	}

	public void setDisplayCount(int count) {
		if (executeScene == null) {
			return;
		}
		if (count >= 0 && count <= executeScene.getSize()) {
			displayCount = count;
		}
	}

	@Override
	public String getValue(String elemId, String dataId) {
		if (executeScene == null) {
			return "";
		}
		if (displayCount > 0) {
			try {
				IOValue ioValue = executeScene.getIOData(elemId, dataId);
				if (ioValue != null && ioValue.getSize() >= displayCount) {
					IOParam.ValueType type = ioValue.getType();
					if (type == IOParam.ValueType.REAL) {
						double[] dVals = ioValue.getDoubleValues();
						if (dVals != null) {
							return FORMAT.format(dVals[displayCount - 1]);
						}
					} else if (type == IOParam.ValueType.INT) {
						int[] iVals = ioValue.getIntValues();
						if (iVals != null) {
							return Integer.toString(iVals[displayCount - 1]);
						}
					} else if (type == IOParam.ValueType.BOOL) {
						boolean[] bVals = ioValue.getBoolValues();
						if (bVals != null) {
							return Boolean.toString(bVals[displayCount - 1]);
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return "";
	}

	public List<String> getElementOrders() {
		return idOrders;
	}

	public Element getElement(String id) {
		return elmMap.get(id);
	}

	public IOScene getExecuteScene() {
		return executeScene;
	}
}
