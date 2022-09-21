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
package net.sf.jftp.util;

import net.sf.jftp.*;
import net.sf.jftp.config.*;
import net.sf.jftp.gui.framework.*;
import net.sf.jftp.net.*;
import net.sf.jftp.util.*;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.net.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;


/*
alternative connection class, used for raw tcp/ip connection
*/
public class JRawConnection implements Runnable
{
    private final int timeout = Settings.connectionTimeout;
    private final String host;
    private final int port;
    private PrintStream out;
    private DataInputStream in;
    private Socket s;
    private JReciever jrcv;
    private boolean isOk = false;
    private boolean established = false;
    private boolean reciever = false;
    private final Thread runner;

    public JRawConnection(String host, int port)
    {
        this(host, port, false);
    }

    public JRawConnection(String host, int port, boolean reciever)
    {
        this.host = host;
        this.port = port;
        this.reciever = reciever;

        runner = new Thread(this);
        runner.start();
    }

    public void run()
    {
        try
        {
            s = new Socket(host, port);

            //  s.setSoTimeout(Resource.socketTimeout);
            out = new PrintStream(s.getOutputStream());
            in = new DataInputStream(s.getInputStream());

            if(reciever)
            {
                JReciever jrcv = new JReciever(in);
            }

            isOk = true;
        }
        catch(Exception ex)
        {
            isOk = false;
        }

        established = true;
    }

    public boolean isThere()
    {
        int cnt = 0;

        while(!established && (cnt < timeout))
        {
            pause(100);
            cnt = cnt + 100;
        }

        return isOk;
    }

    public void send(String data)
    {
        try
        {
            out.println(data);
        }
        catch(Exception ex)
        {
            System.out.println(ex + "@JConnection.send()");
        }
    }

    public PrintStream getInetOutputStream()
    {
        return out;
    }

    public DataInputStream getInetInputStream()
    {
        return in;
    }

    private void pause(int time)
    {
        try
        {
            Thread.sleep(time);
        }
        catch(Exception ex)
        {
            System.out.println(ex);
        }
    }
}
