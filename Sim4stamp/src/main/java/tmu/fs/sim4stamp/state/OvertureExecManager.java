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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tmu.fs.sim4stamp.PanelManager;
import tmu.fs.sim4stamp.SimService;
import tmu.fs.sim4stamp.model.IOParamManager;
import tmu.fs.sim4stamp.model.em.Element;
import tmu.fs.sim4stamp.model.iop.IOParam;
import tmu.fs.sim4stamp.model.iop.IOScene;

/**
 *
 * @author Keiichi Tsumuta
 */
public class OvertureExecManager {

    private static volatile OvertureExecManager oeManager = new OvertureExecManager();
    private int loopCounter = 0;
    private int loopMax = 0;
    private Map<String, Element> elmMap = null;
    private List<String> idOrders = null;
    private List<String> nodeIds;

    private IOScene executeScene;

    private Map<String, Integer> indexMap = null;

    private OvertureExecManager() {
    }

    public static OvertureExecManager getInstance() {
        return oeManager;
    }

    public void init() {
        idOrders = new ArrayList<>();
        elmMap = new HashMap<>();
    }

    public void calcInit() {
        loopCounter = -1;
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
        //ioParamManager.setCurrentScene(executeScene);
        loopMax = executeScene.getSize();
    }

    public boolean hasNext() {
        loopCounter++;
        IOParamManager ioParamManager = SimService.getInstance().getIoParamManager();
        if (loopCounter < loopMax) {
            if (loopCounter > 0) {
                try {
                    for (String nodeId : nodeIds) {
                        List<IOParam> iops = ioParamManager.getParams(nodeId);
                        for (IOParam iop : iops) {
                            double[] dData = executeScene.getData(nodeId, iop.getId());
                            //System.out.println("initflag:"+nodeId+","+ iop.getId()+","+executeScene.isInitDataSelected(nodeId, iop.getId()));
                            if (dData != null && !executeScene.isInitDataSelected(nodeId, iop.getId())) {
                                dData[loopCounter] = dData[loopCounter - 1];
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return true;
        }
        ioParamManager.addResultScene(executeScene);
        PanelManager.get().resultDisplay();
        return false;
    }

    public void setData(String elemId, String dataId, double value) {
        executeScene.setData(elemId, dataId, loopCounter, value);
        //System.out.println("set Data:(" + loopCounter + ") " + elemId + "," + dataId + " = " + value);
    }

    public double getData(String elemId, String dataId) {
        double[] vals = executeScene.getData(elemId, dataId);
        //ystem.out.println("get Data:(" + loopCounter + ") " + elemId + "," + dataId + " = " + vals[loopCounter]);
        return vals[loopCounter];
    }

    public List<String> getElementOrders() {
        return idOrders;
    }

    public Element getElement(String id) {
        return elmMap.get(id);
    }
}
