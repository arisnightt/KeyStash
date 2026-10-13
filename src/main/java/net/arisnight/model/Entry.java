package net.arisnight.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Запись хранилища. TODO: добавить уникальный id (UUID), не зависящий от названия.
 */
public record Entry(
        String name,
        String login,
        String password,
        String url,
        String category,
        boolean favorite,
        List<String> tags,
        int strength,
        String note,
        LocalDate created,
        LocalDate modified) {

    public boolean matches(String q) {
        return contains(name, q) || contains(login, q) || contains(url, q)
                || contains(category, q) || contains(note, q)
                || tags.stream().anyMatch(t -> contains(t, q));
    }

    private static boolean contains(String s, String q) {
        return s != null && s.toLowerCase().contains(q);
    }
}