package com.pixelmc.pixelmcwelcome.event;

import com.pixelmc.pixelmcwelcome.config.WelcomeConfig;
import com.pixelmc.pixelmcwelcome.config.WelcomeConfigManager;
import com.pixelmc.pixelmcwelcome.stats.PlayerLoginSnapshot;
import com.pixelmc.pixelmcwelcome.stats.PlayerStatsStore;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class WelcomeEventHandler {
    private final WelcomeConfigManager configManager;
    private final PlayerStatsStore statsStore;
    private final DelayedMessageScheduler scheduler;

    public WelcomeEventHandler(WelcomeConfigManager configManager, PlayerStatsStore statsStore, DelayedMessageScheduler scheduler) {
        this.configManager = configManager;
        this.statsStore = statsStore;
        this.scheduler = scheduler;
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        configManager.loadOrCreate();
        statsStore.load(event.getServer());
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        long nowMillis = System.currentTimeMillis();
        statsStore.ensureLoaded(player.server);
        PlayerLoginSnapshot snapshot = statsStore.recordLogin(player, nowMillis);
        scheduler.schedule(player, snapshot.firstJoin(), snapshot.previousLastJoinEpochMillis());
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        scheduler.cancel(player.getUUID());
        statsStore.recordLogout(player, System.currentTimeMillis());
        WelcomeConfig config = configManager.getConfig();
        if (config.saveOnLogout) {
            statsStore.save();
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        scheduler.onServerTick(event);
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        statsStore.settleAll(System.currentTimeMillis());
        if (configManager.getConfig().saveOnServerStop) {
            statsStore.save();
        }
    }
}
