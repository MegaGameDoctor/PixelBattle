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

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class SQLiteDBManager implements DBManager {
    private PixelBattle plugin;
    private Connection connection;
    private String playersTableName;
    private String pixelLogsTableName;
    private String canvasStateTableName;

    public void connect(PixelBattle plugin) {
        this.plugin = plugin;
        playersTableName = plugin.getDatabaseManager().getPlayersTableName();
        pixelLogsTableName = plugin.getDatabaseManager().getPixelLogsTableName();
        canvasStateTableName = plugin.getDatabaseManager().getCanvasStateTableName();

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite://" + plugin.getDataFolder().getAbsolutePath() + "//" + plugin.getDatabaseManager().getDbFileName() + ".db");
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + playersTableName + " (\n" +
                    "\t`player`\tTEXT(255) UNIQUE,\n" +
                    "\t`painted`\tINTEGER,\n" +
                    "\t`nextPixel`\tINTEGER,\n" +
                    "\t`playedTime`\tINTEGER,\n" +
                    "\t`exp`\tINTEGER,\n" +
                    "\t`level`\tINTEGER);").execute();
            if (plugin.getMainConfig().isLogPixelPaint()) {
                connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + pixelLogsTableName + " (\n" +
                        "\t`time`\tBIGINT,\n" +
                        "\t`world`\tVARCHAR(255),\n" +
                        "\t`location`\tVARCHAR(255),\n" +
                        "\t`newColor`\tVARCHAR(45),\n" +
                        "\t`previousColor`\tVARCHAR(45),\n" +
                        "\t`player`\tVARCHAR(255));").execute();
            }

            if (plugin.getMainConfig().isSaveCanvasState()) {
                connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + canvasStateTableName + " (\n" +
                        "\t`location`\tVARCHAR(255) UNIQUE,\n" +
                        "\t`world`\tVARCHAR(255),\n" +
                        "\t`color`\tVARCHAR(255),\n" +
                        "\t`changeDate`\tBIGINT,\n" +
                        "\t`x`\tINT,\n" +
                        "\t`y`\tINT);").execute();

                PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM " + canvasStateTableName);

                ResultSet set = preparedStatement.executeQuery();
                if (set.next()) {
                    int count = set.getInt("COUNT(*)");
                    if (count == 0) {
                        plugin.getDatabaseManager().loadCanvasToDB();
                    } else if (plugin.getMainConfig().getCanvas().size() != count) {
                        if (plugin.getMainConfig().isLogPixelPaint())
                            connection.prepareStatement("DELETE FROM " + pixelLogsTableName).executeUpdate();
                        connection.prepareStatement("DELETE FROM " + canvasStateTableName).executeUpdate();
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
                connection.prepareStatement("DELETE FROM " + pixelLogsTableName).executeUpdate();
            if (cfg.isSaveCanvasState()) {
                preparedStatement = connection.prepareStatement("UPDATE " + canvasStateTableName + " SET color=?, changeDate=?");
                preparedStatement.setString(1, cfg.getDefaultColor().toString());
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
                    "ON CONFLICT(player) DO UPDATE SET painted=excluded.painted, nextPixel=excluded.nextPixel, playedTime=excluded.playedTime, exp=excluded.exp, level=excluded.level WHERE player=excluded.player");
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
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return frames;
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