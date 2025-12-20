package com.example.fitnessapp.fragment.community;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fitnessapp.R;
import com.example.fitnessapp.adapter.community.PostAdapter;
import com.example.fitnessapp.databinding.FragmentAllPostBinding;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.Pagination;
import com.example.fitnessapp.model.response.community.PostResponse;
import com.example.fitnessapp.repository.PostRepository;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllPostFragment extends Fragment {

    public static final String TAG = AllPostFragment.class.getSimpleName();

    private FragmentAllPostBinding binding;
    private PostRepository repository;
    private int page = 0;
    private boolean loading = false;
    Pagination pagination;
    private PostAdapter adapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAllPostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new PostRepository(requireContext());

        adapter = new PostAdapter(requireContext());

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        binding.rvPostList.setLayoutManager(layoutManager);
        binding.rvPostList.setAdapter(adapter);
        DividerItemDecoration divider = new DividerItemDecoration(requireContext(),
                layoutManager.getOrientation());
        divider.setDrawable(getResources().getDrawable(R.drawable.post_list_divider, null));
        binding.rvPostList.addItemDecoration(divider);
        loadNext();

    }

    private void loadNext() {

        loading = true;

        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("size", String.valueOf(10));
        params.put("order", "desc");

        repository.getAllPosts(params,
                new Callback<ApiResponse<List<PostResponse>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<PostResponse>>> call, Response<ApiResponse<List<PostResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            pagination = response.body().getMeta();
                            if (response.body().isStatus()) {
                                adapter.addPosts(response.body().getData());

                            } else {
                                String error = response.body().getData().toString();
                                Log.e(TAG, error);
                                Snackbar.make(binding.getRoot(),
                                                "onResponse status false call api",
                                                Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                        } else {
                            String error = response.body() != null ? response.body().getData().toString() : getString(R.string.txt_error_loading_data);
                            try {
                                Log.e(TAG, "code: " + response.code() + "message: +" + (response.errorBody() != null ? response.errorBody().string() : "error loading data unknown."));
                            } catch (IOException e) {
                                Log.e(TAG, e.getMessage());
                            }
                            Snackbar.make(
                                            binding.getRoot(),
                                            "onResponse isSuccessfull false",
                                            Snackbar.LENGTH_SHORT
                                    ).setBackgroundTint(getResources().getColor(R.color.red_400, null))
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<PostResponse>>> call, Throwable t) {
                        Log.e(TAG, Objects.requireNonNull(t.getMessage()));
                        Snackbar.make(
                                        binding.getRoot(),
                                        "onFailure",
                                        Snackbar.LENGTH_SHORT
                                ).setBackgroundTint(getResources().getColor(R.color.red_400, null))
                                .show();
                    }
                });
    }
}
