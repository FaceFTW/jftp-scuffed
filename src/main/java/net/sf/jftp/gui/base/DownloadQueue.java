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

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.sf.jftp.gui.base;

import net.sf.jftp.JFtp;
import net.sf.jftp.config.Settings;
import net.sf.jftp.gui.framework.HImageButton;
import net.sf.jftp.gui.framework.HPanel;
import net.sf.jftp.net.BasicConnection;
import net.sf.jftp.net.ConnectionHandler;
import net.sf.jftp.net.ConnectionListener;
import net.sf.jftp.net.DataConnection;
import net.sf.jftp.net.FtpConnection;
import net.sf.jftp.system.StringUtils;
import net.sf.jftp.system.logging.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;


public class DownloadQueue extends HPanel implements ActionListener {
	private static final String SEP = "--> ";
	private final DefaultListModel liststr = new DefaultListModel();
	private final JList list = new JList(this.liststr);
	private final ArrayList queue = new ArrayList();
	private final boolean downloading = false;
	private final ConnectionHandler handler = new ConnectionHandler();
	private final JLabel statuslabel;
	final int NumRetry = 5;
	private queueDownloader thread = new queueDownloader();
	private QueueRecord lastDownload;
	private BasicConnection con;
	private boolean isThere = false;

	public DownloadQueue() {
		super();
		this.setLayout(new BorderLayout());
		HPanel cmdP = new HPanel();

		net.sf.jftp.gui.framework.HImageButton start = new net.sf.jftp.gui.framework.HImageButton(net.sf.jftp.config.Settings.resumeImage, "start", "Start queue download...", this);
		cmdP.add(start);

		net.sf.jftp.gui.framework.HImageButton stop = new net.sf.jftp.gui.framework.HImageButton(net.sf.jftp.config.Settings.pauseImage, "stop", "Stop queue download...", this);
		cmdP.add(stop);

		cmdP.add(new JLabel("   "));

		net.sf.jftp.gui.framework.HImageButton up = new net.sf.jftp.gui.framework.HImageButton(net.sf.jftp.config.Settings.downloadImage, "up", "Change order of queue", this);
		cmdP.add(up);

		net.sf.jftp.gui.framework.HImageButton down = new net.sf.jftp.gui.framework.HImageButton(net.sf.jftp.config.Settings.uploadImage, "down", "Change order of queue", this);
		cmdP.add(down);

		net.sf.jftp.gui.framework.HImageButton delete = new net.sf.jftp.gui.framework.HImageButton(net.sf.jftp.config.Settings.deleteImage, "del", "Delete item in queue", this);
		cmdP.add(delete);

		cmdP.add(new JLabel("   "));

		net.sf.jftp.gui.framework.HImageButton save = new net.sf.jftp.gui.framework.HImageButton(net.sf.jftp.config.Settings.saveImage, "save", "Save queue list to file...", this);
		cmdP.add(save);

		net.sf.jftp.gui.framework.HImageButton load = new net.sf.jftp.gui.framework.HImageButton(net.sf.jftp.config.Settings.cdImage, "load", "Load queue list from...", this);
		cmdP.add(load);

		start.setSize(24, 24);
		stop.setSize(24, 24);
		up.setSize(24, 24);
		down.setSize(24, 24);
		delete.setSize(24, 24);
		save.setSize(24, 24);
		load.setSize(24, 24);

		JScrollPane dP = new JScrollPane(this.list);

		this.add(cmdP, BorderLayout.SOUTH);
		this.add(dP, BorderLayout.CENTER);

		HPanel status = new HPanel();
		this.statuslabel = new JLabel("");
		this.statuslabel.setSize(100, 100);

		status.add(this.statuslabel);
		this.add(status, BorderLayout.NORTH);

		start.setToolTipText("Start queue download...");
		stop.setToolTipText("Stop queue download...");
		save.setToolTipText("Save queue list to file...");
		load.setToolTipText("Load queue list from...");
		up.setToolTipText("Change order of queue");
		down.setToolTipText("Change order of queue");
		delete.setToolTipText("Delete item in queue");

	}

