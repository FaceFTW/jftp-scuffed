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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.sf.jftp.net;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * All control transactions are made here.
 * See doc/FtpDownload.java for examples and note
 * that this class is event-driven and not Exception-based!
 * You might want to take a look at ConnectionListener, too.
 *
 * @author David Hansmann, hansmann.d@debitel.net
 */
public class FtpConnection implements BasicConnection, FtpConstants {
	public static final String ASCII = "A";
	public static final String BINARY = "I";
	public static final String EBCDIC = "E";
	public static final String L8 = "L 8";
	public static final String STREAM = "S";
	public static final String BLOCKED = "B";
	public static final String COMPRESSED = "C";
	private static final boolean TESTMODE = false;
	// everything starting with this will be treated
	// as a negative response
	private static final String NEGATIVE = "5";
	private static final String NEGATIVE2 = "4";
	// everything starting with this will be treated
	// as a positive response
	private static final String POSITIVE = "2";
	// for resuming
	private static final String PROCEED = "3"; //"350";
	private static final char MORE_LINES_APPENDED = '-';
	//*** 1.44: if LIST is not set, then load it from advanced settings
	//***       And if the file for it is not found, then create it
	//***       with LIST -laL as the default
	public static String LIST_DEFAULT = "LIST -laL";
	public static String LIST = "default";

	private static boolean useStream = true;
	private static boolean useBlocked = true;
	private static boolean useCompressed = true;

	// active ftp ports
	private static int porta = 5; // 5 * 256 + 1 = 1281
	private static int portb = 1;
	private final String[] loginAck = new String[]{net.sf.jftp.net.FtpConstants.FTP331_USER_OK_NEED_PASSWORD, net.sf.jftp.net.FtpConstants.FTP230_LOGGED_IN};
	private final List<FtpTransfer> transfers = new ArrayList<>();
	/**
	 * Used to determine the type of transfer.
	 */
	public boolean hasUploaded = false;
	public boolean work = true;
	/**
	 * May contain date listing
	 */
	public Vector<Date> dateVector = new Vector<>();
	public final Vector<String> currentListing = new Vector<>();
	public final Vector<String> currentFiles = new Vector<>();
	public final Vector<String> currentSizes = new Vector<>();
	public final Vector<String> currentPerms = new Vector<>();
	private boolean ok = true;
	private String pwd = "";
	private String initCWD = net.sf.jftp.config.Settings.defaultDir;
	private String osType = "";
	private String dataType;
	private boolean modeStreamSet = false;
	private DataConnection dcon = null;
	private Vector<ConnectionListener> listeners = new Vector<>();
	private ConnectionHandler handler = new ConnectionHandler();
	private FtpKeepAliveThread keepAliveThread = null;
	// directory downloaded to
	private String localPath = null;
	/**
	 * the host
	 */
	private String host = "";
	/**
	 * the user
	 */
	private String username;
	/**
	 * the password
	 */
	private String password;
	/**
	 * the port
	 */
	private int port = 21;
	/**
	 * the InputStream of the control connection
	 */
	private BufferedReader in;
	/**
	 * the OutputStream of the control connection
	 */
	private JConnection jcon;
	/**
	 * we are disconnected
	 */
	private boolean connected = false;
	private boolean shortProgress = false;
	private boolean isDirUpload = false;
	private String baseFile;
	private int fileCount;
	private String typeNow = "";
	private String crlf = null;

	/**
	 * Create an instance with a given host
	 *
	 * @param host The host to connect to
	 */
	public FtpConnection(final String host) {
		this.host = host;

		final File f = new File(".");
		this.setLocalPath(f.getAbsolutePath());
	}

	/**
	 * Create an instance with a given host, port and initial remote working directory
	 *
	 * @param host    The host to connect to
	 * @param port    The port used for the control connection, usually 21
	 * @param initCWD The initial remote working directory
	 */
	public FtpConnection(final String host, final int port, final String initCWD) {
		this.host = host;
		this.port = port;
		this.initCWD = initCWD;

		final File f = new File(".");
		this.setLocalPath(f.getAbsolutePath());
	}

	/**
	 * Connect to the server an log in.
	 * <p>
	 * Does also fire directory update and connection initialized event if successful or
	 * connection failed event if failed.
	 *
	 * @param initCWD The initial remote working directory
	 * @param crlfx   \n, \r, \r\n, CR, LF or CRLF - line break style of the server
	 */
	public FtpConnection(final String host, final int port, final String initCWD, String crlfx) {
		this.host = host;
		this.port = port;
		this.initCWD = initCWD;

		if (crlfx != null) {
			if (crlfx.equals("\n") || crlfx.equals("\r") || crlfx.equals("\r\n")) this.crlf = crlfx;
			crlfx = crlfx.trim().toUpperCase();
			switch (crlfx) {
				case "LF":
					this.crlf = "\n";
					break;
				case "CR":
					this.crlf = "\r";
					break;
				case "CRLF":
					this.crlf = "\r\n";
					break;
			}
		}
		//Log.out("\n\nCRLF:---"+crlf+"---\n\n");

		final File f = new File(".");
		this.setLocalPath(f.getAbsolutePath());
	}

	/**
	 * Connect to the server an log in.
	 * <p>
	 * Does also fire directory update and connection initialized event if successful or
	 * connection failed event if failed.
	 *
	 * @param username The username
	 * @param password The password
	 * @return WRONG_LOGIN_DATA, OFFLINE, GENERIC_FAILED or LOGIN_OK status code
	 */
	public int login(final String username, final String password) {
		this.username = username;
		this.password = password;

		int status = net.sf.jftp.net.FtpConstants.LOGIN_OK;

		final boolean msg = true;
		if (msg) {
			net.sf.jftp.system.logging.Log.debug("Connecting to " + host);
		}

		jcon = new JConnection(host, port);

		if (jcon.isThere()) {
			in = jcon.getReader();

			if (this.getLine(net.sf.jftp.net.FtpConnection.POSITIVE) == null)//FTP220_SERVICE_READY) == null)
			{
				ok = false;
				net.sf.jftp.system.logging.Log.debug("Server closed Connection, maybe too many users...");
				status = net.sf.jftp.net.FtpConstants.OFFLINE;
			}

			if (msg) {
				net.sf.jftp.system.logging.Log.debug("Connection established...");
			}

			jcon.send(net.sf.jftp.net.FtpConstants.USER + " " + username);

			if (!this.getLine(loginAck).startsWith(net.sf.jftp.net.FtpConnection.POSITIVE))//FTP230_LOGGED_IN))
			{
				jcon.send(net.sf.jftp.net.FtpConstants.PASS + " " + password);

				if (this.success(net.sf.jftp.net.FtpConnection.POSITIVE))//FTP230_LOGGED_IN))
				{
					if (msg) {
						net.sf.jftp.system.logging.Log.debug("Logged in...");
					}
				} else {
					net.sf.jftp.system.logging.Log.debug("Wrong password!");
					ok = false;
					status = net.sf.jftp.net.FtpConstants.WRONG_LOGIN_DATA;
				}
			}

			// end if(jcon)...
		} else {
			if (msg) {
				net.sf.jftp.system.logging.Log.debug("FTP not available!");
				ok = false;
				status = net.sf.jftp.net.FtpConstants.GENERIC_FAILED;
			}
		}

		if (ok) {
			connected = true;
			this.system();

			this.binary();

			if (initCWD.trim().equals(net.sf.jftp.config.Settings.defaultDir) || !this.chdirNoRefresh(initCWD)) {

				this.updatePWD();
			}

			if (net.sf.jftp.config.Settings.ftpKeepAlive) {
				keepAliveThread = new FtpKeepAliveThread(this);
			}

			String[] advSettings = new String[6];

			if (this.getOsType().contains("OS/2")) {
				net.sf.jftp.net.FtpConnection.LIST_DEFAULT = "LIST";
			}

			if (net.sf.jftp.net.FtpConnection.LIST.equals("default")) {
				advSettings = net.sf.jftp.config.LoadSet.loadSet(net.sf.jftp.config.Settings.adv_settings);

				if (advSettings == null) {
					net.sf.jftp.net.FtpConnection.LIST = net.sf.jftp.net.FtpConnection.LIST_DEFAULT;

					new net.sf.jftp.config.SaveSet(net.sf.jftp.config.Settings.adv_settings, net.sf.jftp.net.FtpConnection.LIST);
				} else {
					net.sf.jftp.net.FtpConnection.LIST = advSettings[0];

					if (net.sf.jftp.net.FtpConnection.LIST == null) {
						net.sf.jftp.net.FtpConnection.LIST = net.sf.jftp.net.FtpConnection.LIST_DEFAULT;
					}
				}
			}

			if (this.getOsType().contains("MVS")) {
				net.sf.jftp.net.FtpConnection.LIST = "LIST";
			}

			//***
			this.fireDirectoryUpdate(this);
			this.fireConnectionInitialized(this);
		} else {
			this.fireConnectionFailed(this, Integer.toString(status));
		}

		return status;
	}

