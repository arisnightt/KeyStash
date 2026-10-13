package net.arisnight.service;

import net.arisnight.i18n.I18n;

import java.util.prefs.Preferences;

public final class Settings {
    public static final int[] CLIPBOARD_CHOICES = {10, 15, 30, 60, 120};
    public static final int DEFAULT_CLIPBOARD_SECONDS = 30;

    private static final String K_DARK = "dark";
    private static final String K_CLIPBOARD = "clipboardSeconds";
    private static final String K_LANGUAGE = "language";

    private final Preferences prefs = Preferences.userNodeForPackage(Settings.class);

    private boolean dark;
    private int clipboardSeconds;
    private String language;

    public Settings() {
        dark = prefs.getBoolean(K_DARK, false);
        clipboardSeconds = prefs.getInt(K_CLIPBOARD, DEFAULT_CLIPBOARD_SECONDS);
        if (clipboardSeconds < 5 || clipboardSeconds > 600) clipboardSeconds = DEFAULT_CLIPBOARD_SECONDS;
        language = prefs.get(K_LANGUAGE, I18n.SYSTEM);
        if (!I18n.SYSTEM.equals(language) && !I18n.LANGUAGES.contains(language)) language = I18n.SYSTEM;
    }

    public boolean dark() {
        return dark;
    }

    public int clipboardSeconds() {
        return clipboardSeconds;
    }

    public String language() {
        return language;
    }

    public void setDark(boolean dark) {
        this.dark = dark;
    }

    public void setClipboardSeconds(int seconds) {
        this.clipboardSeconds = seconds;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void save() {
        prefs.putBoolean(K_DARK, dark);
        prefs.putInt(K_CLIPBOARD, clipboardSeconds);
        prefs.put(K_LANGUAGE, language);
    }
}