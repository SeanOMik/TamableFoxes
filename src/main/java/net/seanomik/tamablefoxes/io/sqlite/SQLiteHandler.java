package net.seanomik.tamablefoxes.io.sqlite;

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
			String baseLoc = Bukkit.getWorldContainer().toURI().toString().substring(6);
			baseLoc = baseLoc.substring(0,baseLoc.length()-2);

			String url = "jdbc:sqlite:" + baseLoc + "plugins/Tamablefoxes/userFoxAmount.db";
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
