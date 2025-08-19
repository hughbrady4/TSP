package com.organicsystemsllc.travelingsalesman.ui.maps;

import static com.organicsystemsllc.travelingsalesman.MainActivity.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
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
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
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
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.AdvancedMarker;
import com.google.android.gms.maps.model.AdvancedMarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapCapabilities;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PinConfig;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.maps.android.PolyUtil;
import com.organicsystemsllc.travelingsalesman.BuildConfig;
import com.organicsystemsllc.travelingsalesman.NodeDetailFragment;
import com.organicsystemsllc.travelingsalesman.NodeListFragment;
import com.organicsystemsllc.travelingsalesman.MainActivity;
import com.organicsystemsllc.travelingsalesman.R;
import com.organicsystemsllc.travelingsalesman.databinding.FragmentMapsBinding;
import com.organicsystemsllc.travelingsalesman.ui.route.Route;
import com.organicsystemsllc.travelingsalesman.ui.route.RouteRequest;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
    private static final char[] LABELS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private Polyline mLine;
    private final OnMapReadyCallback mCallback = new OnMapReadyCallback() {

        @SuppressLint("PotentialBehaviorOverride")
        @Override
        public void onMapReady(GoogleMap map) {
            mMap = map;
            MapCapabilities capabilities = map.getMapCapabilities();
            Log.i(TAG, "Advanced marker enabled? " + capabilities.isAdvancedMarkersAvailable());

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @SuppressLint("PotentialBehaviorOverride")
                @Override
                public boolean onMarkerClick(@NonNull Marker marker) {
                    NodeDetailFragment.newInstance(marker.getTitle()).show(requireActivity()
                            .getSupportFragmentManager(), "dialog");
                    return false;
                }
            });


            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDrag(@NonNull Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(@NonNull Marker marker) {
//                    MapNode node = (MapNode) marker.getTag();
                    String label = marker.getTitle();
//                    if (node != null) {
                    final MutableLiveData<HashMap<String, MapNode>> nodes = mMapsViewModel.getNodes();
                    HashMap<String, MapNode> newList;
                    if (nodes.getValue() != null) {
                        MapNode node = nodes.getValue().get(label);
                        if (node != null) {
                            node.setPosition(marker.getPosition());
                            reverseGeoCode(marker, node);

                            newList = new HashMap<>(nodes.getValue());
                            nodes.setValue(newList);

                            NodeDetailFragment.newInstance(label).show(requireActivity()
                                    .getSupportFragmentManager(), "dialog");
                        }
                    }


//                    }

                }

                @Override
                public void onMarkerDragStart(@NonNull Marker marker) {
                    mMapsViewModel.getRoute().setValue(null);


                }
            });

            mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
                @Override
                public void onPolylineClick(@NonNull Polyline polyline) {
                    NodeListFragment.newInstance().show(requireActivity()
                        .getSupportFragmentManager(), "dialog");
                }
            });

            mMapsViewModel.getLatLng().observe(requireActivity(), latLng -> {
                Log.i(TAG,"Position updated!");
//                addMarkerToMap(latLng, map);
                CameraUpdate update = CameraUpdateFactory.newLatLng(latLng);
                map.moveCamera(update);
            });

//            final LatLng value = mMapsViewModel.getLatLng().getValue();
//            if (value != null) {
//                CameraUpdate update = CameraUpdateFactory.newLatLng(value);
//                map.moveCamera(update);
//            }

            setLocationEnabled();

            setListeners();

            mMapsViewModel.getNodes().observeForever(new Observer<HashMap<String, MapNode>>() {
                @Override
                public void onChanged(HashMap<String, MapNode> mapNodes) {
                    //clear map if nodes are null
                    if (mapNodes == null) {
                        map.clear();
                    }
//                    callRouteApi(mapNodes);
                }
            });

            mMapsViewModel.getRoute().observe(getViewLifecycleOwner(),
                route -> {
                    if (route != null && route.getPolyline() != null) {
                        mLine = addEdgeToMap(route.getPolyline(), map);
                        NodeListFragment.newInstance().show(requireActivity()
                                .getSupportFragmentManager(), "dialog");
                    } else {
                        if (mLine != null) {
                            mLine.remove();
                        }
                    }
            });



