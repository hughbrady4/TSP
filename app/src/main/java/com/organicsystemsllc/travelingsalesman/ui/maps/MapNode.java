package com.organicsystemsllc.travelingsalesman.ui.maps;


import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.AdvancedMarker;
import com.google.android.gms.maps.model.LatLng;

public class MapNode {

    private LatLng mPosition;
    private String mLabel;
    private boolean mVisited;
    private String mFormattedAddress;
    private AdvancedMarker mMarker;

    public MapNode() {}

    public MapNode(LatLng position, String label, boolean visited, AdvancedMarker marker) {
        this.mPosition = position;
        this.mLabel = label;
        this.mVisited = visited;
        this.mMarker = marker;
    }

    public MapNode(LatLng position, String label, boolean visited, String formattedAddress) {
        this.mPosition = position;
        this.mLabel = label;
        this.mVisited = visited;
        this.mFormattedAddress = formattedAddress;
    }

    public MapNode(String id, boolean visited, String address) {
        this.mLabel = id;
        this.mVisited = visited;
        this.mFormattedAddress = address;
    }

    public LatLng getPosition() {
        return mPosition;
    }

    public void setPosition(LatLng position) {
        this.mPosition = position;
    }

    public String getLabel() {
        return mLabel;
    }

    public void setLabel(String label) {
        this.mLabel = label;
    }

    public boolean isVisited() {
        return mVisited;
    }

    public void setVisited(boolean visited) {
        this.mVisited = visited;
    }

    @NonNull
    @Override
    public String toString() {
        return "MapNode{" +
                "Position=" + mPosition +
                ", Label='" + mLabel  +
                ", Visited=" + mVisited +
                ", Address=" + mFormattedAddress +
                '}';
    }

    public String getFormattedAddress() {
        return mFormattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.mFormattedAddress = formattedAddress;
    }

    public AdvancedMarker getMarker() {
        return mMarker;
    }

}
