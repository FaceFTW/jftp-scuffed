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


public class SystemLogger implements Logger {
	private void log(final String msg) {
		System.out.println(msg);
	}

	private void log(final String msg, final Throwable throwable) {
		System.out.println(msg);
		throwable.printStackTrace();
	}

	public void debug(final String msg) {
		this.log(msg + "\n");
	}

	public void debugRaw(final String msg) {
		this.log(msg);
	}

	public void debug(final String msg, final Throwable throwable) {
		this.log(msg, throwable);
	}

	public void warn(final String msg) {
		this.log(msg);
	}

	public void warn(final String msg, final Throwable throwable) {
		this.log(msg, throwable);
	}

	public void error(final String msg) {
		this.log(msg);
	}

	public void error(final String msg, final Throwable throwable) {
		this.log(msg, throwable);
	}

	public void info(final String msg) {
		this.log(msg);
	}

	public void info(final String msg, final Throwable throwable) {
		this.log(msg, throwable);
	}

	public void fatal(final String msg) {
		this.log(msg);
	}

	public void fatal(final String msg, final Throwable throwable) {
		this.log(msg, throwable);
	}
}
