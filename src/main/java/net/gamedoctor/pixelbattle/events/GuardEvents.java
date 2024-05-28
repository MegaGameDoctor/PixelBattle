package net.gamedoctor.pixelbattle.events;

import lombok.RequiredArgsConstructor;
import net.gamedoctor.pixelbattle.PixelBattle;
import net.gamedoctor.pixelbattle.leaderboard.Hologram;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

@RequiredArgsConstructor
public class GuardEvents implements Listener {
    private final PixelBattle plugin;

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (plugin.isInPixelBattle(event.getPlayer())) {
            if (plugin.getMainConfig().isBlockModify()) {
                if (plugin.getMainConfig().getCanvas().contains(event.getBlock().getLocation())) {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (plugin.isInPixelBattle(event.getPlayer())) {
            if (plugin.getMainConfig().isGuardPlayer()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onSwapItem(PlayerSwapHandItemsEvent event) {
        if (plugin.isInPixelBattle(event.getPlayer())) {
            if (plugin.getMainConfig().isGuardPlayer()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (plugin.isInPixelBattle(event.getPlayer())) {
            if (!plugin.getMainConfig().isBlockModify()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (plugin.isInPixelBattle(player)) {
                if (plugin.getMainConfig().isAllowKnock() && event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
                    event.setDamage(0);
                } else if (plugin.getMainConfig().isGuardPlayer()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player player) {
            if (plugin.isInPixelBattle(player) && plugin.getMainConfig().isGuardPlayer()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof ArmorStand armorStand) {
            for (Hologram h : plugin.getLeaderboardManager().getHolograms().values()) {
                if (h.getEntitylist().contains(armorStand.getUniqueId())) {
                    event.setCancelled(true);
                    break;
                }
            }
        } else if (event.getDamager() instanceof Player player) {
            if (plugin.isInPixelBattle(player) && plugin.getMainConfig().isGuardPlayer() && !plugin.getMainConfig().isAllowKnock()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (plugin.isInPixelBattle(player) && plugin.getMainConfig().isGuardPlayer()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFall(PlayerMoveEvent event) {
        if (event.getFrom().getY() <= 0) {
            Player player = event.getPlayer();
            if (plugin.isInPixelBattle(player) && plugin.getMainConfig().isNoFall()) {
                player.teleport(plugin.getMainConfig().getSpawn());
            }
        }
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (plugin.isInPixelBattle(player) && plugin.getMainConfig().isPreventBlockInteract()) {
            event.setCancelled(true);
        }
    }
}