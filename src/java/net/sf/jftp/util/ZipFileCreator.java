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
package net.sf.jftp.util;

import net.sf.jftp.*;
import net.sf.jftp.net.*;
import net.sf.jftp.system.logging.Log;
import net.sf.jftp.util.*;

import java.io.*;

import java.util.zip.*;
  
 
public class ZipFileCreator
{
    private ZipOutputStream z; 

    public ZipFileCreator(String[] files, String path, String name) 
                   throws Exception
    {
        z = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(path +
                                                                              name)));
        perform(files, path, "");
        z.finish();
        z.flush();
        z.close();
    }

    private void perform(String[] files, String path, String offset)
    {
        byte[] buf = new byte[4096];

        for(int i = 0; i < files.length; i++)
        {
            try
            {
                File f = new File(path + offset + files[i]);
                BufferedInputStream in = null;

                if(f.exists() && !f.isDirectory())
                {
                    in = new BufferedInputStream(new FileInputStream(path +
                                                                     offset +
                                                                     files[i]));
                }
                else if(f.exists())
                {
                    if(!files[i].endsWith("/"))
                    {
                        files[i] = files[i] + "/";
                    }

                    perform(f.list(), path, offset + files[i]);
                }

                ZipEntry tmp = new ZipEntry(offset + files[i]);
                z.putNextEntry(tmp);

                int len = 0;

                while((in != null) && (len != StreamTokenizer.TT_EOF))
                {
                    len = in.read(buf);

                    if(len == StreamTokenizer.TT_EOF)
                    {
                        break;
                    }

                    z.write(buf, 0, len);
                }

                z.closeEntry();
            }
            catch(Exception ex)
            {
                Log.debug("Skipping a file (no permission?)");
            }
        }
    }
}
