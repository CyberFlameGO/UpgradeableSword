package gg.solarmc.upgradeablesword.commands;

import gg.solarmc.upgradeablesword.UpgradeableSword;
import gg.solarmc.upgradeablesword.enchantments.PluginEnchants;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class USwordCommand implements CommandExecutor {
    private final UpgradeableSword plugin;
    private final CommandHandler handler;
    private final PluginEnchants enchants;

    private final Map<UUID, Long> coolDowns = new HashMap<>();

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
                    if (coolDowns.containsKey(player.getUniqueId())) {
                        long playerCoolDown = coolDowns.get(player.getUniqueId());
                        if (playerCoolDown > System.currentTimeMillis()) {
                            long left = playerCoolDown - System.currentTimeMillis();
                            String time = formatTime(left);
                            player.sendMessage(Component.text(
                                    String.format("You need to wait %s to claim another Upgradeable sword!", time),
                                    NamedTextColor.RED));
                            return true;
                        }
                    }

                    long cool = plugin.getPluginConfig().uSwordCoolDown();
                    coolDowns.put(player.getUniqueId(), System.currentTimeMillis() + (cool * 1000));
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

    private String formatTime(long milliseconds) {
        int sec = (int) (milliseconds / 1000) % 60;
        int min = (int) ((milliseconds / (1000 * 60)) % 60);
        int hour = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
        return String.format("%dH %dM %dS", hour, min, sec);
    }
}
