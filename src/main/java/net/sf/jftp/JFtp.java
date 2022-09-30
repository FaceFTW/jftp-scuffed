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
package net.sf.jftp;

import net.sf.jftp.config.Settings;
import net.sf.jftp.gui.framework.FileTransferable;
import net.sf.jftp.gui.framework.GUIDefaults;
import net.sf.jftp.gui.framework.HDesktopBackground;
import net.sf.jftp.gui.framework.HImage;
import net.sf.jftp.gui.framework.HImageButton;
import net.sf.jftp.gui.hostchooser.HostChooser;
import net.sf.jftp.gui.hostchooser.SftpHostChooser;
import net.sf.jftp.gui.tasks.HostInfo;
import net.sf.jftp.net.BasicConnection;
import net.sf.jftp.net.ConnectionHandler;
import net.sf.jftp.net.ConnectionListener;
import net.sf.jftp.net.FilesystemConnection;
import net.sf.jftp.net.FtpConnection;
import net.sf.jftp.system.LocalIO;
import net.sf.jftp.system.UpdateDaemon;
import net.sf.jftp.system.logging.Log;
import net.sf.jftp.system.logging.Logger;
import net.sf.jftp.tools.RSSFeeder;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.DataInputStream;
import java.io.File;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Hashtable;


public class JFtp extends JPanel implements WindowListener, ComponentListener, Logger, ChangeListener, InternalFrameListener {
	public static final int CAPACITY = 9;
	public static final int CONNECTION_DATA_LENGTH = 10;
	private static final ConnectionHandler defaultConnectionHandler = new ConnectionHandler();
	private static final Hashtable<String, JInternalFrame> internalFrames = new Hashtable<>();
	public static boolean mainUsed = false;
	public static net.sf.jftp.gui.base.StatusPanel statusP;
	public static JLabel statusL = new JLabel("Welcome to JFtp...                                                            ");
	public static JFrame mainFrame;
	public static net.sf.jftp.gui.base.dir.Dir localDir;
	public static net.sf.jftp.gui.base.dir.Dir remoteDir;
	public static final net.sf.jftp.gui.base.DownloadList dList = new net.sf.jftp.gui.base.DownloadList();
	public static final net.sf.jftp.gui.base.DownloadQueue dQueue = new net.sf.jftp.gui.base.DownloadQueue();
	public static boolean uiBlocked = false;
	public static final HostInfo hostinfo = new HostInfo();
	public static final JDesktopPane desktop = new JDesktopPane();
	public static JTextArea log;
	public static boolean doScroll = true;
	public static net.sf.jftp.gui.base.AppMenuBar menuBar = null;
	public static DropTarget dropTarget;
	public static DropTargetListener dtListener;
	public static final int acceptableActions = DnDConstants.ACTION_COPY;
	private static JScrollPane logSp;
	private final boolean initSize = true;
	private final String oldText = "";
	private final JToolBar bottomBar = new JToolBar();

	private final JSplitPane workP = null;

	private final JSplitPane logP = null;
	public final JTabbedPane remoteConnectionPanel = new JTabbedPane();
	public final JTabbedPane localConnectionPanel = new JTabbedPane();
	public HostChooser hc;
	public RSSFeeder feeder;
	private HDesktopBackground background;
	private JInternalFrame j1;
	private JInternalFrame j2;
	private JInternalFrame j3;
	private JInternalFrame j4;
	private JInternalFrame j5;
	private String buffer = "";
	private long oldtime = 0;

	public JFtp() {
		Log.setLogger(this);

		if (statusP != null) {
			statusP.remove(statusP.close);
		}

		this.init();
		this.displayGUI();
	}

	public JFtp(final boolean mainUsed) {
		Log.setLogger(this);
		JFtp.mainUsed = mainUsed;
		this.init();
		this.displayGUI();
	}

	public static String getHost() {
		return statusP.getHost();
	}

	public static void setHost(final String which) {
		statusP.setHost(which);
	}

	public static void localUpdate() {
		localDir.fresh();
	}

	public static void remoteUpdate() {
		remoteDir.fresh();
	}

