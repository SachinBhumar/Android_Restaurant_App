package com.onebanc.restaurantapp;
import java.util.Locale;
import android.content.res.Configuration;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;

import com.onebanc.restaurantapp.adapters.CuisineAdapter;
import com.onebanc.restaurantapp.adapters.DishAdapter;
import com.onebanc.restaurantapp.models.Cuisine;
import com.onebanc.restaurantapp.models.Dish;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CuisineAdapter.OnCuisineClickListener, DishAdapter.OnDishActionListener {

    private static final String TAG = "MainActivity";
    private RecyclerView recyclerViewCuisines;
    private RecyclerView recyclerViewTopDishes;
    private CuisineAdapter cuisineAdapter;
    private DishAdapter topDishesAdapter;
    private Button btnCart;
    private Button btnLanguage;

    // This will hold all fetched cuisines and their dishes
    private List<Cuisine> allCuisines = new ArrayList<>();
    // This will hold items currently in the cart (you'll manage this more thoroughly later)
    private List<Dish> cartItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Links your Java code to your UI layout

        // 1. Initialize UI elements by finding their IDs from activity_main.xml
        recyclerViewCuisines = findViewById(R.id.recyclerViewCuisines);
        recyclerViewTopDishes = findViewById(R.id.recyclerViewTopDishes);
        btnCart = findViewById(R.id.btnCart);
        btnLanguage = findViewById(R.id.btnLanguage);

        // 2. Setup RecyclerViews with their LayoutManagers and Adapters
        // For Cuisine Categories (horizontal scroll)
        LinearLayoutManager cuisineLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewCuisines.setLayoutManager(cuisineLayoutManager);
        // Initialize adapter with an empty list for now. Data will be updated after API call.
        cuisineAdapter = new CuisineAdapter(new ArrayList<>(), this); // 'this' because MainActivity implements OnCuisineClickListener
        recyclerViewCuisines.setAdapter(cuisineAdapter);

        // For Top Dishes (grid layout)
        GridLayoutManager topDishesLayoutManager = new GridLayoutManager(this, 2); // 2 columns
        recyclerViewTopDishes.setLayoutManager(topDishesLayoutManager);
        // Initialize adapter with an empty list for now. Data will be updated after API call.
        topDishesAdapter = new DishAdapter(new ArrayList<>(), this); // 'this' because MainActivity implements OnDishActionListener
        recyclerViewTopDishes.setAdapter(topDishesAdapter);

        // 3. Set up button click listeners (initial placeholder)
        btnCart.setOnClickListener(v -> {
            // Implement navigation to Cart Screen (Screen 3)
            Intent intent = new Intent(MainActivity.this, CartActivity.class);
            // Pass 'cartItems' list to the next activity
            intent.putParcelableArrayListExtra("cart_items", new ArrayList<>(cartItems)); // <--- IMPORTANT LINE
            startActivity(intent);
        });

        btnLanguage.setOnClickListener(v -> {
            // Get current locale
            Locale currentLocale = getResources().getConfiguration().locale;
            String currentLanguage = currentLocale.getLanguage();

            Locale newLocale;
            if (currentLanguage.equals("en")) {
                newLocale = new Locale("hi"); // Switch to Hindi
            } else {
                newLocale = new Locale("en"); // Switch to English
            }

            // Update configuration
            Configuration config = getResources().getConfiguration();
            config.setLocale(newLocale);
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());

            // Recreate activity to apply language change
            recreate(); // This will restart MainActivity, applying the new language
            Toast.makeText(MainActivity.this, "Language changed to " + newLocale.getDisplayLanguage(), Toast.LENGTH_SHORT).show();
        });

        // 4. Initiate API call to fetch data using AsyncTask
        new FetchCuisineDataTask().execute();
    }



    // ... (rest of your MainActivity class)

    @Override
    public void onCuisineClick(Cuisine cuisine) {
        // Create an Intent to start CuisineDishesActivity
        Intent intent = new Intent(MainActivity.this, CuisineDishesActivity.class);

        // Put extra data into the Intent, using the correct getter names
        intent.putExtra("cuisine_id", cuisine.getCuisine_id()); // Use getCuisine_id()
        intent.putExtra("cuisine_name", cuisine.getCuisine_name()); // Use getCuisine_name()

        // Start the new Activity
        startActivity(intent);

        // Optional: Keep the Toast for initial debugging if you wish
        Toast.makeText(MainActivity.this, "Cuisine clicked: " + cuisine.getCuisine_name(), Toast.LENGTH_SHORT).show();
    }

