package com.pixelmc.pixelmcmanager.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.pixelmc.pixelmcmanager.config.WelcomeConfig;
import com.pixelmc.pixelmcmanager.config.WelcomeConfigManager;
import com.pixelmc.pixelmcmanager.maintenance.StopServerScheduler;
import com.pixelmc.pixelmcmanager.maintenance.TimeArgumentParser;
import com.pixelmc.pixelmcmanager.placeholder.PlaceholderContext;
import com.pixelmc.pixelmcmanager.placeholder.PlaceholderResolver;
import com.pixelmc.pixelmcmanager.stats.PlayerStats;
import com.pixelmc.pixelmcmanager.stats.PlayerStatsEntry;
import com.pixelmc.pixelmcmanager.stats.PlayerStatsStore;
import com.pixelmc.pixelmcmanager.text.TextTemplateParser;
import com.pixelmc.pixelmcmanager.util.TimeFormatUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class PixelMCManagerCommands {
    private final WelcomeConfigManager configManager;
    private final PlayerStatsStore statsStore;
    private final PlaceholderResolver placeholderResolver;
    private final TextTemplateParser textParser;
    private final StopServerScheduler stopServerScheduler;

    public PixelMCManagerCommands(WelcomeConfigManager configManager, PlayerStatsStore statsStore, PlaceholderResolver placeholderResolver, TextTemplateParser textParser, StopServerScheduler stopServerScheduler) {
        this.configManager = configManager;
        this.statsStore = statsStore;
        this.placeholderResolver = placeholderResolver;
        this.textParser = textParser;
        this.stopServerScheduler = stopServerScheduler;
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(managerRoot());
    }

    private com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> managerRoot() {
        return Commands.literal("pixelmcmanager")
                .requires(source -> source.hasPermission(2))
                .executes(context -> help(context.getSource()))
                .then(Commands.literal("reload")
                        .executes(context -> reload(context.getSource())))
                .then(Commands.literal("preview")
                        .executes(context -> preview(context.getSource(), false))
                        .then(Commands.literal("first")
                                .executes(context -> preview(context.getSource(), true)))
                        .then(Commands.literal("returning")
                                .executes(context -> preview(context.getSource(), false))))
                .then(Commands.literal("logincount")
                        .executes(context -> listLoginCounts(context.getSource()))
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    statsStore.ensureLoaded(context.getSource().getServer());
                                    return SharedSuggestionProvider.suggest(statsStore.listKnownPlayerNames(), builder);
                                })
                                .executes(context -> showLoginCount(context.getSource(), StringArgumentType.getString(context, "player")))))
                .then(Commands.literal("logintime")
                        .executes(context -> listLoginTimes(context.getSource()))
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    statsStore.ensureLoaded(context.getSource().getServer());
                                    return SharedSuggestionProvider.suggest(statsStore.listKnownPlayerNames(), builder);
                                })
                                .executes(context -> showLoginTime(context.getSource(), StringArgumentType.getString(context, "player")))))
                .then(Commands.literal("stopserver")
                        .requires(source -> source.hasPermission(4))
                        .then(Commands.argument("time", StringArgumentType.word())
                                .executes(context -> stopServer(context.getSource(), StringArgumentType.getString(context, "time")))));
    }

    private int help(CommandSourceStack source) {
        send(source, "&7❰ &b&l✦ &f&lPixelMC Manager &e&l✦ &7❱");
        send(source, "&b/pixelmcmanager reload");
        send(source, "&b/pixelmcmanager preview [first|returning]");
        send(source, "&b/pixelmcmanager logincount [player]");
        send(source, "&d/pixelmcmanager logintime [player]");
        send(source, "&c/pixelmcmanager stopserver <time>");
        return Command.SINGLE_SUCCESS;
    }

    private int stopServer(CommandSourceStack source, String input) {
        TimeArgumentParser.Result parsed = TimeArgumentParser.parse(input);
        if (!parsed.success()) {
            source.sendFailure(Component.literal(parsed.errorMessage()));
            return 0;
        }

        StopServerScheduler.ScheduleResult result = stopServerScheduler.schedule(source.getServer(), parsed.duration());
        if (!result.success()) {
            source.sendFailure(Component.literal(result.message()));
            return 0;
        }

        source.sendSuccess(() -> Component.literal(result.message()), false);
        return Command.SINGLE_SUCCESS;
    }

    private int reload(CommandSourceStack source) {
        WelcomeConfigManager.LoadResult result = configManager.loadOrCreate();
        if (result.success()) {
            source.sendSuccess(() -> Component.literal("PixelMC Manager 配置已重新加载。"), false);
            return Command.SINGLE_SUCCESS;
        }

        source.sendFailure(Component.literal("PixelMC Manager 配置加载失败，已保留旧配置：" + result.message()));
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
            source.sendFailure(Component.literal("PixelMC Manager 当前预览消息为空。"));
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
        source.sendSuccess(() -> Component.literal("PixelMC Manager 预览已发送。"), false);
        return Command.SINGLE_SUCCESS;
    }

    private int listLoginCounts(CommandSourceStack source) {
        checkpointForQuery(source);
        List<PlayerStatsEntry> entries = statsStore.listAll().stream()
                .sorted(Comparator
                        .comparingInt((PlayerStatsEntry entry) -> entry.stats().joinCount).reversed()
                        .thenComparing((PlayerStatsEntry entry) -> entry.stats().lastJoinEpochMillis, Comparator.reverseOrder())
                        .thenComparing(entry -> safeName(entry.stats()), String.CASE_INSENSITIVE_ORDER))
                .toList();

        send(source, "&7❰ &b&l✦ &f&lPixelMC 登录次数 &e&l✦ &7❱");
        if (entries.isEmpty()) {
            send(source, "&7暂无玩家记录。");
            return Command.SINGLE_SUCCESS;
        }

        for (int index = 0; index < entries.size(); index++) {
            PlayerStats stats = entries.get(index).stats();
            send(source, "&8#" + (index + 1) + " &e" + safeName(stats) + " &7| &f登录 &a" + stats.joinCount + " &7次 | &f上次 &b" + formatDateTime(stats.lastJoinEpochMillis));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int showLoginCount(CommandSourceStack source, String playerName) {
        checkpointForQuery(source);
        Optional<PlayerStatsEntry> entry = statsStore.findByName(playerName);
        if (entry.isEmpty()) {
            send(source, "&c没有找到玩家 " + playerName + " 的记录。");
            return 0;
        }

        PlayerStats stats = entry.get().stats();
        send(source, "&7❰ &b&l✦ &f&lPixelMC 登录次数 &e&l✦ &7❱");
        send(source, "&f玩家：&e" + safeName(stats));
        send(source, "&fUUID：&7" + entry.get().uuid());
        send(source, "&f登录次数：&a" + stats.joinCount);
        send(source, "&f首次登录：&b" + formatDateTime(stats.firstJoinEpochMillis));
        send(source, "&f上次登录：&b" + formatDateTime(stats.lastJoinEpochMillis));
        return Command.SINGLE_SUCCESS;
    }

    private int listLoginTimes(CommandSourceStack source) {
        checkpointForQuery(source);
        List<PlayerStatsEntry> entries = statsStore.listAll().stream()
                .sorted(Comparator
                        .comparingLong((PlayerStatsEntry entry) -> entry.stats().totalOnlineMillis).reversed()
                        .thenComparing((PlayerStatsEntry entry) -> entry.stats().joinCount, Comparator.reverseOrder())
                        .thenComparing((PlayerStatsEntry entry) -> entry.stats().lastJoinEpochMillis, Comparator.reverseOrder())
                        .thenComparing(entry -> safeName(entry.stats()), String.CASE_INSENSITIVE_ORDER))
                .toList();

        send(source, "&7❰ &b&l✦ &f&lPixelMC 登录总时间 &e&l✦ &7❱");
        if (entries.isEmpty()) {
            send(source, "&7暂无玩家记录。");
            return Command.SINGLE_SUCCESS;
        }

        for (int index = 0; index < entries.size(); index++) {
            PlayerStats stats = entries.get(index).stats();
            send(source, "&8#" + (index + 1) + " &e" + safeName(stats) + " &7| &f总时长 &d" + TimeFormatUtil.formatFriendly(stats.totalOnlineMillis) + " &7| &f登录 &a" + stats.joinCount + " &7次 | &f上次 &b" + formatDateTime(stats.lastJoinEpochMillis));
        }
        return Command.SINGLE_SUCCESS;
    }

    private int showLoginTime(CommandSourceStack source, String playerName) {
        checkpointForQuery(source);
        Optional<PlayerStatsEntry> entry = statsStore.findByName(playerName);
        if (entry.isEmpty()) {
            send(source, "&c没有找到玩家 " + playerName + " 的记录。");
            return 0;
        }

        PlayerStats stats = entry.get().stats();
        send(source, "&7❰ &b&l✦ &f&lPixelMC 登录总时间 &e&l✦ &7❱");
        send(source, "&f玩家：&e" + safeName(stats));
        send(source, "&fUUID：&7" + entry.get().uuid());
        send(source, "&f总游玩时长：&d" + TimeFormatUtil.formatFriendly(stats.totalOnlineMillis));
        send(source, "&f登录次数：&a" + stats.joinCount);
        send(source, "&f首次登录：&b" + formatDateTime(stats.firstJoinEpochMillis));
        send(source, "&f上次登录：&b" + formatDateTime(stats.lastJoinEpochMillis));
        return Command.SINGLE_SUCCESS;
    }

    private void checkpointForQuery(CommandSourceStack source) {
        statsStore.ensureLoaded(source.getServer());
        statsStore.checkpointOnlinePlayers(source.getServer(), System.currentTimeMillis(), false);
    }

    private void send(CommandSourceStack source, String message) {
        WelcomeConfig config = configManager.getConfig();
        Component component = textParser.parse(message, config);
        source.sendSuccess(() -> component, false);
    }

    private String formatDateTime(long epochMillis) {
        WelcomeConfig config = configManager.getConfig();
        return TimeFormatUtil.formatDateTime(epochMillis, config.timezone, config.dateFormat, config.timeFormat);
    }

    private static String safeName(PlayerStats stats) {
        return stats.name == null || stats.name.isBlank() ? "未知玩家" : stats.name;
    }
}
