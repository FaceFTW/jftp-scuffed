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


import org.apache.log4j.*;

import java.io.PrintWriter;
import java.io.StringWriter;


public class Log4JLogger implements Logger
{
    private final Category cat = Category.getInstance("jftp");

    public Log4JLogger()
    {
        BasicConfigurator.configure();
    }

    private String stacktrace(Throwable throwable)
    {
        StringWriter out = new StringWriter();
        throwable.printStackTrace(new PrintWriter(out));

        return (throwable.toString());
    }

    public void debug(String msg)
    {
        cat.debug(msg);
    }

    public void debugRaw(String msg)
    {
        cat.debug(msg);
    }

    public void debug(String msg, Throwable throwable)
    {
        cat.debug(msg);
        cat.debug(stacktrace(throwable));
    }

    public void warn(String msg)
    {
        cat.warn(msg);
    }

    public void warn(String msg, Throwable throwable)
    {
        cat.warn(msg);
        cat.warn(stacktrace(throwable));
    }

    public void error(String msg)
    {
        cat.error(msg);
    }

    public void error(String msg, Throwable throwable)
    {
        cat.error(msg);
        cat.error(stacktrace(throwable));
    }

    public void info(String msg)
    {
        cat.info(msg);
    }

    public void info(String msg, Throwable throwable)
    {
        cat.info(msg);
        cat.info(stacktrace(throwable));
    }

    public void fatal(String msg)
    {
        cat.fatal(msg);
    }

    public void fatal(String msg, Throwable throwable)
    {
        cat.fatal(msg);
        cat.fatal(stacktrace(throwable));
    }
}
