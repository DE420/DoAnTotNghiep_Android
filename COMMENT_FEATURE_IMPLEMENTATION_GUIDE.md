# Comment Feature Implementation Guide

## Summary of Changes Needed in PostDetailFragment.java

You need to make the following changes to `/home/tuyen/data/code/DATN_TONG/DoAnTotNghiep_Android/app/src/main/java/com/example/fitnessapp/fragment/community/PostDetailFragment.java`:

### 1. Add Required Imports and Fields

Add these imports at the top (around line 1-40):
```java
import android.app.Activity;
import android.content.Intent;
import android.provider.MediaStore;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.io.File;
```

Add these fields to the class (around line 47-54):
```java
private Uri selectedCommentImageUri;
private ActivityResultLauncher<Intent> imagePickerLauncher;
```

### 2. Update onCreate() Method

In the `onCreate()` method (around line 64-71), add image picker registration BEFORE the repositories are initialized:

```java
@Override
public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
        postId = getArguments().getLong(ARG_POST_ID);
    }

    // Register image picker launcher
    imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedCommentImageUri = result.getData().getData();
                    displayCommentImagePreview();
                }
            }
    );

    repository = new PostRepository(requireContext());
    commentRepository = new CommentRepository(requireContext());
}
```

### 3. Add Helper Methods

Add these new methods BEFORE the existing `sendComment()` method (around line 452):

```java
/**
 * Display selected comment image preview
 */
private void displayCommentImagePreview() {
    if (selectedCommentImageUri != null) {
        binding.llCommentImagePreview.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(selectedCommentImageUri)
                .into(binding.ivCommentImagePreview);
    } else {
        binding.llCommentImagePreview.setVisibility(View.GONE);
    }
}

/**
 * Convert URI to File for image upload
 */
private File getFileFromUri(Uri uri) throws Exception {
    android.content.ContentResolver contentResolver = requireContext().getContentResolver();
    String fileName = "comment_img_" + System.currentTimeMillis() + ".jpg";
    File tempFile = new File(requireContext().getCacheDir(), fileName);

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
```

### 4. Update setupListeners() Method

Find the `setupListeners()` method (around line 157) and ADD these new listeners:

```java
// ADD after existing listeners:

// Comment image attach button
binding.ibAttachImage.setOnClickListener(v -> {
    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    intent.setType("image/*");
    imagePickerLauncher.launch(intent);
});

// Remove comment image button
binding.ibRemoveCommentImage.setOnClickListener(v -> {
    selectedCommentImageUri = null;
    binding.llCommentImagePreview.setVisibility(View.GONE);
});
```

### 5. Update setupRecyclerView() Method

Find the `commentAdapter.setItemListener` (around line 148) and REPLACE the entire listener with:

```java
commentAdapter.setItemListener(new CommentAdapter.CommentItemListener() {
    @Override
    public void onDeleteClick(View view, int position) {
        showDeleteCommentConfirmation(position);
    }

    @Override
    public void onLikeClick(View view, int position) {
        handleCommentLikeUnlike(position);
    }
});
```

### 6. Update sendComment() Method

Find the `sendComment()` method (around line 452) and REPLACE it with:

```java
private void sendComment(String content) {
    Log.d(TAG, "Sending comment: " + content);
    binding.ibSendComment.setEnabled(false);

    // Prepare image file if selected
    File imageFile = null;
    if (selectedCommentImageUri != null) {
        try {
            imageFile = getFileFromUri(selectedCommentImageUri);
            Log.d(TAG, "Comment image prepared: " + imageFile.getPath());
        } catch (Exception e) {
            Log.e(TAG, "Error preparing comment image", e);
            Toast.makeText(requireContext(), "Error preparing image", Toast.LENGTH_SHORT).show();
            binding.ibSendComment.setEnabled(true);
            return;
        }
    }

    commentRepository.createComment(postId, content, imageFile, new Callback<ApiResponse<CommentResponse>>() {
        @Override
        public void onResponse(Call<ApiResponse<CommentResponse>> call,
                               Response<ApiResponse<CommentResponse>> response) {
            binding.ibSendComment.setEnabled(true);

            if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                CommentResponse newComment = response.body().getData();
                Log.d(TAG, "Comment created successfully");

                // Add comment to adapter
                commentAdapter.addComment(newComment);

                // Show RecyclerView and hide empty state
                binding.rvComments.setVisibility(View.VISIBLE);
                binding.llEmptyComments.setVisibility(View.GONE);

                // Clear input field and image
                binding.etComment.setText("");
                selectedCommentImageUri = null;
                binding.llCommentImagePreview.setVisibility(View.GONE);

                // Update comment count
                if (currentPost != null) {
                    currentPost.setCommentCount(currentPost.getCommentCount() + 1);
                    binding.tvCommentCount.setText(String.valueOf(currentPost.getCommentCount()));
                }

                Toast.makeText(requireContext(), "Comment added", Toast.LENGTH_SHORT).show();

                // Scroll to top to show new comment
                binding.rvComments.smoothScrollToPosition(0);
            } else {
                Log.e(TAG, "Failed to create comment");
                Toast.makeText(requireContext(), "Failed to post comment", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<ApiResponse<CommentResponse>> call, Throwable t) {
            binding.ibSendComment.setEnabled(true);
            Log.e(TAG, "Error creating comment: " + t.getMessage());
            Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
        }
    });
}
```

### 7. Add New Method for Comment Like/Unlike

Add this NEW method after the `sendComment()` method:

```java
/**
 * Handle comment like/unlike toggle
 */
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
```

## Files Already Updated

✅ `item_comment.xml` - Added image display and like button
✅ `fragment_post_detail.xml` - Added image picker and preview
✅ `CommentAdapter.java` - Added like handling and image display
✅ `CommentRepository.java` - Added image upload support
✅ `CommentApi.java` - Updated to accept image parameter
✅ `CommentSpecification.java` (backend) - Added fetchPostWithUser
✅ `CommentService.java` (backend) - Fixed Hibernate lazy loading issue

## Auto-Refresh Feature

The auto-refresh when returning from detail/edit/create is ALREADY WORKING because AllPostFragment and MyPostFragment both have `onStart()` that calls `viewModel.loadPosts()`.

## Testing Checklist

After making these changes:
1. ✅ Build the app
2. ✅ Test creating a comment without image
3. ✅ Test creating a comment with image
4. ✅ Test liking/unliking comments
5. ✅ Test deleting comments
6. ✅ Test viewing comment images
7. ✅ Test auto-refresh when returning from post detail

Good luck!
