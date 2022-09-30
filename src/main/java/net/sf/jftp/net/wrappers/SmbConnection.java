/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.sf.jftp.net.wrappers;

import jcifs.smb.NtlmAuthenticator;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;
import net.sf.jftp.net.BasicConnection;
import net.sf.jftp.net.ConnectionListener;
import net.sf.jftp.net.DataConnection;
import net.sf.jftp.net.FtpConnection;
import net.sf.jftp.net.Transfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;
import java.util.Date;
import java.util.Vector;


public class SmbConnection extends NtlmAuthenticator implements BasicConnection {
	public static final int smbBuffer = 128000;
	private String path = "";
	private String pwd = "smb://";
	private Vector listeners = new Vector();
	private String[] size = new String[0];
	private int[] perms = null;
	private String user;
	private String pass;
	private String host;
	private String domain = null;
	private String baseFile;
	private int fileCount;
	private boolean isDirUpload = false;
	private boolean shortProgress = false;
	private boolean dummy = false;
	private boolean connected = false;

	public SmbConnection() {
	}

	public SmbConnection(final String url, String domain, final String user, final String pass, final ConnectionListener l) {
		NtlmAuthenticator.setDefault(this);

		if (l != null) {
			listeners.add(l);
		}

		this.user = user;
		this.pass = pass;
		//TODO set hostname from smb url
		//this.host = host;

		//***
		if (domain.equals("NONE") || domain.isEmpty()) {
			domain = null;
		}

		this.domain = domain;

		//if(url == null) chdir(getPWD());
		if (url.equals("(LAN)")) {
			connected = this.chdir(this.getPWD());
		} else {
			connected = this.chdir(url);
		}

		//***
	}

	protected NtlmPasswordAuthentication getNtlmPasswordAuthentication() {
    	/*
        Log.debug("Auth: " + domain+ ";" + user + ":" + pass);
        if(getRequestingException() != null) {
        	Log.debug( (getRequestingException() != null ? getRequestingException().getMessage() : "") + " for " + getRequestingURL() );
        }
        */
		try {
			return new NtlmPasswordAuthentication(domain, user, pass);
		} catch (final Exception ex) {
			net.sf.jftp.system.logging.Log.debug("Error logging in: " + ex);
			ex.printStackTrace();

			return null;
		}

	}

	private NtlmPasswordAuthentication getAuth() {
		return this.getNtlmPasswordAuthentication();
	}

	public int removeFileOrDir(String file) {
		file = this.toSMB(file);

		//System.out.println(file);
		try {
			final SmbFile f = new SmbFile(file, this.getAuth());

			if (f.exists() && f.isDirectory()) {
				this.cleanSmbDir(file);
			}

			f.delete();
		} catch (final Exception ex) {
			net.sf.jftp.system.logging.Log.debug("Removal failed (" + ex + ").");

			return -1;
		}

		return 1;
	}

	private void cleanSmbDir(String dir) throws Exception {
		dir = this.toSMB(dir);

		final SmbFile f2 = new SmbFile(dir, this.getAuth());
		final String[] tmp = f2.list();

		if (tmp == null) {
			return;
		}

		for (final String s : tmp) {
			final jcifs.smb.SmbFile f3 = new jcifs.smb.SmbFile(dir + s, this.getAuth());

			if (f3.isDirectory()) {
				//System.out.println(dir);
				this.cleanSmbDir(dir + s);
				f3.delete();
			} else {
				//System.out.println(dir+tmp[i]);
				f3.delete();
			}
		}
	}

	public void sendRawCommand(final String cmd) {
	}

	public void disconnect() {
	}

	public boolean isConnected() {
		return connected;
	}

	public String getPWD() {
		//Log.debug("PWD: " + pwd);
		return this.toSMB(pwd);
	}

	public boolean cdup() {
		String tmp = pwd;

		if (pwd.endsWith("/") && !pwd.equals("smb://")) {
			tmp = pwd.substring(0, pwd.lastIndexOf("/"));
		}

		return this.chdir(tmp.substring(0, tmp.lastIndexOf("/") + 1));
	}

