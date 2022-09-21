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
package net.sf.jftp.util;

import net.sf.jftp.gui.framework.HFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class JHostChooser extends HFrame implements ActionListener {
    private final JLabel hostL = new JLabel("Host:");
    private final JLabel portL = new JLabel("Port:");
    private final JTextField host = new JTextField(20);
    private final JTextField port = new JTextField(5);
    private final JPanel p1 = new JPanel();
    private final JPanel okP = new JPanel();
    private final JButton ok = new JButton("Use these settings");

    public JHostChooser() {
        setSize(400, 120);
        setLocation(200, 250);
        setTitle("Connection...");
        getContentPane().setLayout(new BorderLayout());
        setBackground(Color.lightGray);

        p1.add(hostL);
        p1.add(host);
        p1.add(portL);
        p1.add(port);

        host.setText(RawConnection.host.getText());
        port.setText(RawConnection.port.getText());

        getContentPane().add("Center", p1);
        getContentPane().add("South", okP);
        okP.add(ok);
        ok.addActionListener(this);

        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == ok) {
            RawConnection.host.setText(host.getText());
            RawConnection.port.setText(port.getText());
            RawConnection.established = false;
            RawConnection.mayDispose = true;
            this.dispose();
        }
    }

    public Insets getInsets() {
        Insets std = super.getInsets();

        return new Insets(std.top + 5, std.left + 5, std.bottom + 5,
                std.right + 5);
    }
}
