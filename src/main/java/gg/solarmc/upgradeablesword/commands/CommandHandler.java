package gg.solarmc.upgradeablesword.commands;

import gg.solarmc.upgradeablesword.PluginHelper;
import gg.solarmc.upgradeablesword.UpgradeableSword;
import gg.solarmc.upgradeablesword.config.Config;
import gg.solarmc.upgradeablesword.enchantments.PluginEnchants;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler {
    private final NamespacedKey xp;
    private final UpgradeableSword plugin;
    private final PluginHelper helper;

    public CommandHandler(UpgradeableSword plugin) {
        xp = new NamespacedKey(plugin, "xp");
        this.plugin = plugin;
        this.helper = plugin.getHelper();
    }

    public void usword(Player player) {
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(ChatColor.RED + "Your Inventory is Full!!");
            return;
        }

        ItemStack sword = getUpgradeableSword(player.displayName());

        player.getInventory().addItem(sword);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getPluginConfig().swordGaveMessage()));
    }

    public void help(CommandSender sender) {
        sender.sendMessage(Component.text("Upgradeable Swords Commands : ", NamedTextColor.GOLD)
                .append(Component.newline())
                .append(Component.text("usword - Gives you the upgradeable sword", NamedTextColor.RED))
                .append(Component.newline())
                .append(Component.text("usword sync - Updates the lore of the sword", NamedTextColor.GREEN))
                .append(Component.newline())
                .append(Component.text("usword level - Shows the level of your sword", NamedTextColor.AQUA)));
    }

    public void reload(CommandSender sender) {
        if (!sender.hasPermission("usword.reload"))
            sender.sendMessage(ChatColor.RED + "You don't have permission to reload this plugin");
        else plugin.reloadConfig();
    }

    @SuppressWarnings("ConstantConditions")
    public void sync(Player player, PluginEnchants enchants) {
        if (validateSword(player)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();

        List<Component> lore = new ArrayList<>(meta.lore());
        List<Component> swordLore = helper.replaceSwordLore(plugin.getPluginConfig().swordLore(), player.displayName(), helper.getSwordXP(item));
        boolean containsLifeSteal = enchants.containsEnchantment(item, PluginEnchants.LIFE_STEAL);

        for (int i = containsLifeSteal ? 1 : 0;
             i <= (containsLifeSteal ? swordLore.size() : swordLore.size() - 1);
             i++)
            lore.set(i, swordLore.get(i - (containsLifeSteal ? 1 : 0)));

        meta.lore(lore);
        item.setItemMeta(meta);
    }

    public void level(Player player) {
        if (validateSword(player)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        boolean xpExists = meta.getPersistentDataContainer().has(xp, PersistentDataType.DOUBLE);
        if (!xpExists)
            player.sendMessage("XP of your sword : 0");
        else
            player.sendMessage("XP of your sword : " + meta.getPersistentDataContainer().get(xp, PersistentDataType.DOUBLE));
    }

    private boolean validateSword(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        if (item.getType() == Material.AIR || !meta.hasDisplayName()) {
            player.sendMessage(ChatColor.RED + "Hold your sword in your hand!!");
            return true;
        }

        Config config = plugin.getPluginConfig();
        if (!(helper.stripColorCode(meta.displayName())).equals(config.swordName().replaceAll("&\\w", ""))) {
            player.sendMessage(ChatColor.RED + "Hold your sword in your hand!!");
            return true;
        }

        return false;
    }


    private ItemStack getUpgradeableSword(Component playerName) {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);

        ItemMeta meta = sword.getItemMeta();
        meta.displayName(helper.translateColorCode(plugin.getPluginConfig().swordName()));
        meta.lore(helper.replaceSwordLore(plugin.getPluginConfig().swordLore(), playerName, 0));
        meta.setUnbreakable(true);

        sword.setItemMeta(meta);
        return sword;
    }

}
