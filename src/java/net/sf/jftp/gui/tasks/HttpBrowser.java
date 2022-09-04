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
package net.sf.jftp.gui.tasks;

import net.sf.jftp.*;
import net.sf.jftp.config.*;
import net.sf.jftp.gui.framework.*;
import net.sf.jftp.net.*;
import net.sf.jftp.system.logging.Log;
import net.sf.jftp.util.*;

import java.awt.*;

import java.io.*;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.html.*;


public class HttpBrowser extends JInternalFrame implements HyperlinkListener
{
    public HttpBrowser(String url)
    {
        super("Http Browser", true, true, true, true);

        try
        {
            setTitle(url);

            JEditorPane pane = new JEditorPane(url);
            pane.setEditable(false);
            pane.addHyperlinkListener(this);

            if(!pane.getEditorKit().getContentType().equals("text/html") &&
                   !pane.getEditorKit().getContentType().equals("text/rtf"))
            {
                if(!pane.getEditorKit().getContentType().equals("text/plain"))
                {
                    Log.debug("Could not display URL.");

                    return;
                }

                pane.setEditable(false);
                pane.addHyperlinkListener(this);
            }

            JScrollPane jsp = new JScrollPane(pane);

            getContentPane().setLayout(new BorderLayout());
            getContentPane().add("Center", jsp);

            setLocation(50, 50);
            setSize(800, 500);
            show();
            requestFocus();
        }
        catch(Exception ex)
        {
            Log.debug("Error fetching URL: " + ex);
            ex.printStackTrace();
        }
    }

    public void hyperlinkUpdate(HyperlinkEvent e)
    {
        if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
        {
            JEditorPane pane = (JEditorPane) e.getSource();

            if(e instanceof HTMLFrameHyperlinkEvent)
            {
                HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
                HTMLDocument doc = (HTMLDocument) pane.getDocument();
                doc.processHTMLFrameHyperlinkEvent(evt);
            }
            else
            {
                try
                {
                    String url = e.getURL().toString();
                    String tmp = url.substring(url.lastIndexOf("/"));

                    Vector listeners = new Vector();
                    listeners.add(JFtp.localDir);

                    if(!url.endsWith(".htm") && !url.endsWith(".html") &&
                           (tmp.indexOf(".") >= 0))
                    {
                        JFtp.statusP.startTransfer(url,
                                                   JFtp.localDir.getPath(),
                                                   listeners,
                                                   JFtp.getConnectionHandler());
                    }
                    else
                    {
                        pane.setPage(e.getURL());
                    }
                }
                catch(Throwable t)
                {
                    t.printStackTrace();
                }
            }
        }
    }
}
