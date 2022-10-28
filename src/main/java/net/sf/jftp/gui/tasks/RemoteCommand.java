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

import net.sf.jftp.JFtp;
import net.sf.jftp.gui.framework.HButton;
import net.sf.jftp.gui.framework.HFrame;
import net.sf.jftp.gui.framework.HTextField;
import net.sf.jftp.system.logging.Log;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class RemoteCommand extends HFrame implements ActionListener {
	private final HTextField text;
	private final HButton ok = new HButton("Execute");

	public RemoteCommand() {
		super();
		//setSize(400, 80);
		this.setTitle("Choose command...");
		this.setLocation(150, 150);
		this.getContentPane().setLayout(new FlowLayout());

		this.text = new HTextField("Command:", "SITE CHMOD 755 file", 30);
		this.getContentPane().add(this.text);
		this.getContentPane().add(this.ok);
		this.ok.addActionListener(this);
		this.text.text.addActionListener(this);

		this.pack();
		this.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if ((e.getSource() == this.ok) || (e.getSource() == this.text.text)) {
			this.setVisible(false);

			String cmd = this.text.getText();
			Log.debug("-> " + cmd);
			JFtp.remoteDir.getCon().sendRawCommand(cmd);

			if (cmd.toUpperCase().trim().startsWith("QUIT")) {
				JFtp.safeDisconnect();

				return;
			}

			JDialog j = new JDialog();
			j.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
			j.setTitle("Command response");
			j.setLocation(150, 150);

			JTextArea t = new JTextArea();
			JScrollPane logSp = new JScrollPane(t);
			logSp.setMinimumSize(new Dimension(300, 200));

			t.setText(Log.getCache());
			j.getContentPane().setLayout(new BorderLayout(5, 5));
			j.getContentPane().add(JFtp.CENTER, t);
			j.setSize(new Dimension(400, 300));
			j.pack();
			j.setVisible(true);

			JFtp.remoteUpdate();
		}
	}
}
