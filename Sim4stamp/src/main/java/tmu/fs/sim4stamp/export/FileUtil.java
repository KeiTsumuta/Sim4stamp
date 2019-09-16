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
import java.io.FileOutputStream;

/**
 *
 * @author Keiichi Tsumuta
 */
public class FileUtil {
	
	private static final String ENCODED = "MS932";
	
	private FileUtil(){
		
	}

	public static void writeFile(String fileName, String data) {
		File file = new File(fileName);
		try ( FileOutputStream fo = new FileOutputStream(file)) {
			byte[] buf = data.getBytes(ENCODED);
			fo.write(buf);
			fo.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
