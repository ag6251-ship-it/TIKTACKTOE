package com.healthsense;

import com.formdev.flatlaf.FlatLightLaf;
import com.healthsense.db.Database;
import com.healthsense.ui.MainFrame;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        // Initialize DB and run migrations before UI starts
        Database.initialize();

        // Setup modern look & feel
        FlatLightLaf.setup();

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}


