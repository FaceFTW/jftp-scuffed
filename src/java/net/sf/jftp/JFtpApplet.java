package net.sf.jftp;

import java.applet.Applet;
import java.security.AccessController;
import java.security.PrivilegedAction;

import net.sf.jftp.config.Settings;


public class JFtpApplet extends Applet
{
    public JFtpApplet()
    {
        AccessController.doPrivileged(new PrivilegedAction()
            {
                public Object run()
                {
                	Settings.isStandalone = false;
                    JFtp.main(new String[0]);

                    return new Object();
                }
            });
    }

    public void init()
    {
    }
}
