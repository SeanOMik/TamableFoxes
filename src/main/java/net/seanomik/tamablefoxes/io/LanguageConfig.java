package net.seanomik.tamablefoxes.io;

import net.seanomik.tamablefoxes.TamableFoxes;
import net.seanomik.tamablefoxes.versions.NMSInterface;
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

    // Auto replace alternate color codes.
    @Override
    public String getString(String path) {
        // Only attempt to translate if the text is not empty.
        return (super.getString(path).isEmpty()) ? super.getString(path) : ChatColor.translateAlternateColorCodes('&', super.getString(path));
    }

    // This is the text that shows when registering the custom entity
    public static String getMCVersionLoading(String mcVersionStr) {
        return getConfig().getString("mc-version-loading").replaceAll("%MC_VERSION%", mcVersionStr);
    }

    // Get the error that shows during register when they try to run the plugin on an unsupported mc version.
    public static String getUnsupportedMCVersionRegister() {
        return getConfig().getString("unsupported-mc-version-not-registering");
    }

    // Get the error that shows during disable when they try to run the plugin on an unsupported mc version.
    public static String getUnsupportedMCVersionDisable() {
        return getConfig().getString("unsupported-mc-version-disabling");
    }

    // Get the message that shows when we successfully replaced the entity.
    public static String getSuccessReplaced() {
        return getConfig().getString("success-replaced-entity");
    }

    // Get the error when it failed to replace the entity.
    public static String getFailureReplace() {
        return getConfig().getString("error-to-replaced-entity");
    }

    // Get the message when saving foxes.
    public static String getSavingFoxMessage() {
        return getConfig().getString("saving-foxes-message");
    }

    // Get the message that shows when you tame a fox.
    public static String getTamedMessage() {
        return getConfig().getString("taming-tamed-message");
    }

    // Get the message when you ask for the foxes name.
    public static String getTamingAskingName() {
        return getConfig().getString("taming-asking-for-name-message");
    }

    // Get the message when you give feed back on the new fox name.
    public static String getTamingChosenPerfect(String chosen) {
        return getConfig().getString("taming-chosen-name-perfect").replaceAll("%NEW_FOX_NAME%", chosen);
    }

    // Get the fox name format.
    public static String getFoxNameFormat(String foxName, String ownerName) {
        return getConfig().getString((Config.doesShowOwnerInFoxName()) ? "fox-name-format" : "fox-name-no-owner-name-format").replaceAll("%FOX_NAME%", foxName).replaceAll("%OWNER%", ownerName);
    }

    public static String getFoxDoesntTrust() {
        return getConfig().getString("fox-doesnt-trust");
    }

    public static String getNoPermMessage() {
        return getConfig().getString("no-permission");
    }

    public static String getOnlyRunPlayer() {
        return getConfig().getString("only-run-by-player");
    }

    public static String getSpawnedFoxMessage(NMSInterface.FoxType type) {
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

	