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

import net.sf.jftp.config.LoadSet;
import net.sf.jftp.config.SaveSet;
import net.sf.jftp.config.Settings;
import net.sf.jftp.gui.framework.HPanel;
import net.sf.jftp.gui.framework.HTextField;
import net.sf.jftp.net.FtpConnection;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class AdvancedOptions extends HPanel implements ActionListener {
	private static boolean listOptionSet;
	private final HTextField listCommand = new HTextField("FTP LIST command:", FtpConnection.LIST, 15);
	private final JButton setListCommand = new JButton("Set");

	private final JLabel statusText = new JLabel();

	public AdvancedOptions() {
		super();
		this.setLayout(new BorderLayout(5, 5));

		javax.swing.JLabel text = new javax.swing.JLabel();
		text.setText("Default values for commands can be overridden here.");
		this.statusText.setText("Note: The FTP LIST command should be \"LIST\" when connecting to an OS/2 server.");

		text.setPreferredSize(new Dimension(400, 30));

		this.statusText.setPreferredSize(new Dimension(400, 30));

		String listOptionText = "";
		if (listOptionSet) {
			listOptionText = FtpConnection.LIST;
		} else {
			if (null != LoadSet.loadSet(Settings.adv_settings)) {
				listOptionText = LoadSet.loadSet(Settings.adv_settings)[0];
			} else {
				listOptionText = FtpConnection.LIST_DEFAULT;

				SaveSet s = new SaveSet(Settings.adv_settings, FtpConnection.LIST_DEFAULT);
			}
		}

		this.listCommand.setText(listOptionText);

		HPanel content = new HPanel();
		HPanel panel = new HPanel();
		panel.add(this.listCommand);
		panel.add(this.setListCommand);

		javax.swing.JButton saveCommand = new javax.swing.JButton("Set and Save");
		panel.add(saveCommand);


		content.add(panel);

		this.add("North", text);
		this.add("Center", content);

		this.add("South", this.statusText);

		this.setListCommand.addActionListener(this);
		saveCommand.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.setListCommand) {
			FtpConnection.LIST = this.listCommand.getText().trim();

			this.statusText.setText("LIST command set.");
			listOptionSet = true;

		} else {
			FtpConnection.LIST = this.listCommand.getText().trim();

			listOptionSet = true;

			SaveSet s = new SaveSet(Settings.adv_settings, this.listCommand.getText().trim());

			this.statusText.setText("LIST command set and saved.");


		}
	}

	public Insets getInsets() {
		return new Insets(super.getInsets().top + 5, super.getInsets().left + 5, super.getInsets().bottom + 5, super.getInsets().right + 5);
	}
}