	public boolean mkdir(String dirName) {
		try {
			if (!dirName.endsWith("/")) {
				dirName = dirName + "/";
			}

			dirName = this.toSMB(dirName);

			final SmbFile f = new SmbFile(dirName, this.getAuth());
			f.mkdir();

			this.fireDirectoryUpdate();

			return true;
		} catch (final Exception ex) {
			net.sf.jftp.system.logging.Log.debug("Failed to create directory (" + ex + ").");

			return false;
		}
	}

	public void list() throws IOException {
	}

	public boolean chdir(final String p) {
		return this.chdir(p, true);
	}

	public boolean chdir(final String p, final boolean refresh) {
		try {
			if (p.endsWith("..")) {
				return this.cdup();
			}

			String tmp = this.toSMB(p);

			if (!tmp.endsWith("/")) {
				tmp = tmp + "/";
			}

			//Log.debug("tmp: " + tmp);
			final SmbFile f = new SmbFile(tmp, this.getAuth());
			f.list();

			pwd = tmp;

			//Log.debug("pwd: " + pwd);
			//System.out.println("chdir: " + getPWD());
			if (refresh) {
				this.fireDirectoryUpdate();
			}

			//System.out.println("chdir2: " + getPWD());
			dummy = false;

			return true;
		} catch (final Exception ex) {
			if (ex.getMessage() != null && (ex.getMessage().indexOf("MSBROWSE") > 0) && !dummy) // MSBROWSE is not in the message (anymore)
			{
				net.sf.jftp.system.logging.Log.debug("\nCould not find a master server.");
				net.sf.jftp.system.logging.Log.debug("Please make sure you have the local IP set to the interface you want to use, netbios enabled, and if");
				net.sf.jftp.system.logging.Log.debug("that does not work try \"<default>\"...");
				net.sf.jftp.system.logging.Log.debug("If you still can not find a master make sure that there is one your LAN and submit a bug report.");
				dummy = true;
			} else if (ex.toString().contains("MSBROWSE")) {
				net.sf.jftp.system.logging.Log.debug("\nCould not find a master server.");
				net.sf.jftp.system.logging.Log.debug("Please make sure you have the local IP set to the interface you want to use, netbios enabled");
			} else {
				ex.printStackTrace();
				net.sf.jftp.system.logging.Log.debug("Could not change directory (" + ex + ").");
			}

			return false;
		}
	}

	public boolean chdirNoRefresh(final String p) {
		return this.chdir(p, false);
	}

	public String getLocalPath() {
		return path;
	}

	public boolean setLocalPath(String p) {
		if (net.sf.jftp.system.StringUtils.isRelative(p)) {
			p = path + p;
		}

		p = p.replace('\\', '/');

		//System.out.println(", local 2:" + p);
		final File f = new File(p);

		if (f.exists()) {
			try {
				path = f.getCanonicalPath();
				path = path.replace('\\', '/');

				if (!path.endsWith("/")) {
					path = path + "/";
				}

				//System.out.println("localPath: "+path);
			} catch (final IOException ex) {
				net.sf.jftp.system.logging.Log.debug("Error: can not get pathname (local)!");

				return false;
			}
		} else {
			net.sf.jftp.system.logging.Log.debug("(local) No such path: \"" + p + "\"");

			return false;
		}

		return true;
	}

