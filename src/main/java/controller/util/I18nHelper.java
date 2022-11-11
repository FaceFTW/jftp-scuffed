package controller.util;

import controller.config.Settings;

import java.util.Locale;
import java.util.ResourceBundle;

//This class is effectively a way to easily unify all resource bundles across the application
public class I18nHelper {
	static final String UI_TEXT = "UIText";
	static final String LOG_TEXT = "LogText";
	static final String RESPONSES = "responses";
	/**
	 * Notepad++ regex replace to make en_GB language (screaming goat):
	 * It instead prepends strings with {} sub ins, unless it starts with a {}, in which it does nothing
	 * Find: ((?:.*)=)([^\{\}\n]*)
	 * Replace With: $1AAAA
	 */
	public static final Locale[] locales = {Locale.US, Locale.GERMAN, Locale.UK};
	// Can be changed at any point; represents a locale we don't support as a placeholder.
	public static final Locale unsetLocale = Locale.CANADA_FRENCH;
	public static final Locale fallbackLocale = Locale.US;
	private static ResourceBundle uiResources = ResourceBundle.getBundle(UI_TEXT);
	private static ResourceBundle logResources = ResourceBundle.getBundle(LOG_TEXT);
	private static ResourceBundle responseCodeBundle = ResourceBundle.getBundle(RESPONSES, Locale.US);
	static Locale active = Locale.US;

	static {
		setLocale(Settings.getLocale());
		System.out.println("Using locale: " + active.toLanguageTag());
	}


	public static final String getUIString(String key) {
		return uiResources.getString(key);
	}

	public static final String getLogString(String key) {
		return logResources.getString(key);
	}

	public static String getResponseCodeString(String key) {
		return responseCodeBundle.getString(key);
	}

	public static void setLocale(Locale newLocale) {
		active = newLocale;
		uiResources = ResourceBundle.getBundle(UI_TEXT, newLocale);
		logResources = ResourceBundle.getBundle(LOG_TEXT, newLocale);
		responseCodeBundle = ResourceBundle.getBundle(RESPONSES, newLocale);
	}

	public static boolean hasLocale(Locale l) {
		for (Locale t : locales)
			if (t.equals(l)) return true;
		return false;
	}
}
