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
import com.example.fitnessapp.model.response.nutrition.MenuResponse;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    private final Context context;
    private List<MenuResponse> menuList;
    private OnMenuClickListener listener;

    public MenuAdapter(Context context) {
        this.context = context;
        this.menuList = new ArrayList<>();
    }

    public void setMenuList(List<MenuResponse> menuList) {
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
        MenuResponse menu = menuList.get(position);
        holder.bind(menu);
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    class MenuViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardMenu;
        private final ImageView ivMenuImage;
        private final TextView tvMenuName;
        private final TextView tvCalories;
        private final TextView tvFitnessGoal;
        private final CircleImageView civCreatorAvatar;
        private final TextView tvCreatorName;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            cardMenu = itemView.findViewById(R.id.card_menu);
            ivMenuImage = itemView.findViewById(R.id.iv_menu_image);
            tvMenuName = itemView.findViewById(R.id.tv_menu_name);
            tvCalories = itemView.findViewById(R.id.tv_calories);
            tvFitnessGoal = itemView.findViewById(R.id.tv_fitness_goal);
            civCreatorAvatar = itemView.findViewById(R.id.civ_creator_avatar);
            tvCreatorName = itemView.findViewById(R.id.tv_creator_name);
        }

        public void bind(MenuResponse menu) {
            // Set menu name
            tvMenuName.setText(menu.getName());

            // Set calories
            if (menu.getCalories() != null) {
                tvCalories.setText(String.format(Locale.getDefault(), "%.0f kcal", menu.getCalories()));
            } else {
                tvCalories.setText("-- kcal");
            }

            // Set fitness goal
            if (menu.getFitnessGoal() != null) {
                int goalStringRes = menu.getFitnessGoal().getResId();
                tvFitnessGoal.setText(context.getString(goalStringRes));
            } else {
                tvFitnessGoal.setText("");
            }

            // Load menu image
            if (menu.getImage() != null && !menu.getImage().isEmpty()) {
                Glide.with(context)
                        .load(menu.getImage())
                        .placeholder(R.drawable.ic_empty_nutrition_96)
                        .error(R.drawable.ic_empty_nutrition_96)
                        .centerCrop()
                        .into(ivMenuImage);
            } else {
                ivMenuImage.setImageResource(R.drawable.ic_empty_nutrition_96);
            }

            // Set creator info (only show for menus you don't own)
            boolean isOwner = menu.getIsOwner() != null && menu.getIsOwner();

            if (!isOwner) {
                tvCreatorName.setVisibility(View.VISIBLE);
                civCreatorAvatar.setVisibility(View.VISIBLE);

                // Set creator name - show "Unknown User" if null/empty
                if (menu.getCreatorName() != null && !menu.getCreatorName().isEmpty()) {
                    tvCreatorName.setText(menu.getCreatorName());
                } else {
                    tvCreatorName.setText(R.string.unknown_user);
                }

                // Load creator avatar - use default if null/empty
                if (menu.getCreatorAvatar() != null && !menu.getCreatorAvatar().isEmpty()) {
                    Glide.with(context)
                            .load(menu.getCreatorAvatar())
                            .placeholder(R.drawable.img_user_default_128)
                            .error(R.drawable.img_user_default_128)
                            .centerCrop()
                            .into(civCreatorAvatar);
                } else {
                    civCreatorAvatar.setImageResource(R.drawable.img_user_default_128);
                }
            } else {
                tvCreatorName.setVisibility(View.GONE);
                civCreatorAvatar.setVisibility(View.GONE);
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
        void onMenuClick(MenuResponse menu);
        void onCloneClick(MenuResponse menu);
    }
}
