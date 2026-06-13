package com.pixelmc.pixelmcwelcome;

import com.mojang.logging.LogUtils;
import com.pixelmc.pixelmcwelcome.command.PixelMCWelcomeCommands;
import com.pixelmc.pixelmcwelcome.config.WelcomeConfigManager;
import com.pixelmc.pixelmcwelcome.event.DelayedMessageScheduler;
import com.pixelmc.pixelmcwelcome.event.WelcomeEventHandler;
import com.pixelmc.pixelmcwelcome.placeholder.PlaceholderResolver;
import com.pixelmc.pixelmcwelcome.stats.PlayerStatsStore;
import com.pixelmc.pixelmcwelcome.text.TextTemplateParser;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(PixelMCWelcome.MOD_ID)
public final class PixelMCWelcome {
    public static final String MOD_ID = "pixelmcwelcome";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PixelMCWelcome() {
        WelcomeConfigManager configManager = new WelcomeConfigManager(LOGGER);
        PlayerStatsStore statsStore = new PlayerStatsStore(LOGGER);
        PlaceholderResolver placeholderResolver = new PlaceholderResolver();
        TextTemplateParser textParser = new TextTemplateParser(LOGGER);
        DelayedMessageScheduler scheduler = new DelayedMessageScheduler(configManager, statsStore, placeholderResolver, textParser);

        MinecraftForge.EVENT_BUS.register(new WelcomeEventHandler(configManager, statsStore, scheduler));
        MinecraftForge.EVENT_BUS.register(new PixelMCWelcomeCommands(configManager, statsStore, placeholderResolver, textParser));

        configManager.loadOrCreate();
        LOGGER.info("PixelMC Welcome loaded.");
    }
}
