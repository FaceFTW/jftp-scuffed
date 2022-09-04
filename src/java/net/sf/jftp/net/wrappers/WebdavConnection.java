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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.sf.jftp.net.wrappers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;
import java.util.Date;
import java.util.Vector;

import net.sf.jftp.config.Settings;
import net.sf.jftp.net.BasicConnection;
import net.sf.jftp.net.ConnectionListener;
import net.sf.jftp.net.DataConnection;
import net.sf.jftp.net.FtpConnection;
import net.sf.jftp.system.StringUtils;
import net.sf.jftp.system.logging.Log;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpURL;
import org.apache.webdav.lib.WebdavFile;
import org.apache.webdav.lib.WebdavResource;


public class WebdavConnection implements BasicConnection
{
    public static int webdavBuffer = 32000;
    private String path = "";
    private String pwd = "";
    private Vector listeners = new Vector();
    private String[] files;
    private String[] size = new String[0];
    private int[] perms = null;
    private String baseFile;
    private int fileCount;
    private boolean shortProgress = false;
    private String user;
    private String pass;

    public WebdavConnection(String path, String user, String pass,
                            ConnectionListener l)
    {
        this.user = user;
        this.pass = pass;

        listeners.add(l);
        chdir(path);
    }

    public int removeFileOrDir(String file)
    {
        Log.debug("Feature is not implemented yet");

        if(true)
        {
            return -1;
        }

        try
        {
            if((file == null) || file.equals(""))
            {
                return -1;
            }

            String tmp = file;

            if(StringUtils.isRelative(file))
            {
                tmp = getPWD() + file;
            }

            WebdavFile f = new WebdavFile(getURL(tmp));

            if(!f.getAbsolutePath().equals(f.getCanonicalPath()))
            {
                //Log.debug("WARNING: Symlink removed");//Skipping symlink, remove failed.");
                //Log.debug("This is necessary to prevent possible data loss when removing those symlinks.");
                //return -1;
                if(!f.delete())
                {
                    return -1;
                }
            }

            if(f.exists() && f.isDirectory())
            {
                cleanLocalDir(tmp);
            }

            //System.out.println(tmp);
            if(!f.delete())
            {
                Log.debug("Removal failed.");

                return -1;
            }
        }
        catch(Exception ex)
        {
            Log.debug("Error: " + ex.toString());
            ex.printStackTrace();
        }

        return -1;
    }

    private void cleanLocalDir(String dir)
    {
        try
        {
            dir = dir.replace('\\', '/');

            if(!dir.endsWith("/"))
            {
                dir = dir + "/";
            }

            //String remoteDir = StringUtils.removeStart(dir,path);
            //System.out.println(">>> " + dir);
            WebdavFile f2 = new WebdavFile(getURL(dir));
            String[] tmp = f2.list();

            if(tmp == null)
            {
                return;
            }

            for(int i = 0; i < tmp.length; i++)
            {
                WebdavFile f3 = new WebdavFile(getURL(dir + tmp[i]));

                if(!f3.getAbsolutePath().equals(f3.getCanonicalPath()))
                {
                    //Log.debug("WARNING: Symlink remove");//Skipping symlink, remove may fail.");
                    f3.delete();

                    //Log.debug("This is necessary to prevent possible data loss when removing those symlinks.");
                    //continue;
                }

                if(f3.isDirectory())
                {
                    //System.out.println(dir);
                    cleanLocalDir(dir + tmp[i]);
                    f3.delete();
                }
                else
                {
                    //System.out.println(dir+tmp[i]);
                    f3.delete();
                }
            }
        }
        catch(Exception ex)
        {
            Log.debug("Error: " + ex.toString());
            ex.printStackTrace();
        }
    }

    public void sendRawCommand(String cmd)
    {
    }

    public void disconnect()
    {
    }

    public boolean isConnected()
    {
        return true;
    }

    public String getPWD()
    {
        return pwd;
    }

