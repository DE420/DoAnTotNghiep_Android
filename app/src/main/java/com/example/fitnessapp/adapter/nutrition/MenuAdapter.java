package com.example.fitnessapp.adapter.nutrition;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitnessapp.R;
import com.example.fitnessapp.model.response.nutrition.MenuListResponse;
import com.example.fitnessapp.model.response.nutrition.MenuResponse;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private final Context context;
    private List<MenuListResponse> menuList;
    private OnMenuClickListener listener;

    public MenuAdapter(Context context) {
        this.context = context;
        this.menuList = new ArrayList<>();
    }

    public void setMenuList(List<MenuListResponse> menuList) {
        this.menuList = menuList != null ? menuList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnMenuClickListener(OnMenuClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu_card, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuListResponse menu = menuList.get(position);
        holder.bind(menu);
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    class MenuViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardMenu;
        private final TextView tvMenuName;
        private final TextView tvCalories;

        private final TextView tvProtein;
        private final TextView tvCarbs;
        private final TextView tvFat;

        private final TextView tvFitnessGoal;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            cardMenu = itemView.findViewById(R.id.card_menu);
            tvMenuName = itemView.findViewById(R.id.tv_menu_name);
            tvCalories = itemView.findViewById(R.id.tv_calories);
            tvFitnessGoal = itemView.findViewById(R.id.tv_fitness_goal);
            tvProtein = itemView.findViewById(R.id.tv_protein);
            tvCarbs = itemView.findViewById(R.id.tv_carbs);
            tvFat = itemView.findViewById(R.id.tv_fat);
        }

        public void bind(MenuListResponse menu) {
            // Set menu name
            tvMenuName.setText(menu.getName());

            // Set calories
            if (menu.getCalories() != null) {
                tvCalories.setText(String.format(Locale.getDefault(), "%.0f kcal", menu.getCalories()));
            } else {
                tvCalories.setText("-- kcal");
            }

            if (menu.getProtein() != null) {
                tvProtein.setText(context.getString(R.string.format_protein, menu.getProtein()));
            } else {
                tvProtein.setText(context.getString(R.string.format_protein_dash));
            }

            if (menu.getCarbs() != null) {
                tvCarbs.setText(context.getString(R.string.format_carbs, menu.getCarbs()));
            } else {
                tvCarbs.setText(context.getString(R.string.format_carbs_dash));
            }

            if (menu.getFat() != null) {
                tvFat.setText(context.getString(R.string.format_fat, menu.getFat()));
            } else {
                tvFat.setText(context.getString(R.string.format_fat_dash));
            }

            // Set fitness goal
            if (menu.getFitnessGoal() != null) {
                int goalStringRes = menu.getFitnessGoal().getResId();
                tvFitnessGoal.setText(context.getString(goalStringRes));
            } else {
                tvFitnessGoal.setText("");
            }

            // Click listener
            cardMenu.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMenuClick(menu);
                }
            });
        }
    }

    public interface OnMenuClickListener {
        void onMenuClick(MenuListResponse menu);
        void onCloneClick(MenuListResponse menu);
    }
}
