package com.firefly.emulationstation.gamerepo.repos;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.gamerepo.data.bean.Repo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rany on 18-4-24.
 */

public final class RepoAdapter extends RecyclerView.Adapter<RepoAdapter.ViewHolder> {
    private List<Repo> mData = new ArrayList<>();
    private OnItemClickListener mListener;

    public RepoAdapter(OnItemClickListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_repos_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Repo repo = mData.get(position);

        if (position % 2 == 0) {
            holder.itemView.setBackgroundResource(R.drawable.repo_item_even_selector);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.repo_item_obb_selector);
        }

        holder.title.setText(repo.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClick(repo);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(List<Repo> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public void addData(int position, Repo repo) {
        mData.add(position, repo);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;

        public ViewHolder(View itemView) {
            super(itemView);

            icon = itemView.findViewById(R.id.icon);
            title = itemView.findViewById(R.id.title);
        }
    }

    public interface OnItemClickListener {
        void onClick(Repo repo);
    }
}
