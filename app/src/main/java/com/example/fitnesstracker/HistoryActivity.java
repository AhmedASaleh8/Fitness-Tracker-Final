package com.example.fitnesstracker;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnesstracker.database.DatabaseHelper;
import com.example.fitnesstracker.models.Workout;
import com.example.fitnesstracker.utils.SessionManager;
import com.example.fitnesstracker.utils.ThreadUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ImageView btnBack;
    private ListView lvHistory;
    private TextView tvEmpty;
    private Button btnClear;

    private DatabaseHelper db;
    private SessionManager session;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private String userId;

    private HistoryAdapter adapter;
    private List<Workout> workoutList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        db = new DatabaseHelper(this);
        session = new SessionManager(this);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        btnBack = findViewById(R.id.btn_back);
        lvHistory = findViewById(R.id.lvHistory);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnClear = findViewById(R.id.btnClear);

        lvHistory.setEmptyView(tvEmpty);

        loadWorkoutsFromFirestore();

        btnBack.setOnClickListener(v -> finish());

        btnClear.setOnClickListener(v -> showClearConfirmation());

        lvHistory.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteConfirmation(position);
            return true;
        });
    }

    private void loadWorkoutsFromFirestore() {
        if (userId == null) {
            loadWorkoutsFromSQLite();
            return;
        }

        firestore.collection("users")
                .document(userId)
                .collection("workouts")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    workoutList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String type = document.getString("type");
                        Long durationMinutes = document.getLong("durationMinutes");
                        String date = document.getString("date");

                        if (type != null && durationMinutes != null && date != null) {

                            Workout workout = new Workout();
                            workout.setId(document.getId().hashCode());
                            workout.setUserId(0);
                            workout.setType(type);
                            workout.setDuration(durationMinutes.intValue());
                            workout.setDate(date);

                            workoutList.add(workout);
                        }
                    }

                    adapter = new HistoryAdapter(HistoryActivity.this, workoutList);
                    lvHistory.setAdapter(adapter);

                    if (workoutList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Loading from local storage", Toast.LENGTH_SHORT).show();
                    loadWorkoutsFromSQLite();
                });
    }

    private void loadWorkoutsFromSQLite() {
        ThreadUtils.runInBackground(() -> {
            List<Workout> workouts = db.getAllWorkouts();

            ThreadUtils.runOnMainThread(() -> {
                workoutList = workouts;
                adapter = new HistoryAdapter(HistoryActivity.this, workoutList);
                lvHistory.setAdapter(adapter);

                if (workoutList.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                }
            });
        });
    }

    private void showDeleteConfirmation(int position) {
        Workout workout = workoutList.get(position);

        new AlertDialog.Builder(this)
                .setTitle("Delete Workout")
                .setMessage("Are you sure you want to delete this workout?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteWorkout(workout, position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteWorkout(Workout workout, int position) {
        // حذف من Firestore
        if (userId != null) {
            firestore.collection("users")
                    .document(userId)
                    .collection("workouts")
                    .whereEqualTo("date", workout.getDate())
                    .whereEqualTo("type", workout.getType())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            document.getReference().delete();
                        }
                    });
        }

        workoutList.remove(position);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Workout deleted", Toast.LENGTH_SHORT).show();

        if (workoutList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void showClearConfirmation() {
        if (workoutList == null || workoutList.isEmpty()) {
            Toast.makeText(this, "No workouts to clear", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Clear All History")
                .setMessage("Are you sure you want to delete all your workouts?")
                .setPositiveButton("Clear All", (dialog, which) -> {
                    clearAllWorkouts();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearAllWorkouts() {
        // حذف من Firestore
        if (userId != null) {
            firestore.collection("users")
                    .document(userId)
                    .collection("workouts")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            document.getReference().delete();
                        }
                    });
        }

        workoutList.clear();
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(View.VISIBLE);
        Toast.makeText(this, "All workouts cleared", Toast.LENGTH_SHORT).show();
    }

    private static class HistoryAdapter extends ArrayAdapter<Workout> {
        private final LayoutInflater inflater;

        HistoryAdapter(Context ctx, List<Workout> data) {
            super(ctx, 0, data);
            inflater = LayoutInflater.from(ctx);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View v = convertView;
            if (v == null) v = inflater.inflate(R.layout.item_history, parent, false);

            ImageView iv = v.findViewById(R.id.ivIcon);
            TextView tv1 = v.findViewById(R.id.tvLine1);
            TextView tv2 = v.findViewById(R.id.tvLine2);

            Workout workout = getItem(position);
            if (workout == null) return v;

            String line1 = workout.getType() + " • "
                    + workout.getDuration() + " min";
            tv1.setText(line1);

            tv2.setText(workout.getDate());

            int icon = R.drawable.pushup;
            switch (workout.getType()) {
                case "arm":
                    icon = R.drawable.pushup;
                    break;
                case "leg":
                    icon = R.drawable.squat;
                    break;
                case "core":
                    icon = R.drawable.situp;
                    break;
            }
            iv.setImageResource(icon);

            return v;
        }
    }
}