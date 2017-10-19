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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import tmu.fs.sim4stamp.SimService;
import tmu.fs.sim4stamp.model.IOParamManager;
import tmu.fs.sim4stamp.model.em.Element;
import tmu.fs.sim4stamp.model.iop.AppendParams;
import tmu.fs.sim4stamp.util.Deviation;
import tmu.fs.sim4stamp.model.iop.IOParam;
import tmu.fs.sim4stamp.model.iop.IOScene;

/**
 * 計算条件を設定するパネル
 *
 * @author Keiichi Tsumuta
 */
public class ConditionPanel implements Initializable {

    private static final DecimalFormat FORMAT = new DecimalFormat("#0.00");
    private static final Deviation[] CONNECTOR_DEVIATIONS = {
        Deviation.NORMAL,
        Deviation.NOT_PROVIDING, Deviation.PROVIDING_MORE, Deviation.PROVIDING_LESS,
        Deviation.TOO_LATE,
        Deviation.STOPPING_TOO_SOON, Deviation.APPLYING_TOO_LONG};

    private final TextField sceneTitle;
    private final TableView simItemParamView; // 構成要素パラメータテーブル

    private final TableView simConnectorView; // コネクタパラメータテーブル
    private Map<String, RadioButton> radioMap;

    private final TextField simInitSeqSize;  // 時系列数
    private final TableView initDataTable;  // 初期値設定テーブル
    private final Button conditionSetButton;
    private final ComboBox deviationSelectBox;  // 偏差投入種別
    private final TextField deviationStartIndex;  // 偏差投入開始インデックス

    private Deviation selectedConnectorDeviation = Deviation.NORMAL;

    public ConditionPanel(Control[] controls) {
        this.sceneTitle = (TextField) controls[0];
        this.simItemParamView = (TableView) controls[1];
        this.simConnectorView = (TableView) controls[2];
        this.simInitSeqSize = (TextField) controls[3];
        this.deviationSelectBox = (ComboBox) controls[4];
        this.deviationStartIndex = (TextField) controls[5];
        this.conditionSetButton = (Button) controls[6];
        this.initDataTable = (TableView) controls[7];
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        radioMap = new HashMap<>();
        IOScene ioScene = SimService.getInstance().getIoParamManager().getCurrentScene();
        sceneTitle.setText(ioScene.getScene());
        simInitSeqSize.setText(Integer.toString(ioScene.getSize()));
        setItemTableView();
        setConnectorTableView();
        this.conditionSetButton.setOnAction((ActionEvent event) -> {
            setInitTable();
        });
        ObservableList<Deviation> list = FXCollections.observableArrayList(CONNECTOR_DEVIATIONS);
        deviationSelectBox.getItems().addAll(list);
        deviationSelectBox.setOnAction((Event ev) -> {
            Deviation deviation = (Deviation) deviationSelectBox.getSelectionModel().getSelectedItem();
            //System.out.println("select deviation :" + deviation);
            selectedConnectorDeviation = deviation;
            SimService.getInstance().getIoParamManager().getCurrentScene().setDeviation(deviation);
        });
        deviationStartIndex.setText(Integer.toString(ioScene.getDeviationStartIndex()));
    }

