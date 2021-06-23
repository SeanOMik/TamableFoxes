package net.seanomik.tamablefoxes.util;

import org.bukkit.Location;

public interface NMSInterface {
    enum FoxType {
        RED,
        SNOW
    }

    public void registerCustomFoxEntity();
    public void spawnTamableFox(Location loc, FoxType type);
}