package com.chris.recycler.collectionview.assistant.swap;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.chris.recycler.collectionview.RecyclerCollectionView;
import com.chris.recycler.collectionview.constants.SwapDirection;
import com.chris.recycler.collectionview.constants.ViewType;
import com.chris.recycler.collectionview.structure.SectionPath;

/**
 * Created by chris on 17/5/31.
 */
public class SwapView extends ViewGroup {

    protected final int SWAP_MENU = 0;
    protected final int SWAP_SECTION_ITEM = 1;

    private int widthMeasureSpec = 0;
    private int heightMeasureSpec = 0;
    private boolean blockLayoutRequests = false;

    private RecyclerCollectionView parent = null;
    private SectionPath sectionPath = null;
    private int direction = 0;

    public SwapView(Context context) {
        this(context, null);
    }

    public SwapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SwapView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SwapView setItem(SectionPath sectionPath, RecyclerCollectionView parent) {
        this.parent = parent;
        this.sectionPath = sectionPath;
        setupChildren();
        return this;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = 0;
        int height = 0;

        switch (widthMode) {
            case MeasureSpec.EXACTLY:
            case MeasureSpec.AT_MOST:
                width = widthSize;
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                width = getMaximumWidthOrHeight(0);
                break;
        }

        switch (heightMode) {
            case MeasureSpec.EXACTLY:
            case MeasureSpec.AT_MOST:
                height = heightSize;
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                height = getMaximumWidthOrHeight(1);
                break;
        }

        this.widthMeasureSpec = widthMeasureSpec;
        this.heightMeasureSpec = heightMeasureSpec;
        setMeasuredDimension(width, height);
    }

    private int getMaximumWidthOrHeight(int flag) {
        int wh = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            switch (flag) {
                case 0:
                    if (child.getMeasuredWidth() > wh) {
                        wh = child.getMeasuredWidth();
                    }
                    break;

                case 1:
                    if (child.getMeasuredHeight() > wh) {
                        wh = child.getMeasuredHeight();
                    }
                    break;
            }
        }
        return wh;
    }

    private void measureChild(View child) {
        final LayoutParams lp = child.getLayoutParams();
        final int widthSpec = getChildMeasureSpec(widthMeasureSpec, (getPaddingLeft() + getPaddingRight()), lp.width);
        final int heightSpec = getChildMeasureSpec(heightMeasureSpec, (getPaddingTop() + getPaddingBottom()), lp.height);
        child.measure(widthSpec, heightSpec);
    }

    @Override
    public void requestLayout() {
        if (!blockLayoutRequests) {
            super.requestLayout();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (!blockLayoutRequests) {
            setupChildren();
        }
    }

    private void setupChildren() {
        blockLayoutRequests = true;
        View[] child = new View[2];
        SectionPath spItem = new SectionPath(ViewType.SECTION_ITEM, sectionPath.indexPath);
        int childCount = getChildCount();

        switch (childCount) {
            case 1:
                child[SWAP_MENU] = parent.getAdapter().getSectionSwapView(sectionPath.indexPath, null, this);
                child[SWAP_SECTION_ITEM] = parent.getAdapter().getSectionView(spItem, getChildAt(0), this);
                break;

            case 0:
            case 2:
                child[SWAP_MENU] = parent.getAdapter().getSectionSwapView(sectionPath.indexPath, getChildAt(0), this);
                child[SWAP_SECTION_ITEM] = parent.getAdapter().getSectionView(spItem, getChildAt(1), this);
                break;
        }
        detachAllViewsFromParent();

        child[SWAP_SECTION_ITEM].setLayoutParams(new RecyclerCollectionView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 0));
        measureChild(child[SWAP_SECTION_ITEM]);
        child[SWAP_SECTION_ITEM].layout(0, 0, child[SWAP_SECTION_ITEM].getMeasuredWidth(), child[SWAP_SECTION_ITEM].getMeasuredHeight());
        addView(child[SWAP_SECTION_ITEM]);

        child[SWAP_MENU].setLayoutParams(new RecyclerCollectionView.LayoutParams(LayoutParams.WRAP_CONTENT, child[SWAP_SECTION_ITEM].getMeasuredHeight(), 0));
        measureChild(child[SWAP_MENU]);
        int left = getMeasuredWidth() - getPaddingRight() - child[SWAP_MENU].getMeasuredWidth();
        child[SWAP_MENU].layout(left, 0, left + child[SWAP_MENU].getMeasuredWidth(), child[SWAP_MENU].getMeasuredHeight());
        addView(child[SWAP_MENU], 0);
        blockLayoutRequests = false;
    }

    private View getSectionItem() {
        View child = null;
        switch (getChildCount()) {
            case 1:
                child = getChildAt(0);
                break;

            case 2:
                child = getChildAt(1);
                break;
        }
        return child;
    }

    private View getSwapMenuItem() {
        View child  = null;
        if (getChildCount() == 2) {
            child = getChildAt(SWAP_MENU);
        }
        return child;
    }

    public void swap(int direction, int deltaX, int deltaY) {
        View child = getSectionItem();
        if (child != null) {
            this.direction = direction;
            if (direction == SwapDirection.HORIZONTAL) {
                swapLeftAndRight(child, -deltaX);
            } else if (direction == SwapDirection.VERTICAL) {
                child.offsetTopAndBottom(-deltaY);
            }
        }
    }

    private void swapLeftAndRight(View child, int deltaX) {
        View swapMenu = getSwapMenuItem();
        if (swapMenu == null) {
            return;
        }

        /********************************************************************************************
         * Can move distance are = [leftEdge, 0];
         ********************************************************************************************/
        int leftEdge = -swapMenu.getMeasuredWidth();
        int left = child.getLeft();

        int dx = deltaX;
        if (left + deltaX > 0) {
            dx = -left;
        } else if (left + deltaX < leftEdge) {
            dx = leftEdge - left;
        }
        child.offsetLeftAndRight(dx);
    }

    public void swapViewHint(int x, int y) {
        if (getChildCount() != 2) {
            return;
        }

        ViewGroup child = (ViewGroup) getSwapMenuItem();
        if (x < child.getLeft()) {
            return;
        }

        x -= child.getLeft();
        int childCount = child.getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < childCount; i ++) {
                View view = child.getChildAt(i);
                if (parent.isViewTouched(view, x, y)) {
                    view.performClick();
                    return;
                }
            }
        }
    }

    public void reset() {
        View child = getSectionItem();
        if (child != null) {
            swap(direction, child.getLeft(), child.getTop());
        }
    }
}
