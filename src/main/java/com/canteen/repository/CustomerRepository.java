package com.canteen.repository;

import com.canteen.db.DatabaseManager;
import com.canteen.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CustomerRepository {
    private final DatabaseManager databaseManager;

    public CustomerRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Customer create(String name, String phone) throws SQLException {
        String sql = "INSERT INTO customers(name, phone) VALUES(?, ?)";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, phone);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Customer(keys.getInt(1), name, phone);
                }
            }
        }
        throw new SQLException("Unable to create customer.");
    }

    public Customer findById(int id) throws SQLException {
        String sql = "SELECT id, name, phone FROM customers WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Customer(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getString("phone")
                    );
                }
            }
        }
        return null;
    }

    public boolean hasAnyCustomers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() && resultSet.getInt(1) > 0;
        }
    }

    public List<Customer> findAll() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT id, name, phone FROM customers ORDER BY id";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                customers.add(new Customer(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getString("phone")
                ));
            }
        }
        return customers;
    }
}
