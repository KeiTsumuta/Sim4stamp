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
package ovt.ctl.lib;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import org.overture.interpreter.runtime.ValueException;
import org.overture.interpreter.values.BooleanValue;
import org.overture.interpreter.values.IntegerValue;
import org.overture.interpreter.values.NilValue;
import org.overture.interpreter.values.RealValue;
import org.overture.interpreter.values.SeqValue;
import org.overture.interpreter.values.Value;
import org.overture.interpreter.values.ValueList;
import org.overture.interpreter.values.VoidValue;
import tmu.fs.sim4stamp.tcp.TransObject;

/**
 *
 * @author Keiichi Tsumuta
 */
public class CtlTool {

	private static final int TCP_PORT_NO = 8001;
	private final String encoding = System.getProperty("file.encoding");

	private static Socket socket = null;
	private static ObjectOutputStream objOutStream = null;
	private static ObjectInputStream objInStream = null;
	private static boolean isStepExecute = false;

	private void openConnection() {
		closeConnection();
		try {
			socket = new Socket("localhost", TCP_PORT_NO);
			OutputStream out = socket.getOutputStream();
			objOutStream = new ObjectOutputStream(out);
			InputStream in = socket.getInputStream();
			objInStream = new ObjectInputStream(in);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void closeConnection() {
		try {
			if (objInStream != null) {
				objInStream.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		objInStream = null;
		try {
			if (objOutStream != null) {
				objOutStream.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		objOutStream = null;
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		socket = null;
	}

	private TransObject sendObject(TransObject sendObj) {
		TransObject receive = null;
		try {
			objOutStream.writeObject(sendObj);
			objOutStream.flush();
			receive = (TransObject) objInStream.readObject();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return receive;
	}

	public Value init() throws ValueException {
		openConnection();
		TransObject sObj = new TransObject("init_start");
		TransObject rObj = sendObject(sObj);
		return new BooleanValue(true);
	}

	public Value finish() throws ValueException {
		TransObject sObj = new TransObject("finish");
		try {
			TransObject rObj = sendObject(sObj);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		closeConnection();
		return new BooleanValue(true);
	}

	public Value isLoop() throws ValueException {
		TransObject sObj = new TransObject("is_loop");
		TransObject rObj = sendObject(sObj);
		List<String> values = rObj.getStValues();
		if (values != null && values.get(0).equals("y")) {
			return new BooleanValue(true);
		}
		return new BooleanValue(false);
	}

	public Value breakLine(Value elementId) throws ValueException {
		if (isStepExecute) {
			String elemId = elementId.stringValue(null);
			TransObject sObj = new TransObject("breakline");
			sObj.addStValue(elemId);
			TransObject rObj = sendObject(sObj);
			List<String> values = rObj.getStValues();
		}
		return new VoidValue();
	}

	public Value getElementOrder() throws ValueException {
		isStepExecute = false;
		TransObject sObj = new TransObject("elem_order");
		TransObject rObj = sendObject(sObj);
		List<String> orders = rObj.getStValues();
		List<Boolean> bVals = rObj.getBValues();
		ValueList list = new ValueList();
		try {
			for (String order : orders) {
				// print("---:" + order);
				list.add(new SeqValue(order));
			}
			if (bVals != null && bVals.size() == 1 && bVals.get(0)) {
				isStepExecute = true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new SeqValue(list);
	}

	public Value getCtlId() throws ValueException {
		TransObject sObj = new TransObject("get_elem_id");
		TransObject rObj = sendObject(sObj);
		SeqValue val = new SeqValue(rObj.getStValues().get(0));
		return val;
	}

	public Value getInputValue(Value elementId, Value dataId) throws ValueException {
		String elemId = elementId.stringValue(null);
		String keyId = dataId.stringValue(null);
		TransObject sObj = new TransObject("read_data");
		sObj.addStValue(elemId);
		sObj.addStValue(keyId);
		TransObject rObj = sendObject(sObj);
		try {
			double val = rObj.getDValues().get(0);
			RealValue rv = new RealValue(val);
			return rv;
		} catch (Exception ex) {
		}
		return new RealValue(0L);
	}

	public Value setValue(Value elementId, Value dataId, Value value) throws ValueException {
		String elemId = elementId.stringValue(null);
		String keyId = dataId.stringValue(null);
		TransObject sObj = new TransObject("write_data");
		sObj.addStValue(elemId);
		sObj.addStValue(keyId);
		sObj.addDValue(value.realValue(null));
		TransObject rObj = sendObject(sObj);
		return new VoidValue();
	}

	public Value getInputIntValue(Value elementId, Value dataId) throws ValueException {
		String elemId = elementId.stringValue(null);
		String keyId = dataId.stringValue(null);
		TransObject sObj = new TransObject("read_data");
		sObj.addStValue(elemId);
		sObj.addStValue(keyId);
		TransObject rObj = sendObject(sObj);
		try {
			int val = rObj.getIValues().get(0);
			IntegerValue iv = new IntegerValue(val);
			return iv;
		} catch (Exception ex) {
		}
		return new IntegerValue(0L);
	}

	public Value setIntValue(Value elementId, Value dataId, Value value) throws ValueException {
		String elemId = elementId.stringValue(null);
		String keyId = dataId.stringValue(null);
		TransObject sObj = new TransObject("write_data");
		sObj.addStValue(elemId);
		sObj.addStValue(keyId);
		sObj.addIValue((int) value.intValue(null));
		TransObject rObj = sendObject(sObj);
		return new VoidValue();
	}

	public Value getInputBoolValue(Value elementId, Value dataId) throws ValueException {
		String elemId = elementId.stringValue(null);
		String keyId = dataId.stringValue(null);
		TransObject sObj = new TransObject("read_data");
		sObj.addStValue(elemId);
		sObj.addStValue(keyId);
		TransObject rObj = sendObject(sObj);
		try {
			boolean val = rObj.getBValues().get(0);
			BooleanValue rv = new BooleanValue(val);
			return rv;
		} catch (Exception ex) {
		}
		return new BooleanValue(false);
	}

	public Value setBoolValue(Value elementId, Value dataId, Value value) throws ValueException {
		String elemId = elementId.stringValue(null);
		String keyId = dataId.stringValue(null);
		TransObject sObj = new TransObject("write_data");
		sObj.addStValue(elemId);
		sObj.addStValue(keyId);
		sObj.addBValue(value.boolValue(null));
		TransObject rObj = sendObject(sObj);
		return new VoidValue();
	}

	private void print(String s) {
		try {
			String es = new String(s.getBytes("UTF-8"), encoding);
			System.out.println(es);
		} catch (Exception ex) {
		}
	}
}
