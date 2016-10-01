package com.chris.recycler.collectionview.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.chris.recycler.collectionview.R;
import com.chris.recycler.collectionview.RecyclerAdapter;
import com.chris.recycler.collectionview.RecyclerCollectionView;

public class MainActivity extends AppCompatActivity {

    private RecyclerCollectionView recyclerCollectionView = null;
    private RecyclerAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        adapter = new RecyclerAdapter(this);
        recyclerCollectionView = (RecyclerCollectionView) findViewById(R.id.recyclerCollectionView);
        recyclerCollectionView.setAdapter(adapter);
    }
}
