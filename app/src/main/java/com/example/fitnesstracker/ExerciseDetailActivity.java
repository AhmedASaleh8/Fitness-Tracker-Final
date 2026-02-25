package com.example.fitnesstracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnesstracker.database.DatabaseHelper;
import com.example.fitnesstracker.models.Exercise;

public class ExerciseDetailActivity extends AppCompatActivity {

    private ImageView btnBack, ivExerciseMain;
    private TextView tvTitle, tvExercisesValue, tvMinutesValue;
    private TextView tvDiffStars, tvDiffLabel, tvDescription;
    private Button btnStartWorkout;

    private DatabaseHelper db;
    private Exercise exercise;
    private SharedPreferences prefs;

    private static final String SP_NAME = "workout_prefs";
    private static final String KEY_PREFIX = "exercise_";

    // Options
    private final int[] EX_OPTS = {1, 2, 3};           // Number of sets
    private final int[] MIN_OPTS = {1, 3, 5};          // Minutes per set
    private final String[] DIFF_LABELS = {"Easy", "Medium", "Hard"};
    private final String[] DIFF_STARS = {"★☆☆", "★★☆", "★★★"};

    private int exIdx = 1, minIdx = 1, diffIdx = 1;    // Default: 2, 3, Medium

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_detail);

        db = new DatabaseHelper(this);
        prefs = getSharedPreferences(SP_NAME, MODE_PRIVATE);

        initViews();
        getExerciseFromIntent();
        loadPreferences();
        displayExerciseDetails();
        setClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        ivExerciseMain = findViewById(R.id.iv_exercise_main);
        tvTitle = findViewById(R.id.tv_title);
        tvExercisesValue = findViewById(R.id.tv_exercises_value);
        tvMinutesValue = findViewById(R.id.tv_minutes_value);
        tvDiffStars = findViewById(R.id.tv_diff_stars);
        tvDiffLabel = findViewById(R.id.tv_diff_label);
        tvDescription = findViewById(R.id.tv_description);
        btnStartWorkout = findViewById(R.id.btn_start_workout);
    }

    private void getExerciseFromIntent() {
        exercise = new Exercise();
        exercise.setId(getIntent().getIntExtra("exercise_id", 0));
        exercise.setName(getIntent().getStringExtra("exercise_name"));
        exercise.setDescription(getIntent().getStringExtra("exercise_description"));
        exercise.setDifficulty(getIntent().getIntExtra("exercise_difficulty", 2));
        exercise.setCategory(getIntent().getStringExtra("exercise_category"));
        exercise.setMuscles(getIntent().getStringExtra("exercise_muscles"));
        exercise.setEquipment(getIntent().getStringExtra("exercise_equipment"));
        exercise.setType(getIntent().getStringExtra("exercise_type"));
        exercise.setImageResource(getIntent().getStringExtra("exercise_image"));
    }

    private void loadPreferences() {
        String key = exercise.getName() != null ? exercise.getName() : "default";

        int ex = prefs.getInt(KEY_PREFIX + key + "_sets", EX_OPTS[exIdx]);
        int mins = prefs.getInt(KEY_PREFIX + key + "_minutes", MIN_OPTS[minIdx]);
        int diff = prefs.getInt(KEY_PREFIX + key + "_difficulty", diffIdx);

        exIdx = indexOf(EX_OPTS, ex, exIdx);
        minIdx = indexOf(MIN_OPTS, mins, minIdx);
        diffIdx = clamp(diff, 0, 2);
    }

    private void displayExerciseDetails() {
        if (exercise == null || exercise.getName() == null) {
            tvTitle.setText("Exercise Details");
            tvDescription.setText("No exercise data available");
            return;
        }

        // العنوان
        tvTitle.setText(exercise.getName());

        // الصورة - حسب الفئة
        loadExerciseImage(ivExerciseMain, exercise.getCategory());

        bindUI();

        String description = buildDescription();
        tvDescription.setText(description);
    }

    /**
     * تحميل الصورة حسب الفئة - نفس المنطق من ExerciseAdapter
     */
    private void loadExerciseImage(ImageView imageView, String category) {
        int imageResource = getImageForCategory(category);
        imageView.setImageResource(imageResource);
    }

    /**
     * اختيار الصورة المناسبة حسب الفئة
     */
    private int getImageForCategory(String category) {
        if (category == null) return R.drawable.pushup;

        String categoryLower = category.toLowerCase();

        // Arms - تمارين الذراعين
        if (categoryLower.contains("arm")) {
            int random = (int) (Math.random() * 3);
            switch (random) {
                case 0: return R.drawable.pushup;
                case 1: return R.drawable.diamond_pushup;
                case 2: return R.drawable.dips;
                default: return R.drawable.pushup;
            }
        }

        // Legs - تمارين الأرجل
        else if (categoryLower.contains("leg") || categoryLower.contains("calves")) {
            int random = (int) (Math.random() * 3);
            switch (random) {
                case 0: return R.drawable.squat;
                case 1: return R.drawable.lunges;
                case 2: return R.drawable.calf_raise;
                default: return R.drawable.squat;
            }
        }

        // Chest - تمارين الصدر
        else if (categoryLower.contains("chest")) {
            int random = (int) (Math.random() * 2);
            switch (random) {
                case 0: return R.drawable.pushup;
                case 1: return R.drawable.diamond_pushup;
                default: return R.drawable.pushup;
            }
        }

        // Back - تمارين الظهر
        else if (categoryLower.contains("back")) {
            return R.drawable.pushup;
        }

        // Shoulders - تمارين الأكتاف
        else if (categoryLower.contains("shoulder")) {
            return R.drawable.pushup;
        }

        // Abs - تمارين البطن
        else if (categoryLower.contains("abs") || categoryLower.contains("core")) {
            int random = (int) (Math.random() * 4);
            switch (random) {
                case 0: return R.drawable.plank;
                case 1: return R.drawable.bicycle_crunches;
                case 2: return R.drawable.situp;
                case 3: return R.drawable.sixpack;
                default: return R.drawable.plank;
            }
        }

        // Cardio - تمارين الكارديو
        else if (categoryLower.contains("cardio")) {
            return R.drawable.pushup;
        }

        // افتراضي
        return R.drawable.pushup;
    }

    private void bindUI() {
        tvExercisesValue.setText(String.valueOf(EX_OPTS[exIdx]));
        tvMinutesValue.setText(String.valueOf(MIN_OPTS[minIdx]));
        tvDiffStars.setText(DIFF_STARS[diffIdx]);
        tvDiffLabel.setText(DIFF_LABELS[diffIdx]);
    }

    private void saveAndBind() {
        String key = exercise.getName() != null ? exercise.getName() : "default";

        prefs.edit()
                .putInt(KEY_PREFIX + key + "_sets", EX_OPTS[exIdx])
                .putInt(KEY_PREFIX + key + "_minutes", MIN_OPTS[minIdx])
                .putInt(KEY_PREFIX + key + "_difficulty", diffIdx)
                .apply();

        bindUI();
    }

    private String buildDescription() {
        StringBuilder desc = new StringBuilder();

        if (exercise.getDescription() != null && !exercise.getDescription().isEmpty()) {
            desc.append(exercise.getDescription()).append("\n\n");
        }

        if (exercise.getMuscles() != null && !exercise.getMuscles().isEmpty()) {
            desc.append("💪 Target Muscles:\n");
            desc.append(exercise.getMuscles()).append("\n\n");
        }

        if (exercise.getEquipment() != null && !exercise.getEquipment().isEmpty()) {
            desc.append("🏋️ Equipment:\n");
            desc.append(exercise.getEquipment()).append("\n\n");
        }

        if (exercise.getCategory() != null && !exercise.getCategory().isEmpty()) {
            desc.append("📂 Category:\n");
            desc.append(exercise.getCategory());
        }

        if (desc.length() == 0) {
            desc.append("No detailed information available for this exercise.");
        }

        return desc.toString().trim();
    }

    private void setClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        tvExercisesValue.setOnClickListener(v -> {
            exIdx = (exIdx + 1) % EX_OPTS.length;
            saveAndBind();
            Toast.makeText(this, "Sets: " + EX_OPTS[exIdx], Toast.LENGTH_SHORT).show();
        });

        tvMinutesValue.setOnClickListener(v -> {
            minIdx = (minIdx + 1) % MIN_OPTS.length;
            saveAndBind();
            Toast.makeText(this, "Minutes: " + MIN_OPTS[minIdx], Toast.LENGTH_SHORT).show();
        });

        android.view.View.OnClickListener diffClick = v -> {
            diffIdx = (diffIdx + 1) % 3;
            saveAndBind();
            Toast.makeText(this, "Difficulty: " + DIFF_LABELS[diffIdx], Toast.LENGTH_SHORT).show();
        };
        tvDiffStars.setOnClickListener(diffClick);
        tvDiffLabel.setOnClickListener(diffClick);

        btnStartWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(this, WorkoutSessionActivity.class);
            intent.putExtra("session_type", exercise.getType());
            intent.putExtra("session_title", exercise.getName());
            intent.putExtra("num_exercises", EX_OPTS[exIdx]);
            intent.putExtra("minutes_per_exercise", MIN_OPTS[minIdx]);
            intent.putExtra("difficulty", DIFF_LABELS[diffIdx]);
            startActivity(intent);
        });
    }

    private int indexOf(int[] arr, int val, int defIdx) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == val) return i;
        }
        return defIdx;
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}