package com.chris.recycler.collectionview;

import android.support.v4.view.ViewCompat;
import android.view.animation.LinearInterpolator;
import android.widget.OverScroller;

import com.chris.recycler.collectionview.assistant.scroll.OnScrollListener;
import com.chris.recycler.collectionview.assistant.scroll.SmoothScroller;
import com.chris.recycler.collectionview.constants.ScrollMode;
import com.chris.recycler.collectionview.structure.Point;

/**
 * Created by chris on 16/9/5.
 */
public class ViewFlingingRunnable implements Runnable {

    private int scrollMode = ScrollMode.NONE;
    private RecyclerCollectionView parent = null;
    private OnScrollListener onScrollListener = null;
    private OverScroller overScroller = null;
    private SmoothScroller smoothScroller = null;
    private Point lastFling = null;

    public ViewFlingingRunnable(RecyclerCollectionView parent) {
        this.parent = parent;
        overScroller = new OverScroller(parent.getContext(), new LinearInterpolator());
    }

    public void setOnScrollListener(OnScrollListener l) {
        this.onScrollListener = l;
    }

    public void setSmoothScroller(SmoothScroller scroller) {
        smoothScroller = scroller;
    }

    public int getScrollMode() {
        return scrollMode;
    }

    public void start(int initVelocityX, int initVelocityY) {
        int initialY = initVelocityY > 0 ? 0 : Integer.MAX_VALUE;
        lastFling = new Point(0, initialY);
        overScroller.fling(0, initialY, initVelocityX, initVelocityY, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
        scrollMode = ScrollMode.SCROLL;
        if (onScrollListener != null) {
            onScrollListener.onScrollStateChanged(parent, OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
        }
        postOnAnimation();
    }

    public void fling(int initVelocityX, int initVelocityY) {
        overScroller.fling(0, parent.getScrollY(), initVelocityX, initVelocityY,
                0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, parent.getHeight());
        scrollMode = ScrollMode.OVERFLING;
        if (onScrollListener != null) {
            onScrollListener.onScrollStateChanged(parent, OnScrollListener.SCROLL_STATE_FLING);
        }
        parent.invalidate();
        postOnAnimation();
    }

    void startSpringback() {
        if (overScroller.springBack(0, parent.getScrollY(), 0, 0, 0, 0)) {
            scrollMode = ScrollMode.OVERFLING;
            if (onScrollListener != null) {
                onScrollListener.onScrollStateChanged(parent, OnScrollListener.SCROLL_STATE_FLING);
            }
            parent.invalidate();
            postOnAnimation();
        } else {
            scrollMode = ScrollMode.NONE;
            if (onScrollListener != null) {
                onScrollListener.onScrollStateChanged(parent, OnScrollListener.SCROLL_STATE_IDLE);
            }
        }
    }

    public void stop() {
        scrollMode = ScrollMode.NONE;
        if (onScrollListener != null) {
            onScrollListener.onScrollStateChanged(parent, OnScrollListener.SCROLL_STATE_IDLE);
        }
        if (smoothScroller != null) {
            smoothScroller.scrollFinish(smoothScroller);
            smoothScroller = null;
        }
        parent.removeCallbacks(this);
        overScroller.abortAnimation();
    }

    public void postOnAnimation() {
        ViewCompat.postOnAnimation(parent, this);
    }

    @Override
    public void run() {
        OverScroller scroller = overScroller;
        boolean more = scroller.computeScrollOffset();
        int y = scroller.getCurrY();
        switch (scrollMode) {
            case ScrollMode.SCROLL: {
                int deltaY = lastFling.y - y;
                int height = parent.getHeight() - parent.getPaddingBottom() - parent.getPaddingTop();
                if (deltaY > 0) {
                    deltaY = Math.min(height - 1, deltaY);
                } else {
                    deltaY = Math.max(-(height - 1), deltaY);
                }

                boolean reachEdge = parent.trackScroll(0, deltaY);
                boolean reachEnd = reachEdge && (deltaY != 0);
                if (more && !reachEnd) {
                    if (reachEdge) {
                        parent.invalidate();
                    }
                    lastFling.y = y;
                    postOnAnimation();
                } else {
                    stop();
                }
                break;
            }

            case ScrollMode.OVERFLING: {
                if (more) {
                    final int scrollY = parent.getScrollY();
                    final int deltaY = y - scrollY;
                    if (parent.overScrollBy(0, deltaY, 0, scrollY, 0, 0, 0, parent.overflingDistance, false)) {
                        final boolean crossDown = scrollY >= 0 && y < 0;
                        final boolean crossUp = scrollY <= 0 && y > 0;
                        if (crossDown || crossUp) {
                            int velocity = (int) scroller.getCurrVelocity();
                            if (crossDown) {
                                velocity = -velocity;
                            }
                            scroller.abortAnimation();
                            start(0, velocity);
                        } else {
                            startSpringback();
                        }
                    } else {
                        parent.invalidate();
                        postOnAnimation();
                    }
                } else {
                    stop();
                }
                break;
            }

            default:
                stop();
                break;
        }
    }
}
