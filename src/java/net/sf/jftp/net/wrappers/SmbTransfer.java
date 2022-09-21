package net.sf.jftp.net.wrappers;

import java.util.Vector;

import net.sf.jftp.net.Transfer;


public class SmbTransfer implements Runnable
{
    private final String url;
    private final String domain;
    private final String localPath;
    private final String file;
    private final String user;
    private final String pass;
    private SmbConnection con = null;
    private final String type;
    public Thread runner;
    private final Vector listeners;

    public SmbTransfer(String url, String localPath, String file, String user,
                       String pass, String domain, Vector listeners, String type)
    {
        this.url = url;
        this.localPath = localPath;
        this.file = file;
        this.user = user;
        this.pass = pass;
        this.type = type;
        this.domain = domain;
        this.listeners = listeners;

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
        con = new SmbConnection(url, domain, user, pass, null);
        con.setLocalPath(localPath);
        con.setConnectionListeners(listeners);

        if(type.equals(Transfer.DOWNLOAD))
        {
            con.download(file);
        }
        else if(type.equals(Transfer.UPLOAD))
        {
            con.upload(file);
        }
    }

    public SmbConnection getSmbConnection()
    {
        return con;
    }
}
