package com.example.fitnessapp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fitnessapp.model.response.ExerciseResponse;
import com.example.fitnessapp.adapter.CreatePlanScheduleAdapter.OnItemInteractionListener;

import java.util.ArrayList;
import java.util.List;

public class ExerciseSearchAdapter extends ArrayAdapter<ExerciseResponse> implements Filterable {

    private static final String TAG = "ExerciseSearchAdapter";
    private List<ExerciseResponse> allExercises; // Tất cả exercises
    private List<ExerciseResponse> filteredExercises; // Danh sách đã filter
    private OnItemInteractionListener listener;

    public ExerciseSearchAdapter(@NonNull Context context, int resource, @NonNull List<ExerciseResponse> objects, OnItemInteractionListener listener) {
        super(context, resource, objects);
        this.allExercises = new ArrayList<>(objects);
        this.filteredExercises = new ArrayList<>(objects);
        this.listener = listener;
    }

    public void setExercisesData(List<ExerciseResponse> exercises) {
        this.allExercises.clear();
        this.filteredExercises.clear();
        if (exercises != null) {
            this.allExercises.addAll(exercises);
            this.filteredExercises.addAll(exercises);
        }

        Log.d(TAG, "setExercisesData - allExercises size: " + allExercises.size());
        for (int i = 0; i < allExercises.size(); i++) {
            Log.d(TAG, "  [" + i + "] ID: " + allExercises.get(i).getId() + ", Name: " + allExercises.get(i).getName());
        }

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        int count = filteredExercises.size();
        Log.d(TAG, "getCount: " + count);
        return count;
    }

    @Nullable
    @Override
    public ExerciseResponse getItem(int position) {
        Log.d(TAG, "getItem called - position: " + position + ", filteredExercises size: " + filteredExercises.size());

        if (position >= 0 && position < filteredExercises.size()) {
            ExerciseResponse item = filteredExercises.get(position);
            Log.d(TAG, "  Returning ID: " + item.getId() + ", Name: " + item.getName());
            return item;
        }

        Log.e(TAG, "  Position out of bounds! Returning null");
        return null;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                Log.d(TAG, "performFiltering - constraint: " + constraint);
                FilterResults filterResults = new FilterResults();
                List<ExerciseResponse> filtered = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filtered.addAll(allExercises);
                    Log.d(TAG, "  No constraint, showing all " + filtered.size() + " exercises");
                } else {
                    String query = constraint.toString().toLowerCase().trim();
                    Log.d(TAG, "  Query: '" + query + "'");

                    for (ExerciseResponse exercise : allExercises) {
                        if (exercise.getName() != null) {
                            String exerciseName = exercise.getName().toLowerCase();
                            boolean matches = false;

                            // Check if name starts with query
                            if (exerciseName.startsWith(query)) {
                                matches = true;
                                Log.d(TAG, "    ✓ Match (starts): " + exercise.getName());
                            } else {
                                // Check each word
                                String[] words = exerciseName.split("\\s+");
                                for (String word : words) {
                                    if (word.startsWith(query)) {
                                        matches = true;
                                        Log.d(TAG, "    ✓ Match (word): " + exercise.getName());
                                        break;
                                    }
                                }
                            }

                            if (matches) {
                                filtered.add(exercise);
                            } else {
                                Log.d(TAG, "    ✗ No match: " + exercise.getName());
                            }
                        }
                    }

                    Log.d(TAG, "  Filtered result count: " + filtered.size());
                }

                filterResults.values = filtered;
                filterResults.count = filtered.size();
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                Log.d(TAG, "publishResults - constraint: " + constraint);
                filteredExercises.clear();

                if (results != null && results.values != null) {
                    @SuppressWarnings("unchecked")
                    List<ExerciseResponse> filtered = (List<ExerciseResponse>) results.values;
                    filteredExercises.addAll(filtered);

                    Log.d(TAG, "  Updated filteredExercises size: " + filteredExercises.size());
                    for (int i = 0; i < filteredExercises.size(); i++) {
                        Log.d(TAG, "    [" + i + "] ID: " + filteredExercises.get(i).getId() + ", Name: " + filteredExercises.get(i).getName());
                    }
                }

                if (filteredExercises.size() > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView label = (TextView) super.getView(position, convertView, parent);

        if (position >= 0 && position < filteredExercises.size()) {
            ExerciseResponse exercise = filteredExercises.get(position);
            if (exercise != null && exercise.getName() != null) {
                label.setText(exercise.getName());
                Log.d(TAG, "getView - position: " + position + ", showing: " + exercise.getName());
            }
        }

        label.setTextColor(getContext().getResources().getColor(android.R.color.white));
        label.setPadding(16, 16, 16, 16);
        return label;
    }
}