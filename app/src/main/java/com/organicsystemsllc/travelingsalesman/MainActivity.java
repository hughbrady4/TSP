package com.organicsystemsllc.travelingsalesman;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.DateFormat;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.organicsystemsllc.travelingsalesman.databinding.ActivityMainBinding;
import com.organicsystemsllc.travelingsalesman.ui.maps.MapNode;
import com.organicsystemsllc.travelingsalesman.ui.maps.MapsViewModel;
import com.organicsystemsllc.travelingsalesman.ui.user.UserData;
import com.organicsystemsllc.travelingsalesman.ui.user.UserViewModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private UserViewModel mUserViewModel;
    public static final String TAG = "TRAVELING_SALESMAN";
    protected static final String CHANNEL_ID = "TSP_NOTIFICATIONS";
    private final ActivityResultLauncher<Intent> mSignInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );
    private FusedLocationProviderClient mFusedLocationClient;
    private ActivityResultLauncher<String[]> mLocationPermissionRequest;
    private PendingIntent mPendingIntent;
    private MapsViewModel mMapsViewModel;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityResultLauncher<String[]> mNotificationPermissionRequest;
    private FirebaseFunctions mFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set the layout
        ActivityMainBinding mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        setSupportActionBar(mBinding.appBarNavDrawer.toolbar);
        DrawerLayout mDrawer = mBinding.drawerLayout;
        NavigationView navigationView = mBinding.navView2;

        mUserViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        mMapsViewModel = new ViewModelProvider(this).get(MapsViewModel.class);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Location permission request
        mLocationPermissionRequest = registerForActivityResult(new ActivityResultContracts
                .RequestMultiplePermissions(), result -> {

            Boolean fineGranted = result
                    .getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION,
                            false);
            Boolean coarseGranted = result
                    .getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            false);
            Boolean backgroundGranted = result
                    .getOrDefault(Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                            false);

            if (Boolean.TRUE.equals(fineGranted) || Boolean.TRUE.equals(coarseGranted)) {
                startLocationUpdates();
            }
        });

        mNotificationPermissionRequest = registerForActivityResult(new ActivityResultContracts
                .RequestMultiplePermissions(), result -> {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                Boolean notificationGranted = result
                        .getOrDefault(Manifest.permission.POST_NOTIFICATIONS,
                                false);
            }

        });

        Button signIn = navigationView.getHeaderView(0).findViewById(R.id.btn_signIn);
        signIn.setOnClickListener(v -> signIn());

        // Initialize Firebase Auth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(firebaseAuth -> {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if(currentUser != null) {
                mUserViewModel.getUser().setValue(currentUser);
                Uri uri = currentUser.getPhotoUrl();
                mUserViewModel.getPhotoUri().setValue(currentUser.getPhotoUrl());
                getUserData(currentUser);
                signIn.setVisibility(View.INVISIBLE);
                ImageView image = navigationView.getHeaderView(0).findViewById(R.id.imageViewNav);
                TextView username = navigationView.getHeaderView(0).findViewById(R.id.username);
                if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                    username.setText(currentUser.getDisplayName());
                } else {
                    username.setText(currentUser.getEmail());
                }
                username.setVisibility(View.VISIBLE);

                if (uri != null) {
                    Picasso.get()
                            .load(uri)
                            .into(image);
                }

            } else {
                ImageView image = navigationView.getHeaderView(0).findViewById(R.id.imageViewNav);
                image.setImageResource(R.mipmap.ic_launcher_round);
                signIn.setVisibility(View.VISIBLE);
                TextView username = navigationView.getHeaderView(0).findViewById(R.id.username);
                username.setVisibility(View.INVISIBLE);
                mUserViewModel.getUser().setValue(null);
                mUserViewModel.getPhotoUri().setValue(null);
            }

        });

        mUserViewModel.getPhotoUri().observe(this, uri -> {

        });

        mUserViewModel.getTrackToggle().observe(this, track -> {
            if (track != null && track) {
                startLocationUpdates();
            } else {
                stopLocationUpdates();
            }
        });

        //Setup Android navigation
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_maps, R.id.navigation_routes,
                R.id.navigation_requests, R.id.navigation_user, R.id.navigation_request_detail)
                .setOpenableLayout(mDrawer)
                .build();
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_nav_drawer);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, navController);

            mBinding.appBarNavDrawer.fabCheckout.setOnClickListener(v ->
                    navController.navigate(R.id.navigation_checkout)
            );
        }

        // Define a variable to hold the Places API key.
        String apiKey = BuildConfig.TSP_API_KEY;

        // Log an error if apiKey is not set.
        if (TextUtils.isEmpty(apiKey)) {
            Log.e("Places test", "No api key");
            finish();
            return;
        }

        // Initialize the SDK
        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(getApplicationContext(), apiKey);
        }

        // Create a new PlacesClient instance
        PlacesClient placesClient = Places.createClient(this);

        createNotificationChannel();
        checkNotificationPermission();

        mFunctions = FirebaseFunctions.getInstance();

    }

    private void getUserData(FirebaseUser currentUser) {
        DocumentReference driverRef = FirebaseFirestore.getInstance("travelingsalesman").collection("drivers").document(currentUser.getUid());

        driverRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot document, @Nullable FirebaseFirestoreException error) {

                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                String source = document != null && document.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if (document != null && document.exists()) {
                    Boolean online = (Boolean) document.get("isOnline");
                    Boolean tracking = (Boolean) document.get("trackLocation");
//                            Boolean active = (Boolean) document.get("isActive");
//                            Timestamp updated = data.getUpdated();

                    mUserViewModel.getOnlineToggle().setValue(online);
                    mUserViewModel.getTrackToggle().setValue(tracking);

                    Double latitude = (Double) document.get("latitude");
                    Double longitude = (Double) document.get("longitude");

                    if (latitude != null && longitude != null) {
                        LatLng latLng = new LatLng(latitude, longitude);
                        mUserViewModel.getLatLng().setValue(latLng);
                    }

                    Log.d(TAG, source + " data: " + document.getData());
                } else {
                    Log.d(TAG, source + " data: null");
                    mUserViewModel.getOnlineToggle().setValue(false);
                }



            }
        });

        DocumentReference userRef = FirebaseFirestore.getInstance("travelingsalesman").collection("users").document(currentUser.getUid());

        userRef.addSnapshotListener((snapshot, e) -> {
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
                    if (updated != null) {
                        String dateString = DateFormat.getDateTimeInstance().format(updated.toDate());
                        mUserViewModel.getText().setValue(dateString);
                    }

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


        CollectionReference routeRef = FirebaseFirestore.getInstance("travelingsalesman").collection("users").document(currentUser.getUid())
                .collection("routes");

        routeRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (value == null) {
                    return;
                }

                for (QueryDocumentSnapshot doc : value) {
                    if (doc.get("name") != null) {


                    }
                }

                for (DocumentChange dc : value.getDocumentChanges()) {
                    switch (dc.getType()) {
                        case ADDED:
                            Log.d(TAG, "New route: " + dc.getDocument().getData());
//                            Route route = dc.getDocument().toObject(Route.class);
//                            mMapsViewModel.getRoute().setValue(route);
                            break;
                        case MODIFIED:
                            Log.d(TAG, "Modified route: " + dc.getDocument().getData());
                            break;
                        case REMOVED:
                            Log.d(TAG, "Removed route: " + dc.getDocument().getData());
                            break;
                    }
                }

            }

        });




    }

    private void startLocationUpdates() {
        if (mFusedLocationClient == null) {
            return;
        }

        int finePermission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        int coursePermission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        int version = Build.VERSION.SDK_INT;
        int versionBG = Build.VERSION_CODES.Q;
        Log.i(TAG, version + ", " + versionBG);
        int backgroundPermission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        int fineGranted = ActivityCompat
                .checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int courseGranted = ActivityCompat
                .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
//        if (backgroundPermission != PackageManager.PERMISSION_GRANTED) {
        if (fineGranted != PackageManager.PERMISSION_GRANTED &&
                courseGranted != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, Integer.toString(finePermission));
            Log.i(TAG, Integer.toString(coursePermission));
            Log.i(TAG, Integer.toString(backgroundPermission));

            if (mLocationPermissionRequest != null) {
                mLocationPermissionRequest.launch(new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                });
            }
            return;
        } else {
            Log.i(TAG, "Has background location permission");
        }

        LocationRequest locationRequest = new LocationRequest.Builder(10000)
                .setDurationMillis(Long.MAX_VALUE)
                .setMinUpdateDistanceMeters(1)
                .build();

