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
package net.sf.jftp.gui.tasks;

import net.sf.jftp.*;

//***
import net.sf.jftp.config.*;
import net.sf.jftp.gui.framework.*;
import net.sf.jftp.net.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


//***
public class AdvancedOptions extends HPanel implements ActionListener
{
    //used this to see if it needs to look up file value
    public static boolean listOptionSet = false;
    private HTextField listCommand = new HTextField("FTP LIST command:",
                                                    FtpConnection.LIST, 15);
    private JButton setListCommand = new JButton("Set");

    //*** 
    private JButton saveCommand = new JButton("Set and Save");

    //***
    //*** should it really be set up so that the original message here on top
    //*** is cleared out when events such as changing settings occurs
    //*** (the text area which contains instructions at first has these
    //*** instructions cleared out when some events occur.)
    //*** So maybe should have a text box at the bottom? And a label at top
    //*** with the instruction text?
    //***this line may be deprecated
    private JLabel text = new JLabel();
    //private JLabel note = new JLabel();

    //***
    private JLabel statusText = new JLabel();
    private String listOptionText = new String(); //what the text in the box initially is

    //***
    public AdvancedOptions()
    {
        setLayout(new BorderLayout(5, 5));

        //text.setText("You can override the default values here, but note that " +
        //	     "the values are not saved\n and lost after closing the application.");
        text.setText("Default values for commands can be overidden here.");
        statusText.setText("Note: The FTP LIST command should be \"LIST\" when connecting to an OS/2 server.");

        text.setPreferredSize(new Dimension(400, 30));

        //text.setLineWrap(true);
        //***
        statusText.setPreferredSize(new Dimension(400, 30));

        //statusText.setLineWrap(true);
        //***
        //load initial advanced options here
        //JUST FOR NOW: We just know that value 0 is the FTP LIST command
        if(listOptionSet)
        {
            listOptionText = FtpConnection.LIST;
        }
        else
        {
            //AND NEED TO CHECK IF FILE DOESN'T EXIST (if not, create it
            //and set the file and settings to the default
            if(LoadSet.loadSet(Settings.adv_settings) != null)
            {
                listOptionText = LoadSet.loadSet(Settings.adv_settings)[0];
            }
            else
            {
                listOptionText = FtpConnection.LIST_DEFAULT;

                SaveSet s = new SaveSet(Settings.adv_settings,
                                        FtpConnection.LIST_DEFAULT);
            }
        }

        listCommand.setText(listOptionText);

        HPanel content = new HPanel();
        HPanel panel = new HPanel();
        panel.add(listCommand);
        panel.add(setListCommand);

        //***
        panel.add(saveCommand);

        //***
        content.add(panel);

        add("North", text);
        add("Center", content);

        add("South", statusText);

        setListCommand.addActionListener(this);
        saveCommand.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource() == setListCommand)
        {
            FtpConnection.LIST = listCommand.getText().trim();

            //text.setText("LIST command set.");
            //***
            statusText.setText("LIST command set.");
            listOptionSet = true;

            //***
        }

        //if(e.getSource() == saveCommand)
        else
        {
            //isn't the following line redundant? So I commented it out
            //it would be redundant if the LIST command is read in by LoadSet each time
            //FtpConnection.LIST = listCommand.getText().trim();
            FtpConnection.LIST = listCommand.getText().trim();

            listOptionSet = true;

            //text.setText("LIST command set and saved");
            //statusText.setText("LIST command set and saved.");
            SaveSet s = new SaveSet(Settings.adv_settings,
                                    listCommand.getText().trim());

            //***
            //text.setText("LIST command set and saved");
            statusText.setText("LIST command set and saved.");

            //***
        }
    }
    
	public Insets getInsets() 
	{ 
		return new Insets(super.getInsets().top+5,super.getInsets().left+5,super.getInsets().bottom+5,super.getInsets().right+5);
	}
}
