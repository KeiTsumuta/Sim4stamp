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
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

/**
 *
 * @author Keiichi Tsumuta
 */
public class ExecuteLogPanel implements Initializable {

    private final Button clearButton;
    private final TextArea executeLog;

    public ExecuteLogPanel(Button clearButton, TextArea executeLog) {
        this.clearButton = clearButton;
        this.executeLog = executeLog;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        executeLog.setWrapText(true);
        executeLog.setEditable(false);
        clearButton.setOnAction((ActionEvent ev) -> {
            clear();
        });
    }

    public void addLine(String s) {
        Platform.runLater(() -> {
            executeLog.appendText(s);
        });
    }

    public void clear() {
        executeLog.setText("");
    }

}
