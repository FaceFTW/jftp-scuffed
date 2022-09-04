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
import net.sf.jftp.util.*;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

/**
 * Create directory dialog.
 * 
 * @author David Hansmann
 *
 */
public class Creator extends HFrame implements ActionListener
{
    private HTextField text;
    private HButton ok = new HButton("Create directory...");
    private BasicConnection con;

    public Creator(String l, BasicConnection con)
    {
        this.con = con;

        setTitle("Choose...");
        getContentPane().setLayout(new FlowLayout());

        text = new HTextField(l, "");
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
            con.mkdir(text.getText());
        }
    }
}
