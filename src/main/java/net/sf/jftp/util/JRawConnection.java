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
package net.sf.jftp.util;

import net.sf.jftp.config.Settings;

import java.io.PrintStream;


public class JRawConnection implements Runnable {
	private final String host;
	private final int port;
	private PrintStream out;
	private JReciever jrcv;
	private boolean isOk;
	private boolean established;
	private boolean reciever;

	public JRawConnection(String host, int port, boolean reciever) {
		super();
		this.host = host;
		this.port = port;
		this.reciever = reciever;

		Thread runner = new Thread(this);
		runner.start();
	}

	public void run() {
		try {
			java.net.Socket s = new java.net.Socket(this.host, this.port);

			this.out = new PrintStream(s.getOutputStream());
			java.io.DataInputStream in = new java.io.DataInputStream(s.getInputStream());

			if (this.reciever) {
				JReciever jrcv = new JReciever(in);
			}

			this.isOk = true;
		} catch (Exception ex) {
			this.isOk = false;
		}

		this.established = true;
	}

	public boolean isThere() {
		int cnt = 0;

		final int timeout = Settings.connectionTimeout;
		while (!this.established && (cnt < timeout)) {
			this.pause(100);
			cnt = cnt + 100;
		}

		return this.isOk;
	}

	public void send(String data) {
		try {
			this.out.println(data);
		} catch (Exception ex) {
			System.out.println(ex + "@JConnection.send()");
		}
	}

	private void pause(int time) {
		try {
			Thread.sleep(time);
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}
}
