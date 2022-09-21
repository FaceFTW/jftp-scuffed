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
import net.sf.jftp.util.*;

import java.awt.*;
import java.awt.event.*;

import java.io.*;


public class Remover extends HFrame implements ActionListener
{
    private final HTextField text;
    private final HButton ok = new HButton("Remove file/directory...");
    private final HButton cancel = new HButton("Cancel");
    private final HPanel okP = new HPanel();
    private String type = null;

    public Remover(String l, String type)
    {
        this.type = type;

        setSize(350, 100);
        setTitle("Choose...");
        setLocation(150, 150);
        getContentPane().setLayout(new BorderLayout(10, 10));

        text = new HTextField(l, "");
        okP.add(ok);
        okP.add(cancel);
        getContentPane().add("North", text);
        getContentPane().add("South", okP);
        ok.addActionListener(this);
        cancel.addActionListener(this);

        setVisible(true);
    }

    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource() == ok)
        {
            setVisible(false);

            String tmp = text.getText();

            if(!tmp.endsWith("/"))
            {
                tmp = tmp + "/";
            }

            AutoRemover armv = new AutoRemover(tmp, type);

            if(type.equals("local"))
            {
                JFtp.localUpdate();
            }

            if(type.equals("remote"))
            {
                JFtp.remoteUpdate();
            }
        }

        if(e.getSource() == cancel)
        {
            this.dispose();
        }
    }
}
