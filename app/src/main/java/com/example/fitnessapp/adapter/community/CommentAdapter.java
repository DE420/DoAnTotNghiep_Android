package com.example.fitnessapp.adapter.community;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitnessapp.R;
import com.example.fitnessapp.databinding.ItemCommentBinding;
import com.example.fitnessapp.model.response.community.CommentResponse;
import com.example.fitnessapp.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private static final String TAG = "CommentAdapter";
    private List<CommentResponse> comments;
    private CommentItemListener itemListener;
    private Context context;

    public CommentAdapter(Context context) {
        this.context = context;
        this.comments = new ArrayList<>();
    }

    public void setItemListener(CommentItemListener listener) {
        this.itemListener = listener;
    }

    public void setComments(List<CommentResponse> comments) {
        this.comments.clear();
        if (comments != null) {
            this.comments.addAll(comments);
        }
        notifyDataSetChanged();
    }

    public void addComment(CommentResponse comment) {
        comments.add(0, comment); // Add at the beginning
        notifyItemInserted(0);
    }

    public void removeComment(int position) {
        if (position >= 0 && position < comments.size()) {
            comments.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, comments.size());
        }
    }

    public void updateComment(int position, CommentResponse comment) {
        if (position >= 0 && position < comments.size()) {
            comments.set(position, comment);
            notifyItemChanged(position);
        }
    }

    public CommentResponse getItem(int position) {
        return comments.get(position);
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCommentBinding binding = ItemCommentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CommentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentResponse comment = comments.get(position);
        ItemCommentBinding binding = holder.binding;

        // Set user avatar
        if (comment.getUserAvatarUrl() != null && !comment.getUserAvatarUrl().isEmpty()) {
            Glide.with(context)
                    .load(comment.getUserAvatarUrl())
                    .placeholder(R.drawable.img_user_default_128)
                    .error(R.drawable.img_user_default_128)
                    .into(binding.ivUserAvatar);
        } else {
            binding.ivUserAvatar.setImageResource(R.drawable.img_user_default_128);
        }

        // Set user name
        binding.tvUserName.setText(comment.getUserName() != null ? comment.getUserName() : "Unknown User");

        // Set comment date
        binding.tvCommentDate.setText(comment.getCreatedAt() != null ?
                TimeUtils.getTime(comment.getCreatedAt()) : "");

        // Set comment content
        binding.tvCommentContent.setText(comment.getContent());

        // Display comment image if exists
        if (comment.getImageUrl() != null && !comment.getImageUrl().isEmpty()) {
            binding.ivCommentImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(comment.getImageUrl())
                    .placeholder(R.drawable.ic_image_24)
                    .error(R.drawable.ic_image_24)
                    .into(binding.ivCommentImage);

            // Add click listener to view full image
            binding.ivCommentImage.setOnClickListener(v -> {
                if (itemListener != null) {
                    itemListener.onImageClick(comment.getImageUrl());
                }
            });
        } else {
            binding.ivCommentImage.setVisibility(View.GONE);
        }

        // Set like count and status
        binding.tvLikeCount.setText(String.valueOf(comment.getLikeCount() != null ? comment.getLikeCount() : 0));

        // Update like button appearance based on liked status
        if (comment.getLiked() != null && comment.getLiked()) {
            binding.ibLikeComment.setImageResource(R.drawable.ic_like_filled_24);
            binding.ibLikeComment.setColorFilter(context.getColor(R.color.yellow));
        } else {
            binding.ibLikeComment.setImageResource(R.drawable.ic_like_outline_24);
            binding.ibLikeComment.setColorFilter(context.getColor(R.color.white));
        }

        // Set like button click listener
        binding.ibLikeComment.setOnClickListener(v -> {
            if (itemListener != null) {
                itemListener.onLikeClick(v, position);
            }
        });

        // Show/hide edit and delete buttons based on ownership
        if (comment.getCanDelete() != null && comment.getCanDelete()) {
            binding.ibEditComment.setVisibility(View.VISIBLE);
            binding.ibDeleteComment.setVisibility(View.VISIBLE);
        } else {
            binding.ibEditComment.setVisibility(View.GONE);
            binding.ibDeleteComment.setVisibility(View.GONE);
        }

        // Set edit button click listener
        binding.ibEditComment.setOnClickListener(v -> {
            if (itemListener != null) {
                itemListener.onEditClick(v, position);
            }
        });

        // Set delete button click listener
        binding.ibDeleteComment.setOnClickListener(v -> {
            if (itemListener != null) {
                itemListener.onDeleteClick(v, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return comments != null ? comments.size() : 0;
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        private final ItemCommentBinding binding;

        public CommentViewHolder(@NonNull ItemCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface CommentItemListener {
        void onEditClick(View view, int position);
        void onDeleteClick(View view, int position);
        void onLikeClick(View view, int position);
        void onImageClick(String imageUrl);
    }
}
