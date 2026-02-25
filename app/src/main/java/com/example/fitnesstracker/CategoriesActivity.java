package com.example.fitnesstracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnesstracker.adapters.CategoryAdapter;
import com.example.fitnesstracker.database.DatabaseHelper;
import com.example.fitnesstracker.models.Category;
import com.example.fitnesstracker.models.Exercise;

import java.util.ArrayList;
import java.util.List;

public class CategoriesActivity extends AppCompatActivity {

    private static final String TAG = "CategoriesActivity";

    private ImageView btnBack;
    private RecyclerView rvBodyPartCategories;

    private CategoryAdapter bodyPartAdapter;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        db = new DatabaseHelper(this);

        initViews();
        setupRecyclerViews();
        loadAllCategories();
        setClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        rvBodyPartCategories = findViewById(R.id.rvBodyPartCategories);
    }

    private void setupRecyclerViews() {
        // Body Part Categories
        bodyPartAdapter = new CategoryAdapter(this);
        rvBodyPartCategories.setLayoutManager(new LinearLayoutManager(this));
        rvBodyPartCategories.setAdapter(bodyPartAdapter);

        bodyPartAdapter.setOnCategoryClickListener(category -> {
            openExerciseList(category);
        });
    }

    private void loadAllCategories() {
        // جلب التمارين من Firebase أولاً
        db.getAllExercisesFromFirebase(exercises -> {
            Log.d(TAG, "Loaded exercises from Firebase: " + exercises.size());

            runOnUiThread(() -> {
                // إنشاء الفئات مع العدد الصحيح
                List<Category> bodyPartCategories = new ArrayList<>();
                bodyPartCategories.add(createCategory("Arms", "💪", "Biceps, Triceps, Forearms", "body_part", "Arms", exercises));
                bodyPartCategories.add(createCategory("Legs", "🦵", "Quads, Hamstrings, Glutes", "body_part", "Legs", exercises));
                bodyPartCategories.add(createCategory("Chest", "🫁", "Pectorals, Upper/Lower", "body_part", "Chest", exercises));
                bodyPartCategories.add(createCategory("Back", "🔙", "Lats, Traps, Lower back", "body_part", "Back", exercises));
                bodyPartCategories.add(createCategory("Shoulders", "🤸", "Deltoids, Rotator cuff", "body_part", "Shoulders", exercises));
                bodyPartCategories.add(createCategory("Abs", "🔥", "Core, Obliques", "body_part", "Abs", exercises));
                bodyPartCategories.add(createCategory("Calves", "🦿", "Gastrocnemius, Soleus", "body_part", "Calves", exercises));
                bodyPartCategories.add(createCategory("Cardio", "❤️", "Running, Jumping", "body_part", "Cardio", exercises));

                bodyPartAdapter.setCategories(bodyPartCategories);

                for (Category cat : bodyPartCategories) {
                    Log.d(TAG, cat.getName() + ": " + cat.getExerciseCount() + " exercises");
                }
            });
        });
    }

    private Category createCategory(String name, String icon, String description,
                                    String type, String filterValue, List<Exercise> allExercises) {
        int count = getExerciseCountForCategory(type, filterValue, allExercises);
        return new Category(name, icon, description, count, type, filterValue);
    }

    private int getExerciseCountForCategory(String type, String filterValue, List<Exercise> allExercises) {
        int count = 0;

        for (Exercise exercise : allExercises) {
            boolean matches = false;

            if (type.equals("body_part")) {
                String category = exercise.getCategory();

                if (category != null) {
                    if (category.equalsIgnoreCase(filterValue)) {
                        matches = true;
                    }
                    else if (category.toLowerCase().contains(filterValue.toLowerCase())) {
                        matches = true;
                    }
                    else if (filterValue.toLowerCase().contains(category.toLowerCase())) {
                        matches = true;
                    }
                }

                if (!matches) {
                    String muscles = exercise.getMuscles();
                    if (muscles != null) {
                        String musclesLower = muscles.toLowerCase();

                        if (filterValue.equalsIgnoreCase("Arms")) {
                            if (musclesLower.contains("biceps") ||
                                    musclesLower.contains("triceps") ||
                                    musclesLower.contains("forearm") ||
                                    musclesLower.contains("arm")) {
                                matches = true;
                            }
                        } else if (filterValue.equalsIgnoreCase("Legs")) {
                            if (musclesLower.contains("quad") ||
                                    musclesLower.contains("hamstring") ||
                                    musclesLower.contains("glute") ||
                                    musclesLower.contains("leg") ||
                                    musclesLower.contains("thigh")) {
                                matches = true;
                            }
                        } else if (filterValue.equalsIgnoreCase("Chest")) {
                            if (musclesLower.contains("pectoral") ||
                                    musclesLower.contains("chest")) {
                                matches = true;
                            }
                        } else if (filterValue.equalsIgnoreCase("Back")) {
                            if (musclesLower.contains("lat") ||
                                    musclesLower.contains("trap") ||
                                    musclesLower.contains("back") ||
                                    musclesLower.contains("rhomboid")) {
                                matches = true;
                            }
                        } else if (filterValue.equalsIgnoreCase("Shoulders")) {
                            if (musclesLower.contains("deltoid") ||
                                    musclesLower.contains("shoulder")) {
                                matches = true;
                            }
                        } else if (filterValue.equalsIgnoreCase("Abs")) {
                            if (musclesLower.contains("abs") ||
                                    musclesLower.contains("core") ||
                                    musclesLower.contains("oblique") ||
                                    musclesLower.contains("abdominal")) {
                                matches = true;
                            }
                        } else if (filterValue.equalsIgnoreCase("Calves")) {
                            if (musclesLower.contains("calf") ||
                                    musclesLower.contains("gastrocnemius") ||
                                    musclesLower.contains("soleus")) {
                                matches = true;
                            }
                        } else if (filterValue.equalsIgnoreCase("Cardio")) {
                            if (musclesLower.contains("cardio") ||
                                    musclesLower.contains("aerobic")) {
                                matches = true;
                            }
                        }
                    }
                }
            }

            if (matches) {
                count++;
            }
        }

        return count;
    }

    private void openExerciseList(Category category) {
        Intent intent = new Intent(this, ExerciseListActivity.class);
        intent.putExtra("category_name", category.getName());
        intent.putExtra("category_type", category.getType());
        intent.putExtra("filter_value", category.getFilterValue());
        startActivity(intent);
    }

    private void setClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }
}