	public void addFtp(String file) {
		Log.debug("Remote File" + JFtp.remoteDir.getPath() + file + "Local File" + JFtp.localDir.getPath() + file + "HostName" + JFtp.hostinfo.hostname);

		QueueRecord rec = new QueueRecord();
		rec.type = "ftp";
		rec.hostname = JFtp.hostinfo.hostname;
		rec.username = JFtp.hostinfo.username;
		rec.password = JFtp.hostinfo.password;
		rec.port = JFtp.hostinfo.port;
		rec.local = JFtp.localDir.getPath();
		rec.remote = JFtp.remoteDir.getPath();
		rec.file = file;
		this.queue.add(rec);
		this.liststr.addElement(rec.hostname + " : " + rec.remote + rec.file);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("start")) {
			if (null != this.thread) {
				if (!this.thread.isAlive()) {
					this.thread = new queueDownloader();
					this.thread.start();

				}
			} else {
				this.thread = new queueDownloader();
				this.thread.start();

			}
		} else if (e.getActionCommand().equals("stop")) {
			if (null != this.thread) {
				this.thread.block = true;

				FtpConnection ftpcon = (FtpConnection) this.con;
				DataConnection dcon = ftpcon.getDataConnection();
				dcon.getCon().work = false;

				try {
					dcon.sock.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} else if (e.getActionCommand().equals("down")) {
			if (-1 != this.list.getSelectedIndex()) {
				int a = this.list.getSelectedIndex();
				int b = a + 1;

				QueueRecord qa = (QueueRecord) this.queue.get(b);
				this.queue.remove(b);
				this.queue.add(a, qa);

				String sa = (String) this.liststr.get(b);
				this.liststr.remove(b);
				this.liststr.add(a, sa);

				this.list.setSelectedIndex(b);
			}
		} else if (e.getActionCommand().equals("up")) {
			if (-1 != this.list.getSelectedIndex()) {
				int a = this.list.getSelectedIndex() - 1;
				int b = a + 1;

				QueueRecord qa = (QueueRecord) this.queue.get(b);
				this.queue.remove(b);
				this.queue.add(a, qa);

				String sa = (String) this.liststr.get(b);
				this.liststr.remove(b);
				this.liststr.add(a, sa);

				this.list.setSelectedIndex(a);
			}
		} else if (e.getActionCommand().equals("del")) {
			if (-1 != this.list.getSelectedIndex()) {
				this.queue.remove(this.list.getSelectedIndex());
				this.liststr.remove(this.list.getSelectedIndex());
				this.list.setSelectedIndex(0);
			}
		} else if (e.getActionCommand().equals("save")) {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Save file");
			chooser.setDialogType(JFileChooser.SAVE_DIALOG);

			int returnVal = chooser.showSaveDialog(new JDialog());

			if (javax.swing.JFileChooser.APPROVE_OPTION == returnVal) {
				File f = chooser.getSelectedFile();
				this.saveList(f);
			}
		} else if (e.getActionCommand().equals("load")) {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Open file");
			chooser.setDialogType(JFileChooser.OPEN_DIALOG);

			int returnVal = chooser.showOpenDialog(new JDialog());

			if (javax.swing.JFileChooser.APPROVE_OPTION == returnVal) {
				File f = chooser.getSelectedFile();
				this.loadList(f);
			}
		}
	}

	private void saveList(File file) {
		try {
			if (file.exists()) {
				file.delete();
			}

			FileOutputStream fos;
			PrintStream f = new PrintStream((fos = new FileOutputStream(file)));

			for (int i = 0; i <= this.queue.size(); i++) {
				QueueRecord rec = (QueueRecord) this.queue.get(i);
				f.println(rec.type);
				f.println(rec.hostname);
				f.println(rec.username);
				f.println(rec.password);
				f.println(rec.port);
				f.println(rec.file);
				f.println(rec.local);
				f.println(rec.remote);
				f.println(rec.localip);
				f.println(rec.domain);
			}

			fos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void loadList(File file) {
		try {
			BufferedReader breader = new BufferedReader(new FileReader(file));
			boolean Eof = false;
			this.queue.clear();
			this.liststr.clear();

			while (!Eof) {
				QueueRecord rec = new QueueRecord();
				rec.type = breader.readLine();
				rec.hostname = breader.readLine();
				rec.username = breader.readLine();
				rec.password = breader.readLine();
				rec.port = breader.readLine();
				rec.file = breader.readLine();
				rec.local = breader.readLine();
				rec.remote = breader.readLine();
				rec.localip = breader.readLine();
				rec.domain = breader.readLine();

				if (null == rec.hostname) {
					Eof = true;
				} else {
					this.queue.add(rec);
					this.liststr.addElement(rec.hostname + " : " + rec.remote + rec.file);
				}
			}
		} catch (Exception ex) {
		}
	}

	// ------------ needed by Logger interface  --------------
	// main log method
	public void debug(String msg) {
		System.out.println(msg);
	}

	// rarely used
	public void debugRaw(String msg) {
		System.out.print(msg);
	}

	// methods below are not used yet.
	public void debug(String msg, Throwable throwable) {
	}

	public void warn(String msg) {
	}

	public void warn(String msg, Throwable throwable) {
	}

	public void error(String msg) {
	}

	public void error(String msg, Throwable throwable) {
	}

	public void info(String msg) {
	}

	public void info(String msg, Throwable throwable) {
	}

	public void fatal(String msg) {
	}

	public void fatal(String msg, Throwable throwable) {
	}

	class QueueRecord {
		public String type;

		// parameter used for ftp
		public String hostname;
		public String username;
		public String password;
		public String port;
		public String file;

		// local path
		public String local;

		// remote path
		public String remote;

		// used only for smb
		public String localip;
		public String domain;

		QueueRecord() {
			super();
		}
	}

	class queueDownloader extends Thread implements ConnectionListener {
		public boolean block = false;
		public boolean connected = false;
		public QueueRecord last;
		private FtpConnection conFtp;

		public void run() {
			try {
				while ((1 <= DownloadQueue.this.queue.size()) && !this.block) {
					QueueRecord rec = (QueueRecord) net.sf.jftp.gui.base.DownloadQueue.this.queue.get(0);
					this.last = rec;

					if (!this.connected) {
						net.sf.jftp.gui.base.DownloadQueue.this.con = new FtpConnection(rec.hostname, Integer.parseInt(rec.port), "/");
						this.conFtp = (FtpConnection) net.sf.jftp.gui.base.DownloadQueue.this.con;
						this.conFtp.addConnectionListener(this);
						this.conFtp.setConnectionHandler(net.sf.jftp.gui.base.DownloadQueue.this.handler);
						this.conFtp.login(rec.username, rec.password);
						this.connected = true;
					}

					this.conFtp.chdirNoRefresh(rec.remote);

					this.conFtp.setLocalPath(rec.local);

					int i = 0;

					while (!net.sf.jftp.gui.base.DownloadQueue.this.isThere) {
						i++;

						if (DownloadQueue.this.NumRetry < i) {
							return;
						}

						try {
							Thread.sleep(10);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}

					this.conFtp.download(rec.file);

					net.sf.jftp.gui.base.DownloadQueue.this.statuslabel.setText("");

					if (1 <= DownloadQueue.this.queue.size()) {
						rec = (QueueRecord) net.sf.jftp.gui.base.DownloadQueue.this.queue.get(0);

						if ((0 == rec.hostname.compareTo(this.last.hostname)) && (0 == rec.port.compareTo(this.last.port)) && (0 == rec.username.compareTo(this.last.username)) && (0 == rec.password.compareTo(this.last.password))) {
							this.connected = true;
						} else {
							this.conFtp.disconnect();
							this.connected = false;
						}
					} else {
						this.conFtp.disconnect();
						this.connected = false;
					}

					Thread.sleep(100);
				}
			} catch (InterruptedException e) {
			}

			if (this.connected) {
				net.sf.jftp.gui.base.DownloadQueue.this.con.disconnect();
			}
		}

		// ------------------ needed by ConnectionListener interface -----------------
		// called if the remote directory has changed
		public void updateRemoteDirectory(BasicConnection con) {
		}

		// called if a connection has been established
		public void connectionInitialized(BasicConnection con) {
			net.sf.jftp.gui.base.DownloadQueue.this.isThere = true;
		}

		// called every few kb by DataConnection during the trnsfer (interval can be changed in Settings)
		public void updateProgress(String file, String type, long bytes) {
			String strtmp;

			if (0 == net.sf.jftp.system.StringUtils.getFile(file).compareTo("")) {
				strtmp = "directory " + file;
			} else {
				strtmp = "file " + StringUtils.getFile(file);
			}

			net.sf.jftp.gui.base.DownloadQueue.this.statuslabel.setText("Downloading " + strtmp + " - kbyte " + (bytes / 1024));

			String tmp;

			if (10 <= type.length()) {
				tmp = type.substring(0, 9);
			} else {
				tmp = " ";
			}

			if (((0 == type.compareTo(net.sf.jftp.net.DataConnection.FINISHED)) || (0 == tmp.compareTo(net.sf.jftp.net.DataConnection.DFINISHED))) && !this.block) {
				net.sf.jftp.gui.base.DownloadQueue.this.queue.remove(0);
				net.sf.jftp.gui.base.DownloadQueue.this.liststr.remove(0);
			}
		}

		// called if connection fails
		public void connectionFailed(BasicConnection con, String why) {
			System.out.println("connection failed!");
		}

		// up- or download has finished
		public void actionFinished(BasicConnection con) {
		}
	}
}
