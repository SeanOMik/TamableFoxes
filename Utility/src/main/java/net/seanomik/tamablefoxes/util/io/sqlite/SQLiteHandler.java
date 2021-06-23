package net.seanomik.tamablefoxes.util.io.sqlite;

import org.bukkit.plugin.Plugin;

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

	public void connect(Plugin plugin) {
		String pluginFolder = plugin.getDataFolder().getAbsolutePath();
		connect(pluginFolder);
	}

	public void connect(String pluginFolder) {
		try {
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

	public void newConnection(String pluginFolder) {
		try {
			connection.close();
			connect(pluginFolder);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}
}
