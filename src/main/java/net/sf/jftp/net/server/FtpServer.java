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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


/**
 *
 */
public class FtpServer extends Thread {
	private final int dataPort = 20;
	private int port;

	public FtpServer(final int port) {
		this.port = port;
	}

	public static void main(final String[] args) {
		final FtpServer server = new FtpServer(2100);
		server.start();
	}

	public void run() {
		try {
			final ServerSocket listener = new ServerSocket(port);

			while (true) {
				final Socket s = listener.accept();
				new FtpServerSocket(s);
			}
		} catch (final IOException ioe) {
			net.sf.jftp.system.logging.Log.debug("ServerSocket error: " + ioe);
		}
	}
}
