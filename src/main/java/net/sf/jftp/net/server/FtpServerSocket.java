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
import java.net.ServerSocket;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 *
 */
public class FtpServerSocket extends Thread {
	public static final int port = 21;
	public static final int dataPort = 20;
	private static ArrayList commands;

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

	private final java.util.Map<String, Method> methods = new java.util.HashMap<>();
	private final ResourceBundle bundle = ResourceBundle.getBundle("responses", Locale.US);
	private final String structure = "file";
	private final String transferMode = "stream";
	private final String type = "ascii";
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private File directory = new File(".");
	private final ServerSocket pasvSocket = null;
	private int activePort;
	private final String rootDir = null;
	private final String currentDir = null;

	public FtpServerSocket(Socket s) throws IOException {
		super();
		this.socket = s;

		try {
			this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			this.out = new PrintWriter(this.socket.getOutputStream(), true);
		} catch (IOException ioe) {
			throw ioe;
		}

		Method[] m = this.getClass().getDeclaredMethods();
		String methodName = null;

		for (java.lang.reflect.Method method : m) {
			methodName = method.getName();

			if (commands.contains(methodName)) {
				this.methods.put(methodName, method);
			}
		}

		this.start();
	}

	public static void main(String[] args) {
	}

	private void send(String bundleId) {
		this.out.print(this.bundle.getString(bundleId));
		this.out.flush();
	}

	private void send(String bundleId, Object[] args) {
		MessageFormat fmt = new MessageFormat(this.bundle.getString(bundleId));
		this.out.print(fmt.format(args));
		this.out.flush();
	}

	public void motd() {
		this.send("220motd");
	}

	public void user(String line) {
		this.send("331user");
	}

	public void pass(String line) {
		this.send("230pass");
	}

	public void type(String line) { // needs work A, E, I, L, C, T, N

		Object[] args = {"I"};
		this.send("200type", args);
	}

	public void mode(String line) { // check for S, B, or C

		Object[] args = {line.toUpperCase()};
		this.send("200ok", args);
	}

	public void pwd(String line) {
		Object[] args = {this.currentDir};
		this.send("257pwd", args);
	}

	public void cdup(String line) {
		Object[] args = {"CDUP"};
		File tmp = this.directory.getParentFile();

		if (null != tmp) {
			this.directory = tmp;
		}

		this.send("200", args);
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


	public void list(String line) {
		this.send("150list");

		final boolean passive = false;
		if (!passive) {
			try {
				Socket activeSocket = new Socket(this.socket.getInetAddress(), this.activePort);
				PrintStream ps = new PrintStream(activeSocket.getOutputStream());
				ps.print("bleah\r\n");
				ps.print("bleah\r\n");
				ps.print("bleah\r\n");
				ps.print("bleah\r\n");
				ps.print("bleah\r\n");
				ps.flush();
				ps.close();
				this.send("226");
			} catch (Exception e) {
			}
		}
	}


	public void port(String line) {
		int start = line.lastIndexOf(',');
		int end = line.length();
		String lo = null; // lo-ordered byte

		if ((-1 != start) && (-1 != end)) {
			lo = line.substring(start + 1, end);
		}

		end = start;
		start = line.lastIndexOf(',', start - 1);

		String hi = null; // hi-ordered byte

		if ((-1 != start) && (-1 != end)) {
			hi = line.substring(start + 1, end);
		}

		this.activePort = ((Integer.parseInt(hi) * 256) + Integer.parseInt(lo));

		Object[] args = {"PORT"};
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

				String command = line.substring(0, index).toLowerCase();
				Method o = (Method) this.methods.get(command);

				if (null != o) {
					try {
						o.invoke(this, line);
					} catch (Exception e) {
					}
				} else {
					this.send("500");
				}
			}
		} catch (IOException ioe) {
			net.sf.jftp.system.logging.Log.debug("Socket error: " + ioe);
		}
	}
}
