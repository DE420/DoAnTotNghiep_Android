package com.example.fitnessapp.adapter;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.fitnessapp.R;
import com.example.fitnessapp.enums.ActivityLevel;

import java.util.List;

public class ActivityLevelAdapter extends CustomSpinnerAdapter<ActivityLevel> {

    private final LayoutInflater layoutInflater;
    private final Context context;

    public ActivityLevelAdapter(Context context, List<ActivityLevel> items) {
        super(context, items);
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = layoutInflater.inflate(R.layout.selected_item_spinner, parent, false);
        } else {
            view = convertView;
        }

        ActivityLevel item = super.getItem(position);

        if (item != null) {
            String text = context.getString(item.getResId());
            setItem(view, text);
        }

        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        View view;

        if (position == 0) {
            view = layoutInflater.inflate(R.layout.header_spinner, parent, false);

            view.setOnClickListener(v -> {
                View root = parent.getRootView();
                root.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                root.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
            });

        } else {
            view = layoutInflater.inflate(R.layout.item_spinner, parent, false);

            ActivityLevel item = super.getItem(position);

            if (item != null) {
                String text = context.getString(item.getResId());
                setItem(view, text);
            }

            view.setActivated(position == getSelectedPosition());
        }

        return view;
    }
}
