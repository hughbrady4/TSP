package com.organicsystemsllc.travelingsalesman.ui.account;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.organicsystemsllc.travelingsalesman.R;

import java.util.HashMap;
import java.util.Map;

public class AccountFragment extends Fragment {

    private static final String TAG = "AccountFragment";

    private AccountViewModel mViewModel;
    private FirebaseFunctions mFunctions;

    public static AccountFragment newInstance() {
        return new AccountFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AccountViewModel.class);
        mFunctions = FirebaseFunctions.getInstance();

        Button btnCreateAccount = view.findViewById(R.id.btn_create_account);
        if (btnCreateAccount != null) {
            btnCreateAccount.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Creating account...", Toast.LENGTH_SHORT).show();
                createAccount("create account")
                        .addOnSuccessListener(result -> {
                            Log.d(TAG, "Account created successfully: " + result);
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Account created: " + result, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error creating account", e);
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Failed to create account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            });
        }
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

}