package com.example.fitnesstracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnesstracker.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegisterLink;

    private SessionManager session;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Log.d(TAG, "LoginActivity started");

        mAuth = FirebaseAuth.getInstance();
        session = new SessionManager(this);

        if (session.isLoggedIn()) {
            Log.d(TAG, "User already logged in, redirecting to MainActivity");
            goToMainActivity();
            return;
        }

        initViews();
        setClickListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
    }

    private void setClickListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Login button clicked");
                performLogin();
            }
        });

        tvRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Register link clicked");
                goToRegister();
            }
        });
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        Log.d(TAG, "Attempting login for: " + email);

        // التحقق من البيانات
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            Toast.makeText(this, "Password too short", Toast.LENGTH_SHORT).show();
            return;
        }

        loginWithFirebase(email, password);
    }

    private void loginWithFirebase(String email, String password) {
        Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Starting Firebase login");

        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("LOGIN");

                    if (task.isSuccessful()) {
                        Log.d(TAG, "Firebase login successful");

                        FirebaseUser firebaseUser = mAuth.getCurrentUser();

                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            String userName = firebaseUser.getEmail().split("@")[0];

                            Log.d(TAG, "User ID: " + userId + ", Username: " + userName);

                            session.createLoginSession(0, userName, email);

                            Toast.makeText(LoginActivity.this,
                                    "Welcome " + userName + "!",
                                    Toast.LENGTH_SHORT).show();

                            goToMainActivity();
                        }
                    } else {
                        Log.e(TAG, "Firebase login failed", task.getException());

                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Login failed";
                        Toast.makeText(LoginActivity.this,
                                "Error: " + errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void goToMainActivity() {
        Log.d(TAG, "Navigating to MainActivity");
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToRegister() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
        finish();
    }
}