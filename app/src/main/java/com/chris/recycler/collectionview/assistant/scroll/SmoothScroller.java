package com.chris.recycler.collectionview.assistant.scroll;

import com.chris.recycler.collectionview.structure.SectionPath;

/**
 * Created by chris on 17/5/18.
 */
public class SmoothScroller {

    public interface CallbackListener {
        public void scrollFinish(SmoothScroller smoothScroller);
    }

    public SectionPath sectionPath = null;
    private CallbackListener callback = null;

    public SmoothScroller(SectionPath sectionPath, CallbackListener callback) {
        this.sectionPath = sectionPath;
        this.callback = callback;
    }

    public void scrollFinish(SmoothScroller smoothScroller) {
        if (callback != null) {
            callback.scrollFinish(smoothScroller);
        }
    }
}
