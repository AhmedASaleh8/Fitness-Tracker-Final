package com.example.fitnesstracker.services;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.fitnesstracker.api.AppController;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ExerciseService {

    private static final String TAG = "ExerciseService";
    private Context context;

    public ExerciseService(Context context) {
        this.context = context;
    }

    public void fetchAndSaveExercises(OnExercisesLoadedListener listener) {
        // فحص المستخدم
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            if (listener != null) {
                listener.onError("Please login first");
            }
            return;
        }

        String userId = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String url = AppController.BASE_URL
                + "exercise/?format=json&language=2&limit=300";

        Log.d(TAG, "Starting API request for user: " + userId);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        Log.d(TAG, "API Response received");

                        JSONArray results = response.getJSONArray("results");
                        Log.d(TAG, "Total exercises in API response: " + results.length());

                        int totalValidExercises = 0;
                        int maxExercises = 100;

                        for (int i = 0; i < results.length() && totalValidExercises < maxExercises; i++) {
                            JSONObject exerciseJson = results.getJSONObject(i);
                            String name = getExerciseName(exerciseJson);

                            if (isValidExerciseName(name)) {
                                totalValidExercises++;
                            }
                        }

                        Log.d(TAG, "Valid exercises to save: " + totalValidExercises);

                        if (totalValidExercises == 0) {
                            if (listener != null) {
                                listener.onError("No valid exercises found in API");
                            }
                            return;
                        }

                        final int[] savedCount = {0};
                        final int[] failedCount = {0};
                        final int expectedCount = totalValidExercises;

                        int processedCount = 0;
                        for (int i = 0; i < results.length() && processedCount < maxExercises; i++) {
                            JSONObject exerciseJson = results.getJSONObject(i);

                            String name = getExerciseName(exerciseJson);

                            if (!isValidExerciseName(name)) {
                                continue;
                            }

                            processedCount++;

                            String description = getExerciseDescription(exerciseJson);
                            String category = getCategory(exerciseJson);
                            String muscles = getMuscles(exerciseJson);
                            String equipment = getEquipment(exerciseJson);
                            String imageUrl = getExerciseImage(exerciseJson);

                            // إنشاء Exercise data
                            Map<String, Object> exerciseData = new HashMap<>();
                            exerciseData.put("name", name);
                            exerciseData.put("description", description);
                            exerciseData.put("category", category);
                            exerciseData.put("muscles", muscles);
                            exerciseData.put("equipment", equipment);
                            exerciseData.put("type", mapCategoryToType(category));
                            exerciseData.put("difficulty", calculateDifficulty(equipment));
                            exerciseData.put("imageResource", imageUrl != null ? imageUrl : "pushup");
                            exerciseData.put("imageUrl", imageUrl);

                            // حفظ في Firebase
                            db.collection("users")
                                    .document(userId)
                                    .collection("exercises")
                                    .add(exerciseData)
                                    .addOnSuccessListener(documentReference -> {
                                        savedCount[0]++;
                                        Log.d(TAG, "Saved (" + savedCount[0] + "/" + expectedCount + "): " + name);

                                        if (savedCount[0] + failedCount[0] == expectedCount) {
                                            Log.d(TAG, "All done! Saved: " + savedCount[0] + ", Failed: " + failedCount[0]);
                                            if (listener != null) {
                                                listener.onSuccess(savedCount[0]);
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        failedCount[0]++;
                                        Log.e(TAG, "Failed to save (" + failedCount[0] + "): " + name + " - " + e.getMessage());

                                        if (savedCount[0] + failedCount[0] == expectedCount) {
                                            Log.d(TAG, "All done! Saved: " + savedCount[0] + ", Failed: " + failedCount[0]);
                                            if (listener != null) {
                                                if (savedCount[0] > 0) {
                                                    listener.onSuccess(savedCount[0]);
                                                } else {
                                                    listener.onError("Failed to save exercises to Firebase");
                                                }
                                            }
                                        }
                                    });
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "Parse Error: " + e.getMessage());
                        e.printStackTrace();
                        if (listener != null) {
                            listener.onError("Parse Error: " + e.getMessage());
                        }
                    }
                },
                error -> {
                    String msg = "API Error!";
                    if (error.networkResponse != null) {
                        msg += " Code: " + error.networkResponse.statusCode;
                        Log.e(TAG, "Network error code: " + error.networkResponse.statusCode);
                    } else if (error.getMessage() != null) {
                        msg += " " + error.getMessage();
                        Log.e(TAG, "Error message: " + error.getMessage());
                    }

                    if (listener != null) {
                        listener.onError(msg);
                    }
                }
        );

        AppController.getInstance().addToRequestQueue(request);
    }

    private boolean isValidExerciseName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        if (name.matches(".*[a-zA-Z].*")) {
            return true;
        }
        return false;
    }

    private String getExerciseName(JSONObject exerciseJson) {
        try {
            // محاولة 1: من ترجمة التمرين أولاً (الأولوية)
            if (exerciseJson.has("translations")) {
                JSONArray translations = exerciseJson.getJSONArray("translations");

                // أولوية للإنجليزية (language = 2)
                for (int i = 0; i < translations.length(); i++) {
                    JSONObject translation = translations.getJSONObject(i);
                    if (translation.has("language") && translation.getInt("language") == 2) {
                        if (translation.has("name") && !translation.isNull("name")) {
                            String name = translation.getString("name");
                            if (name != null && !name.trim().isEmpty() && !name.contains("-")) {
                                return name.trim();
                            }
                        }
                    }
                }

                // إذا ما في إنجليزي، خذ أي لغة (لكن ليس UUID)
                for (int i = 0; i < translations.length(); i++) {
                    JSONObject translation = translations.getJSONObject(i);
                    if (translation.has("name") && !translation.isNull("name")) {
                        String name = translation.getString("name");
                        if (name != null && !name.trim().isEmpty() && !name.contains("-")) {
                            return name.trim();
                        }
                    }
                }
            }

            // محاولة 2: من name مباشرة (فقط إذا ليس UUID)
            if (exerciseJson.has("name") && !exerciseJson.isNull("name")) {
                String name = exerciseJson.getString("name");
                // تأكد أنه ليس UUID (لا يحتوي على -)
                if (name != null && !name.trim().isEmpty() && !name.contains("-")) {
                    return name.trim();
                }
            }

            // محاولة 3: استخدم ID كـ fallback
            if (exerciseJson.has("id")) {
                return "Exercise " + exerciseJson.getInt("id");
            }

            return "Unknown Exercise";
        } catch (Exception e) {
            Log.e(TAG, "Error extracting name: " + e.getMessage());
            return "Unknown Exercise";
        }
    }

    private String getExerciseDescription(JSONObject exerciseJson) {
        try {
            if (exerciseJson.has("description") && !exerciseJson.isNull("description")) {
                String desc = exerciseJson.getString("description");
                if (desc != null && !desc.trim().isEmpty()) {

                    desc = desc.replaceAll("<[^>]*>", "")
                            .replaceAll("&nbsp;", " ")
                            .replaceAll("&amp;", "&")
                            .replaceAll("&lt;", "<")
                            .replaceAll("&gt;", ">")
                            .trim();

                    if (!desc.isEmpty() && desc.length() > 5) {
                        if (desc.length() > 200) {
                            return desc.substring(0, 197) + "...";
                        }
                        return desc;
                    }
                }
            }

            if (exerciseJson.has("translations")) {
                JSONArray translations = exerciseJson.getJSONArray("translations");

                for (int i = 0; i < translations.length(); i++) {
                    JSONObject translation = translations.getJSONObject(i);
                    if (translation.has("language") && translation.getInt("language") == 2) {
                        if (translation.has("description") && !translation.isNull("description")) {
                            String desc = translation.getString("description");
                            if (desc != null && !desc.trim().isEmpty()) {
                                desc = desc.replaceAll("<[^>]*>", "")
                                        .replaceAll("&nbsp;", " ")
                                        .replaceAll("&amp;", "&")
                                        .trim();

                                if (desc.length() > 5) {
                                    if (desc.length() > 200) {
                                        return desc.substring(0, 197) + "...";
                                    }
                                    return desc;
                                }
                            }
                        }
                    }
                }

                for (int i = 0; i < translations.length(); i++) {
                    JSONObject translation = translations.getJSONObject(i);
                    if (translation.has("description") && !translation.isNull("description")) {
                        String desc = translation.getString("description");
                        if (desc != null && !desc.trim().isEmpty()) {
                            desc = desc.replaceAll("<[^>]*>", "")
                                    .replaceAll("&nbsp;", " ")
                                    .trim();

                            if (desc.length() > 5) {
                                if (desc.length() > 200) {
                                    return desc.substring(0, 197) + "...";
                                }
                                return desc;
                            }
                        }
                    }
                }
            }

            String category = getCategory(exerciseJson);
            String muscles = getMuscles(exerciseJson);

            if (!muscles.equals("Full body") && !category.equals("General")) {
                return "Targets " + muscles + " - " + category + " exercise";
            } else if (!category.equals("General")) {
                return category + " strengthening exercise";
            }

            return "Strength training exercise";

        } catch (Exception e) {
            Log.e(TAG, "Error extracting description: " + e.getMessage());
            return "Strength training exercise";
        }
    }

    private String getCategory(JSONObject exerciseJson) {
        try {
            if (exerciseJson.has("category") && !exerciseJson.isNull("category")) {
                Object categoryObj = exerciseJson.get("category");

                if (categoryObj instanceof JSONObject) {
                    JSONObject category = (JSONObject) categoryObj;
                    if (category.has("name")) {
                        String catName = category.getString("name");
                        if (catName != null && !catName.trim().isEmpty()) {
                            return catName.trim();
                        }
                    }
                } else if (categoryObj instanceof String) {
                    String catName = (String) categoryObj;
                    if (!catName.trim().isEmpty()) {
                        return catName.trim();
                    }
                } else if (categoryObj instanceof Integer) {
                    int catId = (Integer) categoryObj;
                    return mapCategoryIdToName(catId);
                }
            }

            return "General";
        } catch (Exception e) {
            Log.e(TAG, "Error getting category: " + e.getMessage());
            return "General";
        }
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

    private String getExerciseImage(JSONObject exerciseJson) {
        try {
            // محاولة 1: من images array
            if (exerciseJson.has("images") && !exerciseJson.isNull("images")) {
                JSONArray images = exerciseJson.getJSONArray("images");
                if (images.length() > 0) {
                    JSONObject firstImage = images.getJSONObject(0);
                    if (firstImage.has("image") && !firstImage.isNull("image")) {
                        return firstImage.getString("image");
                    }
                }
            }

            if (exerciseJson.has("main_image") && !exerciseJson.isNull("main_image")) {
                return exerciseJson.getString("main_image");
            }

            if (exerciseJson.has("image") && !exerciseJson.isNull("image")) {
                return exerciseJson.getString("image");
            }

            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error extracting image: " + e.getMessage());
            return null;
        }
    }

    private String mapCategoryToType(String category) {
        if (category == null) return "general";

        String lower = category.toLowerCase();
        if (lower.contains("arm")) return "arm";
        if (lower.contains("leg")) return "leg";
        if (lower.contains("core") || lower.contains("abs")) return "core";
        if (lower.contains("chest") || lower.contains("pectoral")) return "arm";
        if (lower.contains("back")) return "arm";
        if (lower.contains("shoulder") || lower.contains("deltoid")) return "arm";
        if (lower.contains("calves") || lower.contains("calf")) return "leg";
        if (lower.contains("cardio")) return "general";

        return "general";
    }

    private int calculateDifficulty(String equipment) {
        if (equipment == null) return 2;

        String lower = equipment.toLowerCase();
        if (lower.contains("body weight") || lower.contains("bodyweight")) return 1;
        if (lower.contains("dumbbell") || lower.contains("kettlebell")) return 2;
        if (lower.contains("barbell") || lower.contains("cable")) return 3;

        return 2;
    }

    public interface OnExercisesLoadedListener {
        void onSuccess(int count);
        void onError(String error);
    }
}