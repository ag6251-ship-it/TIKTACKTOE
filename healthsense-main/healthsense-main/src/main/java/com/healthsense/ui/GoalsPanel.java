package com.healthsense.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.healthsense.model.User;
import com.healthsense.service.AuthService;
import com.healthsense.service.HealthService;

public class GoalsPanel extends JPanel {
    private final AuthService authService;
    private final HealthService healthService = new HealthService();

    private final JComboBox<String> gender = new JComboBox<>(new String[]{"Male","Female","Other"});
    private final JSpinner heightCm = new JSpinner(new SpinnerNumberModel(170.0, 50.0, 250.0, 0.5));
    private final JSpinner age = new JSpinner(new SpinnerNumberModel(30, 10, 100, 1));
    private final JSpinner targetWeight = new JSpinner(new SpinnerNumberModel(70.0, 20.0, 300.0, 0.1));
    private final JTextField targetDate = new JTextField(); // yyyy-MM-dd
    private final JButton targetCalendarBtn = new JButton("Calendar");

    private final JLabel bmiLabel = new JLabel("-");
    private final JLabel maintenanceLabel = new JLabel("-");
    private final JLabel todaysTotalLabel = new JLabel("-");
    private final JLabel dailyTargetLabel = new JLabel("-");
    private final JLabel remainingLabel = new JLabel("-");

