package com.example.fitnesstracker.models;

public class User {
    private int id;
    private String name;
    private String email;
    private String password;
    private float height;
    private float weight;
    private float bmi;
    private String goal;
    private int weeklyGoalDays;

    public User() {
    }

    public User(int id, String name, String email, String password,
                float height, float weight, float bmi, String goal, int weeklyGoalDays) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.height = height;
        this.weight = weight;
        this.bmi = bmi;
        this.goal = goal;
        this.weeklyGoalDays = weeklyGoalDays;
    }

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.goal = "maintain";
        this.weeklyGoalDays = 3;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getBmi() {
        return bmi;
    }

    public void setBmi(float bmi) {
        this.bmi = bmi;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public int getWeeklyGoalDays() {
        return weeklyGoalDays;
    }

    public void setWeeklyGoalDays(int weeklyGoalDays) {
        this.weeklyGoalDays = weeklyGoalDays;
    }

    public void calculateBmi() {
        if (height > 0 && weight > 0) {
            float heightInMeters = height / 100.0f;
            this.bmi = weight / (heightInMeters * heightInMeters);
        }
    }
}