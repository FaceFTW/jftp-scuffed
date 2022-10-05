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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;


public class Settings {
	private static final String propertyFilename = System.getProperty("user.home") + File.separator + ".jftp/jftp.properties".replace('/', File.separatorChar);
	private static final String defaultWidth = "1000";
	private static final String defaultHeight = "740";
	private static final String defaultX = "20";
	private static final String defaultY = "20";
	// title of the app
	public static final String title = "JFtp - The Java Network Browser";
	// overridden title for insomniac client
	public static final String insomniacTitle = ">>> Insomniac client BETA 1 <<< Based on JFtp ";
	public static final int visibleFileRows = 15;
	public static final int connectionTimeout = 30000;
	public static final int testTimeout = 5000;
	public static final String defaultDir = "<default>";
	public static final String defaultWorkDir = System.getProperty("user.home");
	private static final String userHomeDir = System.getProperty("user.home");
	public static final String appHomeDir = userHomeDir + "/.jftp/".replace('/', File.separatorChar);
	public static final String bookmarks = appHomeDir + "bookmarks.txt".replace('/', File.separatorChar);
	public static final String ls_out = appHomeDir + ".ls_out".replace('/', File.separatorChar);
	public static final String login_def = appHomeDir + ".login_default".replace('/', File.separatorChar);
	public static final String login = appHomeDir + ".login".replace('/', File.separatorChar);
	//*** The new files that I have created
	public static final String last_cons = appHomeDir + ".last_cons".replace('/', File.separatorChar);
	public static final String login_def_sftp = appHomeDir + ".last_cons_sftp".replace('/', File.separatorChar);
	public static final String login_def_smb = appHomeDir + ".last_cons_smb".replace('/', File.separatorChar);
	public static final String login_def_nfs = appHomeDir + ".last_cons_nfs".replace('/', File.separatorChar);
	//***
	//*** added in version 1.44
	public static final String adv_settings = appHomeDir + ".adv_settings".replace('/', File.separatorChar);
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
	public static boolean IS_JAVA_1_6 = true;
	public static String sshHostKeyVerificationFile = System.getProperty("user.home") + File.separator + ".jftp" + File.separator + ".ssh_hostfile";
	public static int maxConnections = 3;
	public static boolean enableResuming; // overridden by JFtp
	public static boolean enableUploadResuming;
	public static boolean noUploadResumingQuestion = true;
	public static boolean askToResume = true;
	public static boolean reconnect = true;
	public static int uiRefresh = 500;
	public static int logFlushInterval = 2000; // obsolete
	public static boolean useLogFlusher; // obsolete
	public static int ftpTransferThreadPause = 2000;
	public static int smallSize; //100000;
	public static int smallSizeUp; //50000;
	public static boolean shortProgress = true;
	public static boolean useFixedTableWidths = true;
	public static boolean enableWebDav;
	public static boolean ftpKeepAlive = true;
	public static int ftpKeepAliveInterval = 29000;
	// 1: manual, 2: onclick, 0: off
	public static int runtimeCommands = 2;
	public static boolean askToRun = false;
	// currently changed by remotedir on-the-fly
	public static boolean showDateNoSize;
	public static boolean showLocalDateNoSize;
	// hides some messages like MODE, Type etc.
	public static boolean hideStatus = false;
	public static boolean showNewlineOption = false;
	// for DataConnection - lower means less buffer, more updates in the downloadmanager
	public static int bufferSize = 1400;
	// sends NOOPs to ensure that buffers are empty
	public static boolean safeMode = false;
	// enables some delays
	public static boolean enableFtpDelays = false;
	// override ui with the insomniac client configuration
	public static boolean isInsomniacClient = false;
	public static int refreshDelay = 250;
	public static boolean useDefaultDir = true;
	// may the windows be resized?
	public static boolean resize = true;
	public static boolean showFileSize = true;
	public static boolean sortDir = true;
	public static int scrollSpeed = 9;
	public static int numFiles = 9;
	public static int statusMessageAfterMillis = 1000;
	public static String iconImage;
	public static String hostImage;
	public static String closeImage;
	private static String infoImage;
	public static String listImage;