	public String[] sortLs() {
		try {
			//Log.debug("sortLs: trying");
			this.chdirNoRefresh(this.getPWD());

			final SmbFile fx = new SmbFile(pwd, this.getAuth());

			//System.out.println(pwd);
			//if(fx == null) System.out.println("Smb: fx null");
			final SmbFile[] f = fx.listFiles();

			//Log.debug("sortLs: file is there and listed");
			//if(f == null) System.out.println("Smb: f null");
			final String[] files = new String[f.length];
			size = new String[f.length];
			perms = new int[f.length];

			int i;

			for (i = 0; i < f.length; i++) {
				files[i] = f[i].toURL().toString();

				int x = 0;

				for (int j = 0; j < files[i].length(); j++) {
					if (files[i].charAt(j) == '/') {
						x++;
					}
				}

				if (files[i].endsWith("/") && (x > 3)) {
					files[i] = net.sf.jftp.system.StringUtils.getDir(files[i]);
				} else if (x > 3) {
					files[i] = net.sf.jftp.system.StringUtils.getFile(files[i]);
				}

				size[i] = "" + f[i].length();

				if (f[i].canRead()) {
					perms[i] = FtpConnection.R;
				} else if (f[i].canWrite()) {
					perms[i] = FtpConnection.R;
				} else {
					perms[i] = FtpConnection.DENIED;
				}
			}

			//Log.debug("sortLs: finished, ok");
			return files;
		} catch (final Exception ex) {
			if (ex instanceof SmbAuthException) {
				net.sf.jftp.system.logging.Log.debug("Access denied: " + ex);

				//Log.debug("URL: " + getPWD());
				//Log.debug("Auth (bin): " + getAuth().toString());
				//Log.debug("Auth: " + domain+ ";" + user + ":" + pass);
			} else {
				net.sf.jftp.system.logging.Log.debug("Error: " + ex);
			}

			return new String[0];
		}
	}

	public String[] sortSize() {
		return size;
	}

	public int[] getPermissions() {
		return perms;
	}

	public int handleUpload(final String f) {
		if (net.sf.jftp.config.Settings.getEnableSmbMultiThreading()) {
			final SmbTransfer t = new SmbTransfer(this.getPWD(), path, f, user, pass, domain, listeners, Transfer.UPLOAD);
		} else {
			this.upload(f);
		}

		return 0;
	}

	public int handleDownload(final String f) {
		if (net.sf.jftp.config.Settings.getEnableSmbMultiThreading()) {
			final SmbTransfer t = new SmbTransfer(this.getPWD(), path, f, user, pass, domain, listeners, Transfer.DOWNLOAD);
		} else {
			this.download(f);
		}

		return 0;
	}

	public int upload(final String f) {
		final String file = this.toSMB(f);

		if (file.endsWith("/")) {
			final String out = net.sf.jftp.system.StringUtils.getDir(file);
			this.uploadDir(file, path + out);
			this.fireActionFinished(this);
		} else {
			final String outfile = net.sf.jftp.system.StringUtils.getFile(file);

			//System.out.println("transfer: " + file + ", " + getLocalPath() + outfile);
			this.work(path + outfile, file);
			this.fireActionFinished(this);
		}

		return 0;
	}

	public int download(final String f) {
		final String file = this.toSMB(f);

		if (file.endsWith("/")) {
			final String out = net.sf.jftp.system.StringUtils.getDir(file);
			this.downloadDir(file, path + out);
			this.fireActionFinished(this);
		} else {
			final String outfile = net.sf.jftp.system.StringUtils.getFile(file);

			//System.out.println("transfer: " + file + ", " + getLocalPath() + outfile);
			this.work(file, path + outfile);
			this.fireActionFinished(this);
		}

		return 0;
	}

	private void downloadDir(final String dir, final String out) {
		try {
			//System.out.println("downloadDir: " + dir + "," + out);
			fileCount = 0;
			shortProgress = true;
			baseFile = net.sf.jftp.system.StringUtils.getDir(dir);

			final SmbFile f2 = new SmbFile(dir, this.getAuth());
			final String[] tmp = f2.list();

			if (tmp == null) {
				return;
			}

			final File fx = new File(out);
			fx.mkdir();

			for (int i = 0; i < tmp.length; i++) {
				tmp[i] = tmp[i].replace('\\', '/');

				//System.out.println("1: " + dir+tmp[i] + ", " + out +tmp[i]);
				final SmbFile f3 = new SmbFile(dir + tmp[i], this.getAuth());

				if (f3.isDirectory()) {
					if (!tmp[i].endsWith("/")) {
						tmp[i] = tmp[i] + "/";
					}

					this.downloadDir(dir + tmp[i], out + tmp[i]);
				} else {
					fileCount++;
					this.fireProgressUpdate(baseFile, DataConnection.GETDIR + ":" + fileCount, -1);
					this.work(dir + tmp[i], out + tmp[i]);
				}
			}

			this.fireProgressUpdate(baseFile, DataConnection.DFINISHED + ":" + fileCount, -1);
		} catch (final Exception ex) {
			ex.printStackTrace();

			//System.out.println(dir + ", " + out);
			net.sf.jftp.system.logging.Log.debug("Transfer error: " + ex);
			this.fireProgressUpdate(baseFile, DataConnection.FAILED + ":" + fileCount, -1);
		}

		shortProgress = false;
	}

