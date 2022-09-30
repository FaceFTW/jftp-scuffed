import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;

public class Grapher extends Canvas
{

public final String[] files = new String[] {"JFtp.java", "LoadSet.java","EventCollector.java", "EventProcessor.java", "FtpEvent.java","DirCellRenderer.java","DirPanel.java",
"Displayer.java","HostChooser.java","HostList.java","Properties.java","Updater.java","GUIDefaults.java","HPasswordField.java",
"DataConnection.java","FtpClient.java","FtpConnection.java","FtpConstants.java","FtpServerSocket.java","FtpURLConnection.java",
"FtpURLStreamHandler.java","JConnection.java","LocalIO.java","Log.java","Log4JLogger.java","Logger.java","SystemLogger.java",
"CommandLine.java","SaveSet.java","Settings.java","Acceptor.java","Event.java","EventHandler.java","FtpEventConstants.java",
"FtpEventHandler.java","AutoRemover.java","Creator.java","DirCanvas.java","DirEntry.java","DirLister.java","DownloadList.java",
"LoadPanel.java","PathChanger.java","RemoteCommand.java","Remover.java","RemoverQuery.java","ResumeDialog.java",
"StatusCanvas.java","StatusPanel.java","Template.java","HFrame.java","HImage.java","HImageButton.java","HPanel.java",
"HTextField.java","ConnectionHandler.java","ConnectionListener.java","FtpServer.java","Transfer.java","StringUtils.java" };

public final String prefix =  "/home/cdemon/JFtp/j-ftp/src/java/net/sf/jftp/";
public final String[] paths = new String[] { prefix, prefix+"gui/", prefix+"net/", prefix+"util/", prefix+"config/", prefix+"gui/framework",
								   prefix+"event/" };

public final Hashtable table = new Hashtable();
public final Hashtable pool = new Hashtable();

public static final int width = 800;
public static final int height = 600;

public Grapher()
{
	this.setSize(Grapher.width, Grapher.height);

 try
 {
	 for (final String s : files) {
		 final java.io.File f = this.getFile(s);

		 if (f != null && f.exists()) {
			 for (final String file : files) {
				 final int x = this.countRelations(f, file);
				 if (x > 0) table.put(s + ":" + file.substring(0, file.indexOf(".java")), "" + x);
			 }
		 }
	 }

	 this.show();
	 this.repaint();
 }
 catch(final Exception ex)
 {
 	ex.printStackTrace();
 }
}

public void paint(final Graphics g)
{
	// init
	g.setColor(Color.white);
	g.fillRect(0,0, this.getSize().width, this.getSize().height);
	g.setColor(Color.blue);
	g.setFont(new Font("serif", Font.BOLD, 14));

	// points
 	final Random r = new Random();

	for (final String s : files) {
		while (true) {
			final int x = r.nextInt(Grapher.width - 200);
			int y = r.nextInt(Grapher.height - 20);
			if (y < 30) y = 30;

			if (this.check(x, y)) {
				//System.out.println("adding: " + files[i]);
				final String tmp = s.substring(0, s.indexOf(".java"));
				final java.awt.Point p = new java.awt.Point(x, y);
				pool.put(tmp, p);

				this.linkPoints(g, p);

				//g.drawString(tmp, x, y);

				break;
			}
		}
	}

	g.setColor(Color.blue);

	for (final String file : files) {
		final String tmp = file.substring(0, file.indexOf(".java"));
		final java.awt.Point p2 = (java.awt.Point) pool.get(tmp);
		if (p2 == null) continue;
		g.drawString(tmp, (int) p2.getX(), (int) p2.getY());
	}

}

public void linkPoints(final Graphics g, final Point p)
{

	final Enumeration k = table.keys();
	final Enumeration e = table.elements();
	String xk = null;
	String file = null;

	while(k.hasMoreElements())
	{
		xk = (String) k.nextElement();
		final int x = Integer.parseInt((String)e.nextElement());

		file = xk.substring(0, xk.indexOf(":"));
		file = file.substring(0,file.indexOf(".java"));

		//System.out.println("<" + file + "> " + "(" + link + ")" + " - " + x);
		final Point x2 = (Point) pool.get(file);
		if(x2 == null) continue;

		if(x > 0)
		{
			//for(int y=0; y<x; y++)
			//{
				int color = 255 - x*10 +40;
				if(color < 0) color=0;
				if(color > 255) color = 255;

				g.setColor(new Color(color,color,color));
				if(color < 255) g.drawLine(((int)p.getX()+20),((int)p.getY()+15),((int)x2.getX()+20),((int)x2.getY()+15));
			//}
		}

	}
}


public boolean check(final int x, final int y)
{
	final Enumeration e = pool.elements();
	Point d = null;
	int a;
	int b;

	while(e.hasMoreElements())
	{
		d = (Point) e.nextElement();
		a = (int) d.getX();
		b = (int) d.getY();

		if(a > x-100 && a < x+100) {
			if(b > y-20 && b < y +20)
			{
				return false;
			}
		}
	}

	return true;
}

public int countRelations(final File f, String what) throws IOException
{
	int x = 0;
	String tmp;
	what = what.substring(0, what.indexOf(".java"));

  	final URL url = f.toURL();
	final DataInputStream in = new DataInputStream(new BufferedInputStream(url.openStream()));

	while(true)
	{
		tmp = in.readLine();
		//System.out.println(f.getAbsolutePath() + ": " + tmp + ": " +what);
		if(tmp == null) break;
		if(tmp.contains(what)) x++;
	}

	in.close();

	return x;
}

public File getFile(final String name)
{
	for (final String path : paths) {
		final java.io.File f = new java.io.File(path + name);
		if (f.exists()) return f;
	}

	return null;
}

public static void main(final String[] argv)
{
	final Grapher g = new Grapher();
	final JFrame j = new JFrame();
	j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	j.getContentPane().setLayout(new BorderLayout(5,5));
	j.setSize(Grapher.width +10, Grapher.height +25);
	j.getContentPane().add("Center",g);
	j.show();
}

}
