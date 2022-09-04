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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.sf.jftp.system;

import net.sf.jftp.config.Settings;
import net.sf.jftp.event.*;
import net.sf.jftp.net.*;
import net.sf.jftp.system.logging.Log;
import net.sf.jftp.system.logging.SystemLogger;
import net.sf.jftp.util.*;

import java.io.*;

import java.net.*;


public class CommandLine implements Runnable, EventHandler, FtpEventConstants
{
    private EventCollector eventCollector;

    public CommandLine()
    {
        Log.setLogger(new SystemLogger());
        eventCollector = new EventCollector();
        EventProcessor.addHandler(FTPCommand, new FtpEventHandler());
        EventProcessor.addHandler(FTPPrompt, this);
        new Thread(this).start();
    }

    public boolean handle(Event e)
    {
        System.out.print("ftp> ");

        return true;
    }

    public void run()
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line = null;

        do
        {
            try
            {
                eventCollector.accept(new FtpEvent(FTPPrompt));
                line = in.readLine();
                eventCollector.accept(new FtpEvent(FTPCommand, line));
            }
            catch(IOException e)
            {
            }
        }
        while(!line.toLowerCase().startsWith("quit"));

        eventCollector.accept(new FtpEvent(FTPShutdown)); // make the quit command spawn this event?
    }

    public static void main(String[] argv)
    {
        CommandLine ftp = new CommandLine();
    }
}
