package net.seanomik.tamablefoxes.util.io;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Config {
    private static FileConfiguration config = null;

    public static void setConfig(FileConfiguration config) {
        Config.config = config;
    }

    // Does the owner's name show if the foxes name?
    public static boolean doesShowOwnerInFoxName() { return config.getBoolean("show-owner-in-fox-name"); }

    // Check if a tamed fox attacks wild animals.
    public static boolean doesTamedAttackWildAnimals() { return config.getBoolean("tamed-behavior.attack-wild-animals"); }

    // Check if the player can tame the fox.
    public static boolean canPlayerTameFox(Player player) {
        return player.hasPermission("tamablefoxes.tame") || player.isOp();
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
