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
package net.sf.jftp.system;

public class StringUtils {
	/**
	 * Makes a (path) string shorter to get it displayed correctly
	 */
	public static String cutPath(String s) {
		final int maxlabel = 64;

		if (s.length() > maxlabel) {
			while (s.contains("/")) {

				s = StringUtils.cutAfter(s, '/');

				if (s.length() < 16) {
					final StringBuilder sb = new StringBuilder(s);
					sb.insert(0, ".../");

					return sb.toString();
				}
			}
		}

		return s;
	}

	/**
	 * Removes the a string at the beginning of a string
	 */
	public static String removeStart(final String str, final String what) {
		if (str.startsWith(what)) {
			final int x = what.length();

			return str.substring(x);
		} else {
			return str;
		}
	}

	/**
	 * Returns the rest of a string after a given character
	 */
	public static String cutAfter(final String str, final char c) {
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == c) {
				//   System.out.println(str.substring(i+1));
				return str.substring(i + 1);
			}
		}

		return str;
	}

	/**
	 * Used to search for return codes in a string array.
	 * Returns the first one found
	 */
	public static String contains(final String[] tmp, final String[] str) {
		for (final String value : tmp) {
			for (final String s : str) {
				if (value.startsWith(s)) {
					return value;
				}
			}
		}

		return "";
	}

	/**
	 * Returns true if the given string contains the given character
	 */
	public static boolean strstr(final String tmp, final char str) {
		for (int i = 0; i < tmp.length(); i++) {
			if (tmp.charAt(i) == str) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns a string representing a given character
	 */
	public static String string(final char c) {
		final char[] buf = new char[1];
		buf[0] = c;

		return new String(buf);
	}

	/**
	 * Get a filename out of a full path string
	 */
	public static String getFile(String file) {
		int x = file.lastIndexOf("/");

		// unix
		if (x >= 0) {
			file = file.substring(x + 1);
		}

		// windows
		x = file.lastIndexOf("\\");

		if (x >= 0) {
			file = file.substring(x + 1);
		}
		return file;
	}

	public static String getDir(String tmp) {
		int x;

		while (true) {
			x = tmp.indexOf("/");

			if ((x == (tmp.length() - 1)) || (x < 0)) {
				break;
			} else {
				tmp = tmp.substring(x + 1);
			}
		}

		while (true) {
			x = tmp.indexOf("\\");

			if ((x == (tmp.length() - 1)) || (x < 0)) {
				break;
			} else {
				tmp = tmp.substring(x + 1);
			}
		}

		return tmp;
	}

	public static boolean isRelative(final String file) {
		if (file.startsWith("/")) {
			return false;
		}

		return (file.length() <= 2) || (file.charAt(1) != ':');

	}
	public static void main(final String[] argv) {
		final String a1 = "E:\\programme\\test.html";
		final String a2 = "programme\\test.html";
		final String a3 = "test.html";
		final String a4 = "/programme/test.html";
		final String a5 = "programme/test.html";

		System.out.println("getfile: " + net.sf.jftp.system.StringUtils.getFile(a1) + " - false, " + net.sf.jftp.system.StringUtils.isRelative(a1));
		System.out.println("getfile: " + net.sf.jftp.system.StringUtils.getFile(a2) + " - true, " + net.sf.jftp.system.StringUtils.isRelative(a2));
		System.out.println("getfile: " + net.sf.jftp.system.StringUtils.getFile(a3) + " - true, " + net.sf.jftp.system.StringUtils.isRelative(a3));
		System.out.println("getfile: " + net.sf.jftp.system.StringUtils.getFile(a4) + " - false, " + net.sf.jftp.system.StringUtils.isRelative(a4));
		System.out.println("getfile: " + net.sf.jftp.system.StringUtils.getFile(a5) + " - true, " + net.sf.jftp.system.StringUtils.isRelative(a5));
	}

	public static String cut(final String tmp, final String where) {
		final StringBuilder ret = new StringBuilder();

		for (int i = 0; i < tmp.length(); i++) {
			if (!net.sf.jftp.system.StringUtils.string(tmp.charAt(i)).equals(where)) {
				ret.append(net.sf.jftp.system.StringUtils.string(tmp.charAt(i)));
			}

			if (net.sf.jftp.system.StringUtils.string(tmp.charAt(i)).equals(where)) {
				return ret.toString();
			}
		}

		return ret.toString();
	}
}
