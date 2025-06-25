package com.organicsystemsllc.travelingsalesman.ui.maps;

import static com.organicsystemsllc.travelingsalesman.MainActivity.TAG;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.AdvancedMarker;
import com.google.android.gms.maps.model.AdvancedMarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapCapabilities;
import com.google.android.gms.maps.model.PinConfig;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.organicsystemsllc.travelingsalesman.MainActivity;
import com.organicsystemsllc.travelingsalesman.R;
import com.organicsystemsllc.travelingsalesman.databinding.FragmentMapsBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class MapsFragment extends Fragment implements
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraMoveCanceledListener,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnMyLocationClickListener {

    private GoogleMap mMap;
    private final OnMapReadyCallback mCallback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap map) {
            mMap = map;
            MapCapabilities capabilities = map.getMapCapabilities();
            Log.i(TAG, "Advanced marker enabled? " + capabilities.isAdvancedMarkersAvailable());

//            mMap.setOnCameraIdleListener();
//            mMap.setOnCameraMoveStartedListener(this);
//            mMap.setOnCameraMoveListener(this);
//            mMap.setOnCameraMoveCanceledListener(this);


            mMapsViewModel.getLatLng().observe(requireActivity(), new Observer<LatLng>() {
                @Override
                public void onChanged(LatLng latLng) {
                    Log.i(TAG,"Position updated!");
                    addMarkerToMap(latLng, map);
                }
            });


            setLocationEnabled();
            map.setOnMyLocationClickListener(MapsFragment.this);

        }
    };
    private MapsViewModel mMapsViewModel;
    private final LinkedList<MapNode> mMapNodes = new LinkedList<>();

    private final LinkedList<AdvancedMarker> mMarkers = new LinkedList<>();
    private final ArrayList<Polyline> mMapEdges = new ArrayList<>();
    private static final char[] LABELS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private FragmentMapsBinding mMapsBinding;
    private final FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
    private ActivityResultLauncher<String[]> mLocationPermissionRequest;
    private FragmentMapsBinding mBinding;


    private void addMarkerToMap(LatLng position, @NonNull GoogleMap map) {
        int index = mMarkers.size();
        String label = String.valueOf(LABELS[index % LABELS.length]);
        // Set the glyph text.

        PinConfig pinConfig = PinConfig.builder()
                .setBackgroundColor(Color.RED)
                .setBorderColor(Color.GREEN)
                .setGlyph(new PinConfig.Glyph(label))
                .build();

        AdvancedMarkerOptions options = new AdvancedMarkerOptions()
                .icon(BitmapDescriptorFactory.fromPinConfig(pinConfig))
                .title(label)
                .position(position);

        AdvancedMarker marker = (AdvancedMarker) map.addMarker(options);
        
        assert marker != null;
        marker.setDraggable(true);
        mMarkers.add(marker);
        MapNode node = new MapNode(position, label, false, marker);
        mMapNodes.add(node);
        mMapsViewModel.getUserLocation().setValue(node);
        marker.setTag(node);
//        reverseGeoCode(marker, node);
//        addToDatabase(node);

    }

    private Polyline addEdgeToMap(@NonNull ArrayList<MapNode> nodes, @NonNull GoogleMap map) {
        PolylineOptions options = new PolylineOptions()
                .clickable(true);
        nodes.forEach(new Consumer<MapNode>() {
            @Override
            public void accept(MapNode node) {
                options.add(node.getPosition());
            }
        });
        return map.addPolyline(options);
    }

//    private Polyline addEdgeToMap(String encodedPolyline, GoogleMap map) {
//        List<LatLng> path = PolyUtil.decode(encodedPolyline);
//        PolylineOptions options = new PolylineOptions().clickable(true);
//        path.forEach(new Consumer<LatLng>() {
//            @Override
//            public void accept(LatLng latLng) {
//                options.add(latLng);
//            }
//        });
//        return map.addPolyline(options);
//    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = FragmentMapsBinding.inflate(inflater, container, false);
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

//        FloatingActionButton fab;
        Button addBtn = mBinding.addNode;
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
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

        FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid())
            .set(userMap, SetOptions.merge())
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Snackbar.make(requireView(), "Location updated on server.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    Log.d(MainActivity.TAG, "DocumentSnapshot successfully written!");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Snackbar.make(requireView(), "Failed to update server.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    Log.w(MainActivity.TAG, "Error writing document", e);
                }
            });

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Firebase Auth

        mMapsViewModel = new ViewModelProvider(requireActivity()).get(MapsViewModel.class);

        mLocationPermissionRequest = registerForActivityResult(new ActivityResultContracts
                .RequestMultiplePermissions(), result -> {

            Boolean fineGranted = result
                    .getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION,
                            false);
            Boolean coarseGranted = result
                    .getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION,
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

        Log.i(TAG, "Location: " + fineGranted + ", " + PackageManager.PERMISSION_GRANTED);
        if (fineGranted != PackageManager.PERMISSION_GRANTED &&
                courseGranted != PackageManager.PERMISSION_GRANTED) {

            Log.i(TAG,"Request Permission!");
            mLocationPermissionRequest.launch(new String[] {
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,

            });
            return;
        }
        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    @Override
    public void onCameraIdle() {

    }

    @Override
    public void onCameraMoveCanceled() {

    }

    @Override
    public void onCameraMove() {

    }

    @Override
    public void onCameraMoveStarted(int i) {

    }
}