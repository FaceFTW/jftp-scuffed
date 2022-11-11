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
package view.gui.tasks;

import view.gui.framework.HButton;
import view.gui.framework.HFrame;
import view.gui.framework.HTextField;
import model.net.BasicConnection;
import controller.util.I18nHelper;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Create directory dialog.
 *
 * @author David Hansmann
 */
public class Creator extends HFrame implements ActionListener {
	private final HTextField text;
	private final HButton ok = new HButton(I18nHelper.getUIString("create.directory"));
	private final BasicConnection con;

	public Creator(String l, BasicConnection con) {
		super();
		this.con = con;

		this.setTitle(I18nHelper.getUIString("choose"));
		this.getContentPane().setLayout(new FlowLayout());

		this.text = new HTextField(l, "");
		this.getContentPane().add(this.text);
		this.getContentPane().add(this.ok);
		this.ok.addActionListener(this);
		this.text.text.addActionListener(this);

		this.pack();
		this.fixLocation();
		this.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if ((e.getSource() == this.ok) || (e.getSource() == this.text.text)) {
			this.setVisible(false);
			this.con.mkdir(this.text.getText());
		}
	}
}
