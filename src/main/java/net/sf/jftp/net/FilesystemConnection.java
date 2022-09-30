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
package net.sf.jftp.net;

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


public class FilesystemConnection implements BasicConnection {
	public static final int filesystemBuffer = 128000;
	public Vector<Date> dateVector = new Vector<>();
	private String path = "";
	private String pwd = "";
	private Vector<ConnectionListener> listeners = new Vector<>();
	private String[] size = new String[0];
	private int[] perms = null;
	private String baseFile;
	private int fileCount;
	private boolean shortProgress = false;

	public FilesystemConnection() {
	}

	public FilesystemConnection(final String path, final ConnectionListener l) {
		this.listeners.add(l);
		this.chdir(path);
	}

	public int removeFileOrDir(final String file) {
		try {
			if ((file == null) || file.isEmpty()) {
				return -1;
			}

			String tmp = file;

			if (net.sf.jftp.system.StringUtils.isRelative(file)) {
				tmp = this.pwd + file;
			}

			final File f = new File(tmp);

			if (!f.getAbsolutePath().equals(f.getCanonicalPath())) {

				if (!f.delete()) {
					return -1;
				}
			}

			if (f.exists() && f.isDirectory()) {
				this.cleanLocalDir(tmp);
			}

			if (!f.delete()) {
				net.sf.jftp.system.logging.Log.debug("Removal failed.");

				return -1;
			}
		} catch (final IOException ex) {
			net.sf.jftp.system.logging.Log.debug("Error: " + ex);
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

			final File f2 = new File(dir);
			final String[] tmp = f2.list();

			if (tmp == null) {
				return;
			}

			for (final String s : tmp) {
				final java.io.File f3 = new java.io.File(dir + s);

				if (!f3.getAbsolutePath().equals(f3.getCanonicalPath())) {
					f3.delete();


				}

				if (f3.isDirectory()) {
					//System.out.println(dir);
					this.cleanLocalDir(dir + s);
					f3.delete();
				} else {
					//System.out.println(dir+tmp[i]);
					f3.delete();
				}
			}
		} catch (final IOException ex) {
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
		return this.pwd;
	}

	public boolean cdup() {
		return this.chdir(this.pwd.substring(0, this.pwd.lastIndexOf("/") + 1));
	}

	public boolean mkdir(String dirName) {
		if (net.sf.jftp.system.StringUtils.isRelative(dirName)) {
			dirName = this.pwd + dirName;
		}

		final File f = new File(dirName);

		final boolean x = f.mkdir();
		this.fireDirectoryUpdate();

		return x;
	}

	public void list() throws IOException {
	}

	public boolean chdir(final String p) {
		final String p2 = this.processPath(p);

		if (p2 == null) {
			return false;
		}

		final File f = new File(p2);

		if (!f.exists() || !f.isDirectory() || !f.canRead()) {
			net.sf.jftp.system.logging.Log.debug("Access denied.");

			return false;
		}

		this.pwd = p2;

		this.fireDirectoryUpdate();

		return true;
	}

	public boolean chdirNoRefresh(final String p) {
		final String p2 = this.processPath(p);

		if (p2 == null) {
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

	public String processPath(String p) {
		p = p.replace('\\', '/');

		//System.out.print("processPath 1: "+p);
		if (net.sf.jftp.system.StringUtils.isRelative(p)) {
			p = this.pwd + p;
		}

		p = p.replace('\\', '/');

		//System.out.println(", processPath 2: "+p);
		final File f = new File(p);
		String p2;

		if (f.exists()) {
			try {
				p2 = f.getCanonicalPath();
				p2 = p2.replace('\\', '/');

				if (!p2.endsWith("/")) {
					p2 = p2 + "/";
				}

				return p2;
			} catch (final IOException ex) {
				net.sf.jftp.system.logging.Log.debug("Error: can not get pathname (processPath)!");

				return null;
			}
		} else {
			net.sf.jftp.system.logging.Log.debug("(processpPath) No such path: \"" + p + "\"");

			return null;
		}
	}

	public boolean setLocalPath(String p) {
		p = p.replace('\\', '/');

		//System.out.print("local 1:" + p);
		if (net.sf.jftp.system.StringUtils.isRelative(p)) {
			p = this.path + p;
		}

		p = p.replace('\\', '/');

		//System.out.println(", local 2:" + p);
		final File f = new File(p);

		if (f.exists()) {
			try {
				this.path = f.getCanonicalPath();
				this.path = this.path.replace('\\', '/');

				if (!this.path.endsWith("/")) {
					this.path = this.path + "/";
				}

				//System.out.println("localPath: "+path);
			} catch (final IOException ex) {
				net.sf.jftp.system.logging.Log.debug("Error: can not get pathname (local)!");

				return false;
			}
		} else {
			net.sf.jftp.system.logging.Log.out("(local) obsolete setLocalDir: \"" + p + "\"");

			return false;
		}

		return true;
	}

	public String[] sortLs() {
		this.dateVector = new Vector<>();

		final File f = new File(this.pwd);
		final String[] files = f.list();

		if (files == null) {
			return new String[0];
		}

		this.size = new String[files.length];
		this.perms = new int[files.length];

		int accessible = 0;

		for (int i = 0; i < files.length; i++) {
			final File f2 = new File(this.pwd + files[i]);

			if (f2.isDirectory() && !files[i].endsWith("/")) {
				files[i] = files[i] + "/";
			}

			this.size[i] = "" + new File(this.pwd + files[i]).length();

			if (f2.canWrite()) {
				accessible = FtpConnection.W;
			} else if (f2.canRead()) {
				accessible = FtpConnection.R;
			} else {
				accessible = FtpConnection.DENIED;
			}

			this.perms[i] = accessible;

			//System.out.println(pwd+files[i] +" : " +accessible + " : " + size[i]);
			final Date d = new Date(f2.lastModified());

			//System.out.println(d.toString());
			this.dateVector.add(d);
		}

		return files;
	}

	public String[] sortSize() {
		return this.size;
	}

	public int[] getPermissions() {
		return this.perms;
	}

	public int handleDownload(final String file) {
		this.transfer(file);

		return 0;
	}

	public int handleUpload(final String file) {
		this.transfer(file);

		return 0;
	}

	public int download(final String file) {
		this.transfer(file);

		return 0;
	}

	public int upload(final String file) {
		this.transfer(file);

		return 0;
	}

	private void transferDir(final String dir, final String out) {
		this.fileCount = 0;
		this.shortProgress = true;
		this.baseFile = net.sf.jftp.system.StringUtils.getDir(dir);

		final File f2 = new File(dir);
		final String[] tmp = f2.list();

		if (tmp == null) {
			return;
		}

		final File fx = new File(out);

		if (!fx.mkdir()) {
			net.sf.jftp.system.logging.Log.debug("Can not create directory: " + out + " - already exist or permission denied?");
		}

		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = tmp[i].replace('\\', '/');

			//System.out.println("1: " + dir+tmp[i] + ", " + out +tmp[i]);
			final File f3 = new File(dir + tmp[i]);

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
	}

	private void transfer(String file) {
		String out = net.sf.jftp.system.StringUtils.getDir(file);

		if (net.sf.jftp.system.StringUtils.isRelative(file)) {
			file = this.pwd + file;
		}

		file = file.replace('\\', '/');
		out = out.replace('\\', '/');

		final String outfile = net.sf.jftp.system.StringUtils.getFile(file);

		if (file.endsWith("/")) {
			this.transferDir(file, this.path + out);

		} else {
			this.work(file, this.path + outfile);
		}
	}

	private void work(final String file, final String outfile) {
		try {
			final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outfile));
			final BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
			final byte[] buf = new byte[net.sf.jftp.net.FilesystemConnection.filesystemBuffer];
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
				this.fireProgressUpdate(net.sf.jftp.system.StringUtils.getFile(file), DataConnection.GET, reallen);
			}

			out.flush();
			out.close();
			in.close();
			this.fireProgressUpdate(file, DataConnection.FINISHED, -1);
		} catch (final IOException ex) {
			net.sf.jftp.system.logging.Log.debug("Error with file IO (" + ex + ")!");
			this.fireProgressUpdate(file, DataConnection.FAILED, -1);
		}
	}

