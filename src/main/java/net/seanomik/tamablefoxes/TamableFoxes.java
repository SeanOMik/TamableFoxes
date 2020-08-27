package net.seanomik.tamablefoxes;

import net.seanomik.tamablefoxes.versions.NMSInterface;
import net.seanomik.tamablefoxes.versions.version_1_14_R1.NMSInterface_1_14_R1;
import net.seanomik.tamablefoxes.versions.version_1_15_R1.NMSInterface_1_15_R1;
import net.seanomik.tamablefoxes.versions.version_1_16_R1.NMSInterface_1_16_R1;
import net.seanomik.tamablefoxes.versions.version_1_16_R2.NMSInterface_1_16_R2;
import net.seanomik.tamablefoxes.io.LanguageConfig;
import org.bukkit.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

// @TODO:

/* @CHANGELOG (1.7-SNAPSHOT):
 *    Update to Minecraft 1.16.1.
 *    This jar file will also work with Minecraft 1.14.X, 1.15.X, and 1.16.X.
 *    Due to merging 1.14 support with all other versions, I also fixed SEVERAL issues that the versions for 1.14 had.
 *    Foxes now sleep with their owner once again.
 */
public final class TamableFoxes extends JavaPlugin implements Listener {
    private static TamableFoxes plugin;

    private boolean versionSupported = true;

    public NMSInterface nmsInterface;

    @Override
    public void onLoad() {
        plugin = this;

        LanguageConfig.getConfig().saveDefault();

        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        if (version.equals("v1_14_R1")) {
            nmsInterface = new NMSInterface_1_14_R1();
        } else if (version.equals("v1_15_R1")) {
            nmsInterface = new NMSInterface_1_15_R1();
        } else if (version.equals("v1_16_R1")) {
            nmsInterface = new NMSInterface_1_16_R1();
        } else if (version.equals("v1_16_R2")) {
            nmsInterface = new NMSInterface_1_16_R2();
        } else {
            Bukkit.getServer().getConsoleSender().sendMessage(Utils.getPrefix() + ChatColor.RED + LanguageConfig.getUnsupportedMCVersionRegister());
            versionSupported = false;
            return;
        }

        // Display starting message
        Bukkit.getServer().getConsoleSender().sendMessage(Utils.getPrefix() + ChatColor.YELLOW + LanguageConfig.getMCVersionLoading(version));
        nmsInterface.registerCustomFoxEntity();
    }

    @Override
    public void onEnable() {
        if (!versionSupported) {
            Bukkit.getServer().getConsoleSender().sendMessage(Utils.getPrefix() + ChatColor.RED + LanguageConfig.getUnsupportedMCVersionDisable());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("spawntamablefox").setExecutor(new CommandSpawnTamableFox(this));

        this.saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(Utils.getPrefix() + ChatColor.YELLOW + LanguageConfig.getSavingFoxMessage());
    }

    public static TamableFoxes getPlugin() {
        return plugin;
    }
}
