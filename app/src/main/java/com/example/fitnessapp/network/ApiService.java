package com.example.fitnessapp.network;

import com.example.fitnessapp.constants.Constants;
import com.example.fitnessapp.enums.DifficultyLevel;
import com.example.fitnessapp.enums.FitnessGoal;
import com.example.fitnessapp.model.request.ChangePasswordRequest;
import com.example.fitnessapp.model.request.CreatePlanRequest;
import com.example.fitnessapp.model.request.ForgotPasswordRequest;
import com.example.fitnessapp.model.request.GoogleLoginRequest;
import com.example.fitnessapp.model.request.LogWorkoutRequest;
import com.example.fitnessapp.model.request.LoginRequest;
import com.example.fitnessapp.model.request.LogoutRequest;
import com.example.fitnessapp.model.request.RefreshTokenRequest;
import com.example.fitnessapp.model.request.RegisterRequest;
import com.example.fitnessapp.model.request.UpdateProfileRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.ExerciseResponse;
import com.example.fitnessapp.model.response.LoginResponse;
import com.example.fitnessapp.model.response.PlanDetailResponse;
import com.example.fitnessapp.model.response.PlanResponse;
import com.example.fitnessapp.model.response.RegisterResponse;
import com.example.fitnessapp.model.response.SelectOptions;

import java.util.List;

