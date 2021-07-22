package gg.solarmc.upgradeablesword.enchantments;

import gg.solarmc.upgradeablesword.UpgradeableSword;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class PluginEnchants {
    private final NamespacedKey lifeStealKey;
    public static final Enchantment LIFE_STEAL = new LifeStealEnchantment();

    public PluginEnchants(UpgradeableSword plugin) {
        this.lifeStealKey = new NamespacedKey(plugin, "enchantment." + LIFE_STEAL.getName().toLowerCase().replace(' ', '_'));
    }

    public void addEnchantment(ItemStack item, Enchantment enchantment, int level) {
        if (level == 0) return;
        if (enchantment == LIFE_STEAL) {
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(lifeStealKey, PersistentDataType.INTEGER, level);
            item.setItemMeta(meta);
            return;
        }

        item.addEnchantment(enchantment, level);
    }

    public boolean containsEnchantment(ItemStack item, Enchantment enchantment) {
        if (enchantment == LIFE_STEAL)
            return item.getItemMeta().getPersistentDataContainer().has(lifeStealKey, PersistentDataType.INTEGER);
        return item.containsEnchantment(enchantment);
    }

    @SuppressWarnings("ConstantConditions")
    public int getEnchantmentLevel(ItemStack item, Enchantment enchantment) {
        if (enchantment == LIFE_STEAL) {
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            return container.has(lifeStealKey, PersistentDataType.INTEGER) ? container.get(lifeStealKey, PersistentDataType.INTEGER) : 0;
        }

        return item.getEnchantmentLevel(enchantment);
    }
}
