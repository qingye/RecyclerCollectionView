package com.chris.recycler.collectionview.structure;

/**
 * Created by chris on 16/9/5.
 */
public class Point {

    public int x = 0;
    public int y = 0;

    public Point() {
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void reset() {
        x = 0;
        y = 0;
    }
}
