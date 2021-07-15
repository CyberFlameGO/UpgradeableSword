package gg.solarmc.upgradeablesword;

import gg.solarmc.upgradeablesword.commands.USwordCommand;
import gg.solarmc.upgradeablesword.config.Config;
import gg.solarmc.upgradeablesword.config.ConfigManager;
import gg.solarmc.upgradeablesword.enchantments.PluginEnchants;
import gg.solarmc.upgradeablesword.events.PluginEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class UpgradeableSword extends JavaPlugin {
    private ConfigManager<Config> configManager;
    private PluginEnchants enchants;

    @Override
    public void onEnable() {
        this.configManager = ConfigManager.create(this.getDataFolder().toPath(), "config.yml", Config.class);

        if (getPluginConfig().isEnabled()) {
            PluginHelper helper = new PluginHelper();

            // Events
            this.getServer().getPluginManager().registerEvents(new PluginEvent(this, helper), this);

            // Enchantments
            enchants = new PluginEnchants(this);
            enchants.register();

            // Commands
            this.getServer().getPluginCommand("usword").setExecutor(new USwordCommand(this, helper));

            getLogger().info("Upgradeable Swords Started");
        }
    }

    @Override
    public void onDisable() {
        enchants.unregister();
    }

    public Config getPluginConfig() {
        return configManager.getConfigData();
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
