package com.example.fitnessapp.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.databinding.ItemHomeMenuCardBinding;
import com.example.fitnessapp.model.response.nutrition.MenuResponse;

import java.util.ArrayList;
import java.util.List;

public class HomeMenuAdapter extends RecyclerView.Adapter<HomeMenuAdapter.ViewHolder> {

    private List<MenuResponse> menus = new ArrayList<>();
    private OnMenuClickListener listener;

    public interface OnMenuClickListener {
        void onMenuClick(MenuResponse menu);
    }

    public HomeMenuAdapter(OnMenuClickListener listener) {
        this.listener = listener;
    }

    public void setMenus(List<MenuResponse> menuList) {
        this.menus = menuList != null ? menuList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomeMenuCardBinding binding = ItemHomeMenuCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(menus.get(position));
    }

    @Override
    public int getItemCount() {
        return menus.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemHomeMenuCardBinding binding;

        ViewHolder(ItemHomeMenuCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MenuResponse menu) {
            // Set menu name and description
            binding.tvMenuName.setText(menu.getName());
            binding.tvMenuDescription.setText(menu.getDescription());

            // Set calories target
            String caloriesTarget = String.format("%d %s",
                    menu.getCaloriesTarget() != null ? menu.getCaloriesTarget() : 0,
                    itemView.getContext().getString(R.string.home_unit_kcal)
            );
            binding.tvCaloriesTarget.setText(caloriesTarget);

            // Set macros
            String macros = itemView.getContext().getString(
                    R.string.home_menu_macros_format,
                    menu.getProtein() != null ? menu.getProtein() : 0f,
                    menu.getCarbs() != null ? menu.getCarbs() : 0f,
                    menu.getFat() != null ? menu.getFat() : 0f
            );
            binding.tvMacros.setText(macros);

            // Set creator name
            String creatorName = menu.getCreatorName() != null ?
                    menu.getCreatorName() : "Unknown";
            binding.tvCreatorName.setText(creatorName);

            // Click listener
            binding.btnViewMenu.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMenuClick(menu);
                }
            });
        }
    }
}
