package net.gamedoctor.pixelbattle.config;

import lombok.Getter;
import net.gamedoctor.pixelbattle.PixelBattle;
import net.gamedoctor.pixelbattle.config.items.ColorItem;
import net.gamedoctor.pixelbattle.config.items.MenuItem;
import net.gamedoctor.pixelbattle.config.messages.Message;
import net.gamedoctor.pixelbattle.config.other.BoardConfig;
import net.gamedoctor.pixelbattle.config.other.Leaderboard;
import net.gamedoctor.pixelbattle.config.other.StandaloneServerConfig;
import net.gamedoctor.pixelbattle.config.other.leveling.LevelingConfig;
import net.gamedoctor.pixelbattle.utils.LocationsCuboid;
import net.gamedoctor.pixelbattle.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.logging.Level;

@Getter
public class Config {
    private final int paintCooldown;
    private final boolean resetPlayer;
    private final boolean guardPlayer;
    private final boolean allowKnock;
    private final boolean enableFly;
    private final boolean blockModify;
    private final ColorItem defaultColor;
    private final String defaultGamemode;
    private final String gui_colorSelectionTitle;
    private final String gui_paintLogsTitle;
    private final Location spawn;
    private final Location exitSpawn;
    private final LinkedList<Location> canvas;
    private final HashMap<Material, ColorItem> items = new LinkedHashMap<>();
    private final StandaloneServerConfig standaloneServerConfig;
    private final BoardConfig boardConfig;
    private final LevelingConfig levelingConfig;
    private final boolean timeFormat;
    private final HashMap<String, Leaderboard> leaderboards = new HashMap<>();
    private final boolean logPixelPaint;
    private final boolean preventPaintSame;
    private final boolean noFall;
    private final boolean preventBlockInteract;
    private final String cooldownPermission;
    private final boolean removePixelsWhenPainted;

    private final boolean timeStringFormat_displayOnlyHighest;
    private final String timeStringFormat_seconds;
    private final String timeStringFormat_minutes;
    private final String timeStringFormat_hours;
    private final String timeStringFormat_days;

    private final Message message_welcome;
    private final Message message_pixelPainted;
    private final Message message_pixelDelay;
    private final Message message_movingToServer;
    private final Message message_exitItemExit;
    private final Message message_cmdNoPerm;
    private final Message message_cmdHelp;
    private final Message message_cmdWebHelp;
    private final Message message_cmdNoPlayer;
    private final Message message_cmdAlreadyIn;
    private final Message message_cmdNotIn;
    private final Message message_cmdSuccess;
    private final Message message_timelapseStarting;
    private final Message message_timelapseStarted;
    private final Message message_timelapseAction;
    private final Message message_timelapseEnded;
    private final Message message_timelapseFrameCounter;
    private final Message message_preventedSameColor;
    private final Message message_wipeStarted;
    private final Message message_noData;
    private final Message message_webRequest;
    private final Message message_webAnswer;
    private final Message message_levelUp;
    private final Message message_expReceived;
    private final Message message_cmdUsersHelp;
    private final Message message_cmdIncorrect;
    private final ItemStack exitItem;
    private final int exitItemSlot;
    private final String command_join_usePermission;
    private final String command_join_useOnOtherPermission;
    private final String command_leave_usePermission;
    private final String command_leave_useOnOtherPermission;
    private final String command_timelapse_usePermission;
    private final String command_wipe_usePermission;
    private final String command_web_usePermission;
    private final String command_users_usePermission;
    private final MenuItem menu_back;
    private final MenuItem menu_next;
    private final MenuItem menu_footer;
    private final MenuItem menu_info;
    private final MenuItem menu_paintedPixelInfo;
    private final boolean usingPlaceholderAPI;
    private boolean saveCanvasState;

