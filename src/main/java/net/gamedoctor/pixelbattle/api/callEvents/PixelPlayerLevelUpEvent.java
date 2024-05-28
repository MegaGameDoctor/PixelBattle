package net.gamedoctor.pixelbattle.api.callEvents;

import lombok.Getter;
import net.gamedoctor.pixelbattle.PixelPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

@Getter
public class PixelPlayerLevelUpEvent extends PlayerEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final PixelPlayer pixelPlayer;
    private final int previousLevel;
    private final int newLevel;

    public PixelPlayerLevelUpEvent(Player who, PixelPlayer pixelPlayer, int previousLevel, int newLevel) {
        super(who);
        this.player = who;
        this.pixelPlayer = pixelPlayer;
        this.previousLevel = previousLevel;
        this.newLevel = newLevel;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}