/*
 *	 sim4stamp - The simulation tool for STAMP/STPA
 *	 Copyright (C) 2018  Keiichi Tsumuta
 *
 *	 This program is free software: you can redistribute it and/or modify
 *	 it under the terms of the GNU General Public License as published by
 *	 the Free Software Foundation, either version 3 of the License, or
 *	 (at your option) any later version.
 *
 *	 This program is distributed in the hope that it will be useful,
 *	 but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	 GNU General Public License for more details.
 *
 *	 You should have received a copy of the GNU General Public License
 *	 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tmu.fs.sim4stamp.gui;

import java.awt.geom.Point2D;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Button;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import tmu.fs.sim4stamp.SimService;
import tmu.fs.sim4stamp.gui.util.GraphData;
import tmu.fs.sim4stamp.gui.util.GuiUtil;
import tmu.fs.sim4stamp.model.IOParamManager;
import tmu.fs.sim4stamp.model.co.Connector;
import tmu.fs.sim4stamp.model.em.Actuator;
import tmu.fs.sim4stamp.model.em.Controller;
import tmu.fs.sim4stamp.model.em.ControllledEquipment;
import tmu.fs.sim4stamp.model.em.Element;
import tmu.fs.sim4stamp.model.em.Injector;
import tmu.fs.sim4stamp.model.em.Sensor;
import tmu.fs.sim4stamp.model.iop.AppendParams;
import tmu.fs.sim4stamp.model.iop.IOParam;
import tmu.fs.sim4stamp.model.iop.IOScene;
import tmu.fs.sim4stamp.state.CommandLineExecute;
import tmu.fs.sim4stamp.state.OvertureExecManager;
import tmu.fs.sim4stamp.util.Deviation;
import tmu.fs.sim4stamp.util.DisplayLevel;
import tmu.fs.sim4stamp.state.VdmRunStatus;

/**
 * 偏差設定を行うパネル。
 *
 * @author Keiichi Tsumuta
 */
public class DeviationMapPanel implements Initializable, VdmRunStatus {

	private static final double DISTANCE_CLOSED = 10.0;
	private static final Color FILL_BACK_COLOR = Color.FLORALWHITE;
	private static final Color STROKE_COLOR = Color.BLACK;

	private static final double ELEMENT_INTERVAL = 160;
	private static final double ELEMENT_Y_POS = 20;
	private static final double ELEMENT_Y_ST = 150;
	private static final double ELEMENT_Y_DS = 34;

	private static final Deviation[] CONNECTOR_DEVIATIONS = CommandLineExecute.CONNECTOR_DEVIATIONS;

	public enum ExecuteMode {

		NORMAL("連続", 0), //
		STEP("ステップ", 10); //

		private final String name;
		private final int id;

		private ExecuteMode(String name, int id) {
			this.name = name;
			this.id = id;
		}

		public int getId() {
			return id;
		}

		@Override
		public String toString() {
			return name;
		}
	}
	private static final ExecuteMode[] EXEC_MODES = {ExecuteMode.NORMAL, ExecuteMode.STEP};

	private final Canvas mapCanvas;
	private final ComboBox deviationSelectionByType;
	private final ComboBox executeTypeSelection;
	private final Button executeButton;
	private final Button stepExecuteSimButton;
	private final Button executeStopButton;
	private final Button loopDisplayButton;
	private final Button loopDisplayDownButton;
	private final Button loopDisplayUpButton;
	private final TextField loopDisplayCount;

	private List<Element> elementSeries;
	private List<Element> elementDisplays;
	private List<Connector> connectors;
	private List<Connector> connectorDrawInfos;
	private Element selectedElement;

	private Connector selectedConnector = null;
	private List<ConnectorPoint> connPoints;

	private ContextMenu popupDeviationMenu; // 偏差設定メニュー
	private Deviation selectedConnectorDeviation = Deviation.NORMAL;
	private ExecuteMode selectEcecuteMode = ExecuteMode.NORMAL;

	private String oldStepSelect = "";

