package com.pixelmc.pixelmcwelcome.stats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerStatsStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Type FILE_TYPE = new TypeToken<StatsFile>() {
    }.getType();

    private final Logger logger;
    private final Map<UUID, PlayerStats> players = new LinkedHashMap<>();
    private final Map<UUID, Long> sessionStarts = new HashMap<>();
    private Path dataFile;
    private boolean loaded = false;

    public PlayerStatsStore(Logger logger) {
        this.logger = logger;
    }

    public synchronized void load(MinecraftServer server) {
        dataFile = server.getWorldPath(LevelResource.ROOT).resolve("pixelmcwelcome").resolve("player_stats.json");
        players.clear();
        sessionStarts.clear();

        try {
            Files.createDirectories(dataFile.getParent());
            if (Files.notExists(dataFile)) {
                loaded = true;
                save();
                return;
            }

            String json = Files.readString(dataFile, StandardCharsets.UTF_8);
            StatsFile statsFile = GSON.fromJson(json, FILE_TYPE);
            if (statsFile == null) {
                throw new JsonParseException("Stats file is empty.");
            }
            if (statsFile.players != null) {
                for (Map.Entry<String, PlayerStats> entry : statsFile.players.entrySet()) {
                    try {
                        UUID uuid = UUID.fromString(entry.getKey());
                        PlayerStats stats = entry.getValue() == null ? new PlayerStats() : entry.getValue();
                        players.put(uuid, stats);
                    } catch (IllegalArgumentException ignored) {
                        logger.warn("Ignoring invalid player UUID in PixelMC Welcome stats: {}", entry.getKey());
                    }
                }
            }
            loaded = true;
        } catch (Exception exception) {
            logger.error("Failed to load PixelMC Welcome player stats from {}.", dataFile, exception);
            backupBrokenStatsFile();
            players.clear();
            loaded = true;
        }
    }

    public synchronized void ensureLoaded(MinecraftServer server) {
        if (!loaded) {
            load(server);
        }
    }

    public synchronized PlayerLoginSnapshot recordLogin(ServerPlayer player, long nowMillis) {
        UUID uuid = player.getUUID();
        PlayerStats stats = players.computeIfAbsent(uuid, ignored -> {
            PlayerStats created = new PlayerStats();
            created.firstJoinEpochMillis = nowMillis;
            return created;
        });

        boolean firstJoin = stats.joinCount == 0;
        long previousLastJoin = stats.lastJoinEpochMillis;
        if (stats.firstJoinEpochMillis <= 0L) {
            stats.firstJoinEpochMillis = nowMillis;
        }
        stats.name = player.getGameProfile().getName();
        stats.joinCount++;
        stats.lastJoinEpochMillis = nowMillis;
        sessionStarts.put(uuid, nowMillis);
        return new PlayerLoginSnapshot(stats.copy(), firstJoin, previousLastJoin);
    }

    public synchronized void recordLogout(ServerPlayer player, long nowMillis) {
        settleSession(player.getUUID(), nowMillis);
    }

    public synchronized PlayerStats getStats(UUID uuid) {
        PlayerStats stats = players.get(uuid);
        return stats == null ? null : stats.copy();
    }

    public synchronized void settleAll(long nowMillis) {
        for (UUID uuid : sessionStarts.keySet().toArray(UUID[]::new)) {
            settleSession(uuid, nowMillis);
        }
    }

    private void settleSession(UUID uuid, long nowMillis) {
        Long startedAt = sessionStarts.remove(uuid);
        PlayerStats stats = players.get(uuid);
        if (startedAt != null && stats != null) {
            stats.totalOnlineMillis += Math.max(0L, nowMillis - startedAt);
        }
    }

    public synchronized void save() {
        if (dataFile == null) {
            return;
        }

        try {
            Files.createDirectories(dataFile.getParent());
            StatsFile statsFile = new StatsFile();
            statsFile.players = new LinkedHashMap<>();
            for (Map.Entry<UUID, PlayerStats> entry : players.entrySet()) {
                statsFile.players.put(entry.getKey().toString(), entry.getValue());
            }

            Path tempFile = dataFile.resolveSibling(dataFile.getFileName() + ".tmp");
            Files.writeString(tempFile, GSON.toJson(statsFile), StandardCharsets.UTF_8);
            try {
                Files.move(tempFile, dataFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicMoveFailed) {
                Files.move(tempFile, dataFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            logger.error("Failed to save PixelMC Welcome player stats to {}.", dataFile, exception);
        }
    }

    private void backupBrokenStatsFile() {
        if (dataFile == null || Files.notExists(dataFile)) {
            return;
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path backup = dataFile.resolveSibling("player_stats.json.broken-" + timestamp);
        try {
            Files.move(dataFile, backup, StandardCopyOption.REPLACE_EXISTING);
            logger.warn("Backed up broken PixelMC Welcome stats file to {}.", backup);
        } catch (IOException exception) {
            logger.error("Failed to back up broken PixelMC Welcome stats file {}.", dataFile, exception);
        }
    }

    private static final class StatsFile {
        int version = 1;
        Map<String, PlayerStats> players = new LinkedHashMap<>();
    }
}
