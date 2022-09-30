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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Enumeration;


public class RSSFeeder extends JPanel implements Runnable, ActionListener {
	public static String urlstring = net.sf.jftp.config.Settings.getRSSFeed();
	Thread runner;
	URL url;
	RSSParser parser;
	final net.sf.jftp.gui.base.StatusCanvas can = new net.sf.jftp.gui.base.StatusCanvas();
	final HImageButton next = new HImageButton(net.sf.jftp.config.Settings.nextRSSImage, "nextRSS", "Display next RSS news item", this);
	boolean header = false;
	boolean breakHeader = false;
	final int HEADER_IVAL = 4000;
	final int LOAD_IVAL = 31 * 60000;

	public RSSFeeder() {
		this.setLayout(new BorderLayout(0, 0));
		next.setPreferredSize(new Dimension(22, 22));
		next.setMaximumSize(new Dimension(22, 22));
		this.add("West", next);
		this.add("Center", can);
		this.setPreferredSize(new Dimension(500, 25));
		runner = new Thread(this);
		runner.start();
	}

	public void switchTo(final String u) {
		if (u == null) {
			return;
		}

		net.sf.jftp.tools.RSSFeeder.urlstring = u;

		runner.stop();
		runner = new Thread(this);
		runner.start();
	}

	public void run() {
		long time;

		net.sf.jftp.system.LocalIO.pause(3000);

		net.sf.jftp.system.logging.Log.out("Starting RSS Feed");

		try {
			can.setInterval(10);
			url = new URL(net.sf.jftp.tools.RSSFeeder.urlstring);
			parser = new RSSParser(url);
			time = System.currentTimeMillis();
		} catch (final Exception ex) {
			net.sf.jftp.system.logging.Log.debug("Error: Can't load RSS feed (" + ex + ")");
			ex.printStackTrace();

			return;
		}

		while (true) {
			try {
				final Enumeration e = parser.titles.elements();
				final Enumeration e2 = parser.descs.elements();

				while (e.hasMoreElements()) {
					can.setText((String) e.nextElement());
					next.setEnabled(true);
					header = true;

					int i = 0;

					while (!breakHeader && (i < 100)) {
						net.sf.jftp.system.LocalIO.pause(HEADER_IVAL / 100);
						i++;
					}

					next.setEnabled(false);
					breakHeader = false;
					header = false;

					if (e2.hasMoreElements()) {
						next.setEnabled(true);
						can.scrollText((String) e2.nextElement());
						next.setEnabled(false);
					}
				}
			} catch (final Exception ex) {
				ex.printStackTrace();
			}

			if (System.currentTimeMillis() > (LOAD_IVAL + time)) {
				parser = new RSSParser(url);
				time = System.currentTimeMillis();
			}
		}
	}

	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == next) {
			if (header) {
				breakHeader = true;
			} else {
				can.forward();
			}
		}
	}
}
