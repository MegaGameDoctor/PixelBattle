package net.gamedoctor.plugins;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class Metrics {

    public static void send(Plugin plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String nowVersion = plugin.getDescription().getVersion();
                URL url = new URL("https://spigot.kosfarix.ru/plugins/metrics/v2/send.php" + getMetricsArgs(plugin, nowVersion));
                URLConnection conn = url.openConnection();
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()))) {
                    String version = in.readLine().trim();
                    if (!version.equalsIgnoreCase(nowVersion)) {
                        plugin.getLogger().log(Level.WARNING, "A new version has been detected! (" + version + " > " + nowVersion + ")");
                        plugin.getLogger().log(Level.WARNING, "Download it from SpigotMC: https://www.spigotmc.org/members/gamedoctor.792259");
                    }
                }
            } catch (Exception ignored) {
                plugin.getLogger().log(Level.SEVERE, "Couldn't check the plugin version");
            }
        });
    }

    private static String getMetricsArgs(Plugin plugin, String nowVersion) {
        return "?plugin=" + URLEncoder.encode(plugin.getDescription().getName(), StandardCharsets.UTF_8) +
                "&version=" + URLEncoder.encode(nowVersion, StandardCharsets.UTF_8) +
                "&bukkitVersion=" + URLEncoder.encode(Bukkit.getVersion(), StandardCharsets.UTF_8) +
                "&bukkitName=" + URLEncoder.encode(Bukkit.getName(), StandardCharsets.UTF_8) +
                "&javaVersion=" + URLEncoder.encode(System.getProperty("java.version"), StandardCharsets.UTF_8) +
                "&osName=" + URLEncoder.encode(System.getProperty("os.name"), StandardCharsets.UTF_8) +
                "&osArch=" + URLEncoder.encode(System.getProperty("os.arch"), StandardCharsets.UTF_8) +
                "&osVersion=" + URLEncoder.encode(System.getProperty("os.version"), StandardCharsets.UTF_8);
    }
}