// ... (rest of your MainActivity class)

// ... (rest of your MainActivity class)

    // --- Implementation of DishAdapter.OnDishActionListener ---
    @Override
    public void onAddDish(Dish dish) {
        boolean found = false;
        for (Dish item : cartItems) {
            if (item.getId().equals(dish.getId())) {
                item.setQuantity(item.getQuantity() + 1);
                found = true;
                break;
            }
        }
        if (!found) {
            dish.setQuantity(1); // Set quantity to 1 for a new item
            cartItems.add(dish);
        }
        Toast.makeText(this, "Added: " + dish.getName() + " (Qty: " + dish.getQuantity() + ")", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Current Cart Items Count: " + cartItems.size());
    }

    @Override
    public void onRemoveDish(Dish dish) {
        for (Dish item : cartItems) {
            if (item.getId().equals(dish.getId())) {
                item.setQuantity(item.getQuantity() - 1);
                if (item.getQuantity() <= 0) {
                    cartItems.remove(item); // Remove from cart if quantity drops to zero
                }
                break;
            }
        }
        Toast.makeText(this, "Removed: " + dish.getName() + " (Qty: " + dish.getQuantity() + ")", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Current Cart Items Count: " + cartItems.size());
    }

    // --- AsyncTask for API Call (get_item_list) ---
    // --- AsyncTask for API Call (get_item_list) ---
    private class FetchCuisineDataTask extends AsyncTask<Void, Void, List<Cuisine>> {

        private final String BASE_URL = "https://uat.onebanc.ai"; // Base URL for APIs
        private final String API_ENDPOINT = "/emulator/interview/get_item_list"; // Endpoint for get_item_list
        private final String PARTNER_API_KEY = "uonebancservceemultrS3cg8RaL30"; // API Key
        private final String PROXY_ACTION = "get_item_list"; // Proxy Action

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Show a loading spinner or progress bar here (optional but good for UX)
            Toast.makeText(MainActivity.this, "Fetching menu...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected List<Cuisine> doInBackground(Void... voids) {
            List<Cuisine> fetchedCuisines = new ArrayList<>();
            HttpURLConnection urlConnection = null;
            // Declare reader once here at the top of the method
            BufferedReader reader = null;

            try {
                URL url = new URL(BASE_URL + API_ENDPOINT);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST"); // API uses POST method
                urlConnection.setRequestProperty("X-Partner-API-Key", PARTNER_API_KEY); // Required Header
                urlConnection.setRequestProperty("X-Forward-Proxy-Action", PROXY_ACTION); // Required Header
                urlConnection.setRequestProperty("Content-Type", "application/json"); // Required Header
                urlConnection.setDoOutput(true); // Indicate that we will write to the output stream for POST request

                // Create JSON request body as per API documentation
                JSONObject requestBody = new JSONObject();
                requestBody.put("page", 1); // Requesting page 1
                requestBody.put("count", 10); // Requesting 10 items

                // Write the request body to the connection's output stream
                OutputStream os = urlConnection.getOutputStream();
                os.write(requestBody.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = urlConnection.getResponseCode();
                Log.d(TAG, "HTTP Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) { // Check for 200 OK response
                    StringBuilder buffer = new StringBuilder();
                    // Assign to the already declared 'reader' variable
                    reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line).append("\n");
                    }

                    String jsonResponse = buffer.toString();
                    Log.d(TAG, "API Response: " + jsonResponse);

                    // These are the ONLY declarations for jsonObject and cuisinesArray
                    JSONObject jsonObject = new JSONObject(jsonResponse);
                    JSONArray cuisinesArray = jsonObject.getJSONArray("cuisines"); // Correct spelling: "cuisines"

                    for (int i = 0; i < cuisinesArray.length(); i++) {
                        JSONObject cuisineJson = cuisinesArray.getJSONObject(i);
                        String cuisineId = cuisineJson.getString("cuisine_id");
                        String cuisineName = cuisineJson.getString("cuisine_name");
                        String cuisineImageUrl = cuisineJson.getString("cuisine_image_url");

                        Cuisine cuisine = new Cuisine();
                        cuisine.setCuisine_id(cuisineId);
                        cuisine.setCuisine_name(cuisineName);
                        cuisine.setCuisine_image_url(cuisineImageUrl);

                        JSONArray itemsArray = cuisineJson.getJSONArray("items");
                        List<Dish> dishes = new ArrayList<>();
                        for (int j = 0; j < itemsArray.length(); j++) {
                            JSONObject itemJson = itemsArray.getJSONObject(j);
                            Dish dish = new Dish();
                            dish.setId(itemJson.getString("id"));
                            dish.setName(itemJson.getString("name"));
                            dish.setImage_url(itemJson.getString("image_url"));
                            dish.setPrice(itemJson.getString("price"));
                            dish.setRating(itemJson.getString("rating"));
                            dish.setCuisine_id(cuisineId); // CRITICAL: Set the cuisine_id for the dish here
                            dishes.add(dish);
                        }
                        cuisine.setItems(dishes);
                        fetchedCuisines.add(cuisine);
                    }

                } else {
                    // Log error details for non-200 responses
                    String errorResponse = "";
                    if (urlConnection.getErrorStream() != null) {
                        // Assign to the already declared 'reader' variable for error stream
                        reader = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
                        String errorLine;
                        StringBuilder errorBuffer = new StringBuilder();
                        while ((errorLine = reader.readLine()) != null) {
                            errorBuffer.append(errorLine);
                        }
                        errorResponse = errorBuffer.toString();
                    }
                    Log.e(TAG, "API Call Failed: " + responseCode + ", Error: " + errorResponse);
                }

            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error during API call or JSON parsing: " + e.getMessage(), e);
            } finally {
                // Ensure resources are closed
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) { // This check should remain
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return fetchedCuisines;
        }

        @Override
        protected void onPostExecute(List<Cuisine> result) {
            super.onPostExecute(result);
            // Hide loading spinner/progress bar here (if you showed one in onPreExecute)

            if (result != null && !result.isEmpty()) {
                allCuisines.addAll(result); // Store all fetched cuisines globally

                // Update Cuisine RecyclerView with fetched data
                cuisineAdapter.updateCuisines(result);

                // Populate Top 3 Dishes from the fetched data
                List<Dish> topDishes = new ArrayList<>();
                // The assignment states "Top 3 famous dishes of the restaurant".
                // This implies a global top 3, not necessarily from a specific cuisine.
                // For simplicity, let's collect the first 3 unique dishes found across all cuisines.
                int count = 0;
                for (Cuisine cuisine : allCuisines) {
                    for (Dish dish : cuisine.getItems()) {
                        if (count < 3) {
                            topDishes.add(dish);
                            count++;
                        } else {
                            break; // We have found 3 top dishes
                        }
                    }
                    if (count >= 3) break; // Break outer loop if 3 dishes are found
                }
                topDishesAdapter.updateDishes(topDishes);

                Toast.makeText(MainActivity.this, "Menu fetched successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Failed to fetch menu or no items found.", Toast.LENGTH_LONG).show();
            }
        }
    }
}