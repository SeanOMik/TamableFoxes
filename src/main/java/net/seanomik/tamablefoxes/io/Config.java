package net.seanomik.tamablefoxes.io;

import net.seanomik.tamablefoxes.TamableFoxes;
import org.bukkit.entity.Player;

public class Config {
    private static TamableFoxes plugin = TamableFoxes.getPlugin();

    public static boolean doesShowOwnerFoxName() {
        return plugin.getConfig().getBoolean("show-owner-in-fox-name");
    }

    public static boolean doesTamedAttackWildAnimals() {
        return plugin.getConfig().getBoolean("tamed-behavior.attack-wild-animals");
    }

    public static boolean canPlayerTameFox(Player player) {
        return !plugin.getConfig().getBoolean("enable-taming-permission") || (plugin.getConfig().getBoolean("enable-taming-permission") && (player.hasPermission("tamablefoxes.tame") || player.isOp()));
    }

    public static boolean askForNameAfterTaming() {
        return plugin.getConfig().getBoolean("ask-for-name-after-taming");
    }

}
