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
package net.sf.jftp.tools;

import net.sf.jftp.config.Settings;
import net.sf.jftp.gui.base.StatusCanvas;
import net.sf.jftp.gui.framework.HImageButton;
import net.sf.jftp.system.LocalIO;
import net.sf.jftp.system.logging.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Enumeration;


public class RSSFeeder extends JPanel implements Runnable, ActionListener {
    public static String urlstring = Settings.getRSSFeed();
    Thread runner;
    URL url;
    RSSParser parser;
    StatusCanvas can = new StatusCanvas();
    HImageButton next = new HImageButton(Settings.nextRSSImage, "nextRSS",
            "Display next RSS news item", this);
    boolean header = false;
    boolean breakHeader = false;
    int HEADER_IVAL = 4000;
    int LOAD_IVAL = 31 * 60000;

    //"http://www.spiegel.de/schlagzeilen/rss/0,5291,,00.xml";
    public RSSFeeder() {
        setLayout(new BorderLayout(0, 0));
        next.setPreferredSize(new Dimension(22, 22));
        next.setMaximumSize(new Dimension(22, 22));

        //next.addActionListener(this);
        add("West", next);
        add("Center", can);
        setPreferredSize(new Dimension(500, 25));
        runner = new Thread(this);
        runner.start();
    }

    public void switchTo(String u) {
        if (u == null) {
            return;
        }

        urlstring = u;

        runner.stop();
        runner = new Thread(this);
        runner.start();
    }

    public void run() {
        long time;

        LocalIO.pause(3000);

        Log.out("Starting RSS Feed");

        try {
            can.setInterval(10);
            url = new URL(urlstring);
            parser = new RSSParser(url);
            time = System.currentTimeMillis();
        } catch (Exception ex) {
            Log.debug("Error: Can't load RSS feed (" + ex + ")");
            ex.printStackTrace();

            return;
        }

        while (true) {
            try {
                Enumeration e = parser.titles.elements();
                Enumeration e2 = parser.descs.elements();

                while (e.hasMoreElements()) {
                    can.setText((String) e.nextElement());
                    next.setEnabled(true);
                    header = true;

                    int i = 0;

                    while (!breakHeader && (i < 100)) {
                        LocalIO.pause(HEADER_IVAL / 100);
                        i++;
                    }

                    next.setEnabled(false);
                    breakHeader = false;
                    header = false;

                    if (e2.hasMoreElements()) {
                        next.setEnabled(true);
                        can.scrollText((String) e2.nextElement());
                        next.setEnabled(false);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (System.currentTimeMillis() > (LOAD_IVAL + time)) {
                parser = new RSSParser(url);
                time = System.currentTimeMillis();
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == next) {
            if (header) {
                breakHeader = true;
            } else {
                can.forward();
            }
        }
    }
}
