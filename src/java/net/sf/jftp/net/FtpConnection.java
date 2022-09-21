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

import net.sf.jftp.config.LoadSet;
import net.sf.jftp.config.SaveSet;
import net.sf.jftp.config.Settings;
import net.sf.jftp.system.StringUtils;
import net.sf.jftp.system.logging.Log;

import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.*;


/**
 * All control transactions are made here.
 * See doc/FtpDownload.java for examples and note
 * that this class is event-driven and not Exception-based!
 * You might want to take a look at ConnectionListener, too.
 *
 * @author David Hansmann, hansmann.d@debitel.net
 */
public class FtpConnection implements BasicConnection, FtpConstants {
    public final static String ASCII = "A";
    public final static String BINARY = "I";
    public final static String EBCDIC = "E";
    public final static String L8 = "L 8";
    public final static String STREAM = "S";
    public final static String BLOCKED = "B";
    public final static String COMPRESSED = "C";
    private static final boolean TESTMODE = false;
    // everything starting with this will be treated
    // as a negative response
    private final static String NEGATIVE = "5";
    private final static String NEGATIVE2 = "4";
    // everything starting with this will be treated
    // as a positive response
    private final static String POSITIVE = "2";
    // for resuming
    private final static String PROCEED = "3"; //"350";
    private final static char MORE_LINES_APPENDED = '-';
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
    private final boolean msg = true;
    private final String[] loginAck = new String[]{FTP331_USER_OK_NEED_PASSWORD, FTP230_LOGGED_IN};
    private final List<FtpTransfer> transfers = new ArrayList<FtpTransfer>();
    /**
     * Used to determine the type of transfer.
     */
    public boolean hasUploaded = false;
    public boolean work = true;
    /**
     * May contain date listing
     */
    public Vector<Date> dateVector = new Vector<Date>();
    public Vector<String> currentListing = new Vector<String>();
    public Vector<String> currentFiles = new Vector<String>();
    public Vector<String> currentSizes = new Vector<String>();
    public Vector<String> currentPerms = new Vector<String>();
    private boolean ok = true;
    private String pwd = "";
    private String initCWD = Settings.defaultDir;
    private String osType = "";
    private String dataType;
    private boolean modeStreamSet = false;
    private DataConnection dcon = null;
    private Vector<ConnectionListener> listeners = new Vector<ConnectionListener>();
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
    public FtpConnection(String host) {
        this.host = host;

        File f = new File(".");
        setLocalPath(f.getAbsolutePath());
    }

    /**
     * Create an instance with a given host, port and initial remote working directory
     *
     * @param host    The host to connect to
     * @param port    The port used for the control connection, usually 21
     * @param initCWD The initial remote working directory
     */
    public FtpConnection(String host, int port, String initCWD) {
        this.host = host;
        this.port = port;
        this.initCWD = initCWD;

        File f = new File(".");
        setLocalPath(f.getAbsolutePath());
    }

