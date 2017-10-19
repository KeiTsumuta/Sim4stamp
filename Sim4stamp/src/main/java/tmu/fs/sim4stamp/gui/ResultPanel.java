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
package tmu.fs.sim4stamp.gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.TableView;
import tmu.fs.sim4stamp.PanelManager;
import tmu.fs.sim4stamp.SimService;
import tmu.fs.sim4stamp.model.IOParamManager;
import tmu.fs.sim4stamp.model.em.Element;
import tmu.fs.sim4stamp.model.iop.AppendParams;
import tmu.fs.sim4stamp.model.iop.IOParam;
import tmu.fs.sim4stamp.model.iop.IOScene;

/**
 *
 * @author Keiichi Tsumuta
 */
public class ResultPanel implements Initializable {

    private static Logger log = Logger.getLogger(ResultPanel.class.getPackage().getName());

    private ChoiceBox<String> resultChoice;

    private ChoiceBox<String> graph1ChoiseBox;
    private LineChart lineChart1;
    private LineGraphPanel linePanel1;
    private String initSelectParentId1 = null;
    private String initSelectId1 = null;
    private String currentSelectParentId1 = null;
    private String currentSelectId1 = null;

    private ChoiceBox<String> graph2ChoiseBox;
    private LineChart lineChart2;
    private LineGraphPanel linePanel2;
    private String initSelectParentId2 = null;
    private String initSelectId2 = null;
    private String currentSelectParentId2 = null;
    private String currentSelectId2 = null;

    private int selectResultIndex = -1;

    private ResultTablePanel resultTable;

    public ResultPanel(Control[] controls, LineChart[] lineCharts) {
        this.resultChoice = (ChoiceBox) controls[0];

        this.graph1ChoiseBox = (ChoiceBox) controls[1];
        this.lineChart1 = lineCharts[0];

        this.graph2ChoiseBox = (ChoiceBox) controls[2];
        this.lineChart2 = lineCharts[1];
        this.resultTable = new ResultTablePanel((TableView) controls[3]);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        linePanel1 = new LineGraphPanel(lineChart1);
        linePanel2 = new LineGraphPanel(lineChart2);

        getResultTable().initialize(location, resources);
        PanelManager.get().setResultTablePanel(getResultTable());
    }

    public void initDisplay() {
        getLinePanel1().reset();
        getLinePanel2().reset();
        resultTable.initData();
        setGraphSelections();
    }

