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
	private final JComboBox sorter = new JComboBox(sortTypes);
	HImageButton cdUpButton;
	private boolean pathChanged = true;
	private boolean firstGui = true;
	private DefaultListModel jlm;
	private JScrollPane jsp = new JScrollPane(jl);
	private int tmpindex = -1;
	private net.sf.jftp.gui.base.dir.DirEntry currentPopup = null;
	private String sortMode = null;
	private boolean dateEnabled = false;

	/**
	 * LocalDir constructor.
	 */
	public LocalDir() {
		type = "local";
		con = new FilesystemConnection();
		con.addConnectionListener(this);
	}

	/**
	 * LocalDir constructor.
	 */
	public LocalDir(final String path) {
		type = "local";
		this.path = path;
		con = new FilesystemConnection();
		con.addConnectionListener(this);
		con.chdir(path);

	}

	/**
	 * Creates the gui and adds the MouseListener etc.
	 */
	public void gui_init() {
		this.setLayout(new BorderLayout());
		currDirPanel.setFloatable(false);
		buttonPanel.setFloatable(false);

		final FlowLayout f = new FlowLayout(FlowLayout.RIGHT);
		f.setHgap(1);
		f.setVgap(2);

		buttonPanel.setLayout(f);
		buttonPanel.setMargin(new Insets(0, 0, 0, 0));

		runFile.addActionListener(this);
		viewFile.addActionListener(this);
		props.addActionListener(this);
		popupMenu.add(runFile);
		popupMenu.add(viewFile);
		popupMenu.add(props);

		deleteButton = new HImageButton(Settings.deleteImage, deleteString, "Delete selected", this);
		deleteButton.setToolTipText("Delete selected");

		mkdirButton = new HImageButton(Settings.mkdirImage, mkdirString, "Create a new directory", this);
		mkdirButton.setToolTipText("Create directory");

		refreshButton = new HImageButton(Settings.refreshImage, refreshString, "Refresh current directory", this);
		refreshButton.setToolTipText("Refresh directory");
		refreshButton.setRolloverIcon(new ImageIcon(HImage.getImage(this, Settings.refreshImage2)));
		refreshButton.setRolloverEnabled(true);

		cdButton = new HImageButton(Settings.cdImage, cdString, "Change directory", this);
		cdButton.setToolTipText("Change directory");

		uploadButton = new HImageButton(Settings.uploadImage, uploadString, "Upload selected", this);
		uploadButton.setToolTipText("Upload selected");

		zipButton = new HImageButton(Settings.zipFileImage, zipString, "Add selected to new zip file", this);
		zipButton.setToolTipText("Create zip");

		cpButton = new HImageButton(Settings.copyImage, cpString, "Copy selected files to another local dir", this);
		cpButton.setToolTipText("Local copy selected");

		rnButton = new HImageButton(Settings.textFileImage, rnString, "Rename selected file or directory", this);
		rnButton.setToolTipText("Rename selected");

		cdUpButton = new HImageButton(Settings.cdUpImage, cdUpString, "Go to Parent Directory", this);
		cdUpButton.setToolTipText("Go to Parent Directory");

		label.setText("Filesystem: " + StringUtils.cutPath(path));
		label.setSize(this.getSize().width - 10, 24);
		currDirPanel.add(label);
		currDirPanel.setSize(this.getSize().width - 10, 32);
		label.setSize(this.getSize().width - 20, 24);

		p.setLayout(new BorderLayout());
		p.add("North", currDirPanel);

		buttonPanel.add(sorter);

		buttonPanel.add(new JLabel("  "));

		buttonPanel.add(refreshButton);
		buttonPanel.add(new JLabel("  "));

		buttonPanel.add(cpButton);
		buttonPanel.add(rnButton);
		buttonPanel.add(mkdirButton);

		buttonPanel.add(cdButton);
		buttonPanel.add(deleteButton);
		buttonPanel.add(cdUpButton);
		buttonPanel.add(new JLabel("  "));

		buttonPanel.add(zipButton);
		buttonPanel.add(new JLabel("              "));

		buttonPanel.setVisible(true);

		buttonPanel.setSize(this.getSize().width - 10, 32);

		sorter.addActionListener(this);

		p.add("South", buttonPanel);

		final JPanel second = new JPanel();
		second.setLayout(new BorderLayout());
		second.add("Center", p);
		uploadButton.setMinimumSize(new Dimension(50, 50));
		uploadButton.setPreferredSize(new Dimension(50, 50));
		uploadButton.setMaximumSize(new Dimension(50, 50));
		second.add("East", uploadButton);

		this.add("North", second);


		this.setDirList(true);
		jlm = new DefaultListModel();
		jl = new JList(jlm);
		jl.setCellRenderer(new net.sf.jftp.gui.base.dir.DirCellRenderer());
		jl.setVisibleRowCount(Settings.visibleFileRows);

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

					if (index < -1) {
						return;
					}

					final String tgt = jl.getSelectedValue().toString();

					if (index < 0) {
						net.sf.jftp.gui.base.LocalDir.this.doChdir(path + tgt);
					} else if ((dirEntry == null) || (dirEntry.length < index) || (dirEntry[index] == null)) {
					} else if (dirEntry[index].isDirectory()) {
						net.sf.jftp.gui.base.LocalDir.this.doChdir(path + tgt);
					} else {
						net.sf.jftp.gui.base.LocalDir.this.showContentWindow(path + dirEntry[index].toString(), dirEntry[index]);

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

	public void gui(final boolean fakeInit) {
		if (firstGui) {
			this.gui_init();
			firstGui = false;
		}

		if (con instanceof FilesystemConnection) {
			label.setText("Filesystem: " + StringUtils.cutPath(path));
		} else {
			label.setText("Remote: " + StringUtils.cutPath(path));
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

				final net.sf.jftp.gui.base.dir.DirLister dir = new net.sf.jftp.gui.base.dir.DirLister(con, sortMode, Settings.getHideLocalDotNames());

				while (!dir.finished) {
					LocalIO.pause(10);
				}

				if (dir.isOk()) {
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

		this.update();
	}

	public boolean chdir(final String p) {
		if ((JFtp.remoteDir == null) || (p == null) || p.trim().isEmpty()) {
			return false;
		}

		final BasicConnection c = JFtp.remoteDir.getCon();

		if ((c != null) && (c instanceof FtpConnection)) {
			final FtpConnection con = (FtpConnection) c;

			final SaveSet s = new SaveSet(Settings.login_def, con.getHost(), con.getUsername(), con.getPassword(), Integer.toString(con.getPort()), con.getCachedPWD(), con.getLocalPath());
		}

		if (con.chdirNoRefresh(p)) {
			path = con.getPWD();

			if (con instanceof FilesystemConnection) {
				JFtp.remoteDir.getCon().setLocalPath(path);

				this.setDate();
			}

			pathChanged = true;
			this.gui(false);

			return true;
		}

		return false;
	}

	/**
	 * Handles the user events if the ui is unlocked
	 */
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
			final net.sf.jftp.gui.tasks.RemoteCommand rc = new net.sf.jftp.gui.tasks.RemoteCommand();

		} else if (e.getActionCommand().equals("cd")) {
			final String tmp = UITool.getPathFromDialog(path);
			this.chdir(tmp);
		} else if (e.getActionCommand().equals("fresh")) {
			this.fresh();
		} else if (e.getActionCommand().equals("cp")) {
			final Object[] o = jl.getSelectedValues();

			if (o == null) {
				return;
			}

			String tmp = UITool.getPathFromDialog(path);

			if (tmp == null) {
				return;
			}

			if (!tmp.endsWith("/")) {
				tmp = tmp + "/";
			}

			try {
				this.copy(o, path, "", tmp);

				Log.debug("Copy finished...");
			} catch (final Exception ex) {
				ex.printStackTrace();
				Log.debug("Copy failed!");
			}
		} else if (e.getActionCommand().equals("zip")) {
			try {
				final Object[] entry = jl.getSelectedValues();

				if (entry == null) {
					return;
				}

				final String[] tmp = new String[entry.length];

				for (int i = 0; i < tmp.length; i++) {
					tmp[i] = entry[i].toString();
				}

				final NameChooser n = new NameChooser();
				final String name = n.text.getText();
				final ZipFileCreator z = new ZipFileCreator(tmp, path, name);
				this.fresh();
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		} else if (e.getActionCommand().equals("->")) {
			this.blockedTransfer(-2);
		} else if (e.getActionCommand().equals("<-")) {
			this.blockedTransfer(-2);
		} else if (e.getActionCommand().equals("rn")) {
			final Object[] target = jl.getSelectedValues();

			if ((target == null) || (target.length == 0)) {
				Log.debug("No file selected");

				return;
			} else if (target.length > 1) {
				Log.debug("Too many files selected");

				return;
			}

			final String val = JOptionPane.showInternalInputDialog(JFtp.desktop, "Choose a name...");

			if (val != null) {
				if (!con.rename(target[0].toString(), val)) {
					Log.debug("Rename failed.");
				} else {
					Log.debug("Successfully renamed.");
					this.fresh();
				}
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
		} else if (e.getSource() == viewFile) {
			if (currentPopup.isDirectory()) {
				Log.debug("This is a directory, not a file.");

				return;
			}

			final String url = JFtp.localDir.getPath() + currentPopup.toString();
			this.showContentWindow(url, currentPopup);
		} else if (e.getSource() == runFile) {
			if (currentPopup.isDirectory()) {
				Log.debug("This is a directory, not a file.");

				return;
			}

			final String url = JFtp.localDir.getPath() + currentPopup.toString();
			this.showContentWindow("popup-run@" + url, currentPopup);
		} else if (e.getSource() == sorter) {
			sortMode = (String) sorter.getSelectedItem();

			Settings.showLocalDateNoSize = sortMode.equals("Date");

			this.fresh();
		} else if (e.getActionCommand().equals("cdUp")) {
			this.chdir("..");
		}
	}

	private void copy(final Object[] fRaw, final String path, final String offset, final String target) throws Exception {
		final String[] files = new String[fRaw.length];

		for (int j = 0; j < fRaw.length; j++) {
			files[j] = fRaw[j].toString();
		}

		for (int i = 0; i < files.length; i++) {
			final File f = new File(path + offset + files[i]);
			BufferedInputStream in = null;
			BufferedOutputStream out = null;
			final byte[] buf = new byte[4096];

			if (f.exists() && !f.isDirectory()) {
				in = new BufferedInputStream(new FileInputStream(path + offset + files[i]));
				out = new BufferedOutputStream(new FileOutputStream(target + offset + files[i]));

				int len = 0;

				while (in != null) {
					len = in.read(buf);

					if (len == StreamTokenizer.TT_EOF) {
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

				final File f2 = new File(target + offset + files[i]);

				if (!f.exists()) {
					f.mkdir();
				}

				this.copy(f.list(), path, offset + files[i], target);
			}
		}
	}

	public synchronized void blockedTransfer(final int index) {
		tmpindex = index;

		final Runnable r = () -> { // --------------- local -------------------

			boolean block = !net.sf.jftp.config.Settings.getEnableMultiThreading();

			if (!(con instanceof net.sf.jftp.net.FtpConnection)) {
				block = true;
			}

			if (block || net.sf.jftp.config.Settings.getNoUploadMultiThreading()) {
				this.lock(false);
			}

			this.transfer(tmpindex);

			if (block || net.sf.jftp.config.Settings.getNoUploadMultiThreading()) {
				this.unlock(false);
			}

		};

		final Thread t = new Thread(r);
		t.start();
	}

	/**
	 * Lock the gui.
	 */
	public void lock(final boolean first) {
		JFtp.uiBlocked = true;
		jl.setEnabled(false);

		if (!first) {
			JFtp.remoteDir.lock(true);
		}
	}

	/**
	 * Unlock the gui.
	 */
	public void unlock(final boolean first) {
		JFtp.uiBlocked = false;
		jl.setEnabled(true);

		if (!first) {
			JFtp.remoteDir.unlock(true);
		}
	}

	public void fresh() {
		JFtp.setAppCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		String i = "";
		final int idx = jl.getSelectedIndex();

		if (idx >= 0) {
			final Object o = jl.getSelectedValue();

			if (o != null) {
				i = o.toString();
			}
		}

		this.chdir(path);

		if ((idx >= 0) && (idx < jl.getModel().getSize())) {
			if (jl.getModel().getElementAt(idx).toString().equals(i)) {
				jl.setSelectedIndex(idx);
			} else {
				jl.setSelectedIndex(0);
			}
		}

		this.update();

		JFtp.setAppCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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

	public void startTransfer(final net.sf.jftp.gui.base.dir.DirEntry entry) {
		if (con instanceof FilesystemConnection && JFtp.remoteDir.getCon() instanceof FtpConnection) {
			if ((entry.getRawSize() < Settings.smallSizeUp) && !entry.isDirectory()) {
				JFtp.remoteDir.getCon().upload(path + entry.file);
			} else {
				JFtp.remoteDir.getCon().handleUpload(path + entry.file);
			}
		} else if (con instanceof FtpConnection && JFtp.remoteDir.getCon() instanceof FtpConnection) {
			if (entry.isDirectory()) {
				Log.debug("Directory transfer between remote connections is not supported yet!");

				return;
			}

			Log.out("direct ftp transfer started (upload)");
			JFtp.remoteDir.getCon().upload(entry.file, JFtp.localDir.getCon().getDownloadInputStream(path + entry.file));
		} else if (con instanceof FtpConnection && JFtp.remoteDir.getCon() instanceof FilesystemConnection) {
			con.setLocalPath(JFtp.remoteDir.getPath());
			con.handleDownload(path + entry.file);
		} else if (con instanceof FilesystemConnection && JFtp.remoteDir.getCon() instanceof FilesystemConnection) {
			con.upload(entry.file);
			JFtp.remoteDir.actionPerformed(con, "FRESH");
		} else if (con instanceof FilesystemConnection) {
			JFtp.remoteDir.getCon().handleUpload(entry.file);
			JFtp.remoteDir.actionPerformed(con, "FRESH");
		} else {
			if (entry.isDirectory()) {
				Log.debug("Directory transfer between remote connections is not supported yet!");

				return;
			}

			Log.out("direct transfer started (upload)");
			JFtp.remoteDir.getCon().upload(entry.file, JFtp.localDir.getCon().getDownloadInputStream(path + entry.file));
			JFtp.remoteDir.actionPerformed(con, "FRESH");
		}
	}

	public void transfer(final int i) {
		if (i == -2) {
			this.transfer();

		} else if (dirEntry[i].selected) {
			this.startTransfer(dirEntry[i]);
		}
	}

	public void safeUpdate() {
		UpdateDaemon.updateLocalDir();
	}

	public void actionPerformed(final Object target, final String msg) {
		this.safeUpdate();
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
			s = new File(this.getPath() + file).length();
			JFtp.dList.sizeCache.put(file, s);
		}

		dList.updateList(file, type, bytes, s);
	}

	public void connectionInitialized(final BasicConnection con) {
	}

	public void actionFinished(final BasicConnection con) {
		//fresh();
		this.safeUpdate();
	}

	public void connectionFailed(final BasicConnection con, final String reason) {
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
				sorter.removeItem("Date");
				dateEnabled = false;
				Settings.showLocalDateNoSize = false;
				UpdateDaemon.updateRemoteDirGUI();
			}
		}
	}

	public void updateRemoteDirectory(final BasicConnection c) {
		if (con instanceof FtpConnection) {
			path = ((FtpConnection) con).getCachedPWD();
		} else if (con instanceof SmbConnection && !path.startsWith("smb://")) {
			path = con.getPWD();
		} else {
			path = con.getPWD();
		}

		this.safeUpdate();
	}

	private void setZipFilePath(final net.sf.jftp.gui.base.dir.DirEntry entry) {
		JFtp.setAppCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		final String n = entry.toString();
		final String tmp = path + n + "-dir/";
		Log.debug("\nExtracting: " + path + n);

		final File tmpDir = new File(tmp);

		if (tmpDir.exists() || tmpDir.mkdir()) {
			try {
				final ZipFile z = new ZipFile(path + n);
				final Enumeration e = z.entries();

				while (e.hasMoreElements()) {
					final ZipEntry z1 = (ZipEntry) e.nextElement();
					Log.debug("-> " + z1.getName());

					final BufferedInputStream in = new BufferedInputStream(z.getInputStream(z1));
					final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tmp + z1.getName()));
					final byte[] buf = new byte[8192];

					while (in.read(buf, 0, 8192) != StreamTokenizer.TT_EOF) {
						out.write(buf);
					}

					out.flush();
					out.close();
					in.close();
				}

				this.chdir(tmp);
			} catch (final IOException ex) {
				ex.printStackTrace();
				Log.debug("ERROR: " + ex);
			}
		} else {
			Log.debug("Cannot create directory, skipping extraction...");
		}

		JFtp.setAppCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	public void showContentWindow(String url, final net.sf.jftp.gui.base.dir.DirEntry d) {
		//------- popup -> run
		if (Settings.runtimeCommands > 0 && url.startsWith("popup-run@")) {
			final String ext = url.substring(10);

			try {
				System.out.println("xx: " + ext);
				if (!Settings.askToRun || (Settings.askToRun && UITool.askToRun(this))) UIUtils.runCommand(ext);
			} catch (final Exception ex) {
				Log.out("Could not launch file: " + ext);
			}

			return;
		}

		if (d.toString().endsWith(".zip")) {
			this.setZipFilePath(d);
		} else {
			try {
				url = "file://" + url;
				final String ext = url.toLowerCase();



				if (Settings.runtimeCommands > 1) {
					try {
						if (!Settings.askToRun || (Settings.askToRun && UITool.askToRun(this))) {
							UIUtils.runCommand(ext);
							return;
						}
					} catch (final Exception ex) {
						Log.out("Could not launch file: " + ext);
					}
				}

				if (d.getRawSize() > 200000) {
					Log.debug("File is too big - 200kb is the maximum, sorry.");

					return;
				}

				final HPanel f = new HPanel();

				final JEditorPane pane = new JEditorPane(url);
				pane.setEditable(false);

				if (!pane.getEditorKit().getContentType().equals("text/html") && !pane.getEditorKit().getContentType().equals("text/rtf")) {
					if (!pane.getEditorKit().getContentType().equals("text/plain")) {
						Log.debug("Nothing to do with this filetype - use the buttons if you want to transfer files.");

						return;
					}

					pane.setEditable(false);
				}

				final JScrollPane jsp = new JScrollPane(pane);

				f.setLayout(new BorderLayout());
				f.add("Center", jsp);
				JFtp.statusP.jftp.addToDesktop(url, f, 600, 400);
			} catch (final Exception ex) {
				Log.debug("File error: " + ex);
			}
		}
	}

	public void keyPressed(final KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			final Object o = jl.getSelectedValue();

			if (o == null) {
				return;
			}

			final String tmp = o.toString();

			if (tmp.endsWith("/") || tmp.equals("..")) {
				this.chdir(tmp);
			} else {
				this.showContentWindow(path + tmp, (net.sf.jftp.gui.base.dir.DirEntry) o);
			}
		} else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			int x = ((net.sf.jftp.gui.base.dir.DirPanel) JFtp.remoteDir).jl.getSelectedIndex();

			if (x == -1) {
				x = 0;
			}

			((net.sf.jftp.gui.base.dir.DirPanel) JFtp.remoteDir).jl.grabFocus();
			((net.sf.jftp.gui.base.dir.DirPanel) JFtp.remoteDir).jl.setSelectedIndex(x);
		}
	}

	public void keyReleased(final KeyEvent e) {
	}

	public void keyTyped(final KeyEvent e) {
	}
}
