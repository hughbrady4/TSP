package com.organicsystemsllc.travelingsalesman.ui.nodes;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.organicsystemsllc.travelingsalesman.databinding.FragmentNodeListItemBinding;
import com.organicsystemsllc.travelingsalesman.ui.maps.MapNode;
import com.organicsystemsllc.travelingsalesman.ui.placeholder.PlaceholderContent.PlaceholderItem;
import com.organicsystemsllc.travelingsalesman.databinding.FragmentNodeBinding;

import java.util.List;

public class NodeRecyclerViewAdapter extends RecyclerView.Adapter<NodeRecyclerViewAdapter.ViewHolder> {


    private final OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    private final List<MapNode> mValues;

    public NodeRecyclerViewAdapter(List<MapNode> items, OnItemClickListener listener) {
        mListener = listener;
        mValues = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentNodeListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), mListener);

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).getLabel());
        holder.mContentView.setText(mValues.get(position).getPosition().toString());
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mIdView;
        public final TextView mContentView;
        public MapNode mItem;

        public ViewHolder(FragmentNodeListItemBinding binding, OnItemClickListener listener) {
            super(binding.getRoot());
            mIdView = binding.tvNodeLabel;
            mContentView = binding.tvNodeAddress;


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

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}