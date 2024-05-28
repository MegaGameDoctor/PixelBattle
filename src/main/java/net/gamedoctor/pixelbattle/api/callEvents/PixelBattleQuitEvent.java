package net.gamedoctor.pixelbattle.api.callEvents;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PixelBattleQuitEvent extends PlayerEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    public PixelBattleQuitEvent(Player who) {
        super(who);
        this.player = who;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }
}