	public FtpKeepAliveThread getKeepAliveThread() {
		return keepAliveThread;
	}

	/**
	 * Sort the filesizes an return an array.
	 * <p>
	 * The Array should be in sync with the other sort*-Methods
	 *
	 * @return An String-array of sizes containing the size or -1 if failed or 0 for directories
	 */
	public String[] sortSize() {
		try {
			currentSizes.removeAllElements();

			for (String tmp : currentListing) {
				// ------------- VMS override -------------------
				if (this.getOsType().contains("VMS")) {
					if (!tmp.contains(";")) {
						continue;
					}

					currentSizes.add("-1");

					continue;
				}

				// ------------- MVS override -------------------
				if (this.getOsType().contains("MVS")) {
					if (tmp.startsWith("Volume") || (!tmp.contains(" "))) {
						continue;
					}

					currentSizes.add("-1");

					continue;
				}

				// ------------------------------------------------
				else if (this.getOsType().contains("WINDOW")) {
					final java.util.StringTokenizer to = new java.util.StringTokenizer(tmp, " ", false);

					if (to.countTokens() >= 4) {
						to.nextToken();
						to.nextToken();

						final String size = to.nextToken();

						if (size.equals("<DIR>")) {
							currentSizes.add("0");
						} else {
							try {
								Long.parseLong(size);
								currentSizes.add(size);
							} catch (final Exception ex) {
								currentSizes.add("-1");
								net.sf.jftp.system.logging.Log.out("WARNING: filesize can not be determined - value is " + size);
							}
						}
					}
				}

				// ------------------------------------------------
				else if (this.getOsType().contains("OS/2")) {
					final java.util.StringTokenizer to = new java.util.StringTokenizer(tmp, " ", false);
					tmp = this.giveSize(to, 0);
					net.sf.jftp.system.logging.Log.out("OS/2 parser (size): " + tmp);
					currentSizes.add(tmp);
				} else {
					//Log.out("\n\nparser\n\n");
					final java.util.StringTokenizer to = new java.util.StringTokenizer(tmp, " ", false);

					if (to.countTokens() >= 8) // unix
					{
						tmp = this.giveSize(to, 4);
						currentSizes.add(tmp);

						//Log.out(tmp);
					} else if (to.countTokens() == 7) // unix, too
					{
						net.sf.jftp.system.logging.Log.out("WARNING: 7 token backup size parser activated!");
						tmp = this.giveSize(to, 4);
						currentSizes.add(tmp);
					} else {
						net.sf.jftp.system.logging.Log.debug("cannot parse line: " + tmp);
					}
				}
			}
		} catch (final Exception ex) {
			net.sf.jftp.system.logging.Log.debug(ex + " @FtpConnection::sortSize#1");
			return new String[0];
		}

		return this.toArray(currentSizes);
	}

	private String[] toArray(final Vector<String> in) {
		final String[] ret = new String[in.size()];

		for (int i = 0; i < in.size(); i++) {
			ret[i] = in.get(i);
		}

		return ret;
	}

	/**
	 * Sort the permissions an return an array.
	 * <p>
	 * The Array should be in sync with the other sort*-Methods
	 *
	 * @return An int-array of sizes containing W, R or DENIED for each file
	 */
	public int[] getPermissions() {
		try {
			currentPerms.removeAllElements();

			for (String tmp : currentListing) {
				// ------------- VMS override -------------------
				if (this.getOsType().contains("VMS")) {
					if (!tmp.contains(";")) {
						continue;
					}

					currentPerms.add("r");

					continue;
				}

				// ------------- MVS override -------------------
				if (this.getOsType().contains("MVS")) {
					if (tmp.startsWith("Volume")) {
						continue;
					}

					currentPerms.add("r");

					continue;
				}

				// ------------------------------------------------

				final java.util.StringTokenizer to = new java.util.StringTokenizer(tmp.trim(), " ", false);

				if (!(to.countTokens() > 3) || (tmp.startsWith("/") && (tmp.indexOf("denied") > 0))) {
					continue;
				}

				tmp = to.nextToken();

				if (tmp.length() != 10) {
					//System.out.println(tmp + " - e");
					return null; // exotic bug, hardlinks are not found or something ("/bin/ls: dir: no such file or directy" in ls output) - we have no permissions then
				}

				final char ur = tmp.charAt(1);
				final char uw = tmp.charAt(2);
				final char ar = tmp.charAt(7);
				final char aw = tmp.charAt(8);

				to.nextToken();

				final String user = to.nextToken();

				//System.out.println(""+ur+":"+ar+":"+tmp);
				if (aw == 'w') {
					currentPerms.add("w");
				} else if (user.equals(username) && (uw == 'w')) {
					currentPerms.add("w");
				} else if (ar == 'r') {
					currentPerms.add("r");
				} else if (user.equals(username) && (ur == 'r')) {
					currentPerms.add("r");
				} else {
					//System.out.println(ur+":"+ar+":"+user+":"+username+":");
					currentPerms.add("n");
				}
			}
		} catch (final Exception ex) {
			net.sf.jftp.system.logging.Log.debug(ex + " @FtpConnection::getPermissions#1");
		}

		final int[] ret = new int[currentPerms.size()];

		for (int i = 0; i < currentPerms.size(); i++) {
			final String fx = currentPerms.get(i);

			if (fx.equals("w")) {
				ret[i] = net.sf.jftp.net.FtpConstants.W;
			} else if (fx.equals("n")) {
				ret[i] = net.sf.jftp.net.FtpConstants.DENIED;
			} else {
				ret[i] = net.sf.jftp.net.FtpConstants.R;
			}
			//System.out.println(ret[i]);
		}

		return ret;
	}

