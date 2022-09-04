package net.sf.jftp.gui.base;

import net.sf.jftp.JFtp;
import net.sf.jftp.config.Settings;


public class LogFlusher implements Runnable
{
    private Thread runner;

    public LogFlusher()
    {
        runner = new Thread(this);

        if(Settings.useLogFlusher)
        {
            runner.start();
        }
    }

    public void run()
    {
        while(true)
        {
            try
            {
                runner.sleep(Settings.logFlushInterval);
            }
            catch(InterruptedException ex)
            {
                ex.printStackTrace();
            }

            //System.out.println("logflush");
            JFtp.statusP.jftp.ensureLogging();
        }
    }
}
