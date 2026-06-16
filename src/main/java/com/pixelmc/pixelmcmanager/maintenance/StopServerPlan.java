package com.pixelmc.pixelmcmanager.maintenance;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public final class StopServerPlan {
    private final long createdAtMillis;
    private final long kickTimeMillis;
    private final long stopTimeMillis;
    private final Duration originalDelay;
    private final Set<Integer> sentMinuteWarnings = new HashSet<>();
    private final Set<Integer> sentSecondWarnings = new HashSet<>();
    private boolean maintenanceActive = false;

    public StopServerPlan(long createdAtMillis, Duration originalDelay) {
        this.createdAtMillis = createdAtMillis;
        this.originalDelay = originalDelay;
        this.kickTimeMillis = createdAtMillis + originalDelay.toMillis();
        this.stopTimeMillis = kickTimeMillis + 15_000L;
    }

    public long createdAtMillis() {
        return createdAtMillis;
    }

    public long kickTimeMillis() {
        return kickTimeMillis;
    }

    public long stopTimeMillis() {
        return stopTimeMillis;
    }

    public Duration originalDelay() {
        return originalDelay;
    }

    public boolean maintenanceActive() {
        return maintenanceActive;
    }

    public void activateMaintenance() {
        maintenanceActive = true;
    }

    public boolean markMinuteWarning(int minutes) {
        return sentMinuteWarnings.add(minutes);
    }

    public boolean markSecondWarning(int seconds) {
        return sentSecondWarnings.add(seconds);
    }
}
