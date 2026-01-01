package com.example.fitnessapp.fragment.community;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.fitnessapp.R;
import com.example.fitnessapp.databinding.FragmentCreateUpdatePostBinding;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.community.PostResponse;
import com.example.fitnessapp.repository.PostRepository;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.audio.AudioAttributes;

import java.io.File;
import java.io.Serializable;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateUpdatePostFragment extends Fragment {

    private static final String TAG = "CreateUpdatePostFrag";
    private static final String ARG_POST = "post";

    private FragmentCreateUpdatePostBinding binding;
    private PostRepository repository;
    private PostResponse existingPost;
    private Uri selectedImageUri;
    private Uri selectedVideoUri;
    private boolean isEditing;
    private ExoPlayer videoPreviewPlayer;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> videoPickerLauncher;

    public static CreateUpdatePostFragment newInstance(PostResponse post) {
        CreateUpdatePostFragment fragment = new CreateUpdatePostFragment();
        if (post != null) {
            Bundle args = new Bundle();
            args.putSerializable(ARG_POST, (Serializable) post);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new PostRepository(requireContext());

        if (getArguments() != null && getArguments().containsKey(ARG_POST)) {
            existingPost = (PostResponse) getArguments().getSerializable(ARG_POST);
            isEditing = existingPost != null;
        }

        // Register image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        displaySelectedMedia();
                    }
                }
        );

        // Register video picker launcher
        videoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedVideoUri = result.getData().getData();
                        displaySelectedMedia();
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateUpdatePostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hide header and bottom navigation
        hideHeaderAndBottomNavigation();

        setupToolbar();
        setupButtons();

        if (isEditing) {
            populateExistingPostData();
        }
    }

    private void hideHeaderAndBottomNavigation() {
        if (getActivity() != null) {
            View appBarLayout = getActivity().findViewById(R.id.app_bar_layout);
            View bottomNavigation = getActivity().findViewById(R.id.bottom_navigation);

            if (appBarLayout != null) {
                appBarLayout.setVisibility(View.GONE);
            }

            if (bottomNavigation != null) {
                bottomNavigation.setVisibility(View.GONE);
            }
        }
    }

    private void showHeaderAndBottomNavigation() {
        if (getActivity() != null) {
            View appBarLayout = getActivity().findViewById(R.id.app_bar_layout);
            View bottomNavigation = getActivity().findViewById(R.id.bottom_navigation);

            if (appBarLayout != null) {
                appBarLayout.setVisibility(View.VISIBLE);
            }

            if (bottomNavigation != null) {
                bottomNavigation.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupToolbar() {
        int titleResId = isEditing ? R.string.community_edit_post_title : R.string.community_create_post_title;
        binding.tvTitle.setText(titleResId);

        // Back button click listener
        binding.ibBack.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });
    }

    private void setupButtons() {
        // Image picker button
        binding.btnPickImage.setOnClickListener(v -> pickImage());

        // Video picker button
        binding.btnPickVideo.setOnClickListener(v -> pickVideo());

        // Remove image button
        binding.btnRemoveImage.setOnClickListener(v -> removeSelectedImage());

        // Remove video button
        binding.btnRemoveVideo.setOnClickListener(v -> removeSelectedVideo());

        // Submit button
        binding.btnSubmit.setOnClickListener(v -> {
            String content = binding.etContent.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(requireContext(), R.string.community_validation_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEditing) {
                updatePost(content);
            } else {
                createPost(content);
            }
        });

        // Cancel button
        binding.btnCancel.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void pickVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.setType("video/*");
        videoPickerLauncher.launch(intent);
    }

    private void displaySelectedMedia() {
        // Display image if selected or if exists in editing mode
        if (selectedImageUri != null) {
            binding.llImagePreview.setVisibility(View.VISIBLE);
            binding.btnRemoveImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(selectedImageUri)
                    .into(binding.ivImagePreview);
        } else if (isEditing && existingPost != null &&
                   existingPost.getImageUrl() != null && !existingPost.getImageUrl().isEmpty()) {
            // Show existing image from server when editing
            binding.llImagePreview.setVisibility(View.VISIBLE);
            binding.btnRemoveImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(existingPost.getImageUrl())
                    .into(binding.ivImagePreview);
        } else {
            binding.llImagePreview.setVisibility(View.GONE);
            binding.btnRemoveImage.setVisibility(View.GONE);
        }

        // Display video if selected or if exists in editing mode
        if (selectedVideoUri != null) {
            binding.llVideoPreview.setVisibility(View.VISIBLE);
            binding.btnRemoveVideo.setVisibility(View.VISIBLE);
            setupVideoPreview(selectedVideoUri);
        } else if (isEditing && existingPost != null &&
                   existingPost.getVideoUrl() != null && !existingPost.getVideoUrl().isEmpty()) {
            // Show existing video from server when editing
            binding.llVideoPreview.setVisibility(View.VISIBLE);
            binding.btnRemoveVideo.setVisibility(View.VISIBLE);
            setupVideoPreview(Uri.parse(existingPost.getVideoUrl()));
        } else {
            binding.llVideoPreview.setVisibility(View.GONE);
            binding.btnRemoveVideo.setVisibility(View.GONE);
            releaseVideoPreview();
        }
    }

    private void removeSelectedImage() {
        selectedImageUri = null;
        binding.llImagePreview.setVisibility(View.GONE);
        binding.btnRemoveImage.setVisibility(View.GONE);
    }

    private void removeSelectedVideo() {
        selectedVideoUri = null;
        binding.llVideoPreview.setVisibility(View.GONE);
        binding.btnRemoveVideo.setVisibility(View.GONE);
        releaseVideoPreview();
    }

    private void populateExistingPostData() {
        if (existingPost != null) {
            binding.etContent.setText(existingPost.getContent());
            binding.btnSubmit.setText(R.string.community_update_button);

            // Display existing media using displaySelectedMedia()
            // This ensures consistency with the media display logic
            displaySelectedMedia();
        }
    }

    /**
     * Setup ExoPlayer for video preview
     * Uses main thread to prevent dead thread issues
     */
    private void setupVideoPreview(Uri videoUri) {
        if (!isAdded() || binding == null) {
            Log.w(TAG, "Fragment not attached, skipping video setup");
            return;
        }

        Log.d(TAG, "Setting up video preview for: " + videoUri);

        try {
            // Release any existing player first
            releaseVideoPreview();

            // Create audio attributes for proper video playback with audio
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(com.google.android.exoplayer2.C.AUDIO_CONTENT_TYPE_MOVIE)
                    .setUsage(com.google.android.exoplayer2.C.USAGE_MEDIA)
                    .build();

            // Create new ExoPlayer on main thread with audio attributes
            videoPreviewPlayer = new ExoPlayer.Builder(requireContext())
                    .setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true)
                    .build();
            binding.playerViewPreview.setPlayer(videoPreviewPlayer);

            // Setup media item
            MediaItem mediaItem = MediaItem.fromUri(videoUri);
            videoPreviewPlayer.setMediaItem(mediaItem);
            videoPreviewPlayer.prepare();
            videoPreviewPlayer.setPlayWhenReady(false); // Don't auto-play

            // Add error listener
            videoPreviewPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlayerError(PlaybackException error) {
                    Log.e(TAG, "Video preview error: " + error.getMessage());
                    if (isAdded()) {
                        Toast.makeText(requireContext(), R.string.error_upload, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            Log.d(TAG, "Video preview setup complete");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up video preview", e);
            if (isAdded()) {
                Toast.makeText(requireContext(), "Error loading video preview", Toast.LENGTH_SHORT).show();
            }
            binding.llVideoPreview.setVisibility(View.GONE);
        }
    }

    /**
     * Release video preview player
     * Runs on main thread to prevent dead thread issues
     */
    private void releaseVideoPreview() {
        if (videoPreviewPlayer != null) {
            try {
                Log.d(TAG, "Releasing video preview player");
                videoPreviewPlayer.stop();
                videoPreviewPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing video preview player", e);
            } finally {
                videoPreviewPlayer = null;
            }
        }
    }

    private void createPost(String content) {
        Log.d(TAG, "Creating post with content: " + content);
        binding.btnSubmit.setEnabled(false);

        File imageFile = null;
        File videoFile = null;

        // Prepare image if selected
        if (selectedImageUri != null) {
            try {
                imageFile = getFileFromUri(selectedImageUri);
                Log.d(TAG, "Image file prepared: " + imageFile.getPath());
            } catch (Exception e) {
                Log.e(TAG, "Error preparing image file", e);
                Toast.makeText(requireContext(), R.string.error_upload, Toast.LENGTH_SHORT).show();
                binding.btnSubmit.setEnabled(true);
                return;
            }
        }

        // Prepare video if selected
        if (selectedVideoUri != null) {
            try {
                videoFile = getFileFromUri(selectedVideoUri);
                Log.d(TAG, "Video file prepared: " + videoFile.getPath());
            } catch (Exception e) {
                Log.e(TAG, "Error preparing video file", e);
                Toast.makeText(requireContext(), R.string.error_upload, Toast.LENGTH_SHORT).show();
                binding.btnSubmit.setEnabled(true);
                return;
            }
        }

        // Show uploading notification and go back immediately
        Toast.makeText(requireContext(), R.string.community_loading, Toast.LENGTH_SHORT).show();
        requireActivity().onBackPressed();

        repository.createPost(content, imageFile, videoFile, new Callback<ApiResponse<PostResponse>>() {



            @Override
            public void onResponse(Call<ApiResponse<PostResponse>> call,
                                   Response<ApiResponse<PostResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Log.d(TAG, "Post created successfully");
                    // Show notification on main thread
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), R.string.success_post_created, Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    Log.e(TAG, "Failed to create post");
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), R.string.error_post_failed, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PostResponse>> call, Throwable t) {
                Log.e(TAG, "Error creating post: " + t.getMessage());
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void updatePost(String content) {
        if (existingPost == null) return;

        Log.d(TAG, "Updating post: " + existingPost.getId());
        binding.btnSubmit.setEnabled(false);

        File imageFile = null;
        File videoFile = null;

        // Prepare image if selected
        if (selectedImageUri != null) {
            try {
                imageFile = getFileFromUri(selectedImageUri);
                Log.d(TAG, "Image file prepared: " + imageFile.getPath());
            } catch (Exception e) {
                Log.e(TAG, "Error preparing image file", e);
                Toast.makeText(requireContext(), R.string.error_upload, Toast.LENGTH_SHORT).show();
                binding.btnSubmit.setEnabled(true);
                return;
            }
        }

        // Prepare video if selected
        if (selectedVideoUri != null) {
            try {
                videoFile = getFileFromUri(selectedVideoUri);
                Log.d(TAG, "Video file prepared: " + videoFile.getPath());
            } catch (Exception e) {
                Log.e(TAG, "Error preparing video file", e);
                Toast.makeText(requireContext(), R.string.error_upload, Toast.LENGTH_SHORT).show();
                binding.btnSubmit.setEnabled(true);
                return;
            }
        }

        // Show uploading notification and go back immediately
        Toast.makeText(requireContext(), R.string.community_loading, Toast.LENGTH_SHORT).show();
        requireActivity().onBackPressed();

        repository.updatePost(existingPost.getId(), content, imageFile, videoFile,
                new Callback<ApiResponse<PostResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PostResponse>> call,
                                           Response<ApiResponse<PostResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            Log.d(TAG, "Post updated successfully");
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(), R.string.success_post_updated, Toast.LENGTH_SHORT).show();
                                });
                            }
                        } else {
                            Log.e(TAG, "Failed to update post");
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(), R.string.error_update_failed, Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<PostResponse>> call, Throwable t) {
                        Log.e(TAG, "Error updating post: " + t.getMessage());
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                });
    }

    private File getFileFromUri(Uri uri) throws Exception {
        // Get content resolver
        android.content.ContentResolver contentResolver = requireContext().getContentResolver();

        // Create a temporary file
        String fileName = "temp_" + System.currentTimeMillis();
        String mimeType = contentResolver.getType(uri);

        // Determine file extension
        String extension = "";
        if (mimeType != null) {
            if (mimeType.startsWith("image/")) {
                extension = ".jpg";
            } else if (mimeType.startsWith("video/")) {
                extension = ".mp4";
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

    @Override
    public void onPause() {
        super.onPause();
        if (videoPreviewPlayer != null) {
            videoPreviewPlayer.pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Show header and bottom navigation when leaving this fragment
        showHeaderAndBottomNavigation();

        releaseVideoPreview();
        binding = null;
    }
}
