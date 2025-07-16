package com.onebanc.restaurantapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.onebanc.restaurantapp.R;
import com.onebanc.restaurantapp.models.Dish;
import com.onebanc.restaurantapp.utils.ImageLoader; // Will be created in next step

import java.util.ArrayList;
import java.util.List;

public class DishAdapter extends RecyclerView.Adapter<DishAdapter.DishViewHolder> {

    private List<Dish> dishList;
    private OnDishActionListener listener;

    public interface OnDishActionListener {
        void onAddDish(Dish dish);
        void onRemoveDish(Dish dish);
    }

    public DishAdapter(List<Dish> dishList, OnDishActionListener listener) {
        this.dishList = dishList != null ? dishList : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public DishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dish_tile, parent, false);
        return new DishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DishViewHolder holder, int position) {
        Dish dish = dishList.get(position);
        holder.tvDishName.setText(dish.getName());
        holder.tvDishPrice.setText("₹ " + dish.getPrice());
        holder.tvDishRating.setText("Rating: " + dish.getRating());
        holder.tvDishQuantity.setText(String.valueOf(dish.getQuantity()));

        ImageLoader.loadImage(holder.ivDishImage, dish.getImage_url()); // Using ImageLoader

        // Update visibility of quantity controls based on current quantity
        if (dish.getQuantity() > 0) {
            holder.btnRemoveDish.setVisibility(View.VISIBLE);
            holder.tvDishQuantity.setVisibility(View.VISIBLE);
        } else {
            holder.btnRemoveDish.setVisibility(View.GONE);
            holder.tvDishQuantity.setVisibility(View.GONE);
        }

        holder.btnAddDish.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddDish(dish);
                dish.setQuantity(dish.getQuantity() + 1); // Optimistic update
                notifyItemChanged(position); // Refresh this item's view
            }
        });

        holder.btnRemoveDish.setOnClickListener(v -> {
            if (listener != null && dish.getQuantity() > 0) {
                listener.onRemoveDish(dish);
                dish.setQuantity(dish.getQuantity() - 1); // Optimistic update
                notifyItemChanged(position); // Refresh this item's view
            }
        });
    }

    @Override
    public int getItemCount() {
        return dishList.size();
    }

    public static class DishViewHolder extends RecyclerView.ViewHolder {
        ImageView ivDishImage;
        TextView tvDishName, tvDishPrice, tvDishRating, tvDishQuantity;
        Button btnAddDish, btnRemoveDish;

        public DishViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDishImage = itemView.findViewById(R.id.ivDishImage);
            tvDishName = itemView.findViewById(R.id.tvDishName);
            tvDishPrice = itemView.findViewById(R.id.tvDishPrice);
            tvDishRating = itemView.findViewById(R.id.tvDishRating);
            tvDishQuantity = itemView.findViewById(R.id.tvDishQuantity);
            btnAddDish = itemView.findViewById(R.id.btnAddDish);
            btnRemoveDish = itemView.findViewById(R.id.btnRemoveDish);
        }
    }

    public void updateDishes(List<Dish> newDishes) {
        this.dishList.clear();
        if (newDishes != null) {
            this.dishList.addAll(newDishes);
        }
        notifyDataSetChanged();
    }
}
