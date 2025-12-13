package com.example.fitnessapp.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.fitnessapp.R;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.ExerciseResponse;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.network.RetrofitClient;
import com.example.fitnessapp.session.SessionManager;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;

import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExerciseDetailFragment extends Fragment {

    private static final String TAG = "ExerciseDetailFragment";
    private static final String ARG_EXERCISE_ID = "exercise_id";

    private Long exerciseId;
    private String currentVideoUrl; // To store video URL for play button

    private ImageView backButton;
    private TextView tvExerciseName, tvExerciseLevel, tvTrainingType;
    private Button btnGuideline, btnNote;
    private LinearLayout layoutGuidelineContent, layoutNoteContent;
    private LinearLayout llEquipmentList, llStepsList;
    private LinearLayout llMuscleGroupList, llTipsList, llMistakesList, llHealthBenefitsList;
    private PlayerView videoPlayerView;
    private ExoPlayer player;
    private SessionManager sessionManager;

    public static ExerciseDetailFragment newInstance(Long exerciseId) {
        ExerciseDetailFragment fragment = new ExerciseDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_EXERCISE_ID, exerciseId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            exerciseId = getArguments().getLong(ARG_EXERCISE_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise_detail, container, false);

        sessionManager = SessionManager.getInstance(requireContext());

        // Initialize Views
        backButton = view.findViewById(R.id.back_button_detail);
        tvExerciseName = view.findViewById(R.id.tv_exercise_name);
        tvExerciseLevel = view.findViewById(R.id.tv_exercise_level);
        tvTrainingType = view.findViewById(R.id.tv_training_type);
        btnGuideline = view.findViewById(R.id.btn_guideline);
        btnNote = view.findViewById(R.id.btn_note);
        layoutGuidelineContent = view.findViewById(R.id.layout_guideline_content);
        layoutNoteContent = view.findViewById(R.id.layout_note_content);
        llEquipmentList = view.findViewById(R.id.ll_equipment_list);
        llStepsList = view.findViewById(R.id.ll_steps_list);
        llMuscleGroupList = view.findViewById(R.id.ll_muscle_group_list);
        llTipsList = view.findViewById(R.id.ll_tips_list);
        llMistakesList = view.findViewById(R.id.ll_mistakes_list);
        llHealthBenefitsList = view.findViewById(R.id.ll_health_benefits_list);
        videoPlayerView = view.findViewById(R.id.video_player_view);

        // Setup listeners
        backButton.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                Toast.makeText(getContext(), "Previous page unavailable.", Toast.LENGTH_SHORT).show();
            }
        });

        btnGuideline.setOnClickListener(v -> showContent(true));
        btnNote.setOnClickListener(v -> showContent(false));

        // Initial state: show Guideline
        showContent(true);

        if (exerciseId != null) {
            fetchExerciseDetail(exerciseId);
        } else {
            Toast.makeText(getContext(), "Exercise unvailable.", Toast.LENGTH_SHORT).show();
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        }

        return view;
    }

    private void fetchExerciseDetail(Long id) {
        String accessToken = sessionManager.getAccessToken();
        String authorizationHeader = null;

        if (accessToken != null && !accessToken.isEmpty()) {
            authorizationHeader = "Bearer " + accessToken;
        } else {
            Toast.makeText(getContext(), "Token unavailable.", Toast.LENGTH_LONG).show();
            return;
        }

        ApiService apiService = RetrofitClient.getApiService();
        Call<ApiResponse<ExerciseResponse>> call = apiService.getExerciseDetail(authorizationHeader, id);

        call.enqueue(new Callback<ApiResponse<ExerciseResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<ExerciseResponse>> call, @NonNull Response<ApiResponse<ExerciseResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ExerciseResponse> apiResponse = response.body();
                    if (apiResponse.isStatus()) {
                        ExerciseResponse exerciseDetail = apiResponse.getData();
                        if (exerciseDetail != null) {
                            displayExerciseDetail(exerciseDetail);
                            initializePlayer(exerciseDetail.getVideoUrl());
                        } else {
                            Toast.makeText(getContext(), "Exercise detail unavailable.", Toast.LENGTH_SHORT).show();
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
            public void onFailure(@NonNull Call<ApiResponse<ExerciseResponse>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Connection error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Connection error: " + t.getMessage(), t);
            }
        });
    }

    private void displayExerciseDetail(ExerciseResponse detail) {
        tvExerciseName.setText(detail.getName());
        tvExerciseLevel.setText(detail.getLevel());
        tvTrainingType.setText(detail.getTrainingType() != null ? detail.getTrainingType() : "N/A");

        // Set background color for level text based on level (optional, you can expand this)
        if ("Beginner".equalsIgnoreCase(detail.getLevel())) {
            tvExerciseLevel.setBackgroundResource(R.drawable.bg_level_beginner);
        } else if ("Intermediate".equalsIgnoreCase(detail.getLevel())) {
            tvExerciseLevel.setBackgroundResource(R.drawable.bg_level_intermediate);
        } else if ("Advanced".equalsIgnoreCase(detail.getLevel())) {
            tvExerciseLevel.setBackgroundResource(R.drawable.bg_level_advanced);
        } else {
            tvExerciseLevel.setBackgroundResource(R.drawable.bg_level_default);
        }

        // Guideline Content
        addListItemsToLayout(llEquipmentList, detail.getEquipments(), "• ");
        addIndexedListItemsToLayout(llStepsList, detail.getSteps());

        // Note Content
        // Add Main Muscle Groups
        addMuscleGroupItemsToLayout(llMuscleGroupList, detail.getPrimaryMuscles(), "Main", R.drawable.bg_main_muscle_rounded, ContextCompat.getColor(requireContext(), R.color.black));

        // Add Secondary Muscle Groups
        addMuscleGroupItemsToLayout(llMuscleGroupList, detail.getSecondaryMuscles(), "Secondary", R.drawable.bg_secondary_muscle_rounded, ContextCompat.getColor(requireContext(), R.color.black));

        // If no muscle groups are added at all, add "N/A"
        if ((detail.getPrimaryMuscles() == null || detail.getPrimaryMuscles().isEmpty()) &&
                (detail.getSecondaryMuscles() == null || detail.getSecondaryMuscles().isEmpty())) {
            TextView tv = new TextView(requireContext());
            tv.setText("N/A");
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.white85));
            llMuscleGroupList.addView(tv);
        }
        addListItemsToLayout(llTipsList, detail.getTips(), "• ");
        addListItemsToLayout(llMistakesList, detail.getMistakes(), "• ");
        addListItemsToLayout(llHealthBenefitsList, detail.getBenefits(), "• ");
    }

    private void showContent(boolean showGuideline) {
        if (showGuideline) {
            layoutGuidelineContent.setVisibility(View.VISIBLE);
            layoutNoteContent.setVisibility(View.GONE);
            btnGuideline.setSelected(true);
            btnNote.setSelected(false);
            btnGuideline.setTextColor(getResources().getColor(R.color.black, null)); // Active color
            btnNote.setTextColor(getResources().getColor(R.color.white85, null));   // Inactive color
        } else {
            layoutGuidelineContent.setVisibility(View.GONE);
            layoutNoteContent.setVisibility(View.VISIBLE);
            btnGuideline.setSelected(false);
            btnNote.setSelected(true);
            btnGuideline.setTextColor(getResources().getColor(R.color.white85, null)); // Inactive color
            btnNote.setTextColor(getResources().getColor(R.color.black, null));       // Active color
        }
    }

    // Helper method to add text items to a LinearLayout
    private void addListItemsToLayout(LinearLayout parentLayout, List<String> items, String prefix) {
        parentLayout.removeAllViews(); // Clear previous items
        if (items != null && !items.isEmpty()) {
            for (String item : items) {
                TextView tv = new TextView(requireContext());
                tv.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                tv.setText(prefix + item);
                tv.setTextColor(getResources().getColor(R.color.white85, null));
                tv.setTextSize(16);
                parentLayout.addView(tv);
            }
        } else {
            TextView tv = new TextView(requireContext());
            tv.setText("N/A");
            tv.setTextColor(getResources().getColor(R.color.white85, null));
            parentLayout.addView(tv);
        }
    }

    // Helper method for indexed lists (steps)
    private void addIndexedListItemsToLayout(LinearLayout parentLayout, List<String> items) {
        parentLayout.removeAllViews();
        if (items != null && !items.isEmpty()) {
            for (int i = 0; i < items.size(); i++) {
                TextView tv = new TextView(requireContext());
                tv.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                tv.setText((i + 1) + ". " + items.get(i));
                tv.setTextColor(getResources().getColor(R.color.white85, null));
                tv.setTextSize(16);
                parentLayout.addView(tv);
            }
        } else {
            TextView tv = new TextView(requireContext());
            tv.setText("N/A");
            tv.setTextColor(getResources().getColor(R.color.white85, null));
            parentLayout.addView(tv);
        }
    }

    // Helper method for muscle groups (Main/Secondary tags)
    private void addMuscleGroupItemsToLayout(LinearLayout parentLayout, List<String> muscleGroups, String type,  int tagBackgroundRes, int tagTextColorRes) {
        // We don't remove all views here, as we're adding Main and Secondary into the same layout
        if (muscleGroups != null && !muscleGroups.isEmpty()) {
            // Create a horizontal LinearLayout for each row (e.g., "Main: muscle1, muscle2")
            LinearLayout muscleRow = new LinearLayout(requireContext());
            muscleRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            muscleRow.setOrientation(LinearLayout.HORIZONTAL);
            muscleRow.setGravity(Gravity.CENTER_VERTICAL); // Align items vertically in the center
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) muscleRow.getLayoutParams();
            params.setMargins(0, 0, 0, dpToPx(4)); // left, top, right, bottom
            muscleRow.setLayoutParams(params);
            // Add the type tag (Main/Secondary)
            TextView typeTv = new TextView(requireContext());
            LinearLayout.LayoutParams typeLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            typeLp.setMarginEnd(dpToPx(8)); // Margin after type tag
            typeTv.setLayoutParams(typeLp);
            typeTv.setText(type);
            typeTv.setTextColor(tagTextColorRes);
            typeTv.setBackgroundResource(tagBackgroundRes);
            typeTv.setTextSize(14);
            typeTv.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));
            muscleRow.addView(typeTv);

            // Create a TextView for comma-separated muscle names
            TextView muscleNamesTv = new TextView(requireContext());
            LinearLayout.LayoutParams namesLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, // Allow text to wrap if it's too long
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            muscleNamesTv.setLayoutParams(namesLp);

            // Join muscle names with a comma and space
            String names = String.join(", ", muscleGroups);
            muscleNamesTv.setText(names);
            muscleNamesTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.white85));
            muscleNamesTv.setTextSize(16);
            muscleNamesTv.setGravity(Gravity.START); // Ensure text is left-aligned

            muscleRow.addView(muscleNamesTv);

            parentLayout.addView(muscleRow);
        }
    }

    // Helper to convert dp to pixels
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    // region ExoPlayer Lifecycle Management
    private void initializePlayer(String videoUrl) {
        currentVideoUrl = videoUrl; // Save the video URL

        if (player == null) {
            player = new ExoPlayer.Builder(requireContext()).build();
            videoPlayerView.setPlayer(player);
            // Hide default play button from ExoPlayer if video is unavailable
            // or if you want custom logic
            videoPlayerView.setControllerShowTimeoutMs(3000); // Show controls for 3 seconds then hide

            player.addListener(new Player.Listener() {
                @Override
                public void onPlayerError(@NonNull com.google.android.exoplayer2.PlaybackException error) {
                    Player.Listener.super.onPlayerError(error);
                    Toast.makeText(getContext(), "Lỗi phát video: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "ExoPlayer Error: " + error.getMessage(), error);
                }
            });
        }

        if (currentVideoUrl != null && !currentVideoUrl.isEmpty()) {
            MediaItem mediaItem = MediaItem.fromUri(currentVideoUrl);
            player.setMediaItem(mediaItem);
            player.prepare();
            player.setPlayWhenReady(true); // Start playing automatically
        } else {
            Toast.makeText(getContext(), "Không có đường dẫn video để phát.", Toast.LENGTH_SHORT).show();
            videoPlayerView.setControllerAutoShow(false); // Hide controls if no video
        }
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (player != null) {
            player.setPlayWhenReady(true); // Resume playback
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (player != null) {
            player.setPlayWhenReady(true); // Resume playback
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (player != null) {
            player.setPlayWhenReady(false); // Pause playback when fragment is not in foreground
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (player != null) {
            player.setPlayWhenReady(false); // Pause playback
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        releasePlayer(); // Release the player when the view is destroyed
    }
}