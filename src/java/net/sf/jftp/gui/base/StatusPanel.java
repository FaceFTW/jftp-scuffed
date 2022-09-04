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
import net.sf.jftp.config.Settings;
import net.sf.jftp.gui.framework.*;
import net.sf.jftp.gui.hostchooser.HostChooser;
import net.sf.jftp.gui.hostchooser.NfsHostChooser;
import net.sf.jftp.gui.hostchooser.SftpHostChooser;
import net.sf.jftp.gui.hostchooser.SmbHostChooser;
import net.sf.jftp.gui.hostchooser.WebdavHostChooser;
import net.sf.jftp.gui.tasks.HttpBrowser;
import net.sf.jftp.gui.tasks.NativeHttpBrowser;
import net.sf.jftp.net.*;
import net.sf.jftp.net.wrappers.HttpTransfer;
import net.sf.jftp.system.logging.Log;
import net.sf.jftp.util.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;


public class StatusPanel extends HPanel implements ActionListener
{
    public static StatusCanvas status = new StatusCanvas();
    private HImageButton newcon = new HImageButton(Settings.hostImage,
                                                   "newcon",
                                                   "Add FTP Connection...", this);
    private HImageButton smbcon = new HImageButton(Settings.openImage,
                                                   "smbcon",
                                                   "Add SMB Connection...", this);
    private HImageButton sftpcon = new HImageButton(Settings.sftpImage,
                                                    "sftpcon",
                                                    "Add SFTP Connection...",
                                                    this);
    private HImageButton nfscon = new HImageButton(Settings.nfsImage, "nfscon",
                                                   "Add NFS Connection...", this);
    private HImageButton webdavcon = new HImageButton(Settings.webdavImage,
                                                      "webdavcon",
                                                      "Add WebDAV Connection...",
                                                      this);
    public HImageButton close = new HImageButton(Settings.closeImage, "close",
                                                 "Close active tab...", this);
    private HImageButton go = new HImageButton(Settings.refreshImage, "go",
                                               "Download URL now...", this);
    private JTextField address = new JTextField("http://www.xkcd.com", 30);
    public JFtp jftp;

    public StatusPanel(JFtp jftp)
    {
        this.jftp = jftp;
        setLayout(new BorderLayout());

        JToolBar bar = new JToolBar();

        /*
                FlowLayout f = new FlowLayout(FlowLayout.LEFT);
                f.setHgap(1);
                f.setVgap(2);
                bar.setLayout(f);
                bar.setMargin(new Insets(0,0,0,0));
        */
        Insets in = bar.getMargin();
        bar.setMargin(new Insets(in.top + 2, in.left + 4, in.bottom + 2,
                                 in.right + 4));

        bar.add(newcon);
        newcon.setSize(24, 24);
        newcon.setToolTipText("New FTP Connection...");
        bar.add(new JLabel(" "));

        bar.add(smbcon);
        smbcon.setSize(24, 24);
        smbcon.setToolTipText("New SMB Connection...");
        bar.add(new JLabel(" "));

        bar.add(sftpcon);
        sftpcon.setSize(24, 24);
        sftpcon.setToolTipText("New SFTP Connection...");
        bar.add(new JLabel(" "));

        bar.add(nfscon);
        nfscon.setSize(24, 24);
        nfscon.setToolTipText("New NFS Connection...");
        bar.add(new JLabel(" "));

        if(Settings.enableWebDav) bar.add(webdavcon);
        webdavcon.setSize(24, 24);
        webdavcon.setToolTipText("New WebDAV Connection...");
        bar.add(new JLabel("   "));

        bar.add(close);
        close.setSize(24, 24);
        close.setToolTipText("Close Active Remote tab...");
        bar.add(new JLabel("    "));

        address.addActionListener(this);
        bar.add(new JLabel("URL: "));
        bar.add(address);
        bar.add(new JLabel(" "));
        bar.add(go);

        //***
        go.setToolTipText("Download URL Now...");

        //***
        bar.add(new JLabel("    "));

        //bar.add(status);
        add("North", bar);

        validate();
        setFont(GUIDefaults.menuFont);
        setVisible(true);
    }

    public void status(String msg)
    {
        status.setText(msg);
    }

    public String getHost()
    {
        return status.getHost();
    }

    public void setHost(String host)
    {
        status.setHost(host);
    }

    public void actionPerformed(ActionEvent e)
    {
        if(e.getActionCommand().equals("go") || (e.getSource() == address))
        {
            Vector listeners = new Vector();
            listeners.add(JFtp.localDir);

            String url = address.getText().trim();

            startTransfer(url, JFtp.localDir.getPath(), listeners,
                          JFtp.getConnectionHandler());
        }
        else if(e.getActionCommand().equals("smbcon"))
        {
            //jftp.safeDisconnect();
            SmbHostChooser hc = new SmbHostChooser();
            hc.toFront();

            //hc.setModal(true);
            hc.update();
        }
        else if(e.getActionCommand().equals("sftpcon"))
        {
            //jftp.safeDisconnect();
            SftpHostChooser hc = new SftpHostChooser();
            hc.toFront();

            //hc.setModal(true);
            hc.update();
        }
        else if(e.getActionCommand().equals("nfscon"))
        {
            //jftp.safeDisconnect();
            NfsHostChooser hc = new NfsHostChooser();
            hc.toFront();

            //hc.setModal(true);
            hc.update();
        }
        else if(e.getActionCommand().equals("webdavcon"))
        {
            //jftp.safeDisconnect();
            WebdavHostChooser hc = new WebdavHostChooser();
            hc.toFront();

            //hc.setModal(true);
            hc.update();
        }
        else if(e.getActionCommand().equals("close"))
        {
            jftp.closeCurrentTab();
        }
        else if(e.getActionCommand().equals("newcon") && (!jftp.uiBlocked))
        {
            // jftp.safeDisconnect();
            // Switch windows
            // jftp.mainFrame.setVisible(false);
            HostChooser hc = new HostChooser();
            hc.toFront();

            //hc.setModal(true);
            hc.update();
        }
    }

    public void startTransfer(String url, String localPath, Vector listeners,
                              ConnectionHandler handler)
    {
        if(url.startsWith("ftp://") &&
               (url.endsWith("/") || (url.lastIndexOf("/") < 10)))
        {
            jftp.safeDisconnect();

            HostChooser hc = new HostChooser();
            hc.update(url);
        }
        else if(url.startsWith("http://") &&
                    (url.endsWith("/") || (url.lastIndexOf("/") < 10)))
        {
            try {
            	NativeHttpBrowser.main(new String[] {url});
            }
            catch(Throwable ex) {
                ex.printStackTrace();
                Log.debug("Native browser intialization failed, using JContentPane...");

                HttpBrowser h = new HttpBrowser(url);
                JFtp.desktop.add(h, new Integer(Integer.MAX_VALUE - 10));
            }
        }
        else
        {
            HttpTransfer t = new HttpTransfer(url, localPath, listeners, handler);
        }
    }

    public Insets getInsets()
    {
        Insets in = super.getInsets();

        return new Insets(in.top, in.left, in.bottom, in.right);
    }
}
