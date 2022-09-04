package net.sf.jftp.net.wrappers;

import java.util.Vector;

import net.sf.jftp.net.Transfer;

public class Sftp2Transfer implements Runnable
{
    private String host;
    private String localPath;
    private String remotePath;
    private String file;
    private String user;
    private String pass;
    private Sftp2Connection con = null;
    private String type;
    public Thread runner;
    private Vector listeners;
    private String keyfile;
    private String port;
    
    public Sftp2Transfer(String localPath, String remotePath,
                        String file, String user, String pass,
                        Vector listeners, String type, String keyfile, String host, String port)
    {
        this.localPath = localPath;
        this.remotePath = remotePath;
        this.file = file;
        this.user = user;
        this.pass = pass;
        this.type = type;
        this.listeners = listeners;
        this.keyfile = keyfile;
        this.host = host;
        this.port = port;

        prepare();
    }

    public void prepare()
    {
        runner = new Thread(this);
        runner.setPriority(Thread.MIN_PRIORITY);
        runner.start();
    }

    public void run()
    {
        con = new Sftp2Connection(host, port, keyfile);
        con.setConnectionListeners(listeners);
        con.login(user, pass);
        con.setLocalPath(localPath);
        con.chdir(remotePath);

        if(type.equals(Transfer.DOWNLOAD))
        { 
            con.download(file);
        }
        else if(type.equals(Transfer.UPLOAD))
        {
            con.upload(file);
        }
    }

    public Sftp2Connection getSftpConnection()
    {
        return con;
    }
}
