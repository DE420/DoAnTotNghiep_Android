package com.example.fitnessapp.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.fitnessapp.model.request.RefreshTokenRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.LoginResponse;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.network.RetrofitClient;

import java.io.IOException;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SessionManager {

    public static final String TAG = SessionManager.class.getPackage() +
             "." + SessionManager.class.getSimpleName();

    private static final String PREF_NAME = "APP_PREFS";
    private static final String KEY_ACCESS_TOKEN = "ACCESS_TOKEN";
    private static final String KEY_REFRESH_TOKEN = "REFRESH_TOKEN";

    private static SessionManager instance;
    private final SharedPreferences sharedPreferences;

    // Constructor là private để ngăn tạo instance từ bên ngoài
    private SessionManager(Context context) {
        // Sử dụng getApplicationContext() để tránh memory leak từ Activity context
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Phương thức để lấy instance duy nhất của class (Singleton)
    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    /**
     * Lưu cả access token và refresh token
     */
    public void saveTokens(String accessToken, String refreshToken) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.apply();
    }

    /**
     * Lấy Access Token
     * @return Access Token hoặc null nếu không tồn tại
     */
    public String getAccessToken() {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    /**
     * Lấy Refresh Token
     * @return Refresh Token hoặc null nếu không tồn tại
     */
    public String getRefreshToken() {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
    }

    /**
     * Kiểm tra xem người dùng đã đăng nhập hay chưa
     * @return true nếu có access token, ngược lại là false
     */
    public boolean isLoggedIn() {
        String accessToken = getAccessToken();
        return accessToken != null && !accessToken.isEmpty();
    }

    /**
     * Xóa dữ liệu session (dùng khi logout):
     * - access token
     * - refresh token
     *
     */
//    public void logout() {
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.clear();
//        editor.apply();
//    }

    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_ACCESS_TOKEN);
        editor.remove(KEY_REFRESH_TOKEN);
        editor.apply();
    }



    public boolean refreshToken() {
        String refreshToken = getRefreshToken();
        if (refreshToken == null) {
            return false;
        }
        final boolean[] isSuccess = {false};
        ApiService apiService = RetrofitClient.getApiService();
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        apiService.refreshToken(request).enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                if (response.isSuccessful() && response.body() != null ) {
                    ApiResponse<LoginResponse> apiResponse = response.body();
                    saveTokens(apiResponse.getData().getAccessToken(),
                            apiResponse.getData().getRefreshToken());
                    isSuccess[0] = true;
                } else {
                    int codeError = response.code();
                    String strError = null;
                    try {
                        strError = response.errorBody().string();
                    } catch (IOException e) {
                        Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                    }
                    Log.e(TAG, "error code: " + codeError + ", error message: " + strError);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                Log.e(TAG, Objects.requireNonNull(t.getMessage()));

            }
        });
        return isSuccess[0];
    }
}