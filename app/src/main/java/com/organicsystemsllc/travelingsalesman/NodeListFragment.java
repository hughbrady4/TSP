package com.organicsystemsllc.travelingsalesman;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.organicsystemsllc.travelingsalesman.databinding.FragmentNodeListBinding;
import com.organicsystemsllc.travelingsalesman.databinding.FragmentNodeListItemBinding;
import com.organicsystemsllc.travelingsalesman.ui.maps.MapNode;
import com.organicsystemsllc.travelingsalesman.ui.maps.MapsViewModel;

import java.util.ArrayList;
import java.util.HashMap;

public class NodeListFragment extends BottomSheetDialogFragment {

    private static final String ARG_NODES = "nodes";
    private FragmentNodeListBinding binding;


    public static NodeListFragment newInstance() {
        final NodeListFragment fragment = new NodeListFragment();
        final Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentNodeListBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        MapsViewModel mMapsViewModel = new ViewModelProvider(requireActivity()).get(MapsViewModel.class);
        final MutableLiveData<HashMap<String, MapNode>> nodes = mMapsViewModel.getNodes();
        if (nodes != null && nodes.getValue() != null) {
            recyclerView.setAdapter(new ItemAdapter(new ArrayList<>(nodes.getValue().values())));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        final TextView text;

        ViewHolder(FragmentNodeListItemBinding binding) {
            super(binding.getRoot());
            text = binding.text;
        }

    }

    private class ItemAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final ArrayList<MapNode> mNodes;

        ItemAdapter(ArrayList<MapNode> nodes) {
            mNodes = nodes;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            return new ViewHolder(FragmentNodeListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String address = mNodes.get(position).getFormattedAddress();
            if (address != null && !address.isEmpty()) {
                holder.text.setText(address);
            } else {
                holder.text.setText(mNodes.get(position).getPosition().toString());
            }
        }

        @Override
        public int getItemCount() {
            return mNodes.size();
        }

    }
}