package net.gamedoctor.pixelbattle.leaderboard;

import lombok.Getter;
import net.gamedoctor.pixelbattle.PixelBattle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Hologram {
    @Getter
    private final List<UUID> entitylist = new ArrayList<>();
    private final Location location;
    private final PixelBattle plugin;
    private LinkedList<String> lines;

    public Hologram(PixelBattle plugin, Location location, LinkedList<String> text) {
        this.plugin = plugin;
        this.lines = text;
        this.location = location;
        clearLocation();
        create();
    }

    public void update(LinkedList<String> text) {
        this.lines = text;
        new BukkitRunnable() {
            public void run() {
                Collection<Entity> entities = Objects.requireNonNull(location.getWorld()).getNearbyEntities(location, 0, lines.size(), 0);
                if (entities.size() > lines.size()) {
                    clearLocation();
                    create();
                }

                int i = 0;
                for (UUID uuid : entitylist) {
                    ArmorStand ar = (ArmorStand) Bukkit.getEntity(uuid);
                    if (ar != null) {
                        if (lines.size() > i) ar.setCustomName(lines.get(i));
                        i++;
                    }
                }
            }
        }.runTask(plugin);
    }

    public void clearLocation() {
        if (!location.getChunk().isLoaded()) {
            location.getChunk().load();
        }

        int radius = this.lines.size();
        for (UUID armorStand : entitylist) {
            ArmorStand ar = (ArmorStand) Bukkit.getEntity(armorStand);
            if (ar != null) {
                ar.remove();
            }
        }

        Collection<Entity> entities = Objects.requireNonNull(this.location.getWorld()).getNearbyEntities(this.location, 1, radius, 1);
        for (Entity en : entities) {
            if (en instanceof ArmorStand) {
                en.remove();
            }
        }
    }

    private void create() {
        double DISTANCE = 0.25D;
        Location loc = this.location.clone();
        loc.setY(loc.getY() + 2);
        for (String line : this.lines) {
            ArmorStand entity = (ArmorStand) this.location.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);

            entity.setCustomName(line);
            entity.setCustomNameVisible(true);
            entity.setInvisible(true);
            entity.setInvulnerable(true);
            entity.setGravity(false);
            entity.setMarker(true);
            this.entitylist.add(entity.getUniqueId());
            loc.subtract(0.0D, DISTANCE, 0.0D);
        }
    }
}