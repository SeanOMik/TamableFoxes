package net.seanomik.tamablefoxes.sqlite;

import net.minecraft.server.v1_15_R1.EntityFox;
import net.minecraft.server.v1_15_R1.EnumItemSlot;
import net.seanomik.tamablefoxes.EntityTamableFox;
import net.seanomik.tamablefoxes.TamableFoxes;
import org.apache.commons.lang.ObjectUtils;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
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
        sqLiteHandler = TamableFoxes.sqLiteHandler;
        //String pluginDatabase = Reference.SQLiteDatabase;

        String foxesTable =
                "CREATE TABLE IF NOT EXISTS `foxes` ( " +
                        "`ID` INTEGER PRIMARY KEY AUTOINCREMENT ,  " +
                        "`OWNER_UUID` TEXT NOT NULL ,  " +
                        "`NAME` TEXT ,  " +
                        "`LOCATION` TEXT NOT NULL ,  " +
                        "`TYPE` TEXT NOT NULL ,  " +
                        "`SITTING` INTEGER NOT NULL ,  " +
                        "`SLEEPING` INTEGER NOT NULL ,  " +
                        "`MOUTH_ITEM` TEXT NOT NULL);"; // @TODO: Add a age field

        try {
            sqLiteHandler.connect();
            // Create previous bans table
            DatabaseMetaData dbm = sqLiteHandler.getConnection().getMetaData();
            ResultSet tables = dbm.getTables(null, null, "foxes", null);
            if (!tables.next()) {
                PreparedStatement statement = sqLiteHandler.getConnection().prepareStatement(foxesTable);
                statement.executeUpdate();

                plugin.getServer().getConsoleSender().sendMessage(TamableFoxes.getPrefix() + "Created foxes table!");
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
        try {
            sqLiteHandler.connect();

            PreparedStatement statement = sqLiteHandler.getConnection().prepareStatement("INSERT INTO foxes (OWNER_UUID,NAME,LOCATION,TYPE,MOUTH_ITEM,SITTING,SLEEPING) VALUES (?,?,?,?,?,?,?)");
            if (fox.databaseID != -1) {
                statement = sqLiteHandler.getConnection().prepareStatement("UPDATE foxes SET OWNER_UUID=?, NAME=?, LOCATION=?, TYPE=?, MOUTH_ITEM=?, SITTING=?, SLEEPING=? WHERE ID=" + fox.databaseID);
            }

            statement.setString(1, (fox.getOwner() == null) ? "none" : fox.getOwner().getUniqueID().toString());
            statement.setString(2, fox.getChosenName());
            statement.setString(3,  fox.getWorld().worldData.getName() + "," + fox.locX() + "," + fox.locY() + "," + fox.locZ());
            statement.setString(4, fox.getFoxType().toString());
            statement.setString(5, fox.getEquipment(EnumItemSlot.MAINHAND).toString().toUpperCase().substring(fox.getEquipment(EnumItemSlot.MAINHAND).toString().indexOf(' ')+1));
            statement.setInt(6, (fox.isSitting()) ? 1 : 0);
            statement.setInt(7, (fox.isSleeping()) ? 1 : 0);
            statement.executeUpdate();

            ResultSet result = statement.getGeneratedKeys();
            while (result.next()) {
                fox.databaseID = result.getInt(1);
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

    public void saveFoxes(List<EntityTamableFox> foxes) { // @TODO: Optimize?
        for (EntityTamableFox fox : foxes) {
            saveFox(fox);
        }
    }

    public List<EntityTamableFox> spawnFoxes() {
        plugin = TamableFoxes.getPlugin(TamableFoxes.class);
        try {
            sqLiteHandler.connect();
            PreparedStatement statement = sqLiteHandler.getConnection().prepareStatement("SELECT * FROM foxes");
            ResultSet results = statement.executeQuery();

            List<EntityTamableFox> foxList = new ArrayList<>();
            while (results.next()) { // Loop through each row
                List<String> locationList = Arrays.asList(results.getString("LOCATION").split("\\s*,\\s*"));
                Location loc = new Location(Bukkit.getWorld(locationList.get(0)), Double.parseDouble(locationList.get(1)), Double.parseDouble(locationList.get(2)), Double.parseDouble(locationList.get(3)));

                EntityTamableFox spawnedFox = (EntityTamableFox) plugin.spawnTamableFox(loc, EntityFox.Type.valueOf(results.getString("TYPE")));
                spawnedFox.databaseID = results.getInt("ID");
                spawnedFox.setSlot(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(new ItemStack(Material.valueOf(results.getString("MOUTH_ITEM")), 1)));

                spawnedFox.setSitting(results.getInt("SITTING") == 1);
                spawnedFox.setSleeping(results.getInt("SLEEPING") == 1);

                if (!results.getString("OWNER_UUID").equals("none")) {
                    UUID ownerUUID = UUID.fromString(results.getString("OWNER_UUID"));

                    OfflinePlayer owner = plugin.getServer().getOfflinePlayer(ownerUUID);
                    if (owner.isOnline()) {
                        spawnedFox.setOwner(((CraftPlayer) owner.getPlayer()).getHandle());
                    }

                    plugin.getFoxUUIDs().put(spawnedFox.getUniqueID(), ownerUUID);
                    spawnedFox.setChosenName(results.getString("NAME"));
                    spawnedFox.setTamed(true);
                }

                foxList.add(spawnedFox);
            }

            return foxList;
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
        return null;
    }

    public List<EntityTamableFox> spawnFoxesInChunk(Chunk chunk) {
        plugin = TamableFoxes.getPlugin(TamableFoxes.class);
        try {
            sqLiteHandler.connect();
            PreparedStatement statement = sqLiteHandler.getConnection().prepareStatement("SELECT * FROM foxes");
            ResultSet results = statement.executeQuery();

            List<EntityTamableFox> foxList = new ArrayList<>();
            while (results.next()) { // Loop through each row
                List<String> locationList = Arrays.asList(results.getString("LOCATION").split("\\s*,\\s*"));
                Location loc = new Location(Bukkit.getWorld(locationList.get(0)), Double.parseDouble(locationList.get(1)), Double.parseDouble(locationList.get(2)), Double.parseDouble(locationList.get(3)));

                // Checks if the location is in a chunk.
                if (chunk.getX() == ((double) loc.getBlockX() / 16 && chunk.getZ() == loc.getBlockZ() / 16) {
                    plugin.getServer().getConsoleSender().sendMessage("SPAWN IN CHUNK");

                    EntityTamableFox spawnedFox = (EntityTamableFox) plugin.spawnTamableFox(loc, EntityFox.Type.valueOf(results.getString("TYPE")));
                    spawnedFox.databaseID = results.getInt("ID");
                    spawnedFox.setSlot(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(new ItemStack(Material.valueOf(results.getString("MOUTH_ITEM")), 1)));

                    spawnedFox.setSitting(results.getInt("SITTING") == 1);
                    spawnedFox.setSleeping(results.getInt("SLEEPING") == 1);

                    if (!results.getString("OWNER_UUID").equals("none")) {
                        UUID ownerUUID = UUID.fromString(results.getString("OWNER_UUID"));

                        OfflinePlayer owner = plugin.getServer().getOfflinePlayer(ownerUUID);
                        if (owner.isOnline()) {
                            spawnedFox.setOwner(((CraftPlayer) owner.getPlayer()).getHandle());
                        }

                        plugin.getFoxUUIDs().put(spawnedFox.getUniqueID(), ownerUUID);
                        spawnedFox.setChosenName(results.getString("NAME"));
                        spawnedFox.setTamed(true);
                    }

                    foxList.add(spawnedFox);
                } else {
                    plugin.getServer().getConsoleSender().sendMessage("ChunkX: " + (chunk.getX()) + ", LocX: " + (loc.getBlockX() / 16) + ", ChunkZ: " + (chunk.getZ()) + ", LocZ: " + (loc.getBlockZ() / 16));
                }
            }

            return foxList;
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
        return null;
    }

    public void removeFox(int databaseID) {
        plugin = TamableFoxes.getPlugin(TamableFoxes.class);
        try {
            sqLiteHandler.connect();

            PreparedStatement statement = sqLiteHandler.getConnection().prepareStatement("DELETE FROM foxes WHERE ID=" + databaseID);
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
        removeFox(fox.databaseID);
    }
}
