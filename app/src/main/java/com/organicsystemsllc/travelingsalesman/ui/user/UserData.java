package com.organicsystemsllc.travelingsalesman.ui.user;

import com.google.firebase.Timestamp;

public class UserData {

    private float altitude;
    private float bearing;
    private boolean isOnline;
    private float latitude;
    private float longitude;
    private boolean trackLocation;
    private Timestamp updated;

    public UserData() {
    }

    public float getAltitude() {
        return altitude;
    }

    public void setAltitude(long altitude) {
        this.altitude = altitude;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(long bearing) {
        this.bearing = bearing;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public float getLatitude() {
        return latitude;
    }


    public float getLongitude() {
        return longitude;
    }


    public boolean isTrackLocation() {
        return trackLocation;
    }

    public void setTrackLocation(boolean trackLocation) {
        this.trackLocation = trackLocation;
    }

    public Timestamp getUpdated() {
        return updated;
    }

    public void setUpdated(Timestamp updated) {
        this.updated = updated;
    }
}
