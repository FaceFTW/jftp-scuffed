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
package net.sf.jftp.net;

import java.util.Enumeration;
import java.util.Hashtable;


/**
* This class manages a connection pool.
* You do not have to create an instance, but when you do use FtpConnection.setConnectionHandler
* for each connection to make the connection recognize its handler.
*/
public class ConnectionHandler
{
    private final Hashtable<String,Transfer> connections = new Hashtable<String,Transfer>();

    public void addConnection(String file, Transfer t)
    {
        connections.put(file, t);
    }

    public void removeConnection(String file)
    {
        connections.remove(file);
    }

    public Hashtable<String,Transfer> getConnections()
    {
        return connections;
    }

    public int getConnectionSize()
    {
        int size = 0;
        Enumeration<Transfer> e = connections.elements();

        while(e.hasMoreElements())
        {
            Transfer t = e.nextElement();

            if(t.hasStarted())
            {
                size++;
            }
        }

        return size;
    }
}
