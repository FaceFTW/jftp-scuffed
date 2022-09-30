package net.sf.jftp.net;

public class FtpKeepAliveThread implements Runnable {

	private final FtpConnection conn;

	public FtpKeepAliveThread(FtpConnection conn) {
		super();
		this.conn = conn;

		Thread runner = new Thread(this);
		runner.start();
	}

	public void run() {
		while (this.conn.isConnected()) {
			try {
				Thread.sleep(net.sf.jftp.config.Settings.ftpKeepAliveInterval);

				this.conn.noop();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
