package com.pixelmc.pixelmcwelcome.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.pixelmc.pixelmcwelcome.config.WelcomeConfig;
import com.pixelmc.pixelmcwelcome.config.WelcomeConfigManager;
import com.pixelmc.pixelmcwelcome.stats.PlayerStats;
import com.pixelmc.pixelmcwelcome.stats.PlayerStatsEntry;
import com.pixelmc.pixelmcwelcome.stats.PlayerStatsStore;
import com.pixelmc.pixelmcwelcome.text.TextTemplateParser;
import com.pixelmc.pixelmcwelcome.util.TimeFormatUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class PixelMCManagerCommands {
    private final WelcomeConfigManager configManager;
    private final PlayerStatsStore statsStore;
    private final TextTemplateParser textParser;

    public PixelMCManagerCommands(WelcomeConfigManager configManager, PlayerStatsStore statsStore, TextTemplateParser textParser) {
        this.configManager = configManager;
        this.statsStore = statsStore;
        this.textParser = textParser;
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("pixelmcmanager")
                .requires(source -> source.hasPermission(2))
                .executes(context -> help(context.getSource()))
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
                                .executes(context -> showLoginTime(context.getSource(), StringArgumentType.getString(context, "player"))))));
    }

    private int help(CommandSourceStack source) {
        send(source, "&7❰ &b&l✦ &f&lPixelMC Manager &e&l✦ &7❱");
        send(source, "&b/pixelmcmanager logincount");
        send(source, "&b/pixelmcmanager logincount <player>");
        send(source, "&d/pixelmcmanager logintime");
        send(source, "&d/pixelmcmanager logintime <player>");
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
