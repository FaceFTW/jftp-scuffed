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


public class ProxyChooser extends net.sf.jftp.gui.framework.HPanel implements ActionListener {
	private final net.sf.jftp.gui.framework.HTextField proxy;
	private final net.sf.jftp.gui.framework.HTextField port;
	private final net.sf.jftp.gui.framework.HButton ok = new net.sf.jftp.gui.framework.HButton("Ok");

	public ProxyChooser() {
		//setSize(500,120);
		//setTitle("Proxy settings...");
		//setLocation(50,150);
		//getContentPane().
		this.setLayout(new FlowLayout(FlowLayout.LEFT));

		proxy = new net.sf.jftp.gui.framework.HTextField("Socks proxy:", "");
		port = new net.sf.jftp.gui.framework.HTextField("Port:", "");

		proxy.setText(net.sf.jftp.config.Settings.getSocksProxyHost());
		port.setText(net.sf.jftp.config.Settings.getSocksProxyPort());

		//getContentPane().
		this.add(proxy);

		//getContentPane().
		this.add(port);

		//getContentPane().
		this.add(ok);

		//getContentPane().
		this.add(new JLabel("Please note that you have to restart JFtp to apply the changes!"));
		ok.addActionListener(this);

		//setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == ok) {
			//setVisible(false);
			String h = proxy.getText().trim();
			String p = port.getText().trim();

			java.util.Properties sysprops = System.getProperties();

			// Remove previous values
			sysprops.remove("socksProxyHost");
			sysprops.remove("socksProxyPort");

			net.sf.jftp.config.Settings.setProperty("jftp.socksProxyHost", h);
			net.sf.jftp.config.Settings.setProperty("jftp.socksProxyPort", p);
			net.sf.jftp.config.Settings.save();

			net.sf.jftp.system.logging.Log.out("proxy vars: " + h + ":" + p);

			if (h.isEmpty() || p.isEmpty()) {
				return;
			}

			// Set your values
			sysprops.put("socksProxyHost", h);
			sysprops.put("socksProxyPort", p);

			net.sf.jftp.system.logging.Log.out("new proxy vars set.");

			this.remove(3);
			this.add(new JLabel("Options set. Please restart JFtp."));
			this.validate();
			this.setVisible(true);
		}
	}
}
