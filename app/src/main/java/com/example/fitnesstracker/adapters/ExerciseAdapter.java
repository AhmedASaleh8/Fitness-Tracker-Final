package com.example.fitnesstracker.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.fitnesstracker.ExerciseDetailActivity;
import com.example.fitnesstracker.R;
import com.example.fitnesstracker.models.Exercise;

import java.util.ArrayList;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private Context context;
    private List<Exercise> exercises;

    public ExerciseAdapter(Context context) {
        this.context = context;
        this.exercises = new ArrayList<>();
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);

        // اسم التمرين
        holder.tvExerciseName.setText(exercise.getName());

        // الوصف
        if (exercise.getDescription() != null && !exercise.getDescription().isEmpty()) {
            holder.tvExerciseDescription.setText(exercise.getDescription());
            holder.tvExerciseDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvExerciseDescription.setVisibility(View.GONE);
        }

        // مستوى التمرين
        holder.tvExerciseDifficulty.setText("Difficulty: " + exercise.getDifficultyStars());

        loadExerciseImage(holder.ivExerciseImage, exercise);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ExerciseDetailActivity.class);
            intent.putExtra("exercise_id", exercise.getId());
            intent.putExtra("exercise_name", exercise.getName());
            intent.putExtra("exercise_description", exercise.getDescription());
            intent.putExtra("exercise_difficulty", exercise.getDifficulty());
            intent.putExtra("exercise_category", exercise.getCategory());
            intent.putExtra("exercise_muscles", exercise.getMuscles());
            intent.putExtra("exercise_equipment", exercise.getEquipment());
            intent.putExtra("exercise_type", exercise.getType());
            intent.putExtra("exercise_image", exercise.getImageResource());
            context.startActivity(intent);
        });
    }


    private void loadExerciseImage(ImageView imageView, Exercise exercise) {
        String imageResource = exercise.getImageResource();

        if (imageResource != null &&
                (imageResource.startsWith("http://") || imageResource.startsWith("https://"))) {

            Glide.with(context)
                    .load(imageResource)
                    .placeholder(getImageForCategory(exercise.getCategory()))
                    .error(getImageForCategory(exercise.getCategory()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(imageView);

        } else {
            imageView.setImageResource(getImageForCategory(exercise.getCategory()));
        }
    }


    private int getImageForCategory(String category) {
        if (category == null) return R.drawable.pushup;

        String categoryLower = category.toLowerCase();

        if (categoryLower.contains("arm")) {
            int random = (int) (Math.random() * 3);
            switch (random) {
                case 0: return R.drawable.pushup;
                case 1: return R.drawable.diamond_pushup;
                case 2: return R.drawable.dips;
                default: return R.drawable.pushup;
            }
        }

        else if (categoryLower.contains("leg") || categoryLower.contains("calves")) {
            int random = (int) (Math.random() * 3);
            switch (random) {
                case 0: return R.drawable.squat;
                case 1: return R.drawable.lunges;
                case 2: return R.drawable.calf_raise;
                default: return R.drawable.squat;
            }
        }

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
            return R.drawable.pushup; // يمكن إضافة صورة خاصة للظهر
        }

        // Shoulders - تمارين الأكتاف
        else if (categoryLower.contains("shoulder")) {
            return R.drawable.pushup;
        }

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
            return R.drawable.pushup; // يمكن إضافة صورة للكارديو
        }

        return R.drawable.pushup;
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        ImageView ivExerciseImage;
        TextView tvExerciseName;
        TextView tvExerciseDescription;
        TextView tvExerciseDifficulty;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            ivExerciseImage = itemView.findViewById(R.id.ivExerciseImage);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            tvExerciseDescription = itemView.findViewById(R.id.tvExerciseDescription);
            tvExerciseDifficulty = itemView.findViewById(R.id.tvExerciseDifficulty);
        }
    }
}