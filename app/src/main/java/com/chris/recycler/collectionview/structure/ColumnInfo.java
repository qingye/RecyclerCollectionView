package com.chris.recycler.collectionview.structure;

/**
 * Created by chris on 16/9/18.
 */
public class ColumnInfo {

    public int item = -1;
    public int height = 0;
    public int column = 0;
    public int offset = 0;

    public ColumnInfo() {
    }

    public ColumnInfo(int height) {
        this.height = height;
    }

    public ColumnInfo(int item, int column) {
        this.item = item;
        this.column = column;
    }
}
