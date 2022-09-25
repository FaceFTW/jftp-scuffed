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
		setMinimumSize(new Dimension(200, 25));
		setPreferredSize(new Dimension(320, 25));
		setLayout(null);

		//setLayout(new FlowLayout(FlowLayout.LEFT));
		//add(host);
		//add(separator);
		//add(text);
		setVisible(true);
	}

	public void setInterval(int x) {
		interval = x;
	}

	public void forward() {
		fwd = true;
	}

	public void setText(String msg) {
		if (false) {
			text.setText(msg);
		} else {
			text.setText("");
			drawText = msg;
			slide = false;

			if (AppMenuBar.fadeMenu.getState()) {
				SwingUtilities.invokeLater(() -> {
					for (pos = 30; pos > 0; pos -= 1) {
						paintImmediately(0, 0, getSize().width, getSize().height);
						net.sf.jftp.system.LocalIO.pause(interval);
					}
				});
			} else {
				repaint();
			}
		}

		validate();
	}

	public void scrollText(String msg) {
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

					//paintImmediately(0, 0, getSize().width, getSize().height);
					repaint();
					net.sf.jftp.system.LocalIO.pause(interval);

					//JFtp.statusP.jftp.validate();
				}
			} else {
				repaint();
			}

			fwd = false;
		}

		validate();
	}

	public String getHost() {
		return host.getText();
	}

	public void setHost(String h) {
		host.setText(h);
		validate();
	}

	public void fresh() {
		image = null;
		offg = null;

		repaint();
	}

	public void paintComponent(Graphics g) {
		if (image == null) {
			image = createImage(getWidth(), getHeight());
		}

		if (offg == null) {
			offg = image.getGraphics();
		}

		offg.setColor(GUIDefaults.light);
		offg.setFont(GUIDefaults.status);
		offg.fillRect(0, 0, getSize().width, getSize().height);
		offg.setColor(new Color(125, 125, 175));

		if (!slide) {
			offg.drawString(drawText, 10, 15 + pos);
		} else {
			offg.drawString(drawText, 10 + pos, 15);
		}

		offg.setColor(GUIDefaults.front);
		offg.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
		offg.drawRect(0, 0, getSize().width - 2, getSize().height - 2);
		g.drawImage(image, 0, 0, this);
	}

	public void update(Graphics g) {
		paintComponent(g);
	}
}
