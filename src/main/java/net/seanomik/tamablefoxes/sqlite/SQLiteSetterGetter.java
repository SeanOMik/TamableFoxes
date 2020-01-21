package net.seanomik.tamablefoxes.sqllite;

import net.minecraft.server.v1_15_R1.EnumItemSlot;
import net.seanomik.tamablefoxes.EntityTamableFox;
import net.seanomik.tamablefoxes.TamableFoxes;
import org.bukkit.plugin.Plugin;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SQLiteSetterGetter {
    public static Plugin plugin;
    public static SQLiteHandler sqLiteHandler;

    public void createTablesIfNotExist() {
        plugin = TamableFoxes.getPlugin(TamableFoxes.class);
        sqLiteHandler = TamableFoxes.sqLiteHandler;
        //String pluginDatabase = Reference.SQLiteDatabase;

        String foxesTable =
                "CREATE TABLE IF NOT EXISTS `foxes` ( " +
                        "`OWNER_UUID` TEXT PRIMARY KEY ,  " +
                        "`NAME` TEXT NOT NULL ,  " +
                        "`LOCATION` TEXT NOT NULL ,  " +
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
            PreparedStatement statement = sqLiteHandler.getConnection()
                    .prepareStatement("INSERT INTO foxes (OWNER_UUID,NAME,LOCATION,MOUTH_ITEM) VALUES (?,?,?,?)");

            statement.setString(1, (fox.getOwner().getUniqueID() == null) ? "none" : fox.getOwner().getUniqueID().toString());
            statement.setString(2, fox.getChosenName());

            statement.setString(3, fox.locX() + "," + fox.locY() + "," + fox.locY());
            statement.setString(4, fox.getEquipment(EnumItemSlot.MAINHAND).toString());
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

    public List<EntityTamableFox> spawnFoxes() {
        plugin = Dexun.getPlugin(Dexun.class);
        try {
            sqLiteHandler.connect();
            PreparedStatement statement = sqLiteHandler.getConnection()
                    .prepareStatement("SELECT * FROM foxes");
            ResultSet results = statement.executeQuery();
            results.next();

            String banReasonsSTR = results.getString("REASONS");
            String banDatesSTR = results.getString("ON_DATES");
            String banEndDatesSTR = results.getString("END_DATES");
            String unbanReasonsSTR = results.getString("UNBAN_REASONS");

            List<String> banReasons = new LinkedList<String>(Arrays.asList(banReasonsSTR.substring(1).split(",")));
            List<String> banDates = new LinkedList<String>(Arrays.asList(banDatesSTR.substring(1).split(",")));
            List<String> banEndDates = new LinkedList<String>(Arrays.asList(banEndDatesSTR.substring(1).split(",")));
            List<String> unbanReasons = new LinkedList<String>(Arrays.asList(unbanReasonsSTR.substring(1).split(",")));

            List<List<String>> bans = new ArrayList<List<String>>();
            bans.add(banReasons);
            bans.add(banDates);
            bans.add(banEndDates);
            bans.add(unbanReasons);

            return bans;
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
}
