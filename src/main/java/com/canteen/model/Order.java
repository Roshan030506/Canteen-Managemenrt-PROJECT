package com.canteen.model;

import java.util.List;

public class Order {
    private final int id;
    private final String customerName;
    private final double totalAmount;
    private final String orderTime;
    private final List<String> itemLines;

    public Order(int id, String customerName, double totalAmount, String orderTime, List<String> itemLines) {
        this.id = id;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.orderTime = orderTime;
        this.itemLines = itemLines;
    }

    public int getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public List<String> getItemLines() {
        return itemLines;
    }

    public String toDisplayString() {
        return String.format("Order ID: %d | Customer: %s | Total: Rs. %.2f | Time: %s",
                id, customerName, totalAmount, orderTime);
    }
}
