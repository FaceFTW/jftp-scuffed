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

import view.JFtp;
import view.gui.framework.HButton;
import view.gui.framework.HFrame;
import view.gui.framework.HTextField;
import controller.util.I18nHelper;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class PathChanger extends HFrame implements ActionListener {
	private final HTextField text;
	private final HButton ok = new HButton(I18nHelper.getUIString("change.directory"));
	private String type = "";

	public PathChanger(String type) {
		super();
		this.type = type;

		//setSize(400, 80);
		this.setTitle(I18nHelper.getUIString("choose.path"));
		//setLocation(150, 150);
		this.getContentPane().setLayout(new FlowLayout());

		this.text = new HTextField(I18nHelper.getUIString("path"), "");
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

			if (this.type.equals("remote")) {
				JFtp.remoteDir.getCon().chdir(this.text.getText());
			}

			if (this.type.equals("local")) {
				JFtp.localDir.getCon().chdir(this.text.getText());
			}
		}
	}
}
