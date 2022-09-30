package net.sf.jftp.gui.base;

import net.sf.jftp.JFtp;
import net.sf.jftp.config.Settings;


public class LogFlusher implements Runnable {

	public LogFlusher() {
		super();
		Thread runner = new Thread(this);

		if (Settings.useLogFlusher) {
			runner.start();
		}
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(Settings.logFlushInterval);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}

			JFtp.statusP.jftp.ensureLogging();
		}
	}
}
