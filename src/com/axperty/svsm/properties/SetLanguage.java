package com.axperty.svsm.properties;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class SetLanguage {

    private static ResourceBundle bundle;

    public static ResourceBundle getBundle() {
        if (bundle == null) {
            Locale locale = Locale.getDefault();
            String languageCode = locale.getLanguage();
            String countryCode = locale.getCountry();
            String bundleName = "com.axperty.svsm.lang." + languageCode + "_" + countryCode;
            try {
                bundle = ResourceBundle.getBundle(bundleName, locale);
            } catch (MissingResourceException e) {
                System.err.println("Resource bundle not found for locale: " + locale + ". Using default English bundle.");
                bundle = ResourceBundle.getBundle("com.axperty.svsm.lang.en_us", Locale.ENGLISH);
            }
        }
        return bundle;
    }
}