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
package tmu.fs.sim4stamp;

import javafx.application.Platform;
import javafx.scene.control.Label;
import tmu.fs.sim4stamp.gui.ConditionPanel;
import tmu.fs.sim4stamp.gui.ExecuteLogPanel;
import tmu.fs.sim4stamp.gui.ModelPanel;
import tmu.fs.sim4stamp.gui.ResultPanel;
import tmu.fs.sim4stamp.gui.ResultTablePanel;
import tmu.fs.sim4stamp.model.IOParamManager;

/**
 *
 * @author Keiichi Tsumuta
 */
public class PanelManager {

    private static PanelManager panelManager = new PanelManager();

    private ModelPanel modelPanel;
    private ConditionPanel conditionPanel;
    private ExecuteLogPanel executeLogPanel;
    private ResultPanel resutPanel;

    private ResultTablePanel resultTablePanel;

    private static Label selectedProjectName;

    public PanelManager() {

    }

    public static PanelManager get() {
        return panelManager;
    }

    public void setSelectLabel(Label selectedProjectName) {
        this.selectedProjectName = selectedProjectName;
    }

    /**
     * @return the modelPanel
     */
    public ModelPanel getModelPanel() {
        return modelPanel;
    }

    /**
     * @param modelPanel the modelPanel to set
     */
    public void setModelPanel(ModelPanel modelPanel) {
        this.modelPanel = modelPanel;
    }

    /**
     * @return the conditionPanel
     */
    public ConditionPanel getConditionPanel() {
        return conditionPanel;
    }

    /**
     * @param conditionPanel the conditionPanel to set
     */
    public void setConditionPanel(ConditionPanel conditionPanel) {
        this.conditionPanel = conditionPanel;
    }

    /**
     * @return the executeLogPanel
     */
    public ExecuteLogPanel getExecuteLogPanel() {
        return executeLogPanel;
    }

    /**
     * @param executeLogPanel the executeLogPanel to set
     */
    public void setExecuteLogPanel(ExecuteLogPanel executeLogPanel) {
        this.executeLogPanel = executeLogPanel;
    }

    /**
     * @return the resutPanel
     */
    public ResultPanel getResutPanel() {
        return resutPanel;
    }

    /**
     * @return the resutPanel
     */
    public void setResutPanel(ResultPanel resultPanel) {
        this.resutPanel = resultPanel;
    }

    /**
     * @return the resultTablePanel
     */
    public ResultTablePanel getResultTablePanel() {
        return resultTablePanel;
    }

    /**
     * @param resultTablePanel the resultTablePanel to set
     */
    public void setResultTablePanel(ResultTablePanel resultTablePanel) {
        this.resultTablePanel = resultTablePanel;
    }

    public void initDisplay() {
        Platform.runLater(() -> {
            try {
                SimService ss = SimService.getInstance();
                getModelPanel().drawCanvasPanel();
                getConditionPanel().setInitDisplay();
                getModelPanel().initDsplayMode();
                if (selectedProjectName != null) {
                    selectedProjectName.setText(ss.getCurrentProjectId());
                }
                getResutPanel().initDisplay();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void resultDisplay() {
        Platform.runLater(() -> {
            try {
                getResutPanel().resultDisplay();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void resetResult() {
        Platform.runLater(() -> {
            try {
                IOParamManager iom = SimService.getInstance().getIoParamManager();
                iom.initResultScenes();
                getResutPanel().resetData();
                //System.out.println("Reset result!!");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
    
     public void resetSecondResult() {
        Platform.runLater(() -> {
            try {
                IOParamManager iom = SimService.getInstance().getIoParamManager();
                iom.initSecondScenes();
                getResutPanel().displayFirstData();
                //System.out.println("Reset result!!");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
    
}
