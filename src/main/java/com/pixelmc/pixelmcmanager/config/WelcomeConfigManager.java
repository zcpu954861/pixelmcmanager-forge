package com.pixelmc.pixelmcmanager.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.pixelmc.pixelmcmanager.PixelMCManager;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class WelcomeConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String LEGACY_MOD_ID = "pixelmcwelcome";

    private final Logger logger;
    private final Path configPath;
    private final Path legacyConfigPath;
    private WelcomeConfig config = WelcomeConfig.defaults();
    private String lastError = "";

    public WelcomeConfigManager(Logger logger) {
        this.logger = logger;
        this.configPath = FMLPaths.CONFIGDIR.get().resolve(PixelMCManager.MOD_ID + ".json");
        this.legacyConfigPath = FMLPaths.CONFIGDIR.get().resolve(LEGACY_MOD_ID + ".json");
    }

    public WelcomeConfig getConfig() {
        return config;
    }

    public String getLastError() {
        return lastError;
    }

    public LoadResult loadOrCreate() {
        try {
            Files.createDirectories(configPath.getParent());
            migrateLegacyConfigIfNeeded();
            if (Files.notExists(configPath)) {
                config = WelcomeConfig.defaults();
                writeDefault();
                lastError = "";
                return LoadResult.success("Created default config.");
            }

            String json = Files.readString(configPath, StandardCharsets.UTF_8);
            WelcomeConfig loaded = GSON.fromJson(json, WelcomeConfig.class);
            if (loaded == null) {
                throw new JsonParseException("Config file is empty.");
            }
            loaded.normalize();
            config = loaded;
            lastError = "";
            return LoadResult.success("Config loaded.");
        } catch (Exception exception) {
            lastError = exception.getMessage();
            logger.error("Failed to load PixelMC Manager config from {}. Keeping previous valid config.", configPath, exception);
            return LoadResult.failure(lastError);
        }
    }

    private void writeDefault() throws IOException {
        Files.writeString(configPath, GSON.toJson(config), StandardCharsets.UTF_8);
    }

    private void migrateLegacyConfigIfNeeded() throws IOException {
        if (Files.notExists(configPath) && Files.exists(legacyConfigPath)) {
            Files.copy(legacyConfigPath, configPath, StandardCopyOption.COPY_ATTRIBUTES);
            logger.info("Copied legacy pixelmcwelcome config from {} to {}.", legacyConfigPath, configPath);
        }
    }

    public record LoadResult(boolean success, String message) {
        public static LoadResult success(String message) {
            return new LoadResult(true, message);
        }

        public static LoadResult failure(String message) {
            return new LoadResult(false, message);
        }
    }
}
