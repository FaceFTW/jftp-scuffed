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
package net.sf.jftp.event;

import java.util.Hashtable;
import java.util.Vector;


public class EventProcessor implements Runnable, Acceptor, FtpEventConstants,
                                       EventHandler
{
    private static final Hashtable table = new Hashtable();
    private final Vector buffer;
    private boolean done = false;

    public EventProcessor(Vector b)
    {
        buffer = b;
        new Thread(this).start();
        addHandler(FTPShutdown, this);
    }

    public void accept(Event e)
    {
        Integer code = new Integer(e.eventCode());
        Vector handlers = (Vector) (table.get(code));

        if(handlers != null)
        {
            for(int i = 0, max = handlers.size(); i < max; i++)
            {
                ((EventHandler) (handlers.elementAt(i))).handle(e);
            }
        }
    }

    public static void addHandler(int eventCode, EventHandler h)
    {
        Integer code = new Integer(eventCode);
        Vector handlers = (Vector) (table.get(code));

        if(handlers == null)
        {
            handlers = new Vector();
            table.put(code, handlers);
        }

        handlers.addElement(h);
    }

    public boolean handle(Event e)
    {
        done = true;

        return true;
    }

    public void run()
    {
        while(!done)
        {
            if(buffer.size() != 0)
            {
                accept((Event) buffer.firstElement());
                buffer.removeElementAt(0);
            }
        }
    }
}
