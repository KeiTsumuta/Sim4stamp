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

import java.awt.geom.Point2D;
import tmu.fs.sim4stamp.model.co.Connector;
import tmu.fs.sim4stamp.model.em.Element;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import tmu.fs.sim4stamp.MainApp;
import tmu.fs.sim4stamp.SimService;
import tmu.fs.sim4stamp.model.ConnectorManager;
import tmu.fs.sim4stamp.model.ElementManager;
import tmu.fs.sim4stamp.model.iop.AppendParams;
import tmu.fs.sim4stamp.util.DisplayLevel;

/**
 *
 * @author Keiichi Tsumuta
 */
public class ModelPanel implements Initializable {

    private static final Logger log = Logger.getLogger(ModelPanel.class.getPackage().getName());
    private static final Color FILL_BACK_COLOR = Color.CORNSILK;

    private final Canvas modelCanvas;
    private double initDfWidth;
    private double initDfHeight;
    private String selectNodeId = null;
    private Connector selectJointConnector = null;
    private final CheckBox modelDitailDisplayCheckbox;
    private final CheckBox connectorJointDisplayCheckbox;
    private volatile boolean isDetailDisplayMode = false;
    private volatile boolean isJointDisplayMode = false;
    private ContextMenu popupAddMenu; // 構成要素追加メニュー
    private ContextMenu popupElementMenu; // 構成要素操作メニュー
    private ContextMenu popupConnectorMenu; // コネクタ操作メニュー
    private Element selectElemnt;
    private double popupX;
    private double popupY;

