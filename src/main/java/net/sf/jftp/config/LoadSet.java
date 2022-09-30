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
package net.sf.jftp.config;

import java.io.BufferedReader;
import java.io.FileReader;


public class LoadSet {
	public static String[] loadSet(final String file, final boolean ask) {
		try {
			final BufferedReader breader = new BufferedReader(new FileReader(file));
			final String[] result = new String[6];
			result[0] = breader.readLine();
			result[1] = breader.readLine();
			result[2] = breader.readLine();
			result[3] = breader.readLine();
			result[4] = breader.readLine();
			result[5] = breader.readLine();

			if ((result[2].isEmpty() || !Settings.getStorePasswords()) && ask) {
				result[2] = net.sf.jftp.gui.base.UIUtils.getPasswordFromUser(net.sf.jftp.JFtp.statusP.jftp);
				net.sf.jftp.system.logging.Log.debug("fetched: " + result[2] + ", storing: " + Settings.getStorePasswords());
			} else if (!result[2].isEmpty() || Settings.getStorePasswords()) {
				// need to decode
				final String decoded = Crypto.Decrypt(result[2]);
				if (decoded.isEmpty()) {
					// failed to decode
					if (ask) {
						// ask for a password
						result[2] = net.sf.jftp.gui.base.UIUtils.getPasswordFromUser(net.sf.jftp.JFtp.statusP.jftp);
					} else {
						// fallback to empty string
						result[2] = "";
					}
				}
			}

			return result;
		} catch (final Exception ex) {
		}

		return new String[1];
	}

	public static String[] loadSet(final String file) {
		return loadSet(file, false);
	}
}
