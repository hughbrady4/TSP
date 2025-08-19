package com.organicsystemsllc.travelingsalesman;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.organicsystemsllc.travelingsalesman.databinding.FragmentNodeDetailDialogBinding;
import com.organicsystemsllc.travelingsalesman.ui.maps.MapNode;
import com.organicsystemsllc.travelingsalesman.ui.maps.MapsViewModel;

import java.util.HashMap;

public class NodeDetailFragment extends BottomSheetDialogFragment {

    private static final String ARG_LABEL = "node_label";
    private FragmentNodeDetailDialogBinding mBinding;

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
                        MapNode node = nodes.get(label);
                        if (node != null && node.getFormattedAddress() != null) {
                            Log.d(MainActivity.TAG, node.getFormattedAddress());
                            TextView tvAddress = view.findViewById(R.id.tv_node_address);
                            tvAddress.setText(node.getFormattedAddress());
                        }

                }
            });

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }




}