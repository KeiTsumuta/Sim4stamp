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

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Keiichi Tsumuta
 */
public class SimServer implements Runnable {

	private ServerSocket server;

	public SimServer(int portNo) throws Exception {
		server = new ServerSocket(portNo);
	}

	@Override
	public void run() {
		ExecutorService es = Executors.newCachedThreadPool();
		try {
			for (;;) {
				Socket client = server.accept();
				es.execute(new SimListener(client));
			}
		} catch (SocketException se) {
			System.out.println("SimServer, Loop out.");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void close() {
		try {
			if (server != null) {
				server.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		server = null;
	}

}
