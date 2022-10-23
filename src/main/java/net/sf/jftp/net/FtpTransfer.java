package net.sf.jftp.net;

import net.sf.jftp.JFtp;
import net.sf.jftp.config.Settings;
import net.sf.jftp.system.logging.Log;
import net.sf.jftp.util.I18nHelper;

import javax.swing.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;


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
	private final java.util.List<ConnectionListener> listeners;
	private Thread runner;
	private FtpConnection con;
	private int stat;
	private boolean started;
	private String newName;
	private int transferStatus;
	private String crlf;

	public FtpTransfer(String host, int port, String localPath, String remotePath, String file, String user, String pass, String type, ConnectionHandler handler, java.util.List<ConnectionListener> listeners, String newName, String crlf) {
		super();
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

		if (null == handler) {
			handler = new ConnectionHandler();
		}

		this.prepare();
	}

	public FtpTransfer(String host, int port, String localPath, String remotePath, String file, String user, String pass, String type, ConnectionHandler handler, java.util.List<ConnectionListener> listeners, String crlf) {
		super();
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

		if (null == handler) {
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
		if (null == this.handler.getConnections().get(this.file)) {
			this.handler.addConnection(this.file, this);
		} else if (!this.pause) {
			Log.debug(MessageFormat.format(I18nHelper.getLogString("transfer.already.in.progress.0"), this.file));
			this.work = false;
			this.stat = 2;

			return;
		}

		boolean hasPaused = false;

		while (this.pause) {
			try {
				Thread.sleep(100);

				if (null != this.listeners) {
					for (ConnectionListener listener : this.listeners) {
						listener.updateProgress(this.file, Transfer.PAUSED, -1);
					}
				}

				if (!this.work) {
					if (null != this.listeners) {
						for (ConnectionListener listener : this.listeners) {
							listener.updateProgress(this.file, Transfer.REMOVED, -1);
						}
					}
				}
			} catch (Exception ex) {
			}

			hasPaused = true;
		}

		while ((this.handler.getConnectionSize() >= Settings.getMaxConnections()) && (0 < this.handler.getConnectionSize()) && this.work) {
			try {
				this.stat = 4;
				Thread.sleep(400);

				if (!hasPaused && (null != this.listeners)) {
					for (ConnectionListener listener : this.listeners) {
						listener.updateProgress(this.file, Transfer.QUEUED, -1);
					}
				} else {
					break;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		if (!this.work) {
			if (null != this.listeners) {
				for (ConnectionListener listener : this.listeners) {
					listener.updateProgress(this.file, Transfer.REMOVED, -1);
				}
			}

			this.handler.removeConnection(this.file);
			this.stat = 3;

			return;
		}

		this.started = true;

		try {
			Thread.sleep(Settings.ftpTransferThreadPause);
		} catch (Exception ex) {
		}

		this.con = new FtpConnection(this.host, this.port, this.remotePath, this.crlf);

		this.con.setConnectionHandler(this.handler);
		this.con.setConnectionListeners(this.listeners);

		int status = this.con.login(this.user, this.pass);

		if (FtpConstants.LOGIN_OK == status) {
			File f = new File(this.localPath);
			this.con.setLocalPath(f.getAbsolutePath());

			if (this.type.equals(Transfer.UPLOAD)) {
				if (null != this.newName) {
					this.transferStatus = this.con.upload(this.file, this.newName);
				} else {
					Path p = Paths.get(this.file + ".lock");
					System.out.println(p.getFileName().toString());
					System.out.println("d: " + this.con.download(p.getFileName().toString()));
					int stat = this.con.download(p.getFileName().toString());
					System.out.println("stat: " + stat);
					if (stat == FtpConstants.PERMISSION_DENIED) {
						JFtp.remoteDir.getCon().sendRawCommand("STOR " + p.getFileName());
						this.transferStatus = this.con.upload(this.file);
						this.con.removeFileOrDir(p.getFileName().toString());
					} else {
						JOptionPane.showMessageDialog(JFtp.mainFrame, "Lock file exists! Someone else is concurrently editing this file");
					}
					if (stat == FtpConstants.TRANSFER_SUCCESSFUL) p.toFile().delete();
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
