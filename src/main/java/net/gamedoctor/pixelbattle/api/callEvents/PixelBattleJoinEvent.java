package net.gamedoctor.pixelbattle.api.callEvents;

import lombok.Getter;
import net.gamedoctor.pixelbattle.PixelPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

@Getter
public class PixelBattleJoinEvent extends PlayerEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final PixelPlayer pixelPlayer;

    public PixelBattleJoinEvent(Player who, PixelPlayer pixelPlayer) {
        super(who);
        this.player = who;
        this.pixelPlayer = pixelPlayer;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}