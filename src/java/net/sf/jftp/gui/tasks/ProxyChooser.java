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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;

import net.sf.jftp.config.Settings;
import net.sf.jftp.gui.framework.HButton;
import net.sf.jftp.gui.framework.HPanel;
import net.sf.jftp.gui.framework.HTextField;
import net.sf.jftp.system.logging.Log;


public class ProxyChooser extends HPanel implements ActionListener
{
    private final HTextField proxy;
    private final HTextField port;
    private final HButton ok = new HButton("Ok");

    public ProxyChooser()
    {
        //setSize(500,120);
        //setTitle("Proxy settings...");
        //setLocation(50,150);
        //getContentPane().
        setLayout(new FlowLayout(FlowLayout.LEFT));

        proxy = new HTextField("Socks proxy:", "");
        port = new HTextField("Port:", "");

        proxy.setText(Settings.getSocksProxyHost());
        port.setText(Settings.getSocksProxyPort());

        //getContentPane().
        add(proxy);

        //getContentPane().
        add(port);

        //getContentPane().
        add(ok);

        //getContentPane().
        add(new JLabel("Please note that you have to restart JFtp to apply the changes!"));
        ok.addActionListener(this);

        //setVisible(true);
    }

    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource() == ok)
        {
            //setVisible(false);
            String h = proxy.getText().trim();
            String p = port.getText().trim();

            java.util.Properties sysprops = System.getProperties();

            // Remove previous values
            sysprops.remove("socksProxyHost");
            sysprops.remove("socksProxyPort");

            Settings.setProperty("jftp.socksProxyHost", h);
            Settings.setProperty("jftp.socksProxyPort", p);
            Settings.save();

            Log.out("proxy vars: " + h + ":" + p);

            if(h.equals("") || p.equals(""))
            {
                return;
            }

            // Set your values
            sysprops.put("socksProxyHost", h);
            sysprops.put("socksProxyPort", p);

            Log.out("new proxy vars set.");

            remove(3);
            add(new JLabel("Options set. Please restart JFtp."));
            validate();
            setVisible(true);
        }
    }
}
