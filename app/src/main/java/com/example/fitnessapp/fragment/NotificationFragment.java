package com.example.fitnessapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fitnessapp.R;
import com.example.fitnessapp.adapter.NotificationAdapter;
import com.example.fitnessapp.model.response.NotificationResponse;
import com.example.fitnessapp.viewmodel.NotificationViewModel;

public class NotificationFragment extends Fragment {

    private static final String TAG = "NotificationFragment";

    private NotificationViewModel viewModel;
    private NotificationAdapter adapter;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout emptyState;
    private LinearLayout errorState;
    private TextView errorMessage;
    private Button retryButton;

    public static NotificationFragment newInstance() {
        return new NotificationFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        observeViewModel();

        // Load notifications on start
        viewModel.loadNotifications();
        viewModel.refreshUnreadCount();
    }

    private void initViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.srl_notifications);
        recyclerView = view.findViewById(R.id.rv_notifications);
        progressBar = view.findViewById(R.id.pb_loading);
        emptyState = view.findViewById(R.id.ll_empty_state);
        errorState = view.findViewById(R.id.ll_error_state);
        errorMessage = view.findViewById(R.id.tv_error_message);
        retryButton = view.findViewById(R.id.btn_retry);

        retryButton.setOnClickListener(v -> {
            errorState.setVisibility(View.GONE);
            viewModel.loadNotifications();
        });
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Handle notification click
        adapter.setOnNotificationClickListener(notification -> {
            if (!notification.isRead()) {
                viewModel.markAsRead(notification.getId());
            }
            // TODO: Handle navigation in Phase 4
            Toast.makeText(getContext(), "Clicked: " + notification.getTitle(), Toast.LENGTH_SHORT).show();
        });

        // Pagination: load more when reaching near end
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    // Load more when 5 items before end
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5
                            && firstVisibleItemPosition >= 0
                            && Boolean.TRUE.equals(viewModel.getHasMore().getValue())) {
                        viewModel.loadMoreNotifications();
                    }
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.loadNotifications();
            viewModel.refreshUnreadCount();
        });
    }

    private void observeViewModel() {
        // Observe notifications list
        viewModel.getNotifications().observe(getViewLifecycleOwner(), notifications -> {
            if (notifications != null) {
                adapter.setNotifications(notifications);

                // Show empty state if no notifications
                if (notifications.isEmpty()) {
                    emptyState.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyState.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                if (isLoading) {
                    // Only show progress bar on initial load, not when refreshing
                    if (adapter.getItemCount() == 0 && !swipeRefreshLayout.isRefreshing()) {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        // Observe error
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                if (adapter.getItemCount() == 0) {
                    // Show error state if no data
                    errorMessage.setText(error);
                    errorState.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    // Show toast if already has data
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
            } else {
                errorState.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh unread count when returning to this screen
        viewModel.refreshUnreadCount();
    }
}
