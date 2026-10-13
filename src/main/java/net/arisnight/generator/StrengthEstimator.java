package net.arisnight.generator;

import static net.arisnight.i18n.I18n.t;

public final class StrengthEstimator {
    private StrengthEstimator() {}

    public static int score(String password) {
        if (password == null || password.isEmpty()) return 0;
        boolean lower = false, upper = false, digit = false, other = false;
        for (char c : password.toCharArray()) {
            if (c >= 'a' && c <= 'z') lower = true;
            else if (c >= 'A' && c <= 'Z') upper = true;
            else if (c >= '0' && c <= '9') digit = true;
            else other = true;
        }
        int pool = (lower ? 26 : 0) + (upper ? 26 : 0) + (digit ? 10 : 0) + (other ? 33 : 0);
        double bits = password.length() * (Math.log(pool) / Math.log(2));
        return (int) Math.min(100, Math.round(bits));
    }

    public static String label(int score) {
        return t(score >= 90 ? "strength.excellent" : score >= 75 ? "strength.good"
                : score >= 50 ? "strength.medium" : "strength.weak");
    }
}