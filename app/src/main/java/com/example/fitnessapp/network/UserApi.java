package com.example.fitnessapp.network;

import com.example.fitnessapp.model.request.ChangePasswordRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.user.ProfileResponse;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public interface UserApi {

    /**
     * Get user profile
     * Authorization header is automatically added by AuthInterceptor
     * @return User profile data
     */
    @GET("user/profile")
    Call<ApiResponse<ProfileResponse>> getUserProfile();

    /**
     * Update user profile with avatar file
     * Authorization header is automatically added by AuthInterceptor
     * @param avatarFile Avatar image file (optional)
     * @param fields Profile fields to update
     * @return Success status
     */
    @Multipart
    @PUT("user/profile")
    Call<ApiResponse<Boolean>> updateUserProfile(
            @Part MultipartBody.Part avatarFile,
            @PartMap Map<String, RequestBody> fields
    );

    /**
     * Update user profile without avatar file
     * Authorization header is automatically added by AuthInterceptor
     * @param fields Profile fields to update
     * @return Success status
     */
    @Multipart
    @PUT("user/profile")
    Call<ApiResponse<Boolean>> updateUserProfile(
            @PartMap Map<String, RequestBody> fields
    );

    /**
     * Change user password
     * Authorization header is automatically added by AuthInterceptor
     * @param request Change password request
     * @return Success message
     */
    @POST("user/change-password")
    Call<ApiResponse<String>> changePassword(@Body ChangePasswordRequest request);
}
