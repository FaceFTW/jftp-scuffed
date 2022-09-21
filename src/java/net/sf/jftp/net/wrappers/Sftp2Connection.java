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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamTokenizer;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JOptionPane;

import net.sf.jftp.config.Settings;
import net.sf.jftp.net.BasicConnection;
import net.sf.jftp.net.ConnectionListener;
import net.sf.jftp.net.DataConnection;
import net.sf.jftp.net.FtpConnection;
import net.sf.jftp.net.Transfer;
import net.sf.jftp.system.StringUtils;
import net.sf.jftp.system.logging.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;
import com.jcraft.jsch.ChannelSftp.LsEntry;

public class Sftp2Connection implements BasicConnection
{
    public static int smbBuffer = 32000;
    private String path = "";
    private String pwd = "/";
    private Vector listeners = new Vector();
    private String[] files;
    private String[] size = new String[0];
    private int[] perms = null;
    private String user;
    private String pass;
    private final String host;
    private String baseFile;
    private int fileCount;
    private boolean isDirUpload = false;
    private boolean shortProgress = false;
    private int port = 22;
    private boolean connected = false;
    private String keyfile = null;
    private Session session;
    private ChannelSftp channel;

    public Sftp2Connection(String host, String port, String keyfile)
    {
    	this.host = host;
    	this.port = Integer.parseInt(port);
    	this.keyfile = keyfile;
    	
    	Log.out("Using JSch wrapper...");
    } 
   
    private boolean login()
    {
        try
        {
            JSch jsch = new JSch();
            if(keyfile != null) {
            	jsch.addIdentity(keyfile);
            }
            session = jsch.getSession(user, host, this.port);
            UserInfo ui = new MyUserInfo(pass);
            session.setUserInfo(ui);
            session.connect();
            

            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            
            Log.debug("Host: "+host+":"+port);

            pwd = channel.pwd();
            
            connected = true;
            return true;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            Log.debug("Error: " + ex);

            return false;
        }
    }

