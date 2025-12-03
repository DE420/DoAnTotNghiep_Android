package com.example.fitnessapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.fitnessapp.databinding.ActivityMainBinding;
import com.example.fitnessapp.fragment.CommunityFragment;
import com.example.fitnessapp.fragment.HomeFragment;
import com.example.fitnessapp.fragment.OtherFragment;
import com.example.fitnessapp.fragment.PlanFragment;
import com.example.fitnessapp.fragment.ProfileFragment; // <-- Import ProfileFragment
import com.example.fitnessapp.session.SessionManager;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private int preSelectedItemIditem = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            // Tải HomeFragment làm mặc định, không thêm vào back stack
            loadFragment(new HomeFragment(), "Home", false);
        }

        // Sự kiện click vào avatar sẽ mở ProfileFragment
        binding.imageAvatar.setOnClickListener(v -> {
            loadFragment(new ProfileFragment(), "Profile", true); // addToBackStack là true
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
                startActivity(new Intent(this, ExerciseCountActivity.class));
                return false;
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

        //fake login
        saveToken();
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

    private void saveToken() {
        String accessToken =
                "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiVVNFUiIsInVzZXJJZCI" +
                        "6MywidXNlcm5hbWUiOiJ0dXllbnZ1MSIsInN1YiI6InR" +
                        "1eWVudnUxIiwiZXhwIjoxNzY3MzY2MzI4fQ.aXv--tHa" +
                        "guu_6-Ob8cIDPcePv_ljF2kWX7x3bTO-51Y";
        String refressToken =
                "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjMsInN1YiI6InR1eWV" +
                        "udnUxIiwiZXhwIjoxNzY5OTU4MzI4fQ.sBLv8v8zPmDb" +
                        "xjSl3PqoTXOOS0ECtAvtvgz9Krbd67U";
        SessionManager sessionManager = SessionManager.getInstance(this);
        sessionManager.saveTokens(accessToken, refressToken);
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

        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }


}