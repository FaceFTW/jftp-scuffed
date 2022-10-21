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
import net.sf.jftp.gui.base.dir.DirCanvas;
import net.sf.jftp.gui.base.dir.DirCellRenderer;
import net.sf.jftp.gui.base.dir.DirComponent;
import net.sf.jftp.gui.base.dir.DirEntry;
import net.sf.jftp.gui.base.dir.DirLister;
import net.sf.jftp.gui.base.dir.DirPanel;
import net.sf.jftp.gui.base.dir.TableUtils;
import net.sf.jftp.gui.framework.HFrame;
import net.sf.jftp.gui.framework.HImage;
import net.sf.jftp.gui.framework.HImageButton;
import net.sf.jftp.gui.tasks.Creator;
import net.sf.jftp.gui.tasks.Displayer;
import net.sf.jftp.gui.tasks.PathChanger;
import net.sf.jftp.gui.tasks.RemoteCommand;
import net.sf.jftp.net.BasicConnection;
import net.sf.jftp.net.ConnectionListener;
import net.sf.jftp.net.FilesystemConnection;
import net.sf.jftp.net.FtpConnection;
import net.sf.jftp.net.FtpConstants;
import net.sf.jftp.net.wrappers.Sftp2Connection;
import net.sf.jftp.net.wrappers.SmbConnection;
import net.sf.jftp.system.LocalIO;
import net.sf.jftp.system.StringUtils;
import net.sf.jftp.system.UpdateDaemon;
import net.sf.jftp.system.logging.Log;
import net.sf.jftp.tools.Shell;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
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
import java.time.LocalDateTime;

import static net.sf.jftp.JFtp.localDir;


public class RemoteDir extends DirComponent implements ActionListener, ConnectionListener, KeyListener {
	private static final String deleteString = "rm";
	private static final String mkdirString = "mkdir";
	private static final String refreshString = "fresh";
	private static final String cdString = "cd";
	private static final String cmdString = "cmd";
	private static final String downloadString = "<-";
	static final String uploadString = "->";
	private static final String queueString = "que";
	private static final String cdUpString = "cdUp";
	private static final String rnString = "rn";
	private final String[] sortTypes = {"Normal", "Reverse", "Size", "Size/Re"};
	private final DirCanvas label = new DirCanvas(this);
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
	private final JComboBox sorter = new JComboBox(this.sortTypes);
	private HImageButton deleteButton;
	private HImageButton mkdirButton;
	private HImageButton cmdButton;
	private HImageButton refreshButton;
	private HImageButton cdButton;
	private HImageButton downloadButton;
	private HImageButton queueButton;
	private HImageButton cdUpButton;
	private HImageButton rnButton;
	private boolean pathChanged = true;
	private boolean firstGui = true;
	private DefaultListModel jlm;
	private JScrollPane jsp = new JScrollPane(this.jl);
	private int tmpindex = -1;
	private DirEntry currentPopup;
	private String sortMode;
	private boolean dateEnabled;

	public RemoteDir() {
		super();
		this.type = "remote";
		this.con = new FilesystemConnection();
		this.con.addConnectionListener(this);

		if (!this.con.chdir("/")) {
			this.con.chdir("C:\\");
		}

		this.setDate();
	}

	public RemoteDir(String path) {
		super();
		this.type = "remote";
		this.path = path;
		this.con = new FilesystemConnection();
		this.con.addConnectionListener(this);
		this.con.chdir(path);

		this.setDate();
	}

