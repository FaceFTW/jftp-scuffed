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
import net.sf.jftp.config.Settings;
import net.sf.jftp.net.BasicConnection;
import net.sf.jftp.net.ConnectionListener;
import net.sf.jftp.net.DataConnection;
import net.sf.jftp.net.FtpConstants;
import net.sf.jftp.net.Transfer;
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
import java.util.ArrayList;
import java.util.List;

public class Sftp2Connection implements BasicConnection {
	private static final int smbBuffer = 32000;
	private final String host;
	private String path = "";
	private String pwd = "/";
	private List<ConnectionListener> listeners = new ArrayList<>();
	private String[] size = new String[0];
	private int[] perms;
	private String user;
	private String pass;
	private String baseFile;
	private int fileCount;
	private boolean isDirUpload;
	private boolean shortProgress;
	private int port = 22;
	private boolean connected;
	private String keyfile;
	private Session session;
	private ChannelSftp channel;

	public Sftp2Connection(String host, String port, String keyfile) {
		super();
		this.host = host;
		this.port = Integer.parseInt(port);
		this.keyfile = keyfile;

		Log.out("Using JSch wrapper...");
	}

	private boolean login() {
		try {
			JSch jsch = new JSch();
			if (null != this.keyfile) {
				jsch.addIdentity(this.keyfile);
			}
			this.session = jsch.getSession(this.user, this.host, this.port);
			UserInfo ui = new MyUserInfo(this.pass);
			this.session.setUserInfo(ui);
			this.session.connect();


			this.channel = (ChannelSftp) this.session.openChannel("sftp");
			this.channel.connect();

			Log.debug("Host: " + this.host + ":" + this.port);

			this.pwd = this.channel.pwd();

			this.connected = true;
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.debug("Error: " + ex);

			return false;
		}
	}

	public int removeFileOrDir(String file) {
		file = this.toSFTP(file);

		try {

			if (file.endsWith("/")) {
				Log.out(">>>>>>>> remove dir: " + file);
				this.cleanSftpDir(file);
				this.channel.rmdir(file);
			} else {
				Log.out(">>>>>>>> remove file: " + file);
				this.channel.rm(file);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.debug("Removal failed (" + ex + ").");
			ex.printStackTrace();

			return -1;
		}

		return 1;
	}

	private void cleanSftpDir(String dir) throws Exception, SftpException {
		Log.out(">>>>>>>> cleanSftpDir: " + dir);

		List<LsEntry> v = new java.util.ArrayList<>(this.channel.ls(dir));

		String[] tmp = new String[v.size()];
		java.util.Iterator<LsEntry> e = v.iterator();
		int x = 0;

		while (e.hasNext()) {
			LsEntry entry = e.next();
			tmp[x] = entry.getFilename();

			//Log.out("sftp delete: " + tmp[x]);
			if (entry.getAttrs().isDir() && !tmp[x].endsWith("/")) {
				tmp[x] = tmp[x] + "/";
			}

			x++;
		}

		if (null == tmp) {
			return;
		}

		for (String s : tmp) {
			if (s.equals("./") || s.equals("../")) {
				continue;
			}

			Log.out(">>>>>>>> remove file/dir: " + dir + s);

			if (s.endsWith("/")) {
				this.cleanSftpDir(dir + s);
				this.channel.rmdir(dir + s);
			} else {
				this.channel.rm(dir + s);
			}
		}
	}

	public void sendRawCommand(String cmd) {
	}

	public void disconnect() {
		try {
			this.channel.disconnect();
			this.session.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
			Log.debug("Sftp2Connection.disconnect()" + e);
		}

		this.connected = false;
	}

	public boolean isConnected() {
		return this.connected;
	}

	public String getPWD() {
		//Log.debug("PWD: " + pwd);
		return this.toSFTPDir(this.pwd);
	}

	public boolean mkdir(String dirName) {
		try {
			if (!dirName.endsWith("/")) {
				dirName = dirName + "/";
			}

			dirName = this.toSFTP(dirName);

			this.channel.mkdir(dirName);

			this.fireDirectoryUpdate();

			return true;
		} catch (Exception ex) {
			Log.debug("Failed to create directory (" + ex + ").");

			return false;
		}
	}

	public void list() throws IOException {
	}

	public boolean chdir(String p) {
		return this.chdir(p, true);
	}

	private boolean chdir(String p, boolean refresh) {
		String tmp = this.toSFTP(p);

		try {
			if (!tmp.endsWith("/")) {
				tmp = tmp + "/";
			}
			if (tmp.endsWith("../")) {
				return this.cdup();
			}

			System.out.println("sftp path: " + tmp + ", chan: " + this.channel);
			this.channel.cd(tmp);

			this.pwd = tmp;

			//Log.debug("chdir: " + getPWD());
			if (refresh) {
				this.fireDirectoryUpdate();
			}
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();

			//System.out.println(tmp);
			Log.debug("Could not change directory (" + ex + ").");

			return false;
		}
	}

	public boolean cdup() {
		String tmp = this.pwd;

		if (this.pwd.endsWith("/")) {
			tmp = this.pwd.substring(0, this.pwd.lastIndexOf('/'));
		}

		return this.chdir(tmp.substring(0, tmp.lastIndexOf('/') + 1));
	}

	public boolean chdirNoRefresh(String p) {
		return this.chdir(p, false);
	}

	public String getLocalPath() {
		return this.path;
	}

	public boolean setLocalPath(String p) {
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
			Log.debug("(local) No such path: \"" + p + "\"");

			return false;
		}

		return true;
	}

