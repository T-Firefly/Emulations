package com.firefly.emulationstation.gamerepo.category;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.gamerepo.data.bean.Category;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a
 * {@link Category} and makes a call to the
 * specified {@link CategoryContract.Presenter}.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final List<Category> mValues;
    private final CategoryContract.Presenter mPresenter;
    private int mSelected = 0;
    private View mSelectedView = null;

    public CategoryAdapter(List<Category> items, CategoryContract.Presenter presenter) {
        mValues = items;
        mPresenter = presenter;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mTitleView.setText(mValues.get(position).name);

        if (mSelected == position) {
            holder.mView.requestFocus();
            mPresenter.onCategorySelected(holder.mItem);

            // clear preselected view
            // needed when back from add repository
            if (mSelectedView != null) {
                mSelectedView.setSelected(false);
            }

            mSelectedView = holder.mView;
            mSelectedView.setSelected(true);
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mPresenter) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mSelected = holder.getAdapterPosition();
                    mPresenter.onCategorySelected(holder.mItem);
                }
                mSelectedView.setSelected(false);
                holder.mView.setSelected(true);
                mSelectedView = holder.mView;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void setSelected(int selected) {
        mSelected = selected;
        notifyDataSetChanged();
    }

    public void setData(List<Category> data) {
        mValues.clear();
        mValues.addAll(data);
        mPresenter.onCategorySelected(mValues.get(mSelected));
        notifyDataSetChanged();
    }

    public List<Category> getCategories() {
        return mValues;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mTitleView;
        Category mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.title);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitleView.getText() + "'";
        }
    }
}
