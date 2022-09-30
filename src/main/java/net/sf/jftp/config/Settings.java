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
package net.sf.jftp.config;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;


public class Settings {
	public static final String propertyFilename = System.getProperty("user.home") + File.separator + ".jftp/jftp.properties".replace('/', File.separatorChar);
	public static final String defaultWidth = "1000";
	public static final String defaultHeight = "740";
	public static final String defaultX = "20";
	public static final String defaultY = "20";

	public static final String title = "JFtp - The Java Network Browser";

	public static final String insomniacTitle = ">>> Insomniac client BETA 1 <<< Based on JFtp ";
	public static final int visibleFileRows = 15;
	public static final int connectionTimeout = 30000;
	public static final int testTimeout = 5000;
	public static final String defaultDir = "<default>";
	public static final String defaultWorkDir = System.getProperty("user.home");
	public static final String userHomeDir = System.getProperty("user.home");
	public static final String appHomeDir = net.sf.jftp.config.Settings.userHomeDir + "/.jftp/".replace('/', File.separatorChar);
	public static final String bookmarks = net.sf.jftp.config.Settings.appHomeDir + "bookmarks.txt".replace('/', File.separatorChar);
	public static final String ls_out = net.sf.jftp.config.Settings.appHomeDir + ".ls_out".replace('/', File.separatorChar);
	public static final String login_def = net.sf.jftp.config.Settings.appHomeDir + ".login_default".replace('/', File.separatorChar);
	public static final String login = net.sf.jftp.config.Settings.appHomeDir + ".login".replace('/', File.separatorChar);
	//*** The new files that I have created
	public static final String last_cons = net.sf.jftp.config.Settings.appHomeDir + ".last_cons".replace('/', File.separatorChar);
	public static final String login_def_sftp = net.sf.jftp.config.Settings.appHomeDir + ".last_cons_sftp".replace('/', File.separatorChar);
	public static final String login_def_smb = net.sf.jftp.config.Settings.appHomeDir + ".last_cons_smb".replace('/', File.separatorChar);
	public static final String login_def_nfs = net.sf.jftp.config.Settings.appHomeDir + ".last_cons_nfs".replace('/', File.separatorChar);
	//***
	//*** added in version 1.44
	public static final String adv_settings = net.sf.jftp.config.Settings.appHomeDir + ".adv_settings".replace('/', File.separatorChar);
	public static final String greeting = "Have a lot of fun...";
	//***
	public static final String readme = "docs/readme";
	public static final String changelog = "docs/CHANGELOG";
	public static final String todo = "docs/TODO";
	public static final String nfsinfo = "docs/doc/nfsinfo";
	private static final Properties p = new Properties();
	private static final String defaultFtpPasvMode = "true";
	private static final String defaultEnableDebug = "false";
	private static final String enableMultiThreading = "true";
	private static final String enableSmbMultiThreading = "true";
	private static final String enableSftpMultiThreading = "true";
	private static final String noUploadMultiThreading = "false";
	private static final String storePasswords = "false";
	public static final boolean IS_JAVA_1_6 = true;
	public static String sshHostKeyVerificationFile = System.getProperty("user.home") + File.separator + ".jftp" + File.separator + ".ssh_hostfile";
	public static int maxConnections = 3;
	public static boolean enableResuming = false; // overridden by JFtp
	public static boolean enableUploadResuming = false;
	public static boolean noUploadResumingQuestion = true;
	public static boolean askToResume = true;
	public static final boolean reconnect = true;
	public static final int uiRefresh = 500;
	public static final int logFlushInterval = 2000; // obsolete
	public static final boolean useLogFlusher = false; // obsolete
	public static final int ftpTransferThreadPause = 2000;
	public static final int smallSize = 0; //100000;
	public static final int smallSizeUp = 0; //50000;
	public static final boolean shortProgress = true;
	public static final boolean useFixedTableWidths = true;
	public static final boolean enableWebDav = false;
	public static final boolean ftpKeepAlive = true;
	public static final int ftpKeepAliveInterval = 29000;
	// 1: manual, 2: onclick, 0: off
	public static final int runtimeCommands = 2;
	public static final boolean askToRun = false;
	// currently changed by remotedir on-the-fly
	public static boolean showDateNoSize = false;
	public static boolean showLocalDateNoSize = false;
	// hides some messages like MODE, Type etc.
	public static final boolean hideStatus = false;
	public static boolean showNewlineOption = false;
	// for DataConnection - lower means less buffer, more updates in the downloadmanager
	// i recommend to use values greater than 2048 bytes
	public static int bufferSize = 8192;
	// sends NOOPs to ensure that buffers are empty
	public static final boolean safeMode = false;
	// enables some delays
	public static final boolean enableFtpDelays = false;
	// override ui with the insomniac client configuration
	public static boolean isInsomniacClient = false;
	public static final int refreshDelay = 250;
	public static boolean useDefaultDir = true;
	// may the windows be resized?
	public static final boolean resize = true;
	public static final boolean showFileSize = true;
	public static boolean sortDir = true;
	public static int scrollSpeed = 9;
	public static int numFiles = 9;
	public static final int statusMessageAfterMillis = 1000;
	public static String iconImage;
	public static String hostImage;
	public static String closeImage;
	public static String infoImage;
	public static String listImage;

