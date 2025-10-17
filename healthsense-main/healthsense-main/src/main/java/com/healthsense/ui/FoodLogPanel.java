package com.healthsense.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.healthsense.model.FoodLog;
import com.healthsense.model.User;
import com.healthsense.service.AuthService;
import com.healthsense.service.HealthService;

public class FoodLogPanel extends JPanel {
	private final AuthService authService;
	private final HealthService healthService = new HealthService();

	private final DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Time", "Meal", "Weight (g)", "Calories", "Protein", "Carbs", "Fat", "Notes"}, 0) {
		@Override public boolean isCellEditable(int row, int column) { return false; }
	};
	private final JTable table = new JTable(model);

	private final JTextField dateField = new JTextField();
	private final JButton refreshBtn = new JButton("Refresh");
	private final JButton calendarBtn = new JButton("Calendar");
	private final JTextField timeField = new JTextField();
	private final JTextField mealField = new JTextField();
	private final JSpinner mealWeight = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 20000.0, 1.0));
	private final JSpinner calories = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 10));
	private final JSpinner protein = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1000.0, 0.5));
	private final JSpinner carbs = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1000.0, 0.5));
	private final JSpinner fat = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 1000.0, 0.5));
	private final JTextField notes = new JTextField();

	private Integer editingId = null;

	public FoodLogPanel(AuthService authService) {
		this.authService = authService;
		setLayout(new BorderLayout());

		// Date/time controls row
		Font inputFont = mealField.getFont().deriveFont(20f);
		dateField.setEditable(false);
		dateField.setFont(inputFont); dateField.setColumns(12);
		timeField.setFont(inputFont); timeField.setColumns(6);
		JButton nowBtn = new JButton("Now");
		nowBtn.addActionListener(e -> timeField.setText(LocalTime.now().withSecond(0).withNano(0).toString()));

		JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		datePanel.add(dateField);
		datePanel.add(refreshBtn);
		datePanel.add(calendarBtn);
		JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
		timePanel.add(timeField);
		timePanel.add(nowBtn);
		JPanel dateTimeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
		dateTimeRow.add(new JLabel("Date:"));
		dateTimeRow.add(datePanel);
		dateTimeRow.add(new JLabel("Time:"));
		dateTimeRow.add(timePanel);

		// Form grid
		JPanel form = new JPanel(new GridLayout(2, 8, 6, 6));
		form.add(new JLabel("Meal")); form.add(new JLabel("Weight (g)")); form.add(new JLabel("Calories")); form.add(new JLabel("Protein")); form.add(new JLabel("Carbs")); form.add(new JLabel("Fat")); form.add(new JLabel("Notes")); form.add(new JLabel(""));
		mealField.setFont(inputFont); mealField.setColumns(14);
		notes.setFont(inputFont); notes.setColumns(14);
		((JSpinner.DefaultEditor) mealWeight.getEditor()).getTextField().setFont(inputFont);
		((JSpinner.DefaultEditor) calories.getEditor()).getTextField().setFont(inputFont);
		((JSpinner.DefaultEditor) protein.getEditor()).getTextField().setFont(inputFont);
		((JSpinner.DefaultEditor) carbs.getEditor()).getTextField().setFont(inputFont);
		((JSpinner.DefaultEditor) fat.getEditor()).getTextField().setFont(inputFont);
		form.add(mealField); form.add(mealWeight); form.add(calories); form.add(protein); form.add(carbs); form.add(fat); form.add(notes); form.add(new JLabel(""));

		JButton addBtn = new JButton("Add");
		JButton deleteBtn = new JButton("Delete Selected");
		JButton loadBtn = new JButton("Load Selected");
		JButton saveBtn = new JButton("Save Changes");
		JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		actions.add(addBtn); actions.add(deleteBtn); actions.add(loadBtn); actions.add(saveBtn);

		JPanel northContainer = new JPanel(new BorderLayout());
		northContainer.add(dateTimeRow, BorderLayout.NORTH);
		northContainer.add(form, BorderLayout.CENTER);
		add(northContainer, BorderLayout.NORTH);
		add(new JScrollPane(table), BorderLayout.CENTER);
		add(actions, BorderLayout.SOUTH);

		addBtn.addActionListener(e -> addFood());
		deleteBtn.addActionListener(e -> deleteSelected());
		loadBtn.addActionListener(e -> loadSelected());
		saveBtn.addActionListener(e -> saveChanges());

		// Nutrition auto-estimation on changes and Enter key
		mealField.getDocument().addDocumentListener((SimpleDocumentListener) e -> estimateNutrition());
		mealField.addActionListener(e -> forceRecalculateNutrition());
		((JSpinner.DefaultEditor) mealWeight.getEditor()).getTextField().getDocument().addDocumentListener((SimpleDocumentListener) e -> estimateNutrition());
		((JSpinner.DefaultEditor) mealWeight.getEditor()).getTextField().addActionListener(e -> forceRecalculateNutrition());

		// default date/time
		updateDateNow();
		timeField.setText(LocalTime.now().withSecond(0).withNano(0).toString());

		// date controls
		refreshBtn.addActionListener(e -> { updateDateNow(); refresh(); });
		calendarBtn.addActionListener(e -> { openCalendarPicker(); refresh(); });

		// Hide ID column
		table.getColumnModel().getColumn(0).setMinWidth(0);
		table.getColumnModel().getColumn(0).setMaxWidth(0);
		table.getColumnModel().getColumn(0).setPreferredWidth(0);

		refresh();
	}

	private void deleteSelected() {
		int row = table.getSelectedRow();
		if (row < 0) return;
		Object idVal = model.getValueAt(row, 0);
		if (idVal == null) return; // header row
		int id = (int) idVal;
		try {
			healthService.deleteFoodLog(id);
			refresh();
		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
		}
	}

	private void estimateNutrition() {
		String meal = mealField.getText().trim();
		double weight = (Double) mealWeight.getValue();
		HealthService.Nutrition n = healthService.inferNutritionPer100g(meal);
		if (((Integer) calories.getValue()) == 0) calories.setValue((int) Math.round(n.calories * (weight / 100.0)));
		if (((Double) protein.getValue()) == 0.0) protein.setValue(Math.round(n.protein * (weight / 100.0) * 10.0) / 10.0);
		if (((Double) carbs.getValue()) == 0.0) carbs.setValue(Math.round(n.carbs * (weight / 100.0) * 10.0) / 10.0);
		if (((Double) fat.getValue()) == 0.0) fat.setValue(Math.round(n.fat * (weight / 100.0) * 10.0) / 10.0);
	}

	private void forceRecalculateNutrition() {
		String meal = mealField.getText().trim();
		double weight = (Double) mealWeight.getValue();
		HealthService.Nutrition n = healthService.inferNutritionPer100g(meal);
		calories.setValue((int) Math.round(n.calories * (weight / 100.0)));
		protein.setValue(Math.round(n.protein * (weight / 100.0) * 10.0) / 10.0);
		carbs.setValue(Math.round(n.carbs * (weight / 100.0) * 10.0) / 10.0);
		fat.setValue(Math.round(n.fat * (weight / 100.0) * 10.0) / 10.0);
	}

	private void refresh() {
		model.setRowCount(0);
		User user = authService.getCurrentUser();
		if (user == null) return;
		try {
			LocalDate selected = parseSelectedDate();
			List<FoodLog> list = healthService.listFoodByDate(user.getId(), selected);
			// Header row with date
			model.addRow(new Object[]{null, selected.toString(), "", "", "", "", "", "", ""});
			for (FoodLog f : list) {
				String time = f.getTime() == null ? "" : f.getTime().toString();
				model.addRow(new Object[]{f.getId(), time, f.getMeal(), f.getMealWeight(), f.getCalories(), f.getProtein(), f.getCarbs(), f.getFat(), f.getNotes()});
			}
		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
		}
	}

	private void addFood() {
		User user = authService.getCurrentUser();
		if (user == null) return;
		if (mealField.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Enter meal");
			return;
		}
		FoodLog f = new FoodLog();
		f.setUserId(user.getId());
		f.setDate(parseSelectedDate());
		f.setMeal(mealField.getText().trim());
		// Default to now if blank
		LocalTime t;
		try { t = timeField.getText().trim().isEmpty() ? LocalTime.now() : LocalTime.parse(timeField.getText().trim()); } catch (Exception e) { t = LocalTime.now(); }
		f.setTime(t);
		f.setMealWeight((Double) mealWeight.getValue());
		f.setCalories((Integer) calories.getValue());
		f.setProtein((Double) protein.getValue());
		f.setCarbs((Double) carbs.getValue());
		f.setFat((Double) fat.getValue());
		f.setNotes(notes.getText().trim());
		try {
			healthService.applyNutritionEstimate(f); // only fills missing fields
			healthService.addFoodLog(f);
			mealField.setText(""); notes.setText("");
			mealWeight.setValue(0.0); calories.setValue(0); protein.setValue(0.0); carbs.setValue(0.0); fat.setValue(0.0);
			timeField.setText(LocalTime.now().withSecond(0).withNano(0).toString());
			refresh();
		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
		}
	}

	private void loadSelected() {
		int row = table.getSelectedRow();
		if (row < 0) return;
		Object idVal = model.getValueAt(row, 0);
		if (idVal == null) return; // header row
		editingId = (Integer) idVal;
		String time = (String) model.getValueAt(row, 1);
		String meal = (String) model.getValueAt(row, 2);
		Object weightObj = model.getValueAt(row, 3);
		Object calObj = model.getValueAt(row, 4);
		Object proObj = model.getValueAt(row, 5);
		Object carbObj = model.getValueAt(row, 6);
		Object fatObj = model.getValueAt(row, 7);
		String note = (String) model.getValueAt(row, 8);

		timeField.setText(time == null ? "" : time);
		mealField.setText(meal == null ? "" : meal);
		mealWeight.setValue(weightObj == null || weightObj.equals("") ? 0.0 : ((Number) weightObj).doubleValue());
		calories.setValue(calObj == null || calObj.equals(0) || calObj.equals("") ? 0 : ((Number) calObj).intValue());
		protein.setValue(proObj == null || proObj.equals("") ? 0.0 : ((Number) proObj).doubleValue());
		carbs.setValue(carbObj == null || carbObj.equals("") ? 0.0 : ((Number) carbObj).doubleValue());
		fat.setValue(fatObj == null || fatObj.equals("") ? 0.0 : ((Number) fatObj).doubleValue());
		notes.setText(note == null ? "" : note);
	}

	private void saveChanges() {
		if (editingId == null) {
			JOptionPane.showMessageDialog(this, "Select a row and click Load Selected first");
			return;
		}
		User user = authService.getCurrentUser();
		if (user == null) return;
		FoodLog f = new FoodLog();
		f.setId(editingId);
		f.setUserId(user.getId());
		f.setDate(parseSelectedDate());
		f.setMeal(mealField.getText().trim());
		LocalTime t;
		try { t = timeField.getText().trim().isEmpty() ? LocalTime.now() : LocalTime.parse(timeField.getText().trim()); } catch (Exception e) { t = LocalTime.now(); }
		f.setTime(t);
		f.setMealWeight((Double) mealWeight.getValue());
		f.setCalories((Integer) calories.getValue());
		f.setProtein((Double) protein.getValue());
		f.setCarbs((Double) carbs.getValue());
		f.setFat((Double) fat.getValue());
		f.setNotes(notes.getText().trim());
		try {
			healthService.updateFoodLog(f);
			editingId = null;
			refresh();
		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
		}
	}

	private void updateDateNow() {
		dateField.setText(LocalDate.now().toString());
	}

	private void openCalendarPicker() {
		LocalDate currentDate;
		try { currentDate = LocalDate.parse(dateField.getText()); } catch (Exception e) { currentDate = LocalDate.now(); }

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
					dateField.setText(chosen.toString());
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
		try { return LocalDate.parse(dateField.getText()); } catch (Exception e) { return LocalDate.now(); }
	}

	// Simple DocumentListener adapter for lambdas
	private interface SimpleDocumentListener extends javax.swing.event.DocumentListener {
		void update(javax.swing.event.DocumentEvent e);
		@Override default void insertUpdate(javax.swing.event.DocumentEvent e) { update(e); }
		@Override default void removeUpdate(javax.swing.event.DocumentEvent e) { update(e); }
		@Override default void changedUpdate(javax.swing.event.DocumentEvent e) { update(e); }
	}
}


