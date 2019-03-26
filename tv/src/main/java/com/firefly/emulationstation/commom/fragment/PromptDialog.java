package com.firefly.emulationstation.commom.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firefly.emulationstation.R;

/**
 * Created by rany on 18-5-5.
 */

public class PromptDialog extends DialogFragment {
    private final static String ARG_MSG = "msg";

    private int mPStringId = -1;
    private int mNStringId = -1;
    private int mTitleId = -1;
    private int mMessageId = -1;
    private String mTitle = null;
    private String mMessage = null;
    private CharSequence[] mItems = null;
    private boolean[] mCheckedItems = null;
    private boolean mIsMultiChoice = false;

    private View mContentView = null;

    private OnClickListener mPListener = null;
    private OnClickListener mNListener = null;
    private DialogInterface.OnDismissListener mDismissListener = null;
    private DialogInterface.OnCancelListener mCancelListener = null;
    private OnMultiChoiceClickListener mOnCheckboxClickListener = null;
    private OnClickListener mSigleChoiceItemListener = null;
    private int mSelectedItem = 0;

    public static PromptDialog newInstance(String msg) {

        Bundle args = new Bundle();
        args.putString(ARG_MSG, msg);

        PromptDialog fragment = new PromptDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mMessage = getArguments().getString(ARG_MSG);
        }

        setStyle(DialogFragment.STYLE_NORMAL, R.style.BaseDialogStyle);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_prompt, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FrameLayout mainContentView = view.findViewById(R.id.content);
        TextView titleView = view.findViewById(android.R.id.title);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.button_cancel:
                        if (mNListener != null) {
                            mNListener.onClick(PromptDialog.this, Dialog.BUTTON_NEGATIVE);
                        }
                        break;
                    case R.id.button_ok:
                        if (mPListener != null) {
                            mPListener.onClick(PromptDialog.this, Dialog.BUTTON_POSITIVE);
                        }
                        break;
                }

                PromptDialog.this.dismiss();
            }
        };

        if (mPStringId != -1) {
            Button okButton = view.findViewById(R.id.button_ok);
            okButton.setVisibility(View.VISIBLE);
            okButton.setText(mPStringId);
            okButton.setOnClickListener(listener);

            View parent = (View) okButton.getParent();
            parent.setVisibility(View.VISIBLE);
        }

        if (mNStringId != -1) {
            Button cancelButton = view.findViewById(R.id.button_cancel);
            cancelButton.setVisibility(View.VISIBLE);
            cancelButton.setText(mNStringId);
            cancelButton.setOnClickListener(listener);

            View parent = (View) cancelButton.getParent();
            parent.setVisibility(View.VISIBLE);
        }

        if (mTitle != null || mTitleId != -1) {
            titleView.setVisibility(View.VISIBLE);
            if (mTitleId != -1) {
                titleView.setText(mTitleId);
            } else {
                titleView.setText(mTitle);
            }
        }

        if (mContentView != null) {
            mainContentView.addView(mContentView);
        } else if (mItems != null) {
            createList(mainContentView);
        } else if (mMessage != null || mMessageId != -1) {
            TextView textView = new TextView(getActivity());
            titleView.setVisibility(View.VISIBLE);
            if (mMessage != null) {
                textView.setText(mMessage);
            } else {
                textView.setText(mMessageId);
            }

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int padding = getResources().getDimensionPixelSize(R.dimen.prompt_dialog_padding);
            textView.setPadding(padding, 0, padding, 0);
            mainContentView.addView(textView, layoutParams);
        }
    }

    private void createList(ViewGroup mainContentView) {
        Resources res = getResources();
        int height = res.getDimensionPixelSize(R.dimen.prompt_dialog_list_height);
        int itemHeight = res.getDimensionPixelSize(R.dimen.prompt_dialog_list_item_height);
        RecyclerView recyclerView = new RecyclerView(getActivity());

        if (height > itemHeight*mItems.length) {
            height = itemHeight*mItems.length;
        }

        RecyclerView.LayoutParams layoutParams =
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        height);
        recyclerView.setLayoutParams(layoutParams);
        recyclerView.setAdapter(new ListAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mainContentView.addView(recyclerView);
    }

    public PromptDialog setPositiveButton(int textId, OnClickListener listener) {
        mPStringId = textId;
        mPListener = listener;

        return this;
    }

    public PromptDialog setNegativeButton(int textId, OnClickListener listener) {
        mNStringId = textId;
        mNListener = listener;

        return this;
    }

    public PromptDialog setOnDismissListener(DialogInterface.OnDismissListener listener) {
        mDismissListener = listener;

        return this;
    }

    public PromptDialog setOnCancelListener(DialogInterface.OnCancelListener listener) {
        mCancelListener = listener;

        return this;
    }

    public PromptDialog setTitle(int titleResId) {
        mTitleId = titleResId;

        return this;
    }

    public PromptDialog setMessage(int msgResId) {
        mMessageId = msgResId;

        return this;
    }

    public PromptDialog setMessage(String msg) {
        mMessage = msg;

        return this;
    }

    public PromptDialog setContentView(View view) {
        mContentView = view;

        return this;
    }

    public PromptDialog setMultiChoiceItems(CharSequence[] items, boolean[] checkedItems,
                                       final OnMultiChoiceClickListener listener) {
        mItems = items;
        mOnCheckboxClickListener = listener;
        mCheckedItems = checkedItems;
        mIsMultiChoice = true;
        return this;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (mDismissListener != null)
            mDismissListener.onDismiss(dialog);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (mCancelListener != null) {
            mCancelListener.onCancel(dialog);
        }
    }

    public PromptDialog setSingleChoiceItems(CharSequence[] items,
                                             int checkedItem,
                                             OnClickListener onClickListener) {
        mItems = items;
        mSelectedItem = checkedItem;
        mSigleChoiceItemListener = onClickListener;
        return this;
    }

    private class ListAdapter extends RecyclerView.Adapter<ViewHolder> {

        ListAdapter() {
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.dialog_prompt_list_item, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            final int finalPosition = position;
            if (mIsMultiChoice) {
                holder.checkBox.setVisibility(View.VISIBLE);
                holder.checkBox.setOnCheckedChangeListener(null);

                if (mCheckedItems[position]) {
                    holder.checkBox.setChecked(true);
                } else {
                    holder.checkBox.setChecked(false);
                }

                holder.checkBox.setOnCheckedChangeListener(
                        new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mOnCheckboxClickListener.onClick(PromptDialog.this, finalPosition, isChecked);
                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.checkBox.performClick();
                    }
                });
            } else {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mSigleChoiceItemListener != null) {
                            mSigleChoiceItemListener.onClick(PromptDialog.this, finalPosition);
                        }
                    }
                });
            }

            holder.titleView.setText(mItems[position]);
        }

        @Override
        public int getItemCount() {
            return mItems.length;
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView titleView;

        public ViewHolder(View itemView) {
            super(itemView);

            checkBox = itemView.findViewById(R.id.checkBox);
            titleView = itemView.findViewById(R.id.title);
        }
    }

    public interface OnClickListener {
        void onClick(DialogFragment dialog, int which);
    }

    public interface OnMultiChoiceClickListener {
        void onClick(DialogFragment dialog, int which, boolean isChecked);
    }
}
