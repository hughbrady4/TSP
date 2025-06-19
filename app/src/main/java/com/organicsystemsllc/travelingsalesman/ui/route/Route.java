package com.organicsystemsllc.travelingsalesman.ui.route;

import android.util.JsonReader;

import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;

public class Route {

    private String mLabel;
    private Long mDistanceMeters;
    private String mDuration;
    private String mPolyline;

    public Route() {
    }

    public Route(JSONObject json) {
        try (JsonReader reader = new JsonReader(new StringReader(json.toString()))) {
            reader.beginObject();
            while (reader.hasNext()) {
                String jsonName = reader.nextName();
                if (jsonName.equals("routes")) {
                    reader.beginArray();
                    while ((reader.hasNext())) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            jsonName = reader.nextName();
                            if (jsonName.equals("distanceMeters")) {
                                mDistanceMeters = reader.nextLong();
                            } else if (jsonName.equals("duration")) {
                                mDuration = reader.nextString();
                            } else if (jsonName.equals("polyline")) {
                                reader.beginObject();
                                while (reader.hasNext()) {
                                    jsonName = reader.nextName();
                                    if (jsonName.equals("encodedPolyline")) {
                                        mPolyline = reader.nextString();
                                    } else {
                                        reader.skipValue();
                                    }
                                }
                                reader.endObject();
                            } else {
                                reader.skipValue();
                            }
                        }
                        reader.endObject();
                    }
                    reader.endArray();
                }
            }
            reader.endObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Long getDistanceMeters() {
        return mDistanceMeters;
    }

    public void setDistanceMeters(Long distanceMeters) {
        this.mDistanceMeters = distanceMeters;
    }

    public String getDuration() {
        return mDuration;
    }

    public void setDuration(String duration) {
        this.mDuration = duration;
    }

    public String getPolyline() {
        return mPolyline;
    }

    public void setPolyline(String polyline) {
        this.mPolyline = polyline;
    }


    public String getLabel() {
        return mLabel;
    }

    public void setLabel(String label) {
        this.mLabel = label;
    }

}
