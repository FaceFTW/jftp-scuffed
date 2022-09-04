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
import net.sf.jftp.system.logging.Log;
import net.sf.jftp.util.*;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import javax.swing.*;


public class BookmarkManager extends JInternalFrame implements ActionListener
{
    private JTextArea info = new JTextArea(25, 50);
    private JButton save = new JButton("Save and close");
    private JButton close = new JButton("Close");

    public BookmarkManager()
    {
        super("Manage Bookmarks", true, true, true, true);
        setLocation(50, 50);
        setSize(600, 540);
        getContentPane().setLayout(new BorderLayout());

        load(Settings.bookmarks);

        JScrollPane jsp = new JScrollPane(info);
        getContentPane().add("Center", jsp);

        HPanel closeP = new HPanel();
        closeP.setLayout(new FlowLayout(FlowLayout.CENTER));

        //closeP.add(close);
        closeP.add(save);

        close.addActionListener(this);
        save.addActionListener(this);

        getContentPane().add("South", closeP);

        info.setCaretPosition(0);
        pack();
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource() == close)
        {
            this.dispose();
        }
        else
        {
            save(Settings.bookmarks);
            JFtp.menuBar.loadBookmarks();
            this.dispose();
        }
    }

    private void setDefaultText()
    {
        info.setText("");
        info.append("# JFtp Bookmark Configuration file\n");
        info.append("#\n");
        info.append("# Syntax: protocol#host#user#password#port#dir/domain#local\n");
        info.append("#\n");
        info.append("# Note: not all values are used by every connection, but all fields must contain at least\n");
        info.append("# one character.\n");
        info.append("Use \"<%hidden%>\" for password fields you don't want to fill out.");
        info.append("#\n");
        info.append("# protocol: FTP, SFTP, SMB or NFS (uppercase)\n");
        info.append("# host: hostname or ip for ftp + sftp, valid url for smb + nfs  (\"(LAN)\" for smb lan browsing)\n");
        info.append("# user, password: the login data\n");
        info.append("# port: this must be a number (even if it is not used for smb+nfs, set it in the url for nfs)\n");
        info.append("# dir/domain: inital directory for the connection, domainname for smb\n");
        info.append("# local: \"true\" if connection should be opened in local tab, \"false\" otherwise\n");
        info.append("# directories must be included in <dir></dir> tags and can be ended" +
                    " using a single\n# <enddir> tag");
        info.append("#\n");
        info.append("#\n");
        info.append("\n<dir>JFtp</dir>\n");
        info.append("FTP#upload.sourceforge.net#anonymous#j-ftp@sf.net#21#/incoming#false\n");
        info.append("<enddir>\n");
        info.append("\n");
        info.append("FTP#ftp.kernel.org#anonymous#j-ftp@sf.net#21#/pub/linux/kernel/v2.6#false\n");
        info.append("\n");
        info.append("SMB#(LAN)#guest#guest#-1#-#false\n\n");
    }

    private void load(String file)
    {
        String data = "";
        String now = "";

        try
        {
            DataInput in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

            while((data = in.readLine()) != null)
            {
                now = now + data + "\n";
            }
        }
        catch(IOException e)
        {
            Log.debug("No bookmarks.txt found, using defaults.");

            setDefaultText();

            return;
        }

        info.setText(now);
    }

    private void save(String file)
    {
        try
        {
            PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(file)));

            out.println(info.getText());
            out.flush();
            out.close();
        }
        catch(IOException e)
        {
            Log.debug(e + " @BookmarkManager.save()");
        }
    }

    public Insets getInsets()
    {
        Insets std = super.getInsets();

        return new Insets(std.top + 5, std.left + 5, std.bottom + 5,
                          std.right + 5);
    }
}
