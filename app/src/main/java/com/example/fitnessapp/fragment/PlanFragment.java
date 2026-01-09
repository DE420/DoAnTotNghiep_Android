package com.example.fitnessapp.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.adapter.PlanAdapter;
import com.example.fitnessapp.enums.DifficultyLevel;
import com.example.fitnessapp.enums.FitnessGoal;
import com.example.fitnessapp.model.request.PlanSearchRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.SelectOptions;
import com.example.fitnessapp.model.response.PlanResponse;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.network.RetrofitClient;
import com.example.fitnessapp.session.SessionManager;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlanFragment extends Fragment implements PlanAdapter.OnItemClickListener {

    private static final String TAG = "PlanFragment";

    private MaterialButtonToggleGroup toggleGroup;
    private Button btnPresetPlan, btnMyPlan;
    private EditText etSearchName, etSearchDuration;
    private Spinner spinnerGoal, spinnerLevel;
    private Button btnPlanSearch;
    private RecyclerView recyclerViewPlans;
    private Button btnAddMorePlan;
    private ProgressBar progressBarPlan;

    private PlanAdapter planAdapter;
    private SessionManager sessionManager;

    private boolean isShowingPresetPlans = true;

    // Pagination
    private int currentPage = 0;
    private final int pageSize = 10;
    private boolean isLoading = false;
    private boolean hasMorePages = true;

    // Current search parameters
    private String currentSearchName = null;
    private FitnessGoal currentSearchGoal = null;
    private DifficultyLevel currentSearchLevel = null;
    private Integer currentSearchDuration = null;

    // Adapters for Spinners
    private ArrayAdapter<SelectOptions> goalAdapter;
    private ArrayAdapter<SelectOptions> levelAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plan, container, false);

        sessionManager = SessionManager.getInstance(requireContext());

        // Initialize Views
        toggleGroup = view.findViewById(R.id.layout_plan_toggle);
        btnPresetPlan = view.findViewById(R.id.btn_preset_plan);
        btnMyPlan = view.findViewById(R.id.btn_my_plan);
        etSearchName = view.findViewById(R.id.et_search_plan_name);
        etSearchDuration = view.findViewById(R.id.et_search_plan_duration);
        spinnerGoal = view.findViewById(R.id.spinner_plan_goal);
        spinnerLevel = view.findViewById(R.id.spinner_plan_level);
        btnPlanSearch = view.findViewById(R.id.btn_plan_search);
        recyclerViewPlans = view.findViewById(R.id.recycler_view_plans);
        btnAddMorePlan = view.findViewById(R.id.btn_add_more_plan);
        progressBarPlan = view.findViewById(R.id.progress_bar_plan);

        // Configure RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewPlans.setLayoutManager(layoutManager);
        planAdapter = new PlanAdapter(this);
        recyclerViewPlans.setAdapter(planAdapter);

        // Initialize Spinner Adapters
        setupSpinners();

        // Setup MaterialButtonToggleGroup listener
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_preset_plan) {
                    showPlans(true);
                } else if (checkedId == R.id.btn_my_plan) {
                    showPlans(false);
                }
            }
        });

        // Initial state: check Preset Plan button
        toggleGroup.check(R.id.btn_preset_plan);

        // Search button listener
        btnPlanSearch.setOnClickListener(v -> {
            currentPage = 0;
            hasMorePages = true;
            isLoading = false;

            applySearchFilters();
            fetchPlans(isShowingPresetPlans, true);
        });

        // Add More button listener
        btnAddMorePlan.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CreatePlanFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // RecyclerView scroll listener for pagination
        recyclerViewPlans.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null &&
                        layoutManager.findLastCompletelyVisibleItemPosition() == planAdapter.getItemCount() - 1 &&
                        hasMorePages &&
                        !isLoading) {
                    currentPage++;
                    fetchPlans(isShowingPresetPlans, false);
                }
            }
        });

        // Initial load
        fetchPlans(true, true);

        return view;
    }

    private void setupSpinners() {
        // Goal Spinner
        List<SelectOptions> goalOptions = new ArrayList<>();
        goalOptions.add(new SelectOptions(null, "(Choose one)"));
        for (FitnessGoal goal : FitnessGoal.values()) {
            goalOptions.add(new SelectOptions((long) goal.ordinal(), getString(goal.getResId())));
        }
        goalAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item_white_text, goalOptions);
        goalAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_white_text);
        spinnerGoal.setAdapter(goalAdapter);

        // Level Spinner
        List<SelectOptions> levelOptions = new ArrayList<>();
        levelOptions.add(new SelectOptions(null, "(Choose one)"));
        for (DifficultyLevel level : DifficultyLevel.values()) {
            levelOptions.add(new SelectOptions((long) level.ordinal(), getString(level.getResId())));
        }
        levelAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item_white_text, levelOptions);
        levelAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_white_text);
        spinnerLevel.setAdapter(levelAdapter);

        // Set up Spinner listeners
        spinnerGoal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SelectOptions selectedOption = (SelectOptions) parent.getItemAtPosition(position);
                if (selectedOption.getId() != null) {
                    currentSearchGoal = FitnessGoal.values()[selectedOption.getId().intValue()];
                } else {
                    currentSearchGoal = null;
                }
                Log.d(TAG, "Selected Goal: " + currentSearchGoal);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentSearchGoal = null;
            }
        });

        spinnerLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SelectOptions selectedOption = (SelectOptions) parent.getItemAtPosition(position);
                if (selectedOption.getId() != null) {
                    currentSearchLevel = DifficultyLevel.values()[selectedOption.getId().intValue()];
                } else {
                    currentSearchLevel = null;
                }
                Log.d(TAG, "Selected Level: " + currentSearchLevel);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentSearchLevel = null;
            }
        });
    }

    private void showPlans(boolean showPreset) {
        if (isShowingPresetPlans == showPreset) return;

        isShowingPresetPlans = showPreset;
        planAdapter.setIsMyPlanView(!showPreset);

        // Update Add More button visibility
        if (showPreset) {
            btnAddMorePlan.setVisibility(View.GONE);
        } else {
            btnAddMorePlan.setVisibility(View.VISIBLE);
        }

        // Reset pagination and fetch new data
        currentPage = 0;
        hasMorePages = true;
        isLoading = false;
        fetchPlans(isShowingPresetPlans, true);
    }

    private void applySearchFilters() {
        currentSearchName = etSearchName.getText().toString().trim();
        if (currentSearchName.isEmpty()) {
            currentSearchName = null;
        }

        String durationStr = etSearchDuration.getText().toString().trim();
        if (!durationStr.isEmpty()) {
            try {
                currentSearchDuration = Integer.parseInt(durationStr);
            } catch (NumberFormatException e) {
                currentSearchDuration = null;
                Toast.makeText(getContext(), "Please enter a valid number for Duration.", Toast.LENGTH_SHORT).show();
            }
        } else {
            currentSearchDuration = null;
        }
    }

    private void fetchPlans(boolean isPreset, boolean clearExisting) {
        if (isLoading) return;
        isLoading = true;
        progressBarPlan.setVisibility(View.VISIBLE);

        String accessToken = sessionManager.getAccessToken();
        String authorizationHeader = null;

        if (accessToken != null && !accessToken.isEmpty()) {
            authorizationHeader = "Bearer " + accessToken;
        } else {
            Toast.makeText(getContext(), "Token unavailable. Please login again.", Toast.LENGTH_LONG).show();
            isLoading = false;
            progressBarPlan.setVisibility(View.GONE);
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        Call<ApiResponse<List<PlanResponse>>> call;

        if (isPreset) {
            call = apiService.getSampleWorkoutPlans(
                    authorizationHeader,
                    currentSearchName,
                    currentSearchGoal,
                    currentSearchLevel,
                    currentSearchDuration,
                    currentPage,
                    pageSize
            );
        } else {
            call = apiService.getMyWorkoutPlans(
                    authorizationHeader,
                    currentSearchName,
                    currentSearchGoal,
                    currentSearchLevel,
                    currentSearchDuration,
                    currentPage,
                    pageSize
            );
        }

        call.enqueue(new Callback<ApiResponse<List<PlanResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<PlanResponse>>> call, @NonNull Response<ApiResponse<List<PlanResponse>>> response) {
                isLoading = false;
                progressBarPlan.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<PlanResponse>> apiResponse = response.body();
                    if (apiResponse.isStatus()) {
                        List<PlanResponse> plans = apiResponse.getData();

                        if (clearExisting) {
                            planAdapter.setPlans(plans);
                        } else {
                            planAdapter.addPlans(plans);
                        }

                        // Handle pagination metadata
                        if (apiResponse.getMeta() != null) {
                            hasMorePages = apiResponse.getMeta().isHasMore();
                            if (plans == null || plans.isEmpty()) {
                                hasMorePages = false;
                            }
                        } else {
                            hasMorePages = plans != null && plans.size() == pageSize;
                        }

                        // Show message if no plans found
                        if ((plans == null || plans.isEmpty()) && clearExisting) {
                            Toast.makeText(getContext(), "No plans found.", Toast.LENGTH_SHORT).show();
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
            public void onFailure(@NonNull Call<ApiResponse<List<PlanResponse>>> call, @NonNull Throwable t) {
                isLoading = false;
                progressBarPlan.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Connection error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Connection error: " + t.getMessage(), t);
            }
        });
    }

    @Override
    public void onItemClick(PlanResponse plan) {
        if (plan != null && plan.getId() != null) {
            PlanDetailFragment detailFragment = PlanDetailFragment.newInstance(plan.getId());
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detailFragment) // Thay thế ID container của bạn
                    .addToBackStack(null)
                    .commit();
        } else {
            Toast.makeText(getContext(), "Chi tiết kế hoạch không có sẵn.", Toast.LENGTH_SHORT).show();
        }
    }
}