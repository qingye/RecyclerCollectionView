package com.chris.recycler.collectionview.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chris.recycler.collectionview.R;
import com.chris.recycler.collectionview.adapter.base.BaseRecyclerCollectionAdapter;
import com.chris.recycler.collectionview.constants.ViewType;
import com.chris.recycler.collectionview.structure.IndexPath;
import com.chris.recycler.collectionview.structure.SectionPath;

/**
 * Created by chris on 17/4/25.
 */
public final class WrapperRecyclerCollectionAdapter extends BaseRecyclerCollectionAdapter {

    private Context context = null;
    private BaseRecyclerCollectionAdapter innerAdapter = null;
    private View refreshHeader = null;
    private View refreshFooter = null;

    public WrapperRecyclerCollectionAdapter(Context context, BaseRecyclerCollectionAdapter adapter) {
        this.context = context;
        this.innerAdapter = adapter;

        refreshHeader = new View(context);
        refreshFooter = new View(context);
    }

    public void setRefreshHeader(View refreshHeader) {
        this.refreshHeader = refreshHeader;
    }

    public View getRefreshHeader() {
        return refreshHeader;
    }

    public void setRefreshFooter(View refreshFooter) {
        this.refreshFooter = refreshFooter;
    }

    public View getRefreshFooter() {
        return refreshFooter;
    }

    public BaseRecyclerCollectionAdapter getAdapter() {
        return innerAdapter;
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
        if (refreshFooter != null) {
            count++;
        }
        return count;
    }

    @Override
    public int getPosition(SectionPath sectionPath) {
        int position = 0;
        if (refreshHeader == null && refreshFooter == null && innerAdapter != null) {
            position = innerAdapter.getPosition(sectionPath);
        } else {
            if (refreshHeader != null && sectionPath.indexPath.section == 0) {
                position = 0;
            } else if (refreshFooter != null && sectionPath.indexPath.section == getSections() - 1) {
                position = getCount() - 1;
            } else if (innerAdapter != null) {
                position = innerAdapter.getPosition(getInnerSectionPath(sectionPath)) + 1;
            }
        }
        return position;
    }

    @Override
    public int getSections() {
        int sections = innerAdapter == null ? 0 : innerAdapter.getSections();
        if (refreshHeader != null) {
            sections++;
        }
        if (refreshFooter != null) {
            sections++;
        }
        return sections;
    }

    @Override
    public int getSectionItemColumn(int section) {
        int column = 0;
        if (refreshHeader != null && section == 0) {
            column = 1;
        } else if (refreshFooter != null && section == getSections() - 1) {
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
        } else if (refreshFooter != null && section == getSections() - 1) {
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
        } else if (refreshFooter != null && section == getSections() - 1) {
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
        } else if (refreshFooter != null && section == getSections() - 1 && sectionType == ViewType.VIEW_FOOTER_REFRESH) {
            count = 1;
        }
        return count;
    }

    @Override
    public int getViewTypeBySectionType(int sectionType, IndexPath indexPath) {
        int viewType = 0;
        if ((refreshHeader != null && indexPath.section == 0) ||
                (refreshFooter != null && indexPath.section == getSections() - 1)) {
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
                (refreshFooter != null && sectionPath.indexPath.section == getSections() - 1)) {
            if (sectionPath.sectionType >= ViewType.VIEW_HEADER_REFRESH) {
                view = getSectionItemView(sectionPath.getIndexPath(), convertView, parent);
            }
        } else if (innerAdapter != null) {
            view = innerAdapter.getSectionView(getInnerSectionPath(sectionPath), convertView, parent);
        }
        return view;
    }

    @Override
    public View getSectionItemView(IndexPath indexPath, View itemView, ViewGroup parent) {
        ViewHolder holder = null;
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.adapter_recycler_item, null);
            itemView.setBackgroundColor(Color.YELLOW);
            holder = new ViewHolder();
            holder.textView = (TextView) itemView.findViewById(R.id.textView);
            itemView.setTag(holder);
        } else {
            holder = (ViewHolder) itemView.getTag();
        }

        holder.textView.setText(String.format("Refresh=>(%d-%d)", indexPath.section, indexPath.item));
        return itemView;
    }

    class ViewHolder {
        public TextView textView = null;
    }
}
