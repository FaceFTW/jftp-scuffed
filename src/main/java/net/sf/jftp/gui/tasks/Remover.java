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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class Remover extends net.sf.jftp.gui.framework.HFrame implements ActionListener {
	private final net.sf.jftp.gui.framework.HTextField text;
	private final net.sf.jftp.gui.framework.HButton ok = new net.sf.jftp.gui.framework.HButton("Remove file/directory...");
	private final net.sf.jftp.gui.framework.HButton cancel = new net.sf.jftp.gui.framework.HButton("Cancel");
	private String type = null;

	public Remover(final String l, final String type) {
		this.type = type;

		this.setSize(350, 100);
		this.setTitle("Choose...");
		this.setLocation(150, 150);
		this.getContentPane().setLayout(new BorderLayout(10, 10));

		this.text = new net.sf.jftp.gui.framework.HTextField(l, "");
		final net.sf.jftp.gui.framework.HPanel okP = new net.sf.jftp.gui.framework.HPanel();
		okP.add(this.ok);
		okP.add(this.cancel);
		this.getContentPane().add("North", this.text);
		this.getContentPane().add("South", okP);
		this.ok.addActionListener(this);
		this.cancel.addActionListener(this);

		this.setVisible(true);
	}

	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == this.ok) {
			this.setVisible(false);

			String tmp = this.text.getText();

			if (!tmp.endsWith("/")) {
				tmp = tmp + "/";
			}

			final AutoRemover armv = new AutoRemover(tmp, this.type);

			if (this.type.equals("local")) {
				net.sf.jftp.JFtp.localUpdate();
			}

			if (this.type.equals("remote")) {
				net.sf.jftp.JFtp.remoteUpdate();
			}
		}

		if (e.getSource() == this.cancel) {
			this.dispose();
		}
	}
}
