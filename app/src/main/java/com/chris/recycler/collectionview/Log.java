package com.chris.recycler.collectionview;

/**
 * Created by chris on 16/9/14.
 */
public class Log {

    private final static String TAG = "RecyclerCollectionView";

    public final static void e(String err) {
        android.util.Log.e(TAG, err);
    }

    public final static void e(String func, SectionPath sectionPath) {
        e(String.format("[%s] st = %d, si = (%d, %d)", func, sectionPath.sectionType, sectionPath.indexPath.section, sectionPath.indexPath.item));
    }
}
