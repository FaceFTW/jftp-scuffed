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

import javax.swing.*;
import java.awt.*;


public class HComboBox extends JPanel //implements ActionListener
{
	private final JLabel label;
	public final JComboBox comboBox;

	public HComboBox(String l) {
		setLayout(new BorderLayout(5, 5));

		label = new JLabel(l + "  ");
		add("West", label);

		//comboBox = new JComboBox(t, 12);
		comboBox = new JComboBox();
		add("East", comboBox);

		setVisible(true);
	}

	public HComboBox(String l, String[] t) {
		setLayout(new BorderLayout(5, 5));

		label = new JLabel(l + "  ");
		add("West", label);

		//comboBox = new JComboBox(t, 12);
		comboBox = new JComboBox(t);
		add("East", comboBox);

		setVisible(true);
	}

	/*
	public HComboBox(String l, String t, int size)
	{
		setLayout(new BorderLayout(5, 5));

		label = new JLabel(l + "  ");
		add("West", label);

		comboBox = new JComboBox(t, size);
		add("East", comboBox);

		setVisible(true);
	}
	*/
	public String getLabel() {
		return label.getText();
	}

	public void setLabel(String l) {
		label.setText(l + "  ");
	}

	public Object getSelectedItem() {
		return comboBox.getSelectedItem();
	}

	public void addItem(Object obj) {
		comboBox.addItem(obj);
	}

	public void addActionListener(java.awt.event.ActionListener actListen) {
		comboBox.addActionListener(actListen);
	}

	/*
	public String getText()
	{
		return comboBox.getText();
	}

	public void setText(String t)
	{
		comboBox.setText(t);
	}

	/*
	public void requestFocus()
	{
		text.requestFocus();
	}
	*/
	public void setEditable(boolean yesno) {
		comboBox.setEditable(yesno);
	}
}
