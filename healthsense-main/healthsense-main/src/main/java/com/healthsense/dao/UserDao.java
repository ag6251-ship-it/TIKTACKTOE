package com.healthsense.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import com.healthsense.db.Database;
import com.healthsense.model.User;
import com.healthsense.util.DateUtil;

public class UserDao {
    public User create(String email, String name, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users(email, name, password_hash) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, email.trim().toLowerCase());
            ps.setString(2, name.trim());
            ps.setString(3, passwordHash);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    return findById(id);
                }
            }
        }
        throw new SQLException("Failed to create user");
    }

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT id, email, name, password_hash, gender, height_cm, age, target_weight, target_date, created_at FROM users WHERE email = ?";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
                return null;
            }
        }
    }

    public User findById(int id) throws SQLException {
        String sql = "SELECT id, email, name, password_hash, gender, height_cm, age, target_weight, target_date, created_at FROM users WHERE id = ?";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
                return null;
            }
        }
    }

    public void updateProfile(User user) throws SQLException {
        String sql = "UPDATE users SET name=?, gender=?, height_cm=?, age=?, target_weight=?, target_date=? WHERE id=?";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getGender());
            if (user.getHeightCm() == null) ps.setNull(3, Types.REAL); else ps.setDouble(3, user.getHeightCm());
            if (user.getAge() == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, user.getAge());
            if (user.getTargetWeight() == null) ps.setNull(5, Types.REAL); else ps.setDouble(5, user.getTargetWeight());
            if (user.getTargetDate() == null) ps.setNull(6, Types.VARCHAR); else ps.setString(6, com.healthsense.util.DateUtil.formatDateTime(user.getTargetDate()));
            ps.setInt(7, user.getId());
            ps.executeUpdate();
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setEmail(rs.getString("email"));
        u.setName(rs.getString("name"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setGender(rs.getString("gender"));
        double h = rs.getDouble("height_cm"); if (!rs.wasNull()) u.setHeightCm(h);
        int age = rs.getInt("age"); if (!rs.wasNull()) u.setAge(age);
        double tw = rs.getDouble("target_weight"); if (!rs.wasNull()) u.setTargetWeight(tw);
        String td = rs.getString("target_date"); if (td != null) u.setTargetDate(com.healthsense.util.DateUtil.parseDateTime(td));
        u.setCreatedAt(DateUtil.parseDateTime(rs.getString("created_at")));
        return u;
    }
}


