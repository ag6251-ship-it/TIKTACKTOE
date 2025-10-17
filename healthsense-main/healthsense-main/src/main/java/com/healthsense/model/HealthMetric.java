package com.healthsense.model;

import java.time.LocalDate;

public class HealthMetric {
    private Integer id;
    private Integer userId;
    private LocalDate date;
    private Integer steps;
    private Double weight;
    private Double sleepHours;
    private Integer heartRate;
    private Integer caloriesBurned;
    private String notes;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Integer getSteps() { return steps; }
    public void setSteps(Integer steps) { this.steps = steps; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Double getSleepHours() { return sleepHours; }
    public void setSleepHours(Double sleepHours) { this.sleepHours = sleepHours; }

    public Integer getHeartRate() { return heartRate; }
    public void setHeartRate(Integer heartRate) { this.heartRate = heartRate; }

    public Integer getCaloriesBurned() { return caloriesBurned; }
    public void setCaloriesBurned(Integer caloriesBurned) { this.caloriesBurned = caloriesBurned; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}


