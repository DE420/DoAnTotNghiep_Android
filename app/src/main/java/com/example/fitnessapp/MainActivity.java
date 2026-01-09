package com.example.fitnessapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.fitnessapp.databinding.ActivityMainBinding;
import com.example.fitnessapp.fragment.CommunityFragment;
import com.example.fitnessapp.fragment.HomeFragment;
import com.example.fitnessapp.fragment.OtherFragment;
import com.example.fitnessapp.fragment.PlanFragment;
import com.example.fitnessapp.fragment.PracticeFragment;
import com.example.fitnessapp.fragment.ProfileFragment;
import com.example.fitnessapp.model.request.LoginRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.LoginResponse;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.network.RetrofitClient;
import com.example.fitnessapp.session.SessionManager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private int preSelectedItemIditem = 0;

    private AppBarLayout appBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        appBarLayout = findViewById(R.id.app_bar_layout);

        if (savedInstanceState == null) {
            // load default home fragment
            loadFragment(new HomeFragment(), "Home", false);
        }

        binding.imageAvatar.setOnClickListener(v -> {
            loadFragment(new ProfileFragment(), "Profile", true);
        });

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = getString(R.string.app_name);
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                title = "Home";
            } else if (itemId == R.id.nav_plan) {
                selectedFragment = new PlanFragment();
                title = "Plan";
            } else if (itemId == R.id.nav_practice) {
                // Thay đổi từ startActivity sang loadFragment
                selectedFragment = new PracticeFragment();
                title = "Practice";
            } else if (itemId == R.id.nav_community) {
                selectedFragment = new CommunityFragment();
                title = "Community";
            } else if (itemId == R.id.nav_other) {
                selectedFragment = new OtherFragment();
                title = "Other";
            }

            if (selectedFragment != null) {
                // Các fragment từ bottom nav không cần thêm vào back stack
                loadFragment(selectedFragment, title, false);
                return true;
            }

            return false;
        });
    }

    // Cập nhật hàm loadFragment để xử lý back stack
    private void loadFragment(Fragment fragment, String title, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (!addToBackStack) {
            fragmentManager.popBackStack("Profile", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in, // enter
                R.anim.fade_out, // exit
                R.anim.fade_in, // popEnter
                R.anim.slide_out // popExit
        );
        transaction.replace(R.id.fragment_container, fragment);

        // Chỉ thêm vào back stack khi được yêu cầu (ví dụ: khi mở Profile)
        if (addToBackStack) {
            transaction.addToBackStack(title);
        }

        transaction.commit();

        preSelectedItemIditem = binding.bottomNavigation.getSelectedItemId();
        if (title.equals("Profile")) {
            binding.appBarLayout.setVisibility(View.GONE);
            binding.bottomNavigation.getMenu().findItem(preSelectedItemIditem).setChecked(false);
        } else {
            binding.bottomNavigation.getMenu().findItem(preSelectedItemIditem).setChecked(true);
            binding.appBarLayout.setVisibility(View.VISIBLE);
            binding.toolbar.setTitle(title);
        }
        // Cập nhật title của toolbar
        if (!title.equals("Profile")) {
            binding.toolbar.setTitle(title);
        }
    }

    public void setAppBarVisible(boolean visible) {
        if (appBarLayout != null) {
            appBarLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();

        Fragment current = fm.findFragmentById(R.id.fragment_container);
        if (current instanceof ProfileFragment) {
            binding.appBarLayout.setVisibility(View.VISIBLE);
            binding.bottomNavigation.getMenu().findItem(preSelectedItemIditem).setChecked(true);
        }
        super.onBackPressed();
    }
}