	public String[] sortLs() {
		try {
			System.out.println(this.pwd);
			List<LsEntry> v = new java.util.ArrayList<>(this.channel.ls(this.pwd));

			String[] tmp = new String[v.size()];
			String[] files = new String[tmp.length];
			this.size = new String[tmp.length];
			this.perms = new int[tmp.length];

			java.util.Iterator<LsEntry> e = v.iterator();
			int x = 0;

			while (e.hasNext()) {
				LsEntry entry = e.next();
				tmp[x] = entry.getFilename();

				this.size[x] = String.valueOf(entry.getAttrs().getSize());

				//Log.debug("Perms: "+entry.getAttrs().getPermissionsString());

                /*
                if(!entry.getAttrs().getPermissionsString())
                {
                    perms[x] = FtpConnection.DENIED;
                }
                else
                {*/
				this.perms[x] = FtpConstants.R;
				//}

				//Log.debugRaw(".");
				if (entry.getAttrs().isDir() && !tmp[x].endsWith("/")) {
					tmp[x] = tmp[x] + "/";
				}

				x++;
			}

			System.arraycopy(tmp, 0, files, 0, tmp.length);

			return files;
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.debug(" Error while listing directory: " + ex);
			return new String[0];
		}
	}

	public String[] sortSize() {
		return this.size;
	}

	public int[] getPermissions() {
		return this.perms;
	}

	public int handleUpload(String f) {
		if (Settings.getEnableSftpMultiThreading()) {

			Sftp2Transfer t = new Sftp2Transfer(this.path, this.getPWD(), f, this.user, this.pass, this.listeners, Transfer.UPLOAD, this.keyfile, this.host, String.valueOf(this.port));
		} else {
			this.upload(f);
		}

		return 0;
	}

	public int handleDownload(String f) {
		if (Settings.getEnableSftpMultiThreading()) {
			Sftp2Transfer t = new Sftp2Transfer(this.path, this.getPWD(), f, this.user, this.pass, this.listeners, Transfer.DOWNLOAD, this.keyfile, this.host, String.valueOf(this.port));
		} else {
			this.download(f);
		}

		return 0;
	}

	public int upload(String f) {
		String file = this.toSFTP(f);

		if (file.endsWith("/")) {
			String out = StringUtils.getDir(file);
			this.uploadDir(file, this.path + out);
			this.fireActionFinished(this);
		} else {
			String outfile = StringUtils.getFile(file);

			//System.out.println("transfer: " + file + ", " + getLocalPath() + outfile);
			this.work(this.path + outfile, file, true);
			this.fireActionFinished(this);
		}

		return 0;
	}

	public int download(String f) {
		String file = this.toSFTP(f);

		if (file.endsWith("/")) {
			String out = StringUtils.getDir(file);
			this.downloadDir(file, this.path + out);
			this.fireActionFinished(this);
		} else {
			String outfile = StringUtils.getFile(file);

			//System.out.println("transfer: " + file + ", " + getLocalPath() + outfile);
			this.work(file, this.path + outfile, false);
			this.fireActionFinished(this);
		}

		return 0;
	}

