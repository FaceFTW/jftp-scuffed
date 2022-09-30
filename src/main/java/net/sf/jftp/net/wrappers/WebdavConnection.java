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

import net.sf.jftp.net.BasicConnection;
import net.sf.jftp.net.ConnectionListener;
import net.sf.jftp.net.DataConnection;
import net.sf.jftp.net.FtpConnection;
import org.apache.commons.httpclient.HttpURL;
import org.apache.webdav.lib.WebdavFile;
import org.apache.webdav.lib.WebdavResource;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;
import java.util.Date;
import java.util.Vector;


public class WebdavConnection implements BasicConnection {
	public static final int webdavBuffer = 32000;
	private final String user;
	private final String pass;
	private String path = "";
	private String pwd = "";
	private Vector listeners = new Vector();
	private String[] size = new String[0];
	private int[] perms = null;
	private String baseFile;
	private int fileCount;
	private boolean shortProgress = false;

	public WebdavConnection(final String path, final String user, final String pass, final ConnectionListener l) {
		this.user = user;
		this.pass = pass;

		listeners.add(l);
		this.chdir(path);
	}

	public int removeFileOrDir(final String file) {
		net.sf.jftp.system.logging.Log.debug("Feature is not implemented yet");

		if (true) {
			return -1;
		}

		try {
			if ((file == null) || file.isEmpty()) {
				return -1;
			}

			String tmp = file;

			if (net.sf.jftp.system.StringUtils.isRelative(file)) {
				tmp = pwd + file;
			}

			final WebdavFile f = new WebdavFile(this.getURL(tmp));

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
				net.sf.jftp.system.logging.Log.debug("Removal failed.");

				return -1;
			}
		} catch (final Exception ex) {
			net.sf.jftp.system.logging.Log.debug("Error: " + ex.toString());
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
			final WebdavFile f2 = new WebdavFile(this.getURL(dir));
			final String[] tmp = f2.list();

			if (tmp == null) {
				return;
			}

			for (final String s : tmp) {
				final org.apache.webdav.lib.WebdavFile f3 = new org.apache.webdav.lib.WebdavFile(this.getURL(dir + s));

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
		} catch (final Exception ex) {
			net.sf.jftp.system.logging.Log.debug("Error: " + ex);
			ex.printStackTrace();
		}
	}

	public void sendRawCommand(final String cmd) {
	}

	public void disconnect() {
	}

	public boolean isConnected() {
		return true;
	}

	public String getPWD() {
		return pwd;
	}

	public boolean cdup() {
		return this.chdir(pwd.substring(0, pwd.lastIndexOf("/") + 1));
	}

	public boolean mkdir(String dirName) {
		net.sf.jftp.system.logging.Log.debug("Feature is not implemented yet");

		if (true) {
			return false;
		}

		try {
			if (net.sf.jftp.system.StringUtils.isRelative(dirName)) {
				dirName = pwd + dirName;
			}

			final WebdavFile f = new WebdavFile(this.getURL(dirName));

			final boolean x = f.mkdir();
			this.fireDirectoryUpdate();

			return x;
		} catch (final Exception ex) {
			net.sf.jftp.system.logging.Log.debug("Error: " + ex.toString());
			ex.printStackTrace();

			return false;
		}
	}

	public void list() throws IOException {
	}

	public boolean chdir(final String p) {
		try {
			final String p2 = this.processPath(p);

			if (p2 == null) {
				return false;
			}

			//WebdavFile f = new WebdavFile(getURL(p2));
			final WebdavResource f = this.getResource(p2);

			if (!f.exists() || !f.isCollection()) //!f.isDirectory() || !f.canRead())
			{
				net.sf.jftp.system.logging.Log.debug("Access denied.");

				return false;
			}

			pwd = p2;

			net.sf.jftp.system.logging.Log.out("PWD: " + pwd);

			this.fireDirectoryUpdate();

			return true;
		} catch (final Exception ex) {
			net.sf.jftp.system.logging.Log.debug("Error: " + ex);
			ex.printStackTrace();

			return false;
		}
	}

	public boolean chdirNoRefresh(final String p) {
		final String p2 = this.processPath(p);

		if (p2 == null) {
			return false;
		}

		//System.out.println(p2);
		pwd = p2;

		return true;
	}

	public String getLocalPath() {
		//System.out.println("local: " + path);
		return path;
	}

