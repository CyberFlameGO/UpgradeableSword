package gg.solarmc.upgradeablesword;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class PluginHelper {
    private final NamespacedKey xpKey;
    private final UpgradeableSword plugin;

    public PluginHelper(UpgradeableSword plugin) {
        xpKey = new NamespacedKey(plugin, "xp");
        this.plugin = plugin;
    }

    public List<Component> replaceSwordLore(List<String> lore, Component playerName, double xp) {
        return lore.stream()
                .map(it -> translateColorCode(it.replace("{playerName}", stripColorCode(playerName))
                        .replace("{xp}", String.valueOf(xp)))
                )
                .toList();
    }

    public Component translateColorCode(String s) {
        return LegacyComponentSerializer.legacy('&').deserialize(s);
    }

    public String stripColorCode(Component c) {
        return PlainComponentSerializer.plain().serialize(c);
    }

    // Only till 5 cause max enchant used in this plugin is 5 (Sharpness)
    public String intToRoman(int i) {
        return switch (i) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> "";
        };
    }

    /**
     * @param item the diamond sword.
     * @return the xp of the sword
     */
    public Double getSwordXP(ItemStack item) {
        final ItemMeta meta = item.getItemMeta();
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

        if (!dataContainer.has(xpKey, PersistentDataType.DOUBLE)) {
            dataContainer.set(xpKey, PersistentDataType.DOUBLE, 0.0);
            item.setItemMeta(meta);
            return (double) 0;
        }

        // Will not produce npe cause we have checked if it has the xpKey :D
        return dataContainer.get(xpKey, PersistentDataType.DOUBLE);
    }

    public void addSwordXP(ItemStack item) {
        double xp = plugin.getPluginConfig().xpRate();
        if (xp < 0) xp = 1.0 / xp;

        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(xpKey, PersistentDataType.DOUBLE, getSwordXP(item) + xp);
        item.setItemMeta(meta);
    }
}
