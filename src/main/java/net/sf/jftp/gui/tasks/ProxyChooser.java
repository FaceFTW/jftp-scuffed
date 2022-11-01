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

import net.sf.jftp.config.Settings;
import net.sf.jftp.gui.framework.HButton;
import net.sf.jftp.gui.framework.HPanel;
import net.sf.jftp.gui.framework.HTextField;
import net.sf.jftp.system.logging.Log;
import net.sf.jftp.util.I18nHelper;

import javax.swing.*;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;


public class ProxyChooser extends HPanel implements ActionListener {
	private final HTextField proxy;
	private final HTextField port;
	private final HButton ok = new HButton(I18nHelper.getUIString("ok1"));

	public ProxyChooser() {
		super();
		//setSize(500,120);
		//setTitle("Proxy settings...");
		//setLocation(50,150);
		//getContentPane().
		this.setLayout(new FlowLayout(FlowLayout.LEFT));

		this.proxy = new HTextField(I18nHelper.getUIString("socks.proxy"), "");
		this.port = new HTextField(I18nHelper.getUIString("port1"), "");

		this.proxy.setText(Settings.getSocksProxyHost());
		this.port.setText(Settings.getSocksProxyPort());

		//getContentPane().
		this.add(this.proxy);

		//getContentPane().
		this.add(this.port);

		//getContentPane().
		this.add(this.ok);

		//getContentPane().
		this.add(new JLabel(I18nHelper.getUIString("please.note.that.you.have.to.restart.jftp.to.apply.the.changes")));
		this.ok.addActionListener(this);

		//setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.ok) {
			//setVisible(false);
			String h = this.proxy.getText().trim();
			String p = this.port.getText().trim();

			java.util.Properties sysprops = System.getProperties();

			// Remove previous values
			sysprops.remove("socksProxyHost");
			sysprops.remove("socksProxyPort");

			Settings.setProperty("jftp.socksProxyHost", h);
			Settings.setProperty("jftp.socksProxyPort", p);
			Settings.save();

			Log.out(MessageFormat.format(I18nHelper.getLogString("proxy.vars.0.1"), h, p));

			if (h.isEmpty() || p.isEmpty()) {
				return;
			}

			// Set your values
			sysprops.put("socksProxyHost", h);
			sysprops.put("socksProxyPort", p);

			Log.out(I18nHelper.getLogString("new.proxy.vars.set"));

			this.remove(3);
			this.add(new JLabel(I18nHelper.getUIString("options.set.please.restart.jftp")));
			this.validate();
			this.setVisible(true);
		}
	}
}
