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
package model.system.logging;


public class SystemLogger implements JftpLogger {
	private static void printLog(String msg, Throwable throwable) {
		System.out.println(msg);
		throwable.printStackTrace();
	}

	private void printLog(String msg) {
		System.out.println(msg);
	}

	public void debug(String msg) {
		this.printLog(msg + "\n");
	}

	public void debugRaw(String msg) {
		this.printLog(msg);
	}

	public void debug(String msg, Throwable throwable) {
		this.printLog(msg, throwable);
	}

	public void warn(String msg) {
		this.printLog(msg);
	}

	public void warn(String msg, Throwable throwable) {
		this.printLog(msg, throwable);
	}

	public void error(String msg) {
		this.printLog(msg);
	}

	public void error(String msg, Throwable throwable) {
		this.printLog(msg, throwable);
	}

	public void info(String msg) {
		this.printLog(msg);
	}

	public void info(String msg, Throwable throwable) {
		this.printLog(msg, throwable);
	}

	public void fatal(String msg) {
		this.printLog(msg);
	}

	public void fatal(String msg, Throwable throwable) {
		this.printLog(msg, throwable);
	}
}
