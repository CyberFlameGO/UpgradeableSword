package gg.solarmc.upgradeablesword.enchantments;

import gg.solarmc.upgradeablesword.UpgradeableSword;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class PluginEnchants {
    private final NamespacedKey lifeStealKey;
    public static final Enchantment LIFE_STEAL = new LifeStealEnchantment();

    public PluginEnchants(UpgradeableSword plugin) {
        this.lifeStealKey = new NamespacedKey(plugin, "enchantment:" + LIFE_STEAL.getName());
    }

    public void addEnchantment(ItemStack item, Enchantment enchantment, int level) {
        if (enchantment == LIFE_STEAL) {
            item.getItemMeta().getPersistentDataContainer().set(lifeStealKey, PersistentDataType.INTEGER, level);
            return;
        }

        item.addEnchantment(enchantment, level);
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