    public ModelPanel(Canvas modelCanvas, Control[] modelPanelControls) {
        this.modelCanvas = modelCanvas;
        this.modelDitailDisplayCheckbox = (CheckBox) modelPanelControls[0];
        this.connectorJointDisplayCheckbox = (CheckBox) modelPanelControls[1];
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        modelCanvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent e) -> {
            mouseDragged(e);
        });
        modelCanvas.addEventHandler(MouseEvent.MOUSE_PRESSED, (MouseEvent t) -> {
            mousePressed(t);
        });
        modelCanvas.addEventHandler(MouseEvent.MOUSE_RELEASED, (MouseEvent t) -> {
            mouseReleased(t);
        });
        selectNodeId = null;

        isDetailDisplayMode = false;
        modelDitailDisplayCheckbox.setOnAction((ActionEvent ev) -> {
            isDetailDisplayMode = !isDetailDisplayMode;
            drawCanvasPanel();
        });

        isJointDisplayMode = false;
        connectorJointDisplayCheckbox.setOnAction((ActionEvent ev) -> {
            isJointDisplayMode = !isJointDisplayMode;
            drawCanvasPanel();
        });
        initPopupMenus();
    }

    private void initPopupMenus() {
        popupAddMenu = new ContextMenu();
        MenuItem mitem1 = new MenuItem("被コントロールプロセス追加");
        mitem1.setOnAction((ActionEvent t) -> {
            showElementAddDialog(t, Element.EType.CONTROLLED_EQUIPMENT);
        });
        MenuItem mitem2 = new MenuItem("センサ追加");
        mitem2.setOnAction((ActionEvent t) -> {
            showElementAddDialog(t, Element.EType.SENSOR);
        });
        MenuItem mitem3 = new MenuItem("コントローラ追加");
        mitem3.setOnAction((ActionEvent t) -> {
            showElementAddDialog(t, Element.EType.CONTROLLER);
        });
        MenuItem mitem4 = new MenuItem("アクチュエータ追加");
        mitem4.setOnAction((ActionEvent t) -> {
            showElementAddDialog(t, Element.EType.ACTUATOR);
        });
        MenuItem mitem5 = new MenuItem("インジェクタ追加");
        mitem5.setOnAction((ActionEvent t) -> {
            showElementAddDialog(t, Element.EType.INJECTOR);
        });
        SeparatorMenuItem sepItem = new SeparatorMenuItem();
        MenuItem mitem6 = new MenuItem("コネクタ追加(-->)");
        mitem6.setOnAction((ActionEvent t) -> {
            addNewConnectorLeft(t);
        });
        MenuItem mitem7 = new MenuItem("コネクタ追加(<--)");
        mitem7.setOnAction((ActionEvent t) -> {
            addNewConnectorRight(t);
        });
        popupAddMenu.getItems().addAll(mitem1, mitem2, mitem3, mitem4, mitem5, sepItem, mitem6, mitem7);

        popupElementMenu = new ContextMenu();
        MenuItem addEParam = new MenuItem("パラメータ追加/削除");
        addEParam.setOnAction((ActionEvent t) -> {
            try {
                ParamsSettingPanel psp = new ParamsSettingPanel();
                String st = selectElemnt.getNodeId() + " 構成要素";
                AppendParams aps = selectElemnt.getAppendParams();
                psp.set(AppendParams.ParamType.Element, st, aps);
                psp.show(t);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        MenuItem eDdelete = new MenuItem("構成要素削除");
        eDdelete.setOnAction((ActionEvent t) -> {
            deleteElement(selectElemnt);
        });
        popupElementMenu.getItems().addAll(addEParam, eDdelete);

        popupConnectorMenu = new ContextMenu();
        MenuItem addCParam = new MenuItem("パラメータ追加");
        addCParam.setOnAction((ActionEvent t) -> {
            try {
                ConnectorManager cm = SimService.getInstance().getConnectorManager();
                Connector c = cm.getSelected();
                ParamsSettingPanel psp = new ParamsSettingPanel();
                String st = c.getNodeFromId() + " - " + c.getNodeToId() + "コネクタ";
                AppendParams aps = c.getAppendParams();
                psp.set(AppendParams.ParamType.Connector, st, aps);
                psp.show(t);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        MenuItem addJoint = new MenuItem("ジョイント追加");
        addJoint.setOnAction((ActionEvent t) -> {
            addConnectorJoint();
        });
        MenuItem deleteJoint = new MenuItem("ジョイント削除");
        deleteJoint.setOnAction((ActionEvent t) -> {
            deleteConnectorJoint();
        });
        MenuItem cDelete = new MenuItem("コネクタ削除");
        cDelete.setOnAction((ActionEvent t) -> {
            deleteConnector();
        });
        popupConnectorMenu.getItems().addAll(addCParam, addJoint, deleteJoint, cDelete);

    }

    public void initDsplayMode() {
        // System.out.println("--- initDsplayMode --- ");
        isDetailDisplayMode = false;
        modelDitailDisplayCheckbox.setSelected(isDetailDisplayMode);
        isJointDisplayMode = false;
        connectorJointDisplayCheckbox.setSelected(isJointDisplayMode);
    }

    private void showElementAddDialog(ActionEvent t, Element.EType eType) {
        try {
            ElementAddDialog ea = new ElementAddDialog(this, eType, popupX, popupY);
            ea.show(t);
            drawCanvasPanel();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addNewConnectorLeft(ActionEvent e) {
        addNewConnector(true);
    }

    private void addNewConnectorRight(ActionEvent e) {
        addNewConnector(false);
    }

    private void addNewConnector(boolean left) {
        ConnectorManager cm = SimService.getInstance().getConnectorManager();
        Connector connector = new Connector();
        List<Point2D.Double> points = new ArrayList<>();
        if (left) {
            points.add(new Point2D.Double(50, 55));
            points.add(new Point2D.Double(150, 50));
            points.add(new Point2D.Double(250, 75));
        } else {
            points.add(new Point2D.Double(250, 65));
            points.add(new Point2D.Double(150, 70));
            points.add(new Point2D.Double(50, 60));
        }
        connector.setPoints(points);
        cm.add(connector);
        drawCanvasPanel();
    }

    private void setDisplayLevel(DisplayLevel.Level level) {
        List<Element> elements = SimService.getInstance().getElements();
        for (Element el : elements) {
            el.setLevel(level);
        }

        List<Connector> connectors = SimService.getInstance().getConnectors();
        for (Connector c : connectors) {
            c.setLevel(level);
        }
        drawCanvasPanel();
    }

    private void deleteElement(Element deleteElement) {
        deleteElement.setSelect(true);
        drawCanvasPanel();

        Alert alert = new Alert(AlertType.CONFIRMATION, "", ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("構成要素削除");
        alert.getDialogPane().setHeaderText("削除確認");
        alert.getDialogPane().setContentText("選択された構成要素を削除します。");
        ButtonType bt = alert.showAndWait().orElse(ButtonType.CANCEL);
        deleteElement.setSelect(false);
        if (bt == ButtonType.OK) {
            // System.out.println("delete!!");
            String id = deleteElement.getNodeId();
            ElementManager em = SimService.getInstance().getElementManger();
            em.deleteElement(id);
            ConnectorManager cm = SimService.getInstance().getConnectorManager();
            for (Connector c : cm.getConnectors()) {
                c.checkNode(id);
            }
        }
        drawCanvasPanel();
    }

    private void addConnectorJoint() {
        ConnectorManager cm = SimService.getInstance().getConnectorManager();
        Connector c = cm.getJointSelected();
        if (c != null) {
            c.addJoint(popupX, popupY);
        }
        drawCanvasPanel();
    }

    private void deleteConnectorJoint() {
        ConnectorManager cm = SimService.getInstance().getConnectorManager();
        Connector c = cm.getSelected();
        if (c != null) {
            c.deleteJoint();
        }
        drawCanvasPanel();
    }

    private void deleteConnector() {
        Alert alert = new Alert(AlertType.CONFIRMATION, "", ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("コネクタ削除");
        alert.getDialogPane().setHeaderText("削除確認");
        alert.getDialogPane().setContentText("選択されたコネクタを削除します。");
        ButtonType bt = alert.showAndWait().orElse(ButtonType.CANCEL);
        if (bt == ButtonType.OK) {
            ConnectorManager cm = SimService.getInstance().getConnectorManager();
            cm.deleteConnector();
        }

        drawCanvasPanel();
    }

    public void initSize() {
        double initWidth = modelCanvas.getWidth();
        double initHeight = modelCanvas.getHeight();
        Scene scene = MainApp.getStage().getScene();
        double initFrameWidth = scene.getWidth();
        double initFrameHeight = scene.getHeight();
        initDfWidth = initFrameWidth - initWidth;
        initDfHeight = initFrameHeight - initHeight;

    }

    public void drawCanvasPanel() {
        Platform.runLater(() -> {
            try {
                drawCanvas(modelCanvas);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void drawCanvas(Canvas canvas) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Scene scene = MainApp.getStage().getScene();
        double frameWidth = scene.getWidth();
        double frameHeight = scene.getHeight();
        canvas.setWidth(frameWidth - initDfWidth);
        canvas.setHeight(frameHeight - initDfHeight);
        double wMax = canvas.getWidth();
        double hMax = canvas.getHeight();
        // log.info("drawCanvas:" + wMax + "," + hMax);

        gc.setFill(FILL_BACK_COLOR);
        gc.fillRect(0, 0, wMax, hMax);

        DisplayLevel.Level level = DisplayLevel.Level.Base;
        if (isDetailDisplayMode) {
            level = DisplayLevel.Level.Detail;
        }
        List<Element> elements = SimService.getInstance().getElements();
        for (Element el : elements) {
            el.setLevel(level);
            el.draw(gc);
        }

        List<Connector> connectors = SimService.getInstance().getConnectors();
        for (Connector c : connectors) {
            c.setLevel(level);
            c.setJointDisplay(isJointDisplayMode);
            c.draw(gc);
        }
    }

    private void mouseDragged(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();
        List<Element> elements = SimService.getInstance().getElements();
        for (Element el : elements) {
            if (el.isSelected()) {
                el.move(x, y);
                break;
            }
        }
        if (selectNodeId != null) {
            List<Connector> connectors = SimService.getInstance().getConnectors();
            for (Connector c : connectors) {
                c.elementMove(selectNodeId, x, y);
            }
        } else if (selectJointConnector != null) {
            selectJointConnector.jointMove(x, y);
        }
        drawCanvasPanel();
    }

    private void mousePressed(MouseEvent e) {
        double x = e.getX();
        double y = e.getY();
        popupX = x;
        popupY = y;
        MouseButton mouseButton = e.getButton();
        selectElemnt = null;
        popupElementMenu.hide();
        popupConnectorMenu.hide();
        popupAddMenu.hide();
        List<Element> elements = SimService.getInstance().getElements();
        List<Connector> connectors = SimService.getInstance().getConnectors();
        for (Element el : elements) {
            el.setSelect(false);
        }
        for (Connector c : connectors) {
            c.resetSelect();
        }
        selectJointConnector = null;
        if (mouseButton == MouseButton.PRIMARY) { // 左クリック
            for (Connector c : connectors) {
                if (c.selectDistance(x, y)) {
                    c.setPointed(true);
                    selectJointConnector = c;
                    break;
                }
            }
            if (selectJointConnector == null) {
                for (Element el : elements) {
                    if (el.containsInside(x, y)) {
                        // System.out.println("Select element:" + el.getNodeId());
                        selectNodeId = el.getNodeId();
                        el.setSelect(true);
                        for (Connector c : connectors) {
                            c.elementFocus(selectNodeId, x, y);
                        }
                        break;
                    }
                }
            }
        } else if (mouseButton == MouseButton.SECONDARY) { // 右クリック
            for (Element el : elements) {
                if (el.containsInside(x, y)) {
                    selectJointConnector = null;
                    selectElemnt = el;
                    selectElemnt.setSelect(true);
                    popupElementMenu.show(modelCanvas, e.getScreenX(), e.getScreenY());
                    break;
                }
            }
            if (selectElemnt == null) {
                for (Connector c : connectors) {
                    if (c.selectDistance(x, y)) {
                        c.setPointed(true);
                        selectJointConnector = c;
                        popupConnectorMenu.show(modelCanvas, e.getScreenX(), e.getScreenY());
                        break;
                    }
                }
                if (selectJointConnector == null) {
                    popupAddMenu.show(modelCanvas, e.getScreenX(), e.getScreenY());
                }
            }
        }
        drawCanvasPanel();
    }

    private void mouseReleased(MouseEvent e) {
        List<Element> elements = SimService.getInstance().getElements();
        for (Element el : elements) {
            el.setSelect(false);
        }
        List<Connector> connectors = SimService.getInstance().getConnectors();
        for (Connector c : connectors) {
            c.setPointed(false);
        }
        if (selectJointConnector != null) {
            selectJointConnector.setPos();
        }
        selectNodeId = null;
        selectJointConnector = null;
        drawCanvasPanel();
    }

}
