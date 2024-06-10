package net.gamedoctor.pixelbattle.config.items;

import lombok.Getter;
import net.gamedoctor.pixelbattle.PixelBattle;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.LinkedList;

@Getter
public class ColorItem extends Item {
    private final String permission;
    private final int needLevel;
    private final int givesExp;

    public ColorItem(PixelBattle plugin, String material, LinkedList<String> defaultLore) {
        super(plugin, "items." + material + ".");
        String path = "items." + material + ".";
        FileConfiguration cfg = plugin.getConfig();

        this.setMaterial(Material.matchMaterial(material.toUpperCase()));
        permission = cfg.getString(path + "permission", "-");
        if (cfg.getBoolean("leveling.enable", false)) {
            needLevel = cfg.getInt(path + "needLevel", cfg.getInt("leveling.defaultLevel"));
            givesExp = cfg.getInt(path + "givesExp", 0);
        } else {
            needLevel = 0;
            givesExp = 0;
        }

        if (!defaultLore.isEmpty()) {
            LinkedList<String> finalLore = new LinkedList<>();
            for (String line : defaultLore) {
                if (line.contains("%itemLore%")) {
                    if (!getLore().isEmpty()) {
                        finalLore.addAll(getLore());
                    }
                } else {
                    finalLore.add(line
                            .replace("%level%", String.valueOf(needLevel))
                            .replace("%exp%", plugin.getUtils().getFormattedNumber(givesExp)));
                }
            }
            setLore(finalLore);
        }

        this.prepareItemStack(plugin);
    }
}