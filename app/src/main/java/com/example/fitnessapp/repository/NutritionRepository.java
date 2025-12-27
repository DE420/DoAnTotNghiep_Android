package com.example.fitnessapp.repository;

import android.content.Context;
import android.util.Log;

import com.example.fitnessapp.model.request.nutrition.MenuRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.nutrition.DishResponse;
import com.example.fitnessapp.model.response.nutrition.MenuResponse;
import com.example.fitnessapp.network.NutritionApi;
import com.example.fitnessapp.network.RetrofitClient;
import com.google.gson.Gson;

import java.io.File;
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

    public void getMyMenus(int page, int size, String search,
                          Callback<ApiResponse<List<MenuResponse>>> callback) {
        Log.d(TAG, "Getting my menus - page: " + page + ", size: " + size + ", search: " + search);
        api.getMyMenus(page, size, search).enqueue(callback);
    }

    public void getMenuDetail(Long id,
                             Callback<ApiResponse<MenuResponse>> callback) {
        Log.d(TAG, "Getting menu detail for ID: " + id);
        api.getMenuDetail(id).enqueue(callback);
    }

    public void createMenu(File imageFile, MenuRequest menuRequest,
                          Callback<ApiResponse<MenuResponse>> callback) {
        try {
            // Convert MenuRequest to JSON
            Gson gson = new Gson();
            String json = gson.toJson(menuRequest);
            RequestBody dataBody = RequestBody.create(
                MediaType.parse("application/json"), json);

            // Handle image
            MultipartBody.Part imagePart = null;
            if (imageFile != null && imageFile.exists()) {
                RequestBody imageBody = RequestBody.create(
                    MediaType.parse("image/*"), imageFile);
                imagePart = MultipartBody.Part.createFormData(
                    "image", imageFile.getName(), imageBody);
                Log.d(TAG, "Image part created: " + imageFile.getName());
            }

            Log.d(TAG, "Creating menu: " + json);
            api.createMenu(imagePart, dataBody).enqueue(callback);
        } catch (Exception e) {
            Log.e(TAG, "Error creating menu", e);
        }
    }

    public void updateMenu(Long id, File imageFile, MenuRequest menuRequest,
                          Callback<ApiResponse<MenuResponse>> callback) {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(menuRequest);
            RequestBody dataBody = RequestBody.create(
                MediaType.parse("application/json"), json);

            MultipartBody.Part imagePart = null;
            if (imageFile != null && imageFile.exists()) {
                RequestBody imageBody = RequestBody.create(
                    MediaType.parse("image/*"), imageFile);
                imagePart = MultipartBody.Part.createFormData(
                    "image", imageFile.getName(), imageBody);
                Log.d(TAG, "Image part created: " + imageFile.getName());
            }

            Log.d(TAG, "Updating menu ID: " + id);
            api.updateMenu(id, imagePart, dataBody).enqueue(callback);
        } catch (Exception e) {
            Log.e(TAG, "Error updating menu", e);
        }
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

    public void getDishDetail(Long id,
                             Callback<ApiResponse<DishResponse>> callback) {
        Log.d(TAG, "Getting dish detail for ID: " + id);
        api.getDishDetail(id).enqueue(callback);
    }
}
