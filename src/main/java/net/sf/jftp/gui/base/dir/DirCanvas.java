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
import net.sf.jftp.gui.base.UITool;
import net.sf.jftp.gui.framework.GUIDefaults;
import net.sf.jftp.gui.tasks.PathChanger;
import net.sf.jftp.net.FilesystemConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


public class DirCanvas extends JPanel implements MouseListener {
	private final Dir target;
	final JLabel text = new JLabel(" ");
	boolean active = false;


	public DirCanvas(final Dir target) {
		this.target = target;
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.add(this.text);
		this.addMouseListener(this);
		this.setVisible(true);
	}

	public void mouseClicked(final MouseEvent e) {
		if (this.target.getType().equals("local") || this.target.getCon() instanceof FilesystemConnection) {
			final String tmp = UITool.getPathFromDialog(Settings.defaultWorkDir);

			if (null != tmp) {
				this.target.setPath(tmp);
				this.target.fresh();
			}
		} else {
			final PathChanger p = new PathChanger("remote");
			this.target.fresh();
		}
	}

	public void mousePressed(final MouseEvent e) {
	}

	public void mouseReleased(final MouseEvent e) {
	}

	public void mouseEntered(final MouseEvent e) {
		this.setCursor(new Cursor(Cursor.HAND_CURSOR));
		this.active = true;
		this.repaint();
	}

	public void mouseExited(final MouseEvent e) {
		this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		this.active = false;
		this.repaint();
	}

	public void setText(final String msg) {
		this.text.setText(msg);
		this.validate();
	}

	public void paintComponent(final Graphics g) {
		if (!this.active) {
			g.setColor(GUIDefaults.light);
		} else {
			g.setColor(GUIDefaults.lightActive);
		}
		g.fillRect(3, 0, this.getSize().width - 6, this.getSize().height);
		g.setColor(GUIDefaults.front);
		g.drawRect(3, 0, this.getSize().width - 7, this.getSize().height - 1);
		g.drawRect(3, 0, this.getSize().width - 8, this.getSize().height - 2);
	}

	public Insets getInsets() {
		final Insets in = super.getInsets();

		return new Insets(in.top, in.left, in.bottom, in.right);
	}
}
