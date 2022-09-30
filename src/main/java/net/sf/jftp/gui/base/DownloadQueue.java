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
	private final JList list = new JList(liststr);
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
		this.setLayout(new BorderLayout());
		final HPanel cmdP = new HPanel();

		final net.sf.jftp.gui.framework.HImageButton start = new net.sf.jftp.gui.framework.HImageButton(net.sf.jftp.config.Settings.resumeImage, "start", "Start queue download...", this);
		cmdP.add(start);

		final net.sf.jftp.gui.framework.HImageButton stop = new net.sf.jftp.gui.framework.HImageButton(net.sf.jftp.config.Settings.pauseImage, "stop", "Stop queue download...", this);
		cmdP.add(stop);

		cmdP.add(new JLabel("   "));

		final net.sf.jftp.gui.framework.HImageButton up = new net.sf.jftp.gui.framework.HImageButton(net.sf.jftp.config.Settings.downloadImage, "up", "Change order of queue", this);
		cmdP.add(up);

		final net.sf.jftp.gui.framework.HImageButton down = new net.sf.jftp.gui.framework.HImageButton(net.sf.jftp.config.Settings.uploadImage, "down", "Change order of queue", this);
		cmdP.add(down);

		final net.sf.jftp.gui.framework.HImageButton delete = new net.sf.jftp.gui.framework.HImageButton(net.sf.jftp.config.Settings.deleteImage, "del", "Delete item in queue", this);
		cmdP.add(delete);

		cmdP.add(new JLabel("   "));

		final net.sf.jftp.gui.framework.HImageButton save = new net.sf.jftp.gui.framework.HImageButton(net.sf.jftp.config.Settings.saveImage, "save", "Save queue list to file...", this);
		cmdP.add(save);

		final net.sf.jftp.gui.framework.HImageButton load = new net.sf.jftp.gui.framework.HImageButton(net.sf.jftp.config.Settings.cdImage, "load", "Load queue list from...", this);
		cmdP.add(load);

		start.setSize(24, 24);
		stop.setSize(24, 24);
		up.setSize(24, 24);
		down.setSize(24, 24);
		delete.setSize(24, 24);
		save.setSize(24, 24);
		load.setSize(24, 24);

		final JScrollPane dP = new JScrollPane(list);

		this.add(cmdP, BorderLayout.SOUTH);
		this.add(dP, BorderLayout.CENTER);

		final HPanel status = new HPanel();
		statuslabel = new JLabel("");
		statuslabel.setSize(100, 100);

		status.add(statuslabel);
		this.add(status, BorderLayout.NORTH);

		start.setToolTipText("Start queue download...");
		stop.setToolTipText("Stop queue download...");
		save.setToolTipText("Save queue list to file...");
		load.setToolTipText("Load queue list from...");
		up.setToolTipText("Change order of queue");
		down.setToolTipText("Change order of queue");
		delete.setToolTipText("Delete item in queue");

	}

	public void addFtp(final String file) {
		Log.debug("Remote File" + JFtp.remoteDir.getPath() + file + "Local File" + JFtp.localDir.getPath() + file + "HostName" + JFtp.hostinfo.hostname);

		final QueueRecord rec = new QueueRecord();
		rec.type = "ftp";
		rec.hostname = JFtp.hostinfo.hostname;
		rec.username = JFtp.hostinfo.username;
		rec.password = JFtp.hostinfo.password;
		rec.port = JFtp.hostinfo.port;
		rec.local = JFtp.localDir.getPath();
		rec.remote = JFtp.remoteDir.getPath();
		rec.file = file;
		queue.add(rec);
		liststr.addElement(rec.hostname + " : " + rec.remote + rec.file);
	}

	public void actionPerformed(final ActionEvent e) {
		if (e.getActionCommand().equals("start")) {
			if (thread != null) {
				if (!thread.isAlive()) {
					thread = new queueDownloader();
					thread.start();

				}
			} else {
				thread = new queueDownloader();
				thread.start();

			}
		} else if (e.getActionCommand().equals("stop")) {
			if (thread != null) {
				thread.block = true;

				final FtpConnection ftpcon = (FtpConnection) con;
				final DataConnection dcon = ftpcon.getDataConnection();
				dcon.getCon().work = false;

				try {
					dcon.sock.close();
				} catch (final Exception ex) {
					ex.printStackTrace();
				}
			}
		} else if (e.getActionCommand().equals("down")) {
			if (list.getSelectedIndex() != -1) {
				final int a = list.getSelectedIndex();
				final int b = a + 1;

				final QueueRecord qa = (QueueRecord) queue.get(b);
				queue.remove(b);
				queue.add(a, qa);

				final String sa = (String) liststr.get(b);
				liststr.remove(b);
				liststr.add(a, sa);

				list.setSelectedIndex(b);
			}
		} else if (e.getActionCommand().equals("up")) {
			if (list.getSelectedIndex() != -1) {
				final int a = list.getSelectedIndex() - 1;
				final int b = a + 1;

				final QueueRecord qa = (QueueRecord) queue.get(b);
				queue.remove(b);
				queue.add(a, qa);

				final String sa = (String) liststr.get(b);
				liststr.remove(b);
				liststr.add(a, sa);

				list.setSelectedIndex(a);
			}
		} else if (e.getActionCommand().equals("del")) {
			if (list.getSelectedIndex() != -1) {
				queue.remove(list.getSelectedIndex());
				liststr.remove(list.getSelectedIndex());
				list.setSelectedIndex(0);
			}
		} else if (e.getActionCommand().equals("save")) {
			final JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Save file");
			chooser.setDialogType(JFileChooser.SAVE_DIALOG);

			final int returnVal = chooser.showSaveDialog(new JDialog());

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				final File f = chooser.getSelectedFile();
				this.saveList(f);
			}
		} else if (e.getActionCommand().equals("load")) {
			final JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Open file");
			chooser.setDialogType(JFileChooser.OPEN_DIALOG);

			final int returnVal = chooser.showOpenDialog(new JDialog());

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				final File f = chooser.getSelectedFile();
				this.loadList(f);
			}
		}
	}

	private void saveList(final File file) {
		try {
			if (file.exists()) {
				file.delete();
			}

			final FileOutputStream fos;
			final PrintStream f = new PrintStream((fos = new FileOutputStream(file)));

			for (int i = 0; i <= queue.size(); i++) {
				final QueueRecord rec = (QueueRecord) queue.get(i);
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
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	private void loadList(final File file) {
		try {
			final BufferedReader breader = new BufferedReader(new FileReader(file));
			boolean Eof = false;
			queue.clear();
			liststr.clear();

			while (!Eof) {
				final QueueRecord rec = new QueueRecord();
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

				if (rec.hostname == null) {
					Eof = true;
				} else {
					queue.add(rec);
					liststr.addElement(rec.hostname + " : " + rec.remote + rec.file);
				}
			}
		} catch (final Exception ex) {
		}
	}

	// ------------ needed by Logger interface  --------------
	// main log method
	public void debug(final String msg) {
		System.out.println(msg);
	}

	// rarely used
	public void debugRaw(final String msg) {
		System.out.print(msg);
	}

	// methods below are not used yet.
	public void debug(final String msg, final Throwable throwable) {
	}

	public void warn(final String msg) {
	}

	public void warn(final String msg, final Throwable throwable) {
	}

	public void error(final String msg) {
	}

	public void error(final String msg, final Throwable throwable) {
	}

	public void info(final String msg) {
	}

	public void info(final String msg, final Throwable throwable) {
	}

	public void fatal(final String msg) {
	}

	public void fatal(final String msg, final Throwable throwable) {
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

		public QueueRecord() {
		}
	}

	class queueDownloader extends Thread implements ConnectionListener {
		public boolean block = false;
		public boolean connected = false;
		public QueueRecord last;
		private FtpConnection conFtp;

		public void run() {
			try {
				while ((queue.size() >= 1) && !block) {
					QueueRecord rec = (QueueRecord) queue.get(0);
					last = rec;

					if (!connected) {
						con = new FtpConnection(rec.hostname, Integer.parseInt(rec.port), "/");
						conFtp = (FtpConnection) con;
						conFtp.addConnectionListener(this);
						conFtp.setConnectionHandler(handler);
						conFtp.login(rec.username, rec.password);
						connected = true;
					}

					conFtp.chdirNoRefresh(rec.remote);

					conFtp.setLocalPath(rec.local);

					int i = 0;

					while (!isThere) {
						i++;

						if (i > NumRetry) {
							return;
						}

						try {
							Thread.sleep(10);
						} catch (final Exception ex) {
							ex.printStackTrace();
						}
					}

					conFtp.download(rec.file);

					statuslabel.setText("");

					if (queue.size() >= 1) {
						rec = (QueueRecord) queue.get(0);

						if ((rec.hostname.compareTo(last.hostname) == 0) && (rec.port.compareTo(last.port) == 0) && (rec.username.compareTo(last.username) == 0) && (rec.password.compareTo(last.password) == 0)) {
							connected = true;
						} else {
							conFtp.disconnect();
							connected = false;
						}
					} else {
						conFtp.disconnect();
						connected = false;
					}

					Thread.sleep(100);
				}
			} catch (final InterruptedException e) {
			}

			if (connected) {
				con.disconnect();
			}
		}

		// ------------------ needed by ConnectionListener interface -----------------
		// called if the remote directory has changed
		public void updateRemoteDirectory(final BasicConnection con) {
		}

		// called if a connection has been established
		public void connectionInitialized(final BasicConnection con) {
			isThere = true;
		}

		// called every few kb by DataConnection during the trnsfer (interval can be changed in Settings)
		public void updateProgress(final String file, final String type, final long bytes) {
			final String strtmp;

			if (StringUtils.getFile(file).compareTo("") == 0) {
				strtmp = "directory " + file;
			} else {
				strtmp = "file " + StringUtils.getFile(file);
			}

			statuslabel.setText("Downloading " + strtmp + " - kbyte " + (bytes / 1024));

			final String tmp;

			if (type.length() >= 10) {
				tmp = type.substring(0, 9);
			} else {
				tmp = " ";
			}

			if (((type.compareTo(DataConnection.FINISHED) == 0) || (tmp.compareTo(DataConnection.DFINISHED) == 0)) && !block) {
				queue.remove(0);
				liststr.remove(0);
			}
		}

		// called if connection fails
		public void connectionFailed(final BasicConnection con, final String why) {
			System.out.println("connection failed!");
		}

		// up- or download has finished
		public void actionFinished(final BasicConnection con) {
		}
	}
}
