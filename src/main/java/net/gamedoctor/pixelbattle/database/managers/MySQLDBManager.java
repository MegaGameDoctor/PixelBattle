package net.gamedoctor.pixelbattle.database.managers;

import net.gamedoctor.pixelbattle.PixelBattle;
import net.gamedoctor.pixelbattle.PixelPlayer;
import net.gamedoctor.pixelbattle.config.Config;
import net.gamedoctor.pixelbattle.database.DBManager;
import net.gamedoctor.pixelbattle.database.data.CanvasFrame;
import net.gamedoctor.pixelbattle.database.data.PaintedPixel;
import net.gamedoctor.pixelbattle.database.data.PixelRollbackData;
import net.gamedoctor.pixelbattle.database.data.ResultValue;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class MySQLDBManager implements DBManager {
    private PixelBattle plugin;
    private Connection connection;
    private String playersTableName;
    private String pixelLogsTableName;
    private String canvasStateTableName;

    public void connect(PixelBattle plugin) {
        this.plugin = plugin;
        FileConfiguration cfg = plugin.getConfig();
        playersTableName = plugin.getDatabaseManager().getPlayersTableName();
        pixelLogsTableName = plugin.getDatabaseManager().getPixelLogsTableName();
        canvasStateTableName = plugin.getDatabaseManager().getCanvasStateTableName();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + cfg.getString("database.host") + "/" + cfg.getString("database.databaseName") + cfg.getString("database.arguments"), cfg.getString("database.user"), cfg.getString("database.password"));
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + playersTableName + " (\n" +
                    "  `player` VARCHAR(255) NOT NULL,\n" +
                    "  `painted` INT NULL,\n" +
                    "  `nextPixel` BIGINT NULL,\n" +
                    "  `playedTime` BIGINT NULL,\n" +
                    "  `exp` INT NULL,\n" +
                    "  `level` INT NULL,\n" +
                    "  PRIMARY KEY (`player`),\n" +
                    "  UNIQUE INDEX `player_UNIQUE` (`player` ASC));").execute();

            if (plugin.getMainConfig().isLogPixelPaint()) {
                connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + pixelLogsTableName + " (\n" +
                        "  `time` BIGINT NOT NULL,\n" +
                        "  `world` VARCHAR(255) NULL,\n" +
                        "  `location` VARCHAR(255) NULL,\n" +
                        "  `newColor` VARCHAR(45) NULL,\n" +
                        "  `previousColor` VARCHAR(45) NULL,\n" +
                        "  `player` VARCHAR(255) NULL)").execute();
            }

            if (plugin.getMainConfig().isSaveCanvasState()) {
                connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + canvasStateTableName + " (\n" +
                        "  `location` VARCHAR(255) NOT NULL,\n" +
                        "  `world` VARCHAR(255) NULL,\n" +
                        "  `color` VARCHAR(255) NULL,\n" +
                        "  `changeDate` BIGINT NULL,\n" +
                        "  `x` INT NULL,\n" +
                        "  `y` INT NULL,\n" +
                        "  PRIMARY KEY (`location`),\n" +
                        "  UNIQUE INDEX `location_UNIQUE` (`location` ASC));").execute();

                PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM " + canvasStateTableName);

                ResultSet set = preparedStatement.executeQuery();
                if (set.next()) {
                    int count = set.getInt("COUNT(*)");
                    if (count == 0) {
                        plugin.getDatabaseManager().loadCanvasToDB();
                    } else if (plugin.getMainConfig().getCanvas().size() != count) {
                        if (plugin.getMainConfig().isLogPixelPaint())
                            connection.prepareStatement("TRUNCATE " + pixelLogsTableName).executeUpdate();
                        connection.prepareStatement("TRUNCATE " + canvasStateTableName).executeUpdate();
                        plugin.getDatabaseManager().loadCanvasToDB();
                    }
                }

                set.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void wipeData() {
        Config cfg = plugin.getMainConfig();
        try {
            PreparedStatement preparedStatement;
            if (cfg.isLogPixelPaint())
                connection.prepareStatement("TRUNCATE " + pixelLogsTableName).executeUpdate();
            if (cfg.isSaveCanvasState()) {
                preparedStatement = connection.prepareStatement("UPDATE " + canvasStateTableName + " SET color=?, changeDate=?");
                preparedStatement.setString(1, cfg.getDefaultColor().getMaterial().toString());
                preparedStatement.setLong(2, 0L);
                preparedStatement.executeUpdate();
            }

            preparedStatement = connection.prepareStatement("UPDATE " + playersTableName + " SET painted=?, nextPixel=?, playedTime=?, exp=?, level=?");
            preparedStatement.setInt(1, 0);
            preparedStatement.setLong(2, 0L);
            preparedStatement.setLong(3, 0L);
            preparedStatement.setInt(4, 0);
            preparedStatement.setInt(5, cfg.getLevelingConfig().getDefaultLevel());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertCanvasState(Location location, int x, int y) {
        String block = location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + canvasStateTableName + " (`location`, `world`, `color`, `changeDate`, `x`, `y`) VALUES (?, ?, ?, ?, ?, ?);");
            preparedStatement.setString(1, block);
            preparedStatement.setString(2, location.getWorld().getName());
            preparedStatement.setString(3, location.getBlock().getType().toString());
            preparedStatement.setLong(4, 0);
            preparedStatement.setInt(5, x);
            preparedStatement.setInt(6, y);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PixelPlayer loadPlayer(String name) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + playersTableName + " WHERE player=?");
            preparedStatement.setString(1, name);
            ResultSet set = preparedStatement.executeQuery();
            if (set.next()) {
                return new PixelPlayer(plugin, name, set.getInt("painted"), set.getLong("nextPixel"), System.currentTimeMillis(), set.getLong("playedTime"), set.getInt("exp"), set.getInt("level"));
            }
            set.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void savePlayer(PixelPlayer pixelPlayer) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + playersTableName + " (`player`, `painted`, `nextPixel`, `playedTime`, `exp`, `level`) VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE painted=VALUES(`painted`), nextPixel=VALUES(`nextPixel`), playedTime=VALUES(`playedTime`), exp=VALUES(`exp`), level=VALUES(`level`)");
            preparedStatement.setString(1, pixelPlayer.getName());
            preparedStatement.setInt(2, pixelPlayer.getPainted());
            preparedStatement.setLong(3, pixelPlayer.getNextPixel());
            preparedStatement.setLong(4, pixelPlayer.getPlayedTime());
            preparedStatement.setInt(5, pixelPlayer.getExp());
            preparedStatement.setInt(6, pixelPlayer.getLevel());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void logPixelPaint(Location location, String newColor, String previousColor, String player, long time) {
        String loc = location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + pixelLogsTableName + " " +
                    "(`time`, `world`, `location`, `newColor`, `previousColor`, `player`) VALUES (?, ?, ?, ?, ?, ?)");
            preparedStatement.setLong(1, time);
            preparedStatement.setString(2, location.getWorld().getName());
            preparedStatement.setString(3, loc);
            preparedStatement.setString(4, newColor);
            preparedStatement.setString(5, previousColor);
            preparedStatement.setString(6, player);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateCanvasState(Location location, String color, long time) {
        String loc = location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + canvasStateTableName + " SET color=?, changeDate=? WHERE location=?");
            preparedStatement.setString(1, color);
            preparedStatement.setLong(2, time);
            preparedStatement.setString(3, loc);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public LinkedHashMap<Integer, CanvasFrame> getAllPixelFrames(boolean includeMeta) {
        LinkedHashMap<Integer, CanvasFrame> frames = new LinkedHashMap<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + pixelLogsTableName + " ORDER BY time");
            workWithFramesData(includeMeta, frames, preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return frames;
    }

    public LinkedHashMap<Integer, CanvasFrame> getAllPixelFrames(boolean includeMeta, Location location) {
        String loc = location.getBlockX() + "_" + location.getBlockY() + "_" + location.getBlockZ();
        LinkedHashMap<Integer, CanvasFrame> frames = new LinkedHashMap<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + pixelLogsTableName + " WHERE location=? ORDER BY time");
            preparedStatement.setString(1, loc);

            workWithFramesData(includeMeta, frames, preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return frames;
    }

    private void workWithFramesData(boolean includeMeta, LinkedHashMap<Integer, CanvasFrame> frames, PreparedStatement preparedStatement) throws SQLException {
        ResultSet set = preparedStatement.executeQuery();

        HashMap<String, CanvasFrame> canvasData = getCanvasData();

        int frame = 0;
        while (set.next()) {
            frame++;
            CanvasFrame canvasFrameFromLocation = canvasData.get(set.getString("location"));

            CanvasFrame newCanvasFrame = new CanvasFrame(
                    canvasFrameFromLocation.getLocation(),
                    canvasFrameFromLocation.getX(),
                    canvasFrameFromLocation.getY(),
                    plugin.getDatabaseManager().getDefaultPixelData(plugin.getMainConfig().getItems().get(Material.getMaterial(set.getString("newColor"))))
            );

            if (includeMeta) {
                newCanvasFrame.setPixelData(new PaintedPixel(newCanvasFrame.getPixelData().getColor(), set.getString("player"), set.getLong("time")));
            }

            frames.put(frame, newCanvasFrame);
        }

        set.close();
    }

    public PixelRollbackData rollbackPixel(String player, Location pixelLoc, long time) {
        String loc = pixelLoc.getBlockX() + "_" + pixelLoc.getBlockY() + "_" + pixelLoc.getBlockZ();
        try {
            List<CanvasFrame> oldCanvasFrames = new ArrayList<>();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + pixelLogsTableName + " WHERE location=? AND player=? AND time > ?");
            preparedStatement.setString(1, loc);
            preparedStatement.setString(2, player);
            preparedStatement.setLong(3, time);
            ResultSet set = preparedStatement.executeQuery();

            while (set.next()) {
                oldCanvasFrames.add(new CanvasFrame(pixelLoc, 0, 0,
                        plugin.getDatabaseManager().getDefaultPixelData(plugin.getMainConfig().getItems().get(Material.getMaterial(set.getString("newColor"))))));
            }

            if (!oldCanvasFrames.isEmpty()) {
                preparedStatement = connection.prepareStatement("DELETE FROM " + pixelLogsTableName + " WHERE location=? AND player=? AND time > ?");
                preparedStatement.setString(1, loc);
                preparedStatement.setString(2, player);
                preparedStatement.setLong(3, time);
                preparedStatement.executeUpdate();

                preparedStatement = connection.prepareStatement("SELECT * FROM " + pixelLogsTableName + " WHERE location=? ORDER BY time DESC LIMIT 1");
                preparedStatement.setString(1, loc);

                set = preparedStatement.executeQuery();

                CanvasFrame newCanvasFrame = new CanvasFrame(pixelLoc, 0, 0,
                        plugin.getDatabaseManager().getDefaultPixelData());
                long newChangeDate = 0L;
                if (set.next()) {
                    newCanvasFrame.setPixelData(plugin.getDatabaseManager().getDefaultPixelData(plugin.getMainConfig().getItems().get(Material.getMaterial(set.getString("newColor")))));
                    newChangeDate = set.getLong("time");
                }

                preparedStatement = connection.prepareStatement("UPDATE " + canvasStateTableName + " SET color=?, changeDate=? WHERE location=?");
                preparedStatement.setString(1, newCanvasFrame.getPixelData().getColor().getMaterial().toString());
                preparedStatement.setLong(2, newChangeDate);
                preparedStatement.setString(3, loc);
                preparedStatement.executeUpdate();

                return new PixelRollbackData(newCanvasFrame, oldCanvasFrames);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public HashMap<String, CanvasFrame> getCanvasData() {
        HashMap<String, CanvasFrame> frames = new HashMap<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + canvasStateTableName);
            ResultSet set = preparedStatement.executeQuery();

            while (set.next()) {
                String[] location = set.getString("location").split("_");
                frames.put(set.getString("location"), new CanvasFrame(
                        new Location(Bukkit.getWorld(set.getString("world")), Integer.parseInt(location[0]), Integer.parseInt(location[1]), Integer.parseInt(location[2])),
                        set.getInt("x"),
                        set.getInt("y"),
                        plugin.getDatabaseManager().getDefaultPixelData(plugin.getMainConfig().getItems().get(Material.getMaterial(set.getString("color"))))
                ));
            }

            set.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return frames;
    }

    public List<ResultValue> getValueResultSet(String value) {
        List<ResultValue> result = new ArrayList<>();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + playersTableName);
            ResultSet set = preparedStatement.executeQuery();
            while (set.next()) {
                result.add(new ResultValue(set.getString("player"), set.getObject(value)));
            }
            set.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }
}