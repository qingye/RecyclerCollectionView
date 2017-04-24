package com.chris.recycler.collectionview.structure;

import com.chris.recycler.collectionview.constants.ViewType;

/**
 * Created by chris on 16/9/13.
 */
public class SectionPath {

    public int sectionType = ViewType.NONE;
    public IndexPath indexPath = null;

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
}
