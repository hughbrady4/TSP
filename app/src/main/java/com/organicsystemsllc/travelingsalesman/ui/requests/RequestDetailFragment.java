package com.organicsystemsllc.travelingsalesman.ui.requests;

import static com.organicsystemsllc.travelingsalesman.MainActivity.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.AdvancedMarker;
import com.google.android.gms.maps.model.AdvancedMarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapCapabilities;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PinConfig;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.maps.android.PolyUtil;
import com.organicsystemsllc.travelingsalesman.BuildConfig;
import com.organicsystemsllc.travelingsalesman.MainActivity;
import com.organicsystemsllc.travelingsalesman.NodeListFragment;
import com.organicsystemsllc.travelingsalesman.R;
import com.organicsystemsllc.travelingsalesman.databinding.FragmentRequestsBinding;
import com.organicsystemsllc.travelingsalesman.ui.maps.GeoCodeResult;
import com.organicsystemsllc.travelingsalesman.ui.maps.MapNode;
import com.organicsystemsllc.travelingsalesman.ui.requests.RequestViewModel;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
                NodeListFragment.newInstance().show(requireActivity()
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

            mRequestViewModel.getPickupPos().observe(requireActivity(), new Observer<HashMap<String, Double>>() {
                @Override
                public void onChanged(HashMap<String, Double> stringDoubleHashMap) {
                    addMarkerToMap(stringDoubleHashMap);
                }
            });



        }
    };

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
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        final DocumentReference docRef = db.collection("requests").document(requestId);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
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

                    HashMap<String, Double> pickup = (HashMap<String, Double>) snapshot.get("pickup");
                    if (pickup != null) {
                        mRequestViewModel.setPickupPos(pickup);

                    }


                } else {
                    Log.d(TAG, source + " data: null");
                }


            }
        });

    }

    @Override
    //write user location to cloud and add marker to map when current location is clicked
    public void onMyLocationClick(@NonNull Location location) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(),"Please login to publish location.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("latitude", location.getLatitude());
        userMap.put("longitude", location.getLongitude());
        userMap.put("altitude", location.getAltitude());
        userMap.put("bearing", location.getBearing());
        userMap.put("updated", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance().collection("requests").document(currentUser.getUid())
            .set(userMap, SetOptions.merge())
            .addOnSuccessListener(aVoid -> {
                Snackbar.make(requireView(), "Location updated on server.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Log.i(MainActivity.TAG, "DocumentSnapshot successfully written!");
            })
            .addOnFailureListener(e -> {
                Snackbar.make(requireView(), "Failed to update server.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Log.e(MainActivity.TAG, "Error writing document", e);
            });

//        addMarkerToMap(new LatLng(location.getLatitude(), location.getLongitude()));

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase Auth
        mRequestViewModel = new ViewModelProvider(requireActivity()).get(RequestViewModel.class);

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

    private void addMarkerToMap(HashMap<String, Double> position) {


        Double lat = position.get("lat");
        Double lng = position.get("lng");
        LatLng latlng = new LatLng(lat, lng);

        PinConfig pinConfig = PinConfig.builder()
                .setBackgroundColor(Color.WHITE)
                .setBorderColor(Color.BLACK)
                .setGlyph(new PinConfig.Glyph("label"))
                .build();

        AdvancedMarkerOptions options = new AdvancedMarkerOptions()
                .icon(BitmapDescriptorFactory.fromPinConfig(pinConfig))
                .title("label")
                .position(latlng)
                .draggable(true);

        AdvancedMarker marker = (AdvancedMarker) mMap.addMarker(options);



//        NodeListFragment.newInstance().show(requireActivity()
//                .getSupportFragmentManager(), "dialog");


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