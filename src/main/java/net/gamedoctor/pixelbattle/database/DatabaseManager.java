package net.gamedoctor.pixelbattle.database;

import lombok.Getter;
import net.gamedoctor.pixelbattle.PixelBattle;
import net.gamedoctor.pixelbattle.PixelPlayer;
import net.gamedoctor.pixelbattle.config.items.ColorItem;
import net.gamedoctor.pixelbattle.config.messages.Placeholder;
import net.gamedoctor.pixelbattle.database.data.CanvasFrame;
import net.gamedoctor.pixelbattle.database.data.PaintedPixel;
import net.gamedoctor.pixelbattle.database.data.PixelRollbackData;
import net.gamedoctor.pixelbattle.database.data.ResultValue;
import net.gamedoctor.pixelbattle.database.managers.FileDBManager;
import net.gamedoctor.pixelbattle.database.managers.MySQLDBManager;
import net.gamedoctor.pixelbattle.database.managers.SQLiteDBManager;
import net.gamedoctor.pixelbattle.leaderboard.ValueType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.logging.Level;

public class DatabaseManager {
    @Getter
    private final HashMap<String, PixelPlayer> loadedPlayers = new HashMap<>();
    @Getter
    private final HashMap<Location, LinkedList<PaintedPixel>> paintedPixels = new HashMap<>();
    private final PixelBattle plugin;
    @Getter
    private final String dbFileName;
    @Getter
    private final String playersTableName;
    @Getter
    private final String pixelLogsTableName;
    @Getter
    private final String canvasStateTableName;
    private DBManager dbManager;

    public DatabaseManager(PixelBattle plugin) {
        this.plugin = plugin;
        FileConfiguration cfg = plugin.getConfig();
        this.dbFileName = cfg.getString("database.fileName");
        this.playersTableName = cfg.getString("database.playersTableName");
        this.pixelLogsTableName = cfg.getString("database.pixelLogsTableName");
        this.canvasStateTableName = cfg.getString("database.canvasStateTableName");
        switch (DBType.valueOf(cfg.getString("database.type"))) {
            case FILE: {
                dbManager = new FileDBManager();
                break;
            }
            case MYSQL: {
                dbManager = new MySQLDBManager();
                break;
            }
            case SQLITE: {
                dbManager = new SQLiteDBManager();
                break;
            }
        }
    }

    public void openConnection() {
        dbManager.connect(plugin);
    }

    /*
    public void saveAllPlayersAndClose() {
        for (String name : loadedPlayers.keySet()) {
            dbManager.savePlayer(loadedPlayers.get(name));
        }

        dbManager.close();
    }

     */

