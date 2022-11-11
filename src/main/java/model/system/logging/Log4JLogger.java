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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;


public class Log4JLogger implements JftpLogger {
	private static final Logger logger = LogManager.getLogger("net.sf.jftp");

	public Log4JLogger() {
		super();
	}

	private static String stacktrace(Throwable throwable) {
		StringWriter out = new StringWriter();
		throwable.printStackTrace(new PrintWriter(out));
		return (throwable.toString());
	}

	public void debugRaw(String msg) {
		logger.debug(msg);
	}

	public void debug(String msg) {
		logger.debug(msg);
	}

	public void debug(String msg, Throwable throwable) {
		logger.debug(msg);
		logger.debug(stacktrace(throwable));
	}

	public void warn(String msg) {
		logger.warn(msg);
	}

	public void warn(String msg, Throwable throwable) {
		logger.warn(msg);
		logger.warn(stacktrace(throwable));
	}

	public void error(String msg) {
		logger.error(msg);
	}

	public void error(String msg, Throwable throwable) {
		logger.error(msg);
		logger.error(stacktrace(throwable));
	}

	public void info(String msg) {
		logger.info(msg);
	}

	public void info(String msg, Throwable throwable) {
		logger.info(msg);
		logger.info(stacktrace(throwable));
	}

	public void fatal(String msg) {
		logger.fatal(msg);
	}

	public void fatal(String msg, Throwable throwable) {
		logger.fatal(msg);
		logger.fatal(stacktrace(throwable));
	}
}
