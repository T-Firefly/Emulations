package com.firefly.emulationstation.commom.fragment;

import android.app.DialogFragment;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firefly.emulationstation.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rany on 18-5-8.
 */

public final class MenuDialog extends DialogFragment {
    private RecyclerView mListView;
    private MenuAdapter mMenuAdapter;
    private List<MenuItem> mData;
    private OnMenuItemClickListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, R.style.MenuDialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        Resources res = inflater.getContext().getResources();
        RecyclerView recyclerView = new RecyclerView(inflater.getContext());
        recyclerView.setPadding(
                0, (int) res.getDimension(R.dimen.menu_dialog_padding_top),
                0, (int) res.getDimension(R.dimen.menu_dialog_padding_bottom));

        RecyclerView.LayoutParams layoutParams =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        recyclerView.setLayoutParams(layoutParams);

        return recyclerView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mListView = (RecyclerView) view;
        mMenuAdapter = new MenuAdapter();
        mListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mListView.setAdapter(mMenuAdapter);
    }

    public void setMenuItem(List<MenuItem> menuItems) {
        mData = menuItems;
    }

    public void setMenuItem(Resources res, int resId) {
        List<MenuDialog.MenuItem> menuItems = new ArrayList<>();
        TypedArray menus = res.obtainTypedArray(resId);;

        for (int i = 0; i < menus.length(); ++i) {
            int menuId = menus.getResourceId(i, R.string.default_menu_item);
            menuItems.add(new MenuDialog.MenuItem(menuId));
        }
        menus.recycle();
        setMenuItem(menuItems);
    }

    public void setListener(OnMenuItemClickListener listener) {
        mListener = listener;
    }

    private final class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.menu_item, parent, false);
            view.setBackgroundResource(R.drawable.header_text_background_selector);
            view.setFocusable(true);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final MenuItem item = mData.get(position);

            if (item.getName() != null) {
                holder.name.setText(item.getName());
            } else {
                holder.name.setText(item.getNameTextId());
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onClick(item);
                    }
                    MenuDialog.this.dismiss();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView name;

            public ViewHolder(View itemView) {
                super(itemView);

                name = itemView.findViewById(R.id.name);
            }
        }
    }

    public final static class MenuItem {
        int id;
        String name = null;
        int textId;

        public MenuItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public MenuItem(int textId) {
            this.id = textId;
            this.textId = textId;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getNameTextId() {
            return textId;
        }
    }

    public interface OnMenuItemClickListener {
        void onClick(MenuItem menuItem);
    }
}
