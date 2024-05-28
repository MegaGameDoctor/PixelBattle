package net.gamedoctor.pixelbattle.database.managers;

import net.gamedoctor.pixelbattle.PixelBattle;
import net.gamedoctor.pixelbattle.PixelPlayer;
import net.gamedoctor.pixelbattle.config.Config;
import net.gamedoctor.pixelbattle.database.DBManager;
import net.gamedoctor.pixelbattle.database.data.CanvasFrame;
import net.gamedoctor.pixelbattle.database.data.PaintedPixel;
import net.gamedoctor.pixelbattle.database.data.ResultValue;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class FileDBManager implements DBManager {
    private PixelBattle plugin;
    private File file;
    private FileConfiguration db;
    private String playersTableName;
    private String pixelLogsTableName;
    private String canvasStateTableName;

    public void connect(PixelBattle plugin) {
        this.plugin = plugin;
        playersTableName = plugin.getDatabaseManager().getPlayersTableName();
        pixelLogsTableName = plugin.getDatabaseManager().getPixelLogsTableName();
        canvasStateTableName = plugin.getDatabaseManager().getCanvasStateTableName();

        file = new File(plugin.getDataFolder(), plugin.getDatabaseManager().getDbFileName() + ".yml");
        db = YamlConfiguration.loadConfiguration(file);

        if (plugin.getMainConfig().isSaveCanvasState()) {
            int count = 0;
            if (db.isConfigurationSection(canvasStateTableName)) {
                count = db.getConfigurationSection(canvasStateTableName).getKeys(false).size();
            }

            if (count == 0) {
                plugin.getDatabaseManager().loadCanvasToDB();
            } else if (plugin.getMainConfig().getCanvas().size() != count) {
                if (plugin.getMainConfig().isLogPixelPaint()) makeSectionEmpty(pixelLogsTableName);
                makeSectionEmpty(canvasStateTableName);
                plugin.getDatabaseManager().loadCanvasToDB();
            }
        }
    }

    public void wipeData() {
        Config cfg = plugin.getMainConfig();
        if (cfg.isLogPixelPaint())
            makeSectionEmpty(pixelLogsTableName);
        if (cfg.isSaveCanvasState()) {
            for (String location : db.getConfigurationSection(canvasStateTableName).getKeys(false)) {
                String path = canvasStateTableName + "." + location + ".";
                db.set(path + "color", cfg.getDefaultColor().toString());
                db.set(path + "changeDate", 0);
            }
        }

        if (db.isSet(playersTableName)) {
            for (String name : db.getConfigurationSection(playersTableName).getKeys(false)) {
                String path = playersTableName + "." + name + ".";
                db.set(path + "painted", 0);
                db.set(path + "nextPixel", 0);
                db.set(path + "playedTime", 0);
                db.set(path + "exp", 0);
                db.set(path + "level", cfg.getLevelingConfig().getDefaultLevel());
            }
        }

        saveFileSync();
    }

    public void insertCanvasState(Location location, int x, int y) {
        String block = location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
        String path = canvasStateTableName + "." + block + ".";
        db.set(path + "world", location.getWorld().getName());
        db.set(path + "color", location.getBlock().getType().toString());
        db.set(path + "changeDate", 0);
        db.set(path + "x", x);
        db.set(path + "y", y);
        //saveFile();
    }

    public void updateCanvasState(Location location, String color, long time) {
        String loc = location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
        String path = canvasStateTableName + "." + loc + ".";
        db.set(path + "color", color);
        db.set(path + "changeDate", time);
        saveFileSync();
    }

    private void makeSectionEmpty(String section) {
        if (db.isConfigurationSection(section)) {
            for (String value : db.getConfigurationSection(section).getKeys(false)) {
                db.set(section + "." + value, null);
            }
            saveFileSync();
        }
    }

    public void close() {
    }

    public void saveFile() {
        try {
            db.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFileSync() {
        new BukkitRunnable() {
            public void run() {
                try {
                    db.save(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.runTask(plugin);
    }

    public PixelPlayer loadPlayer(String name) {
        String path = playersTableName + "." + name + ".";
        if (db.isConfigurationSection(path.substring(0, path.length() - 1))) {
            return new PixelPlayer(plugin, name, db.getInt(path + "painted"), db.getLong(path + "nextPixel"), System.currentTimeMillis(), db.getLong(path + "playedTime"), db.getInt(path + "exp"), db.getInt(path + "level"));
        }
        return null;
    }

    public void savePlayer(PixelPlayer pixelPlayer) {
        String path = playersTableName + "." + pixelPlayer.getName() + ".";
        db.set(path + "painted", pixelPlayer.getPainted());
        db.set(path + "nextPixel", pixelPlayer.getNextPixel());
        db.set(path + "playedTime", pixelPlayer.getPlayedTime());
        db.set(path + "exp", pixelPlayer.getExp());
        db.set(path + "level", pixelPlayer.getLevel());
        saveFile();
    }

    public void logPixelPaint(Location location, String newColor, String previousColor, String player, long time) {
        String path = pixelLogsTableName + "." + time + ".";
        String loc = location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();

        db.set(path + "world", location.getWorld().getName());
        db.set(path + "location", loc);
        db.set(path + "newColor", newColor);
        db.set(path + "previousColor", previousColor);
        db.set(path + "player", player);
        saveFileSync();
    }

    public LinkedHashMap<Integer, CanvasFrame> getAllPixelFrames(boolean includeMeta) {
        LinkedHashMap<Integer, CanvasFrame> frames = new LinkedHashMap<>();
        try {
            int frame = 0;
            HashMap<String, CanvasFrame> canvasData = getCanvasData();
            for (String time : db.getConfigurationSection(pixelLogsTableName).getKeys(false)) {
                frame++;
                String path = pixelLogsTableName + "." + time + ".";
                CanvasFrame canvasFrameFromLocation = canvasData.get(db.getString(path + "location"));

                CanvasFrame newCanvasFrame = new CanvasFrame(
                        canvasFrameFromLocation.getLocation(),
                        canvasFrameFromLocation.getX(),
                        canvasFrameFromLocation.getY(),
                        plugin.getDatabaseManager().getDefaultPixelData(plugin.getMainConfig().getItems().get(Material.getMaterial(db.getString(path + "newColor"))))
                );

                if (includeMeta) {
                    newCanvasFrame.setPixelData(new PaintedPixel(newCanvasFrame.getPixelData().getColor(), db.getString(path + "player"), Long.parseLong(time)));
                }

                frames.put(frame, newCanvasFrame);
            }
        } catch (Exception ignored) {
        }
        return frames;
    }

    public HashMap<String, CanvasFrame> getCanvasData() {
        HashMap<String, CanvasFrame> frames = new HashMap<>();
        try {
            for (String location : db.getConfigurationSection(canvasStateTableName).getKeys(false)) {
                String path = canvasStateTableName + "." + location + ".";
                String[] loc = location.split("_");
                frames.put(location, new CanvasFrame(
                        new Location(Bukkit.getWorld(db.getString(path + "world")), Integer.parseInt(loc[0]), Integer.parseInt(loc[1]), Integer.parseInt(loc[2])),
                        db.getInt(path + "x"),
                        db.getInt(path + "y"),
                        plugin.getDatabaseManager().getDefaultPixelData(plugin.getMainConfig().getItems().get(Material.getMaterial(db.getString(path + "color"))))
                ));
            }
        } catch (Exception ignored) {
        }
        return frames;
    }

    public List<ResultValue> getValueResultSet(String value) {
        List<ResultValue> result = new ArrayList<>();
        if (db.isConfigurationSection(playersTableName)) {
            for (String key : db.getConfigurationSection(playersTableName).getKeys(false)) {
                result.add(new ResultValue(key, db.get(playersTableName + "." + key + "." + value)));
            }
        }
        return result;
    }
}