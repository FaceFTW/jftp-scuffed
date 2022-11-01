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
import net.sf.jftp.gui.hostchooser.HostChooser;
import net.sf.jftp.gui.hostchooser.NfsHostChooser;
import net.sf.jftp.gui.hostchooser.RsyncHostChooser;
import net.sf.jftp.gui.hostchooser.SftpHostChooser;
import net.sf.jftp.gui.hostchooser.SmbHostChooser;
import net.sf.jftp.gui.hostchooser.WebdavHostChooser;
import net.sf.jftp.gui.tasks.AddBookmarks;
import net.sf.jftp.gui.tasks.AdvancedOptions;
import net.sf.jftp.gui.tasks.BookmarkItem;
import net.sf.jftp.gui.tasks.BookmarkManager;
import net.sf.jftp.gui.tasks.HttpDownloader;
import net.sf.jftp.gui.tasks.LastConnections;
import net.sf.jftp.gui.tasks.ProxyChooser;
import net.sf.jftp.net.wrappers.StartConnection;
import net.sf.jftp.system.logging.Log;
import net.sf.jftp.tools.HttpSpider;
import net.sf.jftp.util.I18nHelper;
import net.sf.jftp.util.RawConnection;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

public class AppMenuBar extends JMenuBar implements ActionListener {
	public static final JCheckBoxMenuItem fadeMenu = new JCheckBoxMenuItem(I18nHelper.getUIString(
			"enable.status" + ".animation"), Settings.getEnableStatusAnimation());
	public static final JMenuItem clearItems = new JMenuItem(I18nHelper.getUIString("clear.finished.items"));
	private static final JCheckBoxMenuItem askToDelete =
			new JCheckBoxMenuItem(I18nHelper.getUIString("confirm.remove"), Settings.getAskToDelete());
	private static final JCheckBoxMenuItem debug = new JCheckBoxMenuItem(I18nHelper.getUIString(
			"verbose.console" + ".debugging"), Settings.getEnableDebug());
	private static final JCheckBoxMenuItem disableLog = new JCheckBoxMenuItem(I18nHelper.getUIString("disable.log"),
			Settings.getDisableLog());
	private final JMenu file = new JMenu(I18nHelper.getUIString("file"));
	private final JMenu opt = new JMenu(I18nHelper.getUIString("options"));
	private final JMenu view = new JMenu(I18nHelper.getUIString("view"));
	private final JMenu tools = new JMenu(I18nHelper.getUIString("tools"));
	private final JMenu bookmarks = new JMenu(I18nHelper.getUIString("bookmarks"));
	private final JMenu info = new JMenu(I18nHelper.getUIString("info"));
	private final JMenu lf = new JMenu(I18nHelper.getUIString("switch.look.feel.to"));
	private final JMenu background = new JMenu(I18nHelper.getUIString("desktop.background"));
	private final JMenu ftp = new JMenu(I18nHelper.getUIString("ftp"));
	private final JMenu smb = new JMenu(I18nHelper.getUIString("smb"));
	private final JMenu sftp = new JMenu(I18nHelper.getUIString("sftp"));
	private final JMenu security = new JMenu(I18nHelper.getUIString("security"));
	private final JMenu rss = new JMenu(I18nHelper.getUIString("rss.feed"));
	private final JMenu cnn = new JMenu("CNN");
	private final JMenuItem localFtpCon = new JMenuItem(I18nHelper.getUIString("open.ftp.connection.in.local.tab"));
	private final JMenuItem localSftpCon = new JMenuItem(I18nHelper.getUIString("open.sftp.connection.in.local.tab"));
	private final JMenuItem localSmbCon = new JMenuItem(I18nHelper.getUIString(
			"open.smb.lan.connection.in.local" + ".tab"));
	private final JMenuItem localNfsCon = new JMenuItem(I18nHelper.getUIString("open.nfs.connection.in.local.tab"));
	private final JMenuItem localWebdavCon = new JMenuItem(I18nHelper.getUIString(
			"open.webdav.connection.in.local.tab" + ".alpha"));
	private final JMenuItem closeLocalCon = new JMenuItem(I18nHelper.getUIString(
			"close.active.connection.in.local" + ".tab"));
	private final JMenuItem ftpCon = new JMenuItem(I18nHelper.getUIString("connect.to.ftp.server"));
	private final JMenuItem sftpCon = new JMenuItem(I18nHelper.getUIString("connect.to.sftp.server"));
	private final JMenuItem rsyncCon = new JMenuItem(I18nHelper.getUIString("connect.to.rsync.server"));
	private final JMenuItem smbCon = new JMenuItem(I18nHelper.getUIString("connect.to.smb.server.browse.lan"));
	private final JMenuItem nfsCon = new JMenuItem(I18nHelper.getUIString("connect.to.nfs.server"));
	private final JMenuItem webdavCon = new JMenuItem(I18nHelper.getUIString("connect.to.webdav.server.alpha"));
	private final JMenuItem close = new JMenuItem(I18nHelper.getUIString("disconnect.and.connect.to.filesystem"));
	private final JMenuItem exit = new JMenuItem(I18nHelper.getUIString("exit"));
	private final JMenuItem opts = new JMenuItem(I18nHelper.getUIString("advanced.options2"));
	private final JMenuItem http = new JMenuItem(I18nHelper.getUIString("download.file.from.url"));
	private final JMenuItem raw = new JMenuItem(I18nHelper.getUIString("raw.tcp.ip.connection"));
	private final JMenuItem spider = new JMenuItem(I18nHelper.getUIString("recursive.http.download"));
	private final JMenuItem shell = new JMenuItem(I18nHelper.getUIString("execute.bin.bash"));
	private final JMenuItem xkcd = new JMenuItem(I18nHelper.getUIString("xkcd.info.display"));
	private final JMenuItem loadAudio = new JMenuItem(I18nHelper.getUIString("play.mp3"));
	private final JCheckBoxMenuItem rssDisabled = new JCheckBoxMenuItem(I18nHelper.getUIString("enable.rss.feed"),
			Settings.getEnableRSS());
	private final JCheckBoxMenuItem nl = new JCheckBoxMenuItem(I18nHelper.getUIString("show.newline.option"),
			Settings.showNewlineOption);
	private final JMenuItem loadSlash = new JMenuItem(I18nHelper.getUIString("slashdot"));
	private final JMenuItem loadCNN1 = new JMenuItem(I18nHelper.getUIString("cnn.top.stories"));
	private final JMenuItem loadCNN2 = new JMenuItem(I18nHelper.getUIString("cnn.world"));
	private final JMenuItem loadCNN3 = new JMenuItem(I18nHelper.getUIString("cnn.tech"));
	private final JMenuItem loadRss = new JMenuItem(I18nHelper.getUIString("custom.rss.feed"));
	private final JCheckBoxMenuItem stdback = new JCheckBoxMenuItem(I18nHelper.getUIString("background.image"),
			Settings.getUseBackground());
	private final JCheckBoxMenuItem resuming = new JCheckBoxMenuItem(I18nHelper.getUIString("enable.resuming"),
			Settings.enableResuming);
	private final JCheckBoxMenuItem ask = new JCheckBoxMenuItem(I18nHelper.getUIString("always.ask.to.resume"),
			Settings.askToResume);

