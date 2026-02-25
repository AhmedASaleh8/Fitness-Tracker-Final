package com.example.fitnesstracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnesstracker.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView btn_back, ivAvatar;
    private TextView tvDisplayName, tvBMI, tvBMIClass;
    private Button btnEditName, btnSaveMetrics, btnSaveGoal, btnStartArms, btnStartLegs, btnStartCore;
    private EditText etHeight, etWeight, etTargetWeight;
    private RadioGroup rgGoal;
    private RadioButton rbGain, rbLose, rbMaintain;
    private Spinner spWeeklyDays;

    private SessionManager session;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    private final DecimalFormat df1 = new DecimalFormat("#0.0");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // تهيئة Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        session = new SessionManager(this);

        // الحصول على user ID
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        } else {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupWeeklyDaysSpinner();
        loadUserDataFromFirestore();
        setListeners();
    }

    private void initViews() {
        btn_back = findViewById(R.id.btn_back);
        ivAvatar = findViewById(R.id.ivAvatar);
        tvDisplayName = findViewById(R.id.tvDisplayName);

        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        tvBMI = findViewById(R.id.tvBMI);
        tvBMIClass = findViewById(R.id.tvBMIClass);
        btnSaveMetrics = findViewById(R.id.btnSaveMetrics);

        rgGoal = findViewById(R.id.rgGoal);
        rbGain = findViewById(R.id.rbGain);
        rbLose = findViewById(R.id.rbLose);
        rbMaintain = findViewById(R.id.rbMaintain);
        etTargetWeight = findViewById(R.id.etTargetWeight);
        spWeeklyDays = findViewById(R.id.spWeeklyDays);
        btnSaveGoal = findViewById(R.id.btnSaveGoal);

        btnEditName = findViewById(R.id.btnEditName);
        btnStartArms = findViewById(R.id.btnStartArms);
        btnStartLegs = findViewById(R.id.btnStartLegs);
        btnStartCore = findViewById(R.id.btnStartCore);
    }

    private void setupWeeklyDaysSpinner() {
        String[] days = new String[]{"1","2","3","4","5","6"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, days);
        spWeeklyDays.setAdapter(adapter);
    }

    private void loadUserDataFromFirestore() {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        String name = documentSnapshot.getString("name");
                        tvDisplayName.setText(name != null && !name.isEmpty() ? name : "Athlete");

                        Double height = documentSnapshot.getDouble("height");
                        Double weight = documentSnapshot.getDouble("weight");

                        if (height != null && height > 0) {
                            etHeight.setText(trimZero(height));
                        }
                        if (weight != null && weight > 0) {
                            etWeight.setText(trimZero(weight));
                        }

                        updateBMIViews(height != null ? height : 0, weight != null ? weight : 0);

                        // عرض الهدف
                        String goal = documentSnapshot.getString("goal");
                        if (goal == null) goal = "maintain";
                        switch (goal) {
                            case "gain": rbGain.setChecked(true); break;
                            case "lose": rbLose.setChecked(true); break;
                            default: rbMaintain.setChecked(true);
                        }

                        Long weekly = documentSnapshot.getLong("weeklyGoalDays");
                        int weeklyDays = weekly != null ? weekly.intValue() : 3;
                        int pos = Math.min(Math.max(weeklyDays, 1), 6) - 1;
                        spWeeklyDays.setSelection(pos);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setListeners() {
        btn_back.setOnClickListener(v -> finish());

        btnEditName.setOnClickListener(v -> showEditNameDialog());

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                double h = parseDouble(etHeight.getText().toString().trim(), 0);
                double w = parseDouble(etWeight.getText().toString().trim(), 0);
                updateBMIViews(h, w);
            }
        };
        etHeight.addTextChangedListener(watcher);
        etWeight.addTextChangedListener(watcher);

        btnSaveMetrics.setOnClickListener(v -> saveMetrics());

        btnSaveGoal.setOnClickListener(v -> saveGoal());

        btnStartArms.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExerciseListActivity.class);
            intent.putExtra("category_name", "Arms");
            intent.putExtra("category_type", "body_part");
            intent.putExtra("filter_value", "Arms");
            startActivity(intent);
        });

        btnStartLegs.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExerciseListActivity.class);
            intent.putExtra("category_name", "Legs");
            intent.putExtra("category_type", "body_part");
            intent.putExtra("filter_value", "Legs");
            startActivity(intent);
        });

        btnStartCore.setOnClickListener(v -> {
            Intent intent = new Intent(this, ExerciseListActivity.class);
            intent.putExtra("category_name", "Abs");
            intent.putExtra("category_type", "body_part");
            intent.putExtra("filter_value", "Abs");
            startActivity(intent);
        });
    }

    private void showEditNameDialog() {
        final EditText input = new EditText(this);
        input.setHint("Enter your name");
        input.setText(tvDisplayName.getText().toString());
        input.setPadding(24, 24, 24, 24);

        new AlertDialog.Builder(this)
                .setTitle("Edit Name")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) name = "Athlete";

                    final String finalName = name;

                    // حفظ في Firestore
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("name", finalName);

                    db.collection("users").document(userId)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                tvDisplayName.setText(finalName);
                                session.createLoginSession(0, finalName, session.getUserEmail());
                                Toast.makeText(ProfileActivity.this, "Name updated", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ProfileActivity.this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveMetrics() {
        double h = parseDouble(etHeight.getText().toString().trim(), 0);
        double w = parseDouble(etWeight.getText().toString().trim(), 0);

        if (h <= 0 || w <= 0) {
            Toast.makeText(this, "Please enter valid height and weight", Toast.LENGTH_SHORT).show();
            return;
        }

        double bmi = calcBMI(h, w);

        // حفظ في Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("height", h);
        updates.put("weight", w);
        updates.put("bmi", bmi);

        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Metrics saved", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveGoal() {
        String goal = "maintain";
        int checked = rgGoal.getCheckedRadioButtonId();
        if (checked == R.id.rbGain) goal = "gain";
        else if (checked == R.id.rbLose) goal = "lose";

        int weekly = Integer.parseInt(spWeeklyDays.getSelectedItem().toString());

        // حفظ في Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("goal", goal);
        updates.put("weeklyGoalDays", weekly);

        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Goal saved", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateBMIViews(double heightCm, double weightKg) {
        if (heightCm <= 0 || weightKg <= 0) {
            tvBMI.setText("BMI: --");
            tvBMIClass.setText("  (—)");
            return;
        }
        double bmi = calcBMI(heightCm, weightKg);
        String cls = bmiClass(bmi);
        tvBMI.setText("BMI: " + df1.format(bmi));
        tvBMIClass.setText("  (" + cls + ")");
    }

    private double calcBMI(double heightCm, double weightKg) {
        double m = heightCm / 100.0;
        if (m <= 0) return 0;
        return weightKg / (m * m);
    }

    private String bmiClass(double bmi) {
        if (bmi <= 0) return "—";
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25) return "Normal";
        if (bmi < 30) return "Overweight";
        return "Obese";
    }

    private double parseDouble(String s, double def) {
        try {
            if (s == null || s.isEmpty()) return def;
            return Double.parseDouble(s);
        } catch (Exception e) {
            return def;
        }
    }

    private String trimZero(double v) {
        if (Math.floor(v) == v) return String.valueOf((long) v);
        return df1.format(v);
    }
}