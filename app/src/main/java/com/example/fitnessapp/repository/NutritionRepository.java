package com.example.fitnessapp.repository;

import android.content.Context;
import android.util.Log;

import com.example.fitnessapp.model.request.nutrition.MenuRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.nutrition.DishResponse;
import com.example.fitnessapp.model.response.nutrition.MealDishResponse;
import com.example.fitnessapp.model.response.nutrition.MenuResponse;
import com.example.fitnessapp.network.NutritionApi;
import com.example.fitnessapp.network.RetrofitClient;
import com.google.gson.Gson;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Callback;

public class NutritionRepository {

    private static final String TAG = "NutritionRepository";
    private final NutritionApi api;

    public NutritionRepository(Context ctx) {
        api = RetrofitClient.getNutritionApi(ctx);
        Log.d(TAG, "NutritionRepository initialized");
    }

    // Menu methods
    public void getPublicMenus(Map<String, String> params,
                              Callback<ApiResponse<List<MenuResponse>>> callback) {
        Log.d(TAG, "Getting public menus with params: " + params);
        api.getPublicMenus(params).enqueue(callback);
    }

    public void getMyMenus(Map<String, String> params,
                          Callback<ApiResponse<List<MenuResponse>>> callback) {
        Log.d(TAG, "Getting my menus with params: " + params);
        api.getMyMenus(params).enqueue(callback);
    }

    public void getMenuDetail(Long id,
                             Callback<ApiResponse<MenuResponse>> callback) {
        Log.d(TAG, "Getting menu detail for ID: " + id);
        api.getMenuDetail(id).enqueue(callback);
    }

    public void createMenu(File imageFile, MenuRequest menuRequest,
                          Callback<ApiResponse<MenuResponse>> callback) {
        // Backend doesn't support image upload, send JSON only
        Log.d(TAG, "Creating menu (JSON only): " + menuRequest.getName());
        api.createMenu(menuRequest).enqueue(callback);
    }

    public void updateMenu(Long id, File imageFile, MenuRequest menuRequest,
                          Callback<ApiResponse<MenuResponse>> callback) {
        // Backend doesn't support image upload, send JSON only
        Log.d(TAG, "Updating menu ID: " + id + " (JSON only)");
        api.updateMenu(id, menuRequest).enqueue(callback);
    }

    public void cloneMenu(Long id,
                         Callback<ApiResponse<MenuResponse>> callback) {
        Log.d(TAG, "Cloning menu ID: " + id);
        api.cloneMenu(id).enqueue(callback);
    }

    public void deleteMenu(Long id,
                          Callback<ApiResponse<Void>> callback) {
        Log.d(TAG, "Deleting menu ID: " + id);
        api.deleteMenu(id).enqueue(callback);
    }

    // Dish methods
    public void getDishes(Map<String, String> params,
                         Callback<ApiResponse<List<DishResponse>>> callback) {
        Log.d(TAG, "Getting dishes with params: " + params);
        api.getDishes(params).enqueue(callback);
    }

    public void getDishes(int page, int size, String search,
                         Callback<ApiResponse<List<MealDishResponse>>> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("size", String.valueOf(size));
        if (search != null && !search.isEmpty()) {
            params.put("search", search);
        }
        Log.d(TAG, "Getting dishes - page: " + page + ", size: " + size + ", search: " + search);
        api.getDishesAsMealDish(params).enqueue(callback);
    }

    public void getDishDetail(Long id,
                             Callback<ApiResponse<DishResponse>> callback) {
        Log.d(TAG, "Getting dish detail for ID: " + id);
        api.getDishDetail(id).enqueue(callback);
    }
}
