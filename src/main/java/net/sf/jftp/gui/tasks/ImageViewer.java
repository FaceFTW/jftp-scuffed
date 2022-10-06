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

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.net.URL;


class ImageViewer extends JInternalFrame {
	public ImageViewer(String img) {
		super(img, true, true, true, true);
		this.setLocation(150, 50);
		this.setSize(400, 300);

		this.setLayout(new BorderLayout(2, 2));

		ImagePanel p = new ImagePanel(img);
		JScrollPane scroll = new JScrollPane(p);

		this.getContentPane().add("Center", scroll);

		p.setMinimumSize(new Dimension(1500, 1500));
		p.setPreferredSize(new Dimension(1500, 1500));
		p.setMaximumSize(new Dimension(1500, 1500));

		this.setVisible(true);

	}
}


class ImagePanel extends JPanel {
	private Image img;

	ImagePanel(String url) {
		super();
		try {
			this.setBackground(Color.white);

			this.img = Toolkit.getDefaultToolkit().getImage(new URL(url));

			MediaTracker mt = new MediaTracker(this);
			mt.addImage(this.img, 1);
			mt.waitForAll();

			this.repaint();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void paintComponent(Graphics g) {
		g.setColor(Color.white);
		g.fillRect(0, 0, 1500, 1500);
		g.drawImage(this.img, 0, 0, null);
	}

	public void update(Graphics g) {
		this.paintComponent(g);
	}
}
