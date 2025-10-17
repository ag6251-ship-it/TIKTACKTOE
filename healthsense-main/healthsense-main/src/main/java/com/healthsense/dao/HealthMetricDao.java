package com.healthsense.dao;

import com.healthsense.db.Database;
import com.healthsense.model.HealthMetric;
import com.healthsense.util.DateUtil;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HealthMetricDao {
    public void upsert(HealthMetric metric) throws SQLException {
        String sql = "INSERT INTO health_metrics(user_id, date, steps, weight, sleep_hours, heart_rate, calories_burned, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT(user_id, date) DO UPDATE SET steps=excluded.steps, weight=excluded.weight, " +
                "sleep_hours=excluded.sleep_hours, heart_rate=excluded.heart_rate, calories_burned=excluded.calories_burned, notes=excluded.notes";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, metric.getUserId());
            ps.setString(2, DateUtil.formatDate(metric.getDate()));
            setInteger(ps, 3, metric.getSteps());
            setDouble(ps, 4, metric.getWeight());
            setDouble(ps, 5, metric.getSleepHours());
            setInteger(ps, 6, metric.getHeartRate());
            setInteger(ps, 7, metric.getCaloriesBurned());
            ps.setString(8, metric.getNotes());
            ps.executeUpdate();
        }
    }

    public HealthMetric getByUserAndDate(int userId, LocalDate date) throws SQLException {
        String sql = "SELECT id, user_id, date, steps, weight, sleep_hours, heart_rate, calories_burned, notes FROM health_metrics WHERE user_id=? AND date=?";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, DateUtil.formatDate(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
                return null;
            }
        }
    }

    public List<HealthMetric> listInRange(int userId, LocalDate start, LocalDate end) throws SQLException {
        String sql = "SELECT id, user_id, date, steps, weight, sleep_hours, heart_rate, calories_burned, notes FROM health_metrics WHERE user_id=? AND date BETWEEN ? AND ? ORDER BY date";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, DateUtil.formatDate(start));
            ps.setString(3, DateUtil.formatDate(end));
            try (ResultSet rs = ps.executeQuery()) {
                List<HealthMetric> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        }
    }

    private HealthMetric map(ResultSet rs) throws SQLException {
        HealthMetric m = new HealthMetric();
        m.setId(rs.getInt("id"));
        m.setUserId(rs.getInt("user_id"));
        m.setDate(DateUtil.parseDate(rs.getString("date")));
        m.setSteps(getNullableInt(rs, "steps"));
        m.setWeight(getNullableDouble(rs, "weight"));
        m.setSleepHours(getNullableDouble(rs, "sleep_hours"));
        m.setHeartRate(getNullableInt(rs, "heart_rate"));
        m.setCaloriesBurned(getNullableInt(rs, "calories_burned"));
        m.setNotes(rs.getString("notes"));
        return m;
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


