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

    // User Profile Keys
    private static final String KEY_USER_ID = "USER_ID";
    private static final String KEY_USER_NAME = "USER_NAME";
    private static final String KEY_USER_USERNAME = "USER_USERNAME";
    private static final String KEY_USER_EMAIL = "USER_EMAIL";
    private static final String KEY_USER_AVATAR = "USER_AVATAR";
    private static final String KEY_USER_SEX = "USER_SEX";
    private static final String KEY_USER_WEIGHT = "USER_WEIGHT";
    private static final String KEY_USER_HEIGHT = "USER_HEIGHT";
    private static final String KEY_USER_DOB = "USER_DOB";

    // FCM Token Key
    private static final String KEY_FCM_TOKEN = "fcm_token";

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

    /**
     * Save user profile data to local storage
     * @param userId User ID
     * @param name User's name
     * @param username Username
     * @param email Email
     * @param avatar Avatar URL
     * @param sex Gender
     * @param weight Weight
     * @param height Height
     * @param dateOfBirth Date of birth
     */
    public void saveUserProfile(Long userId, String name, String username, String email,
                                String avatar, String sex, Double weight, Double height,
                                String dateOfBirth) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (userId != null) editor.putLong(KEY_USER_ID, userId);
        if (name != null) editor.putString(KEY_USER_NAME, name);
        if (username != null) editor.putString(KEY_USER_USERNAME, username);
        if (email != null) editor.putString(KEY_USER_EMAIL, email);
        if (avatar != null) editor.putString(KEY_USER_AVATAR, avatar);
        if (sex != null) editor.putString(KEY_USER_SEX, sex);
        if (weight != null) editor.putFloat(KEY_USER_WEIGHT, weight.floatValue());
        if (height != null) editor.putFloat(KEY_USER_HEIGHT, height.floatValue());
        if (dateOfBirth != null) editor.putString(KEY_USER_DOB, dateOfBirth);
        editor.apply();
    }

    /**
     * Update only the avatar URL
     * @param avatarUrl New avatar URL
     */
    public void updateAvatar(String avatarUrl) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_AVATAR, avatarUrl);
        editor.apply();
    }

    /**
     * Get user ID
     * @return User ID or 0 if not found
     */
    public long getUserId() {
        return sharedPreferences.getLong(KEY_USER_ID, 0);
    }

    /**
     * Get user name
     * @return User name or null
     */
    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, null);
    }

    /**
     * Get username
     * @return Username or null
     */
    public String getUsername() {
        return sharedPreferences.getString(KEY_USER_USERNAME, null);
    }

    /**
     * Get email
     * @return Email or null
     */
    public String getEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }

    /**
     * Get avatar URL
     * @return Avatar URL or null
     */
    public String getAvatar() {
        return sharedPreferences.getString(KEY_USER_AVATAR, null);
    }

    /**
     * Get gender
     * @return Gender or null
     */
    public String getSex() {
        return sharedPreferences.getString(KEY_USER_SEX, null);
    }

    /**
     * Get weight
     * @return Weight or 0.0
     */
    public double getWeight() {
        return sharedPreferences.getFloat(KEY_USER_WEIGHT, 0.0f);
    }

    /**
     * Get height
     * @return Height or 0.0
     */
    public double getHeight() {
        return sharedPreferences.getFloat(KEY_USER_HEIGHT, 0.0f);
    }

    /**
     * Get date of birth
     * @return Date of birth or null
     */
    public String getDateOfBirth() {
        return sharedPreferences.getString(KEY_USER_DOB, null);
    }

    /**
     * Clear all user profile data (called during logout)
     */
    public void clearUserProfile() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USER_NAME);
        editor.remove(KEY_USER_USERNAME);
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_USER_AVATAR);
        editor.remove(KEY_USER_SEX);
        editor.remove(KEY_USER_WEIGHT);
        editor.remove(KEY_USER_HEIGHT);
        editor.remove(KEY_USER_DOB);
        editor.apply();
    }

    /**
     * Save FCM token
     * @param token FCM device token
     */
    public void saveFcmToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_FCM_TOKEN, token);
        editor.apply();
    }

    /**
     * Get FCM token
     * @return FCM token or null if not found
     */
    public String getFcmToken() {
        return sharedPreferences.getString(KEY_FCM_TOKEN, null);
    }

    /**
     * Clear FCM token (called during logout)
     */
    public void clearFcmToken() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_FCM_TOKEN);
        editor.apply();
    }

}