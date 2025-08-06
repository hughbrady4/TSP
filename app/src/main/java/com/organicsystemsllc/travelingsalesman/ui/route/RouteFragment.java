package com.organicsystemsllc.travelingsalesman.ui.route;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

/**
 * A fragment representing a list of Items.
 */
public class RouteFragment extends Fragment {



    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
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
                                    Route route = document.toObject(Route.class);
                                    route.setLabel(routeId);
                                    routes.add(route);


                                }
                                RecyclerView listView = (RecyclerView) getView();
                                recyclerView.setAdapter(new RouteAdapter(routes));
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

//    @Override
//    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//        String routeId = snapshot.getKey();
//        Route route = snapshot.getValue(Route.class);
//        mRoutes.add(route);
//        RecyclerView listView = (RecyclerView) getView();
//        if (listView != null && listView.getAdapter() != null)
//            listView.getAdapter().notifyItemChanged(mRoutes.size()-1);
//
//    }

}