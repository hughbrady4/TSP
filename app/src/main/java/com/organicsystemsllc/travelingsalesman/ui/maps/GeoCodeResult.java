package com.organicsystemsllc.travelingsalesman.ui.maps;

import android.util.JsonReader;
import android.util.Log;

import com.organicsystemsllc.travelingsalesman.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.util.Objects;

public class GeoCodeResult {

    private String mFormattedAddress;

    public GeoCodeResult(JSONObject response) {
        try {
            JSONArray results = response.getJSONArray("results");
            JSONObject addressData = (JSONObject) results.get(0);
            mFormattedAddress = addressData.getString("formatted_address");

        } catch (JSONException e) {
            Log.e(MainActivity.TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    public String getFormattedAddress() {
        return mFormattedAddress;
    }
}
