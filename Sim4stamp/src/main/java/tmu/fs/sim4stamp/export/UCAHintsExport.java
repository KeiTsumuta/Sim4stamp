/*
 *	 sim4stamp - The simulation tool for STAMP/STPA
 *	 Copyright (C) 2019  Keiichi Tsumuta
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
package tmu.fs.sim4stamp.export;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import tmu.fs.sim4stamp.SimService;
import tmu.fs.sim4stamp.model.IOParamManager;
import tmu.fs.sim4stamp.model.iop.IOScene;
import tmu.fs.sim4stamp.model.iop.IOValue;
import tmu.fs.sim4stamp.util.UCAItem;

/**
 * UCAヒント出力を作成する。
 *
 * @author Keiichi Tsumuta
 */
public class UCAHintsExport {

	private static final String SP = System.getProperty("file.separator");
	private static final SimpleDateFormat fmt = new SimpleDateFormat("'_'yyyyMMdd'_'HHmmss");

	private static final String TITLE = "No, Control Action, ヒントワードの種類, ハザードシナリオ, 安全制約違反, 補足";
	private List<IOScene> resultScenes;

	public UCAHintsExport() {

	}

	public void exportFile(File exportDir) {
		resultScenes = SimService.getInstance().getIoParamManager().getResultScenes();
		if (resultScenes == null) {
			return;
		}
		int deviationStart = 1;
		List<UCAItem> ucaItems = new ArrayList<>();
		try {
			for (IOScene ioScene : resultScenes) {
				String title = ioScene.getScene();
				deviationStart = ioScene.getDeviationStartIndex() + 1;
				String deviationType = ioScene.getDeviation().toString();

				IOParamManager iom = SimService.getInstance().getIoParamManager();
				List<String> elemIds = iom.getNodeIds();
				for (String elemId : elemIds) {
					List<IOValue> ioValues = ioScene.getIOValues(elemId);
					for (IOValue ioVal : ioValues) {
						ioVal.makeAttentions(deviationStart);
						String id = ioVal.getId();
						boolean upperStatus = ioVal.isUpperWarning(); // 上限逸脱ありフラグ
						boolean underStatus = ioVal.isUnderWarning(); // 下限逸脱ありフラグ
						boolean[] att = ioVal.getAttentionsUpper();
						if (upperStatus || underStatus) {
							String devParamId = ioScene.getDeviationConnParamId();
							String nf = ioScene.getDeviationConnParamFromId();
							String nt = ioScene.getDeviationConnParamToId();
							ucaItems.add(new UCAItem(title, nf, nt, devParamId, deviationType, elemId, id, upperStatus, underStatus));
						}
					}
				}
			}

			//for (UCAItem uca : ucaItems) {
			//	System.out.println(uca.toString());
			//}
			if (ucaItems.size() > 0) {
				UCAItem ucaItem = ucaItems.get(0);

				String buf = makeData(ucaItems);
				FileUtil.writeFile(findFileName(exportDir, "uca"), buf);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private String makeData(List<UCAItem> ucaItems) {
		StringBuilder sb = new StringBuilder();
		sb.append(TITLE).append("\n");
		for (int i = 0; i < ucaItems.size(); i++) {
			UCAItem ucaItem = ucaItems.get(i);
			sb.append(i).append(",******,");
			sb.append(ucaItem.getDeviationType()).append(",");
			sb.append(" ********** ,").append(ucaItem.getElementId()).append(" > ").append(ucaItem.getItemId());
			if (ucaItem.isAttentionUpper()) {
				sb.append(" 上限");
			}
			if (ucaItem.isAttentionUnder()) {
				sb.append(" 下限");
			}
			sb.append("逸脱,");
			sb.append(ucaItem.getTitle()).append("(");
			String p = ucaItem.getNf() + "-" + ucaItem.getNt() + "_" + ucaItem.getDeviationParamId();
			sb.append(p).append("偏差投入)");
			sb.append("\n");
		}
		return sb.toString();
	}

	private String findFileName(File dir, String fileName) {
		Date now = new Date();
		String ymdhms = fmt.format(now);
		return dir.getAbsolutePath() + SP + fileName + "_" + ymdhms + ".csv";
	}

}
