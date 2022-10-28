package net.sf.jftp.util;

import java.util.Locale;
import java.util.ResourceBundle;

//This class is effectively a way to easily unify all resource bundles across the application
public class I18nHelper {
	private static final ResourceBundle uiResources = ResourceBundle.getBundle("UIText");
	private static final ResourceBundle logResources = ResourceBundle.getBundle("LogText");

	private static ResourceBundle bundle = ResourceBundle.getBundle("responses", Locale.US);

	static Locale[] locales = {Locale.US};

	public static final String getUIString(String key) {
		return uiResources.getString(key);
	}

	public static final String getLogString(String key) {
		return logResources.getString(key);
	}

	public static String getString(String key) {
		return bundle.getString(key);
	}

	public static void setLocale(Locale l) {
		I18nHelper.bundle = ResourceBundle.getBundle("responses", l);
	}

	public static boolean hasLocale(Locale l) {
		for(Locale t : locales)
			if(t.equals(l))
			return true;
		return false;
	}
}
