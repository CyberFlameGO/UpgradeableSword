package gg.solarmc.upgradeablesword.events;

import gg.solarmc.upgradeablesword.PlayerData;
import gg.solarmc.upgradeablesword.PluginHelper;
import gg.solarmc.upgradeablesword.UpgradeableSword;
import gg.solarmc.upgradeablesword.config.Config;
import gg.solarmc.upgradeablesword.config.LevelConfig;
import gg.solarmc.upgradeablesword.enchantments.PluginEnchants;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PluginEvent implements Listener, UpgradeableSwordEvent {
    private final Map<UUID, PlayerData> playerData;
    private final UpgradeableSword plugin;
    private final PluginHelper helper;
    private final NamespacedKey xpKey;
    private final PluginEnchants enchants;

    public PluginEvent(UpgradeableSword plugin, PluginHelper helper, PluginEnchants enchants) {
        xpKey = new NamespacedKey(plugin, "solar_xp");
        this.plugin = plugin;
        this.helper = helper;
        this.enchants = enchants;
        playerData = new HashMap<>();
    }

    @EventHandler
    public void onPlayerHitPlayer(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player damagedPlayer && event.getDamager() instanceof Player damager) {
            ItemStack item = damager.getInventory().getItemInMainHand();
            if (item == null) return;
            ItemMeta meta = item.getItemMeta();
            Config config = plugin.getPluginConfig();

            if (!(helper.stripColorCode(meta.displayName())).equals(config.swordName().replaceAll("&\\w", "")))
                return;

            List<Component> lore = meta.lore();
            List<Component> swordLore = helper.replaceSwordLore(config.swordLore(), damager.displayName(), getSwordXP(item) + 1);

            for (int i = 0; i < swordLore.size(); i++)
                lore.set(i, swordLore.get(i));

            addSwordXP((Player) event.getDamager(), item);
            onLifeStealUsed(damager, damagedPlayer, enchants.getEnchantmentLevel(item, PluginEnchants.LIFE_STEAL));
        }
    }

    @Override
    public void onSwordXpIncrease(Player player, ItemStack item) {
        LevelConfig config = plugin.getPluginConfig().getLevelConfig();
        int xp = (int) getSwordXP(item);

        checkAndAddEnchantment(player, item, Enchantment.DAMAGE_ALL, xp, config.sharpness());
        checkAndAddEnchantment(player, item, Enchantment.FIRE_ASPECT, xp, config.fireAspect());
        checkAndAddEnchantment(player, item, PluginEnchants.LIFE_STEAL, xp, config.lifeSteal());
    }

    @Override
    public void onLifeStealEnchantmentAdd(ItemStack sword, int amplifier) {
        if (amplifier == 0) return;
        final ItemMeta meta = sword.getItemMeta();
        List<Component> lore = meta.lore();

        // Get the second last Lore , cause last is `Unbreakable`
        if (sword.containsEnchantment(PluginEnchants.LIFE_STEAL)) lore.remove(lore.size() - 2);

        // lore wouldn't be null :D
        lore.add(lore.size() - 2, Component.text("Life Steal " + helper.intToRoman(amplifier)));

        meta.lore(lore);
        sword.setItemMeta(meta);
    }

    @Override
    public void onLifeStealUsed(Player damager, Player playerDamaged, int lifeStealAmplifier) {
        UUID damagerId = damager.getUniqueId();
        UUID playerDamagedId = playerDamaged.getUniqueId();
        PlayerData damagerData = this.playerData.computeIfAbsent(damagerId, it -> new PlayerData(0, new UUID(0, 0), 0));

        final int lastDamagedPlayerHits = damagerData.lastDamagedPlayerHits();

        if (playerDamagedId.equals(damagerData.lastDamagedPlayer()))
            playerData.put(damagerId, damagerData.withHits(damagerData.hits() + 1).withLastDamagedPlayerHits(lastDamagedPlayerHits + 1));
        else {
            playerData.put(damagerId, damagerData.withHits(0).withLastDamagedPlayer(playerDamagedId).withLastDamagedPlayerHits(1));
            return;
        }

        if (lastDamagedPlayerHits >= plugin.getPluginConfig().maxHitsAlert() && lastDamagedPlayerHits % 10 == 0) {
            damager.getServer().getOnlinePlayers()
                    .stream().filter(it -> it.hasPermission("usword.notifyBoosting"))
                    .forEach(it -> {
                        TextComponent msg = Component.text("(")
                                .append(Component.text("!!").style(Style.style(NamedTextColor.RED, TextDecoration.BOLD)))
                                .append(Component.text(
                                        String.format(") %s could be boosting (Hits to same Player : %s)",
                                                damager.getName(), lastDamagedPlayerHits)));
                        it.sendMessage(msg);
                    });
        }

        if (damagerData.hits() == 4) {
            playerData.put(damagerId, damagerData.withHits(0));

            double damagerHealth = Math.min(damager.getHealth() + lifeStealAmplifier, damager.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            damager.setHealth(damagerHealth);

            double playerDamagedHealth = Math.max(0, playerDamaged.getHealth() - lifeStealAmplifier);
            playerDamaged.setHealth(playerDamagedHealth);
        }
    }

    private void checkAndAddEnchantment(Player player, ItemStack item, Enchantment enchantment, int xp, List<Integer> levels) {
        if (levels.contains(xp)) {
            int amplifier = levels.indexOf(xp);
            enchants.addEnchantment(item, enchantment, amplifier);

            player.sendMessage(plugin.getPluginConfig().levelUpMessage()
                    .replace("{enchantment}", enchantment.getName())
                    .replace("{level}", helper.intToRoman(amplifier)));

            if (enchantment == PluginEnchants.LIFE_STEAL) onLifeStealEnchantmentAdd(item, amplifier);
        }
    }

    /**
     * @param item the diamond sword.
     * @return the xp of the sword
     */
    @SuppressWarnings("ConstantConditions")
    private double getSwordXP(ItemStack item) {
        PersistentDataContainer dataContainer = item.getItemMeta().getPersistentDataContainer();

        if (!dataContainer.has(xpKey, PersistentDataType.DOUBLE)) {
            dataContainer.set(xpKey, PersistentDataType.DOUBLE, 0.0);
            return 0;
        }

        // Will not produce npe cause we have checked if it has the xpKey :D
        return dataContainer.get(xpKey, PersistentDataType.DOUBLE);
    }

    private void addSwordXP(Player player, ItemStack item) {
        double xp = plugin.getPluginConfig().xpRate();
        if (xp < 0) xp = 1.0 / xp;

        item.getItemMeta().getPersistentDataContainer()
                .set(xpKey, PersistentDataType.DOUBLE, getSwordXP(item) + xp);
        onSwordXpIncrease(player, item);
    }
}
