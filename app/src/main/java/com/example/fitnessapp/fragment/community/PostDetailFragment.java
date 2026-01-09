package com.example.fitnessapp.fragment.community;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

import com.bumptech.glide.Glide;
import com.example.fitnessapp.R;
import com.example.fitnessapp.adapter.community.CommentAdapter;
import com.example.fitnessapp.databinding.FragmentPostDetailBinding;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.community.CommentResponse;
import com.example.fitnessapp.model.response.community.PostResponse;
import com.example.fitnessapp.repository.CommentRepository;
import com.example.fitnessapp.repository.PostRepository;
import com.example.fitnessapp.session.SessionManager;
import com.example.fitnessapp.util.TimeUtil;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostDetailFragment extends Fragment {

    private static final String TAG = "PostDetailFragment";
    private static final String ARG_POST_ID = "post_id";
    private static final int COMMENTS_PAGE_SIZE = 10;

    private FragmentPostDetailBinding binding;
    private PostRepository repository;
    private CommentRepository commentRepository;
    private CommentAdapter commentAdapter;
    private ExoPlayer player;
    private long postId;
    private PostResponse currentPost;

    // Comment pagination
    private int currentCommentsPage = 0;
    private boolean isLoadingComments = false;
    private boolean hasMoreComments = true;
    private LinearLayoutManager commentsLayoutManager;

    // Image picker for comments
    private ActivityResultLauncher<Intent> commentImagePickerLauncher;
    private Uri selectedCommentImageUri;

    // Edit comment dialog variables
    private ActivityResultLauncher<Intent> editCommentImagePickerLauncher;
    private Uri selectedEditCommentImageUri;
    private boolean shouldRemoveEditCommentImage = false;
    private Runnable editCommentImageUpdateCallback;

    public static PostDetailFragment newInstance(long postId) {
        PostDetailFragment fragment = new PostDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_POST_ID, postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            postId = getArguments().getLong(ARG_POST_ID);
        }
        repository = new PostRepository(requireContext());
        commentRepository = new CommentRepository(requireContext());

        // Register comment image picker launcher
        commentImagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedCommentImageUri = result.getData().getData();
                        displayCommentImagePreview();
                    }
                }
        );

        // Register edit comment image picker launcher
        editCommentImagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedEditCommentImageUri = result.getData().getData();
                        shouldRemoveEditCommentImage = false; // Reset deletion flag when new image selected
                        // Call the callback to update the image preview in the dialog
                        // Only if fragment is still added and callback is set
                        if (isAdded() && editCommentImageUpdateCallback != null) {
                            editCommentImageUpdateCallback.run();
                        }
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPostDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Header and bottom nav visibility is controlled by MainActivity's FragmentLifecycleCallbacks

        setupToolbar();
        setupRecyclerView();
        setupListeners();
        loadUserAvatar();

        // Load post detail
        loadPostDetail();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        // Add refresh menu item
        binding.toolbar.inflateMenu(R.menu.menu_post_detail);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_refresh) {
                // Reload post data
                loadPostDetail();
                Toast.makeText(requireContext(), "Refreshing post...", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        commentsLayoutManager = new LinearLayoutManager(requireContext());
        binding.rvComments.setLayoutManager(commentsLayoutManager);
        commentAdapter = new CommentAdapter(requireContext());
        commentAdapter.setItemListener(new CommentAdapter.CommentItemListener() {
            @Override
            public void onEditClick(View view, int position) {
                showEditCommentDialog(position);
            }

            @Override
            public void onDeleteClick(View view, int position) {
                showDeleteCommentConfirmation(position);
            }

            @Override
            public void onLikeClick(View view, int position) {
                handleCommentLikeUnlike(position);
            }

            @Override
            public void onImageClick(String imageUrl) {
                showFullImageDialog(imageUrl);
            }
        });
        binding.rvComments.setAdapter(commentAdapter);

        // Add scroll listener for pagination
        binding.rvComments.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = commentsLayoutManager.getChildCount();
                int totalItemCount = commentsLayoutManager.getItemCount();
                int firstVisibleItemPosition = commentsLayoutManager.findFirstVisibleItemPosition();
                int lastVisibleItemPosition = firstVisibleItemPosition + visibleItemCount - 1;

                Log.d(TAG, "Scroll event - dy: " + dy +
                      ", visible: " + visibleItemCount +
                      ", total: " + totalItemCount +
                      ", firstVisible: " + firstVisibleItemPosition +
                      ", lastVisible: " + lastVisibleItemPosition +
                      ", isLoading: " + isLoadingComments +
                      ", hasMore: " + hasMoreComments);

                // Load more when reaching bottom (reduced threshold to 2 items)
                if (!isLoadingComments && hasMoreComments) {
                    // Trigger when last visible item is within 2 items of the end
                    if (lastVisibleItemPosition >= totalItemCount - 3) {
                        Log.d(TAG, "Near bottom, triggering loadMoreComments()");
                        loadMoreComments();
                    }
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.d(TAG, "Scroll state changed: " + newState);
            }
        });

        Log.d(TAG, "RecyclerView scroll listener registered");
    }

    private void loadUserAvatar() {
        SessionManager sessionManager = SessionManager.getInstance(requireContext());

        // Load user avatar
        String avatarUrl = sessionManager.getAvatar();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.img_user_default_128)
                    .error(R.drawable.img_user_default_128)
                    .into(binding.ivCurrentUserAvatar);
        } else {
            binding.ivCurrentUserAvatar.setImageResource(R.drawable.img_user_default_128);
        }
    }

    private void setupListeners() {
        // Like button
        binding.ibLike.setOnClickListener(v -> {
            if (currentPost != null) {
                handleLikeUnlike();
            }
        });

        // Send comment button
        binding.ibSendComment.setOnClickListener(v -> {
            String commentText = binding.etComment.getText().toString().trim();
            if (!commentText.isEmpty()) {
                sendComment(commentText);
            }
        });

        // Attach image button for comment
        binding.ibAttachImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            commentImagePickerLauncher.launch(intent);
        });

        // Remove comment image button
        binding.ibRemoveCommentImage.setOnClickListener(v -> {
            selectedCommentImageUri = null;
            binding.llCommentImagePreview.setVisibility(View.GONE);
        });
    }

    private void loadPostDetail() {
        Log.d(TAG, "Loading post detail for postId: " + postId);
        binding.progressBar.setVisibility(View.VISIBLE);

        repository.getPostDetail(postId, new Callback<ApiResponse<PostResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<PostResponse>> call,
                                   Response<ApiResponse<PostResponse>> response) {
                // Check if fragment is still added before updating UI
                if (!isAdded() || binding == null) {
                    Log.w(TAG, "Fragment not added or binding is null, skipping post detail UI update");
                    return;
                }

                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    currentPost = response.body().getData();
                    Log.d(TAG, "Post detail loaded successfully");
                    displayPostDetail(currentPost);
                } else {
                    Log.e(TAG, "Failed to load post detail");
                    Toast.makeText(requireContext(), "Failed to load post", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PostResponse>> call, Throwable t) {
                // Check if fragment is still added before updating UI
                if (!isAdded() || binding == null) {
                    Log.w(TAG, "Fragment not added or binding is null, skipping error UI update");
                    return;
                }

                binding.progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading post detail: " + t.getMessage());
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
            }
        });
    }

   
   
   

    private void displayPostDetail(PostResponse post) {
        if (post == null) return;

        // User info
        binding.tvUserName.setText(post.getUserName() != null ? post.getUserName() : "Unknown");

        // Load user avatar
        if (post.getUserAvatarUrl() != null && !post.getUserAvatarUrl().isEmpty()) {
            Glide.with(this)
                    .load(post.getUserAvatarUrl())
                    .placeholder(R.drawable.img_user_default_128)
                    .error(R.drawable.img_user_default_128)
                    .into(binding.ivUserAvatar);
        }

        // Post date - Use TimeUtils for localized time
        binding.tvPostDate.setText(TimeUtil.getTime(
                requireContext(), post.getCreateAt()));

        // Content
        binding.tvContent.setText(post.getContent());

        // Display image if available
        boolean hasImage = post.getImageUrl() != null && !post.getImageUrl().isEmpty();
        if (hasImage) {
            binding.cardImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(post.getImageUrl())
                    .placeholder(R.color.gray_450)
                    .into(binding.ivPostImage);

            // Add click listener to view full image
            binding.ivPostImage.setOnClickListener(v -> {
                showFullImageDialog(post.getImageUrl());
            });
        } else {
            binding.cardImage.setVisibility(View.GONE);
        }

        // Display video if available
        boolean hasVideo = post.getVideoUrl() != null && !post.getVideoUrl().isEmpty();
        if (hasVideo) {
            binding.cardVideo.setVisibility(View.VISIBLE);
            setupVideoPlayer(post.getVideoUrl());
        } else {
            binding.cardVideo.setVisibility(View.GONE);
        }

        // Like count and state
        updateLikeUI(post);

        // Comment count
        binding.tvCommentCount.setText(String.valueOf(post.getCommentCount()));

        // Edit and Delete buttons visibility (only show for own posts)
        boolean canEdit = post.getCanEdit() != null && post.getCanEdit();
        boolean canDelete = post.getCanDelete() != null && post.getCanDelete();

        binding.ibEdit.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        binding.ibDelete.setVisibility(canDelete ? View.VISIBLE : View.GONE);

        if (canEdit) {
            binding.ibEdit.setOnClickListener(v -> navigateToEditPost());
        }

        if (canDelete) {
            binding.ibDelete.setOnClickListener(v -> showDeleteConfirmation());
        }

        // Load comments
        loadComments();
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "";
        }

        try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
            java.util.Date date = inputFormat.parse(dateString);

            if (date == null) return dateString;

            long timeInMillis = date.getTime();
            long now = System.currentTimeMillis();
            long diff = now - timeInMillis;

            long minutes = diff / (60 * 1000);
            long hours = diff / (60 * 60 * 1000);
            long days = diff / (24 * 60 * 60 * 1000);

            if (minutes < 1) {
                return "Just now";
            } else if (minutes < 60) {
                return minutes + " min ago";
            } else if (hours < 24) {
                return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
            } else if (days < 7) {
                return days + " day" + (days > 1 ? "s" : "") + " ago";
            } else {
                java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
                return outputFormat.format(date);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date: " + e.getMessage());
            return dateString;
        }
    }

    private void setupVideoPlayer(String videoUrl) {
        Log.d(TAG, "Setting up video player for URL: " + videoUrl);

        try {
            // Create audio attributes for proper video playback with audio
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(com.google.android.exoplayer2.C.AUDIO_CONTENT_TYPE_MOVIE)
                    .setUsage(com.google.android.exoplayer2.C.USAGE_MEDIA)
                    .build();

            // Initialize player with audio attributes
            player = new ExoPlayer.Builder(requireContext())
                    .setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true)
                    .build();
            binding.playerView.setPlayer(player);

            // Create data source factory
            DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(requireContext());

            // Create media source
            MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem);

            // Prepare player
            player.setMediaSource(mediaSource);
            player.prepare();
            player.setPlayWhenReady(false); // Don't auto-play

            // Add listener for errors
            player.addListener(new Player.Listener() {
                @Override
                public void onPlayerError(PlaybackException error) {
                    Log.e(TAG, "ExoPlayer error: " + error.getMessage());
                    Toast.makeText(requireContext(), "Video playback error", Toast.LENGTH_SHORT).show();
                }
            });

            Log.d(TAG, "Video player setup complete");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up video player", e);
            binding.cardVideo.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Failed to load video", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleLikeUnlike() {
        boolean isLiked = currentPost.getLiked();

        // Optimistic UI update
        currentPost.setLiked(!isLiked);
        long newLikeCount = currentPost.getLikeCount() + (isLiked ? -1 : 1);
        currentPost.setLikeCount(newLikeCount);
        updateLikeUI(currentPost);

        // Make API call
        Callback<ApiResponse<PostResponse>> callback = new Callback<ApiResponse<PostResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<PostResponse>> call,
                                   Response<ApiResponse<PostResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    PostResponse updatedPost = response.body().getData();
                    currentPost.setLiked(updatedPost.getLiked());
                    currentPost.setLikeCount(updatedPost.getLikeCount());
                    updateLikeUI(currentPost);
                    Log.d(TAG, "Like/unlike successful");
                } else {
                    // Revert on error
                    currentPost.setLiked(isLiked);
                    currentPost.setLikeCount(currentPost.getLikeCount() + (isLiked ? 1 : -1));
                    updateLikeUI(currentPost);
                    Toast.makeText(requireContext(), "Failed to update like", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PostResponse>> call, Throwable t) {
                // Revert on error
                currentPost.setLiked(isLiked);
                currentPost.setLikeCount(currentPost.getLikeCount() + (isLiked ? 1 : -1));
                updateLikeUI(currentPost);
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        };

        if (isLiked) {
            repository.unlikePost(currentPost.getId(), callback);
        } else {
            repository.likePost(currentPost.getId(), callback);
        }
    }

    private void updateLikeUI(PostResponse post) {
        binding.tvLikeCount.setText(String.valueOf(post.getLikeCount()));

        if (post.getLiked()) {
            binding.ibLike.setImageResource(R.drawable.ic_like_filled_24);
            binding.ibLike.setColorFilter(requireContext()
                    .getApplicationContext()
                    .getColor(R.color.yellow));
        } else {
            binding.ibLike.setImageResource(R.drawable.ic_like_outline_24);
            binding.ibLike.setColorFilter(requireContext()
                    .getApplicationContext()
                    .getColor(R.color.white));
        }
    }

    private void loadComments() {
        Log.d(TAG, "Loading comments for post: " + postId);

        // Reset pagination state
        currentCommentsPage = 0;
        hasMoreComments = true;

        commentRepository.getComments(postId, currentCommentsPage, COMMENTS_PAGE_SIZE, new Callback<ApiResponse<java.util.List<CommentResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<java.util.List<CommentResponse>>> call,
                                   Response<ApiResponse<java.util.List<CommentResponse>>> response) {
                // Check if fragment is still added before updating UI
                if (!isAdded() || binding == null) {
                    Log.w(TAG, "Fragment not added or binding is null, skipping comment UI update");
                    return;
                }

                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    java.util.List<CommentResponse> comments = response.body().getData();
                    Log.d(TAG, "Comments loaded: " + (comments != null ? comments.size() : 0));

                    if (comments != null && !comments.isEmpty()) {
                        commentAdapter.setComments(comments);
                        binding.rvComments.setVisibility(View.VISIBLE);
                        binding.llEmptyComments.setVisibility(View.GONE);

                        // Check pagination metadata for hasMore
                        if (response.body().getMeta() != null) {
                            hasMoreComments = response.body().getMeta().isHasMore();
                            Log.d(TAG, "Pagination metadata - hasMore: " + hasMoreComments +
                                  ", page: " + response.body().getMeta().getPage() +
                                  ", total: " + response.body().getMeta().getTotal());
                        } else {
                            // Fallback: check if returned less than page size
                            hasMoreComments = comments.size() >= COMMENTS_PAGE_SIZE;
                            Log.d(TAG, "No pagination metadata, using fallback - hasMore: " + hasMoreComments);
                        }
                    } else {
                        binding.llEmptyComments.setVisibility(View.VISIBLE);
                        binding.rvComments.setVisibility(View.GONE);
                        hasMoreComments = false;
                    }
                } else {
                    Log.e(TAG, "Failed to load comments");
                    binding.llEmptyComments.setVisibility(View.VISIBLE);
                    binding.rvComments.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<java.util.List<CommentResponse>>> call, Throwable t) {
                Log.e(TAG, "Error loading comments: " + t.getMessage());

                // Check if fragment is still added before updating UI
                if (!isAdded() || binding == null) {
                    Log.w(TAG, "Fragment not added or binding is null, skipping error UI update");
                    return;
                }

                binding.llEmptyComments.setVisibility(View.VISIBLE);
                binding.rvComments.setVisibility(View.GONE);
            }
        });
    }

    private void loadMoreComments() {
        if (isLoadingComments || !hasMoreComments) {
            return;
        }

        Log.d(TAG, "Loading more comments, page: " + (currentCommentsPage + 1));
        isLoadingComments = true;
        currentCommentsPage++;

        commentRepository.getComments(postId, currentCommentsPage, COMMENTS_PAGE_SIZE, new Callback<ApiResponse<java.util.List<CommentResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<java.util.List<CommentResponse>>> call,
                                   Response<ApiResponse<java.util.List<CommentResponse>>> response) {
                // Check if fragment is still added before updating UI
                if (!isAdded() || binding == null) {
                    Log.w(TAG, "Fragment not added or binding is null, skipping more comments UI update");
                    isLoadingComments = false;
                    return;
                }

                isLoadingComments = false;

                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    java.util.List<CommentResponse> newComments = response.body().getData();
                    Log.d(TAG, "More comments loaded: " + (newComments != null ? newComments.size() : 0));

                    if (newComments != null && !newComments.isEmpty()) {
                        // Append new comments to adapter
                        commentAdapter.addComments(newComments);

                        // Check pagination metadata for hasMore
                        if (response.body().getMeta() != null) {
                            hasMoreComments = response.body().getMeta().isHasMore();
                            Log.d(TAG, "Pagination metadata - hasMore: " + hasMoreComments +
                                  ", page: " + response.body().getMeta().getPage() +
                                  ", total: " + response.body().getMeta().getTotal());
                        } else {
                            // Fallback: check if returned less than page size
                            hasMoreComments = newComments.size() >= COMMENTS_PAGE_SIZE;
                            Log.d(TAG, "No pagination metadata, using fallback - hasMore: " + hasMoreComments);
                        }
                    } else {
                        hasMoreComments = false;
                        Log.d(TAG, "No more comments available");
                    }
                } else {
                    Log.e(TAG, "Failed to load more comments");
                    // Revert page increment on error
                    currentCommentsPage--;
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<java.util.List<CommentResponse>>> call, Throwable t) {
                Log.e(TAG, "Error loading more comments: " + t.getMessage());

                // Check if fragment is still added before updating UI
                if (!isAdded() || binding == null) {
                    Log.w(TAG, "Fragment not added or binding is null, skipping error UI update");
                    isLoadingComments = false;
                    return;
                }

                isLoadingComments = false;
                // Revert page increment on error
                currentCommentsPage--;
            }
        });
    }

    private void sendComment(String content) {
        Log.d(TAG, "Sending comment: " + content);
        binding.ibSendComment.setEnabled(false);

        // Get image file if selected
        File imageFile = null;
        if (selectedCommentImageUri != null) {
            try {
                imageFile = getFileFromUri(selectedCommentImageUri);
            } catch (Exception e) {
                Log.e(TAG, "Error getting file from URI: " + e.getMessage());
                Toast.makeText(requireContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
                binding.ibSendComment.setEnabled(true);
                return;
            }
        }

        // Clear input field and image preview immediately
        String savedContent = content;
        binding.etComment.setText("");
        selectedCommentImageUri = null;
        binding.llCommentImagePreview.setVisibility(View.GONE);

        // Show uploading toast and re-enable button
        Toast.makeText(requireContext(), "Uploading comment in background...", Toast.LENGTH_SHORT).show();
        binding.ibSendComment.setEnabled(true);

        commentRepository.createComment(postId, savedContent, imageFile, new Callback<ApiResponse<CommentResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<CommentResponse>> call,
                                   Response<ApiResponse<CommentResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    CommentResponse newComment = response.body().getData();
                    Log.d(TAG, "Comment created successfully");

                    // Run on UI thread to update UI
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            // Add comment to adapter
                            commentAdapter.addComment(newComment);

                            // Show RecyclerView and hide empty state
                            binding.rvComments.setVisibility(View.VISIBLE);
                            binding.llEmptyComments.setVisibility(View.GONE);

                            // Update comment count
                            if (currentPost != null) {
                                currentPost.setCommentCount(currentPost.getCommentCount() + 1);
                                binding.tvCommentCount.setText(String.valueOf(currentPost.getCommentCount()));
                            }

                            Toast.makeText(requireContext(), "Comment added successfully", Toast.LENGTH_SHORT).show();

                            // Scroll to top to show new comment
                            binding.rvComments.smoothScrollToPosition(0);
                        });
                    }
                } else {
                    Log.e(TAG, "Failed to create comment");
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Failed to post comment", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CommentResponse>> call, Throwable t) {
                Log.e(TAG, "Error creating comment: " + t.getMessage());
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void handleCommentLikeUnlike(int position) {
        CommentResponse comment = commentAdapter.getItem(position);
        if (comment == null) return;

        boolean isLiked = comment.getLiked() != null && comment.getLiked();

        if (isLiked) {
            // Unlike the comment
            commentRepository.unlikeComment(comment.getId(), new Callback<ApiResponse<CommentResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<CommentResponse>> call,
                                       Response<ApiResponse<CommentResponse>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                        CommentResponse updatedComment = response.body().getData();
                        commentAdapter.updateComment(position, updatedComment);
                        Log.d(TAG, "Comment unliked successfully");
                    } else {
                        Log.e(TAG, "Failed to unlike comment");
                        Toast.makeText(requireContext(), "Failed to unlike comment", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<CommentResponse>> call, Throwable t) {
                    Log.e(TAG, "Error unliking comment: " + t.getMessage());
                    Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Like the comment
            commentRepository.likeComment(comment.getId(), new Callback<ApiResponse<CommentResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<CommentResponse>> call,
                                       Response<ApiResponse<CommentResponse>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                        CommentResponse updatedComment = response.body().getData();
                        commentAdapter.updateComment(position, updatedComment);
                        Log.d(TAG, "Comment liked successfully");
                    } else {
                        Log.e(TAG, "Failed to like comment");
                        Toast.makeText(requireContext(), "Failed to like comment", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<CommentResponse>> call, Throwable t) {
                    Log.e(TAG, "Error liking comment: " + t.getMessage());
                    Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showDeleteCommentConfirmation(int position) {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.community_delete_comment_title)
                .setMessage(R.string.community_confirm_delete_comment)
                .setPositiveButton(R.string.community_delete, (dialog, which) -> {
                    deleteComment(position);
                })
                .setNegativeButton(R.string.community_cancel, null)
                .show();
    }

    private void showEditCommentDialog(int position) {
        CommentResponse comment = commentAdapter.getItem(position);
        if (comment == null) return;

        // Create dialog
        android.app.Dialog dialog = new android.app.Dialog(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_comment, null);
        dialog.setContentView(dialogView);

        // Make dialog wider
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.95),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }

        // Get dialog views
        com.google.android.material.textfield.TextInputEditText etCommentContent =
                dialogView.findViewById(R.id.et_comment_content);
        androidx.cardview.widget.CardView cvCurrentImage = dialogView.findViewById(R.id.cv_current_image);
        android.widget.ImageView ivCurrentImage = dialogView.findViewById(R.id.iv_current_image);
        android.widget.TextView tvImageSectionTitle = dialogView.findViewById(R.id.tv_image_section_title);
        android.widget.LinearLayout llImageActions = dialogView.findViewById(R.id.ll_image_actions);
        com.google.android.material.button.MaterialButton btnChangeImage =
                dialogView.findViewById(R.id.btn_change_image);
        com.google.android.material.button.MaterialButton btnRemoveImage =
                dialogView.findViewById(R.id.btn_remove_image);
        com.google.android.material.button.MaterialButton btnAddImage =
                dialogView.findViewById(R.id.btn_add_image);
        com.google.android.material.button.MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        com.google.android.material.button.MaterialButton btnSave = dialogView.findViewById(R.id.btn_save);

        // Pre-fill comment content
        etCommentContent.setText(comment.getContent());

        // Reset edit image state
        selectedEditCommentImageUri = null;
        shouldRemoveEditCommentImage = false;

        // Function to update image preview
        Runnable updateImagePreview = () -> {
            // Safety check - ensure fragment is still added and views are valid
            if (!isAdded() || getContext() == null) {
                return;
            }

            if (selectedEditCommentImageUri != null) {
                // Show new selected image
                tvImageSectionTitle.setVisibility(View.VISIBLE);
                cvCurrentImage.setVisibility(View.VISIBLE);
                llImageActions.setVisibility(View.VISIBLE);
                btnAddImage.setVisibility(View.GONE);

                Glide.with(this)
                        .load(selectedEditCommentImageUri)
                        .placeholder(R.color.gray_450)
                        .into(ivCurrentImage);
            } else if (!shouldRemoveEditCommentImage && comment.getImageUrl() != null && !comment.getImageUrl().isEmpty()) {
                // Show existing image from server
                tvImageSectionTitle.setVisibility(View.VISIBLE);
                cvCurrentImage.setVisibility(View.VISIBLE);
                llImageActions.setVisibility(View.VISIBLE);
                btnAddImage.setVisibility(View.GONE);

                Glide.with(this)
                        .load(comment.getImageUrl())
                        .placeholder(R.color.gray_450)
                        .into(ivCurrentImage);
            } else {
                // No image
                tvImageSectionTitle.setVisibility(View.GONE);
                cvCurrentImage.setVisibility(View.GONE);
                llImageActions.setVisibility(View.GONE);
                btnAddImage.setVisibility(View.VISIBLE);
            }
        };

        // Set callback for image picker result
        editCommentImageUpdateCallback = updateImagePreview;

        // Initial image display
        updateImagePreview.run();

        // Change image button - pick new image
        btnChangeImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            editCommentImagePickerLauncher.launch(intent);
        });

        // Remove image button - mark for removal
        btnRemoveImage.setOnClickListener(v -> {
            shouldRemoveEditCommentImage = true;
            selectedEditCommentImageUri = null;
            updateImagePreview.run();
        });

        // Add image button - pick new image
        btnAddImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            editCommentImagePickerLauncher.launch(intent);
        });

        // Cancel button
        btnCancel.setOnClickListener(v -> {
            // Clear callback when dialog is dismissed
            editCommentImageUpdateCallback = null;
            dialog.dismiss();
        });

        // Save button
        btnSave.setOnClickListener(v -> {
            String newContent = etCommentContent.getText().toString().trim();
            if (newContent.isEmpty()) {
                Toast.makeText(requireContext(), R.string.community_validation_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            // Update comment
            updateComment(position, comment.getId(), newContent);

            // Clear callback when dialog is dismissed
            editCommentImageUpdateCallback = null;
            dialog.dismiss();
        });

        // Clear callback when dialog is dismissed by other means (back button, outside tap)
        dialog.setOnDismissListener(d -> {
            editCommentImageUpdateCallback = null;
        });

        // Show dialog
        dialog.show();
    }

    private void deleteComment(int position) {
        CommentResponse comment = commentAdapter.getItem(position);
        if (comment == null) return;

        commentRepository.deleteComment(comment.getId(), new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call,
                                   Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Log.d(TAG, "Comment deleted successfully");

                    // Remove comment from adapter
                    commentAdapter.removeComment(position);

                    // Update comment count
                    if (currentPost != null) {
                        currentPost.setCommentCount(currentPost.getCommentCount() - 1);
                        binding.tvCommentCount.setText(String.valueOf(currentPost.getCommentCount()));
                    }

                    // Show empty state if no comments left
                    if (commentAdapter.getItemCount() == 0) {
                        binding.llEmptyComments.setVisibility(View.VISIBLE);
                        binding.rvComments.setVisibility(View.GONE);
                    }

                    Toast.makeText(requireContext(), "Comment deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Failed to delete comment");
                    Toast.makeText(requireContext(), "Failed to delete comment", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Log.e(TAG, "Error deleting comment: " + t.getMessage());
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateComment(int position, long commentId, String newContent) {
        Log.d(TAG, "Updating comment: " + commentId);
        Log.d(TAG, "Delete image flag: " + shouldRemoveEditCommentImage);

        // Get image file if a new image was selected
        File imageFile = null;
        if (selectedEditCommentImageUri != null) {
            try {
                imageFile = getFileFromUri(selectedEditCommentImageUri);
                Log.d(TAG, "New image file prepared for update: " + imageFile.getPath());
            } catch (Exception e) {
                Log.e(TAG, "Error preparing image file", e);
                Toast.makeText(requireContext(), R.string.error_upload, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Show uploading toast
        Toast.makeText(requireContext(), R.string.community_loading, Toast.LENGTH_SHORT).show();

        // Call repository to update comment with image support and deleteImage flag
        commentRepository.updateComment(commentId, newContent, imageFile, shouldRemoveEditCommentImage, new Callback<ApiResponse<CommentResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<CommentResponse>> call,
                                   Response<ApiResponse<CommentResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    CommentResponse updatedComment = response.body().getData();
                    Log.d(TAG, "Comment updated successfully");

                    // Update adapter
                    commentAdapter.updateComment(position, updatedComment);

                    Toast.makeText(requireContext(), R.string.success_comment_updated, Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Failed to update comment");
                    Toast.makeText(requireContext(), R.string.error_update_failed, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<CommentResponse>> call, Throwable t) {
                Log.e(TAG, "Error updating comment: " + t.getMessage());
                Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });

        // Reset edit image state
        selectedEditCommentImageUri = null;
        shouldRemoveEditCommentImage = false;
    }

    private void showMoreOptionsDialog() {
        // TODO: Check if current user owns this post
        // For now, just show basic options

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());

        // If user owns the post, show edit and delete options
        // Otherwise, show report option
        String[] options = {"Edit Post", "Delete Post", "Cancel"};

        builder.setTitle("Post Options")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Edit
                            // TODO: Navigate to edit post screen
                            Toast.makeText(requireContext(), "Edit post: " + currentPost.getId(),
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case 1: // Delete
                            showDeleteConfirmation();
                            break;
                        case 2: // Cancel
                            dialog.dismiss();
                            break;
                    }
                })
                .show();
    }

    private void showDeleteConfirmation() {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.community_delete_post_title)
                .setMessage(R.string.community_confirm_delete_post)
                .setPositiveButton(R.string.community_delete, (dialog, which) -> {
                    deletePost();
                })
                .setNegativeButton(R.string.community_cancel, null)
                .show();
    }

    private void navigateToEditPost() {
        if (currentPost == null) return;

        CreateUpdatePostFragment editFragment = CreateUpdatePostFragment.newInstance(currentPost);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, editFragment)
                .addToBackStack(null)
                .commit();
    }

    private void deletePost() {
        binding.progressBar.setVisibility(View.VISIBLE);

        repository.deletePost(currentPost.getId(), new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call,
                                   Response<ApiResponse<String>> response) {
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(requireContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                } else {
                    Toast.makeText(requireContext(), "Failed to delete post", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatDate(Date date) {
        if (date == null) return "";

        long diff = System.currentTimeMillis() - date.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 7) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return sdf.format(date);
        } else if (days > 0) {
            return days + "d ago";
        } else if (hours > 0) {
            return hours + "h ago";
        } else if (minutes > 0) {
            return minutes + "m ago";
        } else {
            return "Just now";
        }
    }

    private void displayCommentImagePreview() {
        if (selectedCommentImageUri != null) {
            binding.llCommentImagePreview.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(selectedCommentImageUri)
                    .placeholder(R.color.gray_450)
                    .into(binding.ivCommentImagePreview);
        }
    }

    private File getFileFromUri(Uri uri) throws Exception {
        // Get content resolver
        android.content.ContentResolver contentResolver = requireContext().getContentResolver();

        // Create a temporary file
        String fileName = "temp_comment_" + System.currentTimeMillis();
        String mimeType = contentResolver.getType(uri);

        // Determine file extension
        String extension = "";
        if (mimeType != null) {
            if (mimeType.startsWith("image/")) {
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

    private void showFullImageDialog(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return;

        // Create dialog
        android.app.Dialog dialog = new android.app.Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_full_image);

        // Get views
        android.widget.ImageView imageView = dialog.findViewById(R.id.iv_full_image);
        android.widget.ImageButton closeButton = dialog.findViewById(R.id.ib_close);

        // Load image
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.color.gray_450)
                .into(imageView);

        // Close button
        closeButton.setOnClickListener(v -> dialog.dismiss());

        // Close on image click
        imageView.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        hideHeaderAndBottomNavigation();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        showHeaderAndBottomNavigation();
        Log.e(TAG, "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Header and bottom nav visibility restoration is controlled by MainActivity's FragmentLifecycleCallbacks

        // Clear edit comment callback to prevent crashes
        editCommentImageUpdateCallback = null;
//        showHeaderAndBottomNavigation();
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        binding = null;
        Log.e(TAG, "onDestroyView");
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

}
