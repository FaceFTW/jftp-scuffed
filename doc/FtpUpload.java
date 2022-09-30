import net.sf.jftp.net.ConnectionHandler;
import net.sf.jftp.net.ConnectionListener;
import net.sf.jftp.net.FtpConnection;
import net.sf.jftp.net.BasicConnection;
import net.sf.jftp.system.logging.Log;
import net.sf.jftp.system.logging.Logger;

/**
* See FtpDownload.java for comments.
*/
public class FtpUpload implements Logger, ConnectionListener
{

 private boolean isThere = false;

	public FtpUpload(final String host, final String dir, final String file)
 {
 	Log.setLogger(this);

 	final FtpConnection con = new FtpConnection(host);

	con.addConnectionListener(this);

	 final net.sf.jftp.net.ConnectionHandler handler = new net.sf.jftp.net.ConnectionHandler();
	 con.setConnectionHandler(handler);

	con.login("anonymous","no@no.no");

	while(!this.isThere)
	{
		try { Thread.sleep(10); }
		catch(final Exception ex) { ex.printStackTrace(); }
	}

	con.chdir(dir);

	con.upload(file);
 }

 public static void main(final String[] argv)
 {
    if(argv.length == 3)
    { 
	    final FtpUpload f = new FtpUpload(argv[0], argv[2], argv[1]);
    }
    else 
    {
     final FtpUpload g =
	    new FtpUpload("upload.sourceforge.net", "/incoming", "test.txt");
    }
}


 public void updateRemoteDirectory(final BasicConnection con)
 {
 	System.out.println("new path is: " + con.getPWD());
 }
 
 public void connectionInitialized(final BasicConnection con)
 {
	 this.isThere = true;
 }
 
 public void updateProgress(final String file, final String type, final long bytes) {}
 
 public void connectionFailed(final BasicConnection con, final String why) {System.out.println("connection failed!");}

 public void actionFinished(final BasicConnection con) {}

    public void debug(final String msg) {System.out.println(msg);}

     public void debugRaw(final String msg) {System.out.print(msg);}

    public void debug(final String msg, final Throwable throwable) {}

    public void warn(final String msg) {}

    public void warn(final String msg, final Throwable throwable) {}

    public void error(final String msg) {}

    public void error(final String msg, final Throwable throwable) {}

    public void info(final String msg) {}

    public void info(final String msg, final Throwable throwable) {}

    public void fatal(final String msg) {}

    public void fatal(final String msg, final Throwable throwable) {}

}
