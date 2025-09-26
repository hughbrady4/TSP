package com.organicsystemsllc.travelingsalesman.ui.requests;

import com.google.firebase.Timestamp;

public class Request {

    private Timestamp requestTS;
    private String mId;

    public Timestamp getRequestTS() {
        return requestTS;
    }

    public void setRequestTS(Timestamp ts) {
        this.requestTS = ts;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        this.mId = id;
    }
}
