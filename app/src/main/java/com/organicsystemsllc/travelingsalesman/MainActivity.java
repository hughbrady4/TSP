package com.organicsystemsllc.travelingsalesman;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.DateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.organicsystemsllc.travelingsalesman.databinding.ActivityMainBinding;
import com.organicsystemsllc.travelingsalesman.ui.maps.MapNode;
import com.organicsystemsllc.travelingsalesman.ui.maps.MapsViewModel;
import com.organicsystemsllc.travelingsalesman.ui.route.Route;
import com.organicsystemsllc.travelingsalesman.ui.user.UserData;
import com.organicsystemsllc.travelingsalesman.ui.user.UserViewModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private UserViewModel mUserViewModel;
    public static final String TAG = "TRAVELING_SALESMAN";
    private final ActivityResultLauncher<Intent> mSignInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );
    private FusedLocationProviderClient mFusedLocationClient;
    private ActivityResultLauncher<String[]> mLocationPermissionRequest;
    private PendingIntent mPendingIntent;
    private MapsViewModel mMapsViewModel;
    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        setSupportActionBar(mBinding.appBarNavDrawer.toolbar);
        mDrawer = mBinding.drawerLayout;
        NavigationView navigationView = mBinding.navView2;

        mUserViewModel = new ViewModelProvider(this).get(UserViewModel.class);


        // Initialize Firebase Auth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(firebaseAuth -> {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if(currentUser != null){
                mUserViewModel.getUser().setValue(currentUser);
                Uri uri = currentUser.getPhotoUrl();
                mUserViewModel.getPhotoUri().setValue(currentUser.getPhotoUrl());
//                ImageView image = navigationView.getHeaderView(0).findViewById(R.id.imageView);
//                image.setImageURI(currentUser.getPhotoUrl());
                getUserData(currentUser);

                ImageView image = navigationView.getHeaderView(0).findViewById(R.id.imageViewNav);
                TextView username = navigationView.getHeaderView(0).findViewById(R.id.username);
                if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                    username.setText(currentUser.getDisplayName());
                } else {
                    username.setText(currentUser.getEmail());
                }

                Picasso.get()
                        .load(uri)
//                        .placeholder(R.drawable.placeholder_image) // Optional placeholder drawable
//                        .error(R.drawable.error_image) // Optional error drawable
                        .into(image);


            } else {
                mUserViewModel.getUser().setValue(null);
                mUserViewModel.getPhotoUri().setValue(null);
            }

        });

        mUserViewModel.getPhotoUri().observe(this, uri -> {

        });






        mMapsViewModel = new ViewModelProvider(this).get(MapsViewModel.class);
//        mUserViewModel.getTrackToggle().observeForever(track -> {
//            Log.i(TAG, "Track indicator updated: " + track.toString());
//            if (Boolean.TRUE.equals(track)) {
//                startLocationUpdates();
//            } else {
//                stopLocationUpdates();
//            }
//        });




        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_maps, R.id.navigation_routes)
                .setOpenableLayout(mDrawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_nav_drawer);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        //Location permission request
