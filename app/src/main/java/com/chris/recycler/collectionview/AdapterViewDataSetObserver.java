package com.chris.recycler.collectionview;

import android.database.DataSetObserver;
import android.util.Log;

/**
 * Created by chris on 16/9/1.
 */
public class AdapterViewDataSetObserver extends DataSetObserver {

    @Override
    public void onChanged() {
        Log.i("RecyclerCollectionView", "onChanged");
    }

    @Override
    public void onInvalidated() {
        Log.i("RecyclerCollectionView", "onInvalidated");
    }
}
