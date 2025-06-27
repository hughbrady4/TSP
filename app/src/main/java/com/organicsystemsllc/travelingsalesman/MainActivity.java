package com.organicsystemsllc.travelingsalesman;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.DateFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.organicsystemsllc.travelingsalesman.databinding.ActivityMainBinding;
import com.organicsystemsllc.travelingsalesman.ui.login.UserData;
import com.organicsystemsllc.travelingsalesman.ui.login.UserViewModel;
import com.organicsystemsllc.travelingsalesman.ui.maps.MapsViewModel;

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
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Initialize Firebase Auth
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(firebaseAuth -> {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if(currentUser != null){
                mUserViewModel.getUser().setValue(currentUser);
                mUserViewModel.getPhotoUri().setValue(currentUser.getPhotoUrl());
                getUserData(currentUser);

            } else {
                mUserViewModel.getUser().setValue(null);
                mUserViewModel.getPhotoUri().setValue(null);
            }

        });


        mUserViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        mMapsViewModel = new ViewModelProvider(this).get(MapsViewModel.class);
        mUserViewModel.getTrackToggle().observeForever(track -> {
            Log.i(TAG, "Track indicator updated: " + track.toString());
            if (Boolean.TRUE.equals(track)) {
                startLocationUpdates();
            } else {
                stopLocationUpdates();
            }
        });

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_maps, R.id.navigation_routes, R.id.navigation_notifications)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavigationUI.setupWithNavController(navView, navController);


        //Location permission request
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
            if (Boolean.FALSE.equals(backgroundGranted)) {
                return;
            }

            if (Boolean.TRUE.equals(fineGranted) || Boolean.TRUE.equals(coarseGranted)) {
                startLocationUpdates();
            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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



}