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
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;


public class FileSearch {

	public static final boolean quiet = true;
	public static final boolean ultraquiet = false;
	private final Hashtable checked = new Hashtable();
	final String localDir = ".";
	final int MAX = 999999;
	int MIN_TERM = 1;
	final int MIN_FACTOR = 1;
	final boolean LOAD = false;
	String[] typeArray = {""};
	String[] termArray = {""};
	String[] optArray = {""};
	String[] ignoreArray = {""};
	String[] scanArray = {""};
	private int currentDepth = 0;

	public static void main(final String[] argv) {
		final String[] typeArray = {".gz", ".bz2", ".zip", ".rar"};
		final String[] termArray = {"linux", "kernel"};
		final String[] optArray = {"download", "file", "mirror", "location"};
		final String[] ignoreArray = {".gif", ".jpg", ".png", ".swf", ".jar", ".class", ".google."};
		final String[] scanArray = {".html", ".htm", "/", ".jsp", ".jhtml", ".phtml", ".asp", ".xml", ".js", ".cgi"};
		final StringBuilder url = new StringBuilder("http://www.google.de/search?hl=de&q=");

		for (final String s : termArray) {
			url.append(s).append("+");
		}

		final FileSearch search = new FileSearch();

		search.typeArray = typeArray;
		search.termArray = termArray;
		search.optArray = optArray;
		search.ignoreArray = ignoreArray;
		search.scanArray = scanArray;
		search.MIN_TERM = 1;

		search.spider(url.toString());

	}