	public static String rmdirImage;
	public static String mkdirImage;
	public static String refreshImage;
	public static String refreshImage2;
	public static String cdImage;
	public static String cmdImage;
	public static String downloadImage;
	public static String queueImage;
	public static String uploadImage;
	public static String fileImage;
	public static String dirImage;
	public static String codeFileImage;
	public static String textFileImage;
	public static String execFileImage;
	public static String audioFileImage;
	public static String videoFileImage;
	public static String htmlFileImage;
	public static String zipFileImage;
	public static String imageFileImage;
	public static String presentationFileImage;
	public static String spreadsheetFileImage;
	public static String bookFileImage;
	public static String copyImage;
	public static String openImage;
	public static String sftpImage;
	public static String nfsImage;
	public static String webdavImage;
	public static String linkImage;
	public static String typeImage;
	public static String deleteImage;
	public static String deleteImage2;
	public static String clearImage;
	public static String clearImage2;
	public static String resumeImage;
	public static String resumeImage2;
	public static String pauseImage;
	public static String pauseImage2;
	public static String saveImage;
	public static String cdUpImage;
	public static String nextRSSImage;
	public static String helpImage;

	public static final String scrollLockImage = "images/current/drive_link.png";
	public static final String clearLogImage = "images/current/delete.png";


	public static final String background = "images/back.jpg";

	//set to false if you want it to run as an applet
	public static boolean isStandalone = true;


	public static final String hiddenPassword = "<%hidden%>";

