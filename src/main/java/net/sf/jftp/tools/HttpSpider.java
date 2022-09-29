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

	public HttpSpider(String localDir) {
		this.localDir = localDir;
		setLayout(new BorderLayout());

		javax.swing.JPanel p1 = new javax.swing.JPanel();
		p1.setLayout(new GridLayout(4, 1, 5, 5));
		p1.add(host);
		p1.add(type);
		p1.add(depth);
		dir.setText(localDir);
		p1.add(dir);
		add("Center", p1);
		javax.swing.JPanel okP = new javax.swing.JPanel();
		add("South", okP);
		okP.add(ok);
		ok.addActionListener(this);

		setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == ok) {
			localDir = dir.getText();

			if (!localDir.endsWith("/")) {
				localDir = localDir + "/";
			}

			argv = new String[]{host.getText().trim(), type.getText().trim(), depth.getText().trim()};

			removeAll();
			add("North", new JLabel("Starting download, please watch the log window for details"));
			add("Center", stop);
			stop.addActionListener(this);
			net.sf.jftp.JFtp.statusP.jftp.setClosable(this.hashCode(), false);
			validate();

			Thread runner = new Thread(this);
			runner.start();
		} else if (e.getSource() == stop) {
			stopflag = true;
		}
	}

	public void run() {
		spider(argv);

		if (!stopflag) {
			net.sf.jftp.system.logging.Log.debug("\nRecursive download finished.\nOuptut dir: " + localDir);
		} else {
			net.sf.jftp.system.logging.Log.debug("\nRecursive download aborted.");
		}

		net.sf.jftp.JFtp.statusP.jftp.ensureLogging();
		net.sf.jftp.JFtp.statusP.jftp.removeFromDesktop(this.hashCode());
	}

	private void spider(String[] argv) {
		try {
			String url = "http://j-ftp.sourceforge.net/index.html";

			if (argv.length >= 2) {
				url = clear(argv[0]);

				if (!url.contains("/")) {
					url = url + "/";
				}

				typeArray = check(argv[1]);

				net.sf.jftp.system.logging.Log.debugRaw(">>> Scanning for ");

				for (String s : typeArray) {
					net.sf.jftp.system.logging.Log.debugRaw(s + " ");
				}

				net.sf.jftp.system.logging.Log.debug("");
			}

			if (argv.length > 2) {
				MAX = Integer.parseInt(argv[2]);
			}

			if (stopflag) {
				return;
			}

			net.sf.jftp.system.logging.Log.debug("Fetching initial HTML file...");

			Holer sammy = new Holer(localDir);
			sammy.bringAnStart(url, true);

			if (stopflag) {
				return;
			}

			net.sf.jftp.system.logging.Log.debug("Searching for links...");
			net.sf.jftp.JFtp.statusP.jftp.ensureLogging();
			net.sf.jftp.system.LocalIO.pause(500);

			if (stopflag) {
				return;
			}

			smoke(url);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private String clear(String url) {
		int idx = url.indexOf("http://");

		if (idx >= 0) {
			url = url.substring(7);
		}

		return url;
	}

	private Vector addVector(Vector v, Vector x) {
		Enumeration e = x.elements();

		while (e.hasMoreElements()) {
			String next = (String) e.nextElement();
			v.add(next);
		}

		return v;
	}

	private void smoke(String url) throws Exception {
		if (stopflag) {
			return;
		}

		url = clear(url);

		Holer sammy = new Holer(localDir);
		String zeug = sammy.holZeug(url);

		Vector m = sortiermal(zeug, url.substring(0, url.lastIndexOf("/")), "href=\"");
		m = addVector(m, sortiermal(zeug, url.substring(0, url.lastIndexOf("/")), "src=\""));
		m = addVector(m, sortiermal(zeug, url.substring(0, url.lastIndexOf("/")), "HREF=\""));
		m = addVector(m, sortiermal(zeug, url.substring(0, url.lastIndexOf("/")), "SRC=\""));

		Enumeration mischen = m.elements();

		while (mischen.hasMoreElements()) {
			if (stopflag) {
				return;
			}

			String next = (String) mischen.nextElement();

			net.sf.jftp.system.logging.Log.out("Processing: " + next);

			for (String s : typeArray) {
				if (next.endsWith(s) || s.trim().equals("*")) {
					int x = next.indexOf("/");

					if ((x > 0) && (next.substring(0, x).indexOf(".") > 0)) {
						net.sf.jftp.tools.Holer nochnsammy = new net.sf.jftp.tools.Holer(localDir);
						nochnsammy.bringAnStart(next, false);

						if (stopflag) {
							return;
						}

						continue;
					}
				}
			}

			if (currentDepth < MAX) {
				if (stopflag) {
					return;
				}

				int x = next.indexOf("/");

				if ((x > 0) && (next.substring(0, x).indexOf(".") > 0)) {
					currentDepth++;
					smoke(next);
					currentDepth--;
				}
			}
		}
	}

	private Vector sortiermal(String zeug, String url, String index) {
		Vector mischen = new Vector();
		int wo = 0;

		while (true) {
			wo = zeug.indexOf(index);

			if (wo < 0) {
				return mischen;
			}

			zeug = zeug.substring(wo + index.length());

			String was = zeug.substring(0, zeug.indexOf("\""));

			was = checker(was, url);
			mischen.add(was);
			net.sf.jftp.system.logging.Log.out("Added: " + was);
		}
	}

	private String[] check(String auswahl) {
		StringTokenizer flyer = new StringTokenizer(auswahl, "-", false);
		String[] einkauf = new String[flyer.countTokens()];
		int tmp = 0;

		while (flyer.hasMoreElements()) {
			einkauf[tmp] = (String) flyer.nextElement();
			tmp++;
		}

		return einkauf;
	}

	private String checker(String was, String url) {
		was = clear(was);

		if (was.startsWith(url)) {
			return was;
		}

		if (was.startsWith("/") && (url.indexOf("/") > 0)) {
			was = url.substring(0, url.indexOf("/")) + was;
		} else if (was.startsWith("/") && (!url.contains("/"))) {
			was = url + was;
		} else if ((was.indexOf(".") > 0)) {
			int idx = was.indexOf("/");
			String tmp = "";

			if (idx >= 0) {
				tmp = was.substring(0, idx);
			}

			if ((tmp.indexOf(".") > 0)) {
				return clear(was);
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

	public Holer(String localDir) {
		this.localDir = localDir;
	}

	private static void chill(int time) {
		try {
			Thread.sleep(time);
		} catch (Exception ex) {
		}
	}

	public String holZeug(String wat) {
		try {
			String dealer = wat.substring(0, wat.indexOf("/"));
			String wo = wat.substring(wat.indexOf("/"));
			StringBuilder zeug = new StringBuilder();

			net.sf.jftp.system.logging.Log.out(">> " + dealer + wo);

			Socket deal = new Socket(dealer, 80);
			deal.setSoTimeout(5000);

			BufferedWriter order = new BufferedWriter(new OutputStreamWriter(deal.getOutputStream()));
			BufferedReader checkung = new BufferedReader(new InputStreamReader(deal.getInputStream()));

			order.write("GET http://" + wat + " HTTP/1.0\n\n");
			order.flush();

			int len = 0;

			while (!checkung.ready() && (len < 5000)) {
				chill(100);
				len += 100;
			}

			while (checkung.ready()) {
				zeug.append(checkung.readLine());
			}

			order.close();
			checkung.close();

			return zeug.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return "";
	}

	public void bringAnStart(String wat, boolean force) {
		try {
			String dealer = wat.substring(0, wat.indexOf("/"));
			String wo = wat.substring(wat.indexOf("/"));

			net.sf.jftp.system.logging.Log.debug(">>> " + dealer + wo);

			File d = new File(localDir);
			d.mkdir();

			File f = new File(localDir + wo.substring(wo.lastIndexOf("/") + 1));

			if (f.exists() && !force) {
				net.sf.jftp.system.logging.Log.debug(">>> file already exists...");

				return;
			} else {
				f.delete();
			}

			Socket deal = new Socket(dealer, 80);
			BufferedWriter order = new BufferedWriter(new OutputStreamWriter(deal.getOutputStream()));
			DataInputStream checkung = new DataInputStream(new BufferedInputStream(deal.getInputStream()));

			BufferedOutputStream vorrat = new BufferedOutputStream(new FileOutputStream(localDir + wo.substring(wo.lastIndexOf("/") + 1)));

			byte[] alu = new byte[2048];

			order.write("GET http://" + wat + " HTTP/1.0\n\n");
			order.flush();

			boolean line = true;

			while (true) {
				chill(10);

				StringBuilder tmp = new StringBuilder();

				while (line) {
					String x = checkung.readLine();

					if (x == null) {
						break;
					}

					tmp.append(x).append("\n");

					if (x.isEmpty()) {
						line = false;
					}
				}

				int x = checkung.read(alu);

				if (x == -1) {
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
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
