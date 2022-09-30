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


import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Category;

import java.io.PrintWriter;
import java.io.StringWriter;


public class Log4JLogger implements Logger {
	private final Category cat = Category.getInstance("net/sf/jftp");

	public Log4JLogger() {
		BasicConfigurator.configure();
	}

	private String stacktrace(final Throwable throwable) {
		final StringWriter out = new StringWriter();
		throwable.printStackTrace(new PrintWriter(out));

		return (throwable.toString());
	}

	public void debug(final String msg) {
		cat.debug(msg);
	}

	public void debugRaw(final String msg) {
		cat.debug(msg);
	}

	public void debug(final String msg, final Throwable throwable) {
		cat.debug(msg);
		cat.debug(this.stacktrace(throwable));
	}

	public void warn(final String msg) {
		cat.warn(msg);
	}

	public void warn(final String msg, final Throwable throwable) {
		cat.warn(msg);
		cat.warn(this.stacktrace(throwable));
	}

	public void error(final String msg) {
		cat.error(msg);
	}

	public void error(final String msg, final Throwable throwable) {
		cat.error(msg);
		cat.error(this.stacktrace(throwable));
	}

	public void info(final String msg) {
		cat.info(msg);
	}

	public void info(final String msg, final Throwable throwable) {
		cat.info(msg);
		cat.info(this.stacktrace(throwable));
	}

	public void fatal(final String msg) {
		cat.fatal(msg);
	}

	public void fatal(final String msg, final Throwable throwable) {
		cat.fatal(msg);
		cat.fatal(this.stacktrace(throwable));
	}
}