	static {
		try {
			net.sf.jftp.config.Settings.p.load(new FileInputStream(net.sf.jftp.config.Settings.propertyFilename));
		} catch (final Exception e) {
			System.out.println("no property file loaded, using defaults... (" + e + ")");
		}

		if (!net.sf.jftp.config.Settings.getUseNewIcons()) {
			net.sf.jftp.config.Settings.iconImage = "images/org/javalobby/icons/20x20/ComputerIn.gif";
			net.sf.jftp.config.Settings.hostImage = "images/org/javalobby/icons/20x20/ComputerIn.gif";
			net.sf.jftp.config.Settings.closeImage = "images/org/javalobby/icons/20x20/Error.gif";
			net.sf.jftp.config.Settings.infoImage = "images/org/javalobby/icons/20x20/Inform.gif";
			net.sf.jftp.config.Settings.listImage = "images/org/javalobby/icons/20x20/List.gif";
			net.sf.jftp.config.Settings.deleteImage = "images/org/javalobby/icons/16x16/DeleteDocument.gif";
			net.sf.jftp.config.Settings.deleteImage2 = "images/org/javalobby/icons/16x16/DeleteDocument.gif";
			net.sf.jftp.config.Settings.rmdirImage = "images/org/javalobby/icons/16x16/DeleteFolder.gif";
			net.sf.jftp.config.Settings.mkdirImage = "images/org/javalobby/icons/16x16/NewFolder.gif";
			net.sf.jftp.config.Settings.refreshImage = "images/org/javalobby/icons/16x16/Undo.gif";
			net.sf.jftp.config.Settings.refreshImage2 = "images/org/javalobby/icons/16x16/Undo.gif";
			net.sf.jftp.config.Settings.cdImage = "images/org/javalobby/icons/16x16/Open.gif";
			net.sf.jftp.config.Settings.cmdImage = "images/org/javalobby/icons/16x16/ExecuteProject.gif";
			net.sf.jftp.config.Settings.downloadImage = "images/org/javalobby/icons/16x16/Left.gif";
			net.sf.jftp.config.Settings.uploadImage = "images/org/javalobby/icons/16x16/Right.gif";
			net.sf.jftp.config.Settings.queueImage = "images/org/javalobby/icons/16x16/Left.gif";
			net.sf.jftp.config.Settings.fileImage = "images/org/javalobby/icons/16x16/Document.gif";
			net.sf.jftp.config.Settings.dirImage = "images/org/javalobby/icons/16x16/Folder.gif";
			net.sf.jftp.config.Settings.codeFileImage = "images/org/javalobby/icons/16x16/List.gif";
			net.sf.jftp.config.Settings.textFileImage = "images/org/javalobby/icons/16x16/DocumentDraw.gif";
			net.sf.jftp.config.Settings.execFileImage = "images/org/javalobby/icons/16x16/ExecuteProject.gif";
			net.sf.jftp.config.Settings.audioFileImage = "images/org/javalobby/icons/16x16/cd.gif";
			net.sf.jftp.config.Settings.videoFileImage = "images/org/javalobby/icons/16x16/CameraFlash.gif";
			net.sf.jftp.config.Settings.htmlFileImage = "images/org/javalobby/icons/16x16/World2.gif";
			net.sf.jftp.config.Settings.zipFileImage = "images/org/javalobby/icons/16x16/DataStore.gif";
			net.sf.jftp.config.Settings.imageFileImage = "images/org/javalobby/icons/16x16/Camera.gif";
			net.sf.jftp.config.Settings.presentationFileImage = "images/org/javalobby/icons/16x16/DocumentDiagram.gif";
			net.sf.jftp.config.Settings.spreadsheetFileImage = "images/org/javalobby/icons/16x16/DatePicker.gif";
			net.sf.jftp.config.Settings.bookFileImage = "images/org/javalobby/icons/16x16/Book.gif";
			net.sf.jftp.config.Settings.copyImage = "images/org/javalobby/icons/16x16/Copy.gif";
			net.sf.jftp.config.Settings.openImage = "images/org/javalobby/icons/16x16/World2.gif";
			net.sf.jftp.config.Settings.sftpImage = "images/org/javalobby/icons/16x16/NewEnvelope.gif";
			net.sf.jftp.config.Settings.nfsImage = "images/org/javalobby/icons/16x16/TrafficGreen.gif";
			net.sf.jftp.config.Settings.webdavImage = "images/org/javalobby/icons/16x16/DataStore.gif";
			net.sf.jftp.config.Settings.linkImage = "images/org/javalobby/icons/16x16/Right.gif";
			net.sf.jftp.config.Settings.typeImage = "images/org/javalobby/icons/20x20/Type.gif";
			net.sf.jftp.config.Settings.clearImage = "images/org/javalobby/icons/16x16/Undo.gif";
			net.sf.jftp.config.Settings.resumeImage = "images/org/javalobby/icons/16x16/GreenCircle.gif";
			net.sf.jftp.config.Settings.pauseImage = "images/org/javalobby/icons/16x16/RedCircle.gif";
			net.sf.jftp.config.Settings.clearImage2 = "images/org/javalobby/icons/16x16/Undo.gif";
			net.sf.jftp.config.Settings.resumeImage2 = "images/org/javalobby/icons/16x16/GreenCircle.gif";
			net.sf.jftp.config.Settings.pauseImage2 = "images/org/javalobby/icons/16x16/RedCircle.gif";
			net.sf.jftp.config.Settings.saveImage = "images/org/javalobby/icons/16x16/Save.gif";
			net.sf.jftp.config.Settings.cdUpImage = "images/org/javalobby/icons/16x16/Exit.gif";
			net.sf.jftp.config.Settings.nextRSSImage = "images/org/javalobby/icons/16x16/Forward.gif";
			net.sf.jftp.config.Settings.helpImage = "images/current/help.png";
		} else {
			net.sf.jftp.config.Settings.iconImage = "images/current/server_add.png";
			net.sf.jftp.config.Settings.hostImage = "images/current/server_add.png";
			net.sf.jftp.config.Settings.closeImage = "images/current/cancel.png";
			net.sf.jftp.config.Settings.infoImage = "images/current/information.png";
			net.sf.jftp.config.Settings.listImage = "images/current/page_white_text.png";

			net.sf.jftp.config.Settings.rmdirImage = "images/current/folder_delete.png";
			net.sf.jftp.config.Settings.mkdirImage = "images/current/folder_add.png";
			net.sf.jftp.config.Settings.refreshImage = "images/current/control_repeat.png";
			net.sf.jftp.config.Settings.refreshImage2 = "images/current/control_repeat_blue.png";
			net.sf.jftp.config.Settings.cdImage = "images/current/folder_explore.png";
			net.sf.jftp.config.Settings.cmdImage = "images/current/application_xp_terminal.png";
			net.sf.jftp.config.Settings.downloadImage = "images/graphicsfuel/arrow-left.png";
			net.sf.jftp.config.Settings.uploadImage = "images/graphicsfuel/arrow-right.png";
			net.sf.jftp.config.Settings.queueImage = "images/current/arrow_left.png";
			net.sf.jftp.config.Settings.fileImage = "images/current/page_white.png";
			net.sf.jftp.config.Settings.dirImage = "images/current/folder.png";
			net.sf.jftp.config.Settings.codeFileImage = "images/current/page_white_wrench.png";
			net.sf.jftp.config.Settings.textFileImage = "images/current/page_white_text.png";
			net.sf.jftp.config.Settings.execFileImage = "images/current/cog.png";
			net.sf.jftp.config.Settings.audioFileImage = "images/current/ipod.png";
			net.sf.jftp.config.Settings.videoFileImage = "images/current/camera.png";
			net.sf.jftp.config.Settings.htmlFileImage = "images/current/html.png";
			net.sf.jftp.config.Settings.zipFileImage = "images/current/page_white_zip.png";
			net.sf.jftp.config.Settings.imageFileImage = "images/current/image.png";
			net.sf.jftp.config.Settings.presentationFileImage = "images/current/page_white_powerpoint.png";
			net.sf.jftp.config.Settings.spreadsheetFileImage = "images/current/page_excel.png";
			net.sf.jftp.config.Settings.bookFileImage = "images/current/book.png";
			net.sf.jftp.config.Settings.copyImage = "images/current/disk_multiple.png";
			net.sf.jftp.config.Settings.openImage = "images/current/drive_web.png";
			net.sf.jftp.config.Settings.sftpImage = "images/current/drive_link.png";
			net.sf.jftp.config.Settings.nfsImage = "images/current/drive_network.png";
			net.sf.jftp.config.Settings.webdavImage = "images/current/house_go.png";
			net.sf.jftp.config.Settings.linkImage = "images/current/arrow_right.png";
			net.sf.jftp.config.Settings.typeImage = "images/current/email_go.png";
			net.sf.jftp.config.Settings.deleteImage = "images/current/control_stop.png";
			net.sf.jftp.config.Settings.deleteImage2 = "images/current/control_stop_blue.png";
			net.sf.jftp.config.Settings.clearImage = "images/current/control_eject.png";
			net.sf.jftp.config.Settings.clearImage2 = "images/current/control_eject_blue.png";
			net.sf.jftp.config.Settings.resumeImage = "images/current/control_play.png";
			net.sf.jftp.config.Settings.resumeImage2 = "images/current/control_play_blue.png";
			net.sf.jftp.config.Settings.pauseImage = "images/current/control_pause.png";
			net.sf.jftp.config.Settings.pauseImage2 = "images/current/control_pause_blue.png";
			net.sf.jftp.config.Settings.saveImage = "images/current/disk.png";
			net.sf.jftp.config.Settings.cdUpImage = "images/current/arrow_up.png";
			net.sf.jftp.config.Settings.nextRSSImage = "images/current/rss_go.png";
			net.sf.jftp.config.Settings.helpImage = "images/current/help.png";
		}

	}

