package net.sf.jftp.net;

public class FtpKeepAliveThread implements Runnable {

	private final FtpConnection conn;

	public FtpKeepAliveThread(final FtpConnection conn) {
		this.conn = conn;

		final Thread runner = new Thread(this);
		runner.start();
	}

	public void run() {
		while (conn.isConnected()) {
			try {
				Thread.sleep(net.sf.jftp.config.Settings.ftpKeepAliveInterval);

				conn.noop();
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
