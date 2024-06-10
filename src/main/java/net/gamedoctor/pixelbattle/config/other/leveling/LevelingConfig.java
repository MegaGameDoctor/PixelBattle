package net.gamedoctor.pixelbattle.config.other.leveling;

import lombok.Getter;
import net.gamedoctor.pixelbattle.PixelBattle;
import net.gamedoctor.pixelbattle.utils.Utils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;

@Getter
public class LevelingConfig {
    private final PixelBattle plugin;
    private final int defaultLevel;
    private final boolean stillAddExpWhenMax;
    private final HashMap<Integer, Level> levelToExp = new HashMap<>();
    private final String format_colors;
    private final String format_cooldown;
    private final String format_no;
    private final boolean removePixelsWhenPainted_removeExp;
    private final boolean removePixelsWhenPainted_onlyOther;

    public LevelingConfig(PixelBattle plugin, boolean removePixelsWhenPainted) {
        this.plugin = plugin;
        String path = "leveling.";
        FileConfiguration cfg = plugin.getConfig();
        Utils utils = plugin.getUtils();

        if (cfg.isConfigurationSection(path) && cfg.getBoolean(path + "enable")) {
            defaultLevel = cfg.getInt(path + "defaultLevel");
            stillAddExpWhenMax = cfg.getBoolean(path + "stillAddExpWhenMax");
            format_colors = utils.color(cfg.getString(path + "format.colors"));
            format_cooldown = utils.color(cfg.getString(path + "format.cooldown"));
            format_no = utils.color(cfg.getString(path + "format.empty"));
            int lastPaintCooldown = cfg.getInt("settings.paintCooldown");
            for (String level : cfg.getConfigurationSection(path + "levels").getKeys(false)) {
                String levelPath = path + "levels." + level + ".";
                lastPaintCooldown = cfg.getInt(levelPath + "paintCooldown", lastPaintCooldown);
                levelToExp.put(Integer.parseInt(level), new Level(cfg.getInt(levelPath + "needExp"), lastPaintCooldown));
            }

            levelToExp.put(defaultLevel, new Level(0, cfg.getInt("settings.paintCooldown")));

            if (removePixelsWhenPainted) {
                removePixelsWhenPainted_removeExp = cfg.getBoolean("settings.removePixelsWhenPainted.removeExp", false);
                removePixelsWhenPainted_onlyOther = cfg.getBoolean("settings.removePixelsWhenPainted.onlyOther", true);
            } else {
                removePixelsWhenPainted_removeExp = false;
                removePixelsWhenPainted_onlyOther = true;
            }
        } else {
            format_colors = "";
            format_cooldown = "";
            format_no = "";
            defaultLevel = 0;
            stillAddExpWhenMax = false;
            removePixelsWhenPainted_removeExp = false;
            removePixelsWhenPainted_onlyOther = true;
            levelToExp.put(defaultLevel, new Level(0, cfg.getInt("settings.paintCooldown")));
        }
    }

    public Level getLevelData(int level) {
        return levelToExp.getOrDefault(level, levelToExp.get(defaultLevel));
    }

    public int getExpToNextLevel(int nowLevel) {
        nowLevel++;
        if (levelToExp.containsKey(nowLevel)) {
            return levelToExp.get(nowLevel).getNeedExp();
        } else {
            return 0;
        }
    }

    public boolean isMaxLevel(int level) {
        return !levelToExp.containsKey(level + 1);
    }

    public String getDisplayExpToNextLevel(int nowLevel) {
        int exp = getExpToNextLevel(nowLevel);
        if (exp == 0 && !stillAddExpWhenMax || isMaxLevel(nowLevel)) {
            return "-";
        } else {
            return plugin.getUtils().getFormattedNumber(exp);
        }
    }
}