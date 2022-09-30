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
	private final JComboBox sorter = new JComboBox(sortTypes);
	private boolean pathChanged = true;
	private boolean firstGui = true;
	private DefaultListModel jlm;
	private JScrollPane jsp = new JScrollPane(jl);
	private int tmpindex = -1;
	private net.sf.jftp.gui.base.dir.DirEntry currentPopup = null;
	private String sortMode = null;
	private boolean dateEnabled = false;

	public RemoteDir() {
		type = "remote";
		con = new FilesystemConnection();
		con.addConnectionListener(this);

		if (!con.chdir("/")) {
			con.chdir("C:\\");
		}

		this.setDate();
	}

	public RemoteDir(final String path) {
		type = "remote";
		this.path = path;
		con = new FilesystemConnection();
		con.addConnectionListener(this);
		con.chdir(path);

		this.setDate();
	}

	public void gui_init() {
		this.setLayout(new BorderLayout());
		currDirPanel.setFloatable(false);
		buttonPanel.setFloatable(false);

		final FlowLayout f = new FlowLayout(FlowLayout.LEFT);
		f.setHgap(1);
		f.setVgap(2);

		buttonPanel.setLayout(f);
		buttonPanel.setMargin(new Insets(0, 0, 0, 0));

		props.addActionListener(this);
		popupMenu.add(props);

		rnButton = new HImageButton(Settings.textFileImage, rnString, "Rename selected file or directory", this);
		rnButton.setToolTipText("Rename selected");

		list.setToolTipText("Show remote listing...");
		transferType.setToolTipText("Toggle transfer type...");

		deleteButton = new HImageButton(Settings.deleteImage, deleteString, "Delete  selected", this);
		deleteButton.setToolTipText("Delete selected");

		mkdirButton = new HImageButton(Settings.mkdirImage, mkdirString, "Create a new directory", this);
		mkdirButton.setToolTipText("Create directory");

		refreshButton = new HImageButton(Settings.refreshImage, refreshString, "Refresh current directory", this);
		refreshButton.setToolTipText("Refresh directory");
		refreshButton.setRolloverIcon(new ImageIcon(HImage.getImage(this, Settings.refreshImage2)));
		refreshButton.setRolloverEnabled(true);

		cdButton = new HImageButton(Settings.cdImage, cdString, "Change directory", this);
		cdButton.setToolTipText("Change directory");

		cmdButton = new HImageButton(Settings.cmdImage, cmdString, "Execute remote command", this);
		cmdButton.setToolTipText("Execute remote command");

		downloadButton = new HImageButton(Settings.downloadImage, downloadString, "Download selected", this);
		downloadButton.setToolTipText("Download selected");

		queueButton = new HImageButton(Settings.queueImage, queueString, "Queue selected", this);
		queueButton.setToolTipText("Queue selected");

		cdUpButton = new HImageButton(Settings.cdUpImage, cdUpString, "Go to Parent Directory", this);
		cdUpButton.setToolTipText("Go to Parent Directory");

		this.setLabel();
		label.setSize(this.getSize().width - 10, 24);
		currDirPanel.add(label);
		currDirPanel.setSize(this.getSize().width - 10, 32);
		label.setSize(this.getSize().width - 20, 24);

		p.setLayout(new BorderLayout());
		p.add("North", currDirPanel);

		buttonPanel.add(new JLabel("           "));
		buttonPanel.add(queueButton);

		buttonPanel.add(new JLabel("    "));

		buttonPanel.add(refreshButton);
		buttonPanel.add(new JLabel("  "));
		buttonPanel.add(rnButton);
		buttonPanel.add(mkdirButton);
		buttonPanel.add(cdButton);
		buttonPanel.add(deleteButton);
		buttonPanel.add(cdUpButton);
		buttonPanel.add(new JLabel("  "));

		buttonPanel.add(cmdButton);
		buttonPanel.add(list);
		buttonPanel.add(transferType);

		buttonPanel.add(sorter);

		buttonPanel.setVisible(true);

		buttonPanel.setSize(this.getSize().width - 10, 32);

		p.add("South", buttonPanel);

		final JPanel second = new JPanel();
		second.setLayout(new BorderLayout());
		second.add("Center", p);
		downloadButton.setMinimumSize(new Dimension(50, 50));
		downloadButton.setPreferredSize(new Dimension(50, 50));
		downloadButton.setMaximumSize(new Dimension(50, 50));
		second.add("West", downloadButton);

		this.add("North", second);

		sorter.addActionListener(this);

		jlm = new DefaultListModel();
		jl = new JList(jlm);
		jl.setCellRenderer(new net.sf.jftp.gui.base.dir.DirCellRenderer());
		jl.setVisibleRowCount(Settings.visibleFileRows);
		jl.setDragEnabled(true);
		jl.setDropTarget(JFtp.dropTarget);

		final MouseListener mouseListener = new MouseAdapter() {
			public void mousePressed(final MouseEvent e) {
				if (JFtp.uiBlocked) {
					return;
				}

				if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
					final int index = jl.getSelectedIndex() - 1;

					if (index < -1) {
						return;
					}

					final String tgt = jl.getSelectedValue().toString();

					if (index < 0) {
					} else if ((dirEntry == null) || (dirEntry.length < index) || (dirEntry[index] == null)) {
					} else {
						currentPopup = dirEntry[index];
						popupMenu.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			}

			public void mouseClicked(final MouseEvent e) {
				if (JFtp.uiBlocked) {
					return;
				}

				net.sf.jftp.gui.base.dir.TableUtils.copyTableSelectionsToJList(jl, table);

				if (e.getClickCount() == 2) {
					final int index = jl.getSelectedIndex() - 1;

					// mousewheel bugfix
					if (index < -1) {
						return;
					}

					final String tgt = jl.getSelectedValue().toString();

					if (index < 0) {
						net.sf.jftp.gui.base.RemoteDir.this.doChdir(path + tgt);
					} else if ((dirEntry == null) || (dirEntry.length < index) || (dirEntry[index] == null)) {
					} else if (dirEntry[index].isDirectory()) {
						net.sf.jftp.gui.base.RemoteDir.this.doChdir(path + tgt);
					} else if (dirEntry[index].isLink()) {
						if (!con.chdir(path + tgt)) {
							net.sf.jftp.gui.base.RemoteDir.this.showContentWindow(path + dirEntry[index].toString(), dirEntry[index]);

						}
					} else {
						net.sf.jftp.gui.base.RemoteDir.this.showContentWindow(path + dirEntry[index].toString(), dirEntry[index]);

					}
				}
			}
		};

		jsp = new JScrollPane(table);
		table.getSelectionModel().addListSelectionListener(this);
		table.addMouseListener(mouseListener);

		final AdjustmentListener adjustmentListener = e -> {
			jsp.repaint();
			jsp.revalidate();
		};

		jsp.getHorizontalScrollBar().addAdjustmentListener(adjustmentListener);
		jsp.getVerticalScrollBar().addAdjustmentListener(adjustmentListener);

		jsp.setSize(this.getSize().width - 20, this.getSize().height - 72);
		this.add("Center", jsp);
		jsp.setVisible(true);

		net.sf.jftp.gui.base.dir.TableUtils.tryToEnableRowSorting(table);

		if (Settings.IS_JAVA_1_6) {
			buttonPanel.remove(sorter);
		}

		this.setVisible(true);
	}

	public void doChdir(final String path) {

		JFtp.setAppCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		con.chdir(path);
		JFtp.setAppCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	private void setLabel() {
		if (con instanceof FilesystemConnection) {
			label.setText("Filesystem: " + StringUtils.cutPath(path));
		} else if (con instanceof FtpConnection) {
			label.setText("Ftp: " + StringUtils.cutPath(path));
		} else if (con instanceof Sftp2Connection) {
			label.setText("Sftp: " + StringUtils.cutPath(path));
		} else {
			label.setText(StringUtils.cutPath(path));
		}
	}

	/**
	 * Part of a gui refresh.
	 * There's no need to call this by hand.
	 */
	public void gui(final boolean fakeInit) {
		if (firstGui) {
			this.gui_init();
			firstGui = false;
		}

		this.setLabel();

		if (con instanceof FtpConnection) {
			list.show();
			cmdButton.show();
			transferType.show();
		} else {
			list.hide();
			cmdButton.hide();
			transferType.hide();
		}

		if (!fakeInit) {
			this.setDirList(false);
		}

		this.invalidate();
		this.validate();

	}

	public void setDirList(final boolean fakeInit) {
		jlm = new DefaultListModel();

		final net.sf.jftp.gui.base.dir.DirEntry dwn = new net.sf.jftp.gui.base.dir.DirEntry("..", this);
		dwn.setDirectory();
		jlm.addElement(dwn);

		if (!fakeInit) {
			if (pathChanged) {
				pathChanged = false;

				final net.sf.jftp.gui.base.dir.DirLister dir = new net.sf.jftp.gui.base.dir.DirLister(con, sortMode);

				while (!dir.finished) {
					LocalIO.pause(10);
				}

				if (dir.isOk()) {
					//TODO .debug("dir is ok");
					length = dir.getLength();
					dirEntry = new net.sf.jftp.gui.base.dir.DirEntry[length];
					files = dir.list();

					final String[] fSize = dir.sList();
					final int[] perms = dir.getPermissions();

					for (int i = 0; i < length; i++) {
						if ((files == null) || (files[i] == null)) {
							System.out.println("skipping setDirList, files or files[i] is null!");

							return;
						}
						dirEntry[i] = new net.sf.jftp.gui.base.dir.DirEntry(files[i], this);

						if (dirEntry[i] == null) {
							System.out.println("\nskipping setDirList, dirEntry[i] is null!");

							return;
						}

						if (dirEntry[i].file == null) {
							System.out.println("\nskipping setDirList, dirEntry[i].file is null!");

							return;
						}

						if (perms != null) {
							dirEntry[i].setPermission(perms[i]);
						}

						if (fSize[i].startsWith("@")) {
							fSize[i] = fSize[i].substring(1);
						}

						dirEntry[i].setFileSize(Long.parseLong(fSize[i]));

						if (dirEntry[i].file.endsWith("/")) {
							dirEntry[i].setDirectory();
						} else {
							dirEntry[i].setFile();
						}

						if (dirEntry[i].file.endsWith("###")) {
							dirEntry[i].setLink();
						}

						//------ date parser -------
						final Object[] d = dir.getDates();

						if (d != null) {
							dirEntry[i].setDate((Date) d[i]);
						}

						//--------------------------
						jlm.addElement(dirEntry[i]);
					}
				} else {
					Log.debug("Not a directory: " + path);
				}
			}

		}

		jl.setModel(jlm);
		jl.grabFocus();
		jl.setSelectedIndex(0);
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

			for (int i = 0; i < length; i++) {
				if (dirEntry[i].selected) {
					con.removeFileOrDir(dirEntry[i].file);
				}
			}

			this.unlock(false);
			this.fresh();
		} else if (e.getActionCommand().equals("mkdir")) {
			final net.sf.jftp.gui.tasks.Creator c = new net.sf.jftp.gui.tasks.Creator("Create:", con);

		} else if (e.getActionCommand().equals("cmd")) {
			if (!(con instanceof FtpConnection)) {
				Log.debug("This feature is for ftp only.");

				return;
			}


			final int opt = JOptionPane.showOptionDialog(this, "Would you like to type one command or to open a shell?", "Question", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, new ImageIcon(HImage.getImage(this, Settings.helpImage)), new String[]{"Shell", "Command", "Cancel"}, "Command");

			if (opt == 1) {
				final net.sf.jftp.gui.tasks.RemoteCommand rc = new net.sf.jftp.gui.tasks.RemoteCommand();
			} else if (opt == 0) {
				final FtpConnection conn = (FtpConnection) con;
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
				if (!(con instanceof FtpConnection)) {
					Log.debug("Can only list FtpConnection output!");
				}

				final PrintStream out = new PrintStream(Settings.ls_out);
				for (int i = 0; i < ((FtpConnection) con).currentListing.size(); i++) {
					out.println(((FtpConnection) con).currentListing.get(i));
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
			if (!(con instanceof FtpConnection)) {
				Log.debug("You can only set the transfer type for ftp connections.");

				return;
			}

			final FtpConnection c = (FtpConnection) con;
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
			if (!(con instanceof FtpConnection)) {
				Log.debug("Queue supported only for FTP");

				return;
			}

			final Object[] o = jl.getSelectedValues();
			final net.sf.jftp.gui.base.dir.DirEntry[] tmp = new net.sf.jftp.gui.base.dir.DirEntry[Array.getLength(o)];

			for (int i = 0; i < Array.getLength(o); i++) {
				tmp[i] = (net.sf.jftp.gui.base.dir.DirEntry) o[i];
				JFtp.dQueue.addFtp(tmp[i].toString());
			}
		} else if (e.getSource() == props) {
			JFtp.clearLog();

			final int x = currentPopup.getPermission();
			final String tmp;

			if (x == FtpConnection.R) {
				tmp = "read only";
			} else if (x == FtpConnection.W) {
				tmp = "read/write";
			} else if (x == FtpConnection.DENIED) {
				tmp = "denied";
			} else {
				tmp = "undefined";
			}

			final String msg = "File: " + currentPopup.toString() + "\n" + " Size: " + currentPopup.getFileSize() + " raw size: " + currentPopup.getRawSize() + "\n" + " Symlink: " + currentPopup.isLink() + "\n" + " Directory: " + currentPopup.isDirectory() + "\n" + " Permission: " + tmp + "\n";
			Log.debug(msg);
		} else if (e.getSource() == sorter) {
			sortMode = (String) sorter.getSelectedItem();

			Settings.showDateNoSize = sortMode.equals("Date");

			this.fresh();
		} else if (e.getActionCommand().equals("cdUp")) {
			JFtp.remoteDir.getCon().chdir("..");
		} else if (e.getActionCommand().equals("rn")) {
			final Object[] target = jl.getSelectedValues();

			if ((target == null) || (target.length == 0)) {
				Log.debug("No file selected");

				return;
			} else if (target.length > 1) {
				Log.debug("Too many files selected");

				return;
			}

			final String val = JOptionPane.showInternalInputDialog(this, "Choose a name...");

			if (val != null) {
				if (!con.rename(target[0].toString(), val)) {
					Log.debug("Rename failed.");
				} else {
					Log.debug("Successfully renamed.");
					this.fresh();
				}
			}
		}
	}


	public synchronized void blockedTransfer(final int index) {
		tmpindex = index;

		final Runnable r = () -> {
			boolean block = !net.sf.jftp.config.Settings.getEnableMultiThreading();

			if (!(con instanceof net.sf.jftp.net.FtpConnection)) {
				block = true;
			}

			if (block) {
				this.lock(false);
			}

			this.transfer(tmpindex);

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
		jl.setEnabled(false);

		if (!first) {
			JFtp.localDir.lock(true);
		}

		Log.out("ui locked.");
	}
	public void unlock(final boolean first) {
		JFtp.uiBlocked = false;
		jl.setEnabled(true);

		if (!first) {
			JFtp.localDir.unlock(true);
		}

		Log.out("ui unlocked.");
	}


	public void fresh() {
		Log.out("fresh() called.");

		Cursor x = null;

		if (JFtp.mainFrame != null) {
			x = JFtp.mainFrame.getCursor();
			JFtp.setAppCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}

		String i = "";
		final int idx = jl.getSelectedIndex();

		if (idx >= 0) {
			final Object o = jl.getSelectedValue();

			if (o != null) {
				i = o.toString();
			}
		}

		con.chdir(path);

		if ((idx >= 0) && (idx < jl.getModel().getSize())) {
			if (jl.getModel().getElementAt(idx).toString().equals(i)) {
				jl.setSelectedIndex(idx);
			} else {
				jl.setSelectedIndex(0);
			}
		}

		this.update();

		if ((JFtp.mainFrame != null) && (x.getType() != Cursor.WAIT_CURSOR)) {
			JFtp.setAppCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	public void updateProgress(String file, final String type, final long bytes) {
		if ((dList == null) || (dirEntry == null) || file == null) {
			return;
		}

		boolean flag = false;

		if (file.endsWith("/") && (file.length() > 1)) {
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
			for (final net.sf.jftp.gui.base.dir.DirEntry entry : dirEntry) {
				if (entry == null) {
					continue;
				}

				if (entry.toString().equals(file)) {
					s = entry.getRawSize();
					net.sf.jftp.JFtp.dList.sizeCache.put(file, s);

					break;
				}
			}

			if (s <= 0) {
				final File f = new File(JFtp.localDir.getPath() + file);

				if (f.exists()) {
					s = f.length();
				}
			}
		}

		dList.updateList(file, type, bytes, s);
	}

	public void connectionInitialized(final BasicConnection con) {
		if (con == null) {
			return;
		}

		this.setDate();

		Log.out("remote connection initialized");
	}

	public void connectionFailed(final BasicConnection con, final String reason) {
		Log.out("remote connection failed");

		if ((Integer.parseInt(reason) == FtpConnection.OFFLINE) && Settings.reconnect) {
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
		if (!(con instanceof FtpConnection) && !(con instanceof FilesystemConnection)) {
			try {
				sorter.removeItem("Date");
			} catch (final Exception ex) {
			}

			dateEnabled = false;

			return;
		}

		if ((con instanceof FtpConnection) && (((FtpConnection) con).dateVector.size() > 0)) {
			if (!dateEnabled) {
				sorter.addItem("Date");
				dateEnabled = true;
				UpdateDaemon.updateRemoteDirGUI();
			}
		} else if ((con instanceof FilesystemConnection) && (((FilesystemConnection) con).dateVector.size() > 0)) {
			if (!dateEnabled) {
				sorter.addItem("Date");
				dateEnabled = true;
				UpdateDaemon.updateRemoteDirGUI();
			}
		} else {
			if (dateEnabled) {
				try {
					sorter.removeItem("Date");
					dateEnabled = false;
					Settings.showDateNoSize = false;
					UpdateDaemon.updateRemoteDirGUI();
				} catch (final Exception ex) {
				}
			}
		}
	}
	public void updateRemoteDirectory(final BasicConnection c) {
		//TODO Log.debug("updateRemoteDirectory()");
		if (con == null) {
			return;
		}

		if ((c != con) && !c.hasUploaded && con instanceof FtpConnection) {
			return;
		}
		this.setDate();

		if (con instanceof FtpConnection) {
			path = ((FtpConnection) con).getCachedPWD();
		} else if (con instanceof SmbConnection && !path.startsWith("smb://")) {
			path = c.getPWD();
		} else {
			path = con.getPWD();
		}

		if ((c != null) && (c instanceof FtpConnection)) {
			final FtpConnection con = (FtpConnection) c;

			final String tmp = con.getCachedPWD();
			final SaveSet s = new SaveSet(Settings.login_def, con.getHost(), con.getUsername(), con.getPassword(), Integer.toString(con.getPort()), tmp, con.getLocalPath());
		} else if ((c != null) && (c instanceof FilesystemConnection)) {
			JFtp.localDir.getCon().setLocalPath(path);
		}

		pathChanged = true;
		this.gui(false);

		UpdateDaemon.updateLog();
	}
	public synchronized void transfer() {
		final boolean[] bFileSelected = new boolean[dirEntry.length + 1];
		final net.sf.jftp.gui.base.dir.DirEntry[] cacheEntry = new net.sf.jftp.gui.base.dir.DirEntry[dirEntry.length];
		System.arraycopy(dirEntry, 0, cacheEntry, 0, cacheEntry.length);

		for (int i = 0; i < dirEntry.length; i++) {
			bFileSelected[i] = cacheEntry[i].selected;

			if (!cacheEntry[i].equals(dirEntry[i])) {
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
		if (con instanceof FtpConnection && JFtp.localDir.getCon() instanceof FtpConnection) {
			if (entry.isDirectory()) {
				Log.debug("Directory transfer between remote connections is not supported yet!");

				return;
			}

			Log.out("direct ftp transfer started (download)");
			JFtp.localDir.getCon().upload(entry.file, JFtp.remoteDir.getCon().getDownloadInputStream(path + entry.file));
		} else if (con instanceof FtpConnection && JFtp.localDir.getCon() instanceof FilesystemConnection) {
			// local: file, remote: ftp
			final int status = this.checkForExistingFile(entry);

			if (status >= 0) {

				final long s = entry.getRawSize();
				JFtp.dList.sizeCache.put(entry.file, s);

				// ---------------------------------
				if ((entry.getRawSize() < Settings.smallSize) && !entry.isDirectory()) {
					con.download(entry.file);
				} else {
					con.handleDownload(path + entry.file);
				}
			}
		} else if (con instanceof FilesystemConnection && JFtp.localDir.getCon() instanceof FtpConnection) {
			try {
				final File f = new File(path + entry.file);
				final FileInputStream in = new FileInputStream(f);
				JFtp.localDir.getCon().setLocalPath(path);
				Log.debug(JFtp.localDir.getCon().getPWD());
				JFtp.localDir.getCon().upload(entry.file, in);
			} catch (final FileNotFoundException ex) {
				Log.debug("Error: File not found: " + path + entry.file);
			}
		} else if (con instanceof FilesystemConnection && JFtp.localDir.getCon() instanceof FilesystemConnection) {
			con.download(path + entry.file);
			JFtp.localDir.actionPerformed(con, "");
		} else if (JFtp.localDir.getCon() instanceof FilesystemConnection) {
			// local: file, remote: smb, sftp, nfs
			con.handleDownload(entry.file);
			JFtp.localDir.actionPerformed(con, "");
		} else {
			if (entry.isDirectory()) {
				Log.debug("Directory transfer between remote connections is not supported yet!");

				return;
			}

			Log.out("direct transfer started (download)");
			JFtp.localDir.getCon().upload(entry.file, JFtp.remoteDir.getCon().getDownloadInputStream(path + entry.file));
			JFtp.localDir.actionPerformed(con, "FRESH");
		}
	}

	public void transfer(final int i) {
		if (i == -2) {
			this.transfer();

		} else if (dirEntry[i].selected) {
			this.startTransfer(dirEntry[i]);
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
		if (msg.equals(type)) {
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
			if (d.getRawSize() > 200000) {
				Log.debug("File is too big - 200kb is the maximum, sorry.");

				return;
			}

			String path = JFtp.localDir.getPath();

			if (!path.endsWith("/")) {
				path = path + "/";
			}

			File file = new File(path + StringUtils.getFile(url));

			if (!(con instanceof FilesystemConnection)) {
				if (!file.exists()) {
					con.download(url);
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

			dList.fresh();
			JFtp.localDir.getCon().removeFileOrDir(StringUtils.getFile(url));
			JFtp.localDir.fresh();
		} catch (final Exception ex) {
			Log.debug("File error: " + ex);
		}
	}

	public void keyPressed(final KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			final Object o = jl.getSelectedValue();

			if (o == null) {
				return;
			}

			final String tmp = o.toString();

			if (tmp.endsWith("/")) {
				con.chdir(tmp);
			} else {
				this.showContentWindow(path + tmp, (net.sf.jftp.gui.base.dir.DirEntry) o);
			}
		} else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			int x = ((net.sf.jftp.gui.base.dir.DirPanel) JFtp.localDir).jl.getSelectedIndex();

			if (x == -1) {
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
