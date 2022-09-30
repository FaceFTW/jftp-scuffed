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
	public static final int port = 21;
	public static final int dataPort = 20;
	private static ArrayList commands = null;

	static {
		net.sf.jftp.net.server.FtpServerSocket.commands = new ArrayList();
		net.sf.jftp.net.server.FtpServerSocket.commands.add("auth");
		net.sf.jftp.net.server.FtpServerSocket.commands.add("cdup");
		net.sf.jftp.net.server.FtpServerSocket.commands.add("cwd");
		net.sf.jftp.net.server.FtpServerSocket.commands.add("feat");
		net.sf.jftp.net.server.FtpServerSocket.commands.add("help");
		net.sf.jftp.net.server.FtpServerSocket.commands.add("lang");
		net.sf.jftp.net.server.FtpServerSocket.commands.add("list");
		net.sf.jftp.net.server.FtpServerSocket.commands.add("mkd");
		net.sf.jftp.net.server.FtpServerSocket.commands.add("mode");
		net.sf.jftp.net.server.FtpServerSocket.commands.add("motd");
		net.sf.jftp.net.server.FtpServerSocket.commands.add("nlst");
		net.sf.jftp.net.server.FtpServerSocket.commands.add("noop");
		net.sf.jftp.net.server.FtpServerSocket.commands.add("opts");
		net.sf.jftp.net.server.FtpServerSocket.commands.add("pass");
		net.sf.jftp.net.server.FtpServerSocket.commands.add("pasv");
		net.sf.jftp.net.server.FtpServerSocket.commands.add("port");
		net.sf.jftp.net.server.FtpServerSocket.commands.add("pwd");
		net.sf.jftp.net.server.FtpServerSocket.commands.add("quit");
		net.sf.jftp.net.server.FtpServerSocket.commands.add("rein");
		net.sf.jftp.net.server.FtpServerSocket.commands.add("smnt");
		net.sf.jftp.net.server.FtpServerSocket.commands.add("stat");
		net.sf.jftp.net.server.FtpServerSocket.commands.add("stru");
		net.sf.jftp.net.server.FtpServerSocket.commands.add("syst");
		net.sf.jftp.net.server.FtpServerSocket.commands.add("type");
		net.sf.jftp.net.server.FtpServerSocket.commands.add("user");
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
	private final ServerSocket pasvSocket = null;
	private int activePort = 0;
	private final String rootDir = null;
	private final String currentDir = null;

	public FtpServerSocket(final Socket s) throws IOException {
		this.socket = s;

		try {
			this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			this.out = new PrintWriter(this.socket.getOutputStream(), true);
		} catch (final IOException ioe) {
			throw ioe;
		}

		final Method[] m = this.getClass().getDeclaredMethods();
		String methodName = null;

		for (final java.lang.reflect.Method method : m) {
			methodName = method.getName();

			if (net.sf.jftp.net.server.FtpServerSocket.commands.contains(methodName)) {
				this.methods.put(methodName, method);
			}
		}

		this.start();
	}

	public static void main(final String[] args) {
	}

	private void send(final String bundleId) {
		this.out.print(this.bundle.getString(bundleId));
		this.out.flush();
	}

	private void send(final String bundleId, final Object[] args) {
		final MessageFormat fmt = new MessageFormat(this.bundle.getString(bundleId));
		this.out.print(fmt.format(args));
		this.out.flush();
	}

	public void motd() {
		this.send("220motd");
	}

	public void user(final String line) {
		this.send("331user");
	}

	public void pass(final String line) {
		this.send("230pass");
	}

	public void type(final String line) { // needs work A, E, I, L, C, T, N

		final Object[] args = {"I"};
		this.send("200type", args);
	}

	public void mode(final String line) { // check for S, B, or C

		final Object[] args = {line.toUpperCase()};
		this.send("200ok", args);
	}

	public void pwd(final String line) {
		final Object[] args = {this.currentDir};
		this.send("257pwd", args);
	}

	public void cdup(final String line) {
		final Object[] args = {"CDUP"};
		final File tmp = this.directory.getParentFile();

		if (null != tmp) {
			this.directory = tmp;
		}

		this.send("200", args);
	}


	private String addTrailingSlash(final String s) {
		if (s.endsWith("/")) {
			return s;
		} else {
			return s + "/";
		}
	}

	private String removeTrailingSlash(final String s) {
		if (s.endsWith("/")) {
			return s.substring(0, s.length() - 1);
		} else {
			return s;
		}
	}


	public void list(final String line) {
		this.send("150list");

		final boolean passive = false;
		if (!passive) {
			try {
				final Socket activeSocket = new Socket(this.socket.getInetAddress(), this.activePort);
				final PrintStream ps = new PrintStream(activeSocket.getOutputStream());
				ps.print("bleah\r\n");
				ps.print("bleah\r\n");
				ps.print("bleah\r\n");
				ps.print("bleah\r\n");
				ps.print("bleah\r\n");
				ps.flush();
				ps.close();
				this.send("226");
			} catch (final Exception e) {
			}
		}
	}


	public void port(final String line) {
		int start = line.lastIndexOf(",");
		int end = line.length();
		String lo = null; // lo-ordered byte

		if ((-1 != start) && (-1 != end)) {
			lo = line.substring(start + 1, end);
		}

		end = start;
		start = line.lastIndexOf(",", start - 1);

		String hi = null; // hi-ordered byte

		if ((-1 != start) && (-1 != end)) {
			hi = line.substring(start + 1, end);
		}

		this.activePort = ((Integer.parseInt(hi) * 256) + Integer.parseInt(lo));

		final Object[] args = {"PORT"};
		this.send("200", args);
	}

	public void run() {
		try {
			String line = null;
			this.motd();

			while (null != (line = this.in.readLine())) {
				System.out.println(line);

				int index = line.indexOf(' ');

				if (-1 == index) {
					index = line.length();
				}

				final String command = line.substring(0, index).toLowerCase();
				final Method o = (Method) this.methods.get(command);

				if (null != o) {
					try {
						o.invoke(this, line);
					} catch (final Exception e) {
					}
				} else {
					this.send("500");
				}
			}
		} catch (final IOException ioe) {
			net.sf.jftp.system.logging.Log.debug("Socket error: " + ioe);
		}
	}
}
