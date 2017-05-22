package com.chris.recycler.collectionview.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chris.recycler.collectionview.R;
import com.chris.recycler.collectionview.assistant.adapter.base.BaseRecyclerCollectionAdapter;
import com.chris.recycler.collectionview.constants.ViewType;
import com.chris.recycler.collectionview.structure.IndexPath;

/**
 * Created by chris on 16/9/1.
 */
public class RecyclerCollectionAdapter extends BaseRecyclerCollectionAdapter {

    private Context context = null;

    public RecyclerCollectionAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getSections() {
        return 30;
    }

    @Override
    public int getHeaderOrFooterInSection(int sectionType, int section) {
        int count = 0;
        switch (sectionType) {
            case ViewType.SECTION_HEADER:
                if (section == 2 || section == 6 || section == 3) {
                    count = 1;
                }
                break;

            case ViewType.SECTION_FOOTER:
                if (section > 1) {
                    count = 3;
                } else {
                    count = 2;
                }
                break;
        }
        return count;
    }

    @Override
    public int getItemsInSection(int section) {
        return 8;
    }

    @Override
    public int getSectionItemColumn(int section) {
        int column = 0;
        switch (section) {
            case 0:
                column = 1;
                break;

            case 1:
                column = 2;
                break;

            case 2:
                column = 3;
                break;

            case 3:
                column = 4;
                break;

            default:
                column = 5;
                break;
        }
        return column;
    }

    @Override
    public int getViewTypeBySectionType(int sectionType, IndexPath indexPath) {
        int viewType = 0;
        switch (sectionType) {
            case ViewType.SECTION_HEADER:
                viewType = 1;
                break;

            case ViewType.SECTION_ITEM:
//                viewType = 1;
                switch (indexPath.section) {
                    case 0:
                        viewType = 1;
                        break;

                    case 1:
                        viewType = 2;
                        break;

                    case 2:
                        viewType = 3;
                        break;

                    case 3:
                        viewType = 4;
                        break;

                    default:
                        viewType = 5;
                        break;
                }
                break;

            case ViewType.SECTION_FOOTER:
                viewType = 1;
                break;
        }
        return viewType;
    }

    @Override
    public boolean isSectionHeaderPinned(IndexPath indexPath) {
        boolean ret = false;
        if (indexPath.getItem() == 0) {
            ret = true;
        }
        return ret;
    }

    @Override
    public boolean associateSectionHeaderPinned(IndexPath indexPath) {
        return true;
    }

    @Override
    public View getSectionHeaderView(IndexPath indexPath, View headerView, ViewGroup parent) {
        ViewHolder holder = null;
        if (headerView == null) {
            headerView = LayoutInflater.from(context).inflate(R.layout.adapter_recycler_section_header, null);
            holder = new ViewHolder();
            holder.textView = (TextView) headerView.findViewById(R.id.textView);
            headerView.setTag(holder);
        } else {
            holder = (ViewHolder) headerView.getTag();
        }

        holder.textView.setText(String.format("SectionHeader(%d-%d)", indexPath.section, indexPath.item));
        holder.textView.setTextColor(0xff7777ff);
        headerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "header click", Toast.LENGTH_SHORT).show();
            }
        });

        return headerView;
    }

    @Override
    public View getSectionFooterView(IndexPath indexPath, View footerView, ViewGroup parent) {
        ViewHolder holder = null;
        if (footerView == null) {
            footerView = LayoutInflater.from(context).inflate(R.layout.adapter_recycler_section_header, null);
            holder = new ViewHolder();
            holder.textView = (TextView) footerView.findViewById(R.id.textView);
            footerView.setTag(holder);
        } else {
            holder = (ViewHolder) footerView.getTag();
        }

        holder.textView.setText(String.format("SectionFooter(%d-%d)", indexPath.section, indexPath.item));
        holder.textView.setTextColor(0xffff7777);
        return footerView;
    }

    @Override
    public View getSectionItemView(IndexPath indexPath, View itemView, ViewGroup parent) {
        ViewHolder holder = null;
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.adapter_recycler_item, null);
            holder = new ViewHolder();
            holder.textView = (TextView) itemView.findViewById(R.id.textView);
            itemView.setTag(holder);
        } else {
            holder = (ViewHolder) itemView.getTag();
        }

        holder.textView.setText(String.format("(%d-%d)", indexPath.section, indexPath.item));
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) holder.textView.getLayoutParams();
        switch (indexPath.item % 3) {
            case 0:
                lp.height = 160;
                break;

            case 1:
                lp.height = 30;
                break;

            case 2:
                lp.height = 100;
                break;
        }
        lp.height += 155;
        holder.textView.setLayoutParams(lp);

        itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                Log.e("TAG", "[RecyclerCollectionAdapter] onTouch true");
                return true;
            }
        });
        return itemView;
    }

    class ViewHolder {
        public TextView textView = null;
    }
}
