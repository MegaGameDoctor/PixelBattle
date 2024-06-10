package net.gamedoctor.pixelbattle.events;

import lombok.RequiredArgsConstructor;
import net.gamedoctor.pixelbattle.PixelBattle;
import net.gamedoctor.pixelbattle.config.messages.Placeholder;
import net.gamedoctor.pixelbattle.config.other.StandaloneServerConfig;
import net.gamedoctor.pixelbattle.gui.ChooseColorGUI;
import net.gamedoctor.pixelbattle.gui.PaintLogsGUI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class Events implements Listener {
    private final PixelBattle plugin;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (plugin.getMainConfig().getStandaloneServerConfig().isEnable()) {
            plugin.joinPixelBattle(event.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.exitPixelBattle(event.getPlayer());
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if ((event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && event.getHand() != EquipmentSlot.OFF_HAND && event.hasBlock() && event.getClickedBlock() != null && !event.hasItem()) {
            Location clickedBlock = event.getClickedBlock().getLocation();
            if (plugin.isInPixelBattle(player) && plugin.getMainConfig().getCanvas().contains(clickedBlock)) {
                if (!plugin.isActiveTimeLapse()) {
                    if (System.currentTimeMillis() > plugin.getDatabaseManager().getPlayer(player.getName()).getNextPixel()) {
                        plugin.getWaitingForChoose().put(player.getName(), new ChooseColorGUI(plugin, player, clickedBlock));
                    } else {
                        plugin.getMainConfig().getMessage_pixelDelay().display(player, new Placeholder("%time%", plugin.getUtils().getTimeToNextPixel(player, false)));
                    }
                } else {
                    plugin.getMainConfig().getMessage_timelapseAction().display(player);
                }
                event.setCancelled(true);
            }
        } else if ((event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)) && event.hasItem() && event.getItem() != null && event.getItem().getItemMeta() != null && plugin.isInPixelBattle(player) && plugin.getMainConfig().getExitItem() != null && event.getItem().getItemMeta().equals(plugin.getMainConfig().getExitItem().getItemMeta())) {
            StandaloneServerConfig standaloneServerConfig = plugin.getMainConfig().getStandaloneServerConfig();
            if (standaloneServerConfig.isEnable() && standaloneServerConfig.isRedirectOnExit_enable()) {
                plugin.getUtils().connectBungeeCordServer(player, standaloneServerConfig.getRedirectOnExit_server());
                plugin.getMainConfig().getMessage_movingToServer().display(player, new Placeholder("%server%", standaloneServerConfig.getRedirectOnExit_server()));
            } else {
                player.getInventory().setItem(plugin.getMainConfig().getExitItemSlot(), new ItemStack(Material.AIR));
                plugin.exitPixelBattle(player);
                plugin.getMainConfig().getMessage_exitItemExit().display(player);
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onColorSelect(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (plugin.isInPixelBattle(player)) {
            if (event.getCurrentItem() != null && plugin.getWaitingForChoose().containsKey(player.getName()) && plugin.getWaitingForChoose().get(player.getName()).getInventory().equals(event.getClickedInventory())) {
                Material material = event.getCurrentItem().getType();
                ChooseColorGUI chooseColorGUI = plugin.getWaitingForChoose().get(player.getName());
                if (event.getSlot() == chooseColorGUI.getInvSize() - 1 && material.equals(plugin.getMainConfig().getMenu_next().getMaterial())) {
                    chooseColorGUI.open(chooseColorGUI.getPage() + 1);
                } else if (event.getSlot() == chooseColorGUI.getInvSize() - 9 && material.equals(plugin.getMainConfig().getMenu_back().getMaterial())) {
                    chooseColorGUI.open(chooseColorGUI.getPage() - 1);
                } else if (event.getSlot() == chooseColorGUI.getInvSize() - 5 && material.equals(plugin.getMainConfig().getMenu_info().getMaterial()) && plugin.getMainConfig().getMenu_paintedPixelInfo().isEnable()) {
                    plugin.getOpenedPaintLogs().put(player.getName(), new PaintLogsGUI(plugin, player, chooseColorGUI.getClickedBlockLocation()));
                } else if (plugin.getMainConfig().getItems().containsKey(material) && event.getSlot() < 45) {
                    plugin.getWaitingForChoose().remove(player.getName());
                    player.closeInventory();
                    plugin.getUtils().paintPixel(player, material, chooseColorGUI.getClickedBlockLocation(), true, true, true, true);
                }
                event.setCancelled(true);
            } else if (event.getCurrentItem() != null && plugin.getOpenedPaintLogs().containsKey(player.getName()) && plugin.getOpenedPaintLogs().get(player.getName()).getInventory().equals(event.getClickedInventory())) {
                Material material = event.getCurrentItem().getType();
                PaintLogsGUI paintLogsGUI = plugin.getOpenedPaintLogs().get(player.getName());
                if (event.getSlot() == paintLogsGUI.getInvSize() - 1 && material.equals(plugin.getMainConfig().getMenu_next().getMaterial())) {
                    paintLogsGUI.open(paintLogsGUI.getPage() + 1);
                } else if (event.getSlot() == paintLogsGUI.getInvSize() - 9 && material.equals(plugin.getMainConfig().getMenu_back().getMaterial())) {
                    paintLogsGUI.open(paintLogsGUI.getPage() - 1);
                }
                event.setCancelled(true);
            } else if (plugin.getMainConfig().isGuardPlayer()) {
                event.setCancelled(true);
            }
        }
    }
}