package com.chris.recycler.collectionview;

import com.chris.recycler.collectionview.structure.IndexPath;
import com.chris.recycler.collectionview.structure.SectionPath;

/**
 * Created by chris on 16/9/14.
 */
public class Log {

    private final static String TAG = "RecyclerCollectionView";

    public final static void e(String err) {
        android.util.Log.e(TAG, err);
    }

    public final static void e(String func, SectionPath sectionPath) {
        e(String.format("[%s] sectionType = %d, indexPath = (sec=>%d, item=>%d)", func, sectionPath.sectionType, sectionPath.indexPath.section, sectionPath.indexPath.item));
    }

    public final static void e(String func, IndexPath indexPath) {
        e(String.format("[%s] indexPath = (sec=>%d, item=>%d)", func, indexPath.section, indexPath.item));
    }
}
