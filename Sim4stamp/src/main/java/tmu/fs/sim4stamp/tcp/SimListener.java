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
package tmu.fs.sim4stamp.tcp;

import java.io.EOFException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import tmu.fs.sim4stamp.model.iop.IOParam;
import tmu.fs.sim4stamp.model.iop.IOScene;
import tmu.fs.sim4stamp.model.iop.IOValue;
import tmu.fs.sim4stamp.state.OvertureExecManager;

/**
 *
 * @author Keiichi Tsumuta
 */
public class SimListener implements Runnable {

	private final Socket client;
	private static OvertureExecManager oeManager;

	public SimListener(Socket client) {
		this.client = client;
		oeManager = OvertureExecManager.getInstance();
	}

	@Override
	public void run() {
		try (InputStream fromClient = client.getInputStream();
				ObjectInputStream oi = new ObjectInputStream(fromClient);
				OutputStream toClient = client.getOutputStream();
				ObjectOutputStream oo = new ObjectOutputStream(toClient);) {
			boolean loop = true;
			while (loop) {
				TransObject inObj = (TransObject) oi.readObject();
				String id = inObj.getId();
				// System.out.println("TCP-IP read id:" + id);
				if (id == null) {
					break;
				}
				TransObject sendObj = null;
				if (id.equals("finish")) {
					sendObj = new TransObject("finish");
					oo.writeObject(sendObj);
					oo.flush();
					break;
				}
				sendObj = getReply(id, inObj);
				oo.writeObject(sendObj);
				oo.flush();
			}
		} catch (EOFException eoe) {
			// System.out.println("conection close!!");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			client.close();
		} catch (Exception ex) {

		}
		// System.out.println("conection out");
	}

	private static TransObject getReply(String id, TransObject inObj) {	
		TransObject tobj = new TransObject(id);
		switch (id) {
		case "init_start":
			oeManager.calcInit();
			break;
		case "is_loop":
			if (oeManager.hasNext()) {
				tobj.addStValue("y");
			} else {
				tobj.addStValue("n");
			}
			break;
		case "elem_order":
			setOrder(tobj);
			break;
		case "read_data":
			getReadData(inObj, tobj);
			break;
		case "write_data":
			getWriteData(inObj, tobj);
			break;
		default:
			break;
		}
		return tobj;
	}

	private static void setOrder(TransObject tobj) {
		List<String> list = oeManager.getElementOrders();
		for (int i = 0; i < list.size(); i++) {
			tobj.addStValue(list.get(i));
		}
	}

	private static void getReadData(TransObject inObj, TransObject tobj) {
		String elemId = inObj.getStValues().get(0);
		String dataId = inObj.getStValues().get(1);
		tobj.addStValue(dataId);
		IOScene ios = oeManager.getExecuteScene();
		IOValue iov = ios.getIOData(elemId, dataId);
		if (iov != null) {
			IOParam.ValueType type = iov.getType();
			if (type == IOParam.ValueType.REAL) {
				tobj.addDValue(oeManager.getData(elemId, dataId));
			} else if (type == IOParam.ValueType.INT) {
				tobj.addIValue(oeManager.getIntData(elemId, dataId));
			} else if (type == IOParam.ValueType.BOOL) {
				tobj.addBValue(oeManager.getBoolData(elemId, dataId));
			}
		} else { // 未定義データは実数としてとりあえず設定する。
			tobj.addDValue(oeManager.getData(elemId, dataId));
		}
	}

	private static void getWriteData(TransObject inObj, TransObject tobj) {
		String elemId = inObj.getStValues().get(0);
		String dataId = inObj.getStValues().get(1);
		IOScene ios = oeManager.getExecuteScene();
		IOValue iov = ios.getIOData(elemId, dataId);
		if (iov != null) {
			IOParam.ValueType type = iov.getType();
			if (type == IOParam.ValueType.REAL && inObj.getDValues() != null) {
				oeManager.setData(elemId, dataId, inObj.getDValues().get(0));
			} else if (type == IOParam.ValueType.INT && inObj.getIValues() != null) {
				oeManager.setIntData(elemId, dataId, inObj.getIValues().get(0));
			} else if (type == IOParam.ValueType.BOOL && inObj.getBValues() != null) {
				oeManager.setBoolData(elemId, dataId, inObj.getBValues().get(0));
			}
		} else { // 未定義データは実数としてとりあえず処理する。
			if (inObj.getDValues() != null) {
				oeManager.setData(elemId, dataId, inObj.getDValues().get(0));
			}
		}
		// tobj.addStValue("status");
		// tobj.addIntValue(0);
	}

}
