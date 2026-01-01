package com.example.fitnessapp.fragment.nutrition;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.fitnessapp.R;
import com.example.fitnessapp.adapter.FitnessGoalAdapter;
import com.example.fitnessapp.adapter.nutrition.CreateMenuDishAdapter;
import com.example.fitnessapp.databinding.FragmentCreateEditMenuBinding;
import com.example.fitnessapp.enums.FitnessGoal;
import com.example.fitnessapp.enums.MealType;
import com.example.fitnessapp.model.request.nutrition.MealDishRequest;
import com.example.fitnessapp.model.request.nutrition.MealRequest;
import com.example.fitnessapp.model.request.nutrition.MenuRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.nutrition.DishResponse;
import com.example.fitnessapp.model.response.nutrition.MealDishResponse;
import com.example.fitnessapp.model.response.nutrition.MealResponse;
import com.example.fitnessapp.model.response.nutrition.MenuResponse;
import com.example.fitnessapp.repository.NutritionRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateEditMenuFragment extends Fragment {

    private static final String TAG = "CreateEditMenuFrag";
    private static final String ARG_MENU_ID = "menu_id";
    private static final String ARG_MENU_DATA = "menu_data";
    private static final String CHANNEL_ID = "menu_save_channel";
    private static final int NOTIFICATION_ID_CREATE = 1001;
    private static final int NOTIFICATION_ID_UPDATE = 1002;

    private FragmentCreateEditMenuBinding binding;
    private NutritionRepository repository;
    private Long menuId;
    private MenuResponse existingMenu;
    private boolean isEditing;
    private boolean isSaveInProgress = false;

    // Adapters for each meal type
    private CreateMenuDishAdapter breakfastAdapter;
    private CreateMenuDishAdapter lunchAdapter;
    private CreateMenuDishAdapter dinnerAdapter;
    private CreateMenuDishAdapter snacksAdapter;

    private FitnessGoalAdapter fitnessGoalAdapter;
    private FitnessGoal selectedFitnessGoal = FitnessGoal.MUSCLE_GAIN;

    public static CreateEditMenuFragment newInstance(Long menuId) {
        return newInstance(menuId, null);
    }

    public static CreateEditMenuFragment newInstance(Long menuId, MenuResponse menuData) {
        CreateEditMenuFragment fragment = new CreateEditMenuFragment();
        if (menuId != null) {
            Bundle args = new Bundle();
            args.putLong(ARG_MENU_ID, menuId);
            if (menuData != null) {
                args.putSerializable(ARG_MENU_DATA, menuData);
            }
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new NutritionRepository(requireContext());

        // Create notification channel for menu save operations
        createNotificationChannel();

        if (getArguments() != null && getArguments().containsKey(ARG_MENU_ID)) {
            menuId = getArguments().getLong(ARG_MENU_ID);
            isEditing = true;

            // Check if MenuResponse data was passed
            if (getArguments().containsKey(ARG_MENU_DATA)) {
                existingMenu = (MenuResponse) getArguments().getSerializable(ARG_MENU_DATA);
                Log.d(TAG, "MenuResponse data received via arguments for: " +
                    (existingMenu != null ? existingMenu.getName() : "null"));
            }
        }

        // Image picker removed - backend doesn't support menu images
        /*
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        displaySelectedImage();
                    }
                }
        );
        */
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateEditMenuBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupUI();
        setupAdapters();
        setupListeners();

        // Initialize total nutrition display
        updateTotalNutrition();

        if (isEditing && menuId != null) {
            // If we already have menu data, display it immediately
            if (existingMenu != null) {
                Log.d(TAG, "Displaying existing menu data immediately: " + existingMenu.getName());
                populateMenuData(existingMenu);
                // Fetch updated data in background
                loadMenuDataInBackground();
            } else {
                // No existing data, show progress and fetch
                loadMenuData();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Hide MainActivity's app bar when this fragment becomes visible
        hideMainAppBar();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Don't show app bar in onPause - it will show when fragment is destroyed
        // This prevents the header from appearing when navigating back
    }

    @Override
    public void onStop() {
        super.onStop();
        // Only show app bar if we're not going back to NutritionMainFragment
        if (!isReturningToNutritionFlow()) {
            showMainAppBar();
        }
    }

    /**
     * Check if we're going back to a nutrition-related fragment
     */
    private boolean isReturningToNutritionFlow() {
        if (getActivity() == null) return false;

        // Check all fragments in the back stack
        for (Fragment fragment : getActivity().getSupportFragmentManager().getFragments()) {
            if (fragment instanceof NutritionMainFragment && fragment.isHidden()) {
                return true;
            }
            if (fragment instanceof MenuDetailFragment && fragment.isHidden()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Hide MainActivity's app bar
     */
    private void hideMainAppBar() {
        if (getActivity() instanceof com.example.fitnessapp.MainActivity) {
            ((com.example.fitnessapp.MainActivity) getActivity()).setAppBarVisible(false);
        }
    }

    /**
     * Show MainActivity's app bar
     */
    private void showMainAppBar() {
        if (getActivity() instanceof com.example.fitnessapp.MainActivity) {
            ((com.example.fitnessapp.MainActivity) getActivity()).setAppBarVisible(true);
        }
    }

    /**
     * Create notification channel for menu save operations
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Menu Save";
            String description = "Notifications for menu save operations";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Show a progress notification
     */
    private void showProgressNotification(Context context, int notificationId, String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_empty_nutrition_96)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setProgress(0, 0, true)
                .setOngoing(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
        }
    }

    /**
     * Show a success notification
     */
    private void showSuccessNotification(Context context, int notificationId, String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_empty_nutrition_96)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
        }
    }

    /**
     * Show an error notification
     */
    private void showErrorNotification(Context context, int notificationId, String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_empty_nutrition_96)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
        }
    }

    private void setupUI() {
        // Set title
        binding.tvTitle.setText(isEditing ? R.string.edit_menu : R.string.create_menu);

        // Setup fitness goal spinner
        setupFitnessGoalSpinner();
    }

    private void setupFitnessGoalSpinner() {
        List<FitnessGoal> fitnessGoalList = new ArrayList<>(Arrays.asList(FitnessGoal.values()));
        fitnessGoalAdapter = new FitnessGoalAdapter(requireContext(), fitnessGoalList);
        binding.spFitnessGoal.setAdapter(fitnessGoalAdapter);

        binding.spFitnessGoal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fitnessGoalAdapter.setSelectedPosition(position);
                FitnessGoal goal = fitnessGoalAdapter.getItem(position);
                if (goal != null) {
                    selectedFitnessGoal = goal;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupAdapters() {
        // Breakfast adapter
        breakfastAdapter = new CreateMenuDishAdapter(requireContext());
        binding.rvBreakfastDishes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBreakfastDishes.setAdapter(breakfastAdapter);
        breakfastAdapter.setOnDishChangeListener(this::updateTotalNutrition);
        breakfastAdapter.setOnDishClickListener(this::openDishDetail);

        // Lunch adapter
        lunchAdapter = new CreateMenuDishAdapter(requireContext());
        binding.rvLunchDishes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvLunchDishes.setAdapter(lunchAdapter);
        lunchAdapter.setOnDishChangeListener(this::updateTotalNutrition);
        lunchAdapter.setOnDishClickListener(this::openDishDetail);

        // Dinner adapter
        dinnerAdapter = new CreateMenuDishAdapter(requireContext());
        binding.rvDinnerDishes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDinnerDishes.setAdapter(dinnerAdapter);
        dinnerAdapter.setOnDishChangeListener(this::updateTotalNutrition);
        dinnerAdapter.setOnDishClickListener(this::openDishDetail);

        // Snacks adapter
        snacksAdapter = new CreateMenuDishAdapter(requireContext());
        binding.rvSnacksDishes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSnacksDishes.setAdapter(snacksAdapter);
        snacksAdapter.setOnDishChangeListener(this::updateTotalNutrition);
        snacksAdapter.setOnDishClickListener(this::openDishDetail);
    }

    private void setupListeners() {
        // Back button
        binding.ibBack.setOnClickListener(v -> requireActivity().onBackPressed());

        // Image button removed - backend doesn't support menu images
        // binding.btnChooseImage.setOnClickListener(v -> pickImage());

        // Add dish buttons
        binding.btnAddBreakfastDish.setOnClickListener(v -> openDishSelector(MealType.BREAKFAST));
        binding.btnAddLunchDish.setOnClickListener(v -> openDishSelector(MealType.LUNCH));
        binding.btnAddDinnerDish.setOnClickListener(v -> openDishSelector(MealType.DINNER));
        binding.btnAddSnacksDish.setOnClickListener(v -> openDishSelector(MealType.EXTRA_MEAL));

        // Action buttons
        binding.btnCancel.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.btnSave.setOnClickListener(v -> saveMenu());
    }

    // Image upload removed - backend doesn't support menu images
    /*
    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void displaySelectedImage() {
        if (selectedImageUri != null) {
            Glide.with(this)
                    .load(selectedImageUri)
                    .placeholder(R.drawable.ic_empty_nutrition_96)
                    .centerCrop()
                    .into(binding.ivMenuImage);
        }
    }
    */

    private void openDishSelector(MealType mealType) {
        DishSelectorFragment dishSelector = DishSelectorFragment.newInstance(mealType);
        dishSelector.setOnDishSelectedListener(dish -> {
            // Add dish to appropriate adapter
            switch (mealType) {
                case BREAKFAST:
                    breakfastAdapter.addDish(dish);
                    break;
                case LUNCH:
                    lunchAdapter.addDish(dish);
                    break;
                case DINNER:
                    dinnerAdapter.addDish(dish);
                    break;
                case EXTRA_MEAL:
                    snacksAdapter.addDish(dish);
                    break;
            }
            updateTotalNutrition();
        });

        dishSelector.show(getParentFragmentManager(), "DishSelector");
    }

    private void updateTotalNutrition() {
        double totalCalories = 0;
        double totalProtein = 0;
        double totalCarbs = 0;
        double totalFat = 0;

        // Calculate totals from all meals
        double breakfastCalories = calculateMealCalories(breakfastAdapter.getDishList());
        double lunchCalories = calculateMealCalories(lunchAdapter.getDishList());
        double dinnerCalories = calculateMealCalories(dinnerAdapter.getDishList());
        double snacksCalories = calculateMealCalories(snacksAdapter.getDishList());

        totalCalories = breakfastCalories + lunchCalories + dinnerCalories + snacksCalories;

        totalProtein += calculateMealProtein(breakfastAdapter.getDishList());
        totalProtein += calculateMealProtein(lunchAdapter.getDishList());
        totalProtein += calculateMealProtein(dinnerAdapter.getDishList());
        totalProtein += calculateMealProtein(snacksAdapter.getDishList());

        totalCarbs += calculateMealCarbs(breakfastAdapter.getDishList());
        totalCarbs += calculateMealCarbs(lunchAdapter.getDishList());
        totalCarbs += calculateMealCarbs(dinnerAdapter.getDishList());
        totalCarbs += calculateMealCarbs(snacksAdapter.getDishList());

        totalFat += calculateMealFat(breakfastAdapter.getDishList());
        totalFat += calculateMealFat(lunchAdapter.getDishList());
        totalFat += calculateMealFat(dinnerAdapter.getDishList());
        totalFat += calculateMealFat(snacksAdapter.getDishList());

        // Log calculated totals for debugging
        Log.d(TAG, "Total Nutrition - Calories: " + totalCalories +
                ", Protein: " + totalProtein +
                ", Carbs: " + totalCarbs +
                ", Fat: " + totalFat);

        // Update total nutrition UI
        binding.tvTotalCalories.setText(String.format(Locale.getDefault(), "%.0f", totalCalories));
        binding.tvTotalProtein.setText(String.format(Locale.getDefault(), "%.0fg", totalProtein));
        binding.tvTotalCarbs.setText(String.format(Locale.getDefault(), "%.0fg", totalCarbs));
        binding.tvTotalFat.setText(String.format(Locale.getDefault(), "%.0fg", totalFat));

        // Update per-meal calories
        binding.tvBreakfastCalories.setText(String.format(Locale.getDefault(), "%.0f kcal", breakfastCalories));
        binding.tvLunchCalories.setText(String.format(Locale.getDefault(), "%.0f kcal", lunchCalories));
        binding.tvDinnerCalories.setText(String.format(Locale.getDefault(), "%.0f kcal", dinnerCalories));
        binding.tvSnacksCalories.setText(String.format(Locale.getDefault(), "%.0f kcal", snacksCalories));

        // Update per-meal nutrition UI
        updateMealNutrition(binding.tvBreakfastNutrition, breakfastAdapter.getDishList());
        updateMealNutrition(binding.tvLunchNutrition, lunchAdapter.getDishList());
        updateMealNutrition(binding.tvDinnerNutrition, dinnerAdapter.getDishList());
        updateMealNutrition(binding.tvSnacksNutrition, snacksAdapter.getDishList());

        // Update empty states
        updateEmptyStates();
    }

    private void updateEmptyStates() {
        // Show/hide empty states for each meal based on whether there are dishes
        binding.tvBreakfastEmpty.setVisibility(
                breakfastAdapter.getDishList().isEmpty() ? View.VISIBLE : View.GONE);
        binding.rvBreakfastDishes.setVisibility(
                breakfastAdapter.getDishList().isEmpty() ? View.GONE : View.VISIBLE);

        binding.tvLunchEmpty.setVisibility(
                lunchAdapter.getDishList().isEmpty() ? View.VISIBLE : View.GONE);
        binding.rvLunchDishes.setVisibility(
                lunchAdapter.getDishList().isEmpty() ? View.GONE : View.VISIBLE);

        binding.tvDinnerEmpty.setVisibility(
                dinnerAdapter.getDishList().isEmpty() ? View.VISIBLE : View.GONE);
        binding.rvDinnerDishes.setVisibility(
                dinnerAdapter.getDishList().isEmpty() ? View.GONE : View.VISIBLE);

        binding.tvSnacksEmpty.setVisibility(
                snacksAdapter.getDishList().isEmpty() ? View.VISIBLE : View.GONE);
        binding.rvSnacksDishes.setVisibility(
                snacksAdapter.getDishList().isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void updateMealNutrition(android.widget.TextView textView, List<CreateMenuDishAdapter.MealDishItem> dishes) {
        double protein = calculateMealProtein(dishes);
        double carbs = calculateMealCarbs(dishes);
        double fat = calculateMealFat(dishes);

        String nutritionText = String.format(Locale.getDefault(),
                "P: %.0fg | C: %.0fg | F: %.0fg",
                protein, carbs, fat);
        textView.setText(nutritionText);
    }

    private double calculateMealCalories(List<CreateMenuDishAdapter.MealDishItem> dishes) {
        double total = 0;
        for (CreateMenuDishAdapter.MealDishItem item : dishes) {
            // Calculate from per-serving calories * current quantity
            if (item.getDish().getCalories() != null) {
                total += item.getDish().getCalories() * item.getQuantity();
            }
        }
        return total;
    }

    private double calculateMealProtein(List<CreateMenuDishAdapter.MealDishItem> dishes) {
        double total = 0;
        for (CreateMenuDishAdapter.MealDishItem item : dishes) {
            if (item.getDish().getProtein() != null) {
                double dishProtein = item.getDish().getProtein() * item.getQuantity();
                total += dishProtein;
                Log.d(TAG, "Dish: " + item.getDish().getName() +
                        ", protein per serving: " + item.getDish().getProtein() +
                        ", quantity: " + item.getQuantity() +
                        ", total: " + dishProtein);
            }
        }
        return total;
    }

    private double calculateMealCarbs(List<CreateMenuDishAdapter.MealDishItem> dishes) {
        double total = 0;
        for (CreateMenuDishAdapter.MealDishItem item : dishes) {
            if (item.getDish().getCarbs() != null) {
                total += item.getDish().getCarbs() * item.getQuantity();
            }
        }
        return total;
    }

    private double calculateMealFat(List<CreateMenuDishAdapter.MealDishItem> dishes) {
        double total = 0;
        for (CreateMenuDishAdapter.MealDishItem item : dishes) {
            if (item.getDish().getFat() != null) {
                total += item.getDish().getFat() * item.getQuantity();
            }
        }
        return total;
    }

    private void loadMenuData() {
        binding.progressBar.setVisibility(View.VISIBLE);

        repository.getMenuDetail(menuId, new Callback<ApiResponse<MenuResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<MenuResponse>> call, Response<ApiResponse<MenuResponse>> response) {
                // Check if view is still attached
                if (binding == null || !isAdded()) {
                    return;
                }

                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    existingMenu = response.body().getData();
                    populateMenuData(existingMenu);
                } else {
                    Toast.makeText(requireContext(), "Failed to load menu", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<MenuResponse>> call, Throwable t) {
                // Check if view is still attached
                if (binding == null || !isAdded()) {
                    return;
                }

                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
            }
        });
    }

    /**
     * Load menu data in background without showing progress bar
     * Used when we already have data to display
     */
    private void loadMenuDataInBackground() {
        Log.d(TAG, "Fetching updated menu data in background...");

        repository.getMenuDetail(menuId, new Callback<ApiResponse<MenuResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<MenuResponse>> call, Response<ApiResponse<MenuResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    MenuResponse updatedMenu = response.body().getData();
                    Log.d(TAG, "Received updated menu data: " + updatedMenu.getName());

                    // Only update meals data (keep user's current edits for name/description)
                    existingMenu = updatedMenu;
                    refreshMealsData(updatedMenu);
                } else {
                    Log.w(TAG, "Failed to fetch updated menu data");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<MenuResponse>> call, Throwable t) {
                Log.e(TAG, "Error fetching updated menu data", t);
            }
        });
    }

    /**
     * Refresh only the meals data, preserving user edits to basic fields
     */
    private void refreshMealsData(MenuResponse menu) {
        // Clear current meal adapters
        breakfastAdapter.getDishList().clear();
        lunchAdapter.getDishList().clear();
        dinnerAdapter.getDishList().clear();
        snacksAdapter.getDishList().clear();

        // Repopulate with fresh data
        populateMeals(menu);
    }

    private void populateMenuData(MenuResponse menu) {
        // Set basic fields
        binding.etMenuName.setText(menu.getName());
        binding.etDescription.setText(menu.getDescription());

        // Set fitness goal
        if (menu.getFitnessGoal() != null) {
            FitnessGoal[] goals = FitnessGoal.values();
            for (int i = 0; i < goals.length; i++) {
                if (goals[i] == menu.getFitnessGoal()) {
                    // FitnessGoalAdapter has header at position 0, so add 1
                    binding.spFitnessGoal.setSelection(i + 1);
                    break;
                }
            }
        }

        // Image loading removed - backend doesn't support menu images
        /*
        if (menu.getImage() != null && !menu.getImage().isEmpty()) {
            Glide.with(this)
                    .load(menu.getImage())
                    .placeholder(R.drawable.ic_empty_nutrition_96)
                    .centerCrop()
                    .into(binding.ivMenuImage);
        }
        */

        // Populate meals
        populateMeals(menu);
    }

    /**
     * Populate meals from menu data into adapters
     * Fetches missing nutrition data from dish API if needed
     */
    private void populateMeals(MenuResponse menu) {
        if (menu.getMeals() == null || menu.getMeals().isEmpty()) {
            Log.d(TAG, "No meals to populate");
            return;
        }

        Log.d(TAG, "Starting to populate meals, count: " + menu.getMeals().size());

        for (MealResponse meal : menu.getMeals()) {
            if (meal.getDishes() == null || meal.getDishes().isEmpty()) {
                continue;
            }

            Log.d(TAG, "Populating meal type: " + meal.getMealType() + ", dishes: " + meal.getDishes().size());

            // Get the appropriate adapter based on meal type
            CreateMenuDishAdapter adapter = getAdapterForMealType(meal.getMealType());
            if (adapter == null) {
                continue;
            }

            // Convert MealDishResponse to MealDishItem and add to adapter
            for (MealDishResponse dish : meal.getDishes()) {
                int quantity = dish.getQuantity() != null ? dish.getQuantity() : 1;

                Log.d(TAG, "Processing dish: " + dish.getName() +
                        ", dishId=" + dish.getDishId() +
                        ", quantity=" + quantity +
                        ", calories=" + dish.getCalories() +
                        ", protein=" + dish.getProtein() +
                        ", carbs=" + dish.getCarbs() +
                        ", fat=" + dish.getFat() +
                        ", totalCalories=" + dish.getTotalCalories());

                // Check if we need to fetch nutrition data
                boolean needsNutrition = (dish.getCalories() == null &&
                                         dish.getProtein() == null &&
                                         dish.getCarbs() == null &&
                                         dish.getFat() == null);

                if (needsNutrition && dish.getDishId() != null) {
                    Log.d(TAG, "Dish " + dish.getName() + " needs nutrition data, fetching from API...");
                    fetchDishNutrition(dish, adapter);
                } else {
                    // Try to convert total values to per-serving if we have them
                    if (quantity > 0) {
                        if (dish.getTotalCalories() != null && dish.getCalories() == null) {
                            dish.setCalories((double) dish.getTotalCalories() / quantity);
                            Log.d(TAG, "Converted totalCalories to per-serving: " + dish.getCalories());
                        }
                        if (dish.getTotalProtein() != null && dish.getProtein() == null) {
                            dish.setProtein((double) dish.getTotalProtein() / quantity);
                            Log.d(TAG, "Converted totalProtein to per-serving: " + dish.getProtein());
                        }
                        if (dish.getTotalCarbs() != null && dish.getCarbs() == null) {
                            dish.setCarbs((double) dish.getTotalCarbs() / quantity);
                            Log.d(TAG, "Converted totalCarbs to per-serving: " + dish.getCarbs());
                        }
                        if (dish.getTotalFat() != null && dish.getFat() == null) {
                            dish.setFat((double) dish.getTotalFat() / quantity);
                            Log.d(TAG, "Converted totalFat to per-serving: " + dish.getFat());
                        }
                    }
                }

                CreateMenuDishAdapter.MealDishItem item =
                    new CreateMenuDishAdapter.MealDishItem(dish, quantity);
                adapter.getDishList().add(item);
            }

            adapter.notifyDataSetChanged();
        }

        // Update total nutrition
        updateTotalNutrition();
    }

    /**
     * Fetch nutrition data for a dish from the API
     */
    private void fetchDishNutrition(MealDishResponse dish, CreateMenuDishAdapter adapter) {
        repository.getDishDetail(dish.getDishId(), new Callback<ApiResponse<DishResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<DishResponse>> call,
                                 Response<ApiResponse<DishResponse>> response) {
                if (response.isSuccessful() && response.body() != null &&
                        response.body().isStatus() && response.body().getData() != null) {
                    DishResponse dishDetail = response.body().getData();

                    // Update nutrition data (cast Float to Double)
                    if (dishDetail.getCalories() != null) {
                        dish.setCalories(dishDetail.getCalories().doubleValue());
                    }
                    if (dishDetail.getProtein() != null) {
                        dish.setProtein(dishDetail.getProtein().doubleValue());
                    }
                    if (dishDetail.getCarbs() != null) {
                        dish.setCarbs(dishDetail.getCarbs().doubleValue());
                    }
                    if (dishDetail.getFat() != null) {
                        dish.setFat(dishDetail.getFat().doubleValue());
                    }

                    Log.d(TAG, "Fetched nutrition for " + dishDetail.getName() +
                            ": calories=" + dishDetail.getCalories() +
                            ", protein=" + dishDetail.getProtein() +
                            ", carbs=" + dishDetail.getCarbs() +
                            ", fat=" + dishDetail.getFat());

                    adapter.notifyDataSetChanged();
                    updateTotalNutrition();
                } else {
                    Log.w(TAG, "Failed to fetch nutrition for dish ID: " + dish.getDishId());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DishResponse>> call, Throwable t) {
                Log.e(TAG, "Error fetching nutrition for dish ID: " + dish.getDishId(), t);
            }
        });
    }

    /**
     * Get adapter for specific meal type
     */
    private CreateMenuDishAdapter getAdapterForMealType(MealType mealType) {
        if (mealType == null) {
            return null;
        }

        switch (mealType) {
            case BREAKFAST:
                return breakfastAdapter;
            case LUNCH:
                return lunchAdapter;
            case DINNER:
                return dinnerAdapter;
            case EXTRA_MEAL:
                return snacksAdapter;
            default:
                return null;
        }
    }

    private void saveMenu() {
        // Validate inputs
        String name = binding.etMenuName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter menu name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Don't allow multiple save operations
        if (isSaveInProgress) {
            Toast.makeText(requireContext(), "Save operation already in progress", Toast.LENGTH_SHORT).show();
            return;
        }

        String description = binding.etDescription.getText().toString().trim();

        // Build menu request
        MenuRequest menuRequest = new MenuRequest();
        menuRequest.setName(name);
        menuRequest.setDescription(description);
        menuRequest.setFitnessGoal(selectedFitnessGoal);
        menuRequest.setIsPrivate(true); // Always private since we removed the toggle

        // Build meals
        List<MealRequest> meals = new ArrayList<>();
        meals.addAll(buildMealRequests(MealType.BREAKFAST, breakfastAdapter.getDishList()));
        meals.addAll(buildMealRequests(MealType.LUNCH, lunchAdapter.getDishList()));
        meals.addAll(buildMealRequests(MealType.DINNER, dinnerAdapter.getDishList()));
        meals.addAll(buildMealRequests(MealType.EXTRA_MEAL, snacksAdapter.getDishList()));

        menuRequest.setMeals(meals);

        // Show center loading overlay with blur background
        binding.loadingOverlay.setVisibility(View.VISIBLE);
        binding.btnSave.setEnabled(false);
        isSaveInProgress = true;

        // Backend doesn't support menu images - always pass null
        File imageFile = null;

        // Capture application context for notifications (works even after fragment detaches)
        final Context appContext = requireContext().getApplicationContext();

        // Show notification about background save
        int notificationId = isEditing ? NOTIFICATION_ID_UPDATE : NOTIFICATION_ID_CREATE;
        String notificationTitle = isEditing ? "Updating Menu" : "Creating Menu";
        showProgressNotification(appContext, notificationId, notificationTitle, "Saving " + name + "...");

        // Show toast that allows user to navigate away
        Toast.makeText(requireContext(),
            "Saving in background. You can navigate away.",
            Toast.LENGTH_LONG).show();

        if (isEditing && menuId != null) {
            updateMenu(menuId, imageFile, menuRequest, appContext);
        } else {
            createMenu(imageFile, menuRequest, appContext);
        }
    }

    private List<MealRequest> buildMealRequests(MealType mealType, List<CreateMenuDishAdapter.MealDishItem> dishes) {
        if (dishes.isEmpty()) {
            return new ArrayList<>();
        }

        MealRequest meal = new MealRequest();
        meal.setMealType(mealType);

        List<MealDishRequest> mealDishes = new ArrayList<>();
        for (CreateMenuDishAdapter.MealDishItem item : dishes) {
            MealDishRequest dishRequest = new MealDishRequest();
            dishRequest.setDishId(item.getDish().getDishId());
            dishRequest.setQuantity(item.getQuantity());
            mealDishes.add(dishRequest);
        }

        meal.setDishes(mealDishes);

        List<MealRequest> result = new ArrayList<>();
        result.add(meal);
        return result;
    }

    private void createMenu(File imageFile, MenuRequest menuRequest, final Context appContext) {
        repository.createMenu(imageFile, menuRequest, new Callback<ApiResponse<MenuResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<MenuResponse>> call, Response<ApiResponse<MenuResponse>> response) {
                // Update UI if still attached
                if (binding != null && isAdded()) {
                    binding.loadingOverlay.setVisibility(View.GONE);
                    binding.btnSave.setEnabled(true);
                }

                isSaveInProgress = false;

                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    // Show success notification (using app context, works even if fragment detached)
                    showSuccessNotification(appContext, NOTIFICATION_ID_CREATE,
                        "Menu Created",
                        menuRequest.getName() + " has been created successfully");

                    // Navigate back if still attached
                    if (isAdded()) {
                        requireActivity().onBackPressed();
                    }
                } else {
                    // Show error notification (using app context, works even if fragment detached)
                    showErrorNotification(appContext, NOTIFICATION_ID_CREATE,
                        "Failed to Create Menu",
                        "Could not create " + menuRequest.getName());

                    // Show toast if still attached
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Failed to create menu", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<MenuResponse>> call, Throwable t) {
                // Update UI if still attached
                if (binding != null && isAdded()) {
                    binding.loadingOverlay.setVisibility(View.GONE);
                    binding.btnSave.setEnabled(true);
                }

                isSaveInProgress = false;

                // Show error notification (using app context, works even if fragment detached)
                showErrorNotification(appContext, NOTIFICATION_ID_CREATE,
                    "Network Error",
                    "Failed to create menu: " + t.getMessage());

                // Show toast if still attached
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateMenu(Long menuId, File imageFile, MenuRequest menuRequest, final Context appContext) {
        repository.updateMenu(menuId, imageFile, menuRequest, new Callback<ApiResponse<MenuResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<MenuResponse>> call, Response<ApiResponse<MenuResponse>> response) {
                // Update UI if still attached
                if (binding != null && isAdded()) {
                    binding.loadingOverlay.setVisibility(View.GONE);
                    binding.btnSave.setEnabled(true);
                }

                isSaveInProgress = false;

                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    // Show success notification (using app context, works even if fragment detached)
                    showSuccessNotification(appContext, NOTIFICATION_ID_UPDATE,
                        "Menu Updated",
                        menuRequest.getName() + " has been updated successfully");

                    // Navigate back if still attached
                    if (isAdded()) {
                        requireActivity().onBackPressed();
                    }
                } else {
                    // Show error notification (using app context, works even if fragment detached)
                    showErrorNotification(appContext, NOTIFICATION_ID_UPDATE,
                        "Failed to Update Menu",
                        "Could not update " + menuRequest.getName());

                    // Show toast if still attached
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Failed to update menu", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<MenuResponse>> call, Throwable t) {
                // Update UI if still attached
                if (binding != null && isAdded()) {
                    binding.loadingOverlay.setVisibility(View.GONE);
                    binding.btnSave.setEnabled(true);
                }

                isSaveInProgress = false;

                // Show error notification (using app context, works even if fragment detached)
                showErrorNotification(appContext, NOTIFICATION_ID_UPDATE,
                    "Network Error",
                    "Failed to update menu: " + t.getMessage());

                // Show toast if still attached
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Convert URI to File - REMOVED (backend doesn't support menu images)
     * Copies the content from URI to a temporary file in cache directory
     */
    /*
    private File getFileFromUri(Uri uri) throws Exception {
        // Get content resolver
        android.content.ContentResolver contentResolver = requireContext().getContentResolver();

        // Create a temporary file
        String fileName = "menu_image_" + System.currentTimeMillis();
        String mimeType = contentResolver.getType(uri);

        // Determine file extension
        String extension = ".jpg";
        if (mimeType != null && mimeType.startsWith("image/")) {
            if (mimeType.contains("png")) {
                extension = ".png";
            } else if (mimeType.contains("jpeg") || mimeType.contains("jpg")) {
                extension = ".jpg";
            }
        }

        File tempFile = new File(requireContext().getCacheDir(), fileName + extension);

        // Copy content to temp file
        try (java.io.InputStream inputStream = contentResolver.openInputStream(uri);
             java.io.FileOutputStream outputStream = new java.io.FileOutputStream(tempFile)) {

            if (inputStream == null) {
                throw new Exception("Cannot open input stream");
            }

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        }

        return tempFile;
    }
    */

    private void openDishDetail(Long dishId) {
        DishDetailFragment fragment = DishDetailFragment.newInstance(dishId);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
