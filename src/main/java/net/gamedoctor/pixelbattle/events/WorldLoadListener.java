package net.gamedoctor.pixelbattle.events;

import lombok.RequiredArgsConstructor;
import net.gamedoctor.pixelbattle.PixelBattle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.logging.Level;

@RequiredArgsConstructor
public class WorldLoadListener implements Listener {
    private final PixelBattle plugin;

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        String world = plugin.getConfig().getString("canvas.world");
        if (world != null && world.equalsIgnoreCase(e.getWorld().getName())) {
            e.getHandlers().unregister(this);
            plugin.getLogger().log(Level.INFO, "The world is loaded! Launching the plugin...");
            plugin.preparePlugin();
        }
    }
}