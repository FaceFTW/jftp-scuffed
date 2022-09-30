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
import java.io.DataInputStream;
import java.net.URL;
import java.util.Vector;


public class RSSParser {
	final URL file;
	final Vector titles = new Vector();
	final Vector descs = new Vector();
	final Vector links = new Vector();
	final Vector content = new Vector();

	public RSSParser(final URL f) {
		this.file = f;
		this.parse();
	}

	private void parse() {
		try {
			final DataInputStream in = new DataInputStream(new BufferedInputStream(this.file.openStream()));

			String tmp;
			final StringBuilder data = new StringBuilder();

			while (null != (tmp = in.readLine())) {
				data.append(tmp);
			}

			this.add(data.toString(), this.content, "<title>", "</title>", "<description>", "</description>");
			this.add(data.toString(), this.titles, "<title>", "</title>", null, null);
			this.add(data.toString(), this.descs, "<description>", "</description>", null, null);
			this.add(data.toString(), this.links, "<link>", "</link>", null, null);
		} catch (final Exception ex) {
			ex.printStackTrace();

		}
	}

	private void add(String tmp, final Vector target, String start, String end, String s2, String e2) {
		if (null == s2) {
			s2 = start;
			e2 = end;
		}

		int x = tmp.indexOf(start);
		final int x2 = tmp.indexOf(s2);

		if (((0 > x) && (0 > x2)) || ((0 > x) && start.equals(s2))) {
			return;
		}

		if ((0 <= x2) && ((x2 < x) || (0 > x))) {
			x = x2;

			String t = start;
			start = s2;
			s2 = t;

			t = end;
			end = e2;
			e2 = t;
		}

		final String value = tmp.substring(x + start.length(), tmp.indexOf(end));

		target.add(value);

		tmp = tmp.substring(tmp.indexOf(end) + end.length());

		this.add(tmp, target, start, end, s2, e2);
	}
}
