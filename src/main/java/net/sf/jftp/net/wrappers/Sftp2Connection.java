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

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;
import net.sf.jftp.net.BasicConnection;
import net.sf.jftp.net.ConnectionListener;
import net.sf.jftp.net.DataConnection;
import net.sf.jftp.net.FtpConnection;
import net.sf.jftp.net.Transfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

public class Sftp2Connection implements BasicConnection {
	public static final int smbBuffer = 32000;
	private final String host;
	private String path = "";
	private String pwd = "/";
	private Vector listeners = new Vector();
	private String[] size = new String[0];
	private int[] perms = null;
	private String user;
	private String pass;
	private String baseFile;
	private int fileCount;
	private boolean isDirUpload = false;
	private boolean shortProgress = false;
	private int port = 22;
	private boolean connected = false;
	private String keyfile = null;
	private Session session;
	private ChannelSftp channel;

	public Sftp2Connection(final String host, final String port, final String keyfile) {
		this.host = host;
		this.port = Integer.parseInt(port);
		this.keyfile = keyfile;

		net.sf.jftp.system.logging.Log.out("Using JSch wrapper...");
	}

	private boolean login() {
		try {
			final JSch jsch = new JSch();
			if (keyfile != null) {
				jsch.addIdentity(keyfile);
			}
			session = jsch.getSession(user, host, this.port);
			final UserInfo ui = new MyUserInfo(pass);
			session.setUserInfo(ui);
			session.connect();


			channel = (ChannelSftp) session.openChannel("sftp");
			channel.connect();

			net.sf.jftp.system.logging.Log.debug("Host: " + host + ":" + port);

			pwd = channel.pwd();

			connected = true;
			return true;
		} catch (final Exception ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug("Error: " + ex);

			return false;
		}
	}

