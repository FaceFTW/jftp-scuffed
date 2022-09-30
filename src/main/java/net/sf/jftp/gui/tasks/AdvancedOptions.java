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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class AdvancedOptions extends net.sf.jftp.gui.framework.HPanel implements ActionListener {
	public static boolean listOptionSet = false;
	private final net.sf.jftp.gui.framework.HTextField listCommand = new net.sf.jftp.gui.framework.HTextField("FTP LIST command:", net.sf.jftp.net.FtpConnection.LIST, 15);
	private final JButton setListCommand = new JButton("Set");

	private final JLabel statusText = new JLabel();

	public AdvancedOptions() {
		this.setLayout(new BorderLayout(5, 5));

		final javax.swing.JLabel text = new javax.swing.JLabel();
		text.setText("Default values for commands can be overridden here.");
		this.statusText.setText("Note: The FTP LIST command should be \"LIST\" when connecting to an OS/2 server.");

		text.setPreferredSize(new Dimension(400, 30));

		this.statusText.setPreferredSize(new Dimension(400, 30));

		String listOptionText = "";
		if (net.sf.jftp.gui.tasks.AdvancedOptions.listOptionSet) {
			listOptionText = net.sf.jftp.net.FtpConnection.LIST;
		} else {
			if (null != net.sf.jftp.config.LoadSet.loadSet(net.sf.jftp.config.Settings.adv_settings)) {
				listOptionText = net.sf.jftp.config.LoadSet.loadSet(net.sf.jftp.config.Settings.adv_settings)[0];
			} else {
				listOptionText = net.sf.jftp.net.FtpConnection.LIST_DEFAULT;

				final net.sf.jftp.config.SaveSet s = new net.sf.jftp.config.SaveSet(net.sf.jftp.config.Settings.adv_settings, net.sf.jftp.net.FtpConnection.LIST_DEFAULT);
			}
		}

		this.listCommand.setText(listOptionText);

		final net.sf.jftp.gui.framework.HPanel content = new net.sf.jftp.gui.framework.HPanel();
		final net.sf.jftp.gui.framework.HPanel panel = new net.sf.jftp.gui.framework.HPanel();
		panel.add(this.listCommand);
		panel.add(this.setListCommand);

		final javax.swing.JButton saveCommand = new javax.swing.JButton("Set and Save");
		panel.add(saveCommand);


		content.add(panel);

		this.add("North", text);
		this.add("Center", content);

		this.add("South", this.statusText);

		this.setListCommand.addActionListener(this);
		saveCommand.addActionListener(this);
	}

	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == this.setListCommand) {
			net.sf.jftp.net.FtpConnection.LIST = this.listCommand.getText().trim();

			this.statusText.setText("LIST command set.");
			net.sf.jftp.gui.tasks.AdvancedOptions.listOptionSet = true;

		}

		else {
			net.sf.jftp.net.FtpConnection.LIST = this.listCommand.getText().trim();

			net.sf.jftp.gui.tasks.AdvancedOptions.listOptionSet = true;

			final net.sf.jftp.config.SaveSet s = new net.sf.jftp.config.SaveSet(net.sf.jftp.config.Settings.adv_settings, this.listCommand.getText().trim());

			this.statusText.setText("LIST command set and saved.");


		}
	}

	public Insets getInsets() {
		return new Insets(super.getInsets().top + 5, super.getInsets().left + 5, super.getInsets().bottom + 5, super.getInsets().right + 5);
	}
}
