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
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.AdvancedMarker;
import com.google.android.gms.maps.model.AdvancedMarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapCapabilities;
import com.google.android.gms.maps.model.PinConfig;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.organicsystemsllc.travelingsalesman.MainActivity;
import com.organicsystemsllc.travelingsalesman.R;
import com.organicsystemsllc.travelingsalesman.databinding.FragmentMapsBinding;
import com.organicsystemsllc.travelingsalesman.ui.route.Route;
import com.organicsystemsllc.travelingsalesman.ui.route.RouteRequest;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class MapsFragment extends Fragment implements
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraMoveCanceledListener,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnMyLocationClickListener {

    private GoogleMap mMap;
    private static final char[] LABELS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private final OnMapReadyCallback mCallback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap map) {
            mMap = map;
            MapCapabilities capabilities = map.getMapCapabilities();
            Log.i(TAG, "Advanced marker enabled? " + capabilities.isAdvancedMarkersAvailable());


//            mMapsViewModel.getLatLng().observe(requireActivity(), latLng -> {
//                Log.i(TAG,"Position updated!");
//                addMarkerToMap(latLng, map);
//            });







            setLocationEnabled();

            setListeners();

            getNodes();
        }
    };

    private void setListeners() {

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                addMarkerToMap(latLng);
            }
        });
    }


    private void getNodes() {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(),"Please login to get data.", Toast.LENGTH_SHORT).show();
            return;
        }


    }

    private MapsViewModel mMapsViewModel;

    private ActivityResultLauncher<String[]> mLocationPermissionRequest;
    private FragmentMapsBinding mBinding;


    private void addMarkerToMap(LatLng position) {


        final MutableLiveData<ArrayList<MapNode>> nodes = mMapsViewModel.getNodes();
        ArrayList<MapNode> newList;
        if (nodes.getValue() != null) {
            newList = new ArrayList<MapNode>(nodes.getValue());

        } else {
            newList = new ArrayList<MapNode>();

        }
        int index = newList.size();
        String label = String.valueOf(LABELS[index % LABELS.length]);

        PinConfig pinConfig = PinConfig.builder()
                .setBackgroundColor(Color.RED)
                .setBorderColor(Color.GREEN)
                .setGlyph(new PinConfig.Glyph(label))
                .build();

        AdvancedMarkerOptions options = new AdvancedMarkerOptions()
                .icon(BitmapDescriptorFactory.fromPinConfig(pinConfig))
                .title(label)
                .position(position);

        AdvancedMarker marker = (AdvancedMarker) mMap.addMarker(options);

        MapNode newNode = new MapNode(position, label, false, marker);

        newList.add(newNode);

        nodes.setValue(newList);

    }

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
        Button getRoute = mBinding.getRoute;
        getRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final MutableLiveData<ArrayList<MapNode>> nodes = mMapsViewModel.getNodes();
                if (nodes.getValue() != null) {



                    String url = "https://routes.googleapis.com/directions/v2:computeRoutes";
                    RouteRequest routeRequest = new
                        RouteRequest(url, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Route route = new Route(response);
                                mMapsViewModel.getRoute().setValue(route);
                                addRouteToFirestore(route);
                            }
                        }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "Failed! " + error.getLocalizedMessage());
                        }
                    });



                routeRequest.setOrigin(nodes.getValue().get(0).getPosition());
                routeRequest.setDestination(nodes.getValue().get(nodes.getValue().size()-1).getPosition());
//                LinkedList<MapNode> copyNodes = (LinkedList<MapNode>) nodes.getValue().clone();
//                routeRequest.setMapNodes(copyNodes);
                // Add the request to the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(requireActivity());
                queue.add(routeRequest);

                }


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
            .addOnSuccessListener(aVoid -> {
                Snackbar.make(requireView(), "Location updated on server.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Log.d(MainActivity.TAG, "DocumentSnapshot successfully written!");
            })
            .addOnFailureListener(e -> {
                Snackbar.make(requireView(), "Failed to update server.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Log.w(MainActivity.TAG, "Error writing document", e);
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

        Log.i(TAG, "Location: " + fineGranted + ", " + courseGranted);
        //if both fine and course permission are not allow, then make call to request
        //else enable location on google map
        if (fineGranted != PackageManager.PERMISSION_GRANTED &&
                courseGranted != PackageManager.PERMISSION_GRANTED) {

            Log.i(TAG,"Request Permission!");
            mLocationPermissionRequest.launch(new String[] {
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,

            });
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.setOnMyLocationClickListener(MapsFragment.this);

        }
    }

    public void addRouteToFirestore(Route route) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(),"Please login to add location.", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid())
                .collection("nodes")
                .add(route)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

    }

    public void addNodeToFirestore() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(),"Please login to add location.", Toast.LENGTH_SHORT).show();
            return;
        }
        LatLng latLng = mMap.getCameraPosition().target;
        FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid())
                .collection("nodes")
                .add(latLng)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

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