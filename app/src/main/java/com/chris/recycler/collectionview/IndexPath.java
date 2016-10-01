package com.chris.recycler.collectionview;

/**
 * Created by chris on 16/9/2.
 */
public class IndexPath {

    public int section = 0;
    public int item = 0;

    public IndexPath() {
    }

    public IndexPath(int section) {
        this.section = section;
    }

    public IndexPath(int section, int item) {
        this.section = section;
        this.item = item;
    }

    public IndexPath(IndexPath indexPath) {
        this(indexPath.section, indexPath.item);
    }

    public int getSection() {
        return section;
    }

    public void setSection(int section) {
        this.section = section;
    }

    public int getItem() {
        return item;
    }

    public void setItem(int item) {
        this.item = item;
    }
}
