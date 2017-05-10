package com.chris.recycler.collectionview.structure;

import com.chris.recycler.collectionview.constants.ViewType;

/**
 * Created by chris on 16/9/13.
 */
public class SectionPath {

    public int sectionType = ViewType.NONE;
    public IndexPath indexPath = new IndexPath();

    public SectionPath() {
    }

    public SectionPath(int sectionType) {
        this.sectionType = sectionType;
    }

    public SectionPath(int sectionType, IndexPath indexPath) {
        setSectionType(sectionType);
        setIndexPath(indexPath);
    }

    public SectionPath(SectionPath sectionPath) {
        setSectionType(sectionPath.sectionType);
        setIndexPath(sectionPath.indexPath);
    }

    public int getSectionType() {
        return sectionType;
    }

    public void setSectionType(int sectionType) {
        this.sectionType = sectionType;
    }

    public IndexPath getIndexPath() {
        return indexPath;
    }

    public void setIndexPath(IndexPath indexPath) {
        this.indexPath = new IndexPath(indexPath);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (this == o) {
            return true;
        }

        if (o instanceof SectionPath) {
            SectionPath sp = (SectionPath) o;
            if (this.sectionType == sp.sectionType && this.indexPath.equals(sp.getIndexPath())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (sectionType + 1) * indexPath.hashCode();
    }
}