//        mLocationPermissionRequest = registerForActivityResult(new ActivityResultContracts
//                .RequestMultiplePermissions(), result -> {
//
//            Boolean fineGranted = result
//                    .getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION,
//                            false);
//            Boolean coarseGranted = result
//                    .getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION,
//                            false);
//
//            Boolean backgroundGranted = result
//                    .getOrDefault(Manifest.permission.ACCESS_BACKGROUND_LOCATION,
//                            false);
//            if (Boolean.FALSE.equals(backgroundGranted)) {
//                return;
//            }
//
//            if (Boolean.TRUE.equals(fineGranted) || Boolean.TRUE.equals(coarseGranted)) {
//                startLocationUpdates();
//            }
//        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        // Define a variable to hold the Places API key.
        String apiKey = BuildConfig.TSP_API_KEY;

        // Log an error if apiKey is not set.
        if (TextUtils.isEmpty(apiKey)) {
            Log.e("Places test", "No api key");
            finish();
            return;
        }

        // Initialize the SDK
        Places.initializeWithNewPlacesApiEnabled(getApplicationContext(), apiKey);

        // Create a new PlacesClient instance
        PlacesClient placesClient = Places.createClient(this);
    }

    private void getUserData(FirebaseUser currentUser) {
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid());


        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    UserData data = document.toObject(UserData.class);

                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    if (data != null) {
                        Boolean online = data.isOnline();
                        Boolean tracking = data.isOnline();
//                            Timestamp updated = data.getUpdated();

                        mUserViewModel.getOnlineToggle().setValue(online);
                        mUserViewModel.getTrackToggle().setValue(tracking);
//                            mUserViewModel.getText().setValue(updated.toString());
                    }



                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });


        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                    ? "Local" : "Server";

            if (snapshot != null && snapshot.exists() && source.equalsIgnoreCase("server")) {
                Log.d(TAG, source + " data: " + snapshot.getData());
                UserData data = snapshot.toObject(UserData.class);
                if (data != null) {
                    Timestamp updated = data.getUpdated();
                    String dateString = DateFormat.getDateTimeInstance().format(updated.toDate());
                    mUserViewModel.getText().setValue(dateString);

                    float lat = data.getLatitude();
                    float lng = data.getLongitude();
                    Log.d(TAG, "latLng: " + lat + ", " + lng);
                    LatLng latLng = new LatLng(lat,lng);
                    Log.i(TAG, latLng.toString());
                    mMapsViewModel.getLatLng().setValue(latLng);

                }
            } else {
                Log.d(TAG, source + " data: null");
            }
        });


        CollectionReference routeRef = FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid())
                .collection("routes");

        routeRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                List<MapNode> nodes = new ArrayList<>();
                for (QueryDocumentSnapshot doc : value) {
                    if (doc.get("name") != null) {


                    }
                }

                for (DocumentChange dc : value.getDocumentChanges()) {
                    switch (dc.getType()) {
                        case ADDED:
                            Log.d(TAG, "New route: " + dc.getDocument().getData());
                            Route route = dc.getDocument().toObject(Route.class);
//                            mMapsViewModel.getRoute().setValue(route);
                            break;
                        case MODIFIED:
                            Log.d(TAG, "Modified city: " + dc.getDocument().getData());
                            break;
                        case REMOVED:
                            Log.d(TAG, "Removed city: " + dc.getDocument().getData());
                            break;
                    }
                }

            }

        });




    }

    private void startLocationUpdates() {

        int finePermission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        int coursePermission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        int version = Build.VERSION.SDK_INT;
        int versionBG = Build.VERSION_CODES.Q;
        Log.i(TAG, version + ", " + versionBG);
        int backgroundPermission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        if (backgroundPermission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, Integer.toString(finePermission));
            Log.i(TAG, Integer.toString(coursePermission));
            Log.i(TAG, Integer.toString(backgroundPermission));

            mLocationPermissionRequest.launch(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            });
        } else {
            Log.i(TAG, "Has background location permission");
        }

        LocationRequest locationRequest = new LocationRequest.Builder(10000)
                .setDurationMillis(Long.MAX_VALUE)
                .setMinUpdateDistanceMeters(1)
                .build();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            return;
        }

        Intent intent = new Intent(this, LocationReceiver.class);
        intent.putExtra("com.organicsystemsllc.travelingsalesman.DATA", currentUser.getUid());
        intent.setAction("com.organicsystemsllc.travelingsalesman.ACTION_UPDATE_DATA");

        mPendingIntent = PendingIntent
                .getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE);

        mFusedLocationClient.requestLocationUpdates(locationRequest, mPendingIntent);

    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mPendingIntent);
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            assert user != null;
            mUserViewModel.getUser().setValue(user);
            Toast.makeText(this, "User sign in.", Toast.LENGTH_SHORT).show();

        } else {
            //some error logging
            mUserViewModel.getUser().setValue(null);
            if (response != null && response.getError() != null
                    && response.getError().getLocalizedMessage() != null) {
                Log.e(TAG, response.getError().getLocalizedMessage());
                Toast.makeText(this, response.getError().getLocalizedMessage(), Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void signIn() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());
//                new AuthUI.IdpConfig.FacebookBuilder().build());
//                new AuthUI.IdpConfig.TwitterBuilder().build());

        // Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.mipmap.ic_launcher)
                .setTheme(R.style.Theme_TravelingSalesman)
                .build();
        mSignInLauncher.launch(signInIntent);
    }

    private void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(task -> mUserViewModel.getUser().setValue(null));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_sign_in) {
            signIn();
            return true;
        } else if (id == R.id.action_sign_out) {
            signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_nav_drawer);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    private void search() {


    }

}