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
package net.sf.jftp.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import net.sf.jftp.system.LocalIO;
import net.sf.jftp.system.logging.Log;


public class FileSearch 
{

    private int currentDepth = 0;
    private final Hashtable checked = new Hashtable();
    public static boolean quiet = true;
    public static boolean ultraquiet = false;
    
    String localDir = ".";
    int MAX = 999999;
    int MIN_TERM = 1;
    int MIN_FACTOR = 1;
    boolean LOAD = false;
    String[] typeArray = { "" };
    String[] termArray = { "" };
    String[] optArray = { "" };
    String[] ignoreArray = { "" };
    String[] scanArray = { "" };
    

    public static void main(String[] argv) {
        String[] typeArray = { ".gz", ".bz2", ".zip", ".rar" };
        String[] termArray = { "linux", "kernel" };
        String[] optArray = { "download", "file", "mirror", "location" };
        String[] ignoreArray = { ".gif", ".jpg", ".png", ".swf", ".jar", ".class", ".google." };
        String[] scanArray = { ".html", ".htm", "/", ".jsp", ".jhtml", ".phtml", ".asp", ".xml", ".js", ".cgi" };
        String url = "http://www.google.de/search?hl=de&q=";
        
        for(int i=0; i<termArray.length; i++) {
        	url += termArray[i]+"+";
        }
        
    	FileSearch search = new FileSearch();
        
    	search.typeArray = typeArray;
    	search.termArray = termArray;
    	search.optArray = optArray;
    	search.ignoreArray = ignoreArray;
    	search.scanArray = scanArray;
    	search.MIN_TERM = 1;
    	
    	search.spider(url);
    	
    }

