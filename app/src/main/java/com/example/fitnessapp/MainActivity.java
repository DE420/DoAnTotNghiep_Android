package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.fitnessapp.databinding.ActivityMainBinding;
import com.example.fitnessapp.fragment.CommunityFragment;
import com.example.fitnessapp.fragment.HomeFragment;
import com.example.fitnessapp.fragment.OtherFragment;
import com.example.fitnessapp.fragment.PlanFragment;
import com.example.fitnessapp.ExerciseCountActivity;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Load Fragment mặc định là HomeFragment khi Activity khởi chạy
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            binding.toolbar.setTitle("Home"); // Đặt title mặc định
        }

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = getString(R.string.app_name);
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                title = "Home";
            } else if (itemId == R.id.nav_plan) {
                selectedFragment = new PlanFragment(); // Fragment bạn đã tạo
                title = "Plan";
            } else if (itemId == R.id.nav_practice) {
                // Mở PracticeActivity và không thay đổi fragment
                startActivity(new Intent(this, ExerciseCountActivity.class));
                return false; // Trả về false để item không được chọn, giữ nguyên tab hiện tại
            } else if (itemId == R.id.nav_community) {
                selectedFragment = new CommunityFragment(); // Fragment bạn đã tạo
                title = "Community";
            } else if (itemId == R.id.nav_other) {
                selectedFragment = new OtherFragment(); // Fragment bạn đã tạo
                title = "Other";
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                binding.toolbar.setTitle(title); // Cập nhật title của toolbar
                return true;
            }

            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}