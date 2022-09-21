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


public class Displayer extends JInternalFrame implements ActionListener
{
    public static boolean showCloseButton = false;
    
    private final JTextArea info = new JTextArea(25, 50) {
    	public Insets getInsets() {
    		Insets std = super.getInsets();
    		
    		return new Insets(std.top + 5, std.left + 5, std.bottom + 5,
    				std.right + 5);
    	}
    };
    
    private final JButton close = new JButton("Close");

    public Displayer(java.net.URL file, Font font)
    {
        super(file.getFile(), true, true, true, true);
        setLocation(50, 50);
        setSize(600, 540);
        getContentPane().setLayout(new BorderLayout());

        load(file);
        if(font != null) {
        	info.setFont(font);
        }
        else {
        	info.setFont(new Font("monospaced",Font.PLAIN, 11));
        }
        info.setEditable(false);

        JScrollPane jsp = new JScrollPane(info);
        getContentPane().add("Center", jsp);

        HPanel closeP = new HPanel();
        closeP.setLayout(new FlowLayout(FlowLayout.CENTER));
        closeP.add(close);

        close.addActionListener(this);

        if(showCloseButton)
        {
            getContentPane().add("South", closeP);
        }
        
        info.setCaretPosition(0);

        setVisible(true);
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
