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
import net.sf.jftp.config.Settings;
import net.sf.jftp.net.BasicConnection;
import net.sf.jftp.net.ConnectionListener;
import net.sf.jftp.net.DataConnection;
import net.sf.jftp.net.FtpConnection;
import net.sf.jftp.net.Transfer;
import net.sf.jftp.system.StringUtils;
import net.sf.jftp.system.logging.Log;

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
	public static int smbBuffer = 128000;
	private String path = "";
	private String pwd = "smb://";
	private Vector listeners = new Vector();
	private String[] files;
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

	public SmbConnection(String url, String domain, String user, String pass, ConnectionListener l) {
		NtlmAuthenticator.setDefault(this);

		if (l != null) {
			listeners.add(l);
		}

		this.user = user;
		this.pass = pass;
		//TODO set hostname from smb url
		//this.host = host;

		//***
		if (domain.equals("NONE") || domain.equals("")) {
			domain = null;
		}

		this.domain = domain;

		//if(url == null) chdir(getPWD());
		if (url.equals("(LAN)")) {
			connected = chdir(getPWD());
		} else {
			connected = chdir(url);
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
		} catch (Exception ex) {
			Log.debug("Error logging in: " + ex);
			ex.printStackTrace();

			return null;
		}

	}

	private NtlmPasswordAuthentication getAuth() {
		return getNtlmPasswordAuthentication();
	}

	public int removeFileOrDir(String file) {
		file = toSMB(file);

		//System.out.println(file);
		try {
			SmbFile f = new SmbFile(file, getAuth());

			if (f.exists() && f.isDirectory()) {
				cleanSmbDir(file);
			}

			f.delete();
		} catch (Exception ex) {
			Log.debug("Removal failed (" + ex + ").");

			return -1;
		}

		return 1;
	}

	private void cleanSmbDir(String dir) throws Exception {
		dir = toSMB(dir);

		SmbFile f2 = new SmbFile(dir, getAuth());
		String[] tmp = f2.list();

		if (tmp == null) {
			return;
		}

		for (int i = 0; i < tmp.length; i++) {
			SmbFile f3 = new SmbFile(dir + tmp[i], getAuth());

			if (f3.isDirectory()) {
				//System.out.println(dir);
				cleanSmbDir(dir + tmp[i]);
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
		return connected;
	}

	public String getPWD() {
		//Log.debug("PWD: " + pwd);
		return toSMB(pwd);
	}

	public boolean cdup() {
		String tmp = pwd;

		if (pwd.endsWith("/") && !pwd.equals("smb://")) {
			tmp = pwd.substring(0, pwd.lastIndexOf("/"));
		}

		return chdir(tmp.substring(0, tmp.lastIndexOf("/") + 1));
	}

	public boolean mkdir(String dirName) {
		try {
			if (!dirName.endsWith("/")) {
				dirName = dirName + "/";
			}

			dirName = toSMB(dirName);

			SmbFile f = new SmbFile(dirName, getAuth());
			f.mkdir();

			fireDirectoryUpdate();

			return true;
		} catch (Exception ex) {
			Log.debug("Failed to create directory (" + ex + ").");

			return false;
		}
	}

	public void list() throws IOException {
	}

	public boolean chdir(String p) {
		return chdir(p, true);
	}

	public boolean chdir(String p, boolean refresh) {
		try {
			if (p.endsWith("..")) {
				return cdup();
			}

			String tmp = toSMB(p);

			if (!tmp.endsWith("/")) {
				tmp = tmp + "/";
			}

			//Log.debug("tmp: " + tmp);
			SmbFile f = new SmbFile(tmp, getAuth());
			f.list();

			pwd = tmp;

			//Log.debug("pwd: " + pwd);
			//System.out.println("chdir: " + getPWD());
			if (refresh) {
				fireDirectoryUpdate();
			}

			//System.out.println("chdir2: " + getPWD());
			dummy = false;

			return true;
		} catch (Exception ex) {
			if (ex.getMessage() != null && (ex.getMessage().indexOf("MSBROWSE") > 0) && !dummy) // MSBROWSE is not in the message (anymore)
			{
				Log.debug("\nCould not find a master server.");
				Log.debug("Please make sure you have the local IP set to the interface you want to use, netbios enabled, and if");
				Log.debug("that does not work try \"<default>\"...");
				Log.debug("If you still can not find a master make sure that there is one your LAN and submit a bug report.");
				dummy = true;
			} else if (ex.toString().contains("MSBROWSE")) {
				Log.debug("\nCould not find a master server.");
				Log.debug("Please make sure you have the local IP set to the interface you want to use, netbios enabled");
			} else {
				ex.printStackTrace();
				Log.debug("Could not change directory (" + ex + ").");
			}

			return false;
		}
	}

	public boolean chdirNoRefresh(String p) {
		return chdir(p, false);
	}

	public String getLocalPath() {
		return path;
	}

	public boolean setLocalPath(String p) {
		if (StringUtils.isRelative(p)) {
			p = path + p;
		}

		p = p.replace('\\', '/');

		//System.out.println(", local 2:" + p);
		File f = new File(p);

		if (f.exists()) {
			try {
				path = f.getCanonicalPath();
				path = path.replace('\\', '/');

				if (!path.endsWith("/")) {
					path = path + "/";
				}

				//System.out.println("localPath: "+path);
			} catch (IOException ex) {
				Log.debug("Error: can not get pathname (local)!");

				return false;
			}
		} else {
			Log.debug("(local) No such path: \"" + p + "\"");

			return false;
		}

		return true;
	}

	public String[] sortLs() {
		try {
			//Log.debug("sortLs: trying");
			chdirNoRefresh(getPWD());

			SmbFile fx = new SmbFile(pwd, getAuth());

			//System.out.println(pwd);
			//if(fx == null) System.out.println("Smb: fx null");
			SmbFile[] f = fx.listFiles();

			//Log.debug("sortLs: file is there and listed");
			//if(f == null) System.out.println("Smb: f null");
			files = new String[f.length];
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
					files[i] = StringUtils.getDir(files[i]);
				} else if (x > 3) {
					files[i] = StringUtils.getFile(files[i]);
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
		} catch (Exception ex) {
			if (ex instanceof SmbAuthException) {
				Log.debug("Access denied: " + ex);

				//Log.debug("URL: " + getPWD());
				//Log.debug("Auth (bin): " + getAuth().toString());
				//Log.debug("Auth: " + domain+ ";" + user + ":" + pass);
			} else {
				Log.debug("Error: " + ex);
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

	public int handleUpload(String f) {
		if (Settings.getEnableSmbMultiThreading()) {
			SmbTransfer t = new SmbTransfer(getPWD(), getLocalPath(), f, user, pass, domain, listeners, Transfer.UPLOAD);
		} else {
			upload(f);
		}

		return 0;
	}

	public int handleDownload(String f) {
		if (Settings.getEnableSmbMultiThreading()) {
			SmbTransfer t = new SmbTransfer(getPWD(), getLocalPath(), f, user, pass, domain, listeners, Transfer.DOWNLOAD);
		} else {
			download(f);
		}

		return 0;
	}

	public int upload(String f) {
		String file = toSMB(f);

		if (file.endsWith("/")) {
			String out = StringUtils.getDir(file);
			uploadDir(file, getLocalPath() + out);
			fireActionFinished(this);
		} else {
			String outfile = StringUtils.getFile(file);

			//System.out.println("transfer: " + file + ", " + getLocalPath() + outfile);
			work(getLocalPath() + outfile, file);
			fireActionFinished(this);
		}

		return 0;
	}

	public int download(String f) {
		String file = toSMB(f);

		if (file.endsWith("/")) {
			String out = StringUtils.getDir(file);
			downloadDir(file, getLocalPath() + out);
			fireActionFinished(this);
		} else {
			String outfile = StringUtils.getFile(file);

			//System.out.println("transfer: " + file + ", " + getLocalPath() + outfile);
			work(file, getLocalPath() + outfile);
			fireActionFinished(this);
		}

		return 0;
	}

	private void downloadDir(String dir, String out) {
		try {
			//System.out.println("downloadDir: " + dir + "," + out);
			fileCount = 0;
			shortProgress = true;
			baseFile = StringUtils.getDir(dir);

			SmbFile f2 = new SmbFile(dir, getAuth());
			String[] tmp = f2.list();

			if (tmp == null) {
				return;
			}

			File fx = new File(out);
			fx.mkdir();

			for (int i = 0; i < tmp.length; i++) {
				tmp[i] = tmp[i].replace('\\', '/');

				//System.out.println("1: " + dir+tmp[i] + ", " + out +tmp[i]);
				SmbFile f3 = new SmbFile(dir + tmp[i], getAuth());

				if (f3.isDirectory()) {
					if (!tmp[i].endsWith("/")) {
						tmp[i] = tmp[i] + "/";
					}

					downloadDir(dir + tmp[i], out + tmp[i]);
				} else {
					fileCount++;
					fireProgressUpdate(baseFile, DataConnection.GETDIR + ":" + fileCount, -1);
					work(dir + tmp[i], out + tmp[i]);
				}
			}

			fireProgressUpdate(baseFile, DataConnection.DFINISHED + ":" + fileCount, -1);
		} catch (Exception ex) {
			ex.printStackTrace();

			//System.out.println(dir + ", " + out);
			Log.debug("Transfer error: " + ex);
			fireProgressUpdate(baseFile, DataConnection.FAILED + ":" + fileCount, -1);
		}

		shortProgress = false;
	}

	private void uploadDir(String dir, String out) {
		try {
			//System.out.println("uploadDir: " + dir + "," + out);
			isDirUpload = true;
			fileCount = 0;
			shortProgress = true;
			baseFile = StringUtils.getDir(dir);

			File f2 = new File(out);
			String[] tmp = f2.list();

			if (tmp == null) {
				return;
			}

			SmbFile fx = new SmbFile(dir, getAuth());
			fx.mkdir();

			for (int i = 0; i < tmp.length; i++) {
				tmp[i] = tmp[i].replace('\\', '/');

				//System.out.println("1: " + dir+tmp[i] + ", " + out +tmp[i]);
				File f3 = new File(out + tmp[i]);

				if (f3.isDirectory()) {
					if (!tmp[i].endsWith("/")) {
						tmp[i] = tmp[i] + "/";
					}

					uploadDir(dir + tmp[i], out + tmp[i]);
				} else {
					fileCount++;
					fireProgressUpdate(baseFile, DataConnection.PUTDIR + ":" + fileCount, -1);
					work(out + tmp[i], dir + tmp[i]);
				}
			}

			fireProgressUpdate(baseFile, DataConnection.DFINISHED + ":" + fileCount, -1);
		} catch (Exception ex) {
			ex.printStackTrace();

			//System.out.println(dir + ", " + out);
			Log.debug("Transfer error: " + ex);
			fireProgressUpdate(baseFile, DataConnection.FAILED + ":" + fileCount, -1);
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
			file = getPWD() + f;
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
				out = new BufferedOutputStream(new SmbFileOutputStream(new SmbFile(outfile, getAuth())));
			} else {
				out = new BufferedOutputStream(new FileOutputStream(outfile));
			}

			//System.out.println("out: " + outfile + ", in: " + file);
			if (file.startsWith("smb://")) {
				in = new BufferedInputStream(new SmbFile(file, getAuth()).getInputStream());
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
				if (len == StreamTokenizer.TT_EOF) {
					break;
				}

				out.write(buf, 0, len);
				reallen += len;

				//System.out.println(file + ":" + StringUtils.getFile(file));
				if (outflag) {
					fireProgressUpdate(StringUtils.getFile(outfile), DataConnection.PUT, reallen);
				} else {
					fireProgressUpdate(StringUtils.getFile(file), DataConnection.GET, reallen);
				}
			}

			fireProgressUpdate(file, DataConnection.FINISHED, -1);
		} catch (IOException ex) {
			Log.debug("Error with file IO (" + ex + ")!");
			fireProgressUpdate(file, DataConnection.FAILED, -1);
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
		if (listeners == null) {
		} else {
			for (int i = 0; i < listeners.size(); i++) {
				ConnectionListener listener = (ConnectionListener) listeners.elementAt(i);
				listener.updateProgress(file, type, bytes);
			}
		}
	}

	public void addConnectionListener(ConnectionListener l) {
		listeners.add(l);
	}

	public void setConnectionListeners(Vector l) {
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

	public boolean login(String user, String pass) {
		return true;
	}

	/**
	 * progress update
	 */
	public void fireProgressUpdate(String file, String type, int bytes) {
		//System.out.println(listener);
		if (listeners == null) {
		} else {
			for (int i = 0; i < listeners.size(); i++) {
				ConnectionListener listener = (ConnectionListener) listeners.elementAt(i);

				if (shortProgress && Settings.shortProgress) {
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

	public void fireActionFinished(SmbConnection con) {
		if (listeners == null) {
		} else {
			for (int i = 0; i < listeners.size(); i++) {
				((ConnectionListener) listeners.elementAt(i)).actionFinished(con);
			}
		}
	}

	public int upload(String file, InputStream i) {
		BufferedOutputStream out = null;
		BufferedInputStream in = null;

		try {
			file = toSMB(file);

			out = new BufferedOutputStream(new SmbFileOutputStream(new SmbFile(file, getAuth())));
			in = new BufferedInputStream(i);

			byte[] buf = new byte[smbBuffer];
			int len = 0;
			int reallen = 0;

			while (true) {
				len = in.read(buf);

				if (len == StreamTokenizer.TT_EOF) {
					break;
				}

				out.write(buf, 0, len);
				reallen += len;

				fireProgressUpdate(StringUtils.getFile(file), DataConnection.PUT, reallen);
			}

			fireProgressUpdate(file, DataConnection.FINISHED, -1);

			return 0;
		} catch (IOException ex) {
			ex.printStackTrace();
			Log.debug("Error with file IO (" + ex + ")!");
			fireProgressUpdate(file, DataConnection.FAILED, -1);
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
		file = toSMB(file);

		try {
			return new BufferedInputStream(new SmbFile(file, getAuth()).getInputStream());
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.debug(ex + " @SmbConnection::getDownloadInputStream");

			return null;
		}
	}

	public Date[] sortDates() {
		return null;
	}

	public boolean rename(String file, String to) {
		try {
			file = toSMB(file);
			to = toSMB(to);

			SmbFile src = new SmbFile(file, getAuth());

			src.renameTo(new SmbFile(to, getAuth()));

			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.debug(ex + " @SmbConnection::rename");

			return false;
		}
	}
}
