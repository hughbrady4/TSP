package com.organicsystemsllc.travelingsalesman;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LocationReceiver extends BroadcastReceiver {
    private final FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), "com.organicsystemsllc.travelingsalesman.ACTION_UPDATE_DATA")) {
            String data = intent.getStringExtra("com.organicsystemsllc.travelingsalesman.DATA");
            // Do something with the data, for example send it to a data repository:
            if (LocationResult.hasResult(intent)) {


                LocationResult locationResult = LocationResult.extractResult(intent);

                assert locationResult != null;
                Location location = locationResult.getLastLocation();
                assert location != null;

                LocationAvailability.hasLocationAvailability(intent);
                LocationAvailability.extractLocationAvailability(intent);

                if (data != null) {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("latitude", location.getLatitude());
                    userMap.put("longitude", location.getLongitude());
                    userMap.put("altitude", location.getAltitude());
                    userMap.put("bearing", location.getBearing());
                    userMap.put("updated", FieldValue.serverTimestamp());
                    mFirestore.collection("users").document(data)
                            .set(userMap, SetOptions.merge())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(MainActivity.TAG, "DocumentSnapshot successfully written!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(MainActivity.TAG, "Error writing document", e);
                                }
                            });

                }
            }
        }
    }
}
