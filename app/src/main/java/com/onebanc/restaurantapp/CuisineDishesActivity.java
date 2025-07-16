package com.onebanc.restaurantapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager; // For Grid
import androidx.recyclerview.widget.RecyclerView;

import com.onebanc.restaurantapp.adapters.DishAdapter; // Reusing DishAdapter
import com.onebanc.restaurantapp.models.Dish; // Ensure Dish model is correctly updated with setters

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

public class CuisineDishesActivity extends AppCompatActivity implements DishAdapter.OnDishActionListener {

    private static final String TAG = "CuisineDishesActivity";
    private TextView tvCuisineDishesTitle;
    private RecyclerView recyclerViewCuisineDishes;
    private DishAdapter dishAdapter; // Adapter for dishes in this activity

    private String currentCuisineId; // To store the ID of the selected cuisine

    // This will hold items currently in the cart for this screen (can be merged with MainActivity's cart later)
    private List<Dish> cartItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cuisine_dishes);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cuisine_dishes_root_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        tvCuisineDishesTitle = findViewById(R.id.tvCuisineDishesTitle);
        recyclerViewCuisineDishes = findViewById(R.id.recyclerViewCuisineDishes);

        // Setup RecyclerView
        // You can choose GridLayoutManager or LinearLayoutManager here
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2); // 2 columns for dishes, similar to home
        recyclerViewCuisineDishes.setLayoutManager(layoutManager);
        dishAdapter = new DishAdapter(new ArrayList<>(), this); // 'this' for OnDishActionListener
        recyclerViewCuisineDishes.setAdapter(dishAdapter);

        // Get data from the Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            currentCuisineId = extras.getString("cuisine_id");
            String cuisineName = extras.getString("cuisine_name");

            Log.d(TAG, "Received Cuisine ID: " + currentCuisineId);
            Log.d(TAG, "Received Cuisine Name: " + cuisineName);

            if (cuisineName != null) {
                tvCuisineDishesTitle.setText(cuisineName + " Dishes");
            } else {
                tvCuisineDishesTitle.setText("Cuisine Dishes");
            }

            // Initiate API call to fetch dishes for this cuisine
            if (currentCuisineId != null) {
                new FetchDishesByCuisineTask().execute(cuisineName);
            } else {
                Toast.makeText(this, "Cuisine ID not found.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Cuisine ID is null, cannot fetch dishes.");
            }

        } else {
            Toast.makeText(this, "No cuisine selected.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "No extras found in Intent for CuisineDishesActivity");
            tvCuisineDishesTitle.setText("No Cuisine Selected");
        }
    }

    // --- Implementation of DishAdapter.OnDishActionListener (same as MainActivity, for cart logic) ---
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
        // You might want to update the UI for the specific dish to reflect quantity changes
        // This would involve calling notifyDataSetChanged() or more specific notifyItemChanged() on the adapter
        dishAdapter.notifyDataSetChanged(); // Simple but less efficient update
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
        // Update UI for the specific dish
        dishAdapter.notifyDataSetChanged(); // Simple but less efficient update
    }

    // --- AsyncTask for API Call (get_item_by_filter) ---
    private class FetchDishesByCuisineTask extends AsyncTask<String, Void, List<Dish>> {

        private final String BASE_URL = "https://uat.onebanc.ai"; // Base URL for APIs [cite: 220]
        private final String API_ENDPOINT = "/emulator/interview/get_item_by_filter"; // Endpoint for get_item_by_filter [cite: 219]
        private final String PARTNER_API_KEY = "uonebancservceemultrS3cg8RaL30"; // API Key [cite: 229]
        private final String PROXY_ACTION = "get_item_by_filter"; // Proxy Action [cite: 230]

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(CuisineDishesActivity.this, "Fetching dishes...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected List<Dish> doInBackground(String... params) {
            String cuisineId = params[0]; // Get the cuisine ID passed to AsyncTask
            List<Dish> fetchedDishes = new ArrayList<>();
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(BASE_URL + API_ENDPOINT);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST"); // API uses POST method
                urlConnection.setRequestProperty("X-Partner-API-Key", PARTNER_API_KEY); // Required Header [cite: 229]
                urlConnection.setRequestProperty("X-Forward-Proxy-Action", PROXY_ACTION); // Required Header [cite: 230]
                urlConnection.setRequestProperty("Content-Type", "application/json"); // Required Header [cite: 231]
                urlConnection.setDoOutput(true);

                // Create JSON request body as per API documentation
                JSONObject requestBody = new JSONObject();
                JSONArray cuisineTypeArray = new JSONArray();
                // The API documentation for get_item_by_filter uses "cuisine_type": ["Chinese"]
                // It expects the cuisine name, not the ID.
                // This means your Cuisine model should ideally store cuisine_name as well for filtering.
                // For now, let's assume `currentCuisineName` would be available, or you can adjust API call.
                // A simpler alternative if `cuisine_type` filter by name causes issues or is not detailed:
                // Fetch all items with get_item_list [cite: 139] and then filter locally by cuisine_id.
                // However, let's try to follow get_item_by_filter  first.

                // To use get_item_by_filter, you need cuisine_name. You already pass cuisine_name from MainActivity.
                // So, let's modify the AsyncTask to accept cuisine_name as well, or make currentCuisineName a field.
                // For simplicity, let's retrieve cuisineName inside doInBackground if it's passed as a second param.
                // Or better, directly use the cuisineName that was retrieved in onCreate and stored in a field.
                // Let's assume you pass cuisineName as the second parameter to execute().
                // Or, retrieve it from the Intent again if that's cleaner.

                // Let's simplify: the get_item_by_filter API (page 5 ) expects "cuisine_type": ["Chinese"].
                // This means you need the cuisine's name, not its ID, for this filter.
                // You already get `cuisineName` in `onCreate`. You should pass `cuisineName` to this AsyncTask.
                // Let's refactor `execute()` to take `cuisineName` instead of `cuisineId` for filtering.

                // Let's assume the first parameter `params[0]` is the cuisineName for filtering.
                String cuisineNameForFilter = params[0];
                cuisineTypeArray.put(cuisineNameForFilter);
                requestBody.put("cuisine_type", cuisineTypeArray); //

                // Write request body
                OutputStream os = urlConnection.getOutputStream();
                os.write(requestBody.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = urlConnection.getResponseCode();
                Log.d(TAG, "HTTP Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) { // Check for 200 OK response [cite: 266]
                    StringBuilder buffer = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line).append("\n");
                    }

                    String jsonResponse = buffer.toString();
                    Log.d(TAG, "API Response: " + jsonResponse);

                    // Parse JSON response for dishes
                    JSONObject jsonObject = new JSONObject(jsonResponse);
                    JSONArray cuisinesArray = jsonObject.getJSONArray("cuisines"); // It returns an array of cuisines [cite: 249]

                    // Iterate through cuisines to find the one matching the filter, then extract its items
                    for (int i = 0; i < cuisinesArray.length(); i++) {
                        JSONObject cuisineJson = cuisinesArray.getJSONObject(i);
                        // Check if this cuisine matches the requested one (by name or ID if available)
                        // The API returns 'cuisine_name' in the response for get_item_by_filter [cite: 252]
                        if (cuisineJson.getString("cuisine_name").equalsIgnoreCase(cuisineNameForFilter)) {
                            JSONArray itemsArray = cuisineJson.getJSONArray("items");
                            for (int j = 0; j < itemsArray.length(); j++) {
                                JSONObject itemJson = itemsArray.getJSONObject(j);
                                Dish dish = new Dish();
                                dish.setId(itemJson.getString("id"));
                                dish.setName(itemJson.getString("name"));
                                dish.setImage_url(itemJson.getString("image_url"));
                                // Price and Rating might not always be present in filter response, check API docs [cite: 254, 255, 256, 257, 258, 259]
                                // If they are not directly in `items` for `get_item_by_filter`, you might need `get_item_by_id` or handle defaults.
                                // For get_item_list, price and rating are present [cite: 178, 179]
                                // For get_item_by_filter, only id, name, image_url are shown in sample[cite: 256, 257, 258].
                                // Let's use get_item_by_filter as requested and if price/rating are missing, they will be null or default.
                                if (itemJson.has("price")) { // Check if key exists
                                    dish.setPrice(itemJson.getString("price"));
                                } else {
                                    dish.setPrice("N/A"); // Default if not present
                                }
                                if (itemJson.has("rating")) { // Check if key exists
                                    dish.setRating(itemJson.getString("rating"));
                                } else {
                                    dish.setRating("N/A"); // Default if not present
                                }
                                fetchedDishes.add(dish);
                            }
                            break; // Found the cuisine, no need to check others
                        }
                    }
                } else {
                    String errorResponse = "";
                    if (urlConnection.getErrorStream() != null) {
                        BufferedReader errorReader = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
                        String errorLine;
                        StringBuilder errorBuffer = new StringBuilder();
                        while ((errorLine = errorReader.readLine()) != null) {
                            errorBuffer.append(errorLine);
                        }
                        errorResponse = errorBuffer.toString();
                    }
                    Log.e(TAG, "API Call Failed: " + responseCode + ", Error: " + errorResponse);
                }

            } catch (IOException | JSONException e) {
                Log.e(TAG, "Error during API call or JSON parsing: " + e.getMessage(), e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return fetchedDishes;
        }

        @Override
        protected void onPostExecute(List<Dish> result) {
            super.onPostExecute(result);
            if (result != null && !result.isEmpty()) {
                dishAdapter.updateDishes(result); // Update the RecyclerView
                Toast.makeText(CuisineDishesActivity.this, "Dishes fetched successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CuisineDishesActivity.this, "Failed to fetch dishes or no items found.", Toast.LENGTH_LONG).show();
                Log.d(TAG, "No dishes found for cuisine: " + currentCuisineId);
            }
        }
    }
}