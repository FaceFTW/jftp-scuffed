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

import net.sf.jftp.gui.framework.*;
import net.sf.jftp.util.*;

import java.awt.*;
import java.awt.event.*;


public class RemoverQuery extends HFrame implements ActionListener
{
    private String file;
    private String type;
    private HButton ok = new HButton("Ok");
    private HButton cancel = new HButton("Cancel");

    public RemoverQuery(String file, String type)
    {
        this.file = file;
        this.type = type;

        setSize(200, 70);
        setTitle("Really?");
        setLayout(new FlowLayout());
        setLocation(150, 150);

        add(ok);
        add(cancel);

        ok.addActionListener(this);
        cancel.addActionListener(this);

        setVisible(true);
    }

    public void actionPerformed(ActionEvent e)
    {
        if(e.getSource() == ok)
        {
            AutoRemover armv = new AutoRemover(file, type);
            this.dispose();
        }
        else
        {
            this.dispose();
        }
    }
}
