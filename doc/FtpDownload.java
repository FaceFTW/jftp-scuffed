import net.sf.jftp.net.ConnectionHandler;
import net.sf.jftp.net.ConnectionListener;
import net.sf.jftp.net.FtpConnection;
import net.sf.jftp.net.BasicConnection;
import net.sf.jftp.system.logging.Log;
import net.sf.jftp.system.logging.Logger;
import net.sf.jftp.config.Settings;

// this class download a file via anonymous ftp and shows output.
//
// if you want to use the api in a more complex way, please do at least take a look at the
// FtpConnection, FtpTransfer, ConnectionHandler, DirPanel (blockedTransfer, transfer)
// and ConnectionListener sourcecode.
public class FtpDownload implements Logger, ConnectionListener
{

 // is the connection established?
 private boolean isThere = false;

 public static long time = 0;

	//creates a FtpConnection and downloads a file
 public FtpDownload(final String host, final String file)
 {
	// the ftp client default is very small, you may want to increase this
	Settings.bufferSize = 16384; 

	final long current = System.currentTimeMillis();
	//System.out.println("1) "+(System.currentTimeMillis()-current)+"ms.");

 	// register app as Logger, debug() and debugRaw below are from now on called by
	// FtpConnection
 	Log.setLogger(this);

	// create a FtpConnection - note that it does *not* connect instantly
 	final FtpConnection con = new FtpConnection(host);

	//System.out.println("2) "+(System.currentTimeMillis()-current)+"ms.");

	// set updatelistener, interface methods are below
	con.addConnectionListener(this);

	// set handler
	 // connection pool, not necessary but you should take a look at this class
	 // if you want to use multiple event based ftp transfers.
	 final net.sf.jftp.net.ConnectionHandler handler = new net.sf.jftp.net.ConnectionHandler();
	 con.setConnectionHandler(handler);

	// connect and login. this is from where connectionFailed() may be called for example
	con.login("anonymous","........");

	//System.out.println("3) "+(System.currentTimeMillis()-current)+"ms.");

	// login calls connectionInitialized() below which sets isThere to true
	while(!isThere)
	{
		try { Thread.sleep(10); }
		catch(final Exception ex) { ex.printStackTrace(); }
	}

	//System.out.println("4) "+(System.currentTimeMillis()-current)+"ms.");

	// download the file - this method blocks until the download has finished
	// if you want non-blocking, multithreaded io, just use
	//
	// con.handleDownload(file);
	//
	// which spawns a new thread for the download
	
	con.chdir("pub/linux/kernel");
	
	con.download(file);

	 FtpDownload.time = (System.currentTimeMillis()-current);

	System.out.println("Download took "+ FtpDownload.time +"ms.");
 }

 // download welcome.msg from sourceforge or any other given file
 public static void main(final String[] argv)
 {
 	if(argv.length < 2)
	{
		final FtpDownload f = new FtpDownload("ftp.kernel.org", "CREDITS");
	}
	else
	{
		final FtpDownload f = new FtpDownload(argv[0], argv[1]);
	}
	
	System.exit(0);
 }

// ------------------ needed by ConnectionListener interface -----------------

// called if the remote directory has changed
 public void updateRemoteDirectory(final BasicConnection con)
 {
 	System.out.println("new path is: " + con.getPWD());
 }

 // called if a connection has been established
 public void connectionInitialized(final BasicConnection con)
 {
  	isThere = true;
 }
 
 // called every few kb by DataConnection during the trnsfer (interval can be changed in Settings)
 public void updateProgress(final String file, final String type, final long bytes) {}

 // called if connection fails
 public void connectionFailed(final BasicConnection con, final String why) {System.out.println("connection failed!");}

 // up- or download has finished
 public void actionFinished(final BasicConnection con) {}


 // ------------ needed by Logger interface  --------------

    // main log method
    public void debug(final String msg) {} //{System.out.println(msg);}

    // rarely used
    public void debugRaw(final String msg) {}//System.out.print(msg);}

    // methods below are not used yet.

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
