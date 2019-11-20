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
package tmu.fs.sim4stamp.export;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import tmu.fs.sim4stamp.SimService;
import tmu.fs.sim4stamp.gui.util.MakeResultTable;
import tmu.fs.sim4stamp.model.iop.IOScene;

/**
 * シミュレーション結果をCSV出力する。
 *
 * @author Keiichi Tsumuta
 */
public class ExportResults {

	private static final String CSV_FILE_NAME = "s4s_result";
	private static final String SP = System.getProperty("file.separator");
	private static final SimpleDateFormat fmt = new SimpleDateFormat("'_'yyyyMMdd'_'HHmmss");
	private List<IOScene> resultScenes;

	public ExportResults() {

	}

	public void exportFiles(File exportDir) {
		resultScenes = SimService.getInstance().getIoParamManager().getResultScenes();
		if (resultScenes == null) {
			return;
		}
		try {
			int p = 0;
			StringBuilder sb = new StringBuilder();
			for (IOScene ioScene : resultScenes) {
				String deviationConnParamId = ioScene.getDeviationConnParamId();
				String deviation = ioScene.getDeviation().toString();
				MakeResultTable mrt = new MakeResultTable(MakeResultTable.NUM_VALUE_MODE);
				mrt.makeResultTable(ioScene);
				List<String> eles = mrt.getParentElementIds();
				List<List<String>> dIds = mrt.getDataIds();
				List<String[]> dataList = mrt.getDataList();

				sb.append(deviationConnParamId).append(",");
				sb.append(deviation);
				int dataColumn = 0;
				int dataRow = 0;
				List<String> title1s = new ArrayList<>();
				List<String> title2s = new ArrayList<>();
				List<String[]> arr = new ArrayList<>();
				for (int i = 0; i < eles.size(); i++) {
					for (int n = 0; n < dIds.get(i).size(); n++) {
						title1s.add(eles.get(i));
						title2s.add(dIds.get(i).get(n));
						String[] list = dataList.get(dataColumn++);
						arr.add(list);
					}
				}
				sb.append(",構成要素");
				for (int i = 0; i < title1s.size(); i++) {
					sb.append(",").append(title1s.get(i));
				}
				sb.append("\n").append(",,パラメータ");
				for (int i = 0; i < title2s.size(); i++) {
					sb.append(",").append(title2s.get(i));
				}
				sb.append("\n");
				for (int n = 0; n < arr.get(0).length; n++) {
					sb.append(",,").append((n + 1));
					for (int i = 0; i < arr.size(); i++) {
						sb.append(",").append(arr.get(i)[n]);
					}
					sb.append("\n");
				}
				//System.out.println(sb.toString());

			}
			FileUtil.writeFile(findFileName(exportDir), sb.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private String findFileName(File dir) {
		Date now = new Date();
		String ymdhms = fmt.format(now);
		return dir.getAbsolutePath() + SP + CSV_FILE_NAME + ymdhms + ".csv";
	}

}
