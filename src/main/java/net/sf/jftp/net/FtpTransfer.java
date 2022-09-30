package net.sf.jftp.net;

import java.io.File;
import java.util.Vector;


/**
 * This class is used internally by FtpConnection.
 * You probably don't have to use it directly.
 */
public class FtpTransfer extends Transfer implements Runnable {
	private final String host;
	private final int port;
	private final String localPath;
	private final String remotePath;
	private final String file;
	private final String user;
	private final String pass;
	private final String type;
	private final ConnectionHandler handler;
	private final Vector<ConnectionListener> listeners;
	public Thread runner;
	private FtpConnection con = null;
	private int stat = 0;
	private boolean started = false;
	private String newName = null;
	private int transferStatus = 0;
	private String crlf = null;

	public FtpTransfer(final String host, final int port, final String localPath, final String remotePath, final String file, final String user, final String pass, final String type, ConnectionHandler handler, final Vector<ConnectionListener> listeners, final String newName, final String crlf) {
		this.host = host;
		this.port = port;
		this.localPath = localPath;
		this.remotePath = remotePath;
		this.file = file;
		this.user = user;
		this.pass = pass;
		this.type = type;
		this.handler = handler;
		this.listeners = listeners;
		this.newName = newName;
		this.crlf = crlf;

		if (handler == null) {
			handler = new ConnectionHandler();
		}

		this.prepare();
	}

	public FtpTransfer(final String host, final int port, final String localPath, final String remotePath, final String file, final String user, final String pass, final String type, ConnectionHandler handler, final Vector<ConnectionListener> listeners, final String crlf) {
		this.host = host;
		this.port = port;
		this.localPath = localPath;
		this.remotePath = remotePath;
		this.file = file;
		this.user = user;
		this.pass = pass;
		this.type = type;
		this.handler = handler;
		this.listeners = listeners;
		this.crlf = crlf;

		if (handler == null) {
			handler = new ConnectionHandler();
		}

		this.prepare();
	}

	public void prepare() {
		this.runner = new Thread(this);
		this.runner.setPriority(Thread.MIN_PRIORITY);
		this.runner.start();
	}

	public void run() {
		//System.out.println(file);
		if (this.handler.getConnections().get(this.file) == null) {
			this.handler.addConnection(this.file, this);
		} else if (!this.pause) {
			net.sf.jftp.system.logging.Log.debug("Transfer already in progress: " + this.file);
			this.work = false;
			this.stat = 2;

			return;
		}

		boolean hasPaused = false;

		while (this.pause) {
			try {
				Thread.sleep(100);

				if (this.listeners != null) {
					for (int i = 0; i < this.listeners.size(); i++) {
						this.listeners.elementAt(i).updateProgress(this.file, net.sf.jftp.net.Transfer.PAUSED, -1);
					}
				}

				if (!this.work) {
					if (this.listeners != null) {
						for (int i = 0; i < this.listeners.size(); i++) {
							this.listeners.elementAt(i).updateProgress(this.file, net.sf.jftp.net.Transfer.REMOVED, -1);
						}
					}
				}
			} catch (final Exception ex) {
			}

			hasPaused = true;
		}

		while ((this.handler.getConnectionSize() >= net.sf.jftp.config.Settings.getMaxConnections()) && (this.handler.getConnectionSize() > 0) && this.work) {
			try {
				this.stat = 4;
				Thread.sleep(400);

				if (!hasPaused && (this.listeners != null)) {
					for (int i = 0; i < this.listeners.size(); i++) {
						this.listeners.elementAt(i).updateProgress(this.file, net.sf.jftp.net.Transfer.QUEUED, -1);
					}
				} else {
					break;
				}
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		}

		if (!this.work) {
			if (this.listeners != null) {
				for (int i = 0; i < this.listeners.size(); i++) {
					this.listeners.elementAt(i).updateProgress(this.file, net.sf.jftp.net.Transfer.REMOVED, -1);
				}
			}

			this.handler.removeConnection(this.file);
			this.stat = 3;

			return;
		}

		this.started = true;

		try {
			Thread.sleep(net.sf.jftp.config.Settings.ftpTransferThreadPause);
		} catch (final Exception ex) {
		}

		this.con = new FtpConnection(this.host, this.port, this.remotePath, this.crlf);

		this.con.setConnectionHandler(this.handler);
		this.con.setConnectionListeners(this.listeners);

		final int status = this.con.login(this.user, this.pass);

		if (status == FtpConnection.LOGIN_OK) {
			final File f = new File(this.localPath);
			this.con.setLocalPath(f.getAbsolutePath());

			if (this.type.equals(net.sf.jftp.net.Transfer.UPLOAD)) {
				if (this.newName != null) {
					this.transferStatus = this.con.upload(this.file, this.newName);
				} else {
					this.transferStatus = this.con.upload(this.file);
				}
			} else {
				this.transferStatus = this.con.download(this.file);
			}
		}

		if (!this.pause) {
			this.handler.removeConnection(this.file);
		}
	}

	public int getStatus() {
		return this.stat;
	}

	public int getTransferStatus() {
		return this.transferStatus;
	}

	public boolean hasStarted() {
		return this.started;
	}

	public FtpConnection getFtpConnection() {
		return this.con;
	}

	public DataConnection getDataConnection() {
		return this.con.getDataConnection();
	}
}
