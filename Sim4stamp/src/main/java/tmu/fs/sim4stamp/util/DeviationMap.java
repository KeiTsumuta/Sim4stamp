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
package tmu.fs.sim4stamp.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Keiichi Tsumuta
 */
public class DeviationMap {

	private static final Deviation[] DEVIATIONS = { //
			Deviation.NORMAL, //
			Deviation.NOT_PROVIDING, //
			Deviation.PROVIDING_MORE, //
			Deviation.PROVIDING_LESS, //
			Deviation.TOO_EARLY, //
			Deviation.TOO_LATE, //
			Deviation.WRONG_ORDER, //
			Deviation.STOPPING_TOO_SOON, //
			Deviation.APPLYING_TOO_LONG //
	};

	private static final Map<Integer, Deviation> dmap = new HashMap<Integer, Deviation>();

	static {
		for (Deviation d : DEVIATIONS) {
			dmap.put(new Integer(d.getId()), d);
		}
	}

	public static Deviation getDeviation(int id) {
		return dmap.get(id);
	}

	public static Deviation getDeviation(String sid) {
		try {
			return getDeviation(Integer.parseInt(sid));
		} catch (Exception ex) {
		}
		return Deviation.NORMAL;
	}

	public static Deviation getNameToDeviation(String name) {
		Iterator<Integer> it = dmap.keySet().iterator();
		while (it.hasNext()) {
			int id = it.next();
			Deviation d = dmap.get(id);
			if (d.toString().equals(name)) {
				return d;
			}
		}
		return Deviation.NORMAL;
	}

}
