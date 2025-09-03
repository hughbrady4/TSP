package com.organicsystemsllc.travelingsalesman.ui.route;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.ArrayList;

public class RouteFragment extends Fragment implements RouteAdapter.OnItemClickListener {


    public RouteFragment() {
    }

    public static RouteFragment newInstance() {
        RouteFragment fragment = new RouteFragment();
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
        View view = inflater.inflate(R.layout.fragment_route_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
//          recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setLayoutManager(new GridLayoutManager(context, 1));
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final RecyclerView recyclerView = (RecyclerView) view;
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        FirebaseFirestore mFireStore = FirebaseFirestore.getInstance();

        if (user != null) {
            DocumentReference docRef = mFireStore.collection("users").document(user.getUid());
            docRef.collection("routes")
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                ArrayList<Route> routes = new ArrayList<Route>();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(MainActivity.TAG, document.getId() + " => " + document.getData());

                                    String routeId = document.getId();
                                    Route route = new Route();
                                    route.setLabel(routeId);
                                    routes.add(route);


                                }
                                RecyclerView listView = (RecyclerView) getView();
                                recyclerView.setAdapter(new RouteAdapter(routes, RouteFragment.this));
                            } else {
                                Log.d(MainActivity.TAG, "Error getting documents: ", task.getException());
                            }


                        }
                    });

        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onItemClick(int position) {
        Toast.makeText(getContext(), Integer.toString(position), Toast.LENGTH_SHORT).show();

    }


}