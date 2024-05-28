package net.gamedoctor.pixelbattle.config.other;

import lombok.Getter;
import net.gamedoctor.pixelbattle.PixelBattle;
import net.gamedoctor.pixelbattle.leaderboard.ValueType;
import net.gamedoctor.pixelbattle.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.LinkedList;
import java.util.List;

@Getter
public class Leaderboard {
    private final boolean enable;
    private final List<String> title = new LinkedList<>();
    private String value;
    private int rows;
    private String format;
    private String emptyFormat;
    private int updateDelay;
    private Location location;
    private ValueType valueType;

    public Leaderboard(PixelBattle plugin, String value) {
        String path = "leaderboards." + value + ".";
        FileConfiguration cfg = plugin.getConfig();
        Utils utils = plugin.getUtils();

        this.enable = cfg.getBoolean(path + "enable");
        if (this.enable) {
            this.value = value;
            for (String line : cfg.getStringList(path + "title")) {
                this.title.add(utils.color(line));
            }
            this.rows = cfg.getInt(path + "rows");
            this.format = utils.color(cfg.getString(path + "format"));
            this.emptyFormat = utils.color(cfg.getString(path + "emptyFormat"));
            this.updateDelay = cfg.getInt(path + "updateDelay");
            this.location = new Location(Bukkit.getWorld(cfg.getString(path + "location.world")), cfg.getDouble(path + "location.x"), cfg.getDouble(path + "location.y"), cfg.getDouble(path + "location.z"));
            this.valueType = ValueType.valueOf(cfg.getString(path + "valueType").toUpperCase());
        }
    }
}
