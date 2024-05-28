package net.gamedoctor.pixelbattle.database.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.gamedoctor.pixelbattle.config.items.ColorItem;

@AllArgsConstructor
@Getter
public class PaintedPixel {
    private final ColorItem color;
    private final String player;
    private final long date;
}