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
import net.sf.jftp.gui.framework.HImage;
import net.sf.jftp.gui.framework.HImageButton;
import net.sf.jftp.gui.framework.HPanel;
import net.sf.jftp.gui.tasks.NameChooser;
import net.sf.jftp.net.BasicConnection;
import net.sf.jftp.net.ConnectionListener;
import net.sf.jftp.net.FilesystemConnection;
import net.sf.jftp.net.FtpConnection;
import net.sf.jftp.net.wrappers.SmbConnection;
import net.sf.jftp.system.LocalIO;
import net.sf.jftp.system.StringUtils;
import net.sf.jftp.system.UpdateDaemon;
import net.sf.jftp.system.logging.Log;
import net.sf.jftp.util.ZipFileCreator;

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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class LocalDir extends net.sf.jftp.gui.base.dir.DirComponent implements ListSelectionListener, ActionListener, ConnectionListener, KeyListener {
	static final String deleteString = "rm";
	static final String mkdirString = "mkdir";
	static final String refreshString = "fresh";
	static final String cdString = "cd";
	static final String cmdString = "cmd";
	static final String downloadString = "<-";
	static final String uploadString = "->";
	static final String zipString = "zip";
	static final String cpString = "cp";
	static final String rnString = "rn";
	static final String cdUpString = "cdUp";
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
	private final Hashtable dummy = new Hashtable();
	private final JPopupMenu popupMenu = new JPopupMenu();
	private final JMenuItem runFile = new JMenuItem("Launch file");
	private final JMenuItem viewFile = new JMenuItem("View file");
	private final JMenuItem props = new JMenuItem("Properties");
	HImageButton deleteButton;
	HImageButton mkdirButton;
	HImageButton cmdButton;
	HImageButton refreshButton;
	HImageButton cdButton;
	HImageButton uploadButton;
	HImageButton zipButton;
	HImageButton cpButton;
	HImageButton rnButton;
	final String[] sortTypes = new String[]{"Normal", "Reverse", "Size", "Size/Re"};
	private final JComboBox sorter = new JComboBox(this.sortTypes);
	HImageButton cdUpButton;
	private boolean pathChanged = true;
	private boolean firstGui = true;
	private DefaultListModel jlm;
	private JScrollPane jsp = new JScrollPane(this.jl);
	private int tmpindex = -1;
	private net.sf.jftp.gui.base.dir.DirEntry currentPopup = null;
	private String sortMode = null;
	private boolean dateEnabled = false;

	/**
	 * LocalDir constructor.
	 */
	public LocalDir() {
		super();
		this.type = "local";
		this.con = new FilesystemConnection();
		this.con.addConnectionListener(this);
	}

	/**
	 * LocalDir constructor.
	 */
	public LocalDir(String path) {
		super();
		this.type = "local";
		this.path = path;
		this.con = new FilesystemConnection();
		this.con.addConnectionListener(this);
		this.con.chdir(path);

	}

	/**
	 * Creates the gui and adds the MouseListener etc.
	 */
	public void gui_init() {
		this.setLayout(new BorderLayout());
		this.currDirPanel.setFloatable(false);
		this.buttonPanel.setFloatable(false);

		FlowLayout f = new FlowLayout(FlowLayout.RIGHT);
		f.setHgap(1);
		f.setVgap(2);

		this.buttonPanel.setLayout(f);
		this.buttonPanel.setMargin(new Insets(0, 0, 0, 0));

		this.runFile.addActionListener(this);
		this.viewFile.addActionListener(this);
		this.props.addActionListener(this);
		this.popupMenu.add(this.runFile);
		this.popupMenu.add(this.viewFile);
		this.popupMenu.add(this.props);

		this.deleteButton = new HImageButton(Settings.deleteImage, deleteString, "Delete selected", this);
		this.deleteButton.setToolTipText("Delete selected");

		this.mkdirButton = new HImageButton(Settings.mkdirImage, mkdirString, "Create a new directory", this);
		this.mkdirButton.setToolTipText("Create directory");

		this.refreshButton = new HImageButton(Settings.refreshImage, refreshString, "Refresh current directory", this);
		this.refreshButton.setToolTipText("Refresh directory");
		this.refreshButton.setRolloverIcon(new ImageIcon(HImage.getImage(this, Settings.refreshImage2)));
		this.refreshButton.setRolloverEnabled(true);

		this.cdButton = new HImageButton(Settings.cdImage, cdString, "Change directory", this);
		this.cdButton.setToolTipText("Change directory");

		this.uploadButton = new HImageButton(Settings.uploadImage, uploadString, "Upload selected", this);
		this.uploadButton.setToolTipText("Upload selected");

		this.zipButton = new HImageButton(Settings.zipFileImage, zipString, "Add selected to new zip file", this);
		this.zipButton.setToolTipText("Create zip");

		this.cpButton = new HImageButton(Settings.copyImage, cpString, "Copy selected files to another local dir", this);
		this.cpButton.setToolTipText("Local copy selected");

		this.rnButton = new HImageButton(Settings.textFileImage, rnString, "Rename selected file or directory", this);
		this.rnButton.setToolTipText("Rename selected");

		this.cdUpButton = new HImageButton(Settings.cdUpImage, cdUpString, "Go to Parent Directory", this);
		this.cdUpButton.setToolTipText("Go to Parent Directory");

		this.label.setText("Filesystem: " + StringUtils.cutPath(this.path));
		this.label.setSize(this.getSize().width - 10, 24);
		this.currDirPanel.add(this.label);
		this.currDirPanel.setSize(this.getSize().width - 10, 32);
		this.label.setSize(this.getSize().width - 20, 24);

		this.p.setLayout(new BorderLayout());
		this.p.add("North", this.currDirPanel);

		this.buttonPanel.add(this.sorter);

		this.buttonPanel.add(new JLabel("  "));

		this.buttonPanel.add(this.refreshButton);
		this.buttonPanel.add(new JLabel("  "));

		this.buttonPanel.add(this.cpButton);
		this.buttonPanel.add(this.rnButton);
		this.buttonPanel.add(this.mkdirButton);

		this.buttonPanel.add(this.cdButton);
		this.buttonPanel.add(this.deleteButton);
		this.buttonPanel.add(this.cdUpButton);
		this.buttonPanel.add(new JLabel("  "));

		this.buttonPanel.add(this.zipButton);
		this.buttonPanel.add(new JLabel("              "));

		this.buttonPanel.setVisible(true);

		this.buttonPanel.setSize(this.getSize().width - 10, 32);

		this.sorter.addActionListener(this);

		this.p.add("South", this.buttonPanel);

		JPanel second = new JPanel();
		second.setLayout(new BorderLayout());
		second.add("Center", this.p);
		this.uploadButton.setMinimumSize(new Dimension(50, 50));
		this.uploadButton.setPreferredSize(new Dimension(50, 50));
		this.uploadButton.setMaximumSize(new Dimension(50, 50));
		second.add("East", this.uploadButton);

		this.add("North", second);


		this.setDirList(true);
		this.jlm = new DefaultListModel();
		this.jl = new JList(this.jlm);
		this.jl.setCellRenderer(new net.sf.jftp.gui.base.dir.DirCellRenderer());
		this.jl.setVisibleRowCount(Settings.visibleFileRows);

		MouseListener mouseListener = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (JFtp.uiBlocked) {
					return;
				}

				if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
					int index = net.sf.jftp.gui.base.LocalDir.this.jl.getSelectedIndex() - 1;

					if (-1 > index) {
						return;
					}

					String tgt = net.sf.jftp.gui.base.LocalDir.this.jl.getSelectedValue().toString();

					if (0 > index) {
					} else if ((null == LocalDir.this.dirEntry) || (net.sf.jftp.gui.base.LocalDir.this.dirEntry.length < index) || (null == LocalDir.this.dirEntry[index])) {
					} else {
						net.sf.jftp.gui.base.LocalDir.this.currentPopup = net.sf.jftp.gui.base.LocalDir.this.dirEntry[index];
						net.sf.jftp.gui.base.LocalDir.this.popupMenu.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			}

			public void mouseClicked(MouseEvent e) {
				if (JFtp.uiBlocked) {
					return;
				}

				net.sf.jftp.gui.base.dir.TableUtils.copyTableSelectionsToJList(net.sf.jftp.gui.base.LocalDir.this.jl, net.sf.jftp.gui.base.LocalDir.this.table);

				if (2 == e.getClickCount()) {
					int index = net.sf.jftp.gui.base.LocalDir.this.jl.getSelectedIndex() - 1;

					if (-1 > index) {
						return;
					}

					String tgt = net.sf.jftp.gui.base.LocalDir.this.jl.getSelectedValue().toString();

					if (0 > index) {
						net.sf.jftp.gui.base.LocalDir.this.doChdir(net.sf.jftp.gui.base.LocalDir.this.path + tgt);
					} else if ((null == LocalDir.this.dirEntry) || (net.sf.jftp.gui.base.LocalDir.this.dirEntry.length < index) || (null == LocalDir.this.dirEntry[index])) {
					} else if (net.sf.jftp.gui.base.LocalDir.this.dirEntry[index].isDirectory()) {
						net.sf.jftp.gui.base.LocalDir.this.doChdir(net.sf.jftp.gui.base.LocalDir.this.path + tgt);
					} else {
						net.sf.jftp.gui.base.LocalDir.this.showContentWindow(net.sf.jftp.gui.base.LocalDir.this.path + net.sf.jftp.gui.base.LocalDir.this.dirEntry[index].toString(), net.sf.jftp.gui.base.LocalDir.this.dirEntry[index]);

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

		net.sf.jftp.gui.base.dir.TableUtils.tryToEnableRowSorting(this.table);

		if (Settings.IS_JAVA_1_6) {
			this.buttonPanel.remove(this.sorter);
		}

		this.setVisible(true);
	}

	public void doChdir(String path) {

		JFtp.setAppCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		this.con.chdir(path);
		JFtp.setAppCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	public void gui(boolean fakeInit) {
		if (this.firstGui) {
			this.gui_init();
			this.firstGui = false;
		}

		if (this.con instanceof FilesystemConnection) {
			this.label.setText("Filesystem: " + StringUtils.cutPath(this.path));
		} else {
			this.label.setText("Remote: " + StringUtils.cutPath(this.path));
		}

		if (!fakeInit) {
			this.setDirList(false);
		}

		this.invalidate();
		this.validate();
	}

	public void setDirList(boolean fakeInit) {
		this.jlm = new DefaultListModel();

		net.sf.jftp.gui.base.dir.DirEntry dwn = new net.sf.jftp.gui.base.dir.DirEntry("..", this);
		dwn.setDirectory();
		this.jlm.addElement(dwn);

		if (!fakeInit) {
			if (this.pathChanged) {
				this.pathChanged = false;

				net.sf.jftp.gui.base.dir.DirLister dir = new net.sf.jftp.gui.base.dir.DirLister(this.con, this.sortMode, Settings.getHideLocalDotNames());

				while (!dir.finished) {
					LocalIO.pause(10);
				}

				if (dir.isOk()) {
					this.length = dir.getLength();
					this.dirEntry = new net.sf.jftp.gui.base.dir.DirEntry[this.length];
					this.files = dir.list();

					String[] fSize = dir.sList();
					int[] perms = dir.getPermissions();

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

						//------ date parser -------
						Object[] d = dir.getDates();

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

		this.update();
	}

	public boolean chdir(String p) {
		if ((null == net.sf.jftp.JFtp.remoteDir) || (null == p) || p.trim().isEmpty()) {
			return false;
		}

		BasicConnection c = JFtp.remoteDir.getCon();

		if ((null != c) && (c instanceof FtpConnection)) {
			FtpConnection con = (FtpConnection) c;

			SaveSet s = new SaveSet(Settings.login_def, con.getHost(), con.getUsername(), con.getPassword(), Integer.toString(con.getPort()), con.getCachedPWD(), con.getLocalPath());
		}

		if (this.con.chdirNoRefresh(p)) {
			this.path = this.con.getPWD();

			if (this.con instanceof FilesystemConnection) {
				JFtp.remoteDir.getCon().setLocalPath(this.path);

				this.setDate();
			}

			this.pathChanged = true;
			this.gui(false);

			return true;
		}

		return false;
	}

	/**
	 * Handles the user events if the ui is unlocked
	 */
	public void actionPerformed(ActionEvent e) {
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
			net.sf.jftp.gui.tasks.Creator c = new net.sf.jftp.gui.tasks.Creator("Create:", this.con);

		} else if (e.getActionCommand().equals("cmd")) {
			net.sf.jftp.gui.tasks.RemoteCommand rc = new net.sf.jftp.gui.tasks.RemoteCommand();

		} else if (e.getActionCommand().equals("cd")) {
			String tmp = UITool.getPathFromDialog(this.path);
			this.chdir(tmp);
		} else if (e.getActionCommand().equals("fresh")) {
			this.fresh();
		} else if (e.getActionCommand().equals("cp")) {
			Object[] o = this.jl.getSelectedValues();

			if (null == o) {
				return;
			}

			String tmp = UITool.getPathFromDialog(this.path);

			if (null == tmp) {
				return;
			}

			if (!tmp.endsWith("/")) {
				tmp = tmp + "/";
			}

			try {
				this.copy(o, this.path, "", tmp);

				Log.debug("Copy finished...");
			} catch (Exception ex) {
				ex.printStackTrace();
				Log.debug("Copy failed!");
			}
		} else if (e.getActionCommand().equals("zip")) {
			try {
				Object[] entry = this.jl.getSelectedValues();

				if (null == entry) {
					return;
				}

				String[] tmp = new String[entry.length];

				for (int i = 0; i < tmp.length; i++) {
					tmp[i] = entry[i].toString();
				}

				NameChooser n = new NameChooser();
				String name = n.text.getText();
				ZipFileCreator z = new ZipFileCreator(tmp, this.path, name);
				this.fresh();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else if (e.getActionCommand().equals("->")) {
			this.blockedTransfer(-2);
		} else if (e.getActionCommand().equals("<-")) {
			this.blockedTransfer(-2);
		} else if (e.getActionCommand().equals("rn")) {
			Object[] target = this.jl.getSelectedValues();

			if ((null == target) || (0 == target.length)) {
				Log.debug("No file selected");

				return;
			} else if (1 < target.length) {
				Log.debug("Too many files selected");

				return;
			}

			String val = JOptionPane.showInternalInputDialog(JFtp.desktop, "Choose a name...");

			if (null != val) {
				if (!this.con.rename(target[0].toString(), val)) {
					Log.debug("Rename failed.");
				} else {
					Log.debug("Successfully renamed.");
					this.fresh();
				}
			}
		} else if (e.getSource() == this.props) {
			JFtp.clearLog();

			int x = this.currentPopup.getPermission();
			String tmp;

			if (net.sf.jftp.net.FtpConnection.R == x) {
				tmp = "read only";
			} else if (net.sf.jftp.net.FtpConnection.W == x) {
				tmp = "read/write";
			} else if (net.sf.jftp.net.FtpConnection.DENIED == x) {
				tmp = "denied";
			} else {
				tmp = "undefined";
			}

			String msg = "File: " + this.currentPopup.toString() + "\n" + " Size: " + this.currentPopup.getFileSize() + " raw size: " + this.currentPopup.getRawSize() + "\n" + " Symlink: " + this.currentPopup.isLink() + "\n" + " Directory: " + this.currentPopup.isDirectory() + "\n" + " Permission: " + tmp + "\n";
			Log.debug(msg);
		} else if (e.getSource() == this.viewFile) {
			if (this.currentPopup.isDirectory()) {
				Log.debug("This is a directory, not a file.");

				return;
			}

			String url = JFtp.localDir.getPath() + this.currentPopup.toString();
			this.showContentWindow(url, this.currentPopup);
		} else if (e.getSource() == this.runFile) {
			if (this.currentPopup.isDirectory()) {
				Log.debug("This is a directory, not a file.");

				return;
			}

			String url = JFtp.localDir.getPath() + this.currentPopup.toString();
			this.showContentWindow("popup-run@" + url, this.currentPopup);
		} else if (e.getSource() == this.sorter) {
			this.sortMode = (String) this.sorter.getSelectedItem();

			Settings.showLocalDateNoSize = this.sortMode.equals("Date");

			this.fresh();
		} else if (e.getActionCommand().equals("cdUp")) {
			this.chdir("..");
		}
	}

	private void copy(Object[] fRaw, String path, String offset, String target) throws Exception {
		String[] files = new String[fRaw.length];

		for (int j = 0; j < fRaw.length; j++) {
			files[j] = fRaw[j].toString();
		}

		for (int i = 0; i < files.length; i++) {
			File f = new File(path + offset + files[i]);
			BufferedInputStream in = null;
			BufferedOutputStream out = null;
			byte[] buf = new byte[4096];

			if (f.exists() && !f.isDirectory()) {
				in = new BufferedInputStream(new FileInputStream(path + offset + files[i]));
				out = new BufferedOutputStream(new FileOutputStream(target + offset + files[i]));

				int len = 0;

				while (null != in) {
					len = in.read(buf);

					if (java.io.StreamTokenizer.TT_EOF == len) {
						break;
					}

					out.write(buf, 0, len);
				}

				out.flush();
				out.close();
			} else if (f.exists()) {
				if (!files[i].endsWith("/")) {
					files[i] = files[i] + "/";
				}

				File f2 = new File(target + offset + files[i]);

				if (!f.exists()) {
					f.mkdir();
				}

				this.copy(f.list(), path, offset + files[i], target);
			}
		}
	}

	public synchronized void blockedTransfer(int index) {
		this.tmpindex = index;

		Runnable r = () -> { // --------------- local -------------------

			boolean block = !net.sf.jftp.config.Settings.getEnableMultiThreading();

			if (!(this.con instanceof net.sf.jftp.net.FtpConnection)) {
				block = true;
			}

			if (block || net.sf.jftp.config.Settings.getNoUploadMultiThreading()) {
				this.lock(false);
			}

			this.transfer(this.tmpindex);

			if (block || net.sf.jftp.config.Settings.getNoUploadMultiThreading()) {
				this.unlock(false);
			}

		};

		Thread t = new Thread(r);
		t.start();
	}

	/**
	 * Lock the gui.
	 */
	public void lock(boolean first) {
		JFtp.uiBlocked = true;
		this.jl.setEnabled(false);

		if (!first) {
			JFtp.remoteDir.lock(true);
		}
	}

	/**
	 * Unlock the gui.
	 */
	public void unlock(boolean first) {
		JFtp.uiBlocked = false;
		this.jl.setEnabled(true);

		if (!first) {
			JFtp.remoteDir.unlock(true);
		}
	}

	public void fresh() {
		JFtp.setAppCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		String i = "";
		int idx = this.jl.getSelectedIndex();

		if (0 <= idx) {
			Object o = this.jl.getSelectedValue();

			if (null != o) {
				i = o.toString();
			}
		}

		this.chdir(this.path);

		if ((0 <= idx) && (idx < this.jl.getModel().getSize())) {
			if (this.jl.getModel().getElementAt(idx).toString().equals(i)) {
				this.jl.setSelectedIndex(idx);
			} else {
				this.jl.setSelectedIndex(0);
			}
		}

		this.update();

		JFtp.setAppCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	public synchronized void transfer() {
		boolean[] bFileSelected = new boolean[this.dirEntry.length + 1];
		net.sf.jftp.gui.base.dir.DirEntry[] cacheEntry = new net.sf.jftp.gui.base.dir.DirEntry[this.dirEntry.length];
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

	public void startTransfer(net.sf.jftp.gui.base.dir.DirEntry entry) {
		if (this.con instanceof FilesystemConnection && JFtp.remoteDir.getCon() instanceof FtpConnection) {
			if ((net.sf.jftp.config.Settings.smallSizeUp > entry.getRawSize()) && !entry.isDirectory()) {
				JFtp.remoteDir.getCon().upload(this.path + entry.file);
			} else {
				JFtp.remoteDir.getCon().handleUpload(this.path + entry.file);
			}
		} else if (this.con instanceof FtpConnection && JFtp.remoteDir.getCon() instanceof FtpConnection) {
			if (entry.isDirectory()) {
				Log.debug("Directory transfer between remote connections is not supported yet!");

				return;
			}

			Log.out("direct ftp transfer started (upload)");
			JFtp.remoteDir.getCon().upload(entry.file, JFtp.localDir.getCon().getDownloadInputStream(this.path + entry.file));
		} else if (this.con instanceof FtpConnection && JFtp.remoteDir.getCon() instanceof FilesystemConnection) {
			this.con.setLocalPath(JFtp.remoteDir.getPath());
			this.con.handleDownload(this.path + entry.file);
		} else if (this.con instanceof FilesystemConnection && JFtp.remoteDir.getCon() instanceof FilesystemConnection) {
			this.con.upload(entry.file);
			JFtp.remoteDir.actionPerformed(this.con, "FRESH");
		} else if (this.con instanceof FilesystemConnection) {
			JFtp.remoteDir.getCon().handleUpload(entry.file);
			JFtp.remoteDir.actionPerformed(this.con, "FRESH");
		} else {
			if (entry.isDirectory()) {
				Log.debug("Directory transfer between remote connections is not supported yet!");

				return;
			}

			Log.out("direct transfer started (upload)");
			JFtp.remoteDir.getCon().upload(entry.file, JFtp.localDir.getCon().getDownloadInputStream(this.path + entry.file));
			JFtp.remoteDir.actionPerformed(this.con, "FRESH");
		}
	}

	public void transfer(int i) {
		if (-2 == i) {
			this.transfer();

		} else if (this.dirEntry[i].selected) {
			this.startTransfer(this.dirEntry[i]);
		}
	}

	public void safeUpdate() {
		UpdateDaemon.updateLocalDir();
	}

	public void actionPerformed(Object target, String msg) {
		this.safeUpdate();
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

		if (JFtp.dList.sizeCache.containsKey(file)) {
			s = (Long) net.sf.jftp.JFtp.dList.sizeCache.get(file);
		} else {
			s = new File(this.getPath() + file).length();
			JFtp.dList.sizeCache.put(file, s);
		}

		this.dList.updateList(file, type, bytes, s);
	}

	public void connectionInitialized(BasicConnection con) {
	}

	public void actionFinished(BasicConnection con) {
		//fresh();
		this.safeUpdate();
	}

	public void connectionFailed(BasicConnection con, String reason) {
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
				this.sorter.removeItem("Date");
				this.dateEnabled = false;
				Settings.showLocalDateNoSize = false;
				UpdateDaemon.updateRemoteDirGUI();
			}
		}
	}

	public void updateRemoteDirectory(BasicConnection c) {
		if (this.con instanceof FtpConnection) {
			this.path = ((FtpConnection) this.con).getCachedPWD();
		} else if (this.con instanceof SmbConnection && !this.path.startsWith("smb://")) {
			this.path = this.con.getPWD();
		} else {
			this.path = this.con.getPWD();
		}

		this.safeUpdate();
	}

	private void setZipFilePath(net.sf.jftp.gui.base.dir.DirEntry entry) {
		JFtp.setAppCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		String n = entry.toString();
		String tmp = this.path + n + "-dir/";
		Log.debug("\nExtracting: " + this.path + n);

		File tmpDir = new File(tmp);

		if (tmpDir.exists() || tmpDir.mkdir()) {
			try {
				ZipFile z = new ZipFile(this.path + n);
				Enumeration e = z.entries();

				while (e.hasMoreElements()) {
					ZipEntry z1 = (ZipEntry) e.nextElement();
					Log.debug("-> " + z1.getName());

					BufferedInputStream in = new BufferedInputStream(z.getInputStream(z1));
					BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tmp + z1.getName()));
					byte[] buf = new byte[8192];

					while (java.io.StreamTokenizer.TT_EOF != in.read(buf, 0, 8192)) {
						out.write(buf);
					}

					out.flush();
					out.close();
					in.close();
				}

				this.chdir(tmp);
			} catch (IOException ex) {
				ex.printStackTrace();
				Log.debug("ERROR: " + ex);
			}
		} else {
			Log.debug("Cannot create directory, skipping extraction...");
		}

		JFtp.setAppCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	public void showContentWindow(String url, net.sf.jftp.gui.base.dir.DirEntry d) {
		//------- popup -> run
		if (Settings.runtimeCommands > 0 && url.startsWith("popup-run@")) {
			String ext = url.substring(10);

			try {
				System.out.println("xx: " + ext);
				if (!Settings.askToRun || (Settings.askToRun && UITool.askToRun(this))) UIUtils.runCommand(ext);
			} catch (Exception ex) {
				Log.out("Could not launch file: " + ext);
			}

			return;
		}

		if (d.toString().endsWith(".zip")) {
			this.setZipFilePath(d);
		} else {
			try {
				url = "file://" + url;
				String ext = url.toLowerCase();



				if (Settings.runtimeCommands > 1) {
					try {
						if (!Settings.askToRun || (Settings.askToRun && UITool.askToRun(this))) {
							UIUtils.runCommand(ext);
							return;
						}
					} catch (Exception ex) {
						Log.out("Could not launch file: " + ext);
					}
				}

				if (200000 < d.getRawSize()) {
					Log.debug("File is too big - 200kb is the maximum, sorry.");

					return;
				}

				HPanel f = new HPanel();

				JEditorPane pane = new JEditorPane(url);
				pane.setEditable(false);

				if (!pane.getEditorKit().getContentType().equals("text/html") && !pane.getEditorKit().getContentType().equals("text/rtf")) {
					if (!pane.getEditorKit().getContentType().equals("text/plain")) {
						Log.debug("Nothing to do with this filetype - use the buttons if you want to transfer files.");

						return;
					}

					pane.setEditable(false);
				}

				JScrollPane jsp = new JScrollPane(pane);

				f.setLayout(new BorderLayout());
				f.add("Center", jsp);
				JFtp.statusP.jftp.addToDesktop(url, f, 600, 400);
			} catch (Exception ex) {
				Log.debug("File error: " + ex);
			}
		}
	}

	public void keyPressed(KeyEvent e) {
		if (java.awt.event.KeyEvent.VK_ENTER == e.getKeyCode()) {
			Object o = this.jl.getSelectedValue();

			if (null == o) {
				return;
			}

			String tmp = o.toString();

			if (tmp.endsWith("/") || tmp.equals("..")) {
				this.chdir(tmp);
			} else {
				this.showContentWindow(this.path + tmp, (net.sf.jftp.gui.base.dir.DirEntry) o);
			}
		} else if (java.awt.event.KeyEvent.VK_SPACE == e.getKeyCode()) {
			int x = ((net.sf.jftp.gui.base.dir.DirPanel) JFtp.remoteDir).jl.getSelectedIndex();

			if (-1 == x) {
				x = 0;
			}

			((net.sf.jftp.gui.base.dir.DirPanel) JFtp.remoteDir).jl.grabFocus();
			((net.sf.jftp.gui.base.dir.DirPanel) JFtp.remoteDir).jl.setSelectedIndex(x);
		}
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}
}
