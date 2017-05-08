package com.chris.recycler.collectionview.adapter.refresh;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by chris on 17/5/5.
 */
public class RefreshHeaderView extends RefreshView {

    private Context context = null;

    public RefreshHeaderView(Context context) {
        this.context = context;
    }

    @Override
    public int getMaxDistance(){
        return 200;
    }

    @Override
    public View getRefreshView(int status, View itemView) {
        ViewHolder holder = null;
        if (itemView == null) {
            itemView = createRefreshView();
            holder = new ViewHolder();
            holder.textView = (TextView) itemView.findViewWithTag("textView");
            itemView.setTag(holder);
        } else {
            holder = (ViewHolder) itemView.getTag();
        }

        switch (status) {
            case REFRESH_STATUS_NONE:
                holder.textView.setText("下拉即可刷新");
                break;

            case REFRESH_STATUS_PULL_UNEXCEED:
                holder.textView.setText("下拉即可刷新");
                break;

            case REFRESH_STATUS_PULL_EXCEED:
                holder.textView.setText("释放即可刷新");
                break;

            case REFRESH_STATUS_REFRESHING:
                holder.textView.setText("刷新中...");
                break;
        }

        return itemView;
    }

    private RelativeLayout createRefreshView() {
        RelativeLayout layout = new RelativeLayout(context);
        TextView tv = new TextView(context);
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.addRule(RelativeLayout.CENTER_HORIZONTAL);
        p.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        p.setMargins(0, 0, 0, 10);
        tv.setLayoutParams(p);
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(16);
        tv.setTag("textView");
        layout.addView(tv);
        return layout;
    }

    class ViewHolder {
        public TextView textView = null;
    }
}
