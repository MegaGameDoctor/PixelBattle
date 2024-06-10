package net.gamedoctor.pixelbattle.board;

import me.clip.placeholderapi.PlaceholderAPI;
import net.gamedoctor.pixelbattle.PixelBattle;
import net.gamedoctor.pixelbattle.PixelPlayer;
import net.gamedoctor.pixelbattle.config.Config;
import net.gamedoctor.pixelbattle.config.other.BoardConfig;
import net.gamedoctor.pixelbattle.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class BoardManager {
    private final PixelBattle plugin;
    private final String prefix;

    public BoardManager(PixelBattle plugin) {
        this.plugin = plugin;
        this.prefix = "PB_board";
        BoardConfig boardConfig = plugin.getMainConfig().getBoardConfig();
        long refreshTime = boardConfig.getRefreshTime();
        if (boardConfig.isEnable() && refreshTime != 0) {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
                @Override
                public void run() {
                    for (String player : plugin.getDatabaseManager().getLoadedPlayers().keySet()) {
                        Player p = Bukkit.getPlayer(player);
                        if (p != null && p.isOnline()) {
                            updateScoreboard(p);
                        }
                    }
                }
            }, refreshTime, refreshTime);
        }
    }

    public void setScoreboard(Player player) {
        BoardConfig boardConfig = plugin.getMainConfig().getBoardConfig();
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective(prefix, Criteria.DUMMY, boardConfig.getName());
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        int lineCounter = boardConfig.getLines().size();
        for (String line : boardConfig.getLines()) {
            Team team = board.registerNewTeam(prefix + "_l_" + lineCounter);
            String name = ChatColor.values()[lineCounter].toString();
            team.addEntry(name);
            team.setSuffix(line);
            obj.getScore(name).setScore(0); // Or lineCounter if 0 not work
            lineCounter--;
        }

        player.setScoreboard(board);
        updateScoreboard(player);
    }

    public void updateScoreboard(Player player) {
        Config cfg = plugin.getMainConfig();
        BoardConfig boardConfig = cfg.getBoardConfig();
        Utils utils = plugin.getUtils();

        for (int index = boardConfig.getLines().size(); index > 0; index--) {
            PixelPlayer pixelPlayer = plugin.getDatabaseManager().getPlayer(player.getName());
            String suffix = boardConfig.getLines().get(boardConfig.getLines().size() - index)
                    .replace("%player%", player.getName())
                    .replace("%time%", utils.getTimeToNextPixel(player, true))
                    .replace("%painted%", utils.getFormattedNumber(pixelPlayer.getPainted()))
                    .replace("%playedTime%", utils.getTimeString(pixelPlayer.getPlayedTime()))
                    .replace("%exp%", pixelPlayer.getDisplayExp())
                    .replace("%expToNextLevel%", cfg.getLevelingConfig().getDisplayExpToNextLevel(pixelPlayer.getLevel()))
                    .replace("%level%", utils.getFormattedNumber(pixelPlayer.getLevel()));

            if (cfg.isUsingPlaceholderAPI()) {
                suffix = PlaceholderAPI.setPlaceholders(player, suffix);
            }

            player.getScoreboard().getTeam(prefix + "_l_" + index).setSuffix(suffix);
        }
    }

    public void removeScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }
}