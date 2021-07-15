package gg.solarmc.upgradeablesword;

import java.util.List;

public class PluginHelper {
    public List<String> replaceSwordLore(List<String> lore, String playerName, double xp) {
        return lore.stream()
                .map(it -> it.replace("{playerName}", playerName)
                        .replace("{xp}", String.valueOf(xp)))
                .toList();
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
}
