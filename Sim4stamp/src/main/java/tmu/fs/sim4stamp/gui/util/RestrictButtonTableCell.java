/*
 *   sim4stamp - The simulation tool for STAMP/STPA
 *   Copyright (C) 2019  Keiichi Tsumuta
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
package tmu.fs.sim4stamp.gui.util;

import java.util.function.Function;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

/**
 *
 * @author Keiichi Tsumuta
 */
public class RestrictButtonTableCell<S> extends TableCell<S, Button> {

	private final Button actionButton;

	public RestrictButtonTableCell(String label, Function< S, S> function) {
		this.getStyleClass().add("action-button-table-cell");

		this.actionButton = new Button(label);
		this.actionButton.setOnAction((ActionEvent e) -> {
			function.apply(getCurrentItem());
		});
		this.actionButton.setMaxWidth(Double.MAX_VALUE);
	}

	public S getCurrentItem() {
		return (S) getTableView().getItems().get(getIndex());
	}

	public static <S> Callback<TableColumn<S, Button>, TableCell<S, Button>> forTableColumn(String label, Function< S, S> function) {
		return param -> new RestrictButtonTableCell<>(label, function);
	}

	@Override
	public void updateItem(Button item, boolean empty) {
		super.updateItem(item, empty);

		if (empty) {
			setGraphic(null);
		} else {
			setGraphic(actionButton);
		}
	}
}
