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
package net.sf.jftp.gui.base;

import net.sf.jftp.config.Settings;
import net.sf.jftp.gui.framework.GUIDefaults;

import javax.swing.*;
import java.awt.*;


public class StatusCanvas extends JPanel {
	final JLabel host = new JLabel("");
	JLabel separator = new JLabel("   ");
	final JLabel text = new JLabel(Settings.greeting);
	String drawText = "";
	int pos = 0;
	Image image;
	Graphics offg;
	boolean slide = false;
	int interval = 3;
	boolean fwd = false;

	public StatusCanvas() {
		this.setMinimumSize(new Dimension(200, 25));
		this.setPreferredSize(new Dimension(320, 25));
		this.setLayout(null);
		this.setVisible(true);
	}

	public void setInterval(final int x) {
		interval = x;
	}

	public void forward() {
		fwd = true;
	}

	public void setText(final String msg) {
		if (false) {
			text.setText(msg);
		} else {
			text.setText("");
			drawText = msg;
			slide = false;

			if (AppMenuBar.fadeMenu.getState()) {
				SwingUtilities.invokeLater(() -> {
					for (pos = 30; pos > 0; pos -= 1) {
						this.paintImmediately(0, 0, this.getSize().width, this.getSize().height);
						net.sf.jftp.system.LocalIO.pause(interval);
					}
				});
			} else {
				this.repaint();
			}
		}

		this.validate();
	}

	public void scrollText(final String msg) {
		if (false) {
			text.setText(msg);
		} else {
			text.setText("");
			drawText = msg;
			slide = true;

			if (AppMenuBar.fadeMenu.getState()) {
				for (pos = 700; pos > (msg.length() * -6); pos -= 1) {
					if (fwd) {
						break;
					}
					this.repaint();
					net.sf.jftp.system.LocalIO.pause(interval);

				}
			} else {
				this.repaint();
			}

			fwd = false;
		}

		this.validate();
	}

	public String getHost() {
		return host.getText();
	}

	public void setHost(final String h) {
		host.setText(h);
		this.validate();
	}

	public void fresh() {
		image = null;
		offg = null;

		this.repaint();
	}

	public void paintComponent(final Graphics g) {
		if (image == null) {
			image = this.createImage(this.getWidth(), this.getHeight());
		}

		if (offg == null) {
			offg = image.getGraphics();
		}

		offg.setColor(GUIDefaults.light);
		offg.setFont(GUIDefaults.status);
		offg.fillRect(0, 0, this.getSize().width, this.getSize().height);
		offg.setColor(new Color(125, 125, 175));

		if (!slide) {
			offg.drawString(drawText, 10, 15 + pos);
		} else {
			offg.drawString(drawText, 10 + pos, 15);
		}

		offg.setColor(GUIDefaults.front);
		offg.drawRect(0, 0, this.getSize().width - 1, this.getSize().height - 1);
		offg.drawRect(0, 0, this.getSize().width - 2, this.getSize().height - 2);
		g.drawImage(image, 0, 0, this);
	}

	public void update(final Graphics g) {
		this.paintComponent(g);
	}
}
