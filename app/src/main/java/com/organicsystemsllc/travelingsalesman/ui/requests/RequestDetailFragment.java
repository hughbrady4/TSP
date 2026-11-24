package com.organicsystemsllc.travelingsalesman.ui.requests;

import static com.organicsystemsllc.travelingsalesman.MainActivity.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.AdvancedMarker;
import com.google.android.gms.maps.model.AdvancedMarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapCapabilities;
import com.google.android.gms.maps.model.PinConfig;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.organicsystemsllc.travelingsalesman.MainActivity;
import com.organicsystemsllc.travelingsalesman.NodeDetailFragment;
import com.organicsystemsllc.travelingsalesman.NodeListFragment;
import com.organicsystemsllc.travelingsalesman.R;
import com.organicsystemsllc.travelingsalesman.databinding.FragmentRequestsBinding;
import com.organicsystemsllc.travelingsalesman.ui.user.UserViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestDetailFragment extends Fragment implements
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraMoveCanceledListener,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnMyLocationClickListener {

    private GoogleMap mMap;
    public static final char[] LABELS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private Polyline mLine;
    private RequestViewModel mRequestViewModel;
    private ActivityResultLauncher<String[]> mLocationPermissionRequest;
    private final OnMapReadyCallback mCallback = new OnMapReadyCallback() {

        @SuppressLint("PotentialBehaviorOverride")
        @Override
        public void onMapReady(GoogleMap map) {

            mMap = map;
            MapCapabilities capabilities = map.getMapCapabilities();
            Log.i(TAG, "Advanced marker enabled? " + capabilities.isAdvancedMarkersAvailable());



            mMap.setOnMarkerClickListener(marker -> {
                NodeDetailFragment.newInstance(marker.getTitle()).show(requireActivity()
                        .getSupportFragmentManager(), "dialog");
                return false;
            });


            mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
                @Override
                public void onPolylineClick(@NonNull Polyline polyline) {
                    NodeListFragment.newInstance().show(requireActivity()
                            .getSupportFragmentManager(), "dialog");
                }
            });


            mMap.setOnCameraMoveListener(RequestDetailFragment.this);

//            mRequestViewModel.getPickupPos().observe(requireActivity(), new Observer<HashMap<String, Double>>() {
//                @Override
//                public void onChanged(HashMap<String, Double> stringDoubleHashMap) {
//                    LatLng latLng = addMarkerToMap(stringDoubleHashMap);
//                    LatLngBounds latLngBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
//                    LatLngBounds bounds = latLngBounds.including(latLng);
//                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 10);
//                    map.animateCamera(cu);
//                }
//            });

//            mRequestViewModel.getDestPos().observe(requireActivity(), new Observer<HashMap<String, Double>>() {
//                @Override
//                public void onChanged(HashMap<String, Double> stringDoubleHashMap) {
//                    LatLng latLng = addMarkerToMap(stringDoubleHashMap);
//                    LatLngBounds latLngBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
//                    LatLngBounds bounds = latLngBounds.including(latLng);
//                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 10);
//                    map.animateCamera(cu);
//                }
//            });

            mUserViewModel.getLatLng().observe(requireActivity(), new Observer<LatLng>() {
                @Override
                public void onChanged(LatLng latLng) {
                    if (latLng != null) {
//                        LatLngBounds latLngBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
//                        LatLngBounds bounds = latLngBounds.including(latLng);
                        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(latLng, 11);
                        mMap.animateCamera(cu);
                        addMarkerToMap(latLng);
                    }

                }
            });

            setLocationEnabled();

        }
    };
    private ListenerRegistration mRequestListener;
    private UserViewModel mUserViewModel;
    private AdvancedMarker mDriverMarker;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        FragmentRequestsBinding mBinding = FragmentRequestsBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(mCallback);
        }

        String requestId = mRequestViewModel.getRequestId().getValue();
        if (requestId == null) { return; }

        FirebaseFirestore db = FirebaseFirestore.getInstance("travelingsalesman");


        final DocumentReference docRef = db.collection("requests").document(requestId);
        mRequestListener = docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, source + " data: " + snapshot.getData());
                    Map<String, Object> data = snapshot.getData();

                    HashMap<String, Double> pickup = (HashMap<String, Double>) snapshot.get("pickup");
                    HashMap<String, Double> destination = (HashMap<String, Double>) snapshot.get("destination");
                    if (pickup != null) {
                        mRequestViewModel.setPickupPos(pickup);
                        mRequestViewModel.setDestPos(destination);

                    }
                    if (data != null) {
                        String phone = (String) data.get("phoneNumber");
                        mRequestViewModel.getPhoneNumber().setValue(phone);
                        String originAddress = (String) data.get("A");
                        mRequestViewModel.getOriginAddress().setValue(originAddress);
                        String destinationAddress = (String) data.get("B");
                        mRequestViewModel.getDestinationAddress().setValue(destinationAddress);

                        Double distance = (Double) data.get("tripDistance");
                        Double duration = (Double) data.get("tripDuration");

                        mRequestViewModel.getDistance().setValue(distance);
                        mRequestViewModel.getDuration().setValue(duration);

                        Timestamp timestamp = (Timestamp) data.get("requestTS");
                        mRequestViewModel.getTimestamp().setValue(timestamp);

                    }


                } else {
                    Log.d(TAG, source + " d-+ata: null");
                }


            }
        });

        mRequestViewModel.getPhoneNumber().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                TextView phoneView = view.findViewById(R.id.tv_Phone);
                phoneView.setText(s);
            }
        });

        mRequestViewModel.getOriginAddress().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                TextView pickupView = view.findViewById(R.id.tv_pickup);
                pickupView.setText(s);
            }
        });

        mRequestViewModel.getDestinationAddress().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                TextView destinationView = view.findViewById(R.id.tv_destination);
                destinationView.setText(s);
            }
        });

        mRequestViewModel.getDistance().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double miles) {
                TextView durationView = view.findViewById(R.id.tv_distance);
//                double miles = ((double) meters / 1000) * 0.621371;
                if (miles != null) {
                    durationView.setText(getString(R.string.meters, miles));
                }
            }
        });

        mRequestViewModel.getDuration().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double minutes) {
                TextView durationView = view.findViewById(R.id.tv_duration);
//                long minutes = (millis / 1000) / 60;
                if (minutes != null) {
                    durationView.setText(getString(R.string.minutes, minutes));
                }
            }
        });
        mRequestViewModel.getTimestamp().observe(getViewLifecycleOwner(), new Observer<Timestamp>() {
            @Override
            public void onChanged(Timestamp timestamp) {
                Date tsDate = timestamp.toDate();
                TextView tsView = view.findViewById(R.id.tv_timestamp);
                tsView.setText(tsDate.toString());
            }
        });

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mRequestListener != null) {
            mRequestListener.remove();
            mRequestListener = null;
        }
    }

    @Override
    //write user location to cloud and add marker to map when current location is clicked
    public void onMyLocationClick(@NonNull Location location) {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(),"Please login to publish location.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> posMap = new HashMap<>();
        posMap.put("latitude", location.getLatitude());
        posMap.put("longitude", location.getLongitude());
        posMap.put("altitude", location.getAltitude());
        posMap.put("bearing", location.getBearing());
        posMap.put("updated", FieldValue.serverTimestamp());

        DocumentReference driverRef = FirebaseFirestore.getInstance("travelingsalesman").collection("drivers").document(currentUser.getUid());


        String requestId = mRequestViewModel.getRequestId().getValue();
        if (requestId == null) {
            driverRef.set(posMap, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });

            return;
        }

        WriteBatch batch = FirebaseFirestore.getInstance("travelingsalesman").batch();
        Map<String, Object> driverMap = new HashMap<>();
        driverMap.put("driverPos", posMap);

        DocumentReference requestRef = FirebaseFirestore.getInstance("travelingsalesman").collection("requests").document(requestId);
        batch.set(requestRef, driverMap, SetOptions.merge());
        batch.set(driverRef, posMap, SetOptions.merge());

        batch.commit().addOnSuccessListener(aVoid -> {
            Snackbar.make(requireView(), "Location updated on server.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            Log.i(MainActivity.TAG, "Batch successfully written!");
        })
        .addOnFailureListener(e -> {
            Snackbar.make(requireView(), "Failed to update server.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            Log.e(MainActivity.TAG, "Error writing batch", e);
        });


    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase Auth
        mRequestViewModel = new ViewModelProvider(requireActivity()).get(RequestViewModel.class);
        mUserViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        mLocationPermissionRequest = registerForActivityResult(new ActivityResultContracts
                .RequestMultiplePermissions(), result -> {

            Boolean fineGranted = result
                    .getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION,
                            false);
            Boolean coarseGranted = result
                    .getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION,
                            false);

            Log.i(TAG,"Request Permission Result: " + fineGranted + ", " + coarseGranted);

            if (fineGranted != null && fineGranted) {
                setLocationEnabled();
            } else if (coarseGranted != null && coarseGranted ) {
                setLocationEnabled();
            }
        });
    }

    private void setLocationEnabled() {
        int fineGranted = ActivityCompat
                .checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        int courseGranted = ActivityCompat
                .checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION);

        Log.i(TAG, "Location: " + fineGranted + ", " + courseGranted);
        //if both fine and course permission are not allow, then make call to request
        //else enable location on google map
        if (fineGranted != PackageManager.PERMISSION_GRANTED &&
                courseGranted != PackageManager.PERMISSION_GRANTED) {

            Log.i(TAG,"Request Permission!");
            mLocationPermissionRequest.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,

            });
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.setOnMyLocationClickListener(RequestDetailFragment.this);
        }
    }

    private void addMarkerToMap(LatLng position) {

        if (position == null) {
            if (mDriverMarker != null) {
                mDriverMarker.remove();
            }
            return;
        }

        if (mDriverMarker != null) {
            mDriverMarker.setPosition(position);
        } else {

            PinConfig pinConfig = PinConfig.builder()
                    .setBackgroundColor(Color.WHITE)
                    .setBorderColor(Color.BLACK)
                    .setGlyph(new PinConfig.Glyph("Driver"))
                    .build();

            AdvancedMarkerOptions options = new AdvancedMarkerOptions()
                    .icon(BitmapDescriptorFactory.fromPinConfig(pinConfig))
                    .title("Driver")
                    .position(position)
                    .draggable(true);

            mDriverMarker = (AdvancedMarker) mMap.addMarker(options);
        }
    }

    private void addRequestToMap(LatLng position) {

        if (position == null) {
            if (mDriverMarker != null) {
                mDriverMarker.remove();
            }
            return;
        }

        if (mDriverMarker != null) {
            mDriverMarker.setPosition(position);
        } else {

            PinConfig pinConfig = PinConfig.builder()
                    .setBackgroundColor(Color.WHITE)
                    .setBorderColor(Color.BLACK)
                    .setGlyph(new PinConfig.Glyph("Driver"))
                    .build();

            AdvancedMarkerOptions options = new AdvancedMarkerOptions()
                    .icon(BitmapDescriptorFactory.fromPinConfig(pinConfig))
                    .title("Driver")
                    .position(position)
                    .draggable(true);

            mDriverMarker = (AdvancedMarker) mMap.addMarker(options);
        }
    }

    private Polyline addEdgeToMap(ArrayList path) {
        if (mLine != null) {
            mLine.remove();
        }
        PolylineOptions options = new PolylineOptions()
                .clickable(true)
                .width(25)
                .color(Color.BLUE)
                .geodesic(true);



        return mMap.addPolyline(options);
    }


    private void loadRequests() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        FirebaseFirestore mFireStore = FirebaseFirestore.getInstance("travelingsalesman");

        if (user != null) {
            mFireStore.collection("requests")
//                    .whereEqualTo("state", "CA")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot snapshots,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null || snapshots == null) {
                        Log.w(MainActivity.TAG, "listen:error", e);
                        return;
                    }

                    List<Request> requests = new ArrayList<>();
                    for (QueryDocumentSnapshot request : snapshots) {
                        Request request1 = request.toObject(Request.class);
                        request1.setId(request.getId());
                        requests.add(request1);
                    }

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                Log.d(MainActivity.TAG, "New request: " + dc.getDocument().getData());
                                break;
                            case MODIFIED:
                                Log.d(MainActivity.TAG, "Modified request: " + dc.getDocument().getData());
                                break;
                            case REMOVED:
                                Log.d(MainActivity.TAG, "Removed request: " + dc.getDocument().getData());
                                break;
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onCameraIdle() {}

    @Override
    public void onCameraMoveCanceled() {}

    @Override
    public void onCameraMove() {
        final LatLngBounds latLngBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
    }

    @Override
    public void onCameraMoveStarted(int i) {}


}