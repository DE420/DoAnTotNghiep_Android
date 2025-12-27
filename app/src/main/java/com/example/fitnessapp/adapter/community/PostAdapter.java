package com.example.fitnessapp.adapter.community;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitnessapp.R;
import com.example.fitnessapp.databinding.ItemPostBinding;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.community.PostResponse;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.network.RetrofitClient;
import com.example.fitnessapp.repository.PostRepository;
import com.example.fitnessapp.utils.CountFormatUtils;
import com.example.fitnessapp.utils.TimeUtils;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private static final String TAG = "PostAdapter";
    private static final int MAX_CONCURRENT_PLAYERS = 3; // Limit concurrent players
    private static final long PLAYER_INIT_DELAY_MS = 150; // Delay before creating player

    private PostRepository repository;
    private List<PostResponse> mList;
    private PostItemListener itemListener;
    private Context context;
    private Handler mainHandler;
    private int activePlayerCount = 0;

    // Track all view holders to release players when needed
    private final List<PostViewHolder> allHolders = new ArrayList<>();
    private AudioAttributes audioAttributes;

    public PostAdapter(Context context) {
        mList = new ArrayList<>();
        repository = new PostRepository(context);
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());

        // Initialize audio attributes for proper video playback with audio
        this.audioAttributes = new AudioAttributes.Builder()
                .setContentType(com.google.android.exoplayer2.C.AUDIO_CONTENT_TYPE_MOVIE)
                .setUsage(com.google.android.exoplayer2.C.USAGE_MEDIA)
                .build();
    }

    public void setItemListener(PostItemListener itemListener) {
        this.itemListener = itemListener;
    }

    public void addPosts(List<PostResponse> postResponseList) {
        mList.addAll(postResponseList);
        notifyItemRangeInserted(
                mList.size() - postResponseList.size() - 1,
                postResponseList.size());
    }

    public void setPosts(List<PostResponse> postResponseList) {
        mList.clear();
        mList.addAll(postResponseList);
        notifyDataSetChanged();
    }

    public PostResponse getItem(int position) {
        return mList.get(position);
    }

    /**
     * Remove post at position
     */
    public void removePost(int position) {
        if (position >= 0 && position < mList.size()) {
            mList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mList.size());
            Log.d(TAG, "Post removed at position: " + position);
        }
    }

    /**
     * Pause all video players
     */
    public void pauseAllVideos() {
        Log.d(TAG, "Pausing all videos across all holders");
        for (PostViewHolder holder : allHolders) {
            if (holder.player != null && holder.player.isPlaying()) {
                holder.player.pause();
            }
        }
    }

    /**
     * Release all video players to free resources
     */
    public void releaseAllPlayers() {
        Log.d(TAG, "Releasing all players - total holders: " + allHolders.size());
        for (PostViewHolder holder : allHolders) {
            pauseAndReleasePlayer(holder);
        }
        activePlayerCount = 0;
    }


    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPostBinding itemPostBinding = ItemPostBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        PostViewHolder holder = new PostViewHolder(itemPostBinding);
        allHolders.add(holder); // Track this holder
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        PostResponse item = mList.get(position);
        ItemPostBinding binding = holder.itemPostBinding;

        setDataForItem(holder, item);

        // Delete post click
        binding.ibDelete.setOnClickListener(view -> {
            if (itemListener != null) {
                itemListener.onDeleteClick(view, position);
            }
        });

        // Edit post click
        binding.ibEdit.setOnClickListener(view -> {
            if (itemListener != null) {
                itemListener.onEditClick(view, position);
            }
        });

        // Comment click
        binding.clComment.setOnClickListener(view -> {
            if (itemListener != null) {
                itemListener.onCommentClick(view, position);
            }
        });

        // Post item click (for viewing detail)
        binding.getRoot().setOnClickListener(view -> {
            if (itemListener != null) {
                itemListener.onItemClick(view, position);
            }
        });

        // Content text expand/collapse
        binding.tvContent.setOnClickListener(view -> {
            if (binding.tvContent.getMaxLines() == 3) {
                binding.tvContent.setMaxLines(Integer.MAX_VALUE);
                Log.d(TAG, "Content expanded");
            } else {
                binding.tvContent.setMaxLines(3);
                Log.d(TAG, "Content collapsed");
            }
        });

        // Like/Unlike functionality
        binding.clLike.setOnClickListener(view -> {
            handleLikeUnlike(item, position, binding);
        });
    }

    @Override
    public void onViewRecycled(@NonNull PostViewHolder holder) {
        super.onViewRecycled(holder);
        // Pause and release ExoPlayer when view is recycled
        pauseAndReleasePlayer(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull PostViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        // Pause video when scrolled out of view
        if (holder.player != null && holder.player.isPlaying()) {
            holder.player.pause();
            Log.d(TAG, "Video paused - scrolled out of view");
        }
    }

    /**
     * Pause and release player for a holder
     * MUST be called on main thread
     */
    private void pauseAndReleasePlayer(PostViewHolder holder) {
        if (holder.player != null) {
            // Cancel any pending player initialization
            if (holder.playerInitRunnable != null) {
                mainHandler.removeCallbacks(holder.playerInitRunnable);
                holder.playerInitRunnable = null;
            }

            final ExoPlayer playerToRelease = holder.player;
            final ItemPostBinding binding = holder.itemPostBinding;
            holder.player = null;

            // Release on main thread
            mainHandler.post(() -> {
                try {
                    // Clear PlayerView's player reference first to release surface
                    if (binding != null && binding.playerView != null) {
                        binding.playerView.setPlayer(null);
                    }
                    playerToRelease.stop();
                    playerToRelease.release();
                    activePlayerCount = Math.max(0, activePlayerCount - 1);
                    Log.d(TAG, "Player released, active count: " + activePlayerCount);
                } catch (Exception e) {
                    Log.e(TAG, "Error releasing player: " + e.getMessage());
                }
            });
        }
    }

    /**
     * Pause all currently playing videos in the RecyclerView
     */
    public void pauseAllVideos(RecyclerView recyclerView) {
        if (recyclerView == null) return;

        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
            if (holder instanceof PostViewHolder) {
                PostViewHolder postHolder = (PostViewHolder) holder;
                if (postHolder.player != null && postHolder.player.isPlaying()) {
                    postHolder.player.pause();
                    Log.d(TAG, "Paused video at adapter position: " + postHolder.getAdapterPosition());
                }
            }
        }
    }

    private void handleLikeUnlike(PostResponse item, int position, ItemPostBinding binding) {
        boolean isLiked = item.getLiked();
        long postId = item.getId();

        Log.d(TAG, "handleLikeUnlike - postId: " + postId + ", currentLiked: " + isLiked);

        // Optimistic UI update
        item.setLiked(!isLiked);
        long newLikeCount = item.getLikeCount() + (isLiked ? -1 : 1);
        item.setLikeCount(newLikeCount);

        updateLikeUI(binding, item);

        // Make API call
        Callback<ApiResponse<PostResponse>> callback = new Callback<ApiResponse<PostResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<PostResponse>> call, Response<ApiResponse<PostResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    PostResponse updatedPost = response.body().getData();
                    Log.d(TAG, "Like/Unlike successful - new like count: " + updatedPost.getLikeCount());

                    // Update with server response
                    item.setLiked(updatedPost.getLiked());
                    item.setLikeCount(updatedPost.getLikeCount());
                    updateLikeUI(binding, item);
                } else {
                    // Revert on error
                    Log.e(TAG, "Like/Unlike failed - reverting");
                    item.setLiked(isLiked);
                    item.setLikeCount(item.getLikeCount() + (isLiked ? 1 : -1));
                    updateLikeUI(binding, item);
                    Snackbar.make(binding.getRoot(), "Failed to update like", Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PostResponse>> call, Throwable t) {
                Log.e(TAG, "Like/Unlike error: " + t.getMessage());
                // Revert on error
                item.setLiked(isLiked);
                item.setLikeCount(item.getLikeCount() + (isLiked ? 1 : -1));
                updateLikeUI(binding, item);
                Snackbar.make(binding.getRoot(), "Network error", Snackbar.LENGTH_SHORT).show();
            }
        };

        if (isLiked) {
            repository.unlikePost(postId, callback);
        } else {
            repository.likePost(postId, callback);
        }
    }

    private void updateLikeUI(ItemPostBinding binding, PostResponse item) {
        binding.tvLikeCount.setText(CountFormatUtils.formatCount(item.getLikeCount()));

        if (item.getLiked() != null && item.getLiked()) {
            Glide.with(binding.imgLike)
                    .load(R.drawable.ic_like_filled_24)
                    .into(binding.imgLike);
            binding.imgLike.setColorFilter(context.getColor(R.color.yellow));
        } else {
            Glide.with(binding.imgLike)
                    .load(R.drawable.ic_like_outline_24)
                    .into(binding.imgLike);
            binding.imgLike.setColorFilter(context.getColor(R.color.white));
        }
    }

    private void setDataForItem(PostViewHolder holder, PostResponse item) {
        ItemPostBinding binding = holder.itemPostBinding;

        Glide.with(binding.civAvatar)
                .load(item.getUserAvatarUrl())
                .placeholder(R.drawable.ic_image_24)
                .error(R.drawable.img_user_default_128)
                .into(binding.civAvatar);

        binding.tvUserFullName.setText(item.getUserName() != null ? item.getUserName() : "Unknown User");

        binding.tvPostTime.setText(TimeUtils.getTime(context, item.getCreateAt()));
        binding.tvCommentCount.setText(CountFormatUtils.formatCount(item.getCommentCount()));
        binding.tvContent.setText(item.getContent());

        // Update like UI
        updateLikeUI(binding, item);

        // Handle Image and Video - Show both if available
        boolean hasImage = item.getImageUrl() != null && !item.getImageUrl().isEmpty();
        boolean hasVideo = item.getVideoUrl() != null && !item.getVideoUrl().isEmpty();

        if (hasImage) {
            binding.cardImage.setVisibility(View.VISIBLE);
            Log.d(TAG, "Loading image: " + item.getImageUrl());
            Glide.with(binding.imgPost)
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.ic_empty_community_96)
                    .error(R.drawable.ic_empty_community_96)
                    .into(binding.imgPost);

            // Add click listener to view full image
            binding.imgPost.setOnClickListener(v -> {
                if (itemListener != null) {
                    itemListener.onImageClick(item.getImageUrl());
                }
            });
        } else {
            binding.cardImage.setVisibility(View.GONE);
        }

        // Handle Video - Show if available with rate limiting
        if (hasVideo) {
            binding.cardVideo.setVisibility(View.VISIBLE);

            // Cancel any pending player initialization for this holder
            if (holder.playerInitRunnable != null) {
                mainHandler.removeCallbacks(holder.playerInitRunnable);
                holder.playerInitRunnable = null;
            }

            // Release previous player if exists
            pauseAndReleasePlayer(holder);

            // Check if we can create more players
            if (activePlayerCount >= MAX_CONCURRENT_PLAYERS) {
                Log.d(TAG, "Max concurrent players reached (" + MAX_CONCURRENT_PLAYERS + "), skipping player initialization");
                binding.cardVideo.setVisibility(View.VISIBLE); // Show placeholder
                return;
            }

            // Schedule delayed player initialization
            final String videoUrl = item.getVideoUrl();
            holder.playerInitRunnable = () -> {
                if (holder.playerInitRunnable == null) {
                    // Runnable was cancelled
                    return;
                }

                try {
                    Log.d(TAG, "Initializing player for video: " + videoUrl);

                    // Clear any existing player from PlayerView first
                    binding.playerView.setPlayer(null);

                    // Create new ExoPlayer instance with audio attributes
                    holder.player = new ExoPlayer.Builder(context)
                            .setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true)
                            .build();
                    binding.playerView.setPlayer(holder.player);

                    // Prepare media
                    MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
                    holder.player.setMediaItem(mediaItem);
                    holder.player.prepare();
                    holder.player.setPlayWhenReady(false); // Don't auto-play

                    activePlayerCount++;
                    Log.d(TAG, "Player initialized successfully, active count: " + activePlayerCount);
                } catch (Exception e) {
                    Log.e(TAG, "Error initializing player: " + e.getMessage(), e);
                    binding.cardVideo.setVisibility(View.VISIBLE); // Show placeholder
                    if (holder.player != null) {
                        try {
                            binding.playerView.setPlayer(null);
                            holder.player.release();
                        } catch (Exception ex) {
                            Log.e(TAG, "Error releasing player after init failure: " + ex.getMessage());
                        }
                        holder.player = null;
                    }
                } finally {
                    holder.playerInitRunnable = null;
                }
            };

            // Post delayed to avoid rapid creation during scrolling
            mainHandler.postDelayed(holder.playerInitRunnable, PLAYER_INIT_DELAY_MS);
        } else {
            binding.cardVideo.setVisibility(View.GONE);
            // Release player if no video
            pauseAndReleasePlayer(holder);
        }

        // Show/hide edit and delete buttons
        binding.ibEdit.setVisibility(item.getCanEdit() ? View.VISIBLE : View.GONE);
        binding.ibDelete.setVisibility(item.getCanDelete() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {

        private final ItemPostBinding itemPostBinding;
        public ExoPlayer player;
        public Runnable playerInitRunnable; // Track pending player initialization

        public PostViewHolder(@NonNull ItemPostBinding itemPostBinding) {
            super(itemPostBinding.getRoot());
            this.itemPostBinding = itemPostBinding;
        }
    }

    public interface PostItemListener {
        void onItemClick(View view, int position);
        void onEditClick(View view, int position);
        void onCommentClick(View view, int position);
        void onDeleteClick(View view, int position);
        void onImageClick(String imageUrl);
    }

}
