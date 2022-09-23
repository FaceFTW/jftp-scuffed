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
package net.sf.jftp.gui.framework;

import javax.swing.*;
import java.awt.*;


public class HFrame extends JDialog {
	public HFrame() {
		setFont(GUIDefaults.font);
		setTitle("JFtp...");
		setBackground(new JLabel().getBackground());
		setResizable(net.sf.jftp.config.Settings.resize);

		//setModal(true);
		fixLocation();
	}

	public static void fixLocation(Window w) {
		Toolkit tk = Toolkit.getDefaultToolkit();

		w.setLocation((tk.getScreenSize().width - w.getSize().width) / 2, (tk.getScreenSize().height - w.getSize().height) / 2);
	}

	public void fixLocation() {
		Toolkit tk = Toolkit.getDefaultToolkit();

		setLocation((tk.getScreenSize().width - getSize().width) / 2, (tk.getScreenSize().height - getSize().height) / 2);
	}

	/*
	public Insets getInsets()
	{
		Insets in = super.getInsets();

		return new Insets(in.top + 5, in.left + 5, in.bottom + 5, in.right + 5);
	}
	*/
}
