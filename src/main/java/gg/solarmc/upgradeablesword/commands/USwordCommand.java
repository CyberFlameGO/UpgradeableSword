package gg.solarmc.upgradeablesword.commands;

import gg.solarmc.upgradeablesword.UpgradeableSword;
import gg.solarmc.upgradeablesword.config.Config;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class USwordCommand implements CommandExecutor {

    private final UpgradeableSword plugin;

    public USwordCommand(UpgradeableSword plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Sub Commands : reload | sword");
            return true;
        }

        final Config config = plugin.getPluginConfig();

        switch (args[0]) {
            // usword reload
            case "reload": {
                if (!sender.hasPermission("usword.reload"))
                    sender.sendMessage(ChatColor.RED + "You don't have permission to reload this plugin");
                else
                    plugin.reloadConfig();
            }

            // Gives u a example Sword
            // usword sword
            case "sword": {
                if (sender instanceof Player player) {
                    if (!sender.hasPermission("usword.sword"))
                        sender.sendMessage(ChatColor.RED + "You don't have permission to use this plugin");
                    else {
                        final ItemStack sword = getUpgradeableSword();
                        if (player.getInventory().firstEmpty() == -1) {
                            Location loc = player.getLocation();
                            World world = player.getWorld();

                            world.dropItem(loc, sword);
                            player.sendMessage(ChatColor.AQUA + "Dropped the sword near you");

                            return true;
                        }

                        player.getInventory().addItem(sword);
                        player.sendMessage(ChatColor.AQUA + "Check the sword in your Inventory");
                    }
                }
            }
            default:
                sender.sendMessage(ChatColor.RED + "No SubCommand was found : " + args[0]);
        }

        return true;
    }

    public ItemStack getUpgradeableSword() {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        final ItemMeta meta = sword.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getPluginConfig().swordName()));
        meta.setUnbreakable(true);
        sword.setItemMeta(meta);

        return sword;
    }
}
