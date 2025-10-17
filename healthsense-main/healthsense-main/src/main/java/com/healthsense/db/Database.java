package com.healthsense.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:healthsense.db";

    public static void initialize() {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON;");

            statement.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " email TEXT NOT NULL UNIQUE," +
                " name TEXT NOT NULL," +
                " password_hash TEXT NOT NULL," +
                " gender TEXT," +
                " height_cm REAL," +
                " age INTEGER," +
                " target_weight REAL," +
                " target_date TEXT," +
                " created_at TEXT DEFAULT (datetime('now'))" +
                ")"
            );

            statement.execute(
                "CREATE TABLE IF NOT EXISTS health_metrics (" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " user_id INTEGER NOT NULL," +
                " date TEXT NOT NULL," +
                " steps INTEGER DEFAULT 0," +
                " weight REAL," +
                " sleep_hours REAL," +
                " heart_rate INTEGER," +
                " calories_burned INTEGER," +
                " notes TEXT," +
                " UNIQUE(user_id, date)," +
                " FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ")"
            );

            statement.execute(
                "CREATE TABLE IF NOT EXISTS food_log (" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " user_id INTEGER NOT NULL," +
                " date TEXT NOT NULL," +
                " time TEXT NOT NULL DEFAULT (CURRENT_TIME)," +
                " meal TEXT NOT NULL," +
                " meal_weight REAL DEFAULT 0," +
                " calories INTEGER DEFAULT 0," +
                " protein REAL DEFAULT 0," +
                " carbs REAL DEFAULT 0," +
                " fat REAL DEFAULT 0," +
                " notes TEXT," +
                " FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ")"
            );

            // Migration: add meal_weight column if missing
            try (ResultSet rs = statement.executeQuery("PRAGMA table_info('food_log')")) {
                boolean hasMealWeight = false;
                boolean hasTimeCol = false;
                while (rs.next()) {
                    String col = rs.getString("name");
                    if ("meal_weight".equalsIgnoreCase(col)) { hasMealWeight = true; }
                    if ("time".equalsIgnoreCase(col)) { hasTimeCol = true; }
                }
                if (!hasMealWeight) statement.execute("ALTER TABLE food_log ADD COLUMN meal_weight REAL DEFAULT 0");
                if (!hasTimeCol) {
                    statement.execute("ALTER TABLE food_log ADD COLUMN time TEXT");
                    statement.execute("UPDATE food_log SET time = time('now') WHERE time IS NULL OR time = ''");
                }
            }

            // Migrate users table additional columns if missing
            try (ResultSet rs = statement.executeQuery("PRAGMA table_info('users')")) {
                boolean hasGender = false, hasHeight = false, hasAge = false, hasTargetWeight = false, hasTargetDate = false;
                while (rs.next()) {
                    String col = rs.getString("name");
                    if ("gender".equalsIgnoreCase(col)) hasGender = true;
                    if ("height_cm".equalsIgnoreCase(col)) hasHeight = true;
                    if ("age".equalsIgnoreCase(col)) hasAge = true;
                    if ("target_weight".equalsIgnoreCase(col)) hasTargetWeight = true;
                    if ("target_date".equalsIgnoreCase(col)) hasTargetDate = true;
                }
                if (!hasGender) statement.execute("ALTER TABLE users ADD COLUMN gender TEXT");
                if (!hasHeight) statement.execute("ALTER TABLE users ADD COLUMN height_cm REAL");
                if (!hasAge) statement.execute("ALTER TABLE users ADD COLUMN age INTEGER");
                if (!hasTargetWeight) statement.execute("ALTER TABLE users ADD COLUMN target_weight REAL");
                if (!hasTargetDate) statement.execute("ALTER TABLE users ADD COLUMN target_date TEXT");
            }

            statement.execute("CREATE INDEX IF NOT EXISTS idx_health_metrics_user_date ON health_metrics(user_id, date);");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_food_log_user_date ON food_log(user_id, date);");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}


