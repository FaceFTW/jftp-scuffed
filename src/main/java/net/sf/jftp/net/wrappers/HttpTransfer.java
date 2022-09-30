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
	public boolean pause = false;
	public Thread runner;
	private int stat = 1;
	private ConnectionHandler handler = new ConnectionHandler();

	public HttpTransfer(String url, String localPath, Vector listeners, ConnectionHandler handler) {
		this.url = url;
		this.localPath = localPath;
		this.listeners = listeners;
		this.handler = handler;

		file = net.sf.jftp.system.StringUtils.getFile(url);

		this.prepare();
	}

	public void prepare() {
		runner = new Thread(this);
		runner.setPriority(Thread.MIN_PRIORITY);
		runner.start();
	}

	public void run() {
		try {
			if (handler.getConnections().get(file) == null) {
				net.sf.jftp.system.logging.Log.out("download started: " + url);
				net.sf.jftp.system.logging.Log.out("connection handler present: " + handler + ", poll size: " + handler.getConnections().size());
				net.sf.jftp.system.logging.Log.out("local file: " + localPath + file);
				handler.addConnection(file, this);
			} else {
				net.sf.jftp.system.logging.Log.debug("Transfer already in progress: " + file);
				work = false;
				stat = 2;

				return;
			}

			URL u = new URL(url);

			BufferedOutputStream f = new BufferedOutputStream(new FileOutputStream(localPath + file));
			BufferedInputStream in = new BufferedInputStream(u.openStream());
			byte[] buf = new byte[4096];
			int len = 0;

			while ((stat > 0) && work) {
				stat = in.read(buf);

				if (stat == -1) {
					break;
				}

				f.write(buf, 0, stat);

				len += stat;
				this.fireProgressUpdate(file, DataConnection.GET, len);
			}

			f.flush();
			f.close();
			in.close();

			this.fireProgressUpdate(file, DataConnection.FINISHED, len);
		} catch (Exception ex) {
			work = false;
			net.sf.jftp.system.logging.Log.debug("Download failed: " + ex);

			File f = new File(localPath + file);
			f.delete();
			this.fireProgressUpdate(file, DataConnection.FAILED, -1);

			ex.printStackTrace();

			return;
		}

		if (!work) {
			File f = new File(localPath + file);
			f.delete();
			net.sf.jftp.system.logging.Log.out("download aborted: " + file);
		}
	}

	public void fireProgressUpdate(String file, String type, int bytes) {
		if (listeners == null) {
			return;
		}

		for (int i = 0; i < listeners.size(); i++) {
			((ConnectionListener) listeners.elementAt(i)).updateProgress(file, type, bytes);
		}
	}

	public int getStatus() {
		return stat;
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
