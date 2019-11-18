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
package tmu.fs.sim4stamp.state;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import tmu.fs.sim4stamp.PanelManager;
import tmu.fs.sim4stamp.SimService;
import tmu.fs.sim4stamp.gui.ExecuteLogPanel;
import tmu.fs.sim4stamp.util.Deviation;
import tmu.fs.sim4stamp.vdm.VdmCodeMaker;

/**
 *
 * @author Keiichi Tsumuta
 */
public class CommandLineExecute implements Runnable {

	public static final Deviation[] CONNECTOR_DEVIATIONS = { //
		Deviation.NORMAL, //
		Deviation.NOT_PROVIDING, //
		Deviation.PROVIDING_MORE, //
		Deviation.PROVIDING_LESS, //
		Deviation.TOO_LATE, //
		Deviation.STOPPING_TOO_SOON, //
		Deviation.APPLYING_TOO_LONG}; //

	private static final String[] COMMAND_EXECUTE = new String[]{"-vdmpp", "-w", "-r", "vdm10", "-c", "UTF-8", "-e",
		"\"new ExecuteMain().execute()\""};
	private static final String VDM_LIB = "lib";
	private static final String STAMP_LIB = "stamplib";
	private static final String SIM4STAMP_TCP_LIB = VdmCodeMaker.CTLIB;
	private static final String VDMJ = "org.overture.interpreter.VDMJ";
	private static volatile boolean isAllDeviationRun = false;
	private static volatile boolean runStatus = false;
	private static volatile boolean isStop = false;
	private static LogQueue logQue;

	private static VdmRunStatus exeComp = null;

	public CommandLineExecute(VdmRunStatus ec) {
		exeComp = ec;
		isStop = false;
		if (logQue == null) {
			logQue = new LogQueue();
		}
	}

	public void start() {
		vdmExecuteStart(false);
	}

	public void allStart() {
		vdmExecuteStart(true);
	}

	private void vdmExecuteStart(boolean mode) {
		isAllDeviationRun = mode;
		if (exeComp != null) {
			exeComp.startExec();
		}
		if (!runStatus) {
			runStatus = true;
			new Thread(this).start();
		} else {
			runEnd();
		}
	}

	private void runEnd() {
		if (exeComp != null) {
			exeComp.complete();
		}
	}

	@Override
	public void run() {
		try {
			if (isAllDeviationRun) {
				executeAllCases();
			} else {
				executeVdm();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			runStatus = false;
		}
		runEnd();
	}

	private void executeVdm() throws Exception {
		SimService ss = SimService.getInstance();
		String exeBase = ss.getCurrentProjectHome();
		List<String> list = new ArrayList<>();
		list.add("java");
		list.add("-cp");
		String cp = exeBase + SimService.SP + VDM_LIB + SimService.SP + SIM4STAMP_TCP_LIB;
		cp += SimService.PAS + ss.getOvertureCommandLineJar();
		list.add(cp);
		list.add(VDMJ);
		for (String opt : COMMAND_EXECUTE) {
			list.add(opt);
		}
		list.add("-path");
		list.add(exeBase);
		list.add(exeBase);
		list.add(exeBase + SimService.SP + VDM_LIB);
		list.add(exeBase + SimService.SP + STAMP_LIB);

		ProcessBuilder pb = new ProcessBuilder(list);
		pb = pb.redirectErrorStream(true);
		Process proc = pb.start();
		InputStream is = proc.getInputStream();
		displayInputStream(is);
		int exitVal = proc.waitFor();
	}

	private void executeAllCases() throws Exception {
		for (Deviation deviation : CONNECTOR_DEVIATIONS) {
			if (isStop) {
				return;
			}
			SimService.getInstance().getIoParamManager().getCurrentScene().setDeviation(deviation);
			PanelManager.get().getDeviationMapPanel().drawPanel();
			executeVdm();
		}
	}

	public void displayInputStream(InputStream is) throws IOException {
		try ( InputStreamReader isr = new InputStreamReader(is);  BufferedReader br = new BufferedReader(isr)) {
			for (;;) {
				String line = br.readLine();
				if (line == null) {
					break;
				}
				logQue.add(line + "\n");
			}
		}
	}

	/**
	 * @return the isStop
	 */
	public static boolean isStop() {
		return isStop;
	}

	/**
	 * @param aIsStop the isStop to set
	 */
	public static void setStop() {
		isStop = true;
	}

	class LogQueue {

		private BlockingQueue<String> que = new LinkedBlockingDeque<>();

		public LogQueue() {

			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					try {
						ExecuteLogPanel elp = PanelManager.get().getExecuteLogPanel();
						StringBuilder sb = new StringBuilder();
						for (;;) {
							String s = que.peek();
							if (s == null) {
								elp.addLine(sb.toString());
								sb = new StringBuilder();
							}
							String log = que.take();
							sb.append(log);
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			};
			ExecutorService es = Executors.newFixedThreadPool(1);
			es.submit(runnable);
		}

		public void add(String log) {
			try {
				que.put(log);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}
}