	public int removeFileOrDir(String file) {
		file = this.toSFTP(file);

		try {

			if (!file.endsWith("/")) {
				net.sf.jftp.system.logging.Log.out(">>>>>>>> remove file: " + file);
				channel.rm(file);
			} else {
				net.sf.jftp.system.logging.Log.out(">>>>>>>> remove dir: " + file);
				this.cleanSftpDir(file);
				channel.rmdir(file);
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug("Removal failed (" + ex + ").");
			ex.printStackTrace();

			return -1;
		}

		return 1;
	}

	private void cleanSftpDir(final String dir) throws Exception {
		net.sf.jftp.system.logging.Log.out(">>>>>>>> cleanSftpDir: " + dir);

		final Vector v = channel.ls(dir);

		final String[] tmp = new String[v.size()];
		final Enumeration e = v.elements();
		int x = 0;

		while (e.hasMoreElements()) {
			final LsEntry entry = ((LsEntry) e.nextElement());
			tmp[x] = entry.getFilename();

			//Log.out("sftp delete: " + tmp[x]);
			if (entry.getAttrs().isDir() && !tmp[x].endsWith("/")) {
				tmp[x] = tmp[x] + "/";
			}

			x++;
		}

		if (tmp == null) {
			return;
		}

		for (final String s : tmp) {
			if (s.equals("./") || s.equals("../")) {
				continue;
			}

			net.sf.jftp.system.logging.Log.out(">>>>>>>> remove file/dir: " + dir + s);

			if (s.endsWith("/")) {
				this.cleanSftpDir(dir + s);
				channel.rmdir(dir + s);
			} else {
				channel.rm(dir + s);
			}
		}
	}

	public void sendRawCommand(final String cmd) {
	}

	public void disconnect() {
		try {
			channel.disconnect();
			session.disconnect();
		} catch (final Exception e) {
			e.printStackTrace();
			net.sf.jftp.system.logging.Log.debug("Sftp2Connection.disconnect()" + e);
		}

		connected = false;
	}

	public boolean isConnected() {
		return connected;
	}

	public String getPWD() {
		//Log.debug("PWD: " + pwd);
		return this.toSFTPDir(pwd);
	}

	public boolean mkdir(String dirName) {
		try {
			if (!dirName.endsWith("/")) {
				dirName = dirName + "/";
			}

			dirName = this.toSFTP(dirName);

			channel.mkdir(dirName);

			this.fireDirectoryUpdate();

			return true;
		} catch (final Exception ex) {
			net.sf.jftp.system.logging.Log.debug("Failed to create directory (" + ex + ").");

			return false;
		}
	}

	public void list() throws IOException {
	}

	public boolean chdir(final String p) {
		return this.chdir(p, true);
	}

	public boolean chdir(final String p, final boolean refresh) {
		String tmp = this.toSFTP(p);

		try {
			if (!tmp.endsWith("/")) {
				tmp = tmp + "/";
			}
			if (tmp.endsWith("../")) {
				return this.cdup();
			}

			System.out.println("sftp path: " + tmp + ", chan: " + channel);
			channel.cd(tmp);

			pwd = tmp;

			//Log.debug("chdir: " + getPWD());
			if (refresh) {
				this.fireDirectoryUpdate();
			}
			return true;
		} catch (final Exception ex) {
			ex.printStackTrace();

			//System.out.println(tmp);
			net.sf.jftp.system.logging.Log.debug("Could not change directory (" + ex + ").");

			return false;
		}
	}

	public boolean cdup() {
		String tmp = pwd;

		if (pwd.endsWith("/")) {
			tmp = pwd.substring(0, pwd.lastIndexOf("/"));
		}

		return this.chdir(tmp.substring(0, tmp.lastIndexOf("/") + 1));
	}

	public boolean chdirNoRefresh(final String p) {
		return this.chdir(p, false);
	}

	public String getLocalPath() {
		return path;
	}

	public boolean setLocalPath(String p) {
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
		try {
			System.out.println(pwd);
			final Vector v = channel.ls(pwd);

			final String[] tmp = new String[v.size()];
			final String[] files = new String[tmp.length];
			size = new String[tmp.length];
			perms = new int[tmp.length];

			final Enumeration e = v.elements();
			int x = 0;

			while (e.hasMoreElements()) {
				final LsEntry entry = ((LsEntry) e.nextElement());
				tmp[x] = entry.getFilename();

				size[x] = "" + entry.getAttrs().getSize();

				//Log.debug("Perms: "+entry.getAttrs().getPermissionsString());
                
                /*
                if(!entry.getAttrs().getPermissionsString())
                {
                    perms[x] = FtpConnection.DENIED;
                }
                else
                {*/
				perms[x] = FtpConnection.R;
				//}

				//Log.debugRaw(".");
				if (entry.getAttrs().isDir() && !tmp[x].endsWith("/")) {
					tmp[x] = tmp[x] + "/";
				}

				x++;
			}

			System.arraycopy(tmp, 0, files, 0, tmp.length);

			return files;
		} catch (final Exception ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug(" Error while listing directory: " + ex);
			return new String[0];
		}
	}

	public String[] sortSize() {
		return size;
	}

	public int[] getPermissions() {
		return perms;
	}

	public int handleUpload(final String f) {
		if (net.sf.jftp.config.Settings.getEnableSftpMultiThreading()) {

			final Sftp2Transfer t = new Sftp2Transfer(path, this.getPWD(), f, user, pass, listeners, Transfer.UPLOAD, keyfile, host, "" + port);
		} else {
			this.upload(f);
		}

		return 0;
	}

	public int handleDownload(final String f) {
		if (net.sf.jftp.config.Settings.getEnableSftpMultiThreading()) {
			final Sftp2Transfer t = new Sftp2Transfer(path, this.getPWD(), f, user, pass, listeners, Transfer.DOWNLOAD, keyfile, host, "" + port);
		} else {
			this.download(f);
		}

		return 0;
	}

	public int upload(final String f) {
		final String file = this.toSFTP(f);

		if (file.endsWith("/")) {
			final String out = net.sf.jftp.system.StringUtils.getDir(file);
			this.uploadDir(file, path + out);
			this.fireActionFinished(this);
		} else {
			final String outfile = net.sf.jftp.system.StringUtils.getFile(file);

			//System.out.println("transfer: " + file + ", " + getLocalPath() + outfile);
			this.work(path + outfile, file, true);
			this.fireActionFinished(this);
		}

		return 0;
	}

	public int download(final String f) {
		final String file = this.toSFTP(f);

		if (file.endsWith("/")) {
			final String out = net.sf.jftp.system.StringUtils.getDir(file);
			this.downloadDir(file, path + out);
			this.fireActionFinished(this);
		} else {
			final String outfile = net.sf.jftp.system.StringUtils.getFile(file);

			//System.out.println("transfer: " + file + ", " + getLocalPath() + outfile);
			this.work(file, path + outfile, false);
			this.fireActionFinished(this);
		}

		return 0;
	}

	private void downloadDir(final String dir, final String out) {
		try {
			//System.out.println("downloadDir: " + dir + "," + out);
			fileCount = 0;
			shortProgress = true;
			baseFile = net.sf.jftp.system.StringUtils.getDir(dir);

			final Vector v = channel.ls(dir);

			final String[] tmp = new String[v.size()];
			final Enumeration e = v.elements();
			int x = 0;

			while (e.hasMoreElements()) {
				final LsEntry entry = ((LsEntry) e.nextElement());
				tmp[x] = entry.getFilename();

				if (entry.getAttrs().isDir() && !tmp[x].endsWith("/")) {
					tmp[x] = tmp[x] + "/";
				}

				x++;
			}

			final File fx = new File(out);
			fx.mkdir();

			for (int i = 0; i < tmp.length; i++) {
				if (tmp[i].equals("./") || tmp[i].equals("../")) {
					continue;
				}

				tmp[i] = tmp[i].replace('\\', '/');

				//System.out.println("1: " + dir+tmp[i] + ", " + out +tmp[i]);

				if (tmp[i].endsWith("/")) {
					if (!tmp[i].endsWith("/")) {
						tmp[i] = tmp[i] + "/";
					}

					this.downloadDir(dir + tmp[i], out + tmp[i]);
				} else {
					fileCount++;
					this.fireProgressUpdate(baseFile, DataConnection.GETDIR + ":" + fileCount, -1);
					this.work(dir + tmp[i], out + tmp[i], false);
				}

			}

			//System.out.println("enddir");

			this.fireProgressUpdate(baseFile, DataConnection.DFINISHED + ":" + fileCount, -1);
		} catch (final Exception ex) {
			ex.printStackTrace();
			System.out.println(dir + ", " + out);
			net.sf.jftp.system.logging.Log.debug("Transfer error: " + ex);
			this.fireProgressUpdate(baseFile, DataConnection.FAILED + ":" + fileCount, -1);
		}

		shortProgress = false;
	}

	private void uploadDir(final String dir, final String out) {
		try {
			//System.out.println("uploadDir: " + dir + "," + out);
			isDirUpload = true;
			fileCount = 0;
			shortProgress = true;
			baseFile = net.sf.jftp.system.StringUtils.getDir(dir);

			final File f2 = new File(out);
			final String[] tmp = f2.list();

			if (tmp == null) {
				return;
			}

			channel.mkdir(dir);
			//channel.chmod(744, dir);

			for (int i = 0; i < tmp.length; i++) {
				if (tmp[i].equals("./") || tmp[i].equals("../")) {
					continue;
				}

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
					this.work(out + tmp[i], dir + tmp[i], true);
				}
			}

			this.fireProgressUpdate(baseFile, DataConnection.DFINISHED + ":" + fileCount, -1);
		} catch (final Exception ex) {
			ex.printStackTrace();
			System.out.println(dir + ", " + out);
			net.sf.jftp.system.logging.Log.debug("Transfer error: " + ex);
			this.fireProgressUpdate(baseFile, DataConnection.FAILED + ":" + fileCount, -1);
		}

		isDirUpload = false;
		shortProgress = true;
	}

