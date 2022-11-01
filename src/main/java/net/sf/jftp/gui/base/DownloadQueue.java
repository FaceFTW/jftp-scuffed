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
import net.sf.jftp.util.I18nHelper;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.ArrayList;


public class DownloadQueue extends HPanel implements ActionListener {
	private static final String SEP = "--> ";
	private final int NumRetry = 5;
	private final DefaultListModel liststr = new DefaultListModel();
	private final JList list = new JList(this.liststr);
	private final ArrayList queue = new ArrayList();
	private final boolean downloading = false;
	private final ConnectionHandler handler = new ConnectionHandler();
	private final JLabel statuslabel;
	private queueDownloader thread = new queueDownloader();
	private QueueRecord lastDownload;
	private BasicConnection con;
	private boolean isThere;

	public DownloadQueue() {
		super();
		this.setLayout(new BorderLayout());
		HPanel cmdP = new HPanel();

		HImageButton start = new HImageButton(Settings.resumeImage, "start", I18nHelper.getUIString(
				"start.queue" + ".download"), this);
		cmdP.add(start);

		HImageButton stop = new HImageButton(Settings.pauseImage, "stop",
				I18nHelper.getUIString("stop.queue.download"), this);
		cmdP.add(stop);

		cmdP.add(new JLabel("   "));

		HImageButton up = new HImageButton(Settings.downloadImage, "up", I18nHelper.getUIString(
				"change.order.of" + ".queue"), this);
		cmdP.add(up);

		HImageButton down = new HImageButton(Settings.uploadImage, "down", I18nHelper.getUIString(
				"change.order.of" + ".queue"), this);
		cmdP.add(down);

		HImageButton delete = new HImageButton(Settings.deleteImage, "del", I18nHelper.getUIString(
				"delete.item.in" + ".queue"), this);
		cmdP.add(delete);

		cmdP.add(new JLabel("   "));

		HImageButton save = new HImageButton(Settings.saveImage, "save", I18nHelper.getUIString(
				"save.queue.list.to" + ".file"), this);
		cmdP.add(save);

		HImageButton load = new HImageButton(Settings.cdImage, "load", I18nHelper.getUIString("load.queue.list.from"),
				this);
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

		start.setToolTipText(I18nHelper.getUIString("start.queue.download"));
		stop.setToolTipText(I18nHelper.getUIString("stop.queue.download"));
		save.setToolTipText(I18nHelper.getUIString("save.queue.list.to.file"));
		load.setToolTipText(I18nHelper.getUIString("load.queue.list.from"));
		up.setToolTipText(I18nHelper.getUIString("change.order.of.queue"));
		down.setToolTipText(I18nHelper.getUIString("change.order.of.queue"));
		delete.setToolTipText(I18nHelper.getUIString("delete.item.in.queue"));

	}

