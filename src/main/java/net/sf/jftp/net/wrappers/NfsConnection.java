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

		host = url.substring(6);

		final int x = host.indexOf("/");

		if (x >= 0) {
			host = host.substring(0, x);
		}

		net.sf.jftp.system.logging.Log.out("nfs host is: " + host);
	}

	public boolean login(final String user, final String pass) {
		net.sf.jftp.system.logging.Log.out("nfs login called: " + url);

		try {
			final XFile xf = new XFile(url);

			if (xf.exists()) {
				net.sf.jftp.system.logging.Log.out("nfs url ok");
			} else {
				net.sf.jftp.system.logging.Log.out("WARNING: nfs url not found, cennection will fail!");
			}

			final com.sun.nfs.XFileExtensionAccessor nfsx = (com.sun.nfs.XFileExtensionAccessor) xf.getExtensionAccessor();

			if (!nfsx.loginPCNFSD(host, user, pass)) {
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
		final XFile xf = new XFile(url);
		final com.sun.nfs.XFileExtensionAccessor nfsx = (com.sun.nfs.XFileExtensionAccessor) xf.getExtensionAccessor();

		final String[] tmp = nfsx.getExports();

		if (tmp == null) {
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

		if (tmp == null) {
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
		String tmp = this.toNFS(pwd);

		if (!tmp.endsWith("/")) {
			tmp = tmp + "/";
		}

		return tmp;
	}

	public boolean cdup() {
		String tmp = pwd;

		if (pwd.endsWith("/") && !pwd.equals("nfs://")) {
			tmp = pwd.substring(0, pwd.lastIndexOf("/"));
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

		if (this.check(tmp) < 3) {
			return false;
		}

		pwd = tmp;

		if (refresh) {
			this.fireDirectoryUpdate();
		}

		return true;
	}

	private int check(final String url) {
		int x = 0;

		for (int j = 0; j < url.length(); j++) {
			if (url.charAt(j) == '/') {
				x++;
			}
		}

		return x;
	}

	public boolean chdirNoRefresh(final String p) {
		return this.chdir(p, false);
	}

	public String getLocalPath() {
		return path;
	}

	private String toNFS(String f) {
		String file;

		if (f.lastIndexOf("nfs://") > 0) {
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
			p = path + p;
		}

		final File f = new File(p);

		if (f.exists()) {
			try {
				path = f.getCanonicalPath();
				path = path.replace('\\', '/');

				if (!path.endsWith("/")) {
					path = path + "/";
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

		if (this.check(this.toNFS(dir)) == 3) {
			try {
				files = this.getExports();
			} catch (final Exception ex) {
				net.sf.jftp.system.logging.Log.debug("Can not list exports:" + ex);
				ex.printStackTrace();
			}
		} else {
			final XFile f = new XFile(dir);
			files = f.list();
		}

		if (files == null) {
			return new String[0];
		}

		size = new String[files.length];
		perms = new int[files.length];

		int accessible = 0;

		for (int i = 0; i < files.length; i++) {
			final XFile f2 = new XFile(dir + files[i]);

			if (f2.isDirectory() && !files[i].endsWith("/")) {
				files[i] = files[i] + "/";
			}

			size[i] = "" + f2.length();

			if (f2.canWrite()) {
				accessible = FtpConnection.W;
			} else if (f2.canRead()) {
				accessible = FtpConnection.R;
			} else {
				accessible = FtpConnection.DENIED;
			}

			perms[i] = accessible;
		}

		return files;
	}

	public String[] sortSize() {
		return size;
	}

	public int[] getPermissions() {
		return perms;
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
			this.uploadDir(file, path + out);
			this.fireActionFinished(this);
		} else {
			final String outfile = net.sf.jftp.system.StringUtils.getFile(file);

			this.work(path + outfile, file);
			this.fireActionFinished(this);
		}

		return 0;
	}

	public int download(final String f) {
		final String file = this.toNFS(f);

		if (file.endsWith("/")) {
			final String out = net.sf.jftp.system.StringUtils.getDir(file);
			this.downloadDir(file, path + out);
			this.fireActionFinished(this);
		} else {
			final String outfile = net.sf.jftp.system.StringUtils.getFile(file);
			this.work(file, path + outfile);
			this.fireActionFinished(this);
		}

		return 0;
	}

	private void downloadDir(final String dir, final String out) {
		try {
			fileCount = 0;
			shortProgress = true;
			baseFile = net.sf.jftp.system.StringUtils.getDir(dir);

			final XFile f2 = new XFile(dir);
			final String[] tmp = f2.list();

			if (tmp == null) {
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
					fileCount++;
					this.fireProgressUpdate(baseFile, DataConnection.GETDIR + ":" + fileCount, -1);
					this.work(dir + tmp[i], out + tmp[i]);
				}
			}

			this.fireProgressUpdate(baseFile, DataConnection.DFINISHED + ":" + fileCount, -1);
		} catch (final Exception ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug("Transfer error: " + ex);
			this.fireProgressUpdate(baseFile, DataConnection.FAILED + ":" + fileCount, -1);
		}

		shortProgress = false;
	}

	private void uploadDir(final String dir, final String out) {
		try {
			isDirUpload = true;
			fileCount = 0;
			shortProgress = true;
			baseFile = net.sf.jftp.system.StringUtils.getDir(dir);

			final File f2 = new File(out);
			final String[] tmp = f2.list();

			if (tmp == null) {
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

	public void fireActionFinished(final NfsConnection con) {
		if (listeners == null) {
		} else {
			for (int i = 0; i < listeners.size(); i++) {
				((ConnectionListener) listeners.elementAt(i)).actionFinished(con);
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

				if (len == StreamTokenizer.TT_EOF) {
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
