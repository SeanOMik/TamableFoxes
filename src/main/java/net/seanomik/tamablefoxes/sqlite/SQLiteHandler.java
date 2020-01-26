package net.seanomik.tamablefoxes.sqlite;

import net.seanomik.tamablefoxes.TamableFoxes;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteHandler {
	private Connection connection;

	public void connect() {
		try {
			String baseLoc = Bukkit.getWorldContainer().toURI().toString().substring(6);
			baseLoc = baseLoc.substring(0,baseLoc.length()-2);

			String url = "jdbc:sqlite:" + baseLoc + "plugins/Tamablefoxes/foxes.db";
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
