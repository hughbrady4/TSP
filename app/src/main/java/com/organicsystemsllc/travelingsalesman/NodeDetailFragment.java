package com.organicsystemsllc.travelingsalesman;

import static com.organicsystemsllc.travelingsalesman.MainActivity.TAG;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.type.Date;
import com.organicsystemsllc.travelingsalesman.databinding.FragmentNodeDetailDialogBinding;
import com.organicsystemsllc.travelingsalesman.ui.maps.MapNode;
import com.organicsystemsllc.travelingsalesman.ui.maps.MapsFragment;
import com.organicsystemsllc.travelingsalesman.ui.maps.MapsViewModel;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NodeDetailFragment extends BottomSheetDialogFragment {

    private static final String ARG_LABEL = "node_label";
    private FragmentNodeDetailDialogBinding mBinding;
    private static MapNode mNode;


    public static NodeDetailFragment newInstance(String label) {
        final NodeDetailFragment fragment = new NodeDetailFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_LABEL, label);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mBinding = FragmentNodeDetailDialogBinding.inflate(inflater, container, false);

        return mBinding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        Bundle args = getArguments();

        if (args != null) {
            String label = args.getString(ARG_LABEL);
//            TextView tvLabel = view.findViewById(R.id.tv_node_label);
//            tvLabel.setText(label);
            MapsViewModel mMapsViewModel = new ViewModelProvider(requireActivity()).get(MapsViewModel.class);
            mMapsViewModel.getNodes().observe(getViewLifecycleOwner(), new Observer<HashMap<String, MapNode>>() {
                @Override
                public void onChanged(HashMap<String, MapNode> nodes) {
                    mNode = nodes.get(label);
                    TextView tvAddress = view.findViewById(R.id.tv_node_address);

                        if (mNode != null) {
                            if (mNode.getFormattedAddress() != null &&
                                    !mNode.getFormattedAddress().isEmpty()) {
                                Log.d(MainActivity.TAG, mNode.getFormattedAddress());
                                tvAddress.setText(mNode.getFormattedAddress());
                            } else {
                                tvAddress.setText(mNode.getPosition().toString());
                            }

                        }

                }
            });
            Button save = mBinding.btnSave;
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SaveNodeDialogFragment dialog = new SaveNodeDialogFragment();
                    dialog.show(requireActivity().getSupportFragmentManager(), "SAVE_DIALOG");
                    dismiss();
                }
            });

            Button clear = mBinding.btnClear;

            clear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mNode.getMarker().remove();
                    dismiss();
                }
            });

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    public static class SaveNodeDialogFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction.
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setMessage(R.string.dialog_save_node)
                    .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (currentUser == null) {
                                Toast.makeText(getContext(),"Please login to save location.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Map<String, Object> docData = new HashMap<>();
                            docData.put("label", mNode.getLabel());
                            docData.put("lat", mNode.getPosition().latitude);
                            docData.put("lng", mNode.getPosition().longitude);
                            docData.put("formattedAddress", mNode.getFormattedAddress());

                            docData.put("ts", FieldValue.serverTimestamp());

                            FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid())
                                    .collection("nodes")
                                    .add(docData)
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




}