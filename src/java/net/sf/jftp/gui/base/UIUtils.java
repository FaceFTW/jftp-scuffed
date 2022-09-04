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
package net.sf.jftp.gui.base;

import java.awt.Desktop;


import java.io.File;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import net.sf.jftp.tools.Shell;


public class UIUtils
{
    public static String getPasswordFromUser(JComponent parent)
    {
        JOptionPane j = new JOptionPane();
        JPasswordField pField = new JPasswordField();

        int ret = j.showOptionDialog(parent, pField, "Password required",
                                     JOptionPane.YES_NO_OPTION,
                                     JOptionPane.QUESTION_MESSAGE, null, null,
                                     null);

        return pField.getText();
    }
    
    public static void runCommand(String cmd) throws IOException {		
		if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.OPEN)) {
				File file = null;
				if (cmd.startsWith("file:")){					
					file = new File(cmd.substring(6));
				} else {
					file = new File(cmd);
				}
				System.out.println("Opening: "+file);
				desktop.open(file);
			}
        }     	
    }
}

class Spawn implements Runnable {
	private Thread runner;
	private String cmd;
	
	public Spawn(String cmd) {
		this.cmd = cmd;
		
		runner = new Thread(this);
		runner.start();
	}
	
	public void run() {
        try {   	
        	String str = "";
        	
        	if(cmd.startsWith("file://")) cmd = cmd.substring(7);
        	
        	Process p = Runtime.getRuntime().exec(cmd);       	
        	new Shell(p.getInputStream(), p.getOutputStream());
        	 
        	//while ((str = stdout.readLine()) !=null) {
        	//	Log.debug(str);
        	//} 
         }
         catch(Exception ex) {
        	 ex.printStackTrace();
         }
	}
	
}

