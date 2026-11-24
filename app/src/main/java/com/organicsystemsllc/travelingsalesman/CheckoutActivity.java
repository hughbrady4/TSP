package com.organicsystemsllc.travelingsalesman;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {

    FirebaseFunctions mFunctions;
    private PaymentSheet mPaymentSheet;
    String mPaymentIntentClientSecret;
    PaymentSheet.CustomerConfiguration mCustomerConfig;
    PaymentSheet.Configuration mPaymentSheetConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPaymentSheet = new PaymentSheet.Builder(this::onPaymentSheetResult).build(this);

        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_checkout);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        androidx.recyclerview.widget.RecyclerView cartRecyclerView = findViewById(R.id.cartRecyclerView);
        cartRecyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        List<CartItem> cartItems = new java.util.ArrayList<>();
        CartAdapter cartAdapter = new CartAdapter(cartItems);
        cartRecyclerView.setAdapter(cartAdapter);

        android.widget.TextView totalPriceTextView = findViewById(R.id.totalPriceTextView);

        findViewById(R.id.button2).setOnClickListener(v -> {
            if (mPaymentIntentClientSecret != null && mPaymentSheetConfig != null) {
                mPaymentSheet.presentWithPaymentIntent(mPaymentIntentClientSecret, mPaymentSheetConfig);
            } else {
                Toast.makeText(CheckoutActivity.this, "Payment details are not available or cart is empty", Toast.LENGTH_SHORT).show();
            }
        });

        mFunctions = FirebaseFunctions.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            FirebaseFirestore.getInstance("travelingsalesman").collection("users")
                    .document(currentUser.getUid())
                    .collection("cart")
                    .addSnapshotListener((value, error) -> {
                        if (error != null) {
                            Log.e(MainActivity.TAG, "Listen failed.", error);
                            Toast.makeText(CheckoutActivity.this, "Error loading cart: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            totalPriceTextView.setText("Total: $0.00");
                            return;
                        }

                        cartItems.clear();
                        if (value != null && !value.isEmpty()) {
                            for (QueryDocumentSnapshot doc : value) {
                                String name = doc.getString("name");
                                if (name == null) name = doc.getString("title");
                                if (name == null) name = "Product";

                                String description = doc.getString("description");
                                if (description == null) description = "";

                                double price = 0.0;
                                Object priceObj = doc.get("price");
                                if (priceObj instanceof Number) {
                                    price = ((Number) priceObj).doubleValue();
                                }

                                cartItems.add(new CartItem(name, description, price));
                            }
                        }

                        cartAdapter.notifyDataSetChanged();

                        if (cartItems.isEmpty()) {
                            totalPriceTextView.setText("Total: $0.00");
                            Toast.makeText(CheckoutActivity.this, "Your cart is empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        double total = 0.0;
                        for (CartItem item : cartItems) {
                            total += item.getPrice();
                        }

                        totalPriceTextView.setText(String.format(java.util.Locale.US, "Total: $%.2f", total));

                        int amountInCents = (int) Math.round(total * 100);
                        fetchPaymentIntent(amountInCents).addOnSuccessListener(result -> {
                            Log.i(MainActivity.TAG, "Payment intent response: " + result.toString());
                            try {
                                if (result.has("publicKey")) {
                                    String publishableKey = result.getString("publicKey");
                                    String connectedAccountId = result.has("stripeAccountId") ? result.getString("stripeAccountId")
                                            : (result.has("connectedAccountId") ? result.getString("connectedAccountId") : "acct_1Tb5XUHQJIHAjC0m");
                                    PaymentConfiguration.init(getApplicationContext(), publishableKey, connectedAccountId);
                                }

                                if (result.has("customer") && result.has("customerSessionClientSecret")) {
                                    String customer = result.getString("customer");
                                    String customerSessionClientSecret = result.getString("customerSessionClientSecret");
                                    mCustomerConfig = new PaymentSheet.CustomerConfiguration(customer, customerSessionClientSecret);
                                }

                                String clientSecret = result.has("clientSecret") ? result.getString("clientSecret")
                                        : null;
                                if (clientSecret != null) {
                                    mPaymentSheetConfig =
                                            new PaymentSheet.Configuration.Builder("Traveling Salesman")
                                                    .customer(mCustomerConfig)
                                            .allowsDelayedPaymentMethods(true)
                                            .build();
                                    mPaymentIntentClientSecret = clientSecret;
                                } else {
                                    Log.e(MainActivity.TAG, "No clientSecret found in response: " + result);
                                    Toast.makeText(CheckoutActivity.this, "Failed to retrieve payment details", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Log.e(MainActivity.TAG, "JSON parsing error", e);
                            }
                        }).addOnFailureListener(e -> {
                            Log.e(MainActivity.TAG, "Error fetching payment intent", e);
                            Toast.makeText(CheckoutActivity.this, "Error fetching payment intent: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    });
        } else {
            totalPriceTextView.setText("Total: $0.00");
            Toast.makeText(CheckoutActivity.this, "Please sign in to view your cart", Toast.LENGTH_LONG).show();
        }

    }

    private Task<JSONObject> fetchPaymentIntent(int amount) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("connectedAccountId", "acct_1Tb5XUHQJIHAjC0m");
        data.put("amount", amount);
        data.put("currency", "usd");

        return mFunctions.getHttpsCallable("createPaymentIntent").call(data).continueWith(
            new Continuation<HttpsCallableResult, JSONObject>() {
                @Override
                public JSONObject then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                    Object rawData = task.getResult().getData();
                    if (rawData instanceof Map) {
                        return new JSONObject((Map<?, ?>) rawData);
                    } else if (rawData != null) {
                        return new JSONObject(rawData.toString());
                    }
                    return new JSONObject();
                }
            }
        );
    }

    void onPaymentSheetResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment Complete!", Toast.LENGTH_SHORT).show();
            finish();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(this, "Payment Canceled", Toast.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            PaymentSheetResult.Failed failedResult = (PaymentSheetResult.Failed) paymentSheetResult;
            Log.e(MainActivity.TAG, "Payment failed", failedResult.getError());
            Toast.makeText(this, "Payment Failed: " + failedResult.getError().getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
