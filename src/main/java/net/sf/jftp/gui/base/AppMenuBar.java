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

import javazoom.jl.player.Player;
import net.sf.jftp.JFtp;
import net.sf.jftp.config.Settings;
import net.sf.jftp.gui.framework.HImage;
import net.sf.jftp.gui.hostchooser.HostChooser;
import net.sf.jftp.gui.hostchooser.NfsHostChooser;
import net.sf.jftp.gui.hostchooser.RsyncHostChooser;
import net.sf.jftp.gui.hostchooser.SftpHostChooser;
import net.sf.jftp.gui.hostchooser.SmbHostChooser;
import net.sf.jftp.gui.hostchooser.WebdavHostChooser;
import net.sf.jftp.gui.tasks.AddBookmarks;
import net.sf.jftp.gui.tasks.AdvancedOptions;
import net.sf.jftp.gui.tasks.BookmarkManager;
import net.sf.jftp.gui.tasks.Displayer;
import net.sf.jftp.gui.tasks.HttpBrowser;
import net.sf.jftp.gui.tasks.HttpDownloader;
import net.sf.jftp.gui.tasks.LastConnections;
import net.sf.jftp.gui.tasks.NativeHttpBrowser;
import net.sf.jftp.gui.tasks.ProxyChooser;
import net.sf.jftp.net.wrappers.StartConnection;
import net.sf.jftp.system.logging.Log;
import net.sf.jftp.tools.HttpSpider;
import net.sf.jftp.util.RawConnection;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.StringTokenizer;


//***
public class AppMenuBar extends JMenuBar implements ActionListener {
	public static final JCheckBoxMenuItem fadeMenu = new JCheckBoxMenuItem("Enable Status Animation", Settings.getEnableStatusAnimation());
	public static final JCheckBoxMenuItem askToDelete = new JCheckBoxMenuItem("Confirm Remove", Settings.getAskToDelete());
	public static final JCheckBoxMenuItem debug = new JCheckBoxMenuItem("Verbose Console Debugging", Settings.getEnableDebug());
	public static final JCheckBoxMenuItem disableLog = new JCheckBoxMenuItem("Disable Log", Settings.getDisableLog());
	public static final JMenuItem clearItems = new JMenuItem("Clear Finished Items");
	private final JFtp jftp;
	final JMenu file = new JMenu("File");
	final JMenu opt = new JMenu("Options");
	final JMenu view = new JMenu("View");
	final JMenu tools = new JMenu("Tools");
	final JMenu bookmarks = new JMenu("Bookmarks");
	final JMenu info = new JMenu("Info");
	final JMenu lf = new JMenu("Switch Look & Feel to");
	final JMenu background = new JMenu("Desktop Background");
	final JMenu ftp = new JMenu(" FTP");
	final JMenu smb = new JMenu(" SMB");
	final JMenu sftp = new JMenu(" SFTP");
	final JMenu security = new JMenu("Security");
	JMenu experimental = new JMenu("Experimental Features");
	final JMenu rss = new JMenu("RSS Feed");
	final JMenu cnn = new JMenu("CNN");
	final JMenuItem localFtpCon = new JMenuItem("Open FTP Connection in Local Tab...");
	final JMenuItem localSftpCon = new JMenuItem("Open SFTP Connection in Local Tab...");
	final JMenuItem localSmbCon = new JMenuItem("Open SMB/LAN Connection in Local Tab...");
	final JMenuItem localNfsCon = new JMenuItem("Open NFS Connection in Local Tab...");
	final JMenuItem localWebdavCon = new JMenuItem("Open WebDAV Connection in Local Tab... (ALPHA)");
	final JMenuItem closeLocalCon = new JMenuItem("Close Active Connection in Local Tab");
	final JMenuItem ftpCon = new JMenuItem("Connect to FTP Server...");
	final JMenuItem sftpCon = new JMenuItem("Connect to SFTP Server...");
	final JMenuItem rsyncCon = new JMenuItem("Connect to RSync server...");
	final JMenuItem smbCon = new JMenuItem("Connect to SMB Server / Browse LAN...");
	final JMenuItem nfsCon = new JMenuItem("Connect to NFS Server...");
	final JMenuItem webdavCon = new JMenuItem("Connect to WebDAV Server... (ALPHA)");
	final JMenuItem close = new JMenuItem("Disconnect and Connect to Filesystem");
	final JMenuItem exit = new JMenuItem("Exit");
	final JMenuItem readme = new JMenuItem("Show Readme...");
	final JMenuItem changelog = new JMenuItem("View Changelog...");
	final JMenuItem todo = new JMenuItem("What's Next...");
	final JMenuItem hp = new JMenuItem("Visit Project Homepage...");
	final JMenuItem opts = new JMenuItem("Advanced Options...");
	final JMenuItem http = new JMenuItem("Download File from URL...");
	final JMenuItem raw = new JMenuItem("Raw TCP/IP Connection...");
	final JMenuItem spider = new JMenuItem("Recursive HTTP Download...");
	final JMenuItem shell = new JMenuItem("Execute /bin/bash");
	final JMenuItem loadAudio = new JMenuItem("Play MP3");
	final JCheckBoxMenuItem rssDisabled = new JCheckBoxMenuItem("Enable RSS Feed", Settings.getEnableRSS());
	final JCheckBoxMenuItem nl = new JCheckBoxMenuItem("Show Newline Option", Settings.showNewlineOption);
	final JMenuItem loadSlash = new JMenuItem("Slashdot");
	final JMenuItem loadCNN1 = new JMenuItem("CNN Top Stories");
	final JMenuItem loadCNN2 = new JMenuItem("CNN World");
	final JMenuItem loadCNN3 = new JMenuItem("CNN Tech");
	final JMenuItem loadRss = new JMenuItem("Custom RSS Feed");
	final JCheckBoxMenuItem stdback = new JCheckBoxMenuItem("Background Image", Settings.getUseBackground());
	final JCheckBoxMenuItem resuming = new JCheckBoxMenuItem("Enable Resuming", Settings.enableResuming);
	final JCheckBoxMenuItem ask = new JCheckBoxMenuItem("Always Ask to Resume", Settings.askToResume);
	final JMenuItem proxy = new JMenuItem("Proxy Settings...");
	final JCheckBoxMenuItem smbThreads = new JCheckBoxMenuItem("Multiple Connections", Settings.getEnableSmbMultiThreading());
	final JCheckBoxMenuItem sftpThreads = new JCheckBoxMenuItem("Multiple Connections", Settings.getEnableSftpMultiThreading());
	final JCheckBoxMenuItem sshKeys = new JCheckBoxMenuItem("Enable Host Key check", Settings.getEnableSshKeys());
	final JCheckBoxMenuItem storePasswords = new JCheckBoxMenuItem("Store passwords (encrypted using internal password)", Settings.getStorePasswords());
	final JCheckBoxMenuItem useNewIcons = new JCheckBoxMenuItem("Use Silk Icons", Settings.getUseNewIcons());
	final JCheckBoxMenuItem hideHidden = new JCheckBoxMenuItem("Hide local hidden files (Unix only)", Settings.getHideLocalDotNames());
	final JMenuItem clear = new JMenuItem("Clear Log");
	//*** the menu items for the last connections
	final JMenuItem[] lastConnections = new JMenuItem[JFtp.CAPACITY];
	//*** information on each of the last connections
	//BUGFIX
	String[][] cons = new String[JFtp.CAPACITY][JFtp.CONNECTION_DATA_LENGTH];
	final String[] lastConData = new String[JFtp.CAPACITY];
	final Character charTab = '\t';
	String tab = charTab.toString();
	final JMenuItem manage = new JMenuItem("Manage Bookmarks...");
	final JMenuItem add = new JMenuItem("Add Bookmark...");
	Hashtable marks;
	JMenu current = bookmarks;
	JMenu last = bookmarks;

