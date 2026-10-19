package com.example.donutpay;

/** Tracks a running session total (money in minus money out) and formats it like -102.6M. */
public final class LiveTracker {
    private static double net = 0;

    private LiveTracker() {}

    public static void add(double amount)      { net += amount; }
    public static void subtract(double amount) { net -= amount; }
    public static void reset()                 { net = 0; }
    public static double get()                 { return net; }

    public static String format(double v) {
        if (v == 0) return "0";
        double a = Math.abs(v);
        String sign = v < 0 ? "-" : "+";
        if (a >= 1_000_000_000_000.0) return String.format("%s%.1fT", sign, a / 1_000_000_000_000.0);
        if (a >= 1_000_000_000)       return String.format("%s%.1fB", sign, a / 1_000_000_000);
        if (a >= 1_000_000)           return String.format("%s%.1fM", sign, a / 1_000_000);
        if (a >= 1_000)               return String.format("%s%.1fK", sign, a / 1_000);
        return String.format("%s%.0f", sign, a);
    }

    /** Unsigned version for notifications, e.g. 3500000 -> 3.5M */
    public static String formatAmount(double v) {
        double a = Math.abs(v);
        if (a >= 1_000_000_000_000.0) return String.format("%.1fT", a / 1_000_000_000_000.0);
        if (a >= 1_000_000_000)       return String.format("%.1fB", a / 1_000_000_000);
        if (a >= 1_000_000)           return String.format("%.1fM", a / 1_000_000);
        if (a >= 1_000)               return String.format("%.1fK", a / 1_000);
        return String.format("%.0f", a);
    }
}
