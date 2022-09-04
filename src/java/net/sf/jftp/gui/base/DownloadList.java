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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import net.sf.jftp.JFtp;
import net.sf.jftp.config.Settings;
import net.sf.jftp.gui.base.dir.DirEntry;
import net.sf.jftp.gui.base.dir.DirPanel;
import net.sf.jftp.gui.framework.HImage;
import net.sf.jftp.gui.framework.HImageButton;
import net.sf.jftp.gui.framework.HPanel;
import net.sf.jftp.gui.framework.ProgressBarList;
import net.sf.jftp.gui.framework.ProgressbarItem;
import net.sf.jftp.net.ConnectionHandler;
import net.sf.jftp.net.DataConnection;
import net.sf.jftp.net.Transfer;
import net.sf.jftp.net.wrappers.HttpTransfer;
import net.sf.jftp.system.LocalIO;
import net.sf.jftp.system.UpdateDaemon;
import net.sf.jftp.system.logging.Log;


public class DownloadList extends HPanel implements ActionListener
{
    public Hashtable sizeCache = new Hashtable();
    private ProgressBarList list = new ProgressBarList();
    private Hashtable downloads = new Hashtable();
    private long oldtime = 0;
    private HImageButton resume = new HImageButton(Settings.resumeImage,
                                                   "resume",
                                                   "Resume selected transfer...",
                                                   this);
    private HImageButton pause = new HImageButton(Settings.pauseImage, "pause",
                                                  "Pause selected transfer...",
                                                  this);
    private HImageButton cancel = new HImageButton(Settings.deleteImage,
                                                   "delete",
                                                   "Cancel selected transfer...",
                                                   this);
    private HImageButton clear = new HImageButton(Settings.clearImage, "clear",
                                                  "Remove old/stalled items from output...",
                                                  this);   
    private JScrollPane scroll;

    public DownloadList()
    {
        setLayout(new BorderLayout());
        
		resume.setRolloverIcon(new ImageIcon(HImage.getImage(this, Settings.resumeImage2)));
		resume.setRolloverEnabled(true);
		pause.setRolloverIcon(new ImageIcon(HImage.getImage(this, Settings.pauseImage2)));
		pause.setRolloverEnabled(true);
		clear.setRolloverIcon(new ImageIcon(HImage.getImage(this, Settings.clearImage2)));
		clear.setRolloverEnabled(true);
		cancel.setRolloverIcon(new ImageIcon(HImage.getImage(this, Settings.deleteImage2)));
		cancel.setRolloverEnabled(true);

        HPanel cmdP = new HPanel();

        cmdP.add(clear);

        cmdP.add(new JLabel("   "));

        cmdP.add(resume);
        cmdP.add(pause);

        cmdP.add(new JLabel("   "));

        cmdP.add(cancel);

        clear.setSize(24, 24);
        cancel.setSize(24, 24);
        resume.setSize(24, 24);
        pause.setSize(24, 24);

        clear.setToolTipText("Remove old/stalled items from output...");

        resume.setToolTipText("Resume selected transfer...");
        pause.setToolTipText("Pause selected transfer...");
        cancel.setToolTipText("Cancel selected transfer...");

        scroll = new JScrollPane(list);
        add("South", cmdP);
        add("Center", scroll);
    }

    public void fresh()
    {
        downloads = new Hashtable();
        updateArea();
    }

    public void actionPerformed(ActionEvent e)
    {
        if(e.getActionCommand().equals("delete"))
        {
            deleteCon();
        }
        else if(e.getActionCommand().equals("clear") ||
                    (e.getSource() == AppMenuBar.clearItems))
        {
            fresh();
        }
        else if(e.getActionCommand().equals("pause"))
        {
            pauseCon();
        }
        else if(e.getActionCommand().equals("resume"))
        {
            resumeCon();
        }
    }

