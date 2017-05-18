package com.chris.recycler.collectionview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.chris.recycler.collectionview.assistant.adapter.base.BaseRecyclerCollectionAdapter;
import com.chris.recycler.collectionview.assistant.adapter.observer.AdapterViewDataSetObserver;
import com.chris.recycler.collectionview.assistant.adapter.wrapper.WrapperRecyclerCollectionAdapter;
import com.chris.recycler.collectionview.assistant.refresh.RefreshView;
import com.chris.recycler.collectionview.assistant.scroll.OnScrollListener;
import com.chris.recycler.collectionview.constants.RecyclerCollectionDirection;
import com.chris.recycler.collectionview.constants.ScrollMode;
import com.chris.recycler.collectionview.constants.ViewType;
import com.chris.recycler.collectionview.structure.ColumnInfo;
import com.chris.recycler.collectionview.structure.IndexPath;
import com.chris.recycler.collectionview.structure.Point;
import com.chris.recycler.collectionview.structure.SectionPath;

/**
 * Created by chris on 16/9/1.
 */
public class RecyclerCollectionView extends ViewGroup {

    private int widthMeasureSpec = 0;
    /***********************************************************************************************
     * System parameters for scroll
     ***********************************************************************************************/
    private int touchSlop = 0;
    private int maxVelocity = 0;
    private int minVelocity = 0;
    public int overflingDistance = 0;
    private VelocityTracker velocityTracker = null;

    /***********************************************************************************************
     * Runnable for scroll/fling
     ***********************************************************************************************/
    private ViewFlingingRunnable viewFlingingRunnable = null;

    /***********************************************************************************************
     * Scroll report
     ***********************************************************************************************/
    private OnScrollListener onScrollListener = null;

    /***********************************************************************************************
     * Visible views' first real-position
     ***********************************************************************************************/
    private int mPosY = 0;
    public int firstPosition = 0;
    public int touchPosition = 0;

    /***********************************************************************************************
     * Refresh Attributes
     ***********************************************************************************************/
    private float coefficient = 0.3f;

    /***********************************************************************************************
     * TouchEvents when touch down, move, up/cancel
     ***********************************************************************************************/
    private Point lastPoint = new Point();

    /***********************************************************************************************
     * Direction:
     * 0(default): from top to bottom
     * 1         : from bottom to top
     * 2         : from left to right
     * 3         : from right to left
     ***********************************************************************************************/
    private int direction = RecyclerCollectionDirection.FROM_TOP_TO_BOTTOM;

    /***********************************************************************************************
     * Block layout to re-layout under some case (Ultimate skip to layoutChildren):
     * 1. Recycler
     * 2. ReAdd new children
     ***********************************************************************************************/
    private boolean blockLayoutRequests = false;

    /***********************************************************************************************
     * Reused View collection management
     ***********************************************************************************************/
    private RecyclerCollection recyclerCollection = new RecyclerCollection(this);
    private AdapterViewDataSetObserver dataSetObserver = null;
    private WrapperRecyclerCollectionAdapter adapter = null;

    /***********************************************************************************************
     * Pinned
     ***********************************************************************************************/
    private View pinnedView = null;
    private View touchTarget = null;
    private MotionEvent downEvent = null;
    private Point touchPoint = null;
    private int translateY = 0;

    /***********************************************************************************************
     * Begin
     ***********************************************************************************************/
    public RecyclerCollectionView(Context context) {
        this(context, null);
    }