	private void uploadDir(final String dir, final String out) {
		try {
			//System.out.println("uploadDir: " + dir + "," + out);
			isDirUpload = true;
			fileCount = 0;
			shortProgress = true;
			baseFile = net.sf.jftp.system.StringUtils.getDir(dir);

			final File f2 = new File(out);
			final String[] tmp = f2.list();

			if (tmp == null) {
				return;
			}

			final SmbFile fx = new SmbFile(dir, this.getAuth());
			fx.mkdir();

			for (int i = 0; i < tmp.length; i++) {
				tmp[i] = tmp[i].replace('\\', '/');

				//System.out.println("1: " + dir+tmp[i] + ", " + out +tmp[i]);
				final File f3 = new File(out + tmp[i]);

				if (f3.isDirectory()) {
					if (!tmp[i].endsWith("/")) {
						tmp[i] = tmp[i] + "/";
					}

					this.uploadDir(dir + tmp[i], out + tmp[i]);
				} else {
					fileCount++;
					this.fireProgressUpdate(baseFile, DataConnection.PUTDIR + ":" + fileCount, -1);
					this.work(out + tmp[i], dir + tmp[i]);
				}
			}

			this.fireProgressUpdate(baseFile, DataConnection.DFINISHED + ":" + fileCount, -1);
		} catch (final Exception ex) {
			ex.printStackTrace();

			//System.out.println(dir + ", " + out);
			net.sf.jftp.system.logging.Log.debug("Transfer error: " + ex);
			this.fireProgressUpdate(baseFile, DataConnection.FAILED + ":" + fileCount, -1);
		}

		isDirUpload = false;
		shortProgress = true;
	}

	private String toSMB(String f) {
		String file;

		if (f.lastIndexOf("smb://") > 0) {
			f = f.substring(f.lastIndexOf("smb://"));
		}

		if (f.startsWith("smb://")) {
			file = f;
		} else {
			file = this.getPWD() + f;
		}

		file = file.replace('\\', '/');

		//System.out.println("file: "+file);
		return file;
	}

