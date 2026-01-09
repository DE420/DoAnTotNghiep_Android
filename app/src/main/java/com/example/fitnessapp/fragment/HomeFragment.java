package com.example.fitnessapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fitnessapp.R;
import com.example.fitnessapp.adapter.HomeMenuAdapter;
import com.example.fitnessapp.adapter.HomeWorkoutAdapter;
import com.example.fitnessapp.databinding.FragmentHomeBinding;
import com.example.fitnessapp.model.response.nutrition.MenuResponse;
import com.example.fitnessapp.model.response.PlanResponse;
import com.example.fitnessapp.model.response.SuggestionResponse;
import com.example.fitnessapp.session.SessionManager;
import com.example.fitnessapp.viewmodel.HomeViewModel;

import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private SessionManager sessionManager;

    private HomeWorkoutAdapter workoutAdapter;
    private HomeMenuAdapter menuAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize
        sessionManager = SessionManager.getInstance(requireActivity());
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding.setViewModel(viewModel);

        setupRecyclerViews();
        setupSwipeRefresh();
        setupClickListeners();
        observeViewModel();

        // Load data
        loadData();
    }

    private void setupRecyclerViews() {
        // Workout RecyclerView
        workoutAdapter = new HomeWorkoutAdapter(this::onWorkoutClick);
        binding.rvWorkouts.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        binding.rvWorkouts.setAdapter(workoutAdapter);

        // Menu RecyclerView
        menuAdapter = new HomeMenuAdapter(this::onMenuClick);
        binding.rvMenus.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        binding.rvMenus.setAdapter(menuAdapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.yellow);
        binding.swipeRefresh.setOnRefreshListener(this::loadData);
    }

    private void setupClickListeners() {
        // Notification button
        binding.ibNotification.setOnClickListener(v -> {
            // TODO: Navigate to notification screen
            Toast.makeText(requireContext(), "Thông báo", Toast.LENGTH_SHORT).show();
        });

        // See all workouts
        binding.tvSeeAllWorkouts.setOnClickListener(v -> {
            // TODO: Navigate to workout plans screen
            Toast.makeText(requireContext(), "Xem tất cả kế hoạch", Toast.LENGTH_SHORT).show();
        });

        // See all menus
        binding.tvSeeAllMenus.setOnClickListener(v -> {
            // TODO: Navigate to nutrition/menus screen
            Toast.makeText(requireContext(), "Xem tất cả thực đơn", Toast.LENGTH_SHORT).show();
        });

        // Retry button
        binding.btnRetry.setOnClickListener(v -> loadData());
    }

    private void observeViewModel() {
        // Observe suggestion data
        viewModel.getSuggestionData().observe(getViewLifecycleOwner(), this::updateUI);

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                showLoading();
            } else {
                hideLoading();
                binding.swipeRefresh.setRefreshing(false);
            }
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                showError(errorMsg);
            }
        });
    }

    private void loadData() {
        Long userId = sessionManager.getUserId();

        if (userId == null) {
            showError("Phiên đăng nhập không hợp lệ");
            return;
        }

        viewModel.loadRecommendations(userId);
    }

    private void updateUI(SuggestionResponse data) {
        if (data == null) return;

        // Hide error state, show content
        binding.llError.setVisibility(View.GONE);
        binding.swipeRefresh.setVisibility(View.VISIBLE);

        // Update health metrics
        binding.tvBmi.setText(String.format("%.1f", data.getBmi()));
        binding.tvTdee.setText(String.format("%.0f", data.getTdee()));
        binding.tvTargetCalories.setText(String.format("%.0f", data.getTargetCalories()));

        // Update workouts
        List<PlanResponse> workouts = data.getSuggestedWorkoutPlans();
        if (workouts != null && !workouts.isEmpty()) {
            workoutAdapter.setWorkoutPlans(workouts);
            binding.rvWorkouts.setVisibility(View.VISIBLE);
            binding.tvNoWorkouts.setVisibility(View.GONE);
        } else {
            binding.rvWorkouts.setVisibility(View.GONE);
            binding.tvNoWorkouts.setVisibility(View.VISIBLE);
        }

        // Update menus
        List<MenuResponse> menus = data.getSuggestedMenus();
        if (menus != null && !menus.isEmpty()) {
            menuAdapter.setMenus(menus);
            binding.rvMenus.setVisibility(View.VISIBLE);
            binding.tvNoMenus.setVisibility(View.GONE);
        } else {
            binding.rvMenus.setVisibility(View.GONE);
            binding.tvNoMenus.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading() {
        binding.loadingOverlay.setVisibility(View.VISIBLE);
        binding.llError.setVisibility(View.GONE);
    }

    private void hideLoading() {
        binding.loadingOverlay.setVisibility(View.GONE);
    }

    private void showError(String message) {
        binding.swipeRefresh.setVisibility(View.GONE);
        binding.llError.setVisibility(View.VISIBLE);
        binding.tvErrorMessage.setText(message);
    }

    private void onWorkoutClick(PlanResponse plan) {
        // TODO: Navigate to workout plan detail
        Toast.makeText(requireContext(),
                "Chi tiết kế hoạch: " + plan.getName(),
                Toast.LENGTH_SHORT).show();
    }

    private void onMenuClick(MenuResponse menu) {
        // TODO: Navigate to menu detail
        Toast.makeText(requireContext(),
                "Chi tiết thực đơn: " + menu.getName(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
