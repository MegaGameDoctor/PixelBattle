package net.gamedoctor.pixelbattle.config.other;

import lombok.Getter;
import net.gamedoctor.pixelbattle.PixelBattle;
import net.gamedoctor.pixelbattle.utils.Utils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.LinkedList;

@Getter
public class BoardConfig {
    private final boolean enable;
    private final int refreshTime;
    private final String name;
    private LinkedList<String> lines = new LinkedList<>();

    public BoardConfig(PixelBattle plugin) {
        String path = "scoreboard.";
        FileConfiguration cfg = plugin.getConfig();
        Utils utils = plugin.getUtils();

        enable = cfg.getBoolean(path + "enable");
        name = utils.color(cfg.getString(path + "name"));
        refreshTime = cfg.getInt(path + "refreshTime");
        for (String line : cfg.getStringList(path + "lines")) {
            lines.add(utils.color(line));
        }

        lines = reverseLines(lines);
    }

    private LinkedList<String> reverseLines(LinkedList<String> list) {
        LinkedList<String> revLinkedList = new LinkedList<>();

        for (int i = list.size() - 1; i >= 0; i--) {
            revLinkedList.add(list.get(i));
        }

        return revLinkedList;
    }
}