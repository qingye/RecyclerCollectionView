package com.chris.recycler.collectionview.assistant.adapter.wrapper;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.chris.recycler.collectionview.assistant.adapter.base.BaseRecyclerCollectionAdapter;
import com.chris.recycler.collectionview.assistant.refresh.RefreshView;
import com.chris.recycler.collectionview.constants.ViewType;
import com.chris.recycler.collectionview.structure.IndexPath;
import com.chris.recycler.collectionview.structure.SectionPath;

/**
 * Created by chris on 17/4/25.
 */
public final class WrapperRecyclerCollectionAdapter extends BaseRecyclerCollectionAdapter {

    private BaseRecyclerCollectionAdapter innerAdapter = null;
    private RefreshView refreshHeader = null;
    private RefreshView refreshFooter = null;
    private boolean swap = false;

    public WrapperRecyclerCollectionAdapter(Context context, BaseRecyclerCollectionAdapter adapter) {
        this.innerAdapter = adapter;
    }

    public BaseRecyclerCollectionAdapter getAdapter() {
        return innerAdapter;
    }

    /************************************************************************************************
     * Set & Get RefreshView
     ************************************************************************************************/
    public void setRefreshHeader(RefreshView refreshHeader) {
        this.refreshHeader = refreshHeader;
    }

    public RefreshView getRefreshHeader() {
        return refreshHeader;
    }

    public void setRefreshFooter(RefreshView refreshFooter) {
        this.refreshFooter = refreshFooter;
    }

    public RefreshView getRefreshFooter() {
        if (innerAdapter == null || innerAdapter.getCount() == 0) {
            return null;
        }
        return refreshFooter;
    }

    /************************************************************************************************
     * Only SectionItem && column = 1 can has a swap view
     ************************************************************************************************/
    public void setSwap(boolean swap) {
        this.swap = swap;
    }

    public boolean isSwap() {
        return swap;
    }

    /************************************************************************************************
     * Get InnerAdapter's real section, index path or section path
     ************************************************************************************************/
    private int getInnerSection(int section) {
        return refreshHeader != null ? section - 1 : section;
    }

    private IndexPath getInnerIndexPath(IndexPath indexPath) {
        IndexPath ip = new IndexPath(indexPath);
        if (refreshHeader != null) {
            ip.section--;
        }
        return ip;
    }

    private SectionPath getInnerSectionPath(SectionPath sectionPath) {
        SectionPath sp = new SectionPath(sectionPath);
        if (refreshHeader != null) {
            sp.indexPath.section--;
        }
        return sp;
    }

    /************************************************************************************************
     * Override Base Methods
     ************************************************************************************************/
    @Override
    public int getCount() {
        int count = innerAdapter == null ? 0 : innerAdapter.getCount();
        if (refreshHeader != null) {
            count++;
        }
        if (getRefreshFooter() != null) {
            count++;
        }
        return count;
    }

    @Override
    public int getPosition(SectionPath sectionPath) {
        int position = -1;
        if (refreshHeader == null && getRefreshFooter() == null && innerAdapter != null) {
            position = innerAdapter.getPosition(sectionPath);
        } else {
            if (refreshHeader != null && sectionPath.indexPath.section == 0) {
                position = 0;
            } else if (getRefreshFooter() != null && sectionPath.indexPath.section == getSections() - 1) {
                position = getCount() - 1;
            } else if (innerAdapter != null) {
                position = innerAdapter.getPosition(getInnerSectionPath(sectionPath)) + 1;
            }
        }
        return position;
    }

    @Override
    public SectionPath getSectionPath(int position) {
        SectionPath sectionPath = null;
        if (refreshHeader == null && getRefreshFooter() == null && innerAdapter != null) {
            sectionPath = innerAdapter.getSectionPath(position);
        } else {
            if (refreshHeader != null && position == 0) {
                sectionPath = new SectionPath(ViewType.VIEW_HEADER_REFRESH, new IndexPath(0, 0));
            } else if (getRefreshFooter() != null && position + 1 == getCount()) {
                sectionPath = new SectionPath(ViewType.VIEW_FOOTER_REFRESH, new IndexPath(getSections() - 1, 0));
            } else if (innerAdapter != null) {
                position = position >= getCount() ? innerAdapter.getCount() : position;
                sectionPath = innerAdapter.getSectionPath(position - 1);
                sectionPath.indexPath.section ++; // has RefreshHeader, so section ++
            }
        }
        return sectionPath;
    }

