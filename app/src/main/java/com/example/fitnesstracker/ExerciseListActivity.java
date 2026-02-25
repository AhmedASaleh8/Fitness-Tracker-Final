package com.example.fitnesstracker;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnesstracker.adapters.ExerciseAdapter;
import com.example.fitnesstracker.database.DatabaseHelper;
import com.example.fitnesstracker.models.Exercise;

import java.util.ArrayList;
import java.util.List;

public class ExerciseListActivity extends AppCompatActivity {

    private static final String TAG = "ExerciseListActivity";

    private ImageView btnBack;
    private TextView tvTitle, tvExerciseCount, tvEmpty;
    private SearchView searchView;
    private RecyclerView rvExercises;

    private ExerciseAdapter adapter;
    private DatabaseHelper db;

    private String categoryName;
    private String categoryType;
    private String filterValue;
    private String searchQuery;

    private List<Exercise> allExercises = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_list);

        db = new DatabaseHelper(this);

        getIntentData();
        initViews();
        setupRecyclerView();
        loadExercises();
        setClickListeners();
        setupSearchView();
    }

    private void getIntentData() {
        categoryName = getIntent().getStringExtra("category_name");
        categoryType = getIntent().getStringExtra("category_type");
        filterValue = getIntent().getStringExtra("filter_value");
        searchQuery = getIntent().getStringExtra("search_query");

        Log.d(TAG, "Category: " + categoryName);
        Log.d(TAG, "Type: " + categoryType);
        Log.d(TAG, "Filter: " + filterValue);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tvTitle = findViewById(R.id.tvTitle);
        tvExerciseCount = findViewById(R.id.tvExerciseCount);
        tvEmpty = findViewById(R.id.tvEmpty);
        searchView = findViewById(R.id.searchView);
        rvExercises = findViewById(R.id.rvExercises);

        if (searchQuery != null) {
            tvTitle.setText("Search Results");
        } else if (categoryName != null) {
            tvTitle.setText(categoryName + " Exercises");
        } else {
            tvTitle.setText("All Exercises");
        }

        // إظهار Loading
        tvExerciseCount.setText("Loading...");
    }

    private void setupRecyclerView() {
        adapter = new ExerciseAdapter(this);
        rvExercises.setLayoutManager(new LinearLayoutManager(this));
        rvExercises.setAdapter(adapter);
    }

    private void loadExercises() {

        db.getAllExercisesFromFirebase(exercises -> {
            Log.d(TAG, "Loaded from Firebase: " + exercises.size() + " exercises");

            runOnUiThread(() -> {
                if (exercises.isEmpty()) {
                    Toast.makeText(this,
                            "No exercises found. Please download exercises first.",
                            Toast.LENGTH_LONG).show();
                    allExercises = new ArrayList<>();
                    updateExerciseCount(0);
                    return;
                }

                List<Exercise> filtered;

                if (searchQuery != null) {

                    filtered = searchExercises(exercises, searchQuery);
                } else if (categoryType != null && filterValue != null) {

                    filtered = getExercisesByCategory(exercises, categoryType, filterValue);
                } else {

                    filtered = exercises;
                }

                Log.d(TAG, "Filtered exercises: " + filtered.size());

                allExercises = exercises;
                adapter.setExercises(filtered);
                updateExerciseCount(filtered.size());
            });
        });
    }

    private List<Exercise> getExercisesByCategory(List<Exercise> allExercisesList,
                                                  String type, String value) {
        List<Exercise> filtered = new ArrayList<>();

        for (Exercise exercise : allExercisesList) {
            boolean matches = false;

            if (type.equals("body_part")) {
                String category = exercise.getCategory();

                if (category != null) {

                    if (category.equalsIgnoreCase(value)) {
                        matches = true;
                    }

                    else if (category.toLowerCase().contains(value.toLowerCase())) {
                        matches = true;
                    }

                    else if (value.toLowerCase().contains(category.toLowerCase())) {
                        matches = true;
                    }
                }

                if (!matches) {
                    String muscles = exercise.getMuscles();
                    if (muscles != null) {
                        String musclesLower = muscles.toLowerCase();
                        String filterLower = value.toLowerCase();

                        if (value.equalsIgnoreCase("Arms")) {
                            if (musclesLower.contains("biceps") ||
                                    musclesLower.contains("triceps") ||
                                    musclesLower.contains("forearm") ||
                                    musclesLower.contains("arm")) {
                                matches = true;
                            }
                        } else if (value.equalsIgnoreCase("Legs")) {
                            if (musclesLower.contains("quad") ||
                                    musclesLower.contains("hamstring") ||
                                    musclesLower.contains("glute") ||
                                    musclesLower.contains("leg") ||
                                    musclesLower.contains("thigh")) {
                                matches = true;
                            }
                        } else if (value.equalsIgnoreCase("Chest")) {
                            if (musclesLower.contains("pectoral") ||
                                    musclesLower.contains("chest")) {
                                matches = true;
                            }
                        } else if (value.equalsIgnoreCase("Back")) {
                            if (musclesLower.contains("lat") ||
                                    musclesLower.contains("trap") ||
                                    musclesLower.contains("back") ||
                                    musclesLower.contains("rhomboid")) {
                                matches = true;
                            }
                        } else if (value.equalsIgnoreCase("Shoulders")) {
                            if (musclesLower.contains("deltoid") ||
                                    musclesLower.contains("shoulder")) {
                                matches = true;
                            }
                        } else if (value.equalsIgnoreCase("Abs")) {
                            if (musclesLower.contains("abs") ||
                                    musclesLower.contains("core") ||
                                    musclesLower.contains("oblique") ||
                                    musclesLower.contains("abdominal")) {
                                matches = true;
                            }
                        } else if (value.equalsIgnoreCase("Calves")) {
                            if (musclesLower.contains("calf") ||
                                    musclesLower.contains("gastrocnemius") ||
                                    musclesLower.contains("soleus")) {
                                matches = true;
                            }
                        } else if (value.equalsIgnoreCase("Cardio")) {
                            if (musclesLower.contains("cardio") ||
                                    musclesLower.contains("aerobic")) {
                                matches = true;
                            }
                        }
                    }
                }
            }

            if (matches) {
                filtered.add(exercise);
                Log.d(TAG, "Matched: " + exercise.getName() +
                        " (Category: " + exercise.getCategory() +
                        ", Muscles: " + exercise.getMuscles() + ")");
            }
        }

        return filtered;
    }

    private List<Exercise> searchExercises(List<Exercise> allExercisesList, String query) {
        List<Exercise> filtered = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (Exercise exercise : allExercisesList) {
            if (exercise.getName().toLowerCase().contains(lowerQuery) ||
                    (exercise.getDescription() != null &&
                            exercise.getDescription().toLowerCase().contains(lowerQuery)) ||
                    (exercise.getCategory() != null &&
                            exercise.getCategory().toLowerCase().contains(lowerQuery)) ||
                    (exercise.getMuscles() != null &&
                            exercise.getMuscles().toLowerCase().contains(lowerQuery)) ||
                    (exercise.getEquipment() != null &&
                            exercise.getEquipment().toLowerCase().contains(lowerQuery))) {
                filtered.add(exercise);
            }
        }

        return filtered;
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterExercises(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterExercises(newText);
                return true;
            }
        });
    }

    private void filterExercises(String query) {
        if (query == null || query.trim().isEmpty()) {

            if (categoryType != null && filterValue != null) {
                List<Exercise> filtered = getExercisesByCategory(allExercises, categoryType, filterValue);
                adapter.setExercises(filtered);
                updateExerciseCount(filtered.size());
            } else {
                adapter.setExercises(allExercises);
                updateExerciseCount(allExercises.size());
            }
            return;
        }

        List<Exercise> filtered = searchExercises(allExercises, query);
        adapter.setExercises(filtered);
        updateExerciseCount(filtered.size());
    }

    private void updateExerciseCount(int count) {
        if (count == 0) {
            tvExerciseCount.setText("No exercises found");
            tvEmpty.setVisibility(android.view.View.VISIBLE);
            rvExercises.setVisibility(android.view.View.GONE);
        } else {
            tvExerciseCount.setText(count + " exercise" + (count > 1 ? "s" : "") + " found");
            tvEmpty.setVisibility(android.view.View.GONE);
            rvExercises.setVisibility(android.view.View.VISIBLE);
        }
    }

    private void setClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }
}