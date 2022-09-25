import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;

public class Grapher extends Canvas
{

public String[] files = new String[] {"JFtp.java", "LoadSet.java","EventCollector.java", "EventProcessor.java", "FtpEvent.java","DirCellRenderer.java","DirPanel.java",
"Displayer.java","HostChooser.java","HostList.java","Properties.java","Updater.java","GUIDefaults.java","HPasswordField.java",
"DataConnection.java","FtpClient.java","FtpConnection.java","FtpConstants.java","FtpServerSocket.java","FtpURLConnection.java",
"FtpURLStreamHandler.java","JConnection.java","LocalIO.java","Log.java","Log4JLogger.java","Logger.java","SystemLogger.java",
"CommandLine.java","SaveSet.java","Settings.java","Acceptor.java","Event.java","EventHandler.java","FtpEventConstants.java",
"FtpEventHandler.java","AutoRemover.java","Creator.java","DirCanvas.java","DirEntry.java","DirLister.java","DownloadList.java",
"LoadPanel.java","PathChanger.java","RemoteCommand.java","Remover.java","RemoverQuery.java","ResumeDialog.java",
"StatusCanvas.java","StatusPanel.java","Template.java","HFrame.java","HImage.java","HImageButton.java","HPanel.java",
"HTextField.java","ConnectionHandler.java","ConnectionListener.java","FtpServer.java","Transfer.java","StringUtils.java" };

public String prefix =  "/home/cdemon/JFtp/j-ftp/src/java/net/sf/jftp/";
public String[] paths = new String[] { prefix, prefix+"gui/", prefix+"net/", prefix+"util/", prefix+"config/", prefix+"gui/framework",
								   prefix+"event/" };

public Hashtable table = new Hashtable();
public Hashtable pool = new Hashtable();

public static int width = 800;
public static int height = 600;

public Grapher()
{
 setSize(width,height);

 try
 {
	 for (String s : files) {
		 java.io.File f = getFile(s);

		 if (f != null && f.exists()) {
			 for (String file : files) {
				 int x = countRelations(f, file);
				 if (x > 0) table.put(s + ":" + file.substring(0, file.indexOf(".java")), "" + x);
			 }
		 }
	 }

 show();
 repaint();
 }
 catch(Exception ex)
 {
 	ex.printStackTrace();
 }
}

public void paint(Graphics g)
{
	// init
	g.setColor(Color.white);
	g.fillRect(0,0,getSize().width, getSize().height);
	g.setColor(Color.blue);
	g.setFont(new Font("serif", Font.BOLD, 14));

	// points
 	Random r = new Random();

	for (String s : files) {
		while (true) {
			int x = r.nextInt(width - 200);
			int y = r.nextInt(height - 20);
			if (y < 30) y = 30;

			if (check(x, y)) {
				//System.out.println("adding: " + files[i]);
				String tmp = s.substring(0, s.indexOf(".java"));
				java.awt.Point p = new java.awt.Point(x, y);
				pool.put(tmp, p);

				linkPoints(g, p);

				//g.drawString(tmp, x, y);

				break;
			}
		}
	}

	g.setColor(Color.blue);

	for (String file : files) {
		String tmp = file.substring(0, file.indexOf(".java"));
		java.awt.Point p2 = (java.awt.Point) pool.get(tmp);
		if (p2 == null) continue;
		//g.setColor(new Color(255,180,180));
		//g.fillRect((int)  p2.getX(), (int)  p2.getY()-12, 80,  15);
		//g.setColor(Color.blue);
		g.drawString(tmp, (int) p2.getX(), (int) p2.getY());
	}

}

public void linkPoints(Graphics g, Point p)
{
	// fill

	Enumeration k = table.keys();
	Enumeration e = table.elements();
	String xk = null;
	String file = null;
	String link = null;

	while(k.hasMoreElements())
	{
		xk = (String) k.nextElement();
		int x = Integer.parseInt((String)e.nextElement());

		file = xk.substring(0, xk.indexOf(":"));
		file = file.substring(0,file.indexOf(".java"));
		link = xk.substring( xk.indexOf(":")+1);

		//System.out.println("<" + file + "> " + "(" + link + ")" + " - " + x);
		Point x2 = (Point) pool.get(file);
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


public boolean check(int x, int y)
{
	Enumeration e = pool.elements();
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

public int countRelations(File f, String what) throws IOException
{
	int x = 0;
	String tmp;
	what = what.substring(0, what.indexOf(".java"));

  	URL url = f.toURL();
	DataInputStream in = new DataInputStream(new BufferedInputStream(url.openStream()));

	while(true)
	{
		tmp = in.readLine();
		//System.out.println(f.getAbsolutePath() + ": " + tmp + ": " +what);
		if(tmp == null) break;
		if(tmp.indexOf(what) >= 0) x++;
	}

	in.close();

	return x;
}

public File getFile(String name)
{
	for (String path : paths) {
		java.io.File f = new java.io.File(path + name);
		if (f.exists()) return f;
	}

	return null;
}

public static void main(String[] argv)
{
	Grapher g = new Grapher();
	JFrame j = new JFrame();
	j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	j.getContentPane().setLayout(new BorderLayout(5,5));
	j.setSize(width+10,height+25);
	j.getContentPane().add("Center",g);
	j.show();
}

}
