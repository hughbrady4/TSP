package com.organicsystemsllc.travelingsalesman.ui.login;

import com.google.firebase.Timestamp;

public class UserData {

    private long altitude;
    private long bearing;
    private boolean isOnline;
    private long latitude;
    private long longitude;
    private boolean trackLocation;
    private Timestamp updated;

    public UserData() {
    }

    public long getAltitude() {
        return altitude;
    }

    public void setAltitude(long altitude) {
        this.altitude = altitude;
    }

    public long getBearing() {
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

    public long getLatitude() {
        return latitude;
    }

    public void setLatitude(long latitude) {
        this.latitude = latitude;
    }

    public long getLongitude() {
        return longitude;
    }

    public void setLongitude(long longitude) {
        this.longitude = longitude;
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
