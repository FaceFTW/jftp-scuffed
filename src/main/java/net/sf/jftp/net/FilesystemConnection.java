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

import net.sf.jftp.config.Settings;
import net.sf.jftp.system.StringUtils;
import net.sf.jftp.system.logging.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;


public class FilesystemConnection implements BasicConnection {
	private static final int filesystemBuffer = 128000;
	public List<LocalDateTime> dateVector = new ArrayList<>();
	private String path = "";
	private String pwd = "";
	private java.util.List<ConnectionListener> listeners = new ArrayList<>();
	private String[] size = new String[0];
	private int[] perms;
	private String baseFile;
	private int fileCount;
	private boolean shortProgress;

	public FilesystemConnection() {
		super();
	}

	public FilesystemConnection(String path, ConnectionListener l) {
		super();
		this.listeners.add(l);
		this.chdir(path);
	}

	public int removeFileOrDir(String file) {
		try {
			if ((null == file) || file.isEmpty()) {
				return -1;
			}

			String tmp = file;

			if (StringUtils.isRelative(file)) {
				tmp = this.pwd + file;
			}

			File f = new File(tmp);

			if (!f.getAbsolutePath().equals(f.getCanonicalPath())) {

				if (!f.delete()) {
					return -1;
				}
			}

			if (f.exists() && f.isDirectory()) {
				this.cleanLocalDir(tmp);
			}

			if (!f.delete()) {
				Log.debug("Removal failed.");

				return -1;
			}
		} catch (IOException ex) {
			Log.debug("Error: " + ex);
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

			File f2 = new File(dir);
			String[] tmp = f2.list();

			if (null == tmp) {
				return;
			}

			for (String s : tmp) {
				java.io.File f3 = new java.io.File(dir + s);

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
		} catch (IOException ex) {
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
		if (StringUtils.isRelative(dirName)) {
			dirName = this.pwd + dirName;
		}

		File f = new File(dirName);

		boolean x = f.mkdir();
		this.fireDirectoryUpdate();

		return x;
	}

	public void list() throws IOException {
	}

	public boolean chdir(String p) {
		String p2 = this.processPath(p);

		if (null == p2) {
			return false;
		}

		File f = new File(p2);

		if (!f.exists() || !f.isDirectory() || !f.canRead()) {
			Log.debug("Access denied.");

			return false;
		}

		this.pwd = p2;

		this.fireDirectoryUpdate();

		return true;
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
		p = p.replace('\\', '/');

		//System.out.print("processPath 1: "+p);
		if (StringUtils.isRelative(p)) {
			p = this.pwd + p;
		}

		p = p.replace('\\', '/');

		//System.out.println(", processPath 2: "+p);
		File f = new File(p);
		String p2;

		if (f.exists()) {
			try {
				p2 = f.getCanonicalPath();
				p2 = p2.replace('\\', '/');

				if (!p2.endsWith("/")) {
					p2 = p2 + "/";
				}

				return p2;
			} catch (IOException ex) {
				Log.debug("Error: can not get pathname (processPath)!");

				return null;
			}
		} else {
			Log.debug("(processpPath) No such path: \"" + p + "\"");

			return null;
		}
	}

	public boolean setLocalPath(String p) {
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
			} catch (IOException ex) {
				Log.debug("Error: can not get pathname (local)!");

				return false;
			}
		} else {
			Log.out("(local) obsolete setLocalDir: \"" + p + "\"");

			return false;
		}

		return true;
	}