	public DeviationMapPanel(Canvas mapCanvas, Control[] controls) {
		this.mapCanvas = mapCanvas;
		this.deviationSelectionByType = (ComboBox) controls[0];
		this.executeTypeSelection = (ComboBox) controls[1];
		this.executeButton = (Button) controls[2];
		this.stepExecuteSimButton = (Button) controls[3];
		this.executeStopButton = (Button) controls[4];
		this.loopDisplayButton = (Button) controls[5];
		this.loopDisplayDownButton = (Button) controls[6];
		this.loopDisplayUpButton = (Button) controls[7];
		this.loopDisplayCount = (TextField) controls[8];
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		executeButton.setOnAction((ActionEvent event) -> {
			executeSim();
		});
		stepExecuteSimButton.setDisable(true);
		stepExecuteSimButton.setOnAction((ActionEvent event) -> {
			stepExecute();
		});

		executeStopButton.setOnAction((ActionEvent event) -> {
			executeStop();
		});
		ObservableList<Deviation> list = FXCollections.observableArrayList(CONNECTOR_DEVIATIONS);
		deviationSelectionByType.getItems().addAll(list);
		deviationSelectionByType.setOnAction((Event ev) -> {
			Deviation deviation = (Deviation) deviationSelectionByType.getSelectionModel().getSelectedItem();
			// System.out.println("select deviation :" + deviation);
			selectedConnectorDeviation = deviation;
			SimService.getInstance().getIoParamManager().getCurrentScene().setDeviation(deviation);
			drawPanel();
		});
		ObservableList<ExecuteMode> exelist = FXCollections.observableArrayList(EXEC_MODES);
		executeTypeSelection.getItems().addAll(exelist);
		executeTypeSelection.getSelectionModel().select(0);
		executeTypeSelection.setOnAction((Event ev) -> {
			selectEcecuteMode = (ExecuteMode) executeTypeSelection.getSelectionModel().getSelectedItem();
			if (selectEcecuteMode == ExecuteMode.NORMAL) {
				OvertureExecManager.getInstance().setStepExecute(false);
			} else if (selectEcecuteMode == ExecuteMode.STEP) {
				OvertureExecManager.getInstance().setStepExecute(true);
			}
		});

		// mapCanvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, (MouseEvent e) -> {
		// mouseDragged(e);
		// });
		mapCanvas.addEventHandler(MouseEvent.MOUSE_PRESSED, (MouseEvent t) -> {
			mousePressed(t);
		});
		mapCanvas.addEventHandler(MouseEvent.MOUSE_RELEASED, (MouseEvent t) -> {
			mouseReleased(t);
		});
		connPoints = new ArrayList<>();
		initPopupMenu();

		loopDisplayCount.setTextFormatter(GuiUtil.getIntTextFormater());
		loopDisplayCount.setStyle("-fx-alignment: CENTER-RIGHT;");

		loopDisplayButton.setOnAction((ActionEvent event) -> {
			String count = loopDisplayCount.getText();
			OvertureExecManager.getInstance().setDisplayCount(getInt(count));
			drawPanel();
		});
		loopDisplayDownButton.setOnAction((ActionEvent event) -> {
			String count = loopDisplayCount.getText();
			OvertureExecManager.getInstance().setDisplayCount(getInt(count) - 1);
			drawPanel();
		});
		loopDisplayUpButton.setOnAction((ActionEvent event) -> {
			String count = loopDisplayCount.getText();
			OvertureExecManager.getInstance().setDisplayCount(getInt(count) + 1);
			drawPanel();
		});
	}

