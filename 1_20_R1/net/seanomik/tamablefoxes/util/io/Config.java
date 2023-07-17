package net.seanomik.tamablefoxes.util.io;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class Config {
    private static FileConfiguration config = null;

    public static void reloadConfig(Plugin plugin) {
        plugin.reloadConfig();
        Config.config = plugin.getConfig();
    }

    public static void setConfig(FileConfiguration config) {
        Config.config = config;
    }

    // Does the owner's name show if the foxes name?
    public static boolean doesShowOwnerInFoxName() { return config.getBoolean("show-owner-in-fox-name"); }

    // Check if a tamed fox attacks wild animals.
    public static boolean doesTamedAttackWildAnimals() { return config.getBoolean("tamed-behavior.attack-wild-animals"); }

    // Get worlds that taming is not allowed in.
    public static List<String> BannedWorlds() {
        return config.contains("no-tame-worlds") ? config.getStringList("no-tame-worlds") : new ArrayList<>();
    }

    // Check if the player can tame the fox.
    public static boolean canPlayerTameFox(Player player) {
        return ( player.hasPermission("tamablefoxes.tame") || player.isOp() ) &&
            ( !BannedWorlds().contains(player.getWorld().getName()) || player.hasPermission("tamablefoxes.tame.anywhere") );
    }

    public static int getMaxPlayerFoxTames() {
        return config.getInt("max-fox-tames");
    }

    // Check if the plugin asks for a fox name after taming.
    public static boolean askForNameAfterTaming() { return config.getBoolean("ask-for-name-after-taming"); }

    public static String getPrefix() {
        return ChatColor.translateAlternateColorCodes('&', config.contains("prefix") ? config.getString("prefix") : "&c[Tamable Foxes] ");
    }
}
