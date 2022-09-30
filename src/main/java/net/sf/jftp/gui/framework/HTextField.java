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

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;


public class HTextField extends JPanel {
	public JTextField text;
	private JLabel label;

	public HTextField(String l, String t, int size) {
		super();
		this.init(l, t, size, false);
	}

	public HTextField(String l, String t) {
		super();
		this.init(l, t, 12, false);
	}

	public HTextField(String l, String t, boolean isPw) {
		super();
		this.init(l, t, 12, isPw);
	}

	public void init(String l, String t, int size, boolean isPw) {
		this.setLayout(new MigLayout());

		this.label = new JLabel(l);
		this.add(this.label);

		this.text = isPw ? new JPasswordField(t, size) {
			public Insets getInsets() {
				return new Insets(4, 4, 4, 4);
			}
		} : new JTextField(t, size) {
			public Insets getInsets() {
				return new Insets(4, 4, 4, 4);
			}
		};

		this.add(this.text, "align right");

		this.setVisible(true);
	}


	public String getLabel() {
		return this.label.getText();
	}

	public void setLabel(String l) {
		this.label.setText(l + "  ");
	}

	public String getText() {
		return this.text.getText();
	}

	public void setText(String t) {
		this.text.setText(t);
	}

	public void requestFocus() {
		this.text.requestFocus();
	}

	public void setEnabled(boolean yesno) {
		this.text.setEnabled(yesno);
	}
}
