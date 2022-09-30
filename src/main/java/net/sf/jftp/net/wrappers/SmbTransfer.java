package net.sf.jftp.net.wrappers;

import net.sf.jftp.net.Transfer;

import java.util.Vector;


public class SmbTransfer implements Runnable {
	private final String url;
	private final String domain;
	private final String localPath;
	private final String file;
	private final String user;
	private final String pass;
	private final String type;
	private final Vector listeners;
	public Thread runner;
	private SmbConnection con = null;

	public SmbTransfer(final String url, final String localPath, final String file, final String user, final String pass, final String domain, final Vector listeners, final String type) {
		this.url = url;
		this.localPath = localPath;
		this.file = file;
		this.user = user;
		this.pass = pass;
		this.type = type;
		this.domain = domain;
		this.listeners = listeners;

		this.prepare();
	}

	public void prepare() {
		runner = new Thread(this);
		runner.setPriority(Thread.MIN_PRIORITY);
		runner.start();
	}

	public void run() {
		con = new SmbConnection(url, domain, user, pass, null);
		con.setLocalPath(localPath);
		con.setConnectionListeners(listeners);

		if (type.equals(Transfer.DOWNLOAD)) {
			con.download(file);
		} else if (type.equals(Transfer.UPLOAD)) {
			con.upload(file);
		}
	}

	public SmbConnection getSmbConnection() {
		return con;
	}
}
