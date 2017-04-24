package com.chris.recycler.collectionview.constants;

/**
 * Created by chris on 16/9/6.
 */
public class ViewType {

    /******************************************************************************************
     * Non-meaningful
     ******************************************************************************************/
    public static final int NONE = 0x0;

    /******************************************************************************************
     * RecyclerCollectionView's header & footer
     ******************************************************************************************/
//    public static final int VIEW_HEADER = 0x100;
//    public static final int VIEW_FOOTER = 0x101;
    public static final int VIEW_HEADER_REFRESH = 0x102;
    public static final int VIEW_FOOTER_REFRESH = 0x103;

    /******************************************************************************************
     * Section's header, item, footer
     ******************************************************************************************/
    public static final int SECTION_HEADER = 0x1;
    public static final int SECTION_ITEM = 0x2;
    public static final int SECTION_FOOTER = 0x3;
}