    public boolean cdup()
    {
        return chdir(pwd.substring(0, pwd.lastIndexOf("/") + 1));
    }

    public boolean mkdir(String dirName)
    {
        Log.debug("Feature is not implemented yet");

        if(true)
        {
            return false;
        }

        try
        {
            if(StringUtils.isRelative(dirName))
            {
                dirName = getPWD() + dirName;
            }

            WebdavFile f = new WebdavFile(getURL(dirName));

            boolean x = f.mkdir();
            fireDirectoryUpdate();

            return x;
        }
        catch(Exception ex)
        {
            Log.debug("Error: " + ex.toString());
            ex.printStackTrace();

            return false;
        }
    }

    public void list() throws IOException
    {
    }

    public boolean chdir(String p)
    {
        try
        {
            String p2 = processPath(p);

            if(p2 == null)
            {
                return false;
            }

            //WebdavFile f = new WebdavFile(getURL(p2));
            WebdavResource f = getResource(p2);

            if(!f.exists() || !f.isCollection()) //!f.isDirectory() || !f.canRead())
            {
                Log.debug("Access denied.");

                return false;
            }

            pwd = p2;

            Log.out("PWD: " + pwd);

            fireDirectoryUpdate();

            return true;
        }
        catch(Exception ex)
        {
            Log.debug("Error: " + ex.toString());
            ex.printStackTrace();

            return false;
        }
    }

    public boolean chdirNoRefresh(String p)
    {
        String p2 = processPath(p);

        if(p2 == null)
        {
            return false;
        }

        //System.out.println(p2);
        pwd = p2;

        return true;
    }

    public String getLocalPath()
    {
        //System.out.println("local: " + path);
        return path;
    }

    public String processPath(String p)
    {
        try
        {
            if(!p.startsWith("http://"))
            {
                p = pwd + p;
            }

            if(!p.endsWith("/"))
            {
                p = p + "/";
            }

            while(p.endsWith("/../"))
            {
                p = p.substring(0, p.lastIndexOf("/../") - 1);
                p = p.substring(0, p.lastIndexOf("/"));
            }

            while(p.endsWith("/./"))
            {
                p = p.substring(0, p.lastIndexOf("/./") - 1);
                p = p.substring(0, p.lastIndexOf("/"));
            }

            //System.out.println(", processPath 2: "+p);
            //WebdavFile f = new WebdavFile(getURL(p));
            Log.out("\n\n\nprocessPath URL: " + p);

            WebdavResource f = getResource(p);
            String p2 = p;

            if(f.exists() && f.isCollection())
            {
                try
                {
                    //p2 = f.toURL(); //CanonicalPath();
                    if(!p2.endsWith("/"))
                    {
                        p2 = p2 + "/";
                    }

                    //Log.out("processPath parsed URL: " + p);
                    //Log.out("processPath URL2: " + f.getPath());
                    return p2;
                }
                catch(Exception ex)
                {
                    Log.debug("Error: can not get pathname (processPath)!");

                    return null;
                }
            }
            else
            {
                Log.debug("(processpPath) No such path: \"" + p + "\"");

                return null;
            }
        }
        catch(Exception ex)
        {
            Log.debug("Error: " + ex.toString());
            ex.printStackTrace();

            return null;
        }
    }

    public boolean setLocalPath(String p)
    {
        try
        {
            p = p.replace('\\', '/');

            //System.out.print("local 1:" + p);
            if(StringUtils.isRelative(p))
            {
                p = path + p;
            }

            p = p.replace('\\', '/');

            //System.out.println(", local 2:" + p);
            File f = new File(p);

            if(f.exists())
            {
                try
                {
                    path = f.getCanonicalPath();
                    path = path.replace('\\', '/');

                    if(!path.endsWith("/"))
                    {
                        path = path + "/";
                    }

                    //System.out.println("localPath: "+path);
                }
                catch(Exception ex)
                {
                    Log.debug("Error: can not get pathname (local)!");

                    return false;
                }
            }
            else
            {
                Log.debug("(local) No such path: \"" + p + "\"");

                return false;
            }

            return true;
        }
        catch(Exception ex)
        {
            Log.debug("Error: " + ex.toString());
            ex.printStackTrace();

            return false;
        }
    }

