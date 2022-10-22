package net.sf.jftp.util;

import java.util.ResourceBundle;

//This class is effectively a way to easily unify all resource bundles across the application
public class I18nHelper {
	private static final ResourceBundle uiResources = ResourceBundle.getBundle("UIText");
	private static final ResourceBundle logResources = ResourceBundle.getBundle("LogText");

	public static final String getUIString(String key){
		return uiResources.getString(key);
	}

	public static final String getLogString(String key){
		return logResources.getString(key);
	}
}