	private static String rmdirImage;
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
	private static String bookFileImage;
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

	public static String scrollLockImage = "images/current/drive_link.png";
	public static String clearLogImage = "images/current/delete.png";


	public static String background = "images/back.jpg";

	//set to false if you want it to run as an applet
	public static boolean isStandalone = true;


	public static String hiddenPassword = "<%hidden%>";

	static {
		try {
			p.load(new FileInputStream(propertyFilename));
		} catch (Exception e) {
			System.out.println("no property file loaded, using defaults... (" + e + ")");
		}

		if (!getUseNewIcons()) {
			iconImage = "images/org/javalobby/icons/20x20/ComputerIn.gif";
			hostImage = "images/org/javalobby/icons/20x20/ComputerIn.gif";
			closeImage = "images/org/javalobby/icons/20x20/Error.gif";
			infoImage = "images/org/javalobby/icons/20x20/Inform.gif";
			listImage = "images/org/javalobby/icons/20x20/List.gif";
			deleteImage = "images/org/javalobby/icons/16x16/DeleteDocument.gif";
			deleteImage2 = "images/org/javalobby/icons/16x16/DeleteDocument.gif";
			rmdirImage = "images/org/javalobby/icons/16x16/DeleteFolder.gif";
			mkdirImage = "images/org/javalobby/icons/16x16/NewFolder.gif";
			refreshImage = "images/org/javalobby/icons/16x16/Undo.gif";
			refreshImage2 = "images/org/javalobby/icons/16x16/Undo.gif";
			cdImage = "images/org/javalobby/icons/16x16/Open.gif";
			cmdImage = "images/org/javalobby/icons/16x16/ExecuteProject.gif";
			downloadImage = "images/org/javalobby/icons/16x16/Left.gif";
			uploadImage = "images/org/javalobby/icons/16x16/Right.gif";
			queueImage = "images/org/javalobby/icons/16x16/Left.gif";
			fileImage = "images/org/javalobby/icons/16x16/Document.gif";
			dirImage = "images/org/javalobby/icons/16x16/Folder.gif";
			codeFileImage = "images/org/javalobby/icons/16x16/List.gif";
			textFileImage = "images/org/javalobby/icons/16x16/DocumentDraw.gif";
			execFileImage = "images/org/javalobby/icons/16x16/ExecuteProject.gif";
			audioFileImage = "images/org/javalobby/icons/16x16/cd.gif";
			videoFileImage = "images/org/javalobby/icons/16x16/CameraFlash.gif";
			htmlFileImage = "images/org/javalobby/icons/16x16/World2.gif";
			zipFileImage = "images/org/javalobby/icons/16x16/DataStore.gif";
			imageFileImage = "images/org/javalobby/icons/16x16/Camera.gif";
			presentationFileImage = "images/org/javalobby/icons/16x16/DocumentDiagram.gif";
			spreadsheetFileImage = "images/org/javalobby/icons/16x16/DatePicker.gif";
			bookFileImage = "images/org/javalobby/icons/16x16/Book.gif";
			copyImage = "images/org/javalobby/icons/16x16/Copy.gif";
			openImage = "images/org/javalobby/icons/16x16/World2.gif";
			sftpImage = "images/org/javalobby/icons/16x16/NewEnvelope.gif";
			nfsImage = "images/org/javalobby/icons/16x16/TrafficGreen.gif";
			webdavImage = "images/org/javalobby/icons/16x16/DataStore.gif";
			linkImage = "images/org/javalobby/icons/16x16/Right.gif";
			typeImage = "images/org/javalobby/icons/20x20/Type.gif";
			clearImage = "images/org/javalobby/icons/16x16/Undo.gif";
			resumeImage = "images/org/javalobby/icons/16x16/GreenCircle.gif";
			pauseImage = "images/org/javalobby/icons/16x16/RedCircle.gif";
			clearImage2 = "images/org/javalobby/icons/16x16/Undo.gif";
			resumeImage2 = "images/org/javalobby/icons/16x16/GreenCircle.gif";
			pauseImage2 = "images/org/javalobby/icons/16x16/RedCircle.gif";
			saveImage = "images/org/javalobby/icons/16x16/Save.gif";
			cdUpImage = "images/org/javalobby/icons/16x16/Exit.gif";
			nextRSSImage = "images/org/javalobby/icons/16x16/Forward.gif";
			helpImage = "images/current/help.png";
		} else {
			iconImage = "images/current/server_add.png";
			hostImage = "images/current/server_add.png";
			closeImage = "images/current/cancel.png";
			infoImage = "images/current/information.png";
			listImage = "images/current/page_white_text.png";

			rmdirImage = "images/current/folder_delete.png";
			mkdirImage = "images/current/folder_add.png";
			refreshImage = "images/current/control_repeat.png";
			refreshImage2 = "images/current/control_repeat_blue.png";
			cdImage = "images/current/folder_explore.png";
			cmdImage = "images/current/application_xp_terminal.png";
			downloadImage = "images/graphicsfuel/arrow-left.png";
			uploadImage = "images/graphicsfuel/arrow-right.png";
			queueImage = "images/current/arrow_left.png";
			fileImage = "images/current/page_white.png";
			dirImage = "images/current/folder.png";
			codeFileImage = "images/current/page_white_wrench.png";
			textFileImage = "images/current/page_white_text.png";
			execFileImage = "images/current/cog.png";
			audioFileImage = "images/current/ipod.png";
			videoFileImage = "images/current/camera.png";
			htmlFileImage = "images/current/html.png";
			zipFileImage = "images/current/page_white_zip.png";
			imageFileImage = "images/current/image.png";
			presentationFileImage = "images/current/page_white_powerpoint.png";
			spreadsheetFileImage = "images/current/page_excel.png";
			bookFileImage = "images/current/book.png";
			copyImage = "images/current/disk_multiple.png";
			openImage = "images/current/drive_web.png";
			sftpImage = "images/current/drive_link.png";
			nfsImage = "images/current/drive_network.png";
			webdavImage = "images/current/house_go.png";
			linkImage = "images/current/arrow_right.png";
			typeImage = "images/current/email_go.png";
			deleteImage = "images/current/control_stop.png";
			deleteImage2 = "images/current/control_stop_blue.png";
			clearImage = "images/current/control_eject.png";
			clearImage2 = "images/current/control_eject_blue.png";
			resumeImage = "images/current/control_play.png";
			resumeImage2 = "images/current/control_play_blue.png";
			pauseImage = "images/current/control_pause.png";
			pauseImage2 = "images/current/control_pause_blue.png";
			saveImage = "images/current/disk.png";
			cdUpImage = "images/current/arrow_up.png";
			nextRSSImage = "images/current/rss_go.png";
			helpImage = "images/current/help.png";
		}

	}

