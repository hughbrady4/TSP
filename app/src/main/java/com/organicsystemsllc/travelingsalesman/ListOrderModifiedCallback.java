package com.organicsystemsllc.travelingsalesman;

import com.organicsystemsllc.travelingsalesman.ui.maps.MapNode;

import java.util.ArrayList;

public interface ListOrderModifiedCallback {
    void onListOrderModified(ArrayList<MapNode> nodes);
}