	/**
	 * Sort the filenames of the current working dir an return an array.
	 * <p>
	 * The Array should be in sync with the other sort*-Methods
	 *
	 * @return An String-array of sizes containing the name of the file (symlinks are resolved)
	 */
	public String[] sortLs() {
		try {
			boolean newUnixDateStyle = false;
			dateVector = new Vector<>();
			currentFiles.removeAllElements();

			for (String tmp : currentListing) {
				// ------------------- VMS override --------------------
				if (this.getOsType().contains("VMS")) {
					final int x = tmp.indexOf(";");

					if (x < 0) {
						continue;
					}

					tmp = tmp.substring(0, x);

					if (tmp.endsWith("DIR")) {
						currentFiles.add(tmp.substring(0, tmp.lastIndexOf(".")) + "/");
					} else {
						currentFiles.add(tmp);
					}

					//Log.out("listing - (vms parser): " + tmp);

					continue;
				} else if (this.getOsType().contains("OS/2")) {
					java.util.StringTokenizer to2 = new java.util.StringTokenizer(tmp, " ", true);

					if (this.giveFile(to2, 2).contains("DIR")) {
						to2 = new java.util.StringTokenizer(tmp, " ", true);
						tmp = this.giveFile(to2, 5);
						tmp = tmp + "/";
					} else {
						to2 = new java.util.StringTokenizer(tmp, " ", true);

						if (this.giveFile(to2, 1).contains("DIR")) {
							//Log.out("OS/2 parser (DIRFIX): " + tmp);
							to2 = new java.util.StringTokenizer(tmp, " ", true);
							tmp = this.giveFile(to2, 4);
							tmp = tmp + "/";
						} else {
							to2 = new java.util.StringTokenizer(tmp, " ", true);
							tmp = this.giveFile(to2, 4);
						}
					}

					net.sf.jftp.system.logging.Log.out("OS/2 parser: " + tmp);
					currentFiles.add(tmp);

					continue;
				}

				// -------------------------------------------------------
				// ------------------- MVS override --------------------
				if (this.getOsType().contains("MVS")) {
					if (tmp.startsWith("Volume") || (!tmp.contains(" "))) {
						net.sf.jftp.system.logging.Log.out("->" + tmp);

						continue;
					}

					final String f = tmp.substring(tmp.lastIndexOf(" ")).trim();
					final String isDir = tmp.substring(tmp.lastIndexOf(" ") - 5, tmp.lastIndexOf(" "));

					if (isDir.contains("PO")) {
						currentFiles.add(f + "/");
					} else {
						currentFiles.add(f);
					}

					net.sf.jftp.system.logging.Log.out("listing - (mvs parser): " + tmp + " -> " + f);
					continue;
				} else if (this.getOsType().contains("OS/2")) {
					java.util.StringTokenizer to2 = new java.util.StringTokenizer(tmp, " ", true);

					if (this.giveFile(to2, 2).contains("DIR")) {
						to2 = new java.util.StringTokenizer(tmp, " ", true);
						tmp = this.giveFile(to2, 5);
						tmp = tmp + "/";
					} else {
						to2 = new java.util.StringTokenizer(tmp, " ", true);

						if (this.giveFile(to2, 1).contains("DIR")) {
							//Log.out("OS/2 parser (DIRFIX): " + tmp);
							to2 = new java.util.StringTokenizer(tmp, " ", true);
							tmp = this.giveFile(to2, 4);
							tmp = tmp + "/";
						} else {
							to2 = new java.util.StringTokenizer(tmp, " ", true);
							tmp = this.giveFile(to2, 4);
						}
					}

					net.sf.jftp.system.logging.Log.out("OS/2 parser: " + tmp);
					currentFiles.add(tmp);

					continue;
				}

				// -------------------------------------------------------
				// ------------------- Unix, Windows and others -----
				boolean isDir = false;
				boolean isLink = false;

				if (tmp.startsWith("d") || (tmp.contains("<DIR>"))) {
					isDir = true;
				}

				if (tmp.startsWith("l")) {
					isLink = true;
				}

				if (tmp.startsWith("/") && (tmp.indexOf("denied") > 0)) {
					continue;
				}

				final java.util.StringTokenizer to = new java.util.StringTokenizer(tmp, " ", false);
				final java.util.StringTokenizer to2 = new java.util.StringTokenizer(tmp, " ", true);

				final int tokens = to.countTokens();

				//Log.out("listing: tokens: " + tokens + " - " + tmp);
				boolean set = false;

				//-----preparsing-----
				int lcount = 0;

				if (!newUnixDateStyle) {
					try {
						final java.util.StringTokenizer tx = new java.util.StringTokenizer(tmp, " ", false);
						tx.nextToken();
						tx.nextToken();
						tx.nextToken();
						tx.nextToken();
						tx.nextToken();

						final String date = tx.nextToken();
						final int x = date.indexOf("-");
						final int y = date.lastIndexOf("-");

						if (y > x) {
							net.sf.jftp.system.logging.Log.debug("Using new Unix date parser");
							newUnixDateStyle = true;
						}
					} catch (final Exception ex) {
						net.sf.jftp.system.logging.Log.out("could not preparse line: " + tmp);
						lcount++;

					}
				}

				if (newUnixDateStyle) // unix, too
				{
					set = true;

					try {
						final java.util.StringTokenizer to3 = new java.util.StringTokenizer(tmp, " ", true);
						final String date = this.giveFile(to3, 5);
						String date2 = date.substring(date.indexOf("-") + 1);
						date2 = date2.substring(date.indexOf("-") + 2);

						final String y = date.substring(0, date.indexOf("-"));
						final String m = date.substring(date.indexOf("-") + 1, date.indexOf("-") + 3);
						final String m1 = date.substring(date.indexOf("-") + 1);
						final String day = m1.substring(m1.indexOf("-") + 1, m1.indexOf("-") + 3);
						final String h = date2.substring(0, date2.indexOf(":"));
						final String min = date2.substring(date2.indexOf(":") + 1, date2.indexOf(":") + 3);

						//Log.out("day:"+day+"year:"+y+"mon:"+m+"hour:"+h+"m:"+min);
						final java.util.Calendar c = new java.util.GregorianCalendar();
						c.set(java.util.Calendar.DAY_OF_MONTH, Integer.parseInt(day));
						c.set(java.util.Calendar.YEAR, Integer.parseInt(y));
						c.set(java.util.Calendar.MONTH, Integer.parseInt(m) - 1);
						c.set(java.util.Calendar.HOUR, Integer.parseInt(h));
						c.set(java.util.Calendar.MINUTE, Integer.parseInt(min));

						dateVector.add(c.getTime());

						//Log.out("+++ date: \"" + d.toString() + "\"");
					} catch (final Exception ex) {
						ex.printStackTrace();

						//set = false;
					}

					// -----------------------------
					tmp = this.giveFile(to2, 7);

					if (lcount > 1) {
						dateVector = new java.util.Vector<>();
					}
				} else if (tokens > 8) // unix
				{
					set = true;
					tmp = this.giveFile(to2, 8);

					//Log.out("listing - (unix parser, token 8): " + tmp);
				} else if (tokens > 3) // windows
				{
					set = true;
					tmp = this.giveFile(to2, 3);

					if (tmp.startsWith("<error>")) {
						tmp = this.giveFile(to2, 4);

						//Log.out("listing - (windows parser, token 4): " + tmp);
					} else {
						//Log.out("listing - (windows parser, token 3): " + tmp);
					}
				}

				if (tmp.startsWith("<error>") || !set) {
					if (tokens < 3) {
						continue;
					}

					tmp = this.giveFile(to2, tokens);
					net.sf.jftp.system.logging.Log.out("listing - WARNING: listing style unknown, using last token as filename!");
					net.sf.jftp.system.logging.Log.out("listing - this may work but may also fail, filesizes and permissions will probably fail, too...");
					net.sf.jftp.system.logging.Log.out("listing - please send us a feature request with the serveraddress to let us support this listing style :)");
					net.sf.jftp.system.logging.Log.out("listing - (backup parser, token " + tokens + " ): " + tmp);
				}

				if (isDir) {
					tmp = tmp + "/";
				} else if (isLink) {
					tmp = tmp + "###";
				}

				currentFiles.add(tmp);
				// -------------------------------------------------------------
			}

			final String[] files = this.toArray(currentFiles);

			for (int i = 0; i < files.length; i++) {
				files[i] = this.parseSymlink(files[i]);

			}

			return files;
		} catch (final Exception ex) {
			net.sf.jftp.system.logging.Log.debug(ex + " @FtpConnection::sortLs");
			ex.printStackTrace();
		}

		return new String[0];
	}

	private String giveFile(final StringTokenizer to, final int lev) {

		for (int i = 0; i < lev; i++) {
			if (to.hasMoreTokens()) {
				final String t = to.nextToken();

				if (t.equals(" ") || t.equals("\t")) {

					i--;
				}
			}
		}

		StringBuilder tmp = new StringBuilder("<error>");

		while (tmp.toString().equals(" ") || tmp.toString().equals("\t") || tmp.toString().equals("<error>")) {
			if (to.hasMoreTokens()) {
				tmp = new StringBuilder(to.nextToken());
			} else {
				break;
			}
		}

		while (to.hasMoreTokens()) {
			tmp.append(to.nextToken());
		}

		//Log.out(">>> "+tmp);
		return tmp.toString();
	}

	/**
	 * get a filesize
	 */
	private String giveSize(final StringTokenizer to, final int lev) {
		for (int i = 0; i < lev; i++) {
			if (to.hasMoreTokens()) {
				if (to.nextToken().equals(" ")) {
					i--;
				}
			}
		}

		while (to.hasMoreTokens()) {
			final String tmp = to.nextToken();

			if (!tmp.equals(" ")) {
				try {
					Integer.parseInt(tmp);
				} catch (final NumberFormatException ex) {
					return "-1";
				}

				return tmp;
			}
		}

		return "-2";
	}

	/**
	 * Try to determine the os-type.
	 * (Used internally to check how to parse the ls-output)
	 *
	 * @return A Part of the SYST comman server response
	 */
	public String getOsType() {
		if (net.sf.jftp.net.FtpConnection.TESTMODE) {
			return "MVS";
		} else {
			return osType;
		}
	}

	private void setOsType(final String os) {
		osType = os.toUpperCase();
	}

	private int getPasvPort(final String str) {
		int start = str.lastIndexOf(",");
		final StringBuilder lo = new StringBuilder();
		start++;

		while (start < str.length()) {
			final char c = str.charAt(start);

			if (!Character.isDigit(c)) {
				break;
			}

			lo.append(c);
			start++;
		}

		System.out.println("\n\n\n" + str);

		final StringBuilder hi = new StringBuilder();
		start = str.lastIndexOf(",");
		start--;

		while (true) {
			final char c = str.charAt(start);

			if (!Character.isDigit(c)) {
				break;
			}

			hi.insert(0, c);
			start--;
		}

		//System.out.print(hi+":"+lo+" - ");
		return ((Integer.parseInt(hi.toString()) * 256) + Integer.parseInt(lo.toString()));
	}

	private int getActivePort() {
		return (net.sf.jftp.net.FtpConnection.porta * 256) + net.sf.jftp.net.FtpConnection.portb;
	}

	/**
	 * parse a symlink
	 */
	private String parseSymlink(String file) {
		if (file.contains("->")) {
			//System.out.print(file+" :-> symlink converted to:");
			file = file.substring(0, file.indexOf("->")).trim();
			file = file + "###";

			//System.out.println(file);
		}

		return file;
	}

	/**
	 * parse a symlink and cut the back
	 */
	private String parseSymlinkBack(String file) {
		if (file.contains("->")) {
			//System.out.print(file+" :-> symlink converted to:");
			file = file.substring(file.indexOf("->") + 2).trim();

			//System.out.println(file);
		}

		return file;
	}