	/*
	String[] lastProtocols;
	String[] lastHosts;
	String[] lastUnames;
	*/
	public AppMenuBar(JFtp jftp) {
		this.jftp = jftp;

		ftpCon.addActionListener(this);
		close.addActionListener(this);
		exit.addActionListener(this);
		readme.addActionListener(this);
		changelog.addActionListener(this);
		todo.addActionListener(this);
		resuming.addActionListener(this);
		ask.addActionListener(this);
		smbCon.addActionListener(this);
		clear.addActionListener(this);
		sftpCon.addActionListener(this);
		rsyncCon.addActionListener(this);
		fadeMenu.addActionListener(this);
		askToDelete.addActionListener(this);
		smbThreads.addActionListener(this);
		sftpThreads.addActionListener(this);
		debug.addActionListener(this);
		disableLog.addActionListener(this);
		http.addActionListener(this);
		hp.addActionListener(this);
		raw.addActionListener(this);
		nfsCon.addActionListener(this);
		spider.addActionListener(this);
		proxy.addActionListener(this);
		stdback.addActionListener(this);
		opts.addActionListener(this);
		webdavCon.addActionListener(this);
		shell.addActionListener(this);
		nl.addActionListener(this);

		localFtpCon.addActionListener(this);
		localSftpCon.addActionListener(this);
		localSmbCon.addActionListener(this);
		localNfsCon.addActionListener(this);
		localWebdavCon.addActionListener(this);
		closeLocalCon.addActionListener(this);
		add.addActionListener(this);
		storePasswords.addActionListener(this);
		rssDisabled.addActionListener(this);
		loadRss.addActionListener(this);
		loadSlash.addActionListener(this);
		loadCNN1.addActionListener(this);
		loadCNN2.addActionListener(this);
		loadCNN3.addActionListener(this);
		loadAudio.addActionListener(this);
		useNewIcons.addActionListener(this);
		hideHidden.addActionListener(this);

		clearItems.addActionListener(JFtp.dList);

		clear.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, java.awt.event.InputEvent.ALT_MASK));
		clearItems.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, java.awt.event.InputEvent.ALT_MASK));
		changelog.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, java.awt.event.InputEvent.ALT_MASK));
		readme.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, java.awt.event.InputEvent.ALT_MASK));
		todo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, java.awt.event.InputEvent.ALT_MASK));

		//*** setMnemonics(); was here
		//*** BELOW, ADDITIONS FOR THE FILE MENU ARE PUT IN PUBLIC METHOD
		resetFileItems();

		ftp.add(resuming);
		ftp.add(ask);
		ftp.add(nl);
		smb.add(smbThreads);
		sftp.add(sftpThreads);
		sftp.add(sshKeys);
		security.add(askToDelete);
		security.add(storePasswords);

		cnn.add(loadCNN1);
		cnn.add(loadCNN2);
		cnn.add(loadCNN3);

		rss.add(rssDisabled);
		rss.add(loadSlash);
		rss.add(cnn);
		rss.add(loadRss);

		opt.add(security);
		opt.addSeparator();
		opt.add(ftp);
		opt.add(smb);
		opt.add(sftp);
		opt.addSeparator();
		opt.add(proxy);
		opt.add(opts);

		tools.add(http);
		tools.add(spider);
		tools.addSeparator();
		tools.add(raw);
		tools.addSeparator();
		tools.add(shell);

		view.add(hideHidden);
		view.addSeparator();
		view.add(useNewIcons);
		view.add(fadeMenu);
		view.add(clear);
		view.add(clearItems);

		view.addSeparator();
		view.add(debug);
		view.add(disableLog);
		view.addSeparator();
		view.add(rss);
		view.addSeparator();

		info.add(readme);
		info.add(changelog);
		//info.add(todo);
		//info.addSeparator();
		info.add(hp);

		UIManager.LookAndFeelInfo[] m = UIManager.getInstalledLookAndFeels();

		for (javax.swing.UIManager.LookAndFeelInfo lookAndFeelInfo : m) {
			//JMenuItem tmp = new JMenuItem(m[i].getName());
			//tmp.addActionListener(this);
			//lf.add(tmp);

			/*
			 * Don't add menu items for unsupported look and feel's.
			 *
			 * It would be nice to use something like
			 * isSupportedLookandFeel, but the information provided by
			 * UIManager.LookAndFeelInfo is very limited. This is
			 * supposedly done on purpose according to the API docs,
			 * but what good does a non-supported look and feel in a
			 * menu item do?
			 */
			try {
				javax.swing.LookAndFeel lnf = (javax.swing.LookAndFeel) Class.forName(lookAndFeelInfo.getClassName()).newInstance();

				if (lnf.isSupportedLookAndFeel()) {
					javax.swing.JMenuItem tmp = new javax.swing.JMenuItem(lookAndFeelInfo.getName());
					tmp.addActionListener(this);
					lf.add(tmp);
				}
			} catch (ClassNotFoundException | IllegalAccessException | InstantiationException cnfe) {
				continue;
			}
		}

		view.add(lf);

		background.add(stdback);
		view.add(background);

		manage.addActionListener(this);

		//UIManager.setLookAndFeel();
		add(file);
		add(opt);
		add(view);
		add(tools);
		add(bookmarks);
		add(info);

		loadBookmarks();

		//add(experimental);
	}

	public void loadBookmarks() {
		marks = new Hashtable();
		bookmarks.removeAll();
		bookmarks.add(add);
		bookmarks.add(manage);
		bookmarks.addSeparator();

		String data = "";

		try {
			DataInput in = new DataInputStream(new BufferedInputStream(new FileInputStream(Settings.bookmarks)));

			while ((data = in.readLine()) != null) {
				if (!data.startsWith("#") && !data.trim().equals("")) {
					addBookmarkLine(data);
				}
			}
		} catch (IOException e) {
			Log.out("No bookmarks.txt found, using defaults.");

			addBookmark("FTP", "ftp.kernel.org", "anonymous", "j-ftp@sf.net", 21, "/pub/linux/kernel", "false");
			addBookmark("FTP", "upload.sourceforge.net", "anonymous", "j-ftp@sf.net", 21, "/incoming", "false");
			addBookmark("SMB", "(LAN)", "guest", "guest", -1, "-", "false");

		}
	}

	private void addBookmarkLine(String tmp) {
		try {
			StringTokenizer t = new StringTokenizer(tmp, "#", false);

			if (tmp.toLowerCase().trim().startsWith("<dir>")) {
				String dir = tmp.substring(tmp.indexOf(">") + 1, tmp.lastIndexOf("<"));

				//Log.debug("Dir: " + dir);
				JMenu m = new JMenu(dir);
				current.add(m);

				last = current;
				current = m;
			} else if (tmp.toLowerCase().trim().startsWith("<enddir>")) {
				current = last;
			} else {
				addBookmark(t.nextToken(), t.nextToken(), t.nextToken(), t.nextToken(), Integer.parseInt(t.nextToken()), t.nextToken(), t.nextToken());
			}
		} catch (Exception ex) {
			Log.debug("Broken line: " + tmp);
			ex.printStackTrace();
		}
	}

	public void addBookmark(String pr, String h, String u, String p, int po, String d, String l) {
		net.sf.jftp.gui.tasks.BookmarkItem x = new net.sf.jftp.gui.tasks.BookmarkItem(h);
		x.setUserdata(u, p);

		if (l.trim().startsWith("t")) {
			x.setLocal(true);
		}

		x.setPort(po);
		x.setProtocol(pr);
		x.setDirectory(d);

		//bookmarks
		current.add(x);
		marks.put(x.getLabel(), x);
		x.addActionListener(this);
	}

	//*** Where changes to the file menu are made (iniitalization done here too)
	public void resetFileItems() {
		file.removeAll();

		file.add(ftpCon);
		file.add(sftpCon);
		file.add(rsyncCon);
		file.add(smbCon);
		file.add(nfsCon);
		file.add(webdavCon);
		file.addSeparator();
		file.add(close);
		file.addSeparator();
		file.addSeparator();
		file.add(localFtpCon);
		file.add(localSftpCon);
		file.add(localSmbCon);
		file.add(localNfsCon);

		//file.add(localWebdavCon); -> not yet
		file.addSeparator();
		file.add(closeLocalCon);
		file.addSeparator();

		//*** ADDITION OF THE REMEMBERED CONNECTIONS
		boolean connectionsExist = false;

		try {
			//*** get the information on the last connections
			cons = new String[JFtp.CAPACITY][JFtp.CONNECTION_DATA_LENGTH];

			cons = LastConnections.readFromFile(JFtp.CAPACITY);

			String protocol;

			String htmp;

			String utmp;

			String conNumber;
			String usingLocal = "";
			int conNumberInt;

			//lastConData = new String("");
			//***
			for (int i = 0; i < JFtp.CAPACITY; i++) {
				if (!(cons[i][0].equals("null"))) {
					protocol = cons[i][0];
					htmp = cons[i][1];
					utmp = cons[i][2];

					int j = 3;

					while (!(cons[i][j].equals(LastConnections.SENTINEL))) {
						j++;
					}

					//usingLocal is always last piece of data!
					usingLocal = cons[i][j - 1];

					if (usingLocal.equals("true")) {
						usingLocal = "(in local tab)";
					} else {
						usingLocal = "";
					}

					//lastConnections[i] = new JMenuItem(cons[i]);
					conNumberInt = i + 1;
					conNumber = Integer.toString(conNumberInt);

					//lastConData[i] = new String(conNumber + " " + protocol + ": Hostname: " + htmp + ";  Username: " + utmp);
					lastConData[i] = conNumber + " " + protocol + ": " + htmp + " " + usingLocal;

					lastConnections[i] = new JMenuItem(lastConData[i]);
					lastConnections[i].addActionListener(this);

					connectionsExist = true;

					//*** code repetition: maybe getting these tokens
					//*** should be in a separate private method
					//file.add(protocol + ": Hostname: " + htmp + ";  Username: " + utmp);
					file.add(lastConnections[i]);
				}
			}
		} catch (Exception ex) {
			Log.debug("WARNING: Remembered connections broken.");
			ex.printStackTrace();
		}

		if (connectionsExist) {
			file.addSeparator();
		}

		file.add(exit);

		setMnemonics();
	}

	//resetFileItems
	public void actionPerformed(ActionEvent e) {
		try {
			if (e.getSource() == proxy) {
				//ProxyChooser p =
				JFtp.statusP.jftp.addToDesktop("Proxy Settings", new ProxyChooser(), 500, 110);
			} else if (e.getSource() == add) {
				Log.out("add called");

				AddBookmarks a = new AddBookmarks(JFtp.statusP.jftp);
				a.update();
			} else if (e.getSource() == webdavCon) {
				WebdavHostChooser hc = new WebdavHostChooser();
				hc.toFront();
				hc.update();
			} else if ((e.getSource() == localFtpCon) && (!JFtp.uiBlocked)) {
				HostChooser hc = new HostChooser(null, true);
				hc.toFront();

				//hc.setModal(true);
				hc.update();
			} else if ((e.getSource() == localSmbCon) && (!JFtp.uiBlocked)) {
				SmbHostChooser hc = new SmbHostChooser(null, true);
				hc.toFront();

				//hc.setModal(true);
				hc.update();
			} else if ((e.getSource() == localSftpCon) && (!JFtp.uiBlocked)) {
				SftpHostChooser hc = new SftpHostChooser(null, true);
				hc.toFront();

				//hc.setModal(true);
				hc.update();
			} else if ((e.getSource() == localNfsCon) && (!JFtp.uiBlocked)) {
				NfsHostChooser hc = new NfsHostChooser(null, true);
				hc.toFront();

				//hc.setModal(true);
				hc.update();
			} else if ((e.getSource() == localWebdavCon) && (!JFtp.uiBlocked)) {
				WebdavHostChooser hc = new WebdavHostChooser(null, true);
				hc.toFront();

				//hc.setModal(true);
				hc.update();
			} else if (e.getSource() == closeLocalCon) {
				JFtp.statusP.jftp.closeCurrentLocalTab();
			} else if (e.getSource() == clear) {
				JFtp.clearLog();
			} else if (e.getSource() == spider) {
				jftp.addToDesktop("Http recursive download", new HttpSpider(JFtp.localDir.getPath() + "_httpdownload/"), 440, 250);
			} else if (e.getSource() == hp) {
				try {
					NativeHttpBrowser.main(new String[]{"http://j-ftp.sourceforge.net"});
				} catch (Throwable ex) {
					ex.printStackTrace();
					Log.debug("Native browser intialization failed, using JContentPane...");

					HttpBrowser h = new HttpBrowser("http://j-ftp.sourceforge.net");
					JFtp.desktop.add(h, new Integer(Integer.MAX_VALUE - 10));
				}
			} else if (e.getSource() == raw) {
				RawConnection c = new RawConnection();
			} else if (e.getSource() == readme) {
				show(Settings.readme);
			} else if (e.getSource() == changelog) {
				show(Settings.changelog);
			} else if (e.getSource() == todo) {
				show(Settings.todo);
			} else if (e.getSource() == shell) {
				UIUtils.runCommand("/bin/bash");
			} else if (e.getSource() == loadAudio) {
				try {
					JFileChooser f = new JFileChooser();
					f.showOpenDialog(jftp);

					File file = f.getSelectedFile();

					Player p = new Player(new FileInputStream(file));

					p.play();
				} catch (Exception ex) {
					ex.printStackTrace();
					Log.debug("Error: (" + ex + ")");
				}
			} else if (e.getSource() == exit) {
				jftp.windowClosing(null); // handles everything
			} else if (e.getSource() == close) {
				JFtp.statusP.jftp.closeCurrentTab();
	
	            /*
	            jftp.safeDisconnect();
	            FilesystemConnection con = new FilesystemConnection();
	            jftp.remoteDir.setCon(con);
	            con.addConnectionListener((ConnectionListener)jftp.remoteDir);
	            if(!con.chdir("/")) con.chdir("C:\\");
	            */
			} else if ((e.getSource() == ftpCon) && (!JFtp.uiBlocked)) {
				//jftp.safeDisconnect();
				HostChooser hc = new HostChooser();
				hc.toFront();

				//hc.setModal(true);
				hc.update();
			} else if ((e.getSource() == smbCon) && (!JFtp.uiBlocked)) {
				//jftp.safeDisconnect();
				SmbHostChooser hc = new SmbHostChooser();
				hc.toFront();

				//hc.setModal(true);
				hc.update();
			} else if ((e.getSource() == sftpCon) && (!JFtp.uiBlocked)) {
				//jftp.safeDisconnect();
				SftpHostChooser hc = new SftpHostChooser();
				hc.toFront();

				//hc.setModal(true);
				hc.update();
			} else if ((e.getSource() == rsyncCon) && (!JFtp.uiBlocked)) {
				RsyncHostChooser hc = new RsyncHostChooser();
				hc.toFront();

				hc.update();
			} else if ((e.getSource() == nfsCon) && (!JFtp.uiBlocked)) {
				// jftp.safeDisconnect();
				NfsHostChooser hc = new NfsHostChooser();
				hc.toFront();

				//hc.setModal(true);
				hc.update();
			} else if (e.getSource() == resuming) {
				boolean res = resuming.getState();
				Settings.enableResuming = res;
				Settings.setProperty("jftp.enableResuming", res);
				ask.setEnabled(Settings.enableResuming);
				Settings.save();
			} else if (e.getSource() == useNewIcons) {
				boolean res = useNewIcons.getState();
				Settings.setProperty("jftp.gui.look.newIcons", res);
				Settings.save();

				JOptionPane.showMessageDialog(this, "Please restart JFtp to have the UI changed.");
			} else if (e.getSource() == hideHidden) {
				boolean res = hideHidden.getState();
				Settings.setProperty("jftp.hideHiddenDotNames", res);
				Settings.save();

				JFtp.localUpdate();
			} else if (e.getSource() == nl) {
				Settings.showNewlineOption = nl.getState();
			} else if (e.getSource() == stdback) {
				Settings.setProperty("jftp.useBackground", stdback.getState());
				Settings.save();
				JFtp.statusP.jftp.fireUpdate();
			} else if (e.getSource() == sshKeys) {
				Settings.setProperty("jftp.useSshKeyVerification", sshKeys.getState());
				Settings.save();
				JFtp.statusP.jftp.fireUpdate();
			} else if (e.getSource() == rssDisabled) {
				Settings.setProperty("jftp.enableRSS", rssDisabled.getState());
				Settings.save();

				JFtp.statusP.jftp.fireUpdate();

				String feed = Settings.getProperty("jftp.customRSSFeed");
				if (feed != null && !feed.equals("")) feed = "http://slashdot.org/rss/slashdot.rss";

				switchRSS(feed);
			} else if (e.getSource() == loadRss) {
				String what = JOptionPane.showInputDialog("Enter URL", "http://");

				if (what == null) {
					return;
				}

				switchRSS(what);
			} else if (e.getSource() == loadSlash) {
				switchRSS("http://slashdot.org/rss/slashdot.rss");
			} else if (e.getSource() == loadCNN1) {
				switchRSS("http://rss.cnn.com/rss/cnn_topstories.rss");
			} else if (e.getSource() == loadCNN2) {
				switchRSS("http://rss.cnn.com/rss/cnn_world.rss");
			} else if (e.getSource() == loadCNN3) {
				switchRSS("http://rss.cnn.com/rss/cnn_tech.rss");
			} else if (e.getSource() == debug) {
				Settings.setProperty("jftp.enableDebug", debug.getState());
				Settings.save();
			} else if (e.getSource() == disableLog) {
				Settings.setProperty("jftp.disableLog", disableLog.getState());
				Settings.save();
			} else if (e.getSource() == smbThreads) {
				Settings.setProperty("jftp.enableSmbMultiThreading", smbThreads.getState());
				Settings.save();
			} else if (e.getSource() == sftpThreads) {
				Settings.setProperty("jftp.enableSftpMultiThreading", sftpThreads.getState());
				Settings.save();
			} else if (e.getSource() == ask) {
				Settings.askToResume = ask.getState();
			} else if (e.getSource() == http) {
				HttpDownloader dl = new HttpDownloader();
				jftp.addToDesktop("Http download", dl, 480, 100);
				jftp.setLocation(dl.hashCode(), 100, 150);
			} else if (e.getSource() == fadeMenu) {
				Settings.setProperty("jftp.gui.enableStatusAnimation", fadeMenu.getState());
				Settings.save();
			} else if (e.getSource() == askToDelete) {
				Settings.setProperty("jftp.gui.askToDelete", askToDelete.getState());
				Settings.save();
			}

			//***MY ADDITIONS (***how can I make this flexible enough to
			//*** easily add > 5 connections?)
			else if ((e.getSource() == lastConnections[0]) && (!JFtp.uiBlocked)) {
				connectionSelected(0);
			} else if ((e.getSource() == lastConnections[1]) && (!JFtp.uiBlocked)) {
				connectionSelected(1);
			} else if ((e.getSource() == lastConnections[2]) && (!JFtp.uiBlocked)) {
				connectionSelected(2);
			} else if ((e.getSource() == lastConnections[3]) && (!JFtp.uiBlocked)) {
				connectionSelected(3);
			} else if ((e.getSource() == lastConnections[4]) && (!JFtp.uiBlocked)) {
				connectionSelected(4);
			} else if ((e.getSource() == lastConnections[5]) && (!JFtp.uiBlocked)) {
				connectionSelected(5);
			} else if ((e.getSource() == lastConnections[6]) && (!JFtp.uiBlocked)) {
				connectionSelected(6);
			} else if ((e.getSource() == lastConnections[7]) && (!JFtp.uiBlocked)) {
				connectionSelected(7);
			} else if ((e.getSource() == lastConnections[8]) && (!JFtp.uiBlocked)) {
				connectionSelected(8);
			} else if (e.getSource() == opts) {
				AdvancedOptions adv = new AdvancedOptions();
				jftp.addToDesktop("Advanced Options", adv, 500, 180);
				jftp.setLocation(adv.hashCode(), 110, 180);
			} else if (e.getSource() == manage) {
				BookmarkManager m = new BookmarkManager();
				JFtp.desktop.add(m, new Integer(Integer.MAX_VALUE - 10));
			} else if (marks.contains(e.getSource())) {
				((net.sf.jftp.gui.tasks.BookmarkItem) e.getSource()).connect();
			} else if (e.getSource() == storePasswords) {
				boolean state = storePasswords.getState();

				if (!state) {
					JOptionPane j = new JOptionPane();
					int x = JOptionPane.showConfirmDialog(storePasswords, "You chose not to Save passwords.\n" + "Do you want your old login data to be deleted?", "Delete old passwords?", JOptionPane.YES_NO_OPTION);

					if (x == JOptionPane.YES_OPTION) {
						File f = new File(Settings.login_def);
						f.delete();

						f = new File(Settings.login_def_sftp);
						f.delete();

						f = new File(Settings.login_def_nfs);
						f.delete();

						f = new File(Settings.login_def_smb);
						f.delete();

						f = new File(Settings.login);
						f.delete();

						f = new File(Settings.last_cons);
						f.delete();

						Log.debug("Deleted old login data files.\n" + "Please edit your bookmarks file manually!");
					}
				}

				Settings.setProperty("jftp.security.storePasswords", state);
				Settings.save();
			}

			//*** END OF NEW LISTENERS
			else {
				String tmp = ((JMenuItem) e.getSource()).getLabel();

				UIManager.LookAndFeelInfo[] m = UIManager.getInstalledLookAndFeels();

				for (javax.swing.UIManager.LookAndFeelInfo lookAndFeelInfo : m) {
					if (lookAndFeelInfo.getName().equals(tmp)) {
						net.sf.jftp.JFtp.statusP.jftp.setLookAndFeel(lookAndFeelInfo.getClassName());
						net.sf.jftp.config.Settings.setProperty("jftp.gui.look", lookAndFeelInfo.getClassName());
						net.sf.jftp.config.Settings.save();
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.debug(ex.toString());
		}
	}

	private void switchRSS(String url) {
		Settings.setProperty("jftp.customRSSFeed", url);
		Settings.save();


		if (JFtp.statusP.jftp.feeder == null) {
			JFtp.statusP.jftp.addRSS();
		}

		JFtp.statusP.jftp.feeder.switchTo(url);
	}

	private void show(String file) {
		java.net.URL url = ClassLoader.getSystemResource(file);

		if (url == null) {
			url = HImage.class.getResource("/" + file);
		}

		Displayer d = new Displayer(url, null);
		JFtp.desktop.add(d, new Integer(Integer.MAX_VALUE - 11));
	}

	// by jake
	private void setMnemonics() {
		//*** I added accelerators for more menu items
		//*** (issue: should ALL accelerators have the CTRL modifier (so that
		//*** the ALT modifier is for mnemonics only?)
		//*** I added mnemonics for the main menu items
		file.setMnemonic('F');
		opt.setMnemonic('O');
		view.setMnemonic('V');
		tools.setMnemonic('T');
		bookmarks.setMnemonic('B');
		info.setMnemonic('I');

		//*** set accelerators for the remote connection window
		ftpCon.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
		sftpCon.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
		smbCon.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
		nfsCon.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));

		//*** IMPORTANT NOTE: Adding an accelerator for disconnecting could
		//*** be something of a "gotcha" that we may not want
		close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));

		//*** These next five lines can be commented out if we decide against having accelerators
		//*** starting with the shift key
		//*** version 1.44: we have chosen not to have shift as a modifier
		//***               as we've found this is a "gotcha"
        /*
        localFtpCon.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                                                          ActionEvent.SHIFT_MASK));
        localSftpCon.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                                                           ActionEvent.SHIFT_MASK));
        localSmbCon.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
                                                          ActionEvent.SHIFT_MASK));
        localNfsCon.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                                                          ActionEvent.SHIFT_MASK));

        closeLocalCon.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                                                            ActionEvent.SHIFT_MASK));

        */
		//*** I have decided to get areound the problem by having mnemonics within the menu
		//*** as accelerators. So to quickly start an FTP connection in the local window,
		//*** the user can enter Alt+f+f, and Alt+f+s for SFTP,etc.
		localFtpCon.setMnemonic('F');
		localSftpCon.setMnemonic('S');
		localSmbCon.setMnemonic('L');
		localNfsCon.setMnemonic('N');

		//localNfsCon.setMnemonic('N');
		closeLocalCon.setMnemonic('C');

		//*** and here are some other menu mnemonics I thought I'd include:
		//*** (I'll add more if more are wanted)
		exit.setMnemonic('X');

		proxy.setMnemonic('P');

		http.setMnemonic('D');
		spider.setMnemonic('H');
		raw.setMnemonic('T');

		readme.setMnemonic('R');
		todo.setMnemonic('N');
		changelog.setMnemonic('C');
		hp.setMnemonic('H');

		opts.setMnemonic('A');
		manage.setMnemonic('M');

		clear.setMnemonic('C');
		clearItems.setMnemonic('F');

		try {
			//*** end of new code section
			int intI;
			String stringI;
			char charI;

			for (int i = 0; i < JFtp.CAPACITY; i++) {
				//*** I should note that functionality below only allows
				//*** a maximum of nine connections to be remembered
				//BUGFIX 1.40
				//if (!(cons[i].equals("null"))) {
				if (!(cons[i][0].equals("null"))) {
					intI = i + 1;
					stringI = Integer.toString(intI);
					charI = stringI.charAt(0);

					lastConnections[i].setMnemonic(charI);

					//lastConnections[i].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
				}
			}

			//for
		} catch (Exception ex) {
			Log.out("WARNING: AppMenuBar produced Exception, ignored it");
			ex.printStackTrace();
		}
	}

	//setMnemonics
	private void connectionSelected(int position) {
		//*** tokenize the string to extract the required data
		//*** or is this the thing done elsewhere in the code,
		//*** when the menu is being set up?
		//String connectionInfo = cons[position];
		//StringTokenizer tokens = new StringTokenizer(connectionInfo);
		//StringTokenizer tokens = new StringTokenizer(cons[position], " ", false); // tab);
		String protocol;
		int numTokens;

		String htmp = "";
		String utmp = "";
		String ptmp = "";
		String dtmp = "";
		boolean useLocal = false;
		int potmp = 0;
		String potmpString = "0";
		String useLocalString = "false";

        /*
        //numTokens = tokens.countTokens();
        protocol = tokens.nextToken();

        htmp = tokens.nextToken();
        utmp = tokens.nextToken();
        ptmp = tokens.nextToken();

        */
		protocol = cons[position][0];
		htmp = cons[position][1];
		utmp = cons[position][2];
		ptmp = cons[position][3];

		if (ptmp.equals("")) {
			ptmp = UIUtils.getPasswordFromUser(JFtp.statusP.jftp);
		}

		//int j=4;
		//while (cons[i][j].equals(LastConnections.SENTINEL)) {
		//        j++;
		//}
		//usingLocal = cons[i][j-1];

        /*
        System.out.println(position);
        System.out.println(protocol);
        System.out.println(cons[position][1]);
        System.out.println(cons[position][2]);
        System.out.println(cons[position][3]);
        */

		//
		switch (protocol) {
			case "FTP":
				potmpString = cons[position][4];
				dtmp = cons[position][5];
				useLocalString = cons[position][6];

            /*
            potmpString = tokens.nextToken();
            dtmp = tokens.nextToken();
            useLocalString = tokens.nextToken();


            System.out.println(potmpString);
            System.out.println("FTP");
            */
				potmp = Integer.parseInt(potmpString);

				useLocal = useLocalString.equals("true");

				net.sf.jftp.net.wrappers.StartConnection.startFtpCon(htmp, utmp, ptmp, potmp, dtmp, useLocal);

				//System.out.println(htmp + utmp + ptmp + potmpString +dtmp + useLocalString);
				break;
			case "SFTP":
            /*
                    htmp = tokens.nextToken();
                    utmp = tokens.nextToken();
                    ptmp = tokens.nextToken();
            */

				//useLocalString = tokens.nextToken();
				//System.out.println("SFTP");
				potmpString = cons[position][4];
				useLocalString = cons[position][5];

				//System.out.println(htmp + utmp + ptmp  + useLocalString);
				//if (protocol == "SFTP")
				break;
			case "NFS":
				useLocalString = cons[position][4];
				break;
			case "SMB":
            /*
            htmp = tokens.nextToken();
            utmp = tokens.nextToken();
            ptmp = tokens.nextToken();
            */
            /*
            dtmp = tokens.nextToken();
            useLocalString = tokens.nextToken();
            */
				dtmp = cons[position][4];
				useLocalString = cons[position][5];

				//System.out.println(htmp+utmp+ptmp+dtmp + useLocalString);
				break;
		}

		//***StartConnection functionality to be put in each
		//***if statement
		potmp = Integer.parseInt(potmpString);

		useLocal = useLocalString.equals("true");

		if (protocol.equals("SFTP")) {
			//BUGFIX 1.40: no longer setting port #
			//to 22, now potmp
			StartConnection.startCon(protocol, htmp, utmp, ptmp, potmp, dtmp, useLocal);
		} else if (!(protocol.equals("FTP"))) {
			//System.out.println(protocol);
			StartConnection.startCon(protocol, htmp, utmp, ptmp, potmp, dtmp, useLocal);
		}
	}

	//connectionSelected
}
