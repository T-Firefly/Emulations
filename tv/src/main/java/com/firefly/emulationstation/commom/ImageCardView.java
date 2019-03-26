package com.firefly.emulationstation.commom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.data.bean.DownloadInfo;

public class ImageCardView extends FrameLayout {
    public static final int HIDE_PROGRESS = -1;

    private TextView mTitleView;
    private ImageView mCardView;
    private ImageView mStatusView;
    private View mProgressArea;
    private View mMainView;
    private ProgressBar mProgressBar;
    private TextView mProgressTextView;

    public ImageCardView(Context context) {
        this(context, null);
    }

    public ImageCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        View rootView = inflate(context, R.layout.image_card_view, this);

        mTitleView = rootView.findViewById(R.id.title);
        mCardView = rootView.findViewById(R.id.imageView);
        mStatusView = rootView.findViewById(R.id.status);
        mProgressArea = rootView.findViewById(R.id.progress_area);
        mMainView = rootView.findViewById(R.id.main);
        mProgressBar = rootView.findViewById(R.id.progress_bar);
        mProgressTextView = rootView.findViewById(R.id.progress_text);
    }

    public void setTitle(String title) {
        mTitleView.setText(title);
    }

    public void setMainCardViewDrawable(Drawable drawable) {
        mCardView.setImageDrawable(drawable);
    }

    public void setMainCardViewBitmap(Bitmap bitmap) {
        mCardView.setImageBitmap(bitmap);
    }

    public ImageView getCardView() {
        return mCardView;
    }

    public void setStatus(boolean status) {
        if (status) {
            mStatusView.setVisibility(View.VISIBLE);
        } else {
            mStatusView.setVisibility(View.GONE);
        }
    }

    public void setProgress(DownloadInfo downloadInfo) {
        int progress = downloadInfo.getShowProgress();
        mProgressBar.setProgress(progress);

        switch (downloadInfo.getStatus()) {
            case DownloadInfo.STATUS_PENDING:
                setProgressVisible(true);
                mProgressBar.setIndeterminate(true);
                mProgressTextView.setText(R.string.waiting);
                break;
            case DownloadInfo.STATUS_ERROR:
                setProgressVisible(true);
                mProgressTextView.setText(R.string.error);
                mProgressBar.setIndeterminate(false);
                break;
            case DownloadInfo.STATUS_PAUSE:
                setProgressVisible(true);
                mProgressTextView.setText(R.string.pause);
                mProgressBar.setIndeterminate(false);
                break;
            case DownloadInfo.STATUS_DOWNLOADING:
                setProgressVisible(true);
                if (downloadInfo.getProgress() > 0) {
                    mProgressBar.setIndeterminate(false);
                    mProgressTextView.setText(getContext()
                            .getString(R.string.downloading_with_progress, progress));
                } else {
                    mProgressBar.setIndeterminate(true);
                    mProgressTextView.setText(R.string.connecting);
                }
                break;
            case DownloadInfo.STATUS_STOP:
                setProgressVisible(true);
                mProgressTextView.setText(R.string.canceled);
                mProgressBar.setIndeterminate(false);
                break;
            case DownloadInfo.STATUS_COMPLETED:
                setProgressVisible(true);
                mProgressBar.setIndeterminate(false);
                mProgressTextView.setText(R.string.download_completed);
                break;
            default:
                setProgressVisible(false);
                break;
        }
    }

    public void setProgressVisible(boolean show) {
        if (show) {
            mProgressArea.setVisibility(VISIBLE);
        } else {
            mProgressArea.setVisibility(GONE);
        }
    }

    public void setTitleColor(int titleColor) {
        mTitleView.setTextColor(titleColor);
    }
}
