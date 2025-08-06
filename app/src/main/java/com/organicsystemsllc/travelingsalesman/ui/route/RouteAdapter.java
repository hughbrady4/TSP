package com.organicsystemsllc.travelingsalesman.ui.route;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.organicsystemsllc.travelingsalesman.databinding.FragmentRouteBinding;

import java.util.ArrayList;
import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> {


    private final ArrayList<Route> mRoutes;

    public RouteAdapter(ArrayList<Route> routes) {
        mRoutes = routes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentRouteBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mLabel.setText(mRoutes.get(position).getLabel());


    }

    @Override
    public int getItemCount() {
        return mRoutes.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mContentView;
        public final TextView mLabel;


        public ViewHolder(FragmentRouteBinding binding) {
            super(binding.getRoot());
            mContentView = binding.content;
            mLabel = binding.label;
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}