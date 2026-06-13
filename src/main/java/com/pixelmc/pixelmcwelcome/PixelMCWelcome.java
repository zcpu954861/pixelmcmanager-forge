package com.pixelmc.pixelmcwelcome;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(PixelMCWelcome.MOD_ID)
public final class PixelMCWelcome {
    public static final String MOD_ID = "pixelmcwelcome";
    private static final Logger LOGGER = LogUtils.getLogger();

    public PixelMCWelcome() {
        LOGGER.info("PixelMC Welcome loaded.");
    }
}
