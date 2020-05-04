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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Keiichi Tsumuta
 */
public class ResourceFileIO {

	public String getResource(String fileName) {
		String content = null;
		try {
			byte[] contentBin = getBinResource(fileName);
			content = new String(contentBin, 0, contentBin.length, "UTF-8");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return content;
	}

	public byte[] getBinResource(String fileName) {
		byte[] contentBin = new byte[0];
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try (InputStream is = this.getClass().getResourceAsStream(fileName); BufferedInputStream bi = new BufferedInputStream(is);) {
			byte[] buf = new byte[1024];
			for (;;) {
				int size = bi.read(buf);
				if (size == -1) {
					break;
				}
				out.write(buf, 0, size);
			}
			contentBin = out.toByteArray();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return contentBin;
	}

	public void writeFile(String filname, byte[] buf) {
		File file = new File(filname);
		try (OutputStream os = new FileOutputStream(file); BufferedOutputStream bo = new BufferedOutputStream(os);) {
			bo.write(buf, 0, buf.length);
			bo.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void writeElementClassFile(String filename, byte[] buf) {
		File file = new File(filename);
		if (file.exists()) {
			writeFile(filename + "_ini", buf);
		} else {
			writeFile(filename, buf);
		}
	}

	public void deleteOldFiles(String filePath, String fname, String ext) {
		File file = new File(filePath);
		try {
			for (File delFile : file.listFiles()) {
				if (delFile.getName().startsWith(fname) && delFile.getName().endsWith(ext)) {
					delFile.delete();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
