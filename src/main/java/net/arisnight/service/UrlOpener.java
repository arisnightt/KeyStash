package net.arisnight.service;

import javafx.application.HostServices;

import java.net.URI;

/** Открывает URL во внешнем браузере. Разрешены только http и https. */
public final class UrlOpener {
    private UrlOpener() {}

    public static boolean open(HostServices host, String raw) {
        if (raw == null || raw.isBlank()) return false;
        try {
            String s = raw.trim();
            if (!s.contains("://")) s = "https://" + s;
            URI uri = URI.create(s);
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                return false;
            }
            if (uri.getHost() == null) return false;
            host.showDocument(uri.toString());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}