	public String processPath(String p) {
		try {
			if (!p.startsWith("http://")) {
				p = pwd + p;
			}

			if (!p.endsWith("/")) {
				p = p + "/";
			}

			while (p.endsWith("/../")) {
				p = p.substring(0, p.lastIndexOf("/../") - 1);
				p = p.substring(0, p.lastIndexOf("/"));
			}

			while (p.endsWith("/./")) {
				p = p.substring(0, p.lastIndexOf("/./") - 1);
				p = p.substring(0, p.lastIndexOf("/"));
			}

			net.sf.jftp.system.logging.Log.out("\n\n\nprocessPath URL: " + p);

			final WebdavResource f = this.getResource(p);
			String p2 = p;

			if (f.exists() && f.isCollection()) {
				try {
					if (!p2.endsWith("/")) {
						p2 = p2 + "/";
					}

					return p2;
				} catch (final Exception ex) {
					net.sf.jftp.system.logging.Log.debug("Error: can not get pathname (processPath)!");

					return null;
				}
			} else {
				net.sf.jftp.system.logging.Log.debug("(processpPath) No such path: \"" + p + "\"");

				return null;
			}
		} catch (final Exception ex) {
			net.sf.jftp.system.logging.Log.debug("Error: " + ex);
			ex.printStackTrace();

			return null;
		}
	}

	public boolean setLocalPath(String p) {
		try {
			p = p.replace('\\', '/');

			//System.out.print("local 1:" + p);
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
				} catch (final Exception ex) {
					net.sf.jftp.system.logging.Log.debug("Error: can not get pathname (local)!");

					return false;
				}
			} else {
				net.sf.jftp.system.logging.Log.debug("(local) No such path: \"" + p + "\"");

				return false;
			}

