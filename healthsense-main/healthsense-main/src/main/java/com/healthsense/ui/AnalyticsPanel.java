package com.healthsense.ui;

import com.healthsense.model.HealthMetric;
import com.healthsense.model.User;
import com.healthsense.service.AuthService;
import com.healthsense.service.HealthService;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AnalyticsPanel extends JPanel {
    private final AuthService authService;
    private final HealthService healthService = new HealthService();

    private final JComboBox<String> range = new JComboBox<>(new String[]{"7d", "30d", "90d"});
    private final JPanel charts = new JPanel(new GridLayout(1, 1));

    public AnalyticsPanel(AuthService authService) {
        this.authService = authService;
        setLayout(new BorderLayout());

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controls.add(new JLabel("Range:"));
        controls.add(range);
        JButton refresh = new JButton("Refresh");
        controls.add(refresh);

        add(controls, BorderLayout.NORTH);
        add(charts, BorderLayout.CENTER);

        refresh.addActionListener(e -> renderCharts());
        renderCharts();
    }

    private void renderCharts() {
        charts.removeAll();
        User user = authService.getCurrentUser();
        if (user == null) return;

        int days = switch ((String) range.getSelectedItem()) {
            case "30d" -> 30; case "90d" -> 90; default -> 7;
        };
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days);
        try {
            List<HealthMetric> list = healthService.listMetrics(user.getId(), start, end);
            DefaultCategoryDataset steps = new DefaultCategoryDataset();
            for (HealthMetric m : list) {
                if (m.getSteps() != null) {
                    steps.addValue(m.getSteps(), "Steps", m.getDate().toString());
                }
            }
            JFreeChart chart = ChartFactory.createLineChart("Steps", "Date", "Steps", steps);
            charts.add(new ChartPanel(chart));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
        charts.revalidate(); charts.repaint();
    }
}