    private void setGraphSelections() {
        initSelectParentId1 = null;
        initSelectId1 = null;
        initSelectParentId2 = null;
        initSelectId2 = null;
        selectResultIndex = -1;
        ObservableList<String> rss = FXCollections.observableArrayList();
        resultChoice.setItems(rss);
        resultChoice.getSelectionModel()
                .selectedItemProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    if (newValue == null) {
                        return;
                    }
                    String[] tk = newValue.split(" ");
                    if (tk.length > 1) {
                        selectResultIndex = getInt(tk[0]);
                        System.out.println("sel index:" + selectResultIndex);
                        if (selectResultIndex > 0) {
                            List<IOScene> resultScenes = SimService.getInstance().getIoParamManager().getResultScenes();
                            displayResultTable(resultScenes.get(selectResultIndex - 1));
                        }
                    }
                });
        //
        ObservableList<String> eis = FXCollections.observableArrayList();
        List<Element> elments = SimService.getInstance().getElementManger().getElements();
        elments.forEach((e) -> {
            Element.EType eType = e.getType();
            AppendParams ap = e.getAppendParams();
            if (!(ap == null)) {
                String nodeId = e.getNodeId();
                List<String> colChildren = new ArrayList<>();
                List<IOParam> ios = ap.getParams();
                for (IOParam ip : ios) {
                    if (ip.getParamType() == AppendParams.ParamType.Element) {
                        eis.add(ip.getId());
                        if (eType == Element.EType.INJECTOR && initSelectId1 == null) {
                            initSelectParentId1 = nodeId;
                            initSelectId1 = ip.getId();
                        } else if (initSelectId2 == null) {
                            initSelectParentId2 = nodeId;
                            initSelectId2 = ip.getId();
                        }
                    }
                }
            }
        });
        graph1ChoiseBox.setItems(eis);
        if (initSelectId1 != null) {
            graph1ChoiseBox.getSelectionModel().select(initSelectId1);
        }
        graph1ChoiseBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    //System.out.println(newValue);
                    IOParamManager iom = SimService.getInstance().getIoParamManager();
                    List<String> elemIds = iom.getNodeIds();
                    for (String elemId : elemIds) {
                        String parentId = elemId;
                        List<IOParam> iops = iom.getParamMap().get(elemId);
                        for (IOParam iop : iops) {
                            String id = iop.getId();
                            if (id.equals(newValue)) {
                                linePanel1.reset();
                                addGraphDisplay(linePanel1, id, parentId, id);
                                currentSelectParentId1 = parentId;
                                currentSelectId1 = id;
                                return;
                            }
                        }
                    }
                });
        currentSelectParentId1 = initSelectParentId1;
        currentSelectId1 = initSelectId1;

        graph2ChoiseBox.setItems(eis);
        if (initSelectId2 != null) {
            graph2ChoiseBox.getSelectionModel().select(initSelectId2);
        }
        graph2ChoiseBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                    //System.out.println(newValue);
                    IOParamManager iom = SimService.getInstance().getIoParamManager();
                    List<String> elemIds = iom.getNodeIds();
                    for (String elemId : elemIds) {
                        String parentId = elemId;
                        List<IOParam> iops = iom.getParamMap().get(elemId);
                        for (IOParam iop : iops) {
                            String id = iop.getId();
                            if (id.equals(newValue)) {
                                linePanel2.reset();
                                addGraphDisplay(linePanel2, id, parentId, id);
                                currentSelectParentId2 = parentId;
                                currentSelectId2 = id;
                                return;
                            }
                        }
                    }
                });
        currentSelectParentId2 = initSelectParentId2;
        currentSelectId2 = initSelectId2;

    }

    private int getInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception ex) {

        }
        return 0;
    }

    public void resultDisplay() {
        // 結果データのプルダウンリスト
        makeResultSelect();
        // グラフ
        if (currentSelectParentId1 != null) {
            addGraphDisplay(linePanel1, currentSelectId1, currentSelectParentId1, currentSelectId1);
        }
        if (currentSelectParentId2 != null) {
            addGraphDisplay(linePanel2, currentSelectId2, currentSelectParentId2, currentSelectId2);
        }
        // テーブル
        IOScene executeScene = SimService.getInstance().getIoParamManager().getExceuteScene();
        displayResultTable(executeScene);
    }

    private void makeResultSelect() {
        ObservableList<String> rss = FXCollections.observableArrayList();
        List<IOScene> resultScenes = SimService.getInstance().getIoParamManager().getResultScenes();
        String sel = null;
        int i = 1;
        for (IOScene ioScene : resultScenes) {
            sel = i + " : " + ioScene.getDeviation().toString();
            rss.add(sel);
            i++;
        }
        resultChoice.setItems(rss);
        if (sel != null) {
            resultChoice.getSelectionModel().select(sel);
        }
    }

    private void addGraphDisplay(LineGraphPanel graph, String title, String parentId, String id) {
        List<IOScene> resultScenes = SimService.getInstance().getIoParamManager().getResultScenes();
        if (resultScenes.size() == 0) {
            return;
        }
        graph.reset();
        int i = 1;
        for (IOScene ios : resultScenes) {
            if (currentSelectParentId1 != null) {
                double[] data = ios.getData(parentId, id);
                graph.addData(i + ":" + title, data);
            }
            i++;
        }
    }

    private void displayResultTable(IOScene ioScene) {
        IOParamManager iom = SimService.getInstance().getIoParamManager();
        List<String> colParents = new ArrayList<>();
        List<List<String>> colTitles = new ArrayList<>();
        List<double[]> colData = new ArrayList<>();
        List<String> elemIds = iom.getNodeIds();
        for (String elemId : elemIds) {
            colParents.add(elemId);
            List<String> colChildren = new ArrayList<>();
            List<IOParam> iops = iom.getParamMap().get(elemId);
            for (IOParam iop : iops) {
                colChildren.add(iop.getId());
                double[] dData = ioScene.getData(elemId, iop.getId());
                colData.add(dData);
            }
            colTitles.add(colChildren);
        }
        resultTable.setData(colParents, colTitles, colData);
    }

    public void resetData() {
        getLinePanel1().reset();
        getLinePanel2().reset();
    }

    public void displayFirstData() {
        List<IOScene> resultScenes = SimService.getInstance().getIoParamManager().getResultScenes();
        if (resultScenes.size() > 0) {
            displayResultTable(resultScenes.get(0));
            // グラフ
            if (currentSelectParentId1 != null) {
                addGraphDisplay(linePanel1, currentSelectId1, currentSelectParentId1, currentSelectId1);
            }
            if (currentSelectParentId2 != null) {
                addGraphDisplay(linePanel2, currentSelectId2, currentSelectParentId2, currentSelectId2);
            }
        }
        makeResultSelect();
    }

    /**
     * @return the linePanel1
     */
    public LineGraphPanel getLinePanel1() {
        return linePanel1;
    }

    /**
     * @return the linePanel2
     */
    public LineGraphPanel getLinePanel2() {
        return linePanel2;
    }

    /**
     * @return the resultTable
     */
    public ResultTablePanel getResultTable() {
        return resultTable;
    }

}
