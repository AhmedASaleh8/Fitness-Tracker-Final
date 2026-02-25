package com.example.fitnesstracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnesstracker.database.DatabaseHelper;
import com.example.fitnesstracker.models.Workout;
import com.example.fitnesstracker.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class WorkoutSessionActivity extends AppCompatActivity {

    private ImageView btnBack, ivExerciseImage;
    private TextView tvTitle, tvCounter, tvExerciseName, tvTimer;
    private Button btnStartPause, btnReset, btnNext, btnFinish;

    private CountDownTimer timer;
    private boolean isRunning = false;

    private String sessionType = "arm";
    private String sessionTitle = "Workout Session";

    private final String[] armList  = {"Push-Ups", "Diamond Push-Ups", "Triceps Dips"};
    private final String[] legList  = {"Squats", "Lunges", "Calf Raises"};
    private final String[] coreList = {"Sit-Ups", "Plank", "Bicycle Crunches"};
    private String[] list = armList;

    private final int[] armDrawables  = {
            R.drawable.pushup,
            R.drawable.diamond_pushup,
            R.drawable.dips
    };
    private final int[] legDrawables  = {
            R.drawable.squat,
            R.drawable.lunges,
            R.drawable.calf_raise
    };
    private final int[] coreDrawables = {
            R.drawable.situp,
            R.drawable.plank,
            R.drawable.bicycle_crunches
    };

    private int totalExercises = 1;
    private int exerciseIndex = 0;

    private long timePerExerciseMs = 3 * 60_000L;
    private long timeLeftMs = timePerExerciseMs;

    private boolean savedHistory = false;

    private DatabaseHelper db;
    private SessionManager session;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_session);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        readIntent();
        mapViews();
        preparePlanFromPreferences();
        bindUI();

        btnBack.setOnClickListener(v -> finishSafely());
        btnStartPause.setOnClickListener(v -> { if (isRunning) pauseTimer(); else startTimer(); });
        btnReset.setOnClickListener(v -> resetTimer());
        btnNext.setOnClickListener(v -> goNextExercise());
        btnFinish.setOnClickListener(v -> showFinishDialog());
    }

    private void readIntent() {
        Intent it = getIntent();
        if (it != null) {
            String t = it.getStringExtra("session_type");
            String title = it.getStringExtra("session_title");
            if (t != null) sessionType = t;
            if (title != null) sessionTitle = title;
        }
        switch (sessionType) {
            case "leg":  list = legList;  break;
            case "core": list = coreList; break;
            default:     list = armList;  break;
        }
    }

    private void mapViews() {
        btnBack = findViewById(R.id.btn_back_session);
        ivExerciseImage = findViewById(R.id.iv_exercise_image);
        tvTitle = findViewById(R.id.tv_session_title);
        tvCounter = findViewById(R.id.tv_ex_counter);
        tvExerciseName = findViewById(R.id.tv_exercise_name);
        tvTimer = findViewById(R.id.tv_timer);
        btnStartPause = findViewById(R.id.btn_start_pause);
        btnReset = findViewById(R.id.btn_reset);
        btnNext = findViewById(R.id.btn_next);
        btnFinish = findViewById(R.id.btn_finish);

        tvTitle.setText(sessionTitle);
    }

    private void preparePlanFromPreferences() {
        SharedPreferences wp = getSharedPreferences("workout_prefs", MODE_PRIVATE);

        int defCount = 1, defMins = 3;
        int count, minutes;

        if ("leg".equals(sessionType)) {
            count   = wp.getInt("leg_exercises",  defCount);
            minutes = wp.getInt("leg_minutes",    defMins);
        } else if ("core".equals(sessionType)) {
            count   = wp.getInt("core_exercises", defCount);
            minutes = wp.getInt("core_minutes",   defMins);
        } else {
            count   = wp.getInt("arm_exercises",  defCount);
            minutes = wp.getInt("arm_minutes",    defMins);
        }

        totalExercises = Math.max(1, Math.min(3, count));
        if (minutes != 1 && minutes != 3 && minutes != 5) minutes = defMins;

        timePerExerciseMs = minutes * 60_000L;
        timeLeftMs = timePerExerciseMs;
    }

    private void bindUI() {
        tvCounter.setText("Exercise " + (exerciseIndex + 1) + " of " + totalExercises);
        tvExerciseName.setText(list[exerciseIndex]);
        tvTimer.setText(formatTime(timeLeftMs));
        btnStartPause.setText(isRunning ? "Pause" : "Start");

        ivExerciseImage.setImageResource(getCurrentDrawable(exerciseIndex));
    }

    private int getCurrentDrawable(int idx) {
        int safe = Math.max(0, Math.min(idx, 2));
        switch (sessionType) {
            case "leg":
                return legDrawables[safe];
            case "core":
                return coreDrawables[safe];
            default:
                return armDrawables[safe];
        }
    }

    private void startTimer() {
        if (isRunning) return;
        isRunning = true;
        btnStartPause.setText("Pause");

        timer = new CountDownTimer(timeLeftMs, 1000) {
            @Override public void onTick(long ms) {
                timeLeftMs = ms;
                tvTimer.setText(formatTime(timeLeftMs));
            }
            @Override public void onFinish() {
                isRunning = false;
                timeLeftMs = 0;
                tvTimer.setText(formatTime(timeLeftMs));
                Toast.makeText(WorkoutSessionActivity.this, "Exercise done!", Toast.LENGTH_SHORT).show();
                goNextExercise();
            }
        }.start();
    }

    private void pauseTimer() {
        if (!isRunning) return;
        isRunning = false;
        if (timer != null) timer.cancel();
        btnStartPause.setText("Start");
        Toast.makeText(this, "Paused", Toast.LENGTH_SHORT).show();
    }

    private void resetTimer() {
        if (timer != null) timer.cancel();
        isRunning = false;
        timeLeftMs = timePerExerciseMs;
        tvTimer.setText(formatTime(timeLeftMs));
        btnStartPause.setText("Start");
    }

    private void goNextExercise() {
        if (timer != null) timer.cancel();
        isRunning = false;

        if (exerciseIndex < totalExercises - 1) {
            exerciseIndex++;
            timeLeftMs = timePerExerciseMs;
            bindUI();
        } else {
            showFinishDialog();
        }
    }

    private void showFinishDialog() {
        if (timer != null) timer.cancel();
        isRunning = false;

        appendHistoryOnce();

        new AlertDialog.Builder(this)
                .setTitle("Workout Finished")
                .setMessage("Great job!\nYou completed " + totalExercises + " exercise(s).")
                .setPositiveButton("Back to Home", (d, w) -> {
                    Intent i = new Intent(this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(i);
                    finish();
                })
                .setNegativeButton("Close", (d, w) -> finish())
                .show();
    }

    private void appendHistoryOnce() {
        if (savedHistory) return;
        savedHistory = true;

        int minutesPerExercise = (int) (timePerExerciseMs / 60000L);
        String stamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

        Workout workout = new Workout();
        workout.setUserId(session.getUserId());
        workout.setType(sessionType);
        workout.setDuration(minutesPerExercise);
        workout.setDate(stamp);

        db.addWorkout(workout);

        if (userId != null) {
            Map<String, Object> workoutData = new HashMap<>();
            workoutData.put("type", sessionType);
            workoutData.put("exercisesCount", totalExercises);
            workoutData.put("durationMinutes", minutesPerExercise);
            workoutData.put("date", stamp);
            workoutData.put("timestamp", System.currentTimeMillis());

            firestore.collection("users")
                    .document(userId)
                    .collection("workouts")
                    .add(workoutData)
                    .addOnSuccessListener(documentReference -> {

                    })
                    .addOnFailureListener(e -> {
                    });
        }

        Toast.makeText(this, "Workout saved!", Toast.LENGTH_SHORT).show();
    }

    private void finishSafely() {
        if (isRunning && timeLeftMs > 0) {
            pauseTimer();
            Toast.makeText(this, "Paused. Press back again to exit.", Toast.LENGTH_SHORT).show();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (timer != null) timer.cancel();
        super.onDestroy();
    }

    private String formatTime(long ms) {
        long totalSec = ms / 1000;
        long m = totalSec / 60;
        long s = totalSec % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", m, s);
    }
}