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
import net.sf.jftp.system.logging.Log;
import net.sf.jftp.util.*;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import javax.swing.*;


public class RemoteCommand extends HFrame implements ActionListener
{
    private HTextField text;
    private HButton ok = new HButton("Execute");

    public RemoteCommand()
    {
        //setSize(400, 80);
        setTitle("Choose command...");
        setLocation(150, 150);
        getContentPane().setLayout(new FlowLayout());

        text = new HTextField("Command:", "SITE CHMOD 755 file", 30);
        getContentPane().add(text);
        getContentPane().add(ok);
        ok.addActionListener(this);
        text.text.addActionListener(this);

        pack();
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e)
    {
        if((e.getSource() == ok) || (e.getSource() == text.text))
        {
            setVisible(false);

            String cmd = text.getText();
            Log.debug("-> " + cmd);
            JFtp.remoteDir.getCon().sendRawCommand(cmd);

            if(cmd.toUpperCase().trim().startsWith("QUIT"))
            {
                JFtp.statusP.jftp.safeDisconnect();

                return;
            }

            JDialog j = new JDialog();
            j.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            j.setTitle("Command response");
            j.setLocation(150, 150);

            JTextArea t = new JTextArea();
            JScrollPane logSp = new JScrollPane(t);
            logSp.setMinimumSize(new Dimension(300, 200));

            t.setText(Log.getCache());
            j.getContentPane().setLayout(new BorderLayout(5, 5));
            j.getContentPane().add("Center", t);
            j.setSize(new Dimension(400, 300));
            j.pack();
            j.setVisible(true);

            JFtp.remoteUpdate();
        }
    }
}
