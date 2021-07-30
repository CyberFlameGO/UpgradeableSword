package gg.solarmc.upgradeablesword.commands;

import gg.solarmc.upgradeablesword.PluginHelper;
import gg.solarmc.upgradeablesword.UpgradeableSword;
import gg.solarmc.upgradeablesword.config.Config;
import gg.solarmc.upgradeablesword.enchantments.PluginEnchants;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.LinkedList;
import java.util.List;

public class USwordCommand implements CommandExecutor {

    private final UpgradeableSword plugin;
    private final PluginHelper helper;
    private final PluginEnchants enchants;

    public USwordCommand(UpgradeableSword plugin, PluginHelper helper, PluginEnchants enchants) {
        this.plugin = plugin;
        this.helper = helper;
        this.enchants = enchants;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Config config = plugin.getPluginConfig();

        if (args.length == 0) {
            if (sender instanceof Player player) {
                /*if (!sender.hasPermission("usword.command"))
                    sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                else {*/
                if (player.getInventory().firstEmpty() == -1) {
                    player.sendMessage(ChatColor.RED + "Your Inventory is Full!!");
                    return true;
                }

                ItemStack sword = getUpgradeableSword(player.displayName());

                player.getInventory().addItem(sword);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.swordGaveMessage()));
                //}
            } else sender.sendMessage("Only Players can use this command.");
            return true;
        }

        switch (args[0]) {
            case "help" -> {

            }
            // Reload Command
            case "reload" -> {
                if (!sender.hasPermission("usword.reload"))
                    sender.sendMessage(ChatColor.RED + "You don't have permission to reload this plugin");
                else plugin.reloadConfig();
                return true;
            }
            // Sync COmmand
            case "sync" -> {
                Player player = (Player) sender;
                final ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() == Material.AIR) return true;
                final ItemMeta meta = item.getItemMeta();

                if (!meta.hasDisplayName()) return true;

                if (!(helper.stripColorCode(meta.displayName())).equals(config.swordName().replaceAll("&\\w", "")))
                    return true;

                List<Component> lore = new LinkedList<>(meta.lore());

                List<Component> swordLore = helper.replaceSwordLore(config.swordLore(), player.displayName(), helper.getSwordXP(item));

                final boolean containsLifeSteal = enchants.containsEnchantment(item, PluginEnchants.LIFE_STEAL);

                for (int i = containsLifeSteal ? 1 : 0; i <= (containsLifeSteal ? swordLore.size() : swordLore.size() - 1); i++)
                    lore.set(i, swordLore.get(i - (containsLifeSteal ? 1 : 0)));

                meta.lore(lore);
                item.setItemMeta(meta);
                return true;
            }
            // Level Command
            case "level" -> {
                Player player = (Player) sender;
                final ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() == Material.AIR) {
                    sender.sendMessage(ChatColor.RED + "Hold your sword in your hand!!");
                    return true;
                }
                final ItemMeta meta = item.getItemMeta();
                if (!meta.hasDisplayName()) {
                    sender.sendMessage(ChatColor.RED + "Hold your sword in your hand!!");
                    return true;
                }
                if (!(helper.stripColorCode(meta.displayName())).equals(config.swordName().replaceAll("&\\w", ""))) {
                    sender.sendMessage(ChatColor.RED + "Hold your sword in your hand!!");
                    return true;
                }

                final NamespacedKey xp = new NamespacedKey(plugin, "xp");
                boolean xpExists = meta.getPersistentDataContainer().has(xp, PersistentDataType.DOUBLE);
                if (!xpExists)
                    sender.sendMessage("XP of your sword : 0");
                else
                    sender.sendMessage("XP of your sword : " + meta.getPersistentDataContainer().get(xp, PersistentDataType.DOUBLE));
                return true;
            }

            // TODO: Make this organised and make a help command
        }

        sender.sendMessage(ChatColor.RED + "No SubCommand was found : " + args[0]);
        return true;
    }

    public ItemStack getUpgradeableSword(Component playerName) {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);

        ItemMeta meta = sword.getItemMeta();
        meta.displayName(helper.translateColorCode(plugin.getPluginConfig().swordName()));
        meta.lore(helper.replaceSwordLore(plugin.getPluginConfig().swordLore(), playerName, 0));
        meta.setUnbreakable(true);

        sword.setItemMeta(meta);
        return sword;
    }
}