	private void spider(String url) {
		try {
			if (!url.contains("/")) {
				url = url + "/";
			}

			url = this.clear(url);

			net.sf.jftp.system.logging.Log.out(">>> URL: " + url);
			net.sf.jftp.system.logging.Log.out(">>> Scanning for ");

			for (final String s : this.typeArray) {
				net.sf.jftp.system.logging.Log.out(s + " ");
			}

			net.sf.jftp.system.logging.Log.out("");


			net.sf.jftp.system.logging.Log.out("Fetching initial HTML file...");

			final Getter urlGetter = new Getter(this.localDir);
			urlGetter.fetch(url, true);

			net.sf.jftp.system.logging.Log.out("Searching for links...");
			net.sf.jftp.system.LocalIO.pause(500);

			this.crawl(url);
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

	private int rate(final String content) {
		int score = 0;

		for (final String value : this.termArray) {
			if (content.contains(value)) score += 3;
		}

		if (score < this.MIN_TERM) return 0;

		for (final String s : this.optArray) {
			if (content.contains(s)) score++;
		}

		return score;
	}

	private int checkForResult(final String url) {
		for (final String s : this.ignoreArray) {
			if (url.contains(s)) return -1;
		}

		if (!this.checkForScanableUrl(url)) return -1;

		return 1;
	}

	private boolean checkForScanableUrl(final String url) {

		if (this.checked.containsKey(url)) {
			return false;
		} else {
			this.checked.put(url, "");
		}

		for (final String s : this.scanArray) {
			if (url.endsWith(s)) return true;
		}

		return false;
	}

	private void crawl(String url) throws Exception {
		url = this.clear(url);

		final int urlRating = this.checkForResult(url);
		if (!net.sf.jftp.tools.FileSearch.quiet) net.sf.jftp.system.logging.Log.out("URL-Rating: " + url + " -> " + urlRating + " @" + this.currentDepth);

		if (0 < urlRating) {
		} else if (0 > urlRating && 0 < this.currentDepth) {
			if (!net.sf.jftp.tools.FileSearch.quiet) net.sf.jftp.system.logging.Log.out("SKIP " + url);
			return;
		}


		final Getter urlGetter = new Getter(this.localDir);
		final String content = urlGetter.fetch(url);

		final int factor = this.rate(content);
		if (!net.sf.jftp.tools.FileSearch.quiet) net.sf.jftp.system.logging.Log.out("Content-Rating: " + url + " -> " + factor + " @" + this.currentDepth);

		if (this.MIN_FACTOR > factor) {
			if (!net.sf.jftp.tools.FileSearch.quiet) net.sf.jftp.system.logging.Log.out("DROP: " + url);
			return;
		}

		if (!net.sf.jftp.tools.FileSearch.ultraquiet) net.sf.jftp.system.logging.Log.out("Url: " + url + " -> " + urlRating + ":" + factor + "@" + this.currentDepth);

		Vector m = this.sort(content, url.substring(0, url.lastIndexOf("/")), "href=\"");
		m = this.addVector(m, this.sort(content, url.substring(0, url.lastIndexOf("/")), "src=\""));
		m = this.addVector(m, this.sort(content, url.substring(0, url.lastIndexOf("/")), "HREF=\""));
		m = this.addVector(m, this.sort(content, url.substring(0, url.lastIndexOf("/")), "SRC=\""));

		final Enumeration links = m.elements();

		while (links.hasMoreElements()) {

			final String next = (String) links.nextElement();

			if (!net.sf.jftp.tools.FileSearch.quiet) net.sf.jftp.system.logging.Log.out("PROCESS: " + next);
			boolean skip = false;

			while (!skip) {
				for (final String s : this.typeArray) {
					if (next.endsWith(s) || s.trim().equals("*")) {
						net.sf.jftp.system.logging.Log.out("HIT: " + url + " -> " + next);

						if (!this.LOAD || !this.checkForScanableUrl(url)) continue;

						final int x = next.indexOf("/");

						if ((0 < x) && (0 < next.substring(0, x).indexOf("."))) {
							final net.sf.jftp.tools.Getter urlGetter2 = new net.sf.jftp.tools.Getter(this.localDir);
							urlGetter2.fetch(next, false);

							continue;
						}
					}
				}

				skip = true;
			}

			if (this.MAX > this.currentDepth) {

				final int x = next.indexOf("/");

				if ((0 < x) && (0 < next.substring(0, x).indexOf("."))) {
					this.currentDepth++;
					this.crawl(next);
					this.currentDepth--;
				}
			}
		}
	}

	private Vector sort(String content, final String url, final String index) {
		final Vector res = new Vector();
		int wo = 0;

		while (true) {
			wo = content.indexOf(index);

			if (0 > wo) {
				return res;
			}

			content = content.substring(wo + index.length());

			String was = content.substring(0, content.indexOf("\""));

			was = this.createAbsoluteUrl(was, url);
			res.add(was);
			if (!net.sf.jftp.tools.FileSearch.quiet) net.sf.jftp.system.logging.Log.out("ADD: " + was);
		}
	}

	private String createAbsoluteUrl(String newLink, final String baseUrl) {
		newLink = this.clear(newLink);

		if (newLink.startsWith(baseUrl)) {
			return newLink;
		}

		if (newLink.startsWith("/") && (0 < baseUrl.indexOf("/"))) {
			newLink = baseUrl.substring(0, baseUrl.indexOf("/")) + newLink;
		} else if (newLink.startsWith("/") && (!baseUrl.contains("/"))) {
			newLink = baseUrl + newLink;
		} else if ((0 < newLink.indexOf("."))) {
			final int idx = newLink.indexOf("/");
			String tmp = "";

			if (0 <= idx) {
				tmp = newLink.substring(0, idx);
			}

			if ((0 < tmp.indexOf("."))) {
				return this.clear(newLink);
			}

			if (baseUrl.endsWith("/")) {
				newLink = baseUrl + newLink;
			} else {
				newLink = baseUrl + "/" + newLink;
			}
		}

		return newLink;
	}

}


class Getter {
	private String localDir = null;

	public Getter(final String localDir) {
		super();
		this.localDir = localDir;
	}

	public static void chill(final int time) {
		try {
			Thread.sleep(time);
		} catch (final Exception ex) {
		}
	}

	public String fetch(final String url) {
		try {
			final String host = url.substring(0, url.indexOf("/"));
			final StringBuilder result = new StringBuilder();

			final Socket deal = new Socket(host, 80);
			deal.setSoTimeout(5000);

			final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(deal.getOutputStream()));
			final BufferedReader in = new BufferedReader(new InputStreamReader(deal.getInputStream()));

			out.write("GET http://" + url + " HTTP/1.0\n\n");
			out.flush();

			int len = 0;

			while (!in.ready() && (5000 > len)) {
				net.sf.jftp.tools.Getter.chill(100);
				len += 100;
			}

			while (in.ready()) {
				result.append(in.readLine());
			}

			out.close();
			in.close();

			return result.toString();
		} catch (final Exception ex) {
			if (!FileSearch.quiet) ex.printStackTrace();
		}

		return "";
	}

	public void fetch(final String url, final boolean force) {
		try {
			final String host = url.substring(0, url.indexOf("/"));
			final String wo = url.substring(url.indexOf("/"));

			if (!FileSearch.quiet) net.sf.jftp.system.logging.Log.debug(">>> " + host + wo);

			final File d = new File(this.localDir);
			d.mkdir();

			final File f = new File(this.localDir + wo.substring(wo.lastIndexOf("/") + 1));

			if (f.exists() && !force) {
				if (!FileSearch.quiet) net.sf.jftp.system.logging.Log.debug(">>> file already exists...");

				return;
			} else {
				f.delete();
			}

			final Socket deal = new Socket(host, 80);
			final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(deal.getOutputStream()));
			final DataInputStream in = new DataInputStream(new BufferedInputStream(deal.getInputStream()));

			final BufferedOutputStream localOut = new BufferedOutputStream(new FileOutputStream(this.localDir + wo.substring(wo.lastIndexOf("/") + 1)));

			final byte[] alu = new byte[2048];

			out.write("GET http://" + url + " HTTP/1.0\n\n");
			out.flush();

			boolean line = true;
			final boolean bin = false;

			while (true) {
				net.sf.jftp.tools.Getter.chill(10);

				final StringBuilder tmp = new StringBuilder();

				while (line) {
					final String x = in.readLine();

					if (null == x) {
						break;
					}

					tmp.append(x).append("\n");

					if (x.isEmpty()) {
						line = false;
					}
				}

				final int x = in.read(alu);

				if (-1 == x) {
					if (line) {
						localOut.write(tmp.toString().getBytes(), 0, tmp.length());
					}

					out.close();
					in.close();
					localOut.flush();
					localOut.close();

					return;
				} else {
					localOut.write(alu, 0, x);
				}
			}
		} catch (final Exception ex) {
			if (!FileSearch.quiet) ex.printStackTrace();
		}
	}
}