import com.example.fitnessapp.model.response.WorkoutDayDetailResponse;
import com.example.fitnessapp.model.response.WorkoutHistoryResponse;
import com.example.fitnessapp.model.response.WorkoutLogResponse;
import com.example.fitnessapp.model.response.user.ProfileResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface ApiService {

    @POST("auth/login")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest loginRequest);

    @POST("auth/google")
    Call<ApiResponse<LoginResponse>> loginWithGoogle(@Body GoogleLoginRequest googleLoginRequest);


    @POST("auth/register")
    Call<ApiResponse<RegisterResponse>> register(@Body RegisterRequest registerRequest);

    @POST("auth/forgot-password")
    Call<ApiResponse<String>> forgotPassword(@Body ForgotPasswordRequest forgotPasswordRequest);

    @POST("auth/logout")
    Call<ApiResponse<String>> logout(@Body LogoutRequest logoutRequest);

    @GET("exercises")
    Call<ApiResponse<List<ExerciseResponse>>> getAllExercises(
            @Header("Authorization") String authorizationHeader,
            @Query("search") String search,
            @Query("level") String level,
            @Query("muscleId") Long muscleId,
            @Query("typeId") Long typeId,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("muscle-group/select-options")
    Call<ApiResponse<List<SelectOptions>>> getMuscleGroupOptions(
            @Header("Authorization") String authorizationHeader
    );

    @GET("training-type/select-options")
    Call<ApiResponse<List<SelectOptions>>> getTrainingTypeOptions(
            @Header("Authorization") String authorizationHeader
    );

    @GET("exercises/{id}")
    Call<ApiResponse<ExerciseResponse>> getExerciseDetail(
            @Header("Authorization") String authorizationHeader,
            @Path("id") Long exerciseId
    );

    @POST("auth/refresh-token")
    Call<ApiResponse<LoginResponse>> refreshToken(@Body RefreshTokenRequest refreshTokenRequest);

    @GET("user/profile")
    Call<ApiResponse<ProfileResponse>> getUserProfile(
            @Header(Constants.KEY_AUTHORIZATION) String authorization
    );

    @Multipart
    @PUT("user/profile")
    Call<ApiResponse<Boolean>> updateUserProfile(
            @Header(Constants.KEY_AUTHORIZATION) String authorization,
            @Part MultipartBody.Part avatarFile,
            @Part(UpdateProfileRequest.KEY_NAME) RequestBody name,
            @Part(UpdateProfileRequest.KEY_WEIGHT) RequestBody weight,
            @Part(UpdateProfileRequest.KEY_HEIGHT) RequestBody height,
            @Part(UpdateProfileRequest.KEY_ACTIVITY_LEVEL) RequestBody activityLevel,
            @Part(UpdateProfileRequest.KEY_FITNESS_GOAL) RequestBody fitnessGoal,
            @Part(UpdateProfileRequest.KEY_DATE_OF_BIRTH) RequestBody dateOfBirth
            );

    @Multipart
    @PUT("user/profile")
    Call<ApiResponse<Boolean>> updateUserProfile(
            @Header(Constants.KEY_AUTHORIZATION) String authorization,
            @Part(UpdateProfileRequest.KEY_AVATAR) RequestBody avatar,
            @Part(UpdateProfileRequest.KEY_NAME) RequestBody name,
            @Part(UpdateProfileRequest.KEY_WEIGHT) RequestBody weight,
            @Part(UpdateProfileRequest.KEY_HEIGHT) RequestBody height,
            @Part(UpdateProfileRequest.KEY_ACTIVITY_LEVEL) RequestBody activityLevel,
            @Part(UpdateProfileRequest.KEY_FITNESS_GOAL) RequestBody fitnessGoal,
            @Part(UpdateProfileRequest.KEY_DATE_OF_BIRTH) RequestBody dateOfBirth
            );

    @POST("user/change-password")
    Call<ApiResponse<String>> changePassword(
            @Header(Constants.KEY_AUTHORIZATION) String authorization,
            @Body ChangePasswordRequest request);

    @GET("workout-plan/samples")
    Call<ApiResponse<List<PlanResponse>>> getSampleWorkoutPlans(
            @Header("Authorization") String authorizationHeader,
            @Query("keyword") String keyword,
            @Query("goal") FitnessGoal goal,
            @Query("level") DifficultyLevel level,
            @Query("duration") Integer duration,
            @Query("page") Integer page,
            @Query("limit") Integer limit
    );

    @GET("workout-plan/mine")
    Call<ApiResponse<List<PlanResponse>>> getMyWorkoutPlans(
            @Header("Authorization") String authorizationHeader,
            @Query("keyword") String keyword,
            @Query("goal") FitnessGoal goal,
            @Query("level") DifficultyLevel level,
            @Query("duration") Integer duration,
            @Query("page") Integer page,
            @Query("limit") Integer limit
    );

    @GET("workout-plan/{id}")
    Call<ApiResponse<PlanDetailResponse>> getPlanDetail(
            @Header("Authorization") String authorizationHeader,
            @Path("id") Long planId
    );

    @POST("workout-plan")
    Call<ApiResponse<Long>> createPlan(
            @Header("Authorization") String authorizationHeader,
            @Body CreatePlanRequest request
    );

    @PUT("workout-plan/{id}")
    Call<ApiResponse<Long>> updatePlan(
            @Header("Authorization") String authorizationHeader,
            @Path("id") Long planId,
            @Body CreatePlanRequest request
    );

    @POST("workout-plan/{id}/copy")
    Call<ApiResponse<Long>> copyPlan(
            @Header("Authorization") String authorizationHeader,
            @Path("id") Long planId
    );

    @DELETE("workout-plan/{id}")
    Call<ApiResponse<Boolean>> deletePlan(
            @Header("Authorization") String authorizationHeader,
            @Path("id") Long planId
    );

    @GET("workout-plan/calendar")
    Call<ApiResponse<List<PlanResponse>>> getPlansByDate(
            @Header("Authorization") String authorizationHeader,
            @Query("date") String date // dd/MM/yyyy
    );

    @GET("workout-plan/day/{dayId}")
    Call<ApiResponse<WorkoutDayDetailResponse>> getExercisesByDay(
            @Header("Authorization") String authorizationHeader,
            @Path("dayId") Long dayId
    );

    @POST("workout-logs")
    Call<ApiResponse<Boolean>> logWorkoutSet(
            @Header("Authorization") String authorizationHeader,
            @Body LogWorkoutRequest request
    );

    @GET("workout-logs")
    Call<ApiResponse<List<WorkoutLogResponse>>> getLogsByDate(
            @Header("Authorization") String authorizationHeader,
            @Query("date") String date // dd/MM/yyyy
    );

    @GET("workout-logs/history")
    Call<ApiResponse<List<WorkoutHistoryResponse>>> getWorkoutHistory(
            @Header("Authorization") String authorizationHeader,
            @Query("fromDate") String fromDate, // dd/MM/yyyy
            @Query("toDate") String toDate,     // dd/MM/yyyy
            @Query("exerciseId") Long exerciseId
    );

    @GET("workout-logs/plan-day/{dayId}/exercise/{exerciseId}")
    Call<ApiResponse<List<WorkoutLogResponse>>> getLogsByWorkoutDayAndExercise(
            @Header("Authorization") String authorizationHeader,
            @Path("dayId") Long dayId,
            @Path("exerciseId") Long exerciseId
    );

    @GET("workout-logs/statistics")
    Call<ApiResponse<Object>> getWorkoutLogStatistics(
            @Header("Authorization") String authorizationHeader
    );
}