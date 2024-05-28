package net.gamedoctor.pixelbattle;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.gamedoctor.pixelbattle.api.callEvents.PixelPlayerLevelUpEvent;
import net.gamedoctor.pixelbattle.config.Config;
import net.gamedoctor.pixelbattle.config.messages.Placeholder;
import net.gamedoctor.pixelbattle.config.other.leveling.Level;
import net.gamedoctor.pixelbattle.config.other.leveling.LevelingConfig;
import net.gamedoctor.pixelbattle.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

@AllArgsConstructor
public class PixelPlayer {
    private final PixelBattle plugin;
    @Getter
    private final String name;
    @Getter
    @Setter
    private int painted;
    @Getter
    @Setter
    private long nextPixel;
    @Getter
    @Setter
    private long joinDate;
    @Setter
    private long playedTime;
    @Getter
    @Setter
    private int exp;
    @Getter
    @Setter
    private int level;

    public void addPainted() {
        this.painted++;
    }

    public void removePainted() {
        if (this.painted > 0) this.painted--;
    }

    public void addExp(int exp) {
        if (exp <= 0) return;
        Config cfg = plugin.getMainConfig();
        LevelingConfig levelingConfig = cfg.getLevelingConfig();
        if (levelingConfig.getExpToNextLevel(level) == 0 && !levelingConfig.isStillAddExpWhenMax()) return;
        cfg.getMessage_expReceived().display(getBukkitPlayer(), new Placeholder("%pExp%", String.valueOf(this.exp)), new Placeholder("%exp%", String.valueOf(exp)));
        this.exp += exp;
        HashMap<Integer, Level> levelToExp = levelingConfig.getLevelToExp();
        int originalLevel = level;
        int nextLevel = level + 1;

        while (levelToExp.containsKey(nextLevel) && levelToExp.get(nextLevel).getNeedExp() <= this.exp) {
            level++;
            this.exp -= levelToExp.get(nextLevel).getNeedExp();
            nextLevel++;
        }

        if (originalLevel != level) {
            String newFeatures = "";
            Utils utils = plugin.getUtils();
            List<String> newColors = utils.getNewColors(level);
            if (!newColors.isEmpty()) {
                newFeatures += levelingConfig.getFormat_colors().replace("%colors%", String.join("§7, ", newColors));
            }
            if (levelingConfig.getLevelData(level - 1).getPaintCooldown() != levelingConfig.getLevelData(level).getPaintCooldown()) {
                if (!newFeatures.isEmpty())
                    newFeatures += "\n" + utils.color(plugin.getConfig().getString("settings.messagesPrefix"));
                newFeatures += levelingConfig.getFormat_cooldown().replace("%cooldown%", String.valueOf(levelingConfig.getLevelData(level).getPaintCooldown()));
            }
            if (newFeatures.isEmpty()) {
                newFeatures = levelingConfig.getFormat_no();
            }
            cfg.getMessage_levelUp().display(getBukkitPlayer(), new Placeholder("%pLevel%", String.valueOf(originalLevel)), new Placeholder("%level%", String.valueOf(level)), new Placeholder("%newFeatures%", newFeatures));

            Bukkit.getPluginManager().callEvent(new PixelPlayerLevelUpEvent(getBukkitPlayer(), this, level - 1, level));
        }
    }

    public String getDisplayExp() {
        Config cfg = plugin.getMainConfig();
        LevelingConfig levelingConfig = cfg.getLevelingConfig();
        if (levelingConfig.getExpToNextLevel(level) == 0 && !levelingConfig.isStillAddExpWhenMax()) {
            return "-";
        } else {
            return String.valueOf(exp);
        }
    }

    public Player getBukkitPlayer() {
        return Bukkit.getPlayerExact(name);
    }

    public long getPlayedTime() {
        return playedTime + (System.currentTimeMillis() - joinDate);
    }

    public Object getValueByName(String name) {
        switch (name.toLowerCase()) {
            case "painted":
                return this.painted;
            case "nextpixel":
                return this.nextPixel;
            case "level":
                return this.level;
            case "exp":
                return this.exp;
            case "playedtime":
                return getPlayedTime();
            default:
                return 0;
        }
    }
}