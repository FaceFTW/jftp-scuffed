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
import java.awt.Color;
import java.awt.Graphics;


class BorderPanel extends JPanel {
	private final boolean left = true;

	public void paintComponent(Graphics g) {
		if (this.left) {
			g.setColor(Color.black);
			g.drawLine(2, 2, 2, this.getSize().height - 3);
			g.drawLine(2, 2, this.getSize().width - 3, 2);

			g.setColor(new Color(80, 80, 80));
			g.drawLine(1, 1, 1, this.getSize().height - 2);
			g.drawLine(1, 1, this.getSize().width - 2, 1);

			g.setColor(new Color(180, 180, 180));
			g.drawLine(0, 0, 0, this.getSize().height - 1);
			g.drawLine(0, 0, this.getSize().width - 1, 0);
		} else {
			g.setColor(Color.black);
			g.drawLine(this.getSize().width - 1, 2, this.getSize().width - 1, this.getSize().height - 3);
			g.drawLine(0, 2, this.getSize().width - 3, 2);

			g.setColor(new Color(80, 80, 80));
			g.drawLine(this.getSize().width - 2, 1, this.getSize().width - 2, this.getSize().height - 2);
			g.drawLine(1, 1, this.getSize().width - 2, 1);

			g.setColor(new Color(180, 180, 180));
			g.drawLine(this.getSize().width - 3, 0, this.getSize().width - 3, this.getSize().height - 1);
			g.drawLine(0, 0, this.getSize().width - 1, 0);
		}
	}
}
