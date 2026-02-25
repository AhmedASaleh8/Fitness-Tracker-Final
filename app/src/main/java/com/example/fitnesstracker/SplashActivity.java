package com.example.fitnesstracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnesstracker.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private Button btnGetStarted;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        session = new SessionManager(this);

        initViews();

        checkSession();

        setClickListeners();
    }

    private void initViews() {
        btnGetStarted = findViewById(R.id.btnGetStarted);
    }

    private void checkSession() {
        if (session.isLoggedIn()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    goToMainActivity();
                }
            }, 1500);

            btnGetStarted.setVisibility(View.GONE);
        }
    }

    private void setClickListeners() {
        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}