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

import java.awt.*;
import java.awt.event.*;

import java.io.*;


public class PathChanger extends HFrame implements ActionListener
{
    private final HTextField text;
    private final HButton ok = new HButton("Change Directory");
    private String type = "";

    public PathChanger(String type)
    {
        this.type = type;

        //setSize(400, 80);
        setTitle("Choose path...");
        //setLocation(150, 150);
        getContentPane().setLayout(new FlowLayout());

        text = new HTextField("Path:", "");
        getContentPane().add(text);
        getContentPane().add(ok);
        ok.addActionListener(this);
        text.text.addActionListener(this);

        pack();
	fixLocation();
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e)
    {
        if((e.getSource() == ok) || (e.getSource() == text.text))
        {
            setVisible(false);

            if(type.equals("remote"))
            {
                JFtp.remoteDir.getCon().chdir(text.getText());
            }

            if(type.equals("local"))
            {
                JFtp.localDir.getCon().chdir(text.getText());
            }
        }
    }
}
