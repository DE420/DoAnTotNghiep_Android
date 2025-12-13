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
    // Thay thế EditText bằng Spinner cho Level
    private Spinner spinnerLevel; // <--- Level Spinner
    private Spinner spinnerMuscleType;
    private Spinner spinnerTrainingType;
    private Button btnSearch;
    private RecyclerView recyclerView;
    private ExerciseAdapter exerciseAdapter;
    private ProgressBar progressBar;
    private ImageView backButton;

    // Adapters for Spinners
    private ArrayAdapter<String> levelAdapter; // <--- Adapter cho Level (String)
    private ArrayAdapter<SelectOptions> muscleTypeAdapter;
    private ArrayAdapter<SelectOptions> trainingTypeAdapter;

    // Selected values for search
    private String selectedLevel = null; // <--- Lưu giá trị Level đã chọn
    private Long selectedMuscleTypeId = null;
    private Long selectedTrainingTypeId = null;

    // Pagination
    private int currentPage = 0;
    private final int pageSize = 10;
    private boolean isLoading = false;
    private boolean hasMorePages = true;

    // Current search parameters to maintain state across pagination
    private String currentSearchName = null;
    private String currentSearchLevel = null; // <--- Cập nhật thành currentSearchLevel
    // currentSearchMuscleId và currentSearchTypeId sẽ được cập nhật từ spinner

    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercises, container, false);

        sessionManager = SessionManager.getInstance(requireContext());

        // Initialize Views
        backButton = view.findViewById(R.id.back_button);
        etSearchName = view.findViewById(R.id.et_search_name);
        spinnerLevel = view.findViewById(R.id.spinner_level); // <--- Lấy Level Spinner
        spinnerMuscleType = view.findViewById(R.id.spinner_muscle_type);
        spinnerTrainingType = view.findViewById(R.id.spinner_training_type);
        btnSearch = view.findViewById(R.id.btn_search);
        recyclerView = view.findViewById(R.id.recycler_view_exercises);
        progressBar = view.findViewById(R.id.progress_bar);

        // Configure RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        exerciseAdapter = new ExerciseAdapter(this);
        recyclerView.setAdapter(exerciseAdapter);

        // Initialize Spinners and their Adapters
        // Level Spinner
        List<String> levelOptions = new ArrayList<>(Arrays.asList("(Choose one)", "Beginner", "Intermediate", "Advanced"));
        levelAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, levelOptions);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLevel.setAdapter(levelAdapter);

        // Muscle Type Spinner (Code from previous step, ensure it's still correct)
        List<SelectOptions> initialMuscleOptions = new ArrayList<>();
        initialMuscleOptions.add(new SelectOptions(null, "(Choose one)"));
        muscleTypeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, initialMuscleOptions);
        muscleTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMuscleType.setAdapter(muscleTypeAdapter);

        // Training Type Spinner (Code from previous step, ensure it's still correct)
        List<SelectOptions> initialTrainingOptions = new ArrayList<>();
        initialTrainingOptions.add(new SelectOptions(null, "(Choose one)"));
        trainingTypeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, initialTrainingOptions);
        trainingTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTrainingType.setAdapter(trainingTypeAdapter);

        // Set up Spinner listeners
        // Level Spinner Listener
        spinnerLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLevelString = (String) parent.getItemAtPosition(position);
                if (selectedLevelString.equals("(Choose one)")) {
                    selectedLevel = null; // Gửi null nếu chọn "All"
                } else {
                    selectedLevel = selectedLevelString;
                }
                Log.d(TAG, "Selected Level: " + selectedLevel);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedLevel = null;
            }
        });

        // Muscle Type Spinner Listener (Code from previous step)
        spinnerMuscleType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SelectOptions selectedOption = (SelectOptions) parent.getItemAtPosition(position);
                selectedMuscleTypeId = selectedOption.getId();
                Log.d(TAG, "Selected Muscle Type ID: " + selectedMuscleTypeId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedMuscleTypeId = null;
            }
        });

        // Training Type Spinner Listener (Code from previous step)
        spinnerTrainingType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SelectOptions selectedOption = (SelectOptions) parent.getItemAtPosition(position);
                selectedTrainingTypeId = selectedOption.getId();
                Log.d(TAG, "Selected Training Type ID: " + selectedTrainingTypeId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTrainingTypeId = null;
            }
        });

        // Event listener for the back button
        backButton.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                Toast.makeText(getContext(), "Không có trang trước đó.", Toast.LENGTH_SHORT).show();
            }
        });

        // Event listener for the Search button
        btnSearch.setOnClickListener(v -> {
            // Reset pagination and perform a new search
            currentPage = 0;
            hasMorePages = true;
            isLoading = false;

            // Get search parameters from EditTexts and Spinners
            currentSearchName = etSearchName.getText().toString().trim();
            currentSearchLevel = selectedLevel; // <--- Lấy giá trị từ Level Spinner
            // selectedMuscleTypeId and selectedTrainingTypeId are already updated by spinner listeners

            // Call API to search (true to clear existing data)
            fetchExercises(true);
        });

        // Listen for RecyclerView scroll events for pagination (load more data)
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == exerciseAdapter.getItemCount() - 1 && hasMorePages && !isLoading) {
                    currentPage++; // Increment page number to load the next page
                    fetchExercises(false); // false to append data to the existing list
                }
            }
        });

        // Load initial data when the fragment is created
        fetchExercises(true);
        // Load options for the spinners (Muscle Type and Training Type)
        fetchMuscleGroupOptions();
        fetchTrainingTypeOptions();

        return view;
    }

    // region API Calls for Spinners (unchanged)
    private void fetchMuscleGroupOptions() {
        String accessToken = sessionManager.getAccessToken();
        String authorizationHeader = null;

        if (accessToken != null && !accessToken.isEmpty()) {
            authorizationHeader = "Bearer " + accessToken;
        } else {
            Toast.makeText(getContext(), "Không có token xác thực để tải danh sách nhóm cơ. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
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
                            muscleTypeAdapter.add(new SelectOptions(null, "(Choose one)"));
                            muscleTypeAdapter.addAll(options);
                            muscleTypeAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(getContext(), "Lỗi từ API nhóm cơ: " + apiResponse.getData(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "API Error Muscle Types: " + apiResponse.getData());
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi phản hồi từ server khi tải nhóm cơ: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Server error Muscle Types: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<SelectOptions>>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối khi tải nhóm cơ: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Lỗi mạng nhóm cơ: " + t.getMessage(), t);
            }
        });
    }

    private void fetchTrainingTypeOptions() {
        String accessToken = sessionManager.getAccessToken();
        String authorizationHeader = null;

        if (accessToken != null && !accessToken.isEmpty()) {
            authorizationHeader = "Bearer " + accessToken;
        } else {
            Toast.makeText(getContext(), "Không có token xác thực để tải danh sách loại hình tập. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
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
                            trainingTypeAdapter.add(new SelectOptions(null, "(Choose one)"));
                            trainingTypeAdapter.addAll(options);
                            trainingTypeAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(getContext(), "Lỗi từ API loại hình tập: " + apiResponse.getData(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "API Error Training Types: " + apiResponse.getData());
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi phản hồi từ server khi tải loại hình tập: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Server error Training Types: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<SelectOptions>>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối khi tải loại hình tập: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Lỗi mạng loại hình tập: " + t.getMessage(), t);
            }
        });
    }
    // endregion API Calls for Spinners

    private void fetchExercises(boolean clearExisting) {
        if (isLoading) return;
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);

        String accessToken = sessionManager.getAccessToken();
        String authorizationHeader = null;

        if (accessToken != null && !accessToken.isEmpty()) {
            authorizationHeader = "Bearer " + accessToken;
        } else {
            Toast.makeText(getContext(), "Không có token xác thực. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            isLoading = false;
            progressBar.setVisibility(View.GONE);
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        Call<ApiResponse<List<ExerciseResponse>>> call = apiService.getAllExercises(
                authorizationHeader,
                currentSearchName != null && !currentSearchName.isEmpty() ? currentSearchName : null,
                currentSearchLevel != null && !currentSearchLevel.isEmpty() ? currentSearchLevel : null, // <--- SỬ DỤNG selectedLevel
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
                            Toast.makeText(getContext(), "Không tìm thấy bài tập nào.", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(getContext(), "Lỗi từ API: " + apiResponse.getData(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "API Error: " + apiResponse.getData());
                    }
                } else {
                    if (response.code() == 401) {
                        Toast.makeText(getContext(), "Phiên đăng nhập đã hết hạn hoặc không hợp lệ. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Lỗi phản hồi từ server: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                    Log.e(TAG, "Server error: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<ExerciseResponse>>> call, @NonNull Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Lỗi mạng: " + t.getMessage(), t);
            }
        });
    }

    @Override
    public void onItemClick(ExerciseResponse exercise) {
        if (exercise != null && exercise.getId() != null) {
            ExerciseDetailFragment detailFragment = ExerciseDetailFragment.newInstance(exercise.getId());
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null) // Cho phép quay lại ExercisesFragment
                    .commit();
        } else {
            Toast.makeText(getContext(), "Không thể xem chi tiết bài tập này.", Toast.LENGTH_SHORT).show();
        }
    }
}