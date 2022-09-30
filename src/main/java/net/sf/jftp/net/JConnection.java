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
package net.sf.jftp.net;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;


/**
 * Basic class for getting an TCP/IP-Connection
 * timeout sets (as the name says) the maximum time the Thread
 * waits for the target host...
 */
public class JConnection implements Runnable {
	private final String host;
	private final int port;
	private PrintStream out;
	private BufferedReader in;
	private Socket s;
	private boolean isOk = false;
	private boolean established = false;
	private int localPort = -1;

	public JConnection(String host, int port) {
		this.host = host;
		this.port = port;
		Thread runner = new Thread(this);
		runner.start();
	}


	public void run() {
		try {

			s = new Socket(host, port);

			localPort = s.getLocalPort();

			//if(time > 0) s.setSoTimeout(time);
			out = new PrintStream(new BufferedOutputStream(s.getOutputStream(), net.sf.jftp.config.Settings.bufferSize));
			in = new BufferedReader(new InputStreamReader(s.getInputStream()), net.sf.jftp.config.Settings.bufferSize);
			isOk = true;

			// }
		} catch (Exception ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.out("WARNING: connection closed due to exception (" + host + ":" + port + ")");
			isOk = false;

			try {
				if ((s != null) && !s.isClosed()) {
					s.close();
				}

				if (out != null) {
					out.close();
				}

				if (in != null) {
					in.close();
				}
			} catch (Exception ex2) {
				ex2.printStackTrace();
				net.sf.jftp.system.logging.Log.out("WARNING: got more errors trying to close socket and streams");
			}
		}

		established = true;
	}

	public boolean isThere() {
		int cnt = 0;

		int timeout = net.sf.jftp.config.Settings.connectionTimeout;
		while (!established && (cnt < timeout)) {
			pause(10);
			cnt = cnt + 10;
		}

		return isOk;
	}

	public void send(String data) {
		try {
			//System.out.println(":"+data+":");
			out.print(data);
			out.print("\r\n");
			out.flush();

			if (data.startsWith("PASS")) {
				net.sf.jftp.system.logging.Log.debug("> PASS ****");
			} else {
				net.sf.jftp.system.logging.Log.debug("> " + data);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public PrintStream getInetOutputStream() {
		return out;
	}

	public BufferedReader getReader() {
		return in;
	}

	public int getLocalPort() {
		return localPort;
	}

	public InetAddress getLocalAddress() throws IOException {
		return s.getLocalAddress();
	}

	private void pause(int time) {
		try {
			Thread.sleep(time);
		} catch (Exception ex) {
		}
	}

	public BufferedReader getIn() {
		return in;
	}

	public void setIn(BufferedReader in) {
		this.in = in;
	}

	public PrintStream getOut() {
		return out;
	}

	public void setOut(PrintStream out) {
		this.out = out;
	}
}
