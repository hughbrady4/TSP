package com.organicsystemsllc.travelingsalesman;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;

public class CheckoutActivity extends AppCompatActivity {

    private static final String PAYMENT_INTENT_URL = "https://createpaymentintent-ct2caske7a-uc.a.run.app";

    private PaymentSheet mPaymentSheet;

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

        fetchPaymentIntent();
    }

    private void fetchPaymentIntent() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                PAYMENT_INTENT_URL,
                null,
                response -> {
                    try {
                        String clientSecret = response.has("paymentIntent") ? response.getString("paymentIntent")
                                : (response.has("clientSecret") ? response.getString("clientSecret")
                                : (response.has("client_secret") ? response.getString("client_secret") : null));

                        if (response.has("publishableKey")) {
                            String publishableKey = response.getString("publishableKey");
                            PaymentConfiguration.init(getApplicationContext(), publishableKey);
                        }

                        if (clientSecret != null) {
                            PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("Traveling Salesman")
                                    .allowsDelayedPaymentMethods(true)
                                    .build();

                            mPaymentSheet.presentWithPaymentIntent(clientSecret, configuration);
                        } else {
                            Log.e(MainActivity.TAG, "No clientSecret found in response: " + response);
                            Toast.makeText(CheckoutActivity.this, "Failed to retrieve payment details", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e(MainActivity.TAG, "JSON parsing error", e);
                        Toast.makeText(CheckoutActivity.this, "Error parsing payment response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(MainActivity.TAG, "Volley error fetching payment intent", error);
                    Toast.makeText(CheckoutActivity.this, "Network error fetching payment intent", Toast.LENGTH_SHORT).show();
                }
        );

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
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
