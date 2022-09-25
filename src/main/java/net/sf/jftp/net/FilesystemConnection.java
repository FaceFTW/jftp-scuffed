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
	public static int filesystemBuffer = 128000;
	public Vector<Date> dateVector = new Vector<Date>();
	private String path = "";
	private String pwd = "";
	private Vector<ConnectionListener> listeners = new Vector<ConnectionListener>();
	private String[] files;
	private String[] size = new String[0];
	private int[] perms = null;
	private String baseFile;
	private int fileCount;
	private boolean shortProgress = false;

	public FilesystemConnection() {
	}

	public FilesystemConnection(String path, ConnectionListener l) {
		listeners.add(l);
		chdir(path);
	}

	public int removeFileOrDir(String file) {
		try {
			if ((file == null) || file.equals("")) {
				return -1;
			}

			String tmp = file;

			if (net.sf.jftp.system.StringUtils.isRelative(file)) {
				tmp = getPWD() + file;
			}

			File f = new File(tmp);

			if (!f.getAbsolutePath().equals(f.getCanonicalPath())) {
				//Log.debug("WARNING: Symlink removed");//Skipping symlink, remove failed.");
				//Log.debug("This is necessary to prevent possible data loss when removing those symlinks.");
				//return -1;
				if (!f.delete()) {
					return -1;
				}
			}

			if (f.exists() && f.isDirectory()) {
				cleanLocalDir(tmp);
			}

			//System.out.println(tmp);
			if (!f.delete()) {
				net.sf.jftp.system.logging.Log.debug("Removal failed.");

				return -1;
			}
		} catch (IOException ex) {
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

			//String remoteDir = StringUtils.removeStart(dir,path);
			//System.out.println(">>> " + dir);
			File f2 = new File(dir);
			String[] tmp = f2.list();

			if (tmp == null) {
				return;
			}

			for (String s : tmp) {
				java.io.File f3 = new java.io.File(dir + s);

				if (!f3.getAbsolutePath().equals(f3.getCanonicalPath())) {
					//Log.debug("WARNING: Symlink remove");//Skipping symlink, remove may fail.");
					f3.delete();

					//Log.debug("This is necessary to prevent possible data loss when removing those symlinks.");
					//continue;
				}

				if (f3.isDirectory()) {
					//System.out.println(dir);
					cleanLocalDir(dir + s);
					f3.delete();
				} else {
					//System.out.println(dir+tmp[i]);
					f3.delete();
				}
			}
		} catch (IOException ex) {
			net.sf.jftp.system.logging.Log.debug("Error: " + ex);
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
		return pwd;
	}

	public boolean cdup() {
		return chdir(pwd.substring(0, pwd.lastIndexOf("/") + 1));
	}

	public boolean mkdir(String dirName) {
		if (net.sf.jftp.system.StringUtils.isRelative(dirName)) {
			dirName = getPWD() + dirName;
		}

		File f = new File(dirName);

		boolean x = f.mkdir();
		fireDirectoryUpdate();

		return x;
	}

	public void list() throws IOException {
	}

	public boolean chdir(String p) {
		String p2 = processPath(p);

		if (p2 == null) {
			return false;
		}

		File f = new File(p2);

		if (!f.exists() || !f.isDirectory() || !f.canRead()) {
			net.sf.jftp.system.logging.Log.debug("Access denied.");

			return false;
		}

		pwd = p2;

		fireDirectoryUpdate();

		return true;
	}

	public boolean chdirNoRefresh(String p) {
		String p2 = processPath(p);

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
		p = p.replace('\\', '/');

		//System.out.print("processPath 1: "+p);
		if (net.sf.jftp.system.StringUtils.isRelative(p)) {
			p = pwd + p;
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
		dateVector = new Vector<Date>();

		File f = new File(pwd);
		files = f.list();

		if (files == null) {
			return new String[0];
		}

		size = new String[files.length];
		perms = new int[files.length];

		int accessible = 0;

		for (int i = 0; i < files.length; i++) {
			File f2 = new File(pwd + files[i]);

			if (f2.isDirectory() && !files[i].endsWith("/")) {
				files[i] = files[i] + "/";
			}

			size[i] = "" + new File(pwd + files[i]).length();

			if (f2.canWrite()) {
				accessible = FtpConnection.W;
			} else if (f2.canRead()) {
				accessible = FtpConnection.R;
			} else {
				accessible = FtpConnection.DENIED;
			}

			perms[i] = accessible;

			//System.out.println(pwd+files[i] +" : " +accessible + " : " + size[i]);
			Date d = new Date(f2.lastModified());

			//System.out.println(d.toString());
			dateVector.add(d);
		}

		return files;
	}

	public String[] sortSize() {
		return size;
	}

	public int[] getPermissions() {
		return perms;
	}

	public int handleDownload(String file) {
		transfer(file);

		return 0;
	}

	public int handleUpload(String file) {
		transfer(file);

		return 0;
	}

	public int download(String file) {
		transfer(file);

		return 0;
	}

	public int upload(String file) {
		transfer(file);

		return 0;
	}

	private void transferDir(String dir, String out) {
		fileCount = 0;
		shortProgress = true;
		baseFile = net.sf.jftp.system.StringUtils.getDir(dir);

		File f2 = new File(dir);
		String[] tmp = f2.list();

		if (tmp == null) {
			return;
		}

		File fx = new File(out);

		if (!fx.mkdir()) {
			net.sf.jftp.system.logging.Log.debug("Can not create directory: " + out + " - already exist or permission denied?");
		}

		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = tmp[i].replace('\\', '/');

			//System.out.println("1: " + dir+tmp[i] + ", " + out +tmp[i]);
			File f3 = new File(dir + tmp[i]);

			if (f3.isDirectory()) {
				if (!tmp[i].endsWith("/")) {
					tmp[i] = tmp[i] + "/";
				}

				transferDir(dir + tmp[i], out + tmp[i]);
			} else {
				fireProgressUpdate(baseFile, DataConnection.GETDIR + ":" + fileCount, -1);
				work(dir + tmp[i], out + tmp[i]);
			}
		}

		fireProgressUpdate(baseFile, DataConnection.DFINISHED + ":" + fileCount, -1);
		shortProgress = false;
	}

	private void transfer(String file) {
		String out = net.sf.jftp.system.StringUtils.getDir(file);

		if (net.sf.jftp.system.StringUtils.isRelative(file)) {
			file = getPWD() + file;
		}

		file = file.replace('\\', '/');
		out = out.replace('\\', '/');

		String outfile = net.sf.jftp.system.StringUtils.getFile(file);

		if (file.endsWith("/")) {
			transferDir(file, getLocalPath() + out);

		} else {
			work(file, getLocalPath() + outfile);
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
				if (len == StreamTokenizer.TT_EOF) {
					break;
				}

				out.write(buf, 0, len);

				reallen += len;
				fireProgressUpdate(net.sf.jftp.system.StringUtils.getFile(file), DataConnection.GET, reallen);
			}

			out.flush();
			out.close();
			in.close();
			fireProgressUpdate(file, DataConnection.FINISHED, -1);
		} catch (IOException ex) {
			net.sf.jftp.system.logging.Log.debug("Error with file IO (" + ex + ")!");
			fireProgressUpdate(file, DataConnection.FAILED, -1);
		}
	}

	public int upload(String file, InputStream in) {
		if (net.sf.jftp.system.StringUtils.isRelative(file)) {
			file = getPWD() + file;
		}

		file = file.replace('\\', '/');

		try {
			work(new BufferedInputStream(in), file, file);

			return 0;
		} catch (Exception ex) {
			net.sf.jftp.system.logging.Log.debug("Error: " + ex);
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

				if (len == StreamTokenizer.TT_EOF) {
					break;
				}

				out.write(buf, 0, len);

				reallen += len;
				fireProgressUpdate(net.sf.jftp.system.StringUtils.getFile(file), DataConnection.GET, reallen);
			}

			out.flush();
			out.close();
			in.close();
			fireProgressUpdate(file, DataConnection.FINISHED, -1);
		} catch (IOException ex) {
			net.sf.jftp.system.logging.Log.debug("Error with file IO (" + ex + ")!");
			fireProgressUpdate(file, DataConnection.FAILED, -1);
		}
	}

	public InputStream getDownloadInputStream(String file) {
		if (net.sf.jftp.system.StringUtils.isRelative(file)) {
			file = getPWD() + file;
		}

		file = file.replace('\\', '/');

		try {
			return new FileInputStream(file);
		} catch (Exception ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug(ex + " @Filesystemconnection::getDownloadInputStream");

			return null;
		}
	}

	public void addConnectionListener(ConnectionListener l) {
		listeners.add(l);
		fireDirectoryUpdate();
	}

	public void setConnectionListeners(Vector<ConnectionListener> l) {
		listeners = l;
		fireDirectoryUpdate();
	}

	/**
	 * remote directory has changed
	 */
	public void fireDirectoryUpdate() {
		if (listeners == null) {
		} else {
			for (int i = 0; i < listeners.size(); i++) {
				listeners.elementAt(i).updateRemoteDirectory(this);
			}
		}
	}

	public boolean login(String user, String pass) {
		return true;
	}

	public void fireProgressUpdate(String file, String type, int bytes) {
		if (listeners == null) {
		} else {
			for (int i = 0; i < listeners.size(); i++) {
				ConnectionListener listener = listeners.elementAt(i);

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
		if (dateVector.size() > 0) {
			return (Date[]) dateVector.toArray();
		} else {
			return null;
		}
	}

	public boolean rename(String file, String to) {
		if (net.sf.jftp.system.StringUtils.isRelative(file)) {
			file = getPWD() + file;
		}

		file = file.replace('\\', '/');

		File f = new File(file);

		if (net.sf.jftp.system.StringUtils.isRelative(to)) {
			to = getPWD() + to;
		}

		to = to.replace('\\', '/');

		return f.renameTo(new File(to));
	}
}
