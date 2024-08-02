package net.gamedoctor.pixelbattle.config.other;

import lombok.Getter;
import net.gamedoctor.pixelbattle.PixelBattle;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public class StandaloneServerConfig {
    private final boolean enable;
    private final boolean redirectOnExit_enable;
    private final String redirectOnExit_server;

    public StandaloneServerConfig(PixelBattle plugin) {
        String path = "standaloneServer.";
        FileConfiguration cfg = plugin.getConfig();

        enable = cfg.getBoolean(path + "enable", false);
        redirectOnExit_enable = cfg.getBoolean(path + "redirectOnExit.enable", false);
        redirectOnExit_server = cfg.getString(path + "redirectOnExit.server", "-");
    }
}