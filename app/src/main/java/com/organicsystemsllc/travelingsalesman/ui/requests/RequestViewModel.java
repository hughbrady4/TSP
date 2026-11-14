package com.organicsystemsllc.travelingsalesman.ui.requests;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;

import java.util.HashMap;

public class RequestViewModel extends ViewModel {
    private final MutableLiveData<String> requestId = new MutableLiveData<>();
    private final MutableLiveData<HashMap<String, Double>> pickupPos = new MutableLiveData<>();
    private final MutableLiveData<HashMap<String, Double>> destPos = new MutableLiveData<>();

    private final MutableLiveData<String> originAddress = new MutableLiveData<>();
    private final MutableLiveData<String> destinationAddress = new MutableLiveData<>();


    private final MutableLiveData<String> phoneNumber = new MutableLiveData<>();

    private final MutableLiveData<Double> distance  =new MutableLiveData<>();
    private final MutableLiveData<Double> duration  = new MutableLiveData<>();
    private final MutableLiveData<Timestamp> timestamp = new MutableLiveData<>();

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
    public void setDestPos(HashMap<String, Double> position) {
        destPos.setValue(position);
    }
    public LiveData<HashMap<String, Double>> getDestPos() {
        return destPos;
    }

    public MutableLiveData<String> getPhoneNumber() {
        return phoneNumber;
    }

    public MutableLiveData<String> getOriginAddress() {
        return originAddress;
    }

    public MutableLiveData<Double> getDistance() {
        return distance;
    }

    public MutableLiveData<Double> getDuration() {
        return duration;
    }

    public MutableLiveData<Timestamp> getTimestamp() {
        return timestamp;
    }

    public MutableLiveData<String> getDestinationAddress() {
        return destinationAddress;
    }
}
