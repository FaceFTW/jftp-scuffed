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
import net.sf.jftp.config.SaveSet;
import net.sf.jftp.config.Settings;
import net.sf.jftp.gui.framework.HFrame;
import net.sf.jftp.gui.framework.HImage;
import net.sf.jftp.gui.framework.HImageButton;
import net.sf.jftp.gui.tasks.Displayer;
import net.sf.jftp.gui.tasks.PathChanger;
import net.sf.jftp.net.BasicConnection;
import net.sf.jftp.net.ConnectionListener;
import net.sf.jftp.net.FilesystemConnection;
import net.sf.jftp.net.FtpConnection;
import net.sf.jftp.net.wrappers.Sftp2Connection;
import net.sf.jftp.net.wrappers.SmbConnection;
import net.sf.jftp.system.LocalIO;
import net.sf.jftp.system.StringUtils;
import net.sf.jftp.system.UpdateDaemon;
import net.sf.jftp.system.logging.Log;
import net.sf.jftp.tools.Shell;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.Date;


public class RemoteDir extends net.sf.jftp.gui.base.dir.DirComponent implements ListSelectionListener, ActionListener, ConnectionListener, KeyListener {
	static final String deleteString = "rm";
	static final String mkdirString = "mkdir";
	static final String refreshString = "fresh";
	static final String cdString = "cd";
	static final String cmdString = "cmd";
	static final String downloadString = "<-";
	static final String uploadString = "->";
	static final String queueString = "que";
	static final String cdUpString = "cdUp";
	static final String rnString = "rn";
	private final net.sf.jftp.gui.base.dir.DirCanvas label = new net.sf.jftp.gui.base.dir.DirCanvas(this);
	private final int pos = 0;
	private final JPanel p = new JPanel();
	private final JToolBar buttonPanel = new JToolBar() {
		public Insets getInsets() {
			return new Insets(0, 0, 0, 0);
		}
	};

	private final JToolBar currDirPanel = new JToolBar() {
		public Insets getInsets() {
			return new Insets(0, 0, 0, 0);
		}
	};
	private final HImageButton list = new HImageButton(Settings.listImage, "list", "Show remote listing...", this);
	private final HImageButton transferType = new HImageButton(Settings.typeImage, "type", "Toggle transfer type...", this);
	private final JPopupMenu popupMenu = new JPopupMenu();
	private final JMenuItem props = new JMenuItem("Properties");
	HImageButton deleteButton;
	HImageButton mkdirButton;
	HImageButton cmdButton;
	HImageButton refreshButton;
	HImageButton cdButton;
	HImageButton downloadButton;
	HImageButton queueButton;
	HImageButton cdUpButton;
	HImageButton rnButton;
	final String[] sortTypes = new String[]{"Normal", "Reverse", "Size", "Size/Re"};
	private final JComboBox sorter = new JComboBox(this.sortTypes);
	private boolean pathChanged = true;
	private boolean firstGui = true;
	private DefaultListModel jlm;
	private JScrollPane jsp = new JScrollPane(this.jl);
	private int tmpindex = -1;
	private net.sf.jftp.gui.base.dir.DirEntry currentPopup = null;
	private String sortMode = null;
	private boolean dateEnabled = false;

	public RemoteDir() {
		this.type = "remote";
		this.con = new FilesystemConnection();
		this.con.addConnectionListener(this);

		if (!this.con.chdir("/")) {
			this.con.chdir("C:\\");
		}

		this.setDate();
	}

	public RemoteDir(final String path) {
		this.type = "remote";
		this.path = path;
		this.con = new FilesystemConnection();
		this.con.addConnectionListener(this);
		this.con.chdir(path);

		this.setDate();
	}

