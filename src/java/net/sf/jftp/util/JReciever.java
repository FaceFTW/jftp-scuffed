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

import net.sf.jftp.system.LocalIO;

import javax.swing.JScrollBar;
import java.io.DataInputStream;


public class JReciever implements Runnable {
    private DataInputStream in;
    private Thread reciever;
    private final byte[] buf = new byte[4048];

    public JReciever(DataInputStream in) {
        this.in = in;
        reciever = new Thread(this);
        reciever.start();
    }

    public void run() {
        try {
            while (true) {
                int cnt = in.read(buf);

                if (cnt == -1) {
                    RawConnection.output.append(">>> Connection closed...");

                    LocalIO.pause(100);

                    JScrollBar bar = RawConnection.outputPane.getVerticalScrollBar();
                    bar.setValue(bar.getMaximum());

                    in.close();

                    return;
                } else {
                    String tmp = new String(buf);
                    tmp = tmp.substring(0, cnt);

                    RawConnection.output.append(tmp);
                    LocalIO.pause(100);

                    JScrollBar bar = RawConnection.outputPane.getVerticalScrollBar();
                    bar.setValue(bar.getMaximum());

                    LocalIO.pause(400);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void reset(DataInputStream in) {
        reciever.destroy();
        this.in = in;
        reciever = new Thread(this);
        reciever.start();
    }
}
