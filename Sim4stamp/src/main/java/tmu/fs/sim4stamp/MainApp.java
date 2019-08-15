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

import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Keiichi Tsumuta
 */
public class MainApp extends Application {

	private static Stage stage;

	@Override
	public void start(Stage stage) throws Exception {
		this.stage = stage;
		stage.setOnCloseRequest((WindowEvent t) -> {
			System.exit(0);
		});

		SimService simService = SimService.getInstance();
		simService.setStage(stage);
		simService.readInfoFile();
		simService.readProjectFile(simService.getCurrentProjectId());
        simService.getLogicalValueManager().readInitFile();

		Parent root = FXMLLoader.load(getClass().getResource("/fxml/Scene.fxml"));
		Scene scene = new Scene(root);
		scene.getStylesheets().add("/styles/Styles.css");

		stage.getIcons().add(new Image(MainApp.class.getResourceAsStream("/icon/s4s.png")));
		stage.setTitle("sim4stamp : STAMP/STPA シミュレータ");
		stage.setScene(scene);
		stage.show();
		// System.out.println("start main");

		scene.widthProperty().addListener(
				(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) -> {
					PanelManager.get().getModelPanel().drawCanvasPanel();
				});
		scene.heightProperty().addListener(
				(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) -> {
					PanelManager.get().getModelPanel().drawCanvasPanel();
				});

	}

	public static Stage getStage() {
		return stage;
	}

	public static void main(String[] args) {
		launch(args);
	}

}