	public static Object setProperty(String key, String value) {
		return p.setProperty(key, value);
	}

	public static String getProperty(String key) {
		return p.getProperty(key);
	}

	public static Object setProperty(String key, int value) {
		return p.setProperty(key, Integer.toString(value));
	}

	public static Object setProperty(String key, boolean value) {
		String val = "false";

		if (value) {
			val = "true";
		}

		return p.setProperty(key, val);
	}

	public static void save() {
		try {
			new File(System.getProperty("user.home") + File.separator + ".jftp").mkdir();
			p.store(new FileOutputStream(propertyFilename), "jftp.properties");
		} catch (Exception e) {
			System.out.println("Cannot save properties...");

			//e.printStackTrace();
		}
	}

	public static boolean getHideLocalDotNames() {
		String what = p.getProperty("jftp.hideHiddenDotNames", "false");

		return !what.trim().equals("false");
	}

	public static int getMaxConnections() {
		return maxConnections;
	}

	public static String getSocksProxyHost() {
		String what = p.getProperty("jftp.socksProxyHost", "");

		return what;
	}

	public static String getSocksProxyPort() {
		String what = p.getProperty("jftp.socksProxyPort", "");

		return what;
	}

	public static boolean getUseBackground() {
		String what = p.getProperty("jftp.useBackground", "true");

		return !what.trim().equals("false");
	}

