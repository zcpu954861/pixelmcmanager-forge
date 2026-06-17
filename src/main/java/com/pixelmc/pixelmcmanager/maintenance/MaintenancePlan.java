package com.pixelmc.pixelmcmanager.maintenance;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public final class MaintenancePlan {
    private final long createdAtMillis;
    private final long startTimeMillis;
    private final Duration originalDelay;
    private final Set<Integer> sentMinuteWarnings = new HashSet<>();
    private final Set<Integer> sentSecondWarnings = new HashSet<>();

    public MaintenancePlan(long createdAtMillis, Duration originalDelay) {
        this.createdAtMillis = createdAtMillis;
        this.originalDelay = originalDelay;
        this.startTimeMillis = createdAtMillis + originalDelay.toMillis();
    }

    public long createdAtMillis() {
        return createdAtMillis;
    }

    public long startTimeMillis() {
        return startTimeMillis;
    }

    public Duration originalDelay() {
        return originalDelay;
    }

    public boolean markMinuteWarning(int minutes) {
        return sentMinuteWarnings.add(minutes);
    }

    public boolean markSecondWarning(int seconds) {
        return sentSecondWarnings.add(seconds);
    }
}
