package com.chris.recycler.collectionview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by chris on 16/9/1.
 */
public class RecyclerAdapter extends BaseRecyclerAdapter {

    private Context context = null;

    public RecyclerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getSections() {
        return 30;
    }

    public int getHeaderOrFooterInSection(int sectionType, int section) {
        int count = 0;
        switch (sectionType) {
            case ViewType.SECTION_HEADER:
                count = 1;
                break;

            case ViewType.SECTION_FOOTER:
                count = 3;
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
                column = 3;
                break;

            default:
                column = 3;
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
                viewType = 1;
                break;

            case ViewType.SECTION_FOOTER:
                viewType = 1;
                break;
        }
        return viewType;
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
//                Log.e("TAG", "[RecyclerAdapter] onTouch true");
                return true;
            }
        });
        return itemView;
    }

    class ViewHolder {
        public TextView textView = null;
    }
}
