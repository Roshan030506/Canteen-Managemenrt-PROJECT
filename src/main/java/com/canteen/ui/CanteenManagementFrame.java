package com.canteen.ui;

import com.canteen.model.Customer;
import com.canteen.model.MenuItem;
import com.canteen.model.Order;
import com.canteen.model.OrderRequest;
import com.canteen.service.CanteenService;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CanteenManagementFrame extends JFrame {
    private final CanteenService canteenService;

    // Premium Color Palette
    private static final Color COLOR_BG_MAIN = new Color(21, 21, 33);
    private static final Color COLOR_BG_SIDEBAR = new Color(30, 30, 46);
    private static final Color COLOR_BG_CARD = new Color(37, 37, 56);
    private static final Color COLOR_ACCENT = new Color(124, 77, 255); // Electric Purple
    private static final Color COLOR_ACCENT_HOVER = new Color(145, 105, 255);
    private static final Color COLOR_TEXT_PRIMARY = Color.WHITE;
    private static final Color COLOR_TEXT_SECONDARY = new Color(165, 165, 199);
    private static final Color COLOR_BORDER = new Color(49, 49, 77);
    private static final Color COLOR_SUCCESS = new Color(0, 229, 255); // Cyan
    private static final Color COLOR_ERROR = new Color(255, 82, 82);

    private CardLayout cardLayout;
    private JPanel workspaceContainer;

    // Navigation Buttons
    private SidebarButton btnDashboard;
    private SidebarButton btnPlaceOrder;
    private SidebarButton btnManageMenu;
    private SidebarButton btnManageCustomers;
    private SidebarButton btnOrderHistory;

    // Dashboard Metric Labels
    private JLabel lblTotalOrdersVal;
    private JLabel lblTotalRevenueVal;
    private JLabel lblTopItemVal;
    private DefaultTableModel modelRecentOrders;

    // Place Order Components
    private JComboBox<Customer> cmbCustomers;
    private DefaultTableModel modelMenuSelection;
    private DefaultTableModel modelCart;
    private JLabel lblCartTotal;
    private List<CartItem> cartList = new ArrayList<>();
    private JTextField txtSearchMenu;

    // Manage Menu Components
    private DefaultTableModel modelManageMenu;
    private JTextField txtItemName;
    private JTextField txtItemCategory;
    private JTextField txtItemPrice;
    private JTextField txtItemStock;

    // Manage Customers Components
    private DefaultTableModel modelManageCustomers;
    private JTextField txtCustName;
    private JTextField txtCustPhone;

    // Order History Components
    private DefaultTableModel modelOrderHistory;
    private JList<String> listOrderDetailLines;
    private JLabel lblDetailOrderID;
    private JLabel lblDetailCust;
    private JLabel lblDetailTime;
    private JLabel lblDetailTotal;

    public CanteenManagementFrame(CanteenService canteenService) {
        this.canteenService = canteenService;
        setTitle("Canteen Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(850, 600));

        initUI();
        refreshAllData();
    }

    private void initUI() {
        // Main Container Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(COLOR_BG_MAIN);
        setContentPane(mainPanel);

        // Sidebar Navigation
        JPanel sidebarPanel = createSidebar();
        mainPanel.add(sidebarPanel, BorderLayout.WEST);

        // Workspace Container (uses CardLayout)
        cardLayout = new CardLayout();
        workspaceContainer = new JPanel(cardLayout);
        workspaceContainer.setBackground(COLOR_BG_MAIN);
        workspaceContainer.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.add(workspaceContainer, BorderLayout.CENTER);

        // Create Workspace Views
        workspaceContainer.add(createDashboardPanel(), "DASHBOARD");
        workspaceContainer.add(createPlaceOrderPanel(), "PLACE_ORDER");
        workspaceContainer.add(createManageMenuPanel(), "MANAGE_MENU");
        workspaceContainer.add(createManageCustomersPanel(), "MANAGE_CUSTOMERS");
        workspaceContainer.add(createOrderHistoryPanel(), "ORDER_HISTORY");

        // Show Dashboard first
        showCard("DASHBOARD", btnDashboard);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(COLOR_BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, getHeight()));
        sidebar.setBorder(new EmptyBorder(20, 10, 20, 10));

        // Header Title
        JLabel lblHeader = new JLabel("CANTEEN CMS");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setForeground(COLOR_TEXT_PRIMARY);
        lblHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(lblHeader);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(180, 2));
        sep.setForeground(COLOR_BORDER);
        sep.setBackground(COLOR_BORDER);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebar.add(sep);
        sidebar.add(Box.createRigidArea(new Dimension(0, 30)));

        // Navigation Buttons
        btnDashboard = new SidebarButton("Dashboard", "dashboard.png");
        btnPlaceOrder = new SidebarButton("Place Order", "cart.png");
        btnManageMenu = new SidebarButton("Manage Menu", "menu.png");
        btnManageCustomers = new SidebarButton("Customers", "users.png");
        btnOrderHistory = new SidebarButton("Order History", "history.png");

        // Button Actions
        btnDashboard.addActionListener(e -> showCard("DASHBOARD", btnDashboard));
        btnPlaceOrder.addActionListener(e -> showCard("PLACE_ORDER", btnPlaceOrder));
        btnManageMenu.addActionListener(e -> showCard("MANAGE_MENU", btnManageMenu));
        btnManageCustomers.addActionListener(e -> showCard("MANAGE_CUSTOMERS", btnManageCustomers));
        btnOrderHistory.addActionListener(e -> showCard("ORDER_HISTORY", btnOrderHistory));

        sidebar.add(btnDashboard);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnPlaceOrder);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnManageMenu);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnManageCustomers);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnOrderHistory);

        // Sidebar Footer
        sidebar.add(Box.createVerticalGlue());
        JLabel lblFooter = new JLabel("v1.0.0");
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFooter.setForeground(COLOR_TEXT_SECONDARY);
        lblFooter.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(lblFooter);

        return sidebar;
    }

    private void showCard(String name, SidebarButton activeBtn) {
        cardLayout.show(workspaceContainer, name);

        // Update active selection state of sidebar buttons
        btnDashboard.setActive(btnDashboard == activeBtn);
        btnPlaceOrder.setActive(btnPlaceOrder == activeBtn);
        btnManageMenu.setActive(btnManageMenu == activeBtn);
        btnManageCustomers.setActive(btnManageCustomers == activeBtn);
        btnOrderHistory.setActive(btnOrderHistory == activeBtn);
    }

    // ==========================================
    // VIEW 1: DASHBOARD
    // ==========================================
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);

        // Title Header
        JLabel lblTitle = new JLabel("Dashboard Overview");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(COLOR_TEXT_PRIMARY);
        panel.add(lblTitle, BorderLayout.NORTH);

        // Stats Row Panel
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 20, 0));
        statsRow.setOpaque(false);

        // Card 1: Total Orders
        JPanel cardOrders = createMetricCard("TOTAL ORDERS", "0", COLOR_ACCENT);
        lblTotalOrdersVal = (JLabel) cardOrders.getClientProperty("valueLabel");

        // Card 2: Total Revenue
        JPanel cardRevenue = createMetricCard("TOTAL REVENUE", "Rs. 0.00", COLOR_SUCCESS);
        lblTotalRevenueVal = (JLabel) cardRevenue.getClientProperty("valueLabel");

        // Card 3: Top Item
        JPanel cardTopItem = createMetricCard("TOP SELLING ITEM", "N/A", new Color(255, 193, 7));
        lblTopItemVal = (JLabel) cardTopItem.getClientProperty("valueLabel");

        statsRow.add(cardOrders);
        statsRow.add(cardRevenue);
        statsRow.add(cardTopItem);

        panel.add(statsRow, BorderLayout.CENTER);

        // Recent Orders Table Panel
        CardPanel recentOrdersPanel = new CardPanel();
        recentOrdersPanel.setLayout(new BorderLayout(0, 10));
        recentOrdersPanel.setPreferredSize(new Dimension(recentOrdersPanel.getPreferredSize().width, 300));

        JLabel lblRecent = new JLabel("Recent Orders");
        lblRecent.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblRecent.setForeground(COLOR_TEXT_PRIMARY);
        recentOrdersPanel.add(lblRecent, BorderLayout.NORTH);

        modelRecentOrders = new DefaultTableModel(new Object[]{"Order ID", "Customer Name", "Total Amount", "Order Time"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tblRecent = new JTable(modelRecentOrders);
        styleTable(tblRecent);
        JScrollPane scrollRecent = new JScrollPane(tblRecent);
        scrollRecent.getViewport().setBackground(COLOR_BG_CARD);
        scrollRecent.setBorder(BorderFactory.createEmptyBorder());
        recentOrdersPanel.add(scrollRecent, BorderLayout.CENTER);

        panel.add(recentOrdersPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMetricCard(String title, String value, Color accentColor) {
        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout(0, 10));
        card.setBackground(COLOR_BG_CARD);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(COLOR_TEXT_SECONDARY);

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblVal.setForeground(accentColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.CENTER);

        // Put references on the panel so we can update them later
        card.putClientProperty("valueLabel", lblVal);

        return card;
    }

    // ==========================================
    // VIEW 2: PLACE ORDER
    // ==========================================
    private JPanel createPlaceOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);

        // Header
        JLabel lblTitle = new JLabel("Place New Order");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(COLOR_TEXT_PRIMARY);
        panel.add(lblTitle, BorderLayout.NORTH);

        // Main body split into Menu Selection (Left) and Shopping Cart (Right)
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        panel.add(body, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left Panel (Menu Selection)
        CardPanel leftCard = new CardPanel();
        leftCard.setLayout(new BorderLayout(0, 15));

        // Search menu panel
        JPanel searchBarPanel = new JPanel(new BorderLayout(10, 0));
        searchBarPanel.setOpaque(false);
        JLabel lblSearch = new JLabel("Search Menu:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSearch.setForeground(COLOR_TEXT_SECONDARY);
        searchBarPanel.add(lblSearch, BorderLayout.WEST);

        txtSearchMenu = new ModernTextField(15, "Search by name or category...");
        txtSearchMenu.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterMenuTable(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterMenuTable(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterMenuTable(); }
        });
        searchBarPanel.add(txtSearchMenu, BorderLayout.CENTER);
        leftCard.add(searchBarPanel, BorderLayout.NORTH);

        // Menu selection Table
        modelMenuSelection = new DefaultTableModel(new Object[]{"ID", "Item Name", "Category", "Price (Rs.)", "Stock"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tblMenuSelection = new JTable(modelMenuSelection);
        styleTable(tblMenuSelection);
        JScrollPane scrollMenuSelect = new JScrollPane(tblMenuSelection);
        scrollMenuSelect.getViewport().setBackground(COLOR_BG_CARD);
        scrollMenuSelect.setBorder(BorderFactory.createEmptyBorder());
        leftCard.add(scrollMenuSelect, BorderLayout.CENTER);

        // Add to Cart Button
        ModernButton btnAddToCart = new ModernButton("Add Selected to Cart", COLOR_ACCENT, COLOR_ACCENT_HOVER);
        btnAddToCart.setPreferredSize(new Dimension(btnAddToCart.getPreferredSize().width, 40));
        btnAddToCart.addActionListener(e -> {
            int selectedRow = tblMenuSelection.getSelectedRow();
            if (selectedRow == -1) {
                showWarning("Please select an item from the menu table.");
                return;
            }

            int id = (int) tblMenuSelection.getValueAt(selectedRow, 0);
            String name = (String) tblMenuSelection.getValueAt(selectedRow, 1);
            double price = (double) tblMenuSelection.getValueAt(selectedRow, 3);
            int stock = (int) tblMenuSelection.getValueAt(selectedRow, 4);

            if (stock <= 0) {
                showError("This item is out of stock.");
                return;
            }

            // Prompt for quantity
            String input = JOptionPane.showInputDialog(this, "Enter quantity for '" + name + "' (Available: " + stock + "):", "Add to Cart", JOptionPane.QUESTION_MESSAGE);
            if (input == null || input.trim().isEmpty()) {
                return; // cancelled
            }

            try {
                int qty = Integer.parseInt(input.trim());
                if (qty <= 0) {
                    showWarning("Quantity must be greater than zero.");
                    return;
                }
                if (qty > stock) {
                    showWarning("Insufficient stock. Only " + stock + " items available.");
                    return;
                }

                addToCart(id, name, price, qty);
            } catch (NumberFormatException ex) {
                showWarning("Please enter a valid integer for quantity.");
            }
        });
        leftCard.add(btnAddToCart, BorderLayout.SOUTH);

        gbc.gridx = 0;
        gbc.weightx = 0.55;
        body.add(leftCard, gbc);

        // Gap spacing between panels
        gbc.gridx = 1;
        gbc.weightx = 0.02;
        body.add(Box.createHorizontalStrut(15), gbc);

        // Right Panel (Shopping Cart)
        CardPanel rightCard = new CardPanel();
        rightCard.setLayout(new BorderLayout(0, 15));

        // Customer Selection panel
        JPanel customerSelectPanel = new JPanel(new BorderLayout(10, 0));
        customerSelectPanel.setOpaque(false);
        JLabel lblCust = new JLabel("Customer:");
        lblCust.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCust.setForeground(COLOR_TEXT_SECONDARY);
        customerSelectPanel.add(lblCust, BorderLayout.WEST);

        cmbCustomers = new JComboBox<>();
        cmbCustomers.setBackground(COLOR_BG_CARD);
        cmbCustomers.setForeground(COLOR_TEXT_PRIMARY);
        cmbCustomers.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbCustomers.setRenderer(new CustomerListCellRenderer());
        customerSelectPanel.add(cmbCustomers, BorderLayout.CENTER);

        // Quick register button
        ModernButton btnQuickCust = new ModernButton("+", COLOR_ACCENT, COLOR_ACCENT_HOVER);
        btnQuickCust.setPreferredSize(new Dimension(35, 30));
        btnQuickCust.setToolTipText("Quick Register Customer");
        btnQuickCust.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Enter customer name:", "Quick Register Customer", JOptionPane.PLAIN_MESSAGE);
            if (name == null || name.isBlank()) return;
            String phone = JOptionPane.showInputDialog(this, "Enter customer phone number:", "Quick Register Customer", JOptionPane.PLAIN_MESSAGE);
            if (phone == null || phone.isBlank()) return;

            try {
                Customer c = canteenService.registerCustomer(name, phone);
                refreshCustomersCombo();
                cmbCustomers.setSelectedItem(c);
                showSuccess("Registered and selected customer: " + c.getName());
            } catch (Exception ex) {
                showError("Customer registration failed: " + ex.getMessage());
            }
        });
        customerSelectPanel.add(btnQuickCust, BorderLayout.EAST);
        rightCard.add(customerSelectPanel, BorderLayout.NORTH);

        // Shopping Cart Table
        modelCart = new DefaultTableModel(new Object[]{"Item Name", "Price", "Qty", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tblCart = new JTable(modelCart);
        styleTable(tblCart);
        JScrollPane scrollCart = new JScrollPane(tblCart);
        scrollCart.getViewport().setBackground(COLOR_BG_CARD);
        scrollCart.setBorder(BorderFactory.createEmptyBorder());
        rightCard.add(scrollCart, BorderLayout.CENTER);

        // Checkout controls panel
        JPanel checkoutControlsPanel = new JPanel(new BorderLayout(0, 10));
        checkoutControlsPanel.setOpaque(false);

        lblCartTotal = new JLabel("Total Amount: Rs. 0.00");
        lblCartTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblCartTotal.setForeground(COLOR_SUCCESS);
        checkoutControlsPanel.add(lblCartTotal, BorderLayout.NORTH);

        JPanel checkoutBtnRow = new JPanel(new GridLayout(1, 2, 10, 0));
        checkoutBtnRow.setOpaque(false);

        ModernButton btnClearCart = new ModernButton("Clear Cart", new Color(110, 110, 130), new Color(130, 130, 150));
        btnClearCart.addActionListener(e -> clearCart());
        checkoutBtnRow.add(btnClearCart);

        ModernButton btnCheckout = new ModernButton("Checkout", COLOR_SUCCESS, new Color(0, 195, 220));
        btnCheckout.addActionListener(e -> {
            Customer selectedCust = (Customer) cmbCustomers.getSelectedItem();
            if (selectedCust == null) {
                showWarning("Please select or register a customer first.");
                return;
            }
            if (cartList.isEmpty()) {
                showWarning("Shopping cart is empty. Add menu items first.");
                return;
            }

            // Confirm order
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to place this order of Rs. " + String.format("%.2f", calculateCartTotal()) + " for " + selectedCust.getName() + "?",
                    "Confirm Order", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            try {
                List<OrderRequest> requests = new ArrayList<>();
                for (CartItem ci : cartList) {
                    requests.add(new OrderRequest(ci.itemId, ci.qty));
                }

                canteenService.placeOrder(selectedCust.getId(), requests);
                showSuccess("Order placed successfully!");
                clearCart();
                refreshAllData();
            } catch (Exception ex) {
                showError("Order failed: " + ex.getMessage());
            }
        });
        checkoutBtnRow.add(btnCheckout);
        checkoutControlsPanel.add(checkoutBtnRow, BorderLayout.SOUTH);

        rightCard.add(checkoutControlsPanel, BorderLayout.SOUTH);

        gbc.gridx = 2;
        gbc.weightx = 0.43;
        body.add(rightCard, gbc);

        return panel;
    }

    private void addToCart(int itemId, String name, double price, int qty) {
        // Check if item already exists in cart list
        for (CartItem ci : cartList) {
            if (ci.itemId == itemId) {
                ci.qty += qty;
                refreshCartTable();
                return;
            }
        }
        cartList.add(new CartItem(itemId, name, price, qty));
        refreshCartTable();
    }

    private void clearCart() {
        cartList.clear();
        refreshCartTable();
    }

    private void refreshCartTable() {
        modelCart.setRowCount(0);
        double total = 0;
        for (CartItem ci : cartList) {
            double lineTotal = ci.price * ci.qty;
            total += lineTotal;
            modelCart.addRow(new Object[]{ci.name, String.format("Rs. %.2f", ci.price), ci.qty, String.format("Rs. %.2f", lineTotal)});
        }
        lblCartTotal.setText("Total Amount: Rs. " + String.format("%.2f", total));
    }

    private double calculateCartTotal() {
        double total = 0;
        for (CartItem ci : cartList) {
            total += (ci.price * ci.qty);
        }
        return total;
    }

    private void filterMenuTable() {
        String filter = txtSearchMenu.getText().trim().toLowerCase();
        try {
            List<MenuItem> items = canteenService.getAllMenuItems();
            modelMenuSelection.setRowCount(0);
            for (MenuItem item : items) {
                if (filter.isEmpty() ||
                        item.getName().toLowerCase().contains(filter) ||
                        item.getCategory().toLowerCase().contains(filter)) {
                    modelMenuSelection.addRow(new Object[]{item.getId(), item.getName(), item.getCategory(), item.getPrice(), item.getQuantity()});
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Helper CartItem class
    private static class CartItem {
        int itemId;
        String name;
        double price;
        int qty;

        CartItem(int itemId, String name, double price, int qty) {
            this.itemId = itemId;
            this.name = name;
            this.price = price;
            this.qty = qty;
        }
    }

    // ==========================================
    // VIEW 3: MANAGE MENU
    // ==========================================
    private JPanel createManageMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);

        // Header
        JLabel lblTitle = new JLabel("Manage Menu Items");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(COLOR_TEXT_PRIMARY);
        panel.add(lblTitle, BorderLayout.NORTH);

        // Split body
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        panel.add(body, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left Table Panel
        CardPanel leftCard = new CardPanel();
        leftCard.setLayout(new BorderLayout(0, 15));

        JLabel lblMenuTable = new JLabel("Menu Directory");
        lblMenuTable.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblMenuTable.setForeground(COLOR_TEXT_PRIMARY);
        leftCard.add(lblMenuTable, BorderLayout.NORTH);

        modelManageMenu = new DefaultTableModel(new Object[]{"ID", "Item Name", "Category", "Price", "Stock"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tblManageMenu = new JTable(modelManageMenu);
        styleTable(tblManageMenu);
        JScrollPane scrollManageMenu = new JScrollPane(tblManageMenu);
        scrollManageMenu.getViewport().setBackground(COLOR_BG_CARD);
        scrollManageMenu.setBorder(BorderFactory.createEmptyBorder());
        leftCard.add(scrollManageMenu, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.weightx = 0.6;
        body.add(leftCard, gbc);

        // Spacing
        gbc.gridx = 1;
        gbc.weightx = 0.02;
        body.add(Box.createHorizontalStrut(15), gbc);

        // Right Input Form Panel
        CardPanel rightCard = new CardPanel();
        rightCard.setLayout(new GridBagLayout());

        GridBagConstraints gbcForm = new GridBagConstraints();
        gbcForm.fill = GridBagConstraints.HORIZONTAL;
        gbcForm.insets = new Insets(0, 0, 15, 0);
        gbcForm.weightx = 1.0;

        JLabel lblFormTitle = new JLabel("Add New Item");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblFormTitle.setForeground(COLOR_TEXT_PRIMARY);
        gbcForm.gridx = 0;
        gbcForm.gridy = 0;
        gbcForm.gridwidth = 2;
        rightCard.add(lblFormTitle, gbcForm);

        // Item Name
        JLabel lblName = new JLabel("Item Name:");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblName.setForeground(COLOR_TEXT_SECONDARY);
        gbcForm.gridy = 1;
        gbcForm.gridwidth = 1;
        gbcForm.weightx = 0.3;
        rightCard.add(lblName, gbcForm);

        txtItemName = new ModernTextField(12, "e.g., Veg Sandwich");
        gbcForm.gridx = 1;
        gbcForm.weightx = 0.7;
        rightCard.add(txtItemName, gbcForm);

        // Category
        JLabel lblCat = new JLabel("Category:");
        lblCat.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblCat.setForeground(COLOR_TEXT_SECONDARY);
        gbcForm.gridx = 0;
        gbcForm.gridy = 2;
        gbcForm.weightx = 0.3;
        rightCard.add(lblCat, gbcForm);

        txtItemCategory = new ModernTextField(12, "e.g., Snacks");
        gbcForm.gridx = 1;
        gbcForm.weightx = 0.7;
        rightCard.add(txtItemCategory, gbcForm);

        // Price
        JLabel lblPrice = new JLabel("Price (Rs.):");
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPrice.setForeground(COLOR_TEXT_SECONDARY);
        gbcForm.gridx = 0;
        gbcForm.gridy = 3;
        gbcForm.weightx = 0.3;
        rightCard.add(lblPrice, gbcForm);

        txtItemPrice = new ModernTextField(12, "e.g., 50.00");
        gbcForm.gridx = 1;
        gbcForm.weightx = 0.7;
        rightCard.add(txtItemPrice, gbcForm);

        // Stock
        JLabel lblStock = new JLabel("Initial Stock:");
        lblStock.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStock.setForeground(COLOR_TEXT_SECONDARY);
        gbcForm.gridx = 0;
        gbcForm.gridy = 4;
        gbcForm.weightx = 0.3;
        rightCard.add(lblStock, gbcForm);

        txtItemStock = new ModernTextField(12, "e.g., 30");
        gbcForm.gridx = 1;
        gbcForm.weightx = 0.7;
        rightCard.add(txtItemStock, gbcForm);

        // Save Button
        ModernButton btnAddItem = new ModernButton("Add Item", COLOR_ACCENT, COLOR_ACCENT_HOVER);
        btnAddItem.setPreferredSize(new Dimension(btnAddItem.getPreferredSize().width, 38));
        btnAddItem.addActionListener(e -> {
            String name = txtItemName.getText().trim();
            String cat = txtItemCategory.getText().trim();
            String priceStr = txtItemPrice.getText().trim();
            String stockStr = txtItemStock.getText().trim();

            if (name.isEmpty() || cat.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
                showWarning("All fields are required.");
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                int stock = Integer.parseInt(stockStr);

                canteenService.addMenuItem(name, cat, price, stock);
                showSuccess("Menu item '" + name + "' added successfully!");

                // Clear fields
                txtItemName.setText("");
                txtItemCategory.setText("");
                txtItemPrice.setText("");
                txtItemStock.setText("");

                refreshAllData();
            } catch (NumberFormatException ex) {
                showWarning("Please enter numerical values for price (e.g. 50.00) and stock (e.g. 30).");
            } catch (Exception ex) {
                showError("Failed to add menu item: " + ex.getMessage());
            }
        });
        gbcForm.gridx = 0;
        gbcForm.gridy = 5;
        gbcForm.gridwidth = 2;
        gbcForm.weightx = 1.0;
        rightCard.add(btnAddItem, gbcForm);

        // Filler rigid area
        gbcForm.gridy = 6;
        gbcForm.weighty = 1.0;
        rightCard.add(Box.createVerticalGlue(), gbcForm);

        gbc.gridx = 2;
        gbc.weightx = 0.38;
        body.add(rightCard, gbc);

        return panel;
    }

    // ==========================================
    // VIEW 4: MANAGE CUSTOMERS
    // ==========================================
    private JPanel createManageCustomersPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);

        // Header
        JLabel lblTitle = new JLabel("Manage Customers");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(COLOR_TEXT_PRIMARY);
        panel.add(lblTitle, BorderLayout.NORTH);

        // Split Body
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        panel.add(body, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left Table Panel
        CardPanel leftCard = new CardPanel();
        leftCard.setLayout(new BorderLayout(0, 15));

        JLabel lblCustTable = new JLabel("Customer List");
        lblCustTable.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblCustTable.setForeground(COLOR_TEXT_PRIMARY);
        leftCard.add(lblCustTable, BorderLayout.NORTH);

        modelManageCustomers = new DefaultTableModel(new Object[]{"Customer ID", "Customer Name", "Phone Number"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tblManageCust = new JTable(modelManageCustomers);
        styleTable(tblManageCust);
        JScrollPane scrollManageCust = new JScrollPane(tblManageCust);
        scrollManageCust.getViewport().setBackground(COLOR_BG_CARD);
        scrollManageCust.setBorder(BorderFactory.createEmptyBorder());
        leftCard.add(scrollManageCust, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.weightx = 0.6;
        body.add(leftCard, gbc);

        // Spacing
        gbc.gridx = 1;
        gbc.weightx = 0.02;
        body.add(Box.createHorizontalStrut(15), gbc);

        // Right Input Form Panel
        CardPanel rightCard = new CardPanel();
        rightCard.setLayout(new GridBagLayout());

        GridBagConstraints gbcForm = new GridBagConstraints();
        gbcForm.fill = GridBagConstraints.HORIZONTAL;
        gbcForm.insets = new Insets(0, 0, 15, 0);
        gbcForm.weightx = 1.0;

        JLabel lblFormTitle = new JLabel("Register Customer");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblFormTitle.setForeground(COLOR_TEXT_PRIMARY);
        gbcForm.gridx = 0;
        gbcForm.gridy = 0;
        gbcForm.gridwidth = 2;
        rightCard.add(lblFormTitle, gbcForm);

        // Name
        JLabel lblName = new JLabel("Customer Name:");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblName.setForeground(COLOR_TEXT_SECONDARY);
        gbcForm.gridy = 1;
        gbcForm.gridwidth = 1;
        gbcForm.weightx = 0.35;
        rightCard.add(lblName, gbcForm);

        txtCustName = new ModernTextField(12, "e.g., Ramesh Kumar");
        gbcForm.gridx = 1;
        gbcForm.weightx = 0.65;
        rightCard.add(txtCustName, gbcForm);

        // Phone
        JLabel lblPhone = new JLabel("Phone Number:");
        lblPhone.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPhone.setForeground(COLOR_TEXT_SECONDARY);
        gbcForm.gridx = 0;
        gbcForm.gridy = 2;
        gbcForm.weightx = 0.35;
        rightCard.add(lblPhone, gbcForm);

        txtCustPhone = new ModernTextField(12, "e.g., 9876543210");
        gbcForm.gridx = 1;
        gbcForm.weightx = 0.65;
        rightCard.add(txtCustPhone, gbcForm);

        // Save Button
        ModernButton btnAddCust = new ModernButton("Register Customer", COLOR_ACCENT, COLOR_ACCENT_HOVER);
        btnAddCust.setPreferredSize(new Dimension(btnAddCust.getPreferredSize().width, 38));
        btnAddCust.addActionListener(e -> {
            String name = txtCustName.getText().trim();
            String phone = txtCustPhone.getText().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                showWarning("All fields are required.");
                return;
            }

            try {
                canteenService.registerCustomer(name, phone);
                showSuccess("Customer registered successfully!");

                // Clear fields
                txtCustName.setText("");
                txtCustPhone.setText("");

                refreshAllData();
            } catch (Exception ex) {
                showError("Customer registration failed: " + ex.getMessage());
            }
        });
        gbcForm.gridx = 0;
        gbcForm.gridy = 3;
        gbcForm.gridwidth = 2;
        gbcForm.weightx = 1.0;
        rightCard.add(btnAddCust, gbcForm);

        // Filler rigid area
        gbcForm.gridy = 4;
        gbcForm.weighty = 1.0;
        rightCard.add(Box.createVerticalGlue(), gbcForm);

        gbc.gridx = 2;
        gbc.weightx = 0.38;
        body.add(rightCard, gbc);

        return panel;
    }

    // ==========================================
    // VIEW 5: ORDER HISTORY
    // ==========================================
    private JPanel createOrderHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);

        // Header
        JLabel lblTitle = new JLabel("Order History Log");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(COLOR_TEXT_PRIMARY);
        panel.add(lblTitle, BorderLayout.NORTH);

        // Split Body
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        panel.add(body, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // Left Table Panel
        CardPanel leftCard = new CardPanel();
        leftCard.setLayout(new BorderLayout(0, 15));

        JLabel lblOrderList = new JLabel("Orders master list");
        lblOrderList.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblOrderList.setForeground(COLOR_TEXT_PRIMARY);
        leftCard.add(lblOrderList, BorderLayout.NORTH);

        modelOrderHistory = new DefaultTableModel(new Object[]{"Order ID", "Customer Name", "Total Price", "Order Date/Time"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tblOrderHistory = new JTable(modelOrderHistory);
        styleTable(tblOrderHistory);
        JScrollPane scrollOrderHistory = new JScrollPane(tblOrderHistory);
        scrollOrderHistory.getViewport().setBackground(COLOR_BG_CARD);
        scrollOrderHistory.setBorder(BorderFactory.createEmptyBorder());
        leftCard.add(scrollOrderHistory, BorderLayout.CENTER);

        // Click row action to update right detail pane
        tblOrderHistory.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = tblOrderHistory.getSelectedRow();
                if (selectedRow != -1) {
                    int orderId = (int) tblOrderHistory.getValueAt(selectedRow, 0);
                    showOrderDetails(orderId);
                }
            }
        });

        gbc.gridx = 0;
        gbc.weightx = 0.55;
        body.add(leftCard, gbc);

        // Spacing
        gbc.gridx = 1;
        gbc.weightx = 0.02;
        body.add(Box.createHorizontalStrut(15), gbc);

        // Right Detail Pane Panel
        CardPanel rightCard = new CardPanel();
        rightCard.setLayout(new BorderLayout(0, 15));

        // Details header panel
        JPanel detailsHeader = new JPanel();
        detailsHeader.setLayout(new BoxLayout(detailsHeader, BoxLayout.Y_AXIS));
        detailsHeader.setOpaque(false);

        JLabel lblDetailTitle = new JLabel("Order Specification Details");
        lblDetailTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblDetailTitle.setForeground(COLOR_TEXT_PRIMARY);
        lblDetailTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsHeader.add(lblDetailTitle);
        detailsHeader.add(Box.createRigidArea(new Dimension(0, 15)));

        lblDetailOrderID = new JLabel("Order ID: -");
        lblDetailOrderID.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDetailOrderID.setForeground(COLOR_TEXT_SECONDARY);
        lblDetailOrderID.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsHeader.add(lblDetailOrderID);

        lblDetailCust = new JLabel("Customer: -");
        lblDetailCust.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDetailCust.setForeground(COLOR_TEXT_SECONDARY);
        lblDetailCust.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsHeader.add(Box.createRigidArea(new Dimension(0, 5)));
        detailsHeader.add(lblDetailCust);

        lblDetailTime = new JLabel("Placed Time: -");
        lblDetailTime.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDetailTime.setForeground(COLOR_TEXT_SECONDARY);
        lblDetailTime.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsHeader.add(Box.createRigidArea(new Dimension(0, 5)));
        detailsHeader.add(lblDetailTime);

        rightCard.add(detailsHeader, BorderLayout.NORTH);

        // Items sub list
        DefaultListModel<String> modelOrderItems = new DefaultListModel<>();
        listOrderDetailLines = new JList<>(modelOrderItems);
        listOrderDetailLines.setBackground(COLOR_BG_MAIN);
        listOrderDetailLines.setForeground(COLOR_TEXT_PRIMARY);
        listOrderDetailLines.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        listOrderDetailLines.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        rightCard.add(new JScrollPane(listOrderDetailLines), BorderLayout.CENTER);

        // Detail bottom summary
        lblDetailTotal = new JLabel("Total Billing: Rs. 0.00");
        lblDetailTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblDetailTotal.setForeground(COLOR_SUCCESS);
        rightCard.add(lblDetailTotal, BorderLayout.SOUTH);

        gbc.gridx = 2;
        gbc.weightx = 0.43;
        body.add(rightCard, gbc);

        return panel;
    }

    private void showOrderDetails(int orderId) {
        try {
            List<Order> orders = canteenService.getAllOrders();
            for (Order o : orders) {
                if (o.getId() == orderId) {
                    lblDetailOrderID.setText("Order ID: #" + o.getId());
                    lblDetailCust.setText("Customer: " + o.getCustomerName());
                    lblDetailTime.setText("Placed Time: " + o.getOrderTime());
                    lblDetailTotal.setText("Total Billing: Rs. " + String.format("%.2f", o.getTotalAmount()));

                    DefaultListModel<String> listModel = (DefaultListModel<String>) listOrderDetailLines.getModel();
                    listModel.clear();
                    for (String line : o.getItemLines()) {
                        listModel.addElement(line);
                    }
                    return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // DATA REFRESH LOGIC
    // ==========================================
    private void refreshAllData() {
        try {
            // 1. Refresh Dashboard Statistics
            int totalOrders = canteenService.getTotalOrders();
            double totalRevenue = canteenService.getTotalRevenue();
            MenuItem topSelling = canteenService.getTopSellingItem();

            lblTotalOrdersVal.setText(String.valueOf(totalOrders));
            lblTotalRevenueVal.setText(String.format("Rs. %.2f", totalRevenue));
            lblTopItemVal.setText(topSelling != null ? topSelling.getName() : "N/A");

            // Recent Orders list in Dashboard
            List<Order> orders = canteenService.getAllOrders();
            modelRecentOrders.setRowCount(0);
            int count = 0;
            for (Order o : orders) {
                if (count >= 5) break;
                modelRecentOrders.addRow(new Object[]{o.getId(), o.getCustomerName(), String.format("Rs. %.2f", o.getTotalAmount()), o.getOrderTime()});
                count++;
            }

            // 2. Refresh Menu Table (in Place Order & Manage Menu tabs)
            List<MenuItem> menuItems = canteenService.getAllMenuItems();
            modelMenuSelection.setRowCount(0);
            modelManageMenu.setRowCount(0);
            for (MenuItem item : menuItems) {
                Object[] rowData = {item.getId(), item.getName(), item.getCategory(), item.getPrice(), item.getQuantity()};
                modelMenuSelection.addRow(rowData);
                modelManageMenu.addRow(new Object[]{item.getId(), item.getName(), item.getCategory(), String.format("Rs. %.2f", item.getPrice()), item.getQuantity()});
            }

            // Filter menu table if any search text exists
            filterMenuTable();

            // 3. Refresh Customers Dropdown (Place Order) and Table (Manage Customers)
            refreshCustomersCombo();

            List<Customer> customers = canteenService.getAllCustomers();
            modelManageCustomers.setRowCount(0);
            for (Customer c : customers) {
                modelManageCustomers.addRow(new Object[]{c.getId(), c.getName(), c.getPhone()});
            }

            // 4. Refresh Order History Log
            modelOrderHistory.setRowCount(0);
            for (Order o : orders) {
                modelOrderHistory.addRow(new Object[]{o.getId(), o.getCustomerName(), String.format("Rs. %.2f", o.getTotalAmount()), o.getOrderTime()});
            }

        } catch (SQLException exception) {
            showError("Database retrieval failed: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    private void refreshCustomersCombo() throws SQLException {
        cmbCustomers.removeAllItems();
        List<Customer> customers = canteenService.getAllCustomers();
        for (Customer c : customers) {
            cmbCustomers.addItem(c);
        }
    }

    // ==========================================
    // UTILITY DIALOGS & STYLE METHODS
    // ==========================================
    private void showWarning(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void styleTable(JTable table) {
        table.setRowHeight(32);
        table.setBackground(COLOR_BG_CARD);
        table.setForeground(COLOR_TEXT_PRIMARY);
        table.setSelectionBackground(COLOR_ACCENT);
        table.setSelectionForeground(COLOR_TEXT_PRIMARY);
        table.setShowGrid(false);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setIntercellSpacing(new Dimension(0, 0));

        // Render Table Headers
        JTableHeader header = table.getTableHeader();
        header.setBackground(COLOR_BG_SIDEBAR);
        header.setForeground(COLOR_TEXT_PRIMARY);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        header.setReorderingAllowed(false);

        // Custom Cell Rendering
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(new EmptyBorder(5, 10, 5, 10));

                if (isSelected) {
                    setBackground(COLOR_ACCENT);
                } else {
                    // Alternate colors
                    if (row % 2 == 0) {
                        setBackground(COLOR_BG_CARD);
                    } else {
                        setBackground(new Color(42, 42, 64));
                    }
                }
                return this;
            }
        };
        table.setDefaultRenderer(Object.class, cellRenderer);
        table.setDefaultRenderer(Integer.class, cellRenderer);
        table.setDefaultRenderer(Double.class, cellRenderer);
    }

    // Custom Customer Renderer for ComboBox
    private static class CustomerListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setBackground(COLOR_BG_CARD);
            setForeground(COLOR_TEXT_PRIMARY);

            if (isSelected) {
                setBackground(COLOR_ACCENT);
            }

            if (value instanceof Customer customer) {
                setText(customer.getName() + " (" + customer.getPhone() + ")");
            }
            return this;
        }
    }

    // Custom Modern Button
    private static class ModernButton extends JButton {
        private final Color normalBg;
        private final Color hoverBg;

        public ModernButton(String text, Color normalBg, Color hoverBg) {
            super(text);
            this.normalBg = normalBg;
            this.hoverBg = hoverBg;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(COLOR_TEXT_PRIMARY);
            setBackground(normalBg);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 20, 10, 20));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(hoverBg);
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(normalBg);
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    // Custom Rounded Sidebar Button
    private static class SidebarButton extends JButton {
        private boolean isActive = false;
        private final Color activeBg = COLOR_ACCENT;
        private final Color idleBg = COLOR_BG_SIDEBAR;
        private final Color hoverBg = COLOR_BORDER;

        public SidebarButton(String text, String iconName) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(COLOR_TEXT_PRIMARY);
            setBackground(idleBg);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(new EmptyBorder(10, 15, 10, 15));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.CENTER_ALIGNMENT);
            setMaximumSize(new Dimension(200, 45));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!isActive) {
                        setBackground(hoverBg);
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!isActive) {
                        setBackground(idleBg);
                        repaint();
                    }
                }
            });
        }

        public void setActive(boolean active) {
            this.isActive = active;
            setBackground(active ? activeBg : idleBg);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    // Custom Modern TextField with Placeholder and Focused Outline
    private static class ModernTextField extends JTextField {
        private final String placeholder;
        private final Color borderCol = COLOR_BORDER;
        private final Color focusCol = COLOR_ACCENT;

        public ModernTextField(int columns, String placeholder) {
            super(columns);
            this.placeholder = placeholder;
            setBackground(COLOR_BG_CARD);
            setForeground(COLOR_TEXT_PRIMARY);
            setCaretColor(COLOR_TEXT_PRIMARY);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setBorder(new EmptyBorder(8, 12, 8, 12));
            setOpaque(false);

            addFocusListener(new java.awt.event.FocusListener() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    repaint();
                }
                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
            super.paintComponent(g2);

            // Draw border outline
            g2.setColor(hasFocus() ? focusCol : borderCol);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Double(0.75, 0.75, getWidth() - 1.5, getHeight() - 1.5, 8, 8));

            // Placeholder Text
            if (getText().isEmpty() && !hasFocus()) {
                g2.setColor(COLOR_TEXT_SECONDARY);
                g2.setFont(getFont().deriveFont(Font.ITALIC));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(placeholder, 12, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
        }
    }

    // Custom CardPanel with custom border radius
    private static class CardPanel extends JPanel {
        private final int radius = 12;

        public CardPanel() {
            setOpaque(false);
            setBackground(COLOR_BG_CARD);
            setBorder(new EmptyBorder(16, 16, 16, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), radius, radius));
            g2.dispose();
        }
    }
}
