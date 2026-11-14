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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.organicsystemsllc.travelingsalesman.databinding.DialogRequestDetailBinding;
import com.organicsystemsllc.travelingsalesman.ui.maps.MapNode;

import java.util.HashMap;
import java.util.Map;

public class NodeDetailFragment extends BottomSheetDialogFragment {

    private static final String ARG_LABEL = "node_label";
    private DialogRequestDetailBinding mBinding;
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

        mBinding = DialogRequestDetailBinding.inflate(inflater, container, false);
        return mBinding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        Bundle args = getArguments();


        Button save = mBinding.buttonAccept;
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogConfirmRequestUpdate dialog = new DialogConfirmRequestUpdate();
                dialog.show(requireActivity().getSupportFragmentManager(), "ACCEPT_RIDE");
                dismiss();
            }
        });

//            Button clear = mBinding.btnClear;
//            clear.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    mNode.getMarker().remove();
//                    dismiss();
//                }
//            });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    public static class DialogConfirmRequestUpdate extends DialogFragment {
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
                            Toast.makeText(getContext(),"Please login to update request.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Map<String, Object> docData = new HashMap<>();
                        docData.put("status", "Accepted");
                        docData.put("updated", FieldValue.serverTimestamp());



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