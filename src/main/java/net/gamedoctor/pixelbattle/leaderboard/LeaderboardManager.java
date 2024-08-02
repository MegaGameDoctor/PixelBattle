package net.gamedoctor.pixelbattle.leaderboard;

import lombok.Getter;
import net.gamedoctor.pixelbattle.PixelBattle;
import net.gamedoctor.pixelbattle.config.other.Leaderboard;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.LinkedList;

public class LeaderboardManager {
    private final PixelBattle plugin;
    @Getter
    private final HashMap<String, Hologram> holograms = new HashMap<>();

    public LeaderboardManager(PixelBattle plugin) {
        this.plugin = plugin;
        createLeaderboards();
    }

    private void runUpdateTask(String value, int seconds) {
        new BukkitRunnable() {
            public void run() {
                update(value);
            }
        }.runTaskTimerAsynchronously(plugin, 20L * seconds, 20L * seconds);
    }

    public void update(String value) {
        Leaderboard leaderboard = plugin.getMainConfig().getLeaderboards().get(value);
        holograms.get(leaderboard.getValue()).update(getLines(leaderboard));
    }

    public void clearAll() {
        for (Hologram h : holograms.values()) {
            h.clearLocation();
        }
    }

    private void createLeaderboards() {
        for (String value : plugin.getMainConfig().getLeaderboards().keySet()) {
            Leaderboard leaderboard = plugin.getMainConfig().getLeaderboards().get(value);
            if (leaderboard.isEnable()) {
                holograms.put(value, new Hologram(plugin, leaderboard.getLocation(), getLines(leaderboard)));
                runUpdateTask(value, leaderboard.getUpdateDelay());
            }
        }
    }

    private LinkedList<String> getLines(Leaderboard leaderboard) {
        LinkedList<String> lines = new LinkedList<>(leaderboard.getTitle());
        int num = 0;
        HashMap<String, Integer> values = plugin.getDatabaseManager().getSortedForLeaderboard(leaderboard.getValue(), leaderboard.getRows(), leaderboard.getValueType());

        for (String player : values.keySet()) {
            String resultValue = switch (leaderboard.getValueType()) {
                case INT -> plugin.getUtils().getFormattedNumber(values.get(player));
                case TIME -> plugin.getUtils().getTimeString(values.get(player) * 1000L);
                default -> "NONE";
            };
            num++;
            lines.add(leaderboard.getFormat()
                    .replace("%player%", player)
                    .replace("%num%", String.valueOf(num))
                    .replace("%value%", resultValue));
        }

        for (int i = num; i < leaderboard.getRows(); i++) {
            lines.add(leaderboard.getEmptyFormat()
                    .replace("%num%", String.valueOf(i + 1)));
        }

        return lines;
    }
}
