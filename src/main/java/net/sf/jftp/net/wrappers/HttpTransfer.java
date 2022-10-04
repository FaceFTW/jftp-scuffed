package net.sf.jftp.net.wrappers;

import net.sf.jftp.net.ConnectionHandler;
import net.sf.jftp.net.ConnectionListener;
import net.sf.jftp.net.DataConnection;
import net.sf.jftp.net.FtpConnection;
import net.sf.jftp.net.Transfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Vector;


public class HttpTransfer extends Transfer implements Runnable {
	private final String url;
	private final String localPath;
	private final String file;
	private final Vector listeners;
	public boolean work = true;
	public boolean pause;
	public Thread runner;
	private int stat = 1;
	private ConnectionHandler handler = new ConnectionHandler();

	public HttpTransfer(String url, String localPath, Vector listeners, ConnectionHandler handler) {
		super();
		this.url = url;
		this.localPath = localPath;
		this.listeners = listeners;
		this.handler = handler;

		this.file = net.sf.jftp.system.StringUtils.getFile(url);

		this.prepare();
	}

	public void prepare() {
		this.runner = new Thread(this);
		this.runner.setPriority(Thread.MIN_PRIORITY);
		this.runner.start();
	}

	public void run() {
		try {
			if (null == this.handler.getConnections().get(this.file)) {
				net.sf.jftp.system.logging.Log.out("download started: " + this.url);
				net.sf.jftp.system.logging.Log.out("connection handler present: " + this.handler + ", poll size: " + this.handler.getConnections().size());
				net.sf.jftp.system.logging.Log.out("local file: " + this.localPath + this.file);
				this.handler.addConnection(this.file, this);
			} else {
				net.sf.jftp.system.logging.Log.debug("Transfer already in progress: " + this.file);
				this.work = false;
				this.stat = 2;

				return;
			}

			URL u = new URL(this.url);

			BufferedOutputStream f = new BufferedOutputStream(new FileOutputStream(this.localPath + this.file));
			BufferedInputStream in = new BufferedInputStream(u.openStream());
			byte[] buf = new byte[4096];
			int len = 0;

			while ((0 < this.stat) && this.work) {
				this.stat = in.read(buf);

				if (-1 == this.stat) {
					break;
				}

				f.write(buf, 0, this.stat);

				len += this.stat;
				this.fireProgressUpdate(this.file, DataConnection.GET, len);
			}

			f.flush();
			f.close();
			in.close();

			this.fireProgressUpdate(this.file, DataConnection.FINISHED, len);
		} catch (Exception ex) {
			this.work = false;
			net.sf.jftp.system.logging.Log.debug("Download failed: " + ex);

			File f = new File(this.localPath + this.file);
			f.delete();
			this.fireProgressUpdate(this.file, DataConnection.FAILED, -1);

			ex.printStackTrace();

			return;
		}

		if (!this.work) {
			File f = new File(this.localPath + this.file);
			f.delete();
			net.sf.jftp.system.logging.Log.out("download aborted: " + this.file);
		}
	}

	public void fireProgressUpdate(String file, String type, int bytes) {
		if (null == this.listeners) {
			return;
		}

		for (int i = 0; i < this.listeners.size(); i++) {
			((ConnectionListener) this.listeners.elementAt(i)).updateProgress(file, type, bytes);
		}
	}

	public int getStatus() {
		return this.stat;
	}

	public boolean hasStarted() {
		return true;
	}

	public FtpConnection getFtpConnection() {
		return null;
	}

	public DataConnection getDataConnection() {
		return null;
	}
}
