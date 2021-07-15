package gg.solarmc.upgradeablesword.events;

import gg.solarmc.upgradeablesword.PlayerData;
import gg.solarmc.upgradeablesword.PluginHelper;
import gg.solarmc.upgradeablesword.UpgradeableSword;
import gg.solarmc.upgradeablesword.config.Config;
import gg.solarmc.upgradeablesword.config.LevelConfig;
import gg.solarmc.upgradeablesword.enchantments.PluginEnchants;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PluginEvent implements Listener, UpgradeableSwordEvent {
    private final Map<UUID, PlayerData> playerData;
    private final UpgradeableSword plugin;
    private final PluginHelper helper;
    private final NamespacedKey xpKey;

    public PluginEvent(UpgradeableSword plugin, PluginHelper helper) {
        xpKey = new NamespacedKey(plugin, "solar_xp");
        this.plugin = plugin;
        this.helper = helper;
        playerData = new HashMap<>();
    }

    @EventHandler
    public void onPlayerHitPlayer(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player damagedPlayer && event.getDamager() instanceof Player damager) {
            ItemStack item = damager.getInventory().getItemInMainHand();
            if (item == null) return;
            ItemMeta meta = item.getItemMeta();
            Config config = plugin.getPluginConfig();

            if (!(ChatColor.stripColor(meta.getDisplayName())).equals(config.swordName().replaceAll("&\\w", "")))
                return;

            List<String> lore = meta.getLore();
            final List<String> swordLore = helper.replaceSwordLore(config.swordLore(), damager.getDisplayName(), getSwordXP(item) + 1);

            for (int i = 0; i < swordLore.size(); i++) {
                lore.set(i, swordLore.get(i));
            }

            addSwordXP((Player) event.getDamager(), item);
            onLifeStealUsed(damager, damagedPlayer, item.getEnchantmentLevel(PluginEnchants.LIFE_STEAL));
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
        List<String> lore = meta.getLore();

        // Get the second last Lore , cause last is `Unbreakable`
        if (sword.containsEnchantment(PluginEnchants.LIFE_STEAL)) lore.remove(lore.size() - 2);
        lore.add(lore.size() - 2, "Life Steal " + helper.intToRoman(amplifier));

        meta.setLore(lore);
        sword.setItemMeta(meta);
    }

    @Override
    public void onLifeStealUsed(Player damager, Player playerDamaged, int lifeStealAmplifier) {
        UUID damagerId = damager.getUniqueId();
        UUID playerDamagedId = playerDamaged.getUniqueId();
        PlayerData damagerData = this.playerData.computeIfAbsent(damagerId, it -> new PlayerData(0, new UUID(0, 0), 0));

        if (playerDamagedId.equals(damagerData.lastDamagedPlayer())) {
            playerData.put(damagerId, damagerData.withHits(damagerData.hits() + 1).withLastDamagedPlayerHits(damagerData.lastDamagedPlayerHits() + 1));
        } else {
            playerData.put(damagerId, damagerData.withHits(0).withLastDamagedPlayer(playerDamagedId).withLastDamagedPlayerHits(1));
            return;
        }

        if (damagerData.lastDamagedPlayerHits() / 10 > plugin.getPluginConfig().maxHitsAlert() / 10) {
            damager.getServer().getOnlinePlayers()
                    .stream().filter(it -> it.hasPermission("usword.notifyBoosting"))
                    .forEach(it -> it.sendMessage(String.format(
                            ChatColor.BOLD + "" + ChatColor.GOLD + "!!! %s could be boosting (Hits to same Player : %s) !!!",
                            damager.getName(),
                            damagerData.lastDamagedPlayerHits()
                    )));
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
            item.addEnchantment(enchantment, amplifier);

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
    private double getSwordXP(ItemStack item) {
        /*
        PersistentDataContainer dataContainer = item.getItemMeta().getPersistentDataContainer();

        if (!dataContainer.has(xpKey, PersistentDataType.DOUBLE)) {
            dataContainer.set(xpKey, PersistentDataType.DOUBLE, 0);
            return 0;
        }

        return dataContainer.get(xpKey, PersistentDataType.DOUBLE);
        */

        List<String> lore = item.getLore();
        if (!lore.get(0).startsWith("xp")) {
            lore.add(0, "xp: 0.0");
            item.setLore(lore);
            return 0;
        }

        return Double.parseDouble(lore.get(0).split(":")[1].trim());
    }

    private void addSwordXP(Player player, ItemStack item) {
        double xp = plugin.getPluginConfig().xpRate();
        if (xp < 0) xp = 1.0 / xp;

        // item.getPersistentDataContainer().set(new NamespacedKey(plugin, "solar_xp"), PersistentDataType.DOUBLE, getSwordXP(item) + xp);
        List<String> lore = item.getLore();

        lore.set(0, "xp: " + getSwordXP(item) + xp);
        item.setLore(lore);

        onSwordXpIncrease(player, item);
    }
}
