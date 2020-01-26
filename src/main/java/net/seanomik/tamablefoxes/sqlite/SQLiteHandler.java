package net.seanomik.tamablefoxes.sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteHandler {
	private Connection connection;
	private File dataFolder;

	public SQLiteHandler(File dataFolder) {
		this.dataFolder = dataFolder;
	}

	public void connect() {
		try {
			String url = "jdbc:sqlite:/" + dataFolder.getAbsolutePath() + "/foxes.db";
			connection = DriverManager.getConnection(url);

			//Bukkit.getConsoleSender().sendMessage(TamableFoxes.getPrefix() + "Connection to SQLite has been established.");
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
