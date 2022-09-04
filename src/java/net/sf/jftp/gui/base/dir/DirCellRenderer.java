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
package net.sf.jftp.gui.base.dir;

import net.sf.jftp.config.Settings;
import net.sf.jftp.system.logging.Log;
import net.sf.jftp.gui.base.LocalDir;
import net.sf.jftp.gui.base.RemoteDir;
import net.sf.jftp.gui.framework.GUIDefaults;

import java.awt.*;
import java.awt.event.*;

import java.text.*;

import java.util.*;

import javax.swing.*;


// Display an icon and a string for each object in the list.
// class DirCellRenderer extends JLabel implements ListCellRenderer {
public class DirCellRenderer extends DefaultListCellRenderer
{
    final static ImageIcon longIcon = new ImageIcon(Settings.dirImage);
    final static ImageIcon shortIcon = new ImageIcon(Settings.fileImage);
    private Object value;

    public DirCellRenderer()
    {
    }

    // This is the only method defined by ListCellRenderer.
    // We just reconfigure the JLabel each time we're called.
    public Component getListCellRendererComponent(JList list, Object value, // value to display
                                                  int index, // cell index
                                                  boolean isSelected, // is the cell selected
                                                  boolean cellHasFocus) // the list and the cell have the focus
    {
        /* The DefaultListCellRenderer class will take care of
         * the JLabels text property, it's foreground and background
         * colors, and so on.
         */
        super.getListCellRendererComponent(list, value, index, isSelected,
                                           cellHasFocus);

        ImageIcon i = new ImageIcon(((DirEntry) value).getImage());

        if(i == null)
        {
            System.out.println("Img null: " + ((DirEntry) value).toString() +
                               "/" + ((DirEntry) value).getImage());
        }
        else
        {
            setIcon((Icon) i);
        }

        this.value = value;
        Log.devnull(this.value); // prevents eclipse warning

        if(!((DirEntry) list.getModel().getElementAt(index)).getNoRender())
        {
            String size = "";

            if(Settings.showFileSize)
            {
                size = ((DirEntry) list.getModel().getElementAt(index)).getFileSize();
            }

            setFont(GUIDefaults.monospaced);

            String s = value.toString();

            String date = "";

            if(Settings.showDateNoSize &&
                   (((DirEntry) list.getModel().getElementAt(index)).who instanceof RemoteDir))
            {
                size = ((DirEntry) list.getModel().getElementAt(index)).getDate();

                int x = 12 - size.length();

                for(int j = 0; j < x; j++)
                {
                    size += " ";
                }
            }
            else if(Settings.showLocalDateNoSize &&
                        (((DirEntry) list.getModel().getElementAt(index)).who instanceof LocalDir))
            {
                size = ((DirEntry) list.getModel().getElementAt(index)).getDate();

                int x = 12 - size.length();

                for(int j = 0; j < x; j++)
                {
                    size += " ";
                }
            }

            setText(size + s);
        }
        else
        {
            setFont(GUIDefaults.small);

            String s = value.toString();
            setText(s);
        }

        int ok = ((DirEntry) value).getPermission();

        if(ok == DirEntry.DENIED)
        {
            setForeground(GUIDefaults.deniedColor);
        }
        else if(ok == DirEntry.W)
        {
            setForeground(GUIDefaults.writableColor);
        }
        else
        {
            setForeground(GUIDefaults.defaultColor);
        }

        if(((DirEntry) value).file.equals(".."))
        {
            setBackground(GUIDefaults.cdColor);
        }

        return this;
    }
}
