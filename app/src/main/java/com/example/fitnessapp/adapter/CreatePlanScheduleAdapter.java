package com.example.fitnessapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.fragment.CreatePlanFragment;
import com.example.fitnessapp.model.response.ExerciseResponse;
import com.example.fitnessapp.utils.ExerciseFieldConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CreatePlanScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "CreatePlanScheduleAdapter";
    private static final int VIEW_TYPE_DAY_HEADER = 0;
    private static final int VIEW_TYPE_EXERCISE_ITEM = 1;

    private Context context;
    private List<CreatePlanFragment.ScheduleItem> scheduleItems;
    private OnItemInteractionListener listener;

    public interface OnItemInteractionListener {
        void onAddExerciseClicked(int dayOfWeek);
        void onDeleteExerciseClicked(int dayOfWeek, int exerciseIndex);
        void onExerciseSelected(int dayOfWeek, int exerciseIndex, Long exerciseId, String exerciseName);
        void onExerciseSetsChanged(int dayOfWeek, int exerciseIndex, Integer sets);
        void onExerciseRepsChanged(int dayOfWeek, int exerciseIndex, Integer reps);
        void onExerciseDurationChanged(int dayOfWeek, int exerciseIndex, Integer duration);
        void onExerciseWeightChanged(int dayOfWeek, int exerciseIndex, Double weight);
        List<ExerciseResponse> getAllExercises();
    }

    public CreatePlanScheduleAdapter(Context context, List<CreatePlanFragment.ScheduleItem> scheduleItems, OnItemInteractionListener listener) {
        this.context = context;
        this.scheduleItems = scheduleItems;
        this.listener = listener;
    }

    public void setScheduleItems(List<CreatePlanFragment.ScheduleItem> scheduleItems) {
        this.scheduleItems = scheduleItems;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return scheduleItems.get(position).isDayHeader() ? VIEW_TYPE_DAY_HEADER : VIEW_TYPE_EXERCISE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DAY_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_create_plan_day_section, parent, false);
            return new DayHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_create_plan_exercise, parent, false);
            return new ExerciseItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CreatePlanFragment.ScheduleItem item = scheduleItems.get(position);

        if (holder instanceof DayHeaderViewHolder) {
            DayHeaderViewHolder dayHolder = (DayHeaderViewHolder) holder;
            dayHolder.tvDayName.setText(item.getDayName());
            dayHolder.btnAddExercise.setOnClickListener(v -> listener.onAddExerciseClicked(item.getDayOfWeek()));

        } else if (holder instanceof ExerciseItemViewHolder) {
            ExerciseItemViewHolder exerciseHolder = (ExerciseItemViewHolder) holder;

            // Setup regular Spinner with click to open search dialog
            List<ExerciseResponse> allExercises = listener.getAllExercises();
            List<String> exerciseNames = allExercises.stream()
                    .map(ExerciseResponse::getName)
                    .collect(Collectors.toList());

            // Add placeholder at the beginning
            List<String> spinnerItems = new ArrayList<>();
            spinnerItems.add("Chọn bài tập");
            spinnerItems.addAll(exerciseNames);

            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context,
                    android.R.layout.simple_spinner_item, spinnerItems);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            exerciseHolder.spinnerExercise.setAdapter(spinnerAdapter);

            // Get the current exercise object from the ScheduleItem or find it
            ExerciseResponse currentExercise = item.getExerciseObject();

            // Set current selected exercise if available
            if (item.getExerciseName() != null) {
                int selectedIndex = exerciseNames.indexOf(item.getExerciseName());
                if (selectedIndex >= 0) {
                    exerciseHolder.spinnerExercise.setSelection(selectedIndex + 1); // +1 because of placeholder

                    // If we don't have the exercise object yet, find it
                    if (currentExercise == null) {
                        currentExercise = allExercises.get(selectedIndex);
                    }
                }
            } else {
                exerciseHolder.spinnerExercise.setSelection(0); // Select placeholder
            }

            // Configure field visibility based on exercise type
            configureFieldVisibility(exerciseHolder, currentExercise);

            // Make spinner non-clickable by default, use button to open search dialog
            exerciseHolder.spinnerExercise.setEnabled(false);

            // Button to open search dialog
            exerciseHolder.btnSearchExercise.setOnClickListener(v -> {
                showExerciseSearchDialog(exerciseHolder, item, allExercises, exerciseNames);
            });

            // Clear any existing TextWatchers to avoid duplicates
            exerciseHolder.etSets.removeTextChangedListener(exerciseHolder.setsWatcher);
            exerciseHolder.etReps.removeTextChangedListener(exerciseHolder.repsWatcher);
            exerciseHolder.etDuration.removeTextChangedListener(exerciseHolder.durationWatcher);
            exerciseHolder.etWeight.removeTextChangedListener(exerciseHolder.weightWatcher);

            // Set Sets
            if (item.getExerciseRequest().getSets() != null) {
                exerciseHolder.etSets.setText(String.valueOf(item.getExerciseRequest().getSets()));
            } else {
                exerciseHolder.etSets.setText("");
            }

            // Set Reps
            if (item.getExerciseRequest().getReps() != null) {
                exerciseHolder.etReps.setText(String.valueOf(item.getExerciseRequest().getReps()));
            } else {
                exerciseHolder.etReps.setText("");
            }

            // Set Duration
            if (item.getExerciseRequest().getDuration() != null) {
                exerciseHolder.etDuration.setText(String.valueOf(item.getExerciseRequest().getDuration()));
            } else {
                exerciseHolder.etDuration.setText("");
            }

            // Set Weight
            if (item.getExerciseRequest().getWeight() != null) {
                exerciseHolder.etWeight.setText(String.valueOf(item.getExerciseRequest().getWeight()));
            } else {
                exerciseHolder.etWeight.setText("");
            }

            // TextWatchers for EditTexts
            exerciseHolder.setsWatcher = new SimpleTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        Integer sets = s.toString().isEmpty() ? null : Integer.parseInt(s.toString());
                        listener.onExerciseSetsChanged(item.getDayOfWeek(), item.getExerciseIndex(), sets);
                    } catch (NumberFormatException e) {
                        listener.onExerciseSetsChanged(item.getDayOfWeek(), item.getExerciseIndex(), null);
                    }
                }
            };
            exerciseHolder.etSets.addTextChangedListener(exerciseHolder.setsWatcher);

            exerciseHolder.repsWatcher = new SimpleTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        Integer reps = s.toString().isEmpty() ? null : Integer.parseInt(s.toString());
                        listener.onExerciseRepsChanged(item.getDayOfWeek(), item.getExerciseIndex(), reps);
                    } catch (NumberFormatException e) {
                        listener.onExerciseRepsChanged(item.getDayOfWeek(), item.getExerciseIndex(), null);
                    }
                }
            };
            exerciseHolder.etReps.addTextChangedListener(exerciseHolder.repsWatcher);

            exerciseHolder.durationWatcher = new SimpleTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        Integer duration = s.toString().isEmpty() ? null : Integer.parseInt(s.toString());
                        listener.onExerciseDurationChanged(item.getDayOfWeek(), item.getExerciseIndex(), duration);
                    } catch (NumberFormatException e) {
                        listener.onExerciseDurationChanged(item.getDayOfWeek(), item.getExerciseIndex(), null);
                    }
                }
            };
            exerciseHolder.etDuration.addTextChangedListener(exerciseHolder.durationWatcher);

            exerciseHolder.weightWatcher = new SimpleTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        Double weight = s.toString().isEmpty() ? null : Double.parseDouble(s.toString());
                        listener.onExerciseWeightChanged(item.getDayOfWeek(), item.getExerciseIndex(), weight);
                    } catch (NumberFormatException e) {
                        listener.onExerciseWeightChanged(item.getDayOfWeek(), item.getExerciseIndex(), null);
                    }
                }
            };
            exerciseHolder.etWeight.addTextChangedListener(exerciseHolder.weightWatcher);

            // Delete button
            exerciseHolder.btnDeleteExercise.setOnClickListener(v ->
                    listener.onDeleteExerciseClicked(item.getDayOfWeek(), item.getExerciseIndex())
            );
        }
    }

    private void configureFieldVisibility(ExerciseItemViewHolder holder, ExerciseResponse exercise) {
        if (exercise == null || exercise.getName() == null) {
            // No exercise selected - show Sets + Reps as default
            showSetsAndReps(holder);
            return;
        }

        // Get field display configuration based on exercise name
        ExerciseFieldConfig.FieldDisplay fieldDisplay = ExerciseFieldConfig.getFieldDisplay(exercise.getName());

        switch (fieldDisplay) {
            case SETS_DURATION:
                // Show: Sets + Duration only (e.g., Plank, Wall Sit)
                showSetsAndDuration(holder);
                break;
            case SETS_REPS_WEIGHT:
                // Show: Sets + Reps + Weight (e.g., Bench Press, Squats with weight)
                showSetsRepsAndWeight(holder);
                break;
            case SETS_REPS:
            default:
                // Show: Sets + Reps only (e.g., Push-ups, Pull-ups)
                showSetsAndReps(holder);
                break;
        }

        Log.d(TAG, "Bài tập: " + exercise.getName() + " | Hiển thị: " + fieldDisplay);
    }

    private void showSetsAndReps(ExerciseItemViewHolder holder) {
        holder.tvSetsLabel.setVisibility(View.VISIBLE);
        holder.etSets.setVisibility(View.VISIBLE);
        holder.tvRepsLabel.setVisibility(View.VISIBLE);
        holder.etReps.setVisibility(View.VISIBLE);
        holder.tvDurationLabel.setVisibility(View.GONE);
        holder.etDuration.setVisibility(View.GONE);
        holder.tvWeightLabel.setVisibility(View.GONE);
        holder.etWeight.setVisibility(View.GONE);
    }

    private void showSetsAndDuration(ExerciseItemViewHolder holder) {
        holder.tvSetsLabel.setVisibility(View.VISIBLE);
        holder.etSets.setVisibility(View.VISIBLE);
        holder.tvRepsLabel.setVisibility(View.GONE);
        holder.etReps.setVisibility(View.GONE);
        holder.tvDurationLabel.setVisibility(View.VISIBLE);
        holder.etDuration.setVisibility(View.VISIBLE);
        holder.tvWeightLabel.setVisibility(View.GONE);
        holder.etWeight.setVisibility(View.GONE);
    }

    private void showSetsRepsAndWeight(ExerciseItemViewHolder holder) {
        holder.tvSetsLabel.setVisibility(View.VISIBLE);
        holder.etSets.setVisibility(View.VISIBLE);
        holder.tvRepsLabel.setVisibility(View.VISIBLE);
        holder.etReps.setVisibility(View.VISIBLE);
        holder.tvDurationLabel.setVisibility(View.GONE);
        holder.etDuration.setVisibility(View.GONE);
        holder.tvWeightLabel.setVisibility(View.VISIBLE);
        holder.etWeight.setVisibility(View.VISIBLE);
    }

    private void showAllFields(ExerciseItemViewHolder holder) {
        holder.tvSetsLabel.setVisibility(View.VISIBLE);
        holder.etSets.setVisibility(View.VISIBLE);
        holder.tvRepsLabel.setVisibility(View.VISIBLE);
        holder.etReps.setVisibility(View.VISIBLE);
        holder.tvDurationLabel.setVisibility(View.VISIBLE);
        holder.etDuration.setVisibility(View.VISIBLE);
        holder.tvWeightLabel.setVisibility(View.VISIBLE);
        holder.etWeight.setVisibility(View.VISIBLE);
    }

    private void showExerciseSearchDialog(ExerciseItemViewHolder holder, CreatePlanFragment.ScheduleItem item,
                                          List<ExerciseResponse> allExercises, List<String> exerciseNames) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_exercise_search, null);
        builder.setView(dialogView);

        EditText etSearch = dialogView.findViewById(R.id.et_search_exercise);
        RecyclerView recyclerView = dialogView.findViewById(R.id.rv_exercises);

        List<ExerciseResponse> filteredExercises = new ArrayList<>(allExercises);

        ExerciseSearchDialogAdapter adapter = new ExerciseSearchDialogAdapter(context, filteredExercises, exercise -> {
            // This lambda won't be called directly, we'll use setOnItemClickListener below
        });

        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        AlertDialog dialog = builder.create();

        // Search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                List<ExerciseResponse> filtered = allExercises.stream()
                        .filter(ex -> ex.getName().toLowerCase().contains(query))
                        .collect(Collectors.toList());
                adapter.updateList(filtered);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Handle exercise selection
        adapter.setOnItemClickListener(exercise -> {
            // Update the data model
            listener.onExerciseSelected(
                    item.getDayOfWeek(),
                    item.getExerciseIndex(),
                    exercise.getId(),
                    exercise.getName()
            );

            // Update the spinner display immediately
            int selectedIndex = exerciseNames.indexOf(exercise.getName());
            if (selectedIndex >= 0) {
                holder.spinnerExercise.setSelection(selectedIndex + 1);
            }

            // Configure field visibility based on selected exercise
            configureFieldVisibility(holder, exercise);

            // Clear fields that are now hidden
            if (holder.etReps.getVisibility() == View.GONE) {
                holder.etReps.setText("");
                listener.onExerciseRepsChanged(item.getDayOfWeek(), item.getExerciseIndex(), null);
            }
            if (holder.etDuration.getVisibility() == View.GONE) {
                holder.etDuration.setText("");
                listener.onExerciseDurationChanged(item.getDayOfWeek(), item.getExerciseIndex(), null);
            }
            if (holder.etWeight.getVisibility() == View.GONE) {
                holder.etWeight.setText("");
                listener.onExerciseWeightChanged(item.getDayOfWeek(), item.getExerciseIndex(), null);
            }

            // Force notify the adapter to update
            notifyItemChanged(holder.getAdapterPosition());

            Log.d(TAG, "Bài tập đã chọn: " + exercise.getName() + " (ID: " + exercise.getId() + ")");

            // Dismiss dialog
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return scheduleItems != null ? scheduleItems.size() : 0;
    }

    // ViewHolder for Day Header
    public static class DayHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayName;
        Button btnAddExercise;

        public DayHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tv_day_section_name);
            btnAddExercise = itemView.findViewById(R.id.btn_add_exercise);
        }
    }

    // ViewHolder for Exercise Items
    public static class ExerciseItemViewHolder extends RecyclerView.ViewHolder {
        Spinner spinnerExercise;
        ImageView btnSearchExercise;
        TextView tvSetsLabel, tvRepsLabel, tvDurationLabel, tvWeightLabel;
        EditText etSets, etReps, etDuration, etWeight;
        ImageView btnDeleteExercise;

        TextWatcher setsWatcher, repsWatcher, durationWatcher, weightWatcher;

        public ExerciseItemViewHolder(@NonNull View itemView) {
            super(itemView);
            spinnerExercise = itemView.findViewById(R.id.spinner_exercise);
            btnSearchExercise = itemView.findViewById(R.id.btn_search_exercise);
            tvSetsLabel = itemView.findViewById(R.id.tv_sets_label);
            tvRepsLabel = itemView.findViewById(R.id.tv_reps_label);
            tvDurationLabel = itemView.findViewById(R.id.tv_duration_label);
            tvWeightLabel = itemView.findViewById(R.id.tv_weight_label);
            etSets = itemView.findViewById(R.id.et_sets);
            etReps = itemView.findViewById(R.id.et_reps);
            etDuration = itemView.findViewById(R.id.et_duration_exercise);
            etWeight = itemView.findViewById(R.id.et_weight_exercise);
            btnDeleteExercise = itemView.findViewById(R.id.btn_delete_exercise);
        }
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public abstract void afterTextChanged(Editable s);
    }

    // Adapter for search dialog
    private static class ExerciseSearchDialogAdapter extends RecyclerView.Adapter<ExerciseSearchDialogAdapter.ViewHolder> {
        private Context context;
        private List<ExerciseResponse> exercises;
        private OnExerciseClickListener listener;

        interface OnExerciseClickListener {
            void onExerciseClick(ExerciseResponse exercise);
        }

        public ExerciseSearchDialogAdapter(Context context, List<ExerciseResponse> exercises, OnExerciseClickListener listener) {
            this.context = context;
            this.exercises = exercises;
            this.listener = listener;
        }

        public void setOnItemClickListener(OnExerciseClickListener listener) {
            this.listener = listener;
        }

        public void updateList(List<ExerciseResponse> newList) {
            this.exercises = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_exercise_search, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ExerciseResponse exercise = exercises.get(position);
            holder.textView.setText(exercise.getName());
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExerciseClick(exercise);
                }
            });
        }

        @Override
        public int getItemCount() {
            return exercises.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}