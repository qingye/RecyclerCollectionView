package com.chris.recycler.collectionview.structure;

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

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (this == o) {
            return true;
        }

        if (o instanceof IndexPath) {
            IndexPath ip = (IndexPath) o;
            if (this.section == ip.section && this.item == ip.item) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (section + 1) * (item + 1);
    }
}
