package com.organicsystemsllc.travelingsalesman.ui.requests;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.organicsystemsllc.travelingsalesman.MainActivity;
import com.organicsystemsllc.travelingsalesman.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class RequestListFragment extends Fragment implements RequestAdapter.RequestListSelectionListener {

    public RequestListFragment() {
    }

    @SuppressWarnings("unused")
    public static RequestListFragment newInstance(int columnCount) {
        RequestListFragment fragment = new RequestListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.request_item_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        FirebaseFirestore mFireStore = FirebaseFirestore.getInstance();

        if (user != null) {
            mFireStore.collection("requests")
//                    .whereEqualTo("state", "CA")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null || snapshots == null) {
                            Log.w(MainActivity.TAG, "listen:error", e);
                            return;
                        }

                        List<Request> requests = new ArrayList<>();
                        for (QueryDocumentSnapshot request : snapshots) {
                            Request request1 = request.toObject(Request.class);
                            request1.setId(request.getId());
                            requests.add(request1);
                        }
                        RecyclerView recyclerView = (RecyclerView) view;

                        recyclerView.setAdapter(new RequestAdapter(requests, RequestListFragment.this));

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    Log.d(MainActivity.TAG, "New request: " + dc.getDocument().getData());
                                    break;
                                case MODIFIED:
                                    Log.d(MainActivity.TAG, "Modified request: " + dc.getDocument().getData());
                                    break;
                                case REMOVED:
                                    Log.d(MainActivity.TAG, "Removed request: " + dc.getDocument().getData());
                                    break;
                            }
                        }

                    }
                });

        }

        RequestTouchCallback callback = new RequestTouchCallback();
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        RecyclerView recyclerView = (RecyclerView) view;
        helper.attachToRecyclerView(recyclerView);


    }

    @Override
    public void onRequestSelected(String requestId) {
        RequestViewModel viewModel = new ViewModelProvider(requireActivity()).get(RequestViewModel.class);
        viewModel.setRequestId(requestId);
        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_nav_drawer) .navigate(R.id.action_navigation_requests_to_navigation_request_detail);

    }

    private static class RequestTouchCallback extends ItemTouchHelper.Callback {

        @Override
        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            Log.i(MainActivity.TAG, "Action state: " + Integer.toString(actionState));
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            return 0;
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }

    }


}