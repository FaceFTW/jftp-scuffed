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
package net.sf.jftp.system.logging;

import net.sf.jftp.config.Settings;


public class Log {
	private static final Log log = new Log();
	private static Logger logger = new SystemLogger();
	private static StringBuffer cache = new StringBuffer();

	private Log() {
	}

	public static void setLogger(final Logger logger) {
		Log.logger = logger;
	}

	public static void debug(final String msg) {
		if (Settings.getDisableLog()) {
			return;
		}
		net.sf.jftp.system.logging.Log.logger.debug(msg);
		net.sf.jftp.system.logging.Log.cache.append(msg).append("\n");

		if (!Settings.getEnableDebug()) System.out.println("> " + msg);
	}

	public static void debugRaw(final String msg) {
		if (Settings.getDisableLog()) {
			return;
		}

		net.sf.jftp.system.logging.Log.logger.debugRaw(msg);
		net.sf.jftp.system.logging.Log.cache.append(msg);

		if (Settings.getEnableDebug()) System.out.print(msg);
	}

	public static void out(final String msg) {
		if (!Settings.getEnableDebug()) {
			return;
		}

		System.out.println("> " + msg);
	}

	public static void devnull(final Object msg) {
	}


	public static String getCache() {
		return net.sf.jftp.system.logging.Log.cache.toString();
	}

	public static void clearCache() {
		net.sf.jftp.system.logging.Log.cache = new StringBuffer();
	}
}
