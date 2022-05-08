package net.seanomik.tamablefoxes.util.io.sqlite;

import net.seanomik.tamablefoxes.util.io.Config;
import org.bukkit.plugin.Plugin;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SQLiteHelper {
    public static Plugin plugin;
    public static SQLiteHandler sqLiteHandler;

    private static SQLiteHelper instance;
    private static String userAmountTableName = "USER_FOX_AMT";

    public static SQLiteHelper getInstance(Plugin plugin) {
        if (instance == null) {
            instance = new SQLiteHelper();
            SQLiteHelper.plugin = plugin;
        }

        return instance;
    }

    public void createTablesIfNotExist() {
        sqLiteHandler = SQLiteHandler.getInstance();

        String userFoxAmountQuery =
                "CREATE TABLE IF NOT EXISTS `" + userAmountTableName + "` ( " +
                    "`UUID` TEXT PRIMARY KEY ,  " +
                    "`AMOUNT` INT NOT NULL);";

        try {
            sqLiteHandler.connect(plugin);
            // Create previous bans table
            DatabaseMetaData dbm = sqLiteHandler.getConnection().getMetaData();
            ResultSet tables = dbm.getTables(null, null, userAmountTableName, null);
            if (!tables.next()) {
                PreparedStatement statement = sqLiteHandler.getConnection().prepareStatement(userFoxAmountQuery);
                statement.executeUpdate();

                plugin.getServer().getConsoleSender().sendMessage(Config.getPrefix() + "Created previous player bans table!");
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

    public int getPlayerFoxAmount(UUID uuid) {
        sqLiteHandler = SQLiteHandler.getInstance();

        try {
            sqLiteHandler.connect(plugin);
            PreparedStatement statement = sqLiteHandler.getConnection()
                    .prepareStatement("SELECT * FROM " + userAmountTableName + " WHERE UUID=?");
            statement.setString(1, uuid.toString());
            ResultSet results = statement.executeQuery();

            if (results.next()) {
                return results.getInt("AMOUNT");
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

        return -1;
    }

    public void addPlayerFoxAmount(UUID uuid, int amt) {
        sqLiteHandler = SQLiteHandler.getInstance();

        try {
            String query = "UPDATE " + userAmountTableName + " SET AMOUNT = AMOUNT + " + amt + " WHERE UUID = '" + uuid.toString() + "'";
            if (getPlayerFoxAmount(uuid) == -1) {
                query = "INSERT INTO " + userAmountTableName + " (UUID, AMOUNT) VALUES('" + uuid.toString() + "'," + amt + ")";
            }

            sqLiteHandler.connect(plugin);
            PreparedStatement statement = sqLiteHandler.getConnection().prepareStatement(query);

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

    public void removePlayerFoxAmount(UUID uuid, int amt) {
        sqLiteHandler = SQLiteHandler.getInstance();

        try {
            String query = "UPDATE " + userAmountTableName + " SET AMOUNT = AMOUNT - " + amt + " WHERE UUID = '" + uuid.toString() + "'";

            sqLiteHandler.connect(plugin);
            PreparedStatement statement = sqLiteHandler.getConnection().prepareStatement(query);

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
}
