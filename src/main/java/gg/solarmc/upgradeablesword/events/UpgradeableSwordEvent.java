package gg.solarmc.upgradeablesword.events;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface UpgradeableSwordEvent {

    void onSwordXpIncrease(Player player, ItemStack item);

    void onLifeStealEnchantmentAdd(ItemStack sword, int amplifier);

    void onLifeStealUsed(Player damager, Player playerDamaged, int lifeStealAmplifier);

}
