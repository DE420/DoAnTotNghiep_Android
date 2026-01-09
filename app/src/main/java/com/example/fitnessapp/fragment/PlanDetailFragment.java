package com.example.fitnessapp.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.adapter.PlanDayAdapter;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.PlanDayResponse;
import com.example.fitnessapp.model.response.PlanDetailResponse;
import com.example.fitnessapp.model.response.PlanWeekResponse;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.network.RetrofitClient;
import com.example.fitnessapp.session.SessionManager;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanDetailFragment extends Fragment {

    private static final String TAG = "PlanDetailFragment";
    private static final String ARG_PLAN_ID = "plan_id";

    // formatter
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Long planId;
    private PlanDetailResponse currentPlanDetail;
    private ImageView backButton;
    private TextView toolbarTitle;
    private TextView tvPlanName;
    private LinearLayout layoutStartDate;
    private TextView tvStartDate;
    private TextView tvLevel;
    private TextView tvGoal;
    private TextView tvDuration;
    private TextView tvDays;
    private Spinner spinnerWeek;
    private RecyclerView recyclerViewPlanDays;
    private Button btnCopyAndEdit, btnEditPlan, btnDeletePlan;
    private LinearLayout layoutActionButtons;
    private ProgressBar progressBar;

    private PlanDayAdapter planDayAdapter;
    private SessionManager sessionManager;

    // Static factory method
    public static PlanDetailFragment newInstance(Long planId) {
        PlanDetailFragment fragment = new PlanDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PLAN_ID, planId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            planId = getArguments().getLong(ARG_PLAN_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plan_detail, container, false);

        sessionManager = SessionManager.getInstance(requireContext());

        backButton = view.findViewById(R.id.back_button_plan_detail);
        toolbarTitle = view.findViewById(R.id.toolbar_title_plan_detail);
        tvPlanName = view.findViewById(R.id.tv_plan_detail_name);
        layoutStartDate = view.findViewById(R.id.layout_start_date);
        tvStartDate = view.findViewById(R.id.tv_plan_detail_start_date);
        tvLevel = view.findViewById(R.id.tv_plan_detail_level);
        tvGoal = view.findViewById(R.id.tv_plan_detail_goal);
        tvDuration = view.findViewById(R.id.tv_plan_detail_duration);
        tvDays = view.findViewById(R.id.tv_plan_detail_days);
        spinnerWeek = view.findViewById(R.id.spinner_plan_detail_week);
        recyclerViewPlanDays = view.findViewById(R.id.recycler_view_plan_days);
        btnCopyAndEdit = view.findViewById(R.id.btn_copy_and_edit_plan);
        btnEditPlan = view.findViewById(R.id.btn_edit_plan);
        btnDeletePlan = view.findViewById(R.id.btn_delete_plan);
        layoutActionButtons = view.findViewById(R.id.layout_action_buttons);
        progressBar = view.findViewById(R.id.progress_bar_plan_detail);

        // Configure RecyclerView for Plan Days
        recyclerViewPlanDays.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewPlanDays.setNestedScrollingEnabled(false); // Important inside NestedScrollView

        // Setup back button
        backButton.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                Toast.makeText(getContext(), "Previous page not found.", Toast.LENGTH_SHORT).show();
            }
        });

        // Fetch plan details if ID is available
        if (planId != null) {
            fetchPlanDetail(planId);
        } else {
            Toast.makeText(getContext(), "No plan found.", Toast.LENGTH_SHORT).show();
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        }

        btnCopyAndEdit.setOnClickListener(v -> {
            if (currentPlanDetail != null) {
                copyAndEditPlan(currentPlanDetail.getId());
            } else {
                Toast.makeText(getContext(), "Plan data not available", Toast.LENGTH_SHORT).show();
            }
        });

        btnEditPlan.setOnClickListener(v -> {
            if (currentPlanDetail != null) {
                // Just pass the plan ID
                CreatePlanFragment editFragment = CreatePlanFragment.newInstanceForEdit(currentPlanDetail.getId());
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, editFragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                Toast.makeText(getContext(), "Plan data not available", Toast.LENGTH_SHORT).show();
            }
        });

        btnDeletePlan.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });

        return view;
    }

    private void fetchPlanDetail(Long id) {
        progressBar.setVisibility(View.VISIBLE);

        String accessToken = sessionManager.getAccessToken();
        String authorizationHeader = null;

        if (accessToken != null && !accessToken.isEmpty()) {
            authorizationHeader = "Bearer " + accessToken;
        } else {
            Toast.makeText(getContext(), "Token unavailable. Please login again.", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        Call<ApiResponse<PlanDetailResponse>> call = apiService.getPlanDetail(authorizationHeader, id);

        call.enqueue(new Callback<ApiResponse<PlanDetailResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<PlanDetailResponse>> call, @NonNull Response<ApiResponse<PlanDetailResponse>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<PlanDetailResponse> apiResponse = response.body();
                    if (apiResponse.isStatus()) {
                        currentPlanDetail = apiResponse.getData();
                        if (currentPlanDetail != null) {
                            displayPlanDetail(currentPlanDetail);
                        } else {
                            Toast.makeText(getContext(), "Plan detail not found.", Toast.LENGTH_SHORT).show();
                            if (getParentFragmentManager().getBackStackEntryCount() > 0) getParentFragmentManager().popBackStack();
                        }
                    } else {
                        Toast.makeText(getContext(), "API Error: " + apiResponse.getData(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "API Error: " + apiResponse.getData());
                    }
                } else {
                    if (response.code() == 401) {
                        Toast.makeText(getContext(), "Session expired. Please login again.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                    Log.e(TAG, "Server error: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<PlanDetailResponse>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Connection error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Connection error: " + t.getMessage(), t);
            }
        });
    }

    private void displayPlanDetail(PlanDetailResponse detail) {
        toolbarTitle.setText(detail.getName());
        tvPlanName.setText(detail.getName());

        if (detail.getDefault() != null && !detail.getDefault()) {
            layoutStartDate.setVisibility(View.VISIBLE);
            if (detail.getStartDate() != null) {
                tvStartDate.setText(detail.getStartDate().format(DATE_FORMATTER));
            } else {
                tvStartDate.setText("N/A");
            }

            // Show Edit/Delete buttons for My Plan
            btnEditPlan.setVisibility(View.VISIBLE);
            btnDeletePlan.setVisibility(View.VISIBLE);
            btnCopyAndEdit.setVisibility(View.GONE);
        } else {
            layoutStartDate.setVisibility(View.GONE);
            btnCopyAndEdit.setVisibility(View.VISIBLE);
            btnEditPlan.setVisibility(View.GONE);
            btnDeletePlan.setVisibility(View.GONE);
        }

        tvLevel.setText(detail.getDifficultyLevel() != null ? getString(detail.getDifficultyLevel().getResId()) : "N/A");
        tvGoal.setText(detail.getTargetGoal() != null ? getString(detail.getTargetGoal().getResId()) : "N/A");
        tvDuration.setText(detail.getDurationWeek() != null ? detail.getDurationWeek() + " weeks" : "N/A");

        // Display Days (e.g., "Monday, Thursday, Friday")
        if (detail.getDaysPerWeek() != null && detail.getWeeks() != null && !detail.getWeeks().isEmpty()) {
            List<String> activeDays = detail.getWeeks().stream()
                    .flatMap(week -> week.getDays().stream())
                    .filter(day -> day.getExercises() != null && !day.getExercises().isEmpty())
                    .map(day -> getDayName(day.getDayOfWeek()))
                    .distinct()
                    .sorted()   // Sắp xếp theo thứ tự bảng chữ cái hoặc bạn có thể tự sắp xếp theo thứ tự ngày trong tuần
                    .collect(Collectors.toList());

            if (!activeDays.isEmpty()) {
                tvDays.setText(String.join(", ", activeDays));
            } else {
                tvDays.setText("No active days");
            }
        } else {
            tvDays.setText("N/A");
        }

        // Setup Week Spinner
        if (detail.getWeeks() != null && !detail.getWeeks().isEmpty()) {
            List<String> weekNumbers = new ArrayList<>();
            for (int i = 1; i <= detail.getWeeks().size(); i++) {
                weekNumbers.add(String.valueOf(i));
            }
            ArrayAdapter<String> weekAdapter = new ArrayAdapter<>(requireContext(),
                    R.layout.spinner_item_white_text,
                    weekNumbers);
            weekAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_white_text);
            spinnerWeek.setAdapter(weekAdapter);

            spinnerWeek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // position là index của tuần (0-indexed)
                    if (currentPlanDetail.getWeeks() != null && currentPlanDetail.getWeeks().size() > position) {
                        PlanWeekResponse selectedWeek = currentPlanDetail.getWeeks().get(position);
                        planDayAdapter = new PlanDayAdapter(selectedWeek.getDays());
                        recyclerViewPlanDays.setAdapter(planDayAdapter);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Do nothing
                }
            });

            // Trigger initial selection to load first week's exercises
            if (!weekNumbers.isEmpty()) {
                spinnerWeek.setSelection(0);
            }

        } else {
            // No weeks available
            spinnerWeek.setEnabled(false);
            Toast.makeText(getContext(), "No week data found.", Toast.LENGTH_SHORT).show();
            recyclerViewPlanDays.setAdapter(new PlanDayAdapter(new ArrayList<>())); // Empty adapter
        }
    }

    private String getDayName(int dayOfWeek) {
        // dayOfWeek từ backend thường là 1=Monday, 2=Tuesday, ..., 7=Sunday
        switch (dayOfWeek) {
            case 1: return "Monday";
            case 2: return "Tuesday";
            case 3: return "Wednesday";
            case 4: return "Thursday";
            case 5: return "Friday";
            case 6: return "Saturday";
            case 0: return "Sunday";
            default: return "Day " + dayOfWeek;
        }
    }

    private void copyAndEditPlan(Long planIdToCopy) {
        progressBar.setVisibility(View.VISIBLE);

        String accessToken = sessionManager.getAccessToken();
        String authorizationHeader = null;

        if (accessToken != null && !accessToken.isEmpty()) {
            authorizationHeader = "Bearer " + accessToken;
        } else {
            Toast.makeText(getContext(), "Token unavailable. Please login again.", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        Call<ApiResponse<Long>> call = apiService.copyPlan(authorizationHeader, planIdToCopy);

        call.enqueue(new Callback<ApiResponse<Long>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Long>> call, @NonNull Response<ApiResponse<Long>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Long> apiResponse = response.body();
                    if (apiResponse.isStatus() && apiResponse.getData() != null) {
                        Long newPlanId = apiResponse.getData();
                        Toast.makeText(getContext(), "Plan copied successfully!", Toast.LENGTH_SHORT).show();

                        // Navigate to edit screen with the new plan ID
                        CreatePlanFragment editFragment = CreatePlanFragment.newInstanceForEdit(newPlanId);
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, editFragment)
                                .addToBackStack(null)
                                .commit();

                        Log.d(TAG, "Copied plan ID: " + newPlanId);
                    } else {
                        String errorMessage = "Failed to copy plan";
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Copy plan API returned false status or null data");
                    }
                } else {
                    if (response.code() == 401) {
                        Toast.makeText(getContext(), "Session expired. Please login again.", Toast.LENGTH_LONG).show();
                    } else if (response.code() == 403) {
                        Toast.makeText(getContext(), "You don't have permission to copy this plan.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                    Log.e(TAG, "Server error on copy plan: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Long>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Network error on copy plan: " + t.getMessage(), t);
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        if (currentPlanDetail == null || currentPlanDetail.getId() == null) {
            Toast.makeText(getContext(), "Delete failed. Plan not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                .setTitle("Confirm delete plan")
                .setMessage("Are you sure you want to delete the plan \"" + currentPlanDetail.getName() + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deletePlan(currentPlanDetail.getId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePlan(Long planIdToDelete) {
        progressBar.setVisibility(View.VISIBLE);

        String accessToken = sessionManager.getAccessToken();
        String authorizationHeader = null;

        if (accessToken != null && !accessToken.isEmpty()) {
            authorizationHeader = "Bearer " + accessToken;
        } else {
            Toast.makeText(getContext(), "Token unavailable. Please login again.", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        Call<ApiResponse<Boolean>> call = apiService.deletePlan(authorizationHeader, planIdToDelete);

        call.enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Boolean>> call, @NonNull Response<ApiResponse<Boolean>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Boolean> apiResponse = response.body();
                    if (apiResponse.isStatus() && apiResponse.getData() != null && apiResponse.getData()) {
                        Toast.makeText(getContext(), "Plan \"" + currentPlanDetail.getName() + "\" deleted successfully!", Toast.LENGTH_SHORT).show();
                        // Navigate back to the previous fragment (PlanFragment)
                        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                            getParentFragmentManager().popBackStack();
                        }
                    } else {
                        String errorMessage = apiResponse.getData() != null ? String.valueOf(apiResponse.getData()) : "Unknown error.";
                        Toast.makeText(getContext(), "Plan delete failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Delete plan API returned false status or null data: " + errorMessage);
                    }
                } else {
                    if (response.code() == 401) {
                        Toast.makeText(getContext(), "Session expired. Please login again.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Server error: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                    Log.e(TAG, "Server error on delete plan: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Boolean>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error on delete plan: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Network error on delete plan: " + t.getMessage(), t);
            }
        });
    }
}