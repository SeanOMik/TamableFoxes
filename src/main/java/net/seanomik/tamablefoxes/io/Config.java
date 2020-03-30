package net.seanomik.tamablefoxes.io;

import net.seanomik.tamablefoxes.TamableFoxes;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Config {
    private static FileConfiguration config = TamableFoxes.getPlugin().getConfig();

    // Does the owner's name show if the foxes name?
    public static boolean doesShowOwnerInFoxName() { return config.getBoolean("show-owner-in-fox-name"); }

    // Check if a tamed fox attacks wild animals.
    public static boolean doesTamedAttackWildAnimals() { return config.getBoolean("tamed-behavior.attack-wild-animals"); }

    // Check if the player can tame the fox.
    public static boolean canPlayerTameFox(Player player) {
        return !config.getBoolean("enable-taming-permission") || (config.getBoolean("enable-taming-permission") && (player.hasPermission("tamablefoxes.tame") || player.isOp()));
    }

    // Check if the plugin asks for a fox name after taming.
    public static boolean askForNameAfterTaming() { return config.getBoolean("ask-for-name-after-taming"); }
}
