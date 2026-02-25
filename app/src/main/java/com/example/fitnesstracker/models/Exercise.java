package com.example.fitnesstracker.models;

public class Exercise {
    private int id;
    private String name;
    private String type;              // "arm", "leg", "core"
    private String description;
    private int difficulty;           // 1=Easy, 2=Medium, 3=Hard
    private String imageResource;     // اسم الصورة في drawable

    // حقول جديدة من wger API
    private String category;          // مثل: "Arms", "Legs", "Chest"
    private String muscles;           // مثل: "Biceps, Triceps"
    private String equipment;         // مثل: "Barbell, Dumbbell"

    public Exercise() {
    }

    public Exercise(int id, String name, String type, String description,
                    int difficulty, String imageResource) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.difficulty = difficulty;
        this.imageResource = imageResource;
    }

    public Exercise(int id, String name, String type, String description,
                    int difficulty, String imageResource,
                    String category, String muscles, String equipment) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.difficulty = difficulty;
        this.imageResource = imageResource;
        this.category = category;
        this.muscles = muscles;
        this.equipment = equipment;
    }

    public Exercise(String name, String type, String description,
                    int difficulty, String imageResource) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.difficulty = difficulty;
        this.imageResource = imageResource;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public String getImageResource() {
        return imageResource;
    }

    public void setImageResource(String imageResource) {
        this.imageResource = imageResource;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getMuscles() {
        return muscles;
    }

    public void setMuscles(String muscles) {
        this.muscles = muscles;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    // دالة مستوى الصعوبة
    public String getDifficultyText() {
        switch (difficulty) {
            case 1:
                return "Easy";
            case 2:
                return "Medium";
            case 3:
                return "Hard";
            default:
                return "Unknown";
        }
    }

    public String getDifficultyStars() {
        switch (difficulty) {
            case 1:
                return "★☆☆";
            case 2:
                return "★★☆";
            case 3:
                return "★★★";
            default:
                return "☆☆☆";
        }
    }
}
