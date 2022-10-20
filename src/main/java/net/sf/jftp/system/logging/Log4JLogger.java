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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.PrintWriter;
import java.io.StringWriter;


public class Log4JLogger implements net.sf.jftp.system.logging.Logger {
	private final Logger logger = LogManager.getLogger("net/sf/jftp");

	public Log4JLogger() {
		super();
	}

	private String stacktrace(Throwable throwable) {
		StringWriter out = new StringWriter();
		throwable.printStackTrace(new PrintWriter(out));

		return (throwable.toString());
	}

	public void debug(String msg) {
		this.logger.debug(msg);
	}

	public void debugRaw(String msg) {
		this.logger.debug(msg);
	}

	public void debug(String msg, Throwable throwable) {
		this.logger.debug(msg);
		this.logger.debug(this.stacktrace(throwable));
	}

	public void warn(String msg) {
		this.logger.warn(msg);
	}

	public void warn(String msg, Throwable throwable) {
		this.logger.warn(msg);
		this.logger.warn(this.stacktrace(throwable));
	}

	public void error(String msg) {
		this.logger.error(msg);
	}

	public void error(String msg, Throwable throwable) {
		this.logger.error(msg);
		this.logger.error(this.stacktrace(throwable));
	}

	public void info(String msg) {
		this.logger.info(msg);
	}

	public void info(String msg, Throwable throwable) {
		this.logger.info(msg);
		this.logger.info(this.stacktrace(throwable));
	}

	public void fatal(String msg) {
		this.logger.fatal(msg);
	}

	public void fatal(String msg, Throwable throwable) {
		this.logger.fatal(msg);
		this.logger.fatal(this.stacktrace(throwable));
	}
}
