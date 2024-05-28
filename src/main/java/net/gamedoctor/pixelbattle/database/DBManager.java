package net.gamedoctor.pixelbattle.database;

import net.gamedoctor.pixelbattle.PixelBattle;
import net.gamedoctor.pixelbattle.PixelPlayer;
import net.gamedoctor.pixelbattle.database.data.CanvasFrame;
import net.gamedoctor.pixelbattle.database.data.ResultValue;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public interface DBManager {

    void connect(PixelBattle plugin);

    void close();

    PixelPlayer loadPlayer(String name);

    void savePlayer(PixelPlayer pixelPlayer);

    List<ResultValue> getValueResultSet(String value);

    void logPixelPaint(Location location, String newColor, String previousColor, String player, long time);

    void updateCanvasState(Location location, String color, long time);

    void insertCanvasState(Location location, int x, int y);

    void wipeData();

    HashMap<String, CanvasFrame> getCanvasData();

    LinkedHashMap<Integer, CanvasFrame> getAllPixelFrames(boolean includeMeta);
}
