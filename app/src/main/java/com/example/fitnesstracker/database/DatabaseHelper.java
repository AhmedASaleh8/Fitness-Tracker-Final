package com.example.fitnesstracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.fitnesstracker.models.Exercise;
import com.example.fitnesstracker.models.User;
import com.example.fitnesstracker.models.Workout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "FitnessTracker.db";
    private static final int DATABASE_VERSION = 2;

    // Tables
    private static final String TABLE_USERS = "users";
    private static final String TABLE_EXERCISES = "exercises";
    private static final String TABLE_WORKOUTS = "workouts";

    // Users Table Columns
    private static final String KEY_USER_ID = "id";
    private static final String KEY_USER_NAME = "name";
    private static final String KEY_USER_EMAIL = "email";
    private static final String KEY_USER_PASSWORD = "password";

    // Exercises Table Columns
    private static final String KEY_EXERCISE_ID = "id";
    private static final String KEY_EXERCISE_NAME = "name";
    private static final String KEY_EXERCISE_DESCRIPTION = "description";
    private static final String KEY_EXERCISE_TYPE = "type";
    private static final String KEY_EXERCISE_DIFFICULTY = "difficulty";
    private static final String KEY_EXERCISE_IMAGE = "image_resource";
    private static final String KEY_EXERCISE_CATEGORY = "category";
    private static final String KEY_EXERCISE_MUSCLES = "muscles";
    private static final String KEY_EXERCISE_EQUIPMENT = "equipment";

    // Workouts Table Columns
    private static final String KEY_WORKOUT_ID = "id";
    private static final String KEY_WORKOUT_USER_ID = "user_id";
    private static final String KEY_WORKOUT_TYPE = "type";
    private static final String KEY_WORKOUT_DURATION = "duration";
    private static final String KEY_WORKOUT_DATE = "date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users Table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + KEY_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USER_NAME + " TEXT,"
                + KEY_USER_EMAIL + " TEXT UNIQUE,"
                + KEY_USER_PASSWORD + " TEXT"
                + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Create Exercises Table
        String CREATE_EXERCISES_TABLE = "CREATE TABLE " + TABLE_EXERCISES + "("
                + KEY_EXERCISE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_EXERCISE_NAME + " TEXT,"
                + KEY_EXERCISE_DESCRIPTION + " TEXT,"
                + KEY_EXERCISE_TYPE + " TEXT,"
                + KEY_EXERCISE_DIFFICULTY + " INTEGER,"
                + KEY_EXERCISE_IMAGE + " TEXT,"
                + KEY_EXERCISE_CATEGORY + " TEXT,"
                + KEY_EXERCISE_MUSCLES + " TEXT,"
                + KEY_EXERCISE_EQUIPMENT + " TEXT"
                + ")";
        db.execSQL(CREATE_EXERCISES_TABLE);

        // Create Workouts Table
        String CREATE_WORKOUTS_TABLE = "CREATE TABLE " + TABLE_WORKOUTS + "("
                + KEY_WORKOUT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_WORKOUT_USER_ID + " INTEGER,"
                + KEY_WORKOUT_TYPE + " TEXT,"
                + KEY_WORKOUT_DURATION + " INTEGER,"
                + KEY_WORKOUT_DATE + " TEXT"
                + ")";
        db.execSQL(CREATE_WORKOUTS_TABLE);

        // لا تمارين محلية - كل شيء من API
        Log.d(TAG, "Database created - no local exercises");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_EXERCISES + " ADD COLUMN " + KEY_EXERCISE_CATEGORY + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_EXERCISES + " ADD COLUMN " + KEY_EXERCISE_MUSCLES + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_EXERCISES + " ADD COLUMN " + KEY_EXERCISE_EQUIPMENT + " TEXT");
        }
    }

    // ========== USER METHODS ==========

    public long addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USER_NAME, user.getName());
        values.put(KEY_USER_EMAIL, user.getEmail());
        values.put(KEY_USER_PASSWORD, user.getPassword());

        long id = db.insert(TABLE_USERS, null, values);
        db.close();
        return id;
    }

    public User getUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{KEY_USER_ID, KEY_USER_NAME, KEY_USER_EMAIL},
                KEY_USER_EMAIL + "=? AND " + KEY_USER_PASSWORD + "=?",
                new String[]{email, password},
                null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(0));
            user.setName(cursor.getString(1));
            user.setEmail(cursor.getString(2));
            cursor.close();
        }

        db.close();
        return user;
    }

    // ========== EXERCISE METHODS ==========

    public long addExercise(Exercise exercise) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_EXERCISE_NAME, exercise.getName());
        values.put(KEY_EXERCISE_DESCRIPTION, exercise.getDescription());
        values.put(KEY_EXERCISE_TYPE, exercise.getType());
        values.put(KEY_EXERCISE_DIFFICULTY, exercise.getDifficulty());
        values.put(KEY_EXERCISE_IMAGE, exercise.getImageResource());
        values.put(KEY_EXERCISE_CATEGORY, exercise.getCategory());
        values.put(KEY_EXERCISE_MUSCLES, exercise.getMuscles());
        values.put(KEY_EXERCISE_EQUIPMENT, exercise.getEquipment());

        long id = db.insert(TABLE_EXERCISES, null, values);
        db.close();
        return id;
    }

    public List<Exercise> getAllExercises() {
        List<Exercise> exercises = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EXERCISES,
                null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Exercise exercise = new Exercise();
                exercise.setId(cursor.getInt(0));
                exercise.setName(cursor.getString(1));
                exercise.setDescription(cursor.getString(2));
                exercise.setType(cursor.getString(3));
                exercise.setDifficulty(cursor.getInt(4));
                exercise.setImageResource(cursor.getString(5));
                exercise.setCategory(cursor.getString(6));
                exercise.setMuscles(cursor.getString(7));
                exercise.setEquipment(cursor.getString(8));
                exercises.add(exercise);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return exercises;
    }

    public List<Exercise> getExercisesByType(String type) {
        List<Exercise> exercises = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EXERCISES,
                null,
                KEY_EXERCISE_TYPE + "=?",
                new String[]{type},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Exercise exercise = new Exercise();
                exercise.setId(cursor.getInt(0));
                exercise.setName(cursor.getString(1));
                exercise.setDescription(cursor.getString(2));
                exercise.setType(cursor.getString(3));
                exercise.setDifficulty(cursor.getInt(4));
                exercise.setImageResource(cursor.getString(5));
                exercise.setCategory(cursor.getString(6));
                exercise.setMuscles(cursor.getString(7));
                exercise.setEquipment(cursor.getString(8));
                exercises.add(exercise);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return exercises;
    }

    public void deleteAllExercises() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EXERCISES, null, null);
        db.close();
    }

    // ========== API METHODS ==========

    public void fetchAndStoreExercises(final OnExercisesLoadedListener listener) {
        new Thread(() -> {
            try {
                Log.d(TAG, "Starting API fetch...");

                // رابط API
                String apiUrl = "https://wger.de/api/v2/exercise/?language=2&limit=100";

                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setRequestProperty("Accept", "application/json");

                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream())
                    );
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    Log.d(TAG, "Response received: " + response.length() + " chars");

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray exercises = jsonResponse.getJSONArray("results");

                    Log.d(TAG, "Found " + exercises.length() + " exercises in API");

                    int savedCount = 0;

                    for (int i = 0; i < exercises.length(); i++) {
                        JSONObject exerciseJson = exercises.getJSONObject(i);

                        String name = exerciseJson.optString("name", "Exercise " + i);
                        String description = exerciseJson.optString("description", "");

                        description = description.replaceAll("<[^>]*>", "")
                                .replaceAll("&nbsp;", " ")
                                .trim();

                        if (description.length() > 200) {
                            description = description.substring(0, 200) + "...";
                        }

                        int categoryId = exerciseJson.optInt("category", 0);
                        String category = mapCategoryIdToName(categoryId);

                        String muscles = getMuscles(exerciseJson);

                        String equipment = getEquipment(exerciseJson);

                        String type = mapCategoryToType(category);
                        int difficulty = calculateDifficulty(equipment);

                        Exercise exercise = new Exercise();
                        exercise.setName(name);
                        exercise.setDescription(description);
                        exercise.setCategory(category);
                        exercise.setMuscles(muscles);
                        exercise.setEquipment(equipment);
                        exercise.setDifficulty(difficulty);
                        exercise.setType(type);
                        exercise.setImageResource("pushup");

                        long result = addExercise(exercise);

                        if (result > 0) {
                            savedCount++;
                            Log.d(TAG, "Saved: " + name + " (" + category + ")");
                        }
                    }

                    final int finalCount = savedCount;
                    Log.d(TAG, "Total exercises saved: " + finalCount);

                    if (listener != null) {
                        listener.onSuccess(finalCount);
                    }

                } else {
                    Log.e(TAG, "API Error: Response code " + responseCode);
                    if (listener != null) {
                        listener.onError("Server returned error code: " + responseCode);
                    }
                }

                connection.disconnect();

            } catch (Exception e) {
                Log.e(TAG, "Error fetching exercises from API", e);
                if (listener != null) {
                    listener.onError("Network error: " + e.getMessage());
                }
            }
        }).start();
    }


    private String mapCategoryIdToName(int categoryId) {
        switch (categoryId) {
            case 8: return "Arms";
            case 9: return "Legs";
            case 10: return "Abs";
            case 11: return "Chest";
            case 12: return "Back";
            case 13: return "Shoulders";
            case 14: return "Calves";
            case 15: return "Cardio";
            default: return "General";
        }
    }

    private String getMuscles(JSONObject exerciseJson) {
        try {
            StringBuilder muscles = new StringBuilder();

            if (exerciseJson.has("muscles")) {
                JSONArray musclesArray = exerciseJson.getJSONArray("muscles");
                for (int i = 0; i < musclesArray.length(); i++) {
                    JSONObject muscle = musclesArray.getJSONObject(i);
                    if (i > 0) muscles.append(", ");
                    if (muscle.has("name")) {
                        muscles.append(muscle.getString("name"));
                    }
                }
            }

            if (exerciseJson.has("muscles_secondary")) {
                JSONArray secondaryMuscles = exerciseJson.getJSONArray("muscles_secondary");
                for (int i = 0; i < secondaryMuscles.length(); i++) {
                    JSONObject muscle = secondaryMuscles.getJSONObject(i);
                    if (muscles.length() > 0) muscles.append(", ");
                    if (muscle.has("name")) {
                        muscles.append(muscle.getString("name"));
                    }
                }
            }

            String result = muscles.toString().trim();
            return result.isEmpty() ? "Full body" : result;
        } catch (Exception e) {
            return "Full body";
        }
    }

    private String getEquipment(JSONObject exerciseJson) {
        try {
            StringBuilder equipment = new StringBuilder();

            if (exerciseJson.has("equipment")) {
                JSONArray equipmentArray = exerciseJson.getJSONArray("equipment");
                for (int i = 0; i < equipmentArray.length(); i++) {
                    JSONObject equip = equipmentArray.getJSONObject(i);
                    if (i > 0) equipment.append(", ");
                    if (equip.has("name")) {
                        equipment.append(equip.getString("name"));
                    }
                }
            }

            String result = equipment.toString().trim();
            return result.isEmpty() ? "Body weight" : result;
        } catch (Exception e) {
            return "Body weight";
        }
    }

    private String mapCategoryToType(String category) {
        if (category == null) return "general";

        String lower = category.toLowerCase();
        if (lower.contains("arm") || lower.contains("chest") ||
                lower.contains("back") || lower.contains("shoulder")) {
            return "arm";
        }
        if (lower.contains("leg") || lower.contains("calves")) {
            return "leg";
        }
        if (lower.contains("core") || lower.contains("abs")) {
            return "core";
        }
        return "general";
    }

    private int calculateDifficulty(String equipment) {
        if (equipment == null) return 2;

        String lower = equipment.toLowerCase();
        if (lower.contains("body weight") || lower.contains("bodyweight")) {
            return 1;
        }
        if (lower.contains("barbell") || lower.contains("cable")) {
            return 3;
        }
        return 2;
    }

    // ========== WORKOUT METHODS ==========

    public long addWorkout(Workout workout) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_WORKOUT_USER_ID, workout.getUserId());
        values.put(KEY_WORKOUT_TYPE, workout.getType());
        values.put(KEY_WORKOUT_DURATION, workout.getDuration());
        values.put(KEY_WORKOUT_DATE, workout.getDate());

        long id = db.insert(TABLE_WORKOUTS, null, values);
        db.close();
        return id;
    }

    public List<Workout> getWorkoutsByUserId(int userId) {
        List<Workout> workouts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WORKOUTS,
                null,
                KEY_WORKOUT_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, KEY_WORKOUT_DATE + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Workout workout = new Workout();
                workout.setId(cursor.getInt(0));
                workout.setUserId(cursor.getInt(1));
                workout.setType(cursor.getString(2));
                workout.setDuration(cursor.getInt(3));
                workout.setDate(cursor.getString(4));
                workouts.add(workout);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return workouts;
    }

    public List<Workout> getAllWorkouts() {
        List<Workout> workouts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WORKOUTS,
                null, null, null, null, null,
                KEY_WORKOUT_DATE + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Workout workout = new Workout();
                workout.setId(cursor.getInt(0));
                workout.setUserId(cursor.getInt(1));
                workout.setType(cursor.getString(2));
                workout.setDuration(cursor.getInt(3));
                workout.setDate(cursor.getString(4));
                workouts.add(workout);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return workouts;
    }

    // ========== FIREBASE METHODS ==========

    public void getAllExercisesFromFirebase(OnFirebaseExercisesLoadedListener listener) {
        com.google.firebase.auth.FirebaseAuth mAuth = com.google.firebase.auth.FirebaseAuth.getInstance();
        com.google.firebase.auth.FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Log.d(TAG, "No user logged in - returning empty list");
            if (listener != null) {
                listener.onLoaded(new ArrayList<>());
            }
            return;
        }

        String userId = currentUser.getUid();
        com.google.firebase.firestore.FirebaseFirestore firestore =
                com.google.firebase.firestore.FirebaseFirestore.getInstance();

        Log.d(TAG, "Loading exercises from Firebase for user: " + userId);

        firestore.collection("users")
                .document(userId)
                .collection("exercises")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Exercise> exercises = new ArrayList<>();

                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Exercise exercise = new Exercise();
                        exercise.setId(doc.getId().hashCode());
                        exercise.setName(doc.getString("name"));
                        exercise.setDescription(doc.getString("description"));
                        exercise.setCategory(doc.getString("category"));
                        exercise.setMuscles(doc.getString("muscles"));
                        exercise.setEquipment(doc.getString("equipment"));
                        exercise.setType(doc.getString("type"));

                        Long difficulty = doc.getLong("difficulty");
                        exercise.setDifficulty(difficulty != null ? difficulty.intValue() : 2);

                        exercise.setImageResource(doc.getString("imageResource"));

                        String imageUrl = doc.getString("imageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            exercise.setImageResource(imageUrl);
                        }

                        exercises.add(exercise);
                    }

                    Log.d(TAG, "Loaded " + exercises.size() + " exercises from Firebase");

                    if (listener != null) {
                        listener.onLoaded(exercises);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading from Firebase: " + e.getMessage());
                    if (listener != null) {
                        listener.onLoaded(new ArrayList<>());
                    }
                });
    }

    // ========== INTERFACES ==========

    public interface OnExercisesLoadedListener {
        void onSuccess(int count);
        void onError(String error);
    }

    public interface OnFirebaseExercisesLoadedListener {
        void onLoaded(List<Exercise> exercises);
    }
}