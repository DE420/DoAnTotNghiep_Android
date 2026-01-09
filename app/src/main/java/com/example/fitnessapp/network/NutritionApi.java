package com.example.fitnessapp.network;

import com.example.fitnessapp.model.request.nutrition.MenuRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.nutrition.DishResponse;
import com.example.fitnessapp.model.response.nutrition.MealDishResponse;
import com.example.fitnessapp.model.response.nutrition.MenuListResponse;
import com.example.fitnessapp.model.response.nutrition.MenuResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface NutritionApi {

    // Menu endpoints
    @GET("menus/public")
    Call<ApiResponse<List<MenuListResponse>>> getPublicMenus(
            @QueryMap Map<String, String> params
    );

    @GET("menus/my-menus")
    Call<ApiResponse<List<MenuListResponse>>> getMyMenus(
            @QueryMap Map<String, String> params
    );

    @GET("menus/{id}")
    Call<ApiResponse<MenuResponse>> getMenuDetail(
            @Path("id") Long id
    );

    @POST("menus")
    Call<ApiResponse<MenuResponse>> createMenu(
            @Body MenuRequest request
    );

    @PUT("menus/{id}")
    Call<ApiResponse<MenuResponse>> updateMenu(
            @Path("id") Long id,
            @Body MenuRequest request
    );

    @POST("menus/{id}/clone")
    Call<ApiResponse<MenuResponse>> cloneMenu(
            @Path("id") Long id
    );

    @DELETE("menus/{id}")
    Call<ApiResponse<String>> deleteMenu(
            @Path("id") Long id
    );

    // Dish endpoints
    @GET("dishes")
    Call<ApiResponse<List<DishResponse>>> getDishes(
            @QueryMap Map<String, String> params
    );

    @GET("dishes")
    Call<ApiResponse<List<MealDishResponse>>> getDishesAsMealDish(
            @QueryMap Map<String, String> params
    );

    @GET("dishes/{id}")
    Call<ApiResponse<DishResponse>> getDishDetail(
            @Path("id") Long id
    );
}
