package net.sf.jftp;

import net.sf.jftp.config.Settings;

import java.applet.Applet;
import java.security.AccessController;
import java.security.PrivilegedAction;


public class JFtpApplet extends Applet {
	public JFtpApplet() {
		AccessController.doPrivileged((java.security.PrivilegedAction) () -> {
			net.sf.jftp.config.Settings.isStandalone = false;
			net.sf.jftp.JFtp.main(new String[0]);

			return new Object();
		});
	}

	public void init() {
	}
}
