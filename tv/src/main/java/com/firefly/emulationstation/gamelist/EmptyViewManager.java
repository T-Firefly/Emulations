package com.firefly.emulationstation.gamelist;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.commom.Constants;
import com.firefly.emulationstation.data.bean.GameSystem;
import com.firefly.emulationstation.gamerepo.RepoActivity;

/**
 * Created by rany on 17-12-7.
 */

public final class EmptyViewManager {
    // Default delay for empty view widget.
    private static final long DEFAULT_PROGRESS_BAR_DELAY = 100;

    private long mInitialDelay = DEFAULT_PROGRESS_BAR_DELAY;
    private ViewGroup rootView;
    private View mEmptyView;
    private View mInfoArea;
    private TextView mPathView;
    private Handler mHandler = new Handler();
    private boolean mIsShowing;
    private View.OnClickListener mListener;

    private GameSystem mGameSystem = null;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (rootView == null) {
                return;
            }

            if (mIsShowing) {
                mEmptyView = rootView.findViewWithTag("emptyView");

                if (mEmptyView == null) {
                    mEmptyView = LayoutInflater.from(rootView.getContext())
                            .inflate(R.layout.empty_view, rootView, false);
                    mInfoArea = mEmptyView.findViewById(R.id.info);
                    mPathView = mEmptyView.findViewById(R.id.path_view);
                    mEmptyView.setTag("emptyView");

                    Button mGetRomBtn = mEmptyView.findViewById(R.id.get_rom_btn);
                    mGetRomBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Context context = rootView.getContext();

                            Intent intent = new Intent(context, RepoActivity.class);
                            intent.putExtra(RepoActivity.ARG_GAME_SYSTEM, mGameSystem);
                            context.startActivity(intent);

                            if (mListener != null) {
                                mListener.onClick(v);
                            }
                        }
                    });

                    rootView.addView(mEmptyView, 0);
                } else {
                    mEmptyView.setVisibility(View.VISIBLE);
                }

                if (mGameSystem == null) {
                    mInfoArea.setVisibility(View.GONE);
                } else {
                    mInfoArea.setVisibility(View.VISIBLE);
                    String path = mGameSystem.getRomPath();
                    String externalPath = Environment.getExternalStorageDirectory().getPath();

                    if (path.startsWith(Constants.ROM_PATH_DEVICE_PREFIX)) {
                        path = path.replace(Constants.ROM_PATH_DEVICE_PREFIX, "");
                        mPathView.setText(rootView.getContext()
                                .getString(R.string.rom_path_show, path));
                    } else if (path.startsWith(externalPath)
                            || path.startsWith("/sdcard/")
                            || path.startsWith(Constants.ROM_PATH_INTERNAL_PREFIX)) {
                        mPathView.setText(rootView.getContext()
                                .getString(R.string.rom_path_internal_show, path));
                    } else {
                        mPathView.setText(path);
                    }
                }
            }
        }
    };

    /**
     * Sets the root view on which the empty view will be attached. This class assumes the
     * root view to be {@link FrameLayout} in order to position the empty view widget
     * in the center of the screen.
     *
     * @param rootView view that will contain the empty view.
     */
    public void setRootView(ViewGroup rootView) {
        this.rootView = rootView;
    }

    /**
     * Displays the empty view.
     */
    public void show() {
        mIsShowing = true;
        mHandler.postDelayed(runnable, mInitialDelay);
    }

    public void show(GameSystem gameSystem) {
        mGameSystem = gameSystem;
        show();
    }

    /**
     * Hides the empty view.
     */
    public void hide() {
        mIsShowing = false;
        if (mEmptyView != null) {
            mEmptyView.setVisibility(View.GONE);
        }

        mHandler.removeCallbacks(runnable);
    }

    public void setListener(View.OnClickListener listener) {
        mListener = listener;
    }
}
