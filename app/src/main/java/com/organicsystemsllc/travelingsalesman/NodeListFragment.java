package com.organicsystemsllc.travelingsalesman;

import static com.organicsystemsllc.travelingsalesman.MainActivity.TAG;

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
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.organicsystemsllc.travelingsalesman.databinding.FragmentNodeListBinding;
import com.organicsystemsllc.travelingsalesman.databinding.FragmentNodeListItemBinding;
import com.organicsystemsllc.travelingsalesman.ui.maps.MapNode;
import com.organicsystemsllc.travelingsalesman.ui.maps.MapsViewModel;
import com.organicsystemsllc.travelingsalesman.ui.route.Route;
import com.organicsystemsllc.travelingsalesman.ui.route.RouteRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NodeListFragment extends BottomSheetDialogFragment {

    private FragmentNodeListBinding mBinding;
    private MapsViewModel mMapsViewModel;


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

        mBinding = FragmentNodeListBinding.inflate(inflater, container, false);
        return mBinding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mMapsViewModel = new ViewModelProvider(requireActivity()).get(MapsViewModel.class);
        final MutableLiveData<HashMap<String, MapNode>> nodes = mMapsViewModel.getNodes();
        if (nodes != null && nodes.getValue() != null) {
            ItemAdapter adapter = new ItemAdapter(new ArrayList<>(nodes.getValue().values()));
            recyclerView.setAdapter(adapter);
        }

        Button btnDelete = mBinding.btnDelete;
        Button btnRoute = mBinding.btnRoute;


        mMapsViewModel.getRoute().observeForever(route -> {
            if (route != null && route.getId() != null && !route.getId().isEmpty()) {
                btnRoute.setVisibility(View.INVISIBLE);
                btnDelete.setVisibility(View.VISIBLE);

            } else {
                btnRoute.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.INVISIBLE);

            }
        });

        btnRoute.setOnClickListener(v -> {
            if (nodes != null) {
                HashMap<String, MapNode> nodeList = nodes.getValue();
                callRouteApi(nodeList);
            }
            dismiss();
        });

        Button clear = mBinding.btnClear;
        clear.setOnClickListener(v -> {
//                map.clear();
            mMapsViewModel.getNodes().setValue(null);
            mMapsViewModel.getRoute().setValue(null);
            dismiss();
        });
    }

    public void callRouteApi(HashMap<String, MapNode> nodes) {

        if (nodes == null || nodes.size() < 2) {
            Toast.makeText(getContext(), "Please add at least two points to build route.", 
                    Toast.LENGTH_SHORT).show();
            return;
        }


        final RouteRequest routeRequest = getRouteRequest(nodes);

        routeRequest.setNodes(new ArrayList<>(nodes.values()));
        Log.i(TAG, String.valueOf(nodes));

        // Add the request to the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(requireActivity());
        queue.add(routeRequest);

    }

    @NonNull
    private RouteRequest getRouteRequest(HashMap<String, MapNode> nodes) {
        String url = "https://routes.googleapis.com/directions/v2:computeRoutes";
        return new
                RouteRequest(url, response -> {
                    Route route = new Route(response);
                    route.setNodes(nodes);
                    mMapsViewModel.getRoute().setValue(route);
                    addRouteToFirestore(route);
                }, error -> Log.e(TAG, "Failed! " + error.getLocalizedMessage()));
    }

    public void addRouteToFirestore(Route route) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(),"Please login to add location.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> docData = new HashMap<>();
        docData.put("label", route.getLabel());
        docData.put("distanceMeters", route.getDistanceMeters());
        docData.put("duration", route.getDuration());
        docData.put("stops", route.getNodes().size());
        docData.put("ts", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance().collection("users")
                .document(currentUser.getUid())
                .collection("routes")
                .add(docData)
                .addOnSuccessListener(documentReference -> {
                    route.setId(documentReference.getId());
                    Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView tv_label;
        final TextView tv_address;


        ViewHolder(FragmentNodeListItemBinding binding) {
            super(binding.getRoot());
            tv_label = binding.tvNodeLabel;
            tv_address = binding.tvNodeAddress;

        }

    }

    private static class ItemAdapter extends RecyclerView.Adapter<ViewHolder> {

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
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String address = mNodes.get(position).getFormattedAddress();
            if (address != null && !address.isEmpty()) {
                holder.tv_address.setText(address);
            } else {
                holder.tv_address.setText(mNodes.get(position).getPosition().toString());
            }
            String label = mNodes.get(position).getLabel();
            holder.tv_label.setText(label);
        }

        @Override
        public int getItemCount() {
            return mNodes.size();
        }

    }
}