package gg.solarmc.upgradeablesword.commands;

import gg.solarmc.upgradeablesword.PluginHelper;
import gg.solarmc.upgradeablesword.UpgradeableSword;
import gg.solarmc.upgradeablesword.config.Config;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class USwordCommand implements CommandExecutor {

    private final UpgradeableSword plugin;
    private final PluginHelper helper;

    public USwordCommand(UpgradeableSword plugin, PluginHelper helper) {
        this.plugin = plugin;
        this.helper = helper;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Config config = plugin.getPluginConfig();

        if (args.length == 0) {
            if (sender instanceof Player player) {
                if (!sender.hasPermission("usword.command"))
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                else {
                    if (player.getInventory().firstEmpty() == -1) {
                        player.sendMessage(ChatColor.RED + "Your Inventory is Full!!");
                        return true;
                    }
                    ItemStack sword = getUpgradeableSword(player.getDisplayName());

                    player.getInventory().addItem(sword);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.swordGaveMessage()));
                }
            } else sender.sendMessage("Only Players can use this command.");
            return true;
        }

        if (args[0].equals("reload")) {
            if (!sender.hasPermission("usword.reload"))
                sender.sendMessage(ChatColor.RED + "You don't have permission to reload this plugin");
            else plugin.reloadConfig();
            return true;
        }

        sender.sendMessage(ChatColor.RED + "No SubCommand was found : " + args[0]);
        return true;
    }

    public ItemStack getUpgradeableSword(String playerName) {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);

        ItemMeta meta = sword.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getPluginConfig().swordName()));
        meta.setLore(helper.replaceSwordLore(plugin.getPluginConfig().swordLore(), playerName, 0));
        meta.setUnbreakable(true);

        sword.setItemMeta(meta);
        return sword;
    }
}
