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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class RemoverQuery extends net.sf.jftp.gui.framework.HFrame implements ActionListener {
	private final String file;
	private final String type;
	private final net.sf.jftp.gui.framework.HButton ok = new net.sf.jftp.gui.framework.HButton("Ok");

	public RemoverQuery(String file, String type) {
		super();
		this.file = file;
		this.type = type;

		this.setSize(200, 70);
		this.setTitle("Really?");
		this.setLayout(new FlowLayout());
		this.setLocation(150, 150);

		this.add(this.ok);
		net.sf.jftp.gui.framework.HButton cancel = new net.sf.jftp.gui.framework.HButton("Cancel");
		this.add(cancel);

		this.ok.addActionListener(this);
		cancel.addActionListener(this);

		this.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.ok) {
			AutoRemover armv = new AutoRemover(this.file, this.type);
			this.dispose();
		} else {
			this.dispose();
		}
	}
}
