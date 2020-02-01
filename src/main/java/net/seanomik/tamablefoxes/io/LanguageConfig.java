package net.seanomik.tamablefoxes.io;

import net.minecraft.server.v1_15_R1.EntityFox;
import net.seanomik.tamablefoxes.TamableFoxes;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class LanguageConfig extends YamlConfiguration  {
    private static LanguageConfig config;
    private TamableFoxes plugin;
    private File configFile;

    public static LanguageConfig getConfig() {
        if (LanguageConfig.config == null) {
            LanguageConfig.config = new LanguageConfig();
        }
        return LanguageConfig.config;
    }

    public LanguageConfig() {
        this.plugin = TamableFoxes.getPlugin();
        this.configFile = new File(this.plugin.getDataFolder(), "language.yml");
        this.saveDefault();
        this.reload();
    }
    
    public void reload() {
        try {
            super.load(this.configFile);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void save() {
        try {
            super.save(this.configFile);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void saveDefault() {
        this.plugin.saveResource("language.yml", false);
    }
    
    public void saveConfig() {
        try {
            super.save(this.configFile);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void reloadConfig() {
        try {
            super.load(this.configFile);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void saveDefaultConfig() {
        try {
            this.plugin.saveDefaultConfig();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getUnsupportedMCVersionRegister() {
        return getConfig().getString("unsupported-mc-version-not-registering");
    }

    public static String getUnsupportedMCVersionDisable() {
        return getConfig().getString("unsupported-mc-version-disabling");
    }

    public static String getSuccessReplaced() {
        return getConfig().getString("success-replaced-entity");
    }

    public static String getFailureReplace() {
        return getConfig().getString("error-to-replaced-entity");
    }

    public static String getSavingFoxMessage() {
        return getConfig().getString("saving-foxes-message");
    }

    public static String getTamedMessage() {
        return getConfig().getString("taming-tamed-message");
    }

    public static String getTamingAskingName() {
        return getConfig().getString("taming-asking-for-name-message");
    }

    public static String getTamingChosenPerfect(String chosen) {
        return getConfig().getString("taming-chosen-name-perfect").replaceAll("%NAME%", chosen);
    }

    public static String getFoxNameFormat() {
        return getConfig().getString("fox-name-format");
    }

    public static String getNoPermMessage() {
        return getConfig().getString("no-permission");
    }

    public static String getOnlyRunPlayer() {
        return getConfig().getString("only-run-by-player");
    }

    public static String getSpawnedFoxMessage(EntityFox.Type type) {
        String typeStr = ((type == type.SNOW) ? ChatColor.AQUA + "Snow" : ChatColor.RED + "Red") + ChatColor.RESET;
        return getConfig().getString("spawned-fox-message").replaceAll("%TYPE%", typeStr);
    }

    public static String getFailureSpawn() {
        return getConfig().getString("failed-to-spawn-message");
    }

    public static String getReloadMessage() {
        return getConfig().getString("reloaded-message");
    }
}

	