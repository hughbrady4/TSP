package com.organicsystemsllc.travelingsalesman.ui.maps;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class MapEdge {

    private TextValuePair mDuration;
    private TextValuePair mDistance;
    private String mStatus;
    private String mLabel;

    public MapEdge() {
    }

    public MapEdge(String label, TextValuePair duration, TextValuePair distance, String status) {
        this.mLabel = label;
        this.mDuration = duration;
        this.mDistance = distance;
        this.mStatus = status;
    }

    @NonNull
    @Override
    public String toString() {
        return "MapEdge{" +
                "Label=" + mLabel +
                " Duration=" + mDuration +
                ", Distance=" + mDistance +
                ", Status='" + mStatus + '\'' +
                '}';
    }

    public TextValuePair getDuration() {
        return mDuration;
    }

    public void setDuration(TextValuePair duration) {
        this.mDuration = duration;
    }

    public TextValuePair getDistance() {
        return mDistance;
    }

    public void setDistance(TextValuePair distance) {
        this.mDistance = distance;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        this.mStatus = status;
    }




    public String getLabel() {
        return mLabel;
    }

    public void setLabel(String label) {
        this.mLabel = label;
    }

}
