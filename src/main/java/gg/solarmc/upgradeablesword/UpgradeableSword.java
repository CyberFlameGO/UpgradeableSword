package gg.solarmc.upgradeablesword;

import gg.solarmc.upgradeablesword.commands.USwordCommand;
import gg.solarmc.upgradeablesword.config.Config;
import gg.solarmc.upgradeablesword.config.ConfigManager;
import gg.solarmc.upgradeablesword.enchantments.PluginEnchants;
import gg.solarmc.upgradeablesword.events.HitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class UpgradeableSword extends JavaPlugin {
    private ConfigManager<Config> configManager;

    @Override
    public void onEnable() {
        this.configManager = ConfigManager.create(this.getDataFolder().toPath(), "config.yml", Config.class);
        reloadConfig();

        if (getPluginConfig().isEnabled()) {
            PluginHelper helper = new PluginHelper(this);

            // Enchantments
            PluginEnchants enchants = new PluginEnchants(this);

            // Events
            this.getServer().getPluginManager().registerEvents(new HitEvent(this, helper, enchants), this);

            // Commands
            this.getServer().getPluginCommand("usword").setExecutor(new USwordCommand(this, helper));

            getLogger().info("Upgradeable Swords Started");
        }
    }

    @Override
    public void onDisable() {
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
