package net.gamedoctor.pixelbattle.utils;

import lombok.Getter;
import org.bukkit.Location;

import java.util.LinkedList;

public class LocationsCuboid {
    private final LinkedList<Location> locations = new LinkedList<>();
    @Getter
    private final boolean isVertical;
    private Location min;
    private Location max;

    public LocationsCuboid(Location first, Location second) {
        this.max = first;
        this.min = second;
        isVertical = first.getBlock().getY() != second.getBlock().getY();
        this.normalise();
    }

    private void normalise() {
        double minX, minY, minZ, maxX, maxY, maxZ;

        if (this.min.getX() < this.max.getX()) {
            minX = this.min.getX();
            maxX = this.max.getX();
        } else {
            minX = this.max.getX();
            maxX = this.min.getX();
        }

        if (this.min.getY() < this.max.getY()) {
            minY = this.min.getY();
            maxY = this.max.getY();
        } else {
            minY = this.max.getY();
            maxY = this.min.getY();
        }

        if (this.min.getZ() < this.max.getZ()) {
            minZ = this.min.getZ();
            maxZ = this.max.getZ();
        } else {
            minZ = this.max.getZ();
            maxZ = this.min.getZ();
        }
        this.min = new Location(this.min.getWorld(), minX, minY, minZ);
        this.max = new Location(this.min.getWorld(), maxX, maxY, maxZ);
    }

    private void fillLocations() {
        for (int y = this.min.getBlockY(); y <= this.max.getBlockY(); y++) {
            for (int x = this.min.getBlockX(); x <= this.max.getBlockX(); x++) {
                for (int z = this.min.getBlockZ(); z <= this.max.getBlockZ(); z++) {
                    this.locations.add(new Location(this.min.getWorld(), x, y, z));
                }
            }
        }
    }

    public LinkedList<Location> getLocations() {
        if (this.locations.isEmpty())
            this.fillLocations();
        return this.locations;
    }
}