//        Intent intent = new Intent(this, LocationReceiver.class);
//        intent.putExtra("com.organicsystemsllc.travelingsalesman.DATA", currentUser.getUid());
//        intent.setAction("com.organicsystemsllc.travelingsalesman.ACTION_UPDATE_DATA");

//        mPendingIntent = PendingIntent
//                .getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE);

//        mFusedLocationClient.requestLocationUpdates(locationRequest, mPendingIntent);

        mFusedLocationClient.requestLocationUpdates(locationRequest, this, Looper.getMainLooper());

    }

    private void stopLocationUpdates() {
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(this);
        }
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
            notificationSettings();

            return true;
        } else if (id == R.id.action_sign_out) {
            signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_content_nav_drawer);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                    || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }

    private void checkNotificationPermission() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            int notificationGranted = ActivityCompat
                    .checkSelfPermission(this,
                            Manifest.permission.POST_NOTIFICATIONS);

            if (notificationGranted != PackageManager.PERMISSION_GRANTED) {
                mNotificationPermissionRequest.launch(new String[] {
                        Manifest.permission.POST_NOTIFICATIONS
                });

            }
        }

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        CharSequence name = getString(R.string.channel_name);
        String description = getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system. You can't change the importance
        // or other notification behaviors after this.
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

    }

    private void notificationSettings() {
        Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        intent.putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL_ID);
        startActivity(intent);


    }

    private Task<String> createAccount(String text) {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("text", text);
        data.put("push", true);

        return mFunctions
                .getHttpsCallable("createConnectedAccount")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        return (String) task.getResult().getData();
                    }
                });
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            return;
        }

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("latitude", location.getLatitude());
        userMap.put("longitude", location.getLongitude());
        userMap.put("altitude", location.getAltitude());
        userMap.put("bearing", location.getBearing());
        userMap.put("updated", FieldValue.serverTimestamp());
        FirebaseFirestore.getInstance("travelingsalesman").collection("drivers").document(currentUser.getUid())
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