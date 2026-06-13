package com.pixelmc.pixelmcwelcome.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.pixelmc.pixelmcwelcome.config.WelcomeConfig;
import com.pixelmc.pixelmcwelcome.config.WelcomeConfigManager;
import com.pixelmc.pixelmcwelcome.placeholder.PlaceholderContext;
import com.pixelmc.pixelmcwelcome.placeholder.PlaceholderResolver;
import com.pixelmc.pixelmcwelcome.stats.PlayerStats;
import com.pixelmc.pixelmcwelcome.stats.PlayerStatsStore;
import com.pixelmc.pixelmcwelcome.text.TextTemplateParser;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public final class PixelMCWelcomeCommands {
    private final WelcomeConfigManager configManager;
    private final PlayerStatsStore statsStore;
    private final PlaceholderResolver placeholderResolver;
    private final TextTemplateParser textParser;

    public PixelMCWelcomeCommands(WelcomeConfigManager configManager, PlayerStatsStore statsStore, PlaceholderResolver placeholderResolver, TextTemplateParser textParser) {
        this.configManager = configManager;
        this.statsStore = statsStore;
        this.placeholderResolver = placeholderResolver;
        this.textParser = textParser;
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pixelmcwelcome")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("reload")
                        .executes(context -> reload(context.getSource())))
                .then(Commands.literal("preview")
                        .executes(context -> preview(context.getSource(), false))
                        .then(Commands.literal("first")
                                .executes(context -> preview(context.getSource(), true)))
                        .then(Commands.literal("returning")
                                .executes(context -> preview(context.getSource(), false)))));
    }

    private int reload(CommandSourceStack source) {
        WelcomeConfigManager.LoadResult result = configManager.loadOrCreate();
        if (result.success()) {
            source.sendSuccess(() -> Component.literal("PixelMC Welcome 配置已重新加载。"), false);
            return Command.SINGLE_SUCCESS;
        }

        source.sendFailure(Component.literal("PixelMC Welcome 配置加载失败，已保留旧配置：" + result.message()));
        return 0;
    }

    private int preview(CommandSourceStack source, boolean first) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("该命令需要玩家执行。"));
            return 0;
        }

        WelcomeConfig config = configManager.getConfig();
        List<String> templates = first ? config.firstJoinMessages : config.returningMessages;
        if (templates.isEmpty()) {
            source.sendFailure(Component.literal("PixelMC Welcome 当前预览消息为空。"));
            return 0;
        }

        PlayerStats stats = statsStore.getStats(player.getUUID());
        if (stats == null) {
            stats = new PlayerStats();
            stats.name = player.getGameProfile().getName();
            stats.joinCount = first ? 1 : 0;
            long now = System.currentTimeMillis();
            stats.firstJoinEpochMillis = now;
            stats.lastJoinEpochMillis = now;
        }

        PlaceholderContext placeholderContext = new PlaceholderContext(player, config, stats, stats.lastJoinEpochMillis);
        for (String template : templates) {
            String resolved = placeholderResolver.resolve(template, placeholderContext);
            player.sendSystemMessage(textParser.parse(resolved, config));
        }
        source.sendSuccess(() -> Component.literal("PixelMC Welcome 预览已发送。"), false);
        return Command.SINGLE_SUCCESS;
    }
}