	public String[] sortLs() {
		this.dateVector = new java.util.ArrayList<>();

		File f = new File(this.pwd);
		String[] files = f.list();

		if (null == files) {
			return new String[0];
		}

		this.size = new String[files.length];
		this.perms = new int[files.length];

		int accessible = 0;

		for (int i = 0; i < files.length; i++) {
			File f2 = new File(this.pwd + files[i]);

			if (f2.isDirectory() && !files[i].endsWith("/")) {
				files[i] = files[i] + "/";
			}

			this.size[i] = String.valueOf(new File(this.pwd + files[i]).length());

			if (f2.canWrite()) {
				accessible = FtpConstants.W;
			} else if (f2.canRead()) {
				accessible = FtpConstants.R;
			} else {
				accessible = FtpConstants.DENIED;
			}

			this.perms[i] = accessible;

			//System.out.println(pwd+files[i] +" : " +accessible + " : " + size[i]);
			LocalDateTime d = LocalDateTime.ofEpochSecond(f2.lastModified(), 0, ZoneOffset.UTC);

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

	public int handleDownload(String file) {
		this.transfer(file);

		return 0;
	}

	public int handleUpload(String file) {
		this.transfer(file);

		return 0;
	}

	public int download(String file) {
		this.transfer(file);

		return 0;
	}

	public int upload(String file) {
		this.transfer(file);

		return 0;
	}

	private void transferDir(String dir, String out) {
		this.fileCount = 0;
		this.shortProgress = true;
		this.baseFile = StringUtils.getDir(dir);

		File f2 = new File(dir);
		String[] tmp = f2.list();

		if (null == tmp) {
			return;
		}

		File fx = new File(out);

		if (!fx.mkdir()) {
			Log.debug("Can not create directory: " + out + " - already exist or permission denied?");
		}

		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = tmp[i].replace('\\', '/');

			//System.out.println("1: " + dir+tmp[i] + ", " + out +tmp[i]);
			File f3 = new File(dir + tmp[i]);

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
		String out = StringUtils.getDir(file);

		if (StringUtils.isRelative(file)) {
			file = this.pwd + file;
		}

		file = file.replace('\\', '/');
		out = out.replace('\\', '/');

		String outfile = StringUtils.getFile(file);

		if (file.endsWith("/")) {
			this.transferDir(file, this.path + out);

		} else {
			this.work(file, this.path + outfile);
		}
	}

	private void work(String file, String outfile) {
		try {
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outfile));
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
			byte[] buf = new byte[filesystemBuffer];
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
				this.fireProgressUpdate(StringUtils.getFile(file), DataConnection.GET, reallen);
			}

			out.flush();
			out.close();
			in.close();
			this.fireProgressUpdate(file, DataConnection.FINISHED, -1);
		} catch (IOException ex) {
			Log.debug("Error with file IO (" + ex + ")!");
			this.fireProgressUpdate(file, DataConnection.FAILED, -1);
		}
	}

	public int upload(String file, InputStream in) {
		if (StringUtils.isRelative(file)) {
			file = this.pwd + file;
		}

		file = file.replace('\\', '/');

		try {
			this.work(new BufferedInputStream(in), file, file);

			return 0;
		} catch (Exception ex) {
			Log.debug("Error: " + ex);
			ex.printStackTrace();

			return -1;
		}
	}

	private void work(BufferedInputStream in, String outfile, String file) {
		try {
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outfile));
			byte[] buf = new byte[filesystemBuffer];
			int len = 0;
			int reallen = 0;

			while (true) {
				len = in.read(buf);
				System.out.print(".");

				if (java.io.StreamTokenizer.TT_EOF == len) {
					break;
				}

				out.write(buf, 0, len);

				reallen += len;
				this.fireProgressUpdate(StringUtils.getFile(file), DataConnection.GET, reallen);
			}

			out.flush();
			out.close();
			in.close();
			this.fireProgressUpdate(file, DataConnection.FINISHED, -1);
		} catch (IOException ex) {
			Log.debug("Error with file IO (" + ex + ")!");
			this.fireProgressUpdate(file, DataConnection.FAILED, -1);
		}
	}

	public InputStream getDownloadInputStream(String file) {
		if (StringUtils.isRelative(file)) {
			file = this.pwd + file;
		}

		file = file.replace('\\', '/');

		try {
			return new FileInputStream(file);
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.debug(ex + " @Filesystemconnection::getDownloadInputStream");

			return null;
		}
	}

	public void addConnectionListener(ConnectionListener l) {
		this.listeners.add(l);
		this.fireDirectoryUpdate();
	}

	public void setConnectionListeners(java.util.List<ConnectionListener> l) {
		this.listeners = l;
		this.fireDirectoryUpdate();
	}

	/**
	 * remote directory has changed
	 */
	private void fireDirectoryUpdate() {
		if (null == this.listeners) {
		} else {
			for (ConnectionListener listener : this.listeners) {
				listener.updateRemoteDirectory(this);
			}
		}
	}

	public boolean login(String user, String pass) {
		return true;
	}

	private void fireProgressUpdate(String file, String type, int bytes) {
		if (null == this.listeners) {
		} else {
			for (ConnectionListener listener : this.listeners) {
				if (this.shortProgress && Settings.shortProgress) {
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

	public LocalDateTime[] sortDates() {
		if (!this.dateVector.isEmpty()) {
			return (LocalDateTime[]) this.dateVector.toArray();
		} else {
			return null;
		}
	}

	public boolean rename(String file, String to) {
		if (StringUtils.isRelative(file)) {
			file = this.pwd + file;
		}

		file = file.replace('\\', '/');

		File f = new File(file);

		if (StringUtils.isRelative(to)) {
			to = this.pwd + to;
		}

		to = to.replace('\\', '/');

		return f.renameTo(new File(to));
	}
}
