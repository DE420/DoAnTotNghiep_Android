package com.example.fitnessapp.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.fitnessapp.LoginActivity;
import com.example.fitnessapp.R;
import com.example.fitnessapp.model.request.LogoutRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.network.RetrofitClient;
import com.example.fitnessapp.session.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmLogoutDialogFragment extends DialogFragment implements View.OnClickListener {

    private Button btnConfirm, btnCancel;
    private ApiService apiService;

    private View viewLoading, viewContent;
    private int shortAnimationDuration;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_confirm_logout_dialog, container, false);


        btnConfirm = view.findViewById(R.id.btn_confirm_logout);
        btnConfirm.setOnClickListener(this);
        btnCancel = view.findViewById(R.id.btn_cancel_logout);
        btnCancel.setOnClickListener(this);

        viewLoading = view.findViewById(R.id.rl_loading);
        viewContent = view.findViewById(R.id.cl_content);
        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        apiService = RetrofitClient.getApiService();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_confirm_logout) {
            crossfadeFromContentViewToLoadingView();
            String refreshToken = SessionManager.getInstance(requireActivity()).getRefreshToken();

            if (refreshToken == null) {
                Toast.makeText(requireContext(), "No active session found.", Toast.LENGTH_SHORT).show();
                clearUserDataAndNavigateToLogin();
                return;
            }

            LogoutRequest logoutRequest = new LogoutRequest(refreshToken);

            apiService.logout(logoutRequest).enqueue(new Callback<ApiResponse<String>>() {
                @Override
                public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                    Toast.makeText(requireContext(), "Logged out successfully.", Toast.LENGTH_SHORT).show();
                    clearUserDataAndNavigateToLogin();
                }

                @Override
                public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                    Toast.makeText(requireContext(), "Logged out (offline).", Toast.LENGTH_SHORT).show();
                    clearUserDataAndNavigateToLogin();
                }
            });
        } else {
            dismiss();
        }
    }

    private void clearUserDataAndNavigateToLogin() {
        SessionManager.getInstance(requireActivity()).logout();

        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Kết thúc Activity chứa Fragment này (MainActivity)
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void crossfadeFromContentViewToLoadingView() {

        btnConfirm.setEnabled(false);
        btnCancel.setEnabled(false);

        // Set the content view to 0% opacity but visible, so that it is
        // visible but fully transparent during the animation.
        viewLoading.setAlpha(0f);
        viewLoading.setVisibility(View.VISIBLE);


        // Animate the content view to 100% opacity and clear any animation
        // listener set on the view.
        viewLoading.animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration)
                .setListener(null);




        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step so it doesn't
        // participate in layout passes.
        viewContent.animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        viewContent.setVisibility(View.INVISIBLE);
                    }
                });
    }
}
