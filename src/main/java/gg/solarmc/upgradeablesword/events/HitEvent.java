package gg.solarmc.upgradeablesword.events;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.Association;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import gg.solarmc.clans.SolarClans;
import gg.solarmc.clans.helper.ClanHelper;
import gg.solarmc.clans.helper.ClanRelation;
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
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class HitEvent implements Listener, UpgradeableSwordEvent {
    private final Map<UUID, PlayerData> playerData;
    private final UpgradeableSword plugin;
    private final PluginHelper helper;
    private final PluginEnchants enchants;
    private final Logger LOGGER = LoggerFactory.getLogger(HitEvent.class);

    public HitEvent(UpgradeableSword plugin, PluginEnchants enchants) {
        this.plugin = plugin;
        this.helper = plugin.getHelper();
        this.enchants = enchants;
        playerData = new HashMap<>();
    }

    @EventHandler
    public void onPlayerHitPlayer(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player damaged && event.getDamager() instanceof Player damager) {
            WorldGuardPlugin wgManager = plugin.getWorldGuardManager();
            ApplicableRegionSet damagerRegions = wgManager.getRegionManager(damager.getWorld()).getApplicableRegions(damager.getLocation());
            ApplicableRegionSet damagedRegions = wgManager.getRegionManager(damaged.getWorld()).getApplicableRegions(damaged.getLocation());

            if (damagerRegions.queryState(r -> Association.NON_MEMBER, DefaultFlag.PVP) == StateFlag.State.DENY
                    || damagedRegions.queryState(r -> Association.NON_MEMBER, DefaultFlag.PVP) == StateFlag.State.DENY) {
                return;
            }

            SolarClans clan = plugin.getClansManager();
            ClanHelper clanHelper = clan.getClanHelper();
            ClanRelation relation = clanHelper.getRelation(damager, damaged);
            if (relation == ClanRelation.MEMBER) {
                if (clan.getClanPvpHelper().isPvpOn(clanHelper.getClan(damager))) return;
            }
            if (relation == ClanRelation.ALLY) {
                if (clan.getAllyPvpHelper().isPvpOn(clanHelper.getClan(damager))
                        || clan.getAllyPvpHelper().isPvpOn(clanHelper.getClan(damaged))) return;
            }

            ItemStack item = damager.getInventory().getItemInMainHand();
            if (item == null || item.getType() == Material.AIR) return;
            ItemMeta meta = item.getItemMeta();
            Config config = plugin.getPluginConfig();

            if (!meta.hasDisplayName()) return;

            if (!(helper.stripColorCode(meta.displayName())).equals(config.swordName().replaceAll("&\\w", "")))
                return;

            helper.addSwordXP(item);
            onSwordXpIncrease(damager, item);
            onLifeStealUsed(damager, damaged, enchants.getEnchantmentLevel(item, PluginEnchants.LIFE_STEAL));
        }
    }

    @Override
    public void onSwordXpIncrease(Player player, ItemStack item) {
        LevelConfig config = plugin.getPluginConfig().getLevelConfig();
        int xp = (int) helper.getSwordXP(item).doubleValue();

        checkAndAddEnchantment(player, item, Enchantment.DAMAGE_ALL, xp, config.sharpness());
        checkAndAddEnchantment(player, item, Enchantment.FIRE_ASPECT, xp, config.fireAspect());
        checkAndAddEnchantment(player, item, PluginEnchants.LIFE_STEAL, xp, config.lifeSteal());
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onLifeStealEnchantmentAdd(ItemStack sword, int amplifier) {
        if (amplifier == 0) return;
        final ItemMeta meta = sword.getItemMeta();
        List<Component> lore = new ArrayList<>(meta.lore());

        if (enchants.containsEnchantment(sword, PluginEnchants.LIFE_STEAL) && lore.size() != 2) lore.remove(0);
        lore.add(0, Component.text(ChatColor.GRAY + "Life Steal " + helper.intToRoman(amplifier)));

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

        if (lastDamagedPlayerHits > plugin.getPluginConfig().maxHitsAlert()) {
            TextComponent msg = Component.text("(")
                    .append(Component.text("!!").style(Style.style(NamedTextColor.RED, TextDecoration.BOLD)))
                    .append(Component.text(
                            String.format(") %s could be boosting (Hits to same Player : %s)",
                                    damager.getName(), lastDamagedPlayerHits)));
            playerData.put(damagerId, damagerData.withLastDamagedPlayerHits(1));
            damager.getServer().getOnlinePlayers()
                    .stream().filter(it -> it.hasPermission("usword.notifyBoosting"))
                    .forEach(it -> it.sendMessage(msg));
            LOGGER.warn("Notified Boosting (" + damager.getName() + "): ");
        }

        if (damagerData.hits() == 4) {
            playerData.put(damagerId, damagerData.withHits(0));

            double damagerHealth = Math.min(damager.getHealth() + lifeStealAmplifier, damager.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            damager.setHealth(damagerHealth);

            // double playerDamagedHealth = Math.max(0, playerDamaged.getHealth() - lifeStealAmplifier);
            // playerDamaged.setHealth(playerDamagedHealth);
        }
    }

    private void checkAndAddEnchantment(Player player, ItemStack item, Enchantment enchantment, int xp, List<Integer> levels) {
        if (levels.contains(xp)) {
            int amplifier = levels.indexOf(xp);
            enchants.addEnchantment(item, enchantment, amplifier);

            player.sendMessage(helper.translateColorCode(plugin.getPluginConfig().levelUpMessage()
                    .replace("{enchantment}", getName(enchantment))
                    .replace("{level}", helper.intToRoman(amplifier))));

            if (enchantment == PluginEnchants.LIFE_STEAL) onLifeStealEnchantmentAdd(item, amplifier);
        }
    }

    private String getName(Enchantment enchantment) {
        if (enchantment.equals(Enchantment.DAMAGE_ALL)) return "Sharpness";
        else if (enchantment.equals(Enchantment.FIRE_ASPECT)) return "Fire Aspect";
        else if (enchantment.equals(PluginEnchants.LIFE_STEAL)) return "Life Steal";
        else return enchantment.getName();
    }
}
