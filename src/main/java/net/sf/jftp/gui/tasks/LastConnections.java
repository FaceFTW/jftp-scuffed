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
	public LastConnections(net.sf.jftp.JFtp jftp) {
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
	public static void writeToFile(String[][] a, int capacity) {
		try {
			File f1 = new File(net.sf.jftp.config.Settings.appHomeDir);
			f1.mkdir();

			File f2 = new File(net.sf.jftp.config.Settings.last_cons);
			f2.createNewFile();

			FileOutputStream fos;
			PrintStream out;

			fos = new FileOutputStream(net.sf.jftp.config.Settings.last_cons);
			out = new PrintStream(fos);

			//String[] lastCons = new String[capacity];
			//lastCons = readFromFile(capacity);
			for (int i = 0; i < capacity; i++) {
				//out.println(a[i]);
				int j = 0;
				out.println(a[i][j]);

				while ((j < net.sf.jftp.JFtp.CAPACITY) && !(a[i][j].equals(SENTINEL))) {
					j++;
					out.println(a[i][j]);

					//retVal[i][j] = raf.readLine();
					//System.out.println();
				}

				//System.out.println(a[i]);
			}

            /*
            RandomAccessFile raf =
                    new RandomAccessFile(f2, "rw");

            for (int i = 0; i < capacity; i++) {

                    raf.writeChars(a[i] + "\n");


            }
            */
			net.sf.jftp.JFtp.updateMenuBar();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//writeToFile
	public static String[][] readFromFile(int capacity) {
		//MAKE THIS 2D
		//String[] retVal = new String[capacity];
		String[][] retVal = new String[capacity][net.sf.jftp.JFtp.CONNECTION_DATA_LENGTH];

		//if ((f.exists()))
		//	System.out.println("not found");
		try {
			File f1 = new File(net.sf.jftp.config.Settings.appHomeDir);
			f1.mkdir();

			File f2 = new File(net.sf.jftp.config.Settings.last_cons);

			//File f;
			//f = init(capacity);
			if (!f2.exists()) {
				init(capacity);
			}

            /*
            FileReader fr = new FileReader(Settings.last_cons);

            BufferedReader breader = new BufferedReader(fr);

            for (int i=0; i<capacity; i++) {

                   retVal[i] = breader.readLine();
            }
            */
			RandomAccessFile raf = new RandomAccessFile(f2, "r");

			//BUGFIX 1.40: determine if old style of file is
			//             being used
			//THIS MAY MAKE THIS SECTION LESS TIME-EFFICIENT
			String[] oldValues = new String[capacity];
			String firstSection = "";
			boolean oldVersion = true;

			//System.out.println(capacity);
			for (int i = 0; i < capacity; i++) {
				if (capacity < net.sf.jftp.JFtp.CAPACITY) {
					oldVersion = false;

					break;
				}

				oldValues[i] = raf.readLine();
				firstSection = oldValues[i].substring(0, 3);

				//System.out.print(i);
				//System.out.print(firstSection);
				//System.out.println("end");
				if (!(firstSection.equals("FTP")) && !(firstSection.equals("SFT")) && !(firstSection.equals("SMB")) && !(firstSection.equals("NFS")) && !(firstSection.equals("nul"))) {
					//System.out.println("###");
					oldVersion = false;
				}

				if (!oldVersion) {
					//System.out.println("!!!");
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

				changeFile(oldValues);
			}

			//reset to read start of file
			raf = new RandomAccessFile(f2, "r");

			for (int i = 0; i < capacity; i++) {
				int j = 0;
				retVal[i][j] = raf.readLine();

				//System.out.print("--->");
				//System.out.println(retVal[i][j]);
				while ((j < net.sf.jftp.JFtp.CONNECTION_DATA_LENGTH) && !(retVal[i][j].equals(SENTINEL))) {
					j++;
					retVal[i][j] = raf.readLine();

					//System.out.print("--->");
					//System.out.println(retVal[i][j]);
					//j++;
				}

				//while
				//retVal[i][j+1] = raf.readLine();
			}

			//for
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return retVal;
	}

	//readFromFile
	public static String[][] prepend(String[] newString, int capacity, boolean newConnection) {
		//BUGFIX: 2D
		//String[] lastCons = new String[capacity];
		String[][] lastCons = new String[capacity][net.sf.jftp.JFtp.CONNECTION_DATA_LENGTH];

		lastCons = readFromFile(capacity);

		for (int i = 0; i < capacity; i++) {
			int j = 0;

			while (!(lastCons[i][j].equals(SENTINEL))) {
				//temp[j] = lastCons[i][j];
				//lastCons[i][j] = newString[j];
				//System.out.println(lastCons[i][j]);
				j++;
			}

			//System.out.println(lastCons[i][j]);
		}

		//BUGFIX: now need array to represent 1st connection data
		//String temp = new String("");
		String[] temp = new String[net.sf.jftp.JFtp.CONNECTION_DATA_LENGTH];

		int j = 0;

		//is temp necessary?
		//System.out.println("++++++++++++++++++++");
		while (!(lastCons[0][j].equals(SENTINEL))) {
			//while (!(newString[j].equals(SENTINEL))) {
			temp[j] = lastCons[0][j];

			//lastCons[0][j] = newString[j];
			//System.out.println(lastCons[0][j]);
			//if (lastCons[0][j].equals(SENTINEL))
			//break;
			j++;
		}

		temp[j] = SENTINEL;

		j = 0;

		while (!(newString[j].equals(SENTINEL))) {
			lastCons[0][j] = newString[j];
			j++;
		}

		lastCons[0][j] = SENTINEL;

		//take out any data that would be "left over" after sentinel
		//is this necessary?
		j++;

		while (j < net.sf.jftp.JFtp.CONNECTION_DATA_LENGTH) {
			lastCons[0][j] = "";
			j++;
		}

		//System.out.println("-----------------");
		for (int i = 0; i < capacity; i++) {
			//while (!(lastCons[j].equals(SENTINEL))) {
			//newString = temp;
			if ((i + 1) != capacity) {
				//System.out.print("-->");
				j = 0;

				//System.out.println(temp[j]);
				//shift temp data into newString
				while (!(temp[j].equals(SENTINEL))) {
					newString[j] = temp[j];

					//System.out.println(newString[j]);
					j++;
				}

				newString[j] = SENTINEL;

				//System.out.println("$$$$$$$$$$$$$");
				//store lastCons data in temp again
				j = 0;

				//while (!(temp[j].equals(SENTINEL))) {
				while (!(lastCons[i + 1][j].equals(SENTINEL))) {
					//newString[j] = temp[j];
					temp[j] = lastCons[i + 1][j];

					//temp[j] = lastCons[i + 1][j];
					//lastCons[i+1][j] = newString[j];
					//j++;
					//System.out.println(lastCons[i+1][j]);
					//System.out.println(temp[j]);
					//if (temp[j].equals(SENTINEL))
					//if (lastCons[i][j].equals(SENTINEL))
					//break;
					//temp[j] = lastCons[i+1][j];
					j++;
				}

				//while
				temp[j] = SENTINEL;

				//System.out.println("+-+-+-+-+-+-");
				//then, make adjustments to lastCons
				j = 0;

				//while (!(lastCons[i+1][j].equals(SENTINEL))) {
				while (!(newString[j].equals(SENTINEL))) {
					lastCons[i + 1][j] = newString[j];

					//System.out.println(lastCons[i+1][j]);
					j++;
				}

				lastCons[i + 1][j] = SENTINEL;

				//j=0;

                /*
                while (!(lastCons[i][j].equals(SENTINEL))) {

                        temp[j] = lastCons[i+1][j];
                        j++;

                }
                */

				//lastCons[i+1][j] = SENTINEL;
			}

			//if
		}

		//for
		for (int i = 0; i < capacity; i++) {
			j = 0;

			while (!lastCons[i][j].equals(SENTINEL)) {
				//System.out.println(lastCons[i][j]);
				if (lastCons[i][j].equals(SENTINEL)) {
					break;
				}

				j++;
			}

			//System.out.println(lastCons[i][j]);
		}

		//***
		if (newConnection) {
			writeToFile(lastCons, capacity);
		}

		return lastCons;
	}

	//prepend
	public static String[][] moveToFront(int position, int capacity) {
		//make these 2D
		//String[] lastCons = new String[capacity];
		//String[] newLastCons = new String[capacity];
		String[][] lastCons = new String[capacity][net.sf.jftp.JFtp.CONNECTION_DATA_LENGTH];
		String[][] newLastCons = new String[capacity][net.sf.jftp.JFtp.CONNECTION_DATA_LENGTH];

		//System.out.print("--------:");
		//System.out.println(capacity);
		lastCons = readFromFile(capacity);

		//String temp = new String("");
		String[] temp = new String[net.sf.jftp.JFtp.CONNECTION_DATA_LENGTH];

		//String[] lastCons = new String[capacity];
		int j = 0;
		temp[j] = lastCons[position][j];

		while (!(lastCons[position][j].equals(SENTINEL))) {
			j++;
			temp[j] = lastCons[position][j];
		}

		j = 0;

		//System.out.println("START");
		while (!(lastCons[position][j].equals(SENTINEL))) {
			//System.out.println(lastCons[position][j]);
			j++;
		}

		//System.out.println("END");
		//possible bugfix code?
        /*
        for (int i=0; i<JFtp.CONNECTION_DATA_LENGTH; i++) {
                temp[i] = lastCons[position][i];
        }
        */
		newLastCons = prepend(temp, position + 1, false);

		for (int i = 0; i <= position; i++) {
			j = 0;

			//while (!(lastCons[position][i].equals(SENTINEL))) {
			//while (!(lastCons[i][j].equals(SENTINEL))) {
			while (!(newLastCons[i][j].equals(SENTINEL))) {
				//j++;
				//temp[i] = lastCons[position][i];
				//System.out.println(i);
				//System.out.println(j);
				//System.out.println(newLastCons[i][j]);
				lastCons[i][j] = newLastCons[i][j];
				j++;
			}

			lastCons[i][j] = SENTINEL;
		}

		//for
		writeToFile(lastCons, capacity);

		return lastCons;
	}

	//moveToFront
	public static int findString(String[] findVal, int capacity) {
		//BUGFIX: 2D
		//String[] lastCons = new String[capacity];
		String[][] lastCons = new String[capacity][net.sf.jftp.JFtp.CONNECTION_DATA_LENGTH];

		lastCons = readFromFile(capacity);

		for (int i = 0; i < capacity; i++) {
			int j = 0;

			while ((j < net.sf.jftp.JFtp.CAPACITY) && findVal[j].equals(lastCons[i][j]) && !(lastCons[i][j].equals(SENTINEL)) && !(findVal[j].equals(SENTINEL))) {
				//System.out.println("start ");
				//System.out.print(lastCons[i][j]);
				//System.out.print(findVal[j]);
				//System.out.println("end");
				j++;

				//if (findVal.equals(lastCons[i]))
				//return i;
			}

			if (findVal[j].equals(lastCons[i][j])) {
				//System.out.println("test");
				//System.out.println(lastCons[i][j]);
				//System.out.println(findVal[j]);
				return i;
			} else {
				//System.out.println("test2");
				//System.out.println(lastCons[i][j]);
				//System.out.println(findVal[j]);
				//System.out.println("NO");
			}
		}

		//if not found, return -1
		return -1;
	}

	//findString

	/*
	//unnecessary?
	public static String[] swap(String a[], int pos1, int pos2) {

			String temp = new String("");

			temp = a[pos2];
			a[pos2] = a[pos1];
			a[pos1] = temp;

			return a;

	} //swap
	*/
	private static void init(int capacity) {
		//File f = new File(Settings.last_cons);
		try {
			FileOutputStream fos;
			PrintStream out;

			fos = new FileOutputStream(net.sf.jftp.config.Settings.last_cons);
			out = new PrintStream(fos);

			for (int i = 0; i < capacity; i++) {
				out.println("null");
				out.println(SENTINEL);
			}

			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		//return f;
	}

	//init
	private static void changeFile(String[] oldValues) {
		StringTokenizer tokens;

		String[][] newData = new String[net.sf.jftp.JFtp.CAPACITY][net.sf.jftp.JFtp.CONNECTION_DATA_LENGTH];

		//this assumes that capacity will not change
		//should this be set to 9 instead of JFtp.CAPACITY?
		for (int i = 0; i < net.sf.jftp.JFtp.CAPACITY; i++) {
			//System.out.println(oldValues[i]);
			tokens = new StringTokenizer(oldValues[i], " ");

			int j = 0;

			while ((tokens.hasMoreTokens())) {
				newData[i][j] = tokens.nextToken();

				//System.out.println(newData[i][j]);
				j++;
			}

			newData[i][j] = SENTINEL;

			//System.out.print("---->");
			//System.out.println(newData[i][0]);
			//System.out.println(j);
			//for backwards compatibility with versions that
			//don't have SFTP port remembered: enter port 22
			//(which is default) there
			if (newData[i][0].equals("SFTP") && (j == 5)) {
				String temp = "";
				String temp2 = "";

				temp = newData[i][4];
				newData[i][4] = "22";

				temp2 = newData[i][5];

				newData[i][5] = temp;

				//temp2 = newData[i][5];
				//newData[i][6] = temp2;
				//newData[i][7] = SENTINEL;
				newData[i][6] = SENTINEL;

                /*
                for (int j=5; j++; j<7) {

                        temp = newData[i][j];
                        newData[i][j] = newData[i][j-1];

                } */
			}

			//if
		}

		//for
		writeToFile(newData, net.sf.jftp.JFtp.CAPACITY);
	}

	//changeFile
}


//LastConnections