    @Override
    public int getSections() {
        int sections = innerAdapter == null ? 0 : innerAdapter.getSections();
        if (refreshHeader != null) {
            sections++;
        }
        if (getRefreshFooter() != null) {
            sections++;
        }
        return sections;
    }

    @Override
    public int getSectionItemColumn(int section) {
        int column = 0;
        if (refreshHeader != null && section == 0) {
            column = 1;
        } else if (getRefreshFooter() != null && section == getSections() - 1) {
            column = 1;
        } else if (innerAdapter != null) {
            column = innerAdapter.getSectionItemColumn(getInnerSection(section));
        }
        return column;
    }

    @Override
    public int getHeaderOrFooterInSection(int sectionType, int section) {
        int count = 0;
        if (refreshHeader != null && section == 0) {
            count = 0;
        } else if (getRefreshFooter() != null && section == getSections() - 1) {
            count = 0;
        } else if (innerAdapter != null) {
            count = innerAdapter.getHeaderOrFooterInSection(sectionType, getInnerSection(section));
        }
        return count;
    }

    @Override
    public int getItemsInSection(int section) {
        int count = 0;
        if (refreshHeader != null && section == 0) {
            count = 0;
        } else if (getRefreshFooter() != null && section == getSections() - 1) {
            count = 0;
        } else if (innerAdapter != null) {
            count = innerAdapter.getItemsInSection(getInnerSection(section));
        }
        return count;
    }

    @Override
    public int getRefreshInSection(int sectionType, int section) {
        int count = 0;
        if (refreshHeader != null && section == 0 && sectionType == ViewType.VIEW_HEADER_REFRESH) {
            count = 1;
        } else if (getRefreshFooter() != null && section == getSections() - 1 && sectionType == ViewType.VIEW_FOOTER_REFRESH) {
            count = 1;
        }
        return count;
    }

    @Override
    public int getViewTypeBySectionType(int sectionType, IndexPath indexPath) {
        int viewType = 0;
        if ((refreshHeader != null && indexPath.section == 0) ||
                (getRefreshFooter() != null && indexPath.section == getSections() - 1)) {
            viewType = 1;
        } else if (innerAdapter != null) {
            viewType = innerAdapter.getViewTypeBySectionType(sectionType, getInnerIndexPath(indexPath));
        }
        return viewType;
    }

    @Override
    public View getSectionView(SectionPath sectionPath, View convertView, ViewGroup parent) {
        View view = null;
        if ((refreshHeader != null && sectionPath.indexPath.section == 0) ||
                (getRefreshFooter() != null && sectionPath.indexPath.section == getSections() - 1)) {
            if (sectionPath.sectionType >= ViewType.VIEW_HEADER_REFRESH) {
                view = getSectionItemView(sectionPath.getIndexPath(), convertView, parent);
            }
        } else if (innerAdapter != null) {
            view = innerAdapter.getSectionView(getInnerSectionPath(sectionPath), convertView, parent);
        }
        return view;
    }

    @Override
    public View getSectionSwapView(IndexPath indexPath, View swapView, ViewGroup parent) {
        View view = null;
        if (innerAdapter != null) {
            view = innerAdapter.getSectionSwapView(getInnerIndexPath(indexPath), swapView, parent);
        }
        return view;
    }

    @Override
    public View getSectionItemView(IndexPath indexPath, View itemView, ViewGroup parent) {
        RefreshView refreshView = indexPath.getSection() == 0 ? refreshHeader : refreshFooter;
        return refreshView.getRefreshView(refreshView.getStatus(), itemView);
    }

    /************************************************************************************************
     * Only SectionHeader has Pinned Option
     ************************************************************************************************/
    @Override
    public boolean isSectionHeaderPinned(IndexPath indexPath) {
        boolean ret = false;
        if (refreshHeader != null && indexPath.getSection() > 0 ||
                getRefreshFooter() != null && indexPath.getSection() < getSections() - 1) {
            ret = innerAdapter.isSectionHeaderPinned(getInnerIndexPath(indexPath));
        }
        return ret;
    }

    @Override
    public boolean associateSectionHeaderPinned(IndexPath indexPath) {
        boolean associate = false;
        if (refreshHeader != null && indexPath.section == 0 ||
                getRefreshFooter() != null && indexPath.section == getSections() - 1) {
            associate = false;
        } else if (innerAdapter != null){
            associate = innerAdapter.associateSectionHeaderPinned(getInnerIndexPath(indexPath));
        }
        return associate;
    }
}
