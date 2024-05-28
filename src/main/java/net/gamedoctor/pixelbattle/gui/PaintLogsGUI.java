package net.gamedoctor.pixelbattle.gui;

import lombok.Getter;
import net.gamedoctor.pixelbattle.PixelBattle;
import net.gamedoctor.pixelbattle.config.items.MenuItem;
import net.gamedoctor.pixelbattle.database.data.PaintedPixel;
import net.gamedoctor.pixelbattle.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.LinkedList;

public class PaintLogsGUI {
    private final PixelBattle plugin;
    private final Location clickedBlockLocation;
    @Getter
    private final Inventory inventory;
    private final Player player;
    @Getter
    private int invSize;
    @Getter
    private int page;

    public PaintLogsGUI(PixelBattle plugin, Player player, Location blockLocation) {
        this.plugin = plugin;
        this.player = player;
        this.clickedBlockLocation = blockLocation;
        int itemsCount = plugin.getDatabaseManager().getPaintedPixels().getOrDefault(blockLocation, new LinkedList<>()).size();

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

        inventory = Bukkit.getServer().createInventory(null, invSize, plugin.getMainConfig().getGui_paintLogsTitle()
                .replace("%x%", String.valueOf(blockLocation.getBlockX()))
                .replace("%y%", String.valueOf(blockLocation.getBlockY()))
                .replace("%z%", String.valueOf(blockLocation.getBlockZ())));
        open(1);
    }

    public void open(int page) {
        if (page == 0) {
            ChooseColorGUI chooseColorGUI = plugin.getWaitingForChoose().get(player.getName());
            chooseColorGUI.open(chooseColorGUI.getPage());
            return;
        }
        this.page = page;
        Utils utils = plugin.getUtils();
        LinkedList<PaintedPixel> paintedPixels = reverseLinkedList(plugin.getDatabaseManager().getPaintedPixels().getOrDefault(clickedBlockLocation, new LinkedList<>()));
        resetInv();
        int itemsPerPage = 27;
        int itemsToSkip = itemsPerPage * (page - 1);
        int slot = 0;

        if (!paintedPixels.isEmpty()) {
            for (PaintedPixel paintedPixel : paintedPixels) {
                if (slot + 1 > itemsPerPage) break;

                if (itemsToSkip > 0) {
                    itemsToSkip--;
                    continue;
                }

                MenuItem menuItem = plugin.getMainConfig().getMenu_paintedPixelInfo();
                LinkedList<String> lore = new LinkedList<>();
                for (String line : menuItem.getLore()) {
                    lore.add(utils.replaceDateAndTime(line
                                    .replace("%color%", paintedPixel.getColor().getName())
                                    .replace("%player%", paintedPixel.getPlayer()),
                            paintedPixel.getDate()
                    ));
                }

                utils.setMenuItem(player, inventory, slot, menuItem, lore, menuItem.getName().replace("%colorName%", paintedPixel.getColor().getName()), paintedPixel.getColor().getMaterial());

                slot++;
            }
        } else {
            for (int i = 0; i <= invSize - 9; i++) {
                inventory.setItem(i, plugin.getUtils().makeItem(Material.BARRIER, " ", new LinkedList<>(), false));
            }
        }

        if (paintedPixels.size() > itemsPerPage * page) {
            //inventory.setItem(invSize - 1, plugin.getUtils().makeItem(plugin.getMainConfig().getMenu_next()));
            utils.setMenuItem(player, inventory, invSize - 1, plugin.getMainConfig().getMenu_next());
        }

        utils.setMenuItem(player, inventory, invSize - 9, plugin.getMainConfig().getMenu_back());

        player.openInventory(inventory);
    }

    private LinkedList<PaintedPixel> reverseLinkedList(LinkedList<PaintedPixel> source) {
        LinkedList<PaintedPixel> revLinkedList = new LinkedList<>();
        for (int i = source.size() - 1; i >= 0; i--) {
            revLinkedList.add(source.get(i));
        }

        return revLinkedList;
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