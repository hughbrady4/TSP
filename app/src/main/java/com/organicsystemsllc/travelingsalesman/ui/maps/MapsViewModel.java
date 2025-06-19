package com.organicsystemsllc.travelingsalesman.ui.maps;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.organicsystemsllc.travelingsalesman.ui.route.Route;

import java.util.LinkedList;


public class MapsViewModel extends ViewModel {

    private final MutableLiveData<Boolean> mAddMarkers;
    private final MutableLiveData<Route> mRoute;

    private final MutableLiveData<Boolean> mTrackToggle;

    private final MutableLiveData<Boolean> mOnlineToggle;


    public MapsViewModel() {
        mAddMarkers = new MutableLiveData<>();
        mRoute = new MutableLiveData<>();
        mTrackToggle = new MutableLiveData<>();
        mOnlineToggle = new MutableLiveData<>();
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
}
