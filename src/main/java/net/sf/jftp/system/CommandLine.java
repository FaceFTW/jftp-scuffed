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
package net.sf.jftp.system;

import net.sf.jftp.event.Event;
import net.sf.jftp.event.EventCollector;
import net.sf.jftp.event.EventHandler;
import net.sf.jftp.event.EventProcessor;
import net.sf.jftp.event.FtpEvent;
import net.sf.jftp.event.FtpEventConstants;
import net.sf.jftp.event.FtpEventHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class CommandLine implements Runnable, EventHandler, FtpEventConstants {
	private final EventCollector eventCollector;

	public CommandLine() {
		net.sf.jftp.system.logging.Log.setLogger(new net.sf.jftp.system.logging.SystemLogger());
		eventCollector = new EventCollector();
		EventProcessor.addHandler(FTPCommand, new FtpEventHandler());
		EventProcessor.addHandler(FTPPrompt, this);
		new Thread(this).start();
	}

	public static void main(final String[] argv) {
		final CommandLine ftp = new CommandLine();
	}

	public boolean handle(final Event e) {
		System.out.print("ftp> ");

		return true;
	}

	public void run() {
		final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line = null;

		do {
			try {
				eventCollector.accept(new FtpEvent(FTPPrompt));
				line = in.readLine();
				eventCollector.accept(new FtpEvent(FTPCommand, line));
			} catch (final IOException e) {
			}
		} while (!line.toLowerCase().startsWith("quit"));

		eventCollector.accept(new FtpEvent(FTPShutdown)); // make the quit command spawn this event?
	}
}
