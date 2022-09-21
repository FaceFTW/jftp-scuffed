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

import net.sf.jftp.JFtp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


public class HImageButton extends JButton implements MouseListener {
    public String label = "";
    public ActionListener who = null;
    private String cmd = "default";

    public HImageButton(String image, String cmd, String label,
                        ActionListener who) {
        this.cmd = cmd;
        this.label = label;
        this.who = who;

        try {
            setIcon(new ImageIcon(HImage.getImage(this, image)));
        } catch (Exception ex) {
            System.out.println("Image file: " + image);
            ex.printStackTrace();
        }
        addMouseListener(this);

        setVisible(true);
        setMinimumSize(new Dimension(25, 25));
        setPreferredSize(new Dimension(25, 25));
        setMaximumSize(new Dimension(25, 25));
    }

    public void update(Graphics g) {
        paintComponent(g);
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
        who.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                cmd));

    }

    public void mouseEntered(MouseEvent e) {
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        JFtp.statusP.status(label);
    }

    public void mouseExited(MouseEvent e) {
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        JFtp.statusP.status("");
    }
}
