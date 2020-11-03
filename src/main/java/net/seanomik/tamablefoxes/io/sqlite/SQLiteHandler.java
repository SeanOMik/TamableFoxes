package net.seanomik.tamablefoxes.io.sqlite;

import net.seanomik.tamablefoxes.TamableFoxes;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteHandler {
	private Connection connection;

	private static SQLiteHandler instance;

	public static SQLiteHandler getInstance() {
		if (instance == null) {
			instance = new SQLiteHandler();
		}

		return instance;
	}

	public void connect() {
		try {
			String pluginFolder = TamableFoxes.getPlugin().getDataFolder().getAbsolutePath();
			String url = "jdbc:sqlite:" + pluginFolder + "/userFoxAmount.db";
			connection = DriverManager.getConnection(url);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Connection getConnection() {
		return connection;
	}

	public void closeConnection() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void newConnection() {
		try {
			connection.close();
			connect();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}
}