	private final JMenuItem rawCon = new JMenuItem(I18nHelper.getUIString("send.raw.command"));

	private final JMenuItem proxy = new JMenuItem(I18nHelper.getUIString("proxy.settings2"));
	private final JCheckBoxMenuItem smbThreads = new JCheckBoxMenuItem(I18nHelper.getUIString("multiple.connections"),
			Settings.getEnableSmbMultiThreading());
	private final JCheckBoxMenuItem sftpThreads = new JCheckBoxMenuItem(I18nHelper.getUIString("multiple.connections")
			, Settings.getEnableSftpMultiThreading());
	private final JCheckBoxMenuItem sshKeys = new JCheckBoxMenuItem(I18nHelper.getUIString("enable.host.key.check"),
			Settings.getEnableSshKeys());
	private final JCheckBoxMenuItem storePasswords = new JCheckBoxMenuItem(I18nHelper.getUIString(
			"store.passwords" + ".encrypted.using.internal.password"), Settings.getStorePasswords());
	private final JCheckBoxMenuItem useNewIcons = new JCheckBoxMenuItem(I18nHelper.getUIString("use.silk.icons"),
			Settings.getUseNewIcons());
	private final JCheckBoxMenuItem hideHidden = new JCheckBoxMenuItem(I18nHelper.getUIString(
			"hide.local.hidden.files" + ".unix.only"), Settings.getHideLocalDotNames());
	private final JMenuItem clear = new JMenuItem(I18nHelper.getUIString("clear.log"));
	//*** the menu items for the last connections
	private final JMenuItem[] lastConnections = new JMenuItem[JFtp.CAPACITY];
	private final String[] lastConData = new String[JFtp.CAPACITY];
	private final char charTab = '\t';
	private final JMenuItem manage = new JMenuItem(I18nHelper.getUIString("manage.bookmarks"));
	private final JMenuItem add = new JMenuItem(I18nHelper.getUIString("add.bookmark1"));
	private final JMenuItem about = new JMenuItem("About jFTP");
	private final JFtp jftp;

