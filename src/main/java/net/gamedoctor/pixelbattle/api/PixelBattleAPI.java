package net.gamedoctor.pixelbattle.api;

import lombok.RequiredArgsConstructor;
import net.gamedoctor.pixelbattle.PixelBattle;
import net.gamedoctor.pixelbattle.PixelPlayer;
import net.gamedoctor.pixelbattle.database.data.CanvasFrame;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@RequiredArgsConstructor
public class PixelBattleAPI {
    private final PixelBattle plugin;

    public void joinPlayer(Player player) {
        plugin.joinPixelBattle(player);
    }

    public List<Location> getCanvasLocations() {
        return plugin.getMainConfig().getCanvas();
    }

    public void quitPlayer(Player player) {
        plugin.exitPixelBattle(player);
    }

    public boolean isInPixelBattle(Player player) {
        return plugin.isInPixelBattle(player);
    }

    public PixelPlayer getPixelPlayer(Player player) {
        return plugin.getDatabaseManager().getPlayer(player.getName());
    }

    public void updateLeaderboard(String type) {
        plugin.getLeaderboardManager().update(type);
    }

    public boolean isTimeLapseActive() {
        return plugin.isActiveTimeLapse();
    }

    public HashMap<String, CanvasFrame> getAllCanvasFrames() {
        return plugin.getDatabaseManager().getCanvasPixelsData();
    }

    public LinkedHashMap<Integer, CanvasFrame> getFramesForTimeLapse() {
        return plugin.getDatabaseManager().getFramesForTimeLapse();
    }

    /**
     * Must be called synchronously
     */
    public boolean paintPixel(Player player, Location blockLocation, Material color, boolean sendMessages) {
        return paintPixel(player, blockLocation, color, sendMessages, true, true, true);
    }

    /**
     * Must be called synchronously
     */
    public boolean paintPixel(Player player, Location blockLocation, Material color, boolean sendMessages, boolean addExp, boolean addPainted, boolean setCooldown) {
        if (!Bukkit.getServer().isPrimaryThread()) {
            throw new IllegalStateException("You can't color a pixel asynchronously!");
        } else if (plugin.getMainConfig().getCanvas().contains(blockLocation)) {
            return plugin.getUtils().paintPixel(player, color, blockLocation, sendMessages, addExp, addPainted, setCooldown);
        } else {
            return false;
        }
    }
}