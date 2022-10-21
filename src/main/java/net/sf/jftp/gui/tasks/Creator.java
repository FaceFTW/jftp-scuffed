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

import net.sf.jftp.gui.framework.HButton;
import net.sf.jftp.gui.framework.HFrame;
import net.sf.jftp.gui.framework.HTextField;
import net.sf.jftp.net.BasicConnection;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

/**
 * Create directory dialog.
 *
 * @author David Hansmann
 */
public class Creator extends HFrame implements ActionListener {
	private static final ResourceBundle uiResources = ResourceBundle.getBundle("UIText");
	private final HTextField text;
	private final HButton ok = new HButton(uiResources.getString("create.directory"));
	private final BasicConnection con;

	public Creator(String l, BasicConnection con) {
		super();
		this.con = con;

		this.setTitle(uiResources.getString("choose"));
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
