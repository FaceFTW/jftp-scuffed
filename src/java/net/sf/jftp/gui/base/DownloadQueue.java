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

import net.sf.jftp.*;
import net.sf.jftp.config.*;
import net.sf.jftp.gui.framework.*;
import net.sf.jftp.net.*;
import net.sf.jftp.system.StringUtils;
import net.sf.jftp.system.logging.Log;
import net.sf.jftp.util.*;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.util.*;

import javax.swing.*;
import javax.swing.JComponent.*;
import javax.swing.event.*;


public class DownloadQueue extends HPanel implements ActionListener
{
    private static final String SEP = "--> ";

    // Number of Retry
    int NumRetry = 5;

    //private JTextArea text = new JTextArea();
    private DefaultListModel liststr = new DefaultListModel();
    private JList list = new JList(liststr);
    private ArrayList queue = new ArrayList();
    private queueDownloader thread = new queueDownloader();
    private QueueRecord lastDownload;
    private BasicConnection con;

    // private Vector listeners = new Vector();
    private HImageButton start = new HImageButton(Settings.resumeImage,
                                                  "start",
                                                  "Start queue download...",
                                                  this);
    private HImageButton stop = new HImageButton(Settings.pauseImage, "stop",
                                                 "Stop queue download...", this);
    private HImageButton save = new HImageButton(Settings.saveImage, "save",
                                                 "Save queue list to file...",
                                                 this);
    private HImageButton load = new HImageButton(Settings.cdImage, "load",
                                                 "Load queue list from...", this);
    private HImageButton up = new HImageButton(Settings.downloadImage, "up",
                                               "Change order of queue", this);
    private HImageButton down = new HImageButton(Settings.uploadImage, "down",
                                                 "Change order of queue", this);
    private HImageButton delete = new HImageButton(Settings.deleteImage, "del",
                                                   "Delete item in queue", this);

    //private HImageButton rotate = new HImageButton(Settings.cmdImage,"rotate","Toggle selected transfer...",this);
    // connection established?
    private boolean isThere = false;
    private boolean downloading = false;
    private ConnectionHandler handler = new ConnectionHandler();
    private JLabel statuslabel;

    public DownloadQueue()
    {
        setLayout(new BorderLayout());

        // list.setCellRenderer(new DirCellRenderer());
        HPanel cmdP = new HPanel();

        cmdP.add(start);

        cmdP.add(stop);

        cmdP.add(new JLabel("   "));

        cmdP.add(up);

        cmdP.add(down);

        cmdP.add(delete);

        cmdP.add(new JLabel("   "));

        cmdP.add(save);

        cmdP.add(load);

        start.setSize(24, 24);
        stop.setSize(24, 24);
        up.setSize(24, 24);
        down.setSize(24, 24);
        delete.setSize(24, 24);
        save.setSize(24, 24);
        load.setSize(24, 24);
        
        JScrollPane dP = new JScrollPane(list);

        //add("South",cmdP);
        //add("North",dP);
        add(cmdP, BorderLayout.SOUTH);
        add(dP, BorderLayout.CENTER);

        HPanel status = new HPanel();
        statuslabel = new JLabel("");
        statuslabel.setSize(100, 100);

        status.add(statuslabel);
        add(status, BorderLayout.NORTH);

        //*** MY ADDITIONS
        start.setToolTipText("Start queue download...");
        stop.setToolTipText("Stop queue download...");
        save.setToolTipText("Save queue list to file...");
        load.setToolTipText("Load queue list from...");
        up.setToolTipText("Change order of queue");
        down.setToolTipText("Change order of queue");
        delete.setToolTipText("Delete item in queue");

        //***
    }

    /*
    public void fresh()
    {
    }
    */
    public void addFtp(String file)
    {
        Log.debug("Remote File" + JFtp.remoteDir.getPath() + file +
                  "Local File" + JFtp.localDir.getPath() + file + "HostName" +
                  JFtp.hostinfo.hostname);

        QueueRecord rec = new QueueRecord();
        rec.type = "ftp";
        rec.hostname = JFtp.hostinfo.hostname;
        rec.username = JFtp.hostinfo.username;
        rec.password = JFtp.hostinfo.password;
        rec.port = JFtp.hostinfo.port;
        rec.local = JFtp.localDir.getPath();
        rec.remote = JFtp.remoteDir.getPath();
        rec.file = file;
        queue.add(rec);
        liststr.addElement(rec.hostname + " : " + rec.remote + rec.file);
    }

