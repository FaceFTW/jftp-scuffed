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

//TODO: Having capacity passed to these methods is redundant, take it out
//*** (should it be in GUI dir?)
package net.sf.jftp.gui.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.StringTokenizer;


public class LastConnections {
	public static final String SENTINEL = "********************";
	private static net.sf.jftp.JFtp jftp;

	//*** changed this so that JFtp object is passed to it and
	//initialized
	public LastConnections(final net.sf.jftp.JFtp jftp) {
		LastConnections.jftp = jftp;

		//init();
	}

	//writeToFile: This code is called when modifications are done
	//being made (should it be private and called from inside
	//this class?)
	//maybe I should return a boolean value stating whether or not it
	//succeeded
	//SHOULD THIS BE PRIVATE?
	//public static void writeToFile(String[] a, int capacity) {
	public static void writeToFile(final String[][] a, final int capacity) {
		try {
			final File f1 = new File(net.sf.jftp.config.Settings.appHomeDir);
			f1.mkdir();

			final File f2 = new File(net.sf.jftp.config.Settings.last_cons);
			f2.createNewFile();

			final FileOutputStream fos;
			final PrintStream out;

			fos = new FileOutputStream(net.sf.jftp.config.Settings.last_cons);
			out = new PrintStream(fos);

			for (int i = 0; i < capacity; i++) {
				int j = 0;
				out.println(a[i][j]);

				while ((net.sf.jftp.JFtp.CAPACITY > j) && !(a[i][j].equals(net.sf.jftp.gui.tasks.LastConnections.SENTINEL))) {
					j++;
					out.println(a[i][j]);

				}

			}

			net.sf.jftp.JFtp.updateMenuBar();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	//writeToFile
	public static String[][] readFromFile(final int capacity) {

		final String[][] retVal = new String[capacity][net.sf.jftp.JFtp.CONNECTION_DATA_LENGTH];

		try {
			final File f1 = new File(net.sf.jftp.config.Settings.appHomeDir);
			f1.mkdir();

			final File f2 = new File(net.sf.jftp.config.Settings.last_cons);

			if (!f2.exists()) {
				net.sf.jftp.gui.tasks.LastConnections.init(capacity);
			}

			RandomAccessFile raf = new RandomAccessFile(f2, "r");

			final String[] oldValues = new String[capacity];
			String firstSection = "";
			boolean oldVersion = true;

			for (int i = 0; i < capacity; i++) {
				if (net.sf.jftp.JFtp.CAPACITY > capacity) {
					oldVersion = false;

					break;
				}

				oldValues[i] = raf.readLine();
				firstSection = oldValues[i].substring(0, 3);

				if (!(firstSection.equals("FTP")) && !(firstSection.equals("SFT")) && !(firstSection.equals("SMB")) && !(firstSection.equals("NFS")) && !(firstSection.equals("nul"))) {
					oldVersion = false;
				}

				if (!oldVersion) {
					break;
				}
			}

			//for
			raf = new RandomAccessFile(f2, "r");

			if (oldVersion) {
				//System.out.println("old file detected");
				for (int i = 0; i < capacity; i++) {
					oldValues[i] = raf.readLine();
				}

				net.sf.jftp.gui.tasks.LastConnections.changeFile(oldValues);
			}

			//reset to read start of file
			raf = new RandomAccessFile(f2, "r");

			for (int i = 0; i < capacity; i++) {
				int j = 0;
				retVal[i][j] = raf.readLine();

				while ((net.sf.jftp.JFtp.CONNECTION_DATA_LENGTH > j) && !(retVal[i][j].equals(net.sf.jftp.gui.tasks.LastConnections.SENTINEL))) {
					j++;
					retVal[i][j] = raf.readLine();


				}

			}

			//for
		} catch (final Exception ex) {
			ex.printStackTrace();
		}

		return retVal;
	}

	//readFromFile
	public static String[][] prepend(final String[] newString, final int capacity, final boolean newConnection) {
		String[][] lastCons = new String[capacity][net.sf.jftp.JFtp.CONNECTION_DATA_LENGTH];

		lastCons = net.sf.jftp.gui.tasks.LastConnections.readFromFile(capacity);

		for (int i = 0; i < capacity; i++) {
			int j = 0;

			while (!(lastCons[i][j].equals(net.sf.jftp.gui.tasks.LastConnections.SENTINEL))) {
				j++;
			}
		}

		final String[] temp = new String[net.sf.jftp.JFtp.CONNECTION_DATA_LENGTH];

		int j = 0;
		while (!(lastCons[0][j].equals(net.sf.jftp.gui.tasks.LastConnections.SENTINEL))) {
			temp[j] = lastCons[0][j];

			j++;
		}

		temp[j] = net.sf.jftp.gui.tasks.LastConnections.SENTINEL;

		j = 0;

		while (!(newString[j].equals(net.sf.jftp.gui.tasks.LastConnections.SENTINEL))) {
			lastCons[0][j] = newString[j];
			j++;
		}

		lastCons[0][j] = net.sf.jftp.gui.tasks.LastConnections.SENTINEL;
		j++;

		while (net.sf.jftp.JFtp.CONNECTION_DATA_LENGTH > j) {
			lastCons[0][j] = "";
			j++;
		}

		for (int i = 0; i < capacity; i++) {
			if ((i + 1) != capacity) {
				j = 0;

				while (!(temp[j].equals(net.sf.jftp.gui.tasks.LastConnections.SENTINEL))) {
					newString[j] = temp[j];
					j++;
				}

				newString[j] = net.sf.jftp.gui.tasks.LastConnections.SENTINEL;

				j = 0;

				//while (!(temp[j].equals(SENTINEL))) {
				while (!(lastCons[i + 1][j].equals(net.sf.jftp.gui.tasks.LastConnections.SENTINEL))) {

					temp[j] = lastCons[i + 1][j];

					j++;
				}

				//while
				temp[j] = net.sf.jftp.gui.tasks.LastConnections.SENTINEL;

				j = 0;

				//while (!(lastCons[i+1][j].equals(SENTINEL))) {
				while (!(newString[j].equals(net.sf.jftp.gui.tasks.LastConnections.SENTINEL))) {
					lastCons[i + 1][j] = newString[j];

					//System.out.println(lastCons[i+1][j]);
					j++;
				}

				lastCons[i + 1][j] = net.sf.jftp.gui.tasks.LastConnections.SENTINEL;

			}

			//if
		}

		//for
		for (int i = 0; i < capacity; i++) {
			j = 0;

			while (!lastCons[i][j].equals(net.sf.jftp.gui.tasks.LastConnections.SENTINEL)) {
				//System.out.println(lastCons[i][j]);
				if (lastCons[i][j].equals(net.sf.jftp.gui.tasks.LastConnections.SENTINEL)) {
					break;
				}

				j++;
			}

		}

		//***
		if (newConnection) {
			net.sf.jftp.gui.tasks.LastConnections.writeToFile(lastCons, capacity);
		}

		return lastCons;
	}

	//prepend
	public static String[][] moveToFront(final int position, final int capacity) {
		String[][] lastCons = new String[capacity][net.sf.jftp.JFtp.CONNECTION_DATA_LENGTH];
		String[][] newLastCons = new String[capacity][net.sf.jftp.JFtp.CONNECTION_DATA_LENGTH];

		lastCons = net.sf.jftp.gui.tasks.LastConnections.readFromFile(capacity);

		final String[] temp = new String[net.sf.jftp.JFtp.CONNECTION_DATA_LENGTH];

		int j = 0;
		temp[j] = lastCons[position][j];

		while (!(lastCons[position][j].equals(net.sf.jftp.gui.tasks.LastConnections.SENTINEL))) {
			j++;
			temp[j] = lastCons[position][j];
		}

		j = 0;

		//System.out.println("START");
		while (!(lastCons[position][j].equals(net.sf.jftp.gui.tasks.LastConnections.SENTINEL))) {
			//System.out.println(lastCons[position][j]);
			j++;
		}

		newLastCons = net.sf.jftp.gui.tasks.LastConnections.prepend(temp, position + 1, false);

		for (int i = 0; i <= position; i++) {
			j = 0;

			//while (!(lastCons[position][i].equals(SENTINEL))) {
			//while (!(lastCons[i][j].equals(SENTINEL))) {
			while (!(newLastCons[i][j].equals(net.sf.jftp.gui.tasks.LastConnections.SENTINEL))) {
				lastCons[i][j] = newLastCons[i][j];
				j++;
			}

			lastCons[i][j] = net.sf.jftp.gui.tasks.LastConnections.SENTINEL;
		}

		//for
		net.sf.jftp.gui.tasks.LastConnections.writeToFile(lastCons, capacity);

		return lastCons;
	}

	//moveToFront
	public static int findString(final String[] findVal, final int capacity) {
		//BUGFIX: 2D
		//String[] lastCons = new String[capacity];
		String[][] lastCons = new String[capacity][net.sf.jftp.JFtp.CONNECTION_DATA_LENGTH];

		lastCons = net.sf.jftp.gui.tasks.LastConnections.readFromFile(capacity);

		for (int i = 0; i < capacity; i++) {
			int j = 0;

			while ((net.sf.jftp.JFtp.CAPACITY > j) && findVal[j].equals(lastCons[i][j]) && !(lastCons[i][j].equals(net.sf.jftp.gui.tasks.LastConnections.SENTINEL)) && !(findVal[j].equals(net.sf.jftp.gui.tasks.LastConnections.SENTINEL))) {

				j++;

			}

			if (findVal[j].equals(lastCons[i][j])) {

				return i;
			}
		}

		//if not found, return -1
		return -1;
	}


	private static void init(final int capacity) {

		try {
			final FileOutputStream fos;
			final PrintStream out;

			fos = new FileOutputStream(net.sf.jftp.config.Settings.last_cons);
			out = new PrintStream(fos);

			for (int i = 0; i < capacity; i++) {
				out.println("null");
				out.println(net.sf.jftp.gui.tasks.LastConnections.SENTINEL);
			}

			fos.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}

		//return f;
	}

	//init
	private static void changeFile(final String[] oldValues) {
		StringTokenizer tokens;

		final String[][] newData = new String[net.sf.jftp.JFtp.CAPACITY][net.sf.jftp.JFtp.CONNECTION_DATA_LENGTH];

		for (int i = 0; net.sf.jftp.JFtp.CAPACITY > i; i++) {
			//System.out.println(oldValues[i]);
			tokens = new StringTokenizer(oldValues[i], " ");

			int j = 0;

			while ((tokens.hasMoreTokens())) {
				newData[i][j] = tokens.nextToken();

				//System.out.println(newData[i][j]);
				j++;
			}

			newData[i][j] = net.sf.jftp.gui.tasks.LastConnections.SENTINEL;

			if (newData[i][0].equals("SFTP") && (5 == j)) {
				String temp = "";
				String temp2 = "";

				temp = newData[i][4];
				newData[i][4] = "22";

				temp2 = newData[i][5];

				newData[i][5] = temp;


				newData[i][6] = net.sf.jftp.gui.tasks.LastConnections.SENTINEL;

			}

			//if
		}

		//for
		net.sf.jftp.gui.tasks.LastConnections.writeToFile(newData, net.sf.jftp.JFtp.CAPACITY);
	}

	//changeFile
}


//LastConnections
