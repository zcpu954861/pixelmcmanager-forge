package com.pixelmc.pixelmcwelcome.placeholder;

import com.pixelmc.pixelmcwelcome.config.WelcomeConfig;
import com.pixelmc.pixelmcwelcome.stats.PlayerStats;
import net.minecraft.server.level.ServerPlayer;

public record PlaceholderContext(ServerPlayer player, WelcomeConfig config, PlayerStats stats, long lastJoinDateEpochMillis) {
}
