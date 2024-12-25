package net.gamedoctor.pixelbattle.utils;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;

public class RestorablePlayerData {
    private final Player player;
    private final ItemStack[] inventoryContents;
    private final float exp;
    private final int level;
    private final int foodLevel;
    private final Collection<PotionEffect> potionEffects;
    private final int fireTicks;
    private final GameMode gameMode;
    private final boolean flying;
    private final boolean allowFlight;
    private final Location location;

    public RestorablePlayerData(Player player) {
        this.player = player;
        inventoryContents = player.getInventory().getContents();
        exp = player.getExp();
        level = player.getLevel();
        foodLevel = player.getFoodLevel();
        potionEffects = player.getActivePotionEffects();
        player.getActivePotionEffects().clear();
        fireTicks = player.getFireTicks();
        gameMode = player.getGameMode();
        allowFlight = player.getAllowFlight();
        flying = player.isFlying();
        location = player.getLocation();
    }

    public void restore(boolean teleportToPrevious) {
        player.getInventory().setContents(inventoryContents);
        player.setExp(exp);
        player.setLevel(level);
        player.setFoodLevel(foodLevel);
        player.setFireTicks(fireTicks);
        player.setGameMode(gameMode);
        player.setAllowFlight(allowFlight);
        player.setFlying(flying);
        for (PotionEffect potionEffect : potionEffects) {
            player.addPotionEffect(potionEffect);
        }
        if (teleportToPrevious)
            player.teleport(location);
    }
}
