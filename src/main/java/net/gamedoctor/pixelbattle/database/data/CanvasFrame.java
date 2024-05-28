package net.gamedoctor.pixelbattle.database.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@AllArgsConstructor
@Getter
public class CanvasFrame {
    private final Location location;
    private final int x;
    private final int y;
    @Setter
    private PaintedPixel pixelData;
}