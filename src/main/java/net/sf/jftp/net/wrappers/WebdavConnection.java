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

import net.sf.jftp.config.Settings;
import net.sf.jftp.net.BasicConnection;
import net.sf.jftp.net.ConnectionListener;
import net.sf.jftp.net.DataConnection;
import net.sf.jftp.net.FtpConstants;
import net.sf.jftp.system.StringUtils;
import net.sf.jftp.system.logging.Log;
import org.apache.commons.httpclient.HttpURL;
import org.apache.webdav.lib.WebdavFile;
import org.apache.webdav.lib.WebdavResource;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;


public class WebdavConnection implements BasicConnection {
	private static final int webdavBuffer = 32000;
	private final String user;
	private final String pass;
	private String path = "";
	private String pwd = "";
	private java.util.List<ConnectionListener> listeners = new java.util.ArrayList<>();
	private String[] size = new String[0];
	private int[] perms;
	private String baseFile;
	private int fileCount;
	private boolean shortProgress;

	public WebdavConnection(String path, String user, String pass, ConnectionListener l) {
		super();
		this.user = user;
		this.pass = pass;

		this.listeners.add(l);
		this.chdir(path);
	}

	public int removeFileOrDir(String file) {
		Log.debug("Feature is not implemented yet");

		if (true) {
			return -1;
		}

		try {
			if ((null == file) || file.isEmpty()) {
				return -1;
			}

			String tmp = file;

			if (StringUtils.isRelative(file)) {
				tmp = this.pwd + file;
			}

			WebdavFile f = new WebdavFile(this.getURL(tmp));

			if (!f.getAbsolutePath().equals(f.getCanonicalPath())) {

				if (!f.delete()) {
					return -1;
				}
			}

			if (f.exists() && f.isDirectory()) {
				this.cleanLocalDir(tmp);
			}

			//System.out.println(tmp);
			if (!f.delete()) {
				Log.debug("Removal failed.");

				return -1;
			}
		} catch (Exception ex) {
			Log.debug("Error: " + ex.toString());
			ex.printStackTrace();
		}

		return -1;
	}

	private void cleanLocalDir(String dir) {
		try {
			dir = dir.replace('\\', '/');

			if (!dir.endsWith("/")) {
				dir = dir + "/";
			}
			WebdavFile f2 = new WebdavFile(this.getURL(dir));
			String[] tmp = f2.list();

			if (null == tmp) {
				return;
			}

			for (String s : tmp) {
				org.apache.webdav.lib.WebdavFile f3 = new org.apache.webdav.lib.WebdavFile(this.getURL(dir + s));

				if (!f3.getAbsolutePath().equals(f3.getCanonicalPath())) {
					f3.delete();

				}

				if (f3.isDirectory()) {
					this.cleanLocalDir(dir + s);
					f3.delete();
				} else {
					//System.out.println(dir+tmp[i]);
					f3.delete();
				}
			}
		} catch (Exception ex) {
			Log.debug("Error: " + ex);
			ex.printStackTrace();
		}
	}

	public void sendRawCommand(String cmd) {
	}

	public void disconnect() {
	}

	public boolean isConnected() {
		return true;
	}

	public String getPWD() {
		return this.pwd;
	}

	public boolean cdup() {
		return this.chdir(this.pwd.substring(0, this.pwd.lastIndexOf('/') + 1));
	}

	public boolean mkdir(String dirName) {
		Log.debug("Feature is not implemented yet");

		if (true) {
			return false;
		}

		try {
			if (StringUtils.isRelative(dirName)) {
				dirName = this.pwd + dirName;
			}

			WebdavFile f = new WebdavFile(this.getURL(dirName));

			boolean x = f.mkdir();
			this.fireDirectoryUpdate();

			return x;
		} catch (Exception ex) {
			Log.debug("Error: " + ex.toString());
			ex.printStackTrace();

			return false;
		}
	}

	public void list() throws IOException {
	}

	public boolean chdir(String p) {
		try {
			String p2 = this.processPath(p);

			if (null == p2) {
				return false;
			}

			//WebdavFile f = new WebdavFile(getURL(p2));
			WebdavResource f = this.getResource(p2);

			if (!f.exists() || !f.isCollection()) //!f.isDirectory() || !f.canRead())
			{
				Log.debug("Access denied.");

				return false;
			}

			this.pwd = p2;

			Log.out("PWD: " + this.pwd);

			this.fireDirectoryUpdate();

			return true;
		} catch (Exception ex) {
			Log.debug("Error: " + ex);
			ex.printStackTrace();

			return false;
		}
	}

