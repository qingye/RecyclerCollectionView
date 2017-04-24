package com.chris.recycler.collectionview;

import android.view.View;

import com.chris.recycler.collectionview.constants.ViewType;
import com.chris.recycler.collectionview.structure.SectionPath;

import java.util.ArrayList;

/**
 * Created by chris on 16/9/2.
 */
public class RecyclerCollection {

    private RecyclerCollectionView parentView = null;
    private int firstVisibleViewPosition = 0;
    private View[] visibleViews = null;

    /***********************************************************************************************
     * Section type count and scarp:
     * [0]: section header
     * [1]: section item
     * [2]: section footer
     ***********************************************************************************************/
    private int[] sectionTypeCount = null;
    private ArrayList<View>[][] scrapSections = null;

    public RecyclerCollection(RecyclerCollectionView parentView) {
        this.parentView = parentView;
    }

    /***********************************************************************************************
     * Initialize the RecyclerCollection by ViewTypeCount
     ***********************************************************************************************/
    public void setViewTypeCount(int sectionHeaderTypeCount, int sectionFooterTypeCount, int sectionItemTypeCount) {
        if (sectionItemTypeCount < 1) {
            throw new IllegalArgumentException("section item view type count should > 0");
        }

        sectionTypeCount = new int[]{
                sectionHeaderTypeCount,
                sectionItemTypeCount,
                sectionFooterTypeCount
        };
        scrapSections = new ArrayList[sectionTypeCount.length][];
        for (int i = 0; i < sectionTypeCount.length; i++) {
            if (sectionTypeCount[i] > 0) {
                scrapSections[i] = new ArrayList[sectionTypeCount[i]];
                for (int j = 0; j < sectionTypeCount[i]; j++) {
                    scrapSections[i][j] = new ArrayList<>();
                }
            }
        }
    }

    /***********************************************************************************************
     * Position reletive to the first visible view's position
     ***********************************************************************************************/
    public View getVisibleView(int position) {
        View view = null;
        int index = position - firstVisibleViewPosition;
        if (index >= 0 && index < visibleViews.length) {
            view = visibleViews[index];
            visibleViews[index] = null;
        }
        return view;
    }

    /***********************************************************************************************
     * Recycler View to the scrap
     ***********************************************************************************************/
    public void addScrapView(View scrap) {
        RecyclerCollectionView.LayoutParams lp = (RecyclerCollectionView.LayoutParams) scrap.getLayoutParams();
        if (lp == null) {
            return;
        }

        int sectionType = lp.getSectionType();
        if (sectionType >= ViewType.VIEW_HEADER_REFRESH) {
            return;
        }

        scrap.onStartTemporaryDetach();

        sectionType -= ViewType.SECTION_HEADER;
        int viewType = lp.getViewType();
        if (scrapSections[sectionType] != null) {
            scrapSections[sectionType][viewType - 1].add(scrap);
        }
    }

    /***********************************************************************************************
     * Retrive the view from scrap list if has
     ***********************************************************************************************/
    public View getScrapView(SectionPath sectionPath) {
        View view = null;
        int sectionType = sectionPath.sectionType;
        int viewType = parentView.getAdapter().getViewTypeBySectionType(sectionType, sectionPath.indexPath);
        sectionType -= ViewType.SECTION_HEADER;
        if (scrapSections[sectionType] != null && viewType > 0) {
            ArrayList<View> scrapView = scrapSections[sectionType][viewType - 1];
            if (scrapView != null && scrapView.size() > 0) {
                view = scrapView.remove(0);
            }
        }
        return view;
    }

    /***********************************************************************************************
     * Store Visible views in the visibleViews
     ***********************************************************************************************/
    public void fillVisibleViews(int childCount, int firstPosition) {
        if (visibleViews == null || visibleViews.length < childCount) {
            visibleViews = new View[childCount];
        }
        firstVisibleViewPosition = firstPosition;

        final View[] activeViews = visibleViews;
        for (int i = 0; i < childCount; i++) {
            View child = parentView.getChildAt(i);
            RecyclerCollectionView.LayoutParams lp = (RecyclerCollectionView.LayoutParams) child.getLayoutParams();
            /***************************************************************************************
             * Don't put header or footer views into the scrap heap
             ***************************************************************************************/
            if (lp != null && (lp.getSectionType() < ViewType.VIEW_HEADER_REFRESH)) {
                /***********************************************************************************
                 * RecyclerCollectionView's header & footer will not scrap
                 * Only section(header, footer, item) can scrap
                 ***********************************************************************************/
                activeViews[i] = child;
            }
        }
    }

    /***********************************************************************************************
     * Make dirty and will layout at next time when be visible
     ***********************************************************************************************/
    public void makeChildrenDirty() {
        for (int i = 0; i < sectionTypeCount.length; i++) {
            if (scrapSections[i] != null) {
                for (int j = 0; j < scrapSections[i].length; j++) {
                    ArrayList<View> scrap = scrapSections[i][j];
                    for (View child : scrap) {
                        child.forceLayout();
                    }
                }
            }
        }
    }

    /***********************************************************************************************
     * Clear the cathe
     ***********************************************************************************************/
    public void clear() {
        for (int i = 0; i < sectionTypeCount.length; i++) {
            if (scrapSections[i] != null) {
                for (int j = 0; j < scrapSections[i].length; j++) {
                    ArrayList<View> scrap = scrapSections[i][j];
                    clearScrap(scrap);
                }
            }
        }
    }

    private void clearScrap(final ArrayList<View> scrap) {
        final int scrapCount = scrap.size();
        if (scrapCount > 0) {
            for (int i = scrapCount - 1; i >= 0; i--) {
                removeDetachedView(scrap.remove(i), false);
            }
        }
    }

    private void removeDetachedView(View child, boolean animate) {
        child.setAccessibilityDelegate(null);
        parentView.removeDetachedView(child, animate);
    }
}