    private void deleteCon()
    {
        try
        {
            String cmd = getActiveItem();

            if(cmd == null)
            {
                return;
            }

            if((cmd.indexOf(Transfer.QUEUED) >= 0) ||
                   (cmd.indexOf(Transfer.PAUSED) >= 0))
            {
                cmd = getFile(cmd);

                try
                {
                    Transfer d = (Transfer) JFtp.getConnectionHandler()
                                                .getConnections().get(cmd);

                    if(d == null)
                    {
                        return;
                    }

                    d.work = false;
                    d.pause = false;
                    
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }
            }
            else
            {
                cmd = getFile(cmd);

                ConnectionHandler h = JFtp.getConnectionHandler();

                Object o = h.getConnections().get(cmd);

                Log.out("aborting  transfer: " + cmd);
                Log.out("connection handler present: " + h + ", pool size: " +
                        h.getConnections().size());

                if(o instanceof HttpTransfer)
                {
                    Transfer d = (Transfer) o;
                    d.work = false;
                    updateList(cmd, DataConnection.FAILED, -1, -1);

                    return;
                }
                else
                {
                    Transfer d = (Transfer) o;

                    DataConnection con = d.getDataConnection();
                    con.getCon().work = false;

                    try
                    {
                        con.sock.close();
                    	
                        //con.getCon().abort();
                        //if(Settings.getEnableMultiThreading()) con.getCon().disconnect();
                    }
                    catch(Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }

                LocalIO.pause(500);
                updateList(getRawFile(getActiveItem()), DataConnection.FAILED,
                           -1, -1);
            }
        }
        catch(Exception ex)
        {
            Log.debug("Action is not supported for this connection.");
            ex.printStackTrace();
        }
    }

    // fake pause, it disconnects instead
    private void pauseCon()
    {
        try
        {
            String cmd = getActiveItem();

            if(cmd == null)
            {
                return;
            }

            if((cmd.indexOf(DataConnection.GET) >= 0) ||
                   (cmd.indexOf(DataConnection.PUT) >= 0))
            {
                cmd = getFile(cmd);

                Object o = JFtp.getConnectionHandler().getConnections().get(cmd);

                if(o == null)
                {
                    return;
                }

                Transfer d = (Transfer) o;
                d.pause = true;

                DataConnection con = d.getDataConnection();

                try
                {
                    con.sock.close();
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }

                d.prepare();
            }
        }
        catch(Exception ex)
        {
            Log.debug("Action is not supported for this connection.");
        }
    }

    private void resumeCon()
    {
        try
        {
            String cmd = getActiveItem();

            if(cmd == null)
            {
                return;
            }

            if((cmd.indexOf(Transfer.PAUSED) >= 0) ||
                   (cmd.indexOf(Transfer.QUEUED) >= 0))
            {
                cmd = getFile(cmd);

                try
                {
                    Object o = JFtp.getConnectionHandler().getConnections().get(cmd);

                    if(o == null)
                    {
                        return;
                    }

                    Transfer d = (Transfer) o;
                    d.work = true;
                    d.pause = false;
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        catch(Exception ex)
        {
            Log.debug("Action is not supported for this connection.");
        }
    }

    private String getActiveItem()
    {
        String tmp = (String) ((ProgressbarItem)(list.getSelectedValue())).getDirEntry().toString();

        if(tmp == null)
        {
            return "";
        }
        else
        {
            return tmp;
        }
    }

    public synchronized void updateList(String file, String type, long bytes,
                                        long size)
    {
        String message = type + ": <" + file + "> ";

        if(!safeUpdate())
        {
            if(!type.startsWith(DataConnection.DFINISHED) &&
                   !type.startsWith(DataConnection.FINISHED) &&
                   !type.startsWith(DataConnection.FAILED) &&
                   !type.startsWith(Transfer.PAUSED) &&
                   !type.startsWith(Transfer.REMOVED))
            {
                return;
            }
        }
        
        // directory
        int count = 0;

        if(type.startsWith(DataConnection.GETDIR) ||
               type.startsWith(DataConnection.PUTDIR) ||
               type.startsWith(DataConnection.PUTDIR) ||
               type.startsWith(DataConnection.DFINISHED))
        {
            //System.out.println(type);
            String tmp = type.substring(type.indexOf(":") + 1);
            type = type.substring(0, type.indexOf(":"));
            count = Integer.parseInt(tmp);
            message = type + ": <" + file + "> ";
        }

        // fetch size from remote panel
        if(type.equals(DataConnection.GET)) {
        	DirEntry[] e = ((DirPanel)JFtp.remoteDir).dirEntry;
        	
        	for(int i=0; i<e.length; i++) {
        		if(e[i].file.equals(file)) {
        			size = e[i].getRawSize();
        		}
        	}
        }
        
        // ---------------
        //System.out.print(size+":");
        String tmp;
        long s = (long) (size / 1024);

        if(s > 0)
        {
            tmp = Long.toString(s);
        }
        else
        {        	
            tmp = "?";
        }

        //System.out.println(message);
        if(type.equals(DataConnection.GET) || type.equals(DataConnection.PUT))
        {
            message = message + (bytes / 1024) + " / " + tmp + " kb";
            list.setTransferred(file, (bytes/1024), message, s);
        }
        else if(type.equals(DataConnection.GETDIR) ||
                    type.equals(DataConnection.PUTDIR))
        {
            message = message + (bytes / 1024) + " kb of file #" + count;
            list.setTransferred(file, (bytes/1024), message, -1);
        }
        else if(type.startsWith(DataConnection.DFINISHED))
        {
            message = message + " " + count + " files.";
        }

        if(type.equals(DataConnection.FINISHED) ||
               type.startsWith(DataConnection.DFINISHED))
        {
            try
            {
                JFtp.getConnectionHandler().removeConnection(file);
            }
            catch(Exception ex)
            {
                // SMB does not need this
            }

            UpdateDaemon.updateCall();
        }
        else if(type.equals(DataConnection.FAILED))
        {
            UpdateDaemon.updateCall();
        }

//        downloads.put(file, message);
        
		DirEntry d = null;
		if(downloads.containsKey(message)) {
			d = (DirEntry) downloads.get(message);
		}
		else {
			d = new DirEntry(message, null);
			d.setNoRender();
			if(getFile(tmp).endsWith("/"))
			{
				d.setDirectory();
			}
			d.setFileSize(size);
		}

		d.setTransferred(bytes);
			
		downloads.put(file, d);

        updateArea();
    }

    private synchronized DirEntry[] toArray()
    {
        DirEntry[] f = new DirEntry[downloads.size()];
        int i = 0;

        Enumeration k = downloads.elements();

        while(k.hasMoreElements())
        {
        	Object o = k.nextElement();
        	if(o instanceof DirEntry) {
        		f[i] = (DirEntry) o;
        	}
        	else {

        		String tmp = (String) o;
        		DirEntry d = new DirEntry(tmp, null);

        		if(getFile(tmp).endsWith("/"))
        		{
        			d.setDirectory();
        		}

        		d.setNoRender();
            	f[i] = d;
        	}

        	i++;
        }

        return f;
    }

    private synchronized void updateArea()
    {
   	
        int idx = list.getSelectedIndex();

        DirEntry[] f = toArray();

        list.setListData(f);
        
        if((f.length == 1) && (idx < 0))
        {
            list.setSelectedIndex(0);
        }
        else
        {
            list.setSelectedIndex(idx);
        }
        
        revalidate();
        scroll.revalidate();
        repaint();            
    }

    private String getFile(String msg)
    {
    	String f = msg;
    	
    	if(msg.indexOf("<") >= 0 && msg.indexOf(">") >= 0) {
    	        f = msg.substring(msg.indexOf("<") + 1);
    	        f = f.substring(0, f.lastIndexOf(">"));
    	}

        //System.out.println(f);
        return getRealName(f);
    }

    private String getRealName(String file)
    {
        //System.out.println(">>>"+file);
        try
        {
            Enumeration e = JFtp.getConnectionHandler().getConnections().keys();

            while(e.hasMoreElements())
            {
                String tmp = (String) e.nextElement();

                //System.out.println(tmp);
                if(tmp.endsWith(file))
                {
                    return tmp;
                }
            }
        }
        catch(Exception ex)
        {
            // SMB does not need this
        }

        return file;
    }

    private String getRawFile(String msg)
    {
        String f = msg.substring(msg.indexOf("<") + 1);
        f = f.substring(0, f.lastIndexOf(">"));

        //System.out.println(f);
        return f;
    }

    private boolean safeUpdate()
    {
        long time = System.currentTimeMillis();

        if((time - oldtime) < Settings.refreshDelay)
        {
            return false;
        }

        oldtime = time;

        return true;
    }
}
