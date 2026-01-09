package com.example.fitnessapp.fragment;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fitnessapp.R;
import com.example.fitnessapp.adapter.WorkoutDayLogAdapter;
import com.example.fitnessapp.databinding.FragmentStatisticsBinding;
import com.example.fitnessapp.model.WorkoutDayLog;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.ExerciseResponse;
import com.example.fitnessapp.model.response.StatisticsResponse;
import com.example.fitnessapp.model.response.WorkoutHistoryResponse;
import com.example.fitnessapp.model.response.WorkoutLogResponse;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.network.RetrofitClient;
import com.example.fitnessapp.session.SessionManager;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatisticsFragment extends Fragment {
    private static final String TAG = "StatisticsFragment";

    private FragmentStatisticsBinding binding;
    private WorkoutDayLogAdapter workoutDayLogAdapter;
    private ApiService apiService;
    private SessionManager sessionManager;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private Calendar calendar = Calendar.getInstance();
    private List<ExerciseResponse> exerciseList = new ArrayList<>();
    private Map<String, Long> exerciseMap = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = SessionManager.getInstance(requireContext());
        apiService = RetrofitClient.getApiService();

        setupRecyclerView();
        setupListeners();
        loadExercises();
        loadStatistics();
        loadWorkoutLogs();
    }

    private void setupRecyclerView() {
        binding.recyclerViewWorkoutLogs.setLayoutManager(new LinearLayoutManager(requireContext()));
        workoutDayLogAdapter = new WorkoutDayLogAdapter(new ArrayList<>());
        binding.recyclerViewWorkoutLogs.setAdapter(workoutDayLogAdapter);
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });

        binding.etStartDate.setOnClickListener(v -> showDatePicker(binding.etStartDate));
        binding.etEndDate.setOnClickListener(v -> showDatePicker(binding.etEndDate));

        binding.btnSearch.setOnClickListener(v -> searchWorkoutLogs());
    }

    private void showDatePicker(android.widget.EditText editText) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    editText.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private String getAuthorizationHeader() {
        String accessToken = sessionManager.getAccessToken();
        if (accessToken != null && !accessToken.isEmpty()) {
            return "Bearer " + accessToken;
        } else {
            Toast.makeText(getContext(), "Token unavailable. Please login again.", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private void loadExercises() {
        String authorizationHeader = getAuthorizationHeader();
        if (authorizationHeader == null) return;

        apiService.getAllExercises(authorizationHeader, null, null, null, null, 0, 1000)
                .enqueue(new Callback<ApiResponse<List<ExerciseResponse>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<ExerciseResponse>>> call,
                                           @NonNull Response<ApiResponse<List<ExerciseResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<ExerciseResponse>> apiResponse = response.body();
                            if (apiResponse.isStatus()) {
                                exerciseList = apiResponse.getData();
                                setupExerciseSpinner();
                            } else {
                                Toast.makeText(getContext(), "API Error: " + apiResponse.getData(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "API Error: " + apiResponse.getData());
                            }
                        } else {
                            handleErrorResponse(response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<ExerciseResponse>>> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Connection error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Connection error: " + t.getMessage(), t);
                    }
                });
    }

    private void setupExerciseSpinner() {
        List<String> exerciseNames = new ArrayList<>();
        exerciseNames.add("Tất cả");
        exerciseMap.put("Tất cả", null);

        for (ExerciseResponse exercise : exerciseList) {
            exerciseNames.add(exercise.getName());
            exerciseMap.put(exercise.getName(), exercise.getId());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                exerciseNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerExercise.setAdapter(adapter);
    }

    private void loadStatistics() {
        String authorizationHeader = getAuthorizationHeader();
        if (authorizationHeader == null) return;

        binding.progressBarStatistics.setVisibility(View.VISIBLE);

        apiService.getWorkoutLogStatistics(authorizationHeader)
                .enqueue(new Callback<ApiResponse<StatisticsResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<StatisticsResponse>> call,
                                           @NonNull Response<ApiResponse<StatisticsResponse>> response) {
                        binding.progressBarStatistics.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<StatisticsResponse> apiResponse = response.body();
                            if (apiResponse.isStatus()) {
                                updateStatisticsUI(apiResponse.getData());
                            } else {
                                Toast.makeText(getContext(), "API Error: " + apiResponse.getData(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "API Error: " + apiResponse.getData());
                            }
                        } else {
                            handleErrorResponse(response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<StatisticsResponse>> call, @NonNull Throwable t) {
                        binding.progressBarStatistics.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Connection error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Connection error: " + t.getMessage(), t);
                    }
                });
    }

    private void updateStatisticsUI(StatisticsResponse stats) {
        if (stats == null) {
            Toast.makeText(getContext(), "No statistics data available.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.tvTotalCalories.setText(String.valueOf((int) stats.getTotalCalories()));
        binding.tvTotalSessions.setText(String.valueOf(stats.getTotalWorkouts()));
        binding.tvCurrentStreak.setText(String.valueOf(stats.getCurrentStreak()));
        binding.tvLongestStreak.setText(String.valueOf(stats.getLongestStreak()));

        setupBarChart(stats.getCaloriesChart());
    }

    private void setupBarChart(List<StatisticsResponse.ChartDataResponseByDate> chartData) {
        if (chartData == null || chartData.isEmpty()) {
            binding.chartCalories.setNoDataText("Chưa có dữ liệu");
            binding.chartCalories.setNoDataTextColor(Color.WHITE);
            binding.chartCalories.invalidate();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> dates = new ArrayList<>();

        for (int i = 0; i < chartData.size(); i++) {
            StatisticsResponse.ChartDataResponseByDate data = chartData.get(i);
            entries.add(new BarEntry(i, (float) data.getValue()));
            dates.add(data.getDate());
        }

        BarDataSet dataSet = new BarDataSet(entries, "Calories");
        dataSet.setColor(Color.parseColor("#FFD700"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f);

        binding.chartCalories.setData(barData);
        binding.chartCalories.getDescription().setEnabled(false);
        binding.chartCalories.setFitBars(true);
        binding.chartCalories.getLegend().setEnabled(false);
        binding.chartCalories.getAxisRight().setEnabled(false);

        XAxis xAxis = binding.chartCalories.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dates));
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        binding.chartCalories.getAxisLeft().setTextColor(Color.WHITE);
        binding.chartCalories.getAxisLeft().setAxisMinimum(0f);
        binding.chartCalories.getAxisLeft().setDrawGridLines(true);
        binding.chartCalories.getAxisLeft().setGridColor(Color.GRAY);

        binding.chartCalories.invalidate();
    }

    private void loadWorkoutLogs() {
        String authorizationHeader = getAuthorizationHeader();
        if (authorizationHeader == null) return;

        String today = dateFormat.format(new Date());

        apiService.getLogsByDate(authorizationHeader, today)
                .enqueue(new Callback<ApiResponse<List<WorkoutLogResponse>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<WorkoutLogResponse>>> call,
                                           @NonNull Response<ApiResponse<List<WorkoutLogResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<WorkoutLogResponse>> apiResponse = response.body();
                            if (apiResponse.isStatus()) {
                                List<WorkoutLogResponse> logs = apiResponse.getData();
                                processWorkoutLogs(logs, today);
                            } else {
                                Toast.makeText(getContext(), "API Error: " + apiResponse.getData(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "API Error: " + apiResponse.getData());
                            }
                        } else {
                            handleErrorResponse(response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<WorkoutLogResponse>>> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Connection error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Connection error: " + t.getMessage(), t);
                    }
                });
    }

    private void searchWorkoutLogs() {
        String startDate = binding.etStartDate.getText().toString().trim();
        String endDate = binding.etEndDate.getText().toString().trim();

        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng chọn ngày bắt đầu và kết thúc", Toast.LENGTH_SHORT).show();
            return;
        }

        String authorizationHeader = getAuthorizationHeader();
        if (authorizationHeader == null) return;

        String selectedExercise = binding.spinnerExercise.getSelectedItem().toString();
        Long exerciseId = exerciseMap.get(selectedExercise);

        binding.progressBarLogs.setVisibility(View.VISIBLE);

        apiService.getWorkoutHistory(authorizationHeader, startDate, endDate, exerciseId)
                .enqueue(new Callback<ApiResponse<List<WorkoutHistoryResponse>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<WorkoutHistoryResponse>>> call,
                                           @NonNull Response<ApiResponse<List<WorkoutHistoryResponse>>> response) {
                        binding.progressBarLogs.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<List<WorkoutHistoryResponse>> apiResponse = response.body();
                            if (apiResponse.isStatus()) {
                                List<WorkoutHistoryResponse> historyList = apiResponse.getData();
                                processWorkoutHistory(historyList);

                                if (historyList == null || historyList.isEmpty()) {
                                    Toast.makeText(getContext(), "No workout history found.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getContext(), "API Error: " + apiResponse.getData(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "API Error: " + apiResponse.getData());
                            }
                        } else {
                            handleErrorResponse(response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<WorkoutHistoryResponse>>> call, @NonNull Throwable t) {
                        binding.progressBarLogs.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Connection error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Connection error: " + t.getMessage(), t);
                    }
                });
    }

    private void processWorkoutHistory(List<WorkoutHistoryResponse> historyList) {
        if (historyList == null) {
            workoutDayLogAdapter.updateData(new ArrayList<>());
            return;
        }

        List<WorkoutDayLog> dayLogs = new ArrayList<>();

        for (WorkoutHistoryResponse history : historyList) {
            List<WorkoutDayLog.WorkoutExerciseLog> exercises = new ArrayList<>();

            if (history.getExercises() != null) {
                for (WorkoutHistoryResponse.ExerciseHistorySummary exercise : history.getExercises()) {
                    exercises.add(new WorkoutDayLog.WorkoutExerciseLog(
                            exercise.getExerciseName(),
                            exercise.getTotalReps(),
                            exercise.getTotalSets(),
                            (int) exercise.getTotalCalories()
                    ));
                }
            }

            dayLogs.add(new WorkoutDayLog(history.getDate(), exercises));
        }

        workoutDayLogAdapter.updateData(dayLogs);
    }

    private void processWorkoutLogs(List<WorkoutLogResponse> logs, String date) {
        if (logs == null || logs.isEmpty()) {
            workoutDayLogAdapter.updateData(new ArrayList<>());
            return;
        }

        Map<String, List<WorkoutDayLog.WorkoutExerciseLog>> groupedLogs = new LinkedHashMap<>();

        for (WorkoutLogResponse log : logs) {
            String exerciseName = log.getExerciseName();

            if (!groupedLogs.containsKey(exerciseName)) {
                groupedLogs.put(exerciseName, new ArrayList<>());
            }

            groupedLogs.get(exerciseName).add(new WorkoutDayLog.WorkoutExerciseLog(
                    exerciseName,
                    log.getReps() != null ? log.getReps() : 0,
                    log.getSetNumber() != null ? log.getSetNumber() : 0,
                    log.getDuration(),
                    log.getWeight(),
                    log.getCaloriesBurned() != null ? log.getCaloriesBurned().intValue() : 0
            ));
        }

        List<WorkoutDayLog> dayLogs = new ArrayList<>();
        if (!groupedLogs.isEmpty()) {
            List<WorkoutDayLog.WorkoutExerciseLog> allExercises = new ArrayList<>();
            for (List<WorkoutDayLog.WorkoutExerciseLog> exercises : groupedLogs.values()) {
                if (!exercises.isEmpty()) {
                    allExercises.add(exercises.get(0));
                }
            }
            dayLogs.add(new WorkoutDayLog(date, allExercises));
        }

        workoutDayLogAdapter.updateData(dayLogs);
    }

    private void handleErrorResponse(int code) {
        if (code == 401) {
            Toast.makeText(getContext(), "Session expired. Please login again.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), "Server error: " + code, Toast.LENGTH_SHORT).show();
        }
        Log.e(TAG, "Server error: " + code);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}