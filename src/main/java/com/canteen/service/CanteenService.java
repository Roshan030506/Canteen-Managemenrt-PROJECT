package com.canteen.service;

import com.canteen.db.DatabaseManager;
import com.canteen.model.Customer;
import com.canteen.model.MenuItem;
import com.canteen.model.Order;
import com.canteen.model.OrderItem;
import com.canteen.model.OrderRequest;
import com.canteen.repository.CustomerRepository;
import com.canteen.repository.MenuItemRepository;
import com.canteen.repository.OrderRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CanteenService {
    private final DatabaseManager databaseManager;
    private final MenuItemRepository menuItemRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public CanteenService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.menuItemRepository = new MenuItemRepository(databaseManager);
        this.customerRepository = new CustomerRepository(databaseManager);
        this.orderRepository = new OrderRepository(databaseManager);
    }

    public void seedSampleData() throws SQLException {
        if (!menuItemRepository.hasAnyMenuItems()) {
            menuItemRepository.create("Veg Sandwich", "Snacks", 50.00, 30);
            menuItemRepository.create("Masala Dosa", "Breakfast", 80.00, 20);
            menuItemRepository.create("Tea", "Beverage", 15.00, 100);
            menuItemRepository.create("Coffee", "Beverage", 25.00, 80);
        }

        if (!customerRepository.hasAnyCustomers()) {
            customerRepository.create("Amit Sharma", "9876543210");
            customerRepository.create("Priya Singh", "9988776655");
        }
    }

    public List<MenuItem> getAllMenuItems() throws SQLException {
        return menuItemRepository.findAll();
    }

    public MenuItem addMenuItem(String name, String category, double price, int quantity) throws SQLException {
        validateMenuItem(name, category, price, quantity);
        return menuItemRepository.create(name.trim(), category.trim(), price, quantity);
    }

    public Customer registerCustomer(String name, String phone) throws SQLException {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Customer name is required.");
        }
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone number is required.");
        }
        return customerRepository.create(name.trim(), phone.trim());
    }

    public List<Customer> getAllCustomers() throws SQLException {
        return customerRepository.findAll();
    }

    public Order placeOrder(int customerId, List<OrderRequest> orderRequests) throws SQLException {
        if (orderRequests == null || orderRequests.isEmpty()) {
            throw new IllegalArgumentException("At least one order item is required.");
        }

        Customer customer = customerRepository.findById(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("Customer not found.");
        }

        try (Connection connection = databaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                List<OrderItem> orderItems = new ArrayList<>();
                List<MenuItem> orderedMenuItems = new ArrayList<>();
                double totalAmount = 0;

                for (OrderRequest orderRequest : orderRequests) {
                    if (orderRequest.getQuantity() <= 0) {
                        throw new IllegalArgumentException("Quantity must be greater than zero.");
                    }

                    MenuItem menuItem = menuItemRepository.findById(orderRequest.getMenuItemId());
                    if (menuItem == null) {
                        throw new IllegalArgumentException("Menu item not found for ID: " + orderRequest.getMenuItemId());
                    }
                    if (menuItem.getQuantity() < orderRequest.getQuantity()) {
                        throw new IllegalArgumentException("Insufficient stock for item: " + menuItem.getName());
                    }

                    double lineTotal = menuItem.getPrice() * orderRequest.getQuantity();
                    totalAmount += lineTotal;
                    orderItems.add(new OrderItem(menuItem.getName(), orderRequest.getQuantity(), menuItem.getPrice(), lineTotal));
                    orderedMenuItems.add(menuItem);

                    int newQuantity = menuItem.getQuantity() - orderRequest.getQuantity();
                    menuItemRepository.updateStock(menuItem.getId(), newQuantity, connection);
                }

                LocalDateTime orderTime = LocalDateTime.now();
                int orderId = orderRepository.createOrder(customerId, totalAmount, orderTime, connection);

                for (int index = 0; index < orderRequests.size(); index++) {
                    OrderRequest request = orderRequests.get(index);
                    MenuItem menuItem = orderedMenuItems.get(index);
                    double unitPrice = menuItem.getPrice();
                    double lineTotal = unitPrice * request.getQuantity();
                    orderRepository.createOrderItem(orderId, menuItem.getId(), request.getQuantity(), unitPrice, lineTotal, connection);
                }

                connection.commit();
                List<String> itemLines = orderItems.stream().map(OrderItem::toDisplayString).toList();
                return new Order(orderId, customer.getName(), totalAmount, orderTime.toString(), itemLines);
            } catch (Exception exception) {
                connection.rollback();
                if (exception instanceof SQLException sqlException) {
                    throw sqlException;
                }
                throw new IllegalArgumentException(exception.getMessage(), exception);
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<Order> getAllOrders() throws SQLException {
        return orderRepository.findAllOrders();
    }

    public int getTotalOrders() throws SQLException {
        return orderRepository.countOrders();
    }

    public double getTotalRevenue() throws SQLException {
        return orderRepository.sumRevenue();
    }

    public MenuItem getTopSellingItem() throws SQLException {
        return menuItemRepository.findTopSellingItem();
    }

    private void validateMenuItem(String name, String category, double price, int quantity) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Item name is required.");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Category is required.");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero.");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Stock cannot be negative.");
        }
    }
}
