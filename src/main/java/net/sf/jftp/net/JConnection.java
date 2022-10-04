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
	private PrintStream out = null;
	private BufferedReader in = null;
	private Socket s = null;
	private boolean isOk = false;
	private boolean established = false;
	private int localPort = -1;

	public JConnection(String host, int port) {
		super();
		this.host = host;
		this.port = port;
		Thread runner = new Thread(this);
		runner.start();
	}


	public void run() {
		try {

			this.s = new Socket(this.host, this.port);

			this.localPort = this.s.getLocalPort();

			//if(time > 0) s.setSoTimeout(time);
			this.out = new PrintStream(new BufferedOutputStream(this.s.getOutputStream(), net.sf.jftp.config.Settings.bufferSize));
			this.in = new BufferedReader(new InputStreamReader(this.s.getInputStream()), net.sf.jftp.config.Settings.bufferSize);
			this.isOk = true;

			// }
		} catch (Exception ex) {
			ex.printStackTrace();
			net.sf.jftp.system.logging.Log.out("WARNING: connection closed due to exception (" + this.host + ":" + this.port + ")");
			this.isOk = false;

			try {
				if ((null != this.s) && !this.s.isClosed()) {
					this.s.close();
				}

				if (null != this.out) {
					this.out.close();
				}

				if (null != this.in) {
					this.in.close();
				}
			} catch (Exception ex2) {
				ex2.printStackTrace();
				net.sf.jftp.system.logging.Log.out("WARNING: got more errors trying to close socket and streams");
			}
		}

		this.established = true;
	}

	public boolean isThere() {
		int cnt = 0;

		final int timeout = net.sf.jftp.config.Settings.connectionTimeout;
		while (!this.established && (cnt < timeout)) {
			this.pause(10);
			cnt = cnt + 10;
		}

		return this.isOk;
	}

	public void send(String data) {
		try {
			//System.out.println(":"+data+":");
			this.out.print(data);
			this.out.print("\r\n");
			this.out.flush();

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
		return this.out;
	}

	public BufferedReader getReader() {
		return this.in;
	}

	public int getLocalPort() {
		return this.localPort;
	}

	public InetAddress getLocalAddress() throws IOException {
		return this.s.getLocalAddress();
	}

	private void pause(int time) {
		try {
			Thread.sleep(time);
		} catch (Exception ex) {
		}
	}

	public BufferedReader getIn() {
		return this.in;
	}

	public void setIn(BufferedReader in) {
		this.in = in;
	}

	public PrintStream getOut() {
		return this.out;
	}

	public void setOut(PrintStream out) {
		this.out = out;
	}
}
