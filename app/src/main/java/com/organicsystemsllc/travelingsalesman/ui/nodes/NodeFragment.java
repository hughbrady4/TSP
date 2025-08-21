package com.organicsystemsllc.travelingsalesman.ui.nodes;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.organicsystemsllc.travelingsalesman.MainActivity;
import com.organicsystemsllc.travelingsalesman.R;
import com.organicsystemsllc.travelingsalesman.ui.maps.MapNode;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A fragment representing a list of Items.
 */
public class NodeFragment extends Fragment implements NodeRecyclerViewAdapter.OnItemClickListener{

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NodeFragment() {
    }

    @SuppressWarnings("unused")
    public static NodeFragment newInstance() {
        NodeFragment fragment = new NodeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_node_list, container, false);
        RecyclerView recyclerView = (RecyclerView) view;

        // Set the adapter
        if (recyclerView != null) {
            Context context = view.getContext();
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        FirebaseFirestore mFireStore = FirebaseFirestore.getInstance();

        if (user != null) {
            DocumentReference docRef = mFireStore.collection("users").document(user.getUid());
            docRef.collection("nodes")
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                ArrayList<MapNode> nodes = new ArrayList<MapNode>();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(MainActivity.TAG, document.getId() + " => " + document.getData());

                                    String nodeId = document.getId();
                                    MapNode node = new MapNode();
                                    String label = document.getString("label");
                                    HashMap<String, Double> pos = (HashMap<String, Double>) document.get("position");
                                    Log.d(MainActivity.TAG, pos.toString());
                                    node.setLabel(label);
                                    node.setPosition(new LatLng(pos.get("latitude"), pos.get("longitude")));
                                    nodes.add(node);


                                }
                                recyclerView.setAdapter(new NodeRecyclerViewAdapter(nodes, NodeFragment.this));
                            } else {
                                Log.d(MainActivity.TAG, "Error getting documents: ", task.getException());
                            }


                        }
                    });

        }
    }

    @Override
    public void onItemClick(int position) {
        Toast.makeText(getContext(), Integer.toString(position), Toast.LENGTH_SHORT).show();

    }
}