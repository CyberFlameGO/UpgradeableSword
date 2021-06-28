package gg.solarmc.upgradeablesword.enchantments;

import gg.solarmc.upgradeablesword.UpgradeableSword;
import org.bukkit.enchantments.Enchantment;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public class PluginEnchants {
    public static final Enchantment LIFE_STEAL = new LifeStealEnchantment();

    private final UpgradeableSword plugin;

    public PluginEnchants(UpgradeableSword plugin) {
        this.plugin = plugin;
    }

    public void register() {
        boolean registered = Arrays.stream(Enchantment.values()).collect(Collectors.toList()).contains(PluginEnchants.LIFE_STEAL);

        if (!registered)
            registerEnchantment(LIFE_STEAL);
    }

    // TODO: remove Ids.
    @SuppressWarnings({"unchecked", "deprecation"})
    public void unregister() {
        try {
            Field idField = Enchantment.class.getDeclaredField("byId");
            idField.setAccessible(true);
            HashMap<Integer, Enchantment> byId = (HashMap<Integer, Enchantment>) idField.get(null);
            byId.remove(LIFE_STEAL.getId());

            Field nameField = Enchantment.class.getDeclaredField("byName");
            nameField.setAccessible(true);
            HashMap<String, Enchantment> byName = (HashMap<String, Enchantment>) nameField.get(null);
            byName.remove(LIFE_STEAL.getName());
        } catch (Exception ignored) {
        }
    }

    public void registerEnchantment(Enchantment enchantment) {
        boolean registered = true;

        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
            f.setAccessible(false);

            Enchantment.registerEnchantment(enchantment);
        } catch (Exception e) {
            registered = false;
            e.printStackTrace();
        }

        Enchantment.stopAcceptingRegistrations();
        if (registered)
            plugin.getLogger().warning("Already Registered : " + enchantment.getName() + " (Report to Infinity)");
    }
}
