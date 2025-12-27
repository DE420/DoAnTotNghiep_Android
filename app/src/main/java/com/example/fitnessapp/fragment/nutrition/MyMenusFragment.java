package com.example.fitnessapp.fragment.nutrition;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android:view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnessapp.R;

public class MyMenusFragment extends Fragment {

    public static final String TAG = MyMenusFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Temporary placeholder - will be implemented in Phase 5
        View view = inflater.inflate(android.R.layout.simple_list_item_1, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Will implement full functionality in Phase 5
    }
}
