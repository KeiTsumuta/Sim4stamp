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
package tmu.fs.sim4stamp.util;

/**
 * UCAに関連する情報を保持する。
 *
 * @author Keiichi Tsumuta
 */
public class UCAItem {

	private final String title; // タイトル
	private final String nf;  // 偏差投入のコネクタ位置
	private final String nt;  // 偏差投入のコネクタ位置
	private final String deviationParamId; // 偏差投入コネクタ
	private final String deviationType; // 偏差投入の種類
	private final String elementId; // 逸脱検出ノード
	private final String itemId;  // 逸脱データ
	private final boolean attentionUpper;
	private final boolean attentionUnder;

	public UCAItem(String title, String nf, String nt, String dId, String type, String elementId, String itemId, boolean attentionUpper, boolean attentionUnder) {
		this.title = title;
		this.nf = nf;
		this.nt = nt;
		this.deviationParamId = dId;
		this.deviationType = type;
		this.elementId = elementId;
		this.itemId = itemId;
		this.attentionUpper = attentionUpper;
		this.attentionUnder = attentionUnder;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the nf
	 */
	public String getNf() {
		return nf;
	}

	/**
	 * @return the nt
	 */
	public String getNt() {
		return nt;
	}

	/**
	 * @return the deviationParamId
	 */
	public String getDeviationParamId() {
		return deviationParamId;
	}

	/**
	 * @return the deviationType
	 */
	public String getDeviationType() {
		return deviationType;
	}

	/**
	 * @return the elementId
	 */
	public String getElementId() {
		return elementId;
	}

	/**
	 * @return the itemId
	 */
	public String getItemId() {
		return itemId;
	}

	/**
	 * @return the attentionUpper
	 */
	public boolean isAttentionUpper() {
		return attentionUpper;
	}

	/**
	 * @return the attentionUnder
	 */
	public boolean isAttentionUnder() {
		return attentionUnder;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append(getNf()).append(" --> ");
		sb.append(getNt()).append(" [");
		sb.append(getDeviationParamId()).append("]},");
		sb.append("＜").append(getDeviationType()).append("＞,");
		sb.append("逸脱位置（").append(getElementId()).append(",").append(getItemId());
		sb.append("）,上限：").append(attentionUpper).append(" 下限：").append(attentionUnder);
		return sb.toString();
	}
}
