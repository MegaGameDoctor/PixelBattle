package net.gamedoctor.pixelbattle.config.messages;

import me.clip.placeholderapi.PlaceholderAPI;
import net.gamedoctor.pixelbattle.PixelBattle;
import net.gamedoctor.pixelbattle.config.Config;
import net.gamedoctor.pixelbattle.utils.Utils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Message {
    private final PixelBattle plugin;
    private String broadcast;
    private String chatMessage;
    private String actionBar;
    private String title;
    private String subtitle;
    private Sound sound;
    private int titleFadeIn;
    private int titleStay;
    private int titleFadeOut;

    public Message(PixelBattle plugin, String section) {
        String path = "messages." + section + ".";
        FileConfiguration cfg = plugin.getConfig();
        Utils utils = plugin.getUtils();
        this.plugin = plugin;

        String prefix = utils.color(cfg.getString("settings.messagesPrefix"));
        broadcast = "";
        chatMessage = "";
        title = "";
        subtitle = "";
        actionBar = "";
        sound = null;
        if (cfg.getBoolean(path + "chat.enable", false)) {
            StringBuilder chatStrBuilder = new StringBuilder();
            for (String line : cfg.getStringList(path + "chat.lines")) {
                chatStrBuilder.append(prefix).append(utils.color(line)).append("\n");
            }
            chatMessage = chatStrBuilder.substring(0, chatStrBuilder.length() - 1);
        }

        if (cfg.getBoolean(path + "title.enable", false)) {
            title = utils.color(cfg.getString(path + "title.line1", ""));
            subtitle = utils.color(cfg.getString(path + "title.line2", ""));
            titleFadeIn = cfg.getInt(path + "title.fadeIn");
            titleStay = cfg.getInt(path + "title.stay");
            titleFadeOut = cfg.getInt(path + "title.fadeOut");
        }

        if (cfg.getBoolean(path + "actionBar.enable", false)) {
            actionBar = utils.color(cfg.getString(path + "actionBar.message", ""));
        }

        if (cfg.getBoolean(path + "sound.enable", false)) {
            sound = Sound.valueOf(cfg.getString(path + "sound.name").toUpperCase());
        }

        if (cfg.getBoolean(path + "broadcast.enable", false)) {
            StringBuilder broadcastStrBuilder = new StringBuilder();
            for (String line : cfg.getStringList(path + "broadcast.lines")) {
                broadcastStrBuilder.append(prefix).append(utils.color(line)).append("\n");
            }
            broadcast = broadcastStrBuilder.substring(0, broadcastStrBuilder.length() - 1);
        }
    }

    public void display(CommandSender commandSender, Placeholder... placeholders) {
        if (commandSender != null) {
            if (commandSender instanceof Player) {
                display((Player) commandSender, placeholders);
            } else {
                if (!chatMessage.isEmpty()) {
                    String chatMsg = chatMessage.replace("%player%", commandSender.getName());
                    for (Placeholder placeholder : placeholders) {
                        chatMsg = chatMsg.replace(placeholder.getTarget(), placeholder.getReplace());
                    }

                    for (String line : chatMsg.split("\\n")) {
                        commandSender.sendMessage(line);
                    }
                }

                if (!broadcast.isEmpty()) {
                    String broadcastMsg = broadcast.replace("%player%", commandSender.getName());
                    for (Placeholder placeholder : placeholders) {
                        broadcastMsg = broadcastMsg.replace(placeholder.getTarget(), placeholder.getReplace());
                    }
                    Bukkit.broadcastMessage(broadcastMsg);
                }
            }
        }
    }

    public void display(Player player, Placeholder... placeholders) {
        Config cfg = plugin.getMainConfig();
        if (player != null) {
            if (!chatMessage.isEmpty()) {
                String chatMsg = chatMessage.replace("%player%", player.getName());
                for (Placeholder placeholder : placeholders) {
                    chatMsg = chatMsg.replace(placeholder.getTarget(), placeholder.getReplace());
                }

                if (cfg.isUsingPlaceholderAPI()) {
                    chatMsg = PlaceholderAPI.setPlaceholders(player, chatMsg);
                }

                player.sendMessage(chatMsg);
            }

            if (!title.isEmpty()) {
                String parsedTitle = title.replace("%player%", player.getName());
                String parsedSubtitle = subtitle.replace("%player%", player.getName());
                for (Placeholder placeholder : placeholders) {
                    parsedTitle = parsedTitle.replace(placeholder.getTarget(), placeholder.getReplace());
                    parsedSubtitle = parsedSubtitle.replace(placeholder.getTarget(), placeholder.getReplace());
                }

                if (cfg.isUsingPlaceholderAPI()) {
                    parsedTitle = PlaceholderAPI.setPlaceholders(player, parsedTitle);
                    parsedSubtitle = PlaceholderAPI.setPlaceholders(player, parsedSubtitle);
                }

                player.sendTitle(parsedTitle, parsedSubtitle, titleFadeIn, titleStay, titleFadeOut);
            }

            if (!actionBar.isEmpty()) {
                String parsedActionBar = actionBar.replace("%player%", player.getName());
                for (Placeholder placeholder : placeholders) {
                    parsedActionBar = parsedActionBar.replace(placeholder.getTarget(), placeholder.getReplace());
                }

                if (cfg.isUsingPlaceholderAPI()) {
                    parsedActionBar = PlaceholderAPI.setPlaceholders(player, parsedActionBar);
                }

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(parsedActionBar));
            }

            if (sound != null) {
                player.playSound(player.getLocation(), sound, 1F, 1F);
            }

            if (!broadcast.isEmpty()) {
                String broadcastMsg = broadcast.replace("%player%", player.getName());
                for (Placeholder placeholder : placeholders) {
                    broadcastMsg = broadcastMsg.replace(placeholder.getTarget(), placeholder.getReplace());
                }

                if (cfg.isUsingPlaceholderAPI()) {
                    broadcastMsg = PlaceholderAPI.setPlaceholders(player, broadcastMsg);
                }

                Bukkit.broadcastMessage(broadcastMsg);
            }
        }
    }
}