package me.example.testplugin;

import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.ConfigurateException;

public class TestPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        try {
            Settings.load(getDataFolder().toPath().resolve("settings.conf"));
        } catch (ConfigurateException ex) {
            getSLF4JLogger().error("Failed to load config", ex);
        }

        getSLF4JLogger().info("Enabled!");
    }

    @Override
    public void onDisable() {
        getSLF4JLogger().info("Disabled!");
    }
}
