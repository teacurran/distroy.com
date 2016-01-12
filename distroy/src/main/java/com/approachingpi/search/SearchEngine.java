package com.approachingpi.search;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Approaching Pi, Inc.
 * User: Tea Curran
 * Date: Nov 14, 2004
 * Time: 9:37:58 PM
 * Desc: This class should never be instantiated, it doesn't have enough code
 * to work by itself.  It should be extended.
 */
public class SearchEngine implements ISearchEngine {
    public static final int SORT_DEFAULT        = 0;


    public static final int SORT_ORDER_ASC      = 0;
    public static final int SORT_ORDER_DESC     = 1;
    public static final int SORT_ORDER_DEFAULT  = SORT_ORDER_ASC;

    protected Vector sort = new Vector();
    protected int sortOrder = SORT_ORDER_DEFAULT;
    private String whereAnd = " where ";
    private int whereAndCount = 0;

    protected ResultPage rp = null;

    public void addSort(int sortIn) {
        sort.add(new Integer(sortIn));
    }
    
    public ArrayList executeAndReturn(Connection con) {
        return executeAndReturn(con,-1);
    }

    public ArrayList executeAndReturn(Connection con, int max) {
        return new ArrayList();
    }
    public int getFirstSort() {
        if (sort.size() > 0) {
            return ((Integer)sort.get(0)).intValue();
        }
        return SORT_DEFAULT;
    }
    public Vector getSort() { return sort; }
    public int getSortOrder() { return sortOrder; }
    public String getWhereAnd() {
        String returnValue = new String(whereAnd);
        if (whereAndCount == 0) {
            whereAndCount++;
            whereAnd = " AND ";
        }
        //System.out.println("count" + whereAndCount + ":" + returnValue);
        return returnValue;
    }

    public ResultPage getResultPage() {
        if (rp == null) {
            return new ResultPage();
        }
        return this.rp;
    }
    public void resetWhereAnd() {
        whereAnd = " WHERE ";
        whereAndCount = 0;
    }
    public void setResultPage(ResultPage in) {
        rp = in;
    }
    public void setSortOrder(int in) {
        sortOrder = in;
    }
}

