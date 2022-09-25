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
package net.sf.jftp.net.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 *
 */
public class FtpServerSocket extends Thread {
	public final static int port = 21;
	public final static int dataPort = 20;
	private static ArrayList commands = null;

	static {
		commands = new ArrayList();
		commands.add("auth");
		commands.add("cdup");
		commands.add("cwd");
		commands.add("feat");
		commands.add("help");
		commands.add("lang");
		commands.add("list");
		commands.add("mkd");
		commands.add("mode");
		commands.add("motd");
		commands.add("nlst");
		commands.add("noop");
		commands.add("opts");
		commands.add("pass");
		commands.add("pasv");
		commands.add("port");
		commands.add("pwd");
		commands.add("quit");
		commands.add("rein");
		commands.add("smnt");
		commands.add("stat");
		commands.add("stru");
		commands.add("syst");
		commands.add("type");
		commands.add("user");
	}

	private final Hashtable methods = new Hashtable();
	private final ResourceBundle bundle = ResourceBundle.getBundle("responses", Locale.US);
	private final String structure = "file";
	private final String transferMode = "stream";
	private final String type = "ascii";
	private Socket socket = null;
	private BufferedReader in = null;
	private PrintWriter out = null;
	private File directory = new File(".");
	private ServerSocket pasvSocket = null;
	private boolean passive = false;
	private int activePort = 0;
	private String rootDir = null;
	private String currentDir = null;

	public FtpServerSocket(Socket s) throws IOException {
		socket = s;

		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException ioe) {
			throw ioe;
		}

		Method[] m = this.getClass().getDeclaredMethods();
		String methodName = null;

		for (java.lang.reflect.Method method : m) {
			methodName = method.getName();

			if (commands.contains(methodName)) {
				methods.put(methodName, method);
			}
		}