	public static Object setProperty(final String key, final String value) {
		return net.sf.jftp.config.Settings.p.setProperty(key, value);
	}

	public static String getProperty(final String key) {
		return "" + net.sf.jftp.config.Settings.p.getProperty(key);
	}

	public static Object setProperty(final String key, final int value) {
		return net.sf.jftp.config.Settings.p.setProperty(key, Integer.toString(value));
	}

	public static Object setProperty(final String key, final boolean value) {
		String val = "false";

		if (value) {
			val = "true";
		}

		return net.sf.jftp.config.Settings.p.setProperty(key, val);
	}

	public static void save() {
		try {
			new File(System.getProperty("user.home") + File.separator + ".jftp").mkdir();
			net.sf.jftp.config.Settings.p.store(new FileOutputStream(net.sf.jftp.config.Settings.propertyFilename), "jftp.properties");
		} catch (final Exception e) {
			System.out.println("Cannot save properties...");

		}
	}

	public static boolean getHideLocalDotNames() {
		final String what = net.sf.jftp.config.Settings.p.getProperty("jftp.hideHiddenDotNames", "false");

		return !what.trim().equals("false");
	}

	public static int getMaxConnections() {
		return net.sf.jftp.config.Settings.maxConnections;
	}

	public static String getSocksProxyHost() {

		return net.sf.jftp.config.Settings.p.getProperty("jftp.socksProxyHost", "");
	}

