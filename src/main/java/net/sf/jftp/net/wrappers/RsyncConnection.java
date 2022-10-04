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


import com.github.fracpete.processoutput4j.output.ConsoleOutputProcessOutput;
import com.github.fracpete.rsync4j.RSync;
import com.github.fracpete.rsync4j.SshPass;
import com.github.fracpete.rsync4j.core.Binaries;

public class RsyncConnection implements net.sf.jftp.net.BasicConnection {
	public static final int buffer = 128000;
	private final boolean dummy = false;
	private String url = "";
	private String path = "";
	private String pwd = "";
	private java.util.List<net.sf.jftp.net.ConnectionListener> listeners = new java.util.ArrayList<>();
	private String[] files;
	private String[] size = new String[0];
	private int[] perms;
	//private NfsHandler handler = new NfsHandler();
	private String baseFile;
	private int fileCount;

	public RsyncConnection(String url, String utmp, String ptmp, int port, String dtmp, String ltmp) {
		super();
		this.url = url;

		String host = url.substring(6);

		int x = host.indexOf('/');

		if (0 <= x) {
			host = host.substring(0, x);
		}

		net.sf.jftp.system.logging.Log.out("rsync host is: " + host);
	}

	public String[] getExports() throws Exception {
		com.sun.xfile.XFile xf = new com.sun.xfile.XFile(this.url);
		com.sun.nfs.XFileExtensionAccessor nfsx = (com.sun.nfs.XFileExtensionAccessor) xf.getExtensionAccessor();

		String[] tmp = nfsx.getExports();

		if (null == tmp) {
			return new String[0];
		}

		for (String s : tmp) {
			net.sf.jftp.system.logging.Log.out("nfs export found: " + s);
		}

		return tmp;
	}

