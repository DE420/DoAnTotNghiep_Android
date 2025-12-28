package com.example.fitnessapp.fragment;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.Okio;
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

    private static final long MAX_UPLOAD_SIZE = 50L * 1024 * 1024; // 50MB

    private boolean isEdit = false;

    private int colorPurple400, colorWhite200, colorPink200, colorGreen500, colorRed400;
    private int colorYellow;
    private ApiService apiService;


    private OnBackPressedCallback backPressedCallback;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK
                                && result.getData() != null) {

                            Uri uri = result.getData().getData();
                            if (uri == null) return;

                            // Persist permission for API < 33
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                                requireContext().getContentResolver()
                                        .takePersistableUriPermission(
                                                uri,
                                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        );
                            }
                            mUri = uri;
                            Log.e(TAG, "get image ok");
                            Glide.with(this)
                                    .load(mUri)
                                    .error(R.drawable.img_user_default_128)
                                    .into(binding.imgAvatar);
                        }
                        binding.viewEditImageAvatar.setEnabled(true);
                    }
            );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        backPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                // Do nothing → back button disabled
                Toast.makeText(requireActivity(), "Please wait...", Toast.LENGTH_SHORT).show();
            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(this, backPressedCallback);

    }

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




        initViews();
        setupClickListeners();
        setDataToContentView();

        colorYellow = getResources().getColor(R.color.yellow, null);
        colorPurple400 = getResources().getColor(R.color.purple_400, null);
        colorWhite200 = getResources().getColor(R.color.white_200, null);
        colorPink200 = getResources().getColor(R.color.pink_200, null);
        colorGreen500 = getResources().getColor(R.color.green_500, null);
        colorRed400 = getResources().getColor(R.color.red_400, null);


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
        binding.rlLoadingData.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.img_chevron_left) {
            getParentFragmentManager().popBackStack();
        } else if (view.getId() == R.id.img_calendar) {
            datePickerDialog.show();
        } else if (view.getId() == R.id.view_edit_image_avatar) {
            view.setEnabled(false);
            openImagePicker();
        } else if (view.getId() == R.id.btn_update) {
            if (isValidFullName() && isValidDateOfBirth()
                && isValidWeight() && isValidHeight()
                && isValidSpinnerSelection(binding.spnActivityLevel)
                && isValidSpinnerSelection(binding.spnFitnessGoal)) {
                backPressedCallback.setEnabled(true);
                view.setEnabled(false);
                binding.imgChevronLeft.setEnabled(false);
                try {
                    callApiUpdateProfile();
                } catch (FileNotFoundException ex) {
                    Snackbar.make(
                            binding.fragmentEditProfile,
                                    "Image not found",
                                    Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(colorRed400)
                            .show();
                } catch (IOException e) {
                    Snackbar.make(
                            binding.fragmentEditProfile,
                                    "Fail to read image",
                                    Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(colorRed400)
                            .show();
                }
            } else {
//                Toast.makeText(requireContext(), "Invalid input", Toast.LENGTH_SHORT).show();
                Snackbar.make(
                        binding.fragmentEditProfile,
                        "Invalid input",
                        Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(colorRed400)
                        .show();
            }
        } else if (view.getId() == R.id.rl_loading_data) {

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
                checkSpinner(
                        binding.spnActivityLevel,
                        binding.tvActivityLevel,
                        binding.tvActivityLevelSupport,
                        colorWhite200,
                        View.GONE
                );
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                checkSpinner(
                        binding.spnActivityLevel,
                        binding.tvActivityLevel,
                        binding.tvActivityLevelSupport,
                        colorWhite200,
                        View.GONE
                );
            }
        });


        fitnessGoalList = new ArrayList<>(Arrays.asList(FitnessGoal.values()));
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
                checkSpinner(
                        binding.spnFitnessGoal,
                        binding.tvGoal,
                        binding.tvGoalSupport,
                        colorWhite200,
                        View.GONE
                );
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                checkSpinner(
                        binding.spnFitnessGoal,
                        binding.tvGoal,
                        binding.tvGoalSupport,
                        colorWhite200,
                        View.GONE
                );
            }
        });

        DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, year, month, day) -> {
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
            checkEditTextDateOfBirth(colorWhite200, View.GONE);
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
        binding.spnActivityLevel.setOnFocusChangeListener(this);
        binding.spnFitnessGoal.setOnFocusChangeListener(this);


        binding.etFullName.setOnKeyListener(this);
        binding.etDateOfBirth.setOnKeyListener(this);
        binding.etWeight.setOnKeyListener(this);
        binding.etHeight.setOnKeyListener(this);


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
        } else {
            checkSpinner(
                    binding.spnActivityLevel,
                    binding.tvActivityLevel,
                    binding.tvActivityLevelSupport,
                    colorWhite200,
                    View.GONE
            );
        }

        if (updateProfileRequest.getFitnessGoal() != null) {
            String strCurrentGoal = getString(updateProfileRequest.getFitnessGoal().getResId());
            for (int i = 0; i < fitnessGoalList.size(); i++) {
                if (strCurrentGoal.equals(getString(fitnessGoalList.get(i).getResId()))) {
                    binding.spnFitnessGoal.setSelection(i+1);
                    break;
                }
            }
        } else {
            checkSpinner(
                    binding.spnFitnessGoal,
                    binding.tvGoal,
                    binding.tvGoalSupport,
                    colorWhite200,
                    View.GONE
            );
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

        Log.e(TAG, "Selected position fitness goal: " + binding.spnFitnessGoal.getSelectedItemPosition());
    }

    private void openImagePicker() {
        Intent intent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        } else {
            // Android 4.4 – 12
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
        }

        imagePickerLauncher.launch(intent);
    }



    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (view.getId() == R.id.et_full_name) {
            if (hasFocus) {
                checkEditTextFullName(colorYellow, View.VISIBLE);
            } else {
                checkEditTextFullName(colorWhite200, View.GONE);
            }
        } else if (view.getId() == R.id.et_date_of_birth) {
            if (hasFocus) {
                checkEditTextDateOfBirth(colorYellow, View.VISIBLE);
            } else {
                checkEditTextDateOfBirth(colorWhite200, View.GONE);
            }
        } else if (view.getId() == R.id.et_weight) {
            if (hasFocus) {
                checkEditTextWeight(colorYellow, View.VISIBLE);
            } else {
                checkEditTextWeight(colorWhite200, View.GONE);
            }
        } else if (view.getId() == R.id.et_height) {
            if (hasFocus) {
                checkEditTextHeight(colorYellow, View.VISIBLE);
            } else {
                checkEditTextHeight(colorWhite200, View.GONE);
            }
        }
    }

    private void checkSpinner(
            Spinner spinner,
            TextView tvLabel,
            TextView tvSupport,
            int isValidColor,
            int isValidVisibility) {
        if (isValidSpinnerSelection(spinner)) {
            setStateForTextViewLabelAndTextViewSupport(
                    tvLabel,
                    tvSupport,
                    isValidColor,
                    isValidVisibility
            );
        } else {
            setStateForTextViewLabelAndTextViewSupport(
                    tvLabel,
                    tvSupport,
                    colorPink200,
                    View.VISIBLE
            );
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
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

    @SuppressLint("UseCompatLoadingForDrawables")
    private void checkEditTextDateOfBirth(int isValidColor, int isValidVisibility) {
        WrapperEditTextState.Builder builder = new WrapperEditTextState.Builder()
                .editText(binding.etDateOfBirth)
                .tvLabel(binding.tvDateOfBirth)
                .tvSupport(binding.tvDateOfBirthSupport);
        if (!isValidDateOfBirth()) {
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

    @SuppressLint("UseCompatLoadingForDrawables")
    private void checkEditTextWeight(int isValidColor, int isValidVisibility) {
        WrapperEditTextState.Builder builder = new WrapperEditTextState.Builder()
                .editText(binding.etWeight)
                .tvLabel(binding.tvWeight)
                .tvSupport(binding.tvWeightSupport);
        if (!isValidWeight()) {
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

    @SuppressLint("UseCompatLoadingForDrawables")
    private void checkEditTextHeight(int isValidColor, int isValidVisibility) {
        WrapperEditTextState.Builder builder = new WrapperEditTextState.Builder()
                .editText(binding.etHeight)
                .tvLabel(binding.tvHeight)
                .tvSupport(binding.tvHeightSupport);
        if (!isValidHeight()) {
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

    private boolean isValidSpinnerSelection(Spinner spinner) {
        return spinner.getSelectedItem() != null;
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

    private void callApiUpdateProfile() throws FileNotFoundException, IOException {
        binding.rlLoadingData.setVisibility(View.VISIBLE);
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

            byte[] imageBytes = compressImage(mUri);

            if (imageBytes.length > MAX_UPLOAD_SIZE) {
                Snackbar.make(
                        binding.fragmentEditProfile,
                        "Image too large (max 50MB)",
                        Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(colorRed400)
                        .show();
                return;
            }

            RequestBody requestBodyAvt =
                    RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);

            MultipartBody.Part multipartBodyAvt =
                    MultipartBody.Part.createFormData(
                            UpdateProfileRequest.KEY_AVATAR_FILE,
                            "avatar.jpg",
                            requestBodyAvt
                    );


//            String strRealPath = RealPathUtil.getRealPath(requireContext(), mUri);
//            File file = new File(strRealPath);
//            RequestBody requestBodyAvt = RequestBody.create(mediaType, file);
//            MultipartBody.Part multipartBodyAvt = MultipartBody.Part.createFormData(
//                    UpdateProfileRequest.KEY_AVATAR_FILE, file.getName(), requestBodyAvt);


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
        } else if (updateProfileRequest.getAvatar() != null) {
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
        } else {
            apiService.updateUserProfile(
                    authorizationHeader,
                    requestBodyName,
                    requestBodyWeight,
                    requestBodyHeight,
                    requestBodyActivityLevel,
                    requestBodyFitnessGoal,
                    requestBodyDateOfBirth
            ).enqueue(new UpdateProfileCallback());
        }

    }

    private byte[] compressImage(Uri uri) throws IOException {
        Bitmap bitmap = BitmapFactory.decodeStream(
                requireContext().getContentResolver().openInputStream(uri)
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int quality = 90;
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);

        while (out.size() > MAX_UPLOAD_SIZE && quality > 30) {
            out.reset();
            quality -= 10;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
        }

        return out.toByteArray();
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
            checkEditTextFullName(colorYellow, View.VISIBLE);
        } else if (view.getId() == R.id.et_date_of_birth) {
            checkEditTextDateOfBirth(colorYellow, View.VISIBLE);
        } else if (view.getId() == R.id.et_weight) {
            checkEditTextWeight(colorYellow, View.VISIBLE);
        } else if (view.getId() == R.id.et_height) {
            checkEditTextHeight(colorYellow, View.VISIBLE);
        }
        return false;
    }

    private class UpdateProfileCallback implements Callback<ApiResponse<Boolean>> {

        @Override
        public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
            backPressedCallback.setEnabled(false);
            binding.btnUpdate.setEnabled(true);
            binding.imgChevronLeft.setEnabled(true);
            binding.rlLoadingData.setVisibility(View.GONE);
            if (response.isSuccessful() && response.body() != null) {
                if (response.body().isStatus()) {
//                    Toast.makeText(requireContext(), "Update profile successes", Toast.LENGTH_SHORT).show();
                    Snackbar.make(
                            binding.fragmentEditProfile,
                            "Update profile success\nRedirect to profile",
                            Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(colorGreen500)
                            .show();
                    new Handler().postDelayed(() -> {
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    }, 1250);

                } else {
//                    Toast.makeText(requireContext(), "Update profile fail", Toast.LENGTH_SHORT).show();
                    Snackbar.make(
                            binding.fragmentEditProfile,
                            "Update profile fail",
                            Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(colorRed400)
                            .show();

                    Log.e(TAG, "onResponse, isSuccessfull but isStatus false, code: " +
                            response.code() + ", data: " + response.body().getData());
                }
            } else {
//                Toast.makeText(requireContext(), "Update profile fail", Toast.LENGTH_SHORT).show();
                if (response.code() == 401 ) {
                    Snackbar.make(
                            binding.fragmentEditProfile,
                            "Redirect to login",
                            Snackbar.LENGTH_SHORT)
                            .show();
                }

                Snackbar.make(
                        binding.fragmentEditProfile,
                                "Update profile fail",
                                Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(colorRed400)
                        .show();
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
            backPressedCallback.setEnabled(false);
            binding.btnUpdate.setEnabled(true);
            binding.imgChevronLeft.setEnabled(true);
            binding.rlLoadingData.setVisibility(View.GONE);
            Snackbar.make(binding.fragmentEditProfile,
                    "Update profile fail",
                    Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(colorRed400)
                    .show();
            Log.e(TAG, "onFailure: " + t.getMessage());

        }
    }


}