	public boolean chdirNoRefresh(String p) {
		String p2 = this.processPath(p);

		if (null == p2) {
			return false;
		}

		//System.out.println(p2);
		this.pwd = p2;

		return true;
	}

	public String getLocalPath() {
		//System.out.println("local: " + path);
		return this.path;
	}

	private String processPath(String p) {
		try {
			if (!p.startsWith("http://")) {
				p = this.pwd + p;
			}

			if (!p.endsWith("/")) {
				p = p + "/";
			}

			while (p.endsWith("/../")) {
				p = p.substring(0, p.lastIndexOf("/../") - 1);
				p = p.substring(0, p.lastIndexOf('/'));
			}

			while (p.endsWith("/./")) {
				p = p.substring(0, p.lastIndexOf("/./") - 1);
				p = p.substring(0, p.lastIndexOf('/'));
			}

			Log.out("\n\n\nprocessPath URL: " + p);

			WebdavResource f = this.getResource(p);
			String p2 = p;

			if (f.exists() && f.isCollection()) {
				try {
					if (!p2.endsWith("/")) {
						p2 = p2 + "/";
					}

					return p2;
				} catch (Exception ex) {
					Log.debug("Error: can not get pathname (processPath)!");

					return null;
				}
			} else {
				Log.debug("(processpPath) No such path: \"" + p + "\"");

				return null;
			}
		} catch (Exception ex) {
			Log.debug("Error: " + ex);
			ex.printStackTrace();

			return null;
		}
	}

