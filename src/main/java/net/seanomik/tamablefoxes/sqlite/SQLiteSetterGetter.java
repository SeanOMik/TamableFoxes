package net.seanomik.tamablefoxes.sqlite;

import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.EntityFox;
import net.minecraft.server.v1_15_R1.EnumItemSlot;
import net.seanomik.tamablefoxes.EntityTamableFox;
import net.seanomik.tamablefoxes.TamableFoxes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

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
                        "`INDEX` INTEGER PRIMARY KEY AUTOINCREMENT ,  " +
                        "`OWNER_UUID` TEXT NOT NULL ,  " +
                        "`NAME` TEXT ,  " +
                        "`LOCATION` TEXT NOT NULL ,  " +
                        "`TYPE` TEXT NOT NULL ,  " +
                        "`MOUTH_ITEM` TEXT NOT NULL);";

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

            PreparedStatement statement = sqLiteHandler.getConnection().prepareStatement("INSERT INTO foxes (OWNER_UUID,NAME,LOCATION,TYPE,MOUTH_ITEM) VALUES (?,?,?,?,?)");
            if (fox.databaseIndex != -1) {
                statement = sqLiteHandler.getConnection().prepareStatement("UPDATE foxes SET OWNER_UUID=?, NAME=?, LOCATION=?, TYPE=?, MOUTH_ITEM=? WHERE INDEX=" + fox.databaseIndex);
            }

            statement.setString(1, (fox.getOwner() == null) ? "none" : fox.getOwner().getUniqueID().toString());
            statement.setString(2, fox.getChosenName());

            statement.setString(3,  fox.getWorld().worldData.getName() + "," + fox.locX() + "," + fox.locY() + "," + fox.locY());
            statement.setString(4, fox.getFoxType().toString());
            statement.setString(5, fox.getEquipment(EnumItemSlot.MAINHAND).toString().toUpperCase().substring(fox.getEquipment(EnumItemSlot.MAINHAND).toString().indexOf(' ')+1));
            statement.executeUpdate();

            ResultSet result = statement.getGeneratedKeys();
            if (result.next()) {
                fox.databaseIndex = result.getInt("INDEX");
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

    public void saveFoxes(List<EntityTamableFox> foxes) { // @TODO: Optimize
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
                spawnedFox.databaseIndex = results.getInt("INDEX");
                spawnedFox.setSlot(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(new ItemStack(Material.valueOf(results.getString("MOUTH_ITEM")), 1)));

                if (!results.getString("OWNER_UUID").equals("none")) {
                    UUID ownerUUID = UUID.fromString(results.getString("OWNER_UUID"));

                    OfflinePlayer owner = plugin.getServer().getOfflinePlayer(ownerUUID);
                    if (owner.isOnline()) {
                        spawnedFox.setOwner(((CraftPlayer) owner.getPlayer()).getHandle());
                    }

                    plugin.getFoxUUIDs().put(spawnedFox.getUniqueID(), ownerUUID);
                    spawnedFox.setChosenName(results.getString("NAME"));
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

    public void removeFox(int databaseID) {
        plugin = TamableFoxes.getPlugin(TamableFoxes.class);
        try {
            sqLiteHandler.connect();

            PreparedStatement statement = sqLiteHandler.getConnection().prepareStatement("DELETE FROM foxes WHERE INDEX=" + databaseID);
            statement.setInt(1, databaseID);
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
        removeFox(fox.databaseIndex);
    }
}
