package com.example.fitnesstracker.models;

public class Workout {
    private int id;
    private int userId;
    private String type;
    private int duration;
    private String date;

    public Workout() {
    }

    public Workout(int userId, String type, int duration, String date) {
        this.userId = userId;
        this.type = type;
        this.duration = duration;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}