package net.gamedoctor.pixelbattle.config.items;

import lombok.Getter;
import net.gamedoctor.pixelbattle.PixelBattle;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public class MenuItem extends Item {
    private final boolean enable;
    private final String permission;

    public MenuItem(PixelBattle plugin, String name) {
        super(plugin, "gui.items." + name + ".");
        String path = "gui.items." + name + ".";
        FileConfiguration cfg = plugin.getConfig();

        this.setMaterial(Material.matchMaterial(cfg.getString(path + "material", "BARRIER").toUpperCase()));
        enable = cfg.getBoolean(path + "enable", false);

        permission = cfg.getString(path + "permission", "-");

        this.prepareItemStack(plugin);
    }
}
