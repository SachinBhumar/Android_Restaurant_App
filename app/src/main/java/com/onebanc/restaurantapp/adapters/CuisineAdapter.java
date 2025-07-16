package com.onebanc.restaurantapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.onebanc.restaurantapp.R; // Make sure this R is correct for your package
import com.onebanc.restaurantapp.models.Cuisine;
import com.onebanc.restaurantapp.utils.ImageLoader; // Will be created in next step

import java.util.ArrayList; // Important to initialize lists in constructors
import java.util.List;

public class CuisineAdapter extends RecyclerView.Adapter<CuisineAdapter.CuisineViewHolder> {

    private List<Cuisine> cuisineList;
    private OnCuisineClickListener listener;

    public interface OnCuisineClickListener {
        void onCuisineClick(Cuisine cuisine);
    }

    public CuisineAdapter(List<Cuisine> cuisineList, OnCuisineClickListener listener) {
        this.cuisineList = cuisineList != null ? cuisineList : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public CuisineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cuisine_card, parent, false);
        return new CuisineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CuisineViewHolder holder, int position) {
        Cuisine cuisine = cuisineList.get(position);
        holder.tvCuisineName.setText(cuisine.getCuisine_name());
        ImageLoader.loadImage(holder.ivCuisineImage, cuisine.getCuisine_image_url()); // Using ImageLoader

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCuisineClick(cuisine);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cuisineList.size();
    }

    public static class CuisineViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCuisineImage;
        TextView tvCuisineName;

        public CuisineViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCuisineImage = itemView.findViewById(R.id.ivCuisineImage);
            tvCuisineName = itemView.findViewById(R.id.tvCuisineName);
        }
    }

    // Method to update data (useful after API call)
    public void updateCuisines(List<Cuisine> newCuisines) {
        this.cuisineList.clear();
        if (newCuisines != null) {
            this.cuisineList.addAll(newCuisines);
        }
        notifyDataSetChanged();
    }
}
