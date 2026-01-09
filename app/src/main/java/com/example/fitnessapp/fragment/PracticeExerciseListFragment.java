package com.example.fitnessapp.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.adapter.PracticeExerciseAdapter;
import com.example.fitnessapp.model.PracticeExerciseItem;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.WorkoutDayExerciseResponse;
import com.example.fitnessapp.model.response.WorkoutDayDetailResponse;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.network.RetrofitClient;
import com.example.fitnessapp.session.SessionManager;
import com.example.fitnessapp.utils.WorkoutProgressManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PracticeExerciseListFragment extends Fragment implements PracticeExerciseAdapter.OnExerciseActionListener {

    private static final String TAG = "PracticeExerciseList";
    private static final String ARG_PLAN_NAME = "plan_name";
    private static final String ARG_WORKOUT_DAY_ID = "workout_day_id";

    private RecyclerView recyclerViewExercises;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private TextView tvPlanTitle;
    private ImageView btnBack;
    private Button btnFinish;

    private PracticeExerciseAdapter adapter;
    private ApiService apiService;
    private SessionManager sessionManager;
    private WorkoutProgressManager progressManager;

    private String planName;
    private Long workoutDayId;

    public static PracticeExerciseListFragment newInstance(String planName, Long workoutDayId) {
        PracticeExerciseListFragment fragment = new PracticeExerciseListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PLAN_NAME, planName);
        args.putLong(ARG_WORKOUT_DAY_ID, workoutDayId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            planName = getArguments().getString(ARG_PLAN_NAME);
            workoutDayId = getArguments().getLong(ARG_WORKOUT_DAY_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_practice_exercise_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        loadExercises();
    }

    private void initViews(View view) {
        recyclerViewExercises = view.findViewById(R.id.recyclerViewExercises);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvPlanTitle = view.findViewById(R.id.tvPlanTitle);
        btnBack = view.findViewById(R.id.btnBack);
        btnFinish = view.findViewById(R.id.btnFinish);

        apiService = RetrofitClient.getApiService();
        sessionManager = SessionManager.getInstance(requireContext());
        progressManager = WorkoutProgressManager.getInstance(requireContext());

        tvPlanTitle.setText(planName);

        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        btnFinish.setOnClickListener(v -> {
            // TODO: Navigate to completion screen
            Toast.makeText(requireContext(), "Kết thúc luyện tập!", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerView() {
        adapter = new PracticeExerciseAdapter(this);
        recyclerViewExercises.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewExercises.setAdapter(adapter);
    }

    private void loadExercises() {
        String accessToken = sessionManager.getAccessToken();
        String authorizationHeader = null;

        if (accessToken != null && !accessToken.isEmpty()) {
            authorizationHeader = "Bearer " + accessToken;
        } else {
            Toast.makeText(getContext(), "Token expired.", Toast.LENGTH_LONG).show();
            return;
        }

        if (workoutDayId == null) {
            Toast.makeText(getContext(), "Invalid workout day", Toast.LENGTH_SHORT).show();
            showEmptyState(true);
            return;
        }

        showLoading(true);

        apiService.getExercisesByDay(authorizationHeader, workoutDayId)
                .enqueue(new Callback<ApiResponse<WorkoutDayDetailResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<WorkoutDayDetailResponse>> call, @NonNull Response<ApiResponse<WorkoutDayDetailResponse>> response) {
                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<WorkoutDayDetailResponse> apiResponse = response.body();
                            if (apiResponse.isStatus()) {
                                WorkoutDayDetailResponse dayDetail = apiResponse.getData();

                                if (dayDetail != null && dayDetail.getExercises() != null && !dayDetail.getExercises().isEmpty()) {
                                    List<PracticeExerciseItem> items = new ArrayList<>();
                                    for (WorkoutDayExerciseResponse exercise : dayDetail.getExercises()) {
                                        PracticeExerciseItem item = new PracticeExerciseItem(exercise);

                                        // Load completed sets từ SharedPreferences
                                        int savedCompletedSets = progressManager.getCompletedSets(workoutDayId, exercise.getId());
                                        item.setCompletedSets(savedCompletedSets);

                                        Log.d(TAG, "Loaded exercise: " + exercise.getExerciseName()
                                                + ", saved completed sets: " + savedCompletedSets);

                                        items.add(item);
                                    }
                                    adapter.setExerciseItems(items);
                                    showEmptyState(false);

                                    Log.d(TAG, "Loaded " + items.size() + " exercises");
                                } else {
                                    showEmptyState(true);
                                }
                            } else {
                                Toast.makeText(getContext(), "API Error: " + apiResponse.getData(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "API Error: " + apiResponse.getData());
                                showEmptyState(true);
                            }
                        } else {
                            if (response.code() == 401) {
                                Toast.makeText(getContext(), "Session expired. Please login again.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getContext(), "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                            Log.e(TAG, "Server error: " + response.code() + " " + response.message());
                            showEmptyState(true);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<WorkoutDayDetailResponse>> call, @NonNull Throwable t) {
                        showLoading(false);
                        Toast.makeText(getContext(), "Connection error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Connection error: " + t.getMessage(), t);
                        showEmptyState(true);
                    }
                });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerViewExercises.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean isEmpty) {
        tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerViewExercises.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onPlayClick(PracticeExerciseItem item, int position) {
        // Lấy completed sets từ SharedPreferences
        int currentCompletedSets = progressManager.getCompletedSets(workoutDayId, item.getExercise().getId());

        Log.d(TAG, "Opening ready screen - Exercise: " + item.getExercise().getExerciseName()
                + ", Position: " + position
                + ", Current completed sets from storage: " + currentCompletedSets
                + ", Total sets: " + item.getExercise().getSets());

        // Update item với giá trị mới nhất
        item.setCompletedSets(currentCompletedSets);

        if (!item.hasRemainingSet()) {
            Toast.makeText(getContext(), "Đã hoàn thành tất cả các set!", Toast.LENGTH_SHORT).show();
            return;
        }

        PracticeReadyFragment fragment = PracticeReadyFragment.newInstance(
                item.getExercise(),
                workoutDayId,
                currentCompletedSets,
                position
        );

        fragment.setOnSetCompletedListener(new PracticeReadyFragment.OnSetCompletedListener() {
            @Override
            public void onSetCompleted(int pos, int newCompletedSets) {
                // Lưu vào SharedPreferences
                List<PracticeExerciseItem> items = adapter.getExerciseItems();
                if (pos >= 0 && pos < items.size()) {
                    PracticeExerciseItem updatedItem = items.get(pos);
                    updatedItem.setCompletedSets(newCompletedSets);

                    // Save to storage
                    progressManager.saveCompletedSets(workoutDayId, updatedItem.getExercise().getId(), newCompletedSets);

                    adapter.notifyItemChanged(pos);

                    Log.d(TAG, "Callback - Saved completed sets: " + newCompletedSets
                            + " for exercise: " + updatedItem.getExercise().getExerciseName());
                }
            }
        });

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("PracticeReady")
                .commit();
    }
}