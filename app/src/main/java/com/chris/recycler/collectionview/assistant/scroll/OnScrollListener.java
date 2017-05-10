package com.chris.recycler.collectionview.assistant.scroll;

import com.chris.recycler.collectionview.RecyclerCollectionView;

/**
 * Created by chris on 17/5/10.
 */
public interface OnScrollListener {

    /**
     * The view is not scrolling. Note navigating the list using the trackball counts as
     * being in the idle state since these transitions are not animated.
     */
    public static int SCROLL_STATE_IDLE = 0;

    /**
     * The user is scrolling using touch, and their finger is still on the screen
     */
    public static int SCROLL_STATE_TOUCH_SCROLL = 1;

    /**
     * The user had previously been scrolling using touch and had performed a fling. The
     * animation is now coasting to a stop
     */
    public static int SCROLL_STATE_FLING = 2;

    /**
     * If the view is being scrolled, this method will be called before the next frame of the scroll
     * is rendered. In particular, it will be called before any calls to
     *
     * @param view The view whose scroll state is being reported
     *
     * @param scrollState The current scroll state. One of SCROLL_STATE_TOUCH_SCROLL SCROLL_STATE_IDLE
     */
    public void onScrollStateChanged(RecyclerCollectionView view, int scrollState);

    /**
     * Callback method to be invoked when the view has been scrolled. This will be
     * called after the scroll has completed
     * @param view The view whose scroll state is being reported
     * @param firstVisibleItem the index of the first visible cell (ignore if visibleItemCount == 0)
     * @param visibleItemCount the number of visible cells
     * @param totalItemCount the number of items
     */
    public void onScroll(RecyclerCollectionView view, int firstVisibleItem, int visibleItemCount, int totalItemCount);
}
