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

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tmu.fs.sim4stamp.SimService;
import tmu.fs.sim4stamp.model.ElementManager;
import tmu.fs.sim4stamp.model.em.Actuator;
import tmu.fs.sim4stamp.model.em.Controller;
import tmu.fs.sim4stamp.model.em.ControllledEquipment;
import tmu.fs.sim4stamp.model.em.Element;
import tmu.fs.sim4stamp.model.em.Injector;
import tmu.fs.sim4stamp.model.em.Sensor;

/**
 *
 * @author Keiichi Tsumuta
 */
public class ElementAddDialog implements Initializable {

    @FXML
    private Label elementAddTitleId;

    @FXML
    private TextField elementid;

    @FXML
    private Label errorMessageDisplay;

    private static ModelPanel modelPanel;
    private static Element.EType eType;
    private static double xAxis = 0;
    private static double yAxis = 0;

    public ElementAddDialog() {
    }

    public ElementAddDialog(ModelPanel mp, Element.EType e, double x, double y) {
        modelPanel = mp;
        eType = e;
        xAxis = x;
        yAxis = y;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //System.out.println("init!! " + eType);
        String title = "";
        switch (eType) {
            case CONTROLLER:
                title = "コントローラ";
                break;
            case ACTUATOR:
                title = "アクチュエータ";
                break;
            case SENSOR:
                title = "センサ";
                break;
            case CONTROLLED_EQUIPMENT:
                title = "被コントロールプロセス";
                break;
            case INJECTOR:
                title = "インジェクタ";
                break;
        }
        elementAddTitleId.setText("＜" + title + "＞ ");
        errorMessageDisplay.setText("");
        elementid.setText("");
    }

    public void show(ActionEvent event) throws IOException {
        Stage stage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/elementAddDialog.fxml"));
        stage.setScene(new Scene(root));
        stage.setTitle("sim4stamp 構成要素追加");
        stage.initModality(Modality.WINDOW_MODAL);
        SimService s = SimService.getInstance();
        stage.initOwner(s.getStage());
        stage.show();
    }

    @FXML
    public void addButtonActin(ActionEvent event) {
        String id = elementid.getText();
        if (id == null || id.length() == 0) {
            errorMessageDisplay.setText("？？？ ：構成要素名が入力されていません。");
            return;
        }
        ElementManager em = SimService.getInstance().getElementManger();
        List<Element> elements = em.getElements();
        for (Element element : elements) {
            if (element.getNodeId().equals(id)) {
                errorMessageDisplay.setText("？？？ ： 入力した構成要素名は既に登録されています。");
                return;
            }
        }
        Element ce = createElement(xAxis, yAxis, id);
        if (ce != null) {
            em.addElement(ce);
        }
        modelPanel.drawCanvasPanel();
        ((Node) event.getSource()).getScene().getWindow().hide();
    }

    private Element createElement(double x, double y, String id) {
        switch (eType) {
            case CONTROLLER:
                Controller cl = new Controller(id);
                cl.setPoint(x, y);
                return cl;
            case ACTUATOR:
                Actuator ac = new Actuator(id);
                ac.setPoint(x, y);
                return ac;
            case SENSOR:
                Sensor ss = new Sensor(id);
                ss.setPoint(x, y);
                return ss;
            case CONTROLLED_EQUIPMENT:
                ControllledEquipment ce = new ControllledEquipment(id);
                ce.setPoint(x, y);
                return ce;
            case INJECTOR:
                Injector ij = new Injector(id);
                ij.setPoint(x, y);
                return ij;
        }
        return null;
    }

    @FXML
    public void cancelAction(ActionEvent event) {
        ((Node) event.getSource()).getScene().getWindow().hide();
    }
}
