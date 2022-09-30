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
package net.sf.jftp.system;

import net.sf.jftp.config.Settings;

public class UpdateDaemon implements Runnable {
	private static int rem;
	private static int loc;
	private static int log;
	private static int reg;
	private static int cal;
	private final net.sf.jftp.JFtp jftp;

	public UpdateDaemon(final net.sf.jftp.JFtp jftp) {
		this.jftp = jftp;

		final Thread runner = new Thread(this);
		runner.start();
	}

	public static void updateRemoteDirGUI() {
		net.sf.jftp.system.UpdateDaemon.reg++;
	}

	public static void updateRemoteDir() {
		net.sf.jftp.system.UpdateDaemon.rem++;
	}

	public static void updateLocalDir() {
		net.sf.jftp.system.UpdateDaemon.loc++;
	}

	public static void updateLog() {
		net.sf.jftp.system.UpdateDaemon.log++;
	}

	public static void updateCall() {
		net.sf.jftp.system.UpdateDaemon.cal++;
	}

	public void run() {
		while (true) {
			try {
				if (net.sf.jftp.system.UpdateDaemon.rem > 0) {
					net.sf.jftp.JFtp.remoteDir.fresh();

					net.sf.jftp.system.UpdateDaemon.rem = 0;
					Thread.sleep(100);
				}

				if (net.sf.jftp.system.UpdateDaemon.reg > 0) {
					((net.sf.jftp.gui.base.RemoteDir) net.sf.jftp.JFtp.remoteDir).gui(true);
					net.sf.jftp.system.UpdateDaemon.reg = 0;
					Thread.sleep(100);
				}

				if (net.sf.jftp.system.UpdateDaemon.loc > 0) {
					net.sf.jftp.JFtp.localDir.fresh();
					net.sf.jftp.system.UpdateDaemon.loc = 0;
					Thread.sleep(100);
				}

				if (net.sf.jftp.system.UpdateDaemon.cal > 0) {
					jftp.fireUpdate();
					net.sf.jftp.system.UpdateDaemon.cal = 0;
					Thread.sleep(100);
				}

				if (net.sf.jftp.system.UpdateDaemon.log > 0) {
					jftp.ensureLogging();
					net.sf.jftp.system.UpdateDaemon.log = 0;
					Thread.sleep(500);
				}

				Thread.sleep(Settings.uiRefresh);
			} catch (final Exception ex) {
				ex.printStackTrace();
				System.exit(1);
			}
		}
	}
}