	/**
	 * test for symlink
	 */
	private boolean isSymlink(final String file) {
		return file.contains("->");
	}

	/**
	 * Download a file or directory.
	 * Uses multithreading if enabled and does not block.
	 *
	 * @param file The file to download
	 * @return An int-statuscode, see constants defined in this class for details
	 */
	public int handleDownload(final String file) {
		if (net.sf.jftp.config.Settings.getEnableMultiThreading()) {
			net.sf.jftp.system.logging.Log.out("spawning new thread for this download.");

			final FtpTransfer t = new FtpTransfer(host, port, localPath, pwd, file, username, password, Transfer.DOWNLOAD, handler, listeners, crlf);
			transfers.add(t);

			return net.sf.jftp.net.FtpConstants.NEW_TRANSFER_SPAWNED;
		} else {
			net.sf.jftp.system.logging.Log.out("multithreading is completely disabled.");

			return this.download(file);
		}
	}

	/**
	 * Download a file or directory, block until finished.
	 *
	 * @param file The file to download
	 * @return An int returncode
	 */
	public int download(final String file) {
		//Log.out("ftp download started:" + this);

		final int stat;

		if (file.endsWith("/")) {
			shortProgress = true;
			fileCount = 0;
			baseFile = file;
			dataType = DataConnection.GETDIR;

			stat = this.downloadDir(file);

			//pause(100);
			this.fireActionFinished(this);
			this.fireProgressUpdate(baseFile, DataConnection.DFINISHED + ":" + fileCount, -1);
			shortProgress = false;
		} else {
			dataType = DataConnection.GET;

			stat = this.rawDownload(file);

			if (net.sf.jftp.config.Settings.enableFtpDelays) {
				try {
					Thread.sleep(100);
				} catch (final Exception ex) {
				}
			}

			this.fireActionFinished(this);
		}

		if (net.sf.jftp.config.Settings.enableFtpDelays) {
			try {
				Thread.sleep(400);
			} catch (final Exception ex) {
			}
		}

		return stat;
	}

	/**
	 * Get download InputStream.
	 *
	 * @param file The file to download
	 * @return An InputStream
	 */
	public InputStream getDownloadInputStream(String file) {
		net.sf.jftp.system.logging.Log.out("ftp stream download started:" + this);
		file = this.parse(file);

		try {
			int p = 0;
			dataType = DataConnection.GET;
			file = net.sf.jftp.system.StringUtils.getFile(file);

			final String path = localPath + file;

			//BufferedReader in = jcon.getReader();

			this.modeStream();
			p = this.negotiatePort();

			dcon = new DataConnection(this, p, host, path, dataType, false, true);

			while (!dcon.isThere()) {
				this.pause(10);
			}

			jcon.send(net.sf.jftp.net.FtpConstants.RETR + " " + file);

			return dcon.getInputStream();
		} catch (final Exception ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug(ex + " @FtpConnection::getDownloadInputStream");

			return null;
		}
	}