	public int removeFileOrDir(String file) {
		try {
			String tmp = this.toNFS(file);

			com.sun.xfile.XFile f = new com.sun.xfile.XFile(tmp);

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
		} catch (java.io.IOException ex) {
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
		com.sun.xfile.XFile f2 = new com.sun.xfile.XFile(dir);
		String[] tmp = f2.list();

		if (null == tmp) {
			return;
		}

		for (String s : tmp) {
			com.sun.xfile.XFile f3 = new com.sun.xfile.XFile(dir + s);

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

	public void sendRawCommand(String cmd) {
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
			tmp = this.pwd.substring(0, this.pwd.lastIndexOf('/'));
		}

		return this.chdir(tmp.substring(0, tmp.lastIndexOf('/') + 1));
	}

	public boolean mkdir(String dirName) {
		if (!dirName.endsWith("/")) {
			dirName = dirName + "/";
		}

		dirName = this.toNFS(dirName);

		java.io.File f = new java.io.File(dirName);

		boolean x = f.mkdir();
		this.fireDirectoryUpdate();

		return x;
	}

	public void list() throws java.io.IOException {
	}

	public boolean chdir(String p) {
		return this.chdir(p, true);
	}

	public boolean chdir(String p, boolean refresh) {
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

	private int check(String url) {
		int x = 0;

		for (int j = 0; j < url.length(); j++) {
			if ('/' == url.charAt(j)) {
				x++;
			}
		}

		return x;
	}

	public boolean chdirNoRefresh(String p) {

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

		java.io.File f = new java.io.File(p);

		if (f.exists()) {
			try {
				this.path = f.getCanonicalPath();
				this.path = this.path.replace('\\', '/');

				if (!this.path.endsWith("/")) {
					this.path = this.path + "/";
				}

			} catch (java.io.IOException ex) {
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
		String dir = this.getPWD();

		if (3 == this.check(this.toNFS(dir))) {
			try {
				this.files = this.getExports();
			} catch (Exception ex) {
				net.sf.jftp.system.logging.Log.debug("Can not list exports:" + ex);
				ex.printStackTrace();
			}
		} else {
			com.sun.xfile.XFile f = new com.sun.xfile.XFile(dir);
			this.files = f.list();
		}

		if (null == this.files) {
			return new String[0];
		}

		this.size = new String[this.files.length];
		this.perms = new int[this.files.length];

		int accessible = 0;

		for (int i = 0; i < this.files.length; i++) {
			com.sun.xfile.XFile f2 = new com.sun.xfile.XFile(dir + this.files[i]);

			if (f2.isDirectory() && !this.files[i].endsWith("/")) {
				this.files[i] = this.files[i] + "/";
			}

			this.size[i] = "" + f2.length();

			if (f2.canWrite()) {
				accessible = net.sf.jftp.net.FtpConstants.W;
			} else if (f2.canRead()) {
				accessible = net.sf.jftp.net.FtpConstants.R;
			} else {
				accessible = net.sf.jftp.net.FtpConstants.DENIED;
			}

			this.perms[i] = accessible;

			//System.out.println(pwd+files[i] +" : " +accessible + " : " + size[i]);
		}

		return this.files;
	}

	public String[] sortSize() {
		return this.size;
	}

	public int[] getPermissions() {
		return this.perms;
	}

	public int handleUpload(String f) {
		this.upload(f);

		return 0;
	}

	@Override
	public int download(String file) {
		return 0;
	}

	public int handleDownload(String f) {
		this.download(f);

		return 0;
	}

	public int upload(String f) {
		String file = this.toNFS(f);

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

	private void uploadDir(String dir, String out) {
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

	private void work(String file, String outfile) {
		java.io.BufferedOutputStream out = null;
		java.io.BufferedInputStream in = null;


	}

	private void update(String file, String type, int bytes) {
		if (null == this.listeners) {
		} else {
			for (int i = 0; i < this.listeners.size(); i++) {
				net.sf.jftp.net.ConnectionListener listener = (net.sf.jftp.net.ConnectionListener) this.listeners.get(i);
				listener.updateProgress(file, type, bytes);
			}
		}
	}

	public void addConnectionListener(net.sf.jftp.net.ConnectionListener l) {
		this.listeners.add(l);
	}

	public void setConnectionListeners(java.util.List<net.sf.jftp.net.ConnectionListener> l) {
		this.listeners = l;
	}

	/**
	 * remote directory has changed
	 */
	public void fireDirectoryUpdate() {
		if (null == this.listeners) {
		} else {
			for (int i = 0; i < this.listeners.size(); i++) {
				((net.sf.jftp.net.ConnectionListener) this.listeners.get(i)).updateRemoteDirectory(this);
			}
		}
	}

	/**
	 * progress update
	 */
	public void fireProgressUpdate(String file, String type, int bytes) {
		//System.out.println(listener);
		if (null == this.listeners) {
		} else {
			for (int i = 0; i < this.listeners.size(); i++) {
				net.sf.jftp.net.ConnectionListener listener = (net.sf.jftp.net.ConnectionListener) this.listeners.get(i);

				final boolean shortProgress = false;
				if (shortProgress && net.sf.jftp.config.Settings.shortProgress) {
					final boolean isDirUpload = false;
					if (type.startsWith(net.sf.jftp.net.DataConnection.DFINISHED)) {
						listener.updateProgress(this.baseFile, net.sf.jftp.net.DataConnection.DFINISHED + ":" + this.fileCount, bytes);
					} else if (isDirUpload) {
						listener.updateProgress(this.baseFile, net.sf.jftp.net.DataConnection.PUTDIR + ":" + this.fileCount, bytes);
					} else {
						listener.updateProgress(this.baseFile, net.sf.jftp.net.DataConnection.GETDIR + ":" + this.fileCount, bytes);
					}
				} else {
					listener.updateProgress(file, type, bytes);
				}
			}
		}
	}

	public void fireActionFinished(net.sf.jftp.net.wrappers.RsyncConnection con) {
		if (null == this.listeners) {
		} else {
			for (int i = 0; i < this.listeners.size(); i++) {
				((net.sf.jftp.net.ConnectionListener) this.listeners.get(i)).actionFinished(con);
			}
		}
	}

	public int upload(String file, java.io.InputStream i) {
		java.io.BufferedInputStream in = null;
		java.io.BufferedOutputStream out = null;

		try {
			file = this.toNFS(file);

			out = new java.io.BufferedOutputStream(new com.sun.xfile.XFileOutputStream(file));
			in = new java.io.BufferedInputStream(i);

			byte[] buf = new byte[buffer];
			int len = 0;
			int reallen = 0;

			while (true) {
				len = in.read(buf);

				if (java.io.StreamTokenizer.TT_EOF == len) {
					break;
				}

				out.write(buf, 0, len);
				reallen += len;

				this.fireProgressUpdate(net.sf.jftp.system.StringUtils.getFile(file), net.sf.jftp.net.DataConnection.PUT, reallen);
			}

			this.fireProgressUpdate(file, net.sf.jftp.net.DataConnection.FINISHED, -1);
		} catch (java.io.IOException ex) {
			net.sf.jftp.system.logging.Log.debug("Error with file IO (" + ex + ")!");
			this.fireProgressUpdate(file, net.sf.jftp.net.DataConnection.FAILED, -1);

			return -1;
		} finally {
			try {
				out.flush();
				out.close();
				in.close();
			} catch (Exception ex) {
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
		} catch (Exception ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug(ex + " @NfsConnection::getDownloadInputStream");

			return null;
		}
	}

	public java.util.Date[] sortDates() {
		return null;
	}

	public boolean rename(String from, String to) {
		net.sf.jftp.system.logging.Log.debug("Not implemented!");

		return false;
	}

	public void transfer(String ltmp, String htmp, String dtmp, String utmp, String ptmp) {
		String sourcePath = ltmp;
		String destinationUserHost = utmp + "@" + htmp;
		String destinationPath = dtmp;
		String password = ptmp;

		SshPass pass = new SshPass().password(password);

		String source = sourcePath;
		String destination = destinationUserHost + ":" + destinationPath;

		RSync rsync = null;
		try {
			rsync = new RSync().recursive(true).sshPass(pass).source(source).destination(destination).verbose(true).rsh(Binaries.sshBinary() + " -o StrictHostKeyChecking=no");

			ConsoleOutputProcessOutput output = new ConsoleOutputProcessOutput();
			output.monitor(rsync.builder());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