	private void work(final String file, final String outfile) {
		BufferedOutputStream out = null;
		BufferedInputStream in = null;

		try {
			boolean outflag = false;

			if (outfile.startsWith("smb://")) {
				outflag = true;
				out = new BufferedOutputStream(new SmbFileOutputStream(new SmbFile(outfile, this.getAuth())));
			} else {
				out = new BufferedOutputStream(new FileOutputStream(outfile));
			}

			//System.out.println("out: " + outfile + ", in: " + file);
			if (file.startsWith("smb://")) {
				in = new BufferedInputStream(new SmbFile(file, this.getAuth()).getInputStream());
			} else {
				in = new BufferedInputStream(new FileInputStream(file));
			}

			final byte[] buf = new byte[smbBuffer];
			int len = 0;
			int reallen = 0;

			//System.out.println(file+":"+getLocalPath()+outfile);
			while (true) {
				len = in.read(buf);

				//System.out.print(".");
				if (len == StreamTokenizer.TT_EOF) {
					break;
				}

				out.write(buf, 0, len);
				reallen += len;

				//System.out.println(file + ":" + StringUtils.getFile(file));
				if (outflag) {
					this.fireProgressUpdate(net.sf.jftp.system.StringUtils.getFile(outfile), DataConnection.PUT, reallen);
				} else {
					this.fireProgressUpdate(net.sf.jftp.system.StringUtils.getFile(file), DataConnection.GET, reallen);
				}
			}

			this.fireProgressUpdate(file, DataConnection.FINISHED, -1);
		} catch (final IOException ex) {
			net.sf.jftp.system.logging.Log.debug("Error with file IO (" + ex + ")!");
			this.fireProgressUpdate(file, DataConnection.FAILED, -1);
		} finally {
			try {
				out.flush();
				out.close();
				in.close();
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void update(final String file, final String type, final int bytes) {
		if (listeners == null) {
		} else {
			for (int i = 0; i < listeners.size(); i++) {
				final ConnectionListener listener = (ConnectionListener) listeners.elementAt(i);
				listener.updateProgress(file, type, bytes);
			}
		}
	}

	public void addConnectionListener(final ConnectionListener l) {
		listeners.add(l);
	}

	public void setConnectionListeners(final Vector l) {
		listeners = l;
	}

	/**
	 * remote directory has changed
	 */
	public void fireDirectoryUpdate() {
		if (listeners == null) {
		} else {
			for (int i = 0; i < listeners.size(); i++) {
				((ConnectionListener) listeners.elementAt(i)).updateRemoteDirectory(this);
			}
		}
	}

	public boolean login(final String user, final String pass) {
		return true;
	}

	/**
	 * progress update
	 */
	public void fireProgressUpdate(final String file, final String type, final int bytes) {
		//System.out.println(listener);
		if (listeners == null) {
		} else {
			for (int i = 0; i < listeners.size(); i++) {
				final ConnectionListener listener = (ConnectionListener) listeners.elementAt(i);

				if (shortProgress && net.sf.jftp.config.Settings.shortProgress) {
					if (type.startsWith(DataConnection.DFINISHED)) {
						listener.updateProgress(baseFile, DataConnection.DFINISHED + ":" + fileCount, bytes);
					} else if (isDirUpload) {
						listener.updateProgress(baseFile, DataConnection.PUTDIR + ":" + fileCount, bytes);
					} else {
						listener.updateProgress(baseFile, DataConnection.GETDIR + ":" + fileCount, bytes);
					}
				} else {
					listener.updateProgress(file, type, bytes);
				}
			}
		}
	}

	public void fireActionFinished(final SmbConnection con) {
		if (listeners == null) {
		} else {
			for (int i = 0; i < listeners.size(); i++) {
				((ConnectionListener) listeners.elementAt(i)).actionFinished(con);
			}
		}
	}

	public int upload(String file, final InputStream i) {
		BufferedOutputStream out = null;
		BufferedInputStream in = null;

		try {
			file = this.toSMB(file);

			out = new BufferedOutputStream(new SmbFileOutputStream(new SmbFile(file, this.getAuth())));
			in = new BufferedInputStream(i);

			final byte[] buf = new byte[smbBuffer];
			int len = 0;
			int reallen = 0;

			while (true) {
				len = in.read(buf);

				if (len == StreamTokenizer.TT_EOF) {
					break;
				}

				out.write(buf, 0, len);
				reallen += len;

				this.fireProgressUpdate(net.sf.jftp.system.StringUtils.getFile(file), DataConnection.PUT, reallen);
			}

			this.fireProgressUpdate(file, DataConnection.FINISHED, -1);

			return 0;
		} catch (final IOException ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug("Error with file IO (" + ex + ")!");
			this.fireProgressUpdate(file, DataConnection.FAILED, -1);
		} finally {
			try {
				out.flush();
				out.close();
				in.close();
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		}

		return -1;
	}

	public InputStream getDownloadInputStream(String file) {
		file = this.toSMB(file);

		try {
			return new BufferedInputStream(new SmbFile(file, this.getAuth()).getInputStream());
		} catch (final Exception ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug(ex + " @SmbConnection::getDownloadInputStream");

			return null;
		}
	}

	public Date[] sortDates() {
		return null;
	}

	public boolean rename(String file, String to) {
		try {
			file = this.toSMB(file);
			to = this.toSMB(to);

			final SmbFile src = new SmbFile(file, this.getAuth());

			src.renameTo(new SmbFile(to, this.getAuth()));

			return true;
		} catch (final Exception ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug(ex + " @SmbConnection::rename");

			return false;
		}
	}
}