	private void gui_init() {
		this.setLayout(new BorderLayout());
		this.currDirPanel.setFloatable(false);
		this.buttonPanel.setFloatable(false);

		FlowLayout f = new FlowLayout(FlowLayout.LEFT);
		f.setHgap(1);
		f.setVgap(2);

		this.buttonPanel.setLayout(f);
		this.buttonPanel.setMargin(new Insets(0, 0, 0, 0));

		this.props.addActionListener(this);
		this.popupMenu.add(this.props);

		this.rnButton = new HImageButton(Settings.textFileImage, rnString, "Rename selected file or directory", this);
		this.rnButton.setToolTipText("Rename selected");

		this.list.setToolTipText("Show remote listing...");
		this.transferType.setToolTipText("Toggle transfer type...");

		this.deleteButton = new HImageButton(Settings.deleteImage, deleteString, "Delete  selected", this);
		this.deleteButton.setToolTipText("Delete selected");

		this.mkdirButton = new HImageButton(Settings.mkdirImage, mkdirString, "Create a new directory", this);
		this.mkdirButton.setToolTipText("Create directory");

		this.refreshButton = new HImageButton(Settings.refreshImage, refreshString, "Refresh current directory", this);
		this.refreshButton.setToolTipText("Refresh directory");
		this.refreshButton.setRolloverIcon(new ImageIcon(HImage.getImage(this, Settings.refreshImage2)));
		this.refreshButton.setRolloverEnabled(true);

		this.cdButton = new HImageButton(Settings.cdImage, cdString, "Change directory", this);
		this.cdButton.setToolTipText("Change directory");

		this.cmdButton = new HImageButton(Settings.cmdImage, cmdString, "Execute remote command", this);
		this.cmdButton.setToolTipText("Execute remote command");

		this.downloadButton = new HImageButton(Settings.downloadImage, downloadString, "Download selected", this);
		this.downloadButton.setToolTipText("Download selected");

		this.queueButton = new HImageButton(Settings.queueImage, queueString, "Queue selected", this);
		this.queueButton.setToolTipText("Queue selected");

		this.cdUpButton = new HImageButton(Settings.cdUpImage, cdUpString, "Go to Parent Directory", this);
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

		JPanel second = new JPanel();
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
		this.jl.setCellRenderer(new DirCellRenderer());
		this.jl.setVisibleRowCount(Settings.visibleFileRows);
		this.jl.setDragEnabled(true);
		this.jl.setDropTarget(JFtp.dropTarget);

		MouseListener mouseListener = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (JFtp.uiBlocked) {
					return;
				}

				if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
					int index = RemoteDir.this.jl.getSelectedIndex() - 1;

					if (-1 > index) {
						return;
					}

					String tgt = RemoteDir.this.jl.getSelectedValue().toString();

					if (0 > index) {
					} else if ((null == RemoteDir.this.dirEntry) || (RemoteDir.this.dirEntry.length < index) || (null == RemoteDir.this.dirEntry[index])) {
					} else {
						RemoteDir.this.currentPopup = RemoteDir.this.dirEntry[index];
						RemoteDir.this.popupMenu.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			}

			public void mouseClicked(MouseEvent e) {
				if (JFtp.uiBlocked) {
					return;
				}

				TableUtils.copyTableSelectionsToJList(RemoteDir.this.jl, RemoteDir.this.table);

				if (2 == e.getClickCount()) {
					int index = RemoteDir.this.jl.getSelectedIndex() - 1;

					// mousewheel bugfix
					if (-1 > index) {
						return;
					}

					String tgt = RemoteDir.this.jl.getSelectedValue().toString();

					if (0 > index) {
						RemoteDir.this.doChdir(RemoteDir.this.path + tgt);
					} else if ((null == RemoteDir.this.dirEntry) || (RemoteDir.this.dirEntry.length < index) || (null == RemoteDir.this.dirEntry[index])) {
					} else if (RemoteDir.this.dirEntry[index].isDirectory()) {
						RemoteDir.this.doChdir(RemoteDir.this.path + tgt);
					} else if (RemoteDir.this.dirEntry[index].isLink()) {
						if (!RemoteDir.this.con.chdir(RemoteDir.this.path + tgt)) {
							RemoteDir.this.showContentWindow(RemoteDir.this.path + RemoteDir.this.dirEntry[index].toString(), RemoteDir.this.dirEntry[index]);

						}
					} else {
						RemoteDir.this.showContentWindow(RemoteDir.this.path + RemoteDir.this.dirEntry[index].toString(), RemoteDir.this.dirEntry[index]);

					}
				}
			}
		};

		this.jsp = new JScrollPane(this.table);
		this.table.getSelectionModel().addListSelectionListener(this);
		this.table.addMouseListener(mouseListener);

		AdjustmentListener adjustmentListener = e -> {
			this.jsp.repaint();
			this.jsp.revalidate();
		};

		this.jsp.getHorizontalScrollBar().addAdjustmentListener(adjustmentListener);
		this.jsp.getVerticalScrollBar().addAdjustmentListener(adjustmentListener);

