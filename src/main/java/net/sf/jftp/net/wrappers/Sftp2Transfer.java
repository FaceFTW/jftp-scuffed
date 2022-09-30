package net.sf.jftp.net.wrappers;

import net.sf.jftp.net.Transfer;

import java.util.Vector;

public class Sftp2Transfer implements Runnable {
	private final String host;
	private final String localPath;
	private final String remotePath;
	private final String file;
	private final String user;
	private final String pass;
	private final String type;
	private final Vector listeners;
	private final String keyfile;
	private final String port;
	public Thread runner;
	private Sftp2Connection con = null;

	public Sftp2Transfer(final String localPath, final String remotePath, final String file, final String user, final String pass, final Vector listeners, final String type, final String keyfile, final String host, final String port) {
		this.localPath = localPath;
		this.remotePath = remotePath;
		this.file = file;
		this.user = user;
		this.pass = pass;
		this.type = type;
		this.listeners = listeners;
		this.keyfile = keyfile;
		this.host = host;
		this.port = port;

		this.prepare();
	}

	public void prepare() {
		runner = new Thread(this);
		runner.setPriority(Thread.MIN_PRIORITY);
		runner.start();
	}

	public void run() {
		con = new Sftp2Connection(host, port, keyfile);
		con.setConnectionListeners(listeners);
		con.login(user, pass);
		con.setLocalPath(localPath);
		con.chdir(remotePath);

		if (type.equals(Transfer.DOWNLOAD)) {
			con.download(file);
		} else if (type.equals(Transfer.UPLOAD)) {
			con.upload(file);
		}
	}

	public Sftp2Connection getSftpConnection() {
		return con;
	}
}
