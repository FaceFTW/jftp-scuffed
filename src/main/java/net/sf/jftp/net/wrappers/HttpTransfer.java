package net.sf.jftp.net.wrappers;

import net.sf.jftp.net.ConnectionHandler;
import net.sf.jftp.net.ConnectionListener;
import net.sf.jftp.net.DataConnection;
import net.sf.jftp.net.FtpConnection;
import net.sf.jftp.net.Transfer;
import net.sf.jftp.system.StringUtils;
import net.sf.jftp.system.logging.Log;
import net.sf.jftp.util.I18nHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Vector;


public class HttpTransfer extends Transfer implements Runnable {
	private final String url;
	private final String localPath;
	private final String file;
	private final Vector listeners;
	public boolean pause;
	private boolean work = true;
	private Thread runner;
	private int stat = 1;
	private ConnectionHandler handler = new ConnectionHandler();

	public HttpTransfer(String url, String localPath, Vector listeners, ConnectionHandler handler) {
		super();
		this.url = url;
		this.localPath = localPath;
		this.listeners = listeners;
		this.handler = handler;

		this.file = StringUtils.getFile(url);

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
				Log.out(MessageFormat.format(I18nHelper.getLogString("download.started.0"), this.url));
				Log.out("connection handler present: " + this.handler + ", poll size: " + this.handler.getConnections().size());
				Log.out(MessageFormat.format(I18nHelper.getLogString("local.file.0.1"), this.localPath, this.file));
				this.handler.addConnection(this.file, this);
			} else {
				Log.debug(MessageFormat.format(I18nHelper.getLogString("transfer.already.in.progress.01"), this.file));
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
			Log.debug(MessageFormat.format(I18nHelper.getLogString("download.failed.0"), ex));

			File f = new File(this.localPath + this.file);
			f.delete();
			this.fireProgressUpdate(this.file, DataConnection.FAILED, -1);

			ex.printStackTrace();

			return;
		}

		if (!this.work) {
			File f = new File(this.localPath + this.file);
			f.delete();
			Log.out(MessageFormat.format(I18nHelper.getLogString("download.aborted.0"), this.file));
		}
	}

	private void fireProgressUpdate(String file, String type, int bytes) {
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
