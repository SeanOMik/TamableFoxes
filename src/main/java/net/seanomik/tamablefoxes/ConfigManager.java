package net.seanomik.tamablefoxes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private TamableFoxes plugin = TamableFoxes.getPlugin(TamableFoxes.class);

    //Files and File Configs Here
    public FileConfiguration foxesSave;
    public File foxesfile;

    public void setup() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        foxesfile = new File(plugin.getDataFolder(), "Foxes.yml");

        if (!foxesfile.exists()) {
            try {
                foxesfile.createNewFile();
            } catch (IOException e) {
                Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Could not create the Foxes.yml file!");
            }
        }

        foxesSave = YamlConfiguration.loadConfiguration(foxesfile);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "The Foxes.yml file has been created successfully!");
    }

    public FileConfiguration getFoxes() {
        return foxesSave;
    }

    public void saveFoxes() {
        try {
            foxesSave.save(foxesfile);
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "The Foxes.yml file has been saved successfully!");
        } catch (IOException e) {
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Could not save the Foxes.yml file!");
        }
    }

    public void reloadFoxes() {
        foxesSave = YamlConfiguration.loadConfiguration(foxesfile);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.BLUE + "The Foxes.yml file has been reloaded successfully!");
    }
}