    public int removeFileOrDir(String file)
    {
        file = toSFTP(file);

        try
        {

            if(!file.endsWith("/"))
            {
                Log.out(">>>>>>>> remove file: " + file);
                channel.rm(file); 
            }
            else
            {
                Log.out(">>>>>>>> remove dir: " + file);
                cleanSftpDir(file);
                channel.rmdir(file);
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            Log.debug("Removal failed (" + ex + ").");
            ex.printStackTrace();

            return -1;
        }

        return 1;
    }

    private void cleanSftpDir(String dir)
                       throws Exception
    {
        Log.out(">>>>>>>> cleanSftpDir: " + dir);

        Vector v = channel.ls(dir);

        String[] tmp = new String[v.size()];
        Enumeration e = v.elements();
        int x = 0;

        while(e.hasMoreElements())
        {
        	LsEntry entry = ((LsEntry)e.nextElement());
            tmp[x] = entry.getFilename();

            //Log.out("sftp delete: " + tmp[x]);
            if(entry.getAttrs().isDir() && !tmp[x].endsWith("/"))
            {
                tmp[x] = tmp[x] + "/";
            }

            x++;
        }

        if(tmp == null)
        {
            return;
        }

        for(int i = 0; i < tmp.length; i++)
        {
            if(tmp[i].equals("./") || tmp[i].equals("../"))
            {
                continue;
            }

            Log.out(">>>>>>>> remove file/dir: " + dir + tmp[i]);

            if(tmp[i].endsWith("/"))
            {
                cleanSftpDir(dir + tmp[i]);
                channel.rmdir(dir + tmp[i]);
            }
            else
            {
            	channel.rm(dir + tmp[i]);
            }
        }
    }

    public void sendRawCommand(String cmd)
    {
    }

    public void disconnect()
    {
        try
        {
            channel.disconnect();
            session.disconnect();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Log.debug("Sftp2Connection.disconnect()" + e);
        }

        connected = false;
    }

    public boolean isConnected()
    {
        return connected;
    }

    public String getPWD()
    {
        //Log.debug("PWD: " + pwd);
        return toSFTPDir(pwd);
    }

    public boolean mkdir(String dirName)
    {
        try
        {
            if(!dirName.endsWith("/"))
            {
                dirName = dirName + "/";
            }

            dirName = toSFTP(dirName);

            channel.mkdir(dirName);

            fireDirectoryUpdate();

            return true;
        }
        catch(Exception ex)
        {
            Log.debug("Failed to create directory (" + ex + ").");

            return false;
        }
    }

    public void list() throws IOException
    {
    }

    public boolean chdir(String p)
    {
        return chdir(p, true);
    }

    public boolean chdir(String p, boolean refresh)
    {
        String tmp = toSFTP(p);

        try
        {
            if(!tmp.endsWith("/"))
            {
                tmp = tmp + "/";
            }
            if(tmp.endsWith("../"))
            {
                return cdup();
            }
            
            System.out.println("sftp path: "+tmp+", chan: "+channel);  
            channel.cd(tmp);

            pwd = tmp;

            //Log.debug("chdir: " + getPWD());
            if(refresh)
            {
                fireDirectoryUpdate();
            }

            //System.out.println("chdir2: " + getPWD());
            //Log.debug("Changed path to: " + tmp);
            return true;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();

            //System.out.println(tmp);
            Log.debug("Could not change directory (" + ex + ").");

            return false;
        }
    }

    public boolean cdup()
    {
        String tmp = pwd;

        if(pwd.endsWith("/"))
        {
            tmp = pwd.substring(0, pwd.lastIndexOf("/"));
        }

        return chdir(tmp.substring(0, tmp.lastIndexOf("/") + 1));
    }

    public boolean chdirNoRefresh(String p)
    {
        return chdir(p, false);
    }

    public String getLocalPath()
    {
        return path;
    }

    public boolean setLocalPath(String p)
    {
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
            catch(IOException ex)
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
    
    public String[] sortLs()
    {
        try
        {
        	System.out.println(pwd);
            Vector v = channel.ls(pwd);

            String[] tmp = new String[v.size()];
            files = new String[tmp.length];
            size = new String[tmp.length];
            perms = new int[tmp.length];

            Enumeration e = v.elements();
            int x = 0;

            while(e.hasMoreElements())
            {
            	LsEntry entry = ((LsEntry)e.nextElement());
                tmp[x] = entry.getFilename();

                size[x] = ""+entry.getAttrs().getSize();

                //Log.debug("Perms: "+entry.getAttrs().getPermissionsString());
                
                /*
                if(!entry.getAttrs().getPermissionsString())
                {
                    perms[x] = FtpConnection.DENIED;
                }
                else
                {*/
                    perms[x] = FtpConnection.R;
                //}

                //Log.debugRaw(".");
                if(entry.getAttrs().isDir() && !tmp[x].endsWith("/"))
                {
                    tmp[x] = tmp[x] + "/";
                }

                x++;
            }

            System.arraycopy(tmp, 0, files, 0, tmp.length);

            return files;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            Log.debug(" Error while listing directory: " + ex);
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

    public int handleUpload(String f)
    {
        if(Settings.getEnableSftpMultiThreading())
        {

            Sftp2Transfer t = new Sftp2Transfer(getLocalPath(), getPWD(),
                    f, user, pass, listeners,
                    Transfer.UPLOAD, keyfile, host, ""+port);
        }
        else
        {
            upload(f);
        }

        return 0;
    }

    public int handleDownload(String f) 
    {
        if(Settings.getEnableSftpMultiThreading())
        {
            Sftp2Transfer t = new Sftp2Transfer(getLocalPath(), getPWD(),
                    f, user, pass, listeners,
                    Transfer.DOWNLOAD, keyfile, host, ""+port);
        }
        else
        {
            download(f);
        }

        return 0;
    }

    public int upload(String f)
    {
        String file = toSFTP(f);

        if(file.endsWith("/"))
        {
            String out = StringUtils.getDir(file);
            uploadDir(file, getLocalPath() + out);
            fireActionFinished(this);
        }
        else
        {
            String outfile = StringUtils.getFile(file);

            //System.out.println("transfer: " + file + ", " + getLocalPath() + outfile);
            work(getLocalPath() + outfile, file, true);
            fireActionFinished(this);
        }

        return 0;
    }

    public int download(String f)
    {
        String file = toSFTP(f);

        if(file.endsWith("/"))
        {
            String out = StringUtils.getDir(file);
            downloadDir(file, getLocalPath() + out);
            fireActionFinished(this);
        }
        else
        {
            String outfile = StringUtils.getFile(file);

            //System.out.println("transfer: " + file + ", " + getLocalPath() + outfile);
            work(file, getLocalPath() + outfile, false);
            fireActionFinished(this);
        }

        return 0;
    }

    private void downloadDir(String dir, String out)
    {
        try
        {
            //System.out.println("downloadDir: " + dir + "," + out);
            fileCount = 0;
            shortProgress = true;
            baseFile = StringUtils.getDir(dir);
            
            Vector v = channel.ls(dir);

            String[] tmp = new String[v.size()];
            Enumeration e = v.elements();
            int x = 0;

            while(e.hasMoreElements())
            {
            	LsEntry entry = ((LsEntry)e.nextElement());
                tmp[x] = entry.getFilename();
 
                if(entry.getAttrs().isDir() && !tmp[x].endsWith("/"))
                {
                    tmp[x] = tmp[x] + "/";
                }

                x++;
            }

            File fx = new File(out);
            fx.mkdir();

            for(int i = 0; i < tmp.length; i++)
            {
                if(tmp[i].equals("./") || tmp[i].equals("../"))
                {
                    continue;
                }

                tmp[i] = tmp[i].replace('\\', '/');

                //System.out.println("1: " + dir+tmp[i] + ", " + out +tmp[i]);

                if(tmp[i].endsWith("/"))
                {
                    if(!tmp[i].endsWith("/"))
                    {
                        tmp[i] = tmp[i] + "/";
                    }

                    downloadDir(dir + tmp[i], out + tmp[i]);
                }
                else
                {
                    fileCount++;
                    fireProgressUpdate(baseFile,
                                       DataConnection.GETDIR + ":" + fileCount,
                                       -1);
                    work(dir + tmp[i], out + tmp[i], false);
                }

            }
            
            //System.out.println("enddir");

            fireProgressUpdate(baseFile,
                               DataConnection.DFINISHED + ":" + fileCount, -1);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            System.out.println(dir + ", " + out);
            Log.debug("Transfer error: " + ex);
            fireProgressUpdate(baseFile,
                               DataConnection.FAILED + ":" + fileCount, -1);
        }

        shortProgress = false;
    }

    private void uploadDir(String dir, String out)
    {
        try
        {
            //System.out.println("uploadDir: " + dir + "," + out);
            isDirUpload = true;
            fileCount = 0;
            shortProgress = true;
            baseFile = StringUtils.getDir(dir);

            File f2 = new File(out);
            String[] tmp = f2.list();

            if(tmp == null)
            {
                return;
            }

            channel.mkdir(dir); 
            //channel.chmod(744, dir);

            for(int i = 0; i < tmp.length; i++)
            {
                if(tmp[i].equals("./") || tmp[i].equals("../"))
                {
                    continue;
                }

                tmp[i] = tmp[i].replace('\\', '/');

                //System.out.println("1: " + dir+tmp[i] + ", " + out +tmp[i]);
                File f3 = new File(out + tmp[i]);

                if(f3.isDirectory())
                {
                    if(!tmp[i].endsWith("/"))
                    {
                        tmp[i] = tmp[i] + "/";
                    }

                    uploadDir(dir + tmp[i], out + tmp[i]);
                }
                else
                {
                    fileCount++;
                    fireProgressUpdate(baseFile,
                                       DataConnection.PUTDIR + ":" + fileCount,
                                       -1);
                    work(out + tmp[i], dir + tmp[i], true);
                }
            }

            fireProgressUpdate(baseFile,
                               DataConnection.DFINISHED + ":" + fileCount, -1);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            System.out.println(dir + ", " + out);
            Log.debug("Transfer error: " + ex);
            fireProgressUpdate(baseFile,
                               DataConnection.FAILED + ":" + fileCount, -1);
        }

        isDirUpload = false;
        shortProgress = true;
    }

    private String toSFTP(String f)
    {
        String file;

        if(f.startsWith("/"))
        {
            file = f;
        }
        else
        {
            file = getPWD() + f;
        }

        file = file.replace('\\', '/');

        //System.out.println("file: "+file);
        return file;
    }

    private String toSFTPDir(String f)
    {
        String file;

        if(f.startsWith("/"))
        {
            file = f;
        }
        else
        {
            file = pwd + f;
        }

        file = file.replace('\\', '/');

        if(!file.endsWith("/"))
        {
            file = file + "/";
        }

        //System.out.println("file: "+file);
        return file;
    }

    private void work(String file, String outfile, boolean up)
    {
        BufferedInputStream in = null;
        BufferedOutputStream out = null;

        //System.out.println("work");
        
        try
        {
            boolean outflag = false;

            if(up)
            {
                in = new BufferedInputStream(new FileInputStream(file));
            }
            else
            {
                in = new BufferedInputStream(channel.get(file));
            }

            if(up)
            {
                outflag = true;

                try {
                	channel.rm(outfile);
                }
                catch(Exception ex) {
                	
                }
                out = new BufferedOutputStream(channel.put(outfile));
            }
            else
            {
                out = new BufferedOutputStream(new FileOutputStream(outfile));
            }

            //System.out.println("out: " + outfile + ", in: " + file);
            byte[] buf = new byte[smbBuffer];
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

                //System.out.println(file + ":" + StringUtils.getFile(file));
                if(outflag)
                {
                    fireProgressUpdate(StringUtils.getFile(outfile),
                                       DataConnection.PUT, reallen);
                }
                else
                {
                    fireProgressUpdate(StringUtils.getFile(file),
                                       DataConnection.GET, reallen);
                }
            }

            //if(up) {
            //	channel.chmod(744, outfile);
            //}
            
            fireProgressUpdate(file, DataConnection.FINISHED, -1);
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
            Log.debug("Error with file IO (" + ex + ")!");
            fireProgressUpdate(file, DataConnection.FAILED, -1);
        }
        catch(SftpException ex)
        {
            ex.printStackTrace();
            Log.debug("Error with SFTP IO (" + ex + ")!");
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

    public boolean rename(String oldName, String newName)
    {
        try
        {
            oldName = toSFTP(oldName);
            newName = toSFTP(newName);

            channel.rename(oldName, newName);

            return true;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();

            Log.debug("Could rename file (" + ex + ").");

            return false;
        }
    }

    private void update(String file, String type, int bytes)
    {
        if(listeners == null)
        {
        }
        else
        {
            for(int i = 0; i < listeners.size(); i++)
            {
                ConnectionListener listener = (ConnectionListener) listeners.elementAt(i);
                listener.updateProgress(file, type, bytes);
            }
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
        this.user = user;
        this.pass = pass;

        if(!login())
        {
            Log.debug("Login failed.");

            return false;
        }
        else
        {
            Log.debug("Authed successfully.");

            //if(!chdir(getPWD())) chdir("/");
        }

        return true;
    }

    /** progress update */
    public void fireProgressUpdate(String file, String type, int bytes)
    {
        if(listeners == null)
        {
            return;
        }

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
                else if(isDirUpload)
                {
                    listener.updateProgress(baseFile,
                                            DataConnection.PUTDIR + ":" +
                                            fileCount, bytes);
                }
                else
                {
                    listener.updateProgress(baseFile,
                                            DataConnection.GETDIR + ":" +
                                            fileCount, bytes);
                }
            }
            else
            {
                listener.updateProgress(file, type, bytes);
            }
        }
    }

    public void fireActionFinished(Sftp2Connection con)
    {
        if(listeners == null)
        {
        }
        else
        {
            for(int i = 0; i < listeners.size(); i++)
            {
                ((ConnectionListener) listeners.elementAt(i)).actionFinished(con);
            }
        }
    }

    public int upload(String file, InputStream i)
    {
        BufferedOutputStream out = null;
        BufferedInputStream in = null;

        try
        {
            file = toSFTP(file);

            out = new BufferedOutputStream(channel.put(file));
            in = new BufferedInputStream(i);

            //Log.debug(getLocalPath() + ":" + file+ ":"+getPWD());
            byte[] buf = new byte[smbBuffer];
            int len = 0;
            int reallen = 0;

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
                                   DataConnection.PUT, reallen);
            }

            //channel.chmod(744, file);
            
            fireProgressUpdate(file, DataConnection.FINISHED, -1);

            return 0;
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
            Log.debug("Error with file IO (" + ex + ")!");
            fireProgressUpdate(file, DataConnection.FAILED, -1);

            return -1;
        }
        catch(SftpException ex)
        {
            ex.printStackTrace();
            Log.debug("Error with file SFTP IO (" + ex + ")!");
            fireProgressUpdate(file, DataConnection.FAILED, -1);

            return -1;
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

    public InputStream getDownloadInputStream(String file)
    {
        try
        {

            return channel.get(file);
        }
        catch(SftpException ex)
        {
            ex.printStackTrace();
            Log.debug(ex +
                      " @Sftp2Connection::getDownloadInputStream");

            return null;
        }
    }

    public Date[] sortDates()
    {
        return null;
    }
}

class MyUserInfo implements UserInfo {

		String password;

		public MyUserInfo(String pass) {
			this.password = pass;
		}

		public String getPassword(){ return password; }

		public boolean promptYesNo(String str){
			/*
			Object[] options={ "yes", "no" };
			int foo=JOptionPane.showOptionDialog(null, 
					str, 
					"Warning", 
					JOptionPane.DEFAULT_OPTION, 
					JOptionPane.WARNING_MESSAGE,
					null, options, options[0]);
			return foo==0;
			*/
			
			return true;
		}

		public String getPassphrase(){ return password; }

		public boolean promptPassphrase(String message){ return true; }

		public boolean promptPassword(String message){
				return true;
		}
		
	    public void showMessage(String message){
	        //JOptionPane.showMessageDialog(null, message);
	    }
}