	public static String getSocksProxyPort() {

		return net.sf.jftp.config.Settings.p.getProperty("jftp.socksProxyPort", "");
	}

	public static boolean getUseBackground() {
		final String what = net.sf.jftp.config.Settings.p.getProperty("jftp.useBackground", "true");

		return !what.trim().equals("false");
	}

	public static boolean getEnableSshKeys() {
		final String what = net.sf.jftp.config.Settings.p.getProperty("jftp.useSshKeyVerification", "false");

		return !what.trim().equals("false");
	}

	public static boolean getEnableResuming() {
		final String what = net.sf.jftp.config.Settings.p.getProperty("jftp.enableResuming", "true");

		return !what.trim().equals("false");
	}

	public static boolean getEnableDebug() {
		final String what = net.sf.jftp.config.Settings.p.getProperty("jftp.enableDebug", net.sf.jftp.config.Settings.defaultEnableDebug);

		return !what.trim().equals("false");
	}

	public static boolean getDisableLog() {
		final String what = net.sf.jftp.config.Settings.p.getProperty("jftp.disableLog", "false");

		return !what.trim().equals("false");
	}

	public static boolean getEnableStatusAnimation() {
		final String what = net.sf.jftp.config.Settings.p.getProperty("jftp.gui.enableStatusAnimation", "false");

		return !what.trim().equals("false");
	}

	public static boolean getAskToDelete() {
		final String what = net.sf.jftp.config.Settings.p.getProperty("jftp.gui.askToDelete", "true");

		return !what.trim().equals("false");
	}

