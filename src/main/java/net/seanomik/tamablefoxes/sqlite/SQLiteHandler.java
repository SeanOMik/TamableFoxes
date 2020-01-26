package net.seanomik.tamablefoxes.sqlite;

import net.seanomik.tamablefoxes.TamableFoxes;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class SQLiteHandler {
	private Connection connection;

	public void connect() {
		try {
			String url = "jdbc:sqlite:" + TamableFoxes.getPlugin().getDataFolder() + "/foxes.db";
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
