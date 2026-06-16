package com.pixelmc.pixelmcmanager.event;

import com.pixelmc.pixelmcmanager.config.WelcomeConfigManager;
import com.pixelmc.pixelmcmanager.maintenance.StopServerScheduler;
import com.pixelmc.pixelmcmanager.stats.PlayerLoginSnapshot;
import com.pixelmc.pixelmcmanager.stats.PlayerStatsStore;
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
    private final StopServerScheduler stopServerScheduler;
    private long lastStatsCheckpointMillis = 0L;

    public WelcomeEventHandler(WelcomeConfigManager configManager, PlayerStatsStore statsStore, DelayedMessageScheduler scheduler, StopServerScheduler stopServerScheduler) {
        this.configManager = configManager;
        this.statsStore = statsStore;
        this.scheduler = scheduler;
        this.stopServerScheduler = stopServerScheduler;
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        configManager.loadOrCreate();
        statsStore.load(event.getServer());
        lastStatsCheckpointMillis = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (stopServerScheduler.rejectMaintenanceJoin(player)) {
            return;
        }

        long nowMillis = System.currentTimeMillis();
        statsStore.ensureLoaded(player.server);
        PlayerLoginSnapshot snapshot = statsStore.recordLogin(player, nowMillis);
        statsStore.save();
        scheduler.schedule(player, snapshot.firstJoin(), snapshot.previousLastJoinEpochMillis());
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        scheduler.cancel(player.getUUID());
        statsStore.recordLogout(player, System.currentTimeMillis());
        statsStore.save();
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        stopServerScheduler.onServerTick(event);
        scheduler.onServerTick(event);
        if (event.phase != TickEvent.Phase.END || !statsStore.hasOnlineSessions()) {
            return;
        }

        long nowMillis = System.currentTimeMillis();
        long intervalMillis = configManager.getConfig().statsAutoSaveSeconds * 1_000L;
        if (lastStatsCheckpointMillis <= 0L || nowMillis - lastStatsCheckpointMillis >= intervalMillis) {
            statsStore.checkpointOnlinePlayers(event.getServer(), nowMillis, true);
            lastStatsCheckpointMillis = nowMillis;
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        statsStore.settleAll(System.currentTimeMillis());
        statsStore.save();
    }
}
