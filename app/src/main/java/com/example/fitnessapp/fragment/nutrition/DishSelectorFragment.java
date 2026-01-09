package com.example.fitnessapp.fragment.nutrition;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.adapter.nutrition.DishSelectorAdapter;
import com.example.fitnessapp.databinding.FragmentDishSelectorBinding;
import com.example.fitnessapp.enums.MealType;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.nutrition.MealDishResponse;
import com.example.fitnessapp.repository.NutritionRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DishSelectorFragment extends DialogFragment {

    private static final String TAG = "DishSelectorFragment";
    private static final String ARG_MEAL_TYPE = "meal_type";

    private FragmentDishSelectorBinding binding;
    private NutritionRepository repository;
    private DishSelectorAdapter adapter;
    private MealType mealType;
    private OnDishSelectedListener listener;

    public static DishSelectorFragment newInstance(MealType mealType) {
        DishSelectorFragment fragment = new DishSelectorFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_MEAL_TYPE, mealType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        repository = new NutritionRepository(requireContext());

        if (getArguments() != null) {
            mealType = (MealType) getArguments().getSerializable(ARG_MEAL_TYPE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDishSelectorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupUI();
        setupRecyclerView();
        setupListeners();
        loadDishes("");
    }

    @Override
    public void onStart() {
        super.onStart();
        // Configure dialog window to use full screen
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            WindowManager.LayoutParams params = window.getAttributes();

            // Set dialog to full screen
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.MATCH_PARENT;

            window.setAttributes(params);
        }
    }

    private void setupUI() {
        if (mealType != null) {
            String title = getString(R.string.select_dish) + " - " + getString(mealType.getResId());
            binding.tvTitle.setText(title);
        }

        // Set SearchView icon and text colors to white
        int whiteColor = ContextCompat.getColor(requireContext(), R.color.white);

        // Set icon colors
        ImageView searchIcon = binding.svSearch.findViewById(androidx.appcompat.R.id.search_mag_icon);
        ImageView closeIcon = binding.svSearch.findViewById(androidx.appcompat.R.id.search_close_btn);

        if (searchIcon != null) {
            searchIcon.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        }
        if (closeIcon != null) {
            closeIcon.setColorFilter(whiteColor, PorterDuff.Mode.SRC_IN);
        }

        // Set text color
        EditText searchEditText = binding.svSearch.findViewById(androidx.appcompat.R.id.search_src_text);
        if (searchEditText != null) {
            searchEditText.setTextColor(whiteColor);
            searchEditText.setHintTextColor(whiteColor);
        }
    }

    private void setupRecyclerView() {
        adapter = new DishSelectorAdapter(requireContext());
        binding.rvDishes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDishes.setAdapter(adapter);

        adapter.setOnDishClickListener(dish -> {
            if (listener != null) {
                listener.onDishSelected(dish);
            }
            dismiss();
        });

        // Hide progress bar when scrolling down
        binding.rvDishes.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // Scrolling behavior can be added here if needed
            }
        });
    }

    private void setupListeners() {
        // Close button
        binding.ibClose.setOnClickListener(v -> dismiss());

        // Search
        binding.svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadDishes(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText == null || newText.length() == 0) {
                    loadDishes("");
                }
                return true;
            }
        });

        // Swipe refresh with yellow color
        binding.swipeRefresh.setColorSchemeResources(R.color.yellow);
        binding.swipeRefresh.setOnRefreshListener(() -> {
            String query = binding.svSearch.getQuery().toString();
            loadDishes(query);
        });
    }

    private void loadDishes(String search) {
        if (binding == null) return;

        binding.tvEmptyState.setVisibility(View.GONE);

        // Use the dishes API endpoint
        repository.getDishes(0, 50, search, new Callback<ApiResponse<List<MealDishResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<MealDishResponse>>> call, Response<ApiResponse<List<MealDishResponse>>> response) {
                if (binding == null) return; // Fragment destroyed

                binding.swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<MealDishResponse> dishes = response.body().getData();
                    if (dishes != null && !dishes.isEmpty()) {
                        adapter.setDishList(dishes);
                        binding.tvEmptyState.setVisibility(View.GONE);
                    } else {
                        adapter.setDishList(null);
                        binding.tvEmptyState.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), R.string.error_loading_dishes, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<MealDishResponse>>> call, Throwable t) {
                if (binding == null) return; // Fragment destroyed

                binding.swipeRefresh.setRefreshing(false);
                Log.e(TAG, "Failed to load dishes", t);

                if (isAdded()) {
                    Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void setOnDishSelectedListener(OnDishSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public interface OnDishSelectedListener {
        void onDishSelected(MealDishResponse dish);
    }
}
