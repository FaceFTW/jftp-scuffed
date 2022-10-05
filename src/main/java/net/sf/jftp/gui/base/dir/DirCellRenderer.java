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
import net.sf.jftp.gui.base.LocalDir;
import net.sf.jftp.gui.base.RemoteDir;
import net.sf.jftp.gui.framework.GUIDefaults;
import net.sf.jftp.system.logging.Log;

import javax.swing.*;
import java.awt.Component;


// Display an icon and a string for each object in the list.
// class DirCellRenderer extends JLabel implements ListCellRenderer {
public class DirCellRenderer extends DefaultListCellRenderer {
	static final ImageIcon longIcon = new ImageIcon(Settings.dirImage);
	static final ImageIcon shortIcon = new ImageIcon(Settings.fileImage);

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
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		ImageIcon i = new ImageIcon(((DirEntry) value).getImage());

		if (null == i) {
			System.out.println("Img null: " + value.toString() + "/" + ((DirEntry) value).getImage());
		} else {
			this.setIcon(i);
		}

		Log.devnull(value); // prevents eclipse warning

		if (((DirEntry) list.getModel().getElementAt(index)).getNoRender()) {
			this.setFont(GUIDefaults.small);

			String s = value.toString();
			this.setText(s);
		} else {
			StringBuilder size = new StringBuilder();

			if (Settings.showFileSize) {
				size = new StringBuilder(((DirEntry) list.getModel().getElementAt(index)).getFileSize());
			}

			this.setFont(GUIDefaults.monospaced);

			String s = value.toString();

			final String date = "";

			if (Settings.showDateNoSize && (((DirEntry) list.getModel().getElementAt(index)).who instanceof RemoteDir)) {
				size = new StringBuilder(((DirEntry) list.getModel().getElementAt(index)).getDate());

				int x = 12 - size.length();

				for (int j = 0; j < x; j++) {
					size.append(" ");
				}
			} else if (Settings.showLocalDateNoSize && (((DirEntry) list.getModel().getElementAt(index)).who instanceof LocalDir)) {
				size = new StringBuilder(((DirEntry) list.getModel().getElementAt(index)).getDate());

				int x = 12 - size.length();

				for (int j = 0; j < x; j++) {
					size.append(" ");
				}
			}

			this.setText(size + s);
		}

		int ok = ((DirEntry) value).getPermission();

		if (DirEntry.DENIED == ok) {
			this.setForeground(GUIDefaults.deniedColor);
		} else if (DirEntry.W == ok) {
			this.setForeground(GUIDefaults.writableColor);
		} else {
			this.setForeground(GUIDefaults.defaultColor);
		}

		if (((DirEntry) value).file.equals("..")) {
			this.setBackground(GUIDefaults.cdColor);
		}

		return this;
	}
}
