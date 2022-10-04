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
	private java.util.List<net.sf.jftp.net.ConnectionListener> listeners = new java.util.ArrayList<>();
	private String[] size = new String[0];
	private int[] perms = null;
	private String user = null;
	private String pass = null;
	private String host = null;
	private String domain = null;
	private String baseFile = null;
	private int fileCount = 0;
	private boolean isDirUpload = false;
	private boolean shortProgress = false;
	private boolean dummy = false;
	private boolean connected = false;

	public SmbConnection() {
		super();
	}

	public SmbConnection(String url, String domain, String user, String pass, ConnectionListener l) {
		super();
		NtlmAuthenticator.setDefault(this);

		if (null != l) {
			this.listeners.add(l);
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
			this.connected = this.chdir(this.getPWD());
		} else {
			this.connected = this.chdir(url);
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
			return new NtlmPasswordAuthentication(this.domain, this.user, this.pass);
		} catch (Exception ex) {
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
			SmbFile f = new SmbFile(file, this.getAuth());

			if (f.exists() && f.isDirectory()) {
				this.cleanSmbDir(file);
			}

			f.delete();
		} catch (Exception ex) {
			net.sf.jftp.system.logging.Log.debug("Removal failed (" + ex + ").");

			return -1;
		}

		return 1;
	}

	private void cleanSmbDir(String dir) throws Exception {
		dir = this.toSMB(dir);

		SmbFile f2 = new SmbFile(dir, this.getAuth());
		String[] tmp = f2.list();

		if (null == tmp) {
			return;
		}

		for (String s : tmp) {
			jcifs.smb.SmbFile f3 = new jcifs.smb.SmbFile(dir + s, this.getAuth());

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

	public void sendRawCommand(String cmd) {
	}

	public void disconnect() {
	}

	public boolean isConnected() {
		return this.connected;
	}

	public String getPWD() {
		//Log.debug("PWD: " + pwd);
		return this.toSMB(this.pwd);
	}

	public boolean cdup() {
		String tmp = this.pwd;

		if (this.pwd.endsWith("/") && !this.pwd.equals("smb://")) {
			tmp = this.pwd.substring(0, this.pwd.lastIndexOf('/'));
		}

		return this.chdir(tmp.substring(0, tmp.lastIndexOf('/') + 1));
	}

	public boolean mkdir(String dirName) {
		try {
			if (!dirName.endsWith("/")) {
				dirName = dirName + "/";
			}

			dirName = this.toSMB(dirName);

			SmbFile f = new SmbFile(dirName, this.getAuth());
			f.mkdir();

			this.fireDirectoryUpdate();

			return true;
		} catch (Exception ex) {
			net.sf.jftp.system.logging.Log.debug("Failed to create directory (" + ex + ").");

			return false;
		}
	}

	public void list() throws IOException {
	}

	public boolean chdir(String p) {
		return this.chdir(p, true);
	}

	public boolean chdir(String p, boolean refresh) {
		try {
			if (p.endsWith("..")) {
				return this.cdup();
			}

			String tmp = this.toSMB(p);

			if (!tmp.endsWith("/")) {
				tmp = tmp + "/";
			}

			//Log.debug("tmp: " + tmp);
			SmbFile f = new SmbFile(tmp, this.getAuth());
			f.list();

			this.pwd = tmp;

			//Log.debug("pwd: " + pwd);
			//System.out.println("chdir: " + getPWD());
			if (refresh) {
				this.fireDirectoryUpdate();
			}

			//System.out.println("chdir2: " + getPWD());
			this.dummy = false;

			return true;
		} catch (Exception ex) {
			if (null != ex.getMessage() && (0 < ex.getMessage().indexOf("MSBROWSE")) && !this.dummy) // MSBROWSE is not in the message (anymore)
			{
				net.sf.jftp.system.logging.Log.debug("\nCould not find a master server.");
				net.sf.jftp.system.logging.Log.debug("Please make sure you have the local IP set to the interface you want to use, netbios enabled, and if");
				net.sf.jftp.system.logging.Log.debug("that does not work try \"<default>\"...");
				net.sf.jftp.system.logging.Log.debug("If you still can not find a master make sure that there is one your LAN and submit a bug report.");
				this.dummy = true;
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

	public boolean chdirNoRefresh(String p) {
		return this.chdir(p, false);
	}

	public String getLocalPath() {
		return this.path;
	}

	public boolean setLocalPath(String p) {
		if (net.sf.jftp.system.StringUtils.isRelative(p)) {
			p = this.path + p;
		}

		p = p.replace('\\', '/');

		//System.out.println(", local 2:" + p);
		File f = new File(p);

		if (f.exists()) {
			try {
				this.path = f.getCanonicalPath();
				this.path = this.path.replace('\\', '/');

				if (!this.path.endsWith("/")) {
					this.path = this.path + "/";
				}

				//System.out.println("localPath: "+path);
			} catch (IOException ex) {
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

			SmbFile fx = new SmbFile(this.pwd, this.getAuth());

			//System.out.println(pwd);
			//if(fx == null) System.out.println("Smb: fx null");
			SmbFile[] f = fx.listFiles();

			//Log.debug("sortLs: file is there and listed");
			//if(f == null) System.out.println("Smb: f null");
			String[] files = new String[f.length];
			this.size = new String[f.length];
			this.perms = new int[f.length];

			int i;

			for (i = 0; i < f.length; i++) {
				files[i] = f[i].toURL().toString();

				int x = 0;

				for (int j = 0; j < files[i].length(); j++) {
					if ('/' == files[i].charAt(j)) {
						x++;
					}
				}

				if (files[i].endsWith("/") && (3 < x)) {
					files[i] = net.sf.jftp.system.StringUtils.getDir(files[i]);
				} else if (3 < x) {
					files[i] = net.sf.jftp.system.StringUtils.getFile(files[i]);
				}

				this.size[i] = "" + f[i].length();

				if (f[i].canRead()) {
					this.perms[i] = net.sf.jftp.net.FtpConstants.R;
				} else if (f[i].canWrite()) {
					this.perms[i] = net.sf.jftp.net.FtpConstants.R;
				} else {
					this.perms[i] = net.sf.jftp.net.FtpConstants.DENIED;
				}
			}

			//Log.debug("sortLs: finished, ok");
			return files;
		} catch (Exception ex) {
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
		return this.size;
	}

	public int[] getPermissions() {
		return this.perms;
	}

	public int handleUpload(String f) {
		if (net.sf.jftp.config.Settings.getEnableSmbMultiThreading()) {
			SmbTransfer t = new SmbTransfer(this.getPWD(), this.path, f, this.user, this.pass, this.domain, this.listeners, Transfer.UPLOAD);
		} else {
			this.upload(f);
		}

		return 0;
	}

	public int handleDownload(String f) {
		if (net.sf.jftp.config.Settings.getEnableSmbMultiThreading()) {
			SmbTransfer t = new SmbTransfer(this.getPWD(), this.path, f, this.user, this.pass, this.domain, this.listeners, Transfer.DOWNLOAD);
		} else {
			this.download(f);
		}

		return 0;
	}

	public int upload(String f) {
		String file = this.toSMB(f);

		if (file.endsWith("/")) {
			String out = net.sf.jftp.system.StringUtils.getDir(file);
			this.uploadDir(file, this.path + out);
			this.fireActionFinished(this);
		} else {
			String outfile = net.sf.jftp.system.StringUtils.getFile(file);

			//System.out.println("transfer: " + file + ", " + getLocalPath() + outfile);
			this.work(this.path + outfile, file);
			this.fireActionFinished(this);
		}

		return 0;
	}

	public int download(String f) {
		String file = this.toSMB(f);

		if (file.endsWith("/")) {
			String out = net.sf.jftp.system.StringUtils.getDir(file);
			this.downloadDir(file, this.path + out);
			this.fireActionFinished(this);
		} else {
			String outfile = net.sf.jftp.system.StringUtils.getFile(file);

			//System.out.println("transfer: " + file + ", " + getLocalPath() + outfile);
			this.work(file, this.path + outfile);
			this.fireActionFinished(this);
		}

		return 0;
	}

	private void downloadDir(String dir, String out) {
		try {
			//System.out.println("downloadDir: " + dir + "," + out);
			this.fileCount = 0;
			this.shortProgress = true;
			this.baseFile = net.sf.jftp.system.StringUtils.getDir(dir);

			SmbFile f2 = new SmbFile(dir, this.getAuth());
			String[] tmp = f2.list();

			if (null == tmp) {
				return;
			}

			File fx = new File(out);
			fx.mkdir();

			for (int i = 0; i < tmp.length; i++) {
				tmp[i] = tmp[i].replace('\\', '/');

				//System.out.println("1: " + dir+tmp[i] + ", " + out +tmp[i]);
				SmbFile f3 = new SmbFile(dir + tmp[i], this.getAuth());

				if (f3.isDirectory()) {
					if (!tmp[i].endsWith("/")) {
						tmp[i] = tmp[i] + "/";
					}

					this.downloadDir(dir + tmp[i], out + tmp[i]);
				} else {
					this.fileCount++;
					this.fireProgressUpdate(this.baseFile, DataConnection.GETDIR + ":" + this.fileCount, -1);
					this.work(dir + tmp[i], out + tmp[i]);
				}
			}

			this.fireProgressUpdate(this.baseFile, DataConnection.DFINISHED + ":" + this.fileCount, -1);
		} catch (Exception ex) {
			ex.printStackTrace();

			//System.out.println(dir + ", " + out);
			net.sf.jftp.system.logging.Log.debug("Transfer error: " + ex);
			this.fireProgressUpdate(this.baseFile, DataConnection.FAILED + ":" + this.fileCount, -1);
		}

		this.shortProgress = false;
	}

	private void uploadDir(String dir, String out) {
		try {
			//System.out.println("uploadDir: " + dir + "," + out);
			this.isDirUpload = true;
			this.fileCount = 0;
			this.shortProgress = true;
			this.baseFile = net.sf.jftp.system.StringUtils.getDir(dir);

			File f2 = new File(out);
			String[] tmp = f2.list();

			if (null == tmp) {
				return;
			}

			SmbFile fx = new SmbFile(dir, this.getAuth());
			fx.mkdir();

			for (int i = 0; i < tmp.length; i++) {
				tmp[i] = tmp[i].replace('\\', '/');

				//System.out.println("1: " + dir+tmp[i] + ", " + out +tmp[i]);
				File f3 = new File(out + tmp[i]);

				if (f3.isDirectory()) {
					if (!tmp[i].endsWith("/")) {
						tmp[i] = tmp[i] + "/";
					}

					this.uploadDir(dir + tmp[i], out + tmp[i]);
				} else {
					this.fileCount++;
					this.fireProgressUpdate(this.baseFile, DataConnection.PUTDIR + ":" + this.fileCount, -1);
					this.work(out + tmp[i], dir + tmp[i]);
				}
			}

			this.fireProgressUpdate(this.baseFile, DataConnection.DFINISHED + ":" + this.fileCount, -1);
		} catch (Exception ex) {
			ex.printStackTrace();

			//System.out.println(dir + ", " + out);
			net.sf.jftp.system.logging.Log.debug("Transfer error: " + ex);
			this.fireProgressUpdate(this.baseFile, DataConnection.FAILED + ":" + this.fileCount, -1);
		}

		this.isDirUpload = false;
		this.shortProgress = true;
	}

	private String toSMB(String f) {
		String file;

		if (0 < f.lastIndexOf("smb://")) {
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

	private void work(String file, String outfile) {
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

			byte[] buf = new byte[smbBuffer];
			int len = 0;
			int reallen = 0;

			//System.out.println(file+":"+getLocalPath()+outfile);
			while (true) {
				len = in.read(buf);

				//System.out.print(".");
				if (java.io.StreamTokenizer.TT_EOF == len) {
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
		} catch (IOException ex) {
			net.sf.jftp.system.logging.Log.debug("Error with file IO (" + ex + ")!");
			this.fireProgressUpdate(file, DataConnection.FAILED, -1);
		} finally {
			try {
				out.flush();
				out.close();
				in.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void update(String file, String type, int bytes) {
		if (null == this.listeners) {
		} else {
			for (int i = 0; i < this.listeners.size(); i++) {
				ConnectionListener listener = (ConnectionListener) this.listeners.get(i);
				listener.updateProgress(file, type, bytes);
			}
		}
	}

	public void addConnectionListener(ConnectionListener l) {
		this.listeners.add(l);
	}

	public void setConnectionListeners(java.util.List<ConnectionListener> l) {
		this.listeners = l;
	}

	/**
	 * remote directory has changed
	 */
	public void fireDirectoryUpdate() {
		if (null == this.listeners) {
		} else {
			for (int i = 0; i < this.listeners.size(); i++) {
				((ConnectionListener) this.listeners.get(i)).updateRemoteDirectory(this);
			}
		}
	}

	public boolean login(String user, String pass) {
		return true;
	}

	/**
	 * progress update
	 */
	public void fireProgressUpdate(String file, String type, int bytes) {
		//System.out.println(listener);
		if (null == this.listeners) {
		} else {
			for (int i = 0; i < this.listeners.size(); i++) {
				ConnectionListener listener = (ConnectionListener) this.listeners.get(i);

				if (this.shortProgress && net.sf.jftp.config.Settings.shortProgress) {
					if (type.startsWith(DataConnection.DFINISHED)) {
						listener.updateProgress(this.baseFile, DataConnection.DFINISHED + ":" + this.fileCount, bytes);
					} else if (this.isDirUpload) {
						listener.updateProgress(this.baseFile, DataConnection.PUTDIR + ":" + this.fileCount, bytes);
					} else {
						listener.updateProgress(this.baseFile, DataConnection.GETDIR + ":" + this.fileCount, bytes);
					}
				} else {
					listener.updateProgress(file, type, bytes);
				}
			}
		}
	}

	public void fireActionFinished(SmbConnection con) {
		if (null == this.listeners) {
		} else {
			for (int i = 0; i < this.listeners.size(); i++) {
				((ConnectionListener) this.listeners.get(i)).actionFinished(con);
			}
		}
	}

	public int upload(String file, InputStream i) {
		BufferedOutputStream out = null;
		BufferedInputStream in = null;

		try {
			file = this.toSMB(file);

			out = new BufferedOutputStream(new SmbFileOutputStream(new SmbFile(file, this.getAuth())));
			in = new BufferedInputStream(i);

			byte[] buf = new byte[smbBuffer];
			int len = 0;
			int reallen = 0;

			while (true) {
				len = in.read(buf);

				if (java.io.StreamTokenizer.TT_EOF == len) {
					break;
				}

				out.write(buf, 0, len);
				reallen += len;

				this.fireProgressUpdate(net.sf.jftp.system.StringUtils.getFile(file), DataConnection.PUT, reallen);
			}

			this.fireProgressUpdate(file, DataConnection.FINISHED, -1);

			return 0;
		} catch (IOException ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug("Error with file IO (" + ex + ")!");
			this.fireProgressUpdate(file, DataConnection.FAILED, -1);
		} finally {
			try {
				out.flush();
				out.close();
				in.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return -1;
	}

	public InputStream getDownloadInputStream(String file) {
		file = this.toSMB(file);

		try {
			return new BufferedInputStream(new SmbFile(file, this.getAuth()).getInputStream());
		} catch (Exception ex) {
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

			SmbFile src = new SmbFile(file, this.getAuth());

			src.renameTo(new SmbFile(to, this.getAuth()));

			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug(ex + " @SmbConnection::rename");

			return false;
		}
	}
}
