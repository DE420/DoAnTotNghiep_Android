package com.example.fitnessapp.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.adapter.PracticePlanAdapter;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.PlanResponse;
import com.example.fitnessapp.model.response.WorkoutDayDetailResponse;
import com.example.fitnessapp.network.RetrofitClient;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.session.SessionManager;
import com.example.fitnessapp.utils.WorkoutProgressManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PracticeFragment extends Fragment implements PracticePlanAdapter.OnPlanClickListener {

    private static final String TAG = "PracticeFragment";

    private RecyclerView recyclerViewPlans;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private PracticePlanAdapter adapter;
    private ApiService apiService;
    private SessionManager sessionManager;
    private WorkoutProgressManager progressManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_practice, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        loadTodayPlans();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTodayPlans();
    }

    private void initViews(View view) {
        recyclerViewPlans = view.findViewById(R.id.recyclerViewPlans);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        apiService = RetrofitClient.getApiService();
        sessionManager = SessionManager.getInstance(requireContext());
        progressManager = WorkoutProgressManager.getInstance(requireContext());
    }

    private void setupRecyclerView() {
        adapter = new PracticePlanAdapter(this);
        recyclerViewPlans.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewPlans.setAdapter(adapter);
    }

    private void loadTodayPlans() {
        String accessToken = sessionManager.getAccessToken();
        String authorizationHeader = null;

        if (accessToken != null && !accessToken.isEmpty()) {
            authorizationHeader = "Bearer " + accessToken;
        } else {
            Toast.makeText(getContext(), "Token expired.", Toast.LENGTH_LONG).show();
            return;
        }

        // Lấy ngày hiện tại theo định dạng dd/MM/yyyy
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        showLoading(true);

        String finalAuthHeader = authorizationHeader;
        apiService.getPlansByDate(authorizationHeader, currentDate)
                .enqueue(new Callback<ApiResponse<List<PlanResponse>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<PlanResponse>>> call, @NonNull Response<ApiResponse<List<PlanResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<PlanResponse>> apiResponse = response.body();
                            if (apiResponse.isStatus()) {
                                List<PlanResponse> plans = apiResponse.getData();

                                if (plans != null && !plans.isEmpty()) {
                                    // Lấy số lượng bài tập cho từng plan
                                    loadExerciseCountsForPlans(plans, finalAuthHeader);
                                } else {
                                    showLoading(false);
                                    showEmptyState(true);
                                }
                            } else {
                                showLoading(false);
                                Toast.makeText(getContext(), "API Error: " + apiResponse.getData(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "API Error: " + apiResponse.getData());
                                showEmptyState(true);
                            }
                        } else {
                            showLoading(false);
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
                    public void onFailure(@NonNull Call<ApiResponse<List<PlanResponse>>> call, @NonNull Throwable t) {
                        showLoading(false);
                        Toast.makeText(getContext(), "Connection error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Connection error: " + t.getMessage(), t);
                        showEmptyState(true);
                    }
                });
    }

    private void loadExerciseCountsForPlans(List<PlanResponse> plans, String authorizationHeader) {
        final int[] completedRequests = {0};
        final int totalRequests = plans.size();

        for (PlanResponse plan : plans) {
            // NEW: Kiểm tra trạng thái hoàn thành từ SharedPreferences
            if (plan.getCurrentWorkoutDayId() != null) {
                boolean isCompleted = progressManager.isWorkoutDayCompleted(plan.getCurrentWorkoutDayId());
                plan.setCompleted(isCompleted);
                Log.d(TAG, "Plan: " + plan.getName() + ", WorkoutDayId: " + plan.getCurrentWorkoutDayId()
                        + ", isCompleted: " + isCompleted);
            }

            if (plan.getCurrentWorkoutDayId() != null) {
                apiService.getExercisesByDay(authorizationHeader, plan.getCurrentWorkoutDayId())
                        .enqueue(new Callback<ApiResponse<WorkoutDayDetailResponse>>() {
                            @Override
                            public void onResponse(@NonNull Call<ApiResponse<WorkoutDayDetailResponse>> call, @NonNull Response<ApiResponse<WorkoutDayDetailResponse>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    ApiResponse<WorkoutDayDetailResponse> apiResponse = response.body();
                                    if (apiResponse.isStatus() && apiResponse.getData() != null) {
                                        WorkoutDayDetailResponse dayDetail = apiResponse.getData();
                                        if (dayDetail.getExercises() != null) {
                                            plan.setExerciseCount(dayDetail.getExercises().size());
                                        } else {
                                            plan.setExerciseCount(0);
                                        }
                                    } else {
                                        plan.setExerciseCount(0);
                                        Log.e(TAG, "Failed to get exercises for day: " + plan.getCurrentWorkoutDayId());
                                    }
                                } else {
                                    plan.setExerciseCount(0);
                                    Log.e(TAG, "Error loading exercises for plan: " + response.code());
                                }

                                completedRequests[0]++;
                                if (completedRequests[0] == totalRequests) {
                                    updateUIWithPlans(plans);
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<ApiResponse<WorkoutDayDetailResponse>> call, @NonNull Throwable t) {
                                plan.setExerciseCount(0);
                                Log.e(TAG, "Connection error loading exercises: " + t.getMessage(), t);

                                completedRequests[0]++;
                                if (completedRequests[0] == totalRequests) {
                                    updateUIWithPlans(plans);
                                }
                            }
                        });
            } else {
                plan.setExerciseCount(0);
                completedRequests[0]++;
                if (completedRequests[0] == totalRequests) {
                    updateUIWithPlans(plans);
                }
            }
        }
    }

    private void updateUIWithPlans(List<PlanResponse> plans) {
        showLoading(false);
        adapter.setPlanList(plans);
        showEmptyState(false);
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerViewPlans.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean isEmpty) {
        tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerViewPlans.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onPlayClick(PlanResponse plan) {
        if (plan.getCurrentWorkoutDayId() == null) {
            Toast.makeText(getContext(), "Không có bài tập cho ngày hôm nay", Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigate to exercise list
        PracticeExerciseListFragment fragment = PracticeExerciseListFragment.newInstance(
                plan.getName(),
                plan.getCurrentWorkoutDayId()
        );

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("PracticeExerciseList")
                .commit();

        Log.d(TAG, "Navigating to exercises for plan: " + plan.getName() + ", Day ID: " + plan.getCurrentWorkoutDayId());
    }
}