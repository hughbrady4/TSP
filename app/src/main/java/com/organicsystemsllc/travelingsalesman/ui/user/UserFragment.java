package com.organicsystemsllc.travelingsalesman.ui.user;

import static com.organicsystemsllc.travelingsalesman.MainActivity.TAG;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.organicsystemsllc.travelingsalesman.MainActivity;
import com.organicsystemsllc.travelingsalesman.databinding.FragmentUserBinding;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserFragment extends Fragment {

    private UserViewModel mUserViewModel;
    private FragmentUserBinding mUserBinding;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        mUserViewModel =
                new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
//        mAuth.addAuthStateListener(firebaseAuth ->
//                mUserViewModel.getUser().setValue(firebaseAuth.getCurrentUser()));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {



        mUserBinding = FragmentUserBinding.inflate(inflater, container, false);

        return mUserBinding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TextView textView = mUserBinding.textHome;
        mUserViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        final ImageView imageView = mUserBinding.imageView;
        mUserViewModel.getPhotoUri().observe(getViewLifecycleOwner(), uri -> {

            try {
                Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(uri.toString()).getContent());
                imageView.setImageBitmap(bitmap);

            } catch (Exception e) {
                imageView.setImageBitmap(null);
            }
        });

        final EditText usernameEditText = mUserBinding.username;
        final EditText passwordEditText = mUserBinding.password;
        final Button loginButton = mUserBinding.login;
        final Button logoutBtn = mUserBinding.logout;
        final Button registerBtn = mUserBinding.register;
        final Button linkBtn = mUserBinding.webLink;
        final ProgressBar loadingProgressBar = mUserBinding.loading;

        mUserViewModel.getUser().observe(getViewLifecycleOwner(), firebaseUser -> {
            if (firebaseUser != null) {
                usernameEditText.setText(firebaseUser.getEmail());
                usernameEditText.setEnabled(false);
                passwordEditText.setVisibility(View.GONE);
                loginButton.setVisibility(View.GONE);
                registerBtn.setVisibility(View.GONE);
                loadingProgressBar.setVisibility(View.GONE);
                logoutBtn.setVisibility(View.VISIBLE);
                logoutBtn.setEnabled(true);
            } else {
                usernameEditText.setVisibility(View.VISIBLE);
                usernameEditText.setEnabled(true);
                passwordEditText.setVisibility(View.VISIBLE);
                loginButton.setVisibility(View.VISIBLE);
                registerBtn.setVisibility(View.VISIBLE);
                logoutBtn.setVisibility(View.GONE);
                logoutBtn.setEnabled(false);

            }
        });

        mUserViewModel.getState().observe(getViewLifecycleOwner(), loginFormState -> {
            if (loginFormState == null) {
                return;
            }
            loginButton.setEnabled(loginFormState);
            registerBtn.setEnabled(loginFormState);
        });

        mUserViewModel.getValidUserName().observe(getViewLifecycleOwner(), usernameEditText::setError);
        mUserViewModel.getValidPW().observe(getViewLifecycleOwner(), passwordEditText::setError);


        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                mUserViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        loginButton.setOnClickListener(v -> {
            loadingProgressBar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(usernameEditText.getText().toString(),
                    passwordEditText.getText().toString()).addOnCompleteListener(task -> {
                        loadingProgressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            mUserViewModel.getUser().setValue(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(requireActivity(), Objects.requireNonNull(task.getException()).getLocalizedMessage(),
                                    Toast.LENGTH_SHORT).show();
                            mUserViewModel.getUser().setValue(null);

                        }


                    });



        });

        logoutBtn.setOnClickListener(v -> mAuth.signOut());

        registerBtn.setOnClickListener(v -> mAuth.createUserWithEmailAndPassword(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString())
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(requireActivity(), Objects.requireNonNull(task.getException()).getLocalizedMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }));

        linkBtn.setOnClickListener(v -> sendLocationLink());

        SwitchCompat trackToggle = mUserBinding.switchTrack;

        trackToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {


            mUserViewModel.getTrackToggle().setValue(isChecked);

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(getContext(),"Please login to enable switch.", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("trackLocation", isChecked);
            userMap.put("updated", FieldValue.serverTimestamp());
            FirebaseFirestore.getInstance().collection("users")
                    .document(currentUser.getUid())
                    .set(userMap, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Snackbar.make(view, "Tracking updated.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        Log.d(MainActivity.TAG, "DocumentSnapshot successfully written!");
                    })
                    .addOnFailureListener(e -> {
                        Snackbar.make(view, "Failed to update server.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        Log.w(MainActivity.TAG, "Error writing document", e);
                    });

        });

        mUserViewModel.getTrackToggle().observe(getViewLifecycleOwner(), checked -> {
            if (checked != null) {
                trackToggle.setChecked(checked);
            } else {
                trackToggle.setChecked(false);
            }
        });

        SwitchCompat onlineToggle = mUserBinding.switchOnline;

        onlineToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(getContext(),"Please login to enable switch.", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("isOnline", isChecked);
            userMap.put("updated", FieldValue.serverTimestamp());

            mUserViewModel.getOnlineToggle().setValue(isChecked);

            FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid())
                    .set(userMap, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Snackbar.make(view, "Online visibility updated.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        Log.d(MainActivity.TAG, "DocumentSnapshot successfully written!");
                    })
                    .addOnFailureListener(e -> {
                        Snackbar.make(view, "Failed to update server.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        Log.w(MainActivity.TAG, "Error writing document", e);
                    });

        });

        mUserViewModel.getOnlineToggle().observe(getViewLifecycleOwner(), checked -> {
            if (checked != null) {
                onlineToggle.setChecked(checked);
            } else {
                onlineToggle.setChecked(false);
            }
        });

        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser == null) {
                trackToggle.setEnabled(false);
                trackToggle.setVisibility(View.GONE);
                onlineToggle.setEnabled(false);
                onlineToggle.setVisibility(View.GONE);
                mUserViewModel.getPhotoUri().setValue(null);
            } else {
                mUserViewModel.getPhotoUri().setValue(currentUser.getPhotoUrl());

                trackToggle.setEnabled(true);
                trackToggle.setVisibility(View.VISIBLE);
                onlineToggle.setEnabled(true);
                onlineToggle.setVisibility(View.VISIBLE);

            }

        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUserBinding = null;
    }

    private void sendLocationLink() {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("travelingsalesman.web.app");

        if (currentUser != null) {
            builder.appendQueryParameter("id", currentUser.getUid());
        }
        // Create the share intent
        Intent shareIntent = new Intent(Intent.ACTION_VIEW, builder.build());

        // Start the share activity
        startActivity(Intent.createChooser(shareIntent, "Open with"));
    }
}