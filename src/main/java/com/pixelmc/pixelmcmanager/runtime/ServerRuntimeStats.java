package com.pixelmc.pixelmcmanager.runtime;

public final class ServerRuntimeStats {
    private long serverStartedAtMillis = System.currentTimeMillis();

    public void markServerStarted(long nowMillis) {
        serverStartedAtMillis = nowMillis;
    }

    public long uptimeMillis(long nowMillis) {
        return Math.max(0L, nowMillis - serverStartedAtMillis);
    }
}
