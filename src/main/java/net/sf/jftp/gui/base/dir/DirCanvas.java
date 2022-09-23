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
	JLabel text = new JLabel(" ");
	boolean active = false;


	public DirCanvas(Dir target) {
		this.target = target;
		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(text);
		addMouseListener(this);
		setVisible(true);
	}

	public void mouseClicked(MouseEvent e) {
		if (target.getType().equals("local") || target.getCon() instanceof FilesystemConnection) {
			String tmp = UITool.getPathFromDialog(Settings.defaultWorkDir);

			if (tmp != null) {
				target.setPath(tmp);
				target.fresh();
			}
		} else {
			PathChanger p = new PathChanger("remote");
			target.fresh();
		}
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		active = true;
		repaint();
	}

	public void mouseExited(MouseEvent e) {
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		active = false;
		repaint();
	}

	public void setText(String msg) {
		text.setText(msg);
		validate();
	}

	public void paintComponent(Graphics g) {
		if (!active) {
			g.setColor(GUIDefaults.light);
		} else {
			g.setColor(GUIDefaults.lightActive);
		}
		g.fillRect(3, 0, getSize().width - 6, getSize().height);
		g.setColor(GUIDefaults.front);
		g.drawRect(3, 0, getSize().width - 7, getSize().height - 1);
		g.drawRect(3, 0, getSize().width - 8, getSize().height - 2);
	}

	public Insets getInsets() {
		Insets in = super.getInsets();

		return new Insets(in.top, in.left, in.bottom, in.right);
	}
}
