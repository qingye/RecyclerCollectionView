package com.chris.recycler.collectionview;

import android.view.View;

import com.chris.recycler.collectionview.constants.ViewType;
import com.chris.recycler.collectionview.structure.SectionPath;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by chris on 16/9/2.
 */
public class RecyclerCollection {

    private RecyclerCollectionView parentView = null;
    private int firstVisibleViewPosition = 0;
    private View[] visibleViews = null;

    /***********************************************************************************************
     * Section type count and scarp [TreeMap]:
     * [1]: section header
     * [2]: section item
     * [3]: section footer
     * [4]: refresh header
     * [5]: refresh footer
     ***********************************************************************************************/
    private TreeMap<Integer, TreeMap<Integer, ArrayList<View>>> mapScrapSections = null;

    public RecyclerCollection(RecyclerCollectionView parentView) {
        this.parentView = parentView;
        initialize();
    }

    /***********************************************************************************************
     * Initialize the RecyclerCollection
     * 1. By Section ViewType;
     * 2. By View's view type;
     ***********************************************************************************************/
    private void initialize() {
        mapScrapSections = new TreeMap<>();
        for (Integer type = ViewType.SECTION_HEADER; type <= ViewType.VIEW_FOOTER_REFRESH; type++) {
            mapScrapSections.put(type, new TreeMap<Integer, ArrayList<View>>());
        }
    }

    /***********************************************************************************************
     * Recycler View to the scrap
     ***********************************************************************************************/
    public void addScrapView(View scrap) {
        if (scrap == null) {
            return;
        }

        RecyclerCollectionView.LayoutParams lp = (RecyclerCollectionView.LayoutParams) scrap.getLayoutParams();
        if (lp == null) {
            return;
        }
        scrap.onStartTemporaryDetach();

        int sectionType = lp.getSectionType();
        int viewType = lp.getViewType();
        if (sectionType > ViewType.NONE && sectionType <= ViewType.VIEW_FOOTER_REFRESH) {
            TreeMap<Integer, ArrayList<View>> map = mapScrapSections.get(sectionType);
            ArrayList<View> list = map.get(viewType);
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(scrap);
            map.put(viewType, list);
            mapScrapSections.put(sectionType, map);
        }
    }

    /***********************************************************************************************
     * Retrive the view from scrap list if has
     ***********************************************************************************************/
    public View getScrapView(SectionPath sectionPath) {
        View view = null;
        int sectionType = sectionPath.getSectionType();
        int viewType = parentView.getAdapter().getViewTypeBySectionType(sectionType, sectionPath.indexPath);
        if (sectionType > ViewType.NONE && sectionType <= ViewType.VIEW_FOOTER_REFRESH) {
            TreeMap<Integer, ArrayList<View>> map = mapScrapSections.get(sectionType);
            ArrayList<View> list = map.get(viewType);
            if (list != null && list.size() > 0) {
                view = list.remove(0);
            }
            map.put(viewType, list);
            mapScrapSections.put(sectionType, map);
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
            if (lp != null) {
                /***********************************************************************************
                 * RecyclerCollectionView's header & footer will not scrap
                 * Only section(header, footer, item) can scrap
                 ***********************************************************************************/
                activeViews[i] = child;
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
     * Make dirty and will layout at next time when be visible
     ***********************************************************************************************/
    public void makeChildrenDirty() {
        for (Integer sectionType : mapScrapSections.keySet()) {
            TreeMap<Integer, ArrayList<View>> map = mapScrapSections.get(sectionType);
            for (Integer viewType : map.keySet()) {
                ArrayList<View> scrap = map.get(viewType);
                if (scrap != null && scrap.size() > 0) {
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
        for (Integer sectionType : mapScrapSections.keySet()) {
            TreeMap<Integer, ArrayList<View>> map = mapScrapSections.get(sectionType);
            for (Integer viewType : map.keySet()) {
                ArrayList<View> scrap = map.get(viewType);
                clearScrap(scrap);
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
