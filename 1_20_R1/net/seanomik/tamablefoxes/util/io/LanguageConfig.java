package net.seanomik.tamablefoxes.util.io;

import net.seanomik.tamablefoxes.util.NMSInterface;
import net.seanomik.tamablefoxes.util.NMSInterface.FoxType;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class LanguageConfig extends YamlConfiguration  {
    private static LanguageConfig config;
    private JavaPlugin plugin;
    private File configFile;

    public static LanguageConfig getConfig(JavaPlugin plugin) {
        if (LanguageConfig.config == null) {
            LanguageConfig.config = new LanguageConfig(plugin);
        }
        return LanguageConfig.config;
    }

    public LanguageConfig(JavaPlugin plugin) {
        this.plugin = plugin;
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
        if (contains(path)) {
            return (super.getString(path).isEmpty()) ? super.getString(path) : ChatColor.translateAlternateColorCodes('&', super.getString(path));
        }

        return "";
    }

    // This is the text that shows when registering the custom entity
    public static String getMCVersionLoading(String mcVersionStr) {
        return config.getString("mc-version-loading").replace("%MC_VERSION%", mcVersionStr);
    }

    // Get the error that shows during register when they try to run the plugin on an unsupported mc version.
    public static String getUnsupportedMCVersionRegister() {
        return config.getString("unsupported-mc-version-not-registering");
    }

    // Get the error that shows during disable when they try to run the plugin on an unsupported mc version.
    public static String getUnsupportedMCVersionDisable() {
        return config.getString("unsupported-mc-version-disabling");
    }

    // Get the message that shows when we successfully replaced the entity.
    public static String getSuccessReplaced() {
        return config.getString("success-replaced-entity");
    }

    // Get the error when it failed to replace the entity.
    public static String getFailureReplace() {
        return config.getString("error-to-replaced-entity");
    }

    // Get the message when saving foxes.
    public static String getSavingFoxMessage() {
        return config.getString("saving-foxes-message");
    }

    // Get the message that shows when you tame a fox.
    public static String getTamedMessage() {
        return config.getString("taming-tamed-message");
    }

    // Get the message when you ask for the foxes name.
    public static String getTamingAskingName() {
        return config.getString("taming-asking-for-name-message");
    }

    // Get the message when you give feed back on the new fox name.
    public static String getTamingChosenPerfect(String chosen) {
        return config.getString("taming-chosen-name-perfect").replace("%NEW_FOX_NAME%", chosen);
    }

    // Get the fox name format.
    public static String getFoxNameFormat(String foxName, String ownerName) {
        return config.getString((Config.doesShowOwnerInFoxName()) ? "fox-name-format" : "fox-name-no-owner-name-format").replace("%FOX_NAME%", foxName).replace("%OWNER%", ownerName);
    }

    public static String getFoxDoesntTrust() {
        return config.getString("fox-doesnt-trust");
    }

    public static String getNoPermMessage() {
        return config.getString("no-permission");
    }

    public static String getGiveFoxOtherNoPermMessage() {
        String str = config.getString("givefox-other-player-no-permission");
        if (str == null || str.isEmpty()) {
            str = "The other player you're trying to fix the fox to is unable to receive it!";
        }
        return str;
    }

    public static String getInteractWithTransferringFox(Player transferringTo) {
        String str = config.getString("givefox-interact-with-transferring-fox");
        if (str == null || str.isEmpty()) {
            str = "Right click the fox that you want to give to " + transferringTo.getDisplayName() + ".";
        } else {
            str = str.replace("%TRANSFER_TO_PLAYER%", transferringTo.getDisplayName());
        }

        return str;
    }

    public static String getGaveFox(Player givingTo) {
        String str = config.getString("givefox-gave-fox");
        if (str == null || str.isEmpty()) {
            str = "Fox has been given to " + givingTo.getDisplayName() + "!";
        } else {
            str = str.replace("%GAVE_TO_PLAYER%", givingTo.getDisplayName());
        }

        return str;
    }

    public static String getNotYourFox() {
        String str = config.getString("givefox-not-your-fox");
        if (str == null || str.isEmpty()) {
            str = "This is not your fox to give!";
        }

        return str;
    }

    public static String getTooLongInteraction() {
        String str = config.getString("givefox-interact-timeout");
        if (str == null || str.isEmpty()) {
            str = "You took too long to interact with a fox!";
        }

        return str;
    }

    public static String getPlayerDoesNotExist() {
        String str = config.getString("givefox-player-does-not-exist");
        if (str == null || str.isEmpty()) {
            str = "The player does not exist!";
        }

        return str;
    }

    public static String getOnlyRunPlayer() {
        return config.getString("only-run-by-player");
    }

    public static String getSpawnedFoxMessage(NMSInterface.FoxType type) {
        String typeStr = ((type == FoxType.SNOW) ? ChatColor.AQUA + "Snow" : ChatColor.RED + "Red") + ChatColor.RESET;
        return config.getString("spawned-fox-message").replace("%TYPE%", typeStr);
    }

    public static String getFailureSpawn() {
        return config.getString("failed-to-spawn-message");
    }

    public static String getReloadMessage() {
        return config.getString("reloaded-message");
    }
}

	