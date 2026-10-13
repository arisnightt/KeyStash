package net.arisnight.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public final class I18n {
    public static final String SYSTEM = "system";
    public static final List<String> LANGUAGES = List.of("en", "ru", "fr", "de");

    private static final String DEFAULT = "en";
    private static final Properties FALLBACK = load(DEFAULT);

    private static Properties current = FALLBACK;
    private static Locale locale = Locale.ENGLISH;

    private I18n() {}

    public static void setLanguage(String code) {
        String lang = resolve(code);
        current = lang.equals(DEFAULT) ? FALLBACK : load(lang);
        locale = Locale.forLanguageTag(lang);
    }

    public static String resolve(String code) {
        String c = (code == null || SYSTEM.equals(code)) ? Locale.getDefault().getLanguage() : code;
        return LANGUAGES.contains(c) ? c : DEFAULT;
    }

    public static Locale locale() {
        return locale;
    }

    public static String t(String key, Object... args) {
        String v = current.getProperty(key);
        if (v == null) v = FALLBACK.getProperty(key, key);
        return args.length == 0 ? v : String.format(locale, v, args);
    }

    private static Properties load(String lang) {
        Properties p = new Properties();
        try (InputStream in = I18n.class.getResourceAsStream("/i18n/messages_" + lang + ".properties")) {
            if (in != null) p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException ignored) {
        }
        return p;
    }
}