	public void addFtp(String file) {
		Log.debug("Remote File" + JFtp.remoteDir.getPath() + file + "Local File" + JFtp.localDir.getPath() + file
				+ "HostName" + JFtp.hostinfo.hostname);

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
			chooser.setDialogTitle(I18nHelper.getUIString("save.file"));
			chooser.setDialogType(JFileChooser.SAVE_DIALOG);

			int returnVal = chooser.showSaveDialog(new JDialog());

			if (javax.swing.JFileChooser.APPROVE_OPTION == returnVal) {
				File f = chooser.getSelectedFile();
				this.saveList(f);
			}
		} else if (e.getActionCommand().equals("load")) {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle(I18nHelper.getUIString("open.file"));
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
		} catch (FileNotFoundException e) {
			Log.debug(I18nHelper.getLogString("downloadqueue.filenotfoundexception.in.loadlist"));
		} catch (IOException e) {
			Log.debug(I18nHelper.getLogString("downloadqueue.ioexception.in.loadlist"));
		} catch (RuntimeException ex) {
			Log.debug(I18nHelper.getLogString("downloadqueue.runtimeexception.in.loadlist"));
		}
	}

	// ------------ needed by JftpLogger interface  --------------
	// main log method
	public void debug(String msg) {
		System.out.println(msg);
	}

	// rarely used
	public void debugRaw(String msg) {
		System.out.print(msg);
	}

	class QueueRecord {
		String type;

		// parameter used for ftp
		String hostname;
		String username;
		String password;
		String port;
		String file;

		// local path
		String local;

		// remote path
		String remote;

		// used only for smb
		String localip;
		String domain;

	}

	class queueDownloader extends Thread implements ConnectionListener {
		boolean block;
		boolean connected;
		QueueRecord last;
		private FtpConnection conFtp;

		public void run() {
			try {
				while ((1 <= DownloadQueue.this.queue.size()) && !this.block) {
					QueueRecord rec = (QueueRecord) DownloadQueue.this.queue.get(0);
					this.last = rec;

					if (!this.connected) {
						DownloadQueue.this.con = new FtpConnection(rec.hostname, Integer.parseInt(rec.port), "/");
						this.conFtp = (FtpConnection) DownloadQueue.this.con;
						this.conFtp.addConnectionListener(this);
						this.conFtp.setConnectionHandler(DownloadQueue.this.handler);
						this.conFtp.login(rec.username, rec.password);
						this.connected = true;
					}

					this.conFtp.chdirNoRefresh(rec.remote);

					this.conFtp.setLocalPath(rec.local);

					int i = 0;

					while (!DownloadQueue.this.isThere) {
						i++;

						if (DownloadQueue.this.NumRetry < i) {
							return;
						}

						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							Log.debug(I18nHelper.getLogString(
									"downloadqueue.interruptedexception.on.thread.sleep.in" + ".run"));
						} catch (RuntimeException ex) {
							Log.debug(I18nHelper.getLogString(
									"downloadqueue.runtimeexception.on.thread.sleep.in" + ".run"));
						}
					}

					this.conFtp.download(rec.file);

					DownloadQueue.this.statuslabel.setText("");

					if (1 <= DownloadQueue.this.queue.size()) {
						rec = (QueueRecord) DownloadQueue.this.queue.get(0);

						if ((0 == rec.hostname.compareTo(this.last.hostname)) && (0
								== rec.port.compareTo(this.last.port)) && (0
								== rec.username.compareTo(this.last.username)) && (0
								== rec.password.compareTo(this.last.password))) {
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
				Log.debug(I18nHelper.getLogString("downloadqueue.interruptedexception.in.run"));
			}

			if (this.connected) {
				DownloadQueue.this.con.disconnect();
			}
		}

		// ------------------ needed by ConnectionListener interface -----------------
		// called if the remote directory has changed
		public void updateRemoteDirectory(BasicConnection con) {
		}

		// called if a connection has been established
		public void connectionInitialized(BasicConnection con) {
			DownloadQueue.this.isThere = true;
		}

		// called every few kb by DataConnection during the trnsfer (interval can be changed in Settings)
		public void updateProgress(String file, String type, long bytes) {
			String strtmp;

			if (0 == StringUtils.getFile(file).compareTo("")) {
				strtmp = MessageFormat.format(I18nHelper.getUIString("directory.0"), file);
			} else {
				strtmp = MessageFormat.format(I18nHelper.getUIString("file.0"), StringUtils.getFile(file));
			}

			DownloadQueue.this.statuslabel.setText(MessageFormat.format(I18nHelper.getUIString("downloading.0.kbyte.1"), strtmp,
					bytes / 1024));

			String tmp;

			if (10 <= type.length()) {
				tmp = type.substring(0, 9);
			} else {
				tmp = " ";
			}

			if (((0 == type.compareTo(DataConnection.FINISHED)) || (0 == tmp.compareTo(DataConnection.DFINISHED)))
					&& !this.block) {
				DownloadQueue.this.queue.remove(0);
				DownloadQueue.this.liststr.remove(0);
			}
		}

		// called if connection fails
		public void connectionFailed(BasicConnection con, String why) {
			System.out.println(I18nHelper.getLogString("connection.failed"));
		}

		// up- or download has finished
		public void actionFinished(BasicConnection con) {
		}
	}
}
