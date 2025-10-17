package com.healthsense.model;

import java.time.LocalDateTime;

public class User {
    private Integer id;
    private String email;
    private String name;
    private String passwordHash;
    private LocalDateTime createdAt;
    private String gender; // male/female/other
    private Double heightCm;
    private Integer age;
    private Double targetWeight;
    private LocalDateTime targetDate;

    public User() {}

    public User(Integer id, String email, String name, String passwordHash, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Double getHeightCm() { return heightCm; }
    public void setHeightCm(Double heightCm) { this.heightCm = heightCm; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Double getTargetWeight() { return targetWeight; }
    public void setTargetWeight(Double targetWeight) { this.targetWeight = targetWeight; }

    public LocalDateTime getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDateTime targetDate) { this.targetDate = targetDate; }
}


