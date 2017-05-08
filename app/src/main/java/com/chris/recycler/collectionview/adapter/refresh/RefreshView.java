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

    public RefreshView setOnRefreshListener(OnRefreshListener l) {
        onRefreshListener = l;
        return this;
    }

    public void onRefresh() {
        if (onRefreshListener != null) {
            onRefreshListener.onRefresh();
        }
    }

    public abstract int getMaxDistance();

    public abstract View getRefreshView(int status, View itemView);
}
