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
    /***********************************************************************************************
     * Section type count and scarp [TreeMap]:
     ***********************************************************************************************/
    private TreeMap<Integer, TreeMap<Integer, ArrayList<View>>> scrapSections = null;

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
        scrapSections = new TreeMap<>();
        for (Integer type = ViewType.SECTION_HEADER; type <= ViewType.SECTION_COMPOSITE; type++) {
            scrapSections.put(type, new TreeMap<Integer, ArrayList<View>>());
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
        if (lp.getSectionPath().subType == ViewType.SECTION_COMPOSITE) {
            sectionType = lp.getSectionPath().subType;
        }
        if (sectionType > ViewType.NONE && sectionType <= ViewType.SECTION_COMPOSITE) {
            TreeMap<Integer, ArrayList<View>> map = scrapSections.get(sectionType);
            ArrayList<View> list = map.get(viewType);
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(scrap);
            map.put(viewType, list);
            scrapSections.put(sectionType, map);
        }
    }

    /***********************************************************************************************
     * Retrive the view from scrap list if has
     ***********************************************************************************************/
    public View getScrapView(SectionPath sectionPath) {
        View view = null;
        int sectionType = sectionPath.getSectionType();
        int viewType = parentView.getAdapter().getViewTypeBySectionType(sectionType, sectionPath.indexPath);
        if (sectionPath.subType == ViewType.SECTION_COMPOSITE) {
            sectionType = sectionPath.subType;
        }
        if (sectionType > ViewType.NONE && sectionType <= ViewType.SECTION_COMPOSITE) {
            TreeMap<Integer, ArrayList<View>> map = scrapSections.get(sectionType);
            ArrayList<View> list = map.get(viewType);
            if (list != null && list.size() > 0) {
                view = list.remove(0);
            }
            if (list != null) {
                map.put(viewType, list);
            }
            scrapSections.put(sectionType, map);
        }

        return view;
    }

    /***********************************************************************************************
     * Scrap all views to scrapSections
     ***********************************************************************************************/
    public void scrapAll() {
        for (int i = 0; i < parentView.getChildCount(); i++) {
            addScrapView(parentView.getChildAt(i));
        }
    }

    /***********************************************************************************************
     * Make dirty and will layout at next time when be visible
     ***********************************************************************************************/
    public void makeChildrenDirty() {
        for (Integer sectionType : scrapSections.keySet()) {
            TreeMap<Integer, ArrayList<View>> map = scrapSections.get(sectionType);
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
        for (Integer sectionType : scrapSections.keySet()) {
            TreeMap<Integer, ArrayList<View>> map = scrapSections.get(sectionType);
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
