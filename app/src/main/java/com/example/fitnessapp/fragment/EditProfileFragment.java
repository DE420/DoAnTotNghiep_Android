package com.example.fitnessapp.fragment;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.fitnessapp.R;
import com.example.fitnessapp.adapter.ActivityLevelAdapter;
import com.example.fitnessapp.adapter.CustomSpinnerAdapter;
import com.example.fitnessapp.adapter.FitnessGoalAdapter;
import com.example.fitnessapp.constants.Constants;
import com.example.fitnessapp.databinding.FragmentEditProfileBinding;
import com.example.fitnessapp.enums.ActivityLevel;
import com.example.fitnessapp.enums.FitnessGoal;
import com.example.fitnessapp.model.WrapperEditTextState;
import com.example.fitnessapp.model.request.UpdateProfileRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.network.RetrofitClient;
import com.example.fitnessapp.session.SessionManager;
import com.example.fitnessapp.utils.DateUtil;
import com.example.fitnessapp.utils.RealPathUtil;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener, View.OnKeyListener {

    public static final String TAG = EditProfileFragment.class.getName();
    private FragmentEditProfileBinding binding;
    private UpdateProfileRequest updateProfileRequest;
    private List<ActivityLevel> activityLevelList;
    private List<FitnessGoal> fitnessGoalList;
    private DatePickerDialog datePickerDialog;

    private Uri mUri;
    private boolean isEdit = false;

    private int colorPurple400, colorWhite200, colorPink200;
    private ApiService apiService;

    private int shortAnimationDuration;


    private ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.e(TAG, "onActivityResult");
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data == null) {
                            return;
                        }
                        Uri uri = data.getData();
                        mUri = uri;

                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
                            binding.imgAvatar.setImageBitmap(bitmap);
                            isEdit = true;
                        } catch (IOException e) {
                            Glide.with(binding.fragmentEditProfile)
                                    .load(updateProfileRequest.getAvatar())
                                    .error(R.drawable.img_user_default_128)
                                    .into(binding.imgAvatar);
                            Log.e(TAG, "Error when set bitmap img: " + e.getMessage());
                        }

                    }
                    binding.viewEditImageAvatar.setEnabled(true);
                }
            }
    );

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            Snackbar.make(binding.fragmentEditProfile, R.string.read_external_storage_granted,
                                    Snackbar.LENGTH_SHORT).show();
                            openGallery();
                        } else {
                            Snackbar.make(binding.fragmentEditProfile, R.string.read_external_storage_denied,
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    });




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateProfileRequest = (UpdateProfileRequest) requireArguments()
                .getSerializable(UpdateProfileRequest.KEY_UPDATE_PROFILE_REQUEST);

        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        initViews();
        setupClickListeners();
        setDataToContentView();

        colorPurple400 = getResources().getColor(R.color.purple_400, null);
        colorWhite200 = getResources().getColor(R.color.white_200, null);
        colorPink200 = getResources().getColor(R.color.pink_200, null);


        checkEditTextFullName(colorWhite200, View.GONE);
        checkEditTextDateOfBirth(colorWhite200, View.GONE);
        checkEditTextWeight(colorWhite200, View.GONE);
        checkEditTextHeight(colorWhite200, View.GONE);

        apiService = RetrofitClient.getApiService();
    }

    private void setupClickListeners() {
        binding.imgChevronLeft.setOnClickListener(this);
        binding.imgCalendar.setOnClickListener(this);
        binding.btnUpdate.setOnClickListener(this);
        binding.viewEditImageAvatar.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.img_chevron_left) {
            getParentFragmentManager().popBackStack();
        } else if (view.getId() == R.id.img_calendar) {
            datePickerDialog.show();
        } else if (view.getId() == R.id.view_edit_image_avatar) {
            view.setEnabled(false);
            onClickRequestPermission();
        } else if (view.getId() == R.id.btn_update) {
            if (isValidFullName() && isValidDateOfBirth()
                && isValidWeight() && isValidHeight()) {
                view.setEnabled(false);
                binding.imgChevronLeft.setEnabled(false);
                callApiUpdateProfile();
            } else {
//                Toast.makeText(requireContext(), "Invalid input", Toast.LENGTH_SHORT).show();
                Snackbar.make(binding.fragmentEditProfile, "Invalid input", Snackbar.LENGTH_SHORT).show();
            }
        }
    }



    private void initViews() {

        activityLevelList = new ArrayList<ActivityLevel>(Arrays.asList(ActivityLevel.values()));
        ActivityLevelAdapter activityLevelAdapter =
                new ActivityLevelAdapter(requireContext(), activityLevelList);
//        activityLevelAdapter.setDropDownViewResource(R.drawable.background_item_profile);
        binding.spnActivityLevel.setAdapter(activityLevelAdapter);

        binding.spnActivityLevel.setPopupBackgroundDrawable(
                getResources().getDrawable(R.drawable.background_item_profile, null));

        binding.spnActivityLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                activityLevelAdapter.setSelectedPosition(position);
                if (position > 0 && activityLevelAdapter.getSelectedPosition() != position) {
                    isEdit = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });


        fitnessGoalList = new ArrayList<FitnessGoal>(Arrays.asList(FitnessGoal.values()));
        FitnessGoalAdapter fitnessGoalAdapter =
                new FitnessGoalAdapter(requireContext(), fitnessGoalList);
