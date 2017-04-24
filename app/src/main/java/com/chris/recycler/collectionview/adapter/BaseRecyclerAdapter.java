package com.chris.recycler.collectionview.adapter;

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
public abstract class BaseRecyclerAdapter {

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
    public final int getCount() {
        int total = 0;
        for (int i = 0; i < getSections(); i++) {
            for (int sectionType = ViewType.SECTION_HEADER; sectionType <= ViewType.SECTION_FOOTER; sectionType++) {
                total += getSectionItemInSection(sectionType, i);
            }
        }
        return total;
    }

    public final int getPosition(SectionPath sectionPath) {
        int position = 0;
        for (int i = 0; i < sectionPath.getIndexPath().getSection(); i++) {
            for (int sectionType = ViewType.SECTION_HEADER; sectionType <= ViewType.SECTION_FOOTER; sectionType++) {
                position += getSectionItemInSection(sectionType, i);
            }
        }

        position += sectionPath.getIndexPath().getItem();
        for (int sectionType = sectionPath.getSectionType() - 1; sectionType >= ViewType.SECTION_HEADER; sectionType--) {
            position += getSectionItemInSection(sectionType, sectionPath.getIndexPath().getSection());
        }
        return position;
    }

    /************************************************************************************************
     * Number of view types
     ************************************************************************************************/
    public int getSectionHeaderTypeCount() {
        return 1;
    }

    public int getSectionFooterTypeCount() {
        return 1;
    }

    public int getSectionItemTypeCount() {
        return 1;
    }

    /************************************************************************************************
     * Section, each section has lots of items(Header, Item, Footer)
     ************************************************************************************************/
    public int getSections() {
        return 1;
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
        }
        return count;
    }

    public abstract int getHeaderOrFooterInSection(int sectionType, int section);

    public abstract int getItemsInSection(int section);

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
        }
        return viewType;
    }

    /************************************************************************************************
     * Section View
     ************************************************************************************************/
    public final View getSectionView(SectionPath sectionPath, View convertView, ViewGroup parent) {
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

    public abstract View getSectionItemView(IndexPath indexPath, View itemView, ViewGroup parent);
}
