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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.MainActivity;
import com.example.fitnessapp.R;
import com.example.fitnessapp.adapter.ExerciseAdapter;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.ExerciseResponse;
import com.example.fitnessapp.model.response.SelectOptions;
import com.example.fitnessapp.network.RetrofitClient;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.session.SessionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExercisesFragment extends Fragment implements ExerciseAdapter.OnItemClickListener {

    private static final String TAG = "ExercisesFragment";

    private EditText etSearchName;
    private Spinner spinnerLevel;
    private Spinner spinnerMuscleType;
    private Spinner spinnerTrainingType;
    private Button btnSearch;
    private RecyclerView recyclerView;
    private ExerciseAdapter exerciseAdapter;
    private ProgressBar progressBar;
    private ImageView backButton;

    // Adapters for Spinners
    private ArrayAdapter<String> levelAdapter;
    private ArrayAdapter<SelectOptions> muscleTypeAdapter;
    private ArrayAdapter<SelectOptions> trainingTypeAdapter;

    // Selected values for search
    private String selectedLevel = null;
    private Long selectedMuscleTypeId = null;
    private Long selectedTrainingTypeId = null;

    // Pagination
    private int currentPage = 0;
    private final int pageSize = 10;
    private boolean isLoading = false;
    private boolean hasMorePages = true;

    private String currentSearchName = null;
    private String currentSearchLevel = null;

    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercises, container, false);

        sessionManager = SessionManager.getInstance(requireContext());

        backButton = view.findViewById(R.id.back_button);
        etSearchName = view.findViewById(R.id.et_search_name);
        spinnerLevel = view.findViewById(R.id.spinner_level);
        spinnerMuscleType = view.findViewById(R.id.spinner_muscle_type);
        spinnerTrainingType = view.findViewById(R.id.spinner_training_type);
        btnSearch = view.findViewById(R.id.btn_search);
        recyclerView = view.findViewById(R.id.recycler_view_exercises);
        progressBar = view.findViewById(R.id.progress_bar);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        exerciseAdapter = new ExerciseAdapter(this);
        recyclerView.setAdapter(exerciseAdapter);

        // Level Spinner
        List<String> levelOptions = new ArrayList<>(Arrays.asList("(Chọn một)", "Cơ bản", "Trung cấp", "Nâng cao"));
        levelAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item_white_text, levelOptions);
        levelAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_white_text);
        spinnerLevel.setAdapter(levelAdapter);

        // Muscle Type Spinner
        List<SelectOptions> initialMuscleOptions = new ArrayList<>();
        initialMuscleOptions.add(new SelectOptions(null, "(Chọn một)"));
        muscleTypeAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item_white_text, initialMuscleOptions);
        muscleTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_white_text);
        spinnerMuscleType.setAdapter(muscleTypeAdapter);

        // Training Type Spinner
        List<SelectOptions> initialTrainingOptions = new ArrayList<>();
        initialTrainingOptions.add(new SelectOptions(null, "(Chọn một)"));
        trainingTypeAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item_white_text, initialTrainingOptions);
        trainingTypeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_white_text);
        spinnerTrainingType.setAdapter(trainingTypeAdapter);

        // Level Spinner Listener
        spinnerLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLevelString = (String) parent.getItemAtPosition(position);
                if (selectedLevelString.equals("(Chọn một)")) {
                    selectedLevel = null;
                } else {
                    // Convert Vietnamese back to English for API
                    switch (selectedLevelString) {
                        case "Người mới bắt đầu":
                            selectedLevel = "Beginner";
                            break;
                        case "Trung cấp":
                            selectedLevel = "Intermediate";
                            break;
                        case "Nâng cao":
                            selectedLevel = "Advanced";
                            break;
                        default:
                            selectedLevel = null;
                    }
                }
                Log.d(TAG, "Cấp độ đã chọn: " + selectedLevel);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedLevel = null;
            }
        });

        // Muscle Type Spinner Listener
        spinnerMuscleType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SelectOptions selectedOption = (SelectOptions) parent.getItemAtPosition(position);
                selectedMuscleTypeId = selectedOption.getId();
                Log.d(TAG, "ID nhóm cơ đã chọn: " + selectedMuscleTypeId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedMuscleTypeId = null;
            }
        });

        // Training Type Spinner Listener
        spinnerTrainingType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SelectOptions selectedOption = (SelectOptions) parent.getItemAtPosition(position);
                selectedTrainingTypeId = selectedOption.getId();
                Log.d(TAG, "ID loại tập đã chọn: " + selectedTrainingTypeId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTrainingTypeId = null;
            }
        });

        backButton.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                Toast.makeText(getContext(), "Không tìm thấy trang trước đó.", Toast.LENGTH_SHORT).show();
            }
        });

        btnSearch.setOnClickListener(v -> {
            currentPage = 0;
            hasMorePages = true;
            isLoading = false;

            currentSearchName = etSearchName.getText().toString().trim();
            currentSearchLevel = selectedLevel;

            fetchExercises(true);
        });

        // Load more exercises at the end of a page
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == exerciseAdapter.getItemCount() - 1 && hasMorePages && !isLoading) {
                    currentPage++;
                    fetchExercises(false);
                }
            }
        });

        fetchExercises(true);
        fetchMuscleGroupOptions();
        fetchTrainingTypeOptions();

        return view;
    }

    // region API Calls for Spinners
    private void fetchMuscleGroupOptions() {
        String accessToken = sessionManager.getAccessToken();
        String authorizationHeader = null;

        if (accessToken != null && !accessToken.isEmpty()) {
            authorizationHeader = "Bearer " + accessToken;
        } else {
            Toast.makeText(getContext(), "Token đã hết hạn.", Toast.LENGTH_LONG).show();
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        Call<ApiResponse<List<SelectOptions>>> call = apiService.getMuscleGroupOptions(authorizationHeader);

        call.enqueue(new Callback<ApiResponse<List<SelectOptions>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<SelectOptions>>> call, @NonNull Response<ApiResponse<List<SelectOptions>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<SelectOptions>> apiResponse = response.body();
                    if (apiResponse.isStatus()) {
                        List<SelectOptions> options = apiResponse.getData();
                        if (options != null) {
                            muscleTypeAdapter.clear();
                            muscleTypeAdapter.add(new SelectOptions(null, "(Chọn một)"));
                            muscleTypeAdapter.addAll(options);
                            muscleTypeAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(getContext(), "Lỗi API nhóm cơ: " + apiResponse.getData(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Lỗi API nhóm cơ: " + apiResponse.getData());
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi máy chủ nhóm cơ: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Lỗi máy chủ nhóm cơ: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<SelectOptions>>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối nhóm cơ: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Lỗi kết nối nhóm cơ: " + t.getMessage(), t);
            }
        });
    }

    private void fetchTrainingTypeOptions() {
        String accessToken = sessionManager.getAccessToken();
        String authorizationHeader = null;

        if (accessToken != null && !accessToken.isEmpty()) {
            authorizationHeader = "Bearer " + accessToken;
        } else {
            Toast.makeText(getContext(), "Token đã hết hạn.", Toast.LENGTH_LONG).show();
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        Call<ApiResponse<List<SelectOptions>>> call = apiService.getTrainingTypeOptions(authorizationHeader);

        call.enqueue(new Callback<ApiResponse<List<SelectOptions>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<SelectOptions>>> call, @NonNull Response<ApiResponse<List<SelectOptions>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<SelectOptions>> apiResponse = response.body();
                    if (apiResponse.isStatus()) {
                        List<SelectOptions> options = apiResponse.getData();
                        if (options != null) {
                            trainingTypeAdapter.clear();
                            trainingTypeAdapter.add(new SelectOptions(null, "(Chọn một)"));
                            trainingTypeAdapter.addAll(options);
                            trainingTypeAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(getContext(), "Lỗi API loại tập: " + apiResponse.getData(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Lỗi API loại tập: " + apiResponse.getData());
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi máy chủ loại tập: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Lỗi máy chủ loại tập: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<SelectOptions>>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối loại tập: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Lỗi kết nối loại tập: " + t.getMessage(), t);
            }
        });
    }

    private void fetchExercises(boolean clearExisting) {
        if (isLoading) return;
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);

        String accessToken = sessionManager.getAccessToken();
        String authorizationHeader = null;

        if (accessToken != null && !accessToken.isEmpty()) {
            authorizationHeader = "Bearer " + accessToken;
        } else {
            Toast.makeText(getContext(), "Token đã hết hạn.", Toast.LENGTH_LONG).show();
            isLoading = false;
            progressBar.setVisibility(View.GONE);
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        Call<ApiResponse<List<ExerciseResponse>>> call = apiService.getAllExercises(
                authorizationHeader,
                currentSearchName != null && !currentSearchName.isEmpty() ? currentSearchName : null,
                currentSearchLevel != null && !currentSearchLevel.isEmpty() ? currentSearchLevel : null,
                selectedMuscleTypeId,
                selectedTrainingTypeId,
                currentPage,
                pageSize
        );

        call.enqueue(new Callback<ApiResponse<List<ExerciseResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<ExerciseResponse>>> call, @NonNull Response<ApiResponse<List<ExerciseResponse>>> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<ExerciseResponse>> apiResponse = response.body();
                    if (apiResponse.isStatus()) {
                        List<ExerciseResponse> exercises = apiResponse.getData();
                        if (clearExisting) {
                            exerciseAdapter.setExercises(exercises);
                        } else {
                            exerciseAdapter.addExercises(exercises);
                        }

                        if (apiResponse.getMeta() != null) {
                            hasMorePages = apiResponse.getMeta().isHasMore();
                            if (exercises == null || exercises.isEmpty()) {
                                hasMorePages = false;
                            }
                        } else {
                            hasMorePages = exercises != null && exercises.size() == pageSize;
                        }

                        if (exercises == null || exercises.isEmpty() && clearExisting) {
                            Toast.makeText(getContext(), "Không tìm thấy bài tập.", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(getContext(), "Lỗi API: " + apiResponse.getData(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Lỗi API: " + apiResponse.getData());
                    }
                } else {
                    if (response.code() == 401) {
                        Toast.makeText(getContext(), "Phiên đã hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Lỗi máy chủ: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                    Log.e(TAG, "Lỗi máy chủ: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<ExerciseResponse>>> call, @NonNull Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Lỗi kết nối: " + t.getMessage(), t);
            }
        });
    }

    @Override
    public void onItemClick(ExerciseResponse exercise) {
        if (exercise != null && exercise.getId() != null) {
            ExerciseDetailFragment detailFragment = ExerciseDetailFragment.newInstance(exercise.getId());
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            Toast.makeText(getContext(), "Chi tiết bài tập không khả dụng.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Hide AppBar when this fragment is visible
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setAppBarVisible(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Show AppBar when leaving this fragment
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setAppBarVisible(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Ensure AppBar is visible when fragment is destroyed
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setAppBarVisible(true);
        }
    }
}