	public boolean setLocalPath(String p) {
		try {
			p = p.replace('\\', '/');

			//System.out.print("local 1:" + p);
			if (StringUtils.isRelative(p)) {
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
				} catch (Exception ex) {
					Log.debug("Error: can not get pathname (local)!");

					return false;
				}
			} else {
				Log.debug("(local) No such path: \"" + p + "\"");

				return false;
			}

			return true;
		} catch (Exception ex) {
			Log.debug("Error: " + ex);
			ex.printStackTrace();

			return false;
		}
	}

	public String[] sortLs() {
		try {
			Log.out("sortLs PWD: " + this.pwd);

			WebdavResource fp = this.getResource(this.pwd);
			WebdavResource[] f = fp.listWebdavResources();
			String[] files = new String[f.length];
			this.size = new String[f.length];
			this.perms = new int[f.length];

			final int accessible = 0;

			for (int i = 0; i < f.length; i++) {
				files[i] = f[i].getName();
				Log.out("sortLs files[" + i + "]: " + files[i]);

				//size[i] = "" + (int) f[i].length();
				this.size[i] = String.valueOf((int) f[i].getGetContentLength());
				this.perms[i] = FtpConstants.R;

				if (f[i].isCollection() && !files[i].endsWith("/")) {
					files[i] = files[i] + "/";
				}
			}

			return files;
		} catch (Exception ex) {
			Log.debug("Error: " + ex);
			ex.printStackTrace();

			return new String[0];
		}
	}

	public String[] sortSize() {
		return this.size;
	}

	public int[] getPermissions() {
		return this.perms;
	}

	public int handleDownload(String file) {
		this.transfer(file);

		return 0;
	}

	public int handleUpload(String file) {
		this.transfer(file, true);

		return 0;
	}

	public int download(String file) {
		this.transfer(file);

		return 0;
	}

	public int upload(String file) {
		this.transfer(file, true);

		return 0;
	}

	private void transferDir(String dir, String out) {
		try {
			this.fileCount = 0;
			this.shortProgress = true;
			this.baseFile = StringUtils.getDir(dir);

			WebdavFile f2 = new WebdavFile(this.getURL(dir));
			String[] tmp = f2.list();

			if (null == tmp) {
				return;
			}

			WebdavFile fx = new WebdavFile(this.getURL(out));

			if (!fx.mkdir()) {
				Log.debug("Can not create directory: " + out + " - already exist or permission denied?");
			}

			for (int i = 0; i < tmp.length; i++) {
				tmp[i] = tmp[i].replace('\\', '/');
				WebdavFile f3 = new WebdavFile(this.getURL(dir + tmp[i]));

				if (f3.isDirectory()) {
					if (!tmp[i].endsWith("/")) {
						tmp[i] = tmp[i] + "/";
					}

					this.transferDir(dir + tmp[i], out + tmp[i]);
				} else {
					this.fireProgressUpdate(this.baseFile, DataConnection.GETDIR + ":" + this.fileCount, -1);
					this.work(dir + tmp[i], out + tmp[i]);
				}
			}

			this.fireProgressUpdate(this.baseFile, DataConnection.DFINISHED + ":" + this.fileCount, -1);
			this.shortProgress = false;
		} catch (Exception ex) {
			Log.debug("Error: " + ex);
			ex.printStackTrace();
		}
	}

	private HttpURL getURL(String u) {
		try {
			HttpURL url = new HttpURL(u);

			url.setUserinfo(this.user, this.pass);

			return url;
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.debug("ERROR: " + ex);

			return null;
		}
	}

	private WebdavResource getResource(String res) throws IOException {
		return new WebdavResource(this.getURL(res));
	}

	private void transfer(String file) {
		this.transfer(file, false);
	}

	private void transfer(String file, boolean up) {
		String out = StringUtils.getDir(file);

		if (StringUtils.isRelative(file)) {
			file = this.pwd + file;
		}

		file = file.replace('\\', '/');
		out = out.replace('\\', '/');

		String outfile = StringUtils.getFile(file);

		if (file.endsWith("/")) {
			if (up) {
				Log.debug("Directory upload not implemented yet.");

				return;
			}

			this.transferDir(file, this.path + out);

		} else {
			if (up) {
				this.work(this.path + outfile, file);
			} else {
				this.work(file, this.path + outfile);
			}
		}
	}

	private void work(String file, String outfile) {
		Log.out("transfer started\nfile: " + file + "\noutfile: " + outfile);

		BufferedInputStream in = null;
		BufferedOutputStream out = null;

		try {
			if (outfile.startsWith("http://")) {
				String resPath = outfile.substring(0, outfile.lastIndexOf('/') + 1);
				String name = outfile.substring(outfile.lastIndexOf('/') + 1);

				Log.debug("Uploading " + file + " to " + resPath + " as " + name);
				WebdavResource res = this.getResource(resPath);

				if (res.putMethod(new File(file))) {
					this.fireProgressUpdate(file, DataConnection.FINISHED, -1);
				} else {
					Log.debug("Upload failed.");
					this.fireProgressUpdate(file, DataConnection.FAILED, -1);
				}

				return;
			}

			Log.debug("Downloading " + file + " to " + outfile);

			out = new BufferedOutputStream(new FileOutputStream(outfile));
			in = new BufferedInputStream(this.getResource(file).getMethodData());
			byte[] buf = new byte[webdavBuffer];
			int len = 0;
			int reallen = 0;
			while (true) {
				len = in.read(buf);
				if (java.io.StreamTokenizer.TT_EOF == len) {
					break;
				}

				out.write(buf, 0, len);

				reallen += len;
				this.fireProgressUpdate(StringUtils.getFile(file), DataConnection.GET, reallen);
			}

			this.fireProgressUpdate(file, DataConnection.FINISHED, -1);
		} catch (IOException ex) {
			Log.debug("Error with file IO (" + ex + ")!");
			ex.printStackTrace();
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

	public int upload(String file, InputStream in) {

		Log.debug("Upload using InputStream is not implemented yet!");

		return -1;
	}

	public InputStream getDownloadInputStream(String file) {
		if (StringUtils.isRelative(file)) {
			file = this.pwd + file;
		}

		file = file.replace('\\', '/');

		try {
			return this.getResource(file).getMethodData();
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.debug(ex + " @WebdavConnection::getDownloadInputStream");

			return null;
		}
	}

	public void addConnectionListener(ConnectionListener l) {
		this.listeners.add(l);
	}

	public void setConnectionListeners(java.util.List<ConnectionListener> l) {
		this.listeners = l;
	}

	private void fireDirectoryUpdate() {
		if (null == this.listeners) {
		} else {
			for (ConnectionListener listener : this.listeners) {
				((ConnectionListener) listener).updateRemoteDirectory(this);
			}
		}
	}

	public boolean login(String user, String pass) {
		return true;
	}

	private void fireProgressUpdate(String file, String type, int bytes) {
		if (null == this.listeners) {
		} else {
			for (ConnectionListener connectionListener : this.listeners) {

				if (this.shortProgress && Settings.shortProgress) {
					if (type.startsWith(DataConnection.DFINISHED)) {
						((ConnectionListener) connectionListener).updateProgress(this.baseFile, DataConnection.DFINISHED + ":" + this.fileCount, bytes);
					}

					((ConnectionListener) connectionListener).updateProgress(this.baseFile, DataConnection.GETDIR + ":" + this.fileCount, bytes);
				} else {
					((ConnectionListener) connectionListener).updateProgress(file, type, bytes);
				}
			}
		}
	}

	public LocalDateTime[] sortDates() {
		return null;
	}

	public boolean rename(String from, String to) {
		Log.debug("Not implemented!");

		return false;
	}
}
