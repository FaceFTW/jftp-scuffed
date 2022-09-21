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
import net.sf.jftp.system.logging.Log;
import net.sf.jftp.util.*;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import javax.swing.*;


public class ExternalDisplayer extends HFrame implements ActionListener
{
    private final JTextArea info = new JTextArea(25, 50);
    private final JButton close = new JButton("Close");

    public ExternalDisplayer(java.net.URL file)
    {
        setTitle("Info...");
        setLocation(50, 50);
        setSize(600, 540);
        getContentPane().setLayout(new BorderLayout());

        addWindowListener(new WindowAdapter()
            {
                public void windowClosing(WindowEvent e)
                {
                    dispose();
                }
            });
        load(file);
        info.setEditable(false);

        JScrollPane jsp = new JScrollPane(info);
        getContentPane().add("Center", jsp);

        HPanel closeP = new HPanel();
        closeP.setLayout(new FlowLayout(FlowLayout.CENTER));
        closeP.add(close);

        close.addActionListener(this);

        getContentPane().add("South", closeP);

        info.setCaretPosition(0);
        setVisible(true);
        pack();
    }

    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource() == close)
        {
            this.dispose();
        }
    }

    private void load(java.net.URL file)
    {
        String data = "";
        String now = "";

        try
        {
            DataInput in = new DataInputStream(new BufferedInputStream(file.openStream()));

            while((data = in.readLine()) != null)
            {
                now = now + data + "\n";
            }
        }
        catch(IOException e)
        {
            Log.debug(e + " @Displayer.load()");
        }

        info.setText(now);
    }

    public Insets getInsets()
    {
        Insets std = super.getInsets();

        return new Insets(std.top + 5, std.left + 5, std.bottom + 5,
                          std.right + 5);
    }
}
