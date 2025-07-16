package com.onebanc.restaurantapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.onebanc.restaurantapp.R; // Make sure this is your app's R file
import com.onebanc.restaurantapp.models.Dish;
import com.bumptech.glide.Glide; // Assuming you'll use Glide for image loading

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<Dish> cartItems;
    private OnCartItemActionListener listener;

    public interface OnCartItemActionListener {
        void onQuantityChange(Dish dish, int newQuantity);
    }

    public CartAdapter(List<Dish> cartItems, OnCartItemActionListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_dish, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Dish dish = cartItems.get(position);

        holder.tvDishName.setText(dish.getName());
        holder.tvDishPrice.setText(String.format("₹%s", dish.getPrice()));
        holder.tvDishQuantity.setText(String.valueOf(dish.getQuantity()));

        // Load image using Glide (you'll need to add Glide dependency if you haven't)
        // implementation 'com.github.bumptech.glide:glide:4.12.0' in build.gradle (app level)
        // annotationProcessor 'com.github.bumptech.glide:compiler:4.12.0'
        Glide.with(holder.ivDishImage.getContext())
                .load(dish.getImage_url())
                .placeholder(R.drawable.ic_launcher_background) // Add a placeholder image
                .error(R.drawable.ic_launcher_background) // Add an error image
                .into(holder.ivDishImage);

        holder.btnRemoveOne.setOnClickListener(v -> {
            int currentQuantity = dish.getQuantity();
            if (currentQuantity > 0) {
                listener.onQuantityChange(dish, currentQuantity - 1);
            }
        });

        holder.btnAddOne.setOnClickListener(v -> {
            listener.onQuantityChange(dish, dish.getQuantity() + 1);
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivDishImage;
        TextView tvDishName;
        TextView tvDishPrice;
        TextView tvDishQuantity;
        Button btnRemoveOne;
        Button btnAddOne;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDishImage = itemView.findViewById(R.id.ivCartDishImage);
            tvDishName = itemView.findViewById(R.id.tvCartDishName);
            tvDishPrice = itemView.findViewById(R.id.tvCartDishPrice);
            tvDishQuantity = itemView.findViewById(R.id.tvCartDishQuantity);
            btnRemoveOne = itemView.findViewById(R.id.btnRemoveOne);
            btnAddOne = itemView.findViewById(R.id.btn_add_one);
        }
    }
}