	private void downloadDir(String dir, String out) {
		try {
			//System.out.println("downloadDir: " + dir + "," + out);
			this.fileCount = 0;
			this.shortProgress = true;
			this.baseFile = StringUtils.getDir(dir);

			List<LsEntry> v = new ArrayList<>(this.channel.ls(dir));

			String[] tmp = new String[v.size()];
			java.util.Iterator<LsEntry> e = v.iterator();
			int x = 0;

			while (e.hasNext()) {
				LsEntry entry = e.next();
				tmp[x] = entry.getFilename();

				if (entry.getAttrs().isDir() && !tmp[x].endsWith("/")) {
					tmp[x] = tmp[x] + "/";
				}

				x++;
			}

			File fx = new File(out);
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
					this.fileCount++;
					this.fireProgressUpdate(this.baseFile, DataConnection.GETDIR + ":" + this.fileCount, -1);
					this.work(dir + tmp[i], out + tmp[i], false);
				}

			}

			//System.out.println("enddir");

			this.fireProgressUpdate(this.baseFile, DataConnection.DFINISHED + ":" + this.fileCount, -1);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(dir + ", " + out);
			Log.debug("Transfer error: " + ex);
			this.fireProgressUpdate(this.baseFile, DataConnection.FAILED + ":" + this.fileCount, -1);
		}

		this.shortProgress = false;
	}

	private void uploadDir(String dir, String out) {
		try {
			//System.out.println("uploadDir: " + dir + "," + out);
			this.isDirUpload = true;
			this.fileCount = 0;
			this.shortProgress = true;
			this.baseFile = StringUtils.getDir(dir);

			File f2 = new File(out);
			String[] tmp = f2.list();

			if (null == tmp) {
				return;
			}

			this.channel.mkdir(dir);
			//channel.chmod(744, dir);

			for (int i = 0; i < tmp.length; i++) {
				if (tmp[i].equals("./") || tmp[i].equals("../")) {
					continue;
				}

				tmp[i] = tmp[i].replace('\\', '/');

				//System.out.println("1: " + dir+tmp[i] + ", " + out +tmp[i]);
				File f3 = new File(out + tmp[i]);

				if (f3.isDirectory()) {
					if (!tmp[i].endsWith("/")) {
						tmp[i] = tmp[i] + "/";
					}

					this.uploadDir(dir + tmp[i], out + tmp[i]);
				} else {
					this.fileCount++;
					this.fireProgressUpdate(this.baseFile, DataConnection.PUTDIR + ":" + this.fileCount, -1);
					this.work(out + tmp[i], dir + tmp[i], true);
				}
			}

			this.fireProgressUpdate(this.baseFile, DataConnection.DFINISHED + ":" + this.fileCount, -1);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(dir + ", " + out);
			Log.debug("Transfer error: " + ex);
			this.fireProgressUpdate(this.baseFile, DataConnection.FAILED + ":" + this.fileCount, -1);
		}

		this.isDirUpload = false;
		this.shortProgress = true;
	}

	private String toSFTP(String f) {
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

	private String toSFTPDir(String f) {
		String file;

		if (f.startsWith("/")) {
			file = f;
		} else {
			file = this.pwd + f;
		}

		file = file.replace('\\', '/');

		if (!file.endsWith("/")) {
			file = file + "/";
		}

		//System.out.println("file: "+file);
		return file;
	}

	private void work(String file, String outfile, boolean up) {
		BufferedInputStream in = null;
		BufferedOutputStream out = null;

		//System.out.println("work");

		try {
			boolean outflag = false;

			if (up) {
				in = new BufferedInputStream(new FileInputStream(file));
			} else {
				in = new BufferedInputStream(this.channel.get(file));
			}

			if (up) {
				outflag = true;

				try {
					this.channel.rm(outfile);
				} catch (Exception ex) {

				}
				out = new BufferedOutputStream(this.channel.put(outfile));
			} else {
				out = new BufferedOutputStream(new FileOutputStream(outfile));
			}

			//System.out.println("out: " + outfile + ", in: " + file);
			byte[] buf = new byte[smbBuffer];
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
					this.fireProgressUpdate(StringUtils.getFile(outfile), DataConnection.PUT, reallen);
				} else {
					this.fireProgressUpdate(StringUtils.getFile(file), DataConnection.GET, reallen);
				}
			}


			this.fireProgressUpdate(file, DataConnection.FINISHED, -1);
		} catch (IOException ex) {
			ex.printStackTrace();
			Log.debug("Error with file IO (" + ex + ")!");
			this.fireProgressUpdate(file, DataConnection.FAILED, -1);
		} catch (SftpException ex) {
			ex.printStackTrace();
			Log.debug("Error with SFTP IO (" + ex + ")!");
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

	public boolean rename(String oldName, String newName) {
		try {
			oldName = this.toSFTP(oldName);
			newName = this.toSFTP(newName);

			this.channel.rename(oldName, newName);

			return true;
		} catch (Exception ex) {
			ex.printStackTrace();

			Log.debug("Could rename file (" + ex + ").");

			return false;
		}
	}

	private void update(String file, String type, int bytes) {
		if (null == this.listeners) {
		} else {
			for (ConnectionListener connectionListener : this.listeners) {
				connectionListener.updateProgress(file, type, bytes);
			}
		}
	}

	public void addConnectionListener(ConnectionListener l) {
		this.listeners.add(l);
	}

	public void setConnectionListeners(List<ConnectionListener> l) {
		this.listeners = l;
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
		this.user = user;
		this.pass = pass;

		if (this.login()) {
			Log.debug("Authed successfully.");

			//if(!chdir(getPWD())) chdir("/");
		} else {
			Log.debug("Login failed.");

			return false;
		}

		return true;
	}

	/**
	 * progress update
	 */
	private void fireProgressUpdate(String file, String type, int bytes) {
		if (null == this.listeners) {
			return;
		}

		for (ConnectionListener connectionListener : this.listeners) {

			if (this.shortProgress && Settings.shortProgress) {
				if (type.startsWith(DataConnection.DFINISHED)) {
					connectionListener.updateProgress(this.baseFile, DataConnection.DFINISHED + ":" + this.fileCount, bytes);
				} else if (this.isDirUpload) {
					connectionListener.updateProgress(this.baseFile, DataConnection.PUTDIR + ":" + this.fileCount, bytes);
				} else {
					connectionListener.updateProgress(this.baseFile, DataConnection.GETDIR + ":" + this.fileCount, bytes);
				}
			} else {
				connectionListener.updateProgress(file, type, bytes);
			}
		}
	}

	private void fireActionFinished(Sftp2Connection con) {
		if (null == this.listeners) {
		} else {
			for (ConnectionListener listener : this.listeners) {
				listener.actionFinished(con);
			}
		}
	}

	public int upload(String file, InputStream i) {
		BufferedOutputStream out = null;
		BufferedInputStream in = null;

		try {
			file = this.toSFTP(file);

			out = new BufferedOutputStream(this.channel.put(file));
			in = new BufferedInputStream(i);

			//Log.debug(getLocalPath() + ":" + file+ ":"+getPWD());
			byte[] buf = new byte[smbBuffer];
			int len = 0;
			int reallen = 0;

			while (true) {
				len = in.read(buf);

				//System.out.print(".");
				if (java.io.StreamTokenizer.TT_EOF == len) {
					break;
				}

				out.write(buf, 0, len);
				reallen += len;

				this.fireProgressUpdate(StringUtils.getFile(file), DataConnection.PUT, reallen);
			}

			//channel.chmod(744, file);

			this.fireProgressUpdate(file, DataConnection.FINISHED, -1);

			return 0;
		} catch (IOException ex) {
			ex.printStackTrace();
			Log.debug("Error with file IO (" + ex + ")!");
			this.fireProgressUpdate(file, DataConnection.FAILED, -1);

			return -1;
		} catch (SftpException ex) {
			ex.printStackTrace();
			Log.debug("Error with file SFTP IO (" + ex + ")!");
			this.fireProgressUpdate(file, DataConnection.FAILED, -1);

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
	}

	public InputStream getDownloadInputStream(String file) {
		try {

			return this.channel.get(file);
		} catch (SftpException ex) {
			ex.printStackTrace();
			Log.debug(ex + " @Sftp2Connection::getDownloadInputStream");

			return null;
		}
	}

	public LocalDateTime[] sortDates() {
		return null;
	}
}

class MyUserInfo implements UserInfo {

	private final String password;

	MyUserInfo(String pass) {
		super();
		this.password = pass;
	}

	public String getPassword() {
		return this.password;
	}

	public boolean promptYesNo(String str) {
		return true;
	}

	public String getPassphrase() {
		return this.password;
	}

	public boolean promptPassphrase(String message) {
		return true;
	}

	public boolean promptPassword(String message) {
		return true;
	}

	public void showMessage(String message) {
		//JOptionPane.showMessageDialog(null, message);
	}
}
