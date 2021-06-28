package gg.solarmc.upgradeablesword.config;

import space.arim.dazzleconf.annote.ConfComments;
import space.arim.dazzleconf.annote.ConfDefault;
import space.arim.dazzleconf.annote.ConfKey;

import java.util.List;

public interface LevelConfig {

    @ConfKey("sharpness")
    @ConfDefault.DefaultIntegers({0, 100, 500, 1000, 3000, 8000})
    @ConfComments("Levels for Sharpness enchantment")
    List<Integer> sharpness();

    @ConfKey("fireaspect")
    @ConfDefault.DefaultIntegers({0, 2000, 5000})
    @ConfComments("Levels for Fire Aspect enchantment")
    List<Integer> fireAspect();

    @ConfKey("lifesteal")
    @ConfDefault.DefaultIntegers({0, 4000, 7000, 10000})
    @ConfComments("Levels for Life-Steal enchantment")
    List<Integer> lifeSteal();

}