		this.start();
	}

	public static void main(String[] args) {
	}

	private void send(String bundleId) {
		out.print(bundle.getString(bundleId));
		out.flush();
	}

	private void send(String bundleId, Object[] args) {
		MessageFormat fmt = new MessageFormat(bundle.getString(bundleId));
		out.print(fmt.format(args));
		out.flush();
	}

	public void motd() {
		send("220motd");
	}

	public void user(String line) {
		send("331user");
	}

	public void pass(String line) {
		send("230pass");
	}

	public void syst(String line) {
		send("215syst");
	}

	public void type(String line) { // needs work A, E, I, L, C, T, N

		Object[] args = {"I"};
		send("200type", args);
	}

	public void stru(String line) { // needs work - check for F, R, or P

		Object[] args = {line.toUpperCase()};
		send("200ok", args);
	}

	public void mode(String line) { // check for S, B, or C

		Object[] args = {line.toUpperCase()};
		send("200ok", args);
	}

	public void rein(String line) {
		Object[] args = {"REIN"};
		send("502", args);
	}

	public void smnt(String line) {
		Object[] args = {"SMNT"};
		send("502", args);
	}

	public void quit(String line) {
		out.print("221-You have transferred 0 bytes in 0 files.\r\n");
		out.print("221-Total traffic for this session was 0 bytes in 0 transfers.\r\n");
		out.print("221 Thank you for using the FTP service on pikachu.\r\n");
		out.flush();

		try {
			socket.close();
		} catch (Exception e) {
		}
	}

	public void pwd(String line) {
		Object[] args = {currentDir};
		send("257pwd", args);
	}

	public void cwd(String line) {
		String arg = line.substring(4);
		String tmpDir;

		if (arg.startsWith("/")) {
			tmpDir = removeTrailingSlash(rootDir) + arg;
		} else {
			tmpDir = addTrailingSlash(currentDir) + arg;
		}

		File tmp = new File(tmpDir);

		if (tmp.exists() && tmp.isDirectory()) {
			currentDir = tmpDir;
			send("250cwd");
		} else {
			out.print("550 " + arg + ": No such file or directory.\r\n");
			out.flush();
		}
	}

	public void cdup(String line) {
		Object[] args = {"CDUP"};
		File tmp = directory.getParentFile();

		if (tmp != null) {
			directory = tmp;
		}

		send("200", args);
	}

	public void noop(String line) {
		Object[] args = {"NOOP"};
		send("200", args);
	}

	public void help(String line) {
		out.print("214-The following commands are recognized.\r\n");

        /*
           USER    PORT    STOR    RNTO    NLST    MKD     CDUP
           PASS    PASV    APPE    ABOR    SITE    XMKD    XCUP
           TYPE    DELE    SYST    RMD     STOU
           STRU    ALLO    CWD     STAT    XRMD    SIZE
           MODE    REST    XCWD    HELP    PWD     MDTM
           QUIT    RETR    RNFR    LIST    NOOP    XPWD
        */
		out.print("214 Direct comments to root@localhost.\r\n");
		out.flush();
	}

	public void stat(String line) {
		out.print("211-FTP server status:\r\n");

        /*
             Version wu-2.6.1-16
             Connected to pikachu (127.0.0.1)
             Logged in as gary
             TYPE: ASCII, FORM: Nonprint; STRUcture: File; transfer MODE: Stream
             No data connection
             0 data bytes received in 0 files
             0 data bytes transmitted in 0 files
             0 data bytes total in 0 files
             82 traffic bytes received in 0 transfers
             2502 traffic bytes transmitted in 1 transfers
             2634 traffic bytes total in 1 transfers
             Disk quota: 0 disk blocks in use, 0 quota, 0 limit - time left
             Inode quota: 0 inodes in use, 0 quota, 0 limit - time left
        */
		out.print("211 End of status\r\n");
		out.flush();
	}

	private String addTrailingSlash(String s) {
		if (s.endsWith("/")) {
			return s;
		} else {
			return s + "/";
		}
	}

	private String removeTrailingSlash(String s) {
		if (s.endsWith("/")) {
			return s.substring(0, s.length() - 1);
		} else {
			return s;
		}
	}

	public void mkd(String line) {
		String arg = line.substring(4);

		if (arg.startsWith("/")) {
			arg = removeTrailingSlash(rootDir) + arg;
		} else {
			arg = addTrailingSlash(currentDir) + arg;
		}

		File f = new File(arg);
		MessageFormat fmt = null;
		Object[] args = {f.getAbsolutePath()};

		if (f.exists()) {
			send("521mkd", args);
		} else if (f.mkdirs()) {
			send("257mkd", args);
		} else {
			send("550mkd", args);
		}
	}

	public void feat(String line) {
		out.print("211-Extensions supported\r\n");
		out.print(" LANG EN*;FR\r\n");
		out.print("211 END\r\n");
		out.flush();
	}

	public void pasv(String line) {
		InetAddress address = socket.getLocalAddress();
		String pasvAddress = address.getHostAddress().replace('.', ',');

		try {
			pasvSocket = new ServerSocket(0, 5, address);
			passive = true;
		} catch (IOException ioe) {
		}

		int pasvPort = pasvSocket.getLocalPort();
		Object[] args = {pasvAddress + "," + (pasvPort / 256) + "," + (pasvPort % 256)};
		send("227pasv", args);
	}

	public void list(String line) {
		send("150list");

		if (!passive) {
			try {
				Socket activeSocket = new Socket(socket.getInetAddress(), activePort);
				PrintStream ps = new PrintStream(activeSocket.getOutputStream());
				ps.print("bleah\r\n");
				ps.print("bleah\r\n");
				ps.print("bleah\r\n");
				ps.print("bleah\r\n");
				ps.print("bleah\r\n");
				ps.flush();
				ps.close();
				send("226");
			} catch (Exception e) {
			}
		}
	}

	public void nlst(String line) {
		send("150nlst");

		if (!passive) {
			try {
				Socket activeSocket = new Socket(socket.getInetAddress(), activePort);
				PrintStream ps = new PrintStream(activeSocket.getOutputStream());
				ps.print("bleah\r\n");
				ps.print("bleah\r\n");
				ps.print("bleah\r\n");
				ps.print("bleah\r\n");
				ps.print("bleah\r\n");
				ps.flush();
				ps.close();
				send("225");
			} catch (Exception e) {
			}
		}
	}

	public void port(String line) {
		int start = line.lastIndexOf(",");
		int end = line.length();
		String lo = null; // lo-ordered byte

		if ((start != -1) && (end != -1)) {
			lo = line.substring(start + 1, end);
		}

		end = start;
		start = line.lastIndexOf(",", start - 1);

		String hi = null; // hi-ordered byte

		if ((start != -1) && (end != -1)) {
			hi = line.substring(start + 1, end);
		}

		activePort = ((Integer.parseInt(hi) * 256) + Integer.parseInt(lo));

		Object[] args = {"PORT"};
		send("200", args);
	}

	public void opts(String line) {
		// return 200, 451 or 501
	}

	public void lang(String line) {
		out.print("200 Responses changed to english\r\n");
		out.flush();
	}

	public void auth(String line) {
	}

	public void setRoot(String line) {
		if (new File(line).exists()) {
			rootDir = line;
		} else {
			System.err.println("invalid root directory");
		}
	}

	public void run() {
		try {
			String line = null;
			motd();

			while ((line = in.readLine()) != null) {
				System.out.println(line);

				int index = line.indexOf(' ');

				if (index == -1) {
					index = line.length();
				}

				String command = line.substring(0, index).toLowerCase();
				Method o = (Method) methods.get(command);

				if (o != null) {
					try {
						o.invoke(this, line);
					} catch (Exception e) {
					}
				} else {
					send("500");
				}
			}
		} catch (IOException ioe) {
			net.sf.jftp.system.logging.Log.debug("Socket error: " + ioe);
		}
	}
}
