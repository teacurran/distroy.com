package com.approachingpi.search;

import java.util.ArrayList;

/**
 * User: terrence
 * Date: Nov 8, 2004
 * Time: 11:36:25 AM
 */
public interface IResultPage {
    public void addItem(Object in);
    public int getCountTotal();
    public int getDesiredStart();
    public ArrayList getItems();
    public int getEnd();
    public int getPageCount();
    public int getStart();
    public int getDesiredEnd();
    public int getPage();
    public int getPageSize();
    public void setPage(int in);
    public void setCountTotal(int in);
    public void setStart(int in);
    public void setPageSize (int in);
    public void setItems (ArrayList in);
}
