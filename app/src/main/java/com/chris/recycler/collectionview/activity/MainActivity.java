package com.chris.recycler.collectionview.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.chris.recycler.collectionview.Log;
import com.chris.recycler.collectionview.R;
import com.chris.recycler.collectionview.RecyclerCollectionView;
import com.chris.recycler.collectionview.assistant.refresh.RefreshFooterView;
import com.chris.recycler.collectionview.assistant.refresh.RefreshHeaderView;
import com.chris.recycler.collectionview.assistant.refresh.RefreshView;

public class MainActivity extends AppCompatActivity {

    private RecyclerCollectionView recyclerCollectionView = null;
    private RecyclerCollectionAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        adapter = new RecyclerCollectionAdapter(this);
        recyclerCollectionView = (RecyclerCollectionView) findViewById(R.id.recyclerCollectionView);
        recyclerCollectionView.setAdapter(adapter)
                .setRefreshHeader(new RefreshHeaderView(this).setOnRefreshListener(new RefreshView.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.e("header on refresh");
                        recyclerCollectionView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                recyclerCollectionView.onComplete();
                            }
                        }, 30000);
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
                        }, 30000);
                    }
                }));
    }
}
