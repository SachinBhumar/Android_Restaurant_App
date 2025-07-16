package com.onebanc.restaurantapp.models;

import java.util.ArrayList; // Added for initializing items in default constructor
import java.util.List;

public class Cuisine {
    private String cuisine_id;
    private String cuisine_name;
    private String cuisine_image_url;
    private List<Dish> items;

    // Existing parameterized constructor
    public Cuisine(String cuisine_id, String cuisine_name, String cuisine_image_url, List<Dish> items) {
        this.cuisine_id = cuisine_id;
        this.cuisine_name = cuisine_name;
        this.cuisine_image_url = cuisine_image_url;
        this.items = items;
    }

    // NEW: Default constructor - needed because you call `new Cuisine()` in AsyncTask
    public Cuisine() {
        this.items = new ArrayList<>(); // Initialize the list to avoid NullPointerExceptions
    }

    // Existing Getters (ensure they are correct based on field names)
    public String getCuisine_id() {
        return cuisine_id;
    }

    public String getCuisine_name() {
        return cuisine_name;
    }

    public String getCuisine_image_url() {
        return cuisine_image_url;
    }

    public List<Dish> getItems() {
        return items;
    }

    // NEW: Setters - needed because you call `cuisine.setCuisine_id()` etc. in AsyncTask
    public void setCuisine_id(String cuisine_id) {
        this.cuisine_id = cuisine_id;
    }

    public void setCuisine_name(String cuisine_name) {
        this.cuisine_name = cuisine_name;
    }

    public void setCuisine_image_url(String cuisine_image_url) {
        this.cuisine_image_url = cuisine_image_url;
    }

    public void setItems(List<Dish> items) {
        this.items = items;
    }
}