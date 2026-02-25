package com.example.fitnesstracker.models;

public class Category {

    private String name;           // "Arms", "Legs", "Chest"
    private String icon;           // "💪", "🦵", "🫁"
    private String description;    // "Biceps, Triceps, Forearms"
    private int exerciseCount;     // 25
    private String type;           // "body_part" or "equipment"
    private String filterValue;    // قيمة الفلتر في SQLite

    public Category() {
    }

    public Category(String name, String icon, String description,
                    int exerciseCount, String type, String filterValue) {
        this.name = name;
        this.icon = icon;
        this.description = description;
        this.exerciseCount = exerciseCount;
        this.type = type;
        this.filterValue = filterValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getExerciseCount() {
        return exerciseCount;
    }

    public void setExerciseCount(int exerciseCount) {
        this.exerciseCount = exerciseCount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFilterValue() {
        return filterValue;
    }

    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }

    public String getFullTitle() {
        return icon + " " + name;
    }

    public String getInfo() {
        return description + "\n" + exerciseCount + " exercises";
    }
}
