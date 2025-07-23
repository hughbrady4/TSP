package com.organicsystemsllc.travelingsalesman.ui.route;


import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;
import com.organicsystemsllc.travelingsalesman.BuildConfig;
import com.organicsystemsllc.travelingsalesman.MainActivity;
import com.organicsystemsllc.travelingsalesman.ui.maps.MapNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class RouteRequest extends JsonObjectRequest {

    private ArrayList<MapNode> mNodes;
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

    public void setNodes(ArrayList<MapNode> nodes) {
        this.mNodes = nodes;
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

            int size = mNodes.size();
            MapNode origin = mNodes.get(0);

            int index = size - 2;
            MapNode destination = mNodes.get(size-1);


            JSONObject origCordJSON = new JSONObject();
            origCordJSON.put("latitude", origin.getPosition().latitude);
            origCordJSON.put("longitude", origin.getPosition().longitude);

            JSONObject locationJSON = new JSONObject();
            locationJSON.put("latLng", origCordJSON);

            JSONObject originJSON = new JSONObject();
            originJSON.put("location", locationJSON);
            body.put("origin", originJSON);

            JSONObject destCordJSON = new JSONObject();
            destCordJSON.put("latitude", destination.getPosition().latitude);
            destCordJSON.put("longitude", destination.getPosition().longitude);


            JSONObject location2 = new JSONObject();
            location2.put("latLng", destCordJSON);

            JSONObject destJSON = new JSONObject();
            destJSON.put("location", location2);
            body.put("destination", destJSON);

            if (size > 2) {
                List<MapNode> interNodes = mNodes.subList(0, size - 1);
                JSONArray intermediates = new JSONArray();
                interNodes.forEach(new Consumer<MapNode>() {
                    @Override
                    public void accept(MapNode mapNode) {
                        JSONObject interCordJSON = new JSONObject();
                        try {
                            interCordJSON.put("latitude", mapNode.getPosition().latitude);
                            interCordJSON.put("longitude", mapNode.getPosition().longitude);
                            JSONObject location3 = new JSONObject();
                            location3.put("latLng", interCordJSON);
                            JSONObject interJSON = new JSONObject();
                            interJSON.put("location", location3);
                            intermediates.put(interJSON);
                        } catch (JSONException e) {
                            Log.e(MainActivity.TAG, Objects.requireNonNull(e.getMessage()));
                        }
                    }
                });
                body.put("intermediates", intermediates);

            }

            body.put("travelMode", travelMode);
            body.put("routingPreference", routingPreference);
            body.put("languageCode", languageCode);
            body.put("units", units);

        } catch (JSONException e) {
            Log.e(MainActivity.TAG, Objects.requireNonNull(e.getMessage()));
        }

        Log.i(MainActivity.TAG, body.toString());
        return body.toString().getBytes(StandardCharsets.UTF_8);
    }





}
