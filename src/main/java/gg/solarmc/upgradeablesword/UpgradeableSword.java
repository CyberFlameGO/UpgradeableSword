package gg.solarmc.upgradeablesword;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import gg.solarmc.clans.SolarClans;
import gg.solarmc.upgradeablesword.commands.USwordCommand;
import gg.solarmc.upgradeablesword.config.Config;
import gg.solarmc.upgradeablesword.config.ConfigManager;
import gg.solarmc.upgradeablesword.enchantments.PluginEnchants;
import gg.solarmc.upgradeablesword.events.HitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpgradeableSword extends JavaPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeableSword.class);
    private ConfigManager<Config> configManager;
    private PluginHelper helper;

    private WorldGuardPlugin worldGuard;
    private SolarClans clans;

    @Override
    public void onEnable() {
        worldGuard = (WorldGuardPlugin) setupPlugin("WorldGuard");
        clans = (SolarClans) setupPlugin("SolarClans");

        this.configManager = ConfigManager.create(this.getDataFolder().toPath(), "config.yml", Config.class);
        reloadConfig();

        if (getPluginConfig().isEnabled()) {
            helper = new PluginHelper(this);

            // Enchantments
            PluginEnchants enchants = new PluginEnchants(this);

            // Events
            this.getServer().getPluginManager().registerEvents(new HitEvent(this, enchants), this);

            // Commands
            this.getServer().getPluginCommand("usword").setExecutor(new USwordCommand(this, enchants));

            getLogger().info("Upgradeable Swords Started");
        }
    }

    private Plugin setupPlugin(String name) {
        Plugin plugin = getServer().getPluginManager().getPlugin(name);
        if (plugin == null) {
            LOGGER.warn("*** {} is not installed or not enabled. ***", name);
            throw new IllegalStateException("*** This plugin will be disabled. ***");
        }

        return plugin;
    }

    @Override
    public void onDisable() {
    }

    public Config getPluginConfig() {
        return configManager.getConfigData();
    }

    public WorldGuardPlugin getWorldGuardManager() {
        return worldGuard;
    }

    public SolarClans getClansManager() {
        return clans;
    }

    public PluginHelper getHelper() {
        return helper;
    }

    @Override
    public void reloadConfig() {
        configManager.reloadConfig();
        if (!configManager.getConfigData().isEnabled())
            this.getServer().getPluginManager().disablePlugin(this);
        else
            this.getServer().getPluginManager().enablePlugin(this);
    }
}
