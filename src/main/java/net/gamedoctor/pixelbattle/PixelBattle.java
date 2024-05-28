package net.gamedoctor.pixelbattle;

import lombok.Getter;
import lombok.Setter;
import net.gamedoctor.pixelbattle.PlaceholderAPI.Placeholders;
import net.gamedoctor.pixelbattle.api.PixelBattleAPI;
import net.gamedoctor.pixelbattle.api.callEvents.PixelBattlePreJoinEvent;
import net.gamedoctor.pixelbattle.api.callEvents.PixelBattleQuitEvent;
import net.gamedoctor.pixelbattle.board.BoardManager;
import net.gamedoctor.pixelbattle.commands.PixelBattleCommand;
import net.gamedoctor.pixelbattle.config.Config;
import net.gamedoctor.pixelbattle.config.messages.Placeholder;
import net.gamedoctor.pixelbattle.database.DatabaseManager;
import net.gamedoctor.pixelbattle.database.data.CanvasFrame;
import net.gamedoctor.pixelbattle.events.Events;
import net.gamedoctor.pixelbattle.events.GuardEvents;
import net.gamedoctor.pixelbattle.events.WorldLoadListener;
import net.gamedoctor.pixelbattle.gui.ChooseColorGUI;
import net.gamedoctor.pixelbattle.gui.PaintLogsGUI;
import net.gamedoctor.pixelbattle.leaderboard.LeaderboardManager;
import net.gamedoctor.pixelbattle.utils.Utils;
import net.gamedoctor.pixelbattle.web.WebToolCommunicator;
import net.gamedoctor.plugins.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.logging.Level;

