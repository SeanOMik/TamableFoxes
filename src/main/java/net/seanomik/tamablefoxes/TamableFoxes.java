package net.seanomik.tamablefoxes;

import net.seanomik.tamablefoxes.io.Config;
import net.seanomik.tamablefoxes.io.sqlite.SQLiteHandler;
import net.seanomik.tamablefoxes.io.sqlite.SQLiteHelper;
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

/* @CHANGELOG (1.7.9-SNAPSHOT):
 *    Fix some crashes and errors with the sqlite database failing to be found.
 */
public final class TamableFoxes extends JavaPlugin implements Listener {
    private static TamableFoxes plugin;

    private boolean versionSupported = true;

    public NMSInterface nmsInterface;

    @Override
    public void onLoad() {
        plugin = this;

        LanguageConfig.getConfig().saveDefault();

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
            default:
                Bukkit.getServer().getConsoleSender().sendMessage(Utils.getPrefix() + ChatColor.RED + LanguageConfig.getUnsupportedMCVersionRegister());
                versionSupported = false;
                return;
        }

        // Display starting message then register entity.
        Bukkit.getServer().getConsoleSender().sendMessage(Utils.getPrefix() + ChatColor.YELLOW + LanguageConfig.getMCVersionLoading(version));
        nmsInterface.registerCustomFoxEntity();

        if (Config.getMaxPlayerFoxTames() != 0) {
            SQLiteHelper.getInstance().createTablesIfNotExist();
        }
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
