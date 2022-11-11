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
package model.system;

import view.JFtp;
import controller.config.Settings;
import view.gui.base.RemoteDir;

public class UpdateDaemon implements Runnable {
	private static int rem;
	private static int loc;
	private static int log;
	private static int reg;
	private static int cal;
	private final JFtp jftp;

	public UpdateDaemon(JFtp jftp) {
		super();
		this.jftp = jftp;

		Thread runner = new Thread(this);
		runner.start();
	}

	public static void updateRemoteDirGUI() {
		reg++;
	}

	public static void updateRemoteDir() {
		rem++;
	}

	public static void updateLocalDir() {
		loc++;
	}

	public static void updateLog() {
		log++;
	}

	public static void updateCall() {
		cal++;
	}

	public void run() {
		while (true) {
			try {
				if (0 < rem) {
					JFtp.remoteDir.fresh();

					rem = 0;
					Thread.sleep(100);
				}

				if (0 < reg) {
					((RemoteDir) JFtp.remoteDir).gui(true);
					reg = 0;
					Thread.sleep(100);
				}

				if (0 < loc) {
					JFtp.localDir.fresh();
					loc = 0;
					Thread.sleep(100);
				}

				if (0 < cal) {
					this.jftp.fireUpdate();
					cal = 0;
					Thread.sleep(100);
				}

				if (0 < log) {
					this.jftp.ensureLogging();
					log = 0;
					Thread.sleep(500);
				}

				Thread.sleep(Settings.uiRefresh);
			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(1);
			}
		}
	}
}
