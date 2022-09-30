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

import com.sun.xfile.XFile;
import com.sun.xfile.XFileInputStream;
import com.sun.xfile.XFileOutputStream;
import net.sf.jftp.net.BasicConnection;
import net.sf.jftp.net.ConnectionListener;
import net.sf.jftp.net.DataConnection;
import net.sf.jftp.net.FtpConnection;

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


public class NfsConnection implements BasicConnection {
	public static final int buffer = 128000;
	private final boolean dummy = false;
	private String url = "";
	private String host = "";
	private String path = "";
	private String pwd = "";
	private Vector listeners = new Vector();
	private String[] files;
	private String[] size = new String[0];
	private int[] perms = null;
	private String baseFile;
	private int fileCount;
	private boolean isDirUpload = false;
	private boolean shortProgress = false;

	public NfsConnection(final String url) {
		this.url = url;

		this.host = url.substring(6);

		final int x = this.host.indexOf("/");

		if (0 <= x) {
			this.host = this.host.substring(0, x);
		}

		net.sf.jftp.system.logging.Log.out("nfs host is: " + this.host);
	}

	public boolean login(final String user, final String pass) {
		net.sf.jftp.system.logging.Log.out("nfs login called: " + this.url);

		try {
			final XFile xf = new XFile(this.url);

			if (xf.exists()) {
				net.sf.jftp.system.logging.Log.out("nfs url ok");
			} else {
				net.sf.jftp.system.logging.Log.out("WARNING: nfs url not found, cennection will fail!");
			}

			final com.sun.nfs.XFileExtensionAccessor nfsx = (com.sun.nfs.XFileExtensionAccessor) xf.getExtensionAccessor();

			if (!nfsx.loginPCNFSD(this.host, user, pass)) {
				net.sf.jftp.system.logging.Log.out("login failed!");

				return false;
			} else {
				net.sf.jftp.system.logging.Log.debug("Login successful...");
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	public String[] getExports() throws Exception {
		final XFile xf = new XFile(this.url);
		final com.sun.nfs.XFileExtensionAccessor nfsx = (com.sun.nfs.XFileExtensionAccessor) xf.getExtensionAccessor();

		final String[] tmp = nfsx.getExports();

		if (null == tmp) {
			return new String[0];
		}

		for (final String s : tmp) {
			net.sf.jftp.system.logging.Log.out("nfs export found: " + s);
		}

		return tmp;
	}

	public int removeFileOrDir(final String file) {
		try {
			final String tmp = this.toNFS(file);

			final XFile f = new XFile(tmp);

			if (!f.getAbsolutePath().equals(f.getCanonicalPath())) {
				net.sf.jftp.system.logging.Log.debug("WARNING: Skipping symlink, remove failed.");
				net.sf.jftp.system.logging.Log.debug("This is necessary to prevent possible data loss when removing those symlinks.");

				return -1;
			}

			if (f.exists() && f.isDirectory()) {
				this.cleanLocalDir(tmp);
			}

			if (!f.delete()) {
				return -1;
			} else {
				return 1;
			}
		} catch (final IOException ex) {
			net.sf.jftp.system.logging.Log.debug("Error: " + ex);
			ex.printStackTrace();
		}

		return -1;
	}

	private void cleanLocalDir(String dir) {
		dir = this.toNFS(dir);

		if (dir.endsWith("\\")) {
			net.sf.jftp.system.logging.Log.out("need to fix \\-problem!!!");
		}

		if (!dir.endsWith("/")) {
			dir = dir + "/";
		}

		final XFile f2 = new XFile(dir);
		final String[] tmp = f2.list();

		if (null == tmp) {
			return;
		}

		for (final String s : tmp) {
			final com.sun.xfile.XFile f3 = new com.sun.xfile.XFile(dir + s);

			if (f3.isDirectory()) {
				this.cleanLocalDir(dir + s);
				f3.delete();
			} else {
				f3.delete();
			}
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
		String tmp = this.toNFS(this.pwd);

		if (!tmp.endsWith("/")) {
			tmp = tmp + "/";
		}

		return tmp;
	}

	public boolean cdup() {
		String tmp = this.pwd;

		if (this.pwd.endsWith("/") && !this.pwd.equals("nfs://")) {
			tmp = this.pwd.substring(0, this.pwd.lastIndexOf("/"));
		}

		return this.chdir(tmp.substring(0, tmp.lastIndexOf("/") + 1));
	}

	public boolean mkdir(String dirName) {
		if (!dirName.endsWith("/")) {
			dirName = dirName + "/";
		}

		dirName = this.toNFS(dirName);

		final File f = new File(dirName);

		final boolean x = f.mkdir();
		this.fireDirectoryUpdate();

		return x;
	}

	public void list() throws IOException {
	}

	public boolean chdir(final String p) {
		return this.chdir(p, true);
	}

	public boolean chdir(final String p, final boolean refresh) {
		if (p.endsWith("..")) {
			return this.cdup();
		}

		String tmp = this.toNFS(p);

		if (!tmp.endsWith("/")) {
			tmp = tmp + "/";
		}

		if (3 > this.check(tmp)) {
			return false;
		}

		this.pwd = tmp;

		if (refresh) {
			this.fireDirectoryUpdate();
		}

		return true;
	}

	private int check(final String url) {
		int x = 0;

		for (int j = 0; j < url.length(); j++) {
			if ('/' == url.charAt(j)) {
				x++;
			}
		}

		return x;
	}

	public boolean chdirNoRefresh(final String p) {
		return this.chdir(p, false);
	}

	public String getLocalPath() {
		return this.path;
	}

	private String toNFS(String f) {
		String file;

		if (0 < f.lastIndexOf("nfs://")) {
			f = f.substring(f.lastIndexOf("nfs://"));
		}

		if (f.startsWith("nfs://")) {
			file = f;
		} else {
			file = this.getPWD() + f;
		}

		file = file.replace('\\', '/');

		net.sf.jftp.system.logging.Log.out("nfs url: " + file);

		return file;
	}

	public boolean setLocalPath(String p) {
		if (!p.startsWith("/") && !p.startsWith(":", 1)) {
			p = this.path + p;
		}

		final File f = new File(p);

		if (f.exists()) {
			try {
				this.path = f.getCanonicalPath();
				this.path = this.path.replace('\\', '/');

				if (!this.path.endsWith("/")) {
					this.path = this.path + "/";
				}

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
		final String dir = this.getPWD();

		if (3 == this.check(this.toNFS(dir))) {
			try {
				this.files = this.getExports();
			} catch (final Exception ex) {
				net.sf.jftp.system.logging.Log.debug("Can not list exports:" + ex);
				ex.printStackTrace();
			}
		} else {
			final XFile f = new XFile(dir);
			this.files = f.list();
		}

		if (null == this.files) {
			return new String[0];
		}

		this.size = new String[this.files.length];
		this.perms = new int[this.files.length];

		int accessible = 0;

		for (int i = 0; i < this.files.length; i++) {
			final XFile f2 = new XFile(dir + this.files[i]);

			if (f2.isDirectory() && !this.files[i].endsWith("/")) {
				this.files[i] = this.files[i] + "/";
			}

			this.size[i] = "" + f2.length();

			if (f2.canWrite()) {
				accessible = FtpConnection.W;
			} else if (f2.canRead()) {
				accessible = FtpConnection.R;
			} else {
				accessible = FtpConnection.DENIED;
			}

			this.perms[i] = accessible;
		}

		return this.files;
	}

	public String[] sortSize() {
		return this.size;
	}

	public int[] getPermissions() {
		return this.perms;
	}

	public int handleUpload(final String f) {
		this.upload(f);

		return 0;
	}

	public int handleDownload(final String f) {
		this.download(f);

		return 0;
	}

	public int upload(final String f) {
		final String file = this.toNFS(f);

		if (file.endsWith("/")) {
			final String out = net.sf.jftp.system.StringUtils.getDir(file);
			this.uploadDir(file, this.path + out);
			this.fireActionFinished(this);
		} else {
			final String outfile = net.sf.jftp.system.StringUtils.getFile(file);

			this.work(this.path + outfile, file);
			this.fireActionFinished(this);
		}

		return 0;
	}

	public int download(final String f) {
		final String file = this.toNFS(f);

		if (file.endsWith("/")) {
			final String out = net.sf.jftp.system.StringUtils.getDir(file);
			this.downloadDir(file, this.path + out);
			this.fireActionFinished(this);
		} else {
			final String outfile = net.sf.jftp.system.StringUtils.getFile(file);
			this.work(file, this.path + outfile);
			this.fireActionFinished(this);
		}

		return 0;
	}

	private void downloadDir(final String dir, final String out) {
		try {
			this.fileCount = 0;
			this.shortProgress = true;
			this.baseFile = net.sf.jftp.system.StringUtils.getDir(dir);

			final XFile f2 = new XFile(dir);
			final String[] tmp = f2.list();

			if (null == tmp) {
				return;
			}

			final File fx = new File(out);
			fx.mkdir();

			for (int i = 0; i < tmp.length; i++) {
				tmp[i] = tmp[i].replace('\\', '/');
				final XFile f3 = new XFile(dir + tmp[i]);

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
		} catch (final Exception ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug("Transfer error: " + ex);
			this.fireProgressUpdate(this.baseFile, DataConnection.FAILED + ":" + this.fileCount, -1);
		}

		this.shortProgress = false;
	}

	private void uploadDir(final String dir, final String out) {
		try {
			this.isDirUpload = true;
			this.fileCount = 0;
			this.shortProgress = true;
			this.baseFile = net.sf.jftp.system.StringUtils.getDir(dir);

			final File f2 = new File(out);
			final String[] tmp = f2.list();

			if (null == tmp) {
				return;
			}

			final XFile fx = new XFile(dir);
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
					this.fileCount++;
					this.fireProgressUpdate(this.baseFile, DataConnection.PUTDIR + ":" + this.fileCount, -1);
					this.work(out + tmp[i], dir + tmp[i]);
				}
			}

			this.fireProgressUpdate(this.baseFile, DataConnection.DFINISHED + ":" + this.fileCount, -1);
		} catch (final Exception ex) {
			ex.printStackTrace();

			//System.out.println(dir + ", " + out);
			net.sf.jftp.system.logging.Log.debug("Transfer error: " + ex);
			this.fireProgressUpdate(this.baseFile, DataConnection.FAILED + ":" + this.fileCount, -1);
		}

		this.isDirUpload = false;
		this.shortProgress = true;
	}

	private void work(final String file, final String outfile) {
		BufferedOutputStream out = null;
		BufferedInputStream in = null;

		try {
			boolean outflag = false;

			if (outfile.startsWith("nfs://")) {
				outflag = true;
				out = new BufferedOutputStream(new XFileOutputStream(outfile));
			} else {
				out = new BufferedOutputStream(new FileOutputStream(outfile));
			}

			//System.out.println("out: " + outfile + ", in: " + file);
			if (file.startsWith("nfs://")) {
				in = new BufferedInputStream(new XFileInputStream(file));
			} else {
				in = new BufferedInputStream(new FileInputStream(file));
			}

			final byte[] buf = new byte[net.sf.jftp.net.wrappers.NfsConnection.buffer];
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
		if (null == this.listeners) {
		} else {
			for (int i = 0; i < this.listeners.size(); i++) {
				final ConnectionListener listener = (ConnectionListener) this.listeners.elementAt(i);
				listener.updateProgress(file, type, bytes);
			}
		}
	}

	public void addConnectionListener(final ConnectionListener l) {
		this.listeners.add(l);
	}

	public void setConnectionListeners(final Vector l) {
		this.listeners = l;
	}

	/**
	 * remote directory has changed
	 */
	public void fireDirectoryUpdate() {
		if (null == this.listeners) {
		} else {
			for (int i = 0; i < this.listeners.size(); i++) {
				((ConnectionListener) this.listeners.elementAt(i)).updateRemoteDirectory(this);
			}
		}
	}

	/**
	 * progress update
	 */
	public void fireProgressUpdate(final String file, final String type, final int bytes) {
		//System.out.println(listener);
		if (null == this.listeners) {
		} else {
			for (int i = 0; i < this.listeners.size(); i++) {
				final ConnectionListener listener = (ConnectionListener) this.listeners.elementAt(i);

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

	public void fireActionFinished(final NfsConnection con) {
		if (null == this.listeners) {
		} else {
			for (int i = 0; i < this.listeners.size(); i++) {
				((ConnectionListener) this.listeners.elementAt(i)).actionFinished(con);
			}
		}
	}

	public int upload(String file, final InputStream i) {
		BufferedInputStream in = null;
		BufferedOutputStream out = null;

		try {
			file = this.toNFS(file);

			out = new BufferedOutputStream(new XFileOutputStream(file));
			in = new BufferedInputStream(i);

			final byte[] buf = new byte[net.sf.jftp.net.wrappers.NfsConnection.buffer];
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
		} catch (final IOException ex) {
			net.sf.jftp.system.logging.Log.debug("Error with file IO (" + ex + ")!");
			this.fireProgressUpdate(file, DataConnection.FAILED, -1);

			return -1;
		} finally {
			try {
				out.flush();
				out.close();
				in.close();
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		}

		return 0;
	}

	public InputStream getDownloadInputStream(String file) {
		file = this.toNFS(file);
		net.sf.jftp.system.logging.Log.debug(file);

		try {
			return new BufferedInputStream(new XFileInputStream(file));
		} catch (final Exception ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug(ex + " @NfsConnection::getDownloadInputStream");

			return null;
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