    public String[] sortLs()
    {
        try
        {
            Log.out("sortLs PWD: " + pwd);

            //WebdavFile fl = new WebdavFile(new URL(pwd), user, pass);
            WebdavResource fp = getResource(pwd);

            //new WebdavResource(getURL(pwd));
            //files = f.list();
            //File[] f = fl.listFiles();
            WebdavResource[] f = fp.listWebdavResources();

            //if(files == null) return new String[0];
            files = new String[f.length];
            size = new String[f.length];
            perms = new int[f.length];

            int accessible = 0;

            for(int i = 0; i < f.length; i++)
            {
                files[i] = f[i].getName();

                /*
                while(files[i].indexOf("%20") >= 0)
                {
                        int x = files[i].indexOf("%20");
                        String tmp = files[i].substring(0,x)+" "+
                                        files[i].substring(files[i].indexOf("%20")+3);
                        files[i] = tmp;
                }
                */
                /*
                try
                {
                        f[i].exists();
                }
                catch(Exception ex)
                {
                        Log.debug("ERROR: parsing problem with:" + files[i]);
                        size[i] = "-1";
                        perms[i] = FtpConnection.DENIED;
                        continue;
                }
                */
                Log.out("sortLs files[" + i + "]: " + files[i]);

                //size[i] = "" + (int) f[i].length();
                size[i] = "" + (int) f[i].getGetContentLength();
                perms[i] = FtpConnection.R;

                if(f[i].isCollection() && !files[i].endsWith("/"))
                {
                    files[i] = files[i] + "/";
                }

                /*
                if(f[i].canWrite()) accessible = FtpConnection.W;
                else if(f[i].canRead()) accessible = FtpConnection.R;
                else accessible = FtpConnection.DENIED;
                perms[i] = accessible;

                Log.out("sortLs files["+i+"]: " + files[i]);
                WebdavFile f2 = new WebdavFile(new URL(files[i]), user, pass);

                if(!f2.exists()) continue;
                if(f2.isDirectory() && !files[i].endsWith("/")) files[i] = files[i] + "/";

                size[i] = "" + new WebdavFile(new URL(files[i]), user, pass).length();
                */
            }

            return files;
        }
        catch(Exception ex)
        {
            Log.debug("Error: " + ex.toString());
            ex.printStackTrace();

            return new String[0];
        }
    }

    public String[] sortSize()
    {
        return size;
    }

    public int[] getPermissions()
    {
        return perms;
    }

    public int handleDownload(String file)
    {
        transfer(file);

        return 0;
    }

    public int handleUpload(String file)
    {
        transfer(file, true);

        return 0;
    }

    public int download(String file)
    {
        transfer(file);

        return 0;
    }

    public int upload(String file)
    {
        transfer(file, true);

        return 0;
    }

    private void transferDir(String dir, String out)
    {
        try
        {
            fileCount = 0;
            shortProgress = true;
            baseFile = StringUtils.getDir(dir);

            WebdavFile f2 = new WebdavFile(getURL(dir));
            String[] tmp = f2.list();

            if(tmp == null)
            {
                return;
            }

            WebdavFile fx = new WebdavFile(getURL(out));

            if(!fx.mkdir())
            {
                Log.debug("Can not create directory: " + out +
                          " - already exist or permission denied?");
            }

            for(int i = 0; i < tmp.length; i++)
            {
                tmp[i] = tmp[i].replace('\\', '/');

                //System.out.println("1: " + dir+tmp[i] + ", " + out +tmp[i]);
                WebdavFile f3 = new WebdavFile(getURL(dir + tmp[i]));

                if(f3.isDirectory())
                {
                    if(!tmp[i].endsWith("/"))
                    {
                        tmp[i] = tmp[i] + "/";
                    }

                    transferDir(dir + tmp[i], out + tmp[i]);
                }
                else
                {
                    fireProgressUpdate(baseFile,
                                       DataConnection.GETDIR + ":" + fileCount,
                                       -1);
                    work(dir + tmp[i], out + tmp[i]);
                }
            }

            fireProgressUpdate(baseFile,
                               DataConnection.DFINISHED + ":" + fileCount, -1);
            shortProgress = false;
        }
        catch(Exception ex)
        {
            Log.debug("Error: " + ex.toString());
            ex.printStackTrace();
        }
    }

