package com.organicsystemsllc.travelingsalesman.ui.route;


import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;
import com.organicsystemsllc.travelingsalesman.BuildConfig;
import com.organicsystemsllc.travelingsalesman.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RouteRequest extends JsonObjectRequest {

    private LatLng mOrigin;
    private LatLng mDestination;
    private String travelMode = "DRIVE";
    private String routingPreference = "TRAFFIC_AWARE";
    private Timestamp departureTime;
    private boolean computeAlternativeRoutes;
    private String languageCode = "en-US";
    private String units = "IMPERIAL";

    public RouteRequest(String url, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener) {
        super(Method.POST, url, null, listener, errorListener);
    }


    public RouteRequest(int method, String url, @Nullable JSONObject jsonRequest, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener) {
        super(Method.POST, url, null, listener, errorListener);
    }

    private class routeModifiers {
        private boolean avoidTolls;
        private boolean avoidHighways;
        private boolean avoidFerries;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=utf-8");
        headers.put("X-Goog-Api-Key", BuildConfig.TSP_API_KEY);
        headers.put("X-Goog-FieldMask", "routes.duration,routes.distanceMeters,routes.polyline.encodedPolyline");
//        headers.put("User-Agent", "Mozilla/5.0");
        return headers;
    }

    @Override
    public byte[] getBody() {
        JSONObject body = new JSONObject();

        try {
            JSONObject origCordJSON = new JSONObject();
            origCordJSON.put("latitude", mOrigin.latitude);
            origCordJSON.put("longitude", mOrigin.longitude);

            JSONObject locationJSON = new JSONObject();
            locationJSON.put("latLng", origCordJSON);

            JSONObject origin = new JSONObject();
            origin.put("location", locationJSON);
            body.put("origin", origin);

            JSONObject destCordJSON = new JSONObject();
            destCordJSON.put("latitude", mDestination.latitude);
            destCordJSON.put("longitude", mDestination.longitude);


            JSONObject location2 = new JSONObject();
            location2.put("latLng", destCordJSON);

            JSONObject destination = new JSONObject();
            destination.put("location", location2);
            body.put("destination", destination);

            JSONArray intermediates = new JSONArray();


//            body.put("intermediates", intermediates);

            body.put("travelMode", "DRIVE");
            body.put("routingPreference", "TRAFFIC_AWARE");
            body.put("languageCode", languageCode);
            body.put("units", units);

        } catch (JSONException e) {
            Log.e(MainActivity.TAG, Objects.requireNonNull(e.getMessage()));
        }

        Log.i(MainActivity.TAG, body.toString());
        return body.toString().getBytes(StandardCharsets.UTF_8);
    }

    public LatLng getOrigin() {
        return mOrigin;
    }

    public void setOrigin(LatLng origin) {
        this.mOrigin = origin;
    }

    public LatLng getDestination() {
        return mDestination;
    }

    public void setDestination(LatLng destination) {
        this.mDestination = destination;
    }



}
