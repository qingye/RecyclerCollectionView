package com.chris.recycler.collectionview.assistant.adapter.base;

import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;

import com.chris.recycler.collectionview.constants.ViewType;
import com.chris.recycler.collectionview.structure.IndexPath;
import com.chris.recycler.collectionview.structure.SectionPath;

/**
 * Created by chris on 16/9/2.
 */
public abstract class BaseRecyclerCollectionAdapter {

    private final DataSetObservable observable = new DataSetObservable();

    public final void registerDataSetObserver(DataSetObserver observer) {
        observable.registerObserver(observer);
    }

    public final void unregisterDataSetObserver(DataSetObserver observer) {
        observable.unregisterObserver(observer);
    }

    public final void notifyDataSetChanged() {
        observable.notifyChanged();
    }

    /************************************************************************************************
     * Total counts
     ************************************************************************************************/
    public int getCount() {
        int total = 0;
        for (int i = 0; i < getSections(); i++) {
            for (int type = ViewType.SECTION_HEADER; type <= ViewType.SECTION_FOOTER; type++) {
                total += getSectionItemInSection(type, i);
            }
        }
        return total;
    }

    public int getPosition(SectionPath sp) {
        int position = -1;
        if (sp != null && sp.indexPath != null && sp.indexPath.section < getSections()) {
            position = 0;
            for (int i = 0; i < sp.indexPath.section; i++) {
                for (int type = ViewType.SECTION_HEADER; type <= ViewType.SECTION_FOOTER; type++) {
                    position += getSectionItemInSection(type, i);
                }
            }

            int count = getSectionItemInSection(sp.sectionType, sp.indexPath.section);
            position += sp.getIndexPath().getItem() < count ? sp.getIndexPath().getItem() : count;
            for (int type = sp.getSectionType() - 1; type >= ViewType.SECTION_HEADER; type--) {
                position += getSectionItemInSection(type, sp.getIndexPath().getSection());
            }
        }
        return position;
    }

    public SectionPath getSectionPath(int position) {
        for (int i = 0; i < getSections(); i++) {
            for (int type = ViewType.SECTION_HEADER; type <= ViewType.SECTION_FOOTER; type++) {
                int count = getSectionItemInSection(type, i);
                if (position >= count) {
                    position -= count;
                } else {
                    SectionPath sectionPath = new SectionPath();
                    sectionPath.setSectionType(type);
                    sectionPath.setIndexPath(new IndexPath(i, position));
                    return sectionPath;
                }
            }
        }
        return null;
    }

    /************************************************************************************************
     * Section, each section has lots of items(Header, Item, Footer)
     ************************************************************************************************/
    public int getSections() {
        return 0;
    }

    /************************************************************************************************
     * Column only apply for SectionItem, not for SectionHeader or SectionFooter
     ************************************************************************************************/
    public int getSectionItemColumn(int section) {
        return 1;
    }

    /************************************************************************************************
     * Header, Footer, Items in the section
     ************************************************************************************************/
    public final int getSectionItemInSection(int sectionType, int section) {
        int count = 0;
        switch (sectionType) {
            case ViewType.SECTION_HEADER:
                count = getHeaderOrFooterInSection(sectionType, section);
                break;

            case ViewType.SECTION_ITEM:
                count = getItemsInSection(section);
                break;

            case ViewType.SECTION_FOOTER:
                count = getHeaderOrFooterInSection(sectionType, section);
                break;

            case ViewType.VIEW_HEADER_REFRESH:
            case ViewType.VIEW_FOOTER_REFRESH:
                count = getRefreshInSection(sectionType, section);
                break;
        }
        return count;
    }

    public int getHeaderOrFooterInSection(int sectionType, int section) {
        return 0;
    }

    public int getItemsInSection(int section) {
        return 0;
    }

    public int getRefreshInSection(int sectionType, int section) {
        return 0;
    }

    /************************************************************************************************
     * View type for reuse
     ************************************************************************************************/
    public int getViewTypeBySectionType(int sectionType, IndexPath indexPath) {
        int viewType = 0;
        switch (sectionType) {
            case ViewType.SECTION_HEADER:
                viewType = 0;
                break;

            case ViewType.SECTION_ITEM:
                viewType = 1;
                break;

            case ViewType.SECTION_FOOTER:
                viewType = 0;
                break;

            case ViewType.VIEW_HEADER_REFRESH:
            case ViewType.VIEW_FOOTER_REFRESH:
            default:
                break;
        }
        return viewType;
    }

    /************************************************************************************************
     * Section View
     ************************************************************************************************/
    public View getSectionView(SectionPath sectionPath, View convertView, ViewGroup parent) {
        View view = null;
        switch (sectionPath.sectionType) {
            case ViewType.SECTION_HEADER:
                view = getSectionHeaderView(sectionPath.getIndexPath(), convertView, parent);
                break;

            case ViewType.SECTION_ITEM:
                view = getSectionItemView(sectionPath.getIndexPath(), convertView, parent);
                break;

            case ViewType.SECTION_FOOTER:
                view = getSectionFooterView(sectionPath.getIndexPath(), convertView, parent);
                break;
        }
        return view;
    }

    public View getSectionHeaderView(IndexPath indexPath, View headerView, ViewGroup parent) {
        return null;
    }

    public View getSectionFooterView(IndexPath indexPath, View footerView, ViewGroup parent) {
        return null;
    }

    public View getSectionSwapView(IndexPath indexPath, View swapView, ViewGroup parent) {
        return null;
    }

    public abstract View getSectionItemView(IndexPath indexPath, View itemView, ViewGroup parent);

    /************************************************************************************************
     * 1. Only SectionHeader has Pinned Option
     * 2. Continuous Sections can have a PinnedView depend on associateSectionHeaderPinned
     * 2.1 Only non-refresh section can has an associate with other PinnedView;
     * 2.2 Only sections after PinnedView Of the section can has an assocation;
     *
     * e.g.
     * 2.3.1 Section 4 has a PinnedView, then Section [4, N-1] (N-1 != refresh) can have an association;
     * 2.3.2 Section 4, 10 has a PinnedView, then Section [4, 9] can have associate with Section 4,
     * after can not;
     ************************************************************************************************/
    public boolean isSectionHeaderPinned(IndexPath indexPath) {
        return false;
    }

    public boolean associateSectionHeaderPinned(IndexPath indexPath) {
        return false;
    }
}
