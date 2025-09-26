package com.organicsystemsllc.travelingsalesman.ui.requests;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

public class RequestViewModel extends ViewModel {
    private final MutableLiveData<String> requestId = new MutableLiveData<>();
    private final MutableLiveData<HashMap<String, Double>> pickupPos = new MutableLiveData<>();
    public void setRequestId(String id) {
        requestId.setValue(id);
    }
    public LiveData<String> getRequestId() {
        return requestId;
    }
    public void setPickupPos(HashMap<String, Double> position) {
        pickupPos.setValue(position);
    }
    public LiveData<HashMap<String, Double>> getPickupPos() {
        return pickupPos;
    }

}
