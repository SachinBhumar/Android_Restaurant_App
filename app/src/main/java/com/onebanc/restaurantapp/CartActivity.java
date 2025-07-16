package com.onebanc.restaurantapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.onebanc.restaurantapp.adapters.CartAdapter;
import com.onebanc.restaurantapp.models.Dish; // Ensure your Dish model is here

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;



public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemActionListener {

    private static final String TAG = "CartActivity";

    private RecyclerView recyclerViewCartItems;

    private List<Dish> cartItems = new ArrayList<>();
    private CartAdapter cartAdapter;
    private Button btnPlaceOrder; // Declare the button
    private TextView tvNetTotal;
    private TextView tvCGST;
    private TextView tvSGST;
    private TextView tvGrandTotal;
    private double currentGrandTotal = 0.0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cart_root_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        recyclerViewCartItems = findViewById(R.id.recyclerViewCartItems);
        // Removed tvCartTotal as it's no longer in XML

        tvNetTotal = findViewById(R.id.tvNetTotal);      // Initialize new TextViews
        tvCGST = findViewById(R.id.tvCGST);
        tvSGST = findViewById(R.id.tvSGST);
        tvGrandTotal = findViewById(R.id.tvGrandTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder); // Initialize the button // Initialize the button

        if (getIntent().hasExtra("cart_items")) {
            cartItems = getIntent().getParcelableArrayListExtra("cart_items");
            if (cartItems == null) {
                cartItems = new ArrayList<>();
            }
            Log.d(TAG, "Received " + cartItems.size() + " items in cart.");
        } else {
            Log.d(TAG, "No cart items received.");
        }

        recyclerViewCartItems.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(cartItems, this);
        recyclerViewCartItems.setAdapter(cartAdapter);

        updateCartTotal();

        // Set up click listener for the "Place Order" button
        // Set up click listener for the "Place Order" button
        btnPlaceOrder.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(CartActivity.this, "Your cart is empty! Add some items first.", Toast.LENGTH_SHORT).show();
                return;
            }

            // // Temporarily commented out to bypass actual API call
            // new MakePaymentTask().execute();

            // Simulate successful order and show a better UI effect
            simulateOrderSuccess();
        });
    }

    private void showOrderSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Order Successful!");
        builder.setMessage("Your order has been placed successfully.\nTxn Ref: SIMULATED_TXN_12345"); // You can put any message here
        builder.setCancelable(false); // Make it not dismissable by back button or outside touch

        builder.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // Optionally, you can navigate back to MainActivity here
                // Intent intent = new Intent(CartActivity.this, MainActivity.class);
                // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                // startActivity(intent);
                // finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Declare this variable at the top of your CartActivity class, with other TextViews
    // private double currentGrandTotal = 0.0;
    private void simulateOrderSuccess() {
        // You can add a small delay to mimic network latency if desired
        // new Handler(Looper.getMainLooper()).postDelayed(() -> {

        // Clear the cart items and update the UI
        cartItems.clear();
        cartAdapter.notifyDataSetChanged();
        updateCartTotal(); // Update totals to show 0

        // Show a custom AlertDialog for "Order Successful"
        showOrderSuccessDialog();

        // }, 1000); // 1-second delay
    }


    private void updateCartTotal() {
        double netTotal = 0.0; // Use netTotal for clarity
        for (Dish dish : cartItems) {
            try {
                String priceString = dish.getPrice().replace("₹", "").trim();
                netTotal += Double.parseDouble(priceString) * dish.getQuantity();
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error parsing price for dish: " + dish.getName() + ", Price: " + dish.getPrice(), e);
                // Consider how to handle items with invalid prices (e.g., skip them, default to 0)
            }
        }

        // Calculate CGST and SGST (2.5% each)
        double cgst = netTotal * 0.025;
        double sgst = netTotal * 0.025;
        double grandTotal = netTotal + cgst + sgst;

        currentGrandTotal = grandTotal; // Store the grand total for the API call

        // Update TextViews with formatted amounts
        tvNetTotal.setText(String.format(Locale.getDefault(), "Net Total: ₹%.2f", netTotal));
        tvCGST.setText(String.format(Locale.getDefault(), "CGST (2.5%%): ₹%.2f", cgst)); // Use %% to escape %
        tvSGST.setText(String.format(Locale.getDefault(), "SGST (2.5%%): ₹%.2f", sgst));
        tvGrandTotal.setText(String.format(Locale.getDefault(), "Grand Total: ₹%.2f", grandTotal));
    }

    @Override
    public void onQuantityChange(Dish dish, int newQuantity) {
        if (newQuantity <= 0) {
            cartItems.remove(dish);
            Toast.makeText(this, "Removed " + dish.getName() + " from cart.", Toast.LENGTH_SHORT).show();
        } else {
            dish.setQuantity(newQuantity);
            Toast.makeText(this, dish.getName() + " quantity updated to " + newQuantity, Toast.LENGTH_SHORT).show();
        }
        cartAdapter.notifyDataSetChanged();
        updateCartTotal();
        Log.d(TAG, "Cart item quantity updated. Current Cart Items Count: " + cartItems.size());
    }

    // AsyncTask for making the payment API call
    private class MakePaymentTask extends AsyncTask<Void, Void, String> {

        private static final String API_KEY = "uonebancservceemultrS3cg8RaL30"; // API Key from documentation
        private static final String BASE_URL = "https://uat.onebanc.ai"; // Base URL from documentation
        private static final String ENDPOINT = "/emulator/interview/make_payment"; // Endpoint from documentation

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(CartActivity.this, "Placing order...", Toast.LENGTH_SHORT).show();
            // You might want to show a progress dialog here
        }

        @Override
        protected String doInBackground(Void... voids) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String responseJson = null;

            try {
                URL url = new URL(BASE_URL + ENDPOINT);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("X-Partner-API-Key", API_KEY);
                urlConnection.setRequestProperty("X-Forward-Proxy-Action", "make_payment");
                urlConnection.setDoOutput(true); // Indicates this is a POST request with a body

                // Build JSON request body
                JSONObject requestBody = new JSONObject();
                double totalAmount = 0.0;
                int totalItems = 0;
                JSONArray dataArray = new JSONArray();

                for (Dish dish : cartItems) {
                    JSONObject itemObject = new JSONObject();
                    // IMPORTANT: The API expects cuisine_id for each item.
                    // This is where you need to ensure your Dish object has the cuisine_id set
                    // when it's added to the cart (from MainActivity's FetchCuisineDataTask).
                    if (dish.getCuisine_id() == null || dish.getCuisine_id().isEmpty()) {
                        Log.e(TAG, "Dish " + dish.getName() + " has no cuisine_id! Payment API might fail.");
                        // You could choose to skip this item or throw an error
                        // For now, it will proceed but the API might reject the request.
                    }
                    itemObject.put("cuisine_id", dish.getCuisine_id()); // Use actual cuisine_id from Dish object
                    itemObject.put("item_id", dish.getId());
                    itemObject.put("item_price", Double.parseDouble(dish.getPrice().replace("₹", "").trim()));
                    itemObject.put("item_quantity", dish.getQuantity());
                    dataArray.put(itemObject);

                    totalAmount += Double.parseDouble(dish.getPrice().replace("₹", "").trim()) * dish.getQuantity();
                    totalItems += dish.getQuantity();
                }

                requestBody.put("total_amount", String.valueOf((int) Math.round(totalAmount)));
                requestBody.put("total_items", totalItems);
                requestBody.put("data", dataArray);

                Log.d(TAG, "Make Payment Request Body: " + requestBody.toString());

                // Write the JSON body to the output stream
                OutputStream os = urlConnection.getOutputStream();
                os.write(requestBody.toString().getBytes("UTF-8"));
                os.close();

                // Get the response
                int responseCode = urlConnection.getResponseCode();
                Log.d(TAG, "Make Payment Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) { // 200 OK
                    reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder buffer = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    responseJson = buffer.toString();
                    Log.d(TAG, "Make Payment Response: " + responseJson);

                    // Parse the response to get message and transaction ID
                    JSONObject jsonResponse = new JSONObject(responseJson);
                    String responseMessage = jsonResponse.optString("response_message", "Order Status Unknown");
                    String txnRefNo = jsonResponse.optString("txn_ref_no", "N/A");
                    return "Order: " + responseMessage + "\nTxn Ref: " + txnRefNo;

                } else {
                    // Handle non-200 responses (e.g., 400 Bad Request, 500 Internal Server Error)
                    reader = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream()));
                    StringBuilder errorBuffer = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        errorBuffer.append(line);
                    }
                    String errorResponse = errorBuffer.toString();
                    Log.e(TAG, "Make Payment Error Response: " + errorResponse);
                    JSONObject errorJson = new JSONObject(errorResponse);
                    return "Error: " + errorJson.optString("response_message", "Failed to place order (Code: " + responseCode + ")");
                }

            } catch (Exception e) {
                Log.e(TAG, "Exception during make_payment API call", e);
                return "Network Error: " + e.getMessage();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (java.io.IOException e) {
                        Log.e(TAG, "Error closing reader", e);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Hide progress dialog if shown

            Toast.makeText(CartActivity.this, result, Toast.LENGTH_LONG).show();

            // If order was successful, clear the cart and update UI
            if (result.contains("Order Placed successfully")) { // Simple check, better to parse actual outcome_code from API response
                cartItems.clear();
                cartAdapter.notifyDataSetChanged();
                updateCartTotal();
                // Optionally, navigate back to MainActivity or a confirmation screen
                // Intent intent = new Intent(CartActivity.this, MainActivity.class);
                // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                // startActivity(intent);
                // finish();
            }
        }
    }
}