    private void spider(String url)
    {
        try
        {
        	if(url.indexOf("/") < 0)
        	{
        		url = url + "/";
        	}
        	
    		url = clear(url);
    		
        	Log.out(">>> URL: "+url);
        	Log.out(">>> Scanning for ");
        	
        	for(int i = 0; i < typeArray.length; i++)
        	{
        		Log.out(typeArray[i] + " ");
        	}
        	
        	Log.out("");
        	

            Log.out("Fetching initial HTML file...");

            Getter urlGetter = new Getter(localDir);
            urlGetter.fetch(url, true);

            Log.out("Searching for links...");
            LocalIO.pause(500);

            crawl(url);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private String clear(String url)
    {
        int idx = url.indexOf("http://");

        if(idx >= 0)
        {
            url = url.substring(7);
        }

        return url;
    }

    private Vector addVector(Vector v, Vector x)
    {
        Enumeration e = x.elements();

        while(e.hasMoreElements())
        {
            String next = (String) e.nextElement();
            v.add(next);
        }

        return v;
    }

    private int rate(String content) {
    	int score = 0;
    	
    	for(int i=0; i<termArray.length; i++) {
    		if(content.indexOf(termArray[i]) >= 0) score += 3;
    	}    	
    	
    	if(score < MIN_TERM) return 0;
 	
    	for(int i=0; i<optArray.length; i++) {
    		if(content.indexOf(optArray[i]) >= 0) score++;
    	}
    	
    	return score;
    }
    
    private int checkForResult(String url) {
    	//for(int i=0; i<typeArray.length; i++) {
    	//	if(url.indexOf(typeArray[i]) >= 0) return 2;
    	//}    	
    	
    	for(int i=0; i<ignoreArray.length; i++) {
    		if(url.indexOf(ignoreArray[i]) >= 0) return -1;	
    	}
    	
    	if(!checkForScanableUrl(url)) return -1;
    	
    	return 1;
    }
    
    private boolean checkForScanableUrl(String url) {
    	
    	if(checked.containsKey(url)) {
    		return false;
    	}
    	else {
    		checked.put(url, "");
    	}
    	
    	if(url.indexOf("/") > 0) {
    		String tmp = url.substring(0, url.indexOf("/"));
    	}
    	
    	for(int i=0; i<scanArray.length; i++) {
    		if(url.endsWith(scanArray[i])) return true;
    	}    	

    	return false;
    }
    
    private void crawl(String url) throws Exception
    {
        url = clear(url);

        int urlRating = checkForResult(url);
        if(!quiet) Log.out("URL-Rating: "+url+" -> "+urlRating+" @"+currentDepth);
        
        if(urlRating > 0) {
        	//System.out.println("!!!");
        	//Getter.chill(1000);
        	//System.exit(0);
        } else if(urlRating < 0 && currentDepth > 0) {
        	if(!quiet) Log.out("SKIP "+url);   	
        	return;
        }


        Getter urlGetter = new Getter(localDir);
        String content = urlGetter.fetch(url);
        
        int factor = rate(content);
        if(!quiet) Log.out("Content-Rating: "+url+" -> "+factor+" @"+currentDepth);
        
        if(factor < MIN_FACTOR) {
        	if(!quiet) Log.out("DROP: "+url);
        	return;
        }
        
        if(!ultraquiet) Log.out("Url: "+url+" -> "+urlRating+":"+factor+"@"+currentDepth);

        Vector m = sort(content, url.substring(0, url.lastIndexOf("/")),
                              "href=\"");
        m = addVector(m,
                      sort(content, url.substring(0, url.lastIndexOf("/")),
                                 "src=\""));
        m = addVector(m,
                      sort(content, url.substring(0, url.lastIndexOf("/")),
                                 "HREF=\""));
        m = addVector(m,
                      sort(content, url.substring(0, url.lastIndexOf("/")),
                                 "SRC=\""));

        Enumeration links = m.elements();

        while(links.hasMoreElements())
        {

            String next = (String) links.nextElement();
            
            if(!quiet)  Log.out("PROCESS: " + next);
            boolean skip = false;
            
            while(!skip) {
            	for(int i = 0; i < typeArray.length; i++)
            	{
            		if(next.endsWith(typeArray[i]) ||
            				typeArray[i].trim().equals("*"))
            		{
            			Log.out("HIT: "+url+" -> "+next);
            			//Getter.chill(2000);
            			
            			if(!LOAD || !checkForScanableUrl(url)) continue;
            			
            			int x = next.indexOf("/");
            			
            			if((x > 0) && (next.substring(0, x).indexOf(".") > 0))
            			{
            				Getter urlGetter2 = new Getter(localDir);
            				urlGetter2.fetch(next, false);
            				
            				continue;
            			}
            		}
            	}
            	
            	skip = true;
            }

            if(currentDepth < MAX)
            {

                int x = next.indexOf("/");

                if((x > 0) && (next.substring(0, x).indexOf(".") > 0))
                {
                    currentDepth++;
                    crawl(next);
                    currentDepth--;
                }
            }
        }
    }

    private Vector sort(String content, String url, String index)
    {
        Vector res = new Vector();
        int wo = 0;

        while(true)
        {
            wo = content.indexOf(index);

            if(wo < 0)
            {
                return res;
            }

            content = content.substring(wo + index.length());

            String was = content.substring(0, content.indexOf("\""));

            was = createAbsoluteUrl(was, url);
            res.add(was);
            if(!quiet) Log.out("ADD: " + was);
        }
    }

    private String[] check(String auswahl)
    {
        StringTokenizer tokenizer = new StringTokenizer(auswahl, "-", false);
        String[] strArr = new String[tokenizer.countTokens()];
        int tmp = 0;

        while(tokenizer.hasMoreElements())
        {
            strArr[tmp] = (String) tokenizer.nextElement();
            tmp++;
        }

        return strArr;
    }

    private String createAbsoluteUrl(String newLink, String baseUrl)
    {
        newLink = clear(newLink);

        if(newLink.startsWith(baseUrl))
        {
            return newLink;
        }

        if(newLink.startsWith("/") && (baseUrl.indexOf("/") > 0))
        {
            newLink = baseUrl.substring(0, baseUrl.indexOf("/")) + newLink;
        }
        else if(newLink.startsWith("/") && (baseUrl.indexOf("/") < 0))
        {
            newLink = baseUrl + newLink;
        }
        else if((newLink.indexOf(".") > 0))
        {
            int idx = newLink.indexOf("/");
            String tmp = "";

            if(idx >= 0)
            {
                tmp = newLink.substring(0, idx);
            }

            if((tmp.indexOf(".") > 0))
            {
                return clear(newLink);
            }

            if(baseUrl.endsWith("/"))
            {
                newLink = baseUrl + newLink;
            }
            else
            {
                newLink = baseUrl + "/" + newLink;
            }
        }

        //Log.out("-> " + newLink);

        return newLink;
    }

}


class Getter
{
    private String localDir = null;

    public Getter(String localDir)
    {
        this.localDir = localDir;
    }

    public String fetch(String url)
    {
        try
        {
            String host = url.substring(0, url.indexOf("/"));
            String wo = url.substring(url.indexOf("/"));
            String result = "";

            //Log.out(">> " + host + wo);

            Socket deal = new Socket(host, 80);
            deal.setSoTimeout(5000);

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(deal.getOutputStream()));
            BufferedReader in = new BufferedReader(new InputStreamReader(deal.getInputStream()));

            out.write("GET http://" + url + " HTTP/1.0\n\n");
            out.flush();

            int len = 0;

            while(!in.ready() && (len < 5000))
            {
                chill(100);
                len += 100;
            }

            while(in.ready())
            {
                result = result + in.readLine();
            }

            out.close();
            in.close();

            return result;
        }
        catch(Exception ex)
        {
        	if(!FileSearch.quiet) ex.printStackTrace();
        }

        return "";
    }

    public void fetch(String url, boolean force)
    {
        try
        {
            String host = url.substring(0, url.indexOf("/"));
            String wo = url.substring(url.indexOf("/"));
            String result = "";

            if(!FileSearch.quiet) Log.debug(">>> " + host + wo);

            //JFtp.statusP.jftp.ensureLogging();
            File d = new File(localDir);
            d.mkdir();

            File f = new File(localDir + wo.substring(wo.lastIndexOf("/") + 1));

            if(f.exists() && !force)
            {
            	if(!FileSearch.quiet) Log.debug(">>> file already exists...");

                return;
            }
            else
            {
                f.delete();
            }

            Socket deal = new Socket(host, 80);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(deal.getOutputStream()));
            DataInputStream in = new DataInputStream(new BufferedInputStream(deal.getInputStream()));

            BufferedOutputStream localOut = new BufferedOutputStream(new FileOutputStream(localDir +
                                                                                        wo.substring(wo.lastIndexOf("/") +
                                                                                                     1)));

            byte[] alu = new byte[2048];

            out.write("GET http://" + url + " HTTP/1.0\n\n");
            out.flush();

            boolean line = true;
            boolean bin = false;

            while(true)
            {
                chill(10);

                String tmp = "";

                while(line)
                {
                    String x = in.readLine();

                    if(x == null)
                    {
                        break;
                    }

                    tmp += (x + "\n");

                    if(x.equals(""))
                    {
                        line = false;
                    }
                }

                int x = in.read(alu);

                if(x == -1)
                {
                    if(line)
                    {
                        localOut.write(tmp.getBytes(), 0, tmp.length());
                    }

                    out.close();
                    in.close();
                    localOut.flush();
                    localOut.close();

                    return;
                }
                else
                {
                    localOut.write(alu, 0, x);
                }
            }
        }
        catch(Exception ex)
        {
        	if(!FileSearch.quiet) ex.printStackTrace();
        }
    }

    public static void chill(int time)
    {
        try
        {
            Thread.sleep(time);
        }
        catch(Exception ex)
        {
        }
    }
}
