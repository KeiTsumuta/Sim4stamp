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
import java.util.logging.Logger;
import tmu.fs.sim4stamp.PanelManager;
import tmu.fs.sim4stamp.SimService;
import tmu.fs.sim4stamp.gui.DeviationMapPanel;
import tmu.fs.sim4stamp.gui.ExecuteLogPanel;
import tmu.fs.sim4stamp.vdm.VdmCodeMaker;

/**
 *
 * @author Keiichi Tsumuta
 */
public class CommandLineExecute implements Runnable {

    private static Logger log = Logger.getLogger(CommandLineExecute.class.getPackage().getName());

    private static final String[] COMMAND_EXECUTE
            = new String[]{"-vdmpp", "-w", "-r", "vdm10",
                "-c", "UTF-8", "-e", "\"new ExecuteMain().execute()\""};
    private static final String VDM_LIB = "lib";
    private static final String STAMP_LIB = "stamplib";
    private static final String SIM4STAMP_TCP_LIB = VdmCodeMaker.CTLIB;
    private static final String VDMJ = "org.overture.interpreter.VDMJ";
    private static volatile boolean runStatus = false;
    private static LogQueue logQue;

    public CommandLineExecute() {
        if (logQue == null) {
            logQue = new LogQueue();
        }
    }

    public void start() {
        if (!runStatus) {
            runStatus = true;
            new Thread(this).start();
        }
    }

    public void run() {
        try {
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
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            runStatus = false;
        }
    }

    public void displayInputStream(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        try {
            for (;;) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                logQue.add(line + "\n");
            }
        } finally {
            br.close();
        }
    }

    class LogQueue {

        private BlockingQueue<String> que = new LinkedBlockingDeque<String>();

        public LogQueue() {

            Runnable runnable = new Runnable() {
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