	public static void safeDisconnect() {
		final BasicConnection con = remoteDir.getCon();

		if ((con != null) && con.isConnected()) {
			try {
				con.disconnect();
			} catch (final Exception ex) {
			}
		}

		try {
			final FilesystemConnection c = new FilesystemConnection();
			c.addConnectionListener((ConnectionListener) remoteDir);
			remoteDir.setCon(c);

			if (!c.chdir("/")) {
				c.chdir("C:\\");
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void setSocksProxyOptions(final String proxy, final String port) {
		if (proxy.isEmpty() || port.isEmpty()) {
			return;
		}

		final java.util.Properties sysprops = System.getProperties();

		sysprops.remove("socksProxyHost");
		sysprops.remove("socksProxyPort");

		sysprops.put("socksProxyHost", proxy);
		sysprops.put("socksProxyPort", port);

		Log.out("socks proxy: " + sysprops.get("socksProxyHost") + ":" + sysprops.get("socksProxyPort"));
	}

	public static void main(final String[] argv) {
		try {
			final long start = System.currentTimeMillis();

			Log.out("starting up jftp...");
			System.setProperty("sshtools.logfile", Settings.appHomeDir + "log4.txt");

			Settings.enableResuming = true;
			Settings.enableUploadResuming = true;
			Settings.noUploadResumingQuestion = false;

			setSocksProxyOptions(Settings.getSocksProxyHost(), Settings.getSocksProxyPort());

			final JFtp jftp = new JFtp(true);


			if (argv.length > 0) {
				if (argv[0].contains("sftp:")) {
					new SftpHostChooser().update(argv[0]);
				} else {
					jftp.hc.update(argv[0]);
				}
			}

			Log.out("jftp is up and running.");
			final long end = System.currentTimeMillis();
			Log.out("startup time: " + (end - start) + "ms.");

			//batch processing
			if (argv.length > 1) {
				int idx = 1;
				if (argv[idx].startsWith("localDir=")) {
					final String path = argv[idx].substring("localDir=".length());
					Log.debug("Setting local Dir: " + path);
					localDir.getCon().chdir(path);
					idx++;

					remoteDir.getCon().setLocalPath(localDir.getCon().getPWD());
				}

				while (!remoteDir.getCon().isConnected()) {
					LocalIO.pause(50);
				}

				for (int i = idx; i < argv.length; i++) {
					String path = null;
					String file = argv[i];

					if (argv[i].contains("/")) {
						path = argv[i].substring(0, argv[i].lastIndexOf("/"));
						file = argv[i].substring(argv[i].lastIndexOf("/") + 1);
					}
					Log.debug("Download: " + path + ":" + file);

					if (path != null) {
						remoteDir.getCon().chdir(path);
					}
					remoteDir.getCon().download(file);
				}
			}
		} catch (final Error ex) {
			ex.printStackTrace();
		}
	}

	public static void clearLog() {
		log.setText("");
		logSp.paintImmediately(0, 0, logSp.getSize().width, logSp.getSize().height);
	}

	public static String getVersion() {
		try {
			URL u = ClassLoader.getSystemResource(Settings.readme);

			if (u == null) {
				u = HImage.class.getResource("/" + Settings.readme);
			}

			final DataInputStream i = new DataInputStream(u.openStream());
			String tmp = i.readLine();
			tmp = tmp.substring(tmp.lastIndexOf(">") + 1);
			tmp = tmp.substring(0, tmp.indexOf("<"));

			return tmp;
		} catch (final Exception ex) {
		}

		return "";
	}

	public static ConnectionHandler getConnectionHandler() {
		final BasicConnection con = remoteDir.getCon();

		if ((con != null) && con instanceof FtpConnection) {
			return ((FtpConnection) con).getConnectionHandler();
		} else {
			return defaultConnectionHandler;
		}
	}

	public static void setAppCursor(final Cursor cursor) {

		if (JFtp.mainFrame != null) {
			if (cursor.getType() != Cursor.DEFAULT_CURSOR) {
				mainFrame.getGlassPane().addMouseListener(new MouseAdapter() {
				});
				mainFrame.getGlassPane().setCursor(cursor);
				mainFrame.getGlassPane().setVisible(true);
			} else {
				mainFrame.getGlassPane().setCursor(cursor);
				for (int i = 0; i < mainFrame.getGlassPane().getMouseListeners().length; i++) {
					mainFrame.getGlassPane().removeMouseListener(mainFrame.getGlassPane().getMouseListeners()[i]);
				}
				mainFrame.getGlassPane().setVisible(false);
			}
		}

	}

	public static void updateMenuBar() {
		menuBar.resetFileItems();
	}

	public void init() {
		dtListener = new DTListener();
		dropTarget = new DropTarget(this, acceptableActions, dtListener, true);

		this.setLayout(new BorderLayout());

		this.setBackground(GUIDefaults.mainBack);
		this.setForeground(GUIDefaults.front);

		statusP = new net.sf.jftp.gui.base.StatusPanel(this);
		this.add("North", statusP);

		localDir = new net.sf.jftp.gui.base.LocalDir(Settings.defaultWorkDir);
		localDir.setDownloadList(dList);

		remoteDir = new net.sf.jftp.gui.base.RemoteDir();
		remoteDir.setDownloadList(dList);

		final Dimension d = Settings.getWindowSize();
		this.setPreferredSize(d);
		this.setSize(d);

		final int width = (int) d.getWidth();
		final int height = (int) d.getHeight();

		dList.setMinimumSize(new Dimension((int) (width / 2.2), (int) (height * 0.20)));
		dList.setPreferredSize(new Dimension((int) (width / 2.2), (int) (height * 0.25)));
		dList.setSize(new Dimension((int) (width / 2.2), (int) (height * 0.25)));

		desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		this.addBackgroundImage();

		j1 = new JInternalFrame("Local filesystem", true, false, true, true);
		j1.setMinimumSize(new Dimension(300, 300));
		j1.setLocation(5, 5);
		localConnectionPanel.addTab("file://", null, (Component) localDir, "Filesystem");
		localConnectionPanel.setSelectedIndex(0);
		localConnectionPanel.addChangeListener(this);
		j1.getContentPane().add(localConnectionPanel);
		localDir.fresh();
		desktop.add(j1);
		j1.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
		j1.addInternalFrameListener(this);
		j1.pack();
		j1.setSize(new Dimension(460, 480));
		j1.show();

		j2 = new JInternalFrame("Remote connections", true, false, true, true);
		j2.setLocation(470, 5);
		remoteConnectionPanel.addTab("file://", null, (Component) remoteDir, "Filesystem");
		remoteConnectionPanel.setSelectedIndex(0);
		remoteConnectionPanel.addChangeListener(this);
		j2.getContentPane().add(remoteConnectionPanel);
		desktop.add(j2);
		j2.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
		j2.addInternalFrameListener(this);
		j2.pack();
		j2.setSize(new Dimension(460, j1.getSize().height));
		j2.show();

		log = new JTextArea();
		log.setBackground(GUIDefaults.light);
		log.setForeground(GUIDefaults.front);
		log.setEditable(false);
		logSp = new JScrollPane(log);
		logSp.setSize(new Dimension(428, 148));

		j5 = new JInternalFrame("Queue System", true, false, true, true);
		j5.setLocation(5, 500);
		j5.getContentPane().add(dQueue, BorderLayout.CENTER);
		desktop.add(j5);
		j5.pack();
		j5.setSize(new Dimension(440, 150));
		j5.show();

		j3 = new JInternalFrame("Log", true, false, true, true);

		final HImageButton clearButton = new HImageButton(Settings.clearLogImage, "clearLog", "Clear Log", e -> net.sf.jftp.JFtp.clearLog());
		final HImageButton lockButton = new HImageButton(Settings.scrollLockImage, "scrollLock", "Toggle Scroll Lock", e -> {
			net.sf.jftp.JFtp.doScroll = !net.sf.jftp.JFtp.doScroll;

			net.sf.jftp.JFtp.statusP.status("Scroll lock " + (net.sf.jftp.JFtp.doScroll ? "deactivated" : "activated"));
		});

		final JPanel logSpPanel = new JPanel();
		final JPanel logButtonPanel = new JPanel();
		logButtonPanel.setPreferredSize(new Dimension(18, 100));
		logButtonPanel.add(clearButton);
		logButtonPanel.add(lockButton);
		logSpPanel.setLayout(new BorderLayout(2, 2));
		logSpPanel.add("West", logButtonPanel);
		logSpPanel.add("Center", logSp);

		final int x = desktop.getSize().width / 2;
		j3.setLocation(5, 525);
		j3.getContentPane().add(logSpPanel, BorderLayout.CENTER);
		desktop.add(j3);
		j3.pack();
		j3.setSize(new Dimension(440, 150));
		j3.show();

		j4 = new JInternalFrame("Download Manager", true, false, true, true);
		j4.setLocation(450, 500);
		j4.getContentPane().add(dList, BorderLayout.CENTER);
		desktop.add(j4);
		j4.pack();
		j4.setSize(new Dimension(480, 175));
		j4.show();

		j1.toFront();
		j2.toFront();

		this.add("Center", desktop);

		bottomBar.setFloatable(false);
		bottomBar.add(net.sf.jftp.gui.base.StatusPanel.status, FlowLayout.LEFT);

		if (Settings.getEnableRSS()) {
			this.addRSS();
		}

		this.add("South", bottomBar);

		this.addComponentListener(this);
		this.componentResized(new ComponentEvent(log, 0));

		this.validate();
		this.setVisible(true);

		if (!mainUsed) {
			SwingUtilities.invokeLater(this::chooseHost);
		} else {
			this.chooseHost();
		}

		final net.sf.jftp.gui.base.LogFlusher flusher = new net.sf.jftp.gui.base.LogFlusher();
		final net.sf.jftp.system.UpdateDaemon daemon = new net.sf.jftp.system.UpdateDaemon(this);

	}

	public void addRSS() {
		feeder = new RSSFeeder();
		bottomBar.add(feeder);
		bottomBar.validate();

		System.out.println("dfdfsgsd");
	}

	protected void chooseHost() {
		hc = new HostChooser(this);

		if (!mainUsed) {
			hc.update();
		}
	}

	private void saveInternalPositions() {
		this.saveInternalPosition(j1, "local");
		this.saveInternalPosition(j2, "remote");
		this.saveInternalPosition(j5, "queue");
		this.saveInternalPosition(j3, "log");
		this.saveInternalPosition(j4, "manager");
	}

	private void restoreInternalPositions() {
		if (Settings.getProperty("jftp.iframes.resize").equals("false")) {

		} else {
			this.restoreInternalPosition(j1, "local");
			this.restoreInternalPosition(j2, "remote");
			this.restoreInternalPosition(j5, "queue");
			this.restoreInternalPosition(j3, "log");
			this.restoreInternalPosition(j4, "manager");
		}
	}

	private void restoreInternalPosition(final JInternalFrame f, final String desc) {
		String x = Settings.getProperty("jftp.iframes." + desc + ".x");
		String y = Settings.getProperty("jftp.iframes." + desc + ".y");
		final String w = Settings.getProperty("jftp.iframes." + desc + ".width");
		final String h = Settings.getProperty("jftp.iframes." + desc + ".height");

		if (x.contains(".")) x = x.substring(0, x.indexOf("."));
		if (y.contains(".")) y = y.substring(0, y.indexOf("."));

		try {
			f.setLocation(Integer.parseInt(x), Integer.parseInt(y));
			f.setSize(Integer.parseInt(w), Integer.parseInt(h));
		} catch (final Exception ex) {
			Log.out("Can not set internal fram position for: " + desc);
		}
	}

	private void saveInternalPosition(final JInternalFrame f, final String desc) {
		final Point p = f.getLocation();

		Settings.setProperty("jftp.iframes." + desc + ".x", "" + p.getX());
		Settings.setProperty("jftp.iframes." + desc + ".y", "" + p.getY());
		Settings.setProperty("jftp.iframes." + desc + ".width", f.getWidth());
		Settings.setProperty("jftp.iframes." + desc + ".height", f.getHeight());
	}

	public void windowClosing(final WindowEvent e) {
		this.saveInternalPositions();

		Settings.setProperty("jftp.window.width", this.getWidth());
		Settings.setProperty("jftp.window.height", this.getHeight());

		if (!mainUsed) {
			Settings.setProperty("jftp.window.x", (int) this.getLocationOnScreen().getX());
			Settings.setProperty("jftp.window.y", (int) this.getLocationOnScreen().getY());
		} else {
			Settings.setProperty("jftp.window.x", (int) mainFrame.getLocationOnScreen().getX());
			Settings.setProperty("jftp.window.y", (int) mainFrame.getLocationOnScreen().getY());
		}

		Settings.save();
		safeDisconnect();

		if (Settings.isStandalone) {
			System.exit(0);
		} else {
			mainFrame.dispose();
		}
	}

	public void windowClosed(final WindowEvent e) {
	}

	public void windowActivated(final WindowEvent e) {
	}

	public void windowDeactivated(final WindowEvent e) {
	}

	public void windowIconified(final WindowEvent e) {
	}

	public void windowDeiconified(final WindowEvent e) {
	}

	public void windowOpened(final WindowEvent e) {
	}

	public void componentHidden(final ComponentEvent e) {
	}

	public void componentMoved(final ComponentEvent e) {
	}

	public void componentShown(final ComponentEvent e) {
	}

	public void componentResized(final ComponentEvent e) {
		localDir.actionPerformed(this, "local");
		remoteDir.actionPerformed(this, "remote");
		desktop.remove(background);
		this.addBackgroundImage();

		this.validate();

		System.out.println(net.sf.jftp.gui.base.StatusPanel.status);
		net.sf.jftp.gui.base.StatusPanel.status.fresh();
	}

	public void addBackgroundImage() {
		try {
			background = new HDesktopBackground(Settings.background, null);
			background.setBounds(0, 0, this.getSize().width, this.getSize().height);
			desktop.add(background, new Integer(Integer.MIN_VALUE));
		} catch (final Exception ex) {
			Log.out(Settings.background + " missing, no background image used");
		}
	}

	protected void displayGUI() {
		UIManager.getLookAndFeelDefaults().put("ClassLoader", this.getClass().getClassLoader());

		final String tmp = Settings.getLookAndFeel();
		if (tmp != null) {
			this.setLookAndFeel(Settings.getLookAndFeel());
		} else {
			this.setLookAndFeel("net.sourceforge.mlf.metouia.MetouiaLookAndFeel");
		}

		if ((Settings.getLookAndFeel() == null) || !Settings.getLookAndFeel().equals("com.incors.plaf.kunststoff.KunststoffLookAndFeel")) {
			try {
				Class.forName("com.incors.plaf.kunststoff.KunststoffLookAndFeel");
				UIManager.installLookAndFeel("Kunststoff", "com.incors.plaf.kunststoff.KunststoffLookAndFeel");
			} catch (final ClassNotFoundException cnfe) {
			}
		}

		if ((Settings.getLookAndFeel() == null) || !Settings.getLookAndFeel().equals("net.sourceforge.mlf.metouia.MetouiaLookAndFeel")) {

			try {
				Class.forName("net.sourceforge.mlf.metouia.MetouiaLookAndFeel");
				UIManager.installLookAndFeel("Metouia", "net.sourceforge.mlf.metouia.MetouiaLookAndFeel");
			} catch (final ClassNotFoundException cnfe) {
			}
		}

		mainFrame = new JFrame();
		mainFrame.setLocation(Settings.getWindowLocation());

		mainFrame.setTitle(Settings.title + " - Version " + getVersion());

		mainFrame.setResizable(Settings.resize);
		mainFrame.addWindowListener(this);

		final Image icon = HImage.getImage(this, Settings.iconImage);
		mainFrame.setIconImage(icon);
		mainFrame.setFont(GUIDefaults.font);

		menuBar = new net.sf.jftp.gui.base.AppMenuBar(this);
		mainFrame.setJMenuBar(menuBar);

		mainFrame.getContentPane().setLayout(new BorderLayout());
		mainFrame.getContentPane().add("Center", this);
		SwingUtilities.updateComponentTreeUI(mainFrame);
		mainFrame.pack();
		mainFrame.validate();
		mainFrame.setVisible(true);
		JOptionPane.showMessageDialog(mainFrame, "Welcome to jFTP!");
	}

	private void log(final String msg) {
		if (msg.startsWith("200") || msg.startsWith("227")) {
			if ((msg.indexOf("NOOP") > 0) || (msg.indexOf("Type") > 0) || (msg.indexOf("MODE") > 0) || (msg.indexOf("Passive") > 0)) {
				if (Settings.hideStatus) {
					return;
				}
			}
		} else if (log == null) {
			return;
		}

		if (!msg.isEmpty()) {
			buffer = buffer + " " + msg;
		}

		log.append(buffer);
		buffer = "";

		final long time = System.currentTimeMillis();

		if (((time - oldtime) < Settings.uiRefresh)) {
			UpdateDaemon.updateLog();

			return;
		}

		oldtime = time;

		if (doScroll) {
			final JScrollBar bar = logSp.getVerticalScrollBar();
			bar.setValue(bar.getMaximum());
		}

		this.repaint();
		this.revalidate();
	}

	private void logRaw(final String msg) {
		log.append(" " + msg);
		Log.out("NOTE: logRaw called");
		this.paintImmediately(0, 0, this.getSize().width, this.getSize().height);

	}

	private void log(final String msg, final Throwable throwable) {
		final PrintWriter p = new PrintWriter(new StringWriter());
		throwable.printStackTrace(p);
		this.log(msg);
		this.log(p.toString());
	}

	public void debug(final String msg) {
		this.log(msg + "\n");
	}

	public void debugRaw(final String msg) {
		this.logRaw(msg);
	}

	public void debug(final String msg, final Throwable throwable) {
		this.log(msg, throwable);
	}

	public void warn(final String msg) {
		this.log(msg);
	}

	public void warn(final String msg, final Throwable throwable) {
		this.log(msg, throwable);
	}

	public void error(final String msg) {
		this.log(msg);
	}

	public void error(final String msg, final Throwable throwable) {
		this.log(msg, throwable);
	}

	public void info(final String msg) {
		this.log(msg);
	}

	public void info(final String msg, final Throwable throwable) {
		this.log(msg, throwable);
	}

	public void fatal(final String msg) {
		this.log(msg);
	}

	public void fatal(final String msg, final Throwable throwable) {
		this.log(msg, throwable);
	}

	public void debugSize(final int size, final boolean recv, final boolean last, final String file) {
	}

	public void fireUpdate() {
		LocalIO.pause(200);
		this.repaint();
	}

	public void ensureLogging() {
		if (buffer.isEmpty()) {
			final JScrollBar bar;

			if ((logSp == null) || ((bar = logSp.getVerticalScrollBar()) == null) || bar == null || (bar.getValue() == bar.getMaximum()) || bar.getValueIsAdjusting()) {
				return;
			} else {
				if (doScroll) {
					bar.setValue(bar.getMaximum());
				}

				logSp.repaint();

				this.repaint();
				this.revalidate();
			}

			return;
		}

		Log.out("flushing log buffer...");
		oldtime = 0;
		this.log("");
	}

	public void setLookAndFeel(final String name) {
		if (name == null) {
			return;
		}

		try {
			UIManager.setLookAndFeel(name);

			if (mainFrame != null) {
				SwingUtilities.invokeLater(() -> {
					javax.swing.SwingUtilities.updateComponentTreeUI(mainFrame);
					javax.swing.SwingUtilities.updateComponentTreeUI(net.sf.jftp.JFtp.statusP);
				});
			}
		} catch (final Exception ex) {
			Log.debug("Error setting look and feel: " + ex);
		}
	}

	public void addConnection(final String name, final BasicConnection con) {
		con.addConnectionListener((ConnectionListener) localDir);

		final net.sf.jftp.gui.base.dir.Dir tmp = new net.sf.jftp.gui.base.RemoteDir();
		tmp.setDownloadList(dList);
		con.addConnectionListener((ConnectionListener) tmp);
		tmp.setCon(con);

		final int x = remoteConnectionPanel.getSelectedIndex();
		remoteConnectionPanel.addTab(this.parse(name), null, (Component) tmp, "Switch to: " + this.parse(name));
		remoteConnectionPanel.setSelectedIndex(x + 1);
		j2.setClosable(true);
	}

	public void addLocalConnection(final String name, final BasicConnection con) {
		con.addConnectionListener((ConnectionListener) remoteDir);

		final net.sf.jftp.gui.base.dir.Dir tmp = new net.sf.jftp.gui.base.LocalDir();
		tmp.setDownloadList(dList);
		con.addConnectionListener((ConnectionListener) tmp);
		tmp.setCon(con);

		final int x = localConnectionPanel.getSelectedIndex();
		localConnectionPanel.addTab(this.parse(name), null, (Component) tmp, "Switch to: " + this.parse(name));
		localConnectionPanel.setSelectedIndex(x + 1);
		j1.setClosable(true);
	}

	private String parse(final String what) {
		if (what.contains("@")) {
			return what.substring(what.lastIndexOf("@") + 1);
		} else {
			return what;
		}
	}

	public void stateChanged(final ChangeEvent e) {
		remoteDir = (net.sf.jftp.gui.base.dir.Dir) remoteConnectionPanel.getSelectedComponent();
		localDir = (net.sf.jftp.gui.base.dir.Dir) localConnectionPanel.getSelectedComponent();
		remoteDir.getCon().setLocalPath(localDir.getPath());

	}

	public void closeCurrentTab() {
		final int x = remoteConnectionPanel.getSelectedIndex();

		if (x > 0) {
			safeDisconnect();
			remoteConnectionPanel.remove(x);
			remoteConnectionPanel.setSelectedIndex(x - 1);
		}

		if (remoteConnectionPanel.getTabCount() < 2) {
			j2.setClosable(false);
		}
	}

	public void closeCurrentLocalTab() {
		final int x = localConnectionPanel.getSelectedIndex();

		if (x > 0) {
			final BasicConnection con = localDir.getCon();

			if ((con != null) && con.isConnected()) {
				try {
					con.disconnect();
				} catch (final Exception ex) {
				}
			}

			localConnectionPanel.remove(x);
			localConnectionPanel.setSelectedIndex(x - 1);
		}

		if (localConnectionPanel.getTabCount() < 2) {
			j1.setClosable(false);
		}
	}

	public void addToDesktop(final String title, final Component c, final int w, final int h) {
		final JInternalFrame jt = new JInternalFrame(title, false, true, false, true);

		if (w < 500) {
			jt.setLocation(200, 100);
		} else {
			jt.setLocation(80, 100);
		}

		jt.getContentPane().add(c);
		desktop.add(jt);

		internalFrames.put("" + c.hashCode(), jt);

		jt.pack();
		jt.setSize(new Dimension(w, h));
		jt.show();
	}

	public void removeFromDesktop(final int component) {
		final JInternalFrame f = internalFrames.get("" + component);

		if (f != null) {
			f.dispose();

		} else {
			Log.debug("ERROR: " + component + " not found in Hashtable!");
		}
	}

	public void setClosable(final int component, final boolean ok) {
		final JInternalFrame f = internalFrames.get("" + component);

		if (f != null) {
			f.setClosable(ok);
		} else {
			Log.debug("ERROR: " + component + " not found in Hashtable!");
		}
	}

	public void setLocation(final int component, final int x, final int y) {
		final JInternalFrame f = internalFrames.get("" + component);

		if (f != null) {
			f.setLocation(x, y);
		} else {
			Log.debug("ERROR: " + component + " not found in Hashtable!");
		}
	}

	public void internalFrameClosing(final InternalFrameEvent e) {
		if (e.getSource() == j1) {
			this.closeCurrentLocalTab();
		} else if (e.getSource() == j2) {
			this.closeCurrentTab();
		}
	}

	public void internalFrameActivated(final InternalFrameEvent e) {
	}

	public void internalFrameClosed(final InternalFrameEvent e) {
	}

	public void internalFrameDeactivated(final InternalFrameEvent e) {
	}

	public void internalFrameDeiconified(final InternalFrameEvent e) {
	}

	public void internalFrameIconified(final InternalFrameEvent e) {
	}

	public void internalFrameOpened(final InternalFrameEvent e) {
	}

	public void drop() {
		try {
			this.handleDrop(null, Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this));
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	public void handleDrop(final DropTargetDropEvent e, final Transferable t) throws Exception {
		System.out.println("Starting dropAttempt");

		final DataFlavor chosen = DataFlavor.javaFileListFlavor;
		final DataFlavor second = FileTransferable.plainTextFlavor;
		DataFlavor flavor = null;
		Object data = null;

		if (e != null) {
			flavor = e.getCurrentDataFlavors()[0];
			e.acceptDrop(acceptableActions);

			final Class c = flavor.getDefaultRepresentationClass();
		}

		if (flavor == null) {
			flavor = second;
		}

		String name = "";

		if (t.isDataFlavorSupported(chosen)) {
			System.out.println("Using List DnD style");

			final java.util.List myList = (java.util.List) t.getTransferData(chosen);

			final File[] f = (File[]) myList.toArray();

			for (final java.io.File file : f) {
				name = file.getAbsolutePath();
				System.out.println("DnD file: " + name);

				name = name.replace("\r", "");
				name = name.replace("\n", "");
			}

			this.draggedTransfer(f, name);
		} else if (t.isDataFlavorSupported(second)) {
			System.out.println("Using text/plain DnD style");

			data = t.getTransferData(flavor);

			StringBuilder str = new StringBuilder();
			int i = 0;

			if (data instanceof Reader) {
				int c;

				while ((c = ((Reader) data).read()) != -1) {
					if (((i == 1) && (c == 0))) {
						i = -1;
					} else {
						str.append((char) c);
					}

					i++;
				}
			} else {
				str = new StringBuilder("" + data);
			}

			System.out.println("Object data: \"" + str + "\"");

			if (str.toString().startsWith("<")) {
				Log.debug("Mozilla DnD detected (preparsing)");
				str = new StringBuilder(str.substring(str.indexOf("\"") + 1));
				str = new StringBuilder(str.substring(0, str.indexOf("\"")));
				Log.debug("Parsed data: " + str);
			}

			if (str.toString().contains("[")) {
				Log.debug("Windows DnD detected");
				name = str.substring(str.indexOf("[") + 1);
				name = name.substring(0, name.lastIndexOf("]"));
			} else if (str.toString().startsWith("file://")) {
				name = str.substring(7);

				name = name.replace("\r", "");
				name = name.replace("\n", "");
				Log.debug("File URL DnD detected: " + name);
			}

			if (!new File(name).exists()) {
				System.out.println("No file string in clipboard: " + name);

				return;
			}

			System.out.println("DnD file: " + name);

			final File[] f1 = new File[1];
			f1[0] = new File(name);

			this.draggedTransfer(f1, name);
		}
	}

	private void draggedTransfer(final File[] f, String name) {
		if ((f.length == 1) && f[0].isFile()) {
			String path = "";

			if (name.contains("/")) {
				path = name.substring(0, name.lastIndexOf("/") + 1);
				name = name.substring(name.lastIndexOf("/") + 1);
			}

			Log.debug("DnD: " + path + " -> " + name);

			if (!path.trim().isEmpty()) {
				((net.sf.jftp.gui.base.LocalDir) localDir).chdir(path);
			}

			((net.sf.jftp.gui.base.LocalDir) localDir).startTransfer(new net.sf.jftp.gui.base.dir.DirEntry(name, ((ActionListener) localDir)));
		} else {
			Log.debug("Dragging multiple files or dirs is not yet supported.");
		}
	}

	class DTListener implements DropTargetListener {
		public void dragEnter(final DropTargetDragEvent e) {
			e.acceptDrag(JFtp.acceptableActions);
		}

		public void dragOver(final DropTargetDragEvent e) {
			e.acceptDrag(JFtp.acceptableActions);
		}

		public void dropActionChanged(final DropTargetDragEvent e) {
			e.acceptDrag(JFtp.acceptableActions);
		}

		public void dragExit(final DropTargetEvent e) {
		}
		public void drop(final DropTargetDropEvent e) {
			try {
				net.sf.jftp.JFtp.this.handleDrop(e, e.getTransferable());

				e.dropComplete(true);
				UpdateDaemon.updateRemoteDir();
			} catch (final Throwable t) {
				t.printStackTrace();
				e.dropComplete(false);

			}
		}
	}
}
