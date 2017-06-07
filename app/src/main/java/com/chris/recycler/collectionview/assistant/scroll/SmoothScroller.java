package com.chris.recycler.collectionview.assistant.scroll;

import com.chris.recycler.collectionview.structure.SectionPath;

/**
 * Created by chris on 17/5/18.
 */
public class SmoothScroller {

    public interface CallbackListener {
        public void scrollFinish(SmoothScroller smoothScroller);
    }

    public int position = 0;
    public SectionPath sectionPath = null;
    private CallbackListener callback = null;

    public SmoothScroller(int position, SectionPath sectionPath, CallbackListener callback) {
        this.position = position;
        this.sectionPath = sectionPath;
        this.callback = callback;
    }

    public void scrollFinish(SmoothScroller smoothScroller) {
        if (callback != null) {
            callback.scrollFinish(smoothScroller);
        }
    }
}
