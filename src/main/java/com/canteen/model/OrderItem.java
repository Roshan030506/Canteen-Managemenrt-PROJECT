package com.canteen.model;

public class OrderItem {
    private final String itemName;
    private final int quantity;
    private final double unitPrice;
    private final double lineTotal;

    public OrderItem(String itemName, int quantity, double unitPrice, double lineTotal) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
    }

    public String getItemName() {
        return itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getLineTotal() {
        return lineTotal;
    }

    public String toDisplayString() {
        return String.format("%s x%d @ Rs. %.2f = Rs. %.2f", itemName, quantity, unitPrice, lineTotal);
    }
}
