package net.seanomik.tamablefoxes.io.sqlite;

import net.seanomik.tamablefoxes.TamableFoxes;
import net.seanomik.tamablefoxes.Utils;
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

    public static SQLiteHelper getInstance() {
        if (instance == null) {
            instance = new SQLiteHelper();
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
            sqLiteHandler.connect();
            // Create previous bans table
            DatabaseMetaData dbm = sqLiteHandler.getConnection().getMetaData();
            ResultSet tables = dbm.getTables(null, null, userAmountTableName, null);
            if (!tables.next()) {
                PreparedStatement statement = sqLiteHandler.getConnection().prepareStatement(userFoxAmountQuery);
                statement.executeUpdate();

                plugin.getServer().getConsoleSender().sendMessage(Utils.getPrefix() + "Created previous player bans table!");
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
        try {
            sqLiteHandler.connect();
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
        try {
            String query = "UPDATE " + userAmountTableName + " SET AMOUNT = AMOUNT + " + amt + " WHERE UUID = '" + uuid.toString() + "'";
            if (getPlayerFoxAmount(uuid) == -1) {
                query = "INSERT INTO " + userAmountTableName + " (UUID, AMOUNT) VALUES('" + uuid.toString() + "'," + amt + ")";
            }

            sqLiteHandler.connect();
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
        try {
            String query = "UPDATE " + userAmountTableName + " SET AMOUNT = AMOUNT - " + amt + " WHERE UUID = '" + uuid.toString() + "'";

            sqLiteHandler.connect();
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
