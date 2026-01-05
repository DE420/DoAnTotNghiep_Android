package com.example.fitnessapp.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.example.fitnessapp.adapter.CreatePlanScheduleAdapter;
import com.example.fitnessapp.adapter.ExerciseSearchAdapter;
import com.example.fitnessapp.enums.DifficultyLevel;
import com.example.fitnessapp.enums.FitnessGoal;
import com.example.fitnessapp.model.request.CreatePlanRequest;
import com.example.fitnessapp.model.request.PlanDayRequest;
import com.example.fitnessapp.model.request.PlanExerciseRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.ExerciseResponse;
import com.example.fitnessapp.model.response.PlanDayResponse;
import com.example.fitnessapp.model.response.PlanDetailResponse;
import com.example.fitnessapp.model.response.PlanExerciseDetailResponse;
import com.example.fitnessapp.model.response.PlanWeekResponse;
import com.example.fitnessapp.model.response.SelectOptions;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.network.RetrofitClient;
import com.example.fitnessapp.session.SessionManager;
import com.google.gson.Gson;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap; // Keep insertion order
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreatePlanFragment extends Fragment implements CreatePlanScheduleAdapter.OnItemInteractionListener {

    private static final String TAG = "CreatePlanFragment";
    private static final String ARG_PLAN_ID = "plan_id";

    // UI elements
    private ImageView backButton;
    private EditText etPlanName, etStartDate, etDuration;
    private Spinner spinnerLevel, spinnerGoal, spinnerExerciseWeek;
    private CheckBox cbMon, cbTue, cbWed, cbThu, cbFri, cbSat, cbSun;
    private RecyclerView recyclerViewSchedule;
    private Button btnSavePlan;
    private ProgressBar progressBar;

    private SessionManager sessionManager;
    private ApiService apiService;

    // Adapters for Spinners
    private ArrayAdapter<SelectOptions> levelAdapter;
    private ArrayAdapter<SelectOptions> goalAdapter;
    private ArrayAdapter<String> exerciseWeekAdapter; // For week spinner

    // Data for plan creation
    private DifficultyLevel selectedLevel = null;
    private FitnessGoal selectedGoal = null;
    private LocalDate selectedStartDate = null;
    private int planDurationWeeks = 1; // Default to 1 week
    private Map<Integer, Boolean> selectedDaysOfWeek = new LinkedHashMap<>(); // 1=Mon, 7=Sun
    private List<CheckBox> dayOfWeekCheckBoxes;

    // Schedule data structure for RecyclerView
    // Map<WeekNumber, Map<DayOfWeek, List<PlanExerciseRequest>>>
    private Map<Integer, Map<Integer, List<PlanExerciseRequest>>> planScheduleData = new LinkedHashMap<>();

    private CreatePlanScheduleAdapter scheduleAdapter;
    private List<ScheduleItem> currentWeekScheduleItems = new ArrayList<>(); // Items for the currently selected week
    private int currentSelectedWeek = 1; // Default to week 1

    // Data for exercise search
    private List<ExerciseResponse> allExercises = new ArrayList<>(); // Cache all exercises for search

    // For editing existing plan (optional, for later extension)
    private Long editingPlanId = null;
    private PlanDetailResponse editingPlanDetail = null;
    private boolean isEditing = false;


    public CreatePlanFragment() {
        // Required empty public constructor
    }

    public static CreatePlanFragment newInstance(Long planIdToEdit) {
        CreatePlanFragment fragment = new CreatePlanFragment();
        if (planIdToEdit != null) {
            Bundle args = new Bundle();
            args.putLong("planId", planIdToEdit);
            fragment.setArguments(args);
        }
        return fragment;
    }

    public static CreatePlanFragment newInstanceForEdit(Long planId) {
        CreatePlanFragment fragment = new CreatePlanFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PLAN_ID, planId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = SessionManager.getInstance(requireContext());
        apiService = RetrofitClient.getApiService();

        // Just store the plan ID here, don't fetch yet
        if (getArguments() != null && getArguments().containsKey(ARG_PLAN_ID)) {
            editingPlanId = getArguments().getLong(ARG_PLAN_ID);
            isEditing = true;
        }

        // Initialize all days to false
        for (int i = 1; i <= 7; i++) {
            selectedDaysOfWeek.put(i, false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_plan, container, false);

        // Initialize UI components
        backButton = view.findViewById(R.id.back_button_create_plan);
        etPlanName = view.findViewById(R.id.et_plan_name);
        etStartDate = view.findViewById(R.id.et_start_date);
        spinnerLevel = view.findViewById(R.id.spinner_level);
        spinnerGoal = view.findViewById(R.id.spinner_goal);
        etDuration = view.findViewById(R.id.et_duration);
        cbMon = view.findViewById(R.id.cb_mon);
        cbTue = view.findViewById(R.id.cb_tue);
        cbWed = view.findViewById(R.id.cb_wed);
        cbThu = view.findViewById(R.id.cb_thu);
        cbFri = view.findViewById(R.id.cb_fri);
        cbSat = view.findViewById(R.id.cb_sat);
        cbSun = view.findViewById(R.id.cb_sun);
        recyclerViewSchedule = view.findViewById(R.id.recycler_view_schedule);
        spinnerExerciseWeek = view.findViewById(R.id.spinner_exercise_week);
        btnSavePlan = view.findViewById(R.id.btn_save_plan);
        progressBar = view.findViewById(R.id.progress_bar_create_plan);

        // Group checkboxes
        dayOfWeekCheckBoxes = Arrays.asList(cbMon, cbTue, cbWed, cbThu, cbFri, cbSat, cbSun);

        // Setup RecyclerView
        recyclerViewSchedule.setLayoutManager(new LinearLayoutManager(getContext()));
        scheduleAdapter = new CreatePlanScheduleAdapter(requireContext(), currentWeekScheduleItems, this);
        recyclerViewSchedule.setAdapter(scheduleAdapter);

        // Setup Spinners
        setupSpinners();

        // Setup Listeners
        setupListeners();

        // Load existing plan data if in editing mode
        if (isEditing && editingPlanId != null) {
            TextView toolbarTitle = view.findViewById(R.id.toolbar_title_create_plan);
            if (toolbarTitle != null) {
                toolbarTitle.setText("Edit Plan");
            }
            btnSavePlan.setText("UPDATE");

            // NOW fetch the plan details (views are initialized)
            fetchPlanDetailsForEditing(editingPlanId);
        } else {
            // Default: initialize schedule for 1 week
            updatePlanScheduleDataStructure(1);
            updateExerciseWeekSpinner(1);
            generateCurrentWeekScheduleItems(currentSelectedWeek);
        }

        fetchAllExercises(); // Fetch all exercises for search functionality

        return view;
    }

    private void setupSpinners() {
        // Level Spinner
        List<SelectOptions> levelOptions = new ArrayList<>();
        levelOptions.add(new SelectOptions(null, "(Choose Level)"));
        for (DifficultyLevel level : DifficultyLevel.values()) {
            levelOptions.add(new SelectOptions((long) level.ordinal(), getString(level.getResId())));
        }
        levelAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item_white_text, levelOptions);
        levelAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_white_text);
        spinnerLevel.setAdapter(levelAdapter);
        spinnerLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SelectOptions selectedOption = (SelectOptions) parent.getItemAtPosition(position);
                selectedLevel = (selectedOption.getId() != null) ? DifficultyLevel.values()[selectedOption.getId().intValue()] : null;
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { selectedLevel = null; }
        });

        // Goal Spinner
        List<SelectOptions> goalOptions = new ArrayList<>();
        goalOptions.add(new SelectOptions(null, "(Choose Goal)"));
        for (FitnessGoal goal : FitnessGoal.values()) {
            goalOptions.add(new SelectOptions((long) goal.ordinal(), getString(goal.getResId())));
        }
        goalAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item_white_text, goalOptions);
        goalAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_white_text);
        spinnerGoal.setAdapter(goalAdapter);
        spinnerGoal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SelectOptions selectedOption = (SelectOptions) parent.getItemAtPosition(position);
                selectedGoal = (selectedOption.getId() != null) ? FitnessGoal.values()[selectedOption.getId().intValue()] : null;
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { selectedGoal = null; }
        });

        // Exercise Week Spinner (initially with 1 week)
        updateExerciseWeekSpinner(1);
        spinnerExerciseWeek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedWeekText = (String) parent.getItemAtPosition(position);
                try {
                    currentSelectedWeek = Integer.parseInt(selectedWeekText.replace("Week ", ""));
                    generateCurrentWeekScheduleItems(currentSelectedWeek); // Refresh RecyclerView for new week
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid week format: " + selectedWeekText, e);
                    currentSelectedWeek = 1;
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { currentSelectedWeek = 1; }
        });
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                Toast.makeText(getContext(), "No previous page.", Toast.LENGTH_SHORT).show();
            }
        });

        // Start Date Picker
        etStartDate.setOnClickListener(v -> showDatePickerDialog());

        // Duration change listener
        etDuration.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int newDuration = Integer.parseInt(s.toString());
                    if (newDuration > 0) {
                        planDurationWeeks = newDuration;
                        updateExerciseWeekSpinner(planDurationWeeks);
                        updatePlanScheduleDataStructure(planDurationWeeks); // Expand/shrink schedule data
                    }
                } catch (NumberFormatException e) {
                    planDurationWeeks = 1; // Default
                    updateExerciseWeekSpinner(1);
                    updatePlanScheduleDataStructure(1);
                }
                generateCurrentWeekScheduleItems(currentSelectedWeek); // Refresh UI
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Day Checkbox listeners
        for (CheckBox cb : dayOfWeekCheckBoxes) {
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int day = Integer.parseInt(buttonView.getTag().toString()); // Get day (1-7) from tag
                selectedDaysOfWeek.put(day, isChecked);
                generateCurrentWeekScheduleItems(currentSelectedWeek); // Refresh UI to show/hide days
            });
        }

        // Save button listener
        btnSavePlan.setOnClickListener(v -> validateAndSavePlan());
    }

    private void fetchPlanDetailsForEditing(Long planId) {
        progressBar.setVisibility(View.VISIBLE);

        String accessToken = sessionManager.getAccessToken();
        String authorizationHeader = "Bearer " + accessToken;

        // First, fetch all exercises, then fetch plan details
        apiService.getAllExercises(authorizationHeader, null, null, null, null, 0, 1000)
                .enqueue(new Callback<ApiResponse<List<ExerciseResponse>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<ExerciseResponse>>> call,
                                           @NonNull Response<ApiResponse<List<ExerciseResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            allExercises = response.body().getData();
                            Log.d(TAG, "Fetched " + allExercises.size() + " exercises for editing.");

                            // Now fetch the plan details after exercises are loaded
                            fetchPlanDetailsAfterExercises(authorizationHeader, planId);
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Failed to load exercises", Toast.LENGTH_SHORT).show();
                            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                                getParentFragmentManager().popBackStack();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<ExerciseResponse>>> call, @NonNull Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Network error loading exercises: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                            getParentFragmentManager().popBackStack();
                        }
                    }
                });
    }

    private void fetchPlanDetailsAfterExercises(String authorizationHeader, Long planId) {
        apiService.getPlanDetail(authorizationHeader, planId)
                .enqueue(new Callback<ApiResponse<PlanDetailResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<PlanDetailResponse>> call,
                                           @NonNull Response<ApiResponse<PlanDetailResponse>> response) {
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            editingPlanDetail = response.body().getData();
                            populatePlanDataForEditing(editingPlanDetail);
                        } else {
                            Toast.makeText(getContext(), "Failed to load plan details", Toast.LENGTH_SHORT).show();
                            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                                getParentFragmentManager().popBackStack();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<PlanDetailResponse>> call, @NonNull Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error fetching plan details", t);
                        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                            getParentFragmentManager().popBackStack();
                        }
                    }
                });
    }

    private void populatePlanDataForEditing(PlanDetailResponse planDetail) {
        Log.d(TAG, "=== Starting to populate plan data ===");

        // Set plan name
        String planName = planDetail.getName();
        if (planDetail.getDefault() != null && planDetail.getDefault()) {
            // This was originally a preset plan, add prefix
            planName = "Copy of " + planName;
        }
        etPlanName.setText(planName);

        // Set start date
        if (planDetail.getStartDate() != null) {
            selectedStartDate = planDetail.getStartDate();
        } else {
            selectedStartDate = LocalDate.now();
        }
        etStartDate.setText(selectedStartDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // Set level
        if (planDetail.getDifficultyLevel() != null) {
            selectedLevel = planDetail.getDifficultyLevel();
            int levelPosition = Arrays.asList(DifficultyLevel.values()).indexOf(selectedLevel) + 1;
            spinnerLevel.setSelection(levelPosition);
        }

        // Set goal
        if (planDetail.getTargetGoal() != null) {
            selectedGoal = planDetail.getTargetGoal();
            int goalPosition = Arrays.asList(FitnessGoal.values()).indexOf(selectedGoal) + 1;
            spinnerGoal.setSelection(goalPosition);
        }

        // Set duration
        if (planDetail.getDurationWeek() != null) {
            planDurationWeeks = planDetail.getDurationWeek();
            etDuration.setText(String.valueOf(planDurationWeeks));
            updatePlanScheduleDataStructure(planDurationWeeks);
            updateExerciseWeekSpinner(planDurationWeeks);
        }

        // Set selected days and populate schedule data
        if (planDetail.getWeeks() != null && !planDetail.getWeeks().isEmpty()) {
            Log.d(TAG, "Processing " + planDetail.getWeeks().size() + " weeks");

            // Clear existing schedule data
            planScheduleData.clear();

            // Find the minimum week number to normalize (in case backend starts from 2)
            int minWeekNumber = Integer.MAX_VALUE;
            for (PlanWeekResponse week : planDetail.getWeeks()) {
                if (week.getWeekNumber() < minWeekNumber) {
                    minWeekNumber = week.getWeekNumber();
                }
            }
            Log.d(TAG, "Minimum week number from backend: " + minWeekNumber);

            // Calculate offset to normalize weeks (so they start from 1)
            int weekOffset = minWeekNumber - 1;
            Log.d(TAG, "Week offset for normalization: " + weekOffset);

            // Process each week and normalize the week numbers
            for (PlanWeekResponse week : planDetail.getWeeks()) {
                int originalWeekNumber = week.getWeekNumber();
                int normalizedWeekNumber = originalWeekNumber - weekOffset; // Normalize to start from 1

                Log.d(TAG, "Processing week " + originalWeekNumber + " -> normalized to week " + normalizedWeekNumber +
                        " with " + week.getDays().size() + " days");

                Map<Integer, List<PlanExerciseRequest>> weekData = new LinkedHashMap<>();

                for (PlanDayResponse day : week.getDays()) {
                    int dayOfWeek = day.getDayOfWeek();
                    Log.d(TAG, "Processing day " + dayOfWeek + " with " +
                            (day.getExercises() != null ? day.getExercises().size() : 0) + " exercises");

                    // Mark this day as selected
                    selectedDaysOfWeek.put(dayOfWeek, true);

                    // Convert PlanExerciseDetailResponse to PlanExerciseRequest
                    List<PlanExerciseRequest> exerciseRequests = new ArrayList<>();
                    if (day.getExercises() != null) {
                        for (PlanExerciseDetailResponse exercise : day.getExercises()) {
                            PlanExerciseRequest request = new PlanExerciseRequest();
                            request.setExerciseId(exercise.getExerciseId());
                            request.setSets(exercise.getSets());
                            request.setReps(exercise.getReps());
                            request.setDuration(exercise.getDuration());
                            request.setWeight(exercise.getWeight());
                            exerciseRequests.add(request);

                            Log.d(TAG, "Added exercise ID: " + exercise.getExerciseId() +
                                    " to normalized week " + normalizedWeekNumber + ", day " + dayOfWeek +
                                    " with sets=" + exercise.getSets() +
                                    ", reps=" + exercise.getReps() +
                                    ", duration=" + exercise.getDuration() +
                                    ", weight=" + exercise.getWeight());
                        }
                    }

                    weekData.put(dayOfWeek, exerciseRequests);
                }

                // Store with normalized week number (starting from 1)
                planScheduleData.put(normalizedWeekNumber, weekData);
                Log.d(TAG, "Stored data for normalized week " + normalizedWeekNumber);
            }

            Log.d(TAG, "Total exercises loaded: " + allExercises.size());
            Log.d(TAG, "planScheduleData now contains weeks: " + planScheduleData.keySet());

            // Update checkboxes
            updateDayCheckboxes();

            // Always start from week 1 after normalization
            currentSelectedWeek = 1;
            spinnerExerciseWeek.setSelection(0); // Select "Week 1" in spinner
            Log.d(TAG, "Set current week to 1 after loading");

            // Generate schedule items for week 1
            generateCurrentWeekScheduleItems(currentSelectedWeek);
        }

        Log.d(TAG, "=== Finished populating plan data ===");
    }

    private void updateDayCheckboxes() {
        for (CheckBox cb : dayOfWeekCheckBoxes) {
            int day = Integer.parseInt(cb.getTag().toString());
            cb.setChecked(selectedDaysOfWeek.get(day) != null && selectedDaysOfWeek.get(day));
        }
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, y, m, d) -> {
                    selectedStartDate = LocalDate.of(y, m + 1, d);
                    etStartDate.setText(selectedStartDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }, year, month, day);
        datePickerDialog.show();
    }

    /**
     * Updates the exercise week spinner options based on the total plan duration.
     */
    private void updateExerciseWeekSpinner(int duration) {
        List<String> weeks = new ArrayList<>();
        for (int i = 1; i <= duration; i++) {
            weeks.add("Week " + i);
        }
        exerciseWeekAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item_white_text, weeks);
        exerciseWeekAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_white_text);
        spinnerExerciseWeek.setAdapter(exerciseWeekAdapter);

        // Keep current selected week if it's still valid
        if (currentSelectedWeek > duration) {
            currentSelectedWeek = 1;
            spinnerExerciseWeek.setSelection(0);
        } else {
            spinnerExerciseWeek.setSelection(currentSelectedWeek - 1);
        }
        generateCurrentWeekScheduleItems(currentSelectedWeek); // Refresh UI for current week
    }

    /**
     * Expands or shrinks the planScheduleData structure based on plan duration.
     */
    private void updatePlanScheduleDataStructure(int newDuration) {
        Log.d(TAG, "Updating plan schedule structure for " + newDuration + " weeks");

        // Save current week's data before restructuring
        Map<Integer, List<PlanExerciseRequest>> currentWeekData = planScheduleData.get(currentSelectedWeek);

        // Clear and rebuild
        planScheduleData.clear();

        // Initialize structure for all weeks starting from 1
        for (int weekNum = 1; weekNum <= newDuration; weekNum++) {
            Map<Integer, List<PlanExerciseRequest>> weekData = new LinkedHashMap<>();

            // If this is the current selected week, restore its data
            if (weekNum == currentSelectedWeek && currentWeekData != null) {
                weekData.putAll(currentWeekData);
                Log.d(TAG, "Restored data for week " + weekNum);
            } else {
                // Initialize empty lists for selected days
                for (int day = 1; day <= 7; day++) {
                    if (selectedDaysOfWeek.get(day) != null && selectedDaysOfWeek.get(day)) {
                        weekData.put(day, new ArrayList<>());
                    }
                }
            }

            planScheduleData.put(weekNum, weekData);
            Log.d(TAG, "Initialized week " + weekNum + " with " + weekData.size() + " days");
        }

        planDurationWeeks = newDuration;

        // Make sure current selected week is valid
        if (currentSelectedWeek > newDuration) {
            currentSelectedWeek = 1;  // Reset to week 1 if current week exceeds duration
            Log.d(TAG, "Reset current week to 1 (was beyond duration)");
        }

        // Make sure current selected week is at least 1
        if (currentSelectedWeek < 1) {
            currentSelectedWeek = 1;
            Log.d(TAG, "Set current week to 1 (was less than 1)");
        }

        Log.d(TAG, "Current selected week is now: " + currentSelectedWeek);
    }

    /**
     * Generates the list of ScheduleItems for the currently selected week and updates the RecyclerView.
     */
    private void generateCurrentWeekScheduleItems(int weekNumber) {
        Log.d(TAG, "=== Generating schedule items for week " + weekNumber + " ===");
        currentWeekScheduleItems.clear();

        Map<Integer, List<PlanExerciseRequest>> weekData = planScheduleData.get(weekNumber);
        Log.d(TAG, "Week data exists: " + (weekData != null));

        if (weekData == null) {
            weekData = new LinkedHashMap<>();
            planScheduleData.put(weekNumber, weekData);
            Log.d(TAG, "Created new empty week data for week " + weekNumber);
        } else {
            Log.d(TAG, "Week data has " + weekData.size() + " days");
        }

        // Iterate through all 7 days of the week in order (1=Mon, 7=Sun)
        for (int day = 1; day <= 7; day++) {
            Boolean isDaySelected = selectedDaysOfWeek.get(day);
            Log.d(TAG, "Day " + day + " (" + getDayName(day) + ") selected: " + isDaySelected);

            if (isDaySelected != null && isDaySelected) {
                // Add day header
                currentWeekScheduleItems.add(new ScheduleItem(day, getDayName(day), weekNumber));
                Log.d(TAG, "Added day header for " + getDayName(day));

                List<PlanExerciseRequest> exercises = weekData.get(day);
                Log.d(TAG, "Exercises for day " + day + ": " + (exercises != null ? exercises.size() : 0));

                if (exercises != null && !exercises.isEmpty()) {
                    for (int i = 0; i < exercises.size(); i++) {
                        PlanExerciseRequest exerciseRequest = exercises.get(i);

                        // Find exercise name and full exercise object from allExercises
                        String exerciseName = null;
                        ExerciseResponse exerciseObject = null;

                        if (exerciseRequest.getExerciseId() != null && !allExercises.isEmpty()) {
                            for (ExerciseResponse ex : allExercises) {
                                if (ex.getId().equals(exerciseRequest.getExerciseId())) {
                                    exerciseName = ex.getName();
                                    exerciseObject = ex;
                                    Log.d(TAG, "Found exercise: " + exerciseName + " (ID: " + ex.getId() + ")");
                                    break;
                                }
                            }
                        }

                        if (exerciseName == null) {
                            Log.w(TAG, "Could not find exercise name for ID: " + exerciseRequest.getExerciseId());
                        }

                        // Create schedule item with exercise object for field configuration
                        ScheduleItem item = new ScheduleItem(day, exerciseName, exerciseRequest, i, weekNumber);
                        item.setExerciseObject(exerciseObject);  // Set the exercise object
                        currentWeekScheduleItems.add(item);

                        Log.d(TAG, "Added exercise item: " + exerciseName);
                    }
                }
            }
        }

        Log.d(TAG, "Generated " + currentWeekScheduleItems.size() + " total schedule items for week " + weekNumber);
        scheduleAdapter.setScheduleItems(currentWeekScheduleItems);
        scheduleAdapter.notifyDataSetChanged();
        Log.d(TAG, "=== Finished generating schedule items ===");
    }


    // --- OnItemInteractionListener methods for CreatePlanScheduleAdapter ---
    @Override
    public void onAddExerciseClicked(int dayOfWeek) {
//        if (currentPlanDetail == null || currentPlanDetail.getWeeks().isEmpty()) {
//            Toast.makeText(getContext(), "Vui lòng chọn tuần và ngày trước.", Toast.LENGTH_SHORT).show();
//            return;
//        }

        // Get the exercises list for the current week and day
        Map<Integer, List<PlanExerciseRequest>> weekData = planScheduleData.get(currentSelectedWeek);
        if (weekData == null) {
            weekData = new LinkedHashMap<>();
            planScheduleData.put(currentSelectedWeek, weekData);
        }

        List<PlanExerciseRequest> exercises = weekData.get(dayOfWeek);
        if (exercises == null) {
            exercises = new ArrayList<>();
            weekData.put(dayOfWeek, exercises);
        }

        exercises.add(new PlanExerciseRequest()); // Add a new empty exercise
        generateCurrentWeekScheduleItems(currentSelectedWeek); // Refresh RecyclerView
    }

    @Override
    public void onDeleteExerciseClicked(int dayOfWeek, int exerciseIndex) {
        new AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                .setTitle("Delete Exercise")
                .setMessage("Are you sure you want to delete this exercise?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    Map<Integer, List<PlanExerciseRequest>> weekData = planScheduleData.get(currentSelectedWeek);
                    if (weekData != null) {
                        List<PlanExerciseRequest> exercises = weekData.get(dayOfWeek);
                        if (exercises != null && exercises.size() > exerciseIndex) {
                            exercises.remove(exerciseIndex);
                            generateCurrentWeekScheduleItems(currentSelectedWeek); // Refresh RecyclerView
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onExerciseSelected(int dayOfWeek, int exerciseIndex, Long exerciseId, String exerciseName) {
        if (planScheduleData.containsKey(currentSelectedWeek)) {
            Map<Integer, List<PlanExerciseRequest>> weekData = planScheduleData.get(currentSelectedWeek);
            if (weekData != null && weekData.containsKey(dayOfWeek)) {
                List<PlanExerciseRequest> exercises = weekData.get(dayOfWeek);
                if (exerciseIndex >= 0 && exerciseIndex < exercises.size()) {
                    PlanExerciseRequest exercise = exercises.get(exerciseIndex);
                    exercise.setExerciseId(exerciseId);

                    Log.d(TAG, "Exercise selected for Day " + dayOfWeek + ", Index " + exerciseIndex +
                            ": " + exerciseName + " (ID: " + exerciseId + ")");

                    // Regenerate the schedule items to reflect the change
                    generateCurrentWeekScheduleItems(currentSelectedWeek);
                }
            }
        }
    }

    @Override
    public void onExerciseSetsChanged(int dayOfWeek, int exerciseIndex, Integer sets) {
        Map<Integer, List<PlanExerciseRequest>> weekData = planScheduleData.get(currentSelectedWeek);
        if (weekData != null) {
            List<PlanExerciseRequest> exercises = weekData.get(dayOfWeek);
            if (exercises != null && exercises.size() > exerciseIndex) {
                exercises.get(exerciseIndex).setSets(sets);
            }
        }
    }

    @Override
    public void onExerciseRepsChanged(int dayOfWeek, int exerciseIndex, Integer reps) {
        Map<Integer, List<PlanExerciseRequest>> weekData = planScheduleData.get(currentSelectedWeek);
        if (weekData != null) {
            List<PlanExerciseRequest> exercises = weekData.get(dayOfWeek);
            if (exercises != null && exercises.size() > exerciseIndex) {
                exercises.get(exerciseIndex).setReps(reps);
            }
        }
    }

    @Override
    public void onExerciseDurationChanged(int dayOfWeek, int exerciseIndex, Integer duration) {
        Map<Integer, List<PlanExerciseRequest>> weekData = planScheduleData.get(currentSelectedWeek);
        if (weekData != null) {
            List<PlanExerciseRequest> exercises = weekData.get(dayOfWeek);
            if (exercises != null && exercises.size() > exerciseIndex) {
                exercises.get(exerciseIndex).setDuration(duration);
            }
        }
    }

    @Override
    public void onExerciseWeightChanged(int dayOfWeek, int exerciseIndex, Double weight) {
        Map<Integer, List<PlanExerciseRequest>> weekData = planScheduleData.get(currentSelectedWeek);
        if (weekData != null) {
            List<PlanExerciseRequest> exercises = weekData.get(dayOfWeek);
            if (exercises != null && exercises.size() > exerciseIndex) {
                exercises.get(exerciseIndex).setWeight(weight);
            }
        }
    }


    /**
     * Fetches all exercises to populate the AutoCompleteTextView suggestions.
     */
    private void fetchAllExercises() {
        String accessToken = sessionManager.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(getContext(), "Authentication token unavailable.", Toast.LENGTH_LONG).show();
            return;
        }

        String authorizationHeader = "Bearer " + accessToken;

        // Call API to get all exercises (possibly without filters for initial load, or with a generic search)
        apiService.getAllExercises(authorizationHeader, null, null, null, null, 0, 1000) // Adjust page/size as needed
                .enqueue(new Callback<ApiResponse<List<ExerciseResponse>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<ExerciseResponse>>> call, @NonNull Response<ApiResponse<List<ExerciseResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            allExercises = response.body().getData();
                            Log.d(TAG, "Fetched " + allExercises.size() + " exercises for search.");
                        } else {
                            Log.e(TAG, "Failed to fetch all exercises: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<ExerciseResponse>>> call, @NonNull Throwable t) {
                        Log.e(TAG, "Network error fetching all exercises: " + t.getMessage(), t);
                    }
                });
    }

    @Override
    public List<ExerciseResponse> getAllExercises() {
        return allExercises;
    }

    private String getDayName(int dayOfWeek) {
        // Maps 1=Monday, 2=Tuesday, ..., 7=Sunday
        switch (dayOfWeek) {
            case 1: return "Monday";
            case 2: return "Tuesday";
            case 3: return "Wednesday";
            case 4: return "Thursday";
            case 5: return "Friday";
            case 6: return "Saturday";
            case 7: return "Sunday";
            default: return "Invalid Day";
        }
    }

    private void validateAndSavePlan() {
        String planName = etPlanName.getText().toString().trim();
        String durationStr = etDuration.getText().toString().trim();

        if (planName.isEmpty()) {
            etPlanName.setError("Plan name cannot be empty");
            return;
        }
        if (selectedStartDate == null) {
            Toast.makeText(getContext(), "Please select a start date.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedLevel == null) {
            Toast.makeText(getContext(), "Please select a difficulty level.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedGoal == null) {
            Toast.makeText(getContext(), "Please select a fitness goal.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (durationStr.isEmpty() || Integer.parseInt(durationStr) <= 0) {
            etDuration.setError("Duration must be a positive number.");
            return;
        }

        // Check if at least one day is selected
        boolean anyDaySelected = selectedDaysOfWeek.values().stream().anyMatch(Boolean::booleanValue);
        if (!anyDaySelected) {
            Toast.makeText(getContext(), "Please select at least one day for the plan.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate schedule
        List<PlanDayRequest> finalSchedule = new ArrayList<>();

        // IMPORTANT: Start from week 1, not from currentSelectedWeek
        for (int weekNum = 1; weekNum <= planDurationWeeks; weekNum++) {  // Start from 1
            Map<Integer, List<PlanExerciseRequest>> weekData = planScheduleData.get(weekNum);

            Log.d(TAG, "Processing week " + weekNum + " for save. Week data exists: " + (weekData != null));

            if (weekData != null) {
                for (int dayOfWeek = 1; dayOfWeek <= 7; dayOfWeek++) {
                    if (selectedDaysOfWeek.get(dayOfWeek) != null && selectedDaysOfWeek.get(dayOfWeek)) {
                        List<PlanExerciseRequest> exercisesForDay = weekData.get(dayOfWeek);

                        Log.d(TAG, "Week " + weekNum + ", Day " + dayOfWeek + ": " +
                                (exercisesForDay != null ? exercisesForDay.size() : 0) + " exercises");

                        if (exercisesForDay != null && !exercisesForDay.isEmpty()) {
                            List<PlanExerciseRequest> validatedExercises = new ArrayList<>();
                            for (PlanExerciseRequest exercise : exercisesForDay) {
                                if (exercise.getExerciseId() == null) {
                                    Toast.makeText(getContext(), "Please select an exercise for all slots.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                // Validate that exercise has either (sets AND reps) OR (sets AND duration)
                                boolean hasSetsAndReps = exercise.getSets() != null && exercise.getReps() != null;
                                boolean hasSetsAndDuration = exercise.getSets() != null && exercise.getDuration() != null;

                                if (!hasSetsAndReps && !hasSetsAndDuration) {
                                    Toast.makeText(getContext(), "Each exercise must have sets and either reps or duration.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                validatedExercises.add(exercise);
                            }

                            // Use weekNum (which starts from 1)
                            finalSchedule.add(new PlanDayRequest(weekNum, dayOfWeek, validatedExercises));
                            Log.d(TAG, "Added PlanDayRequest for week " + weekNum + ", day " + dayOfWeek);
                        }
                    }
                }
            }
        }

        if (finalSchedule.isEmpty()) {
            Toast.makeText(getContext(), "Please add at least one exercise to your plan.", Toast.LENGTH_SHORT).show();
            return;
        }

        CreatePlanRequest createPlanRequest = new CreatePlanRequest(
                planName,
                selectedGoal,
                selectedStartDate,
                Integer.parseInt(durationStr),
                (int) selectedDaysOfWeek.values().stream().filter(Boolean::booleanValue).count(),
                selectedLevel,
                "Default description",
                finalSchedule
        );

        Log.d(TAG, "=== Create Plan Request ===");
        Log.d(TAG, "Plan name: " + planName);
        Log.d(TAG, "Duration: " + durationStr + " weeks");
        Log.d(TAG, "Total plan days: " + finalSchedule.size());
        for (PlanDayRequest day : finalSchedule) {
            Log.d(TAG, "Week " + day.getWeekNumber() + ", Day " + day.getDayOfWeek() +
                    " with " + day.getExercises().size() + " exercises");
        }
        Log.d(TAG, "Full JSON: " + new Gson().toJson(createPlanRequest));

        if (isEditing && editingPlanId != null) {
            callUpdatePlan(editingPlanId, createPlanRequest);
        } else {
            callCreatePlan(createPlanRequest);
        }
    }

    private void callCreatePlan(CreatePlanRequest request) {
        progressBar.setVisibility(View.VISIBLE);
        btnSavePlan.setEnabled(false);

        String accessToken = sessionManager.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(getContext(), "Authentication token unavailable.", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            btnSavePlan.setEnabled(true);
            return;
        }
        String authorizationHeader = "Bearer " + accessToken;

        apiService.createPlan(authorizationHeader, request)
                .enqueue(new Callback<ApiResponse<Long>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Long>> call, @NonNull Response<ApiResponse<Long>> response) {
                        progressBar.setVisibility(View.GONE);
                        btnSavePlan.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            Toast.makeText(getContext(), "Plan created successfully!", Toast.LENGTH_SHORT).show();
                            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                                getParentFragmentManager().popBackStack();
                            }
                        } else {
                            String errorMsg = "Failed to create plan: " + response.message();
                            try {
                                if (response.errorBody() != null) {
                                    errorMsg += " - " + response.errorBody().string();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing error body", e);
                            }
                            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                            Log.e(TAG, errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Long>> call, @NonNull Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        btnSavePlan.setEnabled(true);
                        Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error creating plan", t);
                    }
                });
    }

    private void callUpdatePlan(Long planId, CreatePlanRequest request) {
        progressBar.setVisibility(View.VISIBLE);
        btnSavePlan.setEnabled(false);

        String accessToken = sessionManager.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(getContext(), "Authentication token unavailable.", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            btnSavePlan.setEnabled(true);
            return;
        }
        String authorizationHeader = "Bearer " + accessToken;

        apiService.updatePlan(authorizationHeader, planId, request)
                .enqueue(new Callback<ApiResponse<Long>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Long>> call, @NonNull Response<ApiResponse<Long>> response) {
                        progressBar.setVisibility(View.GONE);
                        btnSavePlan.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            Toast.makeText(getContext(), "Plan updated successfully!", Toast.LENGTH_SHORT).show();
                            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                                getParentFragmentManager().popBackStack();
                            }
                        } else {
                            String errorMsg = "Failed to update plan: " + response.message();
                            try {
                                if (response.errorBody() != null) {
                                    errorMsg += " - " + response.errorBody().string();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing error body", e);
                            }
                            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                            Log.e(TAG, errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Long>> call, @NonNull Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        btnSavePlan.setEnabled(true);
                        Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Error updating plan", t);
                    }
                });
    }

    // Helper class for RecyclerView adapter to combine headers and items
    public static class ScheduleItem {
        private int dayOfWeek;
        private String dayName;
        private String exerciseName;
        private PlanExerciseRequest exerciseRequest;
        private int exerciseIndex;
        private int weekNumber;
        private ExerciseResponse exerciseObject;  // Add this field

        // Constructor for day header
        public ScheduleItem(int dayOfWeek, String dayName, int weekNumber) {
            this.dayOfWeek = dayOfWeek;
            this.dayName = dayName;
            this.weekNumber = weekNumber;
            this.exerciseRequest = null;
            this.exerciseIndex = -1;
        }

        // Constructor for exercise item
        public ScheduleItem(int dayOfWeek, String exerciseName, PlanExerciseRequest exerciseRequest, int exerciseIndex, int weekNumber) {
            this.dayOfWeek = dayOfWeek;
            this.exerciseName = exerciseName;
            this.exerciseRequest = exerciseRequest;
            this.exerciseIndex = exerciseIndex;
            this.weekNumber = weekNumber;
        }

        // Getters
        public int getDayOfWeek() { return dayOfWeek; }
        public String getDayName() { return dayName; }
        public String getExerciseName() { return exerciseName; }
        public PlanExerciseRequest getExerciseRequest() { return exerciseRequest; }
        public int getExerciseIndex() { return exerciseIndex; }
        public int getWeekNumber() { return weekNumber; }
        public ExerciseResponse getExerciseObject() { return exerciseObject; }

        // Setter for exercise object
        public void setExerciseObject(ExerciseResponse exerciseObject) {
            this.exerciseObject = exerciseObject;
        }

        public boolean isExerciseItem() {
            return exerciseRequest != null;
        }

        public boolean isDayHeader() {
            return exerciseRequest == null;
        }
    }
}