	private int getInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {

		}
		return 0;
	}

	private void executeSim() {
		//log.info("overtureExecuteAction: " + OvertureExecManager.getInstance().isStepExecute());
		Platform.runLater(() -> {
			loopDisplayCount.textProperty().set("-1");
		});
		try {
			clearStepSelect();
			if (selectEcecuteMode == ExecuteMode.STEP) {
				stepExecuteSimButton.setDisable(false);
			}
			drawMapPanel();
			CommandLineExecute ce = new CommandLineExecute(this);
			ce.start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// });
	}

	private void stepExecute() {
		OvertureExecManager.getInstance().goStepExecute();
	}

	private void executeStop() {
		//log.info("overtureExecute StopAction:--");
		clearStepSelect();
		OvertureExecManager.getInstance().setStopRequest(true);
		executeButton.setDisable(false);
		stepExecuteSimButton.setDisable(true);
	}

	@Override
	public void startExec() {
		executeButton.setDisable(true);
	}

	@Override
	public void complete() {
		executeButton.setDisable(false);
		stepExecuteSimButton.setDisable(true);
	}

	private void initPopupMenu() {
		popupDeviationMenu = new ContextMenu();
		MenuItem mitem1 = new MenuItem("偏差投入対象設定");
		mitem1.setOnAction((ActionEvent t) -> {
			selectDeviationConnector(t, "y");
		});
		popupDeviationMenu.getItems().add(mitem1);
		// MenuItem mitem2 = new MenuItem("偏差投入対象外す");
		// mitem2.setOnAction((ActionEvent t) -> {
		// selectDeviationConnector(t, "n");
		// });
		// popupDeviationMenu.getItems().add(mitem2);
		MenuItem gitem = new MenuItem("グラフ表示");
		gitem.setOnAction((ActionEvent t) -> {
			showGraphDialog(t);
		});
		popupDeviationMenu.getItems().add(gitem);
	}

	public void selectDeviationConnector(ActionEvent t, String sr) {
		if (selectedConnector == null) {
			return;
		}
		IOScene ioScene = SimService.getInstance().getIoParamManager().getCurrentScene();
		AppendParams ap = selectedConnector.getAppendParams();
		List<IOParam> aps = ap.getParams();
		if (aps != null && aps.size() > 0) {
			String paramId = aps.get(0).getId();
			if (sr.equals("y")) {
				ioScene.setDeviationConnection(paramId);
			} else {
				ioScene.setDeviationConnection(null);
			}
		} else {
			ioScene.setDeviationConnection(null);
		}
		drawPanel();
	}

	private void showGraphDialog(ActionEvent t) {
		if (selectedConnector == null) {
			return;
		}
		IOScene ioScene = SimService.getInstance().getIoParamManager().getCurrentScene();
		AppendParams ap = selectedConnector.getAppendParams();
		List<IOParam> aps = ap.getParams();
		if (aps != null && aps.size() > 0) {
			try {
				GraphDisplayDialog gdd = new GraphDisplayDialog();
				List<IOScene> resultScenes = SimService.getInstance().getIoParamManager().getResultScenes();
				if (resultScenes.size() > 0) {
					IOParam iop = aps.get(0);
					gdd.reset(selectedConnector.getNodeToId() + "." + iop.getId());
					for (IOScene ios : resultScenes) {
						GraphData data = ios.getGraphData(selectedConnector.getNodeToId(), iop.getId());
						gdd.addData(ios.getDeviation().toString(), data);
					}
				}
				gdd.show(t);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void initDisplayPanel() {
		SimService ss = SimService.getInstance();
		IOParamManager im = ss.getIoParamManager();
		elementSeries = im.getSeries();
		getElementInfo();
		connectorOrder(ss);
		drawPanel();
	}

	private void connectorOrder(SimService ss) {
		List<Connector> cons = new ArrayList<>();
		for (Connector c : ss.getConnectors()) {
			if (c.getNodeFromId() == null || c.getNodeToId() == null) {
				continue;
			}
			Connector cc = c.clone();
			cc.setLevel(DisplayLevel.Level.Progress);
			cons.add(cc);
		}
		connectors = cons;
	}

	public void stepSelect(String nodeId) {
		for (Connector c : connectorDrawInfos) {
			if (c.getNodeFromId().equals(oldStepSelect)) {
				c.setPointed(true);
			} else {
				c.setPointed(false);
			}
		}
		oldStepSelect = nodeId;
	}

	public void clearStepSelect() {
		oldStepSelect = "";
		for (Element e : elementDisplays) {
			e.setSelect(false);
		}
		for (Connector c : connectorDrawInfos) {
			c.setPointed(false);
		}
	}

	public void drawPanel() {
		Platform.runLater(() -> {
			try {
				SimService ss = SimService.getInstance();
				IOParamManager im = ss.getIoParamManager();
				SingleSelectionModel model = deviationSelectionByType.getSelectionModel();
				selectedConnectorDeviation = im.getCurrentScene().getDeviation();
				int selectNo = 0;
				if (selectedConnectorDeviation != null) {
					int devId = selectedConnectorDeviation.getId();
					for (int i = 0; i < CONNECTOR_DEVIATIONS.length; i++) {
						if (CONNECTOR_DEVIATIONS[i].getId() == devId) {
							selectNo = i;
							// System.out.println("init sel:" + i + ", " + selectedConnectorDeviation);
							break;
						}
					}
				}
				model.select(selectNo);
				int dispCount = OvertureExecManager.getInstance().getDisplayCount();
				loopDisplayCount.textProperty().set(Integer.toString(dispCount));
				makeConnectorDrawInfos(ss);
				drawCanvas(mapCanvas);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
	}

	public void drawMapPanel() {
		Platform.runLater(() -> {
			try {
				int dispCount = OvertureExecManager.getInstance().getDisplayCount();
				loopDisplayCount.textProperty().set(Integer.toString(dispCount));
				drawCanvas(mapCanvas);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
	}

	private void getElementInfo() {
		elementDisplays = new ArrayList<>();
		Map<String, Element> map = new HashMap<>();
		for (Element e : elementSeries) {
			if (!map.containsKey(e.getNodeId())) {
				Element.EType etype = e.getType();
				Element et = null;
				switch (etype) {
					case CONTROLLED_EQUIPMENT:
						et = ((ControllledEquipment) e).clone();
						break;
					case CONTROLLER:
						et = ((Controller) e).clone();
						break;
					case ACTUATOR:
						et = ((Actuator) e).clone();
						break;
					case SENSOR:
						et = ((Sensor) e).clone();
						break;
					case INJECTOR:
						et = ((Injector) e).clone();
						break;
				}
				et.setLevel(DisplayLevel.Level.Progress);
				elementDisplays.add(et);
				map.put(et.getNodeId(), et);
			}
		}
		elementDisplays.sort((a, b) -> b.getOrder() - a.getOrder());
		// for (Element e : elementSeries) {
		// System.out.print(e.getNodeId() + ",");
		// }
	}

	private void makeConnectorDrawInfos(SimService ss) {
		connectorDrawInfos = new ArrayList<>();
		connPoints = new ArrayList<>();
		if (elementDisplays.size() == 0) {
			return;
		}

		IOScene ioScene = ss.getIoParamManager().getCurrentScene();
		boolean[] flag = new boolean[connectors.size()];
		double yp = ELEMENT_Y_POS + ELEMENT_Y_ST;
		int esize = elementDisplays.size();
		for (int i = 0; i < esize; i++) {
			Element e = elementDisplays.get(i);
			// Element.EType etype = e.getType();
			String enf = e.getNodeId();
			double x1 = ELEMENT_INTERVAL * i + (ELEMENT_INTERVAL / 5.0) * 2.0;
			for (int k = 0; k < connectors.size(); k++) {
				if (flag[k]) {
					continue;
				}
				Connector c = connectors.get(k);
				String nf = c.getNodeFromId();
				String nt = c.getNodeToId();
				for (int m = 0; m < esize; m++) {
					if (i == m) {
						continue;
					}
					if (nt.equals(enf) && nf.equals(elementDisplays.get(m).getNodeId())) {
						double x2 = ELEMENT_INTERVAL * m + (ELEMENT_INTERVAL / 5.0) * 2.0;
						List<Point2D.Double> points = new ArrayList<>();
						points.add(new Point2D.Double(x2, yp));
						points.add(new Point2D.Double(x1, yp));
						c.setPoints(points);
						if (ioScene.isDeviationConnector(c)) {
							c.setMarked(true, selectedConnectorDeviation.toString());
						} else {
							c.setMarked(false, null);
						}
						connectorDrawInfos.add(c);
						connPoints.add(new ConnectorPoint(c, x1, yp, x2));
						yp += ELEMENT_Y_DS;
						flag[k] = true;
						break;
					}
				}
			}
		}
		// System.out.println("connector size:" + connectors.size());
	}

	private void drawCanvas(Canvas canvas) {
		GraphicsContext gc = canvas.getGraphicsContext2D();
		double frameWidth = ELEMENT_INTERVAL * elementDisplays.size() + ELEMENT_INTERVAL * 0.6;
		double frameHeight = ELEMENT_Y_POS + ELEMENT_Y_ST + ELEMENT_Y_DS * (connectors.size() + 2);
		canvas.setWidth(frameWidth);
		canvas.setHeight(frameHeight);

		gc.setFill(FILL_BACK_COLOR);
		gc.fillRect(0, 0, frameWidth, frameHeight);

		double sy0 = ELEMENT_Y_POS;
		double sy1 = ELEMENT_Y_POS + ELEMENT_Y_ST + ELEMENT_Y_DS * (connectors.size() + 0.5);
		gc.setStroke(STROKE_COLOR);
		for (int i = 0; i < elementDisplays.size(); i++) {
			double x = ELEMENT_INTERVAL * i + (ELEMENT_INTERVAL / 5.0) * 2.0;
			gc.strokeLine(x, sy0, x, sy1);
		}

		for (int i = 0; i < elementDisplays.size(); i++) {
			double x = ELEMENT_INTERVAL * i + ELEMENT_INTERVAL / 5.0;
			Element e = elementDisplays.get(i);
			e.setPoint(x, ELEMENT_Y_POS);
			e.draw(gc);
		}

		for (Connector c : connectorDrawInfos) {
			c.draw(gc);
		}

	}

	private void mouseDragged(MouseEvent e) {
		// double x = e.getX();
		// double y = e.getY();
		// drawPanel();
	}

	private void mousePressed(MouseEvent e) {
		double x = e.getX();
		double y = e.getY();
		selectedConnector = null;
		selectedElement = null;
		popupDeviationMenu.hide();
		MouseButton mouseButton = e.getButton();
		if (mouseButton == MouseButton.PRIMARY) { // 左クリック
			for (ConnectorPoint c : connPoints) {
				if (c.isHit(x, y)) {
					selectedConnector = c.get();
					selectedConnector.setPointed(true);
					popupDeviationMenu.show(mapCanvas, e.getScreenX(), e.getScreenY());
					// System.out.println(selectedConnector.getNodeFromId() + "," +
					// selectedConnector.getNodeToId()
					// + selectedConnector.getAppendParams().getParams().get(0).getId());
					drawPanel();
					return;
				}
			}
			for (Element el : elementDisplays) {
				if (el.containsInside(x, y)) {
					selectedElement = el;
					selectedElement.setSelect(true);

					drawPanel();
					return;
				}
			}

		} else if (mouseButton == MouseButton.SECONDARY) { // 右クリック

		}

	}

	private void mouseReleased(MouseEvent e) {
		for (ConnectorPoint c : connPoints) {
			c.get().setPointed(false);
		}
		for (Element el : elementDisplays) {
			el.setSelect(false);
		}
		drawPanel();
	}

	class ConnectorPoint {

		private final Connector connector;
		private final double x1;
		private final double x2;
		private final double y1;

		public ConnectorPoint(Connector conn, double x1, double y1, double x2) {
			this.connector = conn;
			if (x1 < x2) {
				this.x1 = x1;
				this.x2 = x2;
			} else {
				this.x1 = x2;
				this.x2 = x1;
			}
			this.y1 = y1;
		}

		public Connector get() {
			return connector;
		}

		public boolean isHit(double x, double y) {
			if (Math.abs(y1 - y) < DISTANCE_CLOSED) {
				if (x >= x1 && x <= x2) {
					return true;
				}
			}
			return false;
		}
	}

}
