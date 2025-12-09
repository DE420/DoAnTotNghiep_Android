package com.example.fitnessapp.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.constants.Constants;
import com.example.fitnessapp.databinding.FragmentChangePasswordBinding;
import com.example.fitnessapp.model.WrapperEditTextState;
import com.example.fitnessapp.model.request.ChangePasswordRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.network.RetrofitClient;
import com.example.fitnessapp.session.SessionManager;
import com.example.fitnessapp.utils.StringUtil;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener, View.OnKeyListener {

    public static final String TAG = ChangePasswordFragment.class.getSimpleName();
    private ApiService apiService;

    private FragmentChangePasswordBinding binding;
    private int colorPurple400, colorWhite200, colorPink200;
    private int shortAnimationDuration;

    private OnBackPressedCallback backPressedCallback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        backPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                // Do nothing → back button disabled
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(this, backPressedCallback);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        apiService = RetrofitClient.getApiService();

        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        colorPurple400 = getResources().getColor(R.color.purple_400, null);
        colorWhite200 = getResources().getColor(R.color.white_200, null);
        colorPink200 = getResources().getColor(R.color.pink_200, null);

        setupClickListeners();
        setupOnFocusListeners();
        setupOnKeyListeners();

        disableCopyPasteEditText(binding.etCurrentPassword);
        disableCopyPasteEditText(binding.etNewPassword);
        disableCopyPasteEditText(binding.etConfirmPassword);
    }



    private void setupClickListeners() {

        binding.imgChevronLeft.setOnClickListener(this);
        binding.btnChangePassword.setOnClickListener(this);
        binding.imgShowCurrentPassword.setOnClickListener(this);
        binding.imgShowNewPassword.setOnClickListener(this);
        binding.imgShowConfirmPassword.setOnClickListener(this);

    }

    private void setupOnFocusListeners() {

        binding.etCurrentPassword.setOnFocusChangeListener(this);
        binding.etNewPassword.setOnFocusChangeListener(this);
        binding.etConfirmPassword.setOnFocusChangeListener(this);

    }

    private void setupOnKeyListeners() {
        binding.etCurrentPassword.setOnKeyListener(this);
        binding.etNewPassword.setOnKeyListener(this);
        binding.etConfirmPassword.setOnKeyListener(this);
    }



    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.img_chevron_left) {
            getParentFragmentManager().popBackStack();
        } else if (view.getId() == R.id.btn_change_password) {
            if (isValidCurrentPassword() && isValidNewPassword() && isValidConfirmPassword()) {
                backPressedCallback.setEnabled(true);
                view.setEnabled(false);
                binding.imgChevronLeft.setEnabled(false);

                callApiChangePassword();
            } else {
                Snackbar.make(binding.getRoot(), "Invalid input", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(getResources().getColor(
                                R.color.red_400, null
                        ))
                        .show();
            }
        } else if (view.getId() == R.id.img_show_current_password) {
            processClickShowPassword(binding.etCurrentPassword, binding.imgShowCurrentPassword);
        } else if (view.getId() == R.id.img_show_new_password) {
            processClickShowPassword(binding.etNewPassword, binding.imgShowNewPassword);
        } else if (view.getId() == R.id.img_show_confirm_password) {
            processClickShowPassword(binding.etConfirmPassword, binding.imgShowConfirmPassword);
        }
    }




    private void processClickShowPassword(EditText editText, ImageView imageView) {
        if (editText.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
            editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            imageView.setImageResource(R.drawable.ic_eye_off);
        } else {
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            imageView.setImageResource(R.drawable.ic_eye_on);
        }
    }


    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (view.getId() == R.id.et_current_password) {
            if (hasFocus) {
                checkEditTextCurrentPassword(colorPurple400);
            } else {
                checkEditTextCurrentPassword(colorWhite200);
            }
        } else if (view.getId() == R.id.et_new_password) {
            if (hasFocus) {
                checkEditTextNewPassword(colorPurple400);
            } else {
                checkEditTextNewPassword(colorWhite200);
            }
        } else if (view.getId() == R.id.et_confirm_password) {
            if (hasFocus) {
                checkEditTextConfirmPassword(colorPurple400);
            } else {
                checkEditTextConfirmPassword(colorWhite200);
            }
        }
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SPACE &&
                (view.getId() == R.id.et_current_password ||
                        view.getId() == R.id.et_new_password ||
                        view.getId() == R.id.et_confirm_password )
        ) {
            return true;
        }
        if (view.getId() == R.id.et_current_password) {
            checkEditTextCurrentPassword(colorPurple400);
        } else if (view.getId() == R.id.et_new_password) {
            checkEditTextNewPassword(colorPurple400);
        } else if (view.getId() == R.id.et_confirm_password) {
            checkEditTextConfirmPassword(colorPurple400);
        }
        return false;
    }

    private boolean isValidCurrentPassword() {
        String currentPassword = binding.etCurrentPassword.getText().toString().trim();
        return !currentPassword.isBlank();
    }

    private boolean isValidNewPassword() {

        String newPassword = binding.etNewPassword.getText().toString().trim();

        return StringUtil.patternMatches(
                newPassword,
                Constants.PASSWORD_PATTERN);
    }

    private boolean isValidConfirmPassword() {
        String newPassword = binding.etNewPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();
        return confirmPassword.equals(newPassword);
    }


    private void checkEditTextCurrentPassword(int isValidColor) {
        WrapperEditTextState.Builder builder = new WrapperEditTextState.Builder()
                .editText(binding.etCurrentPassword)
                .tvLabel(binding.tvCurrentPassword)
                .tvSupport(binding.tvCurrentPasswordSupport);;

        if (!isValidCurrentPassword()) {
            builder.color(colorPink200);
            builder.visibilityOfTvSupport(View.VISIBLE);
            builder.background(getResources().getDrawable(
                    R.drawable.error_filled_black_text_field,
                    null));

        } else {
            builder.color(isValidColor);
            builder.visibilityOfTvSupport(View.GONE);
            builder.background(getResources().getDrawable(
                    R.drawable.filled_black_text_field,
                    null));
        }
        setStateForEditText(builder.build());
    }

    private void checkEditTextNewPassword(int isValidColor) {
        WrapperEditTextState.Builder builder = new WrapperEditTextState.Builder()
                .editText(binding.etNewPassword)
                .tvLabel(binding.tvNewPassword)
                .tvSupport(binding.tvNewPasswordSupport);;

        if (!isValidNewPassword()) {
            builder.color(colorPink200);
            builder.visibilityOfTvSupport(View.VISIBLE);
            builder.background(getResources().getDrawable(
                    R.drawable.error_filled_black_text_field,
                    null));

        } else {
            builder.color(isValidColor);
            builder.visibilityOfTvSupport(View.GONE);
            builder.background(getResources().getDrawable(
                    R.drawable.filled_black_text_field,
                    null));
        }
        setStateForEditText(builder.build());
    }

    private void checkEditTextConfirmPassword(int isValidColor) {
        WrapperEditTextState.Builder builder = new WrapperEditTextState.Builder()
                .editText(binding.etConfirmPassword)
                .tvLabel(binding.tvConfirmPassword)
                .tvSupport(binding.tvConfirmPasswordSupport);;

        if (!isValidConfirmPassword()) {
            builder.color(colorPink200);
            builder.visibilityOfTvSupport(View.VISIBLE);
            builder.background(getResources().getDrawable(
                    R.drawable.error_filled_black_text_field,
                    null));

        } else {
            builder.color(isValidColor);
            builder.visibilityOfTvSupport(View.GONE);
            builder.background(getResources().getDrawable(
                    R.drawable.filled_black_text_field,
                    null));
        }
        setStateForEditText(builder.build());
    }


    private void setStateForEditText(WrapperEditTextState wrapperEditTextState) {
        setStateForTextViewLabelAndTextViewSupport(
                wrapperEditTextState.getTvLabel(),
                wrapperEditTextState.getTvSupport(),
                wrapperEditTextState.getColor(),
                wrapperEditTextState.getVisibilityOfTvSupport()
        );
        wrapperEditTextState.getEditText().setTag(
                wrapperEditTextState.isTag()
        );
        wrapperEditTextState.getEditText().setBackground(
                wrapperEditTextState.getBackground()
        );
        wrapperEditTextState.getEditText().setTextColor(
                wrapperEditTextState.getColor()
        );
    }

    private void setStateForTextViewLabelAndTextViewSupport(
            TextView tvLabel,
            TextView tvSupport,
            int color,
            int visibilityOfTvSupport) {
        tvLabel.setTextColor(color);
        tvSupport.setTextColor(color);
        tvSupport.setVisibility(visibilityOfTvSupport);
    }

    private void fadeShow(final View view) {

        // Set the content view to 0% opacity but visible, so that it is
        // visible but fully transparent during the animation.
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity and clear any animation
        // listener set on the view.
        view.animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration)
                .setListener(null);
    }

    private void fadeGone(final View view) {
        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step so it doesn't
        // participate in layout passes.
        view.animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                    }
                });

    }



    private void callApiChangePassword() {
        fadeShow(binding.rlLoadingData);
        String accessToken = SessionManager.getInstance(requireContext()).getAccessToken();
        if (accessToken == null) {
            Snackbar.make(binding.getRoot(), "Redirect to login", Snackbar.LENGTH_SHORT)
                    .show();
        }

        String authenticationHeader = Constants.PREFIX_JWT + " " + accessToken;
        ChangePasswordRequest request = new ChangePasswordRequest(
                binding.etCurrentPassword.getText().toString().trim(),
                binding.etNewPassword.getText().toString().trim(),
                binding.etConfirmPassword.getText().toString().trim()
        );

        apiService.changePassword(authenticationHeader, request)
                .enqueue(new Callback<ApiResponse<String>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                        backPressedCallback.setEnabled(false);
                        fadeGone(binding.rlLoadingData);
                        binding.btnChangePassword.setEnabled(true);
                        binding.imgChevronLeft.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            if (response.body().isStatus()) {
                                Snackbar.make(binding.getRoot(), "Change password success", Snackbar.LENGTH_SHORT)
                                        .setBackgroundTint(
                                                getResources().getColor(
                                                        R.color.green_500, null
                                                )
                                        ).show();
                                binding.etCurrentPassword.setText("");
                                binding.etNewPassword.setText("");
                                binding.etConfirmPassword.setText("");
                                new Handler().postDelayed(() -> {
                                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                                }, 2000);
                            } else {
                                Snackbar.make(binding.getRoot(), response.body().getData(), Snackbar.LENGTH_SHORT)
                                        .setBackgroundTint(
                                                getResources().getColor(
                                                        R.color.red_400, null
                                                )
                                        ).show();
                            }
                        } else {
                            Snackbar.make(binding.getRoot(), "Change password fail", Snackbar.LENGTH_SHORT)
                                    .setBackgroundTint(
                                            getResources().getColor(
                                                    R.color.red_400, null
                                            )
                                    ).show();
                            try {
                                String error = response.errorBody().string();
                                Log.e(TAG, "onResponse, isnt successful; code: " + response.code() +
                                        "; message: " + error);
                            } catch (IOException e) {
                                Log.e(TAG, "onResponse, isnt successful: " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                        backPressedCallback.setEnabled(false);
                        fadeGone(binding.rlLoadingData);
                        binding.btnChangePassword.setEnabled(true);
                        binding.imgChevronLeft.setEnabled(true);
                        Snackbar.make(binding.getRoot(), "Change password fail", Snackbar.LENGTH_SHORT)
                                .setBackgroundTint(
                                        getResources().getColor(
                                                R.color.red_400, null
                                        )
                                ).show();
                        Log.e(TAG, "onFailure: " + t.getMessage());
                    }
                });

    }

    private void disableCopyPasteEditText(EditText editText) {
        // Source - https://stackoverflow.com/a
        // Posted by Hardik
        // Retrieved 2025-12-09, License - CC BY-SA 3.0

        editText.setLongClickable(false);

        if (android.os.Build.VERSION.SDK_INT < 11) {
            editText.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

                @Override
                public void onCreateContextMenu(ContextMenu menu, View v,
                                                ContextMenu.ContextMenuInfo menuInfo) {
                    // TODO Auto-generated method stub
                    menu.clear();
                }
            });
        } else {
            editText.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    // TODO Auto-generated method stub
                    return false;
                }

                public void onDestroyActionMode(ActionMode mode) {
                    // TODO Auto-generated method stub

                }

                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    // TODO Auto-generated method stub
                    return false;
                }

                public boolean onActionItemClicked(ActionMode mode,
                                                   MenuItem item) {
                    // TODO Auto-generated method stub
                    return false;
                }
            });
        }

    }


}