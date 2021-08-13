package gg.solarmc.upgradeablesword.commands;

import gg.solarmc.upgradeablesword.UpgradeableSword;
import gg.solarmc.upgradeablesword.enchantments.PluginEnchants;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class USwordCommand implements CommandExecutor {
    private final UpgradeableSword plugin;
    private final CommandHandler handler;
    private final PluginEnchants enchants;

    public USwordCommand(UpgradeableSword plugin, PluginEnchants enchants) {
        this.plugin = plugin;
        handler = new CommandHandler(plugin);
        this.enchants = enchants;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                if (!sender.hasPermission("usword.command"))
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                else {
                    if (player.getInventory().firstEmpty() == -1) {
                        player.sendMessage(ChatColor.RED + "Your Inventory is Full!!");
                        return true;
                    }

                    handler.usword(player);
                }
            } else sender.sendMessage("Only Players can use this command.");
            return true;
        }

        switch (args[0]) {
            case "help" -> handler.help(sender);
            case "reload" -> handler.reload(sender);
            case "sync" -> {
                if (sender instanceof Player player)
                    handler.sync(player, enchants);
                else sender.sendMessage(ChatColor.RED + "Only Players can use this command.");
            }
            case "level" -> {
                if (sender instanceof Player player)
                    handler.level(player);
                else sender.sendMessage(ChatColor.RED + "Only Players can use this command.");
            }
            default -> sender.sendMessage(ChatColor.RED + "No SubCommand was found : " + args[0]);
        }
        return true;
    }
}
