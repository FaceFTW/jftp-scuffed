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

import net.sf.jftp.config.Settings;
import net.sf.jftp.gui.base.StatusCanvas;
import net.sf.jftp.gui.framework.HImageButton;
import net.sf.jftp.system.LocalIO;
import net.sf.jftp.system.logging.Log;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;


public class RSSFeeder extends JPanel implements Runnable, ActionListener {
	private static String urlstring = Settings.getRSSFeed();
	private final StatusCanvas can = new StatusCanvas();
	private final HImageButton next = new HImageButton(Settings.nextRSSImage, "nextRSS", "Display next RSS news item", this);
	private final int HEADER_IVAL = 4000;
	private final int LOAD_IVAL = 31 * 60000;
	private Thread runner;
	private URL url;
	private RSSParser parser;
	private boolean header;
	private boolean breakHeader;

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

		LocalIO.pause(3000);

		Log.out("Starting RSS Feed");

		try {
			this.can.setInterval(10);
			this.url = new URL(urlstring);
			this.parser = new RSSParser(this.url);
			time = System.currentTimeMillis();
		} catch (Exception ex) {
			Log.debug("Error: Can't load RSS feed (" + ex + ")");
			ex.printStackTrace();

			return;
		}

		while (true) {
			try {
				Iterator iterator1 = this.parser.titles.iterator();
				Iterator iterator = this.parser.descs.iterator();

				while (iterator1.hasNext()) {
					this.can.setText((String) iterator1.next());
					this.next.setEnabled(true);
					this.header = true;

					int i = 0;

					while (!this.breakHeader && (100 > i)) {
						LocalIO.pause(this.HEADER_IVAL / 100);
						i++;
					}

					this.next.setEnabled(false);
					this.breakHeader = false;
					this.header = false;

					if (iterator.hasNext()) {
						this.next.setEnabled(true);
						this.can.scrollText((String) iterator.next());
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
