package it.bugbuster.asilapp.utils;

import java.util.Locale;

public class LanguageUtils {
    public static String getCurrentLanguage() {
        return Locale.getDefault().getLanguage();
    }
}

