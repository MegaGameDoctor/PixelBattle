package net.gamedoctor.pixelbattle.commands;

import lombok.RequiredArgsConstructor;
import net.gamedoctor.pixelbattle.PixelBattle;
import net.gamedoctor.pixelbattle.config.Config;
import net.gamedoctor.pixelbattle.config.messages.Placeholder;
import net.gamedoctor.pixelbattle.config.other.StandaloneServerConfig;
import net.gamedoctor.pixelbattle.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class PixelBattleCommand implements CommandExecutor, TabExecutor {
    private final PixelBattle plugin;

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] args) {
        Config cfg = plugin.getMainConfig();
        Utils utils = plugin.getUtils();
        StandaloneServerConfig standaloneServerConfig = plugin.getMainConfig().getStandaloneServerConfig();
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "join":
                    if (args.length > 1 && (cfg.getCommand_join_useOnOtherPermission().equals("-") || commandSender.hasPermission(cfg.getCommand_join_useOnOtherPermission()))) {
                        Player target = Bukkit.getPlayer(args[1]);
                        if (target != null && target.isOnline()) {
                            if (plugin.isInPixelBattle(target)) {
                                cfg.getMessage_cmdAlreadyIn().display(commandSender, new Placeholder("%target%", target.getName()));
                            } else {
                                plugin.joinPixelBattle(target);
                                cfg.getMessage_cmdSuccess().display(commandSender);
                            }
                        } else {
                            cfg.getMessage_cmdNoPlayer().display(commandSender, new Placeholder("%target%", args[1]));
                        }
                    } else if (cfg.getCommand_join_usePermission().equals("-") || commandSender.hasPermission(cfg.getCommand_join_usePermission())) {
                        if (commandSender instanceof Player player) {
                            if (plugin.isInPixelBattle(player)) {
                                cfg.getMessage_cmdAlreadyIn().display(player, new Placeholder("%target%", player.getName()));
                            } else {
                                plugin.joinPixelBattle(player);
                                cfg.getMessage_cmdSuccess().display(player);
                            }
                        }
                    } else {
                        cfg.getMessage_cmdNoPerm().display(commandSender);
                    }
                    return true;
                case "leave":
                    if (args.length > 1 && (cfg.getCommand_leave_useOnOtherPermission().equals("-") || commandSender.hasPermission(cfg.getCommand_leave_useOnOtherPermission()))) {
                        Player target = Bukkit.getPlayer(args[1]);
                        if (target != null && target.isOnline()) {
                            if (!plugin.isInPixelBattle(target)) {
                                cfg.getMessage_cmdNotIn().display(commandSender, new Placeholder("%target%", args[1]));
                            } else if (standaloneServerConfig.isEnable() && standaloneServerConfig.isRedirectOnExit_enable()) {
                                plugin.getUtils().connectBungeeCordServer(target, standaloneServerConfig.getRedirectOnExit_server());
                                plugin.getMainConfig().getMessage_movingToServer().display(target, new Placeholder("%server%", standaloneServerConfig.getRedirectOnExit_server()));
                            } else {
                                plugin.exitPixelBattle(target);
                            }
                            cfg.getMessage_cmdSuccess().display(commandSender);

                        } else {
                            cfg.getMessage_cmdNoPlayer().display(commandSender, new Placeholder("%target%", args[1]));
                        }
                    } else if (cfg.getCommand_leave_usePermission().equals("-") || commandSender.hasPermission(cfg.getCommand_leave_usePermission())) {
                        if (commandSender instanceof Player player) {
                            if (!plugin.isInPixelBattle(player)) {
                                cfg.getMessage_cmdNotIn().display(player, new Placeholder("%target%", player.getName()));
                            } else if (standaloneServerConfig.isEnable() && standaloneServerConfig.isRedirectOnExit_enable()) {
                                plugin.getUtils().connectBungeeCordServer(player, standaloneServerConfig.getRedirectOnExit_server());
                                plugin.getMainConfig().getMessage_movingToServer().display(player, new Placeholder("%server%", standaloneServerConfig.getRedirectOnExit_server()));
                            } else {
                                plugin.exitPixelBattle(player);
                                cfg.getMessage_cmdSuccess().display(player);
                            }

                        }
                    } else {
                        cfg.getMessage_cmdNoPerm().display(commandSender);
                    }
                    return true;
                case "timelapse":
                    if (commandSender.hasPermission(cfg.getCommand_timelapse_usePermission()) || cfg.getCommand_timelapse_usePermission().equals("-")) {
                        if (cfg.isSaveCanvasState() && cfg.isLogPixelPaint()) {
                            if (!plugin.isCanvasLocked()) {
                                long speed = 8;

                                if (args.length > 1 && utils.isNumber(args[1], true)) {
                                    speed = Long.parseLong(args[1]);
                                }

                                cfg.getMessage_timelapseStarting().display(commandSender, new Placeholder("%speed%", String.valueOf(speed)));

                                plugin.startTimeLapse(commandSender, speed);
                            } else {
                                cfg.getMessage_canvasLocked().display(commandSender);
                            }
                        } else {
                            cfg.getMessage_noData().display(commandSender, new Placeholder("%data%", "saveCanvasState, logPixelPaint"));
                        }
                    } else {
                        cfg.getMessage_cmdNoPerm().display(commandSender);
                    }
                    return true;
                case "wipe":
                    if (commandSender.hasPermission(cfg.getCommand_wipe_usePermission()) || cfg.getCommand_wipe_usePermission().equals("-")) {
                        if (!plugin.isCanvasLocked()) {
                            cfg.getMessage_wipeStarted().display(commandSender);
                            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    plugin.getDatabaseManager().wipeData();
                                    cfg.getMessage_cmdSuccess().display(commandSender);
                                }
                            });
                        } else {
                            cfg.getMessage_canvasLocked().display(commandSender);
                        }
                    } else {
                        cfg.getMessage_cmdNoPerm().display(commandSender);
                    }
                    return true;
                case "web":
                    if (commandSender.hasPermission(cfg.getCommand_web_usePermission()) || cfg.getCommand_web_usePermission().equals("-")) {
                        if (args.length > 1) {
                            switch (args[1].toLowerCase()) {
                                case "canvas":
                                    if (cfg.isSaveCanvasState()) {
                                        cfg.getMessage_webRequest().display(commandSender);
                                        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                                            @Override
                                            public void run() {
                                                String answer = null;

                                                try {
                                                    answer = plugin.getWebToolCommunicator().createCanvasImage();
                                                } catch (Exception ignored) {
                                                }

                                                cfg.getMessage_webAnswer().display(commandSender, new Placeholder("%answer%", plugin.getWebToolCommunicator().formatAnswer(answer)));
                                            }
                                        });
                                    } else {
                                        cfg.getMessage_noData().display(commandSender, new Placeholder("%data%", "saveCanvasState"));
                                    }
                                    return true;
                                case "timelapse":
                                    if (cfg.isSaveCanvasState() && cfg.isLogPixelPaint()) {
                                        cfg.getMessage_webRequest().display(commandSender);
                                        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                                            @Override
                                            public void run() {
                                                String answer = null;

                                                try {
                                                    answer = plugin.getWebToolCommunicator().createTimeLapse();
                                                } catch (Exception ignored) {
                                                }

                                                cfg.getMessage_webAnswer().display(commandSender, new Placeholder("%answer%", plugin.getWebToolCommunicator().formatAnswer(answer)));
                                            }
                                        });
                                    } else {
                                        cfg.getMessage_noData().display(commandSender, new Placeholder("%data%", "saveCanvasState, logPixelPaint"));
                                    }
                                    return true;
                                default:
                                    cfg.getMessage_cmdWebHelp().display(commandSender, new Placeholder("%cmd%", s));
                                    return true;
                            }
                        } else {
                            cfg.getMessage_cmdWebHelp().display(commandSender, new Placeholder("%cmd%", s));
                        }
                    } else {
                        cfg.getMessage_cmdNoPerm().display(commandSender);
                    }
                    return true;
                case "users":
                    if (commandSender.hasPermission(cfg.getCommand_users_usePermission()) || cfg.getCommand_users_usePermission().equals("-")) {
                        if (args.length > 1) {
                            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    switch (args[1].toLowerCase()) {
                                        case "wipe":
                                            if (args.length > 2) {
                                                plugin.getDatabaseManager().wipePlayer(args[2]);
                                                plugin.getDatabaseManager().savePlayerWithOnlineCheck(args[2]);
                                                cfg.getMessage_cmdSuccess().display(commandSender);
                                            } else {
                                                cfg.getMessage_cmdIncorrect().display(commandSender);
                                            }
                                            break;
                                        case "setlevel":
                                            if (args.length > 3 && utils.isNumber(args[3], true) && Integer.parseInt(args[3]) > 0) {
                                                int amount = Integer.parseInt(args[3]);
                                                plugin.getDatabaseManager().getPlayer(args[2]).setLevel(amount);
                                                plugin.getDatabaseManager().savePlayerWithOnlineCheck(args[2]);
                                                cfg.getMessage_cmdSuccess().display(commandSender);
                                            } else {
                                                cfg.getMessage_cmdIncorrect().display(commandSender);
                                            }
                                            break;
                                        case "setexp":
                                            if (args.length > 3 && utils.isNumber(args[3], true) && Integer.parseInt(args[3]) >= 0) {
                                                int amount = Integer.parseInt(args[3]);
                                                plugin.getDatabaseManager().getPlayer(args[2]).setExp(amount);
                                                plugin.getDatabaseManager().savePlayerWithOnlineCheck(args[2]);
                                                cfg.getMessage_cmdSuccess().display(commandSender);
                                            } else {
                                                cfg.getMessage_cmdIncorrect().display(commandSender);
                                            }
                                            break;
                                        case "setpainted":
                                            if (args.length > 3 && utils.isNumber(args[3], true) && Integer.parseInt(args[3]) >= 0) {
                                                int amount = Integer.parseInt(args[3]);
                                                plugin.getDatabaseManager().getPlayer(args[2]).setPainted(amount);
                                                plugin.getDatabaseManager().savePlayerWithOnlineCheck(args[2]);
                                                cfg.getMessage_cmdSuccess().display(commandSender);
                                            } else {
                                                cfg.getMessage_cmdIncorrect().display(commandSender);
                                            }
                                            break;
                                        default:
                                            cfg.getMessage_cmdUsersHelp().display(commandSender, new Placeholder("%cmd%", s));
                                            break;
                                    }
                                }
                            });
                        } else {
                            cfg.getMessage_cmdUsersHelp().display(commandSender, new Placeholder("%cmd%", s));
                        }
                    } else {
                        cfg.getMessage_cmdNoPerm().display(commandSender);
                    }
                    return true;
                case "mod":
                    if (commandSender instanceof Player) {
                        if (commandSender.hasPermission(cfg.getCommand_mod_usePermission()) || cfg.getCommand_mod_usePermission().equals("-")) {
                            if (args.length > 1) {
                                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                                    @Override
                                    public void run() {
                                        switch (args[1].toLowerCase()) {
                                            case "rollback":
                                                if (!plugin.isCanvasLocked()) {
                                                    if (args.length > 4 && utils.isNumber(args[3], true)) {
                                                        String player = args[2];
                                                        int radius = Integer.parseInt(args[3]);
                                                        int timeNotFormatted = 1;
                                                        char timeType = 's';
                                                        try {
                                                            timeType = args[4].toCharArray()[args[4].toCharArray().length - 1];
                                                            timeNotFormatted = Integer.parseInt(args[4].substring(0, args[4].length() - 1));
                                                        } catch (Exception ignored) {
                                                        }

                                                        int seconds = switch (timeType) {
                                                            case 's', 'с' -> timeNotFormatted;
                                                            case 'm', 'м' -> timeNotFormatted * 60;
                                                            case 'h', 'ч' -> timeNotFormatted * 60 * 60;
                                                            case 'd', 'д' -> timeNotFormatted * 60 * 60 * 24;
                                                            default -> 0;
                                                        };

                                                        long time = System.currentTimeMillis() - seconds * 1000L;

                                                        cfg.getMessage_cmdModRollbackStart().display(commandSender,
                                                                new Placeholder("%target%", player),
                                                                new Placeholder("%radius%", String.valueOf(radius)),
                                                                new Placeholder("%time%", String.valueOf(timeNotFormatted) + timeType));

                                                        plugin.getDatabaseManager().rollbackPixels(commandSender.getName(), player, utils.getAllNearbyCanvasBlocks(((Player) commandSender).getLocation(), radius), time);
                                                    } else {
                                                        cfg.getMessage_cmdIncorrect().display(commandSender);
                                                    }
                                                } else {
                                                    cfg.getMessage_canvasLocked().display(commandSender);
                                                }
                                                break;
                                            default:
                                                cfg.getMessage_cmdModHelp().display(commandSender, new Placeholder("%cmd%", s));
                                                break;
                                        }
                                    }
                                });
                            } else {
                                cfg.getMessage_cmdModHelp().display(commandSender, new Placeholder("%cmd%", s));
                            }
                        } else {
                            cfg.getMessage_cmdNoPerm().display(commandSender);
                        }
                    }
                    return true;
                default:
                    cfg.getMessage_cmdHelp().display(commandSender, new Placeholder("%cmd%", s));
            }
        } else {
            cfg.getMessage_cmdHelp().display(commandSender, new Placeholder("%cmd%", s));
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String ss, @NotNull String[] args) {
        List<String> list = Arrays.asList("join", "leave", "timelapse", "wipe", "web", "users", "mod");
        String input = args[0].toLowerCase();
        Config cfg = plugin.getMainConfig();
        if (args.length > 1) {
            input = args[1].toLowerCase();
            if (args[0].equalsIgnoreCase("web") && (commandSender.hasPermission(cfg.getCommand_web_usePermission()) || cfg.getCommand_web_usePermission().equals("-"))) {
                list = Arrays.asList("canvas", "timelapse");
            } else if (args[0].equalsIgnoreCase("users") && (commandSender.hasPermission(cfg.getCommand_users_usePermission()) || cfg.getCommand_users_usePermission().equals("-"))) {
                list = Arrays.asList("wipe", "setLevel", "setExp", "setPainted");
            } else if (args[0].equalsIgnoreCase("mod") && (commandSender.hasPermission(cfg.getCommand_mod_usePermission()) || cfg.getCommand_mod_usePermission().equals("-"))) {
                if (args[1].equalsIgnoreCase("rollback")) {
                    if (args.length > 5) {
                        list = new ArrayList<>();
                    } else if (args.length > 4) {
                        list = Arrays.asList("1s", "10s", "1m", "10m", "1h", "10h", "1d", "10d");
                    } else if (args.length > 3) {
                        list = Arrays.asList("2", "5", "10", "30", "40", "50");
                    } else if (args.length > 2) {
                        list = new ArrayList<>();
                    }
                } else {
                    list = List.of("rollback");
                }
            } else {
                list = new ArrayList<>();
            }
        }


        List<String> completions = null;
        for (String s : list) {
            if (s.startsWith(input) || args.length > 2) {
                if (completions == null) {
                    completions = new ArrayList<>();
                }
                completions.add(s);
            }
        }

        if (completions != null)
            Collections.sort(completions);

        return completions;
    }
}