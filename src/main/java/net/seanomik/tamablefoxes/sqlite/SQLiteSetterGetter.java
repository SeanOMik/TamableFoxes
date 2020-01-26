package net.seanomik.tamablefoxes.sqlite;

import net.minecraft.server.v1_15_R1.EntityFox;
import net.minecraft.server.v1_15_R1.EntityLiving;
import net.minecraft.server.v1_15_R1.EnumItemSlot;
import net.seanomik.tamablefoxes.EntityTamableFox;
import net.seanomik.tamablefoxes.TamableFoxes;
import net.seanomik.tamablefoxes.Utils;
import net.seanomik.tamablefoxes.io.LanguageConfig;
import org.apache.commons.lang.ObjectUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SQLiteSetterGetter {
    public static TamableFoxes plugin;
    public static SQLiteHandler sqLiteHandler;

    public void createTablesIfNotExist() {
        plugin = TamableFoxes.getPlugin(TamableFoxes.class);
        sqLiteHandler = plugin.sqLiteHandler;

        String foxesTable =
                "CREATE TABLE IF NOT EXISTS `foxes` ( " +
                        "`ENTITY_UUID` TEXT PRIMARY KEY ,  " +
                        "`OWNER_UUID` TEXT NOT NULL ,  " +
                        "`NAME` TEXT, " +
                        "`SITTING` INTEGER NOT NULL ,  " +
                        "`SLEEPING` INTEGER NOT NULL);";

        try {
            sqLiteHandler.connect();
            // Create previous bans table
            DatabaseMetaData dbm = sqLiteHandler.getConnection().getMetaData();
            ResultSet tables = dbm.getTables(null, null, "foxes", null);
            if (!tables.next()) {
                PreparedStatement statement = sqLiteHandler.getConnection().prepareStatement(foxesTable);
                statement.executeUpdate();

                plugin.getServer().getConsoleSender().sendMessage(Utils.getPrefix() + LanguageConfig.getCreatedSQLDatabase());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (sqLiteHandler.getConnection() != null) {
                try {
                    sqLiteHandler.getConnection().close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void saveFox(EntityTamableFox fox) {
        plugin = TamableFoxes.getPlugin(TamableFoxes.class);

        // If the fox is null or not alive, delete it from the database
        if (fox == null || !fox.isAlive()) {
            removeFox(fox);
            return;
        }

        try {
            sqLiteHandler.connect();

            PreparedStatement statement = sqLiteHandler.getConnection().prepareStatement("INSERT INTO foxes (ENTITY_UUID,OWNER_UUID,NAME,SITTING,SLEEPING) VALUES (?,?,?,?,?)");

            // If the database does contain this fox, then change the statement to an update statement instead of insert.
            PreparedStatement hasFoxStatement = sqLiteHandler.getConnection().prepareStatement("SELECT * FROM foxes WHERE ENTITY_UUID=?");
            hasFoxStatement.setString(1, fox.getUniqueID().toString());
            ResultSet results = hasFoxStatement.executeQuery();
            if (results.next()) {
                statement = sqLiteHandler.getConnection().prepareStatement("UPDATE foxes SET OWNER_UUID=?, NAME=?, SITTING=?, SLEEPING=? WHERE ENTITY_UUID='" + fox.getUniqueID().toString() + "'");
                statement.setString(1, (fox.getOwner() == null) ? "none" : fox.getOwner().getUniqueID().toString());
                statement.setString(2, fox.getChosenName());
                statement.setInt(3, (fox.isSitting()) ? 1 : 0);
                statement.setInt(4, (fox.isSleeping()) ? 1 : 0);
            } else {
                statement.setString(1, fox.getUniqueID().toString());
                statement.setString(2, (fox.getOwner() == null) ? "none" : fox.getOwner().getUniqueID().toString());
                statement.setString(3, fox.getChosenName());
                statement.setInt(4, (fox.isSitting()) ? 1 : 0);
                statement.setInt(5, (fox.isSleeping()) ? 1 : 0);
            }

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (sqLiteHandler.getConnection() != null) {
                try {
                    sqLiteHandler.getConnection().close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void saveFoxes(List<EntityTamableFox> foxes) { // @TODO: Optimize?
        if (foxes == null || foxes.size() == 0) return;

        for (EntityTamableFox fox : foxes) {
            saveFox(fox);
        }
    }

    public List<EntityTamableFox> loadFoxes() {
        plugin = TamableFoxes.getPlugin(TamableFoxes.class);
        List<UUID> toRemoveLater = new ArrayList<>();
        try {
            sqLiteHandler.connect();
            PreparedStatement statement = sqLiteHandler.getConnection().prepareStatement("SELECT * FROM foxes");
            ResultSet results = statement.executeQuery();

            List<EntityTamableFox> spawnedFoxes = new ArrayList<>();
            while (results.next()) { // Loop through each row
                UUID entityUUID = UUID.fromString(results.getString("ENTITY_UUID"));
                String ownerUUIDString = results.getString("OWNER_UUID");
                String name = results.getString("NAME");
                boolean sitting = results.getInt("SITTING") == 1;
                boolean sleeping = results.getInt("SLEEPING") == 1;

                // If the entity is null, it doesn't exist anymore so remove it from database
                if (plugin.getServer().getEntity(entityUUID) == null) {
                    toRemoveLater.add(entityUUID);
                    continue;
                }

                boolean tamed = false;
                EntityTamableFox tamableFox = (EntityTamableFox) ((CraftEntity) plugin.getServer().getEntity(entityUUID)).getHandle();
                if (!ownerUUIDString.equals("none")) {
                    tamed = true;

                    OfflinePlayer owner = plugin.getServer().getOfflinePlayer(UUID.fromString(ownerUUIDString));
                    if (owner.isOnline()) {
                        EntityLiving livingOwner = (EntityLiving) ((CraftEntity) owner).getHandle();
                        tamableFox.setOwner(livingOwner);
                    }

                    tamableFox.setOwnerUUID(owner.getUniqueId());
                    tamableFox.setTamed(true);
                    tamableFox.setChosenName(name);
                }

                // Fox may spawn standing if the server was closed while it was sitting.
                if (sitting) {
                    if (tamed) {
                        tamableFox.setHardSitting(true);
                    }
                } else if (sleeping) {
                    tamableFox.setSleeping(true);
                }

                spawnedFoxes.add(tamableFox);
            }

            return spawnedFoxes;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (sqLiteHandler.getConnection() != null) {
                try {
                    sqLiteHandler.getConnection().close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            // Remove those to remove later UUIDs
            for (UUID uuid : toRemoveLater) {
                removeFox(uuid);
            }
        }
        return null;
    }

    public void removeFox(UUID uuid) {
        plugin = TamableFoxes.getPlugin(TamableFoxes.class);
        try {
            sqLiteHandler.connect();

            PreparedStatement statement = sqLiteHandler.getConnection().prepareStatement("DELETE FROM foxes WHERE ENTITY_UUID='" + uuid.toString() + "'");
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (sqLiteHandler.getConnection() != null) {
                try {
                    sqLiteHandler.getConnection().close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void removeFox(EntityTamableFox fox) {
        removeFox(fox.getUniqueID());
    }
}
