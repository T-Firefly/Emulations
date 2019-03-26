package com.firefly.emulationstation.inputmap;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.commom.fragment.PromptDialog;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * Created by rany on 17-11-16.
 */

public class InputMapFragment extends GuidedStepFragment {
    private static final String TAG = InputMapFragment.class.getSimpleName();
    private static final int COUNTDOWN_TIME = 5;

    private InputMapPresenter mPresenter;
    private CompositeDisposable mDisposables = new CompositeDisposable();

    public static InputMapFragment newInstance(Bundle args) {
        InputMapFragment fragment = new InputMapFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public static InputMapFragment newInstance(int[] deviceIds, int index) {
        Bundle bundle = new Bundle();
        bundle.putIntArray(InputMapActivity.PARAMS_DEVICE_IDS, deviceIds);
        bundle.putInt(InputMapActivity.PARAMS_DEVICE_INDEX, index);

        return newInstance(bundle);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mPresenter.subscribe();

        super.onCreate(savedInstanceState);

        initActionsDescription();
    }

    @Override
    public void onPause() {
        super.onPause();

        mPresenter.unsubscribe();

        mDisposables.clear();
    }

    public void initActionsDescription() {
        List<GuidedAction> actions = getActions();
        for (int i = 0; i < actions.size(); ++i) {
            GuidedAction action = actions.get(i);
            int id = (int) action.getId();

            if (id >= 0) {
                String mapKey = mPresenter.getMapKeyValue(id);
                action.setDescription(getString(R.string.map_set_key, mapKey));
                notifyActionChanged(i);
            }
        }
    }

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        return mPresenter.onCreateGuidance(savedInstanceState);
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        int[] labelIds = mPresenter.getLabelIds();

        for (int i = 0; i < labelIds.length; ++i) {
            actions.add(new GuidedAction.Builder(getActivity())
                    .title(getString(labelIds[i]))
                    .id(i)
                    .build()
            );
        }

        actions.add(new GuidedAction.Builder(getActivity())
                .title(R.string.save_gamepad_btn)
                .id(-1)
                .build()
        );
        actions.add(new GuidedAction.Builder(getActivity())
                .title(R.string.cancel_gamepad_btn)
                .id(-2)
                .build()
        );
    }

    @Override
    public void onGuidedActionClicked(final GuidedAction action) {
        super.onGuidedActionClicked(action);

        if (action.getId() == -1) {
            mPresenter.saveConfigs();
            return;
        } else if (action.getId() == -2) {
            mPresenter.cancel();
            return;
        }

        final View view = getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_input_map, null);
        final PromptDialog dialog = new PromptDialog()
                .setContentView(view);

        TextView buttonLabelView = view.findViewById(R.id.button_label_view);
        final TextView counter = view.findViewById(R.id.counter);

        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return mPresenter.onKey(v, keyCode, event, action, dialog);
            }
        });
        view.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View v, MotionEvent event) {
                return mPresenter.onGenericMotion(v, event, action, dialog);
            }
        });

        buttonLabelView.setText(getString(R.string.map_button_label, action.getTitle()));
        counter.setText(getString(R.string.map_counter, COUNTDOWN_TIME));

         Disposable disposable = Observable.interval(1, TimeUnit.SECONDS)
                .take(COUNTDOWN_TIME)
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(Long aLong) throws Exception {
                        return COUNTDOWN_TIME - aLong - 1;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (aLong == 0) {
                            dialog.dismiss();
                            return;
                        }

                        counter.setText(getString(R.string.map_counter, aLong));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });

        mDisposables.add(disposable);

        dialog.show(getFragmentManager(), "KeyCapture");
    }

    public void updateActionUiAndMoveNext(GuidedAction action, String mapKey) {
        int selectedActionPosition = getSelectedActionPosition();

        action.setDescription(getString(R.string.map_set_key, mapKey));
        notifyActionChanged(selectedActionPosition);

        // Move action focus to next one
        if (getActions().size() > selectedActionPosition) {
            setSelectedActionPosition(selectedActionPosition + 1);
        }
    }

    public void setResultAndExit() {
        getActivity().setResult(Activity.RESULT_OK);
        finishGuidedStepFragments();
    }

    public void startNextMap(int[] deviceIds, int index) {
        InputMapFragment fragment = newInstance(deviceIds, index);
        if (mPresenter instanceof GamepadPresenter) {
            new GamepadPresenter(fragment, getActivity());
        } else {
            new KeyboardPresenter(fragment, getActivity());
        }

        add(getFragmentManager(), fragment);
    }

    public void setPresenter(InputMapPresenter presenter) {
        mPresenter = presenter;
    }
}
