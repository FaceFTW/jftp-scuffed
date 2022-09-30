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

import java.io.DataInputStream;
import java.io.PrintStream;


public class JRawConnection implements Runnable {
	private final String host;
	private final int port;
	private PrintStream out;
	private JReciever jrcv;
	private boolean isOk = false;
	private boolean established = false;
	private boolean reciever = false;

	public JRawConnection(final String host, final int port, final boolean reciever) {
		this.host = host;
		this.port = port;
		this.reciever = reciever;

		final Thread runner = new Thread(this);
		runner.start();
	}

	public void run() {
		try {
			final java.net.Socket s = new java.net.Socket(host, port);

			out = new PrintStream(s.getOutputStream());
			final java.io.DataInputStream in = new java.io.DataInputStream(s.getInputStream());

			if (reciever) {
				final JReciever jrcv = new JReciever(in);
			}

			isOk = true;
		} catch (final Exception ex) {
			isOk = false;
		}

		established = true;
	}

	public boolean isThere() {
		int cnt = 0;

		final int timeout = net.sf.jftp.config.Settings.connectionTimeout;
		while (!established && (cnt < timeout)) {
			this.pause(100);
			cnt = cnt + 100;
		}

		return isOk;
	}

	public void send(final String data) {
		try {
			out.println(data);
		} catch (final Exception ex) {
			System.out.println(ex + "@JConnection.send()");
		}
	}

	private void pause(final int time) {
		try {
			Thread.sleep(time);
		} catch (final Exception ex) {
			System.out.println(ex);
		}
	}
}