	public static String getLookAndFeel() {
		return net.sf.jftp.config.Settings.p.getProperty("jftp.gui.look", null);
	}

	public static boolean getUseNewIcons() {
		final String what = net.sf.jftp.config.Settings.p.getProperty("jftp.gui.look.newIcons", "true");

		return !what.trim().equals("false");
	}

	public static boolean getEnableMultiThreading() {
		final String what = net.sf.jftp.config.Settings.p.getProperty("jftp.enableMultiThreading", net.sf.jftp.config.Settings.enableMultiThreading);

		return !what.trim().equals("false");
	}

	public static boolean getEnableSmbMultiThreading() {
		final String what = net.sf.jftp.config.Settings.p.getProperty("jftp.enableSmbMultiThreading", net.sf.jftp.config.Settings.enableSmbMultiThreading);

		return !what.trim().equals("false");
	}

	public static boolean getEnableSftpMultiThreading() {
		final String what = net.sf.jftp.config.Settings.p.getProperty("jftp.enableSftpMultiThreading", net.sf.jftp.config.Settings.enableSftpMultiThreading);

		return !what.trim().equals("false");
	}

	public static boolean getNoUploadMultiThreading() {
		final String what = net.sf.jftp.config.Settings.p.getProperty("jftp.noUploadMultiThreading", net.sf.jftp.config.Settings.noUploadMultiThreading);

		return !what.trim().equals("false");
	}

	public static boolean getFtpPasvMode() {
		final String what = net.sf.jftp.config.Settings.p.getProperty("jftp.ftpPasvMode", net.sf.jftp.config.Settings.defaultFtpPasvMode);

		return !what.trim().equals("false");
	}

	public static boolean getUseDefaultDir() {
		final String what = net.sf.jftp.config.Settings.p.getProperty("jftp.useDefaultDir", "true");

		return !what.trim().equals("false");
	}

	public static boolean getEnableRSS() {
		final String what = net.sf.jftp.config.Settings.p.getProperty("jftp.enableRSS", "false");

		return !what.trim().equals("false");
	}

	public static String getRSSFeed() {

		return net.sf.jftp.config.Settings.p.getProperty("jftp.customRSSFeed", "http://slashdot.org/rss/slashdot.rss");
	}

	public static java.awt.Dimension getWindowSize() {
		int width = Integer.parseInt(net.sf.jftp.config.Settings.p.getProperty("jftp.window.width", net.sf.jftp.config.Settings.defaultWidth));
		int height = Integer.parseInt(net.sf.jftp.config.Settings.p.getProperty("jftp.window.height", net.sf.jftp.config.Settings.defaultHeight));

		if (100 > width || 100 > height) {
			width = Integer.parseInt(net.sf.jftp.config.Settings.defaultWidth);
			height = Integer.parseInt(net.sf.jftp.config.Settings.defaultHeight);
		}

		return new java.awt.Dimension(width, height);
	}

	public static java.awt.Point getWindowLocation() {
		int x = Integer.parseInt(net.sf.jftp.config.Settings.p.getProperty("jftp.window.x", net.sf.jftp.config.Settings.defaultX));
		int y = Integer.parseInt(net.sf.jftp.config.Settings.p.getProperty("jftp.window.y", net.sf.jftp.config.Settings.defaultY));

		if (0 > x || 0 > y) {
			x = 0;
			y = 0;
		}

		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		if (x >= screenSize.height) {
			x = Integer.parseInt(net.sf.jftp.config.Settings.defaultX);
		}
		if (y >= screenSize.height) {
			y = Integer.parseInt(net.sf.jftp.config.Settings.defaultY);
		}

		return new java.awt.Point(x, y);
	}

	public static int getSocketTimeout() {
		return 3000;
	}

	public static boolean getStorePasswords() {
		final String what = net.sf.jftp.config.Settings.p.getProperty("jftp.security.storePasswords", net.sf.jftp.config.Settings.storePasswords);

		return !what.trim().equals("false");
	}

}
