package com.canteen.model;

public class OrderRequest {
    private final int menuItemId;
    private final int quantity;

    public OrderRequest(int menuItemId, int quantity) {
        this.menuItemId = menuItemId;
        this.quantity = quantity;
    }

    public int getMenuItemId() {
        return menuItemId;
    }

    public int getQuantity() {
        return quantity;
    }
}
