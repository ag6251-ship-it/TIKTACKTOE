package com.healthsense.service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.healthsense.dao.FoodLogDao;
import com.healthsense.dao.HealthMetricDao;
import com.healthsense.dao.UserDao;
import com.healthsense.model.FoodLog;
import com.healthsense.model.HealthMetric;
import com.healthsense.model.User;

public class HealthService {
    private final HealthMetricDao metricDao = new HealthMetricDao();
    private final FoodLogDao foodLogDao = new FoodLogDao();
    private final UserDao userDao = new UserDao();

    public void upsertMetric(HealthMetric metric) throws SQLException {
        metricDao.upsert(metric);
    }

    public HealthMetric getMetric(int userId, LocalDate date) throws SQLException {
        return metricDao.getByUserAndDate(userId, date);
    }

    public List<HealthMetric> listMetrics(int userId, LocalDate start, LocalDate end) throws SQLException {
        return metricDao.listInRange(userId, start, end);
    }

    public FoodLog addFoodLog(FoodLog log) throws SQLException {
        return foodLogDao.insert(log);
    }

    public void updateFoodLog(FoodLog log) throws SQLException {
        foodLogDao.update(log);
    }

    public void deleteFoodLog(int id) throws SQLException {
        foodLogDao.deleteById(id);
    }

    public List<FoodLog> listFoodByDate(int userId, LocalDate date) throws SQLException {
        return foodLogDao.listByDate(userId, date);
    }

    public List<FoodLog> listFoodInRange(int userId, LocalDate start, LocalDate end) throws SQLException {
        return foodLogDao.listInRange(userId, start, end);
    }

    public int sumCaloriesByDate(int userId, LocalDate date) throws SQLException {
        return foodLogDao.sumCaloriesByDate(userId, date);
    }

    public User getUser(int id) throws SQLException { return userDao.findById(id); }

    public void updateUserProfile(User user) throws SQLException { userDao.updateProfile(user); }

    // Nutrition inference based on simple keyword mapping per 100g
    public static class Nutrition {
        public int calories; public double protein; public double carbs; public double fat;
    }

    public Nutrition inferNutritionPer100g(String mealName) {
        String key = mealName == null ? "" : mealName.toLowerCase();
        Nutrition n = new Nutrition();
        if (key.contains("chicken breast") || key.contains("chicken")) { n.calories=165; n.protein=31; n.carbs=0; n.fat=3.6; }
        else if (key.contains("paneer") || key.contains("cottage")) { n.calories=296; n.protein=25; n.carbs=6; n.fat=20; }
        else if (key.contains("milk")) { n.calories=60; n.protein=3.2; n.carbs=5; n.fat=3.3; }
        else if (key.contains("dal") || key.contains("lentil")) { n.calories=116; n.protein=9; n.carbs=20; n.fat=0.4; }
        else if (key.contains("rice")) { n.calories=130; n.protein=2.7; n.carbs=28; n.fat=0.3; }
        else if (key.contains("oat") || key.contains("oats")) { n.calories=389; n.protein=17; n.carbs=66; n.fat=7; }
        else { n.calories=0; n.protein=0; n.carbs=0; n.fat=0; }
        return n;
    }

    public void applyNutritionEstimate(FoodLog log) {
        Nutrition per100 = inferNutritionPer100g(log.getMeal());
        double weight = log.getMealWeight() == null ? 0.0 : log.getMealWeight();
        double factor = weight / 100.0;
        if (log.getCalories() == null || log.getCalories() == 0) log.setCalories((int) Math.round(per100.calories * factor));
        if (log.getProtein() == null || log.getProtein() == 0) log.setProtein(round1(per100.protein * factor));
        if (log.getCarbs() == null || log.getCarbs() == 0) log.setCarbs(round1(per100.carbs * factor));
        if (log.getFat() == null || log.getFat() == 0) log.setFat(round1(per100.fat * factor));
    }

    private double round1(double v) { return Math.round(v * 10.0) / 10.0; }
}


