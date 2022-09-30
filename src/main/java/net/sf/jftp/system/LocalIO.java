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

import java.io.File;


public class LocalIO {
	/**
	 * sorts a string alphabetically
	 * probably better off just calling java.util.Arrays.sort
	 */
	public static String[] sortStrings(String[] array) {
		for (int i = array.length; --i >= 0; ) {
			boolean swapped = false;

			for (int j = 0; j < i; j++) {
				if (array[j].compareTo(array[j + 1]) > 0) {
					String T = array[j];
					array[j] = array[j + 1];
					array[j + 1] = T;
					swapped = true;
				}
			}

			if (!swapped) {
				break;
			}
		}

		return array;
	}

	/**
	 * recursive removal of a local directoy
	 */
	public static void cleanLocalDir(String dir, String path) {
		if (dir.endsWith("\\")) {
			System.out.println("oops... fucked up, need to fix \\-problem!!!");
		}

		if (!dir.endsWith("/")) {
			dir = dir + "/";
		}

		File f2 = new File(path + dir);
		String[] tmp = f2.list();

		for (String s : tmp) {
			java.io.File f3 = new java.io.File(path + dir + s);

			if (f3.isDirectory()) {
				//System.out.println(dir);
				cleanLocalDir(dir + s, path);
				f3.delete();
			} else {
				//System.out.println(dir+tmp[i]);
				f3.delete();
			}
		}
	}

	/**
	 * sleep amount of time in millisconds
	 */
	public static void pause(int time) {
		try {
			Thread.sleep(time);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
