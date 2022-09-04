package net.sf.jftp.net;

import net.sf.jftp.config.Settings;

public class FtpKeepAliveThread implements Runnable {

	private Thread runner;
	private FtpConnection conn;
	
	public FtpKeepAliveThread(FtpConnection conn) {
		this.conn = conn;
		
		runner = new Thread(this);
		runner.start();
	}
	
	public void run() {
		while(conn.isConnected()) {
			try {
				Thread.sleep(Settings.ftpKeepAliveInterval);
				
				conn.noop();
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
