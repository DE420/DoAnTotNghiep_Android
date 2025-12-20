package com.example.fitnessapp.adapter.community;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitnessapp.R;
import com.example.fitnessapp.databinding.ItemPostBinding;
import com.example.fitnessapp.model.response.community.PostResponse;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.network.RetrofitClient;
import com.example.fitnessapp.utils.CountFormatUtils;
import com.example.fitnessapp.utils.TimeUtils;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private ApiService apiService;
    private List<PostResponse> mList;
    private PostItemListener itemListener;
    private Context context;

    public PostAdapter(Context context) {
        mList = new ArrayList<>();
        apiService = RetrofitClient.getApiService();
        this.context = context;
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

    public PostResponse getItem(int position) {
        return mList.get(position);
    }


    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPostBinding itemPostBinding = ItemPostBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new PostViewHolder(itemPostBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        PostResponse item = mList.get(position);

        ItemPostBinding binding = holder.itemPostBinding;

        setDataForItem(holder, item);

        binding.ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(binding.getRoot(), "Delete post", Snackbar.LENGTH_SHORT).show();
            }
        });

        binding.tvContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.tvContent.getMaxLines() < 3) {
                    binding.tvContent.setMaxLines(100);
                } else {
                    binding.tvContent.setMaxLines(2);
                }
            }
        });

        binding.imgLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (item.getLiked()) {

                    Glide.with(binding.imgLike)
                            .load(R.drawable.ic_like_32)
                            .into(binding.imgLike);
                } else {
                    Glide.with(binding.imgLike)
                            .load(R.drawable.ic_blue_like_32)
                            .into(binding.imgLike);
                }
            }
        });
    }

    private void setDataForItem(PostViewHolder holder, PostResponse item) {
        ItemPostBinding binding = holder.itemPostBinding;

        Glide.with(binding.civAvatar)
                .load(item.getUserAvatarUrl())
                .error(R.drawable.img_user_default_128)
                .into(binding.civAvatar);

        binding.tvUserFullName.setText(item.getUserName());

        binding.tvPostTime.setText(TimeUtils.getTime(item.getCreateAt()));
        binding.tvCommentCount.setText(
                CountFormatUtils.formatCount(item.getCommentCount())
        );

        binding.tvLikeCount.setText(
                CountFormatUtils.formatCount(item.getLikeCount())
        );

        binding.tvContent.setText(item.getContent());

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            binding.imgPost.setVisibility(View.VISIBLE);
            Glide.with(binding.imgPost)
                    .load(item.getImageUrl())
                    .into(binding.imgPost);
        } else {
            binding.imgPost.setVisibility(View.GONE);
        }

        if (item.getVideoUrl() != null && !item.getVideoUrl().isEmpty()) {
            binding.videoPost.setVisibility(View.VISIBLE);
            Uri uri = Uri.parse(item.getVideoUrl());
            binding.videoPost.setVideoURI(uri);
            MediaController mediaController = new MediaController(context);
            mediaController.setAnchorView(binding.videoPost);
            binding.videoPost.setMediaController(mediaController);

        } else {
            binding.videoPost.setVisibility(View.GONE);
        }

        if (item.getCanEdit()) {
            binding.ibEdit.setVisibility(View.VISIBLE);
        } else {
            binding.ibEdit.setVisibility(View.GONE);
        }

        if (item.getCanDelete()) {
            binding.ibDelete.setVisibility(View.VISIBLE);
        } else {
            binding.ibDelete.setVisibility(View.GONE);
        }

        if (item.getLiked()) {
            Glide.with(binding.imgLike)
                    .load(R.drawable.ic_blue_like_32)
                    .into(binding.imgLike);
        } else {
            Glide.with(binding.imgLike)
                    .load(R.drawable.ic_like_32)
                    .into(binding.imgLike);
        }
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {

        private final ItemPostBinding itemPostBinding;

        public PostViewHolder(@NonNull ItemPostBinding itemPostBinding) {
            super(itemPostBinding.getRoot());
            this.itemPostBinding = itemPostBinding;
        }
    }

    public interface PostItemListener {
        void onItemClick(View view, int position);
        void onEditClick(View view, int position);
        void onCommentClick(View view, int position);
    }

}
