package com.healthsense.ui;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import com.healthsense.service.AuthService;

public class AppPanel extends JPanel {
    private final AuthService authService;
    private final Runnable onSignOut;
    private final MetricsPanel metricsPanel;
    private final FoodLogPanel foodLogPanel;
    private final GoalsPanel goalsPanel;

    public AppPanel(AuthService authService, Runnable onSignOut) {
        this.authService = authService;
        this.onSignOut = onSignOut;
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        goalsPanel = new GoalsPanel(authService);
        metricsPanel = new MetricsPanel(authService, () -> {
            try {
                java.lang.reflect.Method m = goalsPanel.getClass().getDeclaredMethod("refreshOnTab");
                m.setAccessible(true);
                m.invoke(goalsPanel);
            } catch (Exception ignored) {}
        });
        foodLogPanel = new FoodLogPanel(authService);
        tabs.addTab("Metrics", metricsPanel);
        tabs.addTab("Food Log", foodLogPanel);
        tabs.addTab("Goals", goalsPanel);
        // Removed Analytics tab per request
        tabs.addChangeListener(e -> {
            int idx = ((JTabbedPane) e.getSource()).getSelectedIndex();
            String title = ((JTabbedPane) e.getSource()).getTitleAt(idx);
            if ("Food Log".equals(title)) {
                // Force refresh when switching into Food Log
                try {
                    java.lang.reflect.Method m = foodLogPanel.getClass().getDeclaredMethod("refresh");
                    m.setAccessible(true);
                    m.invoke(foodLogPanel);
                } catch (Exception ignored) {}
            } else if ("Metrics".equals(title)) {
                metricsPanel.refreshAfterAuth();
            } else if ("Goals".equals(title)) {
                try {
                    java.lang.reflect.Method m = goalsPanel.getClass().getDeclaredMethod("refreshOnTab");
                    m.setAccessible(true);
                    m.invoke(goalsPanel);
                } catch (Exception ignored) {}
            }
        });

        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("HEALTHSENSE", SwingConstants.LEFT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        JButton signOut = new JButton("Sign out");
        signOut.addActionListener(e -> { authService.signOut(); onSignOut.run(); });
        header.add(title, BorderLayout.WEST);
        header.add(signOut, BorderLayout.EAST);
        header.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    public void onActivated() {
        metricsPanel.refreshAfterAuth();
        try {
            java.lang.reflect.Method m = foodLogPanel.getClass().getDeclaredMethod("refresh");
            m.setAccessible(true);
            m.invoke(foodLogPanel);
        } catch (Exception ignored) {}
        try {
            java.lang.reflect.Method m2 = goalsPanel.getClass().getDeclaredMethod("loadProfile");
            m2.setAccessible(true);
            m2.invoke(goalsPanel);
        } catch (Exception ignored) {}
    }
}


