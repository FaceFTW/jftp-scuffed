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
package net.sf.jftp.gui.framework;

import java.awt.*;

import javax.swing.*;

import net.miginfocom.swing.MigLayout;


public class HTextField extends JPanel
{
    private JLabel label;
    public JTextField text;
    
    public HTextField(String l, String t, int size)
    {
    	init(l, t, size, false);
    }
    
    public HTextField(String l, String t)
    {
    	init(l, t, 12, false);
    } 
    
    public HTextField(String l, String t, boolean isPw)
    {
    	init(l, t, 12, isPw);
    }      
    
    public void init(String l, String t, int size, boolean isPw)
    {
        setLayout(new MigLayout());

        label = new JLabel(l);
        add(label);

        text = isPw ? 
        	new JPasswordField(t, size){
	        	public Insets getInsets() 
	        	{ 
	        		return new Insets(4,4,4,4);
	        	}
        	} 
        	: 
        	new JTextField(t, size){
	        	public Insets getInsets() 
	        	{ 
	        		return new Insets(4,4,4,4);
	        	}
        	};
        	
        add(text, "align right");

        setVisible(true);
    }



    public String getLabel()
    {
        return label.getText();
    }

    public void setLabel(String l)
    {
        label.setText(l + "  ");
    }

    public String getText()
    {
        return text.getText();
    }

    public void setText(String t)
    {
        text.setText(t);
    }

    public void requestFocus()
    {
        text.requestFocus();
    }

    public void setEnabled(boolean yesno)
    {
        text.setEnabled(yesno);
    }
}