    /**
     * Connect to the server an log in.
     * <p>
     * Does also fire directory update and connection initialized event if successful or
     * connection failed event if failed.
     *
     * @param username The username
     * @param password The password
     * @param initCWD  The initial remote working directory
     * @param crlfx    \n, \r, \r\n, CR, LF or CRLF - line break style of the server
     * @return WRONG_LOGIN_DATA, OFFLINE, GENERIC_FAILED or LOGIN_OK status code
     */
    public FtpConnection(String host, int port, String initCWD, String crlfx) {
        this.host = host;
        this.port = port;
        this.initCWD = initCWD;

        if (crlfx != null) {
            if (crlfx.equals("\n") || crlfx.equals("\r") || crlfx.equals("\r\n")) this.crlf = crlfx;
            crlfx = crlfx.trim().toUpperCase();
            if (crlfx.equals("LF")) this.crlf = "\n";
            else if (crlfx.equals("CR")) this.crlf = "\r";
            else if (crlfx.equals("CRLF")) this.crlf = "\r\n";
        }
        //Log.out("\n\nCRLF:---"+crlf+"---\n\n");

        File f = new File(".");
        setLocalPath(f.getAbsolutePath());
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
    public int login(String username, String password) {
        this.username = username;
        this.password = password;

        int status = LOGIN_OK;

        if (msg) {
            Log.debug("Connecting to " + host);
        }

        jcon = new JConnection(host, port);

        if (jcon.isThere()) {
            in = jcon.getReader();

            if (getLine(POSITIVE) == null)//FTP220_SERVICE_READY) == null)
            {
                ok = false;
                Log.debug("Server closed Connection, maybe too many users...");
                status = OFFLINE;
            }

            if (msg) {
                Log.debug("Connection established...");
            }

            jcon.send(USER + " " + username);

            if (!getLine(loginAck).startsWith(POSITIVE))//FTP230_LOGGED_IN))
            {
                jcon.send(PASS + " " + password);

                if (success(POSITIVE))//FTP230_LOGGED_IN))
                {
                    if (msg) {
                        Log.debug("Logged in...");
                    }
                } else {
                    Log.debug("Wrong password!");
                    ok = false;
                    status = WRONG_LOGIN_DATA;
                }
            }

            // end if(jcon)...
        } else {
            if (msg) {
                Log.debug("FTP not available!");
                ok = false;
                status = GENERIC_FAILED;
            }
        }

        if (ok) {
            connected = true;
            system();

            //if(getOsType().indexOf("MVS") < 0)
            binary();

            if (initCWD.trim().equals(Settings.defaultDir) ||
                    !chdirNoRefresh(initCWD)) {
                //System.out.println("default dir...");
                //if(!chdirNoRefresh(getPWD()))
                //{
                //System.out.println("FtpConnection should not do this...");
                //	if(!chdirNoRefresh("~"))
                //	{
                //  		chdirNoRefresh("/");
                //	}
                //}
                updatePWD();
            }

            if (Settings.ftpKeepAlive) {
                keepAliveThread = new FtpKeepAliveThread(this);
            }

            //Array of length 6 created, as that is maximum LoadSet size (need
            //public variable with that constant there for it to refer to?)
            String[] advSettings = new String[6];

            if (getOsType().indexOf("OS/2") >= 0) {
                LIST_DEFAULT = "LIST";
            }

            if (LIST.equals("default")) {
                //just get the first item (somehow it knows first is the
                //FTP list command)
                advSettings = LoadSet.loadSet(Settings.adv_settings);

                //*** IF FILE NOT FOUND, CREATE IT AND SET IT TO LIST_DEFAULT
                if (advSettings == null) {
                    LIST = LIST_DEFAULT;

                    new SaveSet(Settings.adv_settings, LIST);
                } else {
                    LIST = advSettings[0];

                    if (LIST == null) {
                        LIST = LIST_DEFAULT;
                    }
                }
            }

            if (getOsType().indexOf("MVS") >= 0) {
                LIST = "LIST";
            }

            //***
            fireDirectoryUpdate(this);
            fireConnectionInitialized(this);
        } else {
            fireConnectionFailed(this, Integer.toString(status));
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

            for (int i = 0; i < currentListing.size(); i++) {
                String tmp = currentListing.get(i);

                // ------------- VMS override -------------------
                if (getOsType().indexOf("VMS") >= 0) {
                    if (tmp.indexOf(";") < 0) {
                        continue;
                    }

                    currentSizes.add("-1");

                    continue;
                }

                // ------------- MVS override -------------------
                if (getOsType().indexOf("MVS") >= 0) {
                    if (tmp.startsWith("Volume") || (tmp.indexOf(" ") < 0)) {
                        continue;
                    }

                    currentSizes.add("-1");

                    continue;
                }

                // ------------------------------------------------
                else if (getOsType().indexOf("WINDOW") >= 0) {
                    StringTokenizer to = new StringTokenizer(tmp, " ", false);

                    if (to.countTokens() >= 4) {
                        to.nextToken();
                        to.nextToken();

                        String size = to.nextToken();

                        if (size.equals("<DIR>")) {
                            currentSizes.add("0");
                        } else {
                            try {
                                Long.parseLong(size);
                                currentSizes.add(size);
                            } catch (Exception ex) {
                                currentSizes.add("-1");
                                Log.out("WARNING: filesize can not be determined - value is " +
                                        size);
                            }
                        }
                    }
                }

                // ------------------------------------------------
                else if (getOsType().indexOf("OS/2") >= 0) {
                    StringTokenizer to = new StringTokenizer(tmp, " ", false);
                    tmp = giveSize(to, 0);
                    Log.out("OS/2 parser (size): " + tmp);
                    currentSizes.add(tmp);
                } else {
                    //Log.out("\n\nparser\n\n");
                    StringTokenizer to = new StringTokenizer(tmp, " ", false);

                    if (to.countTokens() >= 8) // unix
                    {
                        tmp = giveSize(to, 4);
                        currentSizes.add(tmp);

                        //Log.out(tmp);
                    } else if (to.countTokens() == 7) // unix, too
                    {
                        Log.out("WARNING: 7 token backup size parser activated!");
                        tmp = giveSize(to, 4);
                        currentSizes.add(tmp);
                    } else {
                        Log.debug("cannot parse line: " + tmp);
                    }
                }
            }
        } catch (Exception ex) {
            Log.debug(ex + " @FtpConnection::sortSize#1");
            return new String[0];
        }

        return toArray(currentSizes);
    }

    private String[] toArray(Vector<String> in) {
        String[] ret = new String[in.size()];

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
     * @param file The file containing the raw server listing, usually Settings.ls_out
     * @return An int-array of sizes containing W, R or DENIED for each file
     */
    public int[] getPermissions() {
        try {
            currentPerms.removeAllElements();

            for (int i = 0; i < currentListing.size(); i++) {
                String tmp = currentListing.get(i);

                // ------------- VMS override -------------------
                if (getOsType().indexOf("VMS") >= 0) {
                    if (tmp.indexOf(";") < 0) {
                        continue;
                    }

                    currentPerms.add("r");

                    continue;
                }

                // ------------- MVS override -------------------
                if (getOsType().indexOf("MVS") >= 0) {
                    if (tmp.startsWith("Volume")) {
                        continue;
                    }

                    currentPerms.add("r");

                    continue;
                }

                // ------------------------------------------------

                StringTokenizer to = new StringTokenizer(tmp.trim(), " ", false);

                if (!(to.countTokens() > 3) ||
                        (tmp.startsWith("/") && (tmp.indexOf("denied") > 0))) {
                    continue;
                }

                tmp = to.nextToken();

                if (tmp.length() != 10) {
                    //System.out.println(tmp + " - e");
                    return null; // exotic bug, hardlinks are not found or something ("/bin/ls: dir: no such file or directy" in ls output) - we have no permissions then
                }

                char ur = tmp.charAt(1);
                char uw = tmp.charAt(2);
                char ar = tmp.charAt(7);
                char aw = tmp.charAt(8);

                to.nextToken();

                String user = to.nextToken();

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
        } catch (Exception ex) {
            Log.debug(ex + " @FtpConnection::getPermissions#1");
        }

        int[] ret = new int[currentPerms.size()];

        for (int i = 0; i < currentPerms.size(); i++) {
            String fx = currentPerms.get(i);

            if (fx.equals("w")) {
                ret[i] = W;
            } else if (fx.equals("n")) {
                ret[i] = DENIED;
            } else {
                ret[i] = R;
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
            dateVector = new Vector<Date>();
            currentFiles.removeAllElements();

            for (int i = 0; i < currentListing.size(); i++) {
                String tmp = currentListing.get(i);

                // ------------------- VMS override --------------------
                if (getOsType().indexOf("VMS") >= 0) {
                    int x = tmp.indexOf(";");

                    if (x < 0) {
                        continue;
                    }

                    tmp = tmp.substring(0, x);

                    if (tmp.endsWith("DIR")) {
                        currentFiles.add(tmp.substring(0, tmp.lastIndexOf(".")) +
                                "/");
                    } else {
                        currentFiles.add(tmp);
                    }

                    //Log.out("listing - (vms parser): " + tmp);

                    continue;
                } else if (getOsType().indexOf("OS/2") >= 0) {
                    StringTokenizer to2 = new StringTokenizer(tmp, " ", true);

                    if (giveFile(to2, 2).indexOf("DIR") >= 0) {
                        to2 = new StringTokenizer(tmp, " ", true);
                        tmp = giveFile(to2, 5);
                        tmp = tmp + "/";
                    } else {
                        to2 = new StringTokenizer(tmp, " ", true);

                        if (giveFile(to2, 1).indexOf("DIR") >= 0) {
                            //Log.out("OS/2 parser (DIRFIX): " + tmp);
                            to2 = new StringTokenizer(tmp, " ", true);
                            tmp = giveFile(to2, 4);
                            tmp = tmp + "/";
                        } else {
                            to2 = new StringTokenizer(tmp, " ", true);
                            tmp = giveFile(to2, 4);
                        }
                    }

                    Log.out("OS/2 parser: " + tmp);
                    currentFiles.add(tmp);

                    continue;
                }

                // -------------------------------------------------------
                // ------------------- MVS override --------------------
                if (getOsType().indexOf("MVS") >= 0) {
                    if (tmp.startsWith("Volume") || (tmp.indexOf(" ") < 0)) {
                        Log.out("->" + tmp);

                        continue;
                    }

                    String f = tmp.substring(tmp.lastIndexOf(" ")).trim();
                    String isDir = tmp.substring(tmp.lastIndexOf(" ") - 5,
                            tmp.lastIndexOf(" "));

                    if (isDir.indexOf("PO") >= 0) {
                        currentFiles.add(f + "/");
                    } else {
                        currentFiles.add(f);
                    }

                    Log.out("listing - (mvs parser): " + tmp + " -> " + f);
                    continue;
                } else if (getOsType().indexOf("OS/2") >= 0) {
                    StringTokenizer to2 = new StringTokenizer(tmp, " ", true);

                    if (giveFile(to2, 2).indexOf("DIR") >= 0) {
                        to2 = new StringTokenizer(tmp, " ", true);
                        tmp = giveFile(to2, 5);
                        tmp = tmp + "/";
                    } else {
                        to2 = new StringTokenizer(tmp, " ", true);

                        if (giveFile(to2, 1).indexOf("DIR") >= 0) {
                            //Log.out("OS/2 parser (DIRFIX): " + tmp);
                            to2 = new StringTokenizer(tmp, " ", true);
                            tmp = giveFile(to2, 4);
                            tmp = tmp + "/";
                        } else {
                            to2 = new StringTokenizer(tmp, " ", true);
                            tmp = giveFile(to2, 4);
                        }
                    }

                    Log.out("OS/2 parser: " + tmp);
                    currentFiles.add(tmp);

                    continue;
                }

                // -------------------------------------------------------
                // ------------------- Unix, Windows and others -----
                boolean isDir = false;
                boolean isLink = false;

                if (tmp.startsWith("d") || (tmp.indexOf("<DIR>") >= 0)) {
                    isDir = true;
                }

                if (tmp.startsWith("l")) {
                    isLink = true;
                }

                if (tmp.startsWith("/") && (tmp.indexOf("denied") > 0)) {
                    continue;
                }

                StringTokenizer to = new StringTokenizer(tmp, " ", false);
                StringTokenizer to2 = new StringTokenizer(tmp, " ", true);

                int tokens = to.countTokens();

                //Log.out("listing: tokens: " + tokens + " - " + tmp);
                boolean set = false;

                //-----preparsing-----
                int lcount = 0;

                if (!newUnixDateStyle) {
                    try {
                        StringTokenizer tx = new StringTokenizer(tmp, " ", false);
                        tx.nextToken();
                        tx.nextToken();
                        tx.nextToken();
                        tx.nextToken();
                        tx.nextToken();

                        String date = tx.nextToken();
                        int x = date.indexOf("-");
                        int y = date.lastIndexOf("-");

                        if (y > x) {
                            Log.debug("Using new Unix date parser");
                            newUnixDateStyle = true;
                        }
                    } catch (Exception ex) {
                        Log.out("could not preparse line: " + tmp);
                        lcount++;

                        //ex.printStackTrace();
                    }
                }

                //--------------------
                //Log.out("tmp: " + tmp);
                if (newUnixDateStyle) // unix, too
                {
                    set = true;

                    //Log.out("listing - (unix parser #2, token 7): " + tmp);
                    //------- date parser testing ---------
                    try {
                        //Log.out(">>> date parser: " + tmp);
                        StringTokenizer to3 = new StringTokenizer(tmp, " ", true);
                        String date = giveFile(to3, 5);
                        String date2 = date.substring(date.indexOf("-") + 1);
                        date2 = date2.substring(date.indexOf("-") + 2);

                        //Log.out("date1: "+date+"/"+"date2: "+date2);
                        String t = date;
                        String y = t.substring(0, t.indexOf("-"));
                        String m = t.substring(t.indexOf("-") + 1,
                                t.indexOf("-") + 3);
                        String m1 = t.substring(t.indexOf("-") + 1);
                        String day = m1.substring(m1.indexOf("-") + 1,
                                m1.indexOf("-") + 3);
                        String h = date2.substring(0, date2.indexOf(":"));
                        String min = date2.substring(date2.indexOf(":") + 1,
                                date2.indexOf(":") + 3);

                        //Log.out("day:"+day+"year:"+y+"mon:"+m+"hour:"+h+"m:"+min);
                        Calendar c = new GregorianCalendar();
                        c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
                        c.set(Calendar.YEAR, Integer.parseInt(y));
                        c.set(Calendar.MONTH, Integer.parseInt(m) - 1);
                        c.set(Calendar.HOUR, Integer.parseInt(h));
                        c.set(Calendar.MINUTE, Integer.parseInt(min));

                        dateVector.add(c.getTime());

                        //Log.out("+++ date: \"" + d.toString() + "\"");
                    } catch (Exception ex) {
                        ex.printStackTrace();

                        //set = false;
                    }

                    // -----------------------------
                    tmp = giveFile(to2, 7);

                    if (lcount > 1) {
                        dateVector = new Vector<Date>();
                    }
                } else if (tokens > 8) // unix
                {
                    set = true;
                    tmp = giveFile(to2, 8);

                    //Log.out("listing - (unix parser, token 8): " + tmp);
                } else if (tokens > 3) // windows
                {
                    set = true;
                    tmp = giveFile(to2, 3);

                    if (tmp.startsWith("<error>")) {
                        tmp = giveFile(to2, 4);

                        //Log.out("listing - (windows parser, token 4): " + tmp);
                    } else {
                        //Log.out("listing - (windows parser, token 3): " + tmp);
                    }
                }

                if (tmp.startsWith("<error>") || !set) {
                    if (tokens < 3) {
                        continue;
                    }

                    tmp = giveFile(to2, tokens);
                    Log.out("listing - WARNING: listing style unknown, using last token as filename!");
                    Log.out("listing - this may work but may also fail, filesizes and permissions will probably fail, too...");
                    Log.out("listing - please send us a feature request with the serveraddress to let us support this listing style :)");
                    Log.out("listing - (backup parser, token " + tokens +
                            " ): " + tmp);
                }

                if (isDir) {
                    tmp = tmp + "/";
                } else if (isLink) {
                    tmp = tmp + "###";
                }

                currentFiles.add(tmp);
                // -------------------------------------------------------------
            }

            String[] files = toArray(currentFiles);

            for (int i = 0; i < files.length; i++) {
                files[i] = parseSymlink(files[i]);

                //System.out.println("> " + files[i]+":"+dateVector.elementAt(i));
                //System.out.println("> " + files.length+":"+dateVector.size());
            }

            return files;
        } catch (Exception ex) {
            Log.debug(ex + " @FtpConnection::sortLs");
            ex.printStackTrace();
        }

        //------- date parser test ------

		/*
        String[] x = (String[]) dateVector.toArray();
        for(int i=0; i<x.length; i++)
        {
                Log.out(":"+x[i]);
        }
		 */

        //-------------------------------
        return new String[0];
    }

    /**
     * get a filename
     */
    private String giveFile(StringTokenizer to, int lev) {

        for (int i = 0; i < lev; i++) {
            if (to.hasMoreTokens()) {
                String t = to.nextToken();

                //Log.out("> "+t);
                if (t.equals(" ") || t.equals("\t")) {
                    // -

					/*
                       while(to.hasMoreTokens())
                       {
                               t2 = to.nextToken();
                           if(!t.equals(" ") && !t.equals("\t")) break;
                       }
					 */
                    i--;
                }
            }
        }

        String tmp = "<error>";

		/*
        if(t2 != null)
        {
                tmp = t2;
        }
		 */
        while (tmp.equals(" ") || tmp.equals("\t") || tmp.equals("<error>")) {
            if (to.hasMoreTokens()) {
                tmp = to.nextToken();
            } else {
                break;
            }
        }

        while (to.hasMoreTokens()) {
            tmp = tmp + to.nextToken();
        }

        //Log.out(">>> "+tmp);
        return tmp;
    }

    /**
     * get a filesize
     */
    private String giveSize(StringTokenizer to, int lev) {
        for (int i = 0; i < lev; i++) {
            if (to.hasMoreTokens()) {
                if (to.nextToken().equals(" ")) {
                    i--;
                }
            }
        }

        while (to.hasMoreTokens()) {
            String tmp = to.nextToken();

            if (!tmp.equals(" ")) {
                try {
                    Integer.parseInt(tmp);
                } catch (NumberFormatException ex) {
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
        if (TESTMODE) {
            return "MVS";
        } else {
            return osType;
        }
    }

    private void setOsType(String os) {
        osType = os.toUpperCase();
    }

    private int getPasvPort(String str) {
        int start = str.lastIndexOf(",");
        String lo = "";
        start++;

        while (start < str.length()) {
            char c = str.charAt(start);

            if (!Character.isDigit(c)) {
                break;
            }

            lo = lo + c;
            start++;
        }

        System.out.println("\n\n\n" + str);

        String hi = "";
        start = str.lastIndexOf(",");
        start--;

        while (true) {
            char c = str.charAt(start);

            if (!Character.isDigit(c)) {
                break;
            }

            hi = c + hi;
            start--;
        }

        //System.out.print(hi+":"+lo+" - ");
        return ((Integer.parseInt(hi) * 256) + Integer.parseInt(lo));
    }

    private int getActivePort() {
        return (getPortA() * 256) + getPortB();
    }

    /**
     * parse a symlink
     */
    private String parseSymlink(String file) {
        if (file.indexOf("->") >= 0) {
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
        if (file.indexOf("->") >= 0) {
            //System.out.print(file+" :-> symlink converted to:");
            file = file.substring(file.indexOf("->") + 2).trim();

            //System.out.println(file);
        }

        return file;
    }

    /**
     * test for symlink
     */
    private boolean isSymlink(String file) {
        return file.indexOf("->") >= 0;
    }

    /**
     * Download a file or directory.
     * Uses multithreading if enabled and does not block.
     *
     * @param file The file to download
     * @return An int-statuscode, see constants defined in this class for details
     */
    public int handleDownload(String file) {
        if (Settings.getEnableMultiThreading()) {
            Log.out("spawning new thread for this download.");

            FtpTransfer t = new FtpTransfer(host, port, getLocalPath(),
                    getCachedPWD(), file, username,
                    password, Transfer.DOWNLOAD,
                    handler, listeners, crlf);
            transfers.add(t);

            return NEW_TRANSFER_SPAWNED;
        } else {
            Log.out("multithreading is completely disabled.");

            return download(file);
        }
    }

    /**
     * Download a file or directory, block until finished.
     *
     * @param file The file to download
     * @return An int returncode
     */
    public int download(String file) {
        //Log.out("ftp download started:" + this);

        int stat;

        if (file.endsWith("/")) {
            shortProgress = true;
            fileCount = 0;
            baseFile = file;
            dataType = DataConnection.GETDIR;

            stat = downloadDir(file);

            //pause(100);
            fireActionFinished(this);
            fireProgressUpdate(baseFile,
                    DataConnection.DFINISHED + ":" + fileCount, -1);
            shortProgress = false;
        } else {
            dataType = DataConnection.GET;

            stat = rawDownload(file);

            if (Settings.enableFtpDelays) {
                try {
                    Thread.sleep(100);
                } catch (Exception ex) {
                }
            }

            fireActionFinished(this);
        }

        if (Settings.enableFtpDelays) {
            try {
                Thread.sleep(400);
            } catch (Exception ex) {
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
        Log.out("ftp stream download started:" + this);
        file = parse(file);

        try {
            int p = 0;
            dataType = DataConnection.GET;
            file = StringUtils.getFile(file);

            String path = getLocalPath() + file;

            //BufferedReader in = jcon.getReader();

            modeStream();
            p = negotiatePort();

            dcon = new DataConnection(this, p, host, path, dataType, false, true);

            while (!dcon.isThere()) {
                pause(10);
            }

            jcon.send(RETR + " " + file);

            return dcon.getInputStream();
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.debug(ex +
                    " @FtpConnection::getDownloadInputStream");

            return null;
        }
    }

    private int rawDownload(String file) {
        file = parse(file);

        //String path = file;
        try {
            int p = 0;

            //if(isRelative(file)) path = getLocalPath() + file;
            file = StringUtils.getFile(file);

            String path = getLocalPath() + file;

            //System.out.println(file + " : " + path);
            //BufferedReader in = jcon.getReader();
            modeStream();

            //binary();
            p = negotiatePort();

            File f = new File(path);

            //System.out.println("path: "+path);
            boolean resume = false;

            if (f.exists() && Settings.enableResuming) {
                jcon.send(REST + " " + f.length());

                if (getLine(PROCEED) != null) {
                    resume = true;
                }
            }

            dcon = new DataConnection(this, p, host, path, dataType, resume); //, new Updater());

            while (!dcon.isThere()) {
                pause(10);
            }

            jcon.send(RETR + " " + file);

            String line = getLine(POSITIVE);
            Log.debug(line);

            // file has been created even if not downloaded, delete it
            if (line.startsWith(NEGATIVE)) {
                File f2 = new File(path);

                if (f2.exists() && (f2.length() == 0)) {
                    f2.delete();
                }

                return PERMISSION_DENIED;
            } else if (!line.startsWith(POSITIVE) && !line.startsWith(PROCEED)) {
                return TRANSFER_FAILED;
            }

            // we need to block since some ftp-servers do not want the
            // refresh command that dirpanel sends otherwise
            while (!dcon.finished) {
                pause(10);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.debug(ex + " @FtpConnection::download");

            return TRANSFER_FAILED;
        }

        return TRANSFER_SUCCESSFUL;
    }

    private int downloadDir(String dir) {
        if (!dir.endsWith("/")) {
            dir = dir + "/";
        }

        if (StringUtils.isRelative(dir)) {
            dir = getCachedPWD() + dir;
        }

        String path = getLocalPath() + StringUtils.getDir(dir);

        //System.out.println("path: "+path);
        String pwd = getCachedPWD() + StringUtils.getDir(dir);

        String oldDir = getLocalPath();
        String oldPwd = getCachedPWD();

        File f = new File(path);

        if (!f.exists()) {
            if (!f.mkdir()) {
                Log.debug("Can't create directory: " + dir);
            } else {
                Log.debug("Created directory...");
            }
        }

        setLocalPath(path);

        if (!chdirNoRefresh(pwd)) {
            return CHDIR_FAILED;
        }

        try {
            list();
        } catch (IOException ex) {
            return PERMISSION_DENIED;
        }

        String[] tmp = sortLs();

        setLocalPath(path);

        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i].endsWith("/")) {
                if (tmp[i].trim().equals("../") || tmp[i].trim().equals("./")) {
                    Log.debug("Skipping " + tmp[i].trim());
                } else {
                    if (!work) {
                        return TRANSFER_STOPPED;
                    }

                    if (downloadDir(tmp[i]) < 0) {
                        // return TRANSFER_FAILED;
                    }
                }
            } else {
                //System.out.println( "file: " + getLocalPath() + tmp[i] + "\n\n");
                if (!work) {
                    return TRANSFER_STOPPED;
                }

                fileCount++;

                if (rawDownload(getLocalPath() + tmp[i]) < 0) {
                    // return TRANSFER_FAILED;
                }
            }
        }

        chdirNoRefresh(oldPwd);
        setLocalPath(oldDir);

        return TRANSFER_SUCCESSFUL;
    }

    /**
     * Upload a file or directory.
     * Uses multithreading if enabled and does not block.
     *
     * @param file The file to upload
     * @return An int-statuscode, NEW_TRANSFER_SPAWNED,TRANSFER_FAILED or TRANSFER_SUCCESSFUL
     */
    public int handleUpload(String file) {
        return handleUpload(file, null);
    }

    /**
     * Upload a file or directory.
     * Uses multithreading if enabled and does not block.
     *
     * @param file     The file to upload
     * @param realName The file to rename the uploaded file to
     * @return An int-statuscode, NEW_TRANSFER_SPAWNED,TRANSFER_FAILED or TRANSFER_SUCCESSFUL
     */
    public int handleUpload(String file, String realName) {
        if (Settings.getEnableMultiThreading() &&
                (!Settings.getNoUploadMultiThreading())) {
            Log.out("spawning new thread for this upload.");

            FtpTransfer t;

            if (realName != null) {
                t = new FtpTransfer(host, port, getLocalPath(), getCachedPWD(),
                        file, username, password, Transfer.UPLOAD,
                        handler, listeners, realName, crlf);
            } else {
                t = new FtpTransfer(host, port, getLocalPath(), getCachedPWD(),
                        file, username, password, Transfer.UPLOAD,
                        handler, listeners, crlf);
            }

            transfers.add(t);

            return NEW_TRANSFER_SPAWNED;
        } else {
            if (Settings.getNoUploadMultiThreading()) {
                Log.out("upload multithreading is disabled.");
            } else {
                Log.out("multithreading is completely disabled.");
            }

            return (realName == null) ? upload(file) : upload(file, realName);
        }
    }

    /**
     * Upload a file or directory, block until finished.
     *
     * @param file The file to upload
     * @return An int returncode
     */
    public int upload(String file) {
        return upload(file, file);
    }

    /**
     * Upload and a file or directory under a given name, block until finished.
     * Note that setting realName does not affect directory transfers
     *
     * @param file     The file to upload
     * @param realName The file to rename the uploaded file to
     * @return An int responsecode
     */
    public int upload(String file, String realName) {
        return upload(file, realName, null);
    }

    /**
     * Upload from an InputStream, block until finished.
     *
     * @param file The file to upload
     * @param in   InputStream to read from
     * @return An int responsecode
     */
    public int upload(String file, InputStream in) {
        return upload(file, file, in);
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
    public int upload(String file, String realName, InputStream in) {
        hasUploaded = true;
        Log.out("ftp upload started: " + this);

        int stat;

        if ((in == null) && new File(file).isDirectory()) {
            shortProgress = true;
            fileCount = 0;
            baseFile = file;
            dataType = DataConnection.PUTDIR;
            isDirUpload = true;

            stat = uploadDir(file);

            shortProgress = false;

            //System.out.println(fileCount + ":" + baseFile);
            fireProgressUpdate(baseFile,
                    DataConnection.DFINISHED + ":" + fileCount, -1);

            fireActionFinished(this);
            fireDirectoryUpdate(this);
        } else {
            dataType = DataConnection.PUT;
            stat = rawUpload(file, realName, in);

            try {
                Thread.sleep(100);
            } catch (Exception ex) {
            }

            fireActionFinished(this);
            fireDirectoryUpdate(this);
        }

        try {
            Thread.sleep(500);
        } catch (Exception ex) {
        }

        return stat;
    }

    private int rawUpload(String file) {
        return rawUpload(file, file);
    }

    private int rawUpload(String file, String realName) {
        return rawUpload(file, realName, null);
    }

    /**
     * uploads a file
     */
    private int rawUpload(String file, String realName, InputStream in) {
        if (!file.equals(realName)) {
            Log.debug("File: " + file + ", stored as " + realName);
        } else {
            Log.debug("File: " + file);
            realName = null;
        }

        file = parse(file);

        String path = file;

        try {
            int p = 0;

            if (StringUtils.isRelative(file)) {
                path = getLocalPath() + file;
            }

            file = StringUtils.getFile(file);

            //BufferedReader in = jcon.getReader();
            boolean resume = false;
            String size = "0";

            if (Settings.enableUploadResuming && (in == null)) {
                list();

                String[] ls = sortLs();
                String[] sizes = sortSize();

                if ((ls == null) || (sizes == null)) {
                    Log.out(">>> ls out of sync (skipping resume check)");
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

                    File f = new File(path);

                    if (f.exists() && (f.length() <= Integer.parseInt(size))) {
                        Log.out("skipping resuming, file is <= filelength");
                        resume = false;
                    } else if (f.exists() && Integer.parseInt(size) > 0) {
                        if (!Settings.noUploadResumingQuestion) {
                            if (JOptionPane.showConfirmDialog(new JLabel(), "A file smaller than the one to be uploaded already exists on the server,\n do you want to resume the upload?", "Resume upload?", JOptionPane.YES_NO_OPTION)
                                    != JOptionPane.OK_OPTION) {
                                resume = false;
                            }
                        }
                        Log.out("resume: " + resume + ", size: " + size);
                    } else {
                        Log.out("resume: " + resume + ", size: " + size);
                    }
                }
            }

            modeStream();

            //binary();
            p = negotiatePort();

            if (resume && Settings.enableUploadResuming) {
                jcon.send(REST + " " + size);

                if (getLine(PROCEED) == null) {
                    resume = false;
                }
            }

            dcon = new DataConnection(this, p, host, path, dataType, resume,
                    Integer.parseInt(size), in); //, new Updater());

            while (!dcon.isThere()) {
                pause(10);
            }

            //System.out.println(path + " : " + file);
            if (realName != null) {
                jcon.send(STOR + " " + realName);
            } else {
                jcon.send(STOR + " " + file);
            }

            String tmp = getLine(POSITIVE);

            if (!tmp.startsWith(POSITIVE) && !tmp.startsWith(PROCEED)) {
                return TRANSFER_FAILED;
            }

            Log.debug(tmp);

            // we need to block since some ftp-servers do not want the
            // refresh command that dirpanel sends otherwise
            while (!dcon.finished) {
                pause(10);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.debug(ex + " @FtpConnection::upload");

            return TRANSFER_FAILED;
        }

        return TRANSFER_SUCCESSFUL;
    }

    private int uploadDir(String dir) {
        //System.out.println("up");
        if (dir.endsWith("\\")) {
            System.out.println("Something's wrong with the selected directory - please report this bug!");
        }

        if (!dir.endsWith("/")) {
            dir = dir + "/";
        }

        if (StringUtils.isRelative(dir)) {
            dir = getLocalPath() + dir;
        }

        String single = StringUtils.getDir(dir);
        //String path = dir.substring(0, dir.indexOf(single));

        String remoteDir = getCachedPWD() + single; //StringUtils.removeStart(dir,path);
        String oldDir = getCachedPWD();

        if (Settings.safeMode) {
            noop();
        }

        boolean successful = mkdir(remoteDir);

        if (!successful) {
            return MKDIR_FAILED;
        }

        if (Settings.safeMode) {
            noop();
        }

        chdirNoRefresh(remoteDir);

        File f2 = new File(dir);
        String[] tmp = f2.list();

        for (int i = 0; i < tmp.length; i++) {
            String res = dir + tmp[i];
            File f3 = new File(res);

            if (f3.isDirectory()) {
                if (!work) {
                    return TRANSFER_STOPPED;
                }

                uploadDir(res);
            } else {
                //System.out.println(">>>\nlp: "+path+single + "\nrp: ");
                //System.out.println(remoteDir +"\nres: "+res);
                if (!work) {
                    return TRANSFER_STOPPED;
                }

                fileCount++;

                if (rawUpload(res) < 0) {
                    //return TRANSFER_STOPPED;
                }
            }
        }

        chdirNoRefresh(oldDir);

        return TRANSFER_SUCCESSFUL;
    }

    private String parse(String file) {
        file = parseSymlink(file);

        return file;
    }

    /**
     * recursive delete remote directory
     */
    private int cleanDir(String dir, String path) {
        if (dir.equals("")) {
            return 0;
        }

        if (!dir.endsWith("/")) {
            dir = dir + "/";
        }

        String remoteDir = StringUtils.removeStart(dir, path);

        String oldDir = pwd;
        chdirNoRefresh(pwd + remoteDir);

        try {
            list();
        } catch (IOException ex) {
            // probably we don't have permission to ls here
            return PERMISSION_DENIED;
        }

        String[] tmp = sortLs();
        chdirNoRefresh(oldDir);

        if (tmp == null) {
            return GENERIC_FAILED;
        }

        for (int i = 0; i < tmp.length; i++) {
            Log.out("cleanDir: " + tmp);

            if (isSymlink(tmp[i])) {
                Log.debug("WARNING: Skipping symlink, remove failed.");
                Log.debug("This is necessary to prevent possible data loss when removing those symlinks.");

                tmp[i] = null;
            }

            if (tmp[i] != null) {
                tmp[i] = parseSymlink(tmp[i]);
            }

            if ((tmp[i] == null) || tmp[i].equals("./") || tmp[i].equals("../")) {
                //System.out.println(tmp[i]+"\n\n\n");
                continue;
            }

            //System.out.println(tmp[i]);
            //pause(500);
            if (tmp[i].endsWith("/")) {
                //   System.out.println(dir);
                cleanDir(dir + tmp[i], path);

                int x = removeFileOrDir(dir + tmp[i]);

                if (x < 0) {
                    return x;
                }
            } else {
                //   System.out.println(dir+tmp[i]);
                int x = removeFileOrDir(dir + tmp[i]);

                if (x < 0) {
                    return x;
                }
            }
        }

        return REMOVE_SUCCESSFUL;
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
            Log.debug("ERROR: Catching attempt to delete . or .. directories");

            return GENERIC_FAILED;
        }

        if (isSymlink(file)) {
            Log.debug("WARNING: Skipping symlink, remove failed.");
            Log.debug("This is necessary to prevent possible data loss when removing those symlinks.");

            return REMOVE_FAILED;
        }

        file = parseSymlink(file);

        if (file.endsWith("/")) {
            int ret = cleanDir(file, getCachedPWD());

            if (ret < 0) {
                return ret;
            }

            jcon.send(RMD + " " + file);
        } else {
            jcon.send(DELE + " " + file);
        }

        if (success(POSITIVE))//FTP250_COMPLETED))
        {
            return REMOVE_SUCCESSFUL;
        } else {
            return REMOVE_FAILED;
        }
    }

    /**
     * Disconnect from the server.
     * The connection is marked ad closed even if the server does not respond correctly -
     * if it fails for any reason the connection should just time out
     */
    public void disconnect() {
        jcon.send(QUIT);
        getLine(POSITIVE);//FTP221_SERVICE_CLOSING);
        connected = false;
    }

    /**
     * Execute a remote command.
     * Sends noops before and after the command
     *
     * @param cmd The raw command that is to be sent to the server
     */
    public void sendRawCommand(String cmd) {
        noop();
        Log.clearCache();
        jcon.send(cmd);
        noop();
    }

    /**
     * Reads the response until line found or error.
     * (Used internally)
     *
     * @param until The String the response line hast to start with, 2 for succesful return codes for example
     */
    public String getLine(String until) {
        return getLine(new String[]{until});
    }

    /**
     * Reads the response until line found or error.
     * (Used internally)
     */
    public String getLine(String[] until) {
        BufferedReader in = null;

        try {
            in = jcon.getReader();
        } catch (Exception ex) {
            Log.debug(ex + " @FtpConnection::getLine");
        }

        //String resultString = null;
        String tmp;

        while (true) {
            try {
                tmp = in.readLine();

                if (tmp == null) {
                    break;
                }

                Log.debug(tmp);

                if (((tmp.startsWith(NEGATIVE) || (tmp.startsWith(NEGATIVE2))) &&
                        (tmp.charAt(3) != MORE_LINES_APPENDED)) ||
                        tmp.startsWith(PROCEED)) {
                    return tmp;
                } else {
                    for (int i = 0; i < until.length; i++) {
                        if (tmp.startsWith(until[i])) {
                            //if(resultString == null) resultString = tmp;
                            //else resultString = resultString + "\n" + tmp;
                            if (tmp.charAt(3) != MORE_LINES_APPENDED) {
                                return tmp;
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                Log.debug(ex + " @FtpConnection::getLine");

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
            updatePWD();
        }

        if (TESTMODE) {
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
        jcon.send(PWD);

        String tmp = getLine(POSITIVE);//FTP257_PATH_CREATED);

        if (tmp == null) {
            return;
        }

        if (TESTMODE) {
            tmp = "\"'T759F.'\"";
        }

        String x1 = tmp;

        if (x1.indexOf("\"") >= 0) {
            x1 = tmp.substring(tmp.indexOf("\"") + 1);
            x1 = x1.substring(0, x1.indexOf("\""));
        }

        if (getOsType().indexOf("MVS") >= 0) {
            //if(x1.indexOf("'") == 0) {
            //        String x2 = x1.substring(1, x1.lastIndexOf("'"));
            //        x1 = x2;
            //}
            Log.out("pwd: " + x1);
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
    public boolean chdirRaw(String dirName) {
        jcon.send(CWD + " " + dirName);

        return success(POSITIVE);//FTP250_COMPLETED);
    }

    private boolean success(String op) {
        String tmp = getLine(op);

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
        jcon.send(CDUP);

        return success(POSITIVE);//FTP200_OK);
    }

    /**
     * Create a directory with the given name.
     *
     * @param dirName The name of the directory to create
     * @return True if successful, false otherwise
     */
    public boolean mkdir(String dirName) {
        jcon.send(MKD + " " + dirName);

        boolean ret = success(POSITIVE); // Filezille server bugfix, was: FTP257_PATH_CREATED);
        Log.out("mkdir(" + dirName + ")  returned: " + ret);

        //*** Added 03/20/2005
        fireDirectoryUpdate(this);

        //***
        return ret;
    }

    private int negotiatePort() throws IOException {
        String tmp = "";

        if (Settings.getFtpPasvMode()) {
            jcon.send(PASV);

            tmp = getLine(FTP227_ENTERING_PASSIVE_MODE);

            if ((tmp != null) && !tmp.startsWith(NEGATIVE)) {
                return getPasvPort(tmp);
            }
        }

        tmp = getActivePortCmd();
        jcon.send(tmp);
        getLine(FTP200_OK);

        return getActivePort();
    }

    /**
     * List remote directory.
     * Note that you have to get the output using the
     * sort*-methods.
     *
     * @param outfile The file to save the output to, usually Settings.ls_out
     */
    public void list() throws IOException {
        String oldType = "";

        try {
            //BufferedReader in = jcon.getReader();

            int p = 0;

            modeStream();

            oldType = getTypeNow();
            ascii();

            p = negotiatePort();
            dcon = new DataConnection(this, p, host, null, DataConnection.GET, false, true); //,null);

            while (dcon.getInputStream() == null) {
                //System.out.print("#");
                pause(10);
            }
            BufferedReader input = new BufferedReader(new InputStreamReader(dcon.getInputStream()));

            jcon.send(LIST);

            String line;
            currentListing.removeAllElements();

            while ((line = input.readLine()) != null) {
                //System.out.println("-> "+line);
                if (!line.trim().equals("")) {
                    currentListing.add(line);
                }
            }

            getLine(POSITIVE); //FTP226_CLOSING_DATA_REQUEST_SUCCESSFUL);
            input.close();

            if (!oldType.equals(ASCII)) {
                type(oldType);
            }
        } catch (Exception ex) {
            Log.debug("Cannot list remote directory!");

            if (!oldType.equals(ASCII)) {
                type(oldType);
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
    public boolean chdir(String p) {
        //Log.out("Change directory to: "+p);
        //String tmpPath = p;

        boolean tmp = chdirWork(p);

        if (!tmp) {
            return false;
        } else {
            fireDirectoryUpdate(this);

            return true;
        }
    }

    /**
     * Parses directory and does a chdir(), but does not send an update signal
     *
     * @return True is successful, false otherwise
     */
    public boolean chdirNoRefresh(String p) {
        return chdirWork(p);
    }

    private boolean chdirWork(String p) {
        if (Settings.safeMode) {
            noop();
        }

        p = parseSymlinkBack(p);

        if (getOsType().indexOf("OS/2") >= 0) {
            return chdirRaw(p);
        } else if (getOsType().indexOf("MVS") >= 0) {
            boolean cdup = false;

            System.out.print("MVS parser: " + p + " -> ");

            //TODO: handle relative unix paths
            if (!getPWD().startsWith("/")) //return chdirRaw(p);
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
                Log.out("MVS parser: " + p);

                return chdirRaw(p);
            }
        }

        try {
            // check if we don't have a relative path
            if (!p.startsWith("/") && !p.startsWith("~")) {
                p = pwd + p;
            }

            // argh, we should document that!
            if (p.endsWith("..")) {
                boolean home = p.startsWith("~");
                StringTokenizer stok = new StringTokenizer(p, "/");

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
            if (!chdirRaw(p)) {
                return false;
            }
        } catch (Exception ex) {
            Log.debug("(remote) Can not get pathname! " + pwd + ":" + p);
            ex.printStackTrace();

            return false;
        }

        //fireDirectoryUpdate(this);

        // --------------------------------------
        updatePWD();


        return true;
    }

    /**
     * Waits a specified amount of time
     *
     * @param time The time to sleep in milliseconds
     */
    public void pause(int time) {
        try {
            Thread.sleep(time);
        } catch (Exception ex) {
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
    public boolean setLocalPath(String newPath) {
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
    public int exists(String file) {
        String dir = null;
        String tmpPWD = getCachedPWD();

        if (file.indexOf("/") >= 0) {
            dir = file.substring(0, file.lastIndexOf("/") + 1);
            Log.out("checking dir: " + dir);

            if (!chdir(dir)) {
                return CHDIR_FAILED;
            }
        }

        try {
            list();
        } catch (IOException ex) {
            ex.printStackTrace();

            return PERMISSION_DENIED;
        }

        String f = file.substring(file.lastIndexOf("/") + 1);
        Log.out("checking file: " + f);

        String[] files = sortLs();
        int[] perms = getPermissions();

        int y = -1;

        for (int x = 0; x < files.length; x++) {
            if (files[x].equals(f)) {
                y = x;

                break;
            }
        }

        if (y == -1) {
            return FILE_NOT_FOUND;
        }

        if (dir != null) {
            chdir(tmpPWD);
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
    public boolean rename(String from, String to) {
        jcon.send("RNFR " + from);

        if (success(RC350)) {
            jcon.send("RNTO " + to);

            //FTP250_COMPLETED))
            return success(POSITIVE);
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
        return porta;
    }

    private int getPortB() {
        return portb;
    }

	/*
    private void incrementPort()
    {
        portb++;
    }
	 */

	/*
    private String getActivePortCmd() throws UnknownHostException, IOException
    {
        InetAddress ipaddr = jcon.getLocalAddress();
        String ip = ipaddr.getHostAddress().replace('.', ',');

        incrementPort();

        int a = getPortA();
        int b = getPortB();
        String ret = "PORT " + ip + "," + a + "," + b;

        //System.out.println(ret);
        return ret;
    }
	 */

    private String getActivePortCmd() throws IOException {
        InetAddress ipaddr = jcon.getLocalAddress();
        String ip = ipaddr.getHostAddress().replace('.', ',');

        ServerSocket aSock = new ServerSocket(0);
        int availPort = aSock.getLocalPort();
        aSock.close();
        porta = availPort / 256;
        portb = availPort % 256;
        String ret = "PORT " + ip + "," + porta + "," + portb;

        //    System.out.println(ret);
        return ret;
    }

    /**
     * Tell server we want binary data connections.
     */
    public void binary() { // possible responses 200, 500, 501, 504, 421 and 530
        type(BINARY);
    }

    /**
     * Tell server we want ascii data connections.
     */
    public void ascii() {
        type(ASCII);
    }

    /**
     * Set type (L8,I,A for example).
     *
     * @return True if the type was changed successfully, false otherwise
     */
    public boolean type(String code) {
        jcon.send(TYPE + " " + code);

        String tmp = getLine(POSITIVE);//FTP200_OK);

        if ((tmp != null) && tmp.startsWith(POSITIVE))//FTP200_OK))
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
        jcon.send(NOOP);
        getLine(POSITIVE);//FTP200_OK);
    }

    /**
     * Try to abort the transfer.
     */
    public void abort() {
        jcon.send(ABOR);
        getLine(POSITIVE); // 226
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
        jcon.send(SYST);

        String response = getLine(POSITIVE);//FTP215_SYSTEM_TYPE);

        if (response != null) {
            //StringTokenizer st = new StringTokenizer(response);
            //if (st.countTokens() >= 2)
            //{
            //     st.nextToken();
            //     String os = st.nextToken();
            //     setOsType(os);
            //}
            setOsType(response);
        } else {
            setOsType("UNIX");
        }

        return response;
    }

    /**
     * Set mode to Stream.
     * Used internally.
     */
    public void modeStream() {
        if (useStream && !modeStreamSet) {
            String ret = mode(STREAM);

            if ((ret != null) && ret.startsWith(NEGATIVE)) {
                useStream = false;
            } else {
                modeStreamSet = true;
            }
        }
    }

    /**
     * Unsupported at this time.
     */
    public void modeBlocked() {
        if (useBlocked) {
            if (mode(BLOCKED).startsWith(NEGATIVE)) {
                useBlocked = false;
            }
        }
    }

    /*
     * Unsupported at this time.
     */
    public void modeCompressed() {
        if (useCompressed) {
            if (mode(COMPRESSED).startsWith(NEGATIVE)) {
                useCompressed = false;
            }
        }
    }

    /**
     * Set mode manually.
     *
     * @param code The mode code wanted, I, A, L8 for example
     * @return The server's response to the request
     */
    public String mode(String code) {
        jcon.send(MODE + " " + code);

        String ret = "";

        try {
            ret = getLine(POSITIVE);//FTP200_OK);
        } catch (Exception ex) {
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
    public void addConnectionListener(ConnectionListener l) {
        listeners.add(l);
    }

    /**
     * Add a Vector of ConnectionListeners.
     * Each Listener is notified about transfers and the connection state.
     *
     * @param l A Vector containig ConnectionListener objects
     */
    public void setConnectionListeners(Vector<ConnectionListener> l) {
        listeners = l;
    }

    /**
     * Remote directory has changed.
     *
     * @param con The FtpConnection calling the event
     */
    public void fireDirectoryUpdate(FtpConnection con) {
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
    public void fireProgressUpdate(String file, String type, long bytes) {
        //System.out.println(listener);
        if (listeners == null) {
        } else {
            for (int i = 0; i < listeners.size(); i++) {
                ConnectionListener listener = listeners.elementAt(i);

                if (shortProgress && Settings.shortProgress) {
                    if (type.startsWith(DataConnection.DFINISHED)) {
                        listener.updateProgress(baseFile,
                                DataConnection.DFINISHED + ":" +
                                        fileCount, bytes);
                    } else if (isDirUpload) {
                        listener.updateProgress(baseFile,
                                DataConnection.PUTDIR + ":" +
                                        fileCount, bytes);
                    } else {
                        listener.updateProgress(baseFile,
                                DataConnection.GETDIR + ":" +
                                        fileCount, bytes);
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
    public void fireConnectionInitialized(FtpConnection con) {
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
    public void fireConnectionFailed(FtpConnection con, String why) {
        if (listeners == null) {
        } else {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.elementAt(i).connectionFailed(con,
                        why);
            }
        }
    }

    /**
     * Transfer is done
     *
     * @param con The FtpConnection calling the event
     */
    public void fireActionFinished(FtpConnection con) {
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
    public void setConnectionHandler(ConnectionHandler h) {
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
        for (FtpTransfer transfer : transfers) {
            try {
                DataConnection dcon = transfer.getDataConnection();

                if (dcon == null || dcon.sock.isClosed()) {
                    Log.out("nothing to abort");

                    continue;
                }

                dcon.getCon().work = false;
                dcon.sock.close();
            } catch (Exception ex) {
                Log.debug("Exception during abort: " + ex);
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

    public void setIn(BufferedReader in) {
        this.in = in;
    }

    public BufferedReader getCommandInputReader() {
        return jcon.getIn();
    }

    public OutputStream getCommandOutputStream() {
        return jcon.getOut();
    }

}
