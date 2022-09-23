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

	public FtpTransfer(String host, int port, String localPath, String remotePath, String file, String user, String pass, String type, ConnectionHandler handler, Vector<ConnectionListener> listeners, String newName, String crlf) {
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

		prepare();
	}

	public FtpTransfer(String host, int port, String localPath, String remotePath, String file, String user, String pass, String type, ConnectionHandler handler, Vector<ConnectionListener> listeners, String crlf) {
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

		prepare();
	}

	public void prepare() {
		runner = new Thread(this);
		runner.setPriority(Thread.MIN_PRIORITY);
		runner.start();
	}

	public void run() {
		//System.out.println(file);
		if (handler.getConnections().get(file) == null) {
			handler.addConnection(file, this);
		} else if (!pause) {
			net.sf.jftp.system.logging.Log.debug("Transfer already in progress: " + file);
			work = false;
			stat = 2;

			return;
		}

		boolean hasPaused = false;

		while (pause) {
			try {
				Thread.sleep(100);

				if (listeners != null) {
					for (int i = 0; i < listeners.size(); i++) {
						listeners.elementAt(i).updateProgress(file, PAUSED, -1);
					}
				}

				if (!work) {
					if (listeners != null) {
						for (int i = 0; i < listeners.size(); i++) {
							listeners.elementAt(i).updateProgress(file, REMOVED, -1);
						}
					}
				}
			} catch (Exception ex) {
			}

			hasPaused = true;
		}

		while ((handler.getConnectionSize() >= net.sf.jftp.config.Settings.getMaxConnections()) && (handler.getConnectionSize() > 0) && work) {
			try {
				stat = 4;
				Thread.sleep(400);

				if (!hasPaused && (listeners != null)) {
					for (int i = 0; i < listeners.size(); i++) {
						listeners.elementAt(i).updateProgress(file, QUEUED, -1);
					}
				} else {
					break;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		if (!work) {
			if (listeners != null) {
				for (int i = 0; i < listeners.size(); i++) {
					listeners.elementAt(i).updateProgress(file, REMOVED, -1);
				}
			}

			handler.removeConnection(file);
			stat = 3;

			return;
		}

		started = true;

		try {
			Thread.sleep(net.sf.jftp.config.Settings.ftpTransferThreadPause);
		} catch (Exception ex) {
		}

		con = new FtpConnection(host, port, remotePath, crlf);

		con.setConnectionHandler(handler);
		con.setConnectionListeners(listeners);

		int status = con.login(user, pass);

		if (status == FtpConnection.LOGIN_OK) {
			File f = new File(localPath);
			con.setLocalPath(f.getAbsolutePath());

			if (type.equals(UPLOAD)) {
				if (newName != null) {
					transferStatus = con.upload(file, newName);
				} else {
					transferStatus = con.upload(file);
				}
			} else {
				transferStatus = con.download(file);
			}
		}

		if (!pause) {
			handler.removeConnection(file);
		}
	}

	public int getStatus() {
		return stat;
	}

	public int getTransferStatus() {
		return transferStatus;
	}

	public boolean hasStarted() {
		return started;
	}

	public FtpConnection getFtpConnection() {
		return con;
	}

	public DataConnection getDataConnection() {
		return con.getDataConnection();
	}
}