    private HttpURL getURL(String u)
    {
        try
        {
            HttpURL url = new HttpURL(u);

            //System.out.println(user + ":"+ pass);
            url.setUserinfo(user, pass);

            return url;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            Log.debug("ERROR: " + ex);

            return null;
        }
    }

    private WebdavResource getResource(String res)
                                throws HttpException, IOException
    {
        return new WebdavResource(getURL(res));
    }

    private void transfer(String file)
    {
        transfer(file, false);
    }

    private void transfer(String file, boolean up)
    {
        String out = StringUtils.getDir(file);

        if(StringUtils.isRelative(file))
        {
            file = getPWD() + file;
        }

        file = file.replace('\\', '/');
        out = out.replace('\\', '/');

        String outfile = StringUtils.getFile(file);

        if(file.endsWith("/"))
        {
            if(up)
            {
                Log.debug("Directory upload not implemented yet.");

                return;
            }

            transferDir(file, getLocalPath() + out);

            return;
        }
        else
        {
            if(up)
            {
                work(getLocalPath() + outfile, file);
            }
            else
            {
                work(file, getLocalPath() + outfile);
            }
        }
    }

    private void work(String file, String outfile)
    {
        Log.out("transfer started\nfile: " + file + "\noutfile: " + outfile);

        BufferedInputStream in = null;
        BufferedOutputStream out = null;

        try
        {
            if(outfile.startsWith("http://"))
            {
                //out = new BufferedOutputStream(new FileOutputStream(new WebdavResource(new HttpURL(file)).getMethodData());
                //new WebdavFile(new URL(outfile), user, pass)));
                //in = new BufferedInputStream(new FileInputStream(file));
                String resPath = outfile.substring(0,
                                                   outfile.lastIndexOf("/") +
                                                   1);
                String name = outfile.substring(outfile.lastIndexOf("/") + 1);

                Log.debug("Uploading " + file + " to " + resPath + " as " +
                          name);

                //HttpURL url = getURL(resPath);
                WebdavResource res = getResource(resPath);

                //new WebdavResource(url);

                /*
                if(res.checkinMethod()) Log.debug("Checkin OK");
                else Log.debug("Checkin FAILED");

                Enumeration e = res.getAllowedMethods();
                while(e != null && e.hasMoreElements())
                {
                        Log.debug("Method: " + e.nextElement().toString());
                }
                */
                if(res.putMethod(new File(file)))
                {
                    fireProgressUpdate(file, DataConnection.FINISHED, -1);
                }
                else
                {
                    Log.debug("Upload failed.");
                    fireProgressUpdate(file, DataConnection.FAILED, -1);
                }

                return;
            }

            Log.debug("Downloading " + file + " to " + outfile);

            out = new BufferedOutputStream(new FileOutputStream(outfile));
            in = new BufferedInputStream(getResource(file).getMethodData());

            //new WebdavResource(getURL(file)).getMethodData());
            byte[] buf = new byte[webdavBuffer];
            int len = 0;
            int reallen = 0;

            //System.out.println(file+":"+getLocalPath()+outfile);
            while(true)
            {
                len = in.read(buf);

                //System.out.print(".");
                if(len == StreamTokenizer.TT_EOF)
                {
                    break;
                }

                out.write(buf, 0, len);

                reallen += len;
                fireProgressUpdate(StringUtils.getFile(file),
                                   DataConnection.GET, reallen);
            }

            fireProgressUpdate(file, DataConnection.FINISHED, -1);
        }
        catch(IOException ex)
        {
            Log.debug("Error with file IO (" + ex + ")!");
            ex.printStackTrace();
            fireProgressUpdate(file, DataConnection.FAILED, -1);
        }
        finally
        {
            try
            {
                out.flush();
                out.close();
                in.close();
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }

    public int upload(String file, InputStream in)
    {
        /*
           if(StringUtils.isRelative(file)) file = getPWD() + file;
           file = file.replace('\\','/');

           try
           {
                   work(new BufferedInputStream(in), file, file);
                   return 0;
           }
           catch(Exception ex)
           {
                   Log.debug("Error: " + ex.toString());
                   ex.printStackTrace();
                   return -1;
           }
           */
        Log.debug("Upload using InputStream is not implemented yet!");

        return -1;
    }

    /*
    private void work(BufferedInputStream in, String outfile, String file)
    {
     Log.out("transfer started, input from stream\noutfile: " + outfile);

     try
     {
             BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(outfile));
        byte buf[] = new byte[webdavBuffer];
        int len = 0;
        int reallen = 0;

        while(true)
        {
                len = in.read(buf);
                System.out.print(".");
                if(len == StreamTokenizer.TT_EOF) break;
                out.write(buf,0,len);

                reallen += len;
                fireProgressUpdate(StringUtils.getFile(file),DataConnection.GET, reallen);
        }

        out.flush();
        out.close();
        in.close();
        fireProgressUpdate(file,DataConnection.FINISHED, -1);
     }
     catch(IOException ex)
     {
             Log.debug("Error with file IO ("+ex+")!");
        ex.printStackTrace();
        fireProgressUpdate(file,DataConnection.FAILED, -1);
     }
    }
    */
    public InputStream getDownloadInputStream(String file)
    {
        if(StringUtils.isRelative(file))
        {
            file = getPWD() + file;
        }

        file = file.replace('\\', '/');

        try
        {
            return getResource(file).getMethodData();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            Log.debug(ex.toString() +
                      " @WebdavConnection::getDownloadInputStream");

            return null;
        }
    }

    public void addConnectionListener(ConnectionListener l)
    {
        listeners.add(l);
    }

    public void setConnectionListeners(Vector l)
    {
        listeners = l;
    }

    /** remote directory has changed */
    public void fireDirectoryUpdate()
    {
        if(listeners == null)
        {
            return;
        }
        else
        {
            for(int i = 0; i < listeners.size(); i++)
            {
                ((ConnectionListener) listeners.elementAt(i)).updateRemoteDirectory(this);
            }
        }
    }

    public boolean login(String user, String pass)
    {
        return true;
    }

    public void fireProgressUpdate(String file, String type, int bytes)
    {
        if(listeners == null)
        {
            return;
        }
        else
        {
            for(int i = 0; i < listeners.size(); i++)
            {
                ConnectionListener listener = (ConnectionListener) listeners.elementAt(i);

                if(shortProgress && Settings.shortProgress)
                {
                    if(type.startsWith(DataConnection.DFINISHED))
                    {
                        listener.updateProgress(baseFile,
                                                DataConnection.DFINISHED + ":" +
                                                fileCount, bytes);
                    }

                    listener.updateProgress(baseFile,
                                            DataConnection.GETDIR + ":" +
                                            fileCount, bytes);
                }
                else
                {
                    listener.updateProgress(file, type, bytes);
                }
            }
        }
    }

    public Date[] sortDates()
    {
        return null;
    }

    public boolean rename(String from, String to)
    {
        Log.debug("Not implemented!");

        return false;
    }
}