    public void loadCanvasToDB() {
        plugin.setCanvasLocked(true);
        plugin.getLogger().log(Level.INFO, "The loading of the canvas into the database has begun...");
        LinkedList<Location> canvas = plugin.getMainConfig().getCanvas();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                int side = (int) Math.sqrt(canvas.size());
                int x = 0;
                int y = 0;
                int counter = 0;

                for (Location location : canvas) {
                    dbManager.insertCanvasState(location, x, y);
                    x++;
                    if (x >= side) x = 0;
                    if (x == 0) y++;
                    counter++;

                    if (counter % 100 == 0) {
                        plugin.getLogger().log(Level.INFO, "The canvas is loading: " + counter + "/" + canvas.size());
                    }
                }

                if (dbManager instanceof FileDBManager) {
                    ((FileDBManager) dbManager).saveFile();
                }

                plugin.setCanvasLocked(false);
                plugin.getLogger().log(Level.INFO, "The canvas has been successfully loaded");
            }
        });
    }

    public HashMap<String, Integer> getSortedForLeaderboard(String key, int rows, ValueType valueType) {
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();

        for (ResultValue resultSet : dbManager.getValueResultSet(key)) {
            switch (valueType) {
                case INT:
                    result.put(resultSet.getKey(), resultSet.getValueASInteger());
                    break;
                case TIME:
                    result.put(resultSet.getKey(), Math.toIntExact(resultSet.getValueASLong() / 1000L));
                    break;
            }
        }

        for (String player : loadedPlayers.keySet()) {
            switch (valueType) {
                case INT:
                    result.put(player, (int) loadedPlayers.get(player).getValueByName(key));
                    break;
                case TIME:
                    result.put(player, Math.toIntExact(((Number) loadedPlayers.get(player).getValueByName(key)).longValue() / 1000L));
                    break;
            }
        }

        Object[] toDisplay = result.entrySet().toArray();
        Arrays.sort(toDisplay, (Comparator) (o1, o2) -> ((Map.Entry<String, Integer>) o2).getValue()
                .compareTo(((Map.Entry<String, Integer>) o1).getValue()));

        result.clear();

        int counter = 0;
        for (Object e : toDisplay) {
            counter++;
            int value = ((Map.Entry<String, Integer>) e).getValue();
            String p = ((Map.Entry<String, Integer>) e).getKey();
            result.put(p, value);
            if (counter >= rows) break;
        }

        return result;
    }

    public void closeConnection() {
        dbManager.close();
    }

    public PixelPlayer loadPlayer(String name) {
        if (!isLoaded(name)) {
            PixelPlayer pixelPlayer = dbManager.loadPlayer(name);
            if (pixelPlayer == null) {
                pixelPlayer = new PixelPlayer(plugin, name, 0, 0L, System.currentTimeMillis(), 0L, 0, plugin.getMainConfig().getLevelingConfig().getDefaultLevel());
            }

            loadedPlayers.put(name, pixelPlayer);
            return pixelPlayer;
        } else {
            return loadedPlayers.get(name);
        }
    }

    public PixelPlayer getPlayer(String name) {
        if (isLoaded(name)) {
            return loadedPlayers.get(name);
        } else {
            return loadPlayer(name);
        }
    }

    public void savePlayer(String name) {
        if (isLoaded(name)) {
            dbManager.savePlayer(loadedPlayers.remove(name));
        }
    }

    public void savePlayerWithOnlineCheck(String name) {
        if (isLoaded(name)) {
            if (Bukkit.getPlayerExact(name) == null) {
                dbManager.savePlayer(loadedPlayers.remove(name));
            }
        }
    }

    public void checkRemovePixelPaintedAsync(String painterName, Location location) {
        if (paintedPixels.containsKey(location)) {
            PaintedPixel pixelData = paintedPixels.get(location).getLast();
            if (!plugin.getMainConfig().getLevelingConfig().isRemovePixelsWhenPainted_onlyOther() || !painterName.equals(pixelData.getPlayer())) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                        PixelPlayer pixelPlayer = getPlayer(pixelData.getPlayer());
                        pixelPlayer.removePainted();
                        if (plugin.getMainConfig().getLevelingConfig().isRemovePixelsWhenPainted_removeExp()) {
                            pixelPlayer.removeExp(pixelData.getColor().getGivesExp());
                        }
                        savePlayerWithOnlineCheck(pixelData.getPlayer());
                    }
                });
            }
        }
    }

    public void logPixelPaint(Location location, Material newColor, Material previousColor, String player) {
        addToPaintedPixelsList(location, new PaintedPixel(plugin.getMainConfig().getItems().get(newColor), player, System.currentTimeMillis()));

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                dbManager.logPixelPaint(location, newColor.toString(), previousColor.toString(), player, System.currentTimeMillis());
            }
        });
    }

    private void addToPaintedPixelsList(Location location, PaintedPixel paintedPixel) {
        LinkedList<PaintedPixel> list = new LinkedList<>();
        if (paintedPixels.containsKey(location)) {
            list = paintedPixels.get(location);
            list.add(paintedPixel);
        } else {
            list.add(paintedPixel);
        }

        paintedPixels.put(location, list);
    }

    public LinkedHashMap<Integer, CanvasFrame> getFramesForTimeLapse() {
        return dbManager.getAllPixelFrames(false);
    }

    public HashMap<String, CanvasFrame> getCanvasPixelsData() {
        return dbManager.getCanvasData();
    }

    public void wipePlayer(String player) {
        PixelPlayer pixelPlayer;
        boolean clear = false;
        if (isLoaded(player)) {
            pixelPlayer = loadedPlayers.get(player);
        } else {
            pixelPlayer = getPlayer(player);
            clear = true;
        }
        wipePlayer(pixelPlayer);
        if (clear) savePlayer(player);
    }

    public void wipePlayer(PixelPlayer pixelPlayer) {
        pixelPlayer.setNextPixel(0L);
        pixelPlayer.setPainted(0);
        pixelPlayer.setPlayedTime(0L);
        pixelPlayer.setExp(0);
        pixelPlayer.setLevel(plugin.getMainConfig().getLevelingConfig().getDefaultLevel());
        pixelPlayer.setJoinDate(System.currentTimeMillis());
    }

    public void wipeData() {
        plugin.setCanvasLocked(true);
        dbManager.wipeData();

        for (PixelPlayer pixelPlayer : loadedPlayers.values()) {
            wipePlayer(pixelPlayer);
        }

        new BukkitRunnable() {
            public void run() {
                for (Location location : plugin.getMainConfig().getCanvas()) {
                    location.getBlock().setType(plugin.getMainConfig().getDefaultColor().getMaterial());
                }
            }
        }.runTask(plugin);
        plugin.setCanvasLocked(false);
    }

    public void updateCanvasState(Location location, Material color) {
        if (!plugin.isCanvasLocked()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    dbManager.updateCanvasState(location, color.toString(), System.currentTimeMillis());
                }
            });
        } else {
            plugin.getLogger().log(Level.WARNING, "Attempt to update the canvas during uploading to the database"); // Попытка обновить полотно во время выгрузки в базу данных
        }
    }

    public void preparePaintedPixels() {
        for (CanvasFrame canvasFrame : dbManager.getAllPixelFrames(true).values()) {
            PaintedPixel pixelData = canvasFrame.getPixelData();
            //paintedPixels.put(canvasFrame.getLocation(), new PaintedPixel(pixelData.getColor(), pixelData.getPlayer(), pixelData.getDate()));
            addToPaintedPixelsList(canvasFrame.getLocation(), new PaintedPixel(pixelData.getColor(), pixelData.getPlayer(), pixelData.getDate()));
        }
    }

    public void rePreparePaintedPixel(Location location) {
        paintedPixels.remove(location);
        for (CanvasFrame canvasFrame : dbManager.getAllPixelFrames(true, location).values()) {
            PaintedPixel pixelData = canvasFrame.getPixelData();
            addToPaintedPixelsList(canvasFrame.getLocation(), new PaintedPixel(pixelData.getColor(), pixelData.getPlayer(), pixelData.getDate()));
        }
    }

    public PaintedPixel getPixelData(Location location) {
        return getPixelData(location, getDefaultPixelData());
    }

    public PaintedPixel getDefaultPixelData() {
        return getDefaultPixelData(plugin.getMainConfig().getDefaultColor());
    }

    public PaintedPixel getDefaultPixelData(ColorItem colorItem) {
        return new PaintedPixel(colorItem, "-", 0L);
    }

    public PaintedPixel getPixelData(Location location, PaintedPixel defaultPixelData) {
        if (paintedPixels.containsKey(location)) {
            return paintedPixels.get(location).getLast();
        } else {
            return defaultPixelData;
        }
    }

    private int rollbackPixel(String player, Location pixelLoc, long time) {
        boolean rollbackNeeded = false;
        int rollbackedPixels = 0;
        if (paintedPixels.containsKey(pixelLoc)) {
            for (PaintedPixel paintedPixel : paintedPixels.get(pixelLoc)) {
                if (paintedPixel.getPlayer().equals(player) && paintedPixel.getDate() > time) {
                    rollbackNeeded = true;
                    break;
                }
            }
        }

        if (rollbackNeeded) {
            PixelRollbackData pixelRollbackData = dbManager.rollbackPixel(player, pixelLoc, time);
            //if (pixelRollbackData != null) {
            for (CanvasFrame removedCanvasFrame : pixelRollbackData.getRemovedCanvasFrames()) {
                PaintedPixel oldPixelData = removedCanvasFrame.getPixelData();
                PixelPlayer pixelPlayer = getPlayer(player);
                pixelPlayer.removePainted();
                pixelPlayer.removeExp(oldPixelData.getColor().getGivesExp());
                rollbackedPixels++;
            }
            savePlayerWithOnlineCheck(player);

            rePreparePaintedPixel(pixelLoc);
            new BukkitRunnable() {
                @Override
                public void run() {
                    pixelLoc.getBlock().setType(pixelRollbackData.getNewCanvasFrame().getPixelData().getColor().getMaterial());
                }
            }.runTask(plugin);
        }

        return rollbackedPixels;
    }

    public void rollbackPixels(String actorName, String player, List<Location> pixelsLocs, long time) {
        plugin.setCanvasLocked(true);

        if (pixelsLocs.size() > plugin.getMainConfig().getCanvas().size()) {
            pixelsLocs.clear();
            pixelsLocs.addAll(plugin.getMainConfig().getCanvas());
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                int successCounter = 0;
                int allCounter = 0;

                for (Location pixel : pixelsLocs) {
                    successCounter += rollbackPixel(player, pixel, time);
                    allCounter++;

                    plugin.getMainConfig().getMessage_cmdModRollbackRunning().display(Bukkit.getPlayer(actorName),
                            new Placeholder("%now%", String.valueOf(allCounter)),
                            new Placeholder("%total%", String.valueOf(pixelsLocs.size())),
                            new Placeholder("%success%", String.valueOf(successCounter)));
                }

                Player actor = Bukkit.getPlayer(actorName);
                if (actor != null && actor.isOnline()) {
                    plugin.getMainConfig().getMessage_cmdModRollbackEnd().display(actor, new Placeholder("%target%", player), new Placeholder("%count%", String.valueOf(successCounter)));
                }

                plugin.setCanvasLocked(false);
            }
        });
    }

    public boolean isLoaded(String name) {
        return loadedPlayers.containsKey(name);
    }
}