    public Config(PixelBattle plugin) {
        FileConfiguration cfg = plugin.getConfig();
        Utils utils = plugin.getUtils();

        paintCooldown = cfg.getInt("settings.paintCooldown");
        resetPlayer = cfg.getBoolean("settings.resetPlayer");
        guardPlayer = cfg.getBoolean("settings.guardPlayer");
        allowKnock = cfg.getBoolean("settings.allowKnock");
        enableFly = cfg.getBoolean("settings.enableFly");
        blockModify = cfg.getBoolean("settings.blockModify");
        noFall = cfg.getBoolean("settings.noFall");
        preventBlockInteract = cfg.getBoolean("settings.preventBlockInteract");
        defaultGamemode = cfg.getString("settings.defaultGamemode").toUpperCase();
        standaloneServerConfig = new StandaloneServerConfig(plugin);
        gui_colorSelectionTitle = utils.color(cfg.getString("gui.colorSelectionTitle"));
        gui_paintLogsTitle = utils.color(cfg.getString("gui.paintLogsTitle"));
        timeFormat = cfg.getBoolean("settings.timeFormat");
        boardConfig = new BoardConfig(plugin);
        levelingConfig = new LevelingConfig(plugin);
        logPixelPaint = cfg.getBoolean("settings.logPixelPaint");
        saveCanvasState = cfg.getBoolean("settings.saveCanvasState");
        preventPaintSame = cfg.getBoolean("settings.preventPaintSame");
        cooldownPermission = cfg.getString("settings.cooldownPermission");
        removePixelsWhenPainted = cfg.getBoolean("settings.removePixelsWhenPainted");

        timeStringFormat_displayOnlyHighest = cfg.getBoolean("settings.timeStringFormat.displayOnlyHighest");
        timeStringFormat_seconds = cfg.getString("settings.timeStringFormat.seconds");
        timeStringFormat_minutes = cfg.getString("settings.timeStringFormat.minutes");
        timeStringFormat_hours = cfg.getString("settings.timeStringFormat.hours");
        timeStringFormat_days = cfg.getString("settings.timeStringFormat.days");

        for (String value : cfg.getConfigurationSection("leaderboards").getKeys(false)) {
            leaderboards.put(value, new Leaderboard(plugin, value));
        }

        World world = Bukkit.getWorld(cfg.getString("canvas.world"));
        spawn = new Location(world, cfg.getDouble("canvas.spawn.x"), cfg.getDouble("canvas.spawn.y"), cfg.getDouble("canvas.spawn.z"), Float.parseFloat(cfg.getString("canvas.spawn.yaw")), Float.parseFloat(cfg.getString("canvas.spawn.pitch")));
        exitSpawn = new Location(world, cfg.getDouble("canvas.exitSpawn.x"), cfg.getDouble("canvas.exitSpawn.y"), cfg.getDouble("canvas.exitSpawn.z"), Float.parseFloat(cfg.getString("canvas.exitSpawn.yaw")), Float.parseFloat(cfg.getString("canvas.exitSpawn.pitch")));

        LocationsCuboid canvasCuboid = new LocationsCuboid(
                new Location(world, cfg.getDouble("canvas.posOne.x"), cfg.getDouble("canvas.posOne.y"), cfg.getDouble("canvas.posOne.z")),
                new Location(world, cfg.getDouble("canvas.posTwo.x"), cfg.getDouble("canvas.posTwo.y"), cfg.getDouble("canvas.posTwo.z")));
        canvas = canvasCuboid.getLocations();

        if (saveCanvasState && canvasCuboid.isVertical()) {
            saveCanvasState = false;
            plugin.getLogger().log(Level.WARNING, "EN: The canvas is positioned vertically, the function of saving the canvas state (saveCanvasState) is disabled");
            plugin.getLogger().log(Level.WARNING, "RU: Полотно расположено вертикально, функция сохранения состояния полотна (saveCanvasState) недоступна");
        }

        double canvasSqrt = Math.sqrt(canvas.size());
        if (saveCanvasState && canvasSqrt * canvasSqrt != canvas.size()) {
            saveCanvasState = false;
            plugin.getLogger().log(Level.SEVERE, "EN: The canvas has different sides. To use the canvas save function (saveCanvasState), the sides must be equal. For example: 11x11, 100x100, 123x123 and so on");
            plugin.getLogger().log(Level.SEVERE, "RU: Полотно имеет разные стороны. Для использования функции сохранения полотна (saveCanvasState) стороны должны быть равны. Например: 11x11, 100x100, 123x123 и так далее");
        }

        LinkedList<String> defaultLore = new LinkedList<>();
        String defaultLorePath = "gui.items.color.defaultLore";
        if (cfg.isSet(defaultLorePath)) {
            for (String line : cfg.getStringList(defaultLorePath)) {
                defaultLore.add(utils.color(line));
            }
        }

        for (String material : cfg.getConfigurationSection("items").getKeys(false)) {
            items.put(Material.getMaterial(material.toUpperCase()), new ColorItem(plugin, material, defaultLore));
        }

        defaultColor = items.get(Material.getMaterial(cfg.getString("settings.defaultColor").toUpperCase()));
        /*
        for (int i = 0; i < 500; i++) {
            Material random = Material.values()[new Random().nextInt(Material.values().length)];
            if (random.isBlock() && !random.isAir()) {
                items.put(random, new ColorItem(config, random.toString()));
            }
        }
         */

        message_welcome = new Message(plugin, "welcome");
        message_pixelPainted = new Message(plugin, "pixelPainted");
        message_pixelDelay = new Message(plugin, "pixelDelay");
        message_movingToServer = new Message(plugin, "movingToServer");
        message_exitItemExit = new Message(plugin, "exitItemExit");
        message_cmdNoPerm = new Message(plugin, "cmdNoPerm");
        message_cmdHelp = new Message(plugin, "cmdHelp");
        message_cmdWebHelp = new Message(plugin, "cmdWebHelp");
        message_cmdNoPlayer = new Message(plugin, "cmdNoPlayer");
        message_cmdAlreadyIn = new Message(plugin, "cmdAlreadyIn");
        message_cmdNotIn = new Message(plugin, "cmdNotIn");
        message_cmdSuccess = new Message(plugin, "cmdSuccess");
        message_timelapseStarting = new Message(plugin, "timelapseStarting");
        message_timelapseStarted = new Message(plugin, "timelapseStarted");
        message_timelapseAction = new Message(plugin, "timelapseAction");
        message_timelapseEnded = new Message(plugin, "timelapseEnded");
        message_timelapseFrameCounter = new Message(plugin, "timelapseFrameCounter");
        message_preventedSameColor = new Message(plugin, "preventedSameColor");
        message_wipeStarted = new Message(plugin, "wipeStarted");
        message_noData = new Message(plugin, "noData");
        message_webRequest = new Message(plugin, "webRequest");
        message_webAnswer = new Message(plugin, "webAnswer");
        message_levelUp = new Message(plugin, "levelUp");
        message_expReceived = new Message(plugin, "expReceived");
        message_cmdUsersHelp = new Message(plugin, "cmdUsersHelp");
        message_cmdIncorrect = new Message(plugin, "cmdIncorrect");

        command_join_usePermission = cfg.getString("command.join.usePermission");
        command_join_useOnOtherPermission = cfg.getString("command.join.useOnOtherPermission");
        command_leave_usePermission = cfg.getString("command.leave.usePermission");
        command_leave_useOnOtherPermission = cfg.getString("command.leave.useOnOtherPermission");
        command_timelapse_usePermission = cfg.getString("command.timelapse.usePermission");
        command_wipe_usePermission = cfg.getString("command.wipe.usePermission");
        command_web_usePermission = cfg.getString("command.web.usePermission");
        command_users_usePermission = cfg.getString("command.users.usePermission");

        menu_back = new MenuItem(plugin, "back");
        menu_next = new MenuItem(plugin, "next");
        menu_footer = new MenuItem(plugin, "footer");
        menu_info = new MenuItem(plugin, "info");
        menu_paintedPixelInfo = new MenuItem(plugin, "paintedPixelInfo");

        if (cfg.getBoolean("exitItem.enable")) {
            LinkedList<String> lore = new LinkedList<>();
            if (cfg.isSet("exitItem.lore")) {
                for (String line : cfg.getStringList("exitItem.lore")) {
                    lore.add(utils.color(line));
                }
            }
            exitItemSlot = cfg.getInt("exitItem.slot");
            exitItem = utils.makeItem(Material.getMaterial(cfg.getString("exitItem.material")), utils.color(cfg.getString("exitItem.name")), lore, cfg.getBoolean("exitItem.glowing"));
        } else {
            exitItemSlot = 0;
            exitItem = null;
        }

        usingPlaceholderAPI = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
    }
}
