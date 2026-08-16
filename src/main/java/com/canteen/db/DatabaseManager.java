package com.canteen.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DATABASE_FOLDER = "data";
    private static final String DATABASE_NAME = "canteen.db";
    private final String jdbcUrl;

    public DatabaseManager() {
        Path databasePath = Paths.get(DATABASE_FOLDER, DATABASE_NAME);
        this.jdbcUrl = "jdbc:sqlite:" + databasePath;
    }

    public void initialize() throws Exception {
        Files.createDirectories(Paths.get(DATABASE_FOLDER));
        createTables();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl);
    }

    private void createTables() throws SQLException {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS menu_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        category TEXT NOT NULL,
                        price REAL NOT NULL,
                        quantity INTEGER NOT NULL
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS customers (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        phone TEXT NOT NULL UNIQUE
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS orders (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        customer_id INTEGER NOT NULL,
                        total_amount REAL NOT NULL,
                        order_time TEXT NOT NULL,
                        FOREIGN KEY (customer_id) REFERENCES customers(id)
                    )
                    """);

            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS order_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        order_id INTEGER NOT NULL,
                        menu_item_id INTEGER NOT NULL,
                        quantity INTEGER NOT NULL,
                        unit_price REAL NOT NULL,
                        line_total REAL NOT NULL,
                        FOREIGN KEY (order_id) REFERENCES orders(id),
                        FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
                    )
                    """);
        }
    }
}
