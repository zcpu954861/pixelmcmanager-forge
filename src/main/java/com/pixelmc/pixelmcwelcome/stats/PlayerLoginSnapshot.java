package com.pixelmc.pixelmcwelcome.stats;

public record PlayerLoginSnapshot(PlayerStats stats, boolean firstJoin, long previousLastJoinEpochMillis) {
}
