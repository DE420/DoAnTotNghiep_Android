package com.example.fitnessapp.network;

import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.nutrition.DishResponse;
import com.example.fitnessapp.model.response.nutrition.MenuResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface NutritionApi {

    // Menu endpoints
    @GET("menus/public")
    Call<ApiResponse<List<MenuResponse>>> getPublicMenus(
            @QueryMap Map<String, String> params
    );

    @GET("menus/my-menus")
    Call<ApiResponse<List<MenuResponse>>> getMyMenus(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("menus/{id}")
    Call<ApiResponse<MenuResponse>> getMenuDetail(
            @Path("id") Long id
    );

    @Multipart
    @POST("menus")
    Call<ApiResponse<MenuResponse>> createMenu(
            @Part MultipartBody.Part image,
            @Part("data") RequestBody data
    );

    @Multipart
    @PUT("menus/{id}")
    Call<ApiResponse<MenuResponse>> updateMenu(
            @Path("id") Long id,
            @Part MultipartBody.Part image,
            @Part("data") RequestBody data
    );

    @POST("menus/{id}/clone")
    Call<ApiResponse<MenuResponse>> cloneMenu(
            @Path("id") Long id
    );

    @DELETE("menus/{id}")
    Call<ApiResponse<Void>> deleteMenu(
            @Path("id") Long id
    );

    // Dish endpoints
    @GET("dishes")
    Call<ApiResponse<List<DishResponse>>> getDishes(
            @QueryMap Map<String, String> params
    );

    @GET("dishes/{id}")
    Call<ApiResponse<DishResponse>> getDishDetail(
            @Path("id") Long id
    );
}