    public GoalsPanel(AuthService authService) {
        this.authService = authService;
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        int row = 0;
        c.gridx=0; c.gridy=row; form.add(new JLabel("Gender"), c); c.gridx=1; form.add(gender, c);
        row++; c.gridx=0; c.gridy=row; form.add(new JLabel("Height (cm)"), c); c.gridx=1; form.add(heightCm, c);
        row++; c.gridx=0; c.gridy=row; form.add(new JLabel("Age"), c); c.gridx=1; form.add(age, c);
        row++; c.gridx=0; c.gridy=row; form.add(new JLabel("Target weight (kg)"), c); c.gridx=1; form.add(targetWeight, c);
        row++; c.gridx=0; c.gridy=row; form.add(new JLabel("Target date"), c);
        JPanel dateRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        targetDate.setEditable(false);
        dateRow.add(targetDate); dateRow.add(targetCalendarBtn);
        c.gridx=1; form.add(dateRow, c);

        JPanel metrics = new JPanel(new GridLayout(5,2,8,8));
        metrics.add(new JLabel("BMI")); metrics.add(bmiLabel);
        metrics.add(new JLabel("Maintenance calories")); metrics.add(maintenanceLabel);
        metrics.add(new JLabel("Today's total calories")); metrics.add(todaysTotalLabel);
        metrics.add(new JLabel("Daily target calories")); metrics.add(dailyTargetLabel);
        metrics.add(new JLabel("Remaining today")); metrics.add(remainingLabel);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save = new JButton("Save Profile");
        JButton recalc = new JButton("Recalculate");
        actions.add(save); actions.add(recalc);

        add(form, BorderLayout.NORTH);
        add(metrics, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        save.addActionListener(e -> saveProfile());
        recalc.addActionListener(e -> compute());
        targetCalendarBtn.addActionListener(e -> openCalendarPicker());

        loadProfile();
    }

    public void loadProfile() {
        User u = authService.getCurrentUser();
        if (u == null) return;
        if (u.getGender() != null) gender.setSelectedItem(u.getGender()); else gender.setSelectedItem("male");
        if (u.getHeightCm() != null) heightCm.setValue(u.getHeightCm());
        if (u.getAge() != null) age.setValue(u.getAge());
        if (u.getTargetWeight() != null) targetWeight.setValue(u.getTargetWeight());
        if (u.getTargetDate() != null) targetDate.setText(u.getTargetDate().toLocalDate().toString());
        compute();
    }

    private void saveProfile() {
        User u = authService.getCurrentUser();
        if (u == null) return;
        u.setGender(((String) gender.getSelectedItem()));
        u.setHeightCm(((Number) heightCm.getValue()).doubleValue());
        u.setAge(((Number) age.getValue()).intValue());
        u.setTargetWeight(((Number) targetWeight.getValue()).doubleValue());
        String td = targetDate.getText().trim();
        if (!td.isEmpty()) {
            try { u.setTargetDate(java.time.LocalDate.parse(td).atStartOfDay()); } catch (Exception ignored) {}
        }
        try {
            new HealthService().updateUserProfile(u);
            JOptionPane.showMessageDialog(this, "Saved");
            compute();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void compute() {
        User u = authService.getCurrentUser();
        if (u == null) return;

        Double h = (Double) heightCm.getValue();
        double w = 0.0;
        try {
            // Prefer the latest non-null weight over only today's
            java.util.List<com.healthsense.model.HealthMetric> history = healthService.listMetrics(u.getId(), LocalDate.now().minusDays(365), LocalDate.now());
            for (int i = history.size() - 1; i >= 0; i--) {
                Double ww = history.get(i).getWeight();
                if (ww != null && ww > 0) { w = ww; break; }
            }
            if (w == 0.0) {
                com.healthsense.model.HealthMetric today = healthService.getMetric(u.getId(), LocalDate.now());
                if (today != null && today.getWeight() != null) w = today.getWeight();
            }
        } catch (SQLException ignored) {}
        if (w <= 0) { bmiLabel.setText("-"); } else { double bmi = w / Math.pow(h/100.0, 2); bmiLabel.setText(String.format("%.1f", bmi)); }

        // Mifflin-St Jeor maintenance estimate
        int a = ((Number) age.getValue()).intValue();
        String g = ((String) gender.getSelectedItem());
        double bmr = (10*w) + (6.25*h) - (5*a) + (g.startsWith("m") ? 5 : -161);
        int maintenance = (int) Math.round(bmr * 1.2); // sedentary
        maintenanceLabel.setText(String.valueOf(maintenance));

        int todays = 0;
        try { todays = healthService.sumCaloriesByDate(u.getId(), LocalDate.now()); } catch (SQLException ignored) {}
        todaysTotalLabel.setText(String.valueOf(todays));

        // Daily target to reach goal by targetDate
        double tw = ((Number) targetWeight.getValue()).doubleValue();
        String td = targetDate.getText().trim();
        int dailyTarget = maintenance; // default
        if (w > 0 && tw > 0 && !td.isEmpty()) {
            try {
                LocalDate goal = LocalDate.parse(td);
                long days = Math.max(1, ChronoUnit.DAYS.between(LocalDate.now(), goal));
                double deltaKg = tw - w; // positive to gain, negative to lose
                // 1 kg ~ 7700 kcal
                double totalDeltaKcal = deltaKg * 7700.0;
                double perDayDelta = totalDeltaKcal / days;
                dailyTarget = (int) Math.round(maintenance + perDayDelta);
            } catch (Exception ignored) {}
        }
        dailyTargetLabel.setText(String.valueOf(dailyTarget));
        remainingLabel.setText(String.valueOf(Math.max(0, dailyTarget - todays)));
    }

    public void refreshOnTab() { compute(); }

    private void openCalendarPicker() {
        LocalDate currentDate;
        try { currentDate = LocalDate.parse(targetDate.getText()); } catch (Exception e) { currentDate = LocalDate.now(); }

        final JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Select Date", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(8, 8));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));

        Integer[] years = new Integer[101];
        int baseYear = 2000;
        for (int i = 0; i < years.length; i++) years[i] = baseYear + i;
        JComboBox<Integer> yearBox = new JComboBox<>(years);
        yearBox.setSelectedItem(currentDate.getYear());

        String[] months = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        JComboBox<String> monthBox = new JComboBox<>(months);
        monthBox.setSelectedIndex(currentDate.getMonthValue() - 1);

        top.add(new JLabel("Year:")); top.add(yearBox);
        top.add(new JLabel("Month:")); top.add(monthBox);
        dialog.add(top, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 7, 4, 4));
        dialog.add(grid, BorderLayout.CENTER);

        Runnable rebuild = () -> {
            grid.removeAll();
            String[] wd = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
            for (String h : wd) {
                JLabel l = new JLabel(h, SwingConstants.CENTER);
                l.setFont(l.getFont().deriveFont(Font.BOLD, 12f));
                grid.add(l);
            }
            int y = (Integer) yearBox.getSelectedItem();
            int m = monthBox.getSelectedIndex() + 1;
            YearMonth ym = YearMonth.of(y, m);
            LocalDate first = ym.atDay(1);
            int firstDow = first.getDayOfWeek().getValue() % 7;
            for (int i = 0; i < firstDow; i++) grid.add(new JLabel(""));
            for (int d = 1; d <= ym.lengthOfMonth(); d++) {
                final int day = d;
                JButton b = new JButton(String.valueOf(day));
                b.addActionListener(ev -> {
                    LocalDate chosen = LocalDate.of(y, m, day);
                    targetDate.setText(chosen.toString());
                    dialog.dispose();
                });
                grid.add(b);
            }
            grid.revalidate(); grid.repaint();
        };

        yearBox.addActionListener(e -> rebuild.run());
        monthBox.addActionListener(e -> rebuild.run());
        rebuild.run();

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dialog.dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(cancel);
        dialog.add(south, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}



