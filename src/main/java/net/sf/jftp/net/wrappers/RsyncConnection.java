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


import com.github.fracpete.rsync4j.SshPass;
import com.github.fracpete.rsync4j.RSync;
import com.github.fracpete.rsync4j.core.Binaries;
import com.github.fracpete.processoutput4j.output.ConsoleOutputProcessOutput;

public class RsyncConnection implements net.sf.jftp.net.BasicConnection {
	public static final int buffer = 128000;
	private final boolean dummy = false;
	private String url = "";
	private String path = "";
	private String pwd = "";
	private java.util.Vector listeners = new java.util.Vector();
	private String[] files;
	private String[] size = new String[0];
	private int[] perms = null;
	//private NfsHandler handler = new NfsHandler();
	private String baseFile;
	private int fileCount;

	public RsyncConnection(final String url, final String utmp, final String ptmp, final int port, final String dtmp, final String ltmp) {
		this.url = url;

		String host = url.substring(6);

		final int x = host.indexOf("/");

		if (x >= 0) {
			host = host.substring(0, x);
		}

		net.sf.jftp.system.logging.Log.out("rsync host is: " + host);
	}

	public String[] getExports() throws Exception {
		final com.sun.xfile.XFile xf = new com.sun.xfile.XFile(url);
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

			final com.sun.xfile.XFile f = new com.sun.xfile.XFile(tmp);

			if (!f.getAbsolutePath().equals(f.getCanonicalPath())) {
				net.sf.jftp.system.logging.Log.debug("WARNING: Skipping symlink, remove failed.");
				net.sf.jftp.system.logging.Log.debug("This is necessary to prevent possible data loss when removing those symlinks.");

				return -1;
			}

			if (f.exists() && f.isDirectory()) {
				this.cleanLocalDir(tmp);
			}

			//System.out.println(tmp);
			if (!f.delete()) {
				return -1;
			} else {
				return 1;
			}
		} catch (final java.io.IOException ex) {
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

		//String remoteDir = StringUtils.removeStart(dir,path);
		//System.out.println(">>> " + dir);
		final com.sun.xfile.XFile f2 = new com.sun.xfile.XFile(dir);
		final String[] tmp = f2.list();

		if (tmp == null) {
			return;
		}

		for (final String s : tmp) {
			final com.sun.xfile.XFile f3 = new com.sun.xfile.XFile(dir + s);

			if (f3.isDirectory()) {
				//System.out.println(dir);
				this.cleanLocalDir(dir + s);
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

		final java.io.File f = new java.io.File(dirName);

		final boolean x = f.mkdir();
		this.fireDirectoryUpdate();

		return x;
	}

	public void list() throws java.io.IOException {
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

		final java.io.File f = new java.io.File(p);

		if (f.exists()) {
			try {
				path = f.getCanonicalPath();
				path = path.replace('\\', '/');

				if (!path.endsWith("/")) {
					path = path + "/";
				}

			} catch (final java.io.IOException ex) {
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
			final com.sun.xfile.XFile f = new com.sun.xfile.XFile(dir);
			files = f.list();
		}

		if (files == null) {
			return new String[0];
		}

		size = new String[files.length];
		perms = new int[files.length];

		int accessible = 0;

		for (int i = 0; i < files.length; i++) {
			final com.sun.xfile.XFile f2 = new com.sun.xfile.XFile(dir + files[i]);

			if (f2.isDirectory() && !files[i].endsWith("/")) {
				files[i] = files[i] + "/";
			}

			size[i] = "" + f2.length();

			if (f2.canWrite()) {
				accessible = net.sf.jftp.net.FtpConnection.W;
			} else if (f2.canRead()) {
				accessible = net.sf.jftp.net.FtpConnection.R;
			} else {
				accessible = net.sf.jftp.net.FtpConnection.DENIED;
			}

			perms[i] = accessible;

			//System.out.println(pwd+files[i] +" : " +accessible + " : " + size[i]);
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

	@Override
	public int download(final String file) {
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

			//System.out.println("transfer: " + file + ", " + getLocalPath() + outfile);
			this.work(path + outfile, file);
			this.fireActionFinished(this);
		}

		return 0;
	}

	private void uploadDir(final String dir, final String out) {
		System.out.println(dir + "," + out);
//		try {
//			//System.out.println("uploadDir: " + dir + "," + out);
//			isDirUpload = true;
//			fileCount = 0;
//			shortProgress = true;
//			baseFile = net.sf.jftp.system.StringUtils.getDir(dir);
//
//			java.io.File f2 = new java.io.File(out);
//			String[] tmp = f2.list();
//
//			if (tmp == null) {
//				return;
//			}
//
//			com.sun.xfile.XFile fx = new com.sun.xfile.XFile(dir);
//			fx.mkdir();
//
//			for (int i = 0; i < tmp.length; i++) {
//				tmp[i] = tmp[i].replace('\\', '/');
//
//				//System.out.println("1: " + dir+tmp[i] + ", " + out +tmp[i]);
//				java.io.File f3 = new java.io.File(out + tmp[i]);
//
//				if (f3.isDirectory()) {
//					if (!tmp[i].endsWith("/")) {
//						tmp[i] = tmp[i] + "/";
//					}
//
//					uploadDir(dir + tmp[i], out + tmp[i]);
//				} else {
//					fileCount++;
//					fireProgressUpdate(baseFile, net.sf.jftp.net.DataConnection.PUTDIR + ":" + fileCount, -1);
//					work(out + tmp[i], dir + tmp[i]);
//				}
//			}
//
//			fireProgressUpdate(baseFile, net.sf.jftp.net.DataConnection.DFINISHED + ":" + fileCount, -1);
//		} catch (Exception ex) {
//			ex.printStackTrace();
//
//			//System.out.println(dir + ", " + out);
//			net.sf.jftp.system.logging.Log.debug("Transfer error: " + ex);
//			fireProgressUpdate(baseFile, net.sf.jftp.net.DataConnection.FAILED + ":" + fileCount, -1);
//		}
//
//		isDirUpload = false;
//		shortProgress = true;
	}

	private void work(final String file, final String outfile) {
		final java.io.BufferedOutputStream out = null;
		final java.io.BufferedInputStream in = null;


	}

	private void update(final String file, final String type, final int bytes) {
		if (listeners == null) {
		} else {
			for (int i = 0; i < listeners.size(); i++) {
				final net.sf.jftp.net.ConnectionListener listener = (net.sf.jftp.net.ConnectionListener) listeners.elementAt(i);
				listener.updateProgress(file, type, bytes);
			}
		}
	}

	public void addConnectionListener(final net.sf.jftp.net.ConnectionListener l) {
		listeners.add(l);
	}

	public void setConnectionListeners(final java.util.Vector l) {
		listeners = l;
	}

	/**
	 * remote directory has changed
	 */
	public void fireDirectoryUpdate() {
		if (listeners == null) {
		} else {
			for (int i = 0; i < listeners.size(); i++) {
				((net.sf.jftp.net.ConnectionListener) listeners.elementAt(i)).updateRemoteDirectory(this);
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
				final net.sf.jftp.net.ConnectionListener listener = (net.sf.jftp.net.ConnectionListener) listeners.elementAt(i);

				final boolean shortProgress = false;
				if (shortProgress && net.sf.jftp.config.Settings.shortProgress) {
					final boolean isDirUpload = false;
					if (type.startsWith(net.sf.jftp.net.DataConnection.DFINISHED)) {
						listener.updateProgress(baseFile, net.sf.jftp.net.DataConnection.DFINISHED + ":" + fileCount, bytes);
					} else if (isDirUpload) {
						listener.updateProgress(baseFile, net.sf.jftp.net.DataConnection.PUTDIR + ":" + fileCount, bytes);
					} else {
						listener.updateProgress(baseFile, net.sf.jftp.net.DataConnection.GETDIR + ":" + fileCount, bytes);
					}
				} else {
					listener.updateProgress(file, type, bytes);
				}
			}
		}
	}

	public void fireActionFinished(final net.sf.jftp.net.wrappers.RsyncConnection con) {
		if (listeners == null) {
		} else {
			for (int i = 0; i < listeners.size(); i++) {
				((net.sf.jftp.net.ConnectionListener) listeners.elementAt(i)).actionFinished(con);
			}
		}
	}

	public int upload(String file, final java.io.InputStream i) {
		java.io.BufferedInputStream in = null;
		java.io.BufferedOutputStream out = null;

		try {
			file = this.toNFS(file);

			out = new java.io.BufferedOutputStream(new com.sun.xfile.XFileOutputStream(file));
			in = new java.io.BufferedInputStream(i);

			final byte[] buf = new byte[net.sf.jftp.net.wrappers.RsyncConnection.buffer];
			int len = 0;
			int reallen = 0;

			while (true) {
				len = in.read(buf);

				if (len == java.io.StreamTokenizer.TT_EOF) {
					break;
				}

				out.write(buf, 0, len);
				reallen += len;

				this.fireProgressUpdate(net.sf.jftp.system.StringUtils.getFile(file), net.sf.jftp.net.DataConnection.PUT, reallen);
			}

			this.fireProgressUpdate(file, net.sf.jftp.net.DataConnection.FINISHED, -1);
		} catch (final java.io.IOException ex) {
			net.sf.jftp.system.logging.Log.debug("Error with file IO (" + ex + ")!");
			this.fireProgressUpdate(file, net.sf.jftp.net.DataConnection.FAILED, -1);

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

	public java.io.InputStream getDownloadInputStream(String file) {
		file = this.toNFS(file);
		net.sf.jftp.system.logging.Log.debug(file);

		try {
			return new java.io.BufferedInputStream(new com.sun.xfile.XFileInputStream(file));
		} catch (final Exception ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug(ex + " @NfsConnection::getDownloadInputStream");

			return null;
		}
	}

	public java.util.Date[] sortDates() {
		return null;
	}

	public boolean rename(final String from, final String to) {
		net.sf.jftp.system.logging.Log.debug("Not implemented!");

		return false;
	}

	public void transfer(final String ltmp, final String htmp, final String dtmp, final String utmp, final String ptmp) {
		final String sourcePath = ltmp;
		final String destinationUserHost = utmp + "@" + htmp;
		final String destinationPath = dtmp;
		final String password = ptmp;

		final SshPass pass = new SshPass().password(password);

		final String source = sourcePath;
		final String destination = destinationUserHost + ":" + destinationPath;

		RSync rsync = null;
		try {
			rsync = new RSync().recursive(true).sshPass(pass).source(source).destination(destination).verbose(true).rsh(Binaries.sshBinary() + " -o StrictHostKeyChecking=no");

			final ConsoleOutputProcessOutput output = new ConsoleOutputProcessOutput();
			output.monitor(rsync.builder());
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}