    public void actionPerformed(ActionEvent e)
    {
        if(e.getActionCommand().equals("start"))
        {
            if(thread != null)
            {
                if(!thread.isAlive())
                {
                    thread = new queueDownloader();
                    thread.start();

                    //listeners.add(thread);
                }
            }
            else
            {
                thread = new queueDownloader();
                thread.start();

                //listeners.add(thread);
            }
        }

        else if(e.getActionCommand().equals("stop"))
        {
            if(thread != null)
            {
                thread.block = true;

                //QueueRecord rec = (QueueRecord)queue.get(0);
                //Transfer d = (Transfer)handler.getConnections().get(rec.file);
                //DataConnection dcon = d.getDataConnection();
                FtpConnection ftpcon = (FtpConnection) con;
                DataConnection dcon = ftpcon.getDataConnection();
                dcon.getCon().work = false;

                try
                {
                    dcon.sock.close();
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }

        else if(e.getActionCommand().equals("down"))
        {
            if(list.getSelectedIndex() != -1)
            {
                int a = list.getSelectedIndex();
                int b = a + 1;

                QueueRecord qa = (QueueRecord) queue.get(b);
                queue.remove(b);
                queue.add(a, qa);

                String sa = (String) liststr.get(b);
                liststr.remove(b);
                liststr.add(a, sa);

                list.setSelectedIndex(b);
            }
        }
        else if(e.getActionCommand().equals("up"))
        {
            if(list.getSelectedIndex() != -1)
            {
                int a = list.getSelectedIndex() - 1;
                int b = a + 1;

                QueueRecord qa = (QueueRecord) queue.get(b);
                queue.remove(b);
                queue.add(a, qa);

                String sa = (String) liststr.get(b);
                liststr.remove(b);
                liststr.add(a, sa);

                list.setSelectedIndex(a);
            }
        }
        else if(e.getActionCommand().equals("del"))
        {
            if(list.getSelectedIndex() != -1)
            {
                queue.remove(list.getSelectedIndex());
                liststr.remove(list.getSelectedIndex());
                list.setSelectedIndex(0);
            }
        }

        else if(e.getActionCommand().equals("save"))
        {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save file");
            chooser.setDialogType(JFileChooser.SAVE_DIALOG);

            int returnVal = chooser.showSaveDialog(new JDialog());

            if(returnVal == JFileChooser.APPROVE_OPTION)
            {
                File f = chooser.getSelectedFile();
                saveList(f);
            }
        }

        else if(e.getActionCommand().equals("load"))
        {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Open file");
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);

            int returnVal = chooser.showOpenDialog(new JDialog());

            if(returnVal == JFileChooser.APPROVE_OPTION)
            {
                File f = chooser.getSelectedFile();
                loadList(f);
            }
        }
    }

    private void saveList(File file)
    {
        try
        {
            if(file.exists())
            {
                file.delete();
            }

            FileOutputStream fos;
            PrintStream f = new PrintStream((fos = new FileOutputStream(file)));

            for(int i = 0; i <= queue.size(); i++)
            {
                QueueRecord rec = (QueueRecord) queue.get(i);
                f.println(rec.type);
                f.println(rec.hostname);
                f.println(rec.username);
                f.println(rec.password);
                f.println(rec.port);
                f.println(rec.file);
                f.println(rec.local);
                f.println(rec.remote);
                f.println(rec.localip);
                f.println(rec.domain);
            }

            fos.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void loadList(File file)
    {
        try
        {
            BufferedReader breader = new BufferedReader(new FileReader(file));
            boolean Eof = false;
            queue.clear();
            liststr.clear();

            while(!Eof)
            {
                QueueRecord rec = new QueueRecord();
                rec.type = breader.readLine();
                rec.hostname = breader.readLine();
                rec.username = breader.readLine();
                rec.password = breader.readLine();
                rec.port = breader.readLine();
                rec.file = breader.readLine();
                rec.local = breader.readLine();
                rec.remote = breader.readLine();
                rec.localip = breader.readLine();
                rec.domain = breader.readLine();

                if(rec.hostname == null)
                {
                    Eof = true;
                }
                else
                {
                    queue.add(rec);
                    liststr.addElement(rec.hostname + " : " + rec.remote +
                                       rec.file);
                }
            }
        }
        catch(Exception ex)
        {
        }
    }

    // ------------ needed by Logger interface  --------------
    // main log method
    public void debug(String msg)
    {
        System.out.println(msg);
    }

    // rarely used
    public void debugRaw(String msg)
    {
        System.out.print(msg);
    }

    // methods below are not used yet.
    public void debug(String msg, Throwable throwable)
    {
    }

    public void warn(String msg)
    {
    }

    public void warn(String msg, Throwable throwable)
    {
    }

    public void error(String msg)
    {
    }

    public void error(String msg, Throwable throwable)
    {
    }

    public void info(String msg)
    {
    }

    public void info(String msg, Throwable throwable)
    {
    }

    public void fatal(String msg)
    {
    }

    public void fatal(String msg, Throwable throwable)
    {
    }

    class QueueRecord
    {
        public String type;

        // parameter used for ftp
        public String hostname;
        public String username;
        public String password;
        public String port;
        public String file;

        // local path
        public String local;

        // remote path
        public String remote;

        // used only for smb
        public String localip;
        public String domain;

        public QueueRecord()
        {
        }
    }

    class queueDownloader extends Thread implements ConnectionListener
    {
        public boolean block = false;
        public boolean connected = false;
        public QueueRecord last;
        private FtpConnection conFtp;

        public void run()
        {
            try
            {
                while((queue.size() >= 1) && !block)
                {
                    QueueRecord rec = (QueueRecord) queue.get(0);
                    last = rec;

                    if(!connected)
                    {
                        con = new FtpConnection(rec.hostname,
                                                Integer.parseInt(rec.port), "/");
                        conFtp = (FtpConnection) con;
                        conFtp.addConnectionListener(this);
                        conFtp.setConnectionHandler(handler);
                        conFtp.login(rec.username, rec.password);
                        connected = true;
                    }

                    conFtp.chdirNoRefresh(rec.remote);

                    conFtp.setLocalPath(rec.local);

                    int i = 0;

                    while(!isThere)
                    {
                        i++;

                        if(i > NumRetry)
                        {
                            return;
                        }

                        try
                        {
                            Thread.sleep(10);
                        }
                        catch(Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }

                    conFtp.download(rec.file);

                    statuslabel.setText("");

                    if(queue.size() >= 1)
                    {
                        rec = (QueueRecord) queue.get(0);

                        if((rec.hostname.compareTo(last.hostname) == 0) &&
                               (rec.port.compareTo(last.port) == 0) &&
                               (rec.username.compareTo(last.username) == 0) &&
                               (rec.password.compareTo(last.password) == 0))
                        {
                            connected = true;
                        }
                        else
                        {
                            conFtp.disconnect();
                            connected = false;
                        }
                    }
                    else
                    {
                        conFtp.disconnect();
                        connected = false;
                    }

                    Thread.sleep(100);
                }
            }
            catch(InterruptedException e)
            {
            }

            if(connected)
            {
                con.disconnect();
            }
        }

        // ------------------ needed by ConnectionListener interface -----------------
        // called if the remote directory has changed
        public void updateRemoteDirectory(BasicConnection con)
        {
        }

        // called if a connection has been established
        public void connectionInitialized(BasicConnection con)
        {
            isThere = true;
        }

        // called every few kb by DataConnection during the trnsfer (interval can be changed in Settings)
        public void updateProgress(String file, String type, long bytes)
        {
            String strtmp;

            if(StringUtils.getFile(file).compareTo("") == 0)
            {
                strtmp = "directory " + file;
            }
            else
            {
                strtmp = "file " + StringUtils.getFile(file);
            }

            statuslabel.setText("Downloading " + strtmp + " - kbyte " +
                                (bytes / 1024));

            String tmp;

            if(type.length() >= 10)
            {
                tmp = type.substring(0, 9);
            }
            else
            {
                tmp = " ";
            }

            if(((type.compareTo(DataConnection.FINISHED) == 0) ||
                   (tmp.compareTo(DataConnection.DFINISHED) == 0)) && !block)
            {
                queue.remove(0);
                liststr.remove(0);
            }
        }

        // called if connection fails
        public void connectionFailed(BasicConnection con, String why)
        {
            System.out.println("connection failed!");
        }

        // up- or download has finished
        public void actionFinished(BasicConnection con)
        {
        }
    }
}
