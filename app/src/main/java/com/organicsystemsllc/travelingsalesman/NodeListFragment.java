package com.organicsystemsllc.travelingsalesman;

import static com.organicsystemsllc.travelingsalesman.MainActivity.TAG;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.organicsystemsllc.travelingsalesman.databinding.FragmentNodeListBinding;
import com.organicsystemsllc.travelingsalesman.databinding.FragmentNodeListItemBinding;
import com.organicsystemsllc.travelingsalesman.ui.maps.MapNode;
import com.organicsystemsllc.travelingsalesman.ui.maps.MapsViewModel;
import com.organicsystemsllc.travelingsalesman.ui.route.Route;
import com.organicsystemsllc.travelingsalesman.ui.route.RouteRequest;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class NodeListFragment extends BottomSheetDialogFragment {

    private static final String ARG_NODES = "nodes";
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


        Button route = mBinding.btnRoute;
        route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nodes != null) {
                    HashMap<String, MapNode> nodeList = nodes.getValue();
                    callRouteApi(nodeList);
                }
                dismiss();
            }
        });

        Button clear = mBinding.btnClear;
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                map.clear();
                mMapsViewModel.getNodes().setValue(null);
                mMapsViewModel.getRoute().setValue(null);
                dismiss();
            }
        });
    }

    public void callRouteApi(HashMap<String, MapNode> nodes) {

        if (nodes == null || nodes.size() < 2) return;


        String url = "https://routes.googleapis.com/directions/v2:computeRoutes";
        RouteRequest routeRequest = new
                RouteRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Route route = new Route(response);
                mMapsViewModel.getRoute().setValue(route);
                //addRouteToFirestore(route);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e(TAG, "Failed! " + error.getLocalizedMessage());
            }
        });

        routeRequest.setNodes(new ArrayList<>(nodes.values()));
        Log.i(TAG, String.valueOf(nodes));

        // Add the request to the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(requireActivity());
        queue.add(routeRequest);

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
        private Route mRoute;

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