package net.gamedoctor.pixelbattle.database.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class PixelRollbackData {
    private final CanvasFrame newCanvasFrame;
    private final List<CanvasFrame> removedCanvasFrames;
}