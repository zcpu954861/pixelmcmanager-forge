package com.pixelmc.pixelmcmanager;

import com.mojang.logging.LogUtils;
import com.pixelmc.pixelmcmanager.command.PixelMCManagerCommands;
import com.pixelmc.pixelmcmanager.config.WelcomeConfigManager;
import com.pixelmc.pixelmcmanager.event.DelayedMessageScheduler;
import com.pixelmc.pixelmcmanager.event.WelcomeEventHandler;
import com.pixelmc.pixelmcmanager.placeholder.PlaceholderResolver;
import com.pixelmc.pixelmcmanager.stats.PlayerStatsStore;
import com.pixelmc.pixelmcmanager.text.TextTemplateParser;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(PixelMCManager.MOD_ID)
public final class PixelMCManager {
    public static final String MOD_ID = "pixelmcmanager";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PixelMCManager() {
        WelcomeConfigManager configManager = new WelcomeConfigManager(LOGGER);
        PlayerStatsStore statsStore = new PlayerStatsStore(LOGGER);
        PlaceholderResolver placeholderResolver = new PlaceholderResolver();
        TextTemplateParser textParser = new TextTemplateParser(LOGGER);
        DelayedMessageScheduler scheduler = new DelayedMessageScheduler(configManager, statsStore, placeholderResolver, textParser);

        MinecraftForge.EVENT_BUS.register(new WelcomeEventHandler(configManager, statsStore, scheduler));
        MinecraftForge.EVENT_BUS.register(new PixelMCManagerCommands(configManager, statsStore, placeholderResolver, textParser));

        configManager.loadOrCreate();
        LOGGER.info("PixelMC Manager loaded.");
    }
}
