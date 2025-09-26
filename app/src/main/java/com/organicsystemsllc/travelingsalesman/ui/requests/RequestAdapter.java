package com.organicsystemsllc.travelingsalesman.ui.requests;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.organicsystemsllc.travelingsalesman.databinding.RequestItemBinding;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    private final List<Request> mValues;
    private RequestListSelectionListener mListener;
    public RequestAdapter(List<Request> items, RequestListSelectionListener listener) {
        mListener = listener;
        mValues = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(RequestItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Request request = mValues.get(position);

        holder.mRequest = mValues.get(position);
//        holder.mIdView.setText(mValues.get(position).id);
        holder.mTS.setText(request.getId());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onRequestSelected(request.getId());

            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mLabel;
        public final TextView mTS;
        public Request mRequest;

        public ViewHolder(RequestItemBinding binding) {
            super(binding.getRoot());
            mLabel = binding.tvLabel;
            mTS = binding.tvTimestamp;
        }

    }

    public interface RequestListSelectionListener {
        void onRequestSelected(String requestId);
    }
}