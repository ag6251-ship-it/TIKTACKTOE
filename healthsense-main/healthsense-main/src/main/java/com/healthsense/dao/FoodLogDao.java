package com.healthsense.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.healthsense.db.Database;
import com.healthsense.model.FoodLog;
import com.healthsense.util.DateUtil;

public class FoodLogDao {
    public FoodLog insert(FoodLog log) throws SQLException {
        String sql = "INSERT INTO food_log(user_id, date, time, meal, meal_weight, calories, protein, carbs, fat, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, log.getUserId());
            ps.setString(2, DateUtil.formatDate(log.getDate()));
            ps.setString(3, DateUtil.formatTime(log.getTime()));
            ps.setString(4, log.getMeal());
            setDouble(ps, 5, log.getMealWeight());
            setInteger(ps, 6, log.getCalories());
            setDouble(ps, 7, log.getProtein());
            setDouble(ps, 8, log.getCarbs());
            setDouble(ps, 9, log.getFat());
            ps.setString(10, log.getNotes());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) log.setId(rs.getInt(1));
            }
            return log;
        }
    }

    public void deleteById(int id) throws SQLException {
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM food_log WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void update(FoodLog log) throws SQLException {
        if (log.getId() == null) throw new SQLException("FoodLog id is required for update");
        String sql = "UPDATE food_log SET date=?, time=?, meal=?, meal_weight=?, calories=?, protein=?, carbs=?, fat=?, notes=? WHERE id=?";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, DateUtil.formatDate(log.getDate()));
            ps.setString(2, DateUtil.formatTime(log.getTime()));
            ps.setString(3, log.getMeal());
            setDouble(ps, 4, log.getMealWeight());
            setInteger(ps, 5, log.getCalories());
            setDouble(ps, 6, log.getProtein());
            setDouble(ps, 7, log.getCarbs());
            setDouble(ps, 8, log.getFat());
            ps.setString(9, log.getNotes());
            ps.setInt(10, log.getId());
            ps.executeUpdate();
        }
    }

    public List<FoodLog> listByDate(int userId, LocalDate date) throws SQLException {
        String sql = "SELECT id, user_id, date, time, meal, meal_weight, calories, protein, carbs, fat, notes FROM food_log WHERE user_id=? AND date=? ORDER BY time DESC, id DESC";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, DateUtil.formatDate(date));
            try (ResultSet rs = ps.executeQuery()) {
                List<FoodLog> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        }
    }

    public List<FoodLog> listInRange(int userId, LocalDate start, LocalDate end) throws SQLException {
        String sql = "SELECT id, user_id, date, time, meal, meal_weight, calories, protein, carbs, fat, notes FROM food_log WHERE user_id=? AND date BETWEEN ? AND ? ORDER BY date DESC, time DESC, id DESC";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, DateUtil.formatDate(start));
            ps.setString(3, DateUtil.formatDate(end));
            try (ResultSet rs = ps.executeQuery()) {
                List<FoodLog> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        }
    }

    public int sumCaloriesByDate(int userId, LocalDate date) throws SQLException {
        String sql = "SELECT COALESCE(SUM(calories),0) as total FROM food_log WHERE user_id=? AND date=?";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, DateUtil.formatDate(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("total");
                return 0;
            }
        }
    }

    private FoodLog map(ResultSet rs) throws SQLException {
        FoodLog f = new FoodLog();
        f.setId(rs.getInt("id"));
        f.setUserId(rs.getInt("user_id"));
        f.setDate(DateUtil.parseDate(rs.getString("date")));
        f.setTime(DateUtil.parseTime(rs.getString("time")));
        f.setMeal(rs.getString("meal"));
        f.setMealWeight(getNullableDouble(rs, "meal_weight"));
        f.setCalories(getNullableInt(rs, "calories"));
        f.setProtein(getNullableDouble(rs, "protein"));
        f.setCarbs(getNullableDouble(rs, "carbs"));
        f.setFat(getNullableDouble(rs, "fat"));
        f.setNotes(rs.getString("notes"));
        return f;
    }

    private void setInteger(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) ps.setNull(index, Types.INTEGER); else ps.setInt(index, value);
    }

    private void setDouble(PreparedStatement ps, int index, Double value) throws SQLException {
        if (value == null) ps.setNull(index, Types.REAL); else ps.setDouble(index, value);
    }

    private Integer getNullableInt(ResultSet rs, String column) throws SQLException {
        int v = rs.getInt(column);
        return rs.wasNull() ? null : v;
    }

    private Double getNullableDouble(ResultSet rs, String column) throws SQLException {
        double v = rs.getDouble(column);
        return rs.wasNull() ? null : v;
    }
}


