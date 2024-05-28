package net.gamedoctor.pixelbattle.utils;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.RequiredArgsConstructor;
import net.gamedoctor.pixelbattle.PixelBattle;
import net.gamedoctor.pixelbattle.PixelPlayer;
import net.gamedoctor.pixelbattle.config.Config;
import net.gamedoctor.pixelbattle.config.items.ColorItem;
import net.gamedoctor.pixelbattle.config.items.MenuItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class Utils {
    private final PixelBattle plugin;

    public String color(String from) {
        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        from = from.replace("&#", "#");
        Matcher matcher = pattern.matcher(from);
        while (matcher.find()) {
            String hexCode = from.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace('#', 'x');
            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch)
                builder.append("&").append(c);
            from = from.replace(hexCode, builder.toString());
            matcher = pattern.matcher(from);
        }

        return ChatColor.translateAlternateColorCodes('&', from);
    }

    public ItemStack makeItem(Material material, String name, LinkedList<String> lore, boolean glowing) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setLore(lore);
        itemMeta.setDisplayName(name);
        if (glowing) {
            itemMeta.addEnchant(Enchantment.MENDING, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public String formatTime(long millis) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    public String getTimeString(long time) {
        Config cfg = plugin.getMainConfig();
        int seconds = (int) ((time /= 1000L) % 60L);
        int minutes = (int) ((time /= 60L) % 60L);
        int hours = (int) ((time /= 60L) % 24L);
        int days = (int) (time / 24L);

        StringBuilder sb = new StringBuilder();
        if (cfg.isTimeStringFormat_displayOnlyHighest()) {
            if (days != 0) {
                sb.append(days).append(" ").append(cfg.getTimeStringFormat_days());
            } else if (hours != 0) {
                sb.append(hours).append(" ").append(cfg.getTimeStringFormat_hours());
            } else if (minutes != 0) {
                sb.append(minutes).append(" ").append(cfg.getTimeStringFormat_minutes());
            } else if (seconds >= 0) {
                sb.append(seconds).append(" ").append(cfg.getTimeStringFormat_seconds());
            }
        } else {
            if (days != 0) {
                sb.append(days).append(" ").append(cfg.getTimeStringFormat_days()).append(" ");
            }

            if (hours != 0) {
                sb.append(hours).append(" ").append(cfg.getTimeStringFormat_hours()).append(" ");
            }

            if (minutes != 0) {
                sb.append(minutes).append(" ").append(cfg.getTimeStringFormat_minutes()).append(" ");
            }

            if (seconds != 0) {
                sb.append(seconds).append(" ").append(cfg.getTimeStringFormat_seconds());
            }
        }
        return sb.toString().trim();
    }

    public String getTimeToNextPixel(Player player, boolean replaceZero) {
        long time = plugin.getDatabaseManager().getPlayer(player.getName()).getNextPixel() - System.currentTimeMillis();
        int converted = Integer.parseInt(String.valueOf((time + 1000L) / 1000L));
        String remaining = String.valueOf(converted);
        if (plugin.getMainConfig().isTimeFormat()) {
            remaining = formatTime(time);
        }
        if (time <= 0 && replaceZero) {
            remaining = "-";
        }
        return remaining;
    }

    public void connectBungeeCordServer(Player player, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);

        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
    }

    public List<String> getNewColors(int newLevel) {
        List<String> colors = new ArrayList<>();
        for (ColorItem colorItem : plugin.getMainConfig().getItems().values()) {
            if (colorItem.getNeedLevel() == newLevel) {
                colors.add(colorItem.getName());
            }
        }
        return colors;
    }

    public List<ColorItem> getAvailableColors(Player player, int level) {
        List<ColorItem> colors = new ArrayList<>();
        for (ColorItem colorItem : plugin.getMainConfig().getItems().values()) {
            if (colorItem.getNeedLevel() <= level && (colorItem.getPermission().equalsIgnoreCase("-") || player.hasPermission(colorItem.getPermission()))) {
                colors.add(colorItem);
            }
        }
        return colors;
    }

    public boolean isNumber(String number) {
        try {
            Double.parseDouble(number);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public int getPlayerCooldown(PixelPlayer pixelPlayer) {
        String permPrefix = plugin.getMainConfig().getCooldownPermission();

        int defaultCooldown = plugin.getMainConfig().getLevelingConfig().getLevelData(pixelPlayer.getLevel()).getPaintCooldown();

        List<PermissionAttachmentInfo> perms = pixelPlayer.getBukkitPlayer().getEffectivePermissions().stream().filter(PermissionAttachmentInfo::getValue).filter((x) -> x.getPermission().startsWith(permPrefix)).collect(Collectors.toList());
        if (perms.isEmpty()) return defaultCooldown;

        AtomicInteger maxVal = new AtomicInteger(0);
        perms.forEach((perm) -> {
            String permPart = perm.getPermission().replace(permPrefix, "");
            int radius = Integer.parseInt(permPart);
            if (radius > maxVal.get()) maxVal.set(radius);
        });

        if (maxVal.intValue() >= 0) return maxVal.intValue();

        return defaultCooldown;
    }

    public void setMenuItem(Player player, Inventory inventory, int slot, MenuItem menuItem) {
        if (menuItem.isEnable() && (menuItem.getPermission().equalsIgnoreCase("-") || player.hasPermission(menuItem.getPermission()))) {
            inventory.setItem(slot, menuItem.getItemStack());
        }
    }

    public void setMenuItem(Player player, Inventory inventory, int slot, MenuItem menuItem, LinkedList<String> lore) {
        setMenuItem(player, inventory, slot, menuItem, lore, menuItem.getName(), menuItem.getMaterial());
    }

    public void setMenuItem(Player player, Inventory inventory, int slot, MenuItem menuItem, LinkedList<String> lore, String name) {
        setMenuItem(player, inventory, slot, menuItem, lore, name, menuItem.getMaterial());
    }

    public void setMenuItem(Player player, Inventory inventory, int slot, MenuItem menuItem, LinkedList<String> lore, String name, Material material) {
        if (menuItem.isEnable() && (menuItem.getPermission().equalsIgnoreCase("-") || player.hasPermission(menuItem.getPermission()))) {
            inventory.setItem(slot, menuItem.recreateItemStack(plugin, lore, name, material));
        }
    }

    public String replaceDateAndTime(String source, long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        if (time <= 0) {
            dateFormat = new SimpleDateFormat("-");
            timeFormat = new SimpleDateFormat("-");
        }
        return source.replace("%date%", dateFormat.format(time)).replace("%time%", timeFormat.format(time));
    }
}