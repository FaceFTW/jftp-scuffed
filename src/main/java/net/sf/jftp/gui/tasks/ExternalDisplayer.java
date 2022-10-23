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

import net.sf.jftp.gui.framework.HFrame;
import net.sf.jftp.gui.framework.HPanel;
import net.sf.jftp.system.logging.Log;
import net.sf.jftp.util.I18nHelper;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.text.MessageFormat;


public class ExternalDisplayer extends HFrame implements ActionListener {
	private final JTextArea info = new JTextArea(25, 50);
	private final JButton close = new JButton(I18nHelper.getUIString("close"));

	public ExternalDisplayer(java.net.URL file) {
		super();
		this.setTitle(I18nHelper.getUIString("info1"));
		this.setLocation(50, 50);
		this.setSize(600, 540);
		this.getContentPane().setLayout(new BorderLayout());

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				ExternalDisplayer.this.dispose();
			}
		});
		this.load(file);
		this.info.setEditable(false);

		JScrollPane jsp = new JScrollPane(this.info);
		this.getContentPane().add(I18nHelper.getUIString("center"), jsp);

		HPanel closeP = new HPanel();
		closeP.setLayout(new FlowLayout(FlowLayout.CENTER));
		closeP.add(this.close);

		this.close.addActionListener(this);

		this.getContentPane().add(I18nHelper.getUIString("south"), closeP);

		this.info.setCaretPosition(0);
		this.setVisible(true);
		this.pack();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.close) {
			this.dispose();
		}
	}

	private void load(java.net.URL file) {
		String data = "";
		StringBuilder now = new StringBuilder();

		try {
			DataInput in = new DataInputStream(new BufferedInputStream(file.openStream()));

			while (null != (data = in.readLine())) {
				now.append(data).append("\n");
			}
		} catch (IOException e) {
			Log.debug(MessageFormat.format(I18nHelper.getLogString("0.displayer.load"), e));
		}

		this.info.setText(now.toString());
	}

	public Insets getInsets() {
		Insets std = super.getInsets();

		return new Insets(std.top + 5, std.left + 5, std.bottom + 5, std.right + 5);
	}
}
