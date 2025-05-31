package com.organicsystemsllc.travelingsalesman.ui.maps;

import static com.organicsystemsllc.travelingsalesman.MainActivity.TAG;

import android.util.JsonReader;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;

public class DistanceMatrix {

    private ArrayList<String> mOrigins;
    private ArrayList<String> mDestinations;
    private ArrayList<ArrayList<MapEdge>> mMatrix;
    private LinkedList<MapNode> mMapNodes;
    private String mStatus;
    private int mOriginIndex;
    private int mDestinationIndex;

    public DistanceMatrix(JSONObject distanceMatrix, LinkedList<MapNode> mapNodes) {
        try {
            mMapNodes = mapNodes;
            mOrigins = getAddresses(distanceMatrix.getJSONArray("origin_addresses"));
            mDestinations = getAddresses(distanceMatrix.getJSONArray("destination_addresses"));
            mMatrix = getMatrix((distanceMatrix.getJSONArray("rows")));
            mStatus = distanceMatrix.getString("status");
        } catch (IOException | JSONException e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }

    }

    private ArrayList<String> getAddresses(JSONArray json) throws IOException {
        try (JsonReader reader = new JsonReader(new StringReader(json.toString()))) {
            ArrayList<String> addresses = new ArrayList<>();
            reader.beginArray();
            while (reader.hasNext()) {
                addresses.add(reader.nextString());
            }
            reader.endArray();
            return addresses;
        }
    }

    private TextValuePair getTextValuePair(JsonReader reader) throws IOException {
        TextValuePair pair = new TextValuePair();

        reader.beginObject();
        while(reader.hasNext()) {
            String jsonName = reader.nextName();
            if (jsonName.equals("text")) {
                pair.setText(reader.nextString());
            } else if (jsonName.equals("value")) {
                pair.setValue(reader.nextLong());
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return pair;
    }

    private MapEdge getEntry(JsonReader reader) throws IOException {
        MapEdge entry = new MapEdge();
        String originLabel = mMapNodes.get(mOriginIndex).getLabel();
        String destinationLabel = mMapNodes.get(mDestinationIndex).getLabel();
        entry.setLabel(originLabel + destinationLabel);

        reader.beginObject();
        while (reader.hasNext()) {
            String jsonName = reader.nextName();
            switch (jsonName) {
                case "distance":
                    entry.setDistance(getTextValuePair(reader));
                    break;
                case "duration":
                    entry.setDuration(getTextValuePair(reader));
                    break;
                case "status":
                    String status = reader.nextString();
                    entry.setStatus(status);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        return entry;

    }

    private ArrayList<MapEdge> getRow(JsonReader reader) throws IOException {
        ArrayList<MapEdge> row = new ArrayList<>();

        reader.beginObject();
        while (reader.hasNext()) {
            String jsonName = reader.nextName();
            if (jsonName.equals("elements")) {
                mDestinationIndex = 0;
                reader.beginArray();
                while (reader.hasNext()) {
                    row.add(getEntry(reader));
                    mDestinationIndex += 1;
                }
                //end rows
                reader.endArray();
            } else {
                reader.skipValue();
            }

        }
        reader.endObject();
        return row;
    }

    private ArrayList<ArrayList<MapEdge>> getMatrix(JSONArray json) throws IOException {
        try (JsonReader reader = new JsonReader(new StringReader(json.toString()))) {
            ArrayList<ArrayList<MapEdge>> rows = new ArrayList<>();

            mOriginIndex = 0;
            reader.beginArray();
            while (reader.hasNext()) {
                rows.add(getRow(reader));
                mOriginIndex += 1;
            }
            reader.endArray();
            return rows;
        }
    }

    public ArrayList<String> getOrigins() {
        return mOrigins;
    }

    public void setOrigins(ArrayList<String> origins) {
        this.mOrigins = origins;
    }

    public ArrayList<ArrayList<MapEdge>> getMatrix() {
        return mMatrix;
    }

    public ArrayList<String> getDestinations() {
        return mDestinations;
    }

    public String getStatus() {
        return mStatus;
    }

    public LinkedList<MapNode> getMapNodes() {
        return mMapNodes;
    }

    public void setMapNodes(LinkedList<MapNode> mapNodes) {
        this.mMapNodes = mapNodes;
    }

    @NonNull
    @Override
    public String toString() {
        return "DistanceMatrix{" +
                "Nodes=" + mMapNodes +
                " Origins=" + mOrigins +
                ", Destinations=" + mDestinations +
                ", Matrix=" + mMatrix +
                ", Status='" + mStatus + '\'' +
                '}';
    }
}
