package com.example.fitnesstracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnesstracker.adapters.CategoryAdapter;
import com.example.fitnesstracker.database.DatabaseHelper;
import com.example.fitnesstracker.models.Category;
import com.example.fitnesstracker.models.Exercise;
import com.example.fitnesstracker.services.ExerciseService;
import com.example.fitnesstracker.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ImageView ivHistory;
    private TextView tvWelcome;
    private SearchView searchView;
    private Button btnUpdateExercises, btnViewAllCategories;
    private RecyclerView rvCategories;

    private LinearLayout navHome, navSettings, navProfile;

    private CategoryAdapter categoryAdapter;
    private DatabaseHelper db;
    private ExerciseService exerciseService;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        exerciseService = new ExerciseService(this);
        session = new SessionManager(this);

        initViews();
        setupCategoriesRecyclerView();
        loadUserName();
        loadPopularCategories();
        setClickListeners();
        checkIfExercisesDownloaded();
    }

    private void initViews() {
        ivHistory = findViewById(R.id.ivHistory);
        tvWelcome = findViewById(R.id.tvWelcome);
        searchView = findViewById(R.id.searchView);
        btnUpdateExercises = findViewById(R.id.btnUpdateExercises);
        btnViewAllCategories = findViewById(R.id.btnViewAllCategories);
        rvCategories = findViewById(R.id.rvCategories);

        navHome = findViewById(R.id.navHome);
        navSettings = findViewById(R.id.navSettings);
        navProfile = findViewById(R.id.navProfile);
    }

    private void setupCategoriesRecyclerView() {
        categoryAdapter = new CategoryAdapter(this);
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        rvCategories.setAdapter(categoryAdapter);

        categoryAdapter.setOnCategoryClickListener(category -> {
            Intent intent = new Intent(MainActivity.this, ExerciseListActivity.class);
            intent.putExtra("category_name", category.getName());
            intent.putExtra("category_type", category.getType());
            intent.putExtra("filter_value", category.getFilterValue());
            startActivity(intent);
        });
    }

    private void loadUserName() {
        String userName = session.getUserName();

        if (userName != null && !userName.isEmpty()) {

            userName = userName.substring(0, 1).toUpperCase()
                    + userName.substring(1).toLowerCase();

            tvWelcome.setText("Welcome, " + userName);
        } else {
            tvWelcome.setText("Welcome, User");
        }
    }

    private void loadPopularCategories() {

        db.getAllExercisesFromFirebase(exercises -> {

            List<Category> popularCategories = new ArrayList<>();
            popularCategories.add(createCategory("Arms", "💪", "Biceps, Triceps, Forearms", "body_part", "Arms", exercises));
            popularCategories.add(createCategory("Legs", "🦵", "Quads, Hamstrings, Glutes", "body_part", "Legs", exercises));
            popularCategories.add(createCategory("Chest", "🫁", "Pectorals, Upper/Lower", "body_part", "Chest", exercises));
            popularCategories.add(createCategory("Abs", "🔥", "Core, Obliques", "body_part", "Abs", exercises));

            runOnUiThread(() -> categoryAdapter.setCategories(popularCategories));
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
            if (type.equals("body_part")) {
                if (exercise.getCategory() != null &&
                        exercise.getCategory().contains(filterValue)) {
                    count++;
                }
            }
        }
        return count;
    }

    private void setClickListeners() {

        ivHistory.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, HistoryActivity.class)));

        btnUpdateExercises.setOnClickListener(v -> updateExercisesFromAPI());

        btnViewAllCategories.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CategoriesActivity.class)));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                openSearchResults(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        navSettings.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SettingsActivity.class)));

        navProfile.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
    }

    private void openSearchResults(String query) {
        Intent intent = new Intent(this, ExerciseListActivity.class);
        intent.putExtra("search_query", query);
        startActivity(intent);
    }

    private void updateExercisesFromAPI() {

        btnUpdateExercises.setEnabled(false);
        btnUpdateExercises.setText("Downloading...");

        exerciseService.fetchAndSaveExercises(new ExerciseService.OnExercisesLoadedListener() {

            @Override
            public void onSuccess(int count) {
                Toast.makeText(MainActivity.this,
                        "✅ Downloaded " + count + " exercises!",
                        Toast.LENGTH_LONG).show();

                btnUpdateExercises.setVisibility(View.GONE);
                loadPopularCategories();
            }

            @Override
            public void onError(String error) {
                btnUpdateExercises.setEnabled(true);
                btnUpdateExercises.setText("🔄 Update Exercises");

                Toast.makeText(MainActivity.this,
                        "❌ Error: " + error,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkIfExercisesDownloaded() {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            btnUpdateExercises.setVisibility(View.VISIBLE);
            return;
        }

        String userId = currentUser.getUid();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("users")
                .document(userId)
                .collection("exercises")
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean hasExercises = !querySnapshot.isEmpty();

                    if (hasExercises) {
                        btnUpdateExercises.setVisibility(View.GONE);
                    } else {
                        btnUpdateExercises.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e ->
                        btnUpdateExercises.setVisibility(View.VISIBLE));
    }
}