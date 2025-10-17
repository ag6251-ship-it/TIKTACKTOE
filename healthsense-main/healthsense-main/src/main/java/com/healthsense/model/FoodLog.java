package com.healthsense.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class FoodLog {
    private Integer id;
    private Integer userId;
    private LocalDate date;
    private String meal;
    private Double mealWeight; // grams
    private Integer calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private String notes;
    private LocalTime time; // HH:mm

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getMeal() { return meal; }
    public void setMeal(String meal) { this.meal = meal; }

    public Double getMealWeight() { return mealWeight; }
    public void setMealWeight(Double mealWeight) { this.mealWeight = mealWeight; }

    public Integer getCalories() { return calories; }
    public void setCalories(Integer calories) { this.calories = calories; }

    public Double getProtein() { return protein; }
    public void setProtein(Double protein) { this.protein = protein; }

    public Double getCarbs() { return carbs; }
    public void setCarbs(Double carbs) { this.carbs = carbs; }

    public Double getFat() { return fat; }
    public void setFat(Double fat) { this.fat = fat; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }
}


