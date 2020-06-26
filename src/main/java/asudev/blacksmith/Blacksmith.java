package asudev.blacksmith;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class Blacksmith extends JavaPlugin {

    private static JavaPlugin plugin;

    private File customConfigFile;
    private FileConfiguration customConfig;

    private File customConfigFile2;
    private FileConfiguration customConfig2;

    public FileConfiguration getCustomConfig() {
        return this.customConfig;
    }
    public FileConfiguration getCustomConfig2() {
        return this.customConfig;
    }

    public void reloadCustomConfig() {
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
    }

    public void reloadCustomConfig2() {
        customConfig2 = YamlConfiguration.loadConfiguration(customConfigFile2);
    }

    private void createCustomConfig() {
        customConfigFile = new File(getDataFolder(), "main.yml");
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            saveResource("main.yml", false);
        }
        customConfig = new YamlConfiguration();
        try {
            customConfig.load(customConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void createCustomConfig2() {
        customConfigFile2 = new File(getDataFolder(), "forge.yml");
        if (!customConfigFile2.exists()) {
            customConfigFile2.getParentFile().mkdirs();
            saveResource("forge.yml", false);
        }
        customConfig2 = new YamlConfiguration();
        try {
            customConfig2.load(customConfigFile2);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        plugin = this;
        Manager.getInstance().init(this);
        try {
            Commands commands = new Commands();
            getCommand(commands.blacksmithmain).setExecutor(commands);
            getServer().getPluginManager().registerEvents(new Events(), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        createCustomConfig();
        createCustomConfig2();
        try {
            getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                @Override
                public void run() {
                    Manager.getInstance().setupItems();
                }
            }, 60L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
