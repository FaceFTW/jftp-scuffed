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
package net.sf.jftp.gui.base.dir;

import net.sf.jftp.*;
import net.sf.jftp.config.Settings;
import net.sf.jftp.gui.framework.*;
import net.sf.jftp.net.*;
import net.sf.jftp.system.LocalIO;
import net.sf.jftp.util.*;

import java.awt.event.*;

import java.io.*;

import java.net.*;

import java.text.*;

import java.util.*;


public class DirLister implements ActionListener
{
    private int length;
    private String[] files;
    private String[] sizes;
    private int[] perms;
    private boolean isDirectory = true;
    public boolean finished = false;
    private BasicConnection con;
    private String sortMode = null;
    private Date[] dates = null;

    public DirLister(BasicConnection con) //String type)
    {
        this.con = con;
        init();
    }

    public DirLister(BasicConnection con, String sortMode) //String type)
    {
        this.con = con;
        this.sortMode = sortMode;
        init();
    }
    
    public DirLister(BasicConnection con, String sortMode, boolean hide) //String type)
    {
        this.con = con;
        this.sortMode = sortMode;
        init();
        
        int cnt = files.length;
        
        if(hide) {
        	for(int i=0; i<files.length; i++) {
        		if(files[i].startsWith(".") && !files[i].startsWith("..")) {
        			files[i] = null;
        			cnt--;
        		}
        	}
        	
        	String newFiles[] = new String[cnt];
        	String newSizes[] = new String[cnt];
        	int newPerms[] = new int[cnt];
        	
        	int idx = 0;
        	for(int i=0; i<files.length; i++) {
        		if(files[i] == null) {
        			continue;
        		}
        		else {
        			newFiles[idx] = files[i];
        			newSizes[idx] = sizes[i];
        			newPerms[idx] = perms[i];
        			idx++;
        		}
        	}
        	
        	files = newFiles;
        	sizes = newSizes;
        	perms = newPerms;
        	length = files.length;
        }
    }

