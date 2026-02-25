package com.example.fitnesstracker;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.fitnesstracker.api.AppController;


public class TestApiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        testAPI();
    }

    private void testAPI() {
        // جلب عدد التمارين المتاحة من wger API
        String url = AppController.BASE_URL
                + "exercise/?format=json&language=2&limit=1";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        int count = response.getInt("count");

                        Toast.makeText(this,
                                "✅ API Works!\n" +
                                        "Total: " + count + " exercises available",
                                Toast.LENGTH_LONG).show();

                    } catch (Exception e) {
                        Toast.makeText(this,
                                "Parse Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }

                    new android.os.Handler().postDelayed(
                            () -> finish(), 4000);
                },
                error -> {
                    String msg = "API Error!";
                    if (error.networkResponse != null) {
                        msg += " Code: " + error.networkResponse.statusCode;
                    } else if (error.getMessage() != null) {
                        msg += " " + error.getMessage();
                    } else {
                        msg += " No internet!";
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    finish();
                }
        );

        AppController.getInstance().addToRequestQueue(request);
    }
}