	public void gui_init() {
		this.setLayout(new BorderLayout());
		this.currDirPanel.setFloatable(false);
		this.buttonPanel.setFloatable(false);

		final FlowLayout f = new FlowLayout(FlowLayout.LEFT);
		f.setHgap(1);
		f.setVgap(2);

		this.buttonPanel.setLayout(f);
		this.buttonPanel.setMargin(new Insets(0, 0, 0, 0));

		this.props.addActionListener(this);
		this.popupMenu.add(this.props);

		this.rnButton = new HImageButton(Settings.textFileImage, net.sf.jftp.gui.base.RemoteDir.rnString, "Rename selected file or directory", this);
		this.rnButton.setToolTipText("Rename selected");

		this.list.setToolTipText("Show remote listing...");
		this.transferType.setToolTipText("Toggle transfer type...");

		this.deleteButton = new HImageButton(Settings.deleteImage, net.sf.jftp.gui.base.RemoteDir.deleteString, "Delete  selected", this);
		this.deleteButton.setToolTipText("Delete selected");

		this.mkdirButton = new HImageButton(Settings.mkdirImage, net.sf.jftp.gui.base.RemoteDir.mkdirString, "Create a new directory", this);
		this.mkdirButton.setToolTipText("Create directory");

		this.refreshButton = new HImageButton(Settings.refreshImage, net.sf.jftp.gui.base.RemoteDir.refreshString, "Refresh current directory", this);
		this.refreshButton.setToolTipText("Refresh directory");
		this.refreshButton.setRolloverIcon(new ImageIcon(HImage.getImage(this, Settings.refreshImage2)));
		this.refreshButton.setRolloverEnabled(true);

		this.cdButton = new HImageButton(Settings.cdImage, net.sf.jftp.gui.base.RemoteDir.cdString, "Change directory", this);
		this.cdButton.setToolTipText("Change directory");

		this.cmdButton = new HImageButton(Settings.cmdImage, net.sf.jftp.gui.base.RemoteDir.cmdString, "Execute remote command", this);
		this.cmdButton.setToolTipText("Execute remote command");

		this.downloadButton = new HImageButton(Settings.downloadImage, net.sf.jftp.gui.base.RemoteDir.downloadString, "Download selected", this);
		this.downloadButton.setToolTipText("Download selected");

		this.queueButton = new HImageButton(Settings.queueImage, net.sf.jftp.gui.base.RemoteDir.queueString, "Queue selected", this);
		this.queueButton.setToolTipText("Queue selected");

		this.cdUpButton = new HImageButton(Settings.cdUpImage, net.sf.jftp.gui.base.RemoteDir.cdUpString, "Go to Parent Directory", this);
		this.cdUpButton.setToolTipText("Go to Parent Directory");

		this.setLabel();
		this.label.setSize(this.getSize().width - 10, 24);
		this.currDirPanel.add(this.label);
		this.currDirPanel.setSize(this.getSize().width - 10, 32);
		this.label.setSize(this.getSize().width - 20, 24);

		this.p.setLayout(new BorderLayout());
		this.p.add("North", this.currDirPanel);

		this.buttonPanel.add(new JLabel("           "));
		this.buttonPanel.add(this.queueButton);

		this.buttonPanel.add(new JLabel("    "));

		this.buttonPanel.add(this.refreshButton);
		this.buttonPanel.add(new JLabel("  "));
		this.buttonPanel.add(this.rnButton);
		this.buttonPanel.add(this.mkdirButton);
		this.buttonPanel.add(this.cdButton);
		this.buttonPanel.add(this.deleteButton);
		this.buttonPanel.add(this.cdUpButton);
		this.buttonPanel.add(new JLabel("  "));

		this.buttonPanel.add(this.cmdButton);
		this.buttonPanel.add(this.list);
		this.buttonPanel.add(this.transferType);

		this.buttonPanel.add(this.sorter);

		this.buttonPanel.setVisible(true);

		this.buttonPanel.setSize(this.getSize().width - 10, 32);

		this.p.add("South", this.buttonPanel);

		final JPanel second = new JPanel();
		second.setLayout(new BorderLayout());
		second.add("Center", this.p);
		this.downloadButton.setMinimumSize(new Dimension(50, 50));
		this.downloadButton.setPreferredSize(new Dimension(50, 50));
		this.downloadButton.setMaximumSize(new Dimension(50, 50));
		second.add("West", this.downloadButton);

		this.add("North", second);

		this.sorter.addActionListener(this);

		this.jlm = new DefaultListModel();
		this.jl = new JList(this.jlm);
		this.jl.setCellRenderer(new net.sf.jftp.gui.base.dir.DirCellRenderer());
		this.jl.setVisibleRowCount(Settings.visibleFileRows);
		this.jl.setDragEnabled(true);
		this.jl.setDropTarget(JFtp.dropTarget);

		final MouseListener mouseListener = new MouseAdapter() {
			public void mousePressed(final MouseEvent e) {
				if (JFtp.uiBlocked) {
					return;
				}

				if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
					final int index = net.sf.jftp.gui.base.RemoteDir.this.jl.getSelectedIndex() - 1;

					if (-1 > index) {
						return;
					}

					final String tgt = net.sf.jftp.gui.base.RemoteDir.this.jl.getSelectedValue().toString();

					if (0 > index) {
					} else if ((null == RemoteDir.this.dirEntry) || (net.sf.jftp.gui.base.RemoteDir.this.dirEntry.length < index) || (null == RemoteDir.this.dirEntry[index])) {
					} else {
						net.sf.jftp.gui.base.RemoteDir.this.currentPopup = net.sf.jftp.gui.base.RemoteDir.this.dirEntry[index];
						net.sf.jftp.gui.base.RemoteDir.this.popupMenu.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			}

			public void mouseClicked(final MouseEvent e) {
				if (JFtp.uiBlocked) {
					return;
				}

				net.sf.jftp.gui.base.dir.TableUtils.copyTableSelectionsToJList(net.sf.jftp.gui.base.RemoteDir.this.jl, net.sf.jftp.gui.base.RemoteDir.this.table);

				if (2 == e.getClickCount()) {
					final int index = net.sf.jftp.gui.base.RemoteDir.this.jl.getSelectedIndex() - 1;

					// mousewheel bugfix
					if (-1 > index) {
						return;
					}

					final String tgt = net.sf.jftp.gui.base.RemoteDir.this.jl.getSelectedValue().toString();

					if (0 > index) {
						net.sf.jftp.gui.base.RemoteDir.this.doChdir(net.sf.jftp.gui.base.RemoteDir.this.path + tgt);
					} else if ((null == RemoteDir.this.dirEntry) || (net.sf.jftp.gui.base.RemoteDir.this.dirEntry.length < index) || (null == RemoteDir.this.dirEntry[index])) {
					} else if (net.sf.jftp.gui.base.RemoteDir.this.dirEntry[index].isDirectory()) {
						net.sf.jftp.gui.base.RemoteDir.this.doChdir(net.sf.jftp.gui.base.RemoteDir.this.path + tgt);
					} else if (net.sf.jftp.gui.base.RemoteDir.this.dirEntry[index].isLink()) {
						if (!net.sf.jftp.gui.base.RemoteDir.this.con.chdir(net.sf.jftp.gui.base.RemoteDir.this.path + tgt)) {
							net.sf.jftp.gui.base.RemoteDir.this.showContentWindow(net.sf.jftp.gui.base.RemoteDir.this.path + net.sf.jftp.gui.base.RemoteDir.this.dirEntry[index].toString(), net.sf.jftp.gui.base.RemoteDir.this.dirEntry[index]);

						}
					} else {
						net.sf.jftp.gui.base.RemoteDir.this.showContentWindow(net.sf.jftp.gui.base.RemoteDir.this.path + net.sf.jftp.gui.base.RemoteDir.this.dirEntry[index].toString(), net.sf.jftp.gui.base.RemoteDir.this.dirEntry[index]);

					}
				}
			}
		};

		this.jsp = new JScrollPane(this.table);
		this.table.getSelectionModel().addListSelectionListener(this);
		this.table.addMouseListener(mouseListener);

		final AdjustmentListener adjustmentListener = e -> {
			this.jsp.repaint();
			this.jsp.revalidate();
		};

		this.jsp.getHorizontalScrollBar().addAdjustmentListener(adjustmentListener);
		this.jsp.getVerticalScrollBar().addAdjustmentListener(adjustmentListener);

		this.jsp.setSize(this.getSize().width - 20, this.getSize().height - 72);
		this.add("Center", this.jsp);
		this.jsp.setVisible(true);

		net.sf.jftp.gui.base.dir.TableUtils.tryToEnableRowSorting(this.table);

		if (Settings.IS_JAVA_1_6) {
			this.buttonPanel.remove(this.sorter);
		}

		this.setVisible(true);
	}

	public void doChdir(final String path) {

		JFtp.setAppCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		this.con.chdir(path);
		JFtp.setAppCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	private void setLabel() {
		if (this.con instanceof FilesystemConnection) {
			this.label.setText("Filesystem: " + StringUtils.cutPath(this.path));
		} else if (this.con instanceof FtpConnection) {
			this.label.setText("Ftp: " + StringUtils.cutPath(this.path));
		} else if (this.con instanceof Sftp2Connection) {
			this.label.setText("Sftp: " + StringUtils.cutPath(this.path));
		} else {
			this.label.setText(StringUtils.cutPath(this.path));
		}
	}

	/**
	 * Part of a gui refresh.
	 * There's no need to call this by hand.
	 */
	public void gui(final boolean fakeInit) {
		if (this.firstGui) {
			this.gui_init();
			this.firstGui = false;
		}

		this.setLabel();

		if (this.con instanceof FtpConnection) {
			this.list.show();
			this.cmdButton.show();
			this.transferType.show();
		} else {
			this.list.hide();
			this.cmdButton.hide();
			this.transferType.hide();
		}

		if (!fakeInit) {
			this.setDirList(false);
		}

		this.invalidate();
		this.validate();

	}

	public void setDirList(final boolean fakeInit) {
		this.jlm = new DefaultListModel();

		final net.sf.jftp.gui.base.dir.DirEntry dwn = new net.sf.jftp.gui.base.dir.DirEntry("..", this);
		dwn.setDirectory();
		this.jlm.addElement(dwn);

		if (!fakeInit) {
			if (this.pathChanged) {
				this.pathChanged = false;

				final net.sf.jftp.gui.base.dir.DirLister dir = new net.sf.jftp.gui.base.dir.DirLister(this.con, this.sortMode);

				while (!dir.finished) {
					LocalIO.pause(10);
				}

				if (dir.isOk()) {
					//TODO .debug("dir is ok");
					this.length = dir.getLength();
					this.dirEntry = new net.sf.jftp.gui.base.dir.DirEntry[this.length];
					this.files = dir.list();

					final String[] fSize = dir.sList();
					final int[] perms = dir.getPermissions();

					for (int i = 0; i < this.length; i++) {
						if ((null == this.files) || (null == this.files[i])) {
							System.out.println("skipping setDirList, files or files[i] is null!");

							return;
						}
						this.dirEntry[i] = new net.sf.jftp.gui.base.dir.DirEntry(this.files[i], this);

						if (null == this.dirEntry[i]) {
							System.out.println("\nskipping setDirList, dirEntry[i] is null!");

							return;
						}

						if (null == this.dirEntry[i].file) {
							System.out.println("\nskipping setDirList, dirEntry[i].file is null!");

							return;
						}

						if (null != perms) {
							this.dirEntry[i].setPermission(perms[i]);
						}

						if (fSize[i].startsWith("@")) {
							fSize[i] = fSize[i].substring(1);
						}

						this.dirEntry[i].setFileSize(Long.parseLong(fSize[i]));

						if (this.dirEntry[i].file.endsWith("/")) {
							this.dirEntry[i].setDirectory();
						} else {
							this.dirEntry[i].setFile();
						}

						if (this.dirEntry[i].file.endsWith("###")) {
							this.dirEntry[i].setLink();
						}

						//------ date parser -------
						final Object[] d = dir.getDates();

						if (null != d) {
							this.dirEntry[i].setDate((Date) d[i]);
						}

						//--------------------------
						this.jlm.addElement(this.dirEntry[i]);
					}
				} else {
					Log.debug("Not a directory: " + this.path);
				}
			}

		}

		this.jl.setModel(this.jlm);
		this.jl.grabFocus();
		this.jl.setSelectedIndex(0);
		this.update();
	}

	public void actionPerformed(final ActionEvent e) {
		if (JFtp.uiBlocked) {
			return;
		}

		if (e.getActionCommand().equals("rm")) {
			this.lock(false);

			if (Settings.getAskToDelete()) {
				if (!UITool.askToDelete(this)) {
					this.unlock(false);

					return;
				}
			}

			for (int i = 0; i < this.length; i++) {
				if (this.dirEntry[i].selected) {
					this.con.removeFileOrDir(this.dirEntry[i].file);
				}
			}

			this.unlock(false);
			this.fresh();
		} else if (e.getActionCommand().equals("mkdir")) {
			final net.sf.jftp.gui.tasks.Creator c = new net.sf.jftp.gui.tasks.Creator("Create:", this.con);

		} else if (e.getActionCommand().equals("cmd")) {
			if (!(this.con instanceof FtpConnection)) {
				Log.debug("This feature is for ftp only.");

				return;
			}


			final int opt = JOptionPane.showOptionDialog(this, "Would you like to type one command or to open a shell?", "Question", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, new ImageIcon(HImage.getImage(this, Settings.helpImage)), new String[]{"Shell", "Command", "Cancel"}, "Command");

			if (1 == opt) {
				final net.sf.jftp.gui.tasks.RemoteCommand rc = new net.sf.jftp.gui.tasks.RemoteCommand();
			} else if (0 == opt) {
				final FtpConnection conn = (FtpConnection) this.con;
				final Shell s = new Shell(conn.getCommandInputReader(), conn.getCommandOutputStream());
			}

		} else if (e.getActionCommand().equals("cd")) {
			final PathChanger pthc = new PathChanger("remote");

		} else if (e.getActionCommand().equals("fresh")) {
			this.fresh();
		} else if (e.getActionCommand().equals("->")) {
			this.blockedTransfer(-2);
		} else if (e.getActionCommand().equals("<-")) {
			this.blockedTransfer(-2);
		} else if (e.getActionCommand().equals("list")) {
			try {
				if (!(this.con instanceof FtpConnection)) {
					Log.debug("Can only list FtpConnection output!");
				}

				final PrintStream out = new PrintStream(Settings.ls_out);
				for (int i = 0; i < ((FtpConnection) this.con).currentListing.size(); i++) {
					out.println(((FtpConnection) this.con).currentListing.get(i));
				}
				out.flush();
				out.close();

				final java.net.URL url = new java.io.File(Settings.ls_out).toURL();
				final Displayer d = new Displayer(url, new Font("monospaced", Font.PLAIN, 11));
				JFtp.desktop.add(d, new Integer(Integer.MAX_VALUE - 13));
			} catch (final java.net.MalformedURLException ex) {
				ex.printStackTrace();
				Log.debug("ERROR: Malformed URL!");
			} catch (final FileNotFoundException ex2) {
				ex2.printStackTrace();
				Log.debug("ERROR: File not found!");
			}
		} else if (e.getActionCommand().equals("type") && (!JFtp.uiBlocked)) {
			if (!(this.con instanceof FtpConnection)) {
				Log.debug("You can only set the transfer type for ftp connections.");

				return;
			}

			final FtpConnection c = (FtpConnection) this.con;
			final String t = c.getTypeNow();
			boolean ret = false;

			if (t.equals(FtpConnection.ASCII)) {
				ret = c.type(FtpConnection.BINARY);
			} else if (t.equals(FtpConnection.BINARY)) {
				ret = c.type(FtpConnection.EBCDIC);
			}

			if (t.equals(FtpConnection.EBCDIC) || (!ret && !t.equals(FtpConnection.L8))) {
				ret = c.type(FtpConnection.L8);
			}

			if (!ret) {
				c.type(FtpConnection.ASCII);
				Log.debug("Warning: type should be \"I\" if you want to transfer binary files!");
			}

			Log.debug("Type is now " + c.getTypeNow());
		} else if (e.getActionCommand().equals("que")) {
			if (!(this.con instanceof FtpConnection)) {
				Log.debug("Queue supported only for FTP");

				return;
			}

			final Object[] o = this.jl.getSelectedValues();
			final net.sf.jftp.gui.base.dir.DirEntry[] tmp = new net.sf.jftp.gui.base.dir.DirEntry[Array.getLength(o)];

			for (int i = 0; i < Array.getLength(o); i++) {
				tmp[i] = (net.sf.jftp.gui.base.dir.DirEntry) o[i];
				JFtp.dQueue.addFtp(tmp[i].toString());
			}
		} else if (e.getSource() == this.props) {
			JFtp.clearLog();

			final int x = this.currentPopup.getPermission();
			final String tmp;

			if (net.sf.jftp.net.FtpConnection.R == x) {
				tmp = "read only";
			} else if (net.sf.jftp.net.FtpConnection.W == x) {
				tmp = "read/write";
			} else if (net.sf.jftp.net.FtpConnection.DENIED == x) {
				tmp = "denied";
			} else {
				tmp = "undefined";
			}

			final String msg = "File: " + this.currentPopup.toString() + "\n" + " Size: " + this.currentPopup.getFileSize() + " raw size: " + this.currentPopup.getRawSize() + "\n" + " Symlink: " + this.currentPopup.isLink() + "\n" + " Directory: " + this.currentPopup.isDirectory() + "\n" + " Permission: " + tmp + "\n";
			Log.debug(msg);
		} else if (e.getSource() == this.sorter) {
			this.sortMode = (String) this.sorter.getSelectedItem();

			Settings.showDateNoSize = this.sortMode.equals("Date");

			this.fresh();
		} else if (e.getActionCommand().equals("cdUp")) {
			JFtp.remoteDir.getCon().chdir("..");
		} else if (e.getActionCommand().equals("rn")) {
			final Object[] target = this.jl.getSelectedValues();

			if ((null == target) || (0 == target.length)) {
				Log.debug("No file selected");

				return;
			} else if (1 < target.length) {
				Log.debug("Too many files selected");

				return;
			}

			final String val = JOptionPane.showInternalInputDialog(this, "Choose a name...");

			if (null != val) {
				if (!this.con.rename(target[0].toString(), val)) {
					Log.debug("Rename failed.");
				} else {
					Log.debug("Successfully renamed.");
					this.fresh();
				}
			}
		}
	}


	public synchronized void blockedTransfer(final int index) {
		this.tmpindex = index;

		final Runnable r = () -> {
			boolean block = !net.sf.jftp.config.Settings.getEnableMultiThreading();

			if (!(this.con instanceof net.sf.jftp.net.FtpConnection)) {
				block = true;
			}

			if (block) {
				this.lock(false);
			}

			this.transfer(this.tmpindex);

			if (block) {
				net.sf.jftp.JFtp.localDir.fresh();
				this.unlock(false);
			}
		};

		final Thread t = new Thread(r);
		t.start();
	}
	public void lock(final boolean first) {
		JFtp.uiBlocked = true;
		this.jl.setEnabled(false);

		if (!first) {
			JFtp.localDir.lock(true);
		}

		Log.out("ui locked.");
	}
	public void unlock(final boolean first) {
		JFtp.uiBlocked = false;
		this.jl.setEnabled(true);

		if (!first) {
			JFtp.localDir.unlock(true);
		}

		Log.out("ui unlocked.");
	}


	public void fresh() {
		Log.out("fresh() called.");

		Cursor x = null;

		if (null != net.sf.jftp.JFtp.mainFrame) {
			x = JFtp.mainFrame.getCursor();
			JFtp.setAppCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}

		String i = "";
		final int idx = this.jl.getSelectedIndex();

		if (0 <= idx) {
			final Object o = this.jl.getSelectedValue();

			if (null != o) {
				i = o.toString();
			}
		}

		this.con.chdir(this.path);

		if ((0 <= idx) && (idx < this.jl.getModel().getSize())) {
			if (this.jl.getModel().getElementAt(idx).toString().equals(i)) {
				this.jl.setSelectedIndex(idx);
			} else {
				this.jl.setSelectedIndex(0);
			}
		}

		this.update();

		if ((null != net.sf.jftp.JFtp.mainFrame) && (java.awt.Cursor.WAIT_CURSOR != x.getType())) {
			JFtp.setAppCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	public void updateProgress(String file, final String type, final long bytes) {
		if ((null == this.dList) || (null == this.dirEntry) || null == file) {
			return;
		}

		boolean flag = false;

		if (file.endsWith("/") && (1 < file.length())) {
			flag = true;
			file = file.substring(0, file.lastIndexOf("/"));
		}

		file = file.substring(file.lastIndexOf("/") + 1);

		if (flag) {
			file = file + "/";
		}

		long s = 0;

		if (JFtp.dList.sizeCache.containsKey(file)) {
			s = (Long) net.sf.jftp.JFtp.dList.sizeCache.get(file);
		} else {
			for (final net.sf.jftp.gui.base.dir.DirEntry entry : this.dirEntry) {
				if (null == entry) {
					continue;
				}

				if (entry.toString().equals(file)) {
					s = entry.getRawSize();
					net.sf.jftp.JFtp.dList.sizeCache.put(file, s);

					break;
				}
			}

			if (0 >= s) {
				final File f = new File(JFtp.localDir.getPath() + file);

				if (f.exists()) {
					s = f.length();
				}
			}
		}

		this.dList.updateList(file, type, bytes, s);
	}

	public void connectionInitialized(final BasicConnection con) {
		if (null == con) {
			return;
		}

		this.setDate();

		Log.out("remote connection initialized");
	}

	public void connectionFailed(final BasicConnection con, final String reason) {
		Log.out("remote connection failed");

		if ((net.sf.jftp.net.FtpConnection.OFFLINE == Integer.parseInt(reason)) && Settings.reconnect) {
			return;
		}

		final HFrame h = new HFrame();
		h.getContentPane().setLayout(new BorderLayout(10, 10));
		h.setTitle("Connection failed!");
		h.setMinimumSize(new Dimension(400, 250));
		h.setLocation((int) JFtp.mainFrame.getLocation().getX() + 300, (int) JFtp.mainFrame.getLocation().getY() + 300);

		final JTextArea text = new JTextArea();
		h.getContentPane().add("Center", text);
		text.setText(" ---------------- Output -----------------\n" + JFtp.log.getText());
		JFtp.log.setText("");
		text.setEditable(false);
		h.pack();
		h.show();
	}

	private void setDate() {
		if (!(this.con instanceof FtpConnection) && !(this.con instanceof FilesystemConnection)) {
			try {
				this.sorter.removeItem("Date");
			} catch (final Exception ex) {
			}

			this.dateEnabled = false;

			return;
		}

		if ((this.con instanceof FtpConnection) && (0 < ((net.sf.jftp.net.FtpConnection) this.con).dateVector.size())) {
			if (!this.dateEnabled) {
				this.sorter.addItem("Date");
				this.dateEnabled = true;
				UpdateDaemon.updateRemoteDirGUI();
			}
		} else if ((this.con instanceof FilesystemConnection) && (0 < ((net.sf.jftp.net.FilesystemConnection) this.con).dateVector.size())) {
			if (!this.dateEnabled) {
				this.sorter.addItem("Date");
				this.dateEnabled = true;
				UpdateDaemon.updateRemoteDirGUI();
			}
		} else {
			if (this.dateEnabled) {
				try {
					this.sorter.removeItem("Date");
					this.dateEnabled = false;
					Settings.showDateNoSize = false;
					UpdateDaemon.updateRemoteDirGUI();
				} catch (final Exception ex) {
				}
			}
		}
	}
	public void updateRemoteDirectory(final BasicConnection c) {
		//TODO Log.debug("updateRemoteDirectory()");
		if (null == this.con) {
			return;
		}

		if ((c != this.con) && !c.hasUploaded && this.con instanceof FtpConnection) {
			return;
		}
		this.setDate();

		if (this.con instanceof FtpConnection) {
			this.path = ((FtpConnection) this.con).getCachedPWD();
		} else if (this.con instanceof SmbConnection && !this.path.startsWith("smb://")) {
			this.path = c.getPWD();
		} else {
			this.path = this.con.getPWD();
		}

		if ((null != c) && (c instanceof FtpConnection)) {
			final FtpConnection con = (FtpConnection) c;

			final String tmp = con.getCachedPWD();
			final SaveSet s = new SaveSet(Settings.login_def, con.getHost(), con.getUsername(), con.getPassword(), Integer.toString(con.getPort()), tmp, con.getLocalPath());
		} else if ((null != c) && (c instanceof FilesystemConnection)) {
			JFtp.localDir.getCon().setLocalPath(this.path);
		}

		this.pathChanged = true;
		this.gui(false);

		UpdateDaemon.updateLog();
	}
	public synchronized void transfer() {
		final boolean[] bFileSelected = new boolean[this.dirEntry.length + 1];
		final net.sf.jftp.gui.base.dir.DirEntry[] cacheEntry = new net.sf.jftp.gui.base.dir.DirEntry[this.dirEntry.length];
		System.arraycopy(this.dirEntry, 0, cacheEntry, 0, cacheEntry.length);

		for (int i = 0; i < this.dirEntry.length; i++) {
			bFileSelected[i] = cacheEntry[i].selected;

			if (!cacheEntry[i].equals(this.dirEntry[i])) {
				Log.out("mismatch");
			}
		}

		for (int i = 0; i < cacheEntry.length; i++) {
			if (bFileSelected[i]) {
				this.startTransfer(cacheEntry[i]);
			}
		}
	}

	/**
	 * Start a file transfer.
	 * Depending on the local and remote connection types some things like
	 * local working directory have to be set, resuming may have to be checked etc.
	 * As with ftp to ftp transfers the action used to download a file might actually be
	 * an upload.
	 * <p>
	 * WARNING: If you do anything here, please check LocalDir.startTransfer(), too!
	 */
	public void startTransfer(final net.sf.jftp.gui.base.dir.DirEntry entry) {
		if (this.con instanceof FtpConnection && JFtp.localDir.getCon() instanceof FtpConnection) {
			if (entry.isDirectory()) {
				Log.debug("Directory transfer between remote connections is not supported yet!");

				return;
			}

			Log.out("direct ftp transfer started (download)");
			JFtp.localDir.getCon().upload(entry.file, JFtp.remoteDir.getCon().getDownloadInputStream(this.path + entry.file));
		} else if (this.con instanceof FtpConnection && JFtp.localDir.getCon() instanceof FilesystemConnection) {
			// local: file, remote: ftp
			final int status = this.checkForExistingFile(entry);

			if (0 <= status) {

				final long s = entry.getRawSize();
				JFtp.dList.sizeCache.put(entry.file, s);

				// ---------------------------------
				if ((net.sf.jftp.config.Settings.smallSize > entry.getRawSize()) && !entry.isDirectory()) {
					this.con.download(entry.file);
				} else {
					this.con.handleDownload(this.path + entry.file);
				}
			}
		} else if (this.con instanceof FilesystemConnection && JFtp.localDir.getCon() instanceof FtpConnection) {
			try {
				final File f = new File(this.path + entry.file);
				final FileInputStream in = new FileInputStream(f);
				JFtp.localDir.getCon().setLocalPath(this.path);
				Log.debug(JFtp.localDir.getCon().getPWD());
				JFtp.localDir.getCon().upload(entry.file, in);
			} catch (final FileNotFoundException ex) {
				Log.debug("Error: File not found: " + this.path + entry.file);
			}
		} else if (this.con instanceof FilesystemConnection && JFtp.localDir.getCon() instanceof FilesystemConnection) {
			this.con.download(this.path + entry.file);
			JFtp.localDir.actionPerformed(this.con, "");
		} else if (JFtp.localDir.getCon() instanceof FilesystemConnection) {
			// local: file, remote: smb, sftp, nfs
			this.con.handleDownload(entry.file);
			JFtp.localDir.actionPerformed(this.con, "");
		} else {
			if (entry.isDirectory()) {
				Log.debug("Directory transfer between remote connections is not supported yet!");

				return;
			}

			Log.out("direct transfer started (download)");
			JFtp.localDir.getCon().upload(entry.file, JFtp.remoteDir.getCon().getDownloadInputStream(this.path + entry.file));
			JFtp.localDir.actionPerformed(this.con, "FRESH");
		}
	}

	public void transfer(final int i) {
		if (-2 == i) {
			this.transfer();

		} else if (this.dirEntry[i].selected) {
			this.startTransfer(this.dirEntry[i]);
		}
	}
	private int checkForExistingFile(final net.sf.jftp.gui.base.dir.DirEntry dirEntry) {
		final File f = new File(JFtp.localDir.getPath() + dirEntry.file);

		if (f.exists() && Settings.enableResuming && Settings.askToResume) {
			final ResumeDialog r = new ResumeDialog(dirEntry); // ResumeDialog handels the rest

			return -1;
		}

		return 1;
	}


	public void actionFinished(final BasicConnection c) {
		JFtp.localDir.actionPerformed(c, "LOWFRESH");

		if (c instanceof FtpConnection) {
			if (((FtpConnection) c).hasUploaded) {
				Log.out("actionFinished called by upload: " + c);

				UpdateDaemon.updateRemoteDir();
			}

			Log.out("actionFinished called by download: " + c);
		} else {
			Log.out("actionFinished called by: " + c);

			UpdateDaemon.updateRemoteDir();
		}

		UpdateDaemon.updateLog();
	}


	public void actionPerformed(final Object target, final String msg) {
		if (msg.equals(this.type)) {
			UpdateDaemon.updateRemoteDirGUI();

		} else if (msg.equals("FRESH")) {
			UpdateDaemon.updateRemoteDir();

		}
		UpdateDaemon.updateLog();
	}

	/**
	 * Mime type handler for doubleclicks on files
	 */
	public void showContentWindow(final String url, final net.sf.jftp.gui.base.dir.DirEntry d) {
		try {
			if (200000 < d.getRawSize()) {
				Log.debug("File is too big - 200kb is the maximum, sorry.");

				return;
			}

			String path = JFtp.localDir.getPath();

			if (!path.endsWith("/")) {
				path = path + "/";
			}

			File file = new File(path + StringUtils.getFile(url));

			if (!(this.con instanceof FilesystemConnection)) {
				if (!file.exists()) {
					this.con.download(url);
				} else {
					Log.debug("\nRemote file must be downloaded to be viewed and\n" + " you already have a local copy present, pleasen rename it\n" + " and try again.");

					return;
				}

				file = new File(JFtp.localDir.getPath() + StringUtils.getFile(url));

				if (!file.exists()) {
					Log.debug("File not found: " + JFtp.localDir.getPath() + StringUtils.getFile(url));
				}
			} else {
				file = new File(this.getPath() + StringUtils.getFile(url));
			}

			final HFrame f = new HFrame();
			f.setTitle(url);

			final String fileUrl = ("file://" + (file.getAbsolutePath().startsWith("/") ? file.getAbsolutePath() : "/" + file.getAbsolutePath()));
			System.out.println(fileUrl);

			final JEditorPane pane = new JEditorPane(fileUrl);


			if (!pane.getEditorKit().getContentType().equals("text/html") && !pane.getEditorKit().getContentType().equals("text/rtf")) {
				if (!pane.getEditorKit().getContentType().equals("text/plain")) {
					Log.debug("Nothing to do with this filetype - use the buttons if you want to transfer files.");

					return;
				}

				pane.setEditable(false);
			}

			final JScrollPane jsp = new JScrollPane(pane);

			f.getContentPane().setLayout(new BorderLayout());
			f.getContentPane().add("Center", jsp);
			f.setModal(false);
			f.setLocation(100, 100);
			f.setSize(600, 400);

			f.show();

			this.dList.fresh();
			JFtp.localDir.getCon().removeFileOrDir(StringUtils.getFile(url));
			JFtp.localDir.fresh();
		} catch (final Exception ex) {
			Log.debug("File error: " + ex);
		}
	}

	public void keyPressed(final KeyEvent e) {
		if (java.awt.event.KeyEvent.VK_ENTER == e.getKeyCode()) {
			final Object o = this.jl.getSelectedValue();

			if (null == o) {
				return;
			}

			final String tmp = o.toString();

			if (tmp.endsWith("/")) {
				this.con.chdir(tmp);
			} else {
				this.showContentWindow(this.path + tmp, (net.sf.jftp.gui.base.dir.DirEntry) o);
			}
		} else if (java.awt.event.KeyEvent.VK_SPACE == e.getKeyCode()) {
			int x = ((net.sf.jftp.gui.base.dir.DirPanel) JFtp.localDir).jl.getSelectedIndex();

			if (-1 == x) {
				x = 0;
			}

			((net.sf.jftp.gui.base.dir.DirPanel) JFtp.localDir).jl.grabFocus();
			((net.sf.jftp.gui.base.dir.DirPanel) JFtp.localDir).jl.setSelectedIndex(x);
		}
	}

	public void keyReleased(final KeyEvent e) {
	}

	public void keyTyped(final KeyEvent e) {
	}
}
