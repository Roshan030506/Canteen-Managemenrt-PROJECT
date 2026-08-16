package com.canteen.model;

public class MenuItem {
    private final int id;
    private final String name;
    private final String category;
    private final double price;
    private final int quantity;

    public MenuItem(int id, String name, String category, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public String toDisplayString() {
        return String.format("ID: %d | %s | %s | Rs. %.2f | Stock: %d", id, name, category, price, quantity);
    }
}
