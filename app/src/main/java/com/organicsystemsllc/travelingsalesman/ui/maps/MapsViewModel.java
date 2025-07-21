package com.organicsystemsllc.travelingsalesman.ui.maps;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.organicsystemsllc.travelingsalesman.ui.route.Route;

import java.util.ArrayList;
import java.util.LinkedList;


public class MapsViewModel extends ViewModel {

    private final MutableLiveData<Boolean> mAddMarkers;
    private final MutableLiveData<Route> mRoute;
    private final MutableLiveData<Boolean> mTrackToggle;
    private final MutableLiveData<Boolean> mOnlineToggle;
    private final MutableLiveData<LatLng> mLatLng;
    private final MutableLiveData<MapNode> mUserLocation;
    private final MutableLiveData<ArrayList<MapNode>> mNodes;


    public MapsViewModel() {
        mAddMarkers = new MutableLiveData<>();
        mRoute = new MutableLiveData<>();
        mTrackToggle = new MutableLiveData<>();
        mOnlineToggle = new MutableLiveData<>();
        mLatLng = new MutableLiveData<>();
        mUserLocation = new MutableLiveData<>();
        mNodes = new MutableLiveData<>();
    }

    public MutableLiveData<Boolean> getAddMarkers() {
        return mAddMarkers;
    }

    public MutableLiveData<Route> getRoute() {
        return mRoute;
    }

    public MutableLiveData<Boolean> getTrackToggle() {
        return mTrackToggle;
    }

    public MutableLiveData<Boolean> getOnlineToggle() {
        return mOnlineToggle;
    }

    public MutableLiveData<LatLng> getLatLng() {
        return mLatLng;
    }

    public MutableLiveData<MapNode> getUserLocation() {
        return mUserLocation;
    }

    public MutableLiveData<ArrayList<MapNode>> getNodes() {
        return mNodes;
    }
}