	public static boolean getEnableSshKeys() {
		String what = p.getProperty("jftp.useSshKeyVerification", "false");

		return !what.trim().equals("false");
	}

	public static boolean getEnableResuming() {
		String what = p.getProperty("jftp.enableResuming", "true");

		return !what.trim().equals("false");
	}

	public static boolean getEnableDebug() {
		String what = p.getProperty("jftp.enableDebug", defaultEnableDebug);

		return !what.trim().equals("false");
	}

	public static boolean getDisableLog() {
		String what = p.getProperty("jftp.disableLog", "false");

		return !what.trim().equals("false");
	}

	public static boolean getEnableStatusAnimation() {
		String what = p.getProperty("jftp.gui.enableStatusAnimation", "false");

		return !what.trim().equals("false");
	}

	public static boolean getAskToDelete() {
		String what = p.getProperty("jftp.gui.askToDelete", "true");

		return !what.trim().equals("false");
	}

	public static String getLookAndFeel() {
		return p.getProperty("jftp.gui.look", null);
	}

	public static boolean getUseNewIcons() {
		String what = p.getProperty("jftp.gui.look.newIcons", "true");

		return !what.trim().equals("false");
	}

	public static boolean getEnableMultiThreading() {
		String what = p.getProperty("jftp.enableMultiThreading", enableMultiThreading);

		return !what.trim().equals("false");
	}

	public static boolean getEnableSmbMultiThreading() {
		String what = p.getProperty("jftp.enableSmbMultiThreading", enableSmbMultiThreading);

		return !what.trim().equals("false");
	}

	public static boolean getEnableSftpMultiThreading() {
		String what = p.getProperty("jftp.enableSftpMultiThreading", enableSftpMultiThreading);

		return !what.trim().equals("false");
	}

	public static boolean getNoUploadMultiThreading() {
		String what = p.getProperty("jftp.noUploadMultiThreading", noUploadMultiThreading);

		return !what.trim().equals("false");
	}

	public static boolean getFtpPasvMode() {
		String what = p.getProperty("jftp.ftpPasvMode", defaultFtpPasvMode);

		return !what.trim().equals("false");
	}

	public static boolean getUseDefaultDir() {
		String what = p.getProperty("jftp.useDefaultDir", "true");

		return !what.trim().equals("false");
	}

	public static boolean getEnableRSS() {
		String what = p.getProperty("jftp.enableRSS", "false");

		return !what.trim().equals("false");
	}

	public static String getRSSFeed() {
		String what = p.getProperty("jftp.customRSSFeed", "http://slashdot.org/rss/slashdot.rss");

		return what;
	}

	public static java.awt.Dimension getWindowSize() {
		int width = Integer.parseInt(p.getProperty("jftp.window.width", defaultWidth));
		int height = Integer.parseInt(p.getProperty("jftp.window.height", defaultHeight));

		if (100 > width || 100 > height) {
			width = Integer.parseInt(defaultWidth);
			height = Integer.parseInt(defaultHeight);
		}

		return new java.awt.Dimension(width, height);
	}

	public static java.awt.Point getWindowLocation() {
		int x = Integer.parseInt(p.getProperty("jftp.window.x", defaultX));
		int y = Integer.parseInt(p.getProperty("jftp.window.y", defaultY));

		if (0 > x || 0 > y) {
			x = 0;
			y = 0;
		}

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		if (x >= screenSize.height) {
			x = Integer.parseInt(defaultX);
		}
		if (y >= screenSize.height) {
			y = Integer.parseInt(defaultY);
		}

		return new java.awt.Point(x, y);
	}

	public static int getSocketTimeout() {
		return 3000;
	}

	public static boolean getStorePasswords() {
		String what = p.getProperty("jftp.security.storePasswords", storePasswords);

		return !what.trim().equals("false");
	}

}
