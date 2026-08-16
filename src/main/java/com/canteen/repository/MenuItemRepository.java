package com.canteen.repository;

import com.canteen.db.DatabaseManager;
import com.canteen.model.MenuItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MenuItemRepository {
    private final DatabaseManager databaseManager;

    public MenuItemRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public MenuItem create(String name, String category, double price, int quantity) throws SQLException {
        String sql = "INSERT INTO menu_items(name, category, price, quantity) VALUES(?, ?, ?, ?)";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, category);
            statement.setDouble(3, price);
            statement.setInt(4, quantity);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new MenuItem(keys.getInt(1), name, category, price, quantity);
                }
            }
        }
        throw new SQLException("Unable to create menu item.");
    }

    public MenuItem findById(int id) throws SQLException {
        String sql = "SELECT id, name, category, price, quantity FROM menu_items WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }
        return null;
    }

    public boolean hasAnyMenuItems() throws SQLException {
        String sql = "SELECT COUNT(*) FROM menu_items";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() && resultSet.getInt(1) > 0;
        }
    }

    public List<MenuItem> findAll() throws SQLException {
        List<MenuItem> menuItems = new ArrayList<>();
        String sql = "SELECT id, name, category, price, quantity FROM menu_items ORDER BY id";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                menuItems.add(mapRow(resultSet));
            }
        }
        return menuItems;
    }

    public void updateStock(int id, int newQuantity, Connection connection) throws SQLException {
        String sql = "UPDATE menu_items SET quantity = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, newQuantity);
            statement.setInt(2, id);
            statement.executeUpdate();
        }
    }

    public MenuItem findTopSellingItem() throws SQLException {
        String sql = """
                SELECT m.id, m.name, m.category, m.price, m.quantity
                FROM order_items oi
                JOIN menu_items m ON m.id = oi.menu_item_id
                GROUP BY m.id, m.name, m.category, m.price, m.quantity
                ORDER BY SUM(oi.quantity) DESC
                LIMIT 1
                """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return mapRow(resultSet);
            }
        }
        return null;
    }

    private MenuItem mapRow(ResultSet resultSet) throws SQLException {
        return new MenuItem(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("category"),
                resultSet.getDouble("price"),
                resultSet.getInt("quantity")
        );
    }
}
