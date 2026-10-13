package net.arisnight.generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static net.arisnight.i18n.I18n.t;

public final class PasswordGenerator {
    private static final SecureRandom RND = new SecureRandom();

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+[]{};:,.?";
    private static final String AMBIGUOUS = "0OlI1|";

    private static final String WORDLIST_RESOURCE = "/generator/wordlist.txt";
    private static final int MIN_WORDS = 1000;

    public record Options(int length, boolean lower, boolean upper, boolean digits,
                          boolean symbols, boolean noAmbiguous) {}

    public record Result(String value, double entropyBits) {}

    private PasswordGenerator() {}

    private static final class Words {
        static final String[] LIST = load();

        private static String[] load() {
            try (InputStream in = PasswordGenerator.class.getResourceAsStream(WORDLIST_RESOURCE)) {
                if (in == null) {
                    throw new IllegalStateException("Wordlist not found: " + WORDLIST_RESOURCE);
                }
                Set<String> uniq = new LinkedHashSet<>();
                try (BufferedReader r = new BufferedReader(
                        new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = r.readLine()) != null) {
                        line = line.strip();
                        if (line.isEmpty() || line.startsWith("#")) continue;
                        int tab = line.lastIndexOf('\t');
                        if (tab >= 0) line = line.substring(tab + 1).strip();
                        if (!line.isEmpty()) uniq.add(line);
                    }
                }
                if (uniq.size() < MIN_WORDS) {
                    throw new IllegalStateException("Wordlist too small: " + uniq.size());
                }
                return uniq.toArray(new String[0]);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot read wordlist", e);
            }
        }
    }

    public static Result password(Options o) {
        List<String> sets = new ArrayList<>();
        if (o.lower()) sets.add(clean(LOWER, o));
        if (o.upper()) sets.add(clean(UPPER, o));
        if (o.digits()) sets.add(clean(DIGITS, o));
        if (o.symbols()) sets.add(SYMBOLS);
        if (sets.isEmpty()) throw new IllegalArgumentException(t("gen.err.noSets"));
        if (o.length() < sets.size()) throw new IllegalArgumentException(t("gen.err.shortLength"));

        String all = String.join("", sets);
        List<Character> chars = new ArrayList<>();
        for (String s : sets) chars.add(pick(s));
        while (chars.size() < o.length()) chars.add(pick(all));
        Collections.shuffle(chars, RND);

        StringBuilder sb = new StringBuilder();
        chars.forEach(sb::append);
        double bits = o.length() * (Math.log(all.length()) / Math.log(2));
        return new Result(sb.toString(), bits);
    }

    public static Result passphrase(int words, String separator) {
        String[] list = Words.LIST;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words; i++) {
            if (i > 0) sb.append(separator);
            sb.append(list[RND.nextInt(list.length)]);
        }
        double bits = words * (Math.log(list.length) / Math.log(2));
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