	private String toSFTP(final String f) {
		String file;

		if (f.startsWith("/")) {
			file = f;
		} else {
			file = this.getPWD() + f;
		}

		file = file.replace('\\', '/');

		//System.out.println("file: "+file);
		return file;
	}

	private String toSFTPDir(final String f) {
		String file;

		if (f.startsWith("/")) {
			file = f;
		} else {
			file = pwd + f;
		}

		file = file.replace('\\', '/');

		if (!file.endsWith("/")) {
			file = file + "/";
		}

		//System.out.println("file: "+file);
		return file;
	}

	private void work(final String file, final String outfile, final boolean up) {
		BufferedInputStream in = null;
		BufferedOutputStream out = null;

		//System.out.println("work");

		try {
			boolean outflag = false;

			if (up) {
				in = new BufferedInputStream(new FileInputStream(file));
			} else {
				in = new BufferedInputStream(channel.get(file));
			}

			if (up) {
				outflag = true;

				try {
					channel.rm(outfile);
				} catch (final Exception ex) {

				}
				out = new BufferedOutputStream(channel.put(outfile));
			} else {
				out = new BufferedOutputStream(new FileOutputStream(outfile));
			}

			//System.out.println("out: " + outfile + ", in: " + file);
			final byte[] buf = new byte[net.sf.jftp.net.wrappers.Sftp2Connection.smbBuffer];
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
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug("Error with file IO (" + ex + ")!");
			this.fireProgressUpdate(file, DataConnection.FAILED, -1);
		} catch (final SftpException ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug("Error with SFTP IO (" + ex + ")!");
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

	public boolean rename(String oldName, String newName) {
		try {
			oldName = this.toSFTP(oldName);
			newName = this.toSFTP(newName);

			channel.rename(oldName, newName);

			return true;
		} catch (final Exception ex) {
			ex.printStackTrace();

			net.sf.jftp.system.logging.Log.debug("Could rename file (" + ex + ").");

			return false;
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

	public boolean login(final String user, final String pass) {
		this.user = user;
		this.pass = pass;

		if (!this.login()) {
			net.sf.jftp.system.logging.Log.debug("Login failed.");

			return false;
		} else {
			net.sf.jftp.system.logging.Log.debug("Authed successfully.");

			//if(!chdir(getPWD())) chdir("/");
		}

		return true;
	}

	/**
	 * progress update
	 */
	public void fireProgressUpdate(final String file, final String type, final int bytes) {
		if (listeners == null) {
			return;
		}

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

	public void fireActionFinished(final Sftp2Connection con) {
		if (listeners == null) {
		} else {
			for (int i = 0; i < listeners.size(); i++) {
				((ConnectionListener) listeners.elementAt(i)).actionFinished(con);
			}
		}
	}

	public int upload(String file, final InputStream i) {
		BufferedOutputStream out = null;
		BufferedInputStream in = null;

		try {
			file = this.toSFTP(file);

			out = new BufferedOutputStream(channel.put(file));
			in = new BufferedInputStream(i);

			//Log.debug(getLocalPath() + ":" + file+ ":"+getPWD());
			final byte[] buf = new byte[net.sf.jftp.net.wrappers.Sftp2Connection.smbBuffer];
			int len = 0;
			int reallen = 0;

			while (true) {
				len = in.read(buf);

				//System.out.print(".");
				if (len == StreamTokenizer.TT_EOF) {
					break;
				}

				out.write(buf, 0, len);
				reallen += len;

				this.fireProgressUpdate(net.sf.jftp.system.StringUtils.getFile(file), DataConnection.PUT, reallen);
			}

			//channel.chmod(744, file);

			this.fireProgressUpdate(file, DataConnection.FINISHED, -1);

			return 0;
		} catch (final IOException ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug("Error with file IO (" + ex + ")!");
			this.fireProgressUpdate(file, DataConnection.FAILED, -1);

			return -1;
		} catch (final SftpException ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug("Error with file SFTP IO (" + ex + ")!");
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
	}

	public InputStream getDownloadInputStream(final String file) {
		try {

			return channel.get(file);
		} catch (final SftpException ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug(ex + " @Sftp2Connection::getDownloadInputStream");

			return null;
		}
	}

	public Date[] sortDates() {
		return null;
	}
}

class MyUserInfo implements UserInfo {

	final String password;

	public MyUserInfo(final String pass) {
		this.password = pass;
	}

	public String getPassword() {
		return password;
	}

	public boolean promptYesNo(final String str) {
		return true;
	}

	public String getPassphrase() {
		return password;
	}

	public boolean promptPassphrase(final String message) {
		return true;
	}

	public boolean promptPassword(final String message) {
		return true;
	}

	public void showMessage(final String message) {
		//JOptionPane.showMessageDialog(null, message);
	}
}
