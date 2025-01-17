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
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import tmu.fs.sim4stamp.gui.util.ResultValue;

/**
 * 結果表の表示データを編集する。
 *
 * @author Keiichi Tsumuta
 */
public class ResultTablePanel implements Initializable {

	private final TableView resultTable;
	private ObservableList<String> headers;
	private ObservableList<ObservableList> dataVals;

	public ResultTablePanel(TableView resultTable) {
		this.resultTable = resultTable;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initData();
	}

	public void initData() {
		headers = FXCollections.observableArrayList();
		dataVals = FXCollections.observableArrayList();
		TableColumn<ObservableList, String>[] columns = new TableColumn[headers.size()];
		resultTable.getColumns().setAll(columns);
		resultTable.setItems(dataVals);
	}

	public void setData(List<String> colParents, List<List<String>> colTitles, List<ResultValue[]> data) {
		headers = FXCollections.observableArrayList();
		headers.add("No.");
		colParents.forEach((colHeader) -> {
			headers.add(colHeader);
		});
		int colIndex = 0;
		int indexAll = colIndex;
		TableColumn<ObservableList, String>[] columns = new TableColumn[headers.size()];
		for (String header : headers) {
			final int idx = colIndex;
			columns[colIndex] = new TableColumn(header);
			columns[colIndex].setStyle("-fx-alignment: CENTER-RIGHT;");
			columns[colIndex]
					.setCellValueFactory((CellDataFeatures<ObservableList, String> param)
							-> new SimpleStringProperty(param.getValue().get(idx).toString()));
			if (colIndex > 0) {
				List<String> subColHeaders = colTitles.get(colIndex - 1);
				ObservableList<String> subHeaders = FXCollections.observableArrayList();
				subColHeaders.forEach((hs) -> {
					subHeaders.add(hs);
				});
				int subColIndex = 0;
				TableColumn<ObservableList, String>[] subColumns = new TableColumn[subColHeaders.size()];
				for (String subHeader : subHeaders) {
					final int fIndexAll = indexAll;
					subColumns[subColIndex] = new TableColumn(subHeader);
					subColumns[subColIndex].setStyle("-fx-alignment: CENTER-RIGHT;");
					subColumns[subColIndex].setCellValueFactory(
							(CellDataFeatures<ObservableList, String> param)
							-> new SimpleStringProperty(param.getValue().get(fIndexAll).toString()));
					subColumns[subColIndex].setCellFactory(tableColumn -> {
						return new TableCell<ObservableList, String>() {
							@Override
							protected void updateItem(final String item, final boolean empty) {
								super.updateItem(item, empty);
								if (item != null) {
									String val = item;
									setText(val);
									if (val.startsWith("*")) {
										getStyleClass().add("tableCellClass");
									} else {
										getStyleClass().remove("tableCellClass");
									}
								}
							}
						};
					});
					subColIndex++;
					indexAll++;
				}
				columns[colIndex].getColumns().addAll(subColumns);
			} else {
				indexAll++;
			}
			colIndex++;
		}
		resultTable.getColumns().setAll(columns);
		
		dataVals = FXCollections.observableArrayList();
		int size = data.get(0).length;
		for (int i = 0; i < size; i++) {
			ObservableList<String> rows = FXCollections.observableArrayList();
			rows.add(Integer.toString(i + 1));
			for (int k = 0; k < data.size(); k++) {
				rows.add(data.get(k)[i].getResult());
			}
			dataVals.add(rows);
		}
		resultTable.setItems(dataVals);
	}

}
