package com.chris.recycler.collectionview.adapter.refresh;

import android.view.View;

/**
 * Created by chris on 17/5/5.
 */
public abstract class RefreshView {

    public interface OnRefreshListener {
        public void onRefresh();
    }

    public static final int REFRESH_STATUS_NONE = 0;
    public static final int REFRESH_STATUS_PULL_UNEXCEED = 1;
    public static final int REFRESH_STATUS_PULL_EXCEED = 2;
    public static final int REFRESH_STATUS_REFRESHING = 3;

    private OnRefreshListener onRefreshListener = null;
    private int status = REFRESH_STATUS_NONE;

    public final RefreshView setOnRefreshListener(OnRefreshListener l) {
        onRefreshListener = l;
        return this;
    }

    public final void onRefresh() {
        if (onRefreshListener != null && status < REFRESH_STATUS_REFRESHING) {
            onRefreshListener.onRefresh();
        }
    }

    public void resetStatus() {
        this.status = REFRESH_STATUS_NONE;
    }

    public int getStatus() {
        return status;
    }

    public final View getRefreshView(int status, View itemView) {
        this.status = status;
        return getView(status, itemView);
    }

    public abstract int getMaxDistance();

    protected abstract View getView(int status, View itemView);
}
