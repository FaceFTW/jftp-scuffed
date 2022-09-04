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
import net.sf.jftp.gui.framework.*;
import net.sf.jftp.net.*;
import net.sf.jftp.net.wrappers.HttpTransfer;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.util.*;


public class HttpDownloader extends HPanel implements ActionListener
{
    private HTextField text;
    private HButton ok = new HButton("Start");

    public HttpDownloader()
    {
        //setSize(480,100);
        //setTitle("Download a file via an URL");
        //setLocation(150,150);
        //getContentPane().
        setLayout(new FlowLayout());

        text = new HTextField("URL:", "http://", 25);

        //getContentPane().
        add(text);

        //getContentPane().
        add(ok);
        ok.addActionListener(this);
        text.text.addActionListener(this);

        setVisible(true);
    }

    public void actionPerformed(ActionEvent e)
    {
        if((e.getSource() == ok) || (e.getSource() == text.text))
        {
            Vector listeners = new Vector();
            listeners.add(JFtp.localDir);

            HttpTransfer t = new HttpTransfer(text.getText().trim(),
                                              JFtp.localDir.getPath(),
                                              listeners,
                                              JFtp.getConnectionHandler());

            JFtp.statusP.jftp.removeFromDesktop(this.hashCode());

            //hide();
        }
    }
}