//        fitnessGoalAdapter.setDropDownViewResource(R.drawable.background_item_profile);
        binding.spnFitnessGoal.setAdapter(fitnessGoalAdapter);

        binding.spnFitnessGoal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fitnessGoalAdapter.setSelectedPosition(position);
                if (position > 0 && fitnessGoalAdapter.getSelectedPosition() != position) {
                    isEdit = true;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day)
            {
                month = month + 1;
                String dateStr = "";
                if (day < 10) {
                    dateStr += "0" + day;
                } else {
                    dateStr += day;
                }
                dateStr += "/";
                if (month < 10) {
                    dateStr += "0" + month;
                } else {
                    dateStr += month;
                }
                dateStr += "/" + year;
                binding.etDateOfBirth.setText(dateStr);
            }
        };

        int year, month, day;

        if (updateProfileRequest.getDateOfBirth() == null) {
            Calendar cal = Calendar.getInstance();
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH);
            day = cal.get(Calendar.DAY_OF_MONTH);
        } else {
            String[] arr = updateProfileRequest.getDateOfBirth().split("/");
            try {
                day = Integer.parseInt(arr[0]);
                month = Integer.parseInt(arr[1]);
                year = Integer.parseInt(arr[2]);
            } catch (NumberFormatException e) {
                Calendar cal = Calendar.getInstance();
                year = cal.get(Calendar.YEAR);
                month = cal.get(Calendar.MONTH);
                day = cal.get(Calendar.DAY_OF_MONTH);
            }
        }

        Log.e(TAG, "year: " + year + ", month: " + month + ", day: " + day);

        datePickerDialog = new DatePickerDialog(requireContext(), dateSetListener, year, month-1, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        binding.etFullName.setOnFocusChangeListener(this);
        binding.etDateOfBirth.setOnFocusChangeListener(this);
        binding.etWeight.setOnFocusChangeListener(this);
        binding.etHeight.setOnFocusChangeListener(this);


        binding.etFullName.setOnKeyListener(this);
        binding.etDateOfBirth.setOnKeyListener(this);
        binding.etWeight.setOnKeyListener(this);
        binding.etHeight.setOnKeyListener(this);

    }

    private List<String> getFitnessGoalList() {
        List<String> fitnessGoalList = new ArrayList<>();
        for (FitnessGoal fitnessGoal : FitnessGoal.values()) {
            fitnessGoalList.add(getString(fitnessGoal.getResId()));
        }
        return fitnessGoalList;
    }

    private List<String> getActivityLevelList() {
        List<String> activityLevelList = new ArrayList<>();
        for (ActivityLevel activityLevel : ActivityLevel.values()) {
            activityLevelList.add(getString(activityLevel.getResId()));
        }
        return activityLevelList;
    }

    private void setDataToContentView() {

        Glide.with(requireContext())
                .load(updateProfileRequest.getAvatar())
                .error(R.drawable.img_user_default_128)
                .into(binding.imgAvatar);

        if (updateProfileRequest.getName() != null) {
            binding.etFullName.setText(updateProfileRequest.getName());
        }

        if (updateProfileRequest.getActivityLevel() != null) {
            String strCurrentActivityLevel = getString(updateProfileRequest.getActivityLevel().getResId());
            for (int i = 0; i < activityLevelList.size(); i++) {
                if (strCurrentActivityLevel.equals(getString(activityLevelList.get(i).getResId()))) {
                    binding.spnActivityLevel.setSelection(i+1);
                    break;
                }
            }
        }

        if (updateProfileRequest.getFitnessGoal() != null) {
            String strCurrentGoal = getString(updateProfileRequest.getFitnessGoal().getResId());
            for (int i = 0; i < fitnessGoalList.size(); i++) {
                if (strCurrentGoal.equals(getString(fitnessGoalList.get(i).getResId()))) {
                    binding.spnFitnessGoal.setSelection(i+1);

                    break;
                }
            }
        }

        if (updateProfileRequest.getDateOfBirth() != null) {
            binding.etDateOfBirth.setText(updateProfileRequest.getDateOfBirth());
        }

        if (updateProfileRequest.getWeight() != null) {
            binding.etWeight.setText(String.valueOf(updateProfileRequest.getWeight()));
        }

        if (updateProfileRequest.getHeight() != null) {
            binding.etHeight.setText(String.valueOf(updateProfileRequest.getHeight()));
        }
    }

    private void onClickRequestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            openGallery();
            return;
        }
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(binding.fragmentEditProfile, R.string.read_external_storage_required,
                        Snackbar.LENGTH_INDEFINITE
                ).setAction(
                        R.string.ok, view -> {
                            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                ).show();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

        }
    }


    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        mActivityResultLauncher.launch(Intent.createChooser(intent, "Select Picture"));
        //startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (view.getId() == R.id.et_full_name) {
            if (hasFocus) {
                checkEditTextFullName(colorPurple400, View.VISIBLE);
            } else {
                checkEditTextFullName(colorWhite200, View.GONE);
            }
        } else if (view.getId() == R.id.et_date_of_birth) {
            if (hasFocus) {
                checkEditTextDateOfBirth(colorPurple400, View.VISIBLE);
            } else {
                checkEditTextDateOfBirth(colorWhite200, View.GONE);
            }
        } else if (view.getId() == R.id.et_weight) {
            if (hasFocus) {
                checkEditTextWeight(colorPurple400, View.VISIBLE);
            } else {
                checkEditTextWeight(colorWhite200, View.GONE);
            }
        } else if (view.getId() == R.id.et_height) {
            if (hasFocus) {
                checkEditTextHeight(colorPurple400, View.VISIBLE);
            } else {
                checkEditTextHeight(colorWhite200, View.GONE);
            }
        }
    }

    private void checkEditTextFullName(int isValidColor, int isValidVisibility) {
        WrapperEditTextState.Builder builder = new WrapperEditTextState.Builder()
                .editText(binding.etFullName)
                .tvLabel(binding.tvFullName)
                .tvSupport(binding.tvFullNameSupport);
        if (!isValidFullName()) {
            builder.color(colorPink200);
            builder.visibilityOfTvSupport(View.VISIBLE);
//            wrapperEditTextState.setTag(false);
            builder.background(getResources().getDrawable(
                    R.drawable.error_filled_black_text_field,
                    null));

        } else {
            builder.color(isValidColor);
            builder.visibilityOfTvSupport(isValidVisibility);
//            wrapperEditTextState.setTag(true);
            builder.background(getResources().getDrawable(
                    R.drawable.filled_black_text_field,
                    null));
        }
        setStateForEditText(builder.build());
    }

    private void checkEditTextDateOfBirth(int isValidColor, int isValidVisibility) {
        WrapperEditTextState.Builder builder = new WrapperEditTextState.Builder()
                .editText(binding.etDateOfBirth)
                .tvLabel(binding.tvDateOfBirth)
                .tvSupport(binding.tvDateOfBirthSupport);
        if (!isValidFullName()) {
            builder.color(colorPink200);
            builder.visibilityOfTvSupport(View.VISIBLE);
//            wrapperEditTextState.setTag(false);
            builder.background(getResources().getDrawable(
                    R.drawable.error_filled_black_text_field,
                    null));

        } else {
            builder.color(isValidColor);
            builder.visibilityOfTvSupport(isValidVisibility);
//            wrapperEditTextState.setTag(true);
            builder.background(getResources().getDrawable(
                    R.drawable.filled_black_text_field,
                    null));
        }
        setStateForEditText(builder.build());
    }

    private void checkEditTextWeight(int isValidColor, int isValidVisibility) {
        WrapperEditTextState.Builder builder = new WrapperEditTextState.Builder()
                .editText(binding.etWeight)
                .tvLabel(binding.tvWeight)
                .tvSupport(binding.tvWeightSupport);
        if (!isValidFullName()) {
            builder.color(colorPink200);
            builder.visibilityOfTvSupport(View.VISIBLE);
//            wrapperEditTextState.setTag(false);
            builder.background(getResources().getDrawable(
                    R.drawable.error_filled_black_text_field,
                    null));

        } else {
            builder.color(isValidColor);
            builder.visibilityOfTvSupport(isValidVisibility);
//            wrapperEditTextState.setTag(true);
            builder.background(getResources().getDrawable(
                    R.drawable.filled_black_text_field,
                    null));
        }
        setStateForEditText(builder.build());
    }

    private void checkEditTextHeight(int isValidColor, int isValidVisibility) {
        WrapperEditTextState.Builder builder = new WrapperEditTextState.Builder()
                .editText(binding.etHeight)
                .tvLabel(binding.tvHeight)
                .tvSupport(binding.tvHeightSupport);
        if (!isValidFullName()) {
            builder.color(colorPink200);
            builder.visibilityOfTvSupport(View.VISIBLE);
//            wrapperEditTextState.setTag(false);
            builder.background(getResources().getDrawable(
                    R.drawable.error_filled_black_text_field,
                    null));

        } else {
            builder.color(isValidColor);
            builder.visibilityOfTvSupport(isValidVisibility);
//            wrapperEditTextState.setTag(true);
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

    private void setTextColor(TextView textView, int color) {
        textView.setTextColor(color);
    }

    private void setViewVisibility(View view, int visibility) {
        view.setVisibility(visibility);
    }

    private boolean isValidFullName() {
        return !binding.etFullName.getText().toString().trim().isEmpty();
    }

    private boolean isValidDateOfBirth() {
        return DateUtil.isValidDate(
                binding.etDateOfBirth.getText().toString().trim(),
                DateUtil.DD_MM_YYYY_DATE_FORMAT);
    }

    private boolean isValidWeight() {
        return isGreaterThanZero(binding.etWeight.getText().toString().trim());
    }

    private boolean isValidHeight() {
        return isGreaterThanZero(binding.etHeight.getText().toString().trim());
    }

    private boolean isGreaterThanZero(String numberStr) {
        double number;
        try {
            number = Double.parseDouble(numberStr);
            return number > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void callApiUpdateProfile() {
        fadeShow(binding.rlLoadingData);
        SessionManager sessionManager = SessionManager.getInstance(requireContext());
        String accessToken = sessionManager.getAccessToken();
        if (accessToken == null) {
            Log.e(TAG, "accesstoken null");
            Snackbar.make(binding.fragmentEditProfile, "Redirect to login", Snackbar.LENGTH_SHORT).show();
            return;
//            sessionManager.refreshToken();
//            accessToken = sessionManager.getAccessToken();
        }

        MediaType mediaType = MediaType.parse(Constants.BODY_DATA_FORM);

        String authorizationHeader = Constants.PREFIX_JWT + " " + accessToken;
        RequestBody requestBodyName = RequestBody.create(
                mediaType,
                binding.etFullName.getText().toString()
                );
        RequestBody requestBodyActivityLevel = RequestBody.create(
                mediaType,
                ((ActivityLevel) binding.spnActivityLevel.getSelectedItem()).name()
        );
        RequestBody requestBodyFitnessGoal = RequestBody.create(
                mediaType,
                ((FitnessGoal) binding.spnFitnessGoal.getSelectedItem()).name()
        );
        String dateStr = binding.etDateOfBirth.getText().toString();
        try {
            dateStr = DateUtil.getNewDateString(
                            binding.etDateOfBirth.getText().toString(),
                            DateUtil.DD_MM_YYYY_DATE_FORMAT,
                            DateUtil.YYYY_MM_DD_DATE_FORMAT
                    );
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
        }
        RequestBody requestBodyDateOfBirth = RequestBody.create(
                mediaType,
                dateStr
        );
        Double weight = 0d, height = 0d;
        try {
            weight = Double.parseDouble(
                    binding.etWeight.getText().toString()
            );
            height = Double.parseDouble(
                    binding.etHeight.getText().toString()
            );
        } catch (NumberFormatException e) {
            Log.e(TAG, e.getMessage());
        }
        RequestBody requestBodyWeight = RequestBody.create(
                mediaType,
                String.valueOf(weight)
        );
        RequestBody requestBodyHeight = RequestBody.create(
                mediaType,
                String.valueOf(height)
        );


        if (mUri != null) {
            String strRealPath = RealPathUtil.getRealPath(requireContext(), mUri);
            File file = new File(strRealPath);
            RequestBody requestBodyAvt = RequestBody.create(mediaType, file);
            MultipartBody.Part multipartBodyAvt = MultipartBody.Part.createFormData(
                    UpdateProfileRequest.KEY_AVATAR_FILE, file.getName(), requestBodyAvt);
            apiService.updateUserProfile(
                    authorizationHeader,
                    multipartBodyAvt,
                    requestBodyName,
                    requestBodyWeight,
                    requestBodyHeight,
                    requestBodyActivityLevel,
                    requestBodyFitnessGoal,
                    requestBodyDateOfBirth
            ).enqueue(new UpdateProfileCallback());
        } else {
            RequestBody requestBodyAvatar = RequestBody.create(
                    mediaType,
                    updateProfileRequest.getAvatar()
            );
//            Log.e(TAG, "authorization: " + authorizationHeader
//            + ", avatar: " + updateProfileRequest.getAvatar()
//            + ", name");
            apiService.updateUserProfile(
                    authorizationHeader,
                    requestBodyAvatar,
                    requestBodyName,
                    requestBodyWeight,
                    requestBodyHeight,
                    requestBodyActivityLevel,
                    requestBodyFitnessGoal,
                    requestBodyDateOfBirth
            ).enqueue(new UpdateProfileCallback());
        }

    }


    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {
        if (view.getId() == R.id.et_full_name ||
            view.getId() == R.id.et_date_of_birth ||
            view.getId() == R.id.et_weight ||
            view.getId() == R.id.et_height) {
            isEdit = true;
        }
        if (view.getId() == R.id.et_full_name) {
            checkEditTextFullName(colorPurple400, View.VISIBLE);
        } else if (view.getId() == R.id.et_date_of_birth) {
            checkEditTextDateOfBirth(colorPurple400, View.VISIBLE);
        } else if (view.getId() == R.id.et_weight) {
            checkEditTextWeight(colorPurple400, View.VISIBLE);
        } else if (view.getId() == R.id.et_height) {
            checkEditTextHeight(colorPurple400, View.VISIBLE);
        }
        return false;
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

    private class UpdateProfileCallback implements Callback<ApiResponse<Boolean>> {

        @Override
        public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
            binding.btnUpdate.setEnabled(true);
            binding.imgChevronLeft.setEnabled(true);
            fadeGone(binding.rlLoadingData);
            if (response.isSuccessful() && response.body() != null) {
                if (response.body().isStatus()) {
//                    Toast.makeText(requireContext(), "Update profile successes", Toast.LENGTH_SHORT).show();

                    Snackbar.make(binding.fragmentEditProfile, "Update profile success", Snackbar.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                } else {
//                    Toast.makeText(requireContext(), "Update profile fail", Toast.LENGTH_SHORT).show();
                    Snackbar.make(binding.fragmentEditProfile, "Update profile fail", Snackbar.LENGTH_SHORT).show();

                    Log.e(TAG, "onResponse, isSuccessfull but isStatus false, code: " +
                            response.code() + ", data: " + response.body().getData());
                }
            } else {
//                Toast.makeText(requireContext(), "Update profile fail", Toast.LENGTH_SHORT).show();
                if (response.code() == 401 ) {
                    Snackbar.make(binding.fragmentEditProfile, "Redirect to login", Snackbar.LENGTH_SHORT).show();
                }
                try {
                    Log.e(TAG, "onResponse but isn't successful, code: " + response.code()
                          + ", errorBody: " + response.errorBody().string());
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }

        }

        @Override
        public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
            binding.btnUpdate.setEnabled(true);
            binding.imgChevronLeft.setEnabled(true);
            fadeGone(binding.rlLoadingData);
            Snackbar.make(binding.fragmentEditProfile, "Update profile fail", Snackbar.LENGTH_SHORT).show();
            Log.e(TAG, "onFailure: " + t.getMessage());
        }
    }


}