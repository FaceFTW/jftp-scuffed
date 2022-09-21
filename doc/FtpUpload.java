import net.sf.jftp.net.ConnectionHandler;
import net.sf.jftp.net.ConnectionListener;
import net.sf.jftp.net.DataConnection;
import net.sf.jftp.net.FtpConnection;
import net.sf.jftp.net.BasicConnection;
import net.sf.jftp.system.logging.Log;
import net.sf.jftp.system.logging.Logger;
import net.sf.jftp.config.Settings;

import java.io.*;

/**
* See FtpDownload.java for comments.
*/
public class FtpUpload implements Logger, ConnectionListener
{

 private boolean isThere = false;

 private final ConnectionHandler handler = new ConnectionHandler();

 public FtpUpload(String host, String dir, String file)
 {
 	Log.setLogger(this);

 	FtpConnection con = new FtpConnection(host);

	con.addConnectionListener(this);

	con.setConnectionHandler(handler);

	con.login("anonymous","no@no.no");

	while(!isThere)
	{
		try { Thread.sleep(10); }
		catch(Exception ex) { ex.printStackTrace(); }
	}

	con.chdir(dir);

	con.upload(file);
 }

 public static void main(String[] argv)
 {
    if(argv.length == 3)
    { 
	    FtpUpload f = new FtpUpload(argv[0], argv[2], argv[1]); 
    }
    else 
    {
     FtpUpload g = 
	    new FtpUpload("upload.sourceforge.net", "/incoming", "test.txt");
    }
}


 public void updateRemoteDirectory(BasicConnection con)
 {
 	System.out.println("new path is: " + con.getPWD());
 }
 
 public void connectionInitialized(BasicConnection con)
 {
  	isThere = true;
 }
 
 public void updateProgress(String file, String type, long bytes) {}
 
 public void connectionFailed(BasicConnection con, String why) {System.out.println("connection failed!");}

 public void actionFinished(BasicConnection con) {}

    public void debug(String msg) {System.out.println(msg);}

     public void debugRaw(String msg) {System.out.print(msg);}

    public void debug(String msg, Throwable throwable) {}

    public void warn(String msg) {}

    public void warn(String msg, Throwable throwable) {}

    public void error(String msg) {}

    public void error(String msg, Throwable throwable) {}

    public void info(String msg) {}

    public void info(String msg, Throwable throwable) {}

    public void fatal(String msg) {}

    public void fatal(String msg, Throwable throwable) {}

}