    public RecyclerCollectionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerCollectionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RecyclerCollectionView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        viewFlingingRunnable = new ViewFlingingRunnable(this);
        viewFlingingRunnable.setOnScrollListener(pinnedScrollListener);

        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        touchSlop = configuration.getScaledTouchSlop();
        maxVelocity = configuration.getScaledMaximumFlingVelocity();
        minVelocity = configuration.getScaledMinimumFlingVelocity();
        overflingDistance = configuration.getScaledOverflingDistance();
        resetVelocityTracker();
        mPosY = getPaddingTop();
    }

    public RecyclerCollectionView setAdapter(BaseRecyclerCollectionAdapter adapter) {
        if (this.adapter != null && dataSetObserver != null) {
            this.adapter.unregisterDataSetObserver(dataSetObserver);
        }
        this.adapter = new WrapperRecyclerCollectionAdapter(getContext(), adapter);
        dataSetObserver = new AdapterViewDataSetObserver();
        this.adapter.registerDataSetObserver(dataSetObserver);
        requestLayout();
        return this;
    }

    public BaseRecyclerCollectionAdapter getAdapter() {
        return adapter;
    }

    /************************************************************************************************
     * Refresh Header & Footer set
     ************************************************************************************************/
    public RecyclerCollectionView setRefreshHeader(RefreshView refreshHeader) {
        if (adapter != null) {
            adapter.setRefreshHeader(refreshHeader);
        }
        return this;
    }

    public RecyclerCollectionView setRefreshFooter(RefreshView refreshFooter) {
        if (adapter != null) {
            adapter.setRefreshFooter(refreshFooter);
        }
        return this;
    }

    public RecyclerCollectionView setRefreshCoeffcient(int coefficient) {
        this.coefficient = coefficient;
        return this;
    }

    public void onComplete() {
        Log.e("onComplete");
        releaseRefresh(0);
    }

    /************************************************************************************************
     * Scroll to sepecified section/position
     ************************************************************************************************/
    public void scrollToSectionPath(SectionPath sectionPath) {
        if (sectionPath == null || adapter == null) {
            return;
        }

        /********************************************************************************************
         * 1. Check if has refresh header
         ********************************************************************************************/
        if (adapter.getRefreshHeader() != null) {
            sectionPath.indexPath.section++;
        }

        /********************************************************************************************
         * 2. Check the specified Section if in the range;
         ********************************************************************************************/
        int sections = adapter.getSections();
        if (sectionPath.indexPath.section >= sections - 1) {
            sectionPath.indexPath.section = sections > 0 ? sections - 1 : 0;
        }

        /********************************************************************************************
         * 3. Only SECTION_ITEM may has column > 1, that will lead some mistake
         ********************************************************************************************/
        if (sectionPath.sectionType == ViewType.SECTION_ITEM &&
                adapter.getSectionItemColumn(sectionPath.indexPath.section) > 1) {
            sectionPath.indexPath.item = 0;
        }

        firstPosition = adapter.getPosition(sectionPath);
        mPosY = 0;
        requestLayout();
    }

    /************************************************************************************************
     * Scroll listener
     ************************************************************************************************/
    public void setOnScrollListener(OnScrollListener l) {
        this.onScrollListener = l;
    }

    /************************************************************************************************
     * System Override Methods
     ************************************************************************************************/
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (adapter != null && dataSetObserver == null) {
            dataSetObserver = new AdapterViewDataSetObserver();
            adapter.registerDataSetObserver(dataSetObserver);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        recyclerCollection.clear();
    }

    public void removeDetachedView(View child, boolean animate) {
        super.removeDetachedView(child, animate);
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
                width = ViewCompat.getMinimumWidth(this);
                break;
        }

        switch (heightMode) {
            case MeasureSpec.EXACTLY:
            case MeasureSpec.AT_MOST:
                height = heightSize;
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                height = ViewCompat.getMinimumHeight(this);
                break;
        }

        this.widthMeasureSpec = widthMeasureSpec;
        setMeasuredDimension(width, height);
    }

    public void measureChild(View child) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        int widthMeasureSpec = this.widthMeasureSpec;
        if (lp.getColumn() > 1) {
            final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize / lp.getColumn(), widthMode);
        }
        final int widthSpec = getChildMeasureSpec(widthMeasureSpec, (getPaddingLeft() + getPaddingRight()) / lp.getColumn(), lp.width);
        final int heightSpec = getChildMeasureSpec(getHeight(), (getPaddingTop() + getPaddingBottom()) / lp.getColumn(), lp.height);
        child.measure(widthSpec, heightSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                getChildAt(i).forceLayout();
            }
            recyclerCollection.makeChildrenDirty();
        }
        layoutChildren();
    }

    @Override
    public void requestLayout() {
        if (!blockLayoutRequests) {
            super.requestLayout();
        }
    }

    /************************************************************************************************
     * Here, real layout the children
     ************************************************************************************************/
    private void layoutChildren() {
        if (blockLayoutRequests) {
            return;
        }

        blockLayoutRequests = true;
        flingStop();
        invalidate();
        recyclerCollection.scrapAll();
        detachAllViewsFromParent();

        switch (direction) {
            case RecyclerCollectionDirection.FROM_TOP_TO_BOTTOM:
                fillDown(findSectionByPosition(firstPosition), mPosY);
                trackPinnedView(firstPosition, getChildCount());
                break;

            case RecyclerCollectionDirection.FROM_BOTTOM_TO_TOP:
                break;

            case RecyclerCollectionDirection.FROM_LEFT_TO_RIGHT:
                break;

            case RecyclerCollectionDirection.FROM_RIGHT_TO_LEFT:
                break;
        }

        blockLayoutRequests = false;
    }

    /************************************************************************************************
     * Layout the rest white space
     * 1. from top to bottom / bottom to top
     * 2. from left to right / right to left
     ************************************************************************************************/
    private void fillYGap(boolean down) {
        int count = getChildCount();
        if (down) {
            View child = getChildAt(count - 1);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            SectionPath sectionPath = new SectionPath(lp.sectionPath);
            sectionPath.indexPath.item++;

            /***************************************************************************************
             * Check when last child's sectionType == SectionItem && its colunm > 1,
             * then we will get the maximum height!
             * Last child isn't necessarily in the maximum height of the column
             ***************************************************************************************/
            int posY = 0;
            if (lp.getColumn() > 1) {
                posY = getChildAt(findBottomIndex(true, sectionPath)).getBottom();
            } else {
                posY = count > 0 ? child.getBottom() : getPaddingTop();
            }
            fillDown(sectionPath, posY);

        } else {
            View child = getChildAt(0);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            SectionPath sectionPath = new SectionPath(lp.sectionPath);
            sectionPath.indexPath.item--;

            /***************************************************************************************
             * Check when last child's sectionType == SectionItem && its colunm > 1,
             * then we will get the maximum height (The minimum top)!
             * First child isn't necessarily in the maximum height of the column
             ***************************************************************************************/
            int posY = 0;
            if (lp.getColumn() > 1) {
                posY = getChildAt(findTopIndex(sectionPath)).getTop();
            } else {
                posY = count > 0 ? child.getTop() : getBottom() - getPaddingBottom();
            }
            fillUp(sectionPath, posY);
        }
        correctGap(down);
    }

    /************************************************************************************************
     * Correct Top or Bottom Gap when scroll to the first or last child:
     * 1. First child's top may be too low that has a gap;
     * - If has, we need to adjust it and do fillGap(down = false) again
     * <p>
     * 2. Last child's bottom may be too high that has a gap;
     * - If has, we need to adjust it and do fillGap(down = true) again
     ************************************************************************************************/
    private void correctGap(boolean down) {
        if (!down) {
            if (firstPosition == 0) {
                View child = getChildAt(0);
                if (child.getTop() > getPaddingTop()) {
                    offsetChildrenTopAndBottom(getPaddingTop() - child.getTop());
                    fillYGap(true);
                }
            }
        } else {
            if (firstPosition + getChildCount() == adapter.getCount()) {
                View child = getChildAt(getChildCount() - 1);
                int height = getHeight() - getPaddingBottom();
                if (child.getBottom() < height) {
                    offsetChildrenTopAndBottom(height - child.getBottom());
                    fillYGap(false);
                }
            }
        }
    }

    /************************************************************************************************
     * Layout From Top to Bottom
     ************************************************************************************************/
    private void fillDown(SectionPath sectionPath, int posY) {
        int maxHeight = getBottom() - getPaddingTop() - getPaddingBottom();
        if (adapter != null && adapter.getCount() > 0) {
            int sections = adapter.getSections();
            int start = sectionPath.indexPath.section;
            for (int i = start; i < sections; i++) {
                int startType = i == start ? sectionPath.sectionType : ViewType.SECTION_HEADER;
                for (int sectionType = startType; sectionType <= ViewType.VIEW_FOOTER_REFRESH; sectionType++) {
                    int startItem = (i == start && sectionType == startType) ? sectionPath.indexPath.item : 0;
                    posY = fillSectionView(i, sectionType, startItem, posY, maxHeight, true);
                    if (posY > maxHeight) {
                        break;
                    }
                }
                if (posY > maxHeight) {
                    break;
                }
            }
        }
    }

    /************************************************************************************************
     * Layout From Bottom to Top
     ************************************************************************************************/
    private void fillUp(SectionPath sectionPath, int posY) {
        int top = getPaddingTop();
        if (posY < top) {
            return;
        }

        if (adapter != null && adapter.getCount() > 0) {
            int start = sectionPath.indexPath.section;
            for (int i = start; i >= 0; i--) {
                int startType = i == start ? sectionPath.sectionType : ViewType.VIEW_FOOTER_REFRESH;
                for (int sectionType = startType; sectionType >= ViewType.SECTION_HEADER; sectionType--) {
                    if (posY < top) {
                        break;
                    }
                    int items = adapter.getSectionItemInSection(sectionType, i) - 1;
                    int startItem = (i == start && sectionType == startType) ? sectionPath.indexPath.item : items;
                    posY = fillSectionView(i, sectionType, startItem, posY, top, false);
                }
                if (posY < top) {
                    break;
                }
            }
            firstPosition = adapter.getPosition(((LayoutParams) getChildAt(0).getLayoutParams()).getSectionPath());
        }
    }

    /************************************************************************************************
     * Layout Section's header, item, footer
     ************************************************************************************************/
    private int fillSectionView(int section, int viewType, int beginItem, int posY, int endY, boolean down) {
        SectionPath sectionPath = null;
        IndexPath indexPath = new IndexPath(section);
        int items = adapter.getSectionItemInSection(viewType, section);
        int column = adapter.getSectionItemColumn(section);
        for (int j = beginItem; down ? j < items : j >= 0; j = down ? ++j : --j) {
            indexPath.item = j;
            sectionPath = new SectionPath(viewType, indexPath);

            /****************************************************************************************
             * Re-check the posY when is SectionItem and column > 1
             *
             * Notice:
             * (This notice only under two sections that both have no SectionHeader and SectionFooter)
             * If only if the item < column (0...column - 1) we should pay attention:
             * 1. When item = 0, we should use the posY to set its top (new section);
             * 2. When item > 0 && < column, we only use last child's top then ok;
             ****************************************************************************************/
            if (viewType == ViewType.SECTION_ITEM && column > 1) {
                if (down) {
                    final int childCount = getChildCount();
                    int index = j == 0 ? 0 : findBottomIndex(false, sectionPath);
                    if (index < 0) {
                        if (j < column) {
                            posY = getChildAt(childCount - 1).getTop();
                        } else {
                            posY = getChildAt(childCount + index).getBottom();
                        }
                    }
                } else {
                    int index = findTopIndex(sectionPath);
                    if (index >= 0) {
                        posY = getChildAt(index).getTop();
                    }
                }
            }

            if (down && posY > endY) {
                break;
            } else if (!down && posY < endY) {
                break;
            }

            View child = makeAndAttachView(sectionPath, posY, down);
            if (down && viewType == ViewType.SECTION_ITEM && column > 1 && j + 1 == items) {
                posY = getChildAt(findBottomIndex(true, sectionPath)).getBottom();
            } else if (!down && viewType == ViewType.SECTION_ITEM && column > 1 && j == 0) {
                posY = getChildAt(0).getTop();
            } else if (child != null) {
                posY += down ? child.getMeasuredHeight() : -child.getMeasuredHeight();
            }
        }
        return posY;
    }

    /************************************************************************************************
     * New view and add to parent
     ************************************************************************************************/
    private View makeAndAttachView(SectionPath sectionPath, int posY, boolean down) {
        View child = makeView(sectionPath);
        if (child != null) {
            setupChildView(sectionPath, child, posY, down);
        }
        return child;
    }

    /************************************************************************************************
     * New view from adapter or from scrap that reused
     ************************************************************************************************/
    private View makeView(SectionPath sectionPath) {
        View scrapView = recyclerCollection.getScrapView(sectionPath);
        View child = adapter.getSectionView(sectionPath, scrapView, this);
        if (scrapView != null && scrapView != child) {
            throw new RuntimeException("forgot to reused?");
        }
        if (child != null) {
            setSectionItemLayoutParam(child, sectionPath);
        } else {
            throw new RuntimeException("convertView is null?");
        }
        return child;
    }

    /************************************************************************************************
     * Attach child to the parent
     ************************************************************************************************/
    private void setupChildView(SectionPath sectionPath, View child, int y, boolean down) {
        measureChild(child);
        Point point = adjust(sectionPath, child, y, down);
        addView(child, down ? -1 : 0);
        child.layout(point.x, point.y, point.x + child.getMeasuredWidth(), point.y + child.getMeasuredHeight());
    }

    /************************************************************************************************
     * Calculate the real position if column > 1
     ************************************************************************************************/
    private Point adjust(SectionPath sectionPath, View child, int y, boolean down) {
        Point point = new Point();
        int column = adapter.getSectionItemColumn(sectionPath.indexPath.section);

        if (column > 1 && ViewType.SECTION_ITEM == sectionPath.getSectionType()) {
            int columnWidth = (getWidth() - getPaddingLeft() - getPaddingRight()) / column;
            int items = adapter.getItemsInSection(sectionPath.indexPath.section);
            ColumnInfo info = findRelativeIndex(down, sectionPath, down ? sectionPath.indexPath.item : items);
            point.x = info.column * columnWidth + getPaddingLeft();
            if (down) {
                int childCount = getChildCount();
                point.y = sectionPath.indexPath.item < column ? y : getChildAt(childCount + info.item).getBottom();
            } else {
                point.y = getChildAt(info.item).getTop() - child.getMeasuredHeight() + info.offset;
            }
        } else { // SECTION_HEADER, SECTION_FOOTER, column = 1
            point.setPoint(getPaddingLeft(), y);
            if (!down) {
                point.y -= child.getMeasuredHeight();
            }
        }

        return point;
    }

    /************************************************************************************************
     * Find Section (Header, Item, Footer) index by Position
     ************************************************************************************************/
    private SectionPath findSectionByPosition(int position) {
        SectionPath sp = null;
        int sections = adapter != null ? adapter.getSections() : 0;
        for (int i = 0; i < sections; i++) {
            for (int sectionType = ViewType.SECTION_HEADER; sectionType <= ViewType.VIEW_FOOTER_REFRESH; sectionType++) {
                int count = adapter.getSectionItemInSection(sectionType, i);
                if (position - count <= 0) {
                    sp = new SectionPath(sectionType, new IndexPath(i, position));
                    if (sp.sectionType == ViewType.SECTION_HEADER && sp.indexPath.section == 0) {
                        mPosY = getPaddingTop();
                    }
                    return sp;
                }
                position -= count;
            }
        }
        return null;
    }

    /************************************************************************************************
     * Find the view index that in the max/min height of the column
     ************************************************************************************************/
    private int findTopIndex(SectionPath sectionPath) {
        int childCount = getChildCount();
        ColumnInfo[] info = initColumnInfo(sectionPath, Integer.MAX_VALUE);
        int columnWidth = getWidth() - getPaddingLeft() - getPaddingRight();

        /*******************************************************************************************
         * We only check the SectionItem in the same Section
         *
         * e.g.
         * If section has 8 items and current sectionPath.indexPath.item = 5,
         * it means items(6,7) are already visible, and item-6 is [0], item-7 is [1] (Usually).
         * So end = 8-5-1 = 2 and we only go through i = 0,1 (i < end = 2)
         *******************************************************************************************/
        int items = adapter.getItemsInSection(sectionPath.indexPath.section);
        int count = items - sectionPath.indexPath.item - 1;
        int end = count > childCount ? childCount : count;
        for (int i = 0; i < end; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp.getSectionType() != ViewType.SECTION_ITEM || lp.getColumn() <= 1) {
                continue;
            }

            int col = child.getLeft() * lp.getColumn() / columnWidth;
            if (info[col].height > child.getTop()) {
                info[col].item = i;
                info[col].height = child.getTop();
            }
        }

        int col = findMaxHeightIndex(info);
        return info[col].item < 0 ? items - sectionPath.indexPath.item - 1 : info[col].item;
    }

    private int findBottomIndex(boolean max, SectionPath sectionPath) {
        final int childCount = getChildCount();
        ColumnInfo[] info = initColumnInfo(sectionPath, -Integer.MAX_VALUE);

        /*******************************************************************************************
         * We only check the SectionItem in the same Section but differentiate [findTopIndex]
         *
         * e.g.
         * If section has 8 items and current sectionPath.indexPath.item = 3, childCount = 10,
         * it means items(0,1,2) are already visible, and item-0 is [7], item-1 is [8], item-2 is [9]
         * So start = 10 - 3 = 7 and we only go through i = 7,8,9
         *******************************************************************************************/
        int start = sectionPath.indexPath.item > childCount ? 0 : childCount - sectionPath.indexPath.item;
        for (int i = start; i < childCount; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp.getSectionType() != ViewType.SECTION_ITEM || lp.getColumn() <= 1 ||
                    lp.sectionPath.indexPath.section != sectionPath.indexPath.section) {
                continue;
            }
            int col = findMinHeightIndex(info);
            info[col].item = max ? i : i - start;
            info[col].height = child.getBottom();
        }

        int col = 0;
        int idx = 0;
        if (max) {
            col = findMaxHeightIndex(info);
            idx = info[col].item < 0 ? childCount - 1 : info[col].item;
        } else {
            col = findMinHeightIndex(info);
            idx = info[col].item - sectionPath.indexPath.item;
            /***************************************************************************************
             * If overfling, sometimes the childCount < visible childCount
             ***************************************************************************************/
            if (Math.abs(idx) > childCount) {
                idx = info[col].item - childCount;
            }
        }
        return idx;
    }

    /************************************************************************************************
     * Find the relative index
     ************************************************************************************************/
    private ColumnInfo findRelativeIndex(boolean down, SectionPath sectionPath, int items) {
        ColumnInfo[] info = initColumnInfo(sectionPath, -1);
        SectionPath path = new SectionPath(sectionPath);
        int childCount = getChildCount();
        View view = null;
        int i = 0;
        ColumnInfo ret = null;
        while (i < items) {
            path.indexPath.item = i;

            if (down && childCount > items) {
                view = getChildAt(childCount - items + i);
            } else {
                view = makeView(path);
                measureChild(view);
                recyclerCollection.addScrapView(view);
            }

            int col = findMinHeightIndex(info);
            if (!down) {
                if (info[col].item == sectionPath.indexPath.item) {
                    ret = new ColumnInfo(i - sectionPath.indexPath.item - 1, col);
                    break;
                } else if (i == sectionPath.indexPath.item) {
                    ret = new ColumnInfo(items - i - 1, col);
                }
            }

            info[col].item = i;
            info[col].height += view.getMeasuredHeight();
            i++;
        }

        if (down) {
            int col = findMinHeightIndex(info);
            ret = new ColumnInfo(info[col].item - sectionPath.indexPath.item, col);
        } else if (ret != null && sectionPath.indexPath.item + ret.item + 1 >= items) {
            int col = findMaxHeightIndex(info);
            ret.offset = info[ret.column].height - info[col].height;
        }
        return ret;
    }

    /************************************************************************************************
     * Column Info(item index, column index, column's total height, columns' gap)
     ************************************************************************************************/
    private ColumnInfo[] initColumnInfo(SectionPath sectionPath, int height) {
        int column = adapter.getSectionItemColumn(sectionPath.indexPath.section);
        ColumnInfo[] columnInfos = new ColumnInfo[column];
        for (int i = 0; i < column; i++) {
            columnInfos[i] = new ColumnInfo(height);
        }
        return columnInfos;
    }

    /************************************************************************************************
     * Column index that has minimum hegiht
     ************************************************************************************************/
    private int findMinHeightIndex(ColumnInfo[] columnInfos) {
        int index = 0;
        if (columnInfos != null) {
            for (int i = 1; i < columnInfos.length; i++) {
                if (columnInfos[index].height > columnInfos[i].height) {
                    index = i;
                }
            }
        }
        return index;
    }

    /************************************************************************************************
     * Column index that has maximum hegiht
     ************************************************************************************************/
    private int findMaxHeightIndex(ColumnInfo[] columnInfos) {
        int index = 0;
        if (columnInfos != null) {
            for (int i = 1; i < columnInfos.length; i++) {
                if (columnInfos[index].height < columnInfos[i].height) {
                    index = i;
                }
            }
        }
        return index;
    }

    /************************************************************************************************
     * TouchEvent process
     ************************************************************************************************/
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN && pinnedView != null &&
                touchTarget == null && isPinnedViewTouched(ev.getX(), ev.getY())) {
            touchTarget = pinnedView;
            touchPoint = new Point((int) ev.getX(), (int) ev.getY());
            downEvent = MotionEvent.obtain(ev);
        }

        if (touchTarget != null) {
            if (isPinnedViewTouched(ev.getX(), ev.getY())) {
                pinnedView.dispatchTouchEvent(ev);
            }

            switch (ev.getAction()) {
                case MotionEvent.ACTION_UP:
                    super.dispatchTouchEvent(ev);
                    performPinnedViewClick();
                    clearTouchTarget();
                    break;

                case MotionEvent.ACTION_CANCEL:
                    clearTouchTarget();
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (Math.abs(ev.getY() - touchPoint.y) > touchSlop) {
                        MotionEvent event = MotionEvent.obtain(ev);
                        event.setAction(MotionEvent.ACTION_CANCEL);
                        touchTarget.dispatchTouchEvent(event);
                        event.recycle();

                        super.dispatchTouchEvent(downEvent);
                        super.dispatchTouchEvent(ev);
                        clearTouchTarget();
                    }
                    break;
            }
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (velocityTracker != null) {
            velocityTracker.addMovement(event);
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastPoint.setPoint((int) event.getX(), (int) event.getY());
                touchPosition = findTouchIndex((int) event.getX(), (int) event.getY());
                if (viewFlingingRunnable.getScrollMode() == ScrollMode.SCROLL ||
                        viewFlingingRunnable.getScrollMode() == ScrollMode.OVERFLING) {
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (checkNeedScroll((int) event.getX(), (int) event.getY(), event)) {
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (velocityTracker != null) {
            velocityTracker.addMovement(event);
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                actionDown(event);
                break;

            case MotionEvent.ACTION_MOVE:
                motionMove(event);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                motionEnd();
                break;
        }
        return true;
    }

    private void resetVelocityTracker() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        } else {
            velocityTracker.clear();
        }
    }

    private boolean checkNeedScroll(int x, int y, MotionEvent ev) {
        boolean need = false;
        int deltaX = lastPoint.x - x;
        int deltaY = lastPoint.y - y;

        if (direction <= RecyclerCollectionDirection.FROM_BOTTOM_TO_TOP && Math.abs(deltaY) > touchSlop) {
            need = true;
        } else if (direction >= RecyclerCollectionDirection.FROM_LEFT_TO_RIGHT && Math.abs(deltaX) > touchSlop) {
            need = true;
        }

        return need;
    }

    /************************************************************************************************
     * Stop view scroll or fling
     ************************************************************************************************/
    private void flingStop() {
        if (viewFlingingRunnable.getScrollMode() != ScrollMode.NONE) {
            viewFlingingRunnable.stop();
        }
    }

    /************************************************************************************************
     * MotionEvent-Down
     ************************************************************************************************/
    private void actionDown(MotionEvent event) {
        flingStop();
    }

    /************************************************************************************************
     * MotionEvent-Move
     ************************************************************************************************/
    private void motionMove(MotionEvent event) {
        int deltaX = 0;
        int deltaY = (int) (lastPoint.y - event.getY());
        lastPoint.setPoint((int) event.getX(), (int) event.getY());
        boolean cantScroll = trackScroll(deltaX, deltaY);

        /********************************************************************************************
         * cantScroll = true means at top or at bottom
         ********************************************************************************************/
        if (cantScroll) {
            trackRefresh(deltaX, deltaY);
        }
    }

    /************************************************************************************************
     * MotionEvent-Up
     ************************************************************************************************/
    private void motionEnd() {
        int childCount = getChildCount();
        if (childCount > 0) {
            int firstTop = getChildAt(0).getTop();
            int lastBottom = getChildAt(childCount - 1).getBottom();
            if (firstPosition == 0 && firstTop >= getPaddingTop() && firstPosition + childCount < adapter.getCount() &&
                    lastBottom <= getBottom()) {
                return;
            }

            if (adapter.getRefreshHeader() != null || adapter.getRefreshFooter() != null) {
                releaseRefresh(2);
            }

            /***************************************************************************************
             * Compute velocity: initVelocityY < 0 = down; > 0 = up
             ***************************************************************************************/
            velocityTracker.computeCurrentVelocity(1000, maxVelocity);
            int initVelocityX = (int) velocityTracker.getXVelocity();
            int initVelocityY = (int) velocityTracker.getYVelocity();
            viewFlingingRunnable.start(initVelocityX, initVelocityY);
        }
        resetVelocityTracker();
    }

    /************************************************************************************************
     * Find view index by touch event(getX & getY)
     ************************************************************************************************/
    private int findTouchIndex(int motionX, int motionY) {
        int childCount = getChildCount();
        int touchIndex = -1;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getLeft() <= motionX && motionX < child.getRight() &&
                    child.getTop() <= motionY && motionY < child.getBottom()) {
                touchIndex = i;
                break;
            }
        }
        return firstPosition + touchIndex;
    }

    /************************************************************************************************
     * Check
     ************************************************************************************************/
    private boolean canScrollDown(int deltaY) {
        int firstPosition = this.firstPosition;
        int firstTop = getChildAt(0).getTop();
        return (firstPosition == 0 && firstTop >= getPaddingTop() && deltaY <= 0);
    }

    private boolean canScrollUp(int deltaY) {
        int firstPosition = this.firstPosition;
        int childCount = getChildCount();
        int lastBottom = getChildAt(childCount - 1).getBottom();
        return (firstPosition + childCount == adapter.getCount() && lastBottom <= getHeight() - getPaddingBottom() && deltaY >= 0);
    }

    private boolean canScrollRefreshHeader() {
        boolean ret = false;
        if (firstPosition == 0 && adapter.getRefreshHeader() != null) {
            LayoutParams lp = (LayoutParams) getChildAt(0).getLayoutParams();
            if (lp.height > 0 && adapter.getRefreshHeader().getStatus() < RefreshView.REFRESH_STATUS_REFRESHING) {
                ret = true;
            }
        }
        return ret;
    }

    private boolean canScrollRefreshFooter() {
        boolean ret = false;
        int childCount = getChildCount();
        if (firstPosition + childCount == adapter.getCount() && adapter.getRefreshFooter() != null) {
            LayoutParams lp = (LayoutParams) getChildAt(childCount - 1).getLayoutParams();
            if (lp.height > 0 && adapter.getRefreshFooter().getStatus() < RefreshView.REFRESH_STATUS_REFRESHING) {
                ret = true;
            }
        }
        return ret;
    }

    /************************************************************************************************
     * Track scroll:
     * First, scrap the view that out of the screen
     * Second, layout new view that will in the screen
     ************************************************************************************************/
    public boolean trackScroll(int deltaX, int deltaY) {
        boolean cantScrollDown = canScrollDown(deltaY);
        boolean cantScrollUp = canScrollUp(deltaY);
        if (cantScrollDown || cantScrollUp || canScrollRefreshHeader() || canScrollRefreshFooter()) {
            return deltaY != 0;
        }
        doScroll(deltaX, deltaY);

        if (pinnedScrollListener != null) {
            pinnedScrollListener.onScroll(this, firstPosition, getChildCount(), adapter.getCount());
        }
        return false;
    }

    /************************************************************************************************
     * Track up and down
     * 1. deltaY > 0 means down
     * 2. deltaY < 0 means up
     * Out of screen will be recycler
     ************************************************************************************************/
    private int[] trackUpDown(int deltaY, int childCount) {
        int start = 0;
        int count = 0;
        if (deltaY > 0) {
            int top = deltaY + getPaddingTop();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                if (child.getBottom() >= top) {
                    break;
                } else {
                    count++;
                    recyclerCollection.addScrapView(child);
                }
            }
            this.firstPosition += count;
        } else {
            int bottom = getHeight() + deltaY - getPaddingBottom();
            for (int i = childCount - 1; i >= 0; i--) {
                View child = getChildAt(i);
                if (child.getTop() <= bottom) {
                    break;
                } else {
                    start = i;
                    count++;
                    recyclerCollection.addScrapView(child);
                }
            }
        }
        return new int[]{start, count};
    }

    /************************************************************************************************
     * Track up and down
     * 1. deltaY > 0 means right to left
     * 2. deltaY < 0 means left to right
     * Out of screen will be recycler
     ************************************************************************************************/
    private int[] trackLeftRight(int deltaX, int childCount) {
        int start = 0;
        int count = 0;

        return new int[]{start, count};
    }

    /************************************************************************************************
     * offset children's top, bottom, left, right
     ************************************************************************************************/
    private void doScroll(int deltaX, int deltaY) {
        int childCount = getChildCount();
        int firstTop = getChildAt(0).getTop();
        int lastBottom = getChildAt(childCount - 1).getBottom();
        int column = 0;
        if (deltaY > 0) {
            column = ((LayoutParams) getChildAt(childCount - 1).getLayoutParams()).getColumn();
        } else {
            column = ((LayoutParams) getChildAt(0).getLayoutParams()).getColumn();
        }

        /*******************************************************************************************
         * int[]{start, count}
         *******************************************************************************************/
        int[] startAndCount = null;
        if (direction <= RecyclerCollectionDirection.FROM_BOTTOM_TO_TOP) {
            startAndCount = trackUpDown(deltaY, childCount);
        }

        /*******************************************************************************************
         * Frozen layout till finish to recycler and re-layout children
         *******************************************************************************************/
        blockLayoutRequests = true;

        if (startAndCount != null) {
            if (startAndCount[1] > 0) {
                detachViewsFromParent(startAndCount[0], startAndCount[1]);
            }
        }

        /*******************************************************************************************
         * invalidate before moving the children to avoid unnecessary invalidate
         * calls to bubble up from the children all the way to the top
         *******************************************************************************************/
        if (!awakenScrollBars()) {
            invalidate();
        }

        /*******************************************************************************************
         * Adjust child's top & bottom / left & right
         *******************************************************************************************/
        if (direction <= RecyclerCollectionDirection.FROM_BOTTOM_TO_TOP) {
            offsetChildrenTopAndBottom(-deltaY);
        } else if (direction >= RecyclerCollectionDirection.FROM_LEFT_TO_RIGHT) {
            offsetChildrenLeftAndRight(-deltaX);
        }

        final int spaceAbove = getPaddingTop() - firstTop;
        final int end = getHeight() - getPaddingBottom();
        final int spaceBelow = lastBottom - end;
        int absDeltaY = Math.abs(deltaY);
        if (spaceAbove < absDeltaY || spaceBelow < absDeltaY || column > 1) {
            fillYGap(deltaY > 0);
        }

        blockLayoutRequests = false;
    }

    /************************************************************************************************
     * At Top or Bottom, then check if have RefreshHeader or RefreshFooter
     ************************************************************************************************/
    private void trackRefresh(int deltaX, int deltaY) {
        if (adapter.getRefreshHeader() != null || adapter.getRefreshFooter() != null) {
            if (direction <= RecyclerCollectionDirection.FROM_BOTTOM_TO_TOP) {
                View view = null;
                int dy = (int) (deltaY * coefficient);
                if ((canScrollDown(deltaY) || canScrollRefreshHeader()) &&
                        adapter.getRefreshHeader() != null && adapter.getRefreshHeader().getStatus() < RefreshView.REFRESH_STATUS_REFRESHING &&
                        (adapter.getRefreshFooter() == null || adapter.getRefreshFooter().getStatus() < RefreshView.REFRESH_STATUS_REFRESHING)) {
                    /*******************************************************************************
                     * Pull Down For Refresh
                     *******************************************************************************/
                    mPosY = getPaddingTop();
                    view = getChildAt(0);
                } else if ((canScrollUp(deltaY) || canScrollRefreshFooter()) &&
                        adapter.getRefreshFooter() != null && adapter.getRefreshFooter().getStatus() < RefreshView.REFRESH_STATUS_REFRESHING &&
                        (adapter.getRefreshHeader() == null || adapter.getRefreshHeader().getStatus() < RefreshView.REFRESH_STATUS_REFRESHING)) {
                    /*******************************************************************************
                     * Pull Up For Load
                     *******************************************************************************/
                    mPosY = getChildAt(0).getTop() - dy;
                    view = getChildAt(getChildCount() - 1);
                } else {
                    return;
                }

                if (view != null) {
                    LayoutParams lp = (LayoutParams) view.getLayoutParams();
                    lp.height = firstPosition == 0 ? view.getHeight() - dy : view.getHeight() + dy;
                    view.setLayoutParams(lp);
                    refreshStatusChange(firstPosition == 0, view, lp.height, 1);
                }
            }
        }
    }

    private void releaseRefresh(int tracking) {
        View view = null;
        int childCount = getChildCount();

        if (childCount > 0 && (adapter.getRefreshHeader() != null || adapter.getRefreshFooter() != null)) {
            if (direction <= RecyclerCollectionDirection.FROM_BOTTOM_TO_TOP) {
                if (adapter.getRefreshHeader() != null && firstPosition == 0
                        && adapter.getRefreshHeader().getStatus() > RefreshView.REFRESH_STATUS_NONE) {
                    view = getChildAt(0);
                    refreshStatusChange(true, view, ((LayoutParams) view.getLayoutParams()).height, tracking);
                } else if (adapter.getRefreshFooter() != null && firstPosition + childCount == adapter.getCount()
                        && adapter.getRefreshFooter().getStatus() > RefreshView.REFRESH_STATUS_NONE) {
                    view = getChildAt(getChildCount() - 1);
                    refreshStatusChange(false, view, ((LayoutParams) view.getLayoutParams()).height, tracking);
                } else if (tracking == 0) {
                    if (adapter.getRefreshHeader() != null) {
                        adapter.getRefreshHeader().resetStatus();
                    }
                    if (adapter.getRefreshFooter() != null) {
                        adapter.getRefreshFooter().resetStatus();
                    }
                }
                mPosY = getChildAt(0).getTop();
            }
        }
    }

    /************************************************************************************************
     * Refresh Header & Footer status change
     * 1. header:
     * - true => header;
     * - false => footer;
     * 2. tracking:
     * - 0 => Reset
     * - 1 => MotionEvent.ACTION_MOVE (moving)
     * - 2 => MotionEvent.ACTION_UP/ACTION_CANCEL (release)
     ************************************************************************************************/
    private void refreshStatusChange(boolean header, View view, int height, int tracking) {
        RefreshView refreshView = null;
        if (header && adapter.getRefreshHeader() != null) {
            refreshView = adapter.getRefreshHeader();
        } else if (adapter.getRefreshFooter() != null) {
            refreshView = adapter.getRefreshFooter();
        }

        int max = 0, status = 0;
        if (refreshView != null) {
            max = refreshView.getMaxDistance();
            switch (tracking) {
                default:
                case 0:
                    status = RefreshView.REFRESH_STATUS_NONE;
                    break;
                case 1:
                    status = height < max ? RefreshView.REFRESH_STATUS_PULL_UNEXCEED : RefreshView.REFRESH_STATUS_PULL_EXCEED;
                    break;
                case 2:
                    status = height < max ? RefreshView.REFRESH_STATUS_NONE : RefreshView.REFRESH_STATUS_REFRESHING;
                    if (status == RefreshView.REFRESH_STATUS_REFRESHING) {
                        refreshView.onRefresh();
                    }
                    break;
            }
            refreshView.getRefreshView(status, view);

            if (tracking == 0 || tracking == 2) {
                refreshLayoutChange(header, view, status, max);
            }
        }
    }

    /************************************************************************************************
     * Change Refresh Header's / Footer's height and bottom
     ************************************************************************************************/
    private void refreshLayoutChange(boolean header, View view, int status, int max) {
        if (view == null) {
            return;
        }

        LayoutParams lp = (LayoutParams) view.getLayoutParams();
        if (lp.getSectionType() < ViewType.VIEW_HEADER_REFRESH) {
            return;
        }

        int offsetY = 0;
        if (status == RefreshView.REFRESH_STATUS_REFRESHING) {
            offsetY = header ? 0 : lp.height - max;
            lp.height = max;
        } else {
            offsetY = header ? 0 : lp.height;
            lp.height = 0;
        }
        view.setLayoutParams(lp);

        if (!header && offsetY > 0) {
            /****************************************************************************************
             * adjust bottom because of height
             ****************************************************************************************/
            view.setBottom(view.getBottom() - offsetY);

            /****************************************************************************************
             * adjust children's bottom because of RefreshFooter's height
             ****************************************************************************************/
            doScroll(0, offsetY);
        }
    }

    /************************************************************************************************
     * Offset Children's top & bottom, both += offsetY to adjust their boundary
     ************************************************************************************************/
    private void offsetChildrenTopAndBottom(int offsetY) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            ViewCompat.offsetTopAndBottom(getChildAt(i), offsetY);
        }
    }

    /************************************************************************************************
     * Offset Children's top & bottom, both += offsetY to adjust their boundary
     ************************************************************************************************/
    private void offsetChildrenLeftAndRight(int offsetX) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            ViewCompat.offsetLeftAndRight(getChildAt(i), offsetX);
        }
    }

    /************************************************************************************************
     * Scroller code block
     * <p>
     * 1. overScrollBy be used by OverScroller;
     * 2. onOverScrolled should be override when use View.overScrollBy;
     * 3. When use View.overScrollBy, then should give:
     * - 3.1 override computeVerticalScrollRange
     * - 3.2 override computeVerticalScrollExtent
     * <p>
     * Notice: only fling will trigger these operation, start won't
     ************************************************************************************************/
    public boolean overScrollBy(int dx, int dy, int sx, int sy, int srx, int sry, int osx, int osy, boolean tv) {
        return super.overScrollBy(dx, dy, sx, sy, srx, sry, osx, osy, tv);
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        if (getScrollY() != scrollY) {
            onScrollChanged(getScrollX(), scrollY, getScrollX(), getScrollY());
            setScrollY(scrollY);
            if (isHardwareAccelerated() && getParent() instanceof View) {
                ((View) getParent()).invalidate();
            }
            awakenScrollBars();
        }
    }

    @Override
    protected int computeVerticalScrollRange() {
        int result = Math.max(adapter.getCount() * 100, 0);
        if (getScrollY() != 0) {
            result += Math.abs((int) ((float) getScrollY() / getHeight() * adapter.getCount() * 100));
        }
        return result;
    }

    @Override
    protected int computeVerticalScrollExtent() {
        int childCount = getChildCount();
        int extent = 1;
        if (childCount > 0) {
            extent = childCount * 100;

            View view = getChildAt(0);
            final int top = view.getTop();
            int height = view.getHeight();
            if (height > 0) {
                extent += (top * 100) / height;
            }

            view = getChildAt(childCount - 1);
            final int bottom = view.getBottom();
            height = view.getHeight();
            if (height > 0) {
                extent -= ((bottom - getHeight()) * 100) / height;
            }
        }
        return extent;
    }

    /************************************************************************************************
     * Pinned Relative
     * 1. Scroll Listener for monitor;
     * 2. DispatchDraw: draw pinned view in canvas;
     * 3. Check PinnedView if touched;
     * - isPinnedViewTouched
     * - performPinnedViewClick
     * - clearTouchTarget
     ************************************************************************************************/
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        if (pinnedView != null) {
            int saveCount = canvas.save();
            canvas.translate(paddingLeft, paddingTop + translateY);
            canvas.clipRect(0, paddingTop, paddingLeft + pinnedView.getWidth(), paddingTop + pinnedView.getHeight());
            drawChild(canvas, pinnedView, getDrawingTime());
            canvas.restoreToCount(saveCount);
        }
    }

    private final OnScrollListener pinnedScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerCollectionView view, int scrollState) {
            if (onScrollListener != null) {
                onScrollListener.onScrollStateChanged(view, scrollState);
            }
        }

        @Override
        public void onScroll(RecyclerCollectionView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (onScrollListener != null) {
                onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }

            if (adapter.getAdapter() == null || adapter.getAdapter().getCount() == 0) {
                return;
            }

            trackPinnedView(firstVisibleItem, visibleItemCount);
        }
    };

    private void trackPinnedView(int firstVisibleItem, int visibleItemCount) {
        boolean pinned = false;
        SectionPath sectionPath = adapter.getSectionPath(firstVisibleItem);
        if (sectionPath.sectionType == ViewType.SECTION_HEADER && sectionPath.indexPath.item == 0) {
            pinned = adapter.isSectionHeaderPinned(sectionPath.getIndexPath());
        }

        if (pinned) {
            View sectionView = getChildAt(0);
            if (sectionView.getTop() == getPaddingTop()) {
                recyclerPinnedView();
            } else {
                ensurePinnedView(sectionPath, firstVisibleItem, visibleItemCount);
            }
        } else {
            sectionPath = findCurrentSectionPinnedView(sectionPath);
            if (sectionPath != null) {
                ensurePinnedView(sectionPath, firstVisibleItem, visibleItemCount);
            } else {
                recyclerPinnedView();
            }
        }
    }

    private void ensurePinnedView(SectionPath sectionPath, int firstVisibleItem, int visibleItemCount) {
        if (pinnedView != null) {
            SectionPath sp = ((LayoutParams) pinnedView.getLayoutParams()).getSectionPath();
            if (!sp.equals(sectionPath)) {
                recyclerPinnedView();
            }
        }

        if (pinnedView == null && sectionPath.sectionType == ViewType.SECTION_HEADER && sectionPath.indexPath.item == 0) {
            makePinnedView(sectionPath);
        }
        checkNextPinnedView(sectionPath, firstVisibleItem, visibleItemCount);
    }

    private void makePinnedView(SectionPath sectionPath) {
        pinnedView = makeView(sectionPath);
        measureChild(pinnedView);
        pinnedView.layout(0, 0, pinnedView.getMeasuredWidth(), pinnedView.getMeasuredHeight());
        translateY = 0;
    }

    private void checkNextPinnedView(SectionPath sectionPath, int firstVisibleItem, int visibleItemCount) {
        SectionPath sp = new SectionPath(sectionPath);
        sp.indexPath.section++;
        if (sp.indexPath.section < adapter.getSections()) {
            int pos = adapter.getPosition(sp);
            if (pos > -1 && firstVisibleItem + visibleItemCount > pos) {
                if (adapter.isSectionHeaderPinned(sp.getIndexPath())) {
                    View nextPinnedView = getChildAt(pos - firstVisibleItem);
                    final int bottom = pinnedView.getBottom() + getPaddingTop();
                    int distanceY = nextPinnedView.getTop() - bottom;
                    if (distanceY < 0) {
                        translateY = distanceY;
                    } else {
                        translateY = 0;
                    }
                }
            }
        }
    }

    private SectionPath findCurrentSectionPinnedView(SectionPath sectionPath) {
        SectionPath sp = new SectionPath(sectionPath);
        sp.setSectionType(ViewType.SECTION_HEADER);
        sp.indexPath.setItem(0);
        if (adapter.getSectionItemInSection(sp.getSectionType(), sp.getIndexPath().getSection()) > 0 &&
                adapter.isSectionHeaderPinned(sp.getIndexPath())) {
            return sp;
        }
        return null;
    }

    private void recyclerPinnedView() {
        recyclerCollection.addScrapView(pinnedView);
        pinnedView = null;
    }

    private boolean isPinnedViewTouched(float x, float y) {
        if (pinnedView != null) {
            Rect rect = new Rect();
            pinnedView.getHitRect(rect);

            rect.top += translateY;
            rect.bottom += translateY + getPaddingTop();
            rect.left += getPaddingLeft();
            rect.right += -getPaddingRight();
            return rect.contains((int) x, (int) y);
        }
        return false;
    }

    private void performPinnedViewClick() {
        if (pinnedView == null) {
            return;
        }
        pinnedView.performClick();
    }

    private void clearTouchTarget() {
        touchTarget = null;
        touchPoint = null;
        if (downEvent != null) {
            downEvent.recycle();
            downEvent = null;
        }
    }

    /************************************************************************************************
     * RecyclerCollectionView.LayoutParams
     ************************************************************************************************/
    public static class LayoutParams extends ViewGroup.LayoutParams {

        /*******************************************************************************************
         * Contains sectionType, indexPath(section index && item index)
         *******************************************************************************************/
        private SectionPath sectionPath = null;

        /*******************************************************************************************
         * Indicator which type of view in scrap can be reuse
         *******************************************************************************************/
        private int viewType = 0;

        /*******************************************************************************************
         * Only sectionItem's column may be >= 1, others = 1
         *******************************************************************************************/
        private int column = 1;

        public LayoutParams(ViewGroup.LayoutParams lp) {
            super(lp);
        }

        public LayoutParams(int w, int h, int viewType) {
            super(w, h);
            this.viewType = viewType;
        }

        public SectionPath getSectionPath() {
            return sectionPath;
        }

        public void setSectionPath(SectionPath sectionPath) {
            this.sectionPath = sectionPath;
        }

        public int getViewType() {
            return viewType;
        }

        public void setViewType(int viewType) {
            this.viewType = viewType;
        }

        public int getColumn() {
            return column;
        }

        public void setColumn(int column) {
            this.column = column;
        }

        public int getSectionType() {
            return sectionPath == null ? ViewType.NONE : sectionPath.sectionType;
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerCollectionView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewType.NONE);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof RecyclerCollectionView.LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new RecyclerCollectionView.LayoutParams(p);
    }

    private LayoutParams getSectionLayoutParam(ViewGroup.LayoutParams lp) {
        LayoutParams params = null;
        if (lp == null) {
            params = (LayoutParams) generateDefaultLayoutParams();
        } else if (!checkLayoutParams(lp)) {
            params = (LayoutParams) generateLayoutParams(lp);
        } else {
            params = (LayoutParams) lp;
        }
        return params;
    }

    private void setSectionItemLayoutParam(View child, SectionPath sectionPath) {
        final ViewGroup.LayoutParams lp = child.getLayoutParams();
        LayoutParams params = getSectionLayoutParam(lp);

        /*******************************************************************************************
         * Deep clone, prevent modification because of memory reference
         *******************************************************************************************/
        params.setSectionPath(new SectionPath(sectionPath));
        params.setViewType(adapter.getViewTypeBySectionType(sectionPath.sectionType, sectionPath.indexPath));
        if (sectionPath.sectionType == ViewType.SECTION_ITEM) {
            params.setColumn(adapter.getSectionItemColumn(sectionPath.indexPath.section));
        } else if (sectionPath.sectionType >= ViewType.VIEW_HEADER_REFRESH) {
            if (sectionPath.sectionType == ViewType.VIEW_HEADER_REFRESH && adapter.getRefreshHeader() != null
                    && adapter.getRefreshHeader().getStatus() == RefreshView.REFRESH_STATUS_NONE) {
                params.height = 0;
            } else if (sectionPath.sectionType == ViewType.VIEW_FOOTER_REFRESH && adapter.getRefreshFooter() != null
                    && adapter.getRefreshFooter().getStatus() == RefreshView.REFRESH_STATUS_NONE) {
                params.height = 0;
            }
            if (params.height <= 0) {
                params.height = 0;
            }
        }
        if (lp != params) {
            child.setLayoutParams(params);
        }
    }
    /************************************************************************************************
     * End
     ************************************************************************************************/
}