    private void setItemTableView() {
        TableColumn<ElementItem, String> elemColumn = new TableColumn("構成要素");
        elemColumn.setCellValueFactory(new PropertyValueFactory<>("elementId"));

        TableColumn<ElementItem, String> paramColumn = new TableColumn("パラメータ");
        paramColumn.setCellValueFactory(new PropertyValueFactory<>("paramId"));

        TableColumn<ElementItem, CheckBox> selColumn = new TableColumn("初期設定");
        selColumn.setStyle("-fx-alignment: CENTER;");
        selColumn.setCellValueFactory((TableColumn.CellDataFeatures<ElementItem, CheckBox> arg0) -> {
            ElementItem elem = arg0.getValue();
            CheckBox checkBox = new CheckBox();
            checkBox.selectedProperty().setValue(elem.isSelected());
            checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                public void changed(ObservableValue<? extends Boolean> ov,
                        Boolean old_val, Boolean new_val) {
                    elem.setSelected(new_val);
                }
            });
            return new SimpleObjectProperty<CheckBox>(checkBox);
        });
        simItemParamView.getColumns().setAll(elemColumn, paramColumn, selColumn);
    }

    private void setConnectorTableView() {
        TableColumn<ConnectorItem, String> fromColumn = new TableColumn("始端構成要素");
        fromColumn.setCellValueFactory(new PropertyValueFactory<>("fromId"));

        TableColumn<ConnectorItem, String> toColumn = new TableColumn("終端構成要素");
        toColumn.setCellValueFactory(new PropertyValueFactory<>("toId"));

        TableColumn<ConnectorItem, String> paramColumn = new TableColumn("パラメータ");
        paramColumn.setCellValueFactory(new PropertyValueFactory<>("paramId"));

        TableColumn<ConnectorItem, RadioButton> selColumn = new TableColumn("偏差投入");
        selColumn.setStyle("-fx-alignment: CENTER;");
        selColumn.setCellValueFactory((TableColumn.CellDataFeatures<ConnectorItem, RadioButton> arg0) -> {
            ConnectorItem conn = arg0.getValue();
            String id = conn.getParamId();
            RadioButton radioButton = radioMap.get(id);
            radioButton.selectedProperty().setValue(conn.isSelected());
            radioButton.selectedProperty().addListener(
                    (ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) -> {
                        //System.out.println("chanege:" + old_val + "," + new_val + "," + conn.getParamId());
                        if (new_val) {
                            conn.setSelected(true);
                            selectDeviationConnection(conn);
                        } else {
                            conn.setSelected(false);
                        }
                    });
            return new SimpleObjectProperty<RadioButton>(radioButton);
        });
        simConnectorView.getColumns().setAll(fromColumn, toColumn, paramColumn, selColumn);
    }

    private void selectDeviationConnection(ConnectorItem conn) {
        IOScene ioScene = SimService.getInstance().getIoParamManager().getCurrentScene();
        ioScene.setDeviationConnection(conn.getParamId());
    }

    public void setInitDisplay() {
        IOScene ioScene = SimService.getInstance().getIoParamManager().getCurrentScene();
        sceneTitle.setText(ioScene.getScene());
        simInitSeqSize.setText(Integer.toString(ioScene.getSize()));
        setItemTableView();
        setConnectorTableView();
        SingleSelectionModel model = deviationSelectBox.getSelectionModel();
        selectedConnectorDeviation = ioScene.getDeviation();
        int selectNo = 0;
        if (selectedConnectorDeviation != null) {
            for (int i = 0; i < CONNECTOR_DEVIATIONS.length; i++) {
                if (CONNECTOR_DEVIATIONS[i].getId() == selectedConnectorDeviation.getId()) {
                    selectNo = i;
                    //System.out.println("init sel:" + i + ", " + selectedConnectorDeviation);
                    break;
                }
            }
        }
        model.select(selectNo);
        setItemDisplay();
        setInitTable();
    }

    public void setItemDisplay() {
        Platform.runLater(() -> {
            try {
                List<Element.EType> etypes = new ArrayList<>();
                List<String> colParents = new ArrayList<>();
                List<List<String>> colTitles = new ArrayList<>();

                List<Element> elments = SimService.getInstance().getElementManger().getElements();
                elments.forEach((elem) -> {
                    AppendParams ap = elem.getAppendParams();
                    if (!(ap == null)) {
                        String nodeId = elem.getNodeId();
                        List<String> colChildren = new ArrayList<>();
                        etypes.add(elem.getType());
                        colParents.add(nodeId);
                        List<IOParam> ios = ap.getParams();
                        for (IOParam ip : ios) {
                            if (ip.getParamType() == AppendParams.ParamType.Element) {
                                colChildren.add(ip.getId());
                            }
                        }
                        colTitles.add(colChildren);
                    }
                });
                setItems(etypes, colParents, colTitles);

                setConnectors();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void setItems(List<Element.EType> etypes, List<String> elements, List<List<String>> elementParams) {
        ObservableList<ElementItem> eis = FXCollections.observableArrayList();
        int n = 0;
        for (int i = 0; i < elements.size(); i++) {
            String ename = elements.get(i);
            for (int k = 0; k < elementParams.get(i).size(); k++) {
                boolean sel = false;
                if (etypes.get(i) == Element.EType.INJECTOR) {
                    sel = true;
                }
                ElementItem ei = new ElementItem(ename, elementParams.get(i).get(k), sel);
                eis.add(ei);
            }
            n++;
        }
        simItemParamView.setItems(eis);
    }

    private void setConnectors() {
        ObservableList<ConnectorItem> eis = FXCollections.observableArrayList();
        IOParamManager iom = SimService.getInstance().getIoParamManager();
        IOScene ioScene = iom.getCurrentScene();
        String devId = ioScene.getDeviationConnParamId();
        List<String[]> nfntList = iom.getNfNtList();
        List<List<IOParam>> connParams = iom.getConnectorParams();
        ToggleGroup toggleGroup = new ToggleGroup();
        radioMap = new HashMap<>();
        for (int i = 0; i < nfntList.size(); i++) {
            String nf = nfntList.get(i)[0];
            String nt = nfntList.get(i)[1];
            for (int k = 0; k < connParams.get(i).size(); k++) {
                String id = connParams.get(i).get(k).getId();
                boolean sel = false;
                if (id.equals(devId)) {
                    sel = true;
                }
                ConnectorItem ci = new ConnectorItem(nf, nt, id, sel);
                eis.add(ci);
                RadioButton rb = new RadioButton();
                rb.setToggleGroup(toggleGroup);
                radioMap.put(id, rb);
            }
        }
        simConnectorView.setItems(eis);
    }

    public void setInitTable() {
        Platform.runLater(() -> {
            try {
                List<ElementItem> initList = new ArrayList<>();
                ObservableList<ElementItem> eis = simItemParamView.getItems();
                eis.stream().filter((ei) -> (ei.isSelected())).forEachOrdered((ei) -> {
                    initList.add(ei);
                });
                List<String> hs = new ArrayList<>();
                hs.add("No.");
                for (ElementItem e : initList) {
                    hs.add(e.getElementId() + "_" + e.getParamId());
                }
                ObservableList<String> headers = FXCollections.observableArrayList();
                headers.setAll(hs);

                int colIndex = 0;
                List<TableColumn<ObservableList, String>> columnList = new ArrayList<>();
                for (String header : headers) {
                    final int idx = colIndex;
                    TableColumn<ObservableList, String> column = new TableColumn<>(header);
                    column.setStyle("-fx-alignment: CENTER-RIGHT;");
                    column.setCellValueFactory(
                            (CellDataFeatures<ObservableList, String> param)
                            -> {
                        return new SimpleStringProperty(param.getValue().get(idx).toString());
                    });
                    if (colIndex > 0) {
                        column.setCellFactory(TextFieldTableCell.forTableColumn());
                        column.setOnEditCommit(
                                new EventHandler<CellEditEvent<ObservableList, String>>() {
                            @Override
                            public void handle(CellEditEvent<ObservableList, String> event) {
                                int row = event.getTablePosition().getRow();
                                String tableId = event.getTableColumn().getText();
                                String[] idel = tableId.split("_");
                                IOParamManager iom = SimService.getInstance().getIoParamManager();
                                IOScene ioScene = iom.getCurrentScene();
                                double newValue = getDouble(event.getNewValue());
                                double[] d = ioScene.getData(idel[0], idel[1]);
                                d[row] = newValue;
                                ObservableList<String> list = event.getTableView().getItems().get(row);
                                list.set(idx, FORMAT.format(d[row]));
                                if ((row + 1) < d.length) {
                                    initDataTable.getSelectionModel().clearAndSelect(row + 1, event.getTableColumn());
                                }
                                initDataTable.requestFocus();
                            }
                        });
                        column.setEditable(true);
                    }
                    columnList.add(column);
                    colIndex++;
                }
                IOParamManager iom = SimService.getInstance().getIoParamManager();
                IOScene ioScene = iom.getCurrentScene();
                ioScene.dataInitSelection();
                ioScene.setScene(sceneTitle.getText());
                //sceneTitle.setText(ioScene.getScene());
                initDataTable.getColumns().setAll(columnList);
                int startIndex = getInt(deviationStartIndex.getText());
                ioScene.setDeviationStartIndex(startIndex);
                int devIdx = startIndex - 1;
                if (devIdx < 0) {
                    devIdx = 0;
                }
                ObservableList<ObservableList> initDataVals = FXCollections.observableArrayList();
                int size = getInt(simInitSeqSize.getText());
                ioScene.setSize(size);
                for (int i = 0; i < size; i++) {
                    ObservableList<String> rows = FXCollections.observableArrayList();
                    if (i == devIdx) {
                        rows.add("# " + Integer.toString(i + 1));
                    } else {
                        rows.add(Integer.toString(i + 1));
                    }
                    for (int k = 0; k < headers.size() - 1; k++) {
                        ElementItem ei = initList.get(k);
                        ioScene.initDataSelect(ei.getElementId(), ei.getParamId());
                        double[] d = ioScene.getData(ei.getElementId(), ei.getParamId());
                        if (d != null && d.length > i) {
                            rows.add(FORMAT.format(d[i]));
                        } else {
                            rows.add(FORMAT.format(0.0));
                        }
                    }
                    initDataVals.add(rows);
                }
                initDataTable.getSelectionModel().setCellSelectionEnabled(true);
                initDataTable.setItems(initDataVals);
                initDataTable.setEditable(true);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private int getInt(String s) {
        int i = 0;
        try {
            if (s != null) {
                i = Integer.parseInt(s);
            }
        } catch (Exception ex) {
        }
        return i;

    }

    private double getDouble(String val) {
        double d = 0.0;
        try {
            d = Double.parseDouble(val);
        } catch (Exception ex) {
        }
        return d;
    }

    public class ElementItem {

        private SimpleStringProperty elementId;
        private SimpleStringProperty paramId;
        private SimpleBooleanProperty selected;

        public ElementItem(String elemId, String paramId, boolean select) {
            this.elementId = new SimpleStringProperty(elemId);
            this.paramId = new SimpleStringProperty(paramId);
            this.selected = new SimpleBooleanProperty(select);
        }

        /**
         * @return the elementId
         */
        public String getElementId() {
            return elementId.get();
        }

        public SimpleStringProperty getElementIdProperty() {
            return elementId;
        }

        /**
         * @return the paramId
         */
        public String getParamId() {
            return paramId.get();
        }

        public SimpleStringProperty getParamIdProperty() {
            return paramId;
        }

        /**
         * @return the select
         */
        public boolean isSelected() {
            return selected.get();
        }

        public SimpleBooleanProperty getSelectedProperty() {
            return selected;
        }

        /**
         * @param select the select to set
         */
        public void setSelected(boolean select) {
            this.selected.set(select);
        }
    }

    public class ConnectorItem {

        private SimpleStringProperty fromId;
        private SimpleStringProperty toId;
        private SimpleStringProperty paramId;
        private SimpleBooleanProperty selected;

        public ConnectorItem(String fromId, String toId, String paramId, boolean select) {
            this.fromId = new SimpleStringProperty(fromId);
            this.toId = new SimpleStringProperty(toId);
            this.paramId = new SimpleStringProperty(paramId);
            this.selected = new SimpleBooleanProperty(select);
        }

        /**
         * @return the fromId
         */
        public SimpleStringProperty getFromIdProperty() {
            return fromId;
        }

        public String getFromId() {
            return fromId.get();
        }

        /**
         * @param fromId the fromId to set
         */
        public void setFromId(SimpleStringProperty fromId) {
            this.fromId = fromId;
        }

        /**
         * @return the toId
         */
        public SimpleStringProperty getToIdProperty() {
            return toId;
        }

        public String getToId() {
            return toId.get();
        }

        /**
         * @param toId the toId to set
         */
        public void setToId(SimpleStringProperty toId) {
            this.toId = toId;
        }

        public boolean isSelected() {
            return selected.get();
        }

        /**
         * @return the paramId
         */
        public String getParamId() {
            return paramId.get();
        }

        public SimpleStringProperty getParamIdProperty() {
            return paramId;
        }

        /**
         * @return the selected
         */
        public SimpleBooleanProperty getSelectedProperty() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }

        /**
         * @param selected the selected to set
         */
        public void setSelected(SimpleBooleanProperty selected) {
            this.selected = selected;
        }

        public String toString() {
            return fromId.get() + "," + toId.get() + "," + paramId.get();
        }

    }

}
