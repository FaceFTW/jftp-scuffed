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
package net.sf.jftp.tools;

import net.sf.jftp.gui.framework.HImageButton;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Enumeration;


public class RSSFeeder extends JPanel implements Runnable, ActionListener {
	public static String urlstring = net.sf.jftp.config.Settings.getRSSFeed();
	final net.sf.jftp.gui.base.StatusCanvas can = new net.sf.jftp.gui.base.StatusCanvas();
	final HImageButton next = new HImageButton(net.sf.jftp.config.Settings.nextRSSImage, "nextRSS", "Display next RSS news item", this);
	final int HEADER_IVAL = 4000;
	final int LOAD_IVAL = 31 * 60000;
	Thread runner;
	URL url;
	RSSParser parser;
	boolean header;
	boolean breakHeader;

	public RSSFeeder() {
		super();
		this.setLayout(new BorderLayout(0, 0));
		this.next.setPreferredSize(new Dimension(22, 22));
		this.next.setMaximumSize(new Dimension(22, 22));
		this.add("West", this.next);
		this.add("Center", this.can);
		this.setPreferredSize(new Dimension(500, 25));
		this.runner = new Thread(this);
		this.runner.start();
	}

	public void switchTo(String u) {
		if (null == u) {
			return;
		}

		urlstring = u;

		this.runner.stop();
		this.runner = new Thread(this);
		this.runner.start();
	}

	public void run() {
		long time;

		net.sf.jftp.system.LocalIO.pause(3000);

		net.sf.jftp.system.logging.Log.out("Starting RSS Feed");

		try {
			this.can.setInterval(10);
			this.url = new URL(urlstring);
			this.parser = new RSSParser(this.url);
			time = System.currentTimeMillis();
		} catch (Exception ex) {
			net.sf.jftp.system.logging.Log.debug("Error: Can't load RSS feed (" + ex + ")");
			ex.printStackTrace();

			return;
		}

		while (true) {
			try {
				Enumeration e = this.parser.titles.elements();
				Enumeration e2 = this.parser.descs.elements();

				while (e.hasMoreElements()) {
					this.can.setText((String) e.nextElement());
					this.next.setEnabled(true);
					this.header = true;

					int i = 0;

					while (!this.breakHeader && (100 > i)) {
						net.sf.jftp.system.LocalIO.pause(this.HEADER_IVAL / 100);
						i++;
					}

					this.next.setEnabled(false);
					this.breakHeader = false;
					this.header = false;

					if (e2.hasMoreElements()) {
						this.next.setEnabled(true);
						this.can.scrollText((String) e2.nextElement());
						this.next.setEnabled(false);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			if (System.currentTimeMillis() > (this.LOAD_IVAL + time)) {
				this.parser = new RSSParser(this.url);
				time = System.currentTimeMillis();
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.next) {
			if (this.header) {
				this.breakHeader = true;
			} else {
				this.can.forward();
			}
		}
	}
}
