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
package view.gui.framework;

import controller.util.I18nHelper;
import view.JFtp;

import javax.swing.*;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.MessageFormat;


public class HImageButton extends JButton implements MouseListener {
	private String label = "";
	private ActionListener who;
	private String cmd = "default";

	public HImageButton(String image, String cmd, String label, ActionListener who) {
		super();
		this.cmd = cmd;
		this.label = label;
		this.who = who;

		try {
			this.setIcon(new ImageIcon(HImage.getImage(this, image)));
		} catch (Exception ex) {
			System.out.println(MessageFormat.format(I18nHelper.getLogString("image.file.0"), image));
			ex.printStackTrace();
		}
		this.addMouseListener(this);

		this.setVisible(true);
		this.setMinimumSize(new Dimension(25, 25));
		this.setPreferredSize(new Dimension(25, 25));
		this.setMaximumSize(new Dimension(25, 25));
	}

	public void update(Graphics g) {
		this.paintComponent(g);
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
		this.who.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, this.cmd));

	}

	public void mouseEntered(MouseEvent e) {
		this.setCursor(new Cursor(Cursor.HAND_CURSOR));

		JFtp.statusP.status(this.label);
	}

	public void mouseExited(MouseEvent e) {
		this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

		JFtp.statusP.status("");
	}
}
