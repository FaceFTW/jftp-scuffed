package net.sf.jftp.net;


/**
 * The basic Transfer interface.
 * You probably don't have to use it directly.
 */
public abstract class Transfer {
	public static final String PAUSED = "STOPPED";
	public static final String QUEUED = "QUEUED";
	public static final String REMOVED = "REMOVED";
	public static final String UPLOAD = "UPLOAD";
	public static final String DOWNLOAD = "DOWNLOAD";
	public boolean work = true;
	public boolean pause;

	public abstract void prepare();

	public abstract int getStatus();

	public abstract boolean hasStarted();

	public abstract FtpConnection getFtpConnection();

	public abstract DataConnection getDataConnection();
}
