package com.firefly.emulationstation.gamerepo.repos;


import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.commom.fragment.MenuDialog;
import com.firefly.emulationstation.commom.fragment.PromptDialog;
import com.firefly.emulationstation.commom.view.ProgressDialog;
import com.firefly.emulationstation.gamerepo.data.GameRepoRepository;
import com.firefly.emulationstation.gamerepo.data.bean.Repo;
import com.firefly.emulationstation.utils.Utils;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReposFragment extends Fragment implements ReposContract.View {
    private static final int REQUEST_CODE_OPEN_DOCUMENT = 1;
    @Inject
    GameRepoRepository mGameRepoRepository;
    @Inject
    ReposContract.Presenter mPresenter;

    private RecyclerView mListView;
    private RepoAdapter mAdapter;

    private ProgressDialog mProgressDialog = ProgressDialog.newInstance("Loading");

    public ReposFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidInjection.inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_repos, container, false);

        view.findViewById(R.id.add_repo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPresenter.onAddRepoButtonClick(view);
            }
        });

        mListView = view.findViewById(R.id.repo_list);
        mAdapter = new RepoAdapter((RepoAdapter.OnItemClickListener) mPresenter);

        mListView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mPresenter.subscribe(this);
    }

    @Override
    public void setPresenter(ReposContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showRepos(List<Repo> repos) {
        mAdapter.setData(repos);
    }

    @Override
    public void showMessage(String msg) {
        Utils.showToast(getActivity(), msg);
    }

    @Override
    public void updateRepo(int position, Repo repo) {
        mAdapter.addData(position, repo);
    }

    @Override
    public void showAddRepoTypeSelector() {
        MenuDialog dialog = new MenuDialog();
        dialog.setMenuItem(getResources(), R.array.repo_add_type);
        dialog.setListener(new MenuDialog.OnMenuItemClickListener() {
            @Override
            public void onClick(MenuDialog.MenuItem menuItem) {
                switch (menuItem.getId()) {
                    case R.string.add_from_url:
                        AddFromUrlDialog addFromUrlDialog = AddFromUrlDialog.newInstance(
                                new AddFromUrlDialog.OnButtonClickListener() {
                                    @Override
                                    public void onCancel() {}

                                    @Override
                                    public void onOk(String url) {
                                        mPresenter.addRepo(url);
                                    }
                                });
                        addFromUrlDialog.show(getFragmentManager(), "AddFromUrlDialog");
                        break;
                    case R.string.add_from_zip:

                        Intent intent = new Intent("com.firefly.FILE_PICKER");
                        final PackageManager mgr = getActivity().getPackageManager();
                        List<ResolveInfo> list =
                                mgr.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

                        if (list.isEmpty()) {
                            // TODO add DocumentsUI picker support
                            Utils.showToast(getActivity(), R.string.no_file_picker);
                        } else {
                            intent.putExtra("selectType", 1);
                            intent.putExtra("supportNet", false);
                            intent.putExtra("title", getString(R.string.select_file_title));
                            startActivityForResult(intent, REQUEST_CODE_OPEN_DOCUMENT);
                        }

                        break;
                }
            }
        });
        dialog.show(getFragmentManager(), "RepoTypeSelectorDialog");
    }

    @Override
    public void showRepoActions(final Repo repo) {
        MenuDialog dialog = new MenuDialog();
        dialog.setMenuItem(getResources(), R.array.repo_actions);
        dialog.setListener(new MenuDialog.OnMenuItemClickListener() {
            @Override
            public void onClick(MenuDialog.MenuItem menuItem) {
                switch (menuItem.getId()) {
                    case R.string.repo_action_delete:
                        mPresenter.deleteRepo(repo, false);
                        break;
                    case R.string.repo_action_update:
                        mPresenter.updateRepo(repo, true);
                        break;
                }
            }
        });
        dialog.show(getFragmentManager(), "actions");
    }

    @Override
    public void showDeleteConfirm(final Repo repo) {
        PromptDialog dialog = PromptDialog
                .newInstance(getString(R.string.repo_delete_confirm));
        PromptDialog.OnClickListener listener = new PromptDialog.OnClickListener() {
            @Override
            public void onClick(DialogFragment dialog, int which) {
                if (which == Dialog.BUTTON_POSITIVE) {
                    mPresenter.deleteRepo(repo, true);
                }
            }
        };

        dialog.setPositiveButton(android.R.string.yes, listener);
        dialog.setNegativeButton(android.R.string.no, listener);
        dialog.show(getFragmentManager(), "deleteConfirm");
    }

    @Override
    public void showProgress(String msg, boolean show) {
        mProgressDialog.setMessage(msg);

        if (show) {
            mProgressDialog.show(getFragmentManager(), "progressDialog");
        } else {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_OPEN_DOCUMENT && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null)
                mPresenter.addRepo(uri.toString());
        }
    }

    public static class AddFromUrlDialog extends DialogFragment {
        OnButtonClickListener mListener;

        public static AddFromUrlDialog newInstance(OnButtonClickListener listener) {
            AddFromUrlDialog dialog = new AddFromUrlDialog();
            dialog.setListener(listener);
            return dialog;
        }

        public void setListener(OnButtonClickListener listener) {
            mListener = listener;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.dialog_add_repo_from_url, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            final EditText urlTextView = view.findViewById(R.id.et_url);
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener == null) {
                        return;
                    }

                    switch (view.getId()) {
                        case R.id.button_cancel:
                            mListener.onCancel();
                            break;
                        case R.id.button_ok:
                            mListener.onOk(urlTextView.getText().toString());
                            break;
                    }

                    dismiss();
                }
            };

            view.findViewById(R.id.button_cancel).setOnClickListener(listener);
            view.findViewById(R.id.button_ok).setOnClickListener(listener);
        }

        interface OnButtonClickListener {
            void onCancel();
            void onOk(String url);
        }
    }
}
