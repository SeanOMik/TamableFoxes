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
import net.seanomik.tamablefoxes.versions.version_1_17_1_R1.NMSInterface_1_17_1_R1;
import net.seanomik.tamablefoxes.util.io.LanguageConfig;

import org.bstats.bukkit.Metrics;
import org.bukkit.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.logging.Level;

public final class TamableFoxes extends JavaPlugin implements Listener {
    private static TamableFoxes plugin;
    public static final int BSTATS_PLUGIN_ID = 11944;

    private boolean versionSupported = true;

    public NMSInterface nmsInterface;

    private boolean equalOrBetween(double num, double min, double max) {
        return num >= min && num <= max;
    }

    @Override
    public void onLoad() {
        plugin = this;
        Utils.tamableFoxesPlugin = this;

        Config.setConfig(this.getConfig());
        LanguageConfig.getConfig(this).saveDefault();

        // Verify server version
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        String specificVersion = Bukkit.getVersion();
        specificVersion = specificVersion.substring(specificVersion.indexOf("(MC: ") + 5, specificVersion.indexOf(')'));

        double versionDouble = Double.parseDouble(specificVersion.substring(2));

        if (equalOrBetween(versionDouble, 14D, 14.4D)) {
            nmsInterface = new NMSInterface_1_14_R1();
        } else if (equalOrBetween(versionDouble, 15D, 15.2D)) {
            nmsInterface = new NMSInterface_1_15_R1();
        } else if (versionDouble == 16D) {
            nmsInterface = new NMSInterface_1_16_R1();
        } else if (versionDouble == 16.2D || versionDouble == 16.3D) {
            nmsInterface = new NMSInterface_1_16_R2();
        } else if (versionDouble == 16.4D || versionDouble == 16.5D) {
            nmsInterface = new NMSInterface_1_16_R3();
        } else if (versionDouble == 17D) {
            nmsInterface = new NMSInterface_1_17_R1();
        } else if (versionDouble == 17.1D) {
            nmsInterface = new NMSInterface_1_17_1_R1();
        } else {
            Bukkit.getServer().getConsoleSender().sendMessage(Config.getPrefix() + ChatColor.RED + LanguageConfig.getUnsupportedMCVersionRegister());
            Bukkit.getServer().getConsoleSender().sendMessage(Config.getPrefix() + ChatColor.RED + "You're trying to run MC version " + specificVersion + " which is not supported!");
            versionSupported = false;
        }

        // Display starting message then register entity.
        Bukkit.getServer().getConsoleSender().sendMessage(Config.getPrefix() + ChatColor.YELLOW + LanguageConfig.getMCVersionLoading(version));
        nmsInterface.registerCustomFoxEntity();

        if (Config.getMaxPlayerFoxTames() != 0) {
            SQLiteHelper.getInstance(this).createTablesIfNotExist();
        }

        Metrics metrics = new Metrics(this, BSTATS_PLUGIN_ID);
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
    public void saveResource(String resourcePath, boolean replace) {
        if (resourcePath == null || resourcePath.equals("")) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + getFile());
        }

        File outFile = new File(getDataFolder(), resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(getDataFolder(), resourcePath.substring(0, lastIndex >= 0 ? lastIndex : 0));

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try {
            if (!outFile.exists() || replace) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            }
            // Ignore could not save because it already exists.
            /* else {
                getLogger().log(Level.WARNING, "Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists.");
            }*/
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, ex);
        }
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(Config.getPrefix() + ChatColor.YELLOW + LanguageConfig.getSavingFoxMessage());
    }

    public static TamableFoxes getPlugin() {
        return plugin;
    }
}
