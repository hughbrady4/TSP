package com.organicsystemsllc.travelingsalesman.ui.route;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.organicsystemsllc.travelingsalesman.databinding.FragmentRouteBinding;

import java.util.ArrayList;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> {


    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private final ArrayList<Route> mRoutes;

    private final OnItemClickListener mListener;

    public RouteAdapter(ArrayList<Route> routes, OnItemClickListener listener) {
        mRoutes = routes;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentRouteBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), mListener);

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


        public ViewHolder(FragmentRouteBinding binding, OnItemClickListener listener) {
            super(binding.getRoot());
            mContentView = binding.content;
            mLabel = binding.label;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });

        }

        @NonNull
        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}