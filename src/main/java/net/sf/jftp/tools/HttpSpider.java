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

import net.sf.jftp.gui.framework.HPanel;
import net.sf.jftp.gui.framework.HTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;


public class HttpSpider extends HPanel implements Runnable, ActionListener {
	private final HTextField host = new HTextField("Full URL:", "http://j-ftp.sourceforge.net/index.html", 30);
	private final HTextField type = new HTextField("Types (use * for all):", "html-htm-css-gif-jpg-zip-gz-avi-mpg", 25);
	private final HTextField depth = new HTextField("Search up to this many levels deeper:", "1", 10);
	private final HTextField dir = new HTextField("Store files in:", "", 25);
	private final JButton ok = new JButton("Start");
	private final JButton stop = new JButton("Stop download (ASAP)");
	private int currentDepth = 0;
	private int MAX = 1;
	private String[] typeArray = {"mpg", "avi", "mpeg", "mov", "rm", "wmv"};
	private String localDir = ".";
	private String[] argv;
	private boolean stopflag = false;

	public HttpSpider(final String localDir) {
		super();
		this.localDir = localDir;
		this.setLayout(new BorderLayout());

		final javax.swing.JPanel p1 = new javax.swing.JPanel();
		p1.setLayout(new GridLayout(4, 1, 5, 5));
		p1.add(this.host);
		p1.add(this.type);
		p1.add(this.depth);
		this.dir.setText(localDir);
		p1.add(this.dir);
		this.add("Center", p1);
		final javax.swing.JPanel okP = new javax.swing.JPanel();
		this.add("South", okP);
		okP.add(this.ok);
		this.ok.addActionListener(this);

		this.setVisible(true);
	}

	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == this.ok) {
			this.localDir = this.dir.getText();

			if (!this.localDir.endsWith("/")) {
				this.localDir = this.localDir + "/";
			}

			this.argv = new String[]{this.host.getText().trim(), this.type.getText().trim(), this.depth.getText().trim()};

			this.removeAll();
			this.add("North", new JLabel("Starting download, please watch the log window for details"));
			this.add("Center", this.stop);
			this.stop.addActionListener(this);
			net.sf.jftp.JFtp.statusP.jftp.setClosable(this.hashCode(), false);
			this.validate();

			final Thread runner = new Thread(this);
			runner.start();
		} else if (e.getSource() == this.stop) {
			this.stopflag = true;
		}
	}

	public void run() {
		this.spider(this.argv);

		if (!this.stopflag) {
			net.sf.jftp.system.logging.Log.debug("\nRecursive download finished.\nOuptut dir: " + this.localDir);
		} else {
			net.sf.jftp.system.logging.Log.debug("\nRecursive download aborted.");
		}

		net.sf.jftp.JFtp.statusP.jftp.ensureLogging();
		net.sf.jftp.JFtp.statusP.jftp.removeFromDesktop(this.hashCode());
	}

	private void spider(final String[] argv) {
		try {
			String url = "http://j-ftp.sourceforge.net/index.html";

			if (2 <= argv.length) {
				url = this.clear(argv[0]);

				if (!url.contains("/")) {
					url = url + "/";
				}

				this.typeArray = this.check(argv[1]);

				net.sf.jftp.system.logging.Log.debugRaw(">>> Scanning for ");

				for (final String s : this.typeArray) {
					net.sf.jftp.system.logging.Log.debugRaw(s + " ");
				}

				net.sf.jftp.system.logging.Log.debug("");
			}

			if (2 < argv.length) {
				this.MAX = Integer.parseInt(argv[2]);
			}

			if (this.stopflag) {
				return;
			}

			net.sf.jftp.system.logging.Log.debug("Fetching initial HTML file...");

			final Holer sammy = new Holer(this.localDir);
			sammy.bringAnStart(url, true);

			if (this.stopflag) {
				return;
			}

			net.sf.jftp.system.logging.Log.debug("Searching for links...");
			net.sf.jftp.JFtp.statusP.jftp.ensureLogging();
			net.sf.jftp.system.LocalIO.pause(500);

			if (this.stopflag) {
				return;
			}

			this.smoke(url);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	private String clear(String url) {
		final int idx = url.indexOf("http://");

		if (0 <= idx) {
			url = url.substring(7);
		}

		return url;
	}

	private Vector addVector(final Vector v, final Vector x) {
		final Enumeration e = x.elements();

		while (e.hasMoreElements()) {
			final String next = (String) e.nextElement();
			v.add(next);
		}

		return v;
	}

	private void smoke(String url) throws Exception {
		if (this.stopflag) {
			return;
		}

		url = this.clear(url);

		final Holer sammy = new Holer(this.localDir);
		final String zeug = sammy.holZeug(url);

		Vector m = this.sortiermal(zeug, url.substring(0, url.lastIndexOf("/")), "href=\"");
		m = this.addVector(m, this.sortiermal(zeug, url.substring(0, url.lastIndexOf("/")), "src=\""));
		m = this.addVector(m, this.sortiermal(zeug, url.substring(0, url.lastIndexOf("/")), "HREF=\""));
		m = this.addVector(m, this.sortiermal(zeug, url.substring(0, url.lastIndexOf("/")), "SRC=\""));

		final Enumeration mischen = m.elements();

		while (mischen.hasMoreElements()) {
			if (this.stopflag) {
				return;
			}

			final String next = (String) mischen.nextElement();

			net.sf.jftp.system.logging.Log.out("Processing: " + next);

			for (final String s : this.typeArray) {
				if (next.endsWith(s) || s.trim().equals("*")) {
					final int x = next.indexOf("/");

					if ((0 < x) && (0 < next.substring(0, x).indexOf("."))) {
						final net.sf.jftp.tools.Holer nochnsammy = new net.sf.jftp.tools.Holer(this.localDir);
						nochnsammy.bringAnStart(next, false);

						if (this.stopflag) {
							return;
						}

						continue;
					}
				}
			}

			if (this.currentDepth < this.MAX) {
				if (this.stopflag) {
					return;
				}

				final int x = next.indexOf("/");

				if ((0 < x) && (0 < next.substring(0, x).indexOf("."))) {
					this.currentDepth++;
					this.smoke(next);
					this.currentDepth--;
				}
			}
		}
	}

	private Vector sortiermal(String zeug, final String url, final String index) {
		final Vector mischen = new Vector();
		int wo = 0;

		while (true) {
			wo = zeug.indexOf(index);

			if (0 > wo) {
				return mischen;
			}

			zeug = zeug.substring(wo + index.length());

			String was = zeug.substring(0, zeug.indexOf("\""));

			was = this.checker(was, url);
			mischen.add(was);
			net.sf.jftp.system.logging.Log.out("Added: " + was);
		}
	}

	private String[] check(final String auswahl) {
		final StringTokenizer flyer = new StringTokenizer(auswahl, "-", false);
		final String[] einkauf = new String[flyer.countTokens()];
		int tmp = 0;

		while (flyer.hasMoreElements()) {
			einkauf[tmp] = (String) flyer.nextElement();
			tmp++;
		}

		return einkauf;
	}

	private String checker(String was, final String url) {
		was = this.clear(was);

		if (was.startsWith(url)) {
			return was;
		}

		if (was.startsWith("/") && (0 < url.indexOf("/"))) {
			was = url.substring(0, url.indexOf("/")) + was;
		} else if (was.startsWith("/") && (!url.contains("/"))) {
			was = url + was;
		} else if ((0 < was.indexOf("."))) {
			final int idx = was.indexOf("/");
			String tmp = "";

			if (0 <= idx) {
				tmp = was.substring(0, idx);
			}

			if ((0 < tmp.indexOf("."))) {
				return this.clear(was);
			}

			if (url.endsWith("/")) {
				was = url + was;
			} else {
				was = url + "/" + was;
			}
		}

		net.sf.jftp.system.logging.Log.out("-> " + was);

		return was;
	}

	public Insets getInsets() {
		return new Insets(5, 5, 5, 5);
	}
}


class Holer {
	private String localDir = null;

	public Holer(final String localDir) {
		super();
		this.localDir = localDir;
	}

	private static void chill(final int time) {
		try {
			Thread.sleep(time);
		} catch (final Exception ex) {
		}
	}

	public String holZeug(final String wat) {
		try {
			final String dealer = wat.substring(0, wat.indexOf("/"));
			final String wo = wat.substring(wat.indexOf("/"));
			final StringBuilder zeug = new StringBuilder();

			net.sf.jftp.system.logging.Log.out(">> " + dealer + wo);

			final Socket deal = new Socket(dealer, 80);
			deal.setSoTimeout(5000);

			final BufferedWriter order = new BufferedWriter(new OutputStreamWriter(deal.getOutputStream()));
			final BufferedReader checkung = new BufferedReader(new InputStreamReader(deal.getInputStream()));

			order.write("GET http://" + wat + " HTTP/1.0\n\n");
			order.flush();

			int len = 0;

			while (!checkung.ready() && (5000 > len)) {
				net.sf.jftp.tools.Holer.chill(100);
				len += 100;
			}

			while (checkung.ready()) {
				zeug.append(checkung.readLine());
			}

			order.close();
			checkung.close();

			return zeug.toString();
		} catch (final Exception ex) {
			ex.printStackTrace();
		}

		return "";
	}

	public void bringAnStart(final String wat, final boolean force) {
		try {
			final String dealer = wat.substring(0, wat.indexOf("/"));
			final String wo = wat.substring(wat.indexOf("/"));

			net.sf.jftp.system.logging.Log.debug(">>> " + dealer + wo);

			final File d = new File(this.localDir);
			d.mkdir();

			final File f = new File(this.localDir + wo.substring(wo.lastIndexOf("/") + 1));

			if (f.exists() && !force) {
				net.sf.jftp.system.logging.Log.debug(">>> file already exists...");

				return;
			} else {
				f.delete();
			}

			final Socket deal = new Socket(dealer, 80);
			final BufferedWriter order = new BufferedWriter(new OutputStreamWriter(deal.getOutputStream()));
			final DataInputStream checkung = new DataInputStream(new BufferedInputStream(deal.getInputStream()));

			final BufferedOutputStream vorrat = new BufferedOutputStream(new FileOutputStream(this.localDir + wo.substring(wo.lastIndexOf("/") + 1)));

			final byte[] alu = new byte[2048];

			order.write("GET http://" + wat + " HTTP/1.0\n\n");
			order.flush();

			boolean line = true;

			while (true) {
				net.sf.jftp.tools.Holer.chill(10);

				final StringBuilder tmp = new StringBuilder();

				while (line) {
					final String x = checkung.readLine();

					if (null == x) {
						break;
					}

					tmp.append(x).append("\n");

					if (x.isEmpty()) {
						line = false;
					}
				}

				final int x = checkung.read(alu);

				if (-1 == x) {
					if (line) {
						vorrat.write(tmp.toString().getBytes(), 0, tmp.length());
					}

					order.close();
					checkung.close();
					vorrat.flush();
					vorrat.close();

					return;
				} else {
					vorrat.write(alu, 0, x);
				}
			}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}
}
