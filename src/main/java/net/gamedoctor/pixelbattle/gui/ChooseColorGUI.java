package net.gamedoctor.pixelbattle.gui;

import lombok.Getter;
import net.gamedoctor.pixelbattle.PixelBattle;
import net.gamedoctor.pixelbattle.PixelPlayer;
import net.gamedoctor.pixelbattle.config.items.ColorItem;
import net.gamedoctor.pixelbattle.database.data.PaintedPixel;
import net.gamedoctor.pixelbattle.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.LinkedList;

public class ChooseColorGUI {
    private final PixelBattle plugin;
    @Getter
    private final Location clickedBlockLocation;
    @Getter
    private final Inventory inventory;
    private final Player player;
    @Getter
    private int invSize;
    @Getter
    private int page;

    public ChooseColorGUI(PixelBattle plugin, Player player, Location blockLocation) {
        this.plugin = plugin;
        this.player = player;
        this.clickedBlockLocation = blockLocation;
        int itemsCount = plugin.getUtils().getAvailableColors(player, plugin.getDatabaseManager().getPlayer(player.getName()).getLevel()).size();

        if (itemsCount <= 9) {
            invSize = 9;
        } else if (itemsCount <= 18) {
            invSize = 18;
        } else if (itemsCount <= 27) {
            invSize = 27;
        } else {
            invSize = 36;
        }

        if (plugin.getMainConfig().getMenu_footer().isEnable() || plugin.getMainConfig().getMenu_info().isEnable()) {
            if (invSize != 36) invSize += 9;
        }

        inventory = Bukkit.getServer().createInventory(null, invSize, plugin.getMainConfig().getGui_colorSelectionTitle());
        open(1);
    }

    public void open(int page) {
        this.page = page;
        Utils utils = plugin.getUtils();
        PixelPlayer pixelPlayer = plugin.getDatabaseManager().getPlayer(player.getName());
        resetInv();
        int itemsPerPage = 27;
        int itemsToSkip = itemsPerPage * (page - 1);
        int slot = 0;
        for (Material material : plugin.getMainConfig().getItems().keySet()) {
            ColorItem colorItem = plugin.getMainConfig().getItems().get(material);
            if (!colorItem.getPermission().equals("-") && !player.hasPermission(colorItem.getPermission())) continue;
            if (colorItem.getNeedLevel() > pixelPlayer.getLevel()) continue;
            if (slot + 1 > itemsPerPage) break;

            if (itemsToSkip > 0) {
                itemsToSkip--;
                continue;
            }

            inventory.setItem(slot, colorItem.getItemStack());
            slot++;
        }

        if (plugin.getUtils().getAvailableColors(player, plugin.getDatabaseManager().getPlayer(player.getName()).getLevel()).size() > itemsPerPage * page) {
            //inventory.setItem(invSize - 1, plugin.getUtils().makeItem(plugin.getMainConfig().getMenu_next()));
            utils.setMenuItem(player, inventory, invSize - 1, plugin.getMainConfig().getMenu_next());
        }

        if (page > 1) {
            //inventory.setItem(invSize - 9, plugin.getUtils().makeItem(plugin.getMainConfig().getMenu_back()));
            utils.setMenuItem(player, inventory, invSize - 9, plugin.getMainConfig().getMenu_back());
        }

        PaintedPixel paintedPixel = plugin.getDatabaseManager().getPixelData(clickedBlockLocation);
        LinkedList<String> lore = new LinkedList<>();
        int paintedCount = plugin.getDatabaseManager().getPaintedPixels().getOrDefault(clickedBlockLocation, new LinkedList<>()).size();
        for (String line : plugin.getMainConfig().getMenu_info().getLore()) {
            lore.add(utils.replaceDateAndTime(line
                            .replace("%color%", paintedPixel.getColor().getName())
                            .replace("%player%", paintedPixel.getPlayer())
                            .replace("%paintedCount%", String.valueOf(paintedCount)),
                    paintedPixel.getDate()
            ));
        }

        //inventory.setItem(invSize - 5, plugin.getUtils().makeItem(plugin.getMainConfig().getMenu_info(), lore));
        //if (invSize == 36)
        utils.setMenuItem(player, inventory, invSize - 5, plugin.getMainConfig().getMenu_info(), lore);

        player.openInventory(inventory);
    }

    private void resetInv() {
        inventory.clear();
        //if (this.invSize == 36) {
        for (int i = invSize - 9; i < invSize; i++) {
            //this.inventory.setItem(i, plugin.getUtils().makeItem(plugin.getMainConfig().getMenu_footer()));
            plugin.getUtils().setMenuItem(player, inventory, i, plugin.getMainConfig().getMenu_footer());
        }
        //}
    }
}