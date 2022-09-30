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
package net.sf.jftp.util;

import net.sf.jftp.gui.framework.HFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class JHostChooser extends HFrame implements ActionListener {
	private final JTextField host = new JTextField(20);
	private final JTextField port = new JTextField(5);
	private final JButton ok = new JButton("Use these settings");

	public JHostChooser() {
		this.setSize(400, 120);
		this.setLocation(200, 250);
		this.setTitle("Connection...");
		this.getContentPane().setLayout(new BorderLayout());
		this.setBackground(Color.lightGray);

		final javax.swing.JPanel p1 = new javax.swing.JPanel();
		final javax.swing.JLabel hostL = new javax.swing.JLabel("Host:");
		p1.add(hostL);
		p1.add(host);
		final javax.swing.JLabel portL = new javax.swing.JLabel("Port:");
		p1.add(portL);
		p1.add(port);

		host.setText(RawConnection.host.getText());
		port.setText(RawConnection.port.getText());

		this.getContentPane().add("Center", p1);
		final javax.swing.JPanel okP = new javax.swing.JPanel();
		this.getContentPane().add("South", okP);
		okP.add(ok);
		ok.addActionListener(this);

		this.setVisible(true);
	}

	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == ok) {
			RawConnection.host.setText(host.getText());
			RawConnection.port.setText(port.getText());
			RawConnection.established = false;
			RawConnection.mayDispose = true;
			this.dispose();
		}
	}

	public Insets getInsets() {
		final Insets std = super.getInsets();

		return new Insets(std.top + 5, std.left + 5, std.bottom + 5, std.right + 5);
	}
}
