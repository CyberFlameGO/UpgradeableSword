package gg.solarmc.upgradeablesword;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;

import java.util.List;

public class PluginHelper {
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
}
