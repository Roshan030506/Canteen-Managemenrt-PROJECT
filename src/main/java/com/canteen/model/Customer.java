package com.canteen.model;

public class Customer {
    private final int id;
    private final String name;
    private final String phone;

    public Customer(int id, String name, String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String toDisplayString() {
        return String.format("Customer ID: %d | %s | %s", id, name, phone);
    }
}
