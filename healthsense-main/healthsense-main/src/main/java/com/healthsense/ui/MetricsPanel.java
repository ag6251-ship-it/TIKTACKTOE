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
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.healthsense.model.HealthMetric;
import com.healthsense.model.User;
import com.healthsense.service.AuthService;
import com.healthsense.service.HealthService;

public class MetricsPanel extends JPanel {
    private final AuthService authService;
    private final HealthService healthService = new HealthService();
    private final Runnable onMetricsChanged;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private final JTextField dateField = new JTextField();
    private final JTextField timeField = new JTextField();
    private final JButton refreshBtn = new JButton("Refresh");
    private final JButton calendarBtn = new JButton("Calendar");
    private final JSpinner stepsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1_000_000, 100));
    private final JSpinner weightSpinner = new JSpinner(new SpinnerNumberModel(70.0, 0.0, 500.0, 0.1));
    private final JSpinner sleepSpinner = new JSpinner(new SpinnerNumberModel(7.0, 0.0, 24.0, 0.25));
    private final JSpinner hrSpinner = new JSpinner(new SpinnerNumberModel(70, 0, 250, 1));
    private final JSpinner caloriesSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10_000, 10));
    private final JTextArea notesArea = new JTextArea(10, 40);
    private final DefaultTableModel logModel = new DefaultTableModel(
            new Object[]{"Date", "Steps", "Weight", "Sleep", "HR", "Calories", "Notes"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable logTable = new JTable(logModel);

    public MetricsPanel(AuthService authService) {
        this(authService, null);
    }

    public MetricsPanel(AuthService authService, Runnable onMetricsChanged) {
        this.authService = authService;
        this.onMetricsChanged = onMetricsChanged;
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        // Increase text area font
        notesArea.setFont(notesArea.getFont().deriveFont(30f));
        // Configure date/time display fields
        dateField.setEditable(false);
        dateField.setFont(dateField.getFont().deriveFont(24f));
        dateField.setColumns(12);
        timeField.setEditable(true);
        timeField.setFont(timeField.getFont().deriveFont(24f));
        timeField.setColumns(6);

        int row = 0;
        c.gridx = 0; c.gridy = row; form.add(new JLabel("Date / Time"), c);
        c.gridx = 1;
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        datePanel.add(dateField);
        datePanel.add(timeField);
        datePanel.add(refreshBtn);
        datePanel.add(calendarBtn);
        form.add(datePanel, c);

        row++; c.gridx = 0; c.gridy = row; form.add(new JLabel("Steps"), c);
        c.gridx = 1; form.add(stepsSpinner, c);

        row++; c.gridx = 0; c.gridy = row; form.add(new JLabel("Weight (kg)"), c);
        c.gridx = 1; form.add(weightSpinner, c);

        row++; c.gridx = 0; c.gridy = row; form.add(new JLabel("Sleep (hours)"), c);
        c.gridx = 1; form.add(sleepSpinner, c);

        row++; c.gridx = 0; c.gridy = row; form.add(new JLabel("Heart rate (bpm)"), c);
        c.gridx = 1; form.add(hrSpinner, c);

        row++; c.gridx = 0; c.gridy = row; form.add(new JLabel("Calories burned"), c);
        c.gridx = 1; form.add(caloriesSpinner, c);

        row++; c.gridx = 0; c.gridy = row; form.add(new JLabel("Notes"), c);
        c.gridx = 1; c.fill = GridBagConstraints.BOTH; c.weighty = 0.6; form.add(new JScrollPane(notesArea), c);

        // Entry log table below notes
        row++; c.gridx = 0; c.gridy = row; c.fill = GridBagConstraints.HORIZONTAL; c.weighty = 0; form.add(new JLabel("Entry log (last 30 days)"), c);
        c.gridx = 1; c.fill = GridBagConstraints.BOTH; c.weighty = 0.4;
        JScrollPane logScroll = new JScrollPane(logTable);
        form.add(logScroll, c);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton load = new JButton("Load");
        JButton save = new JButton("Save");
        actions.add(new JLabel(""));
        actions.add(load); actions.add(save);

        add(form, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        load.addActionListener(e -> loadMetric());
        save.addActionListener(e -> saveMetric());
        refreshBtn.addActionListener(e -> { updateDateTimeNow(); refreshEntryLog(); });
        calendarBtn.addActionListener(e -> { openCalendarPicker(); refreshEntryLog(); });

        // Initialize with current date/time
        updateDateTimeNow();
        refreshEntryLog();
    }

    private void loadMetric() {
        User user = authService.getCurrentUser();
        if (user == null) return;
        LocalDate date = parseSelectedDate();
        try {
            HealthMetric m = healthService.getMetric(user.getId(), date);
            if (m != null) {
                stepsSpinner.setValue(m.getSteps() == null ? 0 : m.getSteps());
                weightSpinner.setValue(m.getWeight() == null ? 0.0 : m.getWeight());
                sleepSpinner.setValue(m.getSleepHours() == null ? 0.0 : m.getSleepHours());
                hrSpinner.setValue(m.getHeartRate() == null ? 0 : m.getHeartRate());
                caloriesSpinner.setValue(m.getCaloriesBurned() == null ? 0 : m.getCaloriesBurned());
                notesArea.setText(m.getNotes() == null ? "" : m.getNotes());
            } else {
                stepsSpinner.setValue(0);
                weightSpinner.setValue(0.0);
                sleepSpinner.setValue(0.0);
                hrSpinner.setValue(0);
                caloriesSpinner.setValue(0);
                notesArea.setText("");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading: " + ex.getMessage());
        }
    }

    private void saveMetric() {
        User user = authService.getCurrentUser();
        if (user == null) return;
        LocalDate date = parseSelectedDate();

        HealthMetric m = new HealthMetric();
        m.setUserId(user.getId());
        m.setDate(date);
        m.setSteps((Integer) stepsSpinner.getValue());
        m.setWeight((Double) weightSpinner.getValue());
        m.setSleepHours((Double) sleepSpinner.getValue());
        m.setHeartRate((Integer) hrSpinner.getValue());
        m.setCaloriesBurned((Integer) caloriesSpinner.getValue());
        m.setNotes(notesArea.getText());
        try {
            healthService.upsertMetric(m);
            JOptionPane.showMessageDialog(this, "Saved");
            if (onMetricsChanged != null) {
                try { onMetricsChanged.run(); } catch (Exception ignored) {}
            }
            refreshEntryLog();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error saving: " + ex.getMessage());
        }
    }

    private void updateDateTimeNow() {
        LocalDateTime now = LocalDateTime.now();
        dateField.setText(now.format(DATE_FORMATTER));
        timeField.setText(now.format(TIME_FORMATTER));
    }

    private void openCalendarPicker() {
        LocalDate currentDate;
        try {
            currentDate = LocalDate.parse(dateField.getText(), DATE_FORMATTER);
        } catch (Exception e) {
            currentDate = LocalDate.now();
        }

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
            // Weekday headers
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
            int firstDow = first.getDayOfWeek().getValue() % 7; // Sunday=0
            for (int i = 0; i < firstDow; i++) grid.add(new JLabel(""));
            for (int d = 1; d <= ym.lengthOfMonth(); d++) {
                final int day = d;
                JButton b = new JButton(String.valueOf(day));
                b.addActionListener(ev -> {
                    LocalDate chosen = LocalDate.of(y, m, day);
                    dateField.setText(chosen.format(DATE_FORMATTER));
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

    private LocalDate parseSelectedDate() {
        try {
            return LocalDate.parse(dateField.getText(), DATE_FORMATTER);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private void refreshEntryLog() {
        logModel.setRowCount(0);
        User user = authService.getCurrentUser();
        if (user == null) return;
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(30);
        try {
            java.util.List<HealthMetric> list = healthService.listMetrics(user.getId(), start, end);
            for (HealthMetric m : list) {
                logModel.addRow(new Object[]{
                        m.getDate() != null ? m.getDate().toString() : "",
                        m.getSteps() != null ? m.getSteps() : "",
                        m.getWeight() != null ? m.getWeight() : "",
                        m.getSleepHours() != null ? m.getSleepHours() : "",
                        m.getHeartRate() != null ? m.getHeartRate() : "",
                        m.getCaloriesBurned() != null ? m.getCaloriesBurned() : "",
                        m.getNotes() != null ? m.getNotes() : ""
                });
            }
        } catch (SQLException ex) {
            // Non-blocking: show a toast
            JOptionPane.showMessageDialog(this, "Error loading log: " + ex.getMessage());
        }
    }

    // Expose a public method so container can refresh after authentication
    public void refreshAfterAuth() {
        updateDateTimeNow();
        refreshEntryLog();
    }
}


