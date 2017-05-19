package com.chris.recycler.collectionview.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.chris.recycler.collectionview.Log;
import com.chris.recycler.collectionview.R;
import com.chris.recycler.collectionview.RecyclerCollectionView;
import com.chris.recycler.collectionview.assistant.refresh.RefreshFooterView;
import com.chris.recycler.collectionview.assistant.refresh.RefreshHeaderView;
import com.chris.recycler.collectionview.assistant.refresh.RefreshView;
import com.chris.recycler.collectionview.assistant.scroll.OnScrollListener;
import com.chris.recycler.collectionview.structure.IndexPath;
import com.chris.recycler.collectionview.structure.SectionPath;

public class MainActivity extends AppCompatActivity {

    private RecyclerCollectionView recyclerCollectionView = null;
    private TextView rightBtn = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        initTitleBar();
        initRecyclerCollectionView();
    }

    private void initTitleBar() {
        rightBtn = (TextView) findViewById(R.id.rightBtn);
        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int sectionType = (int) (Math.random() * 4 + 1);
                int section = (int) (Math.random() * 30);
                int item = (int) (Math.random() * 7);

                SectionPath sp = new SectionPath(sectionType, new IndexPath(section, item));
                recyclerCollectionView.smoothToSectionPath(sp);
            }
        });
    }

    private void initRecyclerCollectionView() {
        recyclerCollectionView = (RecyclerCollectionView) findViewById(R.id.recyclerCollectionView);
        recyclerCollectionView.setAdapter(new RecyclerCollectionAdapter(this))
                .setRefreshHeader(new RefreshHeaderView(this).setOnRefreshListener(new RefreshView.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.e("header on refresh");
                        recyclerCollectionView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                recyclerCollectionView.onComplete();
                            }
                        }, 5000);
                    }
                }))
                .setRefreshFooter(new RefreshFooterView(this).setOnRefreshListener(new RefreshView.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.e("footer on refresh");
                        recyclerCollectionView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                recyclerCollectionView.onComplete();
                            }
                        }, 5000);
                    }
                }));
        recyclerCollectionView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerCollectionView view, int scrollState) {
            }

            @Override
            public void onScroll(RecyclerCollectionView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
    }
}