	private int rawDownload(String file) {
		file = this.parse(file);

		//String path = file;
		try {
			int p = 0;

			//if(isRelative(file)) path = getLocalPath() + file;
			file = net.sf.jftp.system.StringUtils.getFile(file);

			final String path = localPath + file;

			this.modeStream();

			//binary();
			p = this.negotiatePort();

			final File f = new File(path);

			//System.out.println("path: "+path);
			boolean resume = false;

			if (f.exists() && net.sf.jftp.config.Settings.enableResuming) {
				jcon.send(net.sf.jftp.net.FtpConstants.REST + " " + f.length());

				if (this.getLine(net.sf.jftp.net.FtpConnection.PROCEED) != null) {
					resume = true;
				}
			}

			dcon = new DataConnection(this, p, host, path, dataType, resume); //, new Updater());

			while (!dcon.isThere()) {
				this.pause(10);
			}

			jcon.send(net.sf.jftp.net.FtpConstants.RETR + " " + file);

			final String line = this.getLine(net.sf.jftp.net.FtpConnection.POSITIVE);
			net.sf.jftp.system.logging.Log.debug(line);

			// file has been created even if not downloaded, delete it
			if (line.startsWith(net.sf.jftp.net.FtpConnection.NEGATIVE)) {
				final File f2 = new File(path);

				if (f2.exists() && (f2.length() == 0)) {
					f2.delete();
				}

				return net.sf.jftp.net.FtpConstants.PERMISSION_DENIED;
			} else if (!line.startsWith(net.sf.jftp.net.FtpConnection.POSITIVE) && !line.startsWith(net.sf.jftp.net.FtpConnection.PROCEED)) {
				return net.sf.jftp.net.FtpConstants.TRANSFER_FAILED;
			}

			// we need to block since some ftp-servers do not want the
			// refresh command that dirpanel sends otherwise
			while (!dcon.finished) {
				this.pause(10);
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug(ex + " @FtpConnection::download");

			return net.sf.jftp.net.FtpConstants.TRANSFER_FAILED;
		}

		return net.sf.jftp.net.FtpConstants.TRANSFER_SUCCESSFUL;
	}

	private int downloadDir(String dir) {
		if (!dir.endsWith("/")) {
			dir = dir + "/";
		}

		if (net.sf.jftp.system.StringUtils.isRelative(dir)) {
			dir = pwd + dir;
		}

		final String path = localPath + net.sf.jftp.system.StringUtils.getDir(dir);

		//System.out.println("path: "+path);
		final String pwd = this.pwd + net.sf.jftp.system.StringUtils.getDir(dir);

		final String oldDir = localPath;
		final String oldPwd = this.pwd;

		final File f = new File(path);

		if (!f.exists()) {
			if (!f.mkdir()) {
				net.sf.jftp.system.logging.Log.debug("Can't create directory: " + dir);
			} else {
				net.sf.jftp.system.logging.Log.debug("Created directory...");
			}
		}

		this.setLocalPath(path);

		if (!this.chdirNoRefresh(pwd)) {
			return net.sf.jftp.net.FtpConstants.CHDIR_FAILED;
		}

		try {
			this.list();
		} catch (final IOException ex) {
			return net.sf.jftp.net.FtpConstants.PERMISSION_DENIED;
		}

		final String[] tmp = this.sortLs();

		this.setLocalPath(path);

		for (final String s : tmp) {
			if (s.endsWith("/")) {
				if (s.trim().equals("../") || s.trim().equals("./")) {
					net.sf.jftp.system.logging.Log.debug("Skipping " + s.trim());
				} else {
					if (!work) {
						return net.sf.jftp.net.FtpConstants.TRANSFER_STOPPED;
					}

					if (this.downloadDir(s) < 0) {
						// return TRANSFER_FAILED;
					}
				}
			} else {
				//System.out.println( "file: " + getLocalPath() + tmp[i] + "\n\n");
				if (!work) {
					return net.sf.jftp.net.FtpConstants.TRANSFER_STOPPED;
				}

				fileCount++;

				if (this.rawDownload(localPath + s) < 0) {
					// return TRANSFER_FAILED;
				}
			}
		}

		this.chdirNoRefresh(oldPwd);
		this.setLocalPath(oldDir);

		return net.sf.jftp.net.FtpConstants.TRANSFER_SUCCESSFUL;
	}

	/**
	 * Upload a file or directory.
	 * Uses multithreading if enabled and does not block.
	 *
	 * @param file The file to upload
	 * @return An int-statuscode, NEW_TRANSFER_SPAWNED,TRANSFER_FAILED or TRANSFER_SUCCESSFUL
	 */
	public int handleUpload(final String file) {
		return this.handleUpload(file, null);
	}

	/**
	 * Upload a file or directory.
	 * Uses multithreading if enabled and does not block.
	 *
	 * @param file     The file to upload
	 * @param realName The file to rename the uploaded file to
	 * @return An int-statuscode, NEW_TRANSFER_SPAWNED,TRANSFER_FAILED or TRANSFER_SUCCESSFUL
	 */
	public int handleUpload(final String file, final String realName) {
		if (net.sf.jftp.config.Settings.getEnableMultiThreading() && (!net.sf.jftp.config.Settings.getNoUploadMultiThreading())) {
			net.sf.jftp.system.logging.Log.out("spawning new thread for this upload.");

			final FtpTransfer t;

			if (realName != null) {
				t = new FtpTransfer(host, port, localPath, pwd, file, username, password, Transfer.UPLOAD, handler, listeners, realName, crlf);
			} else {
				t = new FtpTransfer(host, port, localPath, pwd, file, username, password, Transfer.UPLOAD, handler, listeners, crlf);
			}

			transfers.add(t);

			return net.sf.jftp.net.FtpConstants.NEW_TRANSFER_SPAWNED;
		} else {
			if (net.sf.jftp.config.Settings.getNoUploadMultiThreading()) {
				net.sf.jftp.system.logging.Log.out("upload multithreading is disabled.");
			} else {
				net.sf.jftp.system.logging.Log.out("multithreading is completely disabled.");
			}

			return (realName == null) ? this.upload(file) : this.upload(file, realName);
		}
	}

	/**
	 * Upload a file or directory, block until finished.
	 *
	 * @param file The file to upload
	 * @return An int returncode
	 */
	public int upload(final String file) {
		return this.upload(file, file);
	}

	/**
	 * Upload and a file or directory under a given name, block until finished.
	 * Note that setting realName does not affect directory transfers
	 *
	 * @param file     The file to upload
	 * @param realName The file to rename the uploaded file to
	 * @return An int responsecode
	 */
	public int upload(final String file, final String realName) {
		return this.upload(file, realName, null);
	}

	/**
	 * Upload from an InputStream, block until finished.
	 *
	 * @param file The file to upload
	 * @param in   InputStream to read from
	 * @return An int responsecode
	 */
	public int upload(final String file, final InputStream in) {
		return this.upload(file, file, in);
	}

	/**
	 * Upload and a file or directory under a given name, block until finished.
	 * Note that setting realName does not affect directory transfers
	 *
	 * @param file     The file to upload
	 * @param realName The file to rename the uploaded file to
	 * @param in       InputStream to read from
	 * @return An int responsecode
	 */
	public int upload(final String file, final String realName, final InputStream in) {
		hasUploaded = true;
		net.sf.jftp.system.logging.Log.out("ftp upload started: " + this);

		final int stat;

		if ((in == null) && new File(file).isDirectory()) {
			shortProgress = true;
			fileCount = 0;
			baseFile = file;
			dataType = DataConnection.PUTDIR;
			isDirUpload = true;

			stat = this.uploadDir(file);

			shortProgress = false;

			//System.out.println(fileCount + ":" + baseFile);
			this.fireProgressUpdate(baseFile, DataConnection.DFINISHED + ":" + fileCount, -1);

			this.fireActionFinished(this);
			this.fireDirectoryUpdate(this);
		} else {
			dataType = DataConnection.PUT;
			stat = this.rawUpload(file, realName, in);

			try {
				Thread.sleep(100);
			} catch (final Exception ex) {
			}

			this.fireActionFinished(this);
			this.fireDirectoryUpdate(this);
		}

		try {
			Thread.sleep(500);
		} catch (final Exception ex) {
		}

		return stat;
	}

	private int rawUpload(final String file) {
		return this.rawUpload(file, file);
	}

	private int rawUpload(final String file, final String realName) {
		return this.rawUpload(file, realName, null);
	}

	/**
	 * uploads a file
	 */
	private int rawUpload(String file, String realName, final InputStream in) {
		if (!file.equals(realName)) {
			net.sf.jftp.system.logging.Log.debug("File: " + file + ", stored as " + realName);
		} else {
			net.sf.jftp.system.logging.Log.debug("File: " + file);
			realName = null;
		}

		file = this.parse(file);

		String path = file;

		try {
			int p = 0;

			if (net.sf.jftp.system.StringUtils.isRelative(file)) {
				path = localPath + file;
			}

			file = net.sf.jftp.system.StringUtils.getFile(file);

			//BufferedReader in = jcon.getReader();
			boolean resume = false;
			String size = "0";

			if (net.sf.jftp.config.Settings.enableUploadResuming && (in == null)) {
				this.list();

				final String[] ls = this.sortLs();
				final String[] sizes = this.sortSize();

				if ((ls == null) || (sizes == null)) {
					net.sf.jftp.system.logging.Log.out(">>> ls out of sync (skipping resume check)");
				} else {
					for (int i = 0; i < ls.length; i++) {
						//Log.out(ls[i] + ":" + sizes[i]);
						if (realName != null) {
							if (ls[i].equals(realName)) {
								resume = true;
								size = sizes[i];

								break;
							}
						} else {
							if (ls[i].equals(file)) {
								resume = true;
								size = sizes[i];
							}
						}
					}

					final File f = new File(path);

					if (f.exists() && (f.length() <= Integer.parseInt(size))) {
						net.sf.jftp.system.logging.Log.out("skipping resuming, file is <= filelength");
						resume = false;
					} else if (f.exists() && Integer.parseInt(size) > 0) {
						if (!net.sf.jftp.config.Settings.noUploadResumingQuestion) {
							if (JOptionPane.showConfirmDialog(new JLabel(), "A file smaller than the one to be uploaded already exists on the server,\n do you want to resume the upload?", "Resume upload?", JOptionPane.YES_NO_OPTION) != JOptionPane.OK_OPTION) {
								resume = false;
							}
						}
						net.sf.jftp.system.logging.Log.out("resume: " + resume + ", size: " + size);
					} else {
						net.sf.jftp.system.logging.Log.out("resume: " + resume + ", size: " + size);
					}
				}
			}

			this.modeStream();

			//binary();
			p = this.negotiatePort();

			if (resume && net.sf.jftp.config.Settings.enableUploadResuming) {
				jcon.send(net.sf.jftp.net.FtpConstants.REST + " " + size);

				if (this.getLine(net.sf.jftp.net.FtpConnection.PROCEED) == null) {
					resume = false;
				}
			}

			dcon = new DataConnection(this, p, host, path, dataType, resume, Integer.parseInt(size), in); //, new Updater());

			while (!dcon.isThere()) {
				this.pause(10);
			}
			if (realName != null) {
				jcon.send(net.sf.jftp.net.FtpConstants.STOR + " " + realName);
			} else {
				jcon.send(net.sf.jftp.net.FtpConstants.STOR + " " + file);
			}

			final String tmp = this.getLine(net.sf.jftp.net.FtpConnection.POSITIVE);

			if (!tmp.startsWith(net.sf.jftp.net.FtpConnection.POSITIVE) && !tmp.startsWith(net.sf.jftp.net.FtpConnection.PROCEED)) {
				return net.sf.jftp.net.FtpConstants.TRANSFER_FAILED;
			}

			net.sf.jftp.system.logging.Log.debug(tmp);

			// we need to block since some ftp-servers do not want the
			// refresh command that dirpanel sends otherwise
			while (!dcon.finished) {
				this.pause(10);
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.debug(ex + " @FtpConnection::upload");

			return net.sf.jftp.net.FtpConstants.TRANSFER_FAILED;
		}

		return net.sf.jftp.net.FtpConstants.TRANSFER_SUCCESSFUL;
	}

	private int uploadDir(String dir) {
		//System.out.println("up");
		if (dir.endsWith("\\")) {
			System.out.println("Something's wrong with the selected directory - please report this bug!");
		}

		if (!dir.endsWith("/")) {
			dir = dir + "/";
		}

		if (net.sf.jftp.system.StringUtils.isRelative(dir)) {
			dir = localPath + dir;
		}

		final String single = net.sf.jftp.system.StringUtils.getDir(dir);
		//String path = dir.substring(0, dir.indexOf(single));

		final String remoteDir = pwd + single; //StringUtils.removeStart(dir,path);
		final String oldDir = pwd;

		if (net.sf.jftp.config.Settings.safeMode) {
			this.noop();
		}

		final boolean successful = this.mkdir(remoteDir);

		if (!successful) {
			return net.sf.jftp.net.FtpConstants.MKDIR_FAILED;
		}

		if (net.sf.jftp.config.Settings.safeMode) {
			this.noop();
		}

		this.chdirNoRefresh(remoteDir);

		final File f2 = new File(dir);
		final String[] tmp = f2.list();

		for (final String s : tmp) {
			final String res = dir + s;
			final java.io.File f3 = new java.io.File(res);

			if (f3.isDirectory()) {
				if (!work) {
					return net.sf.jftp.net.FtpConstants.TRANSFER_STOPPED;
				}

				this.uploadDir(res);
			} else {
				if (!work) {
					return net.sf.jftp.net.FtpConstants.TRANSFER_STOPPED;
				}

				fileCount++;

				if (this.rawUpload(res) < 0) {
					//return TRANSFER_STOPPED;
				}
			}
		}

		this.chdirNoRefresh(oldDir);

		return net.sf.jftp.net.FtpConstants.TRANSFER_SUCCESSFUL;
	}

	private String parse(String file) {
		file = this.parseSymlink(file);

		return file;
	}

	/**
	 * recursive delete remote directory
	 */
	private int cleanDir(String dir, final String path) {
		if (dir.isEmpty()) {
			return 0;
		}

		if (!dir.endsWith("/")) {
			dir = dir + "/";
		}

		final String remoteDir = net.sf.jftp.system.StringUtils.removeStart(dir, path);

		final String oldDir = pwd;
		this.chdirNoRefresh(pwd + remoteDir);

		try {
			this.list();
		} catch (final IOException ex) {
			// probably we don't have permission to ls here
			return net.sf.jftp.net.FtpConstants.PERMISSION_DENIED;
		}

		final String[] tmp = this.sortLs();
		this.chdirNoRefresh(oldDir);

		if (tmp == null) {
			return net.sf.jftp.net.FtpConstants.GENERIC_FAILED;
		}

		for (int i = 0; i < tmp.length; i++) {
			net.sf.jftp.system.logging.Log.out("cleanDir: " + tmp);

			if (this.isSymlink(tmp[i])) {
				net.sf.jftp.system.logging.Log.debug("WARNING: Skipping symlink, remove failed.");
				net.sf.jftp.system.logging.Log.debug("This is necessary to prevent possible data loss when removing those symlinks.");

				tmp[i] = null;
			}

			if (tmp[i] != null) {
				tmp[i] = this.parseSymlink(tmp[i]);
			}

			if ((tmp[i] == null) || tmp[i].equals("./") || tmp[i].equals("../")) {
			continue;
			}

			if (tmp[i].endsWith("/")) {
				this.cleanDir(dir + tmp[i], path);

				final int x = this.removeFileOrDir(dir + tmp[i]);

				if (x < 0) {
					return x;
				}
			} else {
				final int x = this.removeFileOrDir(dir + tmp[i]);

				if (x < 0) {
					return x;
				}
			}
		}

		return net.sf.jftp.net.FtpConstants.REMOVE_SUCCESSFUL;
	}

	/**
	 * Remove a remote file or directory.
	 *
	 * @param file The file to remove
	 * @return REMOVE_SUCCESSFUL, REMOVE_FAILED, PERMISSION_DENIED or  GENERIC_FAILED
	 */
	public int removeFileOrDir(String file) {
		if (file == null) {
			return 0;
		}

		if (file.trim().equals(".") || file.trim().equals("..")) {
			net.sf.jftp.system.logging.Log.debug("ERROR: Catching attempt to delete . or .. directories");

			return net.sf.jftp.net.FtpConstants.GENERIC_FAILED;
		}

		if (this.isSymlink(file)) {
			net.sf.jftp.system.logging.Log.debug("WARNING: Skipping symlink, remove failed.");
			net.sf.jftp.system.logging.Log.debug("This is necessary to prevent possible data loss when removing those symlinks.");

			return net.sf.jftp.net.FtpConstants.REMOVE_FAILED;
		}

		file = this.parseSymlink(file);

		if (file.endsWith("/")) {
			final int ret = this.cleanDir(file, pwd);

			if (ret < 0) {
				return ret;
			}

			jcon.send(net.sf.jftp.net.FtpConstants.RMD + " " + file);
		} else {
			jcon.send(net.sf.jftp.net.FtpConstants.DELE + " " + file);
		}

		if (this.success(net.sf.jftp.net.FtpConnection.POSITIVE))//FTP250_COMPLETED))
		{
			return net.sf.jftp.net.FtpConstants.REMOVE_SUCCESSFUL;
		} else {
			return net.sf.jftp.net.FtpConstants.REMOVE_FAILED;
		}
	}

	/**
	 * Disconnect from the server.
	 * The connection is marked ad closed even if the server does not respond correctly -
	 * if it fails for any reason the connection should just time out
	 */
	public void disconnect() {
		jcon.send(net.sf.jftp.net.FtpConstants.QUIT);
		this.getLine(net.sf.jftp.net.FtpConnection.POSITIVE);//FTP221_SERVICE_CLOSING);
		connected = false;
	}

	/**
	 * Execute a remote command.
	 * Sends noops before and after the command
	 *
	 * @param cmd The raw command that is to be sent to the server
	 */
	public void sendRawCommand(final String cmd) {
		this.noop();
		net.sf.jftp.system.logging.Log.clearCache();
		jcon.send(cmd);
		this.noop();
	}

	/**
	 * Reads the response until line found or error.
	 * (Used internally)
	 *
	 * @param until The String the response line hast to start with, 2 for succesful return codes for example
	 */
	public String getLine(final String until) {
		return this.getLine(new String[]{until});
	}

	/**
	 * Reads the response until line found or error.
	 * (Used internally)
	 */
	public String getLine(final String[] until) {
		BufferedReader in = null;

		try {
			in = jcon.getReader();
		} catch (final Exception ex) {
			net.sf.jftp.system.logging.Log.debug(ex + " @FtpConnection::getLine");
		}

		//String resultString = null;
		String tmp;

		while (true) {
			try {
				tmp = in.readLine();

				if (tmp == null) {
					break;
				}

				net.sf.jftp.system.logging.Log.debug(tmp);

				if (((tmp.startsWith(net.sf.jftp.net.FtpConnection.NEGATIVE) || (tmp.startsWith(net.sf.jftp.net.FtpConnection.NEGATIVE2))) && (tmp.charAt(3) != net.sf.jftp.net.FtpConnection.MORE_LINES_APPENDED)) || tmp.startsWith(net.sf.jftp.net.FtpConnection.PROCEED)) {
					return tmp;
				} else {
					for (final String s : until) {
						if (tmp.startsWith(s)) {
							if (tmp.charAt(3) != net.sf.jftp.net.FtpConnection.MORE_LINES_APPENDED) {
								return tmp;
							}
						}
					}
				}
			} catch (final Exception ex) {
				net.sf.jftp.system.logging.Log.debug(ex + " @FtpConnection::getLine");

				break;
			}
		}

		return null;
	}

	/**
	 * Check if login() was successful.
	 *
	 * @return True if connected, false otherwise
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Get the current remote directory.
	 *
	 * @return The server's CWD
	 */
	public String getPWD() {
		if (connected) {
			this.updatePWD();
		}

		if (net.sf.jftp.net.FtpConnection.TESTMODE) {
			return "HUGO.user.";
		}

		return pwd;
	}

	/**
	 * Returns current remote directory (cached)
	 *
	 * @return The cached CWD
	 */
	public String getCachedPWD() {
		return pwd;
	}

	/**
	 * updates PWD
	 */
	private void updatePWD() {
		jcon.send(net.sf.jftp.net.FtpConstants.PWD);

		String tmp = this.getLine(net.sf.jftp.net.FtpConnection.POSITIVE);//FTP257_PATH_CREATED);

		if (tmp == null) {
			return;
		}

		if (net.sf.jftp.net.FtpConnection.TESTMODE) {
			tmp = "\"'T759F.'\"";
		}

		String x1 = tmp;

		if (x1.contains("\"")) {
			x1 = tmp.substring(tmp.indexOf("\"") + 1);
			x1 = x1.substring(0, x1.indexOf("\""));
		}

		if (this.getOsType().contains("MVS")) {

			net.sf.jftp.system.logging.Log.out("pwd: " + x1);
		} else if (!x1.endsWith("/")) {
			x1 = x1 + "/";
		}

		pwd = x1;
	}

	/**
	 * Change directory unparsed.
	 * Use chdir instead if you do not parse the dir correctly...
	 *
	 * @param dirName The raw directory name
	 * @return True if successful, false otherwise
	 */
	public boolean chdirRaw(final String dirName) {
		jcon.send(net.sf.jftp.net.FtpConstants.CWD + " " + dirName);

		return this.success(net.sf.jftp.net.FtpConnection.POSITIVE);//FTP250_COMPLETED);
	}

	private boolean success(final String op) {
		final String tmp = this.getLine(op);

		return (tmp != null) && tmp.startsWith(op);
	}

	/**
	 * Change to the parent of the current working directory.
	 * The CDUP command is a special case of CWD, and is included
	 * to simplify the implementation of programs for transferring
	 * directory trees between operating systems having different
	 * syntaxes for naming the parent directory.
	 *
	 * @return True if successful, false otherwise
	 */
	public boolean cdup() {
		jcon.send(net.sf.jftp.net.FtpConstants.CDUP);

		return this.success(net.sf.jftp.net.FtpConnection.POSITIVE);//FTP200_OK);
	}

	/**
	 * Create a directory with the given name.
	 *
	 * @param dirName The name of the directory to create
	 * @return True if successful, false otherwise
	 */
	public boolean mkdir(final String dirName) {
		jcon.send(net.sf.jftp.net.FtpConstants.MKD + " " + dirName);

		final boolean ret = this.success(net.sf.jftp.net.FtpConnection.POSITIVE); // Filezille server bugfix, was: FTP257_PATH_CREATED);
		net.sf.jftp.system.logging.Log.out("mkdir(" + dirName + ")  returned: " + ret);

		//*** Added 03/20/2005
		this.fireDirectoryUpdate(this);

		//***
		return ret;
	}

	private int negotiatePort() throws IOException {
		String tmp = "";

		if (net.sf.jftp.config.Settings.getFtpPasvMode()) {
			jcon.send(net.sf.jftp.net.FtpConstants.PASV);

			tmp = this.getLine(net.sf.jftp.net.FtpConstants.FTP227_ENTERING_PASSIVE_MODE);

			if ((tmp != null) && !tmp.startsWith(net.sf.jftp.net.FtpConnection.NEGATIVE)) {
				return this.getPasvPort(tmp);
			}
		}

		tmp = this.getActivePortCmd();
		jcon.send(tmp);
		this.getLine(net.sf.jftp.net.FtpConstants.FTP200_OK);

		return this.getActivePort();
	}

	/**
	 * List remote directory.
	 * Note that you have to get the output using the
	 * sort*-methods.
	 */
	public void list() throws IOException {
		String oldType = "";

		try {
			//BufferedReader in = jcon.getReader();

			int p = 0;

			this.modeStream();

			oldType = typeNow;
			this.ascii();

			p = this.negotiatePort();
			dcon = new DataConnection(this, p, host, null, DataConnection.GET, false, true); //,null);

			while (dcon.getInputStream() == null) {
				//System.out.print("#");
				this.pause(10);
			}
			final BufferedReader input = new BufferedReader(new InputStreamReader(dcon.getInputStream()));

			jcon.send(net.sf.jftp.net.FtpConnection.LIST);

			String line;
			currentListing.removeAllElements();

			while ((line = input.readLine()) != null) {
				//System.out.println("-> "+line);
				if (!line.trim().isEmpty()) {
					currentListing.add(line);
				}
			}

			this.getLine(net.sf.jftp.net.FtpConnection.POSITIVE); //FTP226_CLOSING_DATA_REQUEST_SUCCESSFUL);
			input.close();

			if (!oldType.equals(net.sf.jftp.net.FtpConnection.ASCII)) {
				this.type(oldType);
			}
		} catch (final Exception ex) {
			net.sf.jftp.system.logging.Log.debug("Cannot list remote directory!");

			if (!oldType.equals(net.sf.jftp.net.FtpConnection.ASCII)) {
				this.type(oldType);
			}

			ex.printStackTrace();
			throw new IOException(ex.getMessage());
		}
	}

	/**
	 * Parses directory and does a chdir().
	 * Does also fire a directory update event.
	 *
	 * @return True is successful, false otherwise
	 */
	public boolean chdir(final String p) {

		final boolean tmp = this.chdirWork(p);

		if (!tmp) {
			return false;
		} else {
			this.fireDirectoryUpdate(this);

			return true;
		}
	}

	/**
	 * Parses directory and does a chdir(), but does not send an update signal
	 *
	 * @return True is successful, false otherwise
	 */
	public boolean chdirNoRefresh(final String p) {
		return this.chdirWork(p);
	}

	private boolean chdirWork(String p) {
		if (net.sf.jftp.config.Settings.safeMode) {
			this.noop();
		}

		p = this.parseSymlinkBack(p);

		if (this.getOsType().contains("OS/2")) {
			return this.chdirRaw(p);
		} else if (this.getOsType().contains("MVS")) {
			boolean cdup = false;

			System.out.print("MVS parser: " + p + " -> ");

			//TODO: handle relative unix paths
			if (!this.getPWD().startsWith("/")) //return chdirRaw(p);
			{
				if (p.endsWith("..")) {
					cdup = true;
				}
				//TODO let dirlister form correct syntax

				p = p.replaceAll("/", "");
				boolean ew = p.startsWith("'");
				String tmp = null;

				if (p.startsWith("'") && !p.endsWith("'")) {
					if (cdup) {
						p = p.substring(0, p.length() - 4);
						if (p.indexOf(".") > 0) {
							p = p.substring(0, p.lastIndexOf("."));
						}
						p += "'";
					}

					tmp = p.substring(0, p.lastIndexOf("'"));
					p = p.substring(p.lastIndexOf("'") + 1);
					ew = false;
				} else if (cdup) {
					p = p.substring(0, p.length() - 3);
					if (p.indexOf(".") > 0) {
						p = p.substring(0, p.lastIndexOf("."));
					}
				}

				if (p.indexOf(".") > 0 && !ew) {
					p = p.substring(0, p.indexOf("."));
				}

				if (tmp != null) p = tmp + p + "'";

				System.out.println(p);
				net.sf.jftp.system.logging.Log.out("MVS parser: " + p);

				return this.chdirRaw(p);
			}
		}

		try {
			// check if we don't have a relative path
			if (!p.startsWith("/") && !p.startsWith("~")) {
				p = pwd + p;
			}

			// argh, we should document that!
			if (p.endsWith("..")) {
				final boolean home = p.startsWith("~");
				final StringTokenizer stok = new StringTokenizer(p, "/");

				//System.out.println("path p :"+p +" Tokens: "+stok.countTokens());
				if (stok.countTokens() > 2) {
					String pold1 = "";
					String pold2 = "";
					String pnew = "";

					while (stok.hasMoreTokens()) {
						pold1 = pold2;
						pold2 = pnew;
						pnew = pnew + "/" + stok.nextToken();
					}

					p = pold1;
				} else {
					p = "/";
				}

				if (home) {
					p = p.substring(1);
				}
			}

			//System.out.println("path: "+p);
			if (!this.chdirRaw(p)) {
				return false;
			}
		} catch (final Exception ex) {
			net.sf.jftp.system.logging.Log.debug("(remote) Can not get pathname! " + pwd + ":" + p);
			ex.printStackTrace();

			return false;
		}

		//fireDirectoryUpdate(this);

		// --------------------------------------
		this.updatePWD();


		return true;
	}

	/**
	 * Waits a specified amount of time
	 *
	 * @param time The time to sleep in milliseconds
	 */
	public void pause(final int time) {
		try {
			Thread.sleep(time);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Return the local working directory
	 *
	 * @return The local CWD
	 */
	public String getLocalPath() {
		return localPath;
	}

	/**
	 * Set the local working directory
	 *
	 * @param newPath The new local CWD
	 * @return Always true in the current implementation
	 */
	public boolean setLocalPath(final String newPath) {
		localPath = newPath;

		if (!localPath.endsWith("/")) {
			localPath = localPath + "/";
		}

		return true;
	}

	/**
	 * Checks wheter a file exists.
	 *
	 * @param file the name of the file
	 * @return An int-code: CHDIR_FAILED, PERMISSION_DENIED (no listing allowed), R, W or DENIED
	 */
	public int exists(final String file) {
		String dir = null;
		final String tmpPWD = pwd;

		if (file.contains("/")) {
			dir = file.substring(0, file.lastIndexOf("/") + 1);
			net.sf.jftp.system.logging.Log.out("checking dir: " + dir);

			if (!this.chdir(dir)) {
				return net.sf.jftp.net.FtpConstants.CHDIR_FAILED;
			}
		}

		try {
			this.list();
		} catch (final IOException ex) {
			ex.printStackTrace();

			return net.sf.jftp.net.FtpConstants.PERMISSION_DENIED;
		}

		final String f = file.substring(file.lastIndexOf("/") + 1);
		net.sf.jftp.system.logging.Log.out("checking file: " + f);

		final String[] files = this.sortLs();
		final int[] perms = this.getPermissions();

		int y = -1;

		for (int x = 0; x < files.length; x++) {
			if (files[x].equals(f)) {
				y = x;

				break;
			}
		}

		if (y == -1) {
			return net.sf.jftp.net.FtpConstants.FILE_NOT_FOUND;
		}

		if (dir != null) {
			this.chdir(tmpPWD);
		}

		return perms[y];
	}

	/**
	 * Moves or renames remote file.
	 *
	 * @param from remote file to move or rename.
	 * @param to   new file name.
	 * @return <code>true</code> if completed successfully; <code>false</code> otherwise.
	 */
	public boolean rename(final String from, final String to) {
		jcon.send("RNFR " + from);

		if (this.success(net.sf.jftp.net.FtpConstants.RC350)) {
			jcon.send("RNTO " + to);

			//FTP250_COMPLETED))
			return this.success(net.sf.jftp.net.FtpConnection.POSITIVE);
		}

		return false;
	}

	/**
	 * Get the port.
	 *
	 * @return The port used by the FTP control connection
	 */
	public int getPort() {
		return port;
	}

	private int getPortA() {
		return net.sf.jftp.net.FtpConnection.porta;
	}

	private int getPortB() {
		return net.sf.jftp.net.FtpConnection.portb;
	}


	private String getActivePortCmd() throws IOException {
		final InetAddress ipaddr = jcon.getLocalAddress();
		final String ip = ipaddr.getHostAddress().replace('.', ',');

		final ServerSocket aSock = new ServerSocket(0);
		final int availPort = aSock.getLocalPort();
		aSock.close();
		net.sf.jftp.net.FtpConnection.porta = availPort / 256;
		net.sf.jftp.net.FtpConnection.portb = availPort % 256;

		return "PORT " + ip + "," + net.sf.jftp.net.FtpConnection.porta + "," + net.sf.jftp.net.FtpConnection.portb;
	}

	public void binary() {
		this.type(net.sf.jftp.net.FtpConnection.BINARY);
	}

	public void ascii() {
		this.type(net.sf.jftp.net.FtpConnection.ASCII);
	}

	public boolean type(final String code) {
		jcon.send(net.sf.jftp.net.FtpConstants.TYPE + " " + code);

		final String tmp = this.getLine(net.sf.jftp.net.FtpConnection.POSITIVE);//FTP200_OK);

		if ((tmp != null) && tmp.startsWith(net.sf.jftp.net.FtpConnection.POSITIVE))//FTP200_OK))
		{
			typeNow = code;

			return true;
		}

		return false;
	}

	/**
	 * Returns the type used.
	 *
	 * @return The type String, I, A, L8 for example
	 */
	public String getTypeNow() {
		return typeNow;
	}

	/**
	 * Do nothing, but flush buffers
	 */
	public void noop() {
		jcon.send(net.sf.jftp.net.FtpConstants.NOOP);
		this.getLine(net.sf.jftp.net.FtpConnection.POSITIVE);//FTP200_OK);
	}

	/**
	 * Try to abort the transfer.
	 */
	public void abort() {
		jcon.send(net.sf.jftp.net.FtpConstants.ABOR);
		this.getLine(net.sf.jftp.net.FtpConnection.POSITIVE); // 226
	}

	/**
	 * This command is used to find out the type of operating
	 * system at the server. The reply shall have as its first
	 * word one of the system names listed in the current version
	 * of the Assigned Numbers document (RFC 943).
	 *
	 * @return The server response of the SYST command
	 */
	public String system() { // possible responses 215, 500, 501, 502, and 421
		jcon.send(net.sf.jftp.net.FtpConstants.SYST);

		final String response = this.getLine(net.sf.jftp.net.FtpConnection.POSITIVE);//FTP215_SYSTEM_TYPE);

		if (response != null) {
			this.setOsType(response);
		} else {
			this.setOsType("UNIX");
		}

		return response;
	}

	/**
	 * Set mode to Stream.
	 * Used internally.
	 */
	public void modeStream() {
		if (net.sf.jftp.net.FtpConnection.useStream && !modeStreamSet) {
			final String ret = this.mode(net.sf.jftp.net.FtpConnection.STREAM);

			if ((ret != null) && ret.startsWith(net.sf.jftp.net.FtpConnection.NEGATIVE)) {
				net.sf.jftp.net.FtpConnection.useStream = false;
			} else {
				modeStreamSet = true;
			}
		}
	}

	/**
	 * Unsupported at this time.
	 */
	public void modeBlocked() {
		if (net.sf.jftp.net.FtpConnection.useBlocked) {
			if (this.mode(net.sf.jftp.net.FtpConnection.BLOCKED).startsWith(net.sf.jftp.net.FtpConnection.NEGATIVE)) {
				net.sf.jftp.net.FtpConnection.useBlocked = false;
			}
		}
	}

	/*
	 * Unsupported at this time.
	 */
	public void modeCompressed() {
		if (net.sf.jftp.net.FtpConnection.useCompressed) {
			if (this.mode(net.sf.jftp.net.FtpConnection.COMPRESSED).startsWith(net.sf.jftp.net.FtpConnection.NEGATIVE)) {
				net.sf.jftp.net.FtpConnection.useCompressed = false;
			}
		}
	}

	/**
	 * Set mode manually.
	 *
	 * @param code The mode code wanted, I, A, L8 for example
	 * @return The server's response to the request
	 */
	public String mode(final String code) {
		jcon.send(net.sf.jftp.net.FtpConstants.MODE + " " + code);

		String ret = "";

		try {
			ret = this.getLine(net.sf.jftp.net.FtpConnection.POSITIVE);//FTP200_OK);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}

		return ret;
	}

	/**
	 * Return the host used.
	 *
	 * @return The remote host of this connection
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Return the username.
	 *
	 * @return The username used by this connection
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Return the password.
	 *
	 * @return The password used by this connection
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Return the DataConnection.
	 * Should be necessary only for complex actions.
	 *
	 * @return The DataConnection object currently used by this conenction
	 */
	public DataConnection getDataConnection() {
		return dcon;
	}

	/**
	 * Add a ConnectionListener.
	 * The Listener is notified about transfers and the connection state.
	 *
	 * @param l A ConnectionListener object
	 */
	public void addConnectionListener(final ConnectionListener l) {
		listeners.add(l);
	}

	/**
	 * Add a Vector of ConnectionListeners.
	 * Each Listener is notified about transfers and the connection state.
	 *
	 * @param l A Vector containig ConnectionListener objects
	 */
	public void setConnectionListeners(final Vector<ConnectionListener> l) {
		listeners = l;
	}

	/**
	 * Remote directory has changed.
	 *
	 * @param con The FtpConnection calling the event
	 */
	public void fireDirectoryUpdate(final FtpConnection con) {
		if (listeners == null) {
		} else {
			for (int i = 0; i < listeners.size(); i++) {
				listeners.elementAt(i).updateRemoteDirectory(con);
			}
		}
	}

	/**
	 * Progress update.
	 * The transfer is active and makes progress, so a signal is send
	 * each few kb.
	 *
	 * @param file  The file transferred
	 * @param type  the type of event, DataConnection.GETDIR for example
	 * @param bytes The number of bytes transferred so far
	 */
	public void fireProgressUpdate(final String file, final String type, final long bytes) {
		//System.out.println(listener);
		if (listeners == null) {
		} else {
			for (int i = 0; i < listeners.size(); i++) {
				final ConnectionListener listener = listeners.elementAt(i);

				if (shortProgress && net.sf.jftp.config.Settings.shortProgress) {
					if (type.startsWith(DataConnection.DFINISHED)) {
						listener.updateProgress(baseFile, DataConnection.DFINISHED + ":" + fileCount, bytes);
					} else if (isDirUpload) {
						listener.updateProgress(baseFile, DataConnection.PUTDIR + ":" + fileCount, bytes);
					} else {
						listener.updateProgress(baseFile, DataConnection.GETDIR + ":" + fileCount, bytes);
					}
				} else {
					listener.updateProgress(file, type, bytes);
				}
			}
		}
	}

	/**
	 * Connection is there and user logged in
	 *
	 * @param con The FtpConnection calling the event
	 */
	public void fireConnectionInitialized(final FtpConnection con) {
		if (listeners == null) {
		} else {
			for (int i = 0; i < listeners.size(); i++) {
				listeners.elementAt(i).connectionInitialized(con);
			}
		}
	}

	/**
	 * We are not logged in for some reason
	 *
	 * @param con The FtpConnection calling the event
	 * @param why The login() response code
	 */
	public void fireConnectionFailed(final FtpConnection con, final String why) {
		if (listeners == null) {
		} else {
			for (int i = 0; i < listeners.size(); i++) {
				listeners.elementAt(i).connectionFailed(con, why);
			}
		}
	}

	/**
	 * Transfer is done
	 *
	 * @param con The FtpConnection calling the event
	 */
	public void fireActionFinished(final FtpConnection con) {
		if (listeners == null) {
		} else {
			for (int i = 0; i < listeners.size(); i++) {
				listeners.elementAt(i).actionFinished(con);
			}
		}
	}

	/**
	 * Return the ConnectionHandler.
	 *
	 * @return The connection handler of this instance
	 */
	public ConnectionHandler getConnectionHandler() {
		return handler;
	}

	/**
	 * Set the ConnectionHandler.
	 *
	 * @param h The connection handler that is to be used by this connection
	 */
	public void setConnectionHandler(final ConnectionHandler h) {
		handler = h;
	}

	/**
	 * Get the last FtpTransfer object spawned.
	 */
	public FtpTransfer getLastInitiatedTransfer() {
		return transfers.size() > 0 ? transfers.get(transfers.size() - 1) : null;
	}

	/**
	 * Abort the last spawned transfer.
	 */
	public void abortTransfer() {
		for (final FtpTransfer transfer : transfers) {
			try {
				final DataConnection dcon = transfer.getDataConnection();

				if (dcon == null || dcon.sock.isClosed()) {
					net.sf.jftp.system.logging.Log.out("nothing to abort");

					continue;
				}

				dcon.getCon().work = false;
				dcon.sock.close();
			} catch (final Exception ex) {
				net.sf.jftp.system.logging.Log.debug("Exception during abort: " + ex);
				ex.printStackTrace();
			}
		}
	}

	public Date[] sortDates() {
		if (dateVector.size() > 0) {
			return (Date[]) dateVector.toArray();
		} else {
			return null;
		}
	}

	public String getCRLF() {
		return crlf;
	}

	public BufferedReader getIn() {
		return in;
	}

	public void setIn(final BufferedReader in) {
		this.in = in;
	}

	public BufferedReader getCommandInputReader() {
		return jcon.getIn();
	}

	public OutputStream getCommandOutputStream() {
		return jcon.getOut();
	}

}
