package com.canteen.repository;

import com.canteen.db.DatabaseManager;
import com.canteen.model.Order;
import com.canteen.model.OrderItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderRepository {
    private final DatabaseManager databaseManager;

    public OrderRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public int createOrder(int customerId, double totalAmount, LocalDateTime orderTime, Connection connection)
            throws SQLException {
        String sql = "INSERT INTO orders(customer_id, total_amount, order_time) VALUES(?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, customerId);
            statement.setDouble(2, totalAmount);
            statement.setString(3, orderTime.toString());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Unable to create order.");
    }

    public void createOrderItem(int orderId, int menuItemId, int quantity, double unitPrice, double lineTotal,
                                Connection connection) throws SQLException {
        String sql = "INSERT INTO order_items(order_id, menu_item_id, quantity, unit_price, line_total) VALUES(?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setInt(2, menuItemId);
            statement.setInt(3, quantity);
            statement.setDouble(4, unitPrice);
            statement.setDouble(5, lineTotal);
            statement.executeUpdate();
        }
    }

    public List<Order> findAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = """
                SELECT o.id, c.name AS customer_name, o.total_amount, o.order_time
                FROM orders o
                JOIN customers c ON c.id = o.customer_id
                ORDER BY o.id DESC
                """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                int orderId = resultSet.getInt("id");
                orders.add(new Order(
                        orderId,
                        resultSet.getString("customer_name"),
                        resultSet.getDouble("total_amount"),
                        resultSet.getString("order_time"),
                        getOrderItemLines(orderId, connection)
                ));
            }
        }
        return orders;
    }

    public int countOrders() throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    public double sumRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM orders";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getDouble(1) : 0;
        }
    }

    public List<OrderItem> getOrderItems(int orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = """
                SELECT m.name, oi.quantity, oi.unit_price, oi.line_total
                FROM order_items oi
                JOIN menu_items m ON m.id = oi.menu_item_id
                WHERE oi.order_id = ?
                """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(new OrderItem(
                            resultSet.getString("name"),
                            resultSet.getInt("quantity"),
                            resultSet.getDouble("unit_price"),
                            resultSet.getDouble("line_total")
                    ));
                }
            }
        }
        return items;
    }

    private List<String> getOrderItemLines(int orderId, Connection connection) throws SQLException {
        List<String> lines = new ArrayList<>();
        String sql = """
                SELECT m.name, oi.quantity, oi.unit_price, oi.line_total
                FROM order_items oi
                JOIN menu_items m ON m.id = oi.menu_item_id
                WHERE oi.order_id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    lines.add(String.format("%s x%d @ Rs. %.2f = Rs. %.2f",
                            resultSet.getString("name"),
                            resultSet.getInt("quantity"),
                            resultSet.getDouble("unit_price"),
                            resultSet.getDouble("line_total")));
                }
            }
        }
        return lines;
    }
}
