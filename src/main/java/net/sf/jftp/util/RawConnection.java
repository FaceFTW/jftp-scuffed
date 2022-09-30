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

import net.sf.jftp.gui.framework.HTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;


public class RawConnection extends JFrame implements ActionListener, WindowListener {
	public static final HTextField host = new HTextField("Host:", "", 20);
	public static final HTextField port = new HTextField("Port:", "", 5);
	public static final JTextArea output = new JTextArea();
	public static boolean established = false;
	public static boolean mayDispose = false;
	public static JScrollPane outputPane;
	private final HTextField com = new HTextField("Command:", "", 20);
	private final JButton send = new JButton("Send");
	private final JButton clear = new JButton("Clear");
	final JMenuBar mb = new JMenuBar();
	final JMenu file = new JMenu("Prog");
	final JMenu about = new JMenu("About");
	final JMenu session = new JMenu("Session");
	final JMenuItem close = new JMenuItem("ExIt");
	final JMenuItem changeHost = new JMenuItem("Host...");
	final JMenuItem info = new JMenuItem("Info");
	private JRawConnection c;

	public RawConnection() {
		this("localhost", 25);
	}

	public RawConnection(final String hostname, final int p) {
		host.setText(hostname);

		this.setSize(550, 300);
		this.setLocation(150, 150);
		this.setTitle("Direct TCP/IP connection");
		this.getContentPane().setLayout(new BorderLayout(2, 2));

		final javax.swing.JPanel p1 = new javax.swing.JPanel();
		p1.add(host);
		p1.add(port);
		host.text.setEditable(false);
		port.text.setEditable(false);
		port.setText(Integer.toString(p));

		com.text.addActionListener(this);

		final javax.swing.JPanel p2 = new javax.swing.JPanel();
		p2.add(com);

		com.addKeyListener(new KeyAdapter() {
			public void keyReleased(final KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					net.sf.jftp.util.RawConnection.this.transmit();
				}
			}
		});

		p2.add(send);
		send.addActionListener(this);
		p2.add(clear);
		clear.addActionListener(this);

		output.setEditable(false);

		outputPane = new JScrollPane(output);
		outputPane.setMinimumSize(new Dimension(400, 300));

		this.getContentPane().add("North", p1);
		this.getContentPane().add("Center", outputPane);
		this.getContentPane().add("South", p2);

		com.setText("");

		file.add(close);
		close.addActionListener(this);
		session.add(changeHost);
		changeHost.addActionListener(this);
		about.add(info);
		info.addActionListener(this);

		session.add(close);
		mb.add(session);

		this.setJMenuBar(mb);

		this.addWindowListener(this);
		this.setVisible(true);

		final JHostChooser jhc = new JHostChooser();
	}

	private void transmit() {
		if (!established) {
			c = new JRawConnection(host.getText(), Integer.parseInt(port.getText()), true);

			if (c.isThere()) {
				c.send(com.getText());
				established = true;
			} else {
				this.debugWrite("No connection!");
			}
		} else {
			if (c.isThere()) {
				c.send(com.getText());
			} else {
				this.debugWrite("No connection!");
			}
		}

		com.setText("");
	}

	public void actionPerformed(final ActionEvent e) {
		if ((e.getSource() == send) || (e.getSource() == com.text)) {
			this.transmit();
		}

		if (e.getSource() == clear) {
			output.setText("");
		}

		if (e.getSource() == close) {
			this.dispose();
		}

		if (e.getSource() == changeHost) {
			final JHostChooser jhc = new JHostChooser();
		}
	}

	private void debugWrite(final String str) {
		output.append(str + "\n");
	}

	public void windowClosing(final WindowEvent e) {
		if (mayDispose) {
			this.dispose();
		}
	}

	public void windowIconified(final WindowEvent e) {
	}

	public void windowDeiconified(final WindowEvent e) {
	}

	public void windowClosed(final WindowEvent e) {
	}

	public void windowActivated(final WindowEvent e) {
	}

	public void windowDeactivated(final WindowEvent e) {
	}

	public void windowOpened(final WindowEvent e) {
	}
}
