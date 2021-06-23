package net.seanomik.tamablefoxes;

import net.seanomik.tamablefoxes.util.NMSInterface;
import net.seanomik.tamablefoxes.util.Utils;
import net.seanomik.tamablefoxes.util.io.Config;
import net.seanomik.tamablefoxes.util.io.sqlite.SQLiteHelper;
import net.seanomik.tamablefoxes.versions.version_1_14_R1.NMSInterface_1_14_R1;
import net.seanomik.tamablefoxes.versions.version_1_15_R1.NMSInterface_1_15_R1;
import net.seanomik.tamablefoxes.versions.version_1_16_R1.NMSInterface_1_16_R1;
import net.seanomik.tamablefoxes.versions.version_1_16_R2.NMSInterface_1_16_R2;
import net.seanomik.tamablefoxes.versions.version_1_16_R3.NMSInterface_1_16_R3;
import net.seanomik.tamablefoxes.versions.version_1_17_R1.NMSInterface_1_17_R1;
import net.seanomik.tamablefoxes.util.io.LanguageConfig;
import org.bukkit.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class TamableFoxes extends JavaPlugin implements Listener {
    private static TamableFoxes plugin;

    private boolean versionSupported = true;

    public NMSInterface nmsInterface;

    @Override
    public void onLoad() {
        plugin = this;
        Utils.tamableFoxesPlugin = this;

        Config.setConfig(this.getConfig());
        LanguageConfig.getConfig(this).saveDefault();

        // Verify server version
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        switch (version) {
            case "v1_14_R1":
                nmsInterface = new NMSInterface_1_14_R1();
                break;
            case "v1_15_R1":
                nmsInterface = new NMSInterface_1_15_R1();
                break;
            case "v1_16_R1":
                nmsInterface = new NMSInterface_1_16_R1();
                break;
            case "v1_16_R2":
                nmsInterface = new NMSInterface_1_16_R2();
                break;
            case "v1_16_R3":
                nmsInterface = new NMSInterface_1_16_R3();
                break;
            case "v1_17_R1":
                nmsInterface = new NMSInterface_1_17_R1();
                break;
            default:
                Bukkit.getServer().getConsoleSender().sendMessage(Config.getPrefix() + ChatColor.RED + LanguageConfig.getUnsupportedMCVersionRegister());
                Bukkit.getServer().getConsoleSender().sendMessage(Config.getPrefix() + ChatColor.RED + "You're trying to run MC version " + version + " which is not supported!");
                versionSupported = false;
                return;
        }

        // Display starting message then register entity.
        Bukkit.getServer().getConsoleSender().sendMessage(Config.getPrefix() + ChatColor.YELLOW + LanguageConfig.getMCVersionLoading(version));
        nmsInterface.registerCustomFoxEntity();

        if (Config.getMaxPlayerFoxTames() != 0) {
            SQLiteHelper.getInstance(this).createTablesIfNotExist();
        }
    }

    @Override
    public void onEnable() {
        if (!versionSupported) {
            Bukkit.getServer().getConsoleSender().sendMessage(Config.getPrefix() + ChatColor.RED + LanguageConfig.getUnsupportedMCVersionDisable());
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
        getServer().getConsoleSender().sendMessage(Config.getPrefix() + ChatColor.YELLOW + LanguageConfig.getSavingFoxMessage());
    }

    public static TamableFoxes getPlugin() {
        return plugin;
    }
}
