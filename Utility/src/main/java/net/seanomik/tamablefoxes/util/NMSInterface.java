package net.seanomik.tamablefoxes.util;

import org.bukkit.Location;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface NMSInterface {
    enum FoxType {
        RED,
        SNOW
    }

    void registerCustomFoxEntity();
    void spawnTamableFox(Location loc, FoxType type);
    void changeFoxOwner(Fox fox, Player newOwner);
    UUID getFoxOwner(Fox fox);
    void renameFox(org.bukkit.entity.Fox fox, Player player);
}