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
	private final JPanel p1 = new JPanel();
	private final HTextField com = new HTextField("Command:", "", 20);
	private final JPanel p2 = new JPanel();
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

	public RawConnection(String hostname, int p) {
		host.setText(hostname);

		setSize(550, 300);
		setLocation(150, 150);
		setTitle("Direct TCP/IP connection");
		getContentPane().setLayout(new BorderLayout(2, 2));

		p1.add(host);
		p1.add(port);
		host.text.setEditable(false);
		port.text.setEditable(false);
		port.setText(Integer.toString(p));

		com.text.addActionListener(this);

		p2.add(com);

		com.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					transmit();
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

		getContentPane().add("North", p1);
		getContentPane().add("Center", outputPane);
		getContentPane().add("South", p2);

		com.setText("");

		file.add(close);
		close.addActionListener(this);
		session.add(changeHost);
		changeHost.addActionListener(this);
		about.add(info);
		info.addActionListener(this);

		//mb.add(file);
		session.add(close);
		mb.add(session);

		//mb.add(about);
		setJMenuBar(mb);

		addWindowListener(this);
		setVisible(true);

		JHostChooser jhc = new JHostChooser();
	}

	private void transmit() {
		if (!established) {
			c = new JRawConnection(host.getText(), Integer.parseInt(port.getText()), true);

			if (c.isThere()) {
				c.send(com.getText());
				established = true;
			} else {
				debugWrite("No connection!");
			}
		} else {
			if (c.isThere()) {
				c.send(com.getText());
			} else {
				debugWrite("No connection!");
			}
		}

		com.setText("");
	}

	public void actionPerformed(ActionEvent e) {
		if ((e.getSource() == send) || (e.getSource() == com.text)) {
			transmit();
		}

		if (e.getSource() == clear) {
			output.setText("");
		}

		if (e.getSource() == close) {
			this.dispose();
		}

		if (e.getSource() == changeHost) {
			JHostChooser jhc = new JHostChooser();
		}
	}

	private void debugWrite(String str) {
		output.append(str + "\n");
	}

	public void windowClosing(WindowEvent e) {
		if (mayDispose) {
			this.dispose();
		}
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}
}
