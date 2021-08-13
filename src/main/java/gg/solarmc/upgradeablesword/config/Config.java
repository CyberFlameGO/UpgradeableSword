package gg.solarmc.upgradeablesword.config;

import space.arim.dazzleconf.annote.ConfComments;
import space.arim.dazzleconf.annote.ConfDefault;
import space.arim.dazzleconf.annote.ConfKey;
import space.arim.dazzleconf.annote.SubSection;

import java.util.List;

public interface Config {
    @ConfKey("enabled")
    @ConfDefault.DefaultBoolean(true)
    boolean isEnabled();

    @ConfKey("xprate")
    @ConfDefault.DefaultInteger(1)
    @ConfComments({"Rate of getting XP", "2 xprate means 2 xp per 1 hit", "-2 xprate means 1 xp per 2 hit"})
    int xpRate();

    @ConfKey("swordname")
    @ConfDefault.DefaultString("&l&4Monster of Death")
    @ConfComments("The Display Name of the Upgradeable Sword")
    String swordName();

    @ConfKey("swordLore")
    @ConfDefault.DefaultStrings({
            "{playerName}'s Upgradeable Sword",
            "Exp : {xp}"
    })
    @ConfComments({
            "Lore of the USword",
            "Variables :",
            "xp - XP of the sword",
            "playerName - name of the player who got the Sword",
            "Tell me if you want more"
    })
    List<String> swordLore();

    @ConfKey("swordGaveMessage")
    @ConfDefault.DefaultString("Check your inventory for the Upgradeable Sword!!!")
    @ConfComments({"Message sent after Using the usword command", "Gimme a better name ;-;"})
    String swordGaveMessage();

    @ConfKey("maxHitsAlert")
    @ConfDefault.DefaultInteger(30)
    @ConfComments({"The number of hits after which it should Alert staff", "Better name..."})
    int maxHitsAlert();

    @ConfKey("levelUpMessage")
    @ConfDefault.DefaultString("Your sword leveled up!! {enchantment} {level} Added.")
    @ConfComments({
            "Message after Level up (Enchantment Added)",
            "Variables :",
            "enchantment - The Enchantment added",
            "level - The Enchantment Level",
            "Tell me if you want more"
    })
    String levelUpMessage();

    @ConfKey("levels")
    @ConfComments({
            "Levels of Enchantment",
            "Each index of these Integers represents the xp for a level of The Enchantment",
            "eg. 100 xp is needed for sharpness level 1 in this default config and so on",
            "    5000 xp is needed for fire aspect level 2"
    })
    @SubSection LevelConfig getLevelConfig();

    @ConfKey("usword-cooldown")
    @ConfComments("The cooldown of /usword command only in seconds")
    @ConfDefault.DefaultLong(60 * 5L)
    long uSwordCoolDown();

}
