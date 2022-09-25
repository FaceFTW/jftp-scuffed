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

	public UpdateDaemon(net.sf.jftp.JFtp jftp) {
		this.jftp = jftp;

		Thread runner = new Thread(this);
		runner.start();
	}

	public static void updateRemoteDirGUI() {
		//System.out.print("+");
		reg++;
	}

	public static void updateRemoteDir() {
		//System.out.print("-");
		rem++;
	}

	public static void updateLocalDir() {
		//System.out.print("#");
		loc++;
	}

	public static void updateLog() {
		//System.out.print(".");
		log++;
	}

	public static void updateCall() {
		cal++;
	}

	public void run() {
		while (true) {
			try {
				//System.out.print(":");
				if (rem > 0) {
					net.sf.jftp.JFtp.remoteDir.fresh();

					//System.out.print("R");
					rem = 0;
					Thread.sleep(100);
				}

				if (reg > 0) {
					((net.sf.jftp.gui.base.RemoteDir) net.sf.jftp.JFtp.remoteDir).gui(true);

					//System.out.print("r");
					reg = 0;
					Thread.sleep(100);
				}

				if (loc > 0) {
					net.sf.jftp.JFtp.localDir.fresh();

					//System.out.print("L");
					loc = 0;
					Thread.sleep(100);
				}

				if (cal > 0) {
					jftp.fireUpdate();
					cal = 0;
					Thread.sleep(100);
				}

				if (log > 0) {
					jftp.ensureLogging();

					//System.out.print("O");
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
