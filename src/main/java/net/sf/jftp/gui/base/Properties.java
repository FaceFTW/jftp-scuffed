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

import net.sf.jftp.JFtp;
import net.sf.jftp.gui.framework.HButton;
import net.sf.jftp.gui.framework.HFrame;
import net.sf.jftp.gui.framework.HPanel;
import net.sf.jftp.system.logging.Log;

import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;


public class Properties extends HFrame implements ActionListener {
	private final Label fileL = new Label("File:                      ");
	private final Label sizeL = new Label("Size: ? bytes              ");
	private final HButton ok = new HButton("Dismiss");
	private String type = "";
	private String file = "";

	public Properties(String file, String type) {
		super();
		this.file = file;
		this.type = type;

		this.setSize(300, 110);
		this.setTitle("File properties...");
		this.setLocation(150, 150);
		this.setLayout(new GridLayout(3, 1));

		HPanel okP = new HPanel();
		okP.add(this.ok);
		this.add(this.sizeL);
		this.add(this.fileL);
		this.add(okP);
		this.ok.addActionListener(this);

		this.process();
		this.setVisible(true);
	}

	private void process() {
		if (this.type.equals("local")) {
			File f = new File(JFtp.localDir.getPath() + this.file);
			this.sizeL.setText("Size: " + f.length() + " bytes");

			try {
				this.fileL.setText("File: " + f.getCanonicalPath());
			} catch (Exception ex) {
				Log.debug(ex.toString());
			}

			this.sizeL.setText("Size: " + f.length() + " bytes");
		}

		if (this.type.equals("remote")) {
			this.fileL.setText("File: " + JFtp.remoteDir.getPath() + this.file);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.ok) {
			this.setVisible(false);
		}
	}
}
