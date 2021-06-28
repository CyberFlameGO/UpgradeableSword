package gg.solarmc.upgradeablesword.config;

import space.arim.dazzleconf.annote.ConfComments;
import space.arim.dazzleconf.annote.ConfDefault;
import space.arim.dazzleconf.annote.ConfKey;

public interface Config {
    static LevelConfig setDefaultLevelConfig(LevelConfig defaultGiftsSection) {
        return defaultGiftsSection;
    }

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

    @ConfKey("levels")
    @ConfDefault.DefaultObject("setDefaultLevelConfig")
    @ConfComments({
            "Levels of Enchantment",
            "Each index of these Integers represents the xp for a level of The Enchantment",
            "eg. 100 xp is needed for sharpness level 1 in this default config and so on",
            "    5000 xp is needed for fire aspect level 2"
    })
    LevelConfig getLevelConfig();
}
