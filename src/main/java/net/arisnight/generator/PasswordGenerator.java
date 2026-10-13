package net.arisnight.generator;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Генератор паролей и парольных фраз на SecureRandom. */
public final class PasswordGenerator {
    private static final SecureRandom RND = new SecureRandom();

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+[]{};:,.?";
    private static final String AMBIGUOUS = "0OlI1|";

    // TODO: заменить на словарь из ресурсов (несколько тысяч слов); энтропия считается по размеру списка.
    private static final String[] WORDS = """
            amber cedar lamp orbit river summer north falcon pixel velvet harbor quartz maple signal ember
            tundra coral linen vector prism anchor birch canyon delta forest glacier meadow nebula ocean
            pebble raven saddle timber willow zephyr bridge candle dune echo flint garnet hazel island
            jasper kettle lantern marble nickel olive pepper rocket sierra thistle umber violet walnut
            yonder zenith copper breeze cobalt
            """.trim().split("\\s+");

    public record Options(int length, boolean lower, boolean upper, boolean digits,
                          boolean symbols, boolean noAmbiguous) {}

    public record Result(String value, double entropyBits) {}

    private PasswordGenerator() {}

    public static Result password(Options o) {
        List<String> sets = new ArrayList<>();
        if (o.lower()) sets.add(clean(LOWER, o));
        if (o.upper()) sets.add(clean(UPPER, o));
        if (o.digits()) sets.add(clean(DIGITS, o));
        if (o.symbols()) sets.add(SYMBOLS);
        if (sets.isEmpty()) throw new IllegalArgumentException("Выберите хотя бы один набор символов");
        if (o.length() < sets.size()) throw new IllegalArgumentException("Длина меньше числа выбранных наборов");

        String all = String.join("", sets);
        List<Character> chars = new ArrayList<>();
        for (String s : sets) chars.add(pick(s));          // минимум по одному символу из каждого набора
        while (chars.size() < o.length()) chars.add(pick(all));
        Collections.shuffle(chars, RND);

        StringBuilder sb = new StringBuilder();
        chars.forEach(sb::append);
        double bits = o.length() * (Math.log(all.length()) / Math.log(2));
        return new Result(sb.toString(), bits);
    }

    public static Result passphrase(int words, String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words; i++) {
            if (i > 0) sb.append(separator);
            sb.append(WORDS[RND.nextInt(WORDS.length)]);
        }
        double bits = words * (Math.log(WORDS.length) / Math.log(2));
        return new Result(sb.toString(), bits);
    }

    private static String clean(String s, Options o) {
        if (!o.noAmbiguous()) return s;
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) if (AMBIGUOUS.indexOf(c) < 0) sb.append(c);
        return sb.toString();
    }

    private static char pick(String s) {
        return s.charAt(RND.nextInt(s.length()));
    }
}