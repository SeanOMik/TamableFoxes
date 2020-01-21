package net.seanomik.tamablefoxes.io;

import com.google.common.collect.Maps;
import net.seanomik.tamablefoxes.TamableFoxes;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class FileManager {

    private final TamableFoxes plugin;
    private HashMap<String, Config> configs;

    public FileManager(TamableFoxes plugin) {
        this.plugin = plugin;
        this.configs = Maps.newHashMap();
    }

    public Config getConfig(String name) {
        if (!this.configs.containsKey(name)) {
            this.configs.put(name, new Config(name));
        }

        return this.configs.get(name);
    }

    public Config saveConfig(String name) {
        return this.getConfig(name).save();
    }

    public Config reloadConfig(String name) {
        return this.getConfig(name).reload();
    }

    public class Config {
        private String name;
        private File file;
        private YamlConfiguration config;

        public Config(String name) {
            this.name = name;
        }

        public Config save() {
            if (this.config != null && this.file != null) {
                try {
                    if (this.config.getKeys(true).size() != 0) {
                        this.config.save(this.file);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return this;
            } else {
                return this;
            }
        }

        public YamlConfiguration get() {
            if (this.config == null) {
                this.reload();
            }

            return this.config;
        }

        public Config saveDefaultConfig() {
            this.file = new File(plugin.getDataFolder(), this.name);
            plugin.saveResource(this.name, false);
            return this;
        }

        public Config reload() {
            if (this.file == null) {
                this.file = new File(plugin.getDataFolder(), this.name);
            }

            this.config = YamlConfiguration.loadConfiguration(this.file);

            try {
                Reader defConfigStream = new InputStreamReader(plugin.getResource(this.name), StandardCharsets.UTF_8);
                if (defConfigStream != null) {
                    YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
                    this.config.setDefaults(defConfig);
                }
            } catch (NullPointerException ignored) {
            }

            return this;
        }

        public Config copyDefaults(boolean force) {
            this.get().options().copyDefaults(force);
            return this;
        }

        public Config set(String key, Object value) {
            this.get().set(key, value);
            return this;
        }

        public Object get(String key) {
            return this.get().get(key);
        }
    }

}
