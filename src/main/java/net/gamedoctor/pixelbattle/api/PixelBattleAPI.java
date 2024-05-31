package net.gamedoctor.pixelbattle.api;

import lombok.RequiredArgsConstructor;
import net.gamedoctor.pixelbattle.PixelBattle;
import net.gamedoctor.pixelbattle.PixelPlayer;
import net.gamedoctor.pixelbattle.database.data.CanvasFrame;
import org.bukkit.Location;
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
}