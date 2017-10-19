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
package tmu.fs.sim4stamp.gui;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tmu.fs.sim4stamp.PanelManager;
import tmu.fs.sim4stamp.SimService;
import tmu.fs.sim4stamp.model.ConnectorManager;
import tmu.fs.sim4stamp.model.ElementManager;
import tmu.fs.sim4stamp.model.IOParamManager;
import tmu.fs.sim4stamp.model.co.Connector;
import tmu.fs.sim4stamp.model.em.Element;
import tmu.fs.sim4stamp.model.iop.AppendParams;
import tmu.fs.sim4stamp.model.iop.IOParam;

/**
 *
 * @author Keiichi Tsumuta
 */
public class ParamsSettingPanel implements Initializable {

    @FXML
    private Label paramType;

    @FXML
    private ListView paramList;

    @FXML
    private Button addButton;

    @FXML
    private TextField addParamId;

    @FXML
    private TextField deleteParamId;

    private Stage stage;
    private static String subTitle;
    private static AppendParams.ParamType aType;
    private static AppendParams appendParams;
    private static ObservableList<String> displayList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //System.out.println("--- init:" + subTitle);
        paramType.setText(subTitle);
        makeDisplayList();
    }

    private void makeDisplayList() {
        displayList = FXCollections.observableArrayList();
        int paramSize = 0;
        if (appendParams != null) {
            List<IOParam> ioParams = appendParams.getParams();
            for (IOParam io : ioParams) {
                if (aType == io.getParamType()) {
                    displayList.add(io.getId());
                    paramSize++;
                }
            }
        }
        // コネクタに設定できるパラメータは１つに限定する。
        if (aType == AppendParams.ParamType.Connector && paramSize > 0) {
            addButton.setDisable(true);
        } else {
            addButton.setDisable(false);
        }
        paramList.setItems(displayList);
        paramList.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                deleteParamId.setText(newValue);
            }
        }
        );
    }

    public void set(AppendParams.ParamType a, String sTitle, AppendParams ap) {
        subTitle = sTitle;
        aType = a;
        appendParams = ap;
        //System.out.println("--- set:" + subTitle);
    }

    public void show(ActionEvent event) throws IOException {
        stage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/appendParamsSettingDialog.fxml"));
        stage.setScene(new Scene(root));
        stage.setTitle("sim4stamp");
        stage.initModality(Modality.WINDOW_MODAL);
        SimService s = SimService.getInstance();
        stage.initOwner(s.getStage());
        stage.show();
    }

    @FXML
    public void addAction(ActionEvent event) {
        String error = null;
        IOParam ioParam = null;
        String id = addParamId.getText();
        if (id == null) {
            return;
        }
        id = id.trim();
        if (id.length() == 0) {
            return;
        }
        ElementManager em = SimService.getInstance().getElementManger();
        for (Element el : em.getElements()) {
            AppendParams aps = el.getAppendParams();
            if (aps == null) {
                continue;
            }
            for (IOParam iop : aps.getParams()) {
                if (iop.getId().equals(id)) {
                    error = "？？？ すでに使用済みです";
                    break;
                }
            }
        }

        ConnectorManager cm = SimService.getInstance().getConnectorManager();
        for (Connector c : cm.getConnectors()) {
            AppendParams aps = c.getAppendParams();
            if (aps == null) {
                continue;
            }
            for (IOParam iop : aps.getParams()) {
                if (iop.getId().equals(id)) {
                    error = "？？？ すでに使用済みです";
                    break;
                }
            }
        }
        if (error == null) {
            if (aType == AppendParams.ParamType.Element) {
                ioParam = new IOParam(AppendParams.ParamType.Element, null, id, IOParam.ValueType.REAL);
                appendParams.addIOParam(ioParam);
            } else {
                String[] parentId = new String[2];
                ioParam = new IOParam(AppendParams.ParamType.Connector, parentId, id, IOParam.ValueType.REAL);
                appendParams.addIOParam(ioParam);
            }
            IOParamManager ipm = SimService.getInstance().getIoParamManager();
            ipm.setItems();
            //IOScene ioScene = ipm.getCurrentScene();
            //ioScene.setSize(ioScene.getSize());
            displayList.add(id);
            // コネクタに設定できるパラメータは１つに限定する。
            if (aType == AppendParams.ParamType.Connector && displayList.size() > 0) {
                addButton.setDisable(true);
            } else {
                addButton.setDisable(false);
            }
            addParamId.setText("");
            PanelManager.get().getModelPanel().drawCanvasPanel();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.OK);
            alert.setTitle("パラメータ追加");
            alert.getDialogPane().setHeaderText("エラー");
            alert.getDialogPane().setContentText(error);
            alert.showAndWait();
        }
    }

    @FXML
    public void deleteAction(ActionEvent event) {
        //System.out.println("deleteAction!!");
        String deleteId = deleteParamId.getText();
        if (deleteId != null && deleteId.length() > 0) {
            ElementManager em = SimService.getInstance().getElementManger();
            for (Element el : em.getElements()) {
                AppendParams aps = el.getAppendParams();
                if (aps == null) {
                    continue;
                }
                for (IOParam iop : aps.getParams()) {
                    if (iop.getId().equals(deleteId)) {
                        aps.deleteIOParam(iop);
                        deleteParamId.setText("");
                        break;
                    }
                }
            }

            ConnectorManager cm = SimService.getInstance().getConnectorManager();
            for (Connector c : cm.getConnectors()) {
                AppendParams aps = c.getAppendParams();
                if (aps == null) {
                    continue;
                }
                for (IOParam iop : aps.getParams()) {
                    if (iop.getId().equals(deleteId)) {
                        aps.deleteIOParam(iop);
                        deleteParamId.setText("");
                        break;
                    }
                }
            }
            IOParamManager iop = SimService.getInstance().getIoParamManager();
            iop.setItems();
            makeDisplayList();
            PanelManager.get().getModelPanel().drawCanvasPanel();
        }
    }

    @FXML
    public void returnAction(ActionEvent event) {

        ((Node) event.getSource()).getScene().getWindow().hide();
    }

    /**
     * @return the appendParams
     */
    public AppendParams getAppendParams() {
        return appendParams;
    }

}
