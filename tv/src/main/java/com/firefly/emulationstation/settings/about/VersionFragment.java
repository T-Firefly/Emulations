package com.firefly.emulationstation.settings.about;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firefly.emulationstation.BuildConfig;
import com.firefly.emulationstation.R;
import com.firefly.emulationstation.data.bean.Version;
import com.firefly.emulationstation.data.repository.VersionRepository;
import com.firefly.emulationstation.update.NewVersionDialog;
import com.firefly.emulationstation.utils.Utils;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VersionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VersionFragment extends Fragment {
    @Inject
    VersionRepository mVersionRepository;

    public VersionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment VersionFragment.
     */
    public static VersionFragment newInstance() {
        VersionFragment fragment = new VersionFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_version, container, false);

        view.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLicensesActivity();
            }
        });

        TextView mVersionView = view.findViewById(R.id.version);
        TextView copyrightView = view.findViewById(R.id.copy_right);
        copyrightView.setMovementMethod(new ScrollingMovementMethod());

        mVersionView.setText(getString(R.string.version, BuildConfig.VERSION_NAME));

        view.findViewById(R.id.more).setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                        startLicensesActivity();
                        return true;
                }
                return false;
            }
        });
        view.findViewById(R.id.check_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disposable disposable = mVersionRepository.getVersion(true)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<Version>() {
                            @Override
                            public void accept(Version version) throws Exception {
                                if (version.getVersionCode() > BuildConfig.VERSION_CODE) {
                                    Intent intent = new Intent(getActivity(), NewVersionDialog.class);
                                    intent.putExtra(NewVersionDialog.ARG_WHICH_NEW_VERSION,
                                            NewVersionDialog.SELF_NEW_VERSION);
                                    startActivity(intent);
                                } else {
                                    Utils.showToast(getActivity(), R.string.no_new_version);
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                throwable.printStackTrace();
                                Utils.showToast(getActivity(), R.string.check_update_error);
                            }
                        });
            }
        });

        return view;
    }

    private void startLicensesActivity() {
        startActivity(new Intent(getActivity(), OssLicensesMenuActivity.class));
    }

}