	//BUGFIX
	private String[][] cons = new String[JFtp.CAPACITY][JFtp.CONNECTION_DATA_LENGTH];
	private Map<String, BookmarkItem> marks;
	private JMenu current = this.bookmarks;
	private JMenu last = this.bookmarks;
	private JMenu lo = new JMenu(I18nHelper.getUIString("change.locale.to"));

	public AppMenuBar(JFtp jftp) {
		super();
		this.jftp = jftp;

		this.ftpCon.addActionListener(this);
		this.close.addActionListener(this);
		this.exit.addActionListener(this);
		this.resuming.addActionListener(this);
		this.ask.addActionListener(this);
		this.smbCon.addActionListener(this);
		this.clear.addActionListener(this);
		this.sftpCon.addActionListener(this);
		this.rsyncCon.addActionListener(this);
		fadeMenu.addActionListener(this);
		askToDelete.addActionListener(this);
		this.smbThreads.addActionListener(this);
		this.sftpThreads.addActionListener(this);
		debug.addActionListener(this);
		disableLog.addActionListener(this);
		this.http.addActionListener(this);
		this.raw.addActionListener(this);
		this.rawCon.addActionListener(this);
		this.nfsCon.addActionListener(this);
		this.spider.addActionListener(this);
		this.proxy.addActionListener(this);
		this.stdback.addActionListener(this);
		this.opts.addActionListener(this);
		this.webdavCon.addActionListener(this);
		this.shell.addActionListener(this);
		this.xkcd.addActionListener(this);
		this.nl.addActionListener(this);

		this.localFtpCon.addActionListener(this);
		this.localSftpCon.addActionListener(this);
		this.localSmbCon.addActionListener(this);
		this.localNfsCon.addActionListener(this);
		this.localWebdavCon.addActionListener(this);
		this.closeLocalCon.addActionListener(this);
		this.add.addActionListener(this);
		this.storePasswords.addActionListener(this);
		this.rssDisabled.addActionListener(this);
		this.loadRss.addActionListener(this);
		this.loadSlash.addActionListener(this);
		this.loadCNN1.addActionListener(this);
		this.loadCNN2.addActionListener(this);
		this.loadCNN3.addActionListener(this);
		this.loadAudio.addActionListener(this);
		this.useNewIcons.addActionListener(this);
		this.hideHidden.addActionListener(this);
		this.clearItems.addActionListener(JFtp.dList);
		this.about.addActionListener(this);


		this.clear.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, java.awt.event.InputEvent.ALT_MASK));
		this.clearItems.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, java.awt.event.InputEvent.ALT_MASK));

		this.resetFileItems();

		this.ftp.add(this.resuming);
		this.ftp.add(this.ask);
		this.ftp.add(this.nl);
		this.ftp.add(this.rawCon);
		this.smb.add(this.smbThreads);
		this.sftp.add(this.sftpThreads);
		this.sftp.add(this.sshKeys);
		this.security.add(askToDelete);
		this.security.add(this.storePasswords);

		this.cnn.add(this.loadCNN1);
		this.cnn.add(this.loadCNN2);
		this.cnn.add(this.loadCNN3);

		this.rss.add(this.rssDisabled);
		this.rss.add(this.loadSlash);
		this.rss.add(this.cnn);
		this.rss.add(this.loadRss);

		this.opt.add(this.security);
		this.opt.addSeparator();
		this.opt.add(this.ftp);
		this.opt.add(this.smb);
		this.opt.add(this.sftp);
		this.opt.addSeparator();
		this.opt.add(this.proxy);
		this.opt.add(this.opts);

		this.tools.add(this.http);
		this.tools.add(this.spider);
		this.tools.addSeparator();
		this.tools.add(this.raw);
		this.tools.addSeparator();
		this.tools.add(this.shell);
		this.tools.add(this.xkcd);

		this.view.add(this.hideHidden);
		this.view.addSeparator();
		this.view.add(this.useNewIcons);
		this.view.add(fadeMenu);
		this.view.add(this.clear);
		this.view.add(clearItems);

		this.view.addSeparator();
		this.view.add(debug);
		this.view.add(disableLog);
		this.view.addSeparator();
		this.view.add(this.rss);
		this.view.addSeparator();

		this.info.add(about);

		UIManager.LookAndFeelInfo[] m = UIManager.getInstalledLookAndFeels();

		for (javax.swing.UIManager.LookAndFeelInfo lookAndFeelInfo : m) {

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
				javax.swing.LookAndFeel lnf =
						(javax.swing.LookAndFeel) Class.forName(lookAndFeelInfo.getClassName()).newInstance();

				if (lnf.isSupportedLookAndFeel()) {
					javax.swing.JMenuItem tmp = new javax.swing.JMenuItem(lookAndFeelInfo.getName());
					tmp.addActionListener(this);
					this.lf.add(tmp);
				}
			} catch (ClassNotFoundException | IllegalAccessException | InstantiationException cnfe) {
				Log.debug(I18nHelper.getLogString("appmenubar.error.in.look.and.feel"));
			}
		}

		this.view.add(this.lf);

		for (Locale locale : Locale.getAvailableLocales()) {
			if (I18nHelper.hasLocale(locale)) {
				javax.swing.JMenuItem tmp = new javax.swing.JMenuItem(locale.getDisplayName());
				tmp.addActionListener(this);
				this.lo.add(tmp);
			}
		}
		this.view.add(this.lo);

		this.background.add(this.stdback);
		this.view.add(this.background);

		this.manage.addActionListener(this);

		this.add(this.file);
		this.add(this.opt);
		this.add(this.view);
		this.add(this.tools);
		this.add(this.bookmarks);
		this.add(this.info);

		this.loadBookmarks();

	}

	public void loadBookmarks() {
		this.marks = new HashMap<>();
		this.bookmarks.removeAll();
		this.bookmarks.add(this.add);
		this.bookmarks.add(this.manage);
		this.bookmarks.addSeparator();

		String data = "";

		try {
			DataInput in = new DataInputStream(new BufferedInputStream(new FileInputStream(Settings.bookmarks)));

			while (null != (data = in.readLine())) {
				if (!data.startsWith("#") && !data.trim().isEmpty()) {
					this.addBookmarkLine(data);
				}
			}
		} catch (IOException e) {
			Log.out(I18nHelper.getLogString("no.bookmarks.txt.found.using.defaults"));

			this.addBookmark("FTP", "ftp.kernel.org", "anonymous", "j-ftp@sf.net", 21, "/pub/linux/kernel", "false");
			this.addBookmark("FTP", "upload.sourceforge.net", "anonymous", "j-ftp@sf.net", 21, "/incoming", "false");
			this.addBookmark("SMB", "(LAN)", "guest", "guest", -1, "-", "false");

		}
	}

	private void addBookmarkLine(String tmp) {
		try {
			StringTokenizer t = new StringTokenizer(tmp, "#", false);

			if (tmp.toLowerCase().trim().startsWith("<dir>")) {
				String dir = tmp.substring(tmp.indexOf('>') + 1, tmp.lastIndexOf('<'));

				JMenu m = new JMenu(dir);
				this.current.add(m);

				this.last = this.current;
				this.current = m;
			} else if (tmp.toLowerCase().trim().startsWith("<enddir>")) {
				this.current = this.last;
			} else {
				this.addBookmark(t.nextToken(), t.nextToken(), t.nextToken(), t.nextToken(),
						Integer.parseInt(t.nextToken()), t.nextToken(), t.nextToken());
			}
		} catch (Exception ex) {
			Log.debug("Broken line: " + tmp);
			ex.printStackTrace();
		}
	}

	private void addBookmark(String pr, String h, String u, String p, int po, String d, String l) {
		BookmarkItem x = new BookmarkItem(h);
		x.setUserdata(u, p);

		if (l.trim().startsWith("t")) {
			x.setLocal(true);
		}

		x.setPort(po);
		x.setProtocol(pr);
		x.setDirectory(d);

		//bookmarks
		this.current.add(x);
		this.marks.put(x.getText(), x);
		x.addActionListener(this);
	}

	public void resetFileItems() {
		this.file.removeAll();

		this.file.add(this.ftpCon);
		this.file.add(this.sftpCon);
		this.file.add(this.rsyncCon);
		this.file.add(this.smbCon);
		this.file.add(this.nfsCon);
		this.file.add(this.webdavCon);
		this.file.addSeparator();
		this.file.add(this.close);
		this.file.addSeparator();
		this.file.addSeparator();
		this.file.add(this.localFtpCon);
		this.file.add(this.localSftpCon);
		this.file.add(this.localSmbCon);
		this.file.add(this.localNfsCon);

		this.file.addSeparator();
		this.file.add(this.closeLocalCon);
		this.file.addSeparator();

		boolean connectionsExist = false;

		try {
			//*** get the information on the last connections
			this.cons = new String[JFtp.CAPACITY][JFtp.CONNECTION_DATA_LENGTH];

			this.cons = LastConnections.readFromFile(JFtp.CAPACITY);

			String protocol;

			String htmp;

			String utmp;

			String conNumber;
			String usingLocal = "";
			int conNumberInt;

			for (int i = 0; JFtp.CAPACITY > i; i++) {
				if (!(this.cons[i][0].equals("null"))) {
					protocol = this.cons[i][0];
					htmp = this.cons[i][1];
					utmp = this.cons[i][2];

					int j = 3;

					while (!(this.cons[i][j].equals(LastConnections.SENTINEL))) {
						j++;
					}

					usingLocal = this.cons[i][j - 1];

					if (usingLocal.equals("true")) {
						usingLocal = "(in local tab)";
					} else {
						usingLocal = "";
					}

					conNumberInt = i + 1;
					conNumber = Integer.toString(conNumberInt);

					this.lastConData[i] = conNumber + " " + protocol + ": " + htmp + " " + usingLocal;

					this.lastConnections[i] = new JMenuItem(this.lastConData[i]);
					this.lastConnections[i].addActionListener(this);

					connectionsExist = true;

					this.file.add(this.lastConnections[i]);
				}
			}
		} catch (Exception ex) {
			Log.debug(I18nHelper.getLogString("warning.remembered.connections.broken"));
			ex.printStackTrace();
		}

		if (connectionsExist) {
			this.file.addSeparator();
		}

		this.file.add(this.exit);

		this.setMnemonics();
	}


	public void actionPerformed(ActionEvent e) {
		try {
			if (e.getSource() == this.proxy) {
				JFtp.statusP.jftp.addToDesktop(I18nHelper.getUIString("proxy.settings"), new ProxyChooser(), 500, 110);
			} else if (e.getSource() == this.add) {
				Log.out(I18nHelper.getLogString("add.called"));

				AddBookmarks a = new AddBookmarks(JFtp.statusP.jftp);
				a.update();
			} else if (e.getSource() == this.webdavCon) {
				WebdavHostChooser hc = new WebdavHostChooser();
				hc.toFront();
				hc.update();
			} else if ((e.getSource() == this.localFtpCon) && (!JFtp.uiBlocked)) {
				HostChooser hc = new HostChooser(null, true);
				hc.toFront();

				hc.update();
			} else if ((e.getSource() == this.localSmbCon) && (!JFtp.uiBlocked)) {
				SmbHostChooser hc = new SmbHostChooser(null, true);
				hc.toFront();

				hc.update();
			} else if ((e.getSource() == this.localSftpCon) && (!JFtp.uiBlocked)) {
				SftpHostChooser hc = new SftpHostChooser(null, true);
				hc.toFront();

				hc.update();
			} else if ((e.getSource() == this.localNfsCon) && (!JFtp.uiBlocked)) {
				NfsHostChooser hc = new NfsHostChooser(null, true);
				hc.toFront();

				hc.update();
			} else if ((e.getSource() == this.localWebdavCon) && (!JFtp.uiBlocked)) {
				WebdavHostChooser hc = new WebdavHostChooser(null, true);
				hc.toFront();

				hc.update();
			} else if (e.getSource() == this.closeLocalCon) {
				JFtp.statusP.jftp.closeCurrentLocalTab();
			} else if (e.getSource() == this.clear) {
				JFtp.clearLog();
			} else if (e.getSource() == this.spider) {
				this.jftp.addToDesktop(I18nHelper.getUIString("http.recursive.download"), new HttpSpider(
						JFtp.localDir.getPath() + "_httpdownload/"), 440, 250);
			} else if (e.getSource() == this.raw) {
				RawConnection c = new RawConnection();
			} else if (e.getSource() == this.shell) {
				UIUtils.runCommand("/bin/bash");
			} else if (e.getSource() == this.xkcd) {
				String messageText = this.getXkcdMessage();
				JOptionPane.showMessageDialog(JFtp.mainFrame, messageText, I18nHelper.getUIString(
						"todays.xkcd.popup" + ".title"), JOptionPane.INFORMATION_MESSAGE);
			} else if (e.getSource() == this.loadAudio) {
				try {
					JFileChooser f = new JFileChooser();
					f.showOpenDialog(this.jftp);

					File file = f.getSelectedFile();

					Player p = new Player(new FileInputStream(file));

					p.play();
				} catch (Exception ex) {
					ex.printStackTrace();
					Log.debug("Error: (" + ex + ")");
				}
			} else if (e.getSource() == this.exit) {
				this.jftp.windowClosing(null); // handles everything
			} else if (e.getSource() == this.close) {
				JFtp.statusP.jftp.closeCurrentTab();

			} else if ((e.getSource() == this.ftpCon) && (!JFtp.uiBlocked)) {

				HostChooser hc = new HostChooser();
				hc.toFront();


				hc.update();
			} else if ((e.getSource() == this.smbCon) && (!JFtp.uiBlocked)) {

				SmbHostChooser hc = new SmbHostChooser();
				hc.toFront();

				hc.update();
			} else if ((e.getSource() == this.sftpCon) && (!JFtp.uiBlocked)) {

				SftpHostChooser hc = new SftpHostChooser();
				hc.toFront();
				hc.update();
			} else if ((e.getSource() == this.rsyncCon) && (!JFtp.uiBlocked)) {
				RsyncHostChooser hc = new RsyncHostChooser();
				hc.toFront();

				hc.update();
			} else if ((e.getSource() == this.nfsCon) && (!JFtp.uiBlocked)) {

				NfsHostChooser hc = new NfsHostChooser();
				hc.toFront();

				hc.update();
			} else if (e.getSource() == this.resuming) {
				boolean res = this.resuming.getState();
				Settings.enableResuming = res;
				Settings.setProperty("jftp.enableResuming", res);
				this.ask.setEnabled(Settings.enableResuming);
				Settings.save();
			} else if (e.getSource() == this.useNewIcons) {
				boolean res = this.useNewIcons.getState();
				Settings.setProperty("jftp.gui.look.newIcons", res);
				Settings.save();

				JOptionPane.showMessageDialog(this, I18nHelper.getUIString(
						"please.restart.jftp.to.have.the.ui" + ".changed"));
			} else if (e.getSource() == this.hideHidden) {
				boolean res = this.hideHidden.getState();
				Settings.setProperty("jftp.hideHiddenDotNames", res);
				Settings.save();

				JFtp.localUpdate();
			} else if (e.getSource() == this.nl) {
				Settings.showNewlineOption = this.nl.getState();
			} else if (e.getSource() == this.stdback) {
				Settings.setProperty("jftp.useBackground", this.stdback.getState());
				Settings.save();
				JFtp.statusP.jftp.fireUpdate();
			} else if (e.getSource() == this.sshKeys) {
				Settings.setProperty("jftp.useSshKeyVerification", this.sshKeys.getState());
				Settings.save();
				JFtp.statusP.jftp.fireUpdate();
			} else if (e.getSource() == this.rssDisabled) {
				Settings.setProperty("jftp.enableRSS", this.rssDisabled.getState());
				Settings.save();

				JFtp.statusP.jftp.fireUpdate();

				String feed = Settings.getProperty("jftp.customRSSFeed");
				if (null != feed && !feed.isEmpty()) {
					feed = "http://slashdot.org/rss/slashdot.rss";
				}

				this.switchRSS(feed);
			} else if (e.getSource() == this.loadRss) {
				String what = JOptionPane.showInputDialog(I18nHelper.getUIString("enter.url"), "http://");

				if (null == what) {
					return;
				}

				this.switchRSS(what);
			} else if (e.getSource() == this.loadSlash) {
				this.switchRSS("http://slashdot.org/rss/slashdot.rss");
			} else if (e.getSource() == this.loadCNN1) {
				this.switchRSS("http://rss.cnn.com/rss/cnn_topstories.rss");
			} else if (e.getSource() == this.loadCNN2) {
				this.switchRSS("http://rss.cnn.com/rss/cnn_world.rss");
			} else if (e.getSource() == this.loadCNN3) {
				this.switchRSS("http://rss.cnn.com/rss/cnn_tech.rss");
			} else if (e.getSource() == debug) {
				Settings.setProperty("jftp.enableDebug", debug.getState());
				Settings.save();
			} else if (e.getSource() == disableLog) {
				Settings.setProperty("jftp.disableLog", disableLog.getState());
				Settings.save();
			} else if (e.getSource() == this.smbThreads) {
				Settings.setProperty("jftp.enableSmbMultiThreading", this.smbThreads.getState());
				Settings.save();
			} else if (e.getSource() == this.sftpThreads) {
				Settings.setProperty("jftp.enableSftpMultiThreading", this.sftpThreads.getState());
				Settings.save();
			} else if (e.getSource() == this.ask) {
				Settings.askToResume = this.ask.getState();
			} else if (e.getSource() == this.http) {
				HttpDownloader dl = new HttpDownloader();
				this.jftp.addToDesktop(I18nHelper.getUIString("http.download"), dl, 480, 100);
				this.jftp.setLocation(dl.hashCode(), 100, 150);
			} else if (e.getSource() == this.about) {
				showAbout();
			} else if (e.getSource() == fadeMenu) {
				Settings.setProperty("jftp.gui.enableStatusAnimation", fadeMenu.getState());
				Settings.save();
			} else if (e.getSource() == askToDelete) {
				Settings.setProperty("jftp.gui.askToDelete", askToDelete.getState());
				Settings.save();
			} else if ((e.getSource() == this.lastConnections[0]) && (!JFtp.uiBlocked)) {
				this.connectionSelected(0);
			} else if ((e.getSource() == this.lastConnections[1]) && (!JFtp.uiBlocked)) {
				this.connectionSelected(1);
			} else if ((e.getSource() == this.lastConnections[2]) && (!JFtp.uiBlocked)) {
				this.connectionSelected(2);
			} else if ((e.getSource() == this.lastConnections[3]) && (!JFtp.uiBlocked)) {
				this.connectionSelected(3);
			} else if ((e.getSource() == this.lastConnections[4]) && (!JFtp.uiBlocked)) {
				this.connectionSelected(4);
			} else if ((e.getSource() == this.lastConnections[5]) && (!JFtp.uiBlocked)) {
				this.connectionSelected(5);
			} else if ((e.getSource() == this.lastConnections[6]) && (!JFtp.uiBlocked)) {
				this.connectionSelected(6);
			} else if ((e.getSource() == this.lastConnections[7]) && (!JFtp.uiBlocked)) {
				this.connectionSelected(7);
			} else if ((e.getSource() == this.lastConnections[8]) && (!JFtp.uiBlocked)) {
				this.connectionSelected(8);
			} else if (e.getSource() == this.rawCon) {
				RawConnectionMenu rcm = new RawConnectionMenu();
				this.jftp.addToDesktop("Send raw FTP command...", rcm, 500, 180);
				this.jftp.setLocation(rcm.hashCode(), 110, 180);
			} else if (e.getSource() == this.opts) {
				AdvancedOptions adv = new AdvancedOptions();
				this.jftp.addToDesktop(I18nHelper.getUIString("advanced.options"), adv, 500, 180);
				this.jftp.setLocation(adv.hashCode(), 110, 180);
			} else if (e.getSource() == this.manage) {
				BookmarkManager m = new BookmarkManager();
				JFtp.desktop.add(m, Integer.MAX_VALUE - 10);
			} else if (this.marks.containsKey(e.getSource())) {
				((BookmarkItem) e.getSource()).connect();
			} else if (e.getSource() == this.storePasswords) {
				boolean state = this.storePasswords.getState();

				if (!state) {
					JOptionPane j = new JOptionPane();
					int x = JOptionPane.showConfirmDialog(this.storePasswords, I18nHelper.getUIString("you.chose.not"
							+ ".to.save.passwords.do.you.want.your.old.login.data.to.be.deleted"),
							I18nHelper.getUIString("delete.old.passwords"), JOptionPane.YES_NO_OPTION);

					if (javax.swing.JOptionPane.YES_OPTION == x) {
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
			} else {
				String tmp = ((JMenuItem) e.getSource()).getText();

				UIManager.LookAndFeelInfo[] m = UIManager.getInstalledLookAndFeels();

				for (javax.swing.UIManager.LookAndFeelInfo lookAndFeelInfo : m) {
					if (lookAndFeelInfo.getName().equals(tmp)) {
						JFtp.statusP.jftp.setLookAndFeel(lookAndFeelInfo.getClassName());
						Settings.setProperty("jftp.gui.look", lookAndFeelInfo.getClassName());
						Settings.save();
					}
				}

				for (Locale locale : Locale.getAvailableLocales()) {
					if (locale.getDisplayName().equals(tmp)) {
						System.out.println("Matched locale: " + locale);
						I18nHelper.setLocale(locale);
						Settings.writeChangedProperty(Settings.LOCALE_KEY, locale.toLanguageTag());
						break;
					}
				}
				JOptionPane.showMessageDialog(JFtp.mainFrame, I18nHelper.getUIString(
						"you.must.reload.jftp.for.this.to" + ".fully.take.effect"));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			Log.debug(ex.toString());
		}
	}

	private String getXkcdMessage() {
		StringBuilder jsonString = new StringBuilder();
		try {
			URL url = new URL(XKCD_CONSTANTS.API_URL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
				for (String line; (line = reader.readLine()) != null; ) {
					jsonString.append(line);
				}
			}
		} catch (Exception ex) {
			String failed = I18nHelper.getUIString("failed.to.contact.xkcd");
			Log.debug(failed);
			Log.debug(ex.toString());
			return failed;
		}
		JSONObject obj = new JSONObject(jsonString.toString());
		try {
			Log.debug(MessageFormat.format(I18nHelper.getLogString("xkcd.log.message"), obj.toString(1)));
			String transcript = obj.getString(XKCD_CONSTANTS.TRANSCRIPT);
			transcript = (transcript.isEmpty() ? I18nHelper.getUIString("xkcd.no.transcript.provided") : transcript);
			return MessageFormat.format(I18nHelper.getUIString("today.s.xkcd.0.1.transcript.2.alt.text.3"),
					obj.getInt(XKCD_CONSTANTS.COMIC_NUMBER), obj.getString(XKCD_CONSTANTS.SAFE_TITLE), transcript,
					obj.getString(XKCD_CONSTANTS.ALT_TEXT));
		} catch (JSONException ex) {
			String failed = I18nHelper.getUIString("failed.to.parse.xkcd");
			Log.debug(failed);
			Log.debug(ex.toString());
			return failed;
		}
	}

	private void switchRSS(String url) {
		Settings.setProperty("jftp.customRSSFeed", url);
		Settings.save();


		if (null == JFtp.statusP.jftp.feeder) {
			JFtp.statusP.jftp.addRSS();
		}

		JFtp.statusP.jftp.feeder.switchTo(url);
	}

	// by jake
	private void setMnemonics() {
		this.file.setMnemonic('F');
		this.opt.setMnemonic('O');
		this.view.setMnemonic('V');
		this.tools.setMnemonic('T');
		this.bookmarks.setMnemonic('B');
		this.info.setMnemonic('I');

		this.ftpCon.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
		this.sftpCon.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
		this.smbCon.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
		this.nfsCon.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));

		this.close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));


		this.localFtpCon.setMnemonic('F');
		this.localSftpCon.setMnemonic('S');
		this.localSmbCon.setMnemonic('L');
		this.localNfsCon.setMnemonic('N');

		this.closeLocalCon.setMnemonic('C');

		this.exit.setMnemonic('X');

		this.proxy.setMnemonic('P');

		this.http.setMnemonic('D');
		this.spider.setMnemonic('H');
		this.raw.setMnemonic('T');

		this.opts.setMnemonic('A');
		this.manage.setMnemonic('M');

		this.clear.setMnemonic('C');
		clearItems.setMnemonic('F');

		try {
			int intI;
			String stringI;
			char charI;

			for (int i = 0; JFtp.CAPACITY > i; i++) {

				if (!(this.cons[i][0].equals("null"))) {
					intI = i + 1;
					stringI = Integer.toString(intI);
					charI = stringI.charAt(0);

					this.lastConnections[i].setMnemonic(charI);
				}
			}

		} catch (Exception ex) {
			Log.out(I18nHelper.getLogString("warning.appmenubar.produced.exception.ignored.it"));
			ex.printStackTrace();
		}
	}

	//setMnemonics
	private void connectionSelected(int position) {
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


		protocol = this.cons[position][0];
		htmp = this.cons[position][1];
		utmp = this.cons[position][2];
		ptmp = this.cons[position][3];

		if (ptmp.isEmpty()) {
			ptmp = UIUtils.getPasswordFromUser(JFtp.statusP.jftp);
		}

		switch (protocol) {
			case "FTP":
				potmpString = this.cons[position][4];
				dtmp = this.cons[position][5];
				useLocalString = this.cons[position][6];

				potmp = Integer.parseInt(potmpString);

				useLocal = useLocalString.equals("true");

				StartConnection.startFtpCon(htmp, utmp, ptmp, potmp, dtmp, useLocal);

				break;
			case "SFTP":

				potmpString = this.cons[position][4];
				useLocalString = this.cons[position][5];
				break;
			case "NFS":
				useLocalString = this.cons[position][4];
				break;
			case "SMB":

				dtmp = this.cons[position][4];
				useLocalString = this.cons[position][5];
				break;
		}

		potmp = Integer.parseInt(potmpString);

		useLocal = useLocalString.equals("true");

		if (protocol.equals("SFTP")) {

			StartConnection.startCon(protocol, htmp, utmp, ptmp, potmp, dtmp, useLocal);
		} else if (!(protocol.equals("FTP"))) {

			StartConnection.startCon(protocol, htmp, utmp, ptmp, potmp, dtmp, useLocal);
		}
	}

	private static void showAbout() {
		JOptionPane.showMessageDialog(JFtp.mainFrame,
				"New\u2122 jFTP Plus Ultra Deluxe v0.0.1-RC1_ALPHA\n\n" + "Original Work by Cyberdemon\n"
						+ "\"Optimizations\" by Alex Westerman, Omar Roth, Rob Budak, Dalton Julian, and Emily Hart "
						+ "(2022)\n\n" + "(we tried)");
	}


	private enum XKCD_CONSTANTS {
		;
		static final String API_URL = "https://xkcd.com/info.0.json";
		static final String COMIC_NUMBER = "num";
		static final String SAFE_TITLE = "safe_title";
		static final String ALT_TEXT = "alt";
		static final String TRANSCRIPT = "transcript";
	}

}
