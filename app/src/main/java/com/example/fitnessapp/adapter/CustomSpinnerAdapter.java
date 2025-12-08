package com.example.fitnessapp.adapter;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fitnessapp.R;

import java.util.List;

public abstract class CustomSpinnerAdapter<T> extends ArrayAdapter<T> {

//    private final LayoutInflater layoutInflater;
    private final List<T> items;
    private int selectedPosition = -1;

    public CustomSpinnerAdapter(Context context, List<T> items) {
        super(context, 0, items);
        this.items = items;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size() + 1;
    }

    @Override
    public boolean isEnabled(int position) {
        return position != 0;
    }

    @Override
    public T getItem(int position) {
        if (position == 0)
            return null;
        return items.get(position - 1);
    }

//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        View view;
//
//        if (convertView == null) {
//            view = layoutInflater.inflate(R.layout.selected_item_spinner, parent, false);
//        } else {
//            view = convertView;
//        }
//
//        T item = getItem(position);
//
//        if (item != null) {
//            String text = item.toString();
//            setItem(view, text);
//        }
//
//        return view;
//    }

//    @Override
//    public View getDropDownView(int position, View convertView, ViewGroup parent) {
//
//        View view;
//
//        if (position == 0) {
//            view = layoutInflater.inflate(R.layout.header_spinner, parent, false);
//
//            view.setOnClickListener(v -> {
//                View root = parent.getRootView();
//                root.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
//                root.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
//            });
//
//        } else {
//            view = layoutInflater.inflate(R.layout.item_spinner, parent, false);
//
//            T item = getItem(position);
//
//            if (item != null) {
//                String text = item.toString();
//                setItem(view, text);
//            }
//
//            view.setActivated(position == selectedPosition);
//        }
//
//        return view;
//    }

    protected void setItem(View view, String text) {
        TextView tvItemSpinner = view.findViewById(R.id.tv_item_spinner);
        tvItemSpinner.setText(text);
    }

}