//            Button save = mBinding.save;
//            save.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    SaveRouteDialogFragment dialog = new SaveRouteDialogFragment();
//                    dialog.show(requireActivity().getSupportFragmentManager(), "SAVE_DIALOG");
//                }
//            });
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


        final MutableLiveData<HashMap<String, MapNode>> nodes = mMapsViewModel.getNodes();
        HashMap<String, MapNode> newList;
        if (nodes.getValue() != null) {
            newList = new HashMap<>(nodes.getValue());

        } else {
            newList = new HashMap<>();

        }

        int index = newList.size();

        if (index < LABELS.length) {

            String label = String.valueOf(LABELS[index % LABELS.length]);

            PinConfig pinConfig = PinConfig.builder()
                    .setBackgroundColor(Color.WHITE)
                    .setBorderColor(Color.BLACK)
                    .setGlyph(new PinConfig.Glyph(label))
                    .build();

            AdvancedMarkerOptions options = new AdvancedMarkerOptions()
                    .icon(BitmapDescriptorFactory.fromPinConfig(pinConfig))
                    .title(label)
                    .position(position)
                    .draggable(true);

            AdvancedMarker marker = (AdvancedMarker) mMap.addMarker(options);


            MapNode newNode = new MapNode(position, label, false, marker);
            if (marker != null) {
                reverseGeoCode(marker, newNode);
                marker.setTag(newNode);
            }


            newList.put(label, newNode);

            nodes.setValue(newList);
            NodeDetailFragment.newInstance(label).show(requireActivity()
                    .getSupportFragmentManager(), "dialog");
        }


    }

    private Polyline addEdgeToMap(String encodedPolyline, GoogleMap map) {
        if (mLine != null) {
            mLine.remove();
        }
        List<LatLng> path = PolyUtil.decode(encodedPolyline);
        PolylineOptions options = new PolylineOptions()
                .clickable(true)
                .width(25)
                .color(Color.BLUE)
                .geodesic(true);
        path.forEach(new Consumer<LatLng>() {
            @Override
            public void accept(LatLng latLng) {
                options.add(latLng);
            }
        });
        return map.addPolyline(options);
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

        // Initialize the AutocompleteSupportFragment.;

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LOCATION));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.toString());
                addMarkerToMap(place.getLocation());
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(place.getLocation(), 10);

                mMap.moveCamera(update);

            }


            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        }


        mMapsViewModel.getRoute().observeForever(new Observer<Route>() {
            @Override
            public void onChanged(Route route) {
                if (route != null) {
//                    mBinding.getRoute.setEnabled(false);
//                    mBinding.getRoute.setVisibility(View.INVISIBLE);
//                    mBinding.save.setEnabled(true);
//                    mBinding.save.setVisibility(View.VISIBLE);

                } else {
//                    mBinding.getRoute.setEnabled(true);
//                    mBinding.getRoute.setVisibility(View.VISIBLE);
//                    mBinding.save.setEnabled(false);
//                    mBinding.save.setVisibility(View.INVISIBLE);

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
        FirebaseFirestore.getInstance().collection("users")
                .document(currentUser.getUid())
                .collection("routes")
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

    private void reverseGeoCode(Marker marker, MapNode node) {
        Uri.Builder url = new Uri.Builder();
        url.scheme("https")
                .authority("maps.googleapis.com")
                .appendPath("maps")
                .appendPath("api")
                .appendPath("geocode")
                .appendPath("json");

        url.appendQueryParameter("latlng", marker.getPosition().latitude + "," + marker.getPosition().longitude);
        url.appendQueryParameter("result_type", "street_address");
        url.appendQueryParameter("key", BuildConfig.TSP_API_KEY);

        Log.i(TAG, url.toString());

        JsonObjectRequest jsonObjectRequest = new
                JsonObjectRequest(Request.Method.POST, url.build().toString(), null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        GeoCodeResult result = new GeoCodeResult(response);
//                        marker.setSnippet(result.getFormattedAddress());
                        node.setFormattedAddress(result.getFormattedAddress());
                        HashMap<String, MapNode> currentData = mMapsViewModel.getNodes().getValue();
                        if (currentData != null) {
                            currentData.put(node.getLabel(), node);
                            mMapsViewModel.getNodes().setValue(currentData); // Post updated HashMap to LiveData
                        }
                        //add to firebase
                     }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, Objects.requireNonNull(error.getLocalizedMessage()));
            }
        });

        // Add the request to the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(requireActivity());
        queue.add(jsonObjectRequest);

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

    public static class SaveRouteDialogFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction.
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setMessage(R.string.dialog_save_route)
                    .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // START THE GAME!
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancels the dialog.
                        }
                    });
            // Create the AlertDialog object and return it.
            return builder.create();
        }
    }

    public void callRouteApi(HashMap<String, MapNode> nodes) {

        if (nodes == null || nodes.size() < 2) return;


        String url = "https://routes.googleapis.com/directions/v2:computeRoutes";
            RouteRequest routeRequest = new
                RouteRequest(url, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Route route = new Route(response);
                        mMapsViewModel.getRoute().setValue(route);
    //                                addRouteToFirestore(route);
                    }
                }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Failed! " + error.getLocalizedMessage());
                }
            });

        routeRequest.setNodes(new ArrayList<>(nodes.values()));
        Log.i(TAG, String.valueOf(nodes));

        // Add the request to the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(requireActivity());
        queue.add(routeRequest);

    }

}