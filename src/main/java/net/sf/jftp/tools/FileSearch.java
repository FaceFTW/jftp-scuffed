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

	public static void main(String[] argv) {
		String[] typeArray = {".gz", ".bz2", ".zip", ".rar"};
		String[] termArray = {"linux", "kernel"};
		String[] optArray = {"download", "file", "mirror", "location"};
		String[] ignoreArray = {".gif", ".jpg", ".png", ".swf", ".jar", ".class", ".google."};
		String[] scanArray = {".html", ".htm", "/", ".jsp", ".jhtml", ".phtml", ".asp", ".xml", ".js", ".cgi"};
		StringBuilder url = new StringBuilder("http://www.google.de/search?hl=de&q=");

		for (String s : termArray) {
			url.append(s).append("+");
		}

		FileSearch search = new FileSearch();

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

			for (String s : this.typeArray) {
				net.sf.jftp.system.logging.Log.out(s + " ");
			}

			net.sf.jftp.system.logging.Log.out("");


			net.sf.jftp.system.logging.Log.out("Fetching initial HTML file...");

			Getter urlGetter = new Getter(this.localDir);
			urlGetter.fetch(url, true);

			net.sf.jftp.system.logging.Log.out("Searching for links...");
			net.sf.jftp.system.LocalIO.pause(500);

			this.crawl(url);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private String clear(String url) {
		int idx = url.indexOf("http://");

		if (0 <= idx) {
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

	private int rate(String content) {
		int score = 0;

		for (String value : this.termArray) {
			if (content.contains(value)) score += 3;
		}

		if (score < this.MIN_TERM) return 0;

		for (String s : this.optArray) {
			if (content.contains(s)) score++;
		}

		return score;
	}

	private int checkForResult(String url) {
		for (String s : this.ignoreArray) {
			if (url.contains(s)) return -1;
		}

		if (!this.checkForScanableUrl(url)) return -1;

		return 1;
	}

	private boolean checkForScanableUrl(String url) {

		if (this.checked.containsKey(url)) {
			return false;
		} else {
			this.checked.put(url, "");
		}

		for (String s : this.scanArray) {
			if (url.endsWith(s)) return true;
		}

		return false;
	}

	private void crawl(String url) throws Exception {
		url = this.clear(url);

		int urlRating = this.checkForResult(url);
		if (!quiet) net.sf.jftp.system.logging.Log.out("URL-Rating: " + url + " -> " + urlRating + " @" + this.currentDepth);

		if (0 < urlRating) {
		} else if (0 > urlRating && 0 < this.currentDepth) {
			if (!quiet) net.sf.jftp.system.logging.Log.out("SKIP " + url);
			return;
		}


		Getter urlGetter = new Getter(this.localDir);
		String content = urlGetter.fetch(url);

		int factor = this.rate(content);
		if (!quiet) net.sf.jftp.system.logging.Log.out("Content-Rating: " + url + " -> " + factor + " @" + this.currentDepth);

		if (this.MIN_FACTOR > factor) {
			if (!quiet) net.sf.jftp.system.logging.Log.out("DROP: " + url);
			return;
		}

		if (!ultraquiet) net.sf.jftp.system.logging.Log.out("Url: " + url + " -> " + urlRating + ":" + factor + "@" + this.currentDepth);

		Vector m = this.sort(content, url.substring(0, url.lastIndexOf('/')), "href=\"");
		m = this.addVector(m, this.sort(content, url.substring(0, url.lastIndexOf('/')), "src=\""));
		m = this.addVector(m, this.sort(content, url.substring(0, url.lastIndexOf('/')), "HREF=\""));
		m = this.addVector(m, this.sort(content, url.substring(0, url.lastIndexOf('/')), "SRC=\""));

		Enumeration links = m.elements();

		while (links.hasMoreElements()) {

			String next = (String) links.nextElement();

			if (!quiet) net.sf.jftp.system.logging.Log.out("PROCESS: " + next);
			boolean skip = false;

			while (!skip) {
				for (String s : this.typeArray) {
					if (next.endsWith(s) || s.trim().equals("*")) {
						net.sf.jftp.system.logging.Log.out("HIT: " + url + " -> " + next);

						if (!this.LOAD || !this.checkForScanableUrl(url)) continue;

						int x = next.indexOf('/');

						if ((0 < x) && (0 < next.substring(0, x).indexOf('.'))) {
							net.sf.jftp.tools.Getter urlGetter2 = new net.sf.jftp.tools.Getter(this.localDir);
							urlGetter2.fetch(next, false);

						}
					}
				}

				skip = true;
			}

			if (this.MAX > this.currentDepth) {

				int x = next.indexOf('/');

				if ((0 < x) && (0 < next.substring(0, x).indexOf('.'))) {
					this.currentDepth++;
					this.crawl(next);
					this.currentDepth--;
				}
			}
		}
	}

	private Vector sort(String content, String url, String index) {
		Vector res = new Vector();
		int wo = 0;

		while (true) {
			wo = content.indexOf(index);

			if (0 > wo) {
				return res;
			}

			content = content.substring(wo + index.length());

			String was = content.substring(0, content.indexOf('"'));

			was = this.createAbsoluteUrl(was, url);
			res.add(was);
			if (!quiet) net.sf.jftp.system.logging.Log.out("ADD: " + was);
		}
	}

	private String createAbsoluteUrl(String newLink, String baseUrl) {
		newLink = this.clear(newLink);

		if (newLink.startsWith(baseUrl)) {
			return newLink;
		}

		if (newLink.startsWith("/") && (0 < baseUrl.indexOf('/'))) {
			newLink = baseUrl.substring(0, baseUrl.indexOf('/')) + newLink;
		} else if (newLink.startsWith("/") && (!baseUrl.contains("/"))) {
			newLink = baseUrl + newLink;
		} else if ((0 < newLink.indexOf('.'))) {
			int idx = newLink.indexOf('/');
			String tmp = "";

			if (0 <= idx) {
				tmp = newLink.substring(0, idx);
			}

			if ((0 < tmp.indexOf('.'))) {
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

	Getter(String localDir) {
		super();
		this.localDir = localDir;
	}

	public static void chill(int time) {
		try {
			Thread.sleep(time);
		} catch (Exception ex) {
		}
	}

	public String fetch(String url) {
		try {
			String host = url.substring(0, url.indexOf('/'));
			StringBuilder result = new StringBuilder();

			Socket deal = new Socket(host, 80);
			deal.setSoTimeout(5000);

			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(deal.getOutputStream()));
			BufferedReader in = new BufferedReader(new InputStreamReader(deal.getInputStream()));

			out.write("GET http://" + url + " HTTP/1.0\n\n");
			out.flush();

			int len = 0;

			while (!in.ready() && (5000 > len)) {
				chill(100);
				len += 100;
			}

			while (in.ready()) {
				result.append(in.readLine());
			}

			out.close();
			in.close();

			return result.toString();
		} catch (Exception ex) {
			if (!FileSearch.quiet) ex.printStackTrace();
		}

		return "";
	}

	public void fetch(String url, boolean force) {
		try {
			String host = url.substring(0, url.indexOf('/'));
			String wo = url.substring(url.indexOf('/'));

			if (!FileSearch.quiet) net.sf.jftp.system.logging.Log.debug(">>> " + host + wo);

			File d = new File(this.localDir);
			d.mkdir();

			File f = new File(this.localDir + wo.substring(wo.lastIndexOf('/') + 1));

			if (f.exists() && !force) {
				if (!FileSearch.quiet) net.sf.jftp.system.logging.Log.debug(">>> file already exists...");

				return;
			} else {
				f.delete();
			}

			Socket deal = new Socket(host, 80);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(deal.getOutputStream()));
			DataInputStream in = new DataInputStream(new BufferedInputStream(deal.getInputStream()));

			BufferedOutputStream localOut = new BufferedOutputStream(new FileOutputStream(this.localDir + wo.substring(wo.lastIndexOf('/') + 1)));

			byte[] alu = new byte[2048];

			out.write("GET http://" + url + " HTTP/1.0\n\n");
			out.flush();

			boolean line = true;
			final boolean bin = false;

			while (true) {
				chill(10);

				StringBuilder tmp = new StringBuilder();

				while (line) {
					String x = in.readLine();

					if (null == x) {
						break;
					}

					tmp.append(x).append("\n");

					if (x.isEmpty()) {
						line = false;
					}
				}

				int x = in.read(alu);

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
		} catch (Exception ex) {
			if (!FileSearch.quiet) ex.printStackTrace();
		}
	}
}