    public void init()
    {
        try
        {
            String outfile = Settings.ls_out;
            
            //BasicConnection con = JFtp.getControlConnection();
            con.list();
            files = con.sortLs();
            sizes = con.sortSize();
            

            //Log.debug("sizes: " + sizes.length);
            /*
            for(int i=0; i<files.length; i++)
            {
                    Log.out("parser: "+files[i]+":"+sizes[i]);
            }
            */
            length = files.length;
            perms = con.getPermissions();
            isDirectory = true; 
            /*
            for(int i=0; i<files.length; i++)
            {
                    if((con instanceof FtpConnection) && sortMode.equals("Date"))
                {
                        Vector v = ((FtpConnection)con).dateVector;
                        if(v.size() > 0) Log.out(files[i]+":"+v.elementAt(i));
                }
            }*/
            if(sortMode != null)
            {
                if(!sortMode.equals("Date"))
                {
                    //Log.out("0"+sortMode);
                    sortFirst();
                }

                sort(sortMode);
            }
            else if(sortMode == null)
            {
                //Log.out("1"+sortMode);
                sortFirst();
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            isDirectory = false;
        }

        finished = true;
    }

    private void sort(String type)
    {
        Vector fv = new Vector();
        Vector sv = new Vector();
        Vector pv = new Vector();

        if(type.equals("Reverse"))
        {
            for(int i = 0; i < length; i++)
            {
                fv.add(files[i]);
                sv.add(sizes[i]);

                if(perms != null)
                {
                    pv.add(new Integer(perms[i]));
                }
            }

            Collections.sort(fv, Collections.reverseOrder());

            //	Collections.sort(sv, Collections.reverseOrder());
            //	if(perms != null) Collections.sort(pv, Collections.reverseOrder());
            Object[] filesTmp = fv.toArray();
            Object[] sizesTmp = sv.toArray();
            Object[] permsTmp = null;

            if(perms != null)
            {
                permsTmp = pv.toArray();
            }

            for(int i = 0; i < length; i++)
            {
                files[i] = (String) filesTmp[i];
                sizes[i] = (String) sizesTmp[length - i - 1];

                if(perms != null)
                {
                    perms[i] = ((Integer) permsTmp[length - i - 1]).intValue();
                }
            }
        }
        else if(type.startsWith("Size"))
        {
            int cnt = 0;
            Hashtable processed = new Hashtable();
            boolean reverse = false;

            if(type.endsWith("/Re"))
            {
                reverse = true;
            }

            while(cnt < length)
            {
                int idx = 0;
                double current = 0;

                if(reverse)
                {
                    current = Double.MAX_VALUE;
                }

                for(int i = 0; i < length; i++)
                {
                    if(processed.containsKey("" + i))
                    {
                        continue;
                    }

                    int si = Integer.parseInt(sizes[i]);

                    if(!reverse && (si >= current))
                    {
                        //Log.out(sizes[i]+"/"+i);
                        idx = i;
                        current = si;
                    }
                    else if(reverse && (si <= current))
                    {
                        idx = i;
                        current = si;
                    }
                }

                processed.put("" + idx, "" + sizes[idx]);
                fv.add(files[idx]);
                sv.add(sizes[idx]);

                //System.out.println(files[idx]+":"+sizes[idx]+":"+idx);
                if(perms != null)
                {
                    pv.add(new Integer(perms[idx]));
                }

                cnt++;
            }

            for(int i = 0; i < length; i++)
            {
                files[i] = (String) fv.elementAt(i);
                sizes[i] = (String) sv.elementAt(i);

                if(perms != null)
                {
                    perms[i] = ((Integer) pv.elementAt(i)).intValue();
                }
            }
        }
        else if(type.equals("Date"))
        {
            String style = "ftp";

            //TODO: may be slow
            if(!(con instanceof FtpConnection) ||
                   (((FtpConnection) con).dateVector == null) ||
                   (((FtpConnection) con).dateVector.size() < 1))
            {
                if(!(con instanceof FilesystemConnection) ||
                       (((FilesystemConnection) con).dateVector == null) ||
                       (((FilesystemConnection) con).dateVector.size() < 1))
                {
                }
                else
                {
                    style = "file";
                }
            }

            Object[] date = null;

            if(style.equals("ftp"))
            {
                date = ((FtpConnection) con).dateVector.toArray();

                /*
                        for(int v=0; v<date.length; v++)
                        {
                                System.out.println(files[v]+":"+((Date)date[v]).toString());
                        }
                */
            }
            else
            {
                date = ((FilesystemConnection) con).dateVector.toArray();
            }

            for(int j = 0; j < date.length; j++)
            {
                for(int i = 0; i < date.length; i++)
                {
                    Date x = (Date) date[i];

                    if(i == (date.length - 1))
                    {
                        break;
                    }

                    if(comp(x, (Date) date[i + 1]))
                    {
                        //Log.debug("switch");
                        Date swp = (Date) date[i + 1];
                        date[i + 1] = x;
                        date[i] = swp;

                        String s1 = files[i + 1];
                        String s2 = files[i];
                        files[i] = s1;
                        files[i + 1] = s2;

                        //if(files[i].startsWith(".cross")) Log.out(files[i]+" / "+ ((Date)date[i]).toString());
                        s1 = sizes[i + 1];
                        s2 = sizes[i];
                        sizes[i] = s1;
                        sizes[i + 1] = s2;

                        int s3 = perms[i + 1];
                        int s4 = perms[i];
                        perms[i] = s3;
                        perms[i + 1] = s4;
                    }
                }
            }

            dates = new Date[date.length];

            for(int i = 0; i < dates.length; i++)
            {
                dates[i] = (Date) date[i];
            }

            /*
            try
            {
            for(int k=0; k<date.length; k++)
            {
                    Log.out("+++ " + date[k].toString());
            }
            }
            catch(Exception ex)
            {
                    ex.printStackTrace();
            }
            */
        }
        else if(type.equals("Normal"))
        {
            // already done.
        }
    }

    private boolean comp(Date one, Date two)
    {
        Calendar c = new GregorianCalendar();
        c.clear();
        c.setTime(one);

        Calendar c2 = new GregorianCalendar();
        c2.clear();
        c2.setTime(two);

        if(c.getTime().compareTo(c2.getTime()) > 0) // c.compareTo(c2) > 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public void sortFirst()
    {
        String[] tmpx = new String[length];

        for(int x = 0; x < length; x++)
        {
            if(perms != null)
            {
                tmpx[x] = files[x] + "@@@" + sizes[x] + "@@@" + perms[x];

                //Log.debug(tmpx[x]);
            }
            else
            {
                tmpx[x] = files[x] + "@@@" + sizes[x];
            }
        }

        LocalIO.sortStrings(tmpx);

        for(int y = 0; y < length; y++)
        {
            files[y] = tmpx[y].substring(0, tmpx[y].indexOf("@@@"));

            String tmp = tmpx[y].substring(tmpx[y].indexOf("@@@") + 3);
            sizes[y] = tmp.substring(0, tmp.lastIndexOf("@@@"));

            if(perms != null)
            {
                perms[y] = Integer.parseInt(tmpx[y].substring(tmpx[y].lastIndexOf("@@@") +
                                                              3));
            }
        }
    }

    public void actionPerformed(ActionEvent e)
    {
    }

    public boolean isOk()
    {
        return isDirectory;
    }

    public int getLength()
    {
        return length;
    }

    public String[] list()
    {
        return files;
    }

    public String[] sList()
    {
        return sizes;
    }

    public int[] getPermissions()
    {
        return perms;
    }

    public Date[] getDates()
    {
        return dates;
    }
}
