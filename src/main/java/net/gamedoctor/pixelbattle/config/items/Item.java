package net.gamedoctor.pixelbattle.config.items;

import lombok.Getter;
import lombok.Setter;
import net.gamedoctor.pixelbattle.PixelBattle;
import net.gamedoctor.pixelbattle.utils.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;

@Getter
public class Item {
    private final String name;
    private final boolean glowing;
    @Setter
    private LinkedList<String> lore = new LinkedList<>();
    @Setter
    private Material material;
    private ItemStack itemStack;

    public Item(PixelBattle plugin, String path) {
        FileConfiguration cfg = plugin.getConfig();
        Utils utils = plugin.getUtils();

        if (cfg.isSet(path + "lore")) {
            for (String line : cfg.getStringList(path + "lore")) {
                lore.add(utils.color(line));
            }
        }

        glowing = cfg.isSet(path + "glowing") && cfg.getBoolean(path + "glowing");

        name = utils.color(cfg.getString(path + "name"));
    }

    public ItemStack recreateItemStack(PixelBattle plugin, LinkedList<String> lore) {
        return plugin.getUtils().makeItem(material, name, lore, glowing);
    }

    public ItemStack recreateItemStack(PixelBattle plugin, LinkedList<String> lore, String name) {
        return plugin.getUtils().makeItem(material, name, lore, glowing);
    }

    public ItemStack recreateItemStack(PixelBattle plugin, LinkedList<String> lore, String name, Material material) {
        return plugin.getUtils().makeItem(material, name, lore, glowing);
    }

    protected void prepareItemStack(PixelBattle plugin) {
        itemStack = plugin.getUtils().makeItem(material, name, lore, glowing);
    }
}
