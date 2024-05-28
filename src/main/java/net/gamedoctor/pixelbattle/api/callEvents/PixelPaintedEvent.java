package net.gamedoctor.pixelbattle.api.callEvents;

import lombok.Getter;
import net.gamedoctor.pixelbattle.PixelPlayer;
import net.gamedoctor.pixelbattle.database.data.PaintedPixel;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

@Getter
public class PixelPaintedEvent extends PlayerEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final PixelPlayer pixelPlayer;
    private final Location paintedBlock;
    private final PaintedPixel previousPixel;
    private final PaintedPixel newPixel;

    public PixelPaintedEvent(Player who, PixelPlayer pixelPlayer, Location paintedBlock, PaintedPixel newPixel, PaintedPixel previousPixel) {
        super(who);
        this.player = who;
        this.pixelPlayer = pixelPlayer;
        this.paintedBlock = paintedBlock;
        this.previousPixel = previousPixel;
        this.newPixel = newPixel;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}