	public int upload(String file, final InputStream in) {
		if (net.sf.jftp.system.StringUtils.isRelative(file)) {
			file = this.pwd + file;
		}

		file = file.replace('\\', '/');

		try {
			this.work(new BufferedInputStream(in), file, file);

			return 0;
		} catch (final Exception ex) {
			net.sf.jftp.system.logging.Log.debug("Error: " + ex);
			ex.printStackTrace();

			return -1;
		}
	}

	private void work(final BufferedInputStream in, final String outfile, final String file) {
		try {
			final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outfile));
			final byte[] buf = new byte[net.sf.jftp.net.FilesystemConnection.filesystemBuffer];
			int len = 0;
			int reallen = 0;

			while (true) {
				len = in.read(buf);
				System.out.print(".");

				if (len == StreamTokenizer.TT_EOF) {
					break;
				}

				out.write(buf, 0, len);

				reallen += len;
				this.fireProgressUpdate(net.sf.jftp.system.StringUtils.getFile(file), DataConnection.GET, reallen);
			}

			out.flush();
			out.close();
			in.close();
			this.fireProgressUpdate(file, DataConnection.FINISHED, -1);
		} catch (final IOException ex) {
			net.sf.jftp.system.logging.Log.debug("Error with file IO (" + ex + ")!");
			this.fireProgressUpdate(file, DataConnection.FAILED, -1);
		}
	}

	public InputStream getDownloadInputStream(String file) {
		if (net.sf.jftp.system.StringUtils.isRelative(file)) {
			file = this.pwd + file;
		}

		file = file.replace('\\', '/');

		try {
			return new FileInputStream(file);
		} catch (final Exception ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug(ex + " @Filesystemconnection::getDownloadInputStream");

			return null;
		}
	}

	public void addConnectionListener(final ConnectionListener l) {
		this.listeners.add(l);
		this.fireDirectoryUpdate();
	}

	public void setConnectionListeners(final Vector<ConnectionListener> l) {
		this.listeners = l;
		this.fireDirectoryUpdate();
	}

	/**
	 * remote directory has changed
	 */
	public void fireDirectoryUpdate() {
		if (this.listeners == null) {
		} else {
			for (int i = 0; i < this.listeners.size(); i++) {
				this.listeners.elementAt(i).updateRemoteDirectory(this);
			}
		}
	}

	public boolean login(final String user, final String pass) {
		return true;
	}

	public void fireProgressUpdate(final String file, final String type, final int bytes) {
		if (this.listeners == null) {
		} else {
			for (int i = 0; i < this.listeners.size(); i++) {
				final ConnectionListener listener = this.listeners.elementAt(i);

				if (this.shortProgress && net.sf.jftp.config.Settings.shortProgress) {
					if (type.startsWith(DataConnection.DFINISHED)) {
						listener.updateProgress(this.baseFile, DataConnection.DFINISHED + ":" + this.fileCount, bytes);
					}

					listener.updateProgress(this.baseFile, DataConnection.GETDIR + ":" + this.fileCount, bytes);
				} else {
					listener.updateProgress(file, type, bytes);
				}
			}
		}
	}

	public Date[] sortDates() {
		if (this.dateVector.size() > 0) {
			return (Date[]) this.dateVector.toArray();
		} else {
			return null;
		}
	}

	public boolean rename(String file, String to) {
		if (net.sf.jftp.system.StringUtils.isRelative(file)) {
			file = this.pwd + file;
		}

		file = file.replace('\\', '/');

		final File f = new File(file);

		if (net.sf.jftp.system.StringUtils.isRelative(to)) {
			to = this.pwd + to;
		}

		to = to.replace('\\', '/');

		return f.renameTo(new File(to));
	}
}
