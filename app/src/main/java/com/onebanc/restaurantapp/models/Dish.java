package com.onebanc.restaurantapp.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Dish implements Parcelable {
    private String id;
    private String name;
    private String image_url;
    private String price;
    private String rating;
    private int quantity; // For cart management
    private String cuisine_id; // Added field

    // No-argument constructor (for flexibility when using setters)
    public Dish() {
        this.quantity = 1; // Default quantity
    }

    // Full argument constructor (recommended for creating fully-formed objects)
    public Dish(String id, String name, String image_url, String price, String rating, String cuisine_id) {
        this.id = id;
        this.name = name;
        this.image_url = image_url;
        this.price = price;
        this.rating = rating;
        this.quantity = 1; // Default quantity when added to cart
        this.cuisine_id = cuisine_id;
    }

    // --- Parcelable Implementation ---
    // Constructor used when reconstructing object from a Parcel
    protected Dish(Parcel in) {
        id = in.readString();
        name = in.readString();
        image_url = in.readString();
        price = in.readString();
        rating = in.readString();
        quantity = in.readInt();
        cuisine_id = in.readString(); // Read cuisine_id from Parcel
    }

    // This static field is required for Parcelable implementation
    public static final Creator<Dish> CREATOR = new Creator<Dish>() {
        @Override
        public Dish createFromParcel(Parcel in) {
            return new Dish(in);
        }

        @Override
        public Dish[] newArray(int size) {
            return new Dish[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(image_url);
        dest.writeString(price);
        dest.writeString(rating);
        dest.writeInt(quantity);
        dest.writeString(cuisine_id); // Write cuisine_id to Parcel
    }
    // --- End Parcelable Implementation ---


    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImage_url() {
        return image_url;
    }

    public String getPrice() {
        return price;
    }

    public String getRating() {
        return rating;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getCuisine_id() {
        return cuisine_id;
    } // Getter for cuisine_id

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setCuisine_id(String cuisine_id) {
        this.cuisine_id = cuisine_id;
    } // Setter for cuisine_id
}