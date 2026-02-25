package com.example.fitnesstracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.fitnesstracker.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private ImageView btn_back;

    private EditText etDisplayName;
    private Button btnSaveName, btnLogout, btnAbout;
    private SwitchCompat swDarkMode;
    private RadioGroup rgLanguage;
    private RadioButton rbArabic, rbEnglish;

    private SharedPreferences prefs;
    private SessionManager session;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        session = new SessionManager(this);

        // تهيئة Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        initViews();
        loadSavedSettings();
        setListeners();
    }

    private void initViews() {
        btn_back        = findViewById(R.id.btn_back);

        etDisplayName   = findViewById(R.id.etDisplayName);
        btnSaveName     = findViewById(R.id.btnSaveName);
        btnLogout       = findViewById(R.id.btnLogout);
        btnAbout        = findViewById(R.id.btnAbout);

        swDarkMode      = findViewById(R.id.swDarkMode);

        rgLanguage      = findViewById(R.id.rgLanguage);
        rbArabic        = findViewById(R.id.rbArabic);
        rbEnglish       = findViewById(R.id.rbEnglish);
    }

    private void loadSavedSettings() {
        if (userId != null) {
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            etDisplayName.setText(name != null ? name : "");
                        }
                    });
        }

        boolean dark = prefs.getBoolean("dark_mode", false);
        swDarkMode.setChecked(dark);
        AppCompatDelegate.setDefaultNightMode(
                dark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        String lang = prefs.getString("language", "ar");
        if ("ar".equals(lang)) rbArabic.setChecked(true); else rbEnglish.setChecked(true);
    }

    private void setListeners() {

        btn_back.setOnClickListener(v -> finish());

        btnSaveName.setOnClickListener(v -> {
            String name = etDisplayName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // حفظ في Firestore
            if (userId != null) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("name", name);

                db.collection("users").document(userId)
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            session.createLoginSession(0, name, session.getUserEmail());
                            Toast.makeText(this, "Name saved", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });

        swDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        rgLanguage.setOnCheckedChangeListener((group, checkedId) -> {
            String lang = (checkedId == R.id.rbArabic) ? "ar" : "en";
            prefs.edit().putString("language", lang).apply();
            Toast.makeText(this, "Language set: " + (lang.equals("ar") ? "Arabic" : "English"), Toast.LENGTH_SHORT).show();
        });

        btnAbout.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();

            session.logout();

            // الذهاب لصفحة Login
            Intent i = new Intent(SettingsActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();

            Toast.makeText(SettingsActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        });
    }
}