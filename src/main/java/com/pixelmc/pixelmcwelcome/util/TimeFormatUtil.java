package com.pixelmc.pixelmcwelcome.util;

public final class TimeFormatUtil {
    private TimeFormatUtil() {
    }

    public static String formatFriendly(long millis) {
        long totalMinutes = Math.max(0L, millis) / 60_000L;
        long days = totalMinutes / (24L * 60L);
        long hours = (totalMinutes % (24L * 60L)) / 60L;
        long minutes = totalMinutes % 60L;

        StringBuilder result = new StringBuilder();
        if (days > 0L) {
            result.append(days).append("天");
        }
        if (hours > 0L) {
            result.append(hours).append("小时");
        }
        if (minutes > 0L || result.length() == 0) {
            result.append(minutes).append("分钟");
        }
        return result.toString();
    }
}