		this.jsp.setSize(this.getSize().width - 20, this.getSize().height - 72);
		this.add("Center", this.jsp);
		this.jsp.setVisible(true);

		TableUtils.tryToEnableRowSorting(this.table);

		if (Settings.IS_JAVA_1_6) {
			this.buttonPanel.remove(this.sorter);
		}

		this.setVisible(true);
	}

	private void doChdir(String path) {

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
	public void gui(boolean fakeInit) {
		if (this.firstGui) {
			this.gui_init();
			this.firstGui = false;
		}

		this.setLabel();

		if (this.con instanceof FtpConnection) {
			this.list.setVisible(true);
			this.cmdButton.setVisible(true);
			this.transferType.setVisible(true);
		} else {
			this.list.setVisible(false);
			this.cmdButton.setVisible(false);
			this.transferType.setVisible(false);
		}

		if (!fakeInit) {
			this.setDirList(false);
		}

		this.invalidate();
		this.validate();

	}

	private void setDirList(boolean fakeInit) {
		this.jlm = new DefaultListModel();

		DirEntry dwn = new DirEntry("..", this);
		dwn.setDirectory();
		this.jlm.addElement(dwn);

		if (!fakeInit) {
			if (this.pathChanged) {
				this.pathChanged = false;

				DirLister dir = new DirLister(this.con, this.sortMode);

				while (!dir.finished) {
					LocalIO.pause(10);
				}

				if (dir.isOk()) {
					//TODO .debug("dir is ok");
					this.length = dir.getLength();
					this.dirEntry = new DirEntry[this.length];
					this.files = dir.list();

					String[] fSize = dir.sList();
					int[] perms = dir.getPermissions();

					for (int i = 0; i < this.length; i++) {
						if ((null == this.files) || (null == this.files[i])) {
							System.out.println("skipping setDirList, files or files[i] is null!");

							return;
						}
						this.dirEntry[i] = new DirEntry(this.files[i], this);

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
						Object[] d = dir.getDates();

						if (null != d) {
							this.dirEntry[i].setDate((LocalDateTime) d[i]);
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

	public void actionPerformed(ActionEvent e) {
		if (JFtp.uiBlocked) {
			return;
		}

		if (e.getActionCommand().equals("rm")) {
			this.lock(false);

			if (Settings.getAskToDelete()) {
				if (UITool.askToDelete(this)) {
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
			Creator c = new Creator("Create:", this.con);

		} else if (e.getActionCommand().equals("cmd")) {
			if (!(this.con instanceof FtpConnection)) {
				Log.debug("This feature is for ftp only.");

				return;
			}


			int opt = JOptionPane.showOptionDialog(this, "Would you like to type one command or to open a shell?", "Question", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, new ImageIcon(HImage.getImage(this, Settings.helpImage)), new String[]{"Shell", "Command", "Cancel"}, "Command");

			if (1 == opt) {
				RemoteCommand rc = new RemoteCommand();
			} else if (0 == opt) {
				FtpConnection conn = (FtpConnection) this.con;
				Shell s = new Shell(conn.getCommandInputReader(), conn.getCommandOutputStream());
			}

		} else if (e.getActionCommand().equals("cd")) {
			PathChanger pthc = new PathChanger("remote");

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

				PrintStream out = new PrintStream(Settings.ls_out);
				for (int i = 0; i < ((FtpConnection) this.con).currentListing.size(); i++) {
					out.println(((FtpConnection) this.con).currentListing.get(i));
				}
				out.flush();
				out.close();

				java.net.URL url = new java.io.File(Settings.ls_out).toURL();
				Displayer d = new Displayer(url, new Font("monospaced", Font.PLAIN, 11));
				JFtp.desktop.add(d, Integer.MAX_VALUE - 13);
			} catch (java.net.MalformedURLException ex) {
				ex.printStackTrace();
				Log.debug("ERROR: Malformed URL!");
			} catch (FileNotFoundException ex2) {
				ex2.printStackTrace();
				Log.debug("ERROR: File not found!");
			}
		} else if (e.getActionCommand().equals("type") && (!JFtp.uiBlocked)) {
			if (!(this.con instanceof FtpConnection)) {
				Log.debug("You can only set the transfer type for ftp connections.");

				return;
			}

			FtpConnection c = (FtpConnection) this.con;
			String t = c.getTypeNow();
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

			Object[] o = this.jl.getSelectedValues();
			DirEntry[] tmp = new DirEntry[Array.getLength(o)];

			for (int i = 0; i < Array.getLength(o); i++) {
				tmp[i] = (DirEntry) o[i];
				JFtp.dQueue.addFtp(tmp[i].toString());
			}
		} else if (e.getSource() == this.props) {
			JFtp.clearLog();

			int x = this.currentPopup.getPermission();
			String tmp;

			if (FtpConstants.R == x) {
				tmp = "read only";
			} else if (FtpConstants.W == x) {
				tmp = "read/write";
			} else if (FtpConstants.DENIED == x) {
				tmp = "denied";
			} else {
				tmp = "undefined";
			}

			String msg = "File: " + this.currentPopup.toString() + "\n" + " Size: " + this.currentPopup.getFileSize() + " raw size: " + this.currentPopup.getRawSize() + "\n" + " Symlink: " + this.currentPopup.isLink() + "\n" + " Directory: " + this.currentPopup.isDirectory() + "\n" + " Permission: " + tmp + "\n";
			Log.debug(msg);
		} else if (e.getSource() == this.sorter) {
			this.sortMode = (String) this.sorter.getSelectedItem();

			Settings.showDateNoSize = this.sortMode.equals("Date");

			this.fresh();
		} else if (e.getActionCommand().equals("cdUp")) {
			JFtp.remoteDir.getCon().chdir("..");
		} else if (e.getActionCommand().equals("rn")) {
			Object[] target = this.jl.getSelectedValues();

			if ((null == target) || (0 == target.length)) {
				Log.debug("No file selected");

				return;
			} else if (1 < target.length) {
				Log.debug("Too many files selected");

				return;
			}

			String val = JOptionPane.showInternalInputDialog(this, "Choose a name...");

			if (null != val) {
				if (this.con.rename(target[0].toString(), val)) {
					Log.debug("Successfully renamed.");
					this.fresh();
				} else {
					Log.debug("Rename failed.");
				}
			}
		}
	}


	private synchronized void blockedTransfer(int index) {
		this.tmpindex = index;

		Runnable r = () -> {
			boolean block = !Settings.getEnableMultiThreading();

			if (!(this.con instanceof FtpConnection)) {
				block = true;
			}

			if (block) {
				this.lock(false);
			}

			this.transfer(this.tmpindex);

			if (block) {
				localDir.fresh();
				this.unlock(false);
			}
		};

		Thread t = new Thread(r);
		t.start();
	}

	public void lock(boolean first) {
		JFtp.uiBlocked = true;
		this.jl.setEnabled(false);

		if (!first) {
			localDir.lock(true);
		}

		Log.out("ui locked.");
	}

	public void unlock(boolean first) {
		JFtp.uiBlocked = false;
		this.jl.setEnabled(true);

		if (!first) {
			localDir.unlock(true);
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
		int idx = this.jl.getSelectedIndex();

		if (0 <= idx) {
			Object o = this.jl.getSelectedValue();

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

	public void updateProgress(String file, String type, long bytes) {
		if ((null == this.dList) || (null == this.dirEntry) || null == file) {
			return;
		}

		boolean flag = false;

		if (file.endsWith("/") && (1 < file.length())) {
			flag = true;
			file = file.substring(0, file.lastIndexOf('/'));
		}

		file = file.substring(file.lastIndexOf('/') + 1);

		if (flag) {
			file = file + "/";
		}

		long s = 0;

		if (dList.sizeCache.containsKey(file)) {
			s = dList.sizeCache.get(file);
		} else {
			for (DirEntry entry : this.dirEntry) {
				if (null == entry) {
					continue;
				}

				if (entry.toString().equals(file)) {
					s = entry.getRawSize();
					dList.sizeCache.put(file, s);

					break;
				}
			}

			if (0 >= s) {
				File f = new File(localDir.getPath() + file);

				if (f.exists()) {
					s = f.length();
				}
			}
		}

		this.dList.updateList(file, type, bytes, s);
	}

	public void connectionInitialized(BasicConnection con) {
		if (null == con) {
			return;
		}

		this.setDate();

		Log.out("remote connection initialized");
	}

	public void connectionFailed(BasicConnection con, String reason) {
		Log.out("remote connection failed");

		if ((FtpConstants.OFFLINE == Integer.parseInt(reason)) && Settings.reconnect) {
			return;
		}

		HFrame h = new HFrame();
		h.getContentPane().setLayout(new BorderLayout(10, 10));
		h.setTitle("Connection failed!");
		h.setMinimumSize(new Dimension(400, 250));
		h.setLocation((int) JFtp.mainFrame.getLocation().getX() + 300, (int) JFtp.mainFrame.getLocation().getY() + 300);

		JTextArea text = new JTextArea();
		h.getContentPane().add("Center", text);
		text.setText(" ---------------- Output -----------------\n" + JFtp.logTextArea.getText());
		JFtp.logTextArea.setText("");
		text.setEditable(false);
		h.pack();
		h.setVisible(true);
	}

	private void setDate() {
		if (!(this.con instanceof FtpConnection) && !(this.con instanceof FilesystemConnection)) {
			try {
				this.sorter.removeItem("Date");
			} catch (Exception ex) {
			}

			this.dateEnabled = false;

			return;
		}

		if ((this.con instanceof FtpConnection) && (!((FtpConnection) this.con).dateVector.isEmpty())) {
			if (!this.dateEnabled) {
				this.sorter.addItem("Date");
				this.dateEnabled = true;
				UpdateDaemon.updateRemoteDirGUI();
			}
		} else if ((this.con instanceof FilesystemConnection) && (!((FilesystemConnection) this.con).dateVector.isEmpty())) {
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
				} catch (Exception ex) {
				}
			}
		}
	}

	public void updateRemoteDirectory(BasicConnection c) {
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
			FtpConnection con = (FtpConnection) c;

			String tmp = con.getCachedPWD();
			SaveSet s = new SaveSet(Settings.login_def, con.getHost(), con.getUsername(), con.getPassword(), Integer.toString(con.getPort()), tmp, con.getLocalPath());
		} else if ((null != c) && (c instanceof FilesystemConnection)) {
			localDir.getCon().setLocalPath(this.path);
		}

		this.pathChanged = true;
		this.gui(false);

		UpdateDaemon.updateLog();
	}

	private synchronized void transfer() {
		boolean[] bFileSelected = new boolean[this.dirEntry.length + 1];
		DirEntry[] cacheEntry = new DirEntry[this.dirEntry.length];
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
	private void startTransfer(DirEntry entry) {
		if (this.con instanceof FtpConnection && localDir.getCon() instanceof FtpConnection) {
			if (entry.isDirectory()) {
				Log.debug("Directory transfer between remote connections is not supported yet!");

				return;
			}

			Log.out("direct ftp transfer started (download)");
			localDir.getCon().upload(entry.file, JFtp.remoteDir.getCon().getDownloadInputStream(this.path + entry.file));
		} else if (this.con instanceof FtpConnection && localDir.getCon() instanceof FilesystemConnection) {
			// local: file, remote: ftp
			int status = this.checkForExistingFile(entry);

			if (0 <= status) {

				long s = entry.getRawSize();
				dList.sizeCache.put(entry.file, s);

				// ---------------------------------
				if ((Settings.smallSize > entry.getRawSize()) && !entry.isDirectory()) {
					this.con.download(entry.file);
				} else {
					this.con.handleDownload(this.path + entry.file);
				}
			}
		} else if (this.con instanceof FilesystemConnection && localDir.getCon() instanceof FtpConnection) {
			try {
				File f = new File(this.path + entry.file);
				FileInputStream in = new FileInputStream(f);
				localDir.getCon().setLocalPath(this.path);
				Log.debug(localDir.getCon().getPWD());
				localDir.getCon().upload(entry.file, in);
			} catch (FileNotFoundException ex) {
				Log.debug("Error: File not found: " + this.path + entry.file);
			}
		} else if (this.con instanceof FilesystemConnection && localDir.getCon() instanceof FilesystemConnection) {
			this.con.download(this.path + entry.file);
			localDir.actionPerformed(this.con, "");
		} else if (localDir.getCon() instanceof FilesystemConnection) {
			// local: file, remote: smb, sftp, nfs
			this.con.handleDownload(entry.file);
			localDir.actionPerformed(this.con, "");
		} else {
			if (entry.isDirectory()) {
				Log.debug("Directory transfer between remote connections is not supported yet!");

				return;
			}

			Log.out("direct transfer started (download)");
			localDir.getCon().upload(entry.file, JFtp.remoteDir.getCon().getDownloadInputStream(this.path + entry.file));
			localDir.actionPerformed(this.con, "FRESH");
		}
	}

	private void transfer(int i) {
		if (-2 == i) {
			this.transfer();

		} else if (this.dirEntry[i].selected) {
			this.startTransfer(this.dirEntry[i]);
		}
	}

	private int checkForExistingFile(DirEntry dirEntry) {
		File f = new File(localDir.getPath() + dirEntry.file);

		if (f.exists() && Settings.enableResuming && Settings.askToResume) {
			ResumeDialog r = new ResumeDialog(dirEntry); // ResumeDialog handels the rest

			return -1;
		}

		return 1;
	}


	public void actionFinished(BasicConnection c) {
		localDir.actionPerformed(c, "LOWFRESH");

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


	public void actionPerformed(Object target, String msg) {
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
	private void showContentWindow(String url, DirEntry d) {
		try {
			if (200000 < d.getRawSize()) {
				Log.debug("File is too big - 200kb is the maximum, sorry.");

				return;
			}

			String path = localDir.getPath();

			if (!path.endsWith("/")) {
				path = path + "/";
			}

			File file = new File(path + StringUtils.getFile(url));

			if (this.con instanceof FilesystemConnection) {
				file = new File(this.getPath() + StringUtils.getFile(url));
			} else {
				if (file.exists()) {
					Log.debug("\nRemote file must be downloaded to be viewed and\n" + " you already have a local copy present, pleasen rename it\n" + " and try again.");

					return;
				} else {
					this.con.download(url);
				}

				file = new File(localDir.getPath() + StringUtils.getFile(url));

				if (!file.exists()) {
					Log.debug("File not found: " + localDir.getPath() + StringUtils.getFile(url));
				}
			}

			HFrame f = new HFrame();
			f.setTitle(url);

			String fileUrl = ("file://" + (file.getAbsolutePath().startsWith("/") ? file.getAbsolutePath() : "/" + file.getAbsolutePath()));
			System.out.println(fileUrl);

			JEditorPane pane = new JEditorPane(fileUrl);


			if (!pane.getEditorKit().getContentType().equals("text/html") && !pane.getEditorKit().getContentType().equals("text/rtf")) {
				if (!pane.getEditorKit().getContentType().equals("text/plain")) {
					Log.debug("Nothing to do with this filetype - use the buttons if you want to transfer files.");

					return;
				}

				pane.setEditable(false);
			}

			JScrollPane jsp = new JScrollPane(pane);

			f.getContentPane().setLayout(new BorderLayout());
			f.getContentPane().add("Center", jsp);
			f.setModal(false);
			f.setLocation(100, 100);
			f.setSize(600, 400);

			f.setVisible(true);

			this.dList.fresh();
			localDir.getCon().removeFileOrDir(StringUtils.getFile(url));
			localDir.fresh();
		} catch (Exception ex) {
			Log.debug("File error: " + ex);
		}
	}

	public void keyPressed(KeyEvent e) {
		if (java.awt.event.KeyEvent.VK_ENTER == e.getKeyCode()) {
			Object o = this.jl.getSelectedValue();

			if (null == o) {
				return;
			}

			String tmp = o.toString();

			if (tmp.endsWith("/")) {
				this.con.chdir(tmp);
			} else {
				this.showContentWindow(this.path + tmp, (DirEntry) o);
			}
		} else if (java.awt.event.KeyEvent.VK_SPACE == e.getKeyCode()) {
			int x = ((DirPanel) localDir).jl.getSelectedIndex();

			if (-1 == x) {
				x = 0;
			}

			((DirPanel) localDir).jl.grabFocus();
			((DirPanel) localDir).jl.setSelectedIndex(x);
		}
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}
}
