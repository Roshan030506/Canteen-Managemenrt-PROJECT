package com.canteen;

import com.canteen.db.DatabaseManager;
import com.canteen.service.CanteenService;
import com.canteen.ui.CanteenManagementFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseManager databaseManager = new DatabaseManager();
            databaseManager.initialize();

            CanteenService canteenService = new CanteenService(databaseManager);
            canteenService.seedSampleData();

            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Keep default look and feel if the system one is unavailable.
            }

            SwingUtilities.invokeLater(() -> {
                CanteenManagementFrame frame = new CanteenManagementFrame(canteenService);
                frame.setVisible(true);
            });
        } catch (Exception exception) {
            System.err.println("Application failed to start: " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}
