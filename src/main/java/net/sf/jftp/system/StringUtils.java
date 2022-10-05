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

public enum StringUtils {
	;

	/**
	 * Makes a (path) string shorter to get it displayed correctly
	 */
	public static String cutPath(String s) {
		final int maxlabel = 64;

		if (maxlabel < s.length()) {
			while (s.contains("/")) {

				s = cutAfter(s, '/');

				if (16 > s.length()) {
					StringBuilder sb = new StringBuilder(s);
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
	public static String removeStart(String str, String what) {
		if (str.startsWith(what)) {
			int x = what.length();

			return str.substring(x);
		} else {
			return str;
		}
	}

	/**
	 * Returns the rest of a string after a given character
	 */
	private static String cutAfter(String str, char c) {
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
	public static String contains(String[] tmp, String[] str) {
		for (String value : tmp) {
			for (String s : str) {
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
	public static boolean strstr(String tmp, char str) {
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
	private static String string(char c) {
		char[] buf = new char[1];
		buf[0] = c;

		return new String(buf);
	}

	/**
	 * Get a filename out of a full path string
	 */
	public static String getFile(String file) {
		int x = file.lastIndexOf('/');

		// unix
		if (0 <= x) {
			file = file.substring(x + 1);
		}

		// windows
		x = file.lastIndexOf('\\');

		if (0 <= x) {
			file = file.substring(x + 1);
		}
		return file;
	}

	public static String getDir(String tmp) {
		int x;

		while (true) {
			x = tmp.indexOf('/');

			if ((x == (tmp.length() - 1)) || (0 > x)) {
				break;
			} else {
				tmp = tmp.substring(x + 1);
			}
		}

		while (true) {
			x = tmp.indexOf('\\');

			if ((x == (tmp.length() - 1)) || (0 > x)) {
				break;
			} else {
				tmp = tmp.substring(x + 1);
			}
		}

		return tmp;
	}

	public static boolean isRelative(String file) {
		if (file.startsWith("/")) {
			return false;
		}

		return (2 >= file.length()) || (':' != file.charAt(1));

	}

	public static void main(String[] argv) {
		final String a1 = "E:\\programme\\test.html";
		final String a2 = "programme\\test.html";
		final String a3 = "test.html";
		final String a4 = "/programme/test.html";
		final String a5 = "programme/test.html";

		System.out.println("getfile: " + getFile(a1) + " - false, " + isRelative(a1));
		System.out.println("getfile: " + getFile(a2) + " - true, " + isRelative(a2));
		System.out.println("getfile: " + getFile(a3) + " - true, " + isRelative(a3));
		System.out.println("getfile: " + getFile(a4) + " - false, " + isRelative(a4));
		System.out.println("getfile: " + getFile(a5) + " - true, " + isRelative(a5));
	}

	public static String cut(String tmp, String where) {
		StringBuilder ret = new StringBuilder();

		for (int i = 0; i < tmp.length(); i++) {
			if (!string(tmp.charAt(i)).equals(where)) {
				ret.append(string(tmp.charAt(i)));
			}

			if (string(tmp.charAt(i)).equals(where)) {
				return ret.toString();
			}
		}

		return ret.toString();
	}
}
