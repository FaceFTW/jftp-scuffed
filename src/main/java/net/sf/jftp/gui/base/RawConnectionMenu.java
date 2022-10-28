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

import net.sf.jftp.JFtp;
import net.sf.jftp.gui.framework.HButton;
import net.sf.jftp.gui.framework.HPanel;
import net.sf.jftp.gui.framework.HTextField;
import net.sf.jftp.net.wrappers.HttpTransfer;
import net.sf.jftp.util.I18nHelper;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;


public class RawConnectionMenu extends HPanel implements ActionListener {
	private final HTextField text;
	private final HButton ok = new HButton("send");

	public RawConnectionMenu() {
		super();
		this.setLayout(new FlowLayout());

		this.text = new HTextField(I18nHelper.getUIString("command"), I18nHelper.getUIString("send"), 25);

		//getContentPane().
		this.add(this.text);

		//getContentPane().
		this.add(this.ok);
		this.ok.addActionListener(this);
		this.text.text.addActionListener(this);

		this.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if ((e.getSource() == this.ok) || (e.getSource() == this.text.text)) {
            JFtp.remoteDir.getCon().sendRawCommand(this.text.getText());
			net.sf.jftp.JFtp.statusP.jftp.removeFromDesktop(this.hashCode());
		}
	}
}