@Getter
public class PixelBattle extends JavaPlugin {
    @Getter
    private static PixelBattleAPI pixelBattleAPI;
    private final HashMap<String, ChooseColorGUI> waitingForChoose = new HashMap<>();
    private final HashMap<String, PaintLogsGUI> openedPaintLogs = new HashMap<>();
    private BoardManager boardManager;
    private DatabaseManager databaseManager;
    private LeaderboardManager leaderboardManager;
    private WebToolCommunicator webToolCommunicator;
    private Config mainConfig;
    private Utils utils;
    @Setter
    private boolean activeTimeLapse = false;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getLogger().log(Level.INFO, "Waiting for the world to load...");
        Bukkit.getPluginManager().registerEvents(new WorldLoadListener(this), this);
    }

    public void preparePlugin() {
        long start = System.currentTimeMillis();
        utils = new Utils(this);
        this.getLogger().log(Level.INFO, "Processing the config...");
        mainConfig = new Config(this);
        this.getLogger().log(Level.INFO, "Completed!");

        this.getLogger().log(Level.INFO, "Connecting to the database...");
        databaseManager = new DatabaseManager(this);
        databaseManager.openConnection();
        this.getLogger().log(Level.INFO, "Completed!");

        if (mainConfig.getStandaloneServerConfig().isEnable() && mainConfig.getExitItem() != null)
            Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        this.getLogger().log(Level.INFO, "Performing other processes...");
        if (mainConfig.getMenu_info().isEnable() && mainConfig.isLogPixelPaint())
            databaseManager.preparePaintedPixels();
        Bukkit.getPluginManager().registerEvents(new Events(this), this);
        Bukkit.getPluginManager().registerEvents(new GuardEvents(this), this);

        PixelBattleCommand pixelBattleCommand = new PixelBattleCommand(this);
        this.getCommand("pixelbattle").setExecutor(pixelBattleCommand);
        this.getCommand("pixelbattle").setTabCompleter(pixelBattleCommand);

        boardManager = new BoardManager(this);
        leaderboardManager = new LeaderboardManager(this);
        webToolCommunicator = new WebToolCommunicator(this);
        pixelBattleAPI = new PixelBattleAPI(this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(this).register();
        }

        this.getLogger().log(Level.INFO, "Fully completed! (" + (System.currentTimeMillis() - start) + " ms)");

        Metrics.send(this);
    }

    @Override
    public void onDisable() {
        this.getLogger().log(Level.INFO, "Disabling the plugin...");
        Bukkit.getScheduler().cancelTasks(this);

        if (databaseManager != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                exitPixelBattle(p);
            }

            for (String pixelPlayerName : databaseManager.getLoadedPlayers().keySet()) {
                databaseManager.savePlayer(pixelPlayerName);
            }
        }

        if (leaderboardManager != null) leaderboardManager.clearAll();
        if (databaseManager != null) databaseManager.closeConnection();
        this.getLogger().log(Level.INFO, "The plugin has been successfully disabled!");
    }

    public void joinPixelBattle(Player player) {
        if (!isInPixelBattle(player)) {
            PixelBattlePreJoinEvent preJoinEvent = new PixelBattlePreJoinEvent(player);
            Bukkit.getPluginManager().callEvent(preJoinEvent);
            if (!preJoinEvent.isCancelled()) {
                databaseManager.loadPlayer(player.getName());

                if (mainConfig.isResetPlayer()) {
                    player.setExp(0);
                    player.setLevel(0);
                    player.getInventory().clear();
                    player.setFoodLevel(20);
                    player.setHealth(player.getHealthScale());
                }

                if (!mainConfig.getDefaultGamemode().equals("NO")) {
                    player.setGameMode(GameMode.valueOf(mainConfig.getDefaultGamemode()));
                }

                if (mainConfig.getExitItem() != null) {
                    player.getInventory().setItem(mainConfig.getExitItemSlot(), mainConfig.getExitItem());
                }

                if (mainConfig.getBoardConfig().isEnable()) {
                    boardManager.setScoreboard(player);
                }

                player.teleport(mainConfig.getSpawn());

                if (mainConfig.isEnableFly()) {
                    player.setAllowFlight(true);
                    player.setFlying(true);
                }

                mainConfig.getMessage_welcome().display(player);
            }
        }
    }

    public void exitPixelBattle(Player player) {
        if (isInPixelBattle(player)) {
            databaseManager.savePlayer(player.getName());

            if (mainConfig.isEnableFly()) {
                player.setFlying(false);
                player.setAllowFlight(false);
            }

            if (mainConfig.getBoardConfig().isEnable()) {
                boardManager.removeScoreboard(player);
            }

            if (mainConfig.getExitItem() != null) {
                player.getInventory().remove(mainConfig.getExitItem());
            }

            player.teleport(mainConfig.getExitSpawn());

            Bukkit.getPluginManager().callEvent(new PixelBattleQuitEvent(player));
        }
    }

    public void startTimeLapse(CommandSender actor, long speed) {
        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
            @Override
            public void run() {
                activeTimeLapse = true;

                new BukkitRunnable() {
                    public void run() {
                        for (Location loc : mainConfig.getCanvas()) {
                            loc.getBlock().setType(mainConfig.getDefaultColor().getMaterial());
                        }
                    }
                }.runTask(PixelBattle.this);

                LinkedHashMap<Integer, CanvasFrame> frames = databaseManager.getFramesForTimeLapse();

                mainConfig.getMessage_timelapseStarted().display(actor,
                        new Placeholder("%speed%", String.valueOf(speed)),
                        new Placeholder("%frames%", String.valueOf(frames.size())),
                        new Placeholder("%time%", utils.formatTime(speed * frames.size()))
                );

                for (int frame : frames.keySet()) {
                    try {
                        Thread.sleep(speed);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    CanvasFrame canvasFrame = frames.get(frame);

                    new BukkitRunnable() {
                        public void run() {
                            canvasFrame.getLocation().getWorld().getBlockAt(canvasFrame.getLocation()).setType(canvasFrame.getPixelData().getColor().getMaterial());
                        }
                    }.runTask(PixelBattle.this);

                    mainConfig.getMessage_timelapseFrameCounter().display(actor,
                            new Placeholder("%current%", String.valueOf(frame)),
                            new Placeholder("%all%", String.valueOf(frames.size()))
                    );
                }

                activeTimeLapse = false;

                mainConfig.getMessage_timelapseEnded().display(actor,
                        new Placeholder("%speed%", String.valueOf(speed)),
                        new Placeholder("%frames%", String.valueOf(frames.size())),
                        new Placeholder("%time%", utils.formatTime(speed * frames.size()))
                );
            }
        });
    }

    public boolean isInPixelBattle(Player player) {
        return databaseManager.isLoaded(player.getName());
    }
}