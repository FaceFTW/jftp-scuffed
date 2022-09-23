package net.sf.jftp.net;

public class FtpKeepAliveThread implements Runnable {

	private final Thread runner;
	private final FtpConnection conn;

	public FtpKeepAliveThread(FtpConnection conn) {
		this.conn = conn;

		runner = new Thread(this);
		runner.start();
	}

	public void run() {
		while (conn.isConnected()) {
			try {
				Thread.sleep(net.sf.jftp.config.Settings.ftpKeepAliveInterval);

				conn.noop();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