			return true;
		} catch (final Exception ex) {
			net.sf.jftp.system.logging.Log.debug("Error: " + ex);
			ex.printStackTrace();

			return false;
		}
	}

	public String[] sortLs() {
		try {
			net.sf.jftp.system.logging.Log.out("sortLs PWD: " + pwd);

			final WebdavResource fp = this.getResource(pwd);
			final WebdavResource[] f = fp.listWebdavResources();
			final String[] files = new String[f.length];
			size = new String[f.length];
			perms = new int[f.length];

			final int accessible = 0;

			for (int i = 0; i < f.length; i++) {
				files[i] = f[i].getName();
				net.sf.jftp.system.logging.Log.out("sortLs files[" + i + "]: " + files[i]);

				//size[i] = "" + (int) f[i].length();
				size[i] = "" + (int) f[i].getGetContentLength();
				perms[i] = FtpConnection.R;

				if (f[i].isCollection() && !files[i].endsWith("/")) {
					files[i] = files[i] + "/";
				}
			}

			return files;
		} catch (final Exception ex) {
			net.sf.jftp.system.logging.Log.debug("Error: " + ex);
			ex.printStackTrace();

			return new String[0];
		}
	}

	public String[] sortSize() {
		return size;
	}

	public int[] getPermissions() {
		return perms;
	}

	public int handleDownload(final String file) {
		this.transfer(file);

		return 0;
	}

	public int handleUpload(final String file) {
		this.transfer(file, true);

		return 0;
	}

	public int download(final String file) {
		this.transfer(file);

		return 0;
	}

	public int upload(final String file) {
		this.transfer(file, true);

		return 0;
	}

	private void transferDir(final String dir, final String out) {
		try {
			fileCount = 0;
			shortProgress = true;
			baseFile = net.sf.jftp.system.StringUtils.getDir(dir);

			final WebdavFile f2 = new WebdavFile(this.getURL(dir));
			final String[] tmp = f2.list();

			if (tmp == null) {
				return;
			}

			final WebdavFile fx = new WebdavFile(this.getURL(out));

			if (!fx.mkdir()) {
				net.sf.jftp.system.logging.Log.debug("Can not create directory: " + out + " - already exist or permission denied?");
			}

			for (int i = 0; i < tmp.length; i++) {
				tmp[i] = tmp[i].replace('\\', '/');
				final WebdavFile f3 = new WebdavFile(this.getURL(dir + tmp[i]));

				if (f3.isDirectory()) {
					if (!tmp[i].endsWith("/")) {
						tmp[i] = tmp[i] + "/";
					}

					this.transferDir(dir + tmp[i], out + tmp[i]);
				} else {
					this.fireProgressUpdate(baseFile, DataConnection.GETDIR + ":" + fileCount, -1);
					this.work(dir + tmp[i], out + tmp[i]);
				}
			}

			this.fireProgressUpdate(baseFile, DataConnection.DFINISHED + ":" + fileCount, -1);
			shortProgress = false;
		} catch (final Exception ex) {
			net.sf.jftp.system.logging.Log.debug("Error: " + ex);
			ex.printStackTrace();
		}
	}

	private HttpURL getURL(final String u) {
		try {
			final HttpURL url = new HttpURL(u);

			url.setUserinfo(user, pass);

			return url;
		} catch (final Exception ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug("ERROR: " + ex);

			return null;
		}
	}

	private WebdavResource getResource(final String res) throws IOException {
		return new WebdavResource(this.getURL(res));
	}

	private void transfer(final String file) {
		this.transfer(file, false);
	}

	private void transfer(String file, final boolean up) {
		String out = net.sf.jftp.system.StringUtils.getDir(file);

		if (net.sf.jftp.system.StringUtils.isRelative(file)) {
			file = pwd + file;
		}

		file = file.replace('\\', '/');
		out = out.replace('\\', '/');

		final String outfile = net.sf.jftp.system.StringUtils.getFile(file);

		if (file.endsWith("/")) {
			if (up) {
				net.sf.jftp.system.logging.Log.debug("Directory upload not implemented yet.");

				return;
			}

			this.transferDir(file, path + out);

		} else {
			if (up) {
				this.work(path + outfile, file);
			} else {
				this.work(file, path + outfile);
			}
		}
	}

	private void work(final String file, final String outfile) {
		net.sf.jftp.system.logging.Log.out("transfer started\nfile: " + file + "\noutfile: " + outfile);

		BufferedInputStream in = null;
		BufferedOutputStream out = null;

		try {
			if (outfile.startsWith("http://")) {
				final String resPath = outfile.substring(0, outfile.lastIndexOf("/") + 1);
				final String name = outfile.substring(outfile.lastIndexOf("/") + 1);

				net.sf.jftp.system.logging.Log.debug("Uploading " + file + " to " + resPath + " as " + name);
final WebdavResource res = this.getResource(resPath);

				if (res.putMethod(new File(file))) {
					this.fireProgressUpdate(file, DataConnection.FINISHED, -1);
				} else {
					net.sf.jftp.system.logging.Log.debug("Upload failed.");
					this.fireProgressUpdate(file, DataConnection.FAILED, -1);
				}

				return;
			}

			net.sf.jftp.system.logging.Log.debug("Downloading " + file + " to " + outfile);

			out = new BufferedOutputStream(new FileOutputStream(outfile));
			in = new BufferedInputStream(this.getResource(file).getMethodData());
final byte[] buf = new byte[net.sf.jftp.net.wrappers.WebdavConnection.webdavBuffer];
			int len = 0;
			int reallen = 0;
while (true) {
				len = in.read(buf);
if (len == StreamTokenizer.TT_EOF) {
					break;
				}

				out.write(buf, 0, len);

				reallen += len;
	this.fireProgressUpdate(net.sf.jftp.system.StringUtils.getFile(file), DataConnection.GET, reallen);
			}

			this.fireProgressUpdate(file, DataConnection.FINISHED, -1);
		} catch (final IOException ex) {
			net.sf.jftp.system.logging.Log.debug("Error with file IO (" + ex + ")!");
			ex.printStackTrace();
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

	public int upload(final String file, final InputStream in) {

		net.sf.jftp.system.logging.Log.debug("Upload using InputStream is not implemented yet!");

		return -1;
	}
	public InputStream getDownloadInputStream(String file) {
		if (net.sf.jftp.system.StringUtils.isRelative(file)) {
			file = pwd + file;
		}

		file = file.replace('\\', '/');

		try {
			return this.getResource(file).getMethodData();
		} catch (final Exception ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug(ex + " @WebdavConnection::getDownloadInputStream");

			return null;
		}
	}

	public void addConnectionListener(final ConnectionListener l) {
		listeners.add(l);
	}

	public void setConnectionListeners(final Vector l) {
		listeners = l;
	}
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

	public void fireProgressUpdate(final String file, final String type, final int bytes) {
		if (listeners == null) {
		} else {
			for (int i = 0; i < listeners.size(); i++) {
				final ConnectionListener listener = (ConnectionListener) listeners.elementAt(i);

				if (shortProgress && net.sf.jftp.config.Settings.shortProgress) {
					if (type.startsWith(DataConnection.DFINISHED)) {
						listener.updateProgress(baseFile, DataConnection.DFINISHED + ":" + fileCount, bytes);
					}

					listener.updateProgress(baseFile, DataConnection.GETDIR + ":" + fileCount, bytes);
				} else {
					listener.updateProgress(file, type, bytes);
				}
			}
		}
	}

	public Date[] sortDates() {
		return null;
	}

	public boolean rename(final String from, final String to) {
		net.sf.jftp.system.logging.Log.debug("Not implemented!");

		return false;
	}
}
