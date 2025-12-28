package com.example.fitnessapp.fragment.nutrition;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.fitnessapp.R;
import com.example.fitnessapp.databinding.FragmentDishDetailBinding;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.nutrition.DishResponse;
import com.example.fitnessapp.repository.NutritionRepository;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DishDetailFragment extends Fragment {

    public static final String TAG = DishDetailFragment.class.getSimpleName();
    private static final String ARG_DISH_ID = "dish_id";
    private static final String ARG_SHOW_ADD_BUTTON = "show_add_button";

    private FragmentDishDetailBinding binding;
    private NutritionRepository repository;
    private Long dishId;
    private boolean showAddButton = false;
    private OnDishAddListener addListener;

    public static DishDetailFragment newInstance(Long dishId) {
        return newInstance(dishId, false);
    }

    public static DishDetailFragment newInstance(Long dishId, boolean showAddButton) {
        DishDetailFragment fragment = new DishDetailFragment();
        Bundle args = new Bundle();
        if (dishId != null) {
            args.putLong(ARG_DISH_ID, dishId);
        }
        args.putBoolean(ARG_SHOW_ADD_BUTTON, showAddButton);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().containsKey(ARG_DISH_ID)) {
                dishId = getArguments().getLong(ARG_DISH_ID);
            }
            showAddButton = getArguments().getBoolean(ARG_SHOW_ADD_BUTTON, false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDishDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hide MainActivity's app bar
        hideMainAppBar();

        // Setup Toolbar
        setupToolbar();

        // Setup FAB button
        setupFabButton();

        // Initialize Repository
        repository = new NutritionRepository(requireContext());

        // Load dish detail
        if (dishId != null) {
            loadDishDetail(dishId);
        }
    }

    private void setupFabButton() {
        // Show/hide FAB based on parameter
        if (showAddButton) {
            binding.fabAddDish.setVisibility(View.VISIBLE);
            binding.fabAddDish.setOnClickListener(v -> {
                if (addListener != null && dishId != null) {
                    addListener.onDishAdd(dishId);
                    requireActivity().onBackPressed();
                }
            });
        } else {
            binding.fabAddDish.setVisibility(View.GONE);
        }
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            requireActivity().onBackPressed();
        });
    }

    private void loadDishDetail(Long id) {
        Log.d(TAG, "Loading dish detail for ID: " + id);

        binding.progressBar.setVisibility(View.VISIBLE);

        repository.getDishDetail(id, new Callback<ApiResponse<DishResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<DishResponse>> call,
                                 Response<ApiResponse<DishResponse>> response) {
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<DishResponse> apiResponse = response.body();

                    if (apiResponse.isStatus() && apiResponse.getData() != null) {
                        DishResponse dish = apiResponse.getData();
                        Log.d(TAG, "Successfully loaded dish: " + dish.getName());
                        displayDish(dish);
                    } else {
                        Log.e(TAG, "API returned error status");
                        showError("Failed to load dish details");
                    }
                } else {
                    Log.e(TAG, "Response unsuccessful: " + response.code());
                    showError("Failed to load dish details");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DishResponse>> call, Throwable t) {
                Log.e(TAG, "Network failure loading dish", t);
                binding.progressBar.setVisibility(View.GONE);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void displayDish(DishResponse dish) {
        // Set dish name
        binding.tvDishName.setText(dish.getName());

        // Set cooking time
        if (dish.getCookingTime() != null && !dish.getCookingTime().isEmpty()) {
            binding.llCookingTime.setVisibility(View.VISIBLE);
            binding.tvCookingTime.setText(dish.getCookingTime());
        } else {
            binding.llCookingTime.setVisibility(View.GONE);
        }

        // Load dish image
        if (dish.getImage() != null && !dish.getImage().isEmpty()) {
            Glide.with(this)
                    .load(dish.getImage())
                    .placeholder(R.drawable.ic_empty_nutrition_96)
                    .error(R.drawable.ic_empty_nutrition_96)
                    .centerCrop()
                    .into(binding.ivDishImage);
        } else {
            binding.ivDishImage.setImageResource(R.drawable.ic_empty_nutrition_96);
        }

        // Set nutrition facts
        if (dish.getCalories() != null) {
            binding.tvCalories.setText(String.format(Locale.getDefault(), "%.0f", dish.getCalories()));
        } else {
            binding.tvCalories.setText("--");
        }

        if (dish.getProtein() != null) {
            binding.tvProtein.setText(String.format(Locale.getDefault(), "%.0fg", dish.getProtein()));
        } else {
            binding.tvProtein.setText("--");
        }

        if (dish.getCarbs() != null) {
            binding.tvCarbs.setText(String.format(Locale.getDefault(), "%.0fg", dish.getCarbs()));
        } else {
            binding.tvCarbs.setText("--");
        }

        if (dish.getFat() != null) {
            binding.tvFat.setText(String.format(Locale.getDefault(), "%.0fg", dish.getFat()));
        } else {
            binding.tvFat.setText("--");
        }

        // Set ingredients
        if (dish.getIngredients() != null && !dish.getIngredients().isEmpty()) {
            binding.tvIngredients.setText(dish.getIngredients());
        } else {
            binding.tvIngredients.setText("No ingredients information available");
        }

        // Set preparation
        if (dish.getPreparation() != null && !dish.getPreparation().isEmpty()) {
            binding.tvPreparation.setText(dish.getPreparation());
        } else {
            binding.tvPreparation.setText("No preparation instructions available");
        }
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void hideMainAppBar() {
        if (getActivity() instanceof com.example.fitnessapp.MainActivity) {
            ((com.example.fitnessapp.MainActivity) getActivity()).setAppBarVisible(false);
        }
    }

    private void showMainAppBar() {
        if (getActivity() instanceof com.example.fitnessapp.MainActivity) {
            ((com.example.fitnessapp.MainActivity) getActivity()).setAppBarVisible(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideMainAppBar();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!isNutritionFragmentInForeground()) {
            showMainAppBar();
        }
    }

    private boolean isNutritionFragmentInForeground() {
        if (getActivity() == null) return false;

        Fragment currentFragment = getActivity().getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);

        return currentFragment instanceof NutritionMainFragment ||
               currentFragment instanceof MenuDetailFragment ||
               currentFragment instanceof DishDetailFragment;
    }

    public void setOnDishAddListener(OnDishAddListener listener) {
        this.addListener = listener;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public interface OnDishAddListener {
        void onDishAdd(Long dishId);
    }
}
