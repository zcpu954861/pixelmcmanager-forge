package com.pixelmc.pixelmcmanager.stats;

public final class PlayerStats {
    public String name = "";
    public int joinCount = 0;
    public long firstJoinEpochMillis = 0L;
    public long lastJoinEpochMillis = 0L;
    public long totalOnlineMillis = 0L;

    public PlayerStats copy() {
        PlayerStats copy = new PlayerStats();
        copy.name = name;
        copy.joinCount = joinCount;
        copy.firstJoinEpochMillis = firstJoinEpochMillis;
        copy.lastJoinEpochMillis = lastJoinEpochMillis;
        copy.totalOnlineMillis = totalOnlineMillis;
        return copy;
    }
}
