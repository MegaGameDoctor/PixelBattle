package net.gamedoctor.pixelbattle.PlaceholderAPI;

import lombok.RequiredArgsConstructor;
import net.gamedoctor.pixelbattle.PixelBattle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public final class Placeholders extends Placeholder {
    private final PixelBattle plugin;

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.equalsIgnoreCase("playedTime") && player != null) {
            if (plugin.isInPixelBattle(player)) {
                return plugin.getUtils().getTimeString(plugin.getDatabaseManager().getPlayer(player.getName()).getPlayedTime());
            } else {
                return "-";
            }
        } else if (params.equalsIgnoreCase("painted") && player != null) {
            if (plugin.isInPixelBattle(player)) {
                return String.valueOf(plugin.getDatabaseManager().getPlayer(player.getName()).getPainted());
            } else {
                return "-";
            }
        } else if (params.equalsIgnoreCase("exp") && player != null) {
            if (plugin.isInPixelBattle(player)) {
                return String.valueOf(plugin.getDatabaseManager().getPlayer(player.getName()).getDisplayExp());
            } else {
                return "-";
            }
        } else if (params.equalsIgnoreCase("expToNextLevel") && player != null) {
            if (plugin.isInPixelBattle(player)) {
                return plugin.getMainConfig().getLevelingConfig().getDisplayExpToNextLevel(plugin.getDatabaseManager().getPlayer(player.getName()).getLevel());
            } else {
                return "-";
            }
        } else if (params.equalsIgnoreCase("level") && player != null) {
            if (plugin.isInPixelBattle(player)) {
                return String.valueOf(plugin.getDatabaseManager().getPlayer(player.getName()).getLevel());
            } else {
                return "-";
            }
        } else if (params.equalsIgnoreCase("nextPixel") && player != null) {
            if (plugin.isInPixelBattle(player)) {
                return plugin.getUtils().getTimeToNextPixel(player, true);
            } else {
                return "-";
            }
        } else if (params.equalsIgnoreCase("inPixelBattle")) {
            return String.valueOf(plugin.getDatabaseManager().getLoadedPlayers().size());
        